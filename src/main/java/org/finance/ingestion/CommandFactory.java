package org.finance.ingestion;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import org.finance.TradeType;
import org.finance.command.Cancel;
import org.finance.command.Command;
import org.finance.command.NewOrder;

public class CommandFactory
{
    private static final int RANDOM_MAX_OFFSET = 20;
    private final Random random;
    private final AtomicLong idSource;
    private final List<Long> liveIds = new ArrayList<>();
    private final long mid = 100;


    public CommandFactory( final Random random, final AtomicLong idSource )
    {
        this.random = random;
        this.idSource = idSource;
    }

    public Command nextCommand( long scheduledNanos )
    {
        final int roll = random.nextInt( 10 ) + 1;

        if ( roll >= 3 || liveIds.isEmpty() )
        {
            // True is bid, false is ask
            final boolean bidAskBoolean = random.nextBoolean();
            final long price = mid + (random.nextInt( RANDOM_MAX_OFFSET + 1 ) - (RANDOM_MAX_OFFSET / 2)) - (5 * (bidAskBoolean ? 1 : -1));
            final long quantity = random.nextInt( 100 ) + 1;
            final long id = idSource.incrementAndGet();
            liveIds.add( id );
            final TradeType tradeType = bidAskBoolean ? TradeType.BID : TradeType.ASK;
            return new NewOrder( id, tradeType, price, quantity, scheduledNanos );
        }
        else
        {
            final int index = random.nextInt( liveIds.size() );
            final long id = liveIds.remove( index );
            return new Cancel( id, scheduledNanos );
        }
    }

    public int getLiveIdsSize()
    {
        return liveIds.size();
    }
}
