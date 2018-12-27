/**
 * Project: KJ DSS
 * File   : KJDSSSample.java
 *
 * Author : Kristofer Fudalewski
 * Email  : sirk_sytes@hotmail.com   
 * Website: http://sirk.sytes.net
 */

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.HeadlessException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Line;
import javax.sound.sampled.Port;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;
import javax.swing.JFrame;

import kjdss.KJDigitalSignalSynchronizer;
import kjdss.ui.KJScopeAndSpectrumAnalyzer;

/**
 * @author Kris Fudalewski
 * 
 *         Sample application demonstrating KJDSS
 */
public class Test extends JFrame
{

	private static final int READ_BUFFER_SIZE = 1024 * 4;

	private KJDigitalSignalSynchronizer dss;

	public Test() throws HeadlessException
	{

		super();

		initGUI();

		initDSS();

		setVisible(true);

		mainLoop();

	}

	private void initDSS( )
	{

		// -- Create a DSS.
		dss = new KJDigitalSignalSynchronizer();

		// -- Create DSP that comes with KJDSS (also used in KJ).
		KJScopeAndSpectrumAnalyzer wDsp = new KJScopeAndSpectrumAnalyzer();

		// -- Add DSP to DSS.
		dss.add(wDsp);

		// -- Add DSP as component to JFrame
		add((Component) wDsp);

	}

	private void initGUI( )
	{

		setTitle("KJDSS - Sample");

		setLayout(new BorderLayout());

		setSize(456, 208);

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

	}

	private void mainLoop( )
	{

		while (true)
		{

			playFile();
		}

		// System.exit(0);

	}

	private void playFile( )
	{

		try
		{

			try
			{
				AudioFormat audioFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 44100.0F, 16, 2, 4, 44100.0F, false);
				DataLine.Info info = new DataLine.Info(TargetDataLine.class, audioFormat);
				TargetDataLine targetDataLine = (TargetDataLine) AudioSystem.getLine(info);
				targetDataLine.open(audioFormat);
				targetDataLine.start();
				
				// -- Load the WAV file.
				AudioInputStream audioInputStream = new AudioInputStream(targetDataLine);

				// -- Create a source data line in the format of the file.
				SourceDataLine wSourceDataLine = AudioSystem.getSourceDataLine(audioInputStream.getFormat());

				// -- Open the source data line and start it.
				wSourceDataLine.open();
				wSourceDataLine.start();

				// -- Have the DSS monitor the source data line.
				dss.start(wSourceDataLine);

				// -- Allocate a read buffer.
				byte[] wRb = new byte[READ_BUFFER_SIZE];
				int wRs = 0;

				// -- Read from WAV file and write to DSS (and the monitored
				// source
				// data line)
				while ((wRs = audioInputStream.read(wRb)) != -1)
				{
					dss.writeAudioData(wRb, 0, wRs);
				}

				// -- EOF, stop monitoring source data line.
				dss.stop();

				// -- Stop and close the source data line.
				wSourceDataLine.stop();
				wSourceDataLine.close();
			}
			catch (Exception e)
			{
				System.out.println("unable to get a recording line");
				e.printStackTrace();
				System.exit(1);
			}
		}
		catch (Exception pEx)
		{
			pEx.printStackTrace();
		}

	}

	public static void main( String[] pArgs )
	{
		new Test();
	}

}
