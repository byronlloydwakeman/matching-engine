package org.finance.capture;

import java.util.LinkedList;
import java.util.List;

public class CaptureEventSink implements EventSink
{
    // Keeps insertion order
    private final LinkedList<Event> events = new LinkedList<>();
    @Override
    public void accept( Event event )
    {
        events.add( event );
    }

    @Override
    public List<Event> getEvents()
    {
        return events;
    }
}
