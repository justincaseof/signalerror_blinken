package vu.de.signalerror.blinken.datasource.simple;

import java.awt.Frame;
import java.util.Random;

import org.apache.log4j.Logger;
import org.osgi.service.component.ComponentContext;

import vu.de.signalerror.base.ApplicationMaster;
import vu.de.signalerror.blinken.BlinkenFrame;
import vu.de.signalerror.blinken.BlinkenViewerDataSource;
import vu.de.signalerror.blinken.datasource.simple.ui.DelayChangeListener;
import vu.de.signalerror.blinken.datasource.simple.ui.DelayChanger;
import vu.de.signalerror.blinken.impl.BlinkenFrameImpl;

public class RandomPixelChangeDataSourceImpl extends BlinkenViewerDataSource implements DelayChangeListener
{
	private static Logger logger = Logger.getLogger(RandomPixelChangeDataSourceImpl.class);

	private long changeInterval = 1;
	private PixelChangeThread pixelChangeThread;

	private BlinkenFrame currentFrame;

	private Random rand = new Random();

	private Frame configFrame;

	private ApplicationMaster applicationMaster;

	protected void activate( ComponentContext componentContext )
	{
		applicationMaster = (ApplicationMaster) componentContext.locateService("ApplicationMaster");
		initGUI();
	}

	private void initGUI( )
	{
		configFrame = new DelayChanger(this, applicationMaster, "Select Delay for RandomPixelChanger");
		
		//try to fetch old position
//		configFrame.set
		
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
			this.pixelChangeThread = new PixelChangeThread(blinkenConfig.getRows(), blinkenConfig.getColumns());
			pixelChangeThread.start();
		}
		else
			logger.warn("No BlinkenConfiguration set, yet. Will not start FrameGeneration.");
	}

	@Override
	public void stopFrameGeneration( )
	{
		if (configFrame != null)
			configFrame.setVisible(false);

		if (pixelChangeThread != null)
			pixelChangeThread.stopIt();
		pixelChangeThread = null;
	}

	public void setStrobeInterval( long strobeInterval )
	{
		this.changeInterval = strobeInterval;
	}

	@Override
	public BlinkenFrame getCurrentFrame( )
	{
		return currentFrame;
	}

	@Override
	public void onNewDelayValue( int delayValue )
	{
		this.changeInterval = delayValue * 3;
	}

	private class PixelChangeThread extends Thread
	{
		private boolean running = true;

		public void stopIt( )
		{
			this.running = false;
		}

		public PixelChangeThread(int rows, int columns)
		{
			// generate a frame that is always on.
			currentFrame = new BlinkenFrameImpl();
			Boolean[][] data = new Boolean[blinkenConfig.getRows()][blinkenConfig.getColumns()];
			for (int i = 0; i < blinkenConfig.getRows(); i++)
			{
				for (int j = 0; j < blinkenConfig.getColumns(); j++)
				{
					data[i][j] = false;
				}
			}
			currentFrame.setFrameData(data);
		}

		@Override
		public void run( )
		{
			while (running)
			{
				try
				{
					Thread.sleep(changeInterval);

					int randRow = rand.nextInt(blinkenConfig.getRows());
					int randCol = rand.nextInt(blinkenConfig.getColumns());
					currentFrame.togglePixel(randRow, randCol);

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
