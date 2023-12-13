package net.remgant.quartz.scheduler;

import net.remgant.quartz.Event;

public interface EventScheduler {
    String scheduleEvent(Event event);
}
