package vu.de.signalerror.blinken.impl;

import vu.de.signalerror.blinken.BlinkenConfig;

public class BlinkenConfigImpl implements BlinkenConfig
{
	private int rows;
	private int columns;

	public int getRows( )
	{
		return rows;
	}

	public void setRows( int rows )
	{
		this.rows = rows;
	}

	public int getColumns( )
	{
		return columns;
	}

	public void setColumns( int columns )
	{
		this.columns = columns;
	}
}
