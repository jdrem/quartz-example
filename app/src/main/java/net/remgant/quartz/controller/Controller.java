package net.remgant.quartz.controller;

import lombok.extern.slf4j.Slf4j;
import net.remgant.quartz.scheduler.JobScheduler;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
@Slf4j
public class Controller {

    final private JobScheduler jobScheduler;

    public Controller(JobScheduler jobScheduler) {
        this.jobScheduler = jobScheduler;
    }

    @RequestMapping(value = "/schedules", method = RequestMethod.GET)
    public ResponseEntity<Map<String,Object>> schedules() {
        Map<String,Object> result = jobScheduler.listAllSchedules();
        return ResponseEntity.ok(result);
    }
    @RequestMapping(value="/schedule/event", method = RequestMethod.POST)
    public ResponseEntity<Map<String,Object>> scheduleEvent(@RequestBody Map<String,Object> event) {
        Map<String,Object> result = jobScheduler.scheduleJob(event);
        return ResponseEntity.ok(result);
    }

    @ResponseStatus(code = HttpStatus.NOT_FOUND)
    private static class NotFoundException extends RuntimeException {}

    @RequestMapping(value = "/schedule/event/{jobKey}", method = RequestMethod.DELETE)
    // Need to explicitly declare path variable name due to https://youtrack.jetbrains.com/issue/IDEA-339211
    public ResponseEntity<Map<String,Object>> deleteEvent(@PathVariable("jobKey") String jobKey) {
        if (jobScheduler.deleteJob(jobKey))
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        else
            throw new NotFoundException();
    }
}

