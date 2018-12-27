package vu.de.signalerror.datasource.equalizer;

import javax.sound.sampled.Mixer;

public interface MixerMonitor
{
	public void startMixerMonitoring( Mixer.Info mixerInfo, MixerMonitorFaultListener faultListener );
}
