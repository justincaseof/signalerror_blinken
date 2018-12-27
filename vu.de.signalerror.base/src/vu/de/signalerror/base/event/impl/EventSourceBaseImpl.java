package vu.de.signalerror.base.event.impl;

import java.util.ArrayList;
import java.util.List;

import vu.de.signalerror.base.event.Event;
import vu.de.signalerror.base.event.EventConsumer;
import vu.de.signalerror.base.event.EventSource;

public abstract class EventSourceBaseImpl implements EventSource
{
	private List<EventConsumer> eventConsumers = new ArrayList<EventConsumer>();

	public void addConsumer( EventConsumer eventConsumer )
	{
		synchronized(eventConsumer)
		{
			eventConsumers.add(eventConsumer);
		}
	}

	public void removeConsumer( EventConsumer eventConsumer )
	{
		synchronized(eventConsumer)
		{
			eventConsumers.remove(eventConsumer);
		}
	}

	public void removeAllConsumers( )
	{
		synchronized(eventConsumers)
		{
			eventConsumers.clear();
		}	
	}

	public void fireEvent( Event event )
	{
		synchronized (eventConsumers)
		{
    		for (EventConsumer consumer : eventConsumers)
    			consumer.notify(event);
		}
	}
}
