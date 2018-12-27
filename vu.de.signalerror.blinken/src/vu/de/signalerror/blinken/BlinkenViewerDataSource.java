package vu.de.signalerror.blinken;

import vu.de.signalerror.base.event.EventType;
import vu.de.signalerror.base.event.impl.EventImpl;
import vu.de.signalerror.base.event.impl.EventSourceBaseImpl;

public abstract class BlinkenViewerDataSource extends EventSourceBaseImpl
{

	protected BlinkenConfig blinkenConfig;

	public abstract BlinkenFrame getCurrentFrame( );

	public void setBlinkenConfig( BlinkenConfig config )
	{
		this.blinkenConfig = config;
	}

	public BlinkenConfig getBlinkenConfig( )
	{
		return this.blinkenConfig;
	}

	public abstract void startFrameGeneration( );

	public abstract void stopFrameGeneration( );

	protected void fireNewFrame( )
	{
		EventImpl event = new EventImpl(EventType.NEW_FRAME, this);
		fireEvent(event);
	}

}
