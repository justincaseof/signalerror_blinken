package vu.de.signalerror.datasource.equalizer.impl;

import java.awt.Frame;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;

import kjdss.KJDigitalSignalSynchronizer;

import org.apache.log4j.Logger;
import org.osgi.service.component.ComponentContext;

import vu.de.signalerror.base.ApplicationMaster;
import vu.de.signalerror.base.thread.ThreadUtils;
import vu.de.signalerror.blinken.BlinkenConfig;
import vu.de.signalerror.blinken.BlinkenFrame;
import vu.de.signalerror.blinken.BlinkenViewerDataSource;
import vu.de.signalerror.blinken.impl.BlinkenFrameImpl;
import vu.de.signalerror.datasource.equalizer.FFT_to_BlinkenFrameConverter;
import vu.de.signalerror.datasource.equalizer.MixerMonitor;
import vu.de.signalerror.datasource.equalizer.MixerMonitorFaultListener;
import vu.de.signalerror.datasource.equalizer.ui.EqualizerUI;

public class EQ1_DataSourceImpl extends BlinkenViewerDataSource implements FFT_to_BlinkenFrameConverter, MixerMonitor
{
	protected static Logger logger = Logger.getLogger(EQ1_DataSourceImpl.class);

	protected BlinkenFrame currentFrame;

	protected static final int READ_BUFFER_SIZE = 256 * 4;

	protected static final int NUM_BANDS = 9;
	
	protected KJDigitalSignalSynchronizer signalSynchronizer;
	
	protected Frame configFrame;
	
	protected ApplicationMaster applicationMaster;

	private boolean isTerminationRequested = false;
	private boolean isTerminationDone = true;

