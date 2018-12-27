package vu.de.signalerror.datasource.equalizer.impl;

import java.text.DecimalFormat;
import java.util.LinkedList;
import java.util.List;

import javax.sound.sampled.SourceDataLine;

import org.apache.log4j.Logger;

import kjdss.KJDigitalSignalProcessor;
import kjdss.KJFFT;
import kjdss.KJDigitalSignalSynchronizer.Context;
import kjdss.ui.KJScopeAndSpectrumAnalyzer.Band;
import kjdss.ui.KJScopeAndSpectrumAnalyzer.BandDistribution;
import kjdss.ui.KJScopeAndSpectrumAnalyzer.BandGain;
import kjdss.ui.KJScopeAndSpectrumAnalyzer.FlatBandGain;
import kjdss.ui.KJScopeAndSpectrumAnalyzer.FrequencyBandGain;
import kjdss.ui.KJScopeAndSpectrumAnalyzer.LinearBandDistribution;
import kjdss.ui.KJScopeAndSpectrumAnalyzer.LogBandDistribution;
import vu.de.signalerror.datasource.equalizer.FFT_to_BlinkenFrameConverter;

public class BlinkenSignalProcessor implements KJDigitalSignalProcessor
{
	private static Logger logger = Logger.getLogger( BlinkenSignalProcessor.class);
	
	public static final BandDistribution BAND_DISTRIBUTION_LINEAR = new LinearBandDistribution();
	public static final BandDistribution BAND_DISTRIBUTION_LOG = new LogBandDistribution(4, 20.0);

	public static final BandGain BAND_GAIN_FLAT = new FlatBandGain(4.0f);
	public static final BandGain BAND_GAIN_FREQUENCY = new FrequencyBandGain(4.0f);

	public static final int DISPLAY_MODE_SCOPE = 0;
	public static final int DISPLAY_MODE_SPECTRUM_ANALYSER = 1;
	public static final int DISPLAY_MODE_VU_METER = 2;

	public static final int DEFAULT_WIDTH = 256;
	public static final int DEFAULT_HEIGHT = 128;

	public static final int DEFAULT_SCOPE_DETAIL_LEVEL = 1;

	public static final int DEFAULT_SPECTRUM_ANALYSER_BAND_COUNT = 16;
	public static final BandDistribution DEFAULT_SPECTRUM_ANALYSER_BAND_DISTRIBUTION = BAND_DISTRIBUTION_LINEAR;
	public static final BandGain DEFAULT_SPECTRUM_ANALYSER_BAND_GAIN = BAND_GAIN_FREQUENCY;
	public static final float DEFAULT_SPECTRUM_ANALYSER_DECAY = 0.03f;
	public static final float DEFAULT_SPECTRUM_ANALYSER_GAIN = 1.0f;

	// -- Spectrum analyser varibles.
	private int saFFTSampleSize;
	private float saFFTSampleRate;
	private float saDecay = DEFAULT_SPECTRUM_ANALYSER_DECAY;
	private float saGain = DEFAULT_SPECTRUM_ANALYSER_GAIN;

	private int saBands;// = 9;
	private BandDistribution saBandDistribution = DEFAULT_SPECTRUM_ANALYSER_BAND_DISTRIBUTION;
	private Band[] sabdTable;
	private BandGain saBandGain = DEFAULT_SPECTRUM_ANALYSER_BAND_GAIN;
	private float[] sabgTable;

	private float saBandWidth;

	private KJFFT fft;
	private float[] old_FFT;
	
	private int width;
	
	// -- VU Meter
	private float[] oldVolume;
	
	private List<FFT_to_BlinkenFrameConverter> fft_to_BlinkenFrame = new LinkedList<FFT_to_BlinkenFrameConverter>();
	
	public BlinkenSignalProcessor( int bands )
	{
		this.saBands = bands;
	}

	@Override
	public void initialize( int pSampleSize, SourceDataLine pSourceDataLine )
	{
		setSpectrumAnalyserSampleSizeAndRate(pSampleSize, pSourceDataLine.getFormat().getSampleRate());

		oldVolume = new float[pSourceDataLine.getFormat().getChannels()];
	}

	@Override
	public void process( Context pDssContext )
	{
		float[] mergedChannels = channelMerge(pDssContext.getDataNormalized());
		draw(mergedChannels, pDssContext.getFrameRatioHint());

	}

	public void addFFT_to_BlinkenFrameConverter( FFT_to_BlinkenFrameConverter fft_to_BlinkenFrameConverter )
	{
		this.fft_to_BlinkenFrame.add( fft_to_BlinkenFrameConverter );
	}
	
