package net.remgant.app;

import lombok.extern.slf4j.Slf4j;
import net.remgant.secheduling.JobScheduler;
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
        jobScheduler.scheduleJob(event);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

}

