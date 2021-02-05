package org.garry.disruptor_clone;

import org.garry.disruptor_clone.support.DaemonThreadFactory;
import org.garry.disruptor_clone.support.ReadingCallable;
import org.garry.disruptor_clone.support.StubEntry;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.*;

import static org.junit.Assert.assertEquals;

public class RingBufferTest {

    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor(new DaemonThreadFactory());
    private RingBuffer<StubEntry> ringBuffer;
    private ConsumerBarrier<StubEntry> consumerBarrier;
    private ProducerBarrier<StubEntry> producerBarrier;
    private BatchHandler<StubEntry> batchHandler;
    private BatchConsumer<StubEntry> batchConsumer;

    @Before
    public void setUp()
    {
        ringBuffer = new RingBuffer<StubEntry>(StubEntry.FACTORY,20);
        consumerBarrier = ringBuffer.createConsumerBarrier();
        batchHandler = new BatchHandler<StubEntry>() {
            @Override
            public void onAvailable(StubEntry entry) throws Exception {
            }

            @Override
            public void onEndOfBatch() throws Exception {

            }

            @Override
            public void onCompletion() {

            }
        };
        batchConsumer = new BatchConsumer<StubEntry>(consumerBarrier, batchHandler);
        producerBarrier = ringBuffer.createProducerBarrier(batchConsumer);

    }

    @Test
    public void shouldClaimAndGet() throws AlertException, InterruptedException {
        assertEquals(RingBuffer.INITIAL_CURSOR_VALUE,ringBuffer.getCursor());

        StubEntry expectedEntry = new StubEntry(2701);

        StubEntry oldEntry = producerBarrier.nextEntry();
        oldEntry.copy(expectedEntry);
        producerBarrier.commit(oldEntry);

        long sequence = consumerBarrier.waitFor(0);
        assertEquals(0,sequence);

        StubEntry entry = ringBuffer.getEntry(sequence);
        assertEquals(expectedEntry,entry);

        assertEquals(0L,ringBuffer.getCursor());
    }

    @Test
    public void shouldClaimAndGetWithTimeout() throws AlertException, InterruptedException {
        assertEquals(RingBuffer.INITIAL_CURSOR_VALUE, ringBuffer.getCursor());

        StubEntry expectedEntry = new StubEntry(2701);

        StubEntry oldEntry = producerBarrier.nextEntry();
        oldEntry.copy(expectedEntry);
        producerBarrier.commit(oldEntry);

        long sequence = consumerBarrier.waitFor(0, 5, TimeUnit.MILLISECONDS);
        assertEquals(0,sequence);

        StubEntry entry = ringBuffer.getEntry(sequence);
        assertEquals(expectedEntry,entry);

        assertEquals(0L,ringBuffer.getCursor());
    }

    @Test
    public void shouldGetWitTimeout() throws AlertException, InterruptedException {
        long sequence = consumerBarrier.waitFor(0, 5, TimeUnit.MILLISECONDS);
        assertEquals(RingBuffer.INITIAL_CURSOR_VALUE,sequence);
    }

    @Test
    public void shouldClaimAndGetInSeparateThread() throws BrokenBarrierException, InterruptedException, ExecutionException {
        Future<List<StubEntry>> messages = getMessages(0, 0);

        StubEntry expectedEntry = new StubEntry(2701);

        StubEntry oldEntry = producerBarrier.nextEntry();
        oldEntry.copy(expectedEntry);
        producerBarrier.commit(oldEntry);

        assertEquals(expectedEntry,messages.get().get(0));
    }



    private Future<List<StubEntry>> getMessages(final long initial, final long toWaitFor) throws BrokenBarrierException, InterruptedException {
        final CyclicBarrier barrier = new CyclicBarrier(2);
        Future<List<StubEntry>> f = EXECUTOR.submit(new ReadingCallable(ringBuffer, initial, toWaitFor, barrier));
        barrier.await();
        return f;
    }
}
