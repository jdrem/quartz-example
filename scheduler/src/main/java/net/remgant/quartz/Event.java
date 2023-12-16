package net.remgant.quartz;

import lombok.Data;

@Data
public abstract class Event {
    protected String triggerTime;
}
