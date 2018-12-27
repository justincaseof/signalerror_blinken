package vu.de.signalerror.blinken.cmd;

import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.osgi.framework.console.CommandProvider;

import vu.de.signalerror.blinken.BlinkenApplication;
import vu.de.signalerror.blinken.BlinkenViewer;
import vu.de.signalerror.blinken.BlinkenViewerDataSource;

public class BlinkenApplicationStopCommand implements CommandProvider
{

	private BlinkenApplication controller;
	
	public BlinkenApplicationStopCommand(BlinkenApplication controller)
	{
		this.controller = controller;
	}
	
	public Object _stopFG( CommandInterpreter intp )
	{
		String stopWhat = intp.nextArgument();
		StringBuilder builder = new StringBuilder();

		int src = -1;
		try
		{
			src = Integer.valueOf(stopWhat);
			
			BlinkenViewerDataSource bvds = controller.getBlinkenViewerDataSources().get(src);
			bvds.stopFrameGeneration();
			
			builder.append("\nStopped " + bvds.getClass().getSimpleName()+ "["+bvds.hashCode()+"]."  );
		}
		catch(Exception nfe)
		{
			return "invalid argument.";
		}
		
		return builder.toString();
	}
	
	@Override
	public String getHelp() {
		return "stopFG <source_id>  -  stops frame generation of source with given id";
	}

}
