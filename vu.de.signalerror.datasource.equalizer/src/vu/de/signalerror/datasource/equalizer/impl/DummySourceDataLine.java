package vu.de.signalerror.datasource.equalizer.impl;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.Control;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.Control.Type;

public class DummySourceDataLine implements SourceDataLine
{
	private AudioFormat format;
	
	public DummySourceDataLine( DataLine.Info sourceInfo )
	{
		// nuthn
	}
	
	@Override
	public void open( AudioFormat format ) throws LineUnavailableException
	{
		this.format = format;
	}

	@Override
	public void open( AudioFormat format, int bufferSize ) throws LineUnavailableException
	{
		this.format = format;
	}

	@Override
	public int write( byte[] b, int off, int len )
	{
		return 0;
	}

	@Override
	public int available( )
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void drain( )
	{
		// TODO Auto-generated method stub
	}
	@Override
	public void flush( )
	{
		// TODO Auto-generated method stub
		
	}
	@Override
	public int getBufferSize( )
	{
		// TODO Auto-generated method stub
		return 0;
	}
	@Override
	public AudioFormat getFormat( )
	{
		return this.format;
	}
	@Override
	public int getFramePosition( )
	{
		// TODO Auto-generated method stub
		return 0;
	}
	@Override
	public float getLevel( )
	{
		// TODO Auto-generated method stub
		return 0;
	}
	@Override
	public long getLongFramePosition( )
	{
		// TODO Auto-generated method stub
		return 0;
	}
	@Override
	public long getMicrosecondPosition( )
	{
		// TODO Auto-generated method stub
		return 0;
	}
	@Override
	public boolean isActive( )
	{
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public boolean isRunning( )
	{
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public void start( )
	{
		// TODO Auto-generated method stub
		
	}
	@Override
	public void stop( )
	{
		// TODO Auto-generated method stub
		
	}
	@Override
	public void addLineListener( LineListener listener )
	{
		// TODO Auto-generated method stub
		
	}
	@Override
	public void close( )
	{
		// TODO Auto-generated method stub
		
	}
	@Override
	public Control getControl( Type control )
	{
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public Control[] getControls( )
	{
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public javax.sound.sampled.Line.Info getLineInfo( )
	{
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public boolean isControlSupported( Type control )
	{
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public boolean isOpen( )
	{
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public void open( ) throws LineUnavailableException
	{
		// TODO Auto-generated method stub
		
	}
	@Override
	public void removeLineListener( LineListener listener )
	{
		// TODO Auto-generated method stub
		
	}
}
