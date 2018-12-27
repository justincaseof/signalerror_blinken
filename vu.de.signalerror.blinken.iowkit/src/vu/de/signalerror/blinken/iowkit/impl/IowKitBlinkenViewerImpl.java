package vu.de.signalerror.blinken.iowkit.impl;

import org.apache.log4j.Logger;

import vu.de.signalerror.base.event.Event;
import vu.de.signalerror.base.event.EventSource;
import vu.de.signalerror.blinken.BlinkenConfig;
import vu.de.signalerror.blinken.BlinkenFrame;
import vu.de.signalerror.blinken.BlinkenViewer;
import vu.de.signalerror.blinken.BlinkenViewerDataSource;

public class IowKitBlinkenViewerImpl implements BlinkenViewer
{

	private static Logger logger = Logger.getLogger(IowKitBlinkenViewerImpl.class);

	private boolean active = false;

	private BlinkenConfig blinkenConfig;

	private BlinkenDriver blinkenDriver;

	public IowKitBlinkenViewerImpl()
	{
		blinkenDriver = new BlinkenDriver();
		if (blinkenDriver.openIow())
			logger.info("Succesfully opened BlinkenDriver.");
		else
			logger.error("Could not open BlinkenDriver.");
	}

	public void setBlinkenConfig( BlinkenConfig blinkenConfig )
	{
		this.blinkenConfig = blinkenConfig;
	}

	@Override
	public boolean isActive( )
	{
		return active;
	}

	@Override
	public void setActive( boolean active )
	{
		this.active = active;
	}

	private void showFrame( BlinkenFrame frame )
	{
		if (active)
		{
			int rows = blinkenConfig.getRows();
			int cols = blinkenConfig.getColumns();
			int iFrame[][] = new int[rows][cols];
			for (int i = 0; i < rows; i++)
			{
				for (int j = 0; j < cols; j++)
					iFrame[i][j] = frame.getPixel(i, j) ? 1 : 0;
			}
			blinkenDriver.refresh_blinken(iFrame);
		}
	}

	@Override
	public void notify( Event event )
	{
		EventSource src = null;
		if (((src = event.getEventSource()) instanceof BlinkenViewerDataSource))
		{
			BlinkenFrame frame = ((BlinkenViewerDataSource) src).getCurrentFrame();
			showFrame(frame);
		}

	}

}
