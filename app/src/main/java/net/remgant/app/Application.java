package net.remgant.app;

import net.remgant.secheduling.JobScheduler;
import org.quartz.Scheduler;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public JobScheduler jobScheduler(Scheduler scheduler) {
        return new JobScheduler(scheduler);
    }
}
