package vu.de.signalerror.blinken;

public interface BlinkenFrame
{
	public Boolean[][] getFrameData( );

	public void setFrameData( Boolean[][] frame );

	public void setPixel( int row, int col );

	public void clearPixel( int row, int col );

	public void togglePixel( int row, int col );

	public Boolean getPixel( int row, int col );
}
