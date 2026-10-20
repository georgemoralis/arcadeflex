package gr.codebb.arcadeflex.v036.platform;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/** Regression coverage for the frame-boundary pause protocol. */
public final class CooperativePauseTest {
    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    private static void awaitWaiting(Thread thread) throws Exception {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(3);
        while (thread.getState() != Thread.State.WAITING && System.nanoTime() < deadline) {
            Thread.sleep(1);
        }
        check(thread.getState() == Thread.State.WAITING, "emulator did not park");
    }

    @org.junit.Test(timeout = 10000)
    public void pauseWaitsUntilRenderingReturns() throws Exception {
        MainStream stream = new MainStream(null);
        CountDownLatch rendering = new CountDownLatch(1);
        CountDownLatch finishRendering = new CountDownLatch(1);
        CountDownLatch renderReturned = new CountDownLatch(1);
        Thread emulator = new Thread(() -> {
            synchronized (stream) {
                rendering.countDown();
                try {
                    finishRendering.await();
                } catch (InterruptedException ex) {
                    throw new AssertionError(ex);
                }
            }
            renderReturned.countDown();
            stream.awaitEmulationResumed();
        });
        emulator.setDaemon(true);
        emulator.start();
        try {
            check(rendering.await(3, TimeUnit.SECONDS), "rendering did not start");
            stream.setEmulationPaused(true);
            finishRendering.countDown();
            check(renderReturned.await(3, TimeUnit.SECONDS), "pause froze rendering");
            awaitWaiting(emulator);
        } finally {
            finishRendering.countDown();
            stream.setEmulationPaused(false);
            emulator.join(3000);
        }
        check(!emulator.isAlive(), "resume failed after rendering");
    }

    @org.junit.Test(timeout = 15000)
    public void pauseResumeIsCooperative() throws Exception {
        MainStream stream = new MainStream(null);
        // Pause before startup must be safe; repeated requests are idempotent.
        for (int cycle = 0; cycle < 100; cycle++) {
            stream.setEmulationPaused(true);
            stream.setEmulationPaused(true);
            CountDownLatch passed = new CountDownLatch(1);
            AtomicBoolean interruptPreserved = new AtomicBoolean();
            Thread emulator = new Thread(() -> {
                stream.awaitEmulationResumed();
                interruptPreserved.set(Thread.currentThread().isInterrupted());
                passed.countDown();
            });
            emulator.setDaemon(true);
            emulator.start();
            awaitWaiting(emulator);
            check(passed.getCount() == 1, "paused emulator advanced");
            // Pausing must not retain the monitor used by screenshot/blit.
            synchronized (stream) {
                check(passed.getCount() == 1, "pause unexpectedly released");
            }
            emulator.interrupt();
            Thread.sleep(2);
            awaitWaiting(emulator);
            check(passed.getCount() == 1, "interrupt incorrectly resumed emulator");
            stream.setEmulationPaused(false);
            check(passed.await(3, TimeUnit.SECONDS), "resume lost its wakeup");
            emulator.join(3000);
            check(!emulator.isAlive(), "emulator failed to exit");
            check(interruptPreserved.get(), "interrupt status was lost");
        }
        // A resume before the next checkpoint must not be lost.
        stream.setEmulationPaused(true);
        stream.setEmulationPaused(false);
        Thread unpaused = new Thread(stream::awaitEmulationResumed);
        unpaused.setDaemon(true);
        unpaused.start();
        unpaused.join(3000);
        check(!unpaused.isAlive(), "early resume was lost");
        System.out.println("PASS: 100 pause/resume cycles, monitor access, interrupts, early resume");
    }
}
