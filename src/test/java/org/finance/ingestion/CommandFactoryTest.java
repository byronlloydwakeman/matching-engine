package org.finance.ingestion;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicLong;
import org.finance.TradeType;
import org.finance.command.Cancel;
import org.finance.command.NewOrder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class CommandFactoryTest
{
    private CommandFactory commandFactory;
    private MockRandom mockRandom;
    private AtomicLong idCounter;

    @BeforeEach
    void setup()
    {
        mockRandom = new MockRandom();
        idCounter = new AtomicLong( 0L );
        commandFactory = new CommandFactory( mockRandom, idCounter );
    }

    @Test
    void shouldGenerateCommands()
    {
        mockRandom.setBooleanValues( new boolean[]{ true, true, false } );
        // AddOrder, Random, Random, AddOrder, Random, Random, CancelOrder, Index 0
        mockRandom.setIntValues( new int[]{ 10, 2, 3, 10, 5, 6, 1, 0 } ); // Last value must be <= commands - 1

        final var firstCommand = commandFactory.nextCommand( System.nanoTime() );
        final var secondCommand = commandFactory.nextCommand( System.nanoTime() );
        final var thirdCommand = commandFactory.nextCommand( System.nanoTime() );

        assertInstanceOf( NewOrder.class, firstCommand );
        assertInstanceOf( NewOrder.class, secondCommand );
        assertInstanceOf( Cancel.class, thirdCommand );
    }

    @Test
    void shouldGenerateOneNewOrderCommandTradeType()
    {
        // Roll is 7
        // Price is 100 + 1 - 5 = 96
        // Quantity is 50
        mockRandom.setBooleanValues( new boolean[]{ true } );
        mockRandom.setIntValues( new int[]{ 7, 1, 49 } );

        final var firstCommand = commandFactory.nextCommand( System.nanoTime() );

        assertInstanceOf( NewOrder.class, firstCommand );

        final NewOrder firstNewOrderCommand = ( NewOrder ) firstCommand;
        assertEquals( TradeType.BID, firstNewOrderCommand.tradeType() );

    }

    // Check liveIds gets properly updated
    // Check prices are correct
    // Assert Command properties

    @Test
    void liveIdsShouldHaveExpectedSize()
    {
        // Add two commands, one cancel

        mockRandom.setBooleanValues( new boolean[]{ true, true, false } );
        // AddOrder, Random, Random, AddOrder, Random, Random, CancelOrder, Index 0
        mockRandom.setIntValues( new int[]{ 10, 2, 3, 10, 5, 6, 1, 0 } ); // Last value must be <= commands - 1

        assertEquals( 0, commandFactory.getLiveIdsSize() );

        final var firstCommand = commandFactory.nextCommand( System.nanoTime() );
        assertEquals( 1, commandFactory.getLiveIdsSize() );

        final var secondCommand = commandFactory.nextCommand( System.nanoTime() );
        assertEquals( 2, commandFactory.getLiveIdsSize() );

        final var thirdCommand = commandFactory.nextCommand( System.nanoTime() );
        assertEquals( 1, commandFactory.getLiveIdsSize() );

        assertInstanceOf( NewOrder.class, firstCommand );
        assertInstanceOf( NewOrder.class, secondCommand );
        assertInstanceOf( Cancel.class, thirdCommand );

    }

    @Test
    void shouldGenerateCancelCommand()
    {
        mockRandom.setBooleanValues( new boolean[]{ true } );
        mockRandom.setIntValues( new int[]{ 7, 1, 49, 0, 0 } );

        final var firstCommand = commandFactory.nextCommand( System.nanoTime() ); // Update liveIds

        final long systemNanos = System.nanoTime();
        final var cancelCommand = commandFactory.nextCommand( systemNanos );

        assertEquals( Cancel.class, cancelCommand.getClass() );

        final Cancel cancel = ( Cancel ) cancelCommand;

        assertEquals( 1, cancel.orderId() );
        assertEquals( systemNanos, cancel.enqueueNanos() );
    }

    @Test
    void shouldGenerateValidNewOrderBid()
    {
        // Roll is 7
        // Price is 100 + 1 - 5 = 96
        // Quantity is 50
        mockRandom.setBooleanValues( new boolean[]{ true } );
        mockRandom.setIntValues( new int[]{ 7, 1, 49 } );
        idCounter.incrementAndGet();

        final long systemNanos = System.nanoTime();
        final var firstCommand = commandFactory.nextCommand( systemNanos );

        assertEquals( NewOrder.class, firstCommand.getClass() );

        final NewOrder firstNewOrderCommand = ( NewOrder ) firstCommand;

        // Already incremented once
        assertEquals( 2, firstNewOrderCommand.orderId() );
        assertEquals( TradeType.BID, firstNewOrderCommand.tradeType() );
        // 100 + (1 - 10) - (5 * 1) = 86
        assertEquals( 86L, firstNewOrderCommand.priceInPence() );
        // 49 + 1
        assertEquals( 50L, firstNewOrderCommand.qty() );
        assertEquals( systemNanos, firstNewOrderCommand.enqueueNanos() );
    }

    @Test
    void shouldGenerateValidNewOrderAsk()
    {
        // Roll is 7
        // Price is 100 + 1 - 5 = 96
        // Quantity is 50
        mockRandom.setBooleanValues( new boolean[]{ false } ); // ASK
        mockRandom.setIntValues( new int[]{ 7, 1, 49 } );
        idCounter.incrementAndGet();

        final long systemNanos = System.nanoTime();
        final var firstCommand = commandFactory.nextCommand( systemNanos );

        assertEquals( NewOrder.class, firstCommand.getClass() );

        final NewOrder firstNewOrderCommand = ( NewOrder ) firstCommand;

        // Already incremented once
        assertEquals( 2, firstNewOrderCommand.orderId() );
        assertEquals( TradeType.ASK, firstNewOrderCommand.tradeType() );
        // 100 + (1 - 10) - (5 * -1) = 96
        assertEquals( 96L, firstNewOrderCommand.priceInPence() );
        // 49 + 1
        assertEquals( 50L, firstNewOrderCommand.qty() );
        assertEquals( systemNanos, firstNewOrderCommand.enqueueNanos() );
    }

    @Test
    void bidsAndAsksShouldCrossOver()
    {
        // The orders should be able to cross over
        // BID
        mockRandom.setBooleanValues( new boolean[]{ true, false } );
        mockRandom.setIntValues( new int[]{ 7, 19, 49, 7, 0, 49 } );

        final long systemNanos = System.nanoTime();
        final var firstCommand = commandFactory.nextCommand( systemNanos );

        assertEquals( NewOrder.class, firstCommand.getClass() );

        final NewOrder firstNewOrderCommand = ( NewOrder ) firstCommand;

        // Already incremented once
        assertEquals( 1, firstNewOrderCommand.orderId() );
        assertEquals( TradeType.BID, firstNewOrderCommand.tradeType() );
        // 100 + (19 - 10) - (5 * 1) = 104
        assertEquals( 104L, firstNewOrderCommand.priceInPence() );
        // 49 + 1
        assertEquals( 50L, firstNewOrderCommand.qty() );
        assertEquals( systemNanos, firstNewOrderCommand.enqueueNanos() );

        // ASK
        final long systemNanos2 = System.nanoTime();
        final var secondCommand = commandFactory.nextCommand( systemNanos2 );

        assertEquals( NewOrder.class, secondCommand.getClass() );

        final NewOrder secondNewOrderCommand = ( NewOrder ) secondCommand;

        // Already incremented once
        assertEquals( 2, secondNewOrderCommand.orderId() );
        assertEquals( TradeType.ASK, secondNewOrderCommand.tradeType() );
        // 100 + (0 - 10) - (5 * -1) = 95
        assertEquals( 95L, secondNewOrderCommand.priceInPence() );
        // 49 + 1
        assertEquals( 50L, secondNewOrderCommand.qty() );
        assertEquals( systemNanos2, secondNewOrderCommand.enqueueNanos() );

        // The most someone can be willing to pay, must be higher than the least someone is willing to sell for
        assertTrue( firstNewOrderCommand.priceInPence() > secondNewOrderCommand.priceInPence() );
    }

    @Test
    void verifyPriceDistribution()
    {
        commandFactory = new CommandFactory( new Random( 42 ), new AtomicLong( 0 ) );
        int cancelCount = 0;
        int orderCount = 0;
        int bidCount = 0;
        int askCount = 0;
        TreeMap<Long, Integer> askPriceDistribution = new TreeMap<>();
        TreeMap<Long, Integer> bidPriceDistribution = new TreeMap<>();


        for ( int i = 0; i < 10_000; i++ )
        {
            final var command = commandFactory.nextCommand( System.nanoTime() );
            if ( command.getClass().equals( Cancel.class ) )
            {
                cancelCount++;
            }
            else if ( command.getClass().equals( NewOrder.class ) )
            {
                orderCount++;
                final NewOrder newOrderCommand = ( NewOrder ) command;
                if ( newOrderCommand.tradeType().equals( TradeType.ASK ) )
                {
                    askCount++;
                    askPriceDistribution.merge( newOrderCommand.priceInPence(), 1, Integer::sum );

                }
                else
                {
                    bidCount++;
                    bidPriceDistribution.merge( newOrderCommand.priceInPence(), 1, Integer::sum );
                }
            }
        }

        System.out.printf( "Cancel - %s%n", cancelCount );
        System.out.printf( "Order - %s%n", orderCount );
        System.out.printf( "Bid - %s%n", bidCount );
        System.out.printf( "Ask - %s%n", askCount );

        System.out.println("ASK prices:");
        for ( Map.Entry<Long, Integer> entry : askPriceDistribution.entrySet() )
        {
            System.out.printf( "Price: %s, Count: %s%n", entry.getKey(), entry.getValue() );
        }

        System.out.println("BID prices:");
        for ( Map.Entry<Long, Integer> entry : bidPriceDistribution.entrySet() )
        {
            System.out.printf( "Price: %s, Count: %s%n", entry.getKey(), entry.getValue() );
        }

        assertEquals( 95L, askPriceDistribution.firstKey() );
        assertEquals( 115L, askPriceDistribution.lastKey() );

        assertEquals( 85L, bidPriceDistribution.firstKey() );
        assertEquals( 105L, bidPriceDistribution.lastKey() );
    }
}
