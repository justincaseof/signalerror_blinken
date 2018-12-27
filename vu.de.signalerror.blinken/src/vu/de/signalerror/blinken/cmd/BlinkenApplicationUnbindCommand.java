package vu.de.signalerror.blinken.cmd;

import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.osgi.framework.console.CommandProvider;

import vu.de.signalerror.blinken.BlinkenApplication;
import vu.de.signalerror.blinken.BlinkenViewer;
import vu.de.signalerror.blinken.BlinkenViewerDataSource;

public class BlinkenApplicationUnbindCommand implements CommandProvider
{

	private BlinkenApplication controller;
	
	public BlinkenApplicationUnbindCommand(BlinkenApplication controller)
	{
		this.controller = controller;
	}
	
	public Object _unbind( CommandInterpreter intp )
	{
		String unbindWhat = intp.nextArgument();
		String toWhat = intp.nextArgument();
		StringBuilder builder = new StringBuilder();

		int src = -1;
		int snk = -1;
		try
		{
			src = Integer.valueOf(unbindWhat);
			snk = Integer.valueOf(toWhat);
			
			BlinkenViewerDataSource bvds = controller.getBlinkenViewerDataSources().get(src);
			BlinkenViewer bv = controller.getBlinkenViewers().get(snk);

			bvds.removeConsumer(bv);
			
			builder.append("\nUnbound " + bv.getClass().getSimpleName()+ "["+bv.hashCode()+"] from " + bvds.getClass().getSimpleName()+ "["+bvds.hashCode()+"]."  );
		}
		catch(Exception nfe)
		{
			return "invalid argument.";
		}
		
		return builder.toString();
	}
	
	@Override
	public String getHelp() {
		return "unbind <sink_id> <source_id>  -  Unbinds a DataSink from a DataSource.";
	}

}
