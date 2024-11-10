# 背景

Thrift是一个轻量级的、独立于语言的软件栈，用于点对点PCC实现。Thrift为数据传输、数据序列化和应用程序级处理提供了清晰的抽象和实现。代码生成系统采用简单的定义语言作为输入，并生成跨编程语言的代码，这些语言使用抽象堆栈来构建可互操作的PRC客户端和服务器。

在使用thrift的时候大多数人都是直接按照官网Demo来搞的，这张Demo在大多数情况下是没有问题的，但是当在高并发场景下就会有性能问题产生。

本文通过对Thrift客户端从：每次**new Socket**、**异步thrift**、**共享连接池**、**响应式模式** 一步一步优化thrift的使用，并每次进行压测对比。

# 环境

1. Jdk版本

   ```shell
   java version "21.0.5" 2024-10-15 LTS
   Java(TM) SE Runtime Environment (build 21.0.5+9-LTS-239)
   Java HotSpot(TM) 64-Bit Server VM (build 21.0.5+9-LTS-239, mixed mode, sharing)
   ```

2. SpringBoot版本：3.3.5

# thrift实现

```thrift
namespace java hello

struct HelloResponse{
    1: string name;
    2: i32 age;
    3: string message;
    4: i64 timestamp;
}
struct HelloRequest{
    1: string name;
    2: string message;
}

service HelloService {
    HelloResponse sayHello(1:HelloRequest req);
}
```



# 服务端

## 启动类

```java
@SpringBootApplication
public class ServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServerApplication.class, args);
    }

}

```

## thrift服务实现（异步模式）

```java
@Slf4j
@Service("helloService")
public class HelloServiceImpl implements HelloService.AsyncIface {
    @Resource
    private ObjectMapper objectMapper;

    @Override
    public void sayHello(HelloRequest req, AsyncMethodCallback<HelloResponse> resultHandler) throws TException {
        HelloResponse helloResponse = new HelloResponse();
        try{
            var json = objectMapper.writeValueAsString(req);
//            log.info("sayHello: {}", json);
            helloResponse.setAge(19);
            helloResponse.setName(req.getName());
            helloResponse.setMessage("hello, " + req.getName());
            helloResponse.setTimestamp(System.currentTimeMillis());
        }catch (Exception e){
            log.error("sayHello failed: ", e);
            helloResponse.setMessage("error, " +e.getMessage());
        }
        // 通过handler来设置返回值
        resultHandler.onComplete(helloResponse);
    }
}

```

## 启动thrift服务

1. 创建非阻塞`TNonblockingServerSocket`
2. 参见Server并指定对用的服务提供者（processor）
3. 提供服务

```java
@Slf4j
@Component
public class ThriftServer {
    @Resource
    HelloService.AsyncIface helloService;
    @PostConstruct
    public void init() throws TTransportException {
        log.info("ThriftServer init");
        new Thread(()->{
            try(TNonblockingServerSocket serverTransport = new TNonblockingServerSocket(9999)){
                TServer server = new TNonblockingServer(new TNonblockingServer.Args(serverTransport)
                        .processor(new HelloService.AsyncProcessor<>(helloService)));
                server.serve();
            }catch (Exception e){
                log.error("init failed: ", e);
            }

        }).start();
        log.info("ThriftServer init done");
    }
}

```

# 客户端

## 启动类

```java
@SpringBootApplication
public class ClientApplication {

    public static void main(String[] args) {
        SpringApplication.run(ClientApplication.class, args);
    }

}

```

## Controller

```java
@RestController
public class HelloController {

    @Resource(name = "nonblockService")
    HelloService helloService;

    @GetMapping("/hello")
    public HelloResponse hello() {
        return helloService.hello();
    }
}

```

## 公共Service

```java
public interface HelloService {
    HelloResponse hello();

    default HelloRequest getRequest(){
        HelloRequest helloRequest = new HelloRequest();
        helloRequest.setName("Jonny");
        helloRequest.setMessage("good night");
        return helloRequest;
    }
}

```

## 每次创建新Socket

### 同步阻塞模式

```java
@Slf4j
@Service("blockService")
public class HelloServiceBlock implements HelloService{
    @Override
    public HelloResponse hello() {
        HelloResponse helloResponse = new HelloResponse();
        try(TTransport transport = new TSocket("127.0.0.1",9999)){
            TFramedTransport framedTransport = new TFramedTransport(transport);
            TProtocol protocol = new TBinaryProtocol(framedTransport);
            framedTransport.open();
            cn.mj.thrift.test.HelloService.Client client = new cn.mj.thrift.test.HelloService.Client(protocol);
            HelloResponse response = client.sayHello(getRequest());
            framedTransport.close();
            return response;
        }catch (Exception e){
            log.error("hello failed: ", e);
            helloResponse.setMessage("error, " +e.getMessage());
        }
        return helloResponse;
    }
}

```

