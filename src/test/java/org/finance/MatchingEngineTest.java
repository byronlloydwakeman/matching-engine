package org.finance;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import org.finance.capture.CancelAccepted;
import org.finance.capture.CancelRejected;
import org.finance.capture.CaptureEventSink;
import org.finance.capture.Event;
import org.finance.capture.EventSink;
import org.finance.capture.Trade;
import org.finance.command.Cancel;
import org.finance.command.Command;
import org.finance.command.NewOrder;
import org.finance.command.Shutdown;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class MatchingEngineTest
{
    // Get items from queue correctly.
    // Correctly match them

    private BlockingQueue<Command> queue;
    private MatchingEngine matchingEngine;
    private OrderBook orderBook;
    private EventSink eventSink;
    private Thread engineThread;

    @BeforeEach
    void setup()
    {
        queue = new ArrayBlockingQueue<>( 65_536 );
        orderBook = new OrderBook();
        eventSink = new CaptureEventSink();
        matchingEngine = new MatchingEngine( queue, orderBook, eventSink );
        engineThread = new Thread( matchingEngine, "test-matching-engine" );
    }

    @AfterEach
    void clean()
    {
        if ( engineThread.isAlive() )
        {
            queue.add( new Shutdown() );
            try
            {
                engineThread.join();
            }
            catch ( InterruptedException e )
            {
                Thread.currentThread().interrupt();
            }
        }
    }

    @Test
    void shouldPollQueueCorrectly() throws InterruptedException
    {
        queue.add( new NewOrder( 1L, TradeType.BID, 100L, 50L, System.nanoTime() ) );
        queue.add( new Shutdown() );

        assertEquals( 2, queue.size() );

        engineThread.start();

        engineThread.join(); // Join with the main thread

        assertEquals( 0, queue.size() );
    }

    @Test
    void shouldMatchAskAndBidSamePrice() throws InterruptedException
    {
        queue.add( new NewOrder( 1L, TradeType.BID, 100L, 50L, System.nanoTime() ) );
        queue.add( new NewOrder( 2L, TradeType.ASK, 100L, 50L, System.nanoTime() ) );
        queue.add( new Shutdown() );

        assertEquals( 3, queue.size() );

        engineThread.start();

        engineThread.join(); // Join with the main thread

        assertEquals( 0, queue.size() );

        eventSink.getEvents();
    }

    @Test
    void shouldNotMatchAskAndBidNoOverlappingPrice() throws InterruptedException
    {
        queue.add( new NewOrder( 1L, TradeType.BID, 99L, 50L, System.nanoTime() ) );
        queue.add( new NewOrder( 2L, TradeType.ASK, 101L, 50L, System.nanoTime() ) );
        queue.add( new Shutdown() );

        assertEquals( 3, queue.size() );

        engineThread.start();

        engineThread.join(); // Join with the main thread

        assertEquals( 0, queue.size() );

        eventSink.getEvents();
    }

    @Test
    void shouldNotAddTradeAfterShutdown() throws InterruptedException
    {
        queue.add( new NewOrder( 1L, TradeType.BID, 99L, 50L, System.nanoTime() ) );
        queue.add( new Shutdown() );
        queue.add( new NewOrder( 2L, TradeType.ASK, 101L, 50L, System.nanoTime() ) );

        assertEquals( 3, queue.size() );

        engineThread.start();

        engineThread.join(); // Join with the main thread

        assertEquals( 1, queue.size() );

        eventSink.getEvents();
    }

    @Test
    void shouldAddBidOrderToOrderBook() throws InterruptedException
    {
        queue.add( new NewOrder( 1L, TradeType.BID, 100L, 50L, System.nanoTime() ) );
        queue.add( new Shutdown() );

        assertEquals( 2, queue.size() );

        engineThread.start();

        engineThread.join(); // Join with the main thread

        assertEquals( 0, queue.size() );

        final Optional<Order> bestBid = orderBook.getBestBid();

        assertTrue( bestBid.isPresent() );

        final Order bid = bestBid.get();

        assertEquals( "1", bid.getOrderId() );
        assertEquals( TradeType.BID, bid.getTradeType() );
        assertEquals( 100L, bid.getPrice() );
        assertEquals( 50L, bid.getQuantity() );
    }

    @Test
    void shouldAddAskOrderToOrderBook() throws InterruptedException
    {
        queue.add( new NewOrder( 1L, TradeType.ASK, 100L, 50L, System.nanoTime() ) );
        queue.add( new Shutdown() );

        assertEquals( 2, queue.size() );

        engineThread.start();

        engineThread.join(); // Join with the main thread

        assertEquals( 0, queue.size() );

        final Optional<Order> bestAsk = orderBook.getBestAsk();

        assertTrue( bestAsk.isPresent() );

        final Order ask = bestAsk.get();

        assertEquals( "1", ask.getOrderId() );
        assertEquals( TradeType.ASK, ask.getTradeType() );
        assertEquals( 100L, ask.getPrice() );
        assertEquals( 50L, ask.getQuantity() );
    }

    @Test
    void shouldGetEventSinkWithTrade() throws InterruptedException
    {
        queue.add( new NewOrder( 1L, TradeType.BID, 100L, 50L, System.nanoTime() ) );
        queue.add( new NewOrder( 2L, TradeType.ASK, 100L, 50L, System.nanoTime() ) );
        queue.add( new Shutdown() );

        assertEquals( 3, queue.size() );

        engineThread.start();

        engineThread.join(); // Join with the main thread

        assertEquals( 0, queue.size() );

        List<Event> events = eventSink.getEvents();

        Event event = events.get( 0 );

        assertInstanceOf( Trade.class, event );

        Trade trade = ( Trade ) event;

        assertEquals( "1", trade.bidId() );
        assertEquals( "2", trade.askId() );
        assertEquals( 100L, trade.price() );
        assertEquals( 50L, trade.quantity() );
    }

    @Test
    void shouldGetEmptyEventSinkWithNoPriceOverlap() throws InterruptedException
    {
        queue.add( new NewOrder( 1L, TradeType.BID, 99L, 50L, System.nanoTime() ) );
        queue.add( new NewOrder( 2L, TradeType.ASK, 101L, 50L, System.nanoTime() ) );
        queue.add( new Shutdown() );

        assertEquals( 3, queue.size() );

        engineThread.start();

        engineThread.join(); // Join with the main thread

        assertEquals( 0, queue.size() );

        List<Event> events = eventSink.getEvents();

        assertTrue( events.isEmpty() );
    }

    @Test
    void shouldGetEventSinkWithCancelAccepted() throws InterruptedException
    {
        queue.add( new NewOrder( 1L, TradeType.BID, 100L, 50L, System.nanoTime() ) );
        queue.add( new Cancel( 1L, System.nanoTime() ) );
        queue.add( new Shutdown() );

        assertEquals( 3, queue.size() );

        engineThread.start();

        engineThread.join(); // Join with the main thread

        assertEquals( 0, queue.size() );

        List<Event> events = eventSink.getEvents();

        Event event = events.get( 0 );

        assertInstanceOf( CancelAccepted.class, event );
        assertEquals( 1, events.size() );
    }

    @Test
    void shouldGetEventSinkWithCancelRejected() throws InterruptedException
    {
        queue.add( new NewOrder( 1L, TradeType.BID, 100L, 50L, System.nanoTime() ) );
        queue.add( new Cancel( 2L, System.nanoTime() ) ); // Non-existent id
        queue.add( new Shutdown() );

        assertEquals( 3, queue.size() );

        engineThread.start();

        engineThread.join(); // Join with the main thread

        assertEquals( 0, queue.size() );

        List<Event> events = eventSink.getEvents();

        Event event = events.get( 0 );

        assertInstanceOf( CancelRejected.class, event );
        assertEquals( 1, events.size() );
    }

    // Fail to cancel as match has been made
    @Test
    void shouldGetEventSinkWithCancelRejectedDueToMatch() throws InterruptedException
    {
        queue.add( new NewOrder( 1L, TradeType.BID, 100L, 50L, System.nanoTime() ) );
        queue.add( new NewOrder( 2L, TradeType.ASK, 100L, 50L, System.nanoTime() ) );
        queue.add( new Cancel( 2L, System.nanoTime() ) ); // Non-existent id
        queue.add( new Shutdown() );

        assertEquals( 4, queue.size() );

        engineThread.start();

        engineThread.join(); // Join with the main thread

        assertEquals( 0, queue.size() );

        List<Event> events = eventSink.getEvents();

        Event trade = events.get( 0 );
        Event cancelRejected = events.get( 1 );

        assertInstanceOf( Trade.class, trade );
        Trade tradeEvent = ( Trade ) trade;
        assertEquals( "1", tradeEvent.bidId() );
        assertEquals( "2", tradeEvent.askId() );
        assertEquals( 100L, tradeEvent.price() );
        assertEquals( 50L, tradeEvent.quantity() );

        assertInstanceOf( CancelRejected.class, cancelRejected );
        assertEquals( 2, events.size() );
    }

    @Test
    void matchingEngineShouldHangWithoutShutdown() throws InterruptedException
    {
        queue.add( new NewOrder( 1L, TradeType.BID, 100L, 50L, System.nanoTime() ) );

        assertEquals( 1, queue.size() );

        engineThread.start();

        engineThread.join(1000); // Join with the main thread

        assertTrue(engineThread.isAlive(), "Engine thread should still be alive without shutdown");
    }
}
