package net.remgant.quartz.scheduler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import net.remgant.quartz.DeactivateAccountEvent;
import net.remgant.quartz.DeactivateDeviceEvent;
import net.remgant.quartz.DoSomethingService;
import net.remgant.quartz.Event;
import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

@Service
@Slf4j
public class EventSchedulerImpl implements EventScheduler {

    final private Scheduler scheduler;
    final private ObjectMapper objectMapper;

    public EventSchedulerImpl(Scheduler scheduler, ObjectMapper objectMapper) {
        this.scheduler = scheduler;
        this.objectMapper = objectMapper;
    }

    @Override
    public String scheduleEvent(Event event) {
        Instant triggerDateTime = DateTimeFormatter.ISO_INSTANT.parse(event.getTriggerTime(), Instant::from);
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put("OBJECT_CLASS_NAME", event.getClass().getName());
        try {
            jobDataMap.put("OBJECT_DATA", objectMapper.writeValueAsString(event));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        String id = UUID.randomUUID().toString();
        JobDetail job = newJob(EventJob.class)
                .withIdentity(id)
                .usingJobData(jobDataMap)
                .build();
        Trigger trigger = newTrigger()
                .withIdentity(id)
                .startAt(Date.from(triggerDateTime))
                .build();
        try {
            scheduler.scheduleJob(job, trigger);
        } catch (SchedulerException e) {
            log.error("Error scheduling", e);
            throw new RuntimeException(e);
        }
        log.info("job scheduled for {}, key {}, group {}", triggerDateTime, job.getKey().getName(), job.getKey().getGroup());
        return id;
    }

    @Override
    public boolean deleteEvent(String eventId) {
        try {
            if (scheduler.deleteJob(new JobKey(eventId, "DEFAULT"))) {
                log.info("Job {} deleted", eventId);
                return true;
            } else {
                log.info("Job {} not deleted, not found", eventId);
                return false;
            }
        } catch (SchedulerException e) {
            log.error("Deleting job {}", eventId, e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<EventDetails> listAllEvents() {
        Set<JobKey> jobKeySet;
        try {
            jobKeySet = scheduler.getJobKeys(GroupMatcher.anyGroup());
        } catch (SchedulerException e) {
            throw new RuntimeException(e);
        }
        @SuppressWarnings("unchecked")
        List<EventDetails> resultList = (List<EventDetails>) jobKeySet.stream().map(jk -> {
                    try {
                        JobDetail jobDetail = scheduler.getJobDetail(jk);
                        JobDataMap jobDataMap = scheduler.getJobDetail(jk).getJobDataMap();
                        String objectClassName = jobDataMap.get("OBJECT_CLASS_NAME").toString();
                        String objectData = jobDataMap.get("OBJECT_DATA").toString();
                        @SuppressWarnings("unchecked")
                        Map<String, Object> objectAsMap = objectMapper.readValue(objectData, Map.class);
                        @SuppressWarnings("unchecked")
                        List<Trigger> triggerList = (List<Trigger>) scheduler.getTriggersOfJob(jobDetail.getKey());
                        String nextFireTime = DateTimeFormatter.ISO_INSTANT.format(triggerList.get(0).getNextFireTime().toInstant());
                        LocalDateTime triggerTime = triggerList.get(0).getNextFireTime().toInstant()
                                .atZone(ZoneOffset.UTC)
                                .toLocalDateTime();
                        log.info("Job Name: {}, Next Fire Time: {}", jk.getName(), nextFireTime);
                        EventDetails eventDetails = new EventDetails(jk.getName(), triggerTime, objectClassName, objectAsMap);
                        return Optional.of(eventDetails);
                    } catch (Exception e) {
                        log.warn("getting job details for {}", jk.getName(), e);
                    }
                    return Optional.empty();
                })
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
        return resultList;
    }

    public static class EventJob extends QuartzJobBean {

        ObjectMapper objectMapper = new ObjectMapper();

        @Override
        protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
            ApplicationContext applicationContext;
            try {
                applicationContext = (ApplicationContext) context.getScheduler().getContext().get("applicationContext");
            } catch (SchedulerException e) {
                throw new JobExecutionException(e);
            }
            log.info("Running job {}", context.getJobDetail().getKey());
            DoSomethingService doSomethingService = applicationContext.getBean(DoSomethingService.class);
            JobDataMap jobDataMap = context.getMergedJobDataMap();
            String eventClassName = jobDataMap.get("OBJECT_CLASS_NAME").toString();
            if (eventClassName.equals(DeactivateDeviceEvent.class.getName())) {
                DeactivateDeviceEvent deactivateDeviceEvent;
                try {
                    deactivateDeviceEvent = objectMapper.readValue(jobDataMap.get("OBJECT_DATA").toString(), DeactivateDeviceEvent.class);
                } catch (JsonProcessingException e) {
                    throw new JobExecutionException(e);
                }
                doSomethingService.deactivateDevice(deactivateDeviceEvent.getDeviceId());
            } else if (eventClassName.equals(DeactivateAccountEvent.class.getName())) {
                DeactivateAccountEvent deactivateAccountEvent;
                try {
                    deactivateAccountEvent = objectMapper.readValue(jobDataMap.get("OBJECT_DATA").toString(), DeactivateAccountEvent.class);
                } catch (JsonProcessingException e) {
                    throw new JobExecutionException(e);
                }
                doSomethingService.deactivateAccount(deactivateAccountEvent.getAccountId());
            }
        }
    }
}
