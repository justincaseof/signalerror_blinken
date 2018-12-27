package vu.de.signalerror.blinken.datasrc.blmfile.impl;

import java.awt.Frame;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.osgi.service.component.ComponentContext;

import vu.de.signalerror.base.ApplicationMaster;
import vu.de.signalerror.blinken.BlinkenFrame;
import vu.de.signalerror.blinken.BlinkenViewerDataSource;
import vu.de.signalerror.blinken.datasrc.blmfile.ui.FilePlayerUI;
import vu.de.signalerror.blinken.impl.BlinkenFrameImpl;

public class BlmFileDataSourceImpl extends BlinkenViewerDataSource
{
	private static Logger logger = Logger.getLogger(BlmFileDataSourceImpl.class);

	private BlmFilePlayerThread blmFilePlayerThread;

	private BlinkenFrame currentFrame;

	private File[] blmFiles;
	private boolean blmFileListValid;

	private Frame frame;

	private int updateMultiplyer = 4;

	private ApplicationMaster applicationMaster;

	protected void activate( ComponentContext componentContext )
	{
		applicationMaster = (ApplicationMaster) componentContext.locateService("ApplicationMaster");
		initGUI();
	}

	private void initGUI( )
	{
		frame = new FilePlayerUI(this, applicationMaster);
		// Display the window.
		frame.pack();
	}

	protected void deactivate( ComponentContext componentContext )
	{
		stopFrameGeneration();
	}

	@Override
	public void startFrameGeneration( )
	{
		if (frame != null)
			frame.setVisible(true);

		if (blinkenConfig != null)
		{
			this.blmFilePlayerThread = new BlmFilePlayerThread(blinkenConfig.getRows(), blinkenConfig.getColumns());
			blmFilePlayerThread.start();
		}
		else
			logger.warn("No BlinkenConfiguration set, yet. Will not start FrameGeneration.");
	}

	@Override
	public void stopFrameGeneration( )
	{
		if (frame != null)
			frame.setVisible(false);

		if (blmFilePlayerThread != null)
			blmFilePlayerThread.stopIt();
		blmFilePlayerThread = null;
	}

	@Override
	public BlinkenFrame getCurrentFrame( )
	{
		return currentFrame;
	}

	public void updateBlmFileList( File[] blmFiles )
	{
		this.blmFiles = blmFiles;
		blmFileListValid = false;
	}

	public void updateUpdateMultiplier( int updateMultiplier )
	{
		this.updateMultiplyer = updateMultiplier;
	}

	private class BlmFilePlayerThread extends Thread
	{
		private boolean running = true;

		public void stopIt( )
		{
			this.running = false;
		}

		public BlmFilePlayerThread(int rows, int columns)
		{
			currentFrame = new BlinkenFrameImpl();
		}

		@Override
		public void run( )
		{
			while (running)
			{
				try
				{
					if (blmFiles != null)
					{
						blmFileListValid = true;

						for (int i = 0; i < blmFiles.length; i++)
						{
							if (blmFileListValid)
								playFile(blmFiles[i]);
						}
					}
					Thread.sleep(250);
				}
				catch (Exception e)
				{
					logger.error("Error while strobing:", e);
				}
				;
			}
		}

		private void playFile( File blmFile ) throws IOException
		{
			if (!blmFile.exists())
			{
				logger.error("BlmFile '" + blmFile.toString() + "' doesn't exist.");
				return;
			}

			logger.info("Playing file '" + blmFile.getAbsolutePath() + "'");

			BufferedReader blmFileReader = new BufferedReader(new FileReader(blmFile));
			String line = null;
			while ((line = blmFileReader.readLine()) != null && blmFileListValid)
			{
				if (!"".equals(line))
				{
					if (line.charAt(0) == '@')
					{
						// TODO: dynamically resolve size of blinken
						Boolean[][] frameData = new Boolean[8][18];
						currentFrame.setFrameData(frameData);

						long sleep = Integer.valueOf(line.substring(1, line.length() - 1));

						// TODO: dynamically resolve size of blinken
						for (int i = 0; i < 8; i++)
						{
							String frameline = blmFileReader.readLine();
							for (int j = 0; j < 18; j++)
							{
								if ('0' == frameline.charAt(j))
									currentFrame.clearPixel(i, j);
								else if ('1' == frameline.charAt(j))
									currentFrame.setPixel(i, j);
								else
									logger.error("Unknown char in blm-file: " + frameline.charAt(j));
							}
						}
						fireNewFrame();

						try
						{
							Thread.sleep(BlmFileDataSourceImpl.this.updateMultiplyer * sleep);
						}
						catch (InterruptedException e)
						{
							e.printStackTrace();
						}

					}
				}
			}
			blmFileReader.close();

			logger.info("Finished playing file '" + blmFile.getAbsolutePath() + "'");
		}

	};

}
