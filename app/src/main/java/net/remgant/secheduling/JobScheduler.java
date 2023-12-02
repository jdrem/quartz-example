package net.remgant.secheduling;

import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

@Component
@Slf4j
public class JobScheduler {

    private static final Random random = new Random();
    Supplier<String> randomString = () -> random.ints(97, 122)
            .limit(8)
            .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
            .toString();

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
        String id = randomString.get();
        JobDetail job = newJob(SimpleJob.class)
                .withIdentity("J"+id, "Event Group")
                .usingJobData(jobDataMap)
                .build();
        Trigger trigger = newTrigger()
                .withIdentity("T"+id, "group1")
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

    public Map<String, Object> listAllSchedules() {
        Set<JobKey> jobKeySet = null;
        try {
            jobKeySet = scheduler.getJobKeys(GroupMatcher.anyGroup());
        } catch (SchedulerException e) {
            throw new RuntimeException(e);
        }
        List<Map<String,Object>> resultList = (List<Map<String, Object>>) jobKeySet.stream().map(jk -> {
            try {
                JobDetail jobDetail = scheduler.getJobDetail(jk);
                log.info("Job Name: {}", jk.getName());
                @SuppressWarnings("unchecked")
                List<Trigger> triggerList = (List<Trigger>) scheduler.getTriggersOfJob(jobDetail.getKey());
                String nextFireTime = DateTimeFormatter.ISO_INSTANT.format(triggerList.get(0).getNextFireTime().toInstant());
                log.info("Trigger: {}", nextFireTime);
                return Optional.of(Map.of("jobName", jk.getName(), "nextFireTime", nextFireTime));
            } catch (Exception e) {
                log.warn("getting job details", e);
            }
            return Optional.empty();
        })
                .filter(o -> o.isPresent())
                .map(o -> o.get())
                .collect(Collectors.toList());
        return Map.of("results", resultList);
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
