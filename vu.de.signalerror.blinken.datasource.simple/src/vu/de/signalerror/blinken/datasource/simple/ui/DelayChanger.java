package vu.de.signalerror.blinken.datasource.simple.ui;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import vu.de.signalerror.base.ApplicationMaster;

@SuppressWarnings("serial")
public class DelayChanger extends Frame implements ChangeListener, ComponentListener
{
	private String UI_PROPERTIES_POS_X;
	private String UI_PROPERTIES_POS_Y;
	
	private ApplicationMaster applicationMaster;
	private JPanel mainPanel;
	private DelayChangeListener listener;

	public DelayChanger(DelayChangeListener listener, ApplicationMaster applicationMaster, String caption)
	{
		super( caption );
		this.listener = listener;
		this.applicationMaster = applicationMaster;
		this.mainPanel = new JPanel(new BorderLayout());
		
		this.UI_PROPERTIES_POS_X = listener.getClass().getName() + ".ui.pos_x";
		this.UI_PROPERTIES_POS_Y = listener.getClass().getName() + ".ui.pos_y";
		
		// create a slider with values between 0 and 100.
		JSlider slider = new JSlider( 0, 100 );
		slider.setValue(40);
		slider.addChangeListener( this );
		
		// remember position of application
		// save new position to userconfig if user changes this.
		addComponentListener( this );
		
		// try to fetch user-stored window-position
		int pos_x = applicationMaster.getUserSettings().getInteger(UI_PROPERTIES_POS_X, 100 );
		int pos_y = applicationMaster.getUserSettings().getInteger(UI_PROPERTIES_POS_Y, 100 );
		
		setLocation(pos_x, pos_y);
		
		// add slider to mainPanel
		mainPanel.add(slider, BorderLayout.CENTER);
		// add panel to ourselves
		add(mainPanel);
	}

	@Override
	public void stateChanged( ChangeEvent e )
	{
		JSlider slider = (JSlider) e.getSource();
		listener.onNewDelayValue(slider.getValue());
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
	

}