结论：

1. 通过jmeter压测QPS最终到1500左右
2. 当请求多起来的时候在创建链接`TFramedTransport::open`会报：`Address already in use: connect`异常
3. CPU、内存使用正常

### 同步非阻塞模式 （服务器情况下不适合）

#### 直接通过CompletableFuture

```java
@Slf4j
@Service("nonblockService")
public class HelloServiceNonBlock implements HelloService {
    @Override
    public HelloResponse hello() {
        HelloResponse helloResponse = new HelloResponse();

        try(TNonblockingSocket socket = new TNonblockingSocket("127.0.0.1",9999)){
            TAsyncClientManager clientManager = new TAsyncClientManager();
            TProtocolFactory protocolFactory = new TBinaryProtocol.Factory();
            cn.mj.thrift.test.HelloService.AsyncClient client = new cn.mj.thrift.test.HelloService.AsyncClient(protocolFactory,clientManager,socket);
            CompletableFuture<HelloResponse> future = new CompletableFuture<>();
            client.sayHello(getRequest(), new AsyncMethodCallback<>() {
                @Override
                public void onComplete(HelloResponse response) {
                    future.complete(response);
                }
                @Override
                public void onError(Exception exception) {
                    future.completeExceptionally(exception);
                }
            });
            return future.get();
        }catch (Exception e){
            log.error("hello failed: ", e);
            helloResponse.setMessage("error, " +e.getMessage());
        }
        return helloResponse;
    }
}
```

结论：

1. 通过jmeter压测QPS巅峰1.7w，最终5.2k左右。但是异常率80%
2. 线程数直接涨到3w多
3. 报错：
   1.  `No buffer space available (maximum connections reached?)` : 每个线程建立一个链接
   2. ` Address already in use`
4. 直接通过`new CompletableFuture`后进行get时会阻塞挂起当前线程。当并发起来后，会无限创建线程，最终创建线程过多异常。如果使用虚拟线程，会直接被卡死（不要尝试）。

### 响应式模式

```java
@Slf4j
@Service("reactiveService")
public class HelloServiceReactive implements HelloService{
    @Override
    public Mono<HelloResponse> hello() {
        HelloResponse helloResponse = new HelloResponse();
        try(TTransport transport = new TSocket("127.0.0.1",9999)){
            TFramedTransport framedTransport = new TFramedTransport(transport);
            TProtocol protocol = new TBinaryProtocol(framedTransport);
            framedTransport.open();
            cn.mj.thrift.test.HelloService.Client client = new cn.mj.thrift.test.HelloService.Client(protocol);
            HelloResponse response = client.sayHello(getRequest());
            framedTransport.close();
            return Mono.just(response);
        }catch (Exception e){
            log.error("hello failed: ", e);
            helloResponse.setMessage("error, " +e.getMessage());
        }
        return Mono.just(helloResponse);
    }
}

```

结论：

并发上来后直接就报`Address already in use`

## 共享连接池

### 连接池实现

基于Commons-pool实现

#### 依赖

```xml
            <dependency>
                <groupId>org.apache.commons</groupId>
                <artifactId>commons-pool2</artifactId>
                <version>2.12.0</version>
            </dependency>

```

1. 先定义PooledProjectFactory
   1. create用于创建实例的方法
   2. wrap用于将实例包装为PooledObject
   3. validateObject用于在获取实例的时候验证实例是否有效
   4. destroyObject用于在清除对象时候做的事情
2. 再创建对应的ObjectPool

#### Socket连接池实现

1. ProtocolFactory实现

   ```java
   public class ProtocolFactory extends BasePooledObjectFactory<TProtocol> {
       @Override
       public TProtocol create() throws Exception {
           TTransport transport = new TSocket("127.0.0.1",9999);
           TFramedTransport framedTransport = new TFramedTransport(transport);
           TProtocol protocol = new TBinaryProtocol(framedTransport);
           framedTransport.open();
           return protocol;
       }
   
       @Override
       public PooledObject<TProtocol> wrap(TProtocol tProtocol) {
           return new DefaultPooledObject<>(tProtocol);
       }
   
       @Override
       public boolean validateObject(PooledObject<TProtocol> p) {
           return p.getObject().getTransport().isOpen();
       }
   
       @Override
       public void destroyObject(PooledObject<TProtocol> p) throws Exception {
           p.getObject().getTransport().close();
       }
   
   }
   ```

