///*jadclipse*/// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
//// Jad home page: http://www.kpdus.com/jad.html
//// Decompiler options: packimports(3) radix(10) lradix(10) 
//// Source File Name:   AudioTrace.java
//
//import java.awt.BorderLayout;
//import java.awt.Color;
//import java.awt.Container;
//import java.awt.GridLayout;
//import java.awt.Toolkit;
//import java.awt.event.ActionEvent;
//import java.awt.event.ActionListener;
//import java.awt.event.ItemEvent;
//import java.awt.event.ItemListener;
//import java.awt.event.MouseAdapter;
//import java.awt.event.MouseEvent;
//import java.awt.event.MouseListener;
//import java.io.IOException;
//import java.util.ArrayList;
//
//import javax.sound.sampled.AudioSystem;
//import javax.sound.sampled.Line;
//import javax.sound.sampled.LineUnavailableException;
//import javax.sound.sampled.Mixer;
//import javax.sound.sampled.TargetDataLine;
//import javax.swing.JButton;
//import javax.swing.JCheckBox;
//import javax.swing.JColorChooser;
//import javax.swing.JComboBox;
//import javax.swing.JFrame;
//import javax.swing.JLabel;
//import javax.swing.JMenu;
//import javax.swing.JMenuBar;
//import javax.swing.JMenuItem;
//import javax.swing.JOptionPane;
//import javax.swing.JPanel;
//import javax.swing.JScrollPane;
//import javax.swing.JSlider;
//import javax.swing.JTextArea;
//import javax.swing.JWindow;
//import javax.swing.KeyStroke;
//import javax.swing.SwingUtilities;
//import javax.swing.UIManager;
//import javax.swing.border.BevelBorder;
//import javax.swing.border.CompoundBorder;
//import javax.swing.border.EmptyBorder;
//import javax.swing.border.TitledBorder;
//
//import org.pscode.sound.AudioPlotPanel;
//import org.pscode.sound.TraceColorOptions;
//
//// Referenced classes of package org.pscode.sound:
////            TraceColorOptions, AudioPlotPanel
//
//public class AudioTrace extends JFrame implements MouseListener
//{
//	class AudioTraceLevelsPanel extends JPanel
//	{
//
//		final AudioTrace this$0;
//
//		AudioTraceLevelsPanel()
//		{
//			super(new BorderLayout());
//			this$0 = AudioTrace.this;
//			cbShowBar = new JCheckBox("Meter", true);
//			cbShowBar.setToolTipText("Show volume meter in lower right of display");
//			cbShowText = new JCheckBox("Text", false);
//			cbShowText.setToolTipText("Show volume levels in upper right of display");
//			JPanel pSilence = new JPanel(new GridLayout(1, 0));
//			pSilence.add(new JLabel("Show Volume"));
//			pSilence.add(cbShowBar);
//			pSilence.add(cbShowText);
//			pSilence.setToolTipText("Display input signal levels (loudness)");
//			add(pSilence, "Center");
//		}
//	}
//
//	class AudioTraceColorsPanel extends JPanel
//	{
//
//		final AudioTrace this$0;
//
//		AudioTraceColorsPanel()
//		{
//			super();
//			this$0 = AudioTrace.this;
//			setLayout(new BorderLayout());
//			try
//			{
//				TraceColorOptions tco = new TraceColorOptions();
//				TraceColorOptions defaultColorSchemes[] = tco.getDefaultColorOptions();
//				final JComboBox defaultSchemesSelector = new JComboBox(defaultColorSchemes);
//				JPanel schemesContainer = new JPanel(new BorderLayout());
//				schemesContainer.setBorder(new TitledBorder("Default Color Palettes"));
//				schemesContainer.add(defaultSchemesSelector);
//				defaultSchemesSelector.addItemListener(new ItemListener()
//				{
//
//					public void itemStateChanged( ItemEvent itemEvent )
//					{
//						TraceColorOptions temp = (TraceColorOptions) defaultSchemesSelector.getSelectedItem();
//						bg.setBackground(temp.getColors()[0]);
//						left.setBackground(temp.getColors()[1]);
//						right.setBackground(temp.getColors()[2]);
//						leftSecondary.setBackground(temp.getColors()[3]);
//						rightSecondary.setBackground(temp.getColors()[4]);
//					}
//
//					final AudioTrace val$this$0;
//					final JComboBox val$defaultSchemesSelector;
//					final AudioTraceColorsPanel this$1;
//
//					// JavaClassFileOutputException: Invalid index accessing method local variables table of <init>
//				});
//				add(schemesContainer, "North");
//			}
//			catch (IOException ex)
//			{
//				ex.printStackTrace();
//			}
//			JPanel pColors = new JPanel(new GridLayout(0, 3, 5, 5));
//			add(pColors, "Center");
//			pColors.setBorder(new TitledBorder("Colors"));
//			bg = new ColorButton("Background");
//			bg.setBackground(Color.black);
//			bg.setToolTipText("Background Color");
//			pColors.add(bg);
//			left = new ColorButton("Left");
//			left.setBackground(new Color(0, 51, 204));
//			left.setToolTipText("Trace Color (main - left)");
//			pColors.add(left);
//			right = new ColorButton("Right");
//			right.setBackground(new Color(51, 255, 51));
//			right.setToolTipText("Trace Color (main - right)");
//			pColors.add(right);
//			gradient = new JCheckBox("Use Gradient", true);
//			gradient.setToolTipText("Use max. color for high signal strength gradient");
//			pColors.add(gradient);
//			leftSecondary = new ColorButton("Maximum Left");
//			leftSecondary.setBackground(new Color(255, 0, 51));
//			leftSecondary.setToolTipText("Trace Color (high values - left)");
//			pColors.add(leftSecondary);
//			rightSecondary = new ColorButton("Maximum Right");
//			rightSecondary.setBackground((new Color(255, 255, 0)).brighter().brighter());
//			rightSecondary.setToolTipText("Trace Color (high values - right)");
//			pColors.add(rightSecondary);
//			String help = "Select a color theme from the 'Color Palettes' drop-down at the top, or click any color to change it.";
//			JTextArea colorHelp = new JTextArea(help, 3, 33);
//			colorHelp.setLineWrap(true);
//			colorHelp.setWrapStyleWord(true);
//			colorHelp.setEditable(false);
//			colorHelp.setEnabled(false);
//			colorHelp.setBorder(new CompoundBorder(new BevelBorder(1), new EmptyBorder(5, 5, 5, 5)));
//			add(colorHelp, "South");
//		}
//	}
//
//	class AudioTraceOptionsPanel extends JPanel
//	{
//
//		final AudioTrace this$0;
//
//		AudioTraceOptionsPanel()
//		{
//			this$0 = AudioTrace.this;
//			super(new BorderLayout());
//			sSampleSize = new JSlider(1, 10, 1);
//			sSampleSize.setPaintLabels(true);
//			sSampleSize.setPaintTicks(true);
//			sSampleSize.setSnapToTicks(true);
//			sSampleSize.setMajorTickSpacing(1);
//			sSampleSize.setToolTipText("Frame sample size (in blocks of 32)");
//			sSampleSize.setBorder(new TitledBorder("Sample Size"));
//			sGain = new JSlider(0, 1, AudioPlotPanel.MAX_GAIN, 1);
//			sGain.setPaintLabels(true);
//			sGain.setPaintTicks(true);
//			sGain.setMajorTickSpacing(1);
//			sGain.setMinorTickSpacing(1);
//			sGain.setToolTipText("Gain factor - loudest signal fills screen at gain 1");
//			cbLoopGain = new JCheckBox("Loop", false);
//			cbLoopGain.setToolTipText("Loop through gain settings (double click trace to toggle)");
//			sFadeRate = new JSlider(1, 0, 100, 15);
//			sFadeRate.setPaintLabels(true);
//			sFadeRate.setPaintTicks(true);
//			sFadeRate.setMajorTickSpacing(10);
//			sFadeRate.setMinorTickSpacing(5);
//			sFadeRate.setToolTipText("Fade rate (100 - clear, 0 - retain)");
//			sScrollStep = new JSlider(1, -15, 15, 3);
//			sScrollStep.setPaintLabels(true);
//			sScrollStep.setPaintTicks(true);
//			sScrollStep.setMajorTickSpacing(5);
//			sScrollStep.setMinorTickSpacing(1);
//			sScrollStep.setToolTipText("Scroll step size (+ve - up, -ve - down)");
//			sZoomStep = new JSlider(1, -30, 30, 20);
//			sZoomStep.setPaintLabels(true);
//			sZoomStep.setPaintTicks(true);
//			sZoomStep.setMajorTickSpacing(10);
//			sZoomStep.setMinorTickSpacing(2);
//			sZoomStep.setToolTipText("Zoom step size (+ve - bigger, -ve - smaller");
//			JPanel pOld = new JPanel(new BorderLayout());
//			pOld.setBorder(new TitledBorder("Old Traces"));
//			pOld.add(sFadeRate, "West");
//			pOld.add(sScrollStep, "Center");
//			pOld.add(sZoomStep, "East");
//			cbAllLines = new JCheckBox("Display all lines", false);
//			cbAllLines.setToolTipText("..else 'Line number'");
//			sLineNumber = new JSlider(1, 5, 1);
//			sLineNumber.setPaintLabels(true);
//			sLineNumber.setPaintTicks(true);
//			sLineNumber.setSnapToTicks(true);
//			sLineNumber.setMajorTickSpacing(1);
//			sLineNumber.setToolTipText("Line number");
//			JPanel pOsc = new JPanel(new BorderLayout());
//			JPanel pGain = new JPanel(new BorderLayout());
//			pGain.setBorder(new TitledBorder("Visual Gain"));
//			pGain.add(sGain, "Center");
//			pGain.add(cbLoopGain, "West");
//			JPanel pTraceType = new JPanel(new BorderLayout());
//			pTraceType.setBorder(new TitledBorder("Trace Style"));
//			cbStandardTrace = new JCheckBox("Standard", false);
//			cbStandardTrace.setToolTipText("..trace (ampl. v. time),else plot amplitude v. gradient");
//			pTraceType.add(cbStandardTrace, "West");
//			sGradientGain = new JSlider(1, 5, 3);
//			sGradientGain.setPaintLabels(true);
//			sGradientGain.setPaintTicks(true);
//			sGradientGain.setMajorTickSpacing(1);
//			sGradientGain.setMinorTickSpacing(1);
//			sGradientGain.setToolTipText("Gradient magnification factor (for plot width in 'amp. v. grad.' mode)");
//			pTraceType.add(sGradientGain, "Center");
//			JPanel pGainTrace = new JPanel(new BorderLayout());
//			pGainTrace.add(pGain, "Center");
//			pGainTrace.add(pTraceType, "North");
//			pGainTrace.add(sSampleSize, "South");
//			JPanel pNorth = new JPanel(new BorderLayout());
//			pNorth.add(pGainTrace, "South");
//			pNorth.add(pOsc, "Center");
//			JPanel pNorthMost = new JPanel(new BorderLayout());
//			pNorthMost.add(pNorth, "South");
//			add(pNorthMost, "North");
//			add(pOld, "South");
//			int maxSleep = 150;
//			Object times[] = new Object[maxSleep];
//			for (int ii = 0; ii < maxSleep; ii++)
//				times[ii] = new Integer(ii + 1);
//
//			threadSleep = new JComboBox(times);
//			threadSleep.setSelectedItem(Integer.valueOf(25));
//			threadSleep.setToolTipText("Milliseconds.  0 gives little clipping, but potential delays");
//			int maxSize = 20;
//			Object sizes[] = new Object[maxSize];
//			for (int ii = 0; ii < maxSize; ii++)
//				sizes[ii] = new Integer(ii + 1);
//
//			strokeSize = new JComboBox(sizes);
//			strokeSize.setSelectedIndex(2);
//			strokeSize.setToolTipText("Trace Thickness (Px)");
//			JPanel pStroke = new JPanel(new BorderLayout());
//			pStroke.setBorder(new TitledBorder("Line Thickness"));
//			pStroke.add(strokeSize, "North");
//			add(pStroke, "Center");
//		}
//	}
//
//	class ColorButton extends JButton implements ActionListener
//	{
//
//		public void actionPerformed( ActionEvent ae )
//		{
//			JColorChooser _tmp = chooseColor;
//			Color c = JColorChooser.showDialog(null, "Choose Color", getBackground());
//			if (c != null)
//				setBackground(c);
//		}
//
//		public void setBackground( Color bg )
//		{
//			super.setBackground(bg);
//			int r = bg.getRed();
//			int g = bg.getGreen();
//			int b = bg.getBlue();
//			if (r + g + b < 384)
//				setForeground(Color.WHITE);
//			else
//				setForeground(Color.BLACK);
//		}
//
//		JColorChooser chooseColor;
//		final AudioTrace this$0;
//
//		ColorButton(String label)
//		{
//			this$0 = AudioTrace.this;
//			super(label);
//			chooseColor = new JColorChooser();
//			setBackground(Color.black);
//			setContentAreaFilled(false);
//			setOpaque(true);
//			addActionListener(this);
//		}
//	}
//
//	class AudioTraceSourcePanel extends JPanel
//	{
//
//		public void update( )
//		{
//			if (sb != null)
//			{
//				lineInfo.setText(sb.toString().substring(1));
//				lineInfo.setCaretPosition(0);
//			}
//		}
//
//		JTextArea lineInfo;
//		final AudioTrace this$0;
//
//		AudioTraceSourcePanel()
//		{
//			this$0 = AudioTrace.this;
//			super(new BorderLayout());
//			JPanel pAudio = new JPanel(new BorderLayout());
//			pAudio.setBorder(new TitledBorder("Audio Sources"));
//			pAudio.add(cbAllLines, "West");
//			pAudio.add(sLineNumber, "Center");
//			JPanel pNorth = new JPanel(new BorderLayout());
//			add(pAudio, "North");
//			String text;
//			if (sb == null)
//				text = "";
//			else
//				text = sb.toString().substring(1);
//			lineInfo = new JTextArea(text, 7, 65);
//			lineInfo.setLineWrap(true);
//			lineInfo.setWrapStyleWord(true);
//			add(new JScrollPane(lineInfo, 20, 31), "South");
//		}
//	}
//
//	public AudioTrace()
//	{
//		super("Audio Trace");
//		fullScreen = false;
//		try
//		{
//			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
//			SwingUtilities.updateComponentTreeUI(this);
//		}
//		catch (Exception e)
//		{
//			System.err.println("Internal Look And Feel Setting Error.");
//			System.err.println(e);
//		}
//		setDefaultCloseOperation(3);
//		Container c = getContentPane();
//		c.setLayout(new BorderLayout());
//		atop = new AudioTraceOptionsPanel();
//		atcp = new AudioTraceColorsPanel();
//		atsp = new AudioTraceSourcePanel();
//		atlp = new AudioTraceLevelsPanel();
//		JMenuBar mb = new JMenuBar();
//		setJMenuBar(mb);
//		JMenu mainMenu = new JMenu("File");
//		mainMenu.setMnemonic('f');
//		mb.add(mainMenu);
//		JMenuItem mExit = new JMenuItem("Exit");
//		mExit.setAccelerator(KeyStroke.getKeyStroke(88, 2));
//		mExit.addActionListener(new ActionListener()
//		{
//
//			public void actionPerformed( ActionEvent ae )
//			{
//				System.exit(0);
//			}
//
//			final AudioTrace this$0;
//
//			{
//				this$0 = AudioTrace.this;
//				super();
//			}
//		});
//		mainMenu.add(mExit);
//		JMenu viewMenu = new JMenu("Settings");
//		viewMenu.setMnemonic('s');
//		mb.add(viewMenu);
//		JMenuItem mColors = new JMenuItem("Colors");
//		mColors.setMnemonic('c');
//		mColors.setAccelerator(KeyStroke.getKeyStroke(67, 2));
//		mColors.addActionListener(new ActionListener()
//		{
//
//			public void actionPerformed( ActionEvent ae )
//			{
//				showColorsDialog();
//			}
//
//			final AudioTrace this$0;
//
//			{
//				this$0 = AudioTrace.this;
//				super();
//			}
//		});
//		viewMenu.add(mColors);
//		JMenuItem mOptions = new JMenuItem("Options");
//		mOptions.setMnemonic('o');
//		mOptions.setAccelerator(KeyStroke.getKeyStroke(79, 2));
//		mOptions.addActionListener(new ActionListener()
//		{
//
//			public void actionPerformed( ActionEvent ae )
//			{
//				showOptionsDialog();
//			}
//
//			final AudioTrace this$0;
//
//			{
//				this$0 = AudioTrace.this;
//				super();
//			}
//		});
//		viewMenu.add(mOptions);
//		JMenuItem mLevels = new JMenuItem("Display Volume");
//		mLevels.setMnemonic('v');
//		mLevels.setAccelerator(KeyStroke.getKeyStroke(86, 2));
//		mLevels.addActionListener(new ActionListener()
//		{
//
//			public void actionPerformed( ActionEvent ae )
//			{
//				showVolumeLevels();
//			}
//
//			final AudioTrace this$0;
//
//			{
//				this$0 = AudioTrace.this;
//				super();
//			}
//		});
//		viewMenu.add(mLevels);
//		JMenuItem mMisc = new JMenuItem("Render Rate");
//		mMisc.setMnemonic('r');
//		mMisc.setAccelerator(KeyStroke.getKeyStroke(82, 2));
//		mMisc.addActionListener(new ActionListener()
//		{
//
//			public void actionPerformed( ActionEvent ae )
//			{
//				showThreadSleep();
//			}
//
//			final AudioTrace this$0;
//
//			{
//				this$0 = AudioTrace.this;
//				super();
//			}
//		});
//		viewMenu.add(mMisc);
//		JMenuItem mLines = new JMenuItem("Line Info.");
//		mLines.setMnemonic('l');
//		mLines.setAccelerator(KeyStroke.getKeyStroke(76, 2));
//		mLines.addActionListener(new ActionListener()
//		{
//
//			public void actionPerformed( ActionEvent ae )
//			{
//				showLineInfo();
//			}
//
//			final AudioTrace this$0;
//
//			{
//				this$0 = AudioTrace.this;
//				super();
//			}
//		});
//		viewMenu.add(mLines);
//		addMouseListener(new MouseAdapter()
//		{
//
//			public void mouseClicked( MouseEvent me )
//			{
//				toggleFullScreen(me);
//			}
//
//			final AudioTrace this$0;
//
//			{
//				this$0 = AudioTrace.this;
//				super();
//			}
//		});
//		String address = "/image/audiotrace-32x32.png";
//		java.net.URL url = getClass().getResource(address);
//		java.awt.Image icon = Toolkit.getDefaultToolkit().getImage(url);
//		setIconImage(icon);
//		constructContentArea();
//		pack();
//		setSize(640, 480);
//		setLocationRelativeTo(null);
//	}
//
//	public void mouseExited( MouseEvent mouseevent )
//	{
//	}
//
//	public void mouseEntered( MouseEvent mouseevent )
//	{
//	}
//
//	public void mouseReleased( MouseEvent mouseevent )
//	{
//	}
//
//	public void mousePressed( MouseEvent mouseevent )
//	{
//	}
//
//	public void mouseClicked( MouseEvent me )
//	{
//		toggleFullScreen(me);
//	}
//
//	public void showOptionsDialog( )
//	{
//		JOptionPane.showMessageDialog(this, atop, "Audio Trace display options", 3);
//		refreshContentArea();
//	}
//
//	public void showColorsDialog( )
//	{
//		JOptionPane.showMessageDialog(this, atcp, "Audio Trace Colors", 3);
//		refreshContentArea();
//	}
//
//	public void showThreadSleep( )
//	{
//		JOptionPane.showMessageDialog(this, threadSleep, "Thread Sleep Time", 1);
//		refreshContentArea();
//	}
//
//	public void showLineInfo( )
//	{
//		atsp.update();
//		JOptionPane.showMessageDialog(this, atsp, "Mixer/Line Information", 1);
//		refreshContentArea();
//	}
//
//	public void showVolumeLevels( )
//	{
//		JOptionPane.showMessageDialog(this, atlp, "Display Volume", 1);
//		refreshContentArea();
//	}
//
//	public void constructContentArea( )
//	{
//		mainPanel = new JPanel(new GridLayout(0, 1));
//		mainPanel.setToolTipText("'Double Click' to go to/return from full-screen mode");
//		getContentPane().add(mainPanel, "Center");
//		mainPanel.addMouseListener(this);
//		javax.sound.sampled.Mixer.Info allMixer[] = AudioSystem.getMixerInfo();
//		int ii = 0;
//		boolean lineFound = false;
//		sb = new StringBuffer();
//		lines = new ArrayList();
//		label0: for (; ii < allMixer.length; ii++)
//		{
//			Mixer mixer = AudioSystem.getMixer(allMixer[ii]);
//			sb.append((new StringBuilder()).append("\nMixer ").append(ii).append(": ").append(mixer.getMixerInfo()).toString());
//			try
//			{
//				Line allTargetLines[] = mixer.getTargetLines();
//				javax.sound.sampled.Line.Info allTLineInfos[] = mixer.getTargetLineInfo();
//				int jj = 0;
//				do
//				{
//					if (jj >= allTLineInfos.length)
//						continue label0;
//					try
//					{
//						TargetDataLine line = (TargetDataLine) mixer.getLine(allTLineInfos[jj]);
//						sb.append((new StringBuilder()).append("\nLine ").append(ii).append("/").append(jj).append(":  ").append(allTLineInfos[jj]).toString());
//						sb.append((new StringBuilder()).append("\n\tfrmt.: ").append(line.getFormat()).toString());
//						lines.add(line);
//						lineFound = true;
//					}
//					catch (ClassCastException cce)
//					{
//						sb.append((new StringBuilder()).append("\nLine ").append(ii).append("/").append(jj).append(": ").append(allTLineInfos[jj]).toString());
//						sb.append("\n\tNote: Is a Data line, but not a TDL");
//					}
//					jj++;
//				}
//				while (true);
//			}
//			catch (LineUnavailableException lue)
//			{
//				lue.printStackTrace();
//			}
//		}
//
//		refreshContentArea();
//	}
//
//	public void refreshContentArea( )
//	{
//		Thread t = new Thread()
//		{
//
//			public void run( )
//			{
//				if (allTracePanels != null)
//				{
//					for (int ii = 0; ii < allTracePanels.size(); ii++)
//						try
//						{
//							AudioPlotPanel temp = (AudioPlotPanel) allTracePanels.get(ii);
//							allTracePanels.remove(temp);
//							mainPanel.remove(temp);
//							temp.stop();
//							temp = null;
//						}
//						catch (Throwable t)
//						{
//							t.printStackTrace();
//						}
//
//				}
//				else
//				{
//					allTracePanels = new ArrayList();
//				}
//				if (cbAllLines.isSelected())
//				{
//					for (int jj = 0; jj < lines.size(); jj++)
//					{
//						AudioPlotPanel app = new AudioPlotPanel((TargetDataLine) lines.get(jj));
//						app.setLineNumber(jj + 1);
//						allTracePanels.add(app);
//					}
//
//				}
//				else if (sLineNumber.getValue() < lines.size())
//				{
//					TargetDataLine line = (TargetDataLine) lines.get(sLineNumber.getValue() - 1);
//					AudioPlotPanel app = new AudioPlotPanel(line);
//					app.setLineNumber(sLineNumber.getValue());
//					allTracePanels.add(app);
//				}
//				else
//				{
//					TargetDataLine line = (TargetDataLine) lines.get(lines.size() - 1);
//					AudioPlotPanel app = new AudioPlotPanel(line);
//					app.setLineNumber(lines.size());
//					allTracePanels.add(app);
//				}
//				for (int jj = 0; jj < allTracePanels.size(); jj++)
//				{
//					AudioPlotPanel app = (AudioPlotPanel) allTracePanels.get(jj);
//					app.setLissajous(!cbStandardTrace.isSelected());
//					app.setBackground(bg.getBackground());
//					if (gradient.isSelected())
//					{
//						app.setPaintGradient(true);
//						app.setColor(left.getBackground(), 0);
//						app.setColor(right.getBackground(), 1);
//						app.setOuterColor(leftSecondary.getBackground(), 0);
//						app.setOuterColor(rightSecondary.getBackground(), 1);
//						app.refreshGradients();
//					}
//					else
//					{
//						app.setPaintGradient(false);
//						app.setColor(left.getBackground(), 0);
//						app.setColor(right.getBackground(), 1);
//					}
//					app.setStrokeSize(((Integer) strokeSize.getSelectedItem()).intValue());
//					app.setSamplingSize(sSampleSize.getValue() * 32);
//					app.setFadeRate((sFadeRate.getValue() * 255) / 100);
//					app.setScrollStep(sScrollStep.getValue());
//					app.setZoomStep(sZoomStep.getValue());
//					app.setGainLoop(cbLoopGain.isSelected());
//					app.setGain(sGain.getValue());
//					app.setGradientGain(sGradientGain.getValue());
//					app.setShowText(cbShowText.isSelected());
//					app.setShowBar(cbShowBar.isSelected());
//					app.setThreadSleep(((Integer) threadSleep.getSelectedItem()).intValue());
//					app.start();
//					mainPanel.add(app);
//				}
//
//				invalidate();
//				validate();
//				if (wFullScreen != null)
//					wFullScreen.validate();
//			}
//
//			final AudioTrace this$0;
//
//			{
//				this$0 = AudioTrace.this;
//				super();
//			}
//		};
//		SwingUtilities.invokeLater(t);
//	}
//
//	public void toggleFullScreen( MouseEvent me )
//	{
//		if (me.getClickCount() == 2)
//		{
//			if (fullScreen)
//			{
//				wFullScreen.setVisible(false);
//				wFullScreen.getContentPane().remove(mainPanel);
//				setContentPane(mainPanel);
//				validate();
//			}
//			else
//			{
//				if (wFullScreen == null)
//				{
//					wFullScreen = new JWindow(this);
//					wFullScreen.setSize(Toolkit.getDefaultToolkit().getScreenSize());
//				}
//				getContentPane().remove(mainPanel);
//				wFullScreen.setContentPane(mainPanel);
//				wFullScreen.validate();
//				wFullScreen.setVisible(true);
//			}
//			fullScreen = !fullScreen;
//		}
//		else if (me.getClickCount() == 1)
//			toFront();
//	}
//
//	public static void main( String args[] )
//	{
//		Thread t = new Thread()
//		{
//
//			public void run( )
//			{
//				AudioTrace at = new AudioTrace();
//				at.setVisible(true);
//			}
//
//		};
//		SwingUtilities.invokeLater(t);
//	}
//
//	AudioTraceOptionsPanel atop;
//	StringBuffer sb;
//	JSlider sSampleSize;
//	JSlider sGain;
//	JCheckBox cbLoopGain;
//	ColorButton bg;
//	ColorButton right;
//	ColorButton left;
//	ColorButton rightSecondary;
//	ColorButton leftSecondary;
//	JComboBox strokeSize;
//	JComboBox threadSleep;
//	JCheckBox gradient;
//	JSlider sFadeRate;
//	JSlider sScrollStep;
//	JSlider sZoomStep;
//	JCheckBox cbAllLines;
//	JSlider sLineNumber;
//	JSlider sGradientGain;
//	JCheckBox cbStandardTrace;
//	JCheckBox cbShowBar;
//	JCheckBox cbShowText;
//	AudioTraceColorsPanel atcp;
//	AudioTraceSourcePanel atsp;
//	AudioTraceLevelsPanel atlp;
//	JPanel mainPanel;
//	boolean fullScreen;
//	JWindow wFullScreen;
//	ArrayList lines;
//	ArrayList allTracePanels;
//}
//
//
///*
//	DECOMPILATION REPORT
//
//	Decompiled from: E:\ECLIPSE_WORK_PRIVATE\vu.de.signalerror.datasource.equalizer\lib\audiotrace.jar
//	Total time: 117 ms
//	Jad reported messages/errors:
//	Exit status: 0
//	Caught exceptions:
//*/