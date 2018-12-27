package vu.de.signalerror.blinken.datasource.udp.impl;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

import org.apache.log4j.Logger;
import org.osgi.service.component.ComponentContext;

import vu.de.signalerror.base.ApplicationMaster;
import vu.de.signalerror.blinken.BlinkenFrame;
import vu.de.signalerror.blinken.BlinkenViewerDataSource;
import vu.de.signalerror.blinken.impl.BlinkenFrameImpl;

public class UDPreceiveDataSourceImpl extends BlinkenViewerDataSource
{
	private static Logger logger = Logger.getLogger(UDPreceiveDataSourceImpl.class);

	private UDPreceiveChangeThread updReceiveChangeThread;

	private BlinkenFrame currentFrame;

	private ApplicationMaster applicationMaster;

	private DatagramSocket clientSocket;
	
	protected void activate( ComponentContext componentContext )
	{
		applicationMaster = (ApplicationMaster) componentContext.locateService("ApplicationMaster");
	}

	protected void deactivate( ComponentContext componentContext )
	{
		stopFrameGeneration();
	}

	@Override
	public void startFrameGeneration( )
	{
		if (blinkenConfig != null)
		{
			this.updReceiveChangeThread = new UDPreceiveChangeThread(blinkenConfig.getRows(), blinkenConfig.getColumns());
			updReceiveChangeThread.start();
		}
		else
			logger.warn("No BlinkenConfiguration set, yet. Will not start FrameGeneration.");
	}

	@Override
	public void stopFrameGeneration( )
	{
		if (updReceiveChangeThread != null)
			updReceiveChangeThread.stopIt();
		updReceiveChangeThread = null;
	}

	@Override
	public BlinkenFrame getCurrentFrame( )
	{
		return currentFrame;
	}

	private class UDPreceiveChangeThread extends Thread
	{
		private boolean running = true;

		public void stopIt( )
		{
			this.running = false;
		}

		public UDPreceiveChangeThread(int rows, int columns)
		{
			// generate a frame that is always off.
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
			try
			{
				int port = UDPreceiveDataSourceImpl.this.applicationMaster.getConfigurtation().getInt( "blinken.udp-receive.port" );
				clientSocket = new DatagramSocket(port);
			}
			catch (Exception e)
			{
				logger.fatal(
						"Unable to connect on local port '" + 
						UDPreceiveDataSourceImpl.this.applicationMaster.getConfigurtation().getInt( "blinken.udp-receive.port" ) +
						"'." );
				return;
			}
			
			while (running)
			{
				try
				{
					//Thread.sleep(changeInterval);
					byte[] receiveData = new byte[2048];
					DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
	    			clientSocket.receive(receivePacket);
					
	    			// don't know what all bytes before byte 12 mean...
	    			for( int i=0; i<8; i++ )
	    			{
	    				for( int j=0; j<18; j++ )
	    				{
	    					if( receiveData[12 + i*18 + j]==1 )
	    						currentFrame.setPixel(i, j);
	    					else
	    						currentFrame.clearPixel(i, j);
	    				}
	    			}
	    			
					fireNewFrame();
				}
				catch (Exception e)
				{
					logger.error("Error while strobing:", e);
				}
			}
			clientSocket.close();
		}
		
	}

}
