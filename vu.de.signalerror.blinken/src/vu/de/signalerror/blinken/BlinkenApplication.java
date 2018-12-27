package vu.de.signalerror.blinken;

import java.util.Vector;

import org.apache.commons.configuration.Configuration;
import org.apache.log4j.Logger;
import org.eclipse.osgi.framework.console.CommandProvider;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.event.EventAdmin;

import vu.de.signalerror.base.ApplicationMaster;
import vu.de.signalerror.base.event.EventType;
import vu.de.signalerror.base.event.impl.EventImpl;
import vu.de.signalerror.base.event.impl.EventSourceBaseImpl;
import vu.de.signalerror.blinken.cmd.BlinkenApplicationBindCommand;
import vu.de.signalerror.blinken.cmd.BlinkenApplicationListCommand;
import vu.de.signalerror.blinken.cmd.BlinkenApplicationStartCommand;
import vu.de.signalerror.blinken.cmd.BlinkenApplicationStopCommand;
import vu.de.signalerror.blinken.cmd.BlinkenApplicationUnbindCommand;
import vu.de.signalerror.blinken.impl.BlinkenConfigImpl;
import vu.de.signalerror.blinken.ui.UI;

public class BlinkenApplication extends EventSourceBaseImpl
{
	private static Logger logger = Logger.getLogger(BlinkenApplication.class);

	private ComponentContext componentContext;
	private EventAdmin eventAdmin;
	private ApplicationMaster applicationMaster;
	private Configuration config;

	private BlinkenConfig blinkenConfig;

	private Vector<BlinkenViewer> blinkenViewers = new Vector<BlinkenViewer>();
	private Vector<BlinkenViewerDataSource> blinkenViewerDataSources = new Vector<BlinkenViewerDataSource>();
	private UI ui;

	protected void activate( ComponentContext componentContext )
	{
		try
		{
			this.eventAdmin = (EventAdmin) componentContext.locateService("EventAdmin");
			this.applicationMaster = (ApplicationMaster) componentContext.locateService("ApplicationMaster");
			this.config = applicationMaster.getConfigurtation();
			this.componentContext = componentContext;
			this.ui = new UI(this);

			this.blinkenConfig = new BlinkenConfigImpl();
			blinkenConfig.setColumns(config.getInt("blinken.columns"));
			blinkenConfig.setRows(config.getInt("blinken.rows"));

			// add already registered services.
			ServiceReference[] refs;
			// BlinkenViewer
			refs = componentContext.getBundleContext().getAllServiceReferences(BlinkenViewer.class.getName(), null);
			if (refs != null)
				for (ServiceReference ref : refs)
					addBlinkenViewer(ref);
			// BlinkenViewerDataSource
			refs = componentContext.getBundleContext().getAllServiceReferences(BlinkenViewerDataSource.class.getName(), null);
			if (refs != null)
				for (ServiceReference ref : refs)
					addBlinkenViewerDataSource(ref);
			
			// register commands
			registerCommands();
		}
		catch (Exception e)
		{
			logger.error("Error initiating BlinkenApplication: ", e);
		}
	}

	protected void deactivate( ComponentContext componentContext )
	{
		this.eventAdmin = null;
	}

	protected void addBlinkenViewer( ServiceReference reference )
	{
		if (componentContext == null)
			return;
		BlinkenViewer blinkenViewer = (BlinkenViewer) componentContext.getBundleContext().getService(reference);
		blinkenViewer.setBlinkenConfig(blinkenConfig);

		this.blinkenViewers.add(blinkenViewer);
		this.ui.notify(new EventImpl(EventType.BLINKENVIEWER_LIST_CHANGED, this));
		logger.debug("Added BlinkenViewer: " + blinkenViewer.getClass().getSimpleName());
	}

	protected void removeBlinkenViewer( ServiceReference reference )
	{
		if (componentContext == null)
			return;
		BlinkenViewer blinkenViewer = (BlinkenViewer) componentContext.getBundleContext().getService(reference);
		this.blinkenViewers.remove(blinkenViewer);
		this.ui.notify(new EventImpl(EventType.BLINKENVIEWER_LIST_CHANGED, this));
		logger.debug("Removed BlinkenViewer: " + blinkenViewer.getClass().getSimpleName());
	}

	protected void addBlinkenViewerDataSource( ServiceReference reference )
	{
		if (componentContext == null)
			return;
		BlinkenViewerDataSource blinkenViewerDataSource = (BlinkenViewerDataSource) componentContext.getBundleContext().getService(reference);
		blinkenViewerDataSource.setBlinkenConfig(blinkenConfig);

		this.blinkenViewerDataSources.add(blinkenViewerDataSource);
		this.ui.notify(new EventImpl(EventType.BLINKENVIEWER_DATASOURCES_LIST_CHANGED, this));
		logger.debug("Added BlinkenViewerDataSource: " + blinkenViewerDataSource.getClass().getSimpleName());
	}

	protected void removeBlinkenViewerDataSource( ServiceReference reference )
	{
		if (componentContext == null)
			return;
		BlinkenViewerDataSource blinkenViewerDataSource = (BlinkenViewerDataSource) componentContext.getBundleContext().getService(reference);
		this.blinkenViewerDataSources.remove(blinkenViewerDataSource);
		this.ui.notify(new EventImpl(EventType.BLINKENVIEWER_DATASOURCES_LIST_CHANGED, this));
		logger.debug("Removed BlinkenViewerDataSource: " + blinkenViewerDataSource.getClass().getSimpleName());
	}

	public Vector<BlinkenViewer> getBlinkenViewers( )
	{
		return this.blinkenViewers;
	}

	public Vector<BlinkenViewerDataSource> getBlinkenViewerDataSources( )
	{
		return this.blinkenViewerDataSources;
	}
	
	private void registerCommands()
	{
		this.componentContext.getBundleContext().registerService(CommandProvider.class.getName(), new BlinkenApplicationListCommand(this), null);
		this.componentContext.getBundleContext().registerService(CommandProvider.class.getName(), new BlinkenApplicationBindCommand(this), null);
		this.componentContext.getBundleContext().registerService(CommandProvider.class.getName(), new BlinkenApplicationUnbindCommand(this), null);
		this.componentContext.getBundleContext().registerService(CommandProvider.class.getName(), new BlinkenApplicationStartCommand(this), null);
		this.componentContext.getBundleContext().registerService(CommandProvider.class.getName(), new BlinkenApplicationStopCommand(this), null);
	}

}