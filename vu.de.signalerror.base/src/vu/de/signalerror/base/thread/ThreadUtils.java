package vu.de.signalerror.base.thread;

public class ThreadUtils
{
	public static void sleep( long milliSecs )
	{
		try
		{
			Thread.sleep( milliSecs );
		}
		catch (Exception e) 
		{
			e.printStackTrace();
		}
	}
}
