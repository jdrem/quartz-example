package net.remgant.quartz.scheduler;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventDetails {
    private String id;
    private LocalDateTime triggerTime;
    private String eventClass;
    private Map<String,Object> eventImplementation;
}