	protected void activate( ComponentContext componentContext )
	{
		try
		{
			applicationMaster = (ApplicationMaster) componentContext.locateService("ApplicationMaster");
    		initGUI();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	protected void deactivate( ComponentContext componentContext )
	{
		stopFrameGeneration();
	}

	@Override
	public void startFrameGeneration( )
	{
		initDSS();
		if (configFrame != null)
			configFrame.setVisible(true);
	}

	@Override
	public void stopFrameGeneration( )
	{
		if (configFrame != null)
			configFrame.setVisible(false);
		
		// shutdown audio
		EQ1_DataSourceImpl.this.isTerminationRequested = true;
		if( signalSynchronizer!=null )
			signalSynchronizer.stop();
	}

	@Override
	public void setBlinkenConfig( BlinkenConfig config )
	{
		super.setBlinkenConfig( config );
		
		this.currentFrame = new BlinkenFrameImpl();
		clearBlinken();
	}
	
	@Override
	public BlinkenFrame getCurrentFrame( )
	{
		return currentFrame;
	}

	private void initDSS( )
	{
		// -- Create a DSS.
		signalSynchronizer = new KJDigitalSignalSynchronizer( );

		BlinkenSignalProcessor blinkenSignalProcessor = new BlinkenSignalProcessor( NUM_BANDS );
		
		// add ourselves as FFT_to_BlinkenFrameConverter for getting callbacks to 'updateBlinkenFrame()'
		blinkenSignalProcessor.addFFT_to_BlinkenFrameConverter( this );

		// -- Add DSP to DSS.
		signalSynchronizer.add( blinkenSignalProcessor );
	}

	private void initGUI()
	{
		configFrame = new EqualizerUI( this, applicationMaster, getClass().getName() );
		// configFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		// Display the window.
		configFrame.pack();
	}
	
	private static int bufferSize = 40960; // audio buffer size
	private static float fFrameRate = 44100.0F;
	@Override
	public void startMixerMonitoring( final Mixer.Info mixerInfo, final MixerMonitorFaultListener faultListener )
	{
		Runnable monitoring = new Runnable()
		{
			@Override
			public void run( )
			{
				// 1) wait for other lala to end
				EQ1_DataSourceImpl.this.isTerminationRequested = true;
				while( !EQ1_DataSourceImpl.this.isTerminationDone )
				{
					logger.warn( "LAAAAANGWEILIG! ICH WARTE NOCH AUF DEN ANDEREN HORST!" );
					ThreadUtils.sleep( 100 );
				}
				
				// 2) start new lala
				try
				{
					AudioFormat format = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, fFrameRate, 16, 2, 4, fFrameRate, false);
					DataLine.Info targetInfo = new DataLine.Info(TargetDataLine.class, format, bufferSize);
					DataLine.Info sourceInfo = new DataLine.Info(SourceDataLine.class, format, bufferSize);

					Mixer mixer = null;
					TargetDataLine targetDataLine = null; 	// Output data Line
					SourceDataLine sourceDataLine = null; 	// Input data Line
					
					mixer = AudioSystem.getMixer( mixerInfo );
					if( mixer!=null )
					{
						targetDataLine = (TargetDataLine) mixer.getLine( targetInfo );	// from mixer
						sourceDataLine = new DummySourceDataLine( sourceInfo );
					}
					else
					{
						throw new IllegalStateException( "WINDOWS. Y U NO HAVE MIXER" );
					}
					
					targetDataLine.open( format, bufferSize );
					sourceDataLine.open( format, bufferSize );
					targetDataLine.start();
					sourceDataLine.start();

					AudioInputStream audioInputStream = new AudioInputStream( targetDataLine );

					// -- Have the DSS monitor the source data line.
					signalSynchronizer.start( sourceDataLine );
					signalSynchronizer.setSourceDataLineWriteEnabled( false );

					byte[] readBuffer = new byte[READ_BUFFER_SIZE];
					int bytesRead = 0;

					// -- Read line and write to DSS
					EQ1_DataSourceImpl.this.isTerminationRequested = false;
					EQ1_DataSourceImpl.this.isTerminationDone = false;
					while (  ( (bytesRead = audioInputStream.read(readBuffer)) != -1 )  &&  !EQ1_DataSourceImpl.this.isTerminationRequested  )
					{
						signalSynchronizer.writeAudioData( readBuffer, 0, bytesRead );
					}

					// -- EOF, stop monitoring source data line.
					signalSynchronizer.stop();

					// -- Stop and close the target and source data line.
					sourceDataLine.stop();
					targetDataLine.stop();
					sourceDataLine.close();
					targetDataLine.close();
					mixer.close();
				}
				catch (Exception e)
				{
					logger.error( "Error:\n", e );
					faultListener.onFault( "Cannot use this Mixer!" );
				}
				finally
				{
					EQ1_DataSourceImpl.this.isTerminationDone = true;
				}
			}
		};
		new Thread(monitoring).start();
	}

//	private long fps_calc_start = 0;
//	private int fps = 0;
	private float[] maxVals = new float[9];
	@Override
	public void updateBlinkenFrame( float[] bands )
	{
//		fps++;
//		long now = System.currentTimeMillis();
//		if( now-fps_calc_start > 1000 )
//		{
//			System.err.println("\n###\nFPS: " + fps + " fps\n###");
//			// start over
//			fps=0;
//			fps_calc_start=now;
//		}
		
		if( bands.length!=NUM_BANDS )
		{
			throw new IllegalStateException( "This should not happen!" );
		}
		else
		{
			for( int i=0; i<NUM_BANDS; i++ )
			{
				if( bands[i] > (maxVals[i]*1.25) )	// only if it is a really larger
				{
					maxVals[i] = bands[i];
				}
				else
				{
					maxVals[i]-=0.00001;	// slowly resetting maxval to adjust to lowering volumes
				}
				
				int normalizedValue = Math.round( ((float)this.blinkenConfig.getRows()) * bands[i] / maxVals[i] );
//				System.out.println("normalizedValue["+i+"] = " + normalizedValue);
				for( int j=0; j<this.blinkenConfig.getRows(); j++ )
				{
					if( normalizedValue>j )
					{
						this.currentFrame.setPixel( 7-j, (2*i) );
						this.currentFrame.setPixel( 7-j, (2*i)+1 );
					}
					else
					{
						this.currentFrame.clearPixel( 7-j, (2*i) );
						this.currentFrame.clearPixel( 7-j, (2*i)+1 );
					}
				}
			}
		}
		fireNewFrame();
	}
	
	protected void lightBlinken()
	{
		setBlinken( true );
	}
	
	protected void clearBlinken()
	{
		setBlinken( false );
	}
	
	private void setBlinken( boolean onOrOff )
	{
		Boolean[][] data = new Boolean[blinkenConfig.getRows()][blinkenConfig.getColumns()];
		for (int i = 0; i < blinkenConfig.getRows(); i++)
		{
			for (int j = 0; j < blinkenConfig.getColumns(); j++)
			{
				data[i][j] = onOrOff;
			}
		}
		this.currentFrame.setFrameData(data);
	}
	
}
