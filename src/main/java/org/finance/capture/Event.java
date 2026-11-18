package org.finance.capture;

public sealed interface Event permits Trade, CancelAccepted, CancelRejected
{
}
