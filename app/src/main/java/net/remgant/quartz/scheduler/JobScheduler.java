package net.remgant.quartz.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

@Component
@Slf4j
public class JobScheduler {

    final private Scheduler scheduler;

    public JobScheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    public Map<String,Object> scheduleJob(Map<String,Object> map) {
        ZonedDateTime triggerDateTime = DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse((String)map
                        .getOrDefault("startDate", DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(ZonedDateTime.now())),
                ZonedDateTime::from);
        JobDataMap jobDataMap = map.entrySet().stream().collect(JobDataMap::new, (m, es) -> m.put(es.getKey(), es.getValue()), JobDataMap::putAll);
        String id = UUID.randomUUID().toString();
        JobDetail job = newJob(SimpleJob.class)
                .withIdentity("J"+id)
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
        log.info("job scheduled for {}, key {}, group {}", triggerDateTime, job.getKey().getName(), job.getKey().getGroup());
        return Map.of("name", job.getKey().getName(), "group", job.getKey().getGroup());
    }

    public Map<String, Object> listAllSchedules() {
        Set<JobKey> jobKeySet;
        try {
            jobKeySet = scheduler.getJobKeys(GroupMatcher.anyGroup());
        } catch (SchedulerException e) {
            throw new RuntimeException(e);
        }
        @SuppressWarnings("unchecked")
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
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
        return Map.of("results", resultList);
    }

    public boolean deleteJob(String jobKey) {
        try {
            if (scheduler.deleteJob(new JobKey(jobKey, "DEFAULT"))) {
                log.info("Job {} deleted", jobKey);
                return true;
            } else {
                log.info("Job {} not deleted, not found", jobKey);
                return false;
            }
        } catch (SchedulerException e) {
            log.error("Deleting job {}", jobKey, e);
            throw new RuntimeException(e);
        }
    }

    @Slf4j
    static public class SimpleJob implements Job {

        @SuppressWarnings("RedundantThrows")
        @Override
        public void execute(JobExecutionContext context) throws JobExecutionException {
            log.info("Running job {}", context.getJobDetail().getKey());
            context.getMergedJobDataMap().forEach((key, value) -> log.info("Key: {}, value: {}", key, value));
        }
    }
}
