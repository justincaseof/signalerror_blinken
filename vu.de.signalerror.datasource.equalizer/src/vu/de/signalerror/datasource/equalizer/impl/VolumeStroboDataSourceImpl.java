package vu.de.signalerror.datasource.equalizer.impl;


public class VolumeStroboDataSourceImpl extends EQ1_DataSourceImpl
{
	private static final int NUM_BANDS = 9;
	private static final int NUM_UPDATECYCLE_TO_AGGREGATE = 60;	// @60FPS => 1s
	private static final boolean BASS_ONLY = false;
	
	private boolean processing = false;
	private int updateCycleNum = 0;
	private double aggregatedVol = 0;
	
	private double averageVol = 0;

	private boolean notified_ON = false;
	private boolean notified_OFF = false;
	
	@Override
	public void updateBlinkenFrame( float[] bands )
	{
		if( processing )
		{
			throw new IllegalStateException( "New update faster than processing old update. This should never happen!" );
		}
		processing = true;
		
		try
		{
    		if( bands.length!=NUM_BANDS )
    		{
    			throw new IllegalStateException( "This should not happen!" );
    		}
    		else
    		{
    			float currentVol = 0;
    			if( BASS_ONLY )
    			{
    				currentVol = bands[0]; // bass only
    			}
    			else
    			{
        			for( int i=0; i<bands.length; i++ )
        			{
        				currentVol += bands[i];
        			}
        		}
    			
    			///////////////////
    			// CHECK
    			if( currentVol>averageVol*1.0 )
    			{
    				notified_OFF = false;
    				if( !notified_ON )
    				{
    					// light blinken
    					super.lightBlinken();
    					super.fireNewFrame();
    					notified_ON = true;
    				}
    			}
    			else
    			{
    				notified_ON = false;
    				if( !notified_OFF )
    				{
    					// clear blinken
    					super.clearBlinken();
    					super.fireNewFrame();
    					notified_OFF = true;
    				}
    			}	
    			
    			///////////////////
    			// UPDATE AVERAGE VOLUME OF LAST SECOND
    			aggregatedVol += currentVol;
    			if( updateCycleNum++ > NUM_UPDATECYCLE_TO_AGGREGATE )
    			{
    				averageVol = aggregatedVol/updateCycleNum;
        			updateCycleNum = 0;
        			aggregatedVol = 0;
    			}
    			//////////////////
    		}
		}
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		finally
		{
			processing = false;
		}
	}
	
}
