package org.finance.capture;

import java.util.List;

public interface EventSink
{
    void accept( Event event );
    List<Event> getEvents();
}
