//package web.car_system.Car_Service.config;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.core.task.AsyncTaskExecutor;
//import org.springframework.core.task.TaskExecutor;
//import org.springframework.core.task.support.TaskExecutorAdapter;
//import org.springframework.scheduling.annotation.EnableAsync;
//import org.springframework.security.task.DelegatingSecurityContextAsyncTaskExecutor;
//
//import java.util.concurrent.Executor;
//import java.util.concurrent.Executors;
//
//@Configuration
//@EnableAsync
//public class AsyncConfig {
//
//    @Bean(name = "asyncExecutor")
//    public Executor asyncExecutor() {
//        // Sử dụng VirtualThreadPerTaskExecutor để tận dụng Virtual Threads
//        Executor virtualThreadExecutor = Executors.newVirtualThreadPerTaskExecutor();
//
//        // Wrap với DelegatingSecurityContextAsyncTaskExecutor để đảm bảo Spring Security context
//        return new DelegatingSecurityContextAsyncTaskExecutor((AsyncTaskExecutor) virtualThreadExecutor);
//    }
//}
