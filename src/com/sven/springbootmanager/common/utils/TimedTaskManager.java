package com.sven.springbootmanager.common.utils;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TimedTaskManager {
    private static final int PERIOD = 3;
    private static final WeakHashMap<Integer, Runnable> subscribers = new WeakHashMap();
    public static void start(){
        ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
// 第二个参数为首次执行的延时时间，第三个参数为定时执行的间隔时间  
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                for(Map.Entry<Integer, Runnable> entry : subscribers.entrySet()) {
                    Runnable r = entry.getValue();
                    if(r != null){
                        r.run();
                    }
                }
            }
        };
        service.scheduleAtFixedRate(runnable,0, PERIOD, TimeUnit.SECONDS);
        service.schedule(runnable, 0, TimeUnit.MILLISECONDS);
    }
    public static void subscribe(Runnable runnable){
        subscribers.put(runnable.hashCode(), runnable);
    }
}
