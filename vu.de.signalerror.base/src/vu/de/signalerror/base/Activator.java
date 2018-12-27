package vu.de.signalerror.base;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator
{

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext
	 * )
	 */
	public void start( BundleContext context ) throws Exception
	{
		System.err.println( "\n["+ context.getBundle().getSymbolicName() +"] STARTING BASE BUNDLE... " );
		System.err.println(   "["+ context.getBundle().getSymbolicName() +"] STARTED. " );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop( BundleContext context ) throws Exception
	{
	}

}