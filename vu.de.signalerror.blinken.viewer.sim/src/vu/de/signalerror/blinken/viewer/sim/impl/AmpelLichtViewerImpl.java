package vu.de.signalerror.blinken.viewer.sim.impl;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.LayoutManager;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import org.osgi.service.component.ComponentContext;
import org.osgi.framework.BundleContext;

import vu.de.signalerror.base.ApplicationMaster;
import vu.de.signalerror.base.event.Event;
import vu.de.signalerror.base.event.EventConsumer;
import vu.de.signalerror.base.event.EventSource;
import vu.de.signalerror.blinken.BlinkenConfig;
import vu.de.signalerror.blinken.BlinkenFrame;
import vu.de.signalerror.blinken.BlinkenViewer;
import vu.de.signalerror.blinken.BlinkenViewerDataSource;

public class AmpelLichtViewerImpl extends Frame implements BlinkenViewer, EventConsumer
{
	private static final long serialVersionUID = 1L;
	
	private static final String UI_PROPERTIES_POS_X = AmpelLichtViewerImpl.class.getName() + ".ui.pos_x";
	private static final String UI_PROPERTIES_POS_Y = AmpelLichtViewerImpl.class.getName() + ".ui.pos_y";

	private ApplicationMaster applicationMaster;
	
	private boolean active = false;

	private BlinkenConfig blinkenConfig;

	/* UI elements */
	private JPanel jpanel_Main = new JPanel();

	private URL imgURL;
	private URL ampel_aus_URL;
	
	public AmpelLichtViewerImpl()
	{
		super("AmpelLichtViewer");

		addWindowListener(new WindowAdapter()
		{
			public void windowClosing( WindowEvent e )
			{
				//System.exit(0);
			}
		});

		if ( blinkenConfig != null && applicationMaster!=null )
		{
			setupLayout();
		}
	}
	
	protected void activate( ComponentContext componentContext )
	{
		this.applicationMaster = (ApplicationMaster) componentContext.locateService("ApplicationMaster");
		
		Enumeration<?> e = null;
		
		e = componentContext.getBundleContext().getBundle().findEntries(".", "ampel_rot.jpg", true);
		// take first match.
		if( e.hasMoreElements() )
		{
			imgURL = (URL) e.nextElement();
		}
		
		e = componentContext.getBundleContext().getBundle().findEntries(".", "ampel_rot_aus.jpg", true);
		// take first match.
		if( e.hasMoreElements() )
		{
			ampel_aus_URL = (URL) e.nextElement();
		}
	}
	
	public void setBlinkenConfig( BlinkenConfig blinkenConfig )
	{
		if (this.blinkenConfig == null) // first time blinkenConfig is set. so
		{
			this.blinkenConfig = blinkenConfig;
			setupLayout();
		}
		else
		{
			this.blinkenConfig = blinkenConfig;
			setupLayout();
		}
	}

	@Override
	public boolean isActive( )
	{
		return active;
	}

	@Override
	public void setActive( boolean active )
	{
		this.active = active;
		setVisible(active);
	}

	private void setupLayout( )
	{
		// set layout
		LayoutManager layoutManager = new GridLayout(8, 18);
		jpanel_Main.setLayout(layoutManager);

		this.add(jpanel_Main);
		
		// save new position to userconfig if user changes this.
		addComponentListener( 
			new ComponentListener()
    		{
    			@Override
    			public void componentShown( ComponentEvent e ) {}
    			@Override
    			public void componentResized( ComponentEvent e ) {}
    			@Override
    			public void componentMoved( ComponentEvent e )
    			{
    				// TODO: !!! edit this. this seems to be pretty unperformant
    				applicationMaster.getUserSettings().setProperty( UI_PROPERTIES_POS_X, getLocation().x );
    				applicationMaster.getUserSettings().setProperty( UI_PROPERTIES_POS_Y, getLocation().y );
    			}
    			@Override
    			public void componentHidden( ComponentEvent e ) {}
    		}
		);

		Dimension size = new Dimension( 564, 270 );
		setSize(size);
		setPreferredSize(size);
		setResizable(false);

		int pos_x = applicationMaster.getUserSettings().getInteger(this.getClass().getName()+".ui.pos_x", 100 );
		int pos_y = applicationMaster.getUserSettings().getInt( this.getClass().getName()+".ui.pos_y", 100 );
		
		setLocation(pos_x, pos_y);
		setVisible(active);
	}

	private void showFrame( BlinkenFrame frame )
	{
		if (active)
		{
//			jpanel_Main.removeAll();
//			for( int i=0; i<18; i++ )
//			{
//				for( int j=0; j<8; j++ )
//				{
//					if( frame.getPixel(j, i) )
//					{
//						jpanel_Main.add(  new ImageComponent( imgURL )  );
//					}
//					else
//					{
//						jpanel_Main.add(  new ImageComponent( ampel_aus_URL )  );
//					}
//				}
//			}
		}
	}
	
	@Override
	public void notify( Event event )
	{
		EventSource src = null;
		if (((src = event.getEventSource()) instanceof BlinkenViewerDataSource))
		{
			BlinkenFrame frame = ((BlinkenViewerDataSource) src).getCurrentFrame();
			showFrame(frame);
		}

	}

}
