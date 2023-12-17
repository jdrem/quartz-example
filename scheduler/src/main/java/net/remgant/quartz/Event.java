package net.remgant.quartz;

import lombok.Data;

import java.time.Instant;

@Data
public abstract class Event {
    protected Instant triggerTime;
}
