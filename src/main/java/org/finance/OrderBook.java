package org.finance;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * We need to be able to
 * Get the earliest cheapest ask and highest bid,
 * Remove stale orders
 * Check for matches, aka is there a price cross
 */
public class OrderBook
{
    // price -> <orderId, Order>
    // Price is ordered by size
    // Linkedlist is ordered by insertion
    private final SortedMap<Long, LinkedHashMap<String, Order>> bidOrderBook = new TreeMap<>();
    private final SortedMap<Long, LinkedHashMap<String, Order>> askOrderBook = new TreeMap<>();
    // orderId -> price, used so we can easily find the order in the respective order book
    private final Map<String, Order> orderIdPriceLookup = new HashMap<>();

    /**
     * Adds an order to the order book
     *
     * @param order Order instance
     * @returns A list of all the matching trades, which include, the price, quantity, bidId, and askId. If no
     * matches are found, an empty list is returned.
     */
    public List<Trade> addOrder( Order order )
    {
        List<Trade> matches =
                matchPrice( order.getOrderId(), order.getPrice(), order.getQuantity(), order.getTradeType() );

        if ( !matches.isEmpty() )
        {
            return matches;
        }

        // Add the order to the lookup map for quick lookups
        orderIdPriceLookup.put( order.getOrderId(), order );

        // BID, someone wants to buy
        if ( order.getTradeType().equals( TradeType.BID ) )
        {
            // Add the order to the bid book
            bidOrderBook.computeIfAbsent( order.getPrice(), k -> new LinkedHashMap<>() )
                    .put( order.getOrderId(), order );
        }
        // ASK, someone wants to sell
        else if ( order.getTradeType().equals( TradeType.ASK ) )
        {
            // Add the order to the ask book
            askOrderBook.computeIfAbsent( order.getPrice(), k -> new LinkedHashMap<>() )
                    .put( order.getOrderId(), order );
        }

        return List.of();
    }

    public boolean cancel( String orderId )
    {
        // Find the price
        if ( !orderIdPriceLookupContains( orderId ) )
        {
            return false;
        }

        final Order order = orderIdPriceLookup.get( orderId );

        if (order.getTradeType().equals( TradeType.BID ))
        {
            final LinkedHashMap<String, Order> ordersAtPrice = bidOrderBook.get( order.getPrice() );
            ordersAtPrice.remove( orderId );
            // If theres no orders for the given price, remove it.
            if ( ordersAtPrice.isEmpty() )
            {
                bidOrderBook.remove( order.getPrice() );
            }
        }
        else if (order.getTradeType().equals( TradeType.ASK ))
        {
            final LinkedHashMap<String, Order> ordersAtPrice = askOrderBook.get( order.getPrice() );
            ordersAtPrice.remove( orderId );
            // If theres no orders for the given price, remove it.
            if ( ordersAtPrice.isEmpty() )
            {
                askOrderBook.remove( order.getPrice() );
            }
        }

        orderIdPriceLookup.remove( orderId );
        return true;
    }

    public Optional<Order> getBestBid()
    {
        // Sorted by size, we want to best bid, which is the most someone is willing to buy
        if ( bidOrderBook.isEmpty() )
        {
            return Optional.empty();
        }
        final LinkedHashMap<String, Order> bestBids = bidOrderBook.get( bidOrderBook.lastKey() );
        // Get the earliest bid of that value
        final Order bestBid = bestBids.get( bestBids.keySet().iterator().next() );
        return Optional.of( bestBid );
    }

    public Optional<Order> getBestAsk()
    {
        if ( askOrderBook.isEmpty() )
        {
            return Optional.empty();
        }
        final LinkedHashMap<String, Order> bestAsks = askOrderBook.get( askOrderBook.firstKey() );
        // Get the earliest ask of that value
        final Order bestAsk = bestAsks.get( bestAsks.keySet().iterator().next() );
        return Optional.of( bestAsk );
    }

    public Optional<Long> getQuantity( final String orderId )
    {
        Order order = orderIdPriceLookup.get( orderId );
        if (order == null)
        {
            return Optional.empty();
        }

        if ( order.getTradeType().equals( TradeType.ASK ) )
        {
            return Optional.of( askOrderBook.get( order.getPrice() ).get( orderId ).getQuantity() );
        }
        else if ( order.getTradeType().equals( TradeType.BID ) )
        {
            return Optional.of( bidOrderBook.get( order.getPrice() ).get( orderId ).getQuantity() );
        }

        return Optional.empty();
    }

    public int getBidOrderBookSize()
    {
        return bidOrderBook.size();
    }

