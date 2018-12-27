package vu.de.signalerror.base.event;

public interface EventSource
{
	public void addConsumer( EventConsumer eventConsumer );

	public void removeConsumer( EventConsumer eventConsumer );

	public void removeAllConsumers( );

	public void fireEvent( Event event );
}
