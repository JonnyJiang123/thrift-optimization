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