	private void draw( float[] sample, float pFrrh )
	{
		// make something with this data...
		updateBlinkenFrame( old_FFT );
		
		float c = 16; // lc = 16 - saBandWidth;
		float wSadfrr = (saDecay * pFrrh);

		int b, bd, i, li = 0, mi;
		float fs, m;

		int wBm = 1;

		// -- FFT processing.
		float[] wFFT = fft.calculate(sample);

		// -- Group up available bands using band distribution table.
		for (bd = 0; bd < saBands; bd++)
		{

			// -- Get band distribution entry.
			i = sabdTable[bd].distribution;

			m = 0;
			mi = 0;

			// -- Find loudest band in group. (Group is from 'li' to 'i')
			for (b = li; b < i; b++)
			{

				float lf = wFFT[b];

				if (lf > m)
				{
					m = lf;
					mi = b;
				}

			}

			li = i;

			// -- Calculate gain using log, then static gain.
			fs = (m * sabgTable[mi]) * saGain;

			// -- Limit over-saturation.
			if (fs > 1.0f)
			{
				fs = 1.0f;
			}

			// -- Compute decay.
			if (fs >= (old_FFT[bd] - wSadfrr))
			{

				old_FFT[bd] = fs;

			}
			else
			{

				old_FFT[bd] -= wSadfrr;

				if (old_FFT[bd] < 0)
				{
					old_FFT[bd] = 0;
				}

				fs = old_FFT[bd];

			}

			c += saBandWidth;

		}
		
	}

	private void updateBlinkenFrame( float[] bands )
	{
		// ...e.g. visualize!
		for( FFT_to_BlinkenFrameConverter c : this.fft_to_BlinkenFrame )
		{
			try
			{
				c.updateBlinkenFrame( bands );
			}
			catch (Exception e) 
			{
				logger.error( "!!! Error updating BlinkenFrame in Class '" + c.getClass().getName() + "': " + e.getMessage() );
			}
		}
	}

	/**
	 * Merges two audio channels into one.
	 * 
	 * @param pLeft
	 *            Left channel data.
	 * @param pRight
	 *            Right channel data.
	 * 
	 * @return Merged results of the supplied left and right channel data.
	 */
	protected float[] channelMerge( float[][] pChannels )
	{

		for (int a = 0; a < pChannels[0].length; a++)
		{

			float wMcd = 0;

			for (int b = 0; b < pChannels.length; b++)
			{
				wMcd += pChannels[b][a];
			}

			pChannels[0][a] = wMcd / (float) pChannels.length;

		}

		return pChannels[0];

	}

	/**
	 * Sets the numbers of bands rendered by the spectrum analyser.
	 * 
	 * @param pCount
	 *            Cannot be more than half the "FFT sample size".
	 */
	public synchronized void setSpectrumAnalyserBandCount( int pCount )
	{

		saBands = pCount;

		computeBandTables();
	}

	/**
	 * Sets the FFT sample size and rate to be just for calculating the spectrum
	 * analyser values.
	 * 
	 * @param pSize
	 *            Cannot be more than the size of the sample provided by the
	 *            DSP.
	 */
	protected synchronized void setSpectrumAnalyserSampleSizeAndRate( int pSize, float pRate )
	{

		saFFTSampleSize = pSize;
		saFFTSampleRate = pRate;

		fft = new KJFFT(saFFTSampleSize);
		old_FFT = new float[saBands];

		computeBandTables();
	}

	/**
	 * Computes and stores a band distribution and gain tables for the spectrum
	 * analyzer. This is performed using the current band distribution and gain
	 * instances. See setSpectrumAnalyzerBandDistribution() or
	 * setSpectrumAnalyserBandGain() methods.
	 */
	protected void computeBandTables( )
	{

		if (saBands > 0 && saFFTSampleSize > 0 & fft != null)
		{

			// -- Create band table.
			sabdTable = saBandDistribution.create(saBands, fft, saFFTSampleRate);
			saBands = sabdTable.length;

			updateSpectrumAnalyserBandWidth();

			// -- Resolve band descriptions.
			resolveBandDescriptions(sabdTable);

			// -- Create gain table.
			sabgTable = saBandGain.create(fft, saFFTSampleRate);

			// printTable( sabdTable );
		}

	}

	protected void updateSpectrumAnalyserBandWidth( )
	{
		saBandWidth = (float) (width - 32) / (float) saBands;
	}

	protected void resolveBandDescriptions( Band[] pBandTable )
	{

		DecimalFormat wDf = new DecimalFormat("###.#");

		for (Band wBand : pBandTable)
		{

			if (wBand.frequency >= 1000.0f)
			{
				wBand.description = wDf.format(wBand.frequency / 1000.0f) + "k";
			}
			else
			{
				wBand.description = wDf.format(wBand.frequency);
			}

		}
 
	}
	
}
