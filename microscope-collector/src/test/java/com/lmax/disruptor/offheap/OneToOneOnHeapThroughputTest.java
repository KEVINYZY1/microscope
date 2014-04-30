package com.lmax.disruptor.offheap;

import com.lmax.disruptor.*;
import com.lmax.disruptor.util.DaemonThreadFactory;

import java.nio.ByteBuffer;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.LockSupport;

public class OneToOneOnHeapThroughputTest extends AbstractPerfTestDisruptor {
    private static final int BLOCK_SIZE = 256;
    private final byte[] data = new byte[BLOCK_SIZE];
    private static final int BUFFER_SIZE = 1024 * 1024;
    private static final long ITERATIONS = 1000 * 1000 * 10L;
    private final Executor executor = Executors.newFixedThreadPool(1, DaemonThreadFactory.INSTANCE);
    private final WaitStrategy waitStrategy = new YieldingWaitStrategy();
    private final RingBuffer<ByteBuffer> buffer =
            RingBuffer.createSingleProducer(BufferFactory.direct(BLOCK_SIZE), BUFFER_SIZE, waitStrategy);
    private final ByteBufferHandler handler = new ByteBufferHandler();
    private final BatchEventProcessor<ByteBuffer> processor =
            new BatchEventProcessor<ByteBuffer>(buffer, buffer.newBarrier(), handler);
    {
        buffer.addGatingSequences(processor.getSequence());
    }
    private final Random r = new Random(1);

    public OneToOneOnHeapThroughputTest() {
        r.nextBytes(data);
    }

    public static void main(String[] args) throws Exception {
        new OneToOneOnHeapThroughputTest().testImplementations();
    }

    @Override
    protected int getRequiredProcessorCount() {
        return 2;
    }

    @Override
    protected long runDisruptorPass() throws Exception {
        byte[] data = this.data;

        final CountDownLatch latch = new CountDownLatch(1);
        long expectedCount = processor.getSequence().get() + ITERATIONS;
        handler.reset(latch, ITERATIONS);
        executor.execute(processor);
        long start = System.currentTimeMillis();

        final RingBuffer<ByteBuffer> rb = buffer;

        for (long i = 0; i < ITERATIONS; i++) {
            long next = rb.next();
            ByteBuffer event = rb.get(next);
            event.clear();
            event.put(data);
            event.flip();
            rb.publish(next);
        }

        latch.await();
        long opsPerSecond = (ITERATIONS * 1000L) / (System.currentTimeMillis() - start);
        waitForEventProcessorSequence(expectedCount);
        processor.halt();

        return opsPerSecond;
    }

    private void waitForEventProcessorSequence(long expectedCount) {
        while (processor.getSequence().get() < expectedCount) {
            LockSupport.parkNanos(1);
        }
    }

    public static class ByteBufferHandler implements EventHandler<ByteBuffer> {
        private long total = 0;
        private long expectedCount;
        private CountDownLatch latch;

        @Override
        public void onEvent(ByteBuffer event, long sequence, boolean endOfBatch) throws Exception {
            for (int i = 0; i < BLOCK_SIZE; i += 8) {
                total += event.getLong();
            }

            if (--expectedCount == 0) {
                latch.countDown();
            }
        }

        public long getTotal() {
            return total;
        }

        public void reset(CountDownLatch latch, long expectedCount) {
            this.latch = latch;
            this.expectedCount = expectedCount;
        }
    }

    private static final class BufferFactory implements EventFactory<ByteBuffer> {
        private final boolean isDirect;
        private final int size;

        private BufferFactory(boolean isDirect, int size) {
            this.isDirect = isDirect;
            this.size = size;
        }

        public static BufferFactory direct(int size) {
            return new BufferFactory(true, size);
        }

        @SuppressWarnings("unused")
        public static BufferFactory heap(int size) {
            return new BufferFactory(false, size);
        }

        @Override
        public ByteBuffer newInstance() {
            if (isDirect) {
                return ByteBuffer.allocateDirect(size);
            } else {
                return ByteBuffer.allocate(size);
            }
        }
    }
}
