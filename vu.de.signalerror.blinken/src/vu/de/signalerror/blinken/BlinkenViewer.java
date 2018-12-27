package vu.de.signalerror.blinken;

import vu.de.signalerror.base.event.EventConsumer;

public interface BlinkenViewer extends EventConsumer
{
	public void setActive( boolean active );

	public boolean isActive( );

	public void setBlinkenConfig( BlinkenConfig blinkenConfig );
}