    public int getAskOrderBookSize()
    {
        return askOrderBook.size();
    }

    public boolean orderIdPriceLookupContains( String orderId )
    {
        return orderIdPriceLookup.containsKey( orderId );
    }

    public int orderIdPriceLookupSize()
    {
        return orderIdPriceLookup.size();
    }

    private List<Trade> matchPrice( final String orderId, final long price, final long quantity,
                                    final TradeType tradeType )
    {
        final var matches = new ArrayList<Trade>();
        final AtomicLong quantityLeft = new AtomicLong( quantity );
        // BID, someone wants to buy
        if ( tradeType.equals( TradeType.BID ) )
        {
            // While these quantity left to match, and there are asks left to match against
            // and those asks have a price less than or equal to the new bid price, then keep looping
            while ( quantityLeft.get() > 0 && getBestAsk().isPresent() && getBestAsk().get().getPrice() <= price )
            {
                // Someone wants to buy, is there someone selling at this price or lower?
                getBestAsk().ifPresent( bestAskOrder -> {
                    if ( bestAskOrder.getPrice() <= price )
                    {
                        long resolvedQuantity = Math.min( bestAskOrder.getQuantity(), quantityLeft.get() );
                        // We use the price that was there first, so the ask price
                        matches.add( new Trade( bestAskOrder.getPrice(), resolvedQuantity, orderId,
                                bestAskOrder.getOrderId() ) );

                        // We need to determine how much of the bestAsk was used...
                        if (bestAskOrder.getQuantity() == resolvedQuantity )
                        {
                            // All of it was used, simply cancel and remove from the lookup
                            cancel( bestAskOrder.getOrderId() );
                            orderIdPriceLookup.remove( bestAskOrder.getOrderId() );
                        }
                        else
                        {
                            // If all of it was not used, we should update its quantity
                            // Not remove as that will ruin the time ordering
                            bestAskOrder.setQuantity( bestAskOrder.getQuantity() - resolvedQuantity );
                        }
                        quantityLeft.set( quantityLeft.get() - resolvedQuantity );
                    }
                } );
            }
        }
        // ASK, someone wants to sell
        else if ( tradeType.equals( TradeType.ASK ) )
        {
            // While these quantity left to match, and there are bids left to match against
            // and those bids have a price greater than or equal to the new ask price, then keep looping
            while ( quantityLeft.get() > 0 && getBestBid().isPresent() && getBestBid().get().getPrice() >= price )
            {
                // Someone wants to sell, is there someone buying at this price or higher?
                getBestBid().ifPresent( bestBidOrder -> {
                    if ( bestBidOrder.getPrice() >= price )
                    {
                        long resolvedQuantity = Math.min( bestBidOrder.getQuantity(), quantityLeft.get() );
                        // We use the price that was there first, so the bid price
                        matches.add( new Trade( bestBidOrder.getPrice(), resolvedQuantity, bestBidOrder.getOrderId(),
                                orderId ) );

                        // We need to determine how much of the bestBid was used...
                        if (bestBidOrder.getQuantity() == resolvedQuantity )
                        {
                            // All of it was used, simply cancel and remove from the lookup
                            cancel( bestBidOrder.getOrderId() );
                            orderIdPriceLookup.remove( bestBidOrder.getOrderId() );
                        }
                        else
                        {
                            // If all of it was not used, we should update its quantity
                            // Not remove as that will ruin the time ordering
                            bestBidOrder.setQuantity( bestBidOrder.getQuantity() - resolvedQuantity );
                        }
                        // Update the quantity left to fill
                        quantityLeft.set( quantityLeft.get() - resolvedQuantity );
                    }
                } );
            }
        }

        // If the quantity left is > 0, then we need to add the remaining quantity to the order book
        if ( quantityLeft.get() > 0 )
        {
            // Add the remaining quantity to the order book
            Order remainingOrder = new Order( tradeType, orderId, price, quantityLeft.get() );
            if ( tradeType.equals( TradeType.BID ) )
            {
                bidOrderBook.computeIfAbsent( price, k -> new LinkedHashMap<>() )
                        .put( orderId, remainingOrder );
                orderIdPriceLookup.put( orderId, remainingOrder );
            }
            else if ( tradeType.equals( TradeType.ASK ) )
            {
                askOrderBook.computeIfAbsent( price, k -> new LinkedHashMap<>() )
                        .put( orderId, remainingOrder );
                orderIdPriceLookup.put( orderId, remainingOrder );

            }
        }

        return matches;
    }

}
