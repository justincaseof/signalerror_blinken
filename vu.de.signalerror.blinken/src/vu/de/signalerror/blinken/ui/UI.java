package vu.de.signalerror.blinken.ui;

import java.awt.Button;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JPanel;

import org.apache.log4j.Logger;

import vu.de.signalerror.base.event.Event;
import vu.de.signalerror.base.event.EventConsumer;
import vu.de.signalerror.base.event.EventSource;
import vu.de.signalerror.base.event.EventType;
import vu.de.signalerror.base.event.impl.EventImpl;
import vu.de.signalerror.blinken.BlinkenApplication;
import vu.de.signalerror.blinken.BlinkenFrame;
import vu.de.signalerror.blinken.BlinkenViewer;
import vu.de.signalerror.blinken.BlinkenViewerDataSource;
import vu.de.signalerror.blinken.impl.BlinkenFrameImpl;
import vu.de.signalerror.blinken.ui.table.BlinkenViewerTable;

public class UI extends Frame implements ActionListener, EventConsumer
{
	private static Logger logger = Logger.getLogger(UI.class);
	private static final long serialVersionUID = 1L;

	private BlinkenApplication blinkenApp;

	// / GUI elements \\\
	private BlinkenViewerTable jPanel_BlinkenViewerTable;

	private JPanel jPanel_Main = new JPanel();
	private JPanel jPanel_Buttons = new JPanel();

	private JPanel jPanel_blinkenViewer = new JPanel();
	private JPanel jPanel_blinkenDataSource = new JPanel();

	private JComboBox jComboBox_dataSources;

	private Button addButton( String caption )
	{
		Button cmd = new Button(caption);
		cmd.addActionListener(this);
		return cmd;
	}

	public UI(BlinkenApplication blinkenApp)
	{
		super("sIgNaLeRrOr");

		this.blinkenApp = blinkenApp;
		blinkenApp.addConsumer(this);

		addWindowListener(new WindowAdapter()
		{
			public void windowClosing( WindowEvent e )
			{
				System.exit(0);
			}
		});

		setupLayout();

		this.setVisible(true);
	}

	private void setupLayout( )
	{
		// set layout
		LayoutManager layoutManager = new BoxLayout(jPanel_Main, BoxLayout.Y_AXIS);
		jPanel_Main.setLayout(layoutManager);

		LayoutManager gridLayout = new GridLayout();

		// adjust orientation
		jPanel_blinkenViewer.setLayout(gridLayout);
		jPanel_blinkenViewer.setMaximumSize(new Dimension(600, 30));

		/* jpanel_blinkenDataSource */
		this.jComboBox_dataSources = initializeDataSourceCombobox();
		jComboBox_dataSources.setPreferredSize(new Dimension(400, 30));
		jPanel_blinkenDataSource.setPreferredSize(new Dimension(400, 30));
		jPanel_blinkenDataSource.add(jComboBox_dataSources);
		jPanel_Main.add(jPanel_blinkenDataSource);

		/* jTable_BlinkenViewer */
		jPanel_BlinkenViewerTable = new BlinkenViewerTable(blinkenApp);
		jPanel_BlinkenViewerTable.setOpaque(true);
		jPanel_blinkenViewer.add(new Label("BlinkenViewer:"));
		jPanel_Main.add(jPanel_BlinkenViewerTable);

		/* buttons */
		jPanel_Buttons.add(addButton("Clear"));
		jPanel_Buttons.add(addButton("Exit"));
		jPanel_Main.add(jPanel_Buttons);

		add(jPanel_Main);
		setSize(500, 300);
	}

	/**
	 * Button-Events
	 **/
	public void actionPerformed( ActionEvent e )
	{
		String caption = e.getActionCommand();

		if (caption.equals("Exit"))
		{
			System.exit(0);
		}
		else if (caption.equals("Clear"))
		{
			Vector<BlinkenViewer> viewers = blinkenApp.getBlinkenViewers();
			if( viewers!=null )
				for( BlinkenViewer viewer : viewers )
				{
					EventSource clearSrc = new BlinkenViewerDataSource()
					{
						@Override
						public BlinkenFrame getCurrentFrame( )
						{
							BlinkenFrame frame = new BlinkenFrameImpl();
							frame.setFrameData(
    							new Boolean[][]
    							{ 
    								{false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false},
    								{false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false},
    								{false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false},
    								{false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false},
    								{false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false},
    								{false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false},
    								{false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false},
    								{false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false},
    							}
    						);
							return frame;
						}
						@Override
						public void startFrameGeneration( ){}
						@Override
						public void stopFrameGeneration( ){}
						
					};
					EventImpl event = new EventImpl(EventType.NEW_FRAME, clearSrc);
					viewer.notify(event);
				}
		}

	}

	private JComboBox initializeDataSourceCombobox( )
	{
		JComboBox result = new JComboBox(blinkenApp.getBlinkenViewerDataSources());
		result.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed( ActionEvent e )
			{
				String cmd = e.getActionCommand();
				if ("comboBoxChanged".equals(cmd))
				{
					JComboBox src = (JComboBox) e.getSource();

					// 1) stop old data sources
					for (int i = 0; i < src.getItemCount(); i++)
					{
						((BlinkenViewerDataSource) src.getItemAt(i)).stopFrameGeneration();
						((BlinkenViewerDataSource) src.getItemAt(i)).removeAllConsumers();
					}

					// 2) start new selected datasource
					BlinkenViewerDataSource dataSrc = (BlinkenViewerDataSource) src.getSelectedItem();
					dataSrc.startFrameGeneration();

					for (BlinkenViewer bV : blinkenApp.getBlinkenViewers())
						dataSrc.addConsumer(bV);
				}
			}
		});
		return result;
	}

	public void notify( Event event )
	{
		switch (event.getEventType())
		{
		case BLINKENVIEWER_LIST_CHANGED:
			this.jPanel_BlinkenViewerTable.tableDataChanged();
			break;
		case BLINKENVIEWER_DATASOURCES_LIST_CHANGED:
			// 
			break;
		default:
			logger.error("Unknown EventType: " + event.getEventType().name());
		}

	}
	
}
