package org.finance.ingestion;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.locks.LockSupport;
import org.finance.command.Command;
import org.finance.command.NewOrder;

public class MockOrderGenerator implements Runnable
{
    private final BlockingQueue<Command> queue;
    private final long ordersPerSecond;
    private final CommandFactory commandFactory;

    public MockOrderGenerator( BlockingQueue<Command> queue, long ordersPerSecond, final CommandFactory commandFactory )
    {
        this.queue = queue;
        this.ordersPerSecond = ordersPerSecond;
        this.commandFactory = commandFactory;
    }



    @Override
    public void run()
    {
        // Mock order generation logic goes here
        // Schedule in nanoseconds

        long batchIntervalNanos = 1_000_000L; // 1ms
        long start = System.nanoTime();
        long batchNumber = 0;
        long carry = 0;

        try
        {
            while ( !Thread.currentThread().isInterrupted() )
            {
                // Find the start time, like in a marathon when people start at interval times
                long due = start + batchIntervalNanos * batchNumber;
                waitUntil( due );

                // Using carry so that number of order are accurate and are not lost as part of long division precision
                carry += ordersPerSecond;
                int batchSize = ( int ) ( carry / 1000 );
                carry %= 1000; // Get the remainder, e.g if carry was 1001, carry would now be 1. This is then carries
                // onto the next batch
                for ( int i = 0; i < batchSize; i++ )
                {
                    queue.put( commandFactory.nextCommand( System.nanoTime() ) );
                }
                batchNumber++;
            }
        }
        catch ( InterruptedException e )
        {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Smart nano sleeper that sleeps the thread until the due time, but with extra mechanisms so that it does not
     * oversleep
     *
     * @param dueNanos Nanoseconds to wait for
     */
    private void waitUntil( long dueNanos )
    {
        long remaining;
        while ( ( remaining = dueNanos - System.nanoTime() ) > 0 )
        {
            // While above 100ms, use non-reliable but CPU safe .parkNanos
            if ( remaining > 100_000 )
            {
                // Park most of it so we do the .onSpinWait()
                LockSupport.parkNanos( remaining - 50_000 );
            }
            else
            {
                // Reliable but means the CPU is constantly 'looking at it's watch' to see if the time is up.
                Thread.onSpinWait();
            }
        }
    }
}
