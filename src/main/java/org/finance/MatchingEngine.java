package org.finance;

public class MatchingEngine implements Runnable {
    private final OrderBook orderBook;

    public MatchingEngine(OrderBook orderBook) {
        this.orderBook = orderBook;
    }

    @Override
    public void run() {
        // Matching engine logic goes here
    }
}
