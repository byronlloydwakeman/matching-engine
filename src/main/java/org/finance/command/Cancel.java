package org.finance.command;

public record Cancel(long orderId, long enqueueNanos) implements Command {}
