# Matching Engine

A small Java order-book matching engine for buy (bid) and sell (ask) orders. It matches incoming orders against the best available prices and returns the resulting trades.

## Requirements

- Java 17 or later
- Maven

## Build and test

Run the test suite with Maven:

```bash
mvn test
```

Build the project:

```bash
mvn package
```

## Usage

Create an `OrderBook` and submit orders with `addOrder`. Prices are integer pence and quantities are whole-number units. A bid matches asks priced at or below its limit; an ask matches bids priced at or above its limit. Trades execute at the price of the resting order.

```java
import org.finance.Order;
import org.finance.OrderBook;
import org.finance.Trade;
import org.finance.TradeType;

import java.util.List;

OrderBook orderBook = new OrderBook();

List<Trade> noTrades = orderBook.addOrder(
        new Order(TradeType.BID, "bid-1", 101, 5));

List<Trade> trades = orderBook.addOrder(
        new Order(TradeType.ASK, "ask-1", 100, 3));
// One trade: price 101, quantity 3, bid ID "bid-1", ask ID "ask-1".
// The remaining quantity of bid-1 stays in the order book.
```

Orders at the same price are matched in insertion order. Better-priced bids and asks take priority over worse-priced orders.

## Order-book operations

- `addOrder(Order)` submits an order and returns the trades generated. Any unfilled quantity is added to the book.
- `cancel(String orderId)` removes an outstanding order and returns whether it was found.
- `getBestBid()` and `getBestAsk()` return the best outstanding order as an `Optional`.
- `getQuantity(String orderId)` returns the remaining quantity for an outstanding order as an `Optional`.

`Trade` exposes its execution price, quantity, bid ID, and ask ID through getters. `TradeType` has `BID` and `ASK` values.

The project currently provides the matching-engine classes and tests; `Main` does not yet implement a command-line interface.
