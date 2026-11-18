package org.finance;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import org.finance.capture.CancelAccepted;
import org.finance.capture.CancelRejected;
import org.finance.capture.EventSink;
import org.finance.capture.Trade;
import org.finance.command.Cancel;
import org.finance.command.Command;
import org.finance.command.NewOrder;
import org.finance.command.Shutdown;

public class MatchingEngine implements Runnable
{
    private final BlockingQueue<Command> queue;
    private final OrderBook orderBook;
    private final EventSink eventSink;

    public MatchingEngine( BlockingQueue<Command> queue, OrderBook orderBook, final EventSink eventSink )
    {
        this.queue = queue;
        this.orderBook = orderBook;
        this.eventSink = eventSink;
    }

    @Override
    public void run()
    {
        // Matching engine logic goes here
        try
        {
            while ( true )
            {
                final Command command = queue.take();   // waits here until something is added

                if ( command.getClass().equals( NewOrder.class ) )
                {
                    final NewOrder newOrder = ( NewOrder ) command;

                    final List<Trade> matches = orderBook.addOrder( castNewOrderToOrder( newOrder ) );
                    for ( final Trade match : matches )
                    {
                        eventSink.accept( match );
                    }
                }
                else if ( command.getClass().equals( Cancel.class ) )
                {
                    final boolean cancelSuccessful =
                            orderBook.cancel( String.valueOf( ( ( Cancel ) command ).orderId() ) );
                    eventSink.accept( cancelSuccessful ? new CancelAccepted() : new CancelRejected() );
                }
                else if ( command.getClass().equals( Shutdown.class ) )
                {
                    break;
                }
            }
        }
        catch ( InterruptedException e )
        {
            Thread.currentThread().interrupt();
        }
    }

    private Order castNewOrderToOrder( final NewOrder newOrder )
    {
        return new Order( newOrder.tradeType(), String.valueOf( newOrder.orderId() ), newOrder.priceInPence(),
                newOrder.qty() );
    }

}
