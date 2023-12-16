package net.remgant.quartz.scheduler;

import net.remgant.quartz.Event;

import java.time.LocalDateTime;
import java.util.List;

public interface EventScheduler {
    String scheduleEvent(Event event);

    boolean deleteEvent(String eventId);

    List<EventDetails> listAllEvents();

    default List<EventDetails> listEvents(LocalDateTime startTime, LocalDateTime endTime) {
        return listAllEvents().stream().filter(ed ->
                (ed.getTriggerTime().isAfter(startTime) || ed.getTriggerTime().isEqual(startTime))
                        && (ed.getTriggerTime().isBefore(endTime) || ed.getTriggerTime().isEqual(endTime))).toList();
    }


}
