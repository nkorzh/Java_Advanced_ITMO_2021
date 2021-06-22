package info.kgeorgiy.ja.kozhuharov.concurrent;

import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class ParallelMapperImpl implements ParallelMapper {
    private static final int MAX_QUEUE_SIZE = 1000;
    private final Queue<Runnable> tasks;
    private final List<Thread> executors;
    private final Lock lock;
    private final Condition canContinue;

    public ParallelMapperImpl(final int threads) {
        if (threads < 1) {
            throw new IllegalArgumentException("Number of threads should be more or equal to 1.");
        }
        tasks = new ArrayDeque<>();
        executors = new ArrayList<>(threads);
        lock = new ReentrantLock();
        canContinue = lock.newCondition();
        initExecutors(threads);
    }

    private void initExecutors(final int threads) {
        for (int i = 0; i < threads; i++) {
            Thread thread = new Thread(() -> {
                Runnable task;
                while (!Thread.interrupted()) {
                    lock.lock();
                    try {
                        while (tasks.isEmpty()) {
                            try {
                                canContinue.await();
                            } catch (final InterruptedException ignored) {
                                return;
                            }
                        }
                        task = tasks.poll();
                        canContinue.signalAll();
                    } finally {
                        lock.unlock();
                    }
                    task.run();
                }
            });
            thread.start();
            executors.add(thread);
        }
    }

    @Override
    public <T, R> List<R> map(Function<? super T, ? extends R> f, List<? extends T> args) throws InterruptedException {
        List<R> resList = new ArrayList<>(Collections.nCopies(args.size(), null));
        final CountDownLatch tasksCounter = new CountDownLatch(args.size());
        for (var i = 0; i < args.size(); i++) {
            final int resultInd = i;
            addTask(() -> f.apply(args.get(resultInd)),
                    res -> resList.set(resultInd, res),
                    tasksCounter
            );
        }
        tasksCounter.await();
        return resList;
    }

    private <R> void addTask(final Supplier<? extends R> doJob,
                             final Consumer<R> writeResult,
                             final CountDownLatch tasksCounter) throws InterruptedException {
        lock.lock();
        try {
            while (tasks.size() == MAX_QUEUE_SIZE) {
                canContinue.await();
            }
            tasks.add(() -> {
                writeResult.accept(doJob.get());
                tasksCounter.countDown();
            });
            canContinue.signalAll();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void close() {
        executors.forEach(Thread::interrupt);
        for (Thread thread : executors) {
            try {
                thread.join();
            } catch (final InterruptedException ignored) {
            }
        }
    }
}
