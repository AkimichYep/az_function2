package org.example.functions;

import com.microsoft.azure.functions.annotation.*;
import com.microsoft.azure.functions.*;

/**
 * Azure Functions with Timer trigger.
 * Runs on a CRON schedule: every 5 minutes by default.
 *
 * CRON format (6 fields): {second} {minute} {hour} {day} {month} {day-of-week}
 * Examples:
 *   "0 *\/5 * * * *"  -> every 5 minutes
 *   "0 0 9 * * MON-FRI" -> every weekday at 9:00 AM
 *   "0 0 0 1 * *"       -> first day of every month at midnight
 */
public class TimerTriggerJava {

    @FunctionName("TimerTriggerJava")
    public void run(
            @TimerTrigger(name = "timerInfo", schedule = "0 */5 * * * *") String timerInfo,
            final ExecutionContext context) {

        context.getLogger().info("Java Timer trigger function executed at: " + java.time.LocalDateTime.now());
        // timerInfo contains JSON with schedule info (isPastDue, scheduleStatus, etc.)
        context.getLogger().info("Timer info: " + timerInfo);
    }
}

