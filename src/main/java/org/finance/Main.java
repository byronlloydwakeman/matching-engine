package org.finance;

import org.finance.command.Command;
import org.finance.command.Shutdown;
import org.finance.ingestion.MockOrderGenerator;

import java.util.concurrent.*;

public class Main
{
    public static void main( String[] args ) throws InterruptedException {
        // Generate mock orders

        // The matching engine takes these mock orders

        // The matching engine with the order book to match the orders

        BlockingQueue<Command> queue = new ArrayBlockingQueue<>(65_536);
        OrderBook book = new OrderBook();

        Thread engineThread = new Thread(new MatchingEngine(queue, book), "matching-engine");
        engineThread.start();

        ExecutorService producers = Executors.newFixedThreadPool(4);
        for (int i = 0; i < 4; i++) producers.submit(new MockOrderGenerator(queue, idSource, 1_000_000));

        producers.shutdown();
        producers.awaitTermination(1, TimeUnit.MINUTES);
        queue.put(new Shutdown());       // after all producers are done
        engineThread.join();
    }
}