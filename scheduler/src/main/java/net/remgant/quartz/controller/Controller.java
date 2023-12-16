package net.remgant.quartz.controller;

import lombok.extern.slf4j.Slf4j;
import net.remgant.quartz.DeactivateAccountEvent;
import net.remgant.quartz.DeactivateDeviceEvent;
import net.remgant.quartz.scheduler.EventDetails;
import net.remgant.quartz.scheduler.EventScheduler;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@Slf4j
public class Controller {

    final private EventScheduler eventScheduler;

    public Controller(EventScheduler eventScheduler) {
        this.eventScheduler = eventScheduler;
    }

    @RequestMapping(value="/schedule/deactivate/device", method = RequestMethod.POST)
    public ResponseEntity<?> deactivateDevice(@RequestBody DeactivateDeviceEvent deactivateDeviceEvent) {
        String result = eventScheduler.scheduleEvent(deactivateDeviceEvent);
        return ResponseEntity.ok(result);
    }

    @RequestMapping(value="/schedule/deactivate/account", method = RequestMethod.POST)
    public ResponseEntity<?> deactivateDevice(@RequestBody DeactivateAccountEvent deactivateAccountEvent) {
        String result = eventScheduler.scheduleEvent(deactivateAccountEvent);
        return ResponseEntity.ok(result);
    }
    @ResponseStatus(code = HttpStatus.NOT_FOUND)
    private static class NotFoundException extends RuntimeException {}

    @RequestMapping(value = "/schedule/event/{jobKey}", method = RequestMethod.DELETE)
    // Need to explicitly declare path variable name due to https://youtrack.jetbrains.com/issue/IDEA-339211
    public ResponseEntity<Map<String,Object>> deleteEvent(@PathVariable("jobKey") String jobKey) {
        if (eventScheduler.deleteEvent(jobKey))
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        else
            throw new NotFoundException();
    }

    @RequestMapping(value = "/schedule/events", method = RequestMethod.GET)
    public ResponseEntity<List<EventDetails>> events(
            @RequestParam(value = "dateFrom", required = false) LocalDateTime dateFrom,
            @RequestParam(value = "dateTo", required = false) LocalDateTime dateTo) {
        List<EventDetails> result;
        if (dateFrom == null && dateTo == null) {
            result = eventScheduler.listAllEvents();
            return ResponseEntity.ok(result);
        }
        if (dateFrom == null)
            dateFrom = LocalDateTime.MIN;
        if (dateTo == null)
            dateTo = LocalDateTime.MAX;
        result = eventScheduler.listEvents(dateFrom, dateTo);
        return ResponseEntity.ok(result);
    }
}

