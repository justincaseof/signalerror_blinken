package vu.de.signalerror.base.event.impl;

import vu.de.signalerror.base.event.Event;
import vu.de.signalerror.base.event.EventSource;
import vu.de.signalerror.base.event.EventType;

public class EventImpl implements Event
{
	private EventType eventType;
	private EventSource eventSource;

	public EventImpl(EventType eventType, EventSource source)
	{
		this.eventType = eventType;
		this.eventSource = source;
	}

	public EventType getEventType( )
	{
		return eventType;
	}

	public EventSource getEventSource( )
	{
		return eventSource;
	}

}
