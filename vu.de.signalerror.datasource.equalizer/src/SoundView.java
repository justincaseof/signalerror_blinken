import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.Vector;
import javax.sound.sampled.*;
import java.lang.Math;
import java.awt.geom.*;

public class SoundView implements MouseMotionListener, ActionListener, MouseListener, LineListener
{
	private boolean DEBUG = false;

	// main parts of the gui
	private JFrame soundFrame;
	private JPanel playPanel;
	private JScrollPane scrollSound;
	private JPanel soundPanel;

	// general information
	/**
	 * The JavaSound this displays.
	 */
	private JavaSound sound;

	/**
	 * Whether to display the sound in stereo - NOT neccessarily whether or not
	 * the sound is in stereo
	 */
	private boolean inStereo;

	// parts of the play panel
	private JLabel startIndex;
	private JLabel stopIndex;
	private JPanel buttonPanel;
	private JButton playEntire;
	private JButton playSelection;
	private JButton playBefore;
	private JButton playAfter;
	private JButton stop;

	// info related to the play panel
	private boolean selectionPrevState;

	// parts of the sound panel
	private JPanel leftSoundPanel;
	private JPanel rightSoundPanel;
	private JPanel leftSampleWrapper;
	private JPanel rightSampleWrapper;
	private SamplingPanel leftSamplePanel;
	private SamplingPanel rightSamplePanel;

	// parts of the information panel
	private JPanel infoPanel;
	private JLabel indexLabel;
	private JPanel sampleLabelPanel;
	private JLabel leftSampleLabel;
	private JLabel rightSampleLabel;
	private JPanel zoomButtonPanel;
	private JButton zoomButton;

	// info related to the sound panel
	private int zoomOutWidth;
	private int zoomInWidth;
	private int sampleWidth;
	private int sampleHeight;
	private int labelHeight;
	private int soundPanelHeight;
	private float framesPerPixel;
	private int cushion;
	private int currentPosition;

	// info related to event handling
	private int mousePressed;
	private int mouseReleased;
	private int mousePressedX;
	private int mouseReleasedX;
	private boolean mouseDragged;
	private int startFrame;
	private int stopFrame;
	private int selectionStart;
	private int selectionStop;

	// /CONSTANTS///
	private static final String currentIndexText = "Current Index: ";
	private static final String startIndexText = "Start At Index: ";
	private static final String stopIndexText = "Stop At Index: ";
	private static final Color selectionColor = Color.gray;
	private static final Color backgroundColor = Color.black;
	private static final Color waveColor = Color.white;
	private static final Color barColor = Color.cyan;

	// SEMI-CONSTANTS
	private String leftSampleText = "Sample Value: ";
	private String rightSampleText = "Right (Bottom) Sample Value: ";

	// set up variables
	public SoundView(JavaSound sound, boolean inStereo)
	{
		this.sound = sound;
		this.inStereo = inStereo;

		if (inStereo)
			leftSampleText = "Left (Top) Sample Value: ";

		// this causes the JavaSound class to add this SoundView
		// as the line listener for any SourceDataLines created so
		// we can monitor starting and stopping to enable/disable
		// play and stop buttons
		sound.setSoundView(this);

		// used for determining difference between a mouse release
		// after a click and a mouse release after a drag. upon dragging,
		// we want to select a region, upon clicking, only move the
		// vertical bar
		mouseDragged = false;
		selectionStart = -1;
		selectionStop = -1;

		// size of the sampling panel
		zoomOutWidth = 640;
		zoomInWidth = sound.getLengthInFrames();
		sampleWidth = zoomOutWidth;
		sampleHeight = 201;
		labelHeight = 50;

		// cushion so that the sampling panel isn't flush against the
		// left side - we want a small border so it looks neater
		cushion = 10;

		// the current x-coordinate of the vertical bar
		currentPosition = 0;

		// display everything
		createWindow();
	}

	private void catchException( Exception ex )
	{
		System.err.println(ex.getMessage());
	}

	public void setTitle( String s )
	{
		soundFrame.setTitle(s);
	}

