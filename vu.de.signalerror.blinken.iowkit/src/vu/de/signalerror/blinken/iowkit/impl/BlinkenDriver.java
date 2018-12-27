package vu.de.signalerror.blinken.iowkit.impl;
import com.codemercs.iow.IowKit;

public class BlinkenDriver
{
	private long handles[];
	int numPipe = 0x00;
	int reportId = 0x00;
	
	public static final long PID_IOW40=0x1500L;
	
	private boolean initialized = false;
	
	/*
	 * DATA: (port a)
	 * 0 - data01
	 * 1 - data03
	 * 2 - data05
	 * 3 - data07
	 * 4 - data09
	 * 5 - data11
	 * 6 - data13
	 * 7 - data15
	 * 
	 * CTRL: (port  b)
	 * 0 - data17
	 * 1 - CLK
	 * 2 - STROBE
	 * 3 - OE
	 * 4 - GND
	 * 
	 */
		
	public BlinkenDriver()
	{
	}
	
	public boolean openIow()
	{
	    if(IowKit.openDevice()!=0L)
	    {
			handles=new long[(int)IowKit.getNumDevs()];
			for(int i=0,j=1;i<handles.length;i++,j++)
			    handles[i]=IowKit.getDeviceHandle(j);
			
			initialized = true;
	    }
	    else
	    {
			System.err.println("No IOW found!");
			initialized = false;
	    }
	    return initialized;
	}
	
	public void closeIow()
	{
	    for(int i=0,j=1;i<handles.length;i++,j++)
		IowKit.closeDevice( handles[i] );
	}
	
	public boolean initialized()
	{
		return initialized;
	}
	
	private void blink(int data, int data17)
	{
		// CLK = 		(LPT: Pin14) IOW: Port1:Pin2 = 0x02
		// STROBE = 	(LPT: Pin16) IOW: Port1:Pin3 = 0x04
		// OE = 		(LPT: Pin17) IOW: Port1:Pin4 = 0x08
		
		int idata[] = new int[5];
		idata[0]= reportId;
		idata[1]= data;
		idata[2]= data17 & 0x01;	// only the first bit is for a row in the display
		
		idata[2] += 0x0A;	// CLK=1; STROBE=0; OE=1
		IowKit.write( handles[0], numPipe, idata );
		
		idata[2]= 0x08;		// CLK=0; STROBE=0; OE=1
		IowKit.write( handles[0], numPipe, idata );
	}

	private void strobe()
	{
		int []idata = new int[5];
		idata[2]= 0x0C;		// CLK=0; STROBE=1; OE=1
		IowKit.write( handles[0], numPipe, idata );
	}
	
	public void refresh_blinken(int frame[][])
	{
		if( !initialized )
			return;
		
		int i;
		int j;

		for(i=7; i>=0; i--)
		{
			int data=0;
			int data17=0;

			for(j=1; j<=17; j+=2)
			{
				if(frame[i][j]==1)
				{
					data = data + (int) Math.pow( 2, (j - 1) / 2 );
				}
				if(j==17)
				{
					if(frame[i][17]==1)
						data17 = 1;
				}
			}
			blink(data,data17);
		}

		for(i=7; i>=0; i--)
		{
			int data=0;
			int data17=0;

			for(j=0; j<=16; j+=2)
			{
				if(frame[i][j]==1)
				{
					data = data + (int) Math.pow( 2, (j / 2) );
				}
				if(j==16)
				{
					if(frame[i][16]==1)
						data17 = 1;
				}
			}
			blink(data,data17);
		}
		
		// frame komplett geschrieben.
		// jetzt geschriebenes frame an den ausgaengen der schieberegister uebernehemn
		strobe();
	}
}