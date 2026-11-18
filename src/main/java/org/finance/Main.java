package org.finance;

import org.finance.capture.CaptureEventSink;
import org.finance.capture.EventSink;
import org.finance.command.Command;
import org.finance.command.NewOrder;
import org.finance.command.Shutdown;
import org.finance.ingestion.CommandFactory;
import org.finance.ingestion.MockOrderGenerator;

import java.util.Random;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

public class Main
{
    public static void main( String[] args ) throws InterruptedException {
        // Generate mock orders

        // The matching engine takes these mock orders

        // The matching engine with the order book to match the orders

        // Blocking queue so that the matching engine can process the orders in a thread-safe manner
        EventSink eventSink = new CaptureEventSink();
        try
        {
            BlockingQueue<Command> queue = new ArrayBlockingQueue<>(65_536); // 2^16
            OrderBook book = new OrderBook();

            // A single thread for the matching engine
            Thread engineThread = new Thread(new MatchingEngine(queue, book, eventSink), "matching-engine");
            engineThread.start();

            AtomicLong idSource = new AtomicLong();
            CommandFactory cf = new CommandFactory( new Random(), idSource );

            // Create the threads that produce the data input
            ExecutorService producers = Executors.newFixedThreadPool(4);
            for (int i = 0; i < 4; i++) producers.submit(new MockOrderGenerator(queue, 1_000_000L, cf));

            producers.shutdown();
            producers.awaitTermination(1, TimeUnit.MINUTES);
            queue.put(new Shutdown());       // after all producers are done
            engineThread.join();
        }
        catch ( Throwable t )
        {
            throw new RuntimeException( t );
        }
    }
}