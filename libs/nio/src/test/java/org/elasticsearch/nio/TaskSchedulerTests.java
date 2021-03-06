/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License
 * 2.0 and the Server Side Public License, v 1; you may not use this file except
 * in compliance with, at your election, the Elastic License 2.0 or the Server
 * Side Public License, v 1.
 */

package org.elasticsearch.nio;

import org.elasticsearch.test.ESTestCase;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.LockSupport;

public class TaskSchedulerTests extends ESTestCase {

    private TaskScheduler scheduler = new TaskScheduler();

    public void testScheduleTask() {
        AtomicBoolean complete = new AtomicBoolean(false);

        long executeTime = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(10);
        scheduler.scheduleAtRelativeTime(() -> complete.set(true), executeTime);

        while (true) {
            long nanoTime = System.nanoTime();
            Runnable runnable = scheduler.pollTask(nanoTime);
            if (nanoTime - executeTime >= 0) {
                runnable.run();
                assertTrue(complete.get());
                break;
            } else {
                assertNull(runnable);
                LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(1));
            }
        }
    }

    public void testPollScheduleTaskAtExactTime() {
        long executeTime = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(10);
        scheduler.scheduleAtRelativeTime(() -> {}, executeTime);

        assertNull(scheduler.pollTask(executeTime - 1));
        assertNotNull(scheduler.pollTask(executeTime));
    }

    public void testTaskOrdering() {
        AtomicBoolean first = new AtomicBoolean(false);
        AtomicBoolean second = new AtomicBoolean(false);
        AtomicBoolean third = new AtomicBoolean(false);
        long executeTime = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(10);
        scheduler.scheduleAtRelativeTime(() -> third.set(true), executeTime + 2);
        scheduler.scheduleAtRelativeTime(() -> first.set(true), executeTime);
        scheduler.scheduleAtRelativeTime(() -> second.set(true), executeTime + 1);

        scheduler.pollTask(executeTime + 10).run();
        assertTrue(first.get());
        assertFalse(second.get());
        assertFalse(third.get());
        scheduler.pollTask(executeTime + 10).run();
        assertTrue(first.get());
        assertTrue(second.get());
        assertFalse(third.get());
        scheduler.pollTask(executeTime + 10).run();
        assertTrue(first.get());
        assertTrue(second.get());
        assertTrue(third.get());
    }

    public void testTaskCancel() {
        AtomicBoolean first = new AtomicBoolean(false);
        AtomicBoolean second = new AtomicBoolean(false);
        long executeTime = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(10);
        Runnable cancellable = scheduler.scheduleAtRelativeTime(() -> first.set(true), executeTime);
        scheduler.scheduleAtRelativeTime(() -> second.set(true), executeTime + 1);

        cancellable.run();
        scheduler.pollTask(executeTime + 10).run();
        assertFalse(first.get());
        assertTrue(second.get());
        assertNull(scheduler.pollTask(executeTime + 10));
    }

    public void testNanosUntilNextTask() {
        long nanoTime = System.nanoTime();
        long executeTime = nanoTime + TimeUnit.MILLISECONDS.toNanos(10);
        scheduler.scheduleAtRelativeTime(() -> {}, executeTime);
        assertEquals(TimeUnit.MILLISECONDS.toNanos(10), scheduler.nanosUntilNextTask(nanoTime));
        assertEquals(TimeUnit.MILLISECONDS.toNanos(5), scheduler.nanosUntilNextTask(nanoTime + TimeUnit.MILLISECONDS.toNanos(5)));
    }
}
