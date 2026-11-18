package org.finance.capture;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class CaptureEventSinkTest
{
    private CaptureEventSink captureEventSink;

    @BeforeEach
    void setup()
    {
        captureEventSink = new CaptureEventSink();
    }

    @Test
    void shouldBeEmptyInitially()
    {
        assert captureEventSink.getEvents().isEmpty();
    }

    @Test
    void shouldAddTradeEvent()
    {
        Event event = new Trade( 100, 50, "bidId", "askId" );
        captureEventSink.accept( event );

        assert captureEventSink.getEvents().contains( event );
    }

    @Test
    void shouldAddCancelAcceptedEvent()
    {
        Event event = new CancelAccepted();
        captureEventSink.accept( event );

        assert captureEventSink.getEvents().contains( event );
    }

    @Test
    void shouldAddCancelRejectedEvent()
    {
        Event event = new CancelRejected();
        captureEventSink.accept( event );

        assert captureEventSink.getEvents().contains( event );
    }
}
