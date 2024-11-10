package cn.mj.thrift.test;

import org.apache.commons.pool2.impl.BaseGenericObjectPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public abstract class MonitorablePool implements NamedPool{
    private final Logger logger = LoggerFactory.getLogger(MonitorablePool.class);
    public abstract BaseGenericObjectPool<?> getPool();
    private Boolean hasMonitor = false;
    private MatrixInfo preMatrixInfo = null;
    @SafeVarargs
    public final synchronized void monitor(final Duration interval,final Consumer<MatrixInfo>... consumer) {
        if (hasMonitor){
            return;
        }
        if (interval == null){
            throw new NullPointerException("interval can not be null");
        }
        hasMonitor = true;
        Runnable task = () -> {
            MatrixInfo matrixInfo = getMatrixInfo();
            if (preMatrixInfo != null){
                matrixInfo = matrixInfo.diff(preMatrixInfo);
            }else{
                preMatrixInfo = matrixInfo;
            }
            logger.info(matrixInfo.toString());
            if (consumer != null){
                for (Consumer<MatrixInfo> matrixInfoConsumer : consumer) {
                    try{
                        matrixInfoConsumer.accept(matrixInfo);
                    }catch (Exception e){
                        logger.error("monitor consumer failed: ",e);
                    }
                }
            }
        };
        try(ScheduledThreadPoolExecutor scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(1)){
            scheduledThreadPoolExecutor.schedule(task,interval.toMillis(), TimeUnit.MILLISECONDS);
        }catch (Exception e){
            logger.error("monitor schedule failed: ",e);
        }

    }

    private MatrixInfo getMatrixInfo() {
        BaseGenericObjectPool<?> pool = getPool();
        return new MatrixInfo(getName(),pool);
    }

    public static class MatrixInfo{
        private final String name;
        private Long borrowCount;
        private Long createdCount;
        private Long idleCount;
        private Long returnedCount;
        private Long destroyedByBorrowValidationCount;
        private Long destroyedByEvictorCount;
        private Long destroyedCount;
        private String evictionPolicyClassName;
        private Long maxTotal;
        private Long meanActiveTimeMillis;
        private Long meanBorrowWaitTimeMillis;
        private Long meanIdleTimeMillis;

        public MatrixInfo(String name,BaseGenericObjectPool<?> pool){
            this.name = name;
            this.borrowCount = pool.getBorrowedCount();
            this.createdCount = pool.getCreatedCount();
            this.idleCount = Long.valueOf(String.valueOf(pool.getNumIdle()));
            this.returnedCount = pool.getReturnedCount();
            this.destroyedByBorrowValidationCount = pool.getDestroyedByBorrowValidationCount();
            this.destroyedByEvictorCount = pool.getDestroyedByEvictorCount();
            this.destroyedCount = pool.getDestroyedCount();
            this.evictionPolicyClassName = pool.getEvictionPolicyClassName();
            this.maxTotal = Long.valueOf(String.valueOf(pool.getMaxTotal()));
            this.meanActiveTimeMillis = pool.getMeanActiveTimeMillis();
            this.meanBorrowWaitTimeMillis = pool.getMeanBorrowWaitTimeMillis();
            this.meanIdleTimeMillis = pool.getMeanIdleTimeMillis();
        }
        private MatrixInfo(String name){
            this.name = name;
        }
        public MatrixInfo diff(MatrixInfo before){
            MatrixInfo diffed = new MatrixInfo(name);
            diffed.borrowCount = this.borrowCount - before.borrowCount;
            diffed.createdCount = this.createdCount - before.createdCount;
            diffed.idleCount = this.idleCount - before.idleCount;
            diffed.returnedCount = this.returnedCount - before.returnedCount;
            diffed.destroyedByBorrowValidationCount = this.destroyedByBorrowValidationCount - before.destroyedByBorrowValidationCount;
            diffed.destroyedByEvictorCount = this.destroyedByEvictorCount - before.destroyedByEvictorCount;
            diffed.destroyedCount = this.destroyedCount - before.destroyedCount;
            diffed.evictionPolicyClassName = this.evictionPolicyClassName;
            diffed.meanActiveTimeMillis = this.meanActiveTimeMillis - before.meanActiveTimeMillis;
            diffed.meanBorrowWaitTimeMillis = this.meanBorrowWaitTimeMillis - before.meanBorrowWaitTimeMillis;
            diffed.meanIdleTimeMillis = this.meanIdleTimeMillis - before.meanIdleTimeMillis;
            diffed.maxTotal = this.maxTotal;
            return diffed;
        }
        public String getName() {
            return name;
        }

        public Long getBorrowCount() {
            return borrowCount;
        }

        public Long getCreatedCount() {
            return createdCount;
        }

        public Long getIdleCount() {
            return idleCount;
        }

        public Long getReturnedCount() {
            return returnedCount;
        }

        public Long getDestroyedByBorrowValidationCount() {
            return destroyedByBorrowValidationCount;
        }

        public Long getDestroyedByEvictorCount() {
            return destroyedByEvictorCount;
        }

        public Long getDestroyedCount() {
            return destroyedCount;
        }

        public String getEvictionPolicyClassName() {
            return evictionPolicyClassName;
        }

        public Long getMaxTotal() {
            return maxTotal;
        }

        public Long getMeanActiveTimeMillis() {
            return meanActiveTimeMillis;
        }

        public Long getMeanBorrowWaitTimeMillis() {
            return meanBorrowWaitTimeMillis;
        }

        public Long getMeanIdleTimeMillis() {
            return meanIdleTimeMillis;
        }

        @Override
        public String toString() {
            return "MatrixInfo{" +
                    "name='" + name + '\'' +
                    ", borrowCount=" + borrowCount +
                    ", createdCount=" + createdCount +
                    ", idleCount=" + idleCount +
                    ", returnedCount=" + returnedCount +
                    ", destroyedByBorrowValidationCount=" + destroyedByBorrowValidationCount +
                    ", destroyedByEvictorCount=" + destroyedByEvictorCount +
                    ", destroyedCount=" + destroyedCount +
                    ", evictionPolicyClassName='" + evictionPolicyClassName + '\'' +
                    ", maxTotal=" + maxTotal +
                    ", meanActiveTimeMillis=" + meanActiveTimeMillis +
                    ", meanBorrowWaitTimeMillis=" + meanBorrowWaitTimeMillis +
                    ", meanIdleTimeMillis=" + meanIdleTimeMillis +
                    '}';
        }
    }
}
