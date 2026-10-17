package org.finance;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class OrderBookTest
{
    private OrderBook orderBook;

    @BeforeEach
    void setup()
    {
        orderBook = new OrderBook();
    }

    // Test cases
    // Exact match in price and quantity ✅
    // Match in price but not quantity, partial fill
    // Match in price but not quantity, fill with multiple orders
    // Match in price but with stale values
    // Consecutive matches not ordered
    // Do not match in price ✅
    // Would be a match but order is cancelled
    // Several matches for a single bid/ask with some quantity remaining

    @Test
    void shouldGetBestBid()
    {
        final var bid5At101 = new Order( TradeType.BID, "bid5At101", 101, 5 );
        var resultBid5At101 = orderBook.addOrder( bid5At101 );

        Assertions.assertEquals( 0, resultBid5At101.size() );
        var bestBid = orderBook.getBestBid();
        Assertions.assertTrue( bestBid.isPresent() );
        Assertions.assertEquals( bid5At101, bestBid.get() );
    }

    @Test
    void shouldGetBestBidOfManyBids()
    {
        final var bid5At100 = new Order( TradeType.BID, "bid5At100", 100, 5 );
        final var bid5At101 = new Order( TradeType.BID, "bid5At101", 101, 5 );
        var resultBid5At100 = orderBook.addOrder( bid5At100 );
        var resultBid5At101 = orderBook.addOrder( bid5At101 );

        // Should be no matches
        Assertions.assertEquals( 0, resultBid5At101.size() );
        Assertions.assertEquals( 0, resultBid5At100.size() );

        var bestBid = orderBook.getBestBid();
        Assertions.assertTrue( bestBid.isPresent() );
        // Gets the later but better bid
        Assertions.assertEquals( bid5At101, bestBid.get() );
    }

    @Test
    void shouldGetBestBidUsingTime()
    {
        final var bid5At100 = new Order( TradeType.BID, "bid5At100", 100, 5 );
        final var bid5At100_2 = new Order( TradeType.BID, "bid5At100_2", 100, 5 );
        var resultBid5At100 = orderBook.addOrder( bid5At100 );
        var resultBid5At100_2 = orderBook.addOrder( bid5At100_2 );

        // Should be no matches
        Assertions.assertEquals( 0, resultBid5At100.size() );
        Assertions.assertEquals( 0, resultBid5At100_2.size() );

        var bestBid = orderBook.getBestBid();
        Assertions.assertTrue( bestBid.isPresent() );
        // Get the earliest bid
        Assertions.assertEquals( bid5At100, bestBid.get() );
    }

    @Test
    void shouldGetEmptyListForBestBid()
    {
        var bestBid = orderBook.getBestBid();
        Assertions.assertFalse( bestBid.isPresent() );
    }

    @Test
    void shouldGetBestAsk()
    {
        final var ask5At101 = new Order( TradeType.ASK, "ask5At101", 101, 5 );
        var resultAsk5At101 = orderBook.addOrder( ask5At101 );

        Assertions.assertEquals( 0, resultAsk5At101.size() );
        var bestAsk = orderBook.getBestAsk();
        Assertions.assertTrue( bestAsk.isPresent() );
        Assertions.assertEquals( ask5At101, bestAsk.get() );
    }

    @Test
    void shouldGetBestAskOfManyAsks()
    {
        final var ask5At100 = new Order( TradeType.ASK, "ask5At100", 100, 5 );
        final var ask5At101 = new Order( TradeType.ASK, "ask5At101", 101, 5 );
        var resultAsk5At101 = orderBook.addOrder( ask5At101 );
        var resultAsk5At100 = orderBook.addOrder( ask5At100 );

        // Should be no matches
        Assertions.assertEquals( 0, resultAsk5At101.size() );
        Assertions.assertEquals( 0, resultAsk5At100.size() );

        var bestAsk = orderBook.getBestAsk();
        Assertions.assertTrue( bestAsk.isPresent() );
        // Gets the earlier but better ask
        Assertions.assertEquals( ask5At100, bestAsk.get() );
    }

    @Test
    void shouldGetBestAskUsingTime()
    {
        final var ask5At100 = new Order( TradeType.ASK, "ask5At100", 100, 5 );
        final var ask5At100_2 = new Order( TradeType.ASK, "ask5At100_2", 100, 5 );
        var resultAsk5At100 = orderBook.addOrder( ask5At100 );
        var resultAsk5At100_2 = orderBook.addOrder( ask5At100_2 );

        // Should be no matches
        Assertions.assertEquals( 0, resultAsk5At100.size() );
        Assertions.assertEquals( 0, resultAsk5At100_2.size() );

        var bestAsk = orderBook.getBestAsk();
        Assertions.assertTrue( bestAsk.isPresent() );
        // Get the earliest ask
        Assertions.assertEquals( ask5At100, bestAsk.get() );
    }

    @Test
    void shouldGetEmptyListForBestAsk()
    {
        var bestAsk = orderBook.getBestAsk();
        Assertions.assertFalse( bestAsk.isPresent() );
    }

    @Test
    void verifyLookupDataIsValidAfterAddingBidOrder()
    {
        final var bid5At100 = new Order( TradeType.BID, "bid5At100", 100, 5 );
        final var bid5At100_2 = new Order( TradeType.BID, "bid5At100_2", 100, 5 );

        var resultBid5At100 = orderBook.addOrder( bid5At100 );
        var resultBid5At100_2 = orderBook.addOrder( bid5At100_2 );

        Assertions.assertEquals( 0, resultBid5At100.size() );
        Assertions.assertEquals( 0, resultBid5At100_2.size() );

        Assertions.assertTrue( orderBook.orderIdPriceLookupContains( "bid5At100" ) );
        Assertions.assertTrue( orderBook.orderIdPriceLookupContains( "bid5At100_2" ) );

        Assertions.assertEquals( 2, orderBook.orderIdPriceLookupSize() );
    }

    @Test
    void verifyLookupDataIsValidAfterAddingAskOrder()
    {
        final var ask5At100 = new Order( TradeType.ASK, "ask5At100", 100, 5 );
        final var ask5At100_2 = new Order( TradeType.ASK, "ask5At100_2", 100, 5 );

        var resultAsk5At100 = orderBook.addOrder( ask5At100 );
        var resultAsk5At100_2 = orderBook.addOrder( ask5At100_2 );

        Assertions.assertEquals( 0, resultAsk5At100.size() );
        Assertions.assertEquals( 0, resultAsk5At100_2.size() );

        Assertions.assertTrue( orderBook.orderIdPriceLookupContains( "ask5At100" ) );
        Assertions.assertTrue( orderBook.orderIdPriceLookupContains( "ask5At100_2" ) );

        Assertions.assertEquals( 2, orderBook.orderIdPriceLookupSize() );
    }

    @Test
    void verifyLookupDataIsEmpty()
    {
        Assertions.assertFalse( orderBook.orderIdPriceLookupContains( "ask5At100" ) );
        Assertions.assertFalse( orderBook.orderIdPriceLookupContains( "ask5At100_2" ) );
        Assertions.assertEquals( 0, orderBook.orderIdPriceLookupSize() );
    }

    @Test
    void shouldCancelBids()
    {
        final var bid5At100 = new Order( TradeType.BID, "bid5At100", 100, 5 );
        final var bid5At100_2 = new Order( TradeType.BID, "bid5At100_2", 100, 5 );

        var resultBid5At100 = orderBook.addOrder( bid5At100 );
        var resultBid5At100_2 = orderBook.addOrder( bid5At100_2 );

        // No matches
        Assertions.assertEquals( 0, resultBid5At100.size() );
        Assertions.assertEquals( 0, resultBid5At100_2.size() );

        Assertions.assertTrue( orderBook.orderIdPriceLookupContains( "bid5At100" ) );
        Assertions.assertTrue( orderBook.orderIdPriceLookupContains( "bid5At100_2" ) );

        Assertions.assertEquals( 2, orderBook.orderIdPriceLookupSize() );
        // 1 as theyre the same price
        Assertions.assertEquals( 1, orderBook.getBidOrderBookSize() );

        Assertions.assertTrue( orderBook.cancel( "bid5At100" ) );
        Assertions.assertTrue( orderBook.cancel( "bid5At100_2" ) );

        Assertions.assertFalse( orderBook.orderIdPriceLookupContains( "bid5At100" ) );
        Assertions.assertFalse( orderBook.orderIdPriceLookupContains( "bid5At100_2" ) );

        Assertions.assertEquals( 0, orderBook.orderIdPriceLookupSize() );
        Assertions.assertEquals( 0, orderBook.getBidOrderBookSize() );
    }

    @Test
    void shouldCancelAsks()
    {
        final var ask5At100 = new Order( TradeType.ASK, "ask5At100", 100, 5 );
        final var ask5At100_2 = new Order( TradeType.ASK, "ask5At100_2", 100, 5 );

        var resultAsk5At100 = orderBook.addOrder( ask5At100 );
        var resultAskAt100_2 = orderBook.addOrder( ask5At100_2 );

        // No matches
        Assertions.assertEquals( 0, resultAsk5At100.size() );
        Assertions.assertEquals( 0, resultAskAt100_2.size() );

        Assertions.assertTrue( orderBook.orderIdPriceLookupContains( "ask5At100" ) );
        Assertions.assertTrue( orderBook.orderIdPriceLookupContains( "ask5At100_2" ) );

        Assertions.assertEquals( 2, orderBook.orderIdPriceLookupSize() );
        // 1 as theyre the same price
        Assertions.assertEquals( 1, orderBook.getAskOrderBookSize() );

        Assertions.assertTrue( orderBook.cancel( "ask5At100" ) );
        Assertions.assertTrue( orderBook.cancel( "ask5At100_2" ) );

        Assertions.assertFalse( orderBook.orderIdPriceLookupContains( "ask5At100" ) );
        Assertions.assertFalse( orderBook.orderIdPriceLookupContains( "ask5At100_2" ) );

        Assertions.assertEquals( 0, orderBook.orderIdPriceLookupSize() );
        Assertions.assertEquals( 0, orderBook.getAskOrderBookSize() );
    }

    @Test
    void shouldGetCorrectBidQuantity()
    {
        final var bid5At101 = new Order( TradeType.BID, "bid5At101", 101, 5L );

        orderBook.addOrder( bid5At101 );

        Assertions.assertEquals( 5L, orderBook.getQuantity( "bid5At101" ).get() );
    }

    @Test
    void shouldGetCorrectAskQuantity()
    {
        final var ask5At101 = new Order( TradeType.ASK, "ask5At101", 101, 5L );

        orderBook.addOrder( ask5At101 );

        Assertions.assertEquals( 5L, orderBook.getQuantity( "ask5At101" ).get() );
    }

    // Add Order
    // Bid 5 @ 101
    // Ask 5 @ 101
    // Valid match
    @Test
    void shouldMatchWithEqualPrice()
    {
        final var bid5At101 = new Order( TradeType.BID, "bid5At101", 101, 5 );
        final var ask5At101 = new Order( TradeType.ASK, "ask5At101", 101, 5 );

        // The entire flow
        // addOrder( bid1 )
        var resultBid5At101 = orderBook.addOrder( bid5At101 );
        // Bid1 is added to the order book
        // Bid1 is added to lookup.
        // Lookup only contains bid1
        Assertions.assertTrue( orderBook.orderIdPriceLookupContains( "bid5At101" ) );
        Assertions.assertEquals( 1, orderBook.orderIdPriceLookupSize() );
        Assertions.assertEquals( 1, orderBook.getBidOrderBookSize() );
        // Returns an empty list, no matches
        Assertions.assertEquals( 0, resultBid5At101.size() );

        // addOrder( ask1 )
        var resultAsk5At101 = orderBook.addOrder( ask5At101 );
        // Ask1 matches bid1
        // Returns 1 trade with bid1 price, min(bid1.quantity, ask1.quantity), ask1Id, bid1Id
        Assertions.assertEquals( 1, resultAsk5At101.size() );
        Assertions.assertEquals( 101, resultAsk5At101.get( 0 ).getPrice() );
        Assertions.assertEquals( 5, resultAsk5At101.get( 0 ).getQuantity() );
        Assertions.assertEquals( "bid5At101", resultAsk5At101.get( 0 ).getBidId() );
        Assertions.assertEquals( "ask5At101", resultAsk5At101.get( 0 ).getAskId() );

        // bidOrderBook should be empty
        Assertions.assertTrue( orderBook.getBestBid().isEmpty() );
        // askOrderBook should be empty
        Assertions.assertTrue( orderBook.getBestAsk().isEmpty() );
        // orderIdPriceLookup should be empty
        Assertions.assertEquals( 0, orderBook.orderIdPriceLookupSize() );
    }

    // Bid 5 @ 101
    // Ask 5 @ 102
    // No match
    @Test
    void shouldNotMatchWhenAskPriceIsHigherThanBidPrice()
    {
        final var bid5At101 = new Order( TradeType.BID, "bid5At101", 101, 5 );
        final var ask5At102 = new Order( TradeType.ASK, "ask5At102", 102, 5 );

        // The entire flow
        // addOrder( bid1 )
        var resultBid5At101 = orderBook.addOrder( bid5At101 );
        // Bid1 is added to the order book
        // Bid1 is added to lookup
        // Lookup only contains bid1
        Assertions.assertTrue( orderBook.orderIdPriceLookupContains( "bid5At101" ) );
        Assertions.assertEquals( 1, orderBook.orderIdPriceLookupSize() );
        Assertions.assertEquals( 1, orderBook.getBidOrderBookSize() );
        // Returns an empty list, no matches
        Assertions.assertEquals( 0, resultBid5At101.size() );

        // addOrder( ask1 )
        var resultAsk5At101 = orderBook.addOrder( ask5At102 );

        // Lookup should now have both
        Assertions.assertEquals( 2, orderBook.orderIdPriceLookupSize() );
        Assertions.assertEquals( 1, orderBook.getBidOrderBookSize() );
        Assertions.assertEquals( 1, orderBook.getAskOrderBookSize() );
        // Ask is 102, which is > 101, so there is no match
        Assertions.assertEquals( 0, resultAsk5At101.size() );
    }

    // Bid 5 @ 101
    // Ask 1 @ 101
    // Valid match
    // Should remain; Bid 4 @ 101
    @Test
    void shouldLeaveRemainingBidQuantityAfterPartialMatch()
    {
        final var bid5At101 = new Order( TradeType.BID, "bid5At101", 101, 5 );
        final var ask1At101 = new Order( TradeType.ASK, "ask1At101", 101, 1 );

        // The entire flow
        // addOrder( bid1 )
        var resultBid5At101 = orderBook.addOrder( bid5At101 );
        // Bid1 is added to the order book
        // Bid1 is added to lookup
        // Lookup only contains bid1
        Assertions.assertTrue( orderBook.orderIdPriceLookupContains( "bid5At101" ) );
        Assertions.assertEquals( 1, orderBook.orderIdPriceLookupSize() );
        Assertions.assertEquals( 1, orderBook.getBidOrderBookSize() );
        // Returns an empty list, no matches
        Assertions.assertEquals( 0, resultBid5At101.size() );

        // addOrder( ask1 )
        var resultAsk1At101 = orderBook.addOrder( ask1At101 );

        // Bids match
        Assertions.assertEquals( 1, resultAsk1At101.size() );
        // However, the bid with a quantity of four should still be present
        final var nextBestBid = orderBook.getBestBid().get();
        Assertions.assertEquals( 4, nextBestBid.getQuantity() );
        Assertions.assertEquals( 101, nextBestBid.getPrice() );
        Assertions.assertEquals( "bid5At101", nextBestBid.getOrderId() );
        Assertions.assertEquals( TradeType.BID, nextBestBid.getTradeType() );


        // Still the one bid
        Assertions.assertEquals( 1, orderBook.getBidOrderBookSize() );
        // But no asks
        Assertions.assertEquals( 0, orderBook.getAskOrderBookSize() );
        // And only one lookup item
        Assertions.assertEquals( 1, orderBook.orderIdPriceLookupSize() );
    }

    // Bid 5 @ 101
    // Bid 5 @ 100
    // Ask 5 @ 100
    // Valid match
    // Should remain; Bid 5 : 100
    @Test
    void shouldMatchAgainstBestBidPriceFirst()
    {
        final var bid5At101 = new Order( TradeType.BID, "bid5At101", 101, 5 );
        final var bid5At100 = new Order( TradeType.BID, "bid5At100", 100, 5 );
        final var ask5At100 = new Order( TradeType.ASK, "ask5At100", 100, 5 );

        // The entire flow
        // addOrder( bid1 )
        var resultBid5At101 = orderBook.addOrder( bid5At101 );
        // Bid1 is added to the order book
        // Bid1 is added to lookup
        // Lookup only contains bid1
        Assertions.assertTrue( orderBook.orderIdPriceLookupContains( "bid5At101" ) );
        Assertions.assertEquals( 1, orderBook.orderIdPriceLookupSize() );
        // Returns an empty list, no matches
        Assertions.assertEquals( 0, resultBid5At101.size() );

        // addOrder( bid2 )
        var resultBid5At100 = orderBook.addOrder( bid5At100 );
        // Bid2 is added to the order book
        // Bid2 is added to lookup
        // Lookup contains bid1 and bid2
        Assertions.assertTrue( orderBook.orderIdPriceLookupContains( "bid5At100" ) );
        Assertions.assertTrue( orderBook.orderIdPriceLookupContains( "bid5At101" ) );
        Assertions.assertEquals( 2, orderBook.orderIdPriceLookupSize() );
        // Returns an empty list, no matches
        Assertions.assertEquals( 0, resultBid5At100.size() );

        // addOrder( ask1 )
        var resultAsk5At101 = orderBook.addOrder( ask5At100 );
        // Match between bid1 and ask1, despite newer orders
        Assertions.assertEquals( 1, resultAsk5At101.size() );
        var match = resultAsk5At101.get( 0 );
        Assertions.assertEquals( 101, match.getPrice() );
        Assertions.assertEquals( 5, match.getQuantity() );
        Assertions.assertEquals( "bid5At101", match.getBidId() );
        Assertions.assertEquals( "ask5At100", match.getAskId() );

        // Should only still have bid2
        Assertions.assertEquals( 1, orderBook.getBidOrderBookSize() );
        var nextBestBid = orderBook.getBestBid().get();
        Assertions.assertEquals( 100, nextBestBid.getPrice() );
        Assertions.assertEquals( 5, nextBestBid.getQuantity() );
        Assertions.assertEquals( "bid5At100", nextBestBid.getOrderId() );
        Assertions.assertEquals( TradeType.BID, nextBestBid.getTradeType() );

        // Only a lookup size of 1
        Assertions.assertEquals( 1, orderBook.orderIdPriceLookupSize() );
    }

    // Bid 5 @ 102, id 1
    // Bid 5 @ 102, id 2
    // Ask 5 @ 101
    // Valid match
    // Should remain; Bid 5 @ 102, id 2
    @Test
    void shouldMatchEarlierOrderFirstWhenBidPricesAreEqual()
    {
        final var bid5At102Id1 = new Order( TradeType.BID, "bid5At102Id1", 102, 5 );
        final var bid5At102Id2 = new Order( TradeType.BID, "bid5At102Id2", 102, 5 );
        final var ask5At101Id1 = new Order( TradeType.ASK, "ask5At101Id1", 101, 5 );
        final var ask5At101Id2 = new Order( TradeType.ASK, "ask5At101Id2", 101, 5 );

        orderBook.addOrder( bid5At102Id1 );
        orderBook.addOrder( bid5At102Id2 );

        var resultAsk5At101Id1 = orderBook.addOrder( ask5At101Id1 );
        var resultAsk5At101Id2 = orderBook.addOrder( ask5At101Id2 );

//        assertSingleMatch( resultAsk5At101Id1, "bid5At102Id1", "ask5At101Id1" );
//        assertSingleMatch( resultAsk5At101Id2, "bid5At102Id2", "ask5At101Id2" );
    }
}
