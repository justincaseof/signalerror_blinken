package vu.de.signalerror.blinken.cmd;

import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.osgi.framework.console.CommandProvider;

import vu.de.signalerror.blinken.BlinkenApplication;
import vu.de.signalerror.blinken.BlinkenViewer;
import vu.de.signalerror.blinken.BlinkenViewerDataSource;

public class BlinkenApplicationBindCommand implements CommandProvider
{

	private BlinkenApplication controller;
	
	public BlinkenApplicationBindCommand(BlinkenApplication controller)
	{
		this.controller = controller;
	}
	
	public Object _bind( CommandInterpreter intp )
	{
		String bindWhat = intp.nextArgument();
		String toWhat = intp.nextArgument();
		StringBuilder builder = new StringBuilder();

		int src = -1;
		int snk = -1;
		try
		{
			src = Integer.valueOf(bindWhat);
			snk = Integer.valueOf(toWhat);
			
			BlinkenViewerDataSource bvds = controller.getBlinkenViewerDataSources().get(src);
			BlinkenViewer bv = controller.getBlinkenViewers().get(snk);

			bvds.addConsumer(bv);
			
			builder.append("\nBound " + bv.getClass().getSimpleName()+ "["+bv.hashCode()+"] to " + bvds.getClass().getSimpleName()+ "["+bvds.hashCode()+"]."  );
		}
		catch(Exception nfe)
		{
			return "invalid argument.";
		}
		
		return builder.toString();
	}
	
	@Override
	public String getHelp() {
		return "bind <sink_id> <source_id>  -  Binds a DataSink to a DataSource.";
	}

}
