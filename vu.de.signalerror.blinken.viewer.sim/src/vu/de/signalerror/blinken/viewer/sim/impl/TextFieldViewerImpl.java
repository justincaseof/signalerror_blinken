package vu.de.signalerror.blinken.viewer.sim.impl;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.LayoutManager;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import org.osgi.service.component.ComponentContext;

import vu.de.signalerror.base.ApplicationMaster;
import vu.de.signalerror.base.event.Event;
import vu.de.signalerror.base.event.EventConsumer;
import vu.de.signalerror.base.event.EventSource;
import vu.de.signalerror.blinken.BlinkenConfig;
import vu.de.signalerror.blinken.BlinkenFrame;
import vu.de.signalerror.blinken.BlinkenViewer;
import vu.de.signalerror.blinken.BlinkenViewerDataSource;

public class TextFieldViewerImpl extends Frame implements BlinkenViewer, EventConsumer
{
	private static final long serialVersionUID = 1L;
	
	private static final String UI_PROPERTIES_POS_X = TextFieldViewerImpl.class.getName() + ".ui.pos_x";
	private static final String UI_PROPERTIES_POS_Y = TextFieldViewerImpl.class.getName() + ".ui.pos_y";

	private ApplicationMaster applicationMaster;
	
	private boolean active = false;

	private BlinkenConfig blinkenConfig;

	/* UI elements */
	private JPanel jpanel_Main = new JPanel();
	private JTextArea jTextarea_blinkenView;

	public TextFieldViewerImpl()
	{
		super("SimViewer");

		addWindowListener(new WindowAdapter()
		{
			public void windowClosing( WindowEvent e )
			{
				//System.exit(0);
			}
		});

		if (blinkenConfig != null && applicationMaster!=null)
		{
			setupLayout();
		}
	}
	
	protected void activate( ComponentContext componentContext )
	{
		this.applicationMaster = (ApplicationMaster) componentContext.locateService("ApplicationMaster");
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
		LayoutManager layoutManager = new BoxLayout(jpanel_Main, BoxLayout.Y_AXIS);
		jpanel_Main.setLayout(layoutManager);

		// LayoutManager gridLayout = new GridLayout();

		this.jTextarea_blinkenView = new JTextArea(this.blinkenConfig.getRows(), this.blinkenConfig.getColumns());
		jTextarea_blinkenView.setEditable(false);
		jTextarea_blinkenView.setLineWrap(false);
		jTextarea_blinkenView.setFont(new Font(Font.MONOSPACED, Font.BOLD, 14));

		jpanel_Main.add(jTextarea_blinkenView);

		add(jpanel_Main);
		
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

		Dimension size = new Dimension(450, 195);
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
			StringBuffer buf = new StringBuffer();
			for (int rows = 0; rows < blinkenConfig.getRows(); rows++)
			{
				for (int cols = 0; cols < blinkenConfig.getColumns(); cols++)
				{
					buf.append(frame.getPixel(rows, cols) ? " # " : "   ");
				}
				buf.append("\n");
			}
			jTextarea_blinkenView.setText(buf.toString());
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
