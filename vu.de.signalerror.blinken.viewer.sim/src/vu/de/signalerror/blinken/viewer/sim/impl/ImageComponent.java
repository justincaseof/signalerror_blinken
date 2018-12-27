package vu.de.signalerror.blinken.viewer.sim.impl;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.MediaTracker;
import java.net.URL;

public class ImageComponent extends Canvas
{
	private Image img = null;

	private static final int IMG_SIZE = 32;
	
	ImageComponent(URL sFile)
	{
		setBackground( Color.BLACK );
		
		img = getToolkit().getImage(sFile);
		MediaTracker mt = new MediaTracker(this);
		mt.addImage(img, 0);
		try
		{
			mt.waitForAll();
		}
		catch (InterruptedException ex)
		{
			ex.printStackTrace();
		}
	}

	public void paint( Graphics g )
	{
		g.drawImage(img, 0, 0, IMG_SIZE, IMG_SIZE, Color.BLACK, this);
	}

	public Dimension getPreferredSize( )
	{
//		return new Dimension( img.getWidth(this), img.getHeight(this) );
		return new Dimension(IMG_SIZE, IMG_SIZE);
	}

	public Dimension getMinimumSize( )
	{
		return getPreferredSize();
	}
}