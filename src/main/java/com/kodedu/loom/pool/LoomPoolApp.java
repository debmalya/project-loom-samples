package com.kodedu.loom.pool;

import com.kodedu.loom.ThreadUtil;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class LoomPoolApp {
    public static void main(String[] args) {
        ExecutorService executor = Executors.newVirtualThreadExecutor();

        List<Future> threadList = IntStream.range(1, 1_000_000)
                .mapToObj(i -> {
                    Runnable runnable = () -> {
                        int millis = ThreadLocalRandom.current().nextInt(0, 100_000);
                        ThreadUtil.sleep(millis); // blocking -> suspend + resume (efficient + scalable)
                    };
                    return runnable;
                })
                .map(executor::submit)
                .collect(Collectors.toList());

        Thread thread = Thread.startVirtualThread(() -> {
            while (true) {
                final long count = threadList.stream()
                        .filter(Future::isDone)
                        .count();
                System.out.println("Done: " + count + " threads");
                ThreadUtil.sleepNoMessage(1000);
                if (count == threadList.size()) {
                    break;
                }
            }
        });

        ThreadUtil.waitAll(threadList);
        ThreadUtil.waitAll(thread);
    }
}
