package org.cthul.org.model.io;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 *
 */
public class JobManager {
    
    private static final ScheduledExecutorService EXEC = new ScheduledThreadPoolExecutor(1);
    private static final Set<Future<?>> FUTURES = Collections.synchronizedSet(new HashSet<>());
    
    public static Future<?> schedule(Runnable command, long delay, TimeUnit unit) {
        class Job implements Runnable {
            Future<?> f;
            @Override
            public void run() {
                FUTURES.remove(f);
                command.run();
            }
        }
        Job j = new Job();
        j.f = EXEC.schedule(command, delay, unit);
        FUTURES.add(j.f);
        return j.f;
    }
    
    public static void shutdown() {
        EXEC.shutdown();
        for (Object f: FUTURES.toArray()) {
            ((Future<?>) f).cancel(true);
        }
    }
    
    static {
        Runtime.getRuntime().addShutdownHook(new Thread(){
            @Override
            public void run() {
                EXEC.shutdownNow();
                for (Object f: FUTURES.toArray()) {
                    ((Future<?>) f).cancel(true);
                }
                try {
                    if (!EXEC.awaitTermination(1, TimeUnit.SECONDS)) {
                        System.exit(1);
                    }
                } catch (InterruptedException e) { }
            }
        });
    }
}
