/*
 * Copyright (c) 1995, 2008, Oracle and/or its affiliates. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Oracle or the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package vu.de.signalerror.blinken.datasrc.blmfile.ui;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.io.File;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import vu.de.signalerror.base.ApplicationMaster;
import vu.de.signalerror.blinken.datasrc.blmfile.impl.BlmFileDataSourceImpl;

@SuppressWarnings("serial")
public class FilePlayerUI extends Frame implements ActionListener, ChangeListener, ComponentListener
{
	private static final String UI_PROPERTIES_POS_X = FilePlayerUI.class.getName() + ".ui.pos_x";
	private static final String UI_PROPERTIES_POS_Y = FilePlayerUI.class.getName() + ".ui.pos_y";
	private static final String UI_PROPERTIES_LASTLOCATION = FilePlayerUI.class.getName() + ".ui.filechooser.lastlocation";
	
	private JButton openButton;
	private JFileChooser fc;
	private JPanel mainPanel;

	private BlmFileDataSourceImpl blmFilePlayer;
	
	private ApplicationMaster applicationMaster;

	public FilePlayerUI(BlmFileDataSourceImpl blmFilePlayer, ApplicationMaster applicationMaster)
	{
		super( "BLM-File Selector" );
		this.applicationMaster = applicationMaster;
		this.blmFilePlayer = blmFilePlayer;
		this.mainPanel = new JPanel(new BorderLayout());
		addComponentListener(this);
		
		// slider
		JSlider slider = new JSlider(0, 100);
		slider.setValue(40);
		slider.addChangeListener(this);

		// Create a file chooser
		String path = applicationMaster.getUserSettings().getString( UI_PROPERTIES_LASTLOCATION );		
		if( path==null )
			fc = new JFileChooser();
		else
			fc = new JFileChooser(path);

		// fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);

		// enable to select more than one file/folder
		fc.setMultiSelectionEnabled(true);

		// Create the open button. We use the image from the JLF
		// Graphics Repository (but we extracted it from the jar).
		openButton = new JButton("Open a File...", createImageIcon("images/Open16.gif"));
		openButton.addActionListener(this);

		// For layout purposes, put the buttons in a separate panel
		JPanel buttonPanel = new JPanel(); // use FlowLayout
		buttonPanel.add(openButton);

		// Add the buttons and the log to this panel.
		mainPanel.add(buttonPanel, BorderLayout.PAGE_START);
		mainPanel.add(slider, BorderLayout.CENTER);
		
		// try to fetch user-stored window-position
		int pos_x = applicationMaster.getUserSettings().getInteger(UI_PROPERTIES_POS_X, 100 );
		int pos_y = applicationMaster.getUserSettings().getInteger(UI_PROPERTIES_POS_Y, 100 );
		
		setLocation(pos_x, pos_y);
		
		add(mainPanel);
	}

	@Override
	public void stateChanged( ChangeEvent e )
	{
		JSlider slider = (JSlider) e.getSource();
		int adjustedMultiplier = slider.getValue() / 3;
		blmFilePlayer.updateUpdateMultiplier(adjustedMultiplier);
	}

	@Override
	public void actionPerformed( ActionEvent e )
	{
		// Handle open button action.
		if (e.getSource() == openButton)
		{
			int returnVal = fc.showOpenDialog(FilePlayerUI.this);

			if (returnVal == JFileChooser.APPROVE_OPTION)
			{
				File[] files = fc.getSelectedFiles();
				blmFilePlayer.updateBlmFileList(files);
				
				/** LOL: **/
				if( files!=null )
					if( files[0]!=null )
						if( files[0].getParentFile()!=null )
							if( files[0].getParentFile().exists() )
								if( files[0].getParentFile().isDirectory() )
									applicationMaster.getUserSettings().setProperty( UI_PROPERTIES_LASTLOCATION, files[0].getParentFile().getAbsolutePath() );
			}
			else
			{
				// log.append("Open command cancelled by user." + newline);
			}
		}
	}
	
	@Override
	public void componentShown( ComponentEvent e ) {}
	@Override
	public void componentResized( ComponentEvent e ) {}
	@Override
	public void componentMoved( ComponentEvent e )
	{
		// TODO: !!! edit this. this seems to be pretty unperformant
		if( applicationMaster!=null )
		{
			applicationMaster.getUserSettings().setProperty( UI_PROPERTIES_POS_X, getLocation().x );
			applicationMaster.getUserSettings().setProperty( UI_PROPERTIES_POS_Y, getLocation().y );
		}
	}
	@Override
	public void componentHidden( ComponentEvent e ) {}

	/** Returns an ImageIcon, or null if the path was invalid. */
	protected static ImageIcon createImageIcon( String path )
	{
		java.net.URL imgURL = FilePlayerUI.class.getResource(path);
		if (imgURL != null)
		{
			return new ImageIcon(imgURL);
		}
		else
		{
			System.err.println("Couldn't find file: " + path);
			return null;
		}
	}

}
