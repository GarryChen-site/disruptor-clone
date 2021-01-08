package org.garry.disruptor_clone;

/**
 * Abstraction for claiming {@link Entry}s in a {@link RingBuffer} while tracking dependent {@link EventConsumer}s
 *
 * @param <T> {@link Entry} implementation stored in the {@link RingBuffer}
 */
public interface ProducerBarrier<T extends Entry> {

    /**
     * Claim the next {@link Entry} in sequence for a producer on the {@link RingBuffer}
     * @return the claimed {@link Entry}
     */
    T nextEntry();

    /**
     * Commit an entry back to the {@link RingBuffer} to make it visible to {@link EventConsumer}s
     * @param entry to be committed back to the {@link RingBuffer}
     */
    void commit(T entry);

    /**
     * Delegate
     * @return value of the cursor for entries that have been published
     */
    long getCursor();
}
