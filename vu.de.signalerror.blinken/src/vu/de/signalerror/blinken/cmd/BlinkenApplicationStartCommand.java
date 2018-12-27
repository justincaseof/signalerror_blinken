package vu.de.signalerror.blinken.cmd;

import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.osgi.framework.console.CommandProvider;

import vu.de.signalerror.blinken.BlinkenApplication;
import vu.de.signalerror.blinken.BlinkenViewer;
import vu.de.signalerror.blinken.BlinkenViewerDataSource;

public class BlinkenApplicationStartCommand implements CommandProvider
{

	private BlinkenApplication controller;
	
	public BlinkenApplicationStartCommand(BlinkenApplication controller)
	{
		this.controller = controller;
	}
	
	public Object _startFG( CommandInterpreter intp )
	{
		String startWhat = intp.nextArgument();
		StringBuilder builder = new StringBuilder();

		int src = -1;
		try
		{
			src = Integer.valueOf(startWhat);
			
			BlinkenViewerDataSource bvds = controller.getBlinkenViewerDataSources().get(src);
			bvds.startFrameGeneration();
			
			builder.append("\nStarted " + bvds.getClass().getSimpleName()+ "["+bvds.hashCode()+"]."  );
		}
		catch(Exception nfe)
		{
			return "invalid argument.";
		}
		
		return builder.toString();
	}
	
	@Override
	public String getHelp() {
		return "startFG <source_id>  -  starts frame generation of source with given id";
	}

}
