package org.finance.capture;

public record Trade(long price, long quantity, String bidId, String askId) implements Event
{
}
