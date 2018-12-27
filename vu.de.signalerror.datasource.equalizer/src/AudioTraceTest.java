import javax.swing.SwingUtilities;

import org.pscode.sound.AudioTrace;


public class AudioTraceTest
{

	/**
	 * @param args
	 */
	public static void main( String args[] )
	{
		Thread t = new Thread()
		{

			public void run( )
			{
				AudioTrace at = new AudioTrace();
				at.setVisible(true);
			}

		};
		SwingUtilities.invokeLater(t);
	}

}
