package vu.de.signalerror.blinken.datasource.simple.ui;

public interface DelayChangeListener
{
	/**
	 * delayValue has to be between 0 and 100.
	 * @param delayValue
	 */
	public void onNewDelayValue(int delayValue);
}