	private void createWindow( )
	{
		String fileName = sound.getFileName();
		if (fileName == null)
			fileName = "no file name";

		soundFrame = new JFrame(fileName);

		if (inStereo)
		{
			soundFrame.setSize(new Dimension(zoomOutWidth + cushion, 2 * (sampleHeight + cushion) + labelHeight + 100));
		}
		else
		{
			soundFrame.setSize(new Dimension(zoomOutWidth + cushion, sampleHeight + cushion + labelHeight + 100));
		}

		soundFrame.getContentPane().setLayout(new BorderLayout());
		soundFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		// also on close we need to remove the soundView listener?

		// creates the play panel
		createPlayPanel();
		soundFrame.getContentPane().add(playPanel, BorderLayout.NORTH);

		// creates the sound panel
		createSoundPanel();

		// creates the scrollpane for the sound
		scrollSound = new JScrollPane();
		scrollSound.setViewportView(soundPanel);
		soundFrame.getContentPane().add(scrollSound, BorderLayout.CENTER);
		scrollSound.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);

		// creates the info panel - this displays the current index
		// and sample values
		createInfoPanel();

		soundFrame.getContentPane().add(infoPanel, BorderLayout.SOUTH);

		soundFrame.setResizable(false);

		soundFrame.setVisible(true);

	}// createWindow()

	private JButton makeButton( String name, boolean enabled, JPanel panel )
	{
		JButton j = new JButton(name);
		j.addActionListener(this);
		j.setEnabled(enabled);
		panel.add(j);
		return j;
	}

	private void createPlayPanel( )
	{
		playPanel = new JPanel();

		playPanel.setPreferredSize(new Dimension(zoomOutWidth, 60));

		playPanel.setLayout(new BorderLayout());

		startIndex = new JLabel(startIndexText + "N/A");
		stopIndex = new JLabel(stopIndexText + "N/A");

		buttonPanel = new JPanel();

		playEntire = makeButton("Play Entire Sound", true, buttonPanel);
		playSelection = makeButton("Play Selection", false, buttonPanel);
		selectionPrevState = false;
		playBefore = makeButton("Play Before", false, buttonPanel);
		playAfter = makeButton("Play After", true, buttonPanel);
		stop = makeButton("Stop", false, buttonPanel);

		playPanel.add(buttonPanel, BorderLayout.NORTH);
		playPanel.add(startIndex, BorderLayout.WEST);
		playPanel.add(stopIndex, BorderLayout.EAST);

	}// createPlayPanel()

	private void createSoundPanel( )
	{
		// the main panel, we'll add everything to this at the end
		soundPanel = new JPanel();
		if (inStereo)
			soundPanel.setLayout(new GridLayout(2, 1));
		else
			soundPanel.setLayout(new GridLayout(1, 1));

		/*
		 * do all the stuff to display the left channel. we'll only make the
		 * stuff to display the right channel if neccessary. everything will go
		 * into the leftSoundPanel, which is then added to the main soundPanel
		 */
		leftSoundPanel = new JPanel();
		leftSoundPanel.setLayout(new BorderLayout());
		leftSoundPanel.setPreferredSize(new Dimension(sampleWidth, sampleHeight + cushion));

		/*
		 * the sampling panel - this is where the wave form displays. we put it
		 * in a wrapper so that it looks centered within the main soundPanel
		 */
		leftSampleWrapper = new JPanel();// so its centered
		leftSamplePanel = new SamplingPanel(true);
		leftSamplePanel.addMouseMotionListener(this);
		leftSamplePanel.addMouseListener(this);
		leftSampleWrapper.add(leftSamplePanel);
		leftSampleWrapper.setPreferredSize(new Dimension(sampleWidth, sampleHeight + cushion));

		/*
		 * put all the pieces into the left sound panel: the sample at the top,
		 * below it we want the current index on the left zoom button in the
		 * middle (unless its stereo - then the zoom button goes in the right
		 * sound panel) and current sample value on the right
		 */
		leftSoundPanel.add(leftSampleWrapper, BorderLayout.NORTH);

		soundPanel.add(leftSoundPanel);

		soundPanelHeight = sampleHeight + cushion;

		if (inStereo)
		{
			rightSoundPanel = new JPanel();
			rightSoundPanel.setLayout(new BorderLayout());
			rightSoundPanel.setPreferredSize(new Dimension(sampleWidth, sampleHeight + cushion));

			rightSampleWrapper = new JPanel();
			rightSamplePanel = new SamplingPanel(false);
			rightSamplePanel.addMouseMotionListener(this);
			rightSamplePanel.addMouseListener(this);
			rightSampleWrapper.add(rightSamplePanel);
			rightSampleWrapper.setPreferredSize(new Dimension(sampleWidth, sampleHeight + cushion));

			rightSoundPanel.add(rightSampleWrapper, BorderLayout.NORTH);

			soundPanel.add(rightSoundPanel);

			soundPanelHeight = 2 * (sampleHeight + cushion);
		}

		soundPanel.setPreferredSize(new Dimension(zoomOutWidth, soundPanelHeight));
		soundPanel.setSize(soundPanel.getPreferredSize());
	}

	public void createInfoPanel( )
	{
		infoPanel = new JPanel();
		sampleLabelPanel = new JPanel();

		indexLabel = new JLabel(currentIndexText + "1");

		try
		{
			leftSampleLabel = new JLabel(leftSampleText + sound.getLeftSample(0));
			if (inStereo)
				rightSampleLabel = new JLabel(rightSampleText + sound.getRightSample(0));
		}
		catch (Exception ex)
		{
			catchException(ex);
		}

		zoomButtonPanel = new JPanel();
		zoomButton = makeButton("Zoom In", true, zoomButtonPanel);

		if (inStereo)
		{
			sampleLabelPanel = new JPanel();
			sampleLabelPanel.setLayout(new GridLayout(2, 1));
			sampleLabelPanel.add(leftSampleLabel);
			sampleLabelPanel.add(rightSampleLabel);

			infoPanel.setLayout(new GridLayout(1, 4));
			infoPanel.add(indexLabel);
			infoPanel.add(zoomButtonPanel);
			infoPanel.add(sampleLabelPanel);
		}
		else
		{
			infoPanel.setLayout(new GridLayout(1, 3));
			infoPanel.add(indexLabel);
			infoPanel.add(zoomButtonPanel);
			infoPanel.add(leftSampleLabel);
		}
	}

	public void mouseClicked( MouseEvent e )
	{
		currentPosition = e.getX();

		if (currentPosition == 0)
		{
			playBefore.setEnabled(false);
			playAfter.setEnabled(true);
		}
		else if (currentPosition < sampleWidth)
		{
			playBefore.setEnabled(true);
			playAfter.setEnabled(true);
		}
		else if (currentPosition == sampleWidth)
		{
			playBefore.setEnabled(true);
			playAfter.setEnabled(false);
		}

		int curFrame = (int) (currentPosition * framesPerPixel);

		if (DEBUG)
			System.out.println("mouse click:  " + currentPosition);

		indexLabel.setText(currentIndexText + curFrame);
		try
		{
			leftSampleLabel.setText(leftSampleText + sound.getLeftSample(curFrame));
		}
		catch (Exception ex)
		{
			catchException(ex);
		}

		if (inStereo)
		{
			indexLabel.setText(currentIndexText + curFrame);
			try
			{
				rightSampleLabel.setText(rightSampleText + sound.getRightSample(curFrame));
			}
			catch (Exception ex)
			{
				catchException(ex);
			}
		}
		soundPanel.repaint();
	}

	public void mousePressed( MouseEvent e )
	{
		mousePressedX = e.getX();
	}

	public void mouseReleased( MouseEvent e )
	{
		mouseReleasedX = e.getX();

		if (mouseDragged)
		{

			mousePressed = mousePressedX;
			mouseReleased = mouseReleasedX;

			if (mousePressed > mouseReleased)// selected right to left
			{
				int temp = mousePressed;
				mousePressed = mouseReleased;
				mouseReleased = temp;
			}

			startFrame = (int) (mousePressed * framesPerPixel);
			stopFrame = (int) (mouseReleased * framesPerPixel);

			// stopped dragging outside the window.
			if (stopFrame >= sound.getLengthInFrames())
				stopFrame = sound.getLengthInFrames() - 1;

			// stopped dragging outside the window
			if (startFrame < 0)
				startFrame = 0;

			// new values for the labels
			startIndex.setText(startIndexText + startFrame);
			stopIndex.setText(stopIndexText + stopFrame);

			// for highlighting the selection
			selectionStart = mousePressed;
			selectionStop = mouseReleased;

			soundPanel.repaint();
			playSelection.setEnabled(true);
			mouseDragged = false;
		}

	}

	public void mouseEntered( MouseEvent e )
	{
	}

	public void mouseExited( MouseEvent e )
	{
	}

	public void mouseDragged( MouseEvent e )
	{
		mouseDragged = true;

		// highlight the selection as we drag by pretending
		// that we're releasing the mouse at each point
		mouseReleased(e);
	}

	public void mouseMoved( MouseEvent e )
	{
	}

	public void update( LineEvent e )
	{
		if (e.getType().equals(LineEvent.Type.OPEN))
		{
			playEntire.setEnabled(false);
			playBefore.setEnabled(false);
			playAfter.setEnabled(false);
			selectionPrevState = playSelection.isEnabled();
			playSelection.setEnabled(false);
			stop.setEnabled(true);
		}
		if (e.getType().equals(LineEvent.Type.CLOSE))
		{
			playEntire.setEnabled(true);
			playSelection.setEnabled(selectionPrevState);
			stop.setEnabled(false);
			if (currentPosition == 0)
			{
				playBefore.setEnabled(false);
				playAfter.setEnabled(true);
			}
			else if (currentPosition < sampleWidth)
			{
				playBefore.setEnabled(true);
				playAfter.setEnabled(true);
			}
			else if (currentPosition == sampleWidth)
			{
				playBefore.setEnabled(true);
				playAfter.setEnabled(false);
			}
		}

	}

	public void actionPerformed( ActionEvent e )
	{
		if (e.getActionCommand() == "Play Entire Sound")
		{
			try
			{
				sound.play();
			}
			catch (Exception ex)
			{
				catchException(ex);
			}
		}
		else if (e.getActionCommand() == "Play Selection")
		{
			try
			{
				sound.playAtRateInRange(1, startFrame, stopFrame);
			}
			catch (Exception ex)
			{
				catchException(ex);
			}
		}
		else if (e.getActionCommand().equals("Stop"))
		{
			// stop all playback threads related to this sound
			for (int i = 0; i < sound.getPlaybacks().size(); i++)
			{
				((JavaSound.Playback) sound.getPlaybacks().elementAt(i)).stopPlaying();
			}
		}
		else if (e.getActionCommand().equals("Zoom In"))
		{
			zoomButton.setText("Zoom Out");

			currentPosition = (int) (currentPosition * framesPerPixel);
			selectionStart = (int) (selectionStart * framesPerPixel);
			selectionStop = (int) (selectionStop * framesPerPixel);

			if (DEBUG)
				System.out.println("Zoom In:  currentPosition = " + currentPosition);

			sampleWidth = zoomInWidth;

			soundPanel.setPreferredSize(new Dimension(zoomInWidth, soundPanel.getHeight()));
			soundPanel.setSize(soundPanel.getPreferredSize());

			leftSoundPanel.setPreferredSize(new Dimension(zoomInWidth, leftSoundPanel.getHeight()));
			leftSoundPanel.setSize(leftSoundPanel.getPreferredSize());

			leftSampleWrapper.setPreferredSize(new Dimension(zoomInWidth, leftSampleWrapper.getHeight()));
			leftSampleWrapper.setSize(leftSampleWrapper.getPreferredSize());
			leftSamplePanel.setPreferredSize(new Dimension(sampleWidth, sampleHeight));
			leftSamplePanel.setSize(leftSamplePanel.getPreferredSize());

			leftSamplePanel.createWaveForm(true);

			if (inStereo)
			{
				rightSoundPanel.setPreferredSize(new Dimension(zoomInWidth, rightSoundPanel.getHeight()));
				rightSoundPanel.setSize(rightSoundPanel.getPreferredSize());

				rightSampleWrapper.setPreferredSize(new Dimension(zoomInWidth, rightSampleWrapper.getHeight()));
				rightSampleWrapper.setSize(rightSampleWrapper.getPreferredSize());

				rightSamplePanel.setPreferredSize(new Dimension(zoomInWidth, rightSamplePanel.getHeight()));
				rightSamplePanel.setSize(rightSamplePanel.getPreferredSize());

				rightSamplePanel.createWaveForm(false);
			}
			if (DEBUG)
			{
				System.out.println("ZOOM IN SIZES:");
				System.out.println("\tleftSamplePanel: " + leftSamplePanel.getSize());
				System.out.println("\t\tpreferred: " + leftSamplePanel.getPreferredSize());

				System.out.println("\tleftSampleWrapper: " + leftSampleWrapper.getSize());
				System.out.println("\t\tpreferred: " + leftSampleWrapper.getPreferredSize());

				System.out.println("\tleftSoundPanel: " + leftSoundPanel.getSize());
				System.out.println("\t\tpreferred: " + leftSoundPanel.getPreferredSize());

				System.out.println("\tsoundPanel: " + soundPanel.getSize());
				System.out.println("\t\tpreferred: " + soundPanel.getPreferredSize());
			}
		}
		else if (e.getActionCommand().equals("Zoom Out"))
		{
			zoomButton.setText("Zoom In");

			sampleWidth = zoomOutWidth;

			int divisor = (sound.getLengthInFrames() / sampleWidth);
			currentPosition = (int) (currentPosition / divisor);
			selectionStart = (int) (selectionStart / divisor);
			selectionStop = (int) (selectionStop / divisor);

			if (DEBUG)
				System.out.println("Zoom Out:  currentPosition = " + currentPosition);

			soundPanel.setPreferredSize(new Dimension(zoomOutWidth, soundPanel.getHeight()));
			soundPanel.setSize(soundPanel.getPreferredSize());

			leftSoundPanel.setPreferredSize(new Dimension(zoomOutWidth, leftSoundPanel.getHeight()));
			leftSoundPanel.setSize(leftSoundPanel.getPreferredSize());

			leftSampleWrapper.setPreferredSize(new Dimension(zoomOutWidth, leftSampleWrapper.getHeight()));
			leftSampleWrapper.setSize(leftSampleWrapper.getPreferredSize());

			leftSamplePanel.setPreferredSize(new Dimension(sampleWidth, sampleHeight));
			leftSamplePanel.setSize(leftSamplePanel.getPreferredSize());

			leftSamplePanel.createWaveForm(true);

			if (inStereo)
			{
				rightSoundPanel.setPreferredSize(new Dimension(zoomOutWidth, rightSoundPanel.getHeight()));
				rightSoundPanel.setSize(rightSoundPanel.getPreferredSize());

				rightSampleWrapper.setPreferredSize(new Dimension(zoomOutWidth, rightSampleWrapper.getHeight()));
				rightSampleWrapper.setSize(rightSampleWrapper.getPreferredSize());

				rightSamplePanel.setPreferredSize(new Dimension(sampleWidth, sampleHeight));
				rightSamplePanel.setSize(rightSamplePanel.getPreferredSize());

				rightSamplePanel.createWaveForm(false);
			}

			soundPanel.repaint();
		}
		else if (e.getActionCommand().equals("Play Before"))
		{
			try
			{
				sound.playAtRateInRange(1, 0, (int) (currentPosition * framesPerPixel));
			}
			catch (Exception ex)
			{
				catchException(ex);
			}
		}
		else if (e.getActionCommand().equals("Play After"))
		{
			try
			{
				sound.playAtRateInRange(1, (int) (currentPosition * framesPerPixel), sound.getLengthInFrames() - 1);
			}
			catch (Exception ex)
			{
				catchException(ex);
			}
		}
		else
		{
			System.err.println("command not defined: " + e.getActionCommand());
		}

	}

	public class SamplingPanel extends JPanel
	{

		private boolean forLeftSample;
		private Vector points;

		public SamplingPanel(boolean inputForLeftSample)
		{
			forLeftSample = inputForLeftSample;

			if (DEBUG)
				System.out.println("creating new sampling panel: " + "\n\tfor left sample: " + forLeftSample + "\n\tsampleWidth: " + sampleWidth + "\n\tsampleHeight: " + sampleHeight);

			setBackground(backgroundColor);
			setPreferredSize(new Dimension(sampleWidth, sampleHeight));
			setSize(getPreferredSize());
			if (DEBUG)
				System.out.println("\tSample panel preferred size: " + getPreferredSize() + "\n\tSample panel size: " + getSize());

			points = new Vector();
			createWaveForm(forLeftSample);
		}// constructor(forLeftSample)

		public void createWaveForm( boolean forLeftSample )
		{

			// get the max y value for a sound of this sample size
			AudioFormat format = sound.getAudioFileFormat().getFormat();
			float maxValue;

			if (format.getSampleSizeInBits() == 8)
			{
				maxValue = (float) Math.pow(2, 7);
			}
			else if (format.getSampleSizeInBits() == 16)
			{
				maxValue = (float) Math.pow(2, 15);
			}
			else if (format.getSampleSizeInBits() == 24)
			{
				maxValue = (float) Math.pow(2, 23);
			}
			else if (format.getSampleSizeInBits() == 32)
			{
				maxValue = (float) Math.pow(2, 31);
			}
			else
			{
				try
				{
					sound.printError("InvalidSampleSize");
				}
				catch (Exception ex)
				{
					catchException(ex);
				}
				return;
			}

			points.clear();
			framesPerPixel = sound.getLengthInFrames() / sampleWidth;
			for (int pixel = 0; pixel < sampleWidth; pixel++)
			{
				float y;
				float sampleValue;

				if (forLeftSample)
				{
					try
					{
						sampleValue = sound.getLeftSample((int) (pixel * framesPerPixel));
					}
					catch (Exception ex)
					{
						catchException(ex);
						return;
					}
				}
				else
				{
					try
					{
						sampleValue = sound.getRightSample((int) (pixel * framesPerPixel));
					}
					catch (Exception ex)
					{
						catchException(ex);
						return;
					}
				}

				y = ((float) Math.floor(sampleHeight / 2) - (sampleValue * ((float) Math.floor(sampleHeight / 2) / maxValue)));

				points.add(new Point2D.Float(pixel, y));
			}// for - collecting points

			if (DEBUG)
				System.out.println("number of points: " + points.size());
			repaint();

		}// createWaveForm()

		public void paint( Graphics g )
		{
			Rectangle rectToPaint = g.getClipBounds();

			if (DEBUG)
			{
				System.out.println("Repainting: " + rectToPaint);
				System.out.println("\tSampleWidth: " + sampleWidth);
				System.out.println("\tframesPerPixel: " + framesPerPixel);
				System.out.println("\tSample panel size: " + getSize());
				System.out.println("\tSamplePanel Width: " + getWidth());
				System.out.println("\tSamplePanel Height: " + getHeight());
			}

			// clear out the image
			Graphics2D g2 = (Graphics2D) g;
			g2.setBackground(backgroundColor);
			g2.clearRect((int) rectToPaint.getX(), (int) rectToPaint.getY(), (int) rectToPaint.getWidth(), (int) rectToPaint.getHeight());

			// draw the selection if it exists
			if (selectionStart != -1 && selectionStop != -1)
			{
				g2.setBackground(selectionColor);
				g2.clearRect(selectionStart, 0, selectionStop - selectionStart + 1, sampleHeight);
			}

			// draw the lines
			g2.setColor(waveColor);
			for (int i = (int) rectToPaint.getX(); i < (rectToPaint.getX() + rectToPaint.getWidth() - 1); i++)
			{
				g2.draw(new Line2D.Float((Point2D.Float) points.elementAt(i), (Point2D.Float) points.elementAt(i + 1)));
			}

			// draw the center line
			g2.setColor(barColor);
			g2.setStroke(new BasicStroke(1));
			g2.draw(new Line2D.Double(rectToPaint.getX(), Math.floor(sampleHeight / 2), rectToPaint.getX() + rectToPaint.getWidth() - 1, Math.floor(sampleHeight / 2)));

			// draw the current position
			if (rectToPaint.getX() < currentPosition && currentPosition < (rectToPaint.getX() + rectToPaint.getWidth() - 1))
			{
				g2.setColor(barColor);
				g2.setStroke(new BasicStroke(1));
				g2.draw(new Line2D.Double(currentPosition, 0, currentPosition, sampleHeight));
			}
		}// paint(g)

	}// public class SamplingPanel

	public static void main( String args[] )
	{
		try
		{
			/*
			 * JavaSound s = new
			 * JavaSound("/Users/ellie/mediacomp/ellie/really_long_sound.wav");
			 * SoundView test = new SoundView(s, s.isStereo());
			 */

			// JavaSound s2 = new
			// JavaSound("/Users/ellie/mediacomp/ellie/JavaSoundDemo/audio/22-new.aif");

			// SoundView teststereo = new SoundView(s2, true);
			// SoundView testmono = new SoundView(s2, false);

			JavaSound windowsSound = new JavaSound("/Users/ellie/Desktop/sound2.wav");
			SoundView testWin = new SoundView(windowsSound, false);

			JavaSound shaggz = new JavaSound("/Users/ellie/Desktop/audio2/SOUND1.WAV");
			System.out.println(shaggz.getAudioFileFormat().getFormat());

			shaggz.blockingPlay();

			SoundView shaggzView = new SoundView(shaggz, false);

			JavaSound shaggz2 = new JavaSound("/Users/ellie/Desktop/audio2/SOUND1.WAV");
			for (int i = 0; i < shaggz2.getLengthInFrames(); i++)
			{
				shaggz2.setSample(i, shaggz2.getSample(i));
			}
			SoundView shaggzView2 = new SoundView(shaggz2, false);

			/*
			 * JavaSound emptySound = new JavaSound(5); SoundView testempty =
			 * new SoundView(emptySound, false);
			 */
			/*
			 * JavaSound sStates = new JavaSound("Z:\\croak.wav"); SoundView
			 * testStates = new SoundView(sStates, sStates.isStereo());
			 */
		}
		catch (Exception ex)
		{
			System.out.println(ex.getMessage());
		}

	}
}// end class SoundView
