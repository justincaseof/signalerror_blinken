package vu.de.signalerror.blinken.cmd;

import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.osgi.framework.console.CommandProvider;

import vu.de.signalerror.blinken.BlinkenApplication;
import vu.de.signalerror.blinken.BlinkenViewer;
import vu.de.signalerror.blinken.BlinkenViewerDataSource;

public class BlinkenApplicationListCommand implements CommandProvider
{

	private BlinkenApplication controller;
	
	public BlinkenApplicationListCommand(BlinkenApplication controller)
	{
		this.controller = controller;
	}
	
	public Object _list( CommandInterpreter intp )
	{
		String listWhat = intp.nextArgument();
		StringBuilder builder = new StringBuilder();
		if( "sources".equals(listWhat) )
		{
			builder.append("\n");
			builder.append("\n");
			builder.append("-- available sources: --\n");
			int i=0;
			for( BlinkenViewerDataSource ds : controller.getBlinkenViewerDataSources() )
			{
				builder.append("\t[" + i++ + "] " + ds.getClass().getName() + "[" + ds.hashCode() + "]" +"\n");
			}
			
		}
		else if("sinks".equals(listWhat) )
		{
			builder.append("\n");
			builder.append("\n");
			builder.append("-- available sinks: --\n");
			int i=0;
			for( BlinkenViewer bv : controller.getBlinkenViewers() )
			{
				builder.append("\t[" + i++ + "] " + bv.getClass().getName() + "[" + bv.hashCode() + "]" +"\n");
			}
			
		}
		else
		{
			return "invalid argument.";
		}
		return builder.toString();
	}
	
	@Override
	public String getHelp() {
		return "list {sources|sinks} -  list sources and sinks";
	}

}
