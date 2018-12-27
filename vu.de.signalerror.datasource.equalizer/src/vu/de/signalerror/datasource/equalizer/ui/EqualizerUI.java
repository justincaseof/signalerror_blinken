package vu.de.signalerror.datasource.equalizer.ui;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.PopupMenu;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Mixer;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import vu.de.signalerror.base.ApplicationMaster;
import vu.de.signalerror.datasource.equalizer.MixerMonitor;
import vu.de.signalerror.datasource.equalizer.MixerMonitorFaultListener;

@SuppressWarnings("serial")
public class EqualizerUI extends Frame implements ComponentListener, MixerMonitorFaultListener
{
	private static final String UI_PROPERTIES_POS_X = EqualizerUI.class.getName() + ".ui.pos_x";
	private static final String UI_PROPERTIES_POS_Y = EqualizerUI.class.getName() + ".ui.pos_y";
	
	private ApplicationMaster applicationMaster;
	private JPanel mainPanel;
	private JComboBox datalineChooser;
	private JLabel label;
	private Button okButton;
	
	public EqualizerUI( final MixerMonitor mixerMonitor, ApplicationMaster applicationMaster, String caption )
	{
		super( caption );
		this.applicationMaster = applicationMaster;
		this.mainPanel = new JPanel( new BorderLayout() );
		
		datalineChooser = new JComboBox( AudioSystem.getMixerInfo());
		datalineChooser.setSelectedItem( null );
		datalineChooser.addActionListener(
			new ActionListener()
    		{
    			@Override
    			public void actionPerformed( ActionEvent e )
    			{
    				String cmd = e.getActionCommand();
    				if ("comboBoxChanged".equals(cmd))
    				{
    					label.setText("");
    					
    					JComboBox src = (JComboBox) e.getSource();
    					Mixer.Info mi =  (Mixer.Info)  src.getSelectedItem();
    					mixerMonitor.startMixerMonitoring( mi, EqualizerUI.this );
    				}
    			}
    		}
		);
		// remember position of application
		// save new position to userconfig if user changes this.
		addComponentListener( this );
		
		// try to fetch user-stored window-position
		int pos_x = applicationMaster.getUserSettings().getInteger( UI_PROPERTIES_POS_X, 100 );
		int pos_y = applicationMaster.getUserSettings().getInteger( UI_PROPERTIES_POS_Y, 100 );
		
		setLocation(pos_x, pos_y);
		
		label = new JLabel();
		label.setSize( 150, 30 );
		
		mainPanel.add( datalineChooser, BorderLayout.NORTH );
		mainPanel.add( label, BorderLayout.CENTER );
		
		// add panel to ourselves
		this.add(mainPanel);
		this.setMinimumSize( new Dimension(400, 150) );
	}
	
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
	public void componentHidden( ComponentEvent e )
	{
	}
	@Override
	public void componentResized( ComponentEvent e )
	{
	}
	@Override
	public void componentShown( ComponentEvent e )
	{
	}

	@Override
	public void onFault( final String faultMessage )
	{
		label.setText( faultMessage );
	}

}
