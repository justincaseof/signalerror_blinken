package vu.de.signalerror.blinken.impl;

import vu.de.signalerror.blinken.BlinkenFrame;

public class BlinkenFrameImpl implements BlinkenFrame
{
	private Boolean[][] frame;

	@Override
	public Boolean[][] getFrameData( )
	{
		return frame;
	}

	@Override
	public void setFrameData( Boolean[][] frame )
	{
		this.frame = frame;
	}

	@Override
	public Boolean getPixel( int row, int col )
	{
		return frame[row][col];
	}

	@Override
	public void setPixel( int row, int col )
	{
		frame[row][col] = true;
	}

	@Override
	public void clearPixel( int row, int col )
	{
		frame[row][col] = false;
	}

	@Override
	public void togglePixel( int row, int col )
	{
		frame[row][col] = !frame[row][col];
	}
	
//	@Override
//	public String toString()
//	{
//		if( frame==null )
//		{
//			return "NO FRAME AVAILABLE!";
//		}
//			
//		String result = "";
//		
//		for( )
//		
//		return result;
//	}
	
}
