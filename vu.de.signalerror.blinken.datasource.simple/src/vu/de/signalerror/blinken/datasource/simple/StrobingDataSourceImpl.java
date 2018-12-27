package vu.de.signalerror.blinken.datasource.simple;

import java.awt.Frame;

import org.apache.log4j.Logger;
import org.osgi.service.component.ComponentContext;

import vu.de.signalerror.base.ApplicationMaster;
import vu.de.signalerror.blinken.BlinkenFrame;
import vu.de.signalerror.blinken.BlinkenViewerDataSource;
import vu.de.signalerror.blinken.datasource.simple.ui.DelayChangeListener;
import vu.de.signalerror.blinken.datasource.simple.ui.DelayChanger;
import vu.de.signalerror.blinken.impl.BlinkenFrameImpl;

public class StrobingDataSourceImpl extends BlinkenViewerDataSource implements DelayChangeListener
{
	private static Logger logger = Logger.getLogger(StrobingDataSourceImpl.class);

	private long strobeInterval = 100;
	private StroboThread stroboThread;

	boolean strobe_on = false;
	private BlinkenFrame strobeFrame;
	private BlinkenFrame nonStrobeFrame;

	private Frame configFrame;

	private ApplicationMaster applicationMaster;

	protected void activate( ComponentContext componentContext )
	{
		applicationMaster = (ApplicationMaster) componentContext.locateService("ApplicationMaster");
		initGUI();
	}

	private void initGUI( )
	{
		configFrame = new DelayChanger(this, applicationMaster, "Select Delay for Strobo");
		// configFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		// Display the window.
		configFrame.pack();
	}

	protected void deactivate( ComponentContext componentContext )
	{
		stopFrameGeneration();
	}

	@Override
	public void startFrameGeneration( )
	{
		if (configFrame != null)
			configFrame.setVisible(true);

		if (blinkenConfig != null)
		{
			this.stroboThread = new StroboThread(blinkenConfig.getRows(), blinkenConfig.getColumns());
			stroboThread.start();
		}
		else
			logger.warn("No BlinkenConfiguration set, yet. Will not start FrameGeneration.");
	}

	@Override
	public void stopFrameGeneration( )
	{
		if (configFrame != null)
			configFrame.setVisible(false);

		if (stroboThread != null)
			stroboThread.stopIt();
		stroboThread = null;
	}

	public void setStrobeInterval( long strobeInterval )
	{
		this.strobeInterval = strobeInterval;
	}

	@Override
	public BlinkenFrame getCurrentFrame( )
	{
		if (strobe_on)
			return strobeFrame;
		else
			return nonStrobeFrame;
	}

	@Override
	public void onNewDelayValue( int delayValue )
	{
		this.strobeInterval = delayValue * 6;
	}

	private class StroboThread extends Thread
	{
		private boolean running = true;

		public void stopIt( )
		{
			this.running = false;
		}

		public StroboThread(int rows, int columns)
		{
			// generate a frame that is always on.
			strobeFrame = new BlinkenFrameImpl();
			nonStrobeFrame = new BlinkenFrameImpl();
			Boolean[][] data_on = new Boolean[blinkenConfig.getRows()][blinkenConfig.getColumns()];
			Boolean[][] data_off = new Boolean[blinkenConfig.getRows()][blinkenConfig.getColumns()];
			for (int i = 0; i < blinkenConfig.getRows(); i++)
			{
				for (int j = 0; j < blinkenConfig.getColumns(); j++)
				{
					data_on[i][j] = true;
					data_off[i][j] = false;
				}
			}
			strobeFrame.setFrameData(data_on);
			nonStrobeFrame.setFrameData(data_off);
		}

		@Override
		public void run( )
		{
			while (running)
			{
				try
				{
					Thread.sleep(strobeInterval);
					strobe_on = !strobe_on;
					// System.err.println( System.currentTimeMillis() );
					fireNewFrame();
				}
				catch (Exception e)
				{
					logger.error("Error while strobing:", e);
				}
				;
			}
		}
	}

}
