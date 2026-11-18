package org.finance.command;

import org.finance.TradeType;

public record NewOrder(long orderId, TradeType tradeType, long priceInPence, long qty, long enqueueNanos) implements Command {}