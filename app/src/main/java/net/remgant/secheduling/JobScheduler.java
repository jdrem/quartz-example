package net.remgant.secheduling;

import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Map;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

@Component
@Slf4j
public class JobScheduler {

    Scheduler scheduler;
    public JobScheduler() {
        try {
            scheduler = StdSchedulerFactory.getDefaultScheduler();
            scheduler.start();
            log.info("Scheculer started");
        } catch (SchedulerException e) {
            log.error("error starting scheduler", e);
            throw new RuntimeException(e);
        }
    }
    public void scheduleJob(Map<String,Object> map) {
        ZonedDateTime triggerDateTime = DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse((String)map
                        .getOrDefault("startDate", DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(ZonedDateTime.now())),
                ZonedDateTime::from);
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put("eventMap", map);
        JobDetail job = newJob(SimpleJob.class)
                .withIdentity("simple job", "group1")
                .usingJobData(jobDataMap)
                .build();
        Trigger trigger = newTrigger()
                .withIdentity("simple Trigger", "group1")
                .startAt(Date.from(triggerDateTime.toInstant()))
                .build();
        try {
            scheduler.scheduleJob(job, trigger);
        } catch (SchedulerException e) {
            log.error("Error scheduling", e);
            throw new RuntimeException(e);
        }
        log.info("job scheduled for {}", triggerDateTime);
    }

    @Slf4j
    static public class SimpleJob implements Job {

        @SuppressWarnings("RedundantThrows")
        @Override
        public void execute(JobExecutionContext context) throws JobExecutionException {
            log.info("Running job {}", context.getJobDetail().getKey());
            @SuppressWarnings("unchecked")
            Map<String,Object> data = (Map<String, Object>) context.getMergedJobDataMap().getOrDefault("eventMap", Map.of());
            data.forEach((key, value) -> log.info("Key: {}, value: {}", key, value));
        }
    }
}