2. ProtocolPool实现（通过优化GenericObjectPoolConfig对象池配置来提高整体性能）

   ```java
   public class ProtocolPool {
       private final GenericObjectPool<TProtocol> pool;
   
       public ProtocolPool(ProtocolFactory factory) {
           GenericObjectPoolConfig<TProtocol> config = new GenericObjectPoolConfig<>();
           // 配置对象池最多存在多少对象（优化点）
           config.setMaxTotal(3096);
           // 配置对象池空闲可以放多少对象（优化点）
           config.setMaxIdle(2048);
           this.pool = new GenericObjectPool<>(factory,config);
       }
   
       public TProtocol getProtocol() throws Exception {
           return pool.borrowObject();
       }
   
       public void returnProtocol(TProtocol protocol) throws Exception {
           pool.returnObject(protocol);
       }
   
   }
   ```

#### thrift客户端池实现

1. ClientFactory实现（基于Socket连接池）

   ```java
   public class ClientFactory extends BasePooledObjectFactory<HelloService.Client> {
       private TProtocol protocol;
       private final ProtocolPool protocolPool;
       public ClientFactory(ProtocolPool protocolPool) throws Exception {
           this.protocolPool = protocolPool;
       }
   
       @Override
       public HelloService.Client create() throws Exception {
           // 获取Protocol
           this.protocol = protocolPool.getProtocol();
           // 创建Client
           return  new cn.mj.thrift.test.HelloService.Client(this.protocol);
       }
   
       @Override
       public PooledObject<HelloService.Client> wrap(HelloService.Client obj) {
           return new DefaultPooledObject<>(obj);
       }
   
       @Override
       public boolean validateObject(PooledObject<HelloService.Client> p) {
           // 直接验证Protocol，如果验证失败就重新调用create创建对象
           return protocol.getTransport().isOpen();
       }
   }
   ```

   

 #### 应用初始化池

```java
    @Bean
    public ProtocolFactory protocolFactory() {
        return new ProtocolFactory();
    }
    @Bean
    public ProtocolPool protocolPool(ProtocolFactory protocolFactory) {
        return new ProtocolPool(protocolFactory);
    }
    @Bean
    public ClientFactory clientFactory(ProtocolPool protocolPool) throws Exception {
        return new ClientFactory(protocolPool);
    }
    @Bean
    public ClientPool clientPool(ClientFactory clientFactory) throws Exception {
        return new ClientPool(clientFactory);
    }
```



### 同步阻塞模式

Service实现：

```java
@Slf4j
@Service("blockService")
public class HelloServiceBlock implements HelloService{

    private final cn.mj.thrift.test.HelloService.Client client;
    private final ClientPool clientPool;

    public HelloServiceBlock(ClientPool clientPool) throws Exception {
        this.clientPool = clientPool;
        this.client = clientPool.getClient();
    }

    @Override
    public HelloResponse hello() throws Exception {
        // 从客户端连接池获取客户端
        cn.mj.thrift.test.HelloService.Client client = clientPool.getClient();
        try{
            return client.sayHello(getRequest());
        }catch (Exception e){
            log.error("hello failed: ", e);
            throw e;
        }finally {
            // 归还客户端
            clientPool.returnClient(client);
        }
    }
}

```

结论：

1. 通过jmeter压测，QPS逐渐稳定到6K左右
2. 没用任何异常
3. 内存、CPU正常

### 同步非阻塞模式

### 响应式模式

```java
@Slf4j
@Service("reactiveService")
public class HelloServiceReactive implements HelloService{
    private final cn.mj.thrift.test.HelloService.Client client;
    private final ClientPool clientPool;

    public HelloServiceReactive(ClientPool clientPool) throws Exception {
        this.clientPool = clientPool;
        this.client = clientPool.getClient();
    }
    @Override
    public Mono<HelloResponse> hello() {
        return Mono.fromFuture(CompletableFuture.supplyAsync(()-> {
            try {
                return clientPool.getClient();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).thenApply(client->{
            try {
                return client.sayHello(getRequest());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }finally {
                try {
                    clientPool.returnClient(client);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }));
    }
}
```

结论：

1. 通过jmeter压测，QPS最终稳定到10K
2. 没用任何异常
3. CPU略高

# 结论

1. 性能最好的为：响应式+共享连接池实现。如果需要应用，需要根据具体场景、公司技术栈来进行调整
2. 在正常Tomcat下，通过共享连接池就可以实现性能翻几倍
3. 在共享连接池的情况下，还可以优化对象池配置来进一步根据具体的业务场景优化
4. 在公司实施的时候，如果有调用下游接口需要考虑下游是否能够承受住

# 代码位置

