import javax.sound.sampled.*;
import java.io.*;
import java.util.Vector;

/**
 * The <code>JavaSound</code> class is an Implementation of the <a href =
 * "http://java.sun.com/products/java-media/sound/index.html"> Java Sound API
 * </a> specifically designed for use with the Jython Environment for Students
 * (JES).
 * <p>
 * This class allows for easy playback, and manipulation of AU, AIFF, and WAV
 * files.
 * <p>
 * Note that EVERYTHING in this file is 0-indexed. (Since we want JES to use a
 * 1-based indexing system for sounds, the media.py file contains wrappers for
 * all of these functions that translate between the 0 and 1 based systems).
 * Except error messages, they are 1-indexed so that JES can catch them and make
 * sense of it all.
 * 
 * @author Ellie Harmon, ellie@cc.gatech.edu
 */

/*
 * Lots of code & ideas for this class related to playing and viewing the sound
 * were borrowed from the Java Sound Demo:
 * http://java.sun.com/products/java-media/sound/samples/JavaSoundDemo/
 * 
 * Also, some code borrowed from Tritonus as noted.
 */

/*
 * Copyright (c) 1999 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * Sun grants you ("Licensee") a non-exclusive, royalty free, license to use,
 * modify and redistribute this software in source and binary code form,
 * provided that i) this copyright notice and license appear on all copies of
 * the software; and ii) Licensee does not utilize the software in a manner
 * which is disparaging to Sun.
 * 
 * This software is provided "AS IS," without a warranty of any kind. ALL
 * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING ANY
 * IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
 * NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN AND ITS LICENSORS SHALL NOT BE
 * LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING
 * OR DISTRIBUTING THE SOFTWARE OR ITS DERIVATIVES. IN NO EVENT WILL SUN OR ITS
 * LICENSORS BE LIABLE FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT,
 * INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER
 * CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF
 * OR INABILITY TO USE SOFTWARE, EVEN IF SUN HAS BEEN ADVISED OF THE POSSIBILITY
 * OF SUCH DAMAGES.
 * 
 * This software is not designed or intended for use in on-line control of
 * aircraft, air traffic, aircraft navigation or aircraft communications; or in
 * the design, construction, operation or maintenance of any nuclear facility.
 * Licensee represents and warrants that it will not use or redistribute the
 * Software for such purposes.
 */

public class JavaSound
{

	/**************************************************************************/
	/********************************** FIELDS ********************************/
	/**************************************************************************/

	/**
	 * A default buffer size.
	 * 
	 * @see JavaSound#JavaSound()
	 * @see Playback#run()
	 */
	private static final int BUFFER_SIZE = 16384;

	/**
	 * Are we in debug mode?
	 */
	private static final boolean DEBUG = false;

	/**
	 * An array of bytes representing the sound.
	 */
	private byte[] buffer;

	/**
	 * Contains information about this sound such as its length, format, and
	 * type.
	 * 
	 * @see AudioFormat
	 */
	private AudioFileFormat audioFileFormat;

	/**
	 * A collection of the threads that are playing this sound.
	 */
	private Vector playbacks;

	/**
	 * The view for this sound, if it exists. If it exists, this becomes the
	 * LineListener for output lines in the Playback class.
	 * 
	 * @see Playback
	 */
	private SoundView soundView;

	/**
	 * The name of the file from which this sound was created. Gets updated
	 * every time load from file is called.
	 * 
	 * @see #loadFromFile
	 */
	private String fileName;

	/**************************************************************************/
	/****************************** ACCESSORS *********************************/
	/**************************************************************************/

	/**
	 * Obtains the byte array representation of this sound.
	 * 
	 * @return the sound represented as a byte array
	 */
	public byte[] getBuffer( )
	{
		return buffer;
	}

	/**
	 * Obtains the AudioFileFormat describing this sound.
	 * 
	 * @return the AudioFileFormat describing this sound
	 * @see AudioFileFormat
	 */
	public AudioFileFormat getAudioFileFormat( )
	{
		return audioFileFormat;
	}

	/**
	 * @see JavaSound#getBuffer
	 */
	public byte[] asArray( )
	{
		return getBuffer();
	}

	/**
	 * Obtains the collection of playback threads currently active on this
	 * sound.
	 */
	public Vector getPlaybacks( )
	{
		return playbacks;
	}

	/**
	 * Obtains the name of the file this sound came from. If this sound did not
	 * orriginate with a file, this value will be null.
	 * 
	 * @see #loadFromFile(String fileName)
	 */
	public String getFileName( )
	{
		return fileName;
	}

	/**************************************************************************/
	/******************************** MODIFIERS ********************************/
	/**************************************************************************/

	/**
	 * Changes the byte array that represents this sound.
	 * 
	 * @param newBuffer
	 *            a byte array representation of the new sound we want this to
	 *            represent.
	 */
	public void setBuffer( byte[] newBuffer )
	{
		buffer = newBuffer;
	}

	/**
	 * Changes the AudioFileFormat of this sound.
	 * 
	 * @param newAudioFileFormat
	 *            the new audioFileFormat that describes this sound.
	 * @see AudioFileFormat
	 */
	public void setAudioFileFormat( AudioFileFormat newAudioFileFormat )
	{
		audioFileFormat = newAudioFileFormat;
	}

	/**
	 * Changes the view of this object.
	 * 
	 * @see #soundView
	 */
	public void setSoundView( SoundView soundView )
	{
		this.soundView = soundView;
	}

	/**************************************************************************/
	/******************************** CONSTRUCTORS ****************************/
	/**************************************************************************/

	/**
	 * Constructs a <code>JavaSound</code> of 3 seconds long.
	 * 
	 * @see JavaSound#JavaSound(int numSeconds)
	 */
	public JavaSound()
	{
		this(3);
	}

	/**
	 * Constructs a <code>JavaSound</code> of the specified length. This sound
	 * will simply consist of an empty byte array, and an
	 * <code>AudioFileFormat</code> with the following values:
	 * <ul>
	 * <li><code>AudioFileFormat.Type.WAVE</code>
	 * <li>22.05K sampling rate
	 * <li>16 bit sample
	 * <li>1 channel
	 * <li>signed PCM encoding
	 * <li>small-endian byte order
	 * </ul>
	 * Note that no new sound file is created, we only represent the sound with
	 * a buffer and the AudioFileFormat. If a file is desired, then the method
	 * <code>writeToFile(String filename)</code> must be called on this newly
	 * created sound.
	 * 
	 * @param numSeconds
	 *            The length, in seconds, for the new sound
	 * @see JavaSound#writeToFile(String filename)
	 */
	public JavaSound(int numSeconds)
	{
		/*
		 * Make a new sound with 22.05K sampling, 16 bits, 1 channel(==1
		 * sample/frame), signed, smallEndian
		 */
		AudioFormat audioFormat = new AudioFormat(22050, 16, 1, true, false);

		/*
		 * (samples/sec)/channel * channels * sec * bytes/sample = bytes
		 */
		int lengthInBytes = 22050 * 1 * numSeconds * 2;

		/*
		 * Make a new WAV file format, with the AudioFormat described above
		 * lengthInFrames = lengthInBytes/frameSizeInBytes
		 * 
		 * note : frame size is number of bytes required to contain one sample
		 * from each channel: in this case: 2 bytes/sample * 1 sample/frame = 2
		 * bytes/frame
		 */
		audioFileFormat = new AudioFileFormat(AudioFileFormat.Type.WAVE, audioFormat, lengthInBytes / (2));

		buffer = new byte[lengthInBytes];
		playbacks = new Vector();
	}

	public JavaSound(int sampleSizeInBits, boolean isBigEndian)
	{
		AudioFormat audioFormat = new AudioFormat(22050, sampleSizeInBits, 2, true, isBigEndian);

		int lengthInBytes = 22050 * 2 * 5 * (sampleSizeInBits / 8);

		audioFileFormat = new AudioFileFormat(AudioFileFormat.Type.WAVE, audioFormat, lengthInBytes / (sampleSizeInBits / 8 * 2));

		buffer = new byte[lengthInBytes];
		playbacks = new Vector();
	}

	/**
	 * Constructs a new JavaSound from the given file.
	 * 
	 * @param fileName
	 *            The File from which to create this sound.
	 * @see JavaSound#loadFromFile(String filename)
	 * @throws JavaSoundException
	 *             if there are problems loading the file
	 */
	public JavaSound(String fileName) throws JavaSoundException
	{
		loadFromFile(fileName);
		playbacks = new Vector();
	}

	/**************************************************************************/
	/*********************************** MISC **********************************/
	/**************************************************************************/

	/**
	 * Creates an <code>AudioInputStream</code> for this sound from the
	 * <code>buffer</code> and the <code>audioFileFormat</code>.
	 * 
	 * @return an AudioInputStream representing this sound.
	 * @see AudioInputStream
	 */
	public AudioInputStream makeAIS( )
	{
		AudioFileFormat.Type fileType = audioFileFormat.getType();
		ByteArrayInputStream bais = new ByteArrayInputStream(buffer);
		int frameSize = audioFileFormat.getFormat().getFrameSize();

		AudioInputStream audioInputStream = new AudioInputStream(bais, audioFileFormat.getFormat(), buffer.length / frameSize);
		return audioInputStream;
	}// makeAIS

	/**
	 * Invokes <code>printError(message, null)</code>
	 * 
	 * @see JavaSound#printError(String message, Exception e)
	 * @throws JavaSoundException
	 *             Will throw under every circumstance. This way we can catch
	 *             the exception in JES.
	 */
	public void printError( String message ) throws JavaSoundException
	{
		printError(message, null);
	}

	/**
	 * Prints the given String to the "standard" error output stream, then
	 * prints a stack trace on the exception, and then exits the program. If the
	 * String is null, then nothing happens, the method just returns. If the
	 * Exception is null, then it prints the String and then exits the program.
	 * 
	 * @param message
	 *            A description of the error
	 * @param e
	 *            The exception, if any, that was caught regarding the error
	 * @throws JavaSoundException
	 *             Will throw under every circumstance. This way we can catch
	 *             the exception in JES.
	 */
	public void printError( String message, Exception e ) throws JavaSoundException
	{
		if (message != null)
		{
			System.err.println(message);
			if (e != null)
			{
				e.printStackTrace();
			}
			// so we can catch the error in JES
			throw (new JavaSoundException(message));
		}
	}

	public boolean isStereo( )
	{
		if (audioFileFormat.getFormat().getChannels() == 1)
			return false;
		else
			return true;
	}

	/**************************************************************************/
	/**************************** FILE INPUT/OUTPUT ***************************/
	/**************************************************************************/

	/**
	 * Creates an audioInputStream from this sound, and then writes this stream
	 * out to the file with the specified name. If no file exists, one is
	 * created. If a file already exists, then it is overwritten. Does not check
	 * the extension of the fileName passed in to make sure it agrees with the
	 * <code>AudioFileFormat.Type</code> of this sound.
	 * 
	 * @param outFileName
	 *            The name of the file to write this sound to
	 * @throws JavaSoundException
	 *             if any error is encountered while writing to the file.
	 */
	public void writeToFile( String outFileName ) throws JavaSoundException
	{

		/*
		 * get an audioInputStream that represents this sound. then, we will
		 * write from the stream to the file
		 */
		AudioInputStream audioInputStream = makeAIS();
		AudioFileFormat.Type type = audioFileFormat.getType();

		try
		{
			audioInputStream.reset();
		}// try reset audioInputStream
		catch (Exception e)
		{
			printError("Unable to reset the Audio stream.  Please " + "try again.", e);
		}// catch

		// get the file to write to
		File file = new File(outFileName);
		if (!file.exists())
		{
			// if the file doesn't exist, make one
			try
			{
				file.createNewFile();
			}// try
			catch (IOException e)
			{
				printError("That file does not already exist, and" + "there were problems creating a new file" + "of that name.  Are you sure the path" + "to: " + outFileName + "exists?", e);
			}// catch
		}// if

		// write to the file
		try
		{
			if (AudioSystem.write(audioInputStream, type, file) == -1)
			{
				printError("Problems writing to file.  Please " + "try again.");
			}
		}// try
		catch (FileNotFoundException e)
		{
			printError("The file you specified did not already exist " + "so we tried to create a new one, but were unable" + "to do so.  Please try again.  If problems persist" + "see your TA.", e);
		}
		catch (Exception e)
		{
			printError("Problems writing to file: " + outFileName, e);
		}// catch

		// cloe the input stream, we're done writing
		try
		{
			audioInputStream.close();
		}// try
		catch (Exception e)
		{
			printError("Unable to close the Audio stream.");
		}// catch

	}// writeToFile(String outFileName)

	/**
	 * Resets the fields of this sound so that it now represents the sound in
	 * the specified file. If successful, the fileName variable is updated such
	 * that it is equivalent to <code>inFileName</code>.
	 * 
	 * @param inFileName
	 *            the path and filename of the sound we want to represent.
	 * @throws JavaSoundException
	 *             if any problem is encountered while reading in from the file.
	 */
	public void loadFromFile( String inFileName ) throws JavaSoundException
	{

		// try to catch the NullPointerException before it occurs
		if (inFileName == null)
		{
			printError("You must pass in a valid file name.  Please try" + "again.");
		}

		/*
		 * get the File object representing the file named inFileName and make
		 * sure it exists
		 */
		File file = new File(inFileName);
		if (!file.exists())
		{
			printError("The file: " + inFileName + " doesn't exist");
		}

		/*
		 * create an audioInputStream from this file
		 */
		AudioInputStream audioInputStream;
		try
		{
			audioInputStream = AudioSystem.getAudioInputStream(file);
		}
		catch (Exception e)
		{
			printError("Unable to create Audio Stream from file " + inFileName + ".  The file type is unsupported.  " + "Are you sure you're using a WAV, AU, or" + "AIFF file?", e);
			return;
		}// catch

		/*
		 * We need to make an array representing this sound, so the number of
		 * bytes we will be storing cannot be greater than Integer.MAX_VALUE The
		 * JavaSound API also supports only integer length frame lengths. (See
		 * AudioFileFormat.getFrameLength(). I don't know why this is
		 * inconsistent with AudioInputStream.getFrameLength().)
		 */
		if ((audioInputStream.getFrameLength() * audioInputStream.getFormat().getFrameSize()) > Integer.MAX_VALUE)
		{
			printError("The sound in file: " + inFileName + " is too long." + "  Try using a shorter sound.");
		}
		int bufferSize = (int) audioInputStream.getFrameLength() * audioInputStream.getFormat().getFrameSize();

		buffer = new byte[bufferSize];

		int numBytesRead = 0;
		int offset = 0;

		// read all the bytes into the buffer
		while (true)
		{
			try
			{
				numBytesRead = audioInputStream.read(buffer, offset, bufferSize);
				if (numBytesRead == -1)// no more data
					break;
				else
					offset += numBytesRead;
			}// try
			catch (Exception e)
			{
				printError("Problems reading the input stream.  " + "You might want to try again using this " + " file: " + inFileName + "or a different" + " file.  If problems persist, ask your TA.", e);
			}// catch
		}// while

		/*
		 * set the format of the file, assuming that the extension is correct
		 */
		if (inFileName.toLowerCase().endsWith(".wav"))
		{
			audioFileFormat = new AudioFileFormat(AudioFileFormat.Type.WAVE, audioInputStream.getFormat(), (int) audioInputStream.getFrameLength());
		}
		else if (inFileName.toLowerCase().endsWith(".au"))
		{
			audioFileFormat = new AudioFileFormat(AudioFileFormat.Type.AU, audioInputStream.getFormat(), (int) audioInputStream.getFrameLength());
		}
		else if (inFileName.toLowerCase().endsWith(".aif") || inFileName.toLowerCase().endsWith(".aiff"))
		{
			audioFileFormat = new AudioFileFormat(AudioFileFormat.Type.AIFF, audioInputStream.getFormat(), (int) audioInputStream.getFrameLength());
		}
		else
		{
			printError("Unsupported file type.  Please try again with a " + "file that ends in .wav, .au, .aif, or .aiff");
		}

		if (DEBUG)
		{
			System.out.println("New sound created from file: " + fileName);
			System.out.println("\tendianness: " + audioInputStream.getFormat().isBigEndian());
			System.out.println("\tencoding: " + audioInputStream.getFormat().getEncoding());
		}

		this.fileName = file.getName();

	}// loadFromFile(String inFileName)

	/**************************************************************************/
	/******************************** PLAYING *********************************/
	/**************************************************************************/

	/**
	 * Creates a new Playback thread and starts it. The thread is guarranteed to
	 * finish playing the sound as long as the program doesn't exit before it is
	 * done. This method does not block, however. So, if you invoke
	 * <code>play()</code> multiple times in a row, sounds will simply play on
	 * top of eachother - "accidental mixing"
	 * 
	 * @see Playback
	 */
	public void play( )
	{
		/*
		 * create the thread, add it to the Vector, and start it
		 */
		Playback playback = new Playback();
		playbacks.add(playback);
		playback.start();
	}

	/**
	 * Creates a new Playback thread, starts it, then waits for the entire sound
	 * to finish playing before it returns. This method is guarranteed to play
	 * the entire sound, and does not allow for any "accidental mixing"
	 * 
	 * @see Playback
	 */
	public void blockingPlay( )
	{
		/*
		 * create the thread, add it to the Vector, start it, and wait until its
		 * done playing to return
		 */
		Playback playback = new Playback();
		playbacks.add(playback);
		playback.start();
		while (playback.isAlive())
		{
			;
		}// wait until the sound is done playing
	}

	/**
	 * Calls <code>playAtRateInRange((float)rate, 0, (int)durInFrames-1, false)
     * </code>. Checks the value of durInFrames to make sure that it is
	 * not larger than Integer.MAX_VALUE to guarrantee safe casting. Also checks
	 * the value of rate to make sure that it is not larger than Float.MAX_VALUE
	 * before casting.
	 * 
	 * @param rate
	 *            a double representing the change in sampleRate (==frameRate)
	 *            for playing back this sound
	 * @param durInFrames
	 *            a double representing how much of this sound we want to play.
	 * @see JavaSound#playAtRateInRange(float rate, int startFrame, int
	 *      endFrame, boolean isBlocking)
	 * @throws JavaSoundException
	 *             if there are problems playing the sound.
	 */
	public void playAtRateDur( double rate, double durInFrames ) throws JavaSoundException
	{
		if (durInFrames > getLengthInFrames())
		{
			printError("The given duration in frames, " + durInFrames + " is out of the playable range.  Try something " + "between 1 and " + getLengthInFrames());
		}
		if (rate > Float.MAX_VALUE)
		{
			printError("The new sample rate, " + rate + "is out of the " + "playable range.  Try something between " + "0 and " + Float.MAX_VALUE);
		}
		playAtRateInRange((float) rate, 0, (int) durInFrames - 1, false);
	}

	/**
	 * Calls <code>playAtRateInRange((float)rate, 0, (int)durInFrames-1, true)
     * </code>. First, checks the value of durInFrames to make sure that
	 * it is not larger than Integer.MAX_VALUE to guarrantee safe casting.
	 * Simmilarly, checks the value of rate to make sure that it is not larger
	 * than FLoat.MAX_VALUE before casting.
	 * 
	 * @param rate
	 *            a double representing the change in sampleRate (==frameRate)
	 *            for playing back this sound
	 * @param durInFrames
	 *            a double representing how much of this sound we want to play
	 * @see JavaSound#playAtRateInRange(float range, int startFrame, int
	 *      endFrame, boolean isBlocking)
	 * @throws JavaSoundException
	 *             if there are problems playing the sound.
	 */
	public void blockingPlayAtRateDur( double rate, double durInFrames ) throws JavaSoundException
	{
		if (durInFrames > getLengthInFrames())
		{
			printError("The given duration in frames, " + durInFrames + " is out of the playable range.  Try something " + "between 1 and " + getLengthInFrames());
		}
		if (rate > Float.MAX_VALUE)
		{
			printError("The new sample rate, " + rate + "is out of the " + "playable range.  Try something between " + "0 and " + Float.MAX_VALUE);
		}

		playAtRateInRange((float) rate, 0, (int) durInFrames - 1, true);

	}

	/**
	 * Calls <code>playAtRateInRange(rate, startFrame, endFrame, false)
     * </code>.
	 * 
	 * @param rate
	 *            a float representing the change in sampleRate (==frameRate)
	 *            for playing back this sound
	 * @param startFrame
	 *            an int representing the frame at which we want to begin
	 *            playing the sound
	 * @param endFrame
	 *            an int representing the frame at which want to stop playing
	 *            the sound
	 * @see JavaSound#playAtRateInRange(float range, int startFrame, int
	 *      endFrame, boolean isBlocking)
	 * @throws JavaSoundException
	 *             if there are problems playing the sound.
	 */
	public void playAtRateInRange( float rate, int startFrame, int endFrame ) throws JavaSoundException
	{
		playAtRateInRange(rate, startFrame, endFrame, false);
	}

	/**
	 * Calls <code>playAtRateInRange(rate, startFrame, endFrame, true)
     * </code>.
	 * 
	 * @param rate
	 *            a float representing the change in sampleRate (==frameRate)
	 *            for playing back this sound
	 * @param startFrame
	 *            an int representing the frame at which we want to begin
	 *            playing the sound
	 * @param endFrame
	 *            an int representing the frame at which want to stop playing
	 *            the sound
	 * @see JavaSound#playAtRateInRange(float range, int startFrame, int
	 *      endFrame, boolean isBlocking)
	 * @throws JavaSoundException
	 *             if there are problems playing the sound.
	 */
	public void blockingPlayAtRateInRange( float rate, int startFrame, int endFrame ) throws JavaSoundException
	{
		playAtRateInRange(rate, startFrame, endFrame, true);
	}

	/**
	 * Plays the specified segment of this sound at the given sample rate. Then
	 * it saves the old fields (buffer and audioFileFormat) of this sound into
	 * temporary variables, and setting the fields of this sound to modified
	 * values. Then it creates a Playback thread on this sound (with the
	 * modified values) and starts the thread. The values for buffer and
	 * audioFileFormat are restored to their original values before the method
	 * returns.
	 * 
	 * @param rate
	 *            The change in the sampleRate (==frameRate) for playing back
	 *            this sound. The old SampleRate is multiplied by this value.
	 *            So, if rate = 2, the sound will play twice as fast (half the
	 *            length), and if rate = .5, the sound will play half as fast
	 *            (twice the length).
	 * @param startFrame
	 *            The index of the frame where we want to begin play
	 * @param endFrame
	 *            The index of the frame where we want to end play
	 * @param isBlocking
	 *            If true, this method waits until the thread is done playing
	 *            the sound before returning. If false, it simply starts the
	 *            thread and then returns.
	 * @throws JavaSoundException
	 *             if there are any problems playing the sound.
	 */
	public void playAtRateInRange( float rate, int startFrame, int endFrame, boolean isBlocking ) throws JavaSoundException
	{

		// before we get started, lets try to check for some obvious errors.
		// maybe we can avoid some of those pesky array out of bounds
		// exceptions.
		if (endFrame >= getAudioFileFormat().getFrameLength())
		{
			printError("You are trying to play to index: " + (endFrame + 1) + ".  The sound only has " + getAudioFileFormat().getFrameLength() + " samples total.");
		}
		if (startFrame < 0)
		{
			printError("You cannot start playing at index " + (startFrame + 1) + ".  Choose 1 to start at the begining.");
		}
		if (endFrame < startFrame)
		{
			printError("You cannot start playing at index " + (startFrame + 1) + " and stop playing at index " + (endFrame + 1) + ".  The start index must be before" + "the stop index.");
		}

		/*
		 * we want to save the current buffer and audioFileFormat so we can
		 * return to them when we're finished.
		 */
		byte[] oldBuffer = getBuffer();
		AudioFileFormat oldAFF = getAudioFileFormat();

		// just to make the code easier to read
		int frameSize = getAudioFileFormat().getFormat().getFrameSize();
		int durInFrames = (endFrame - startFrame) + 1;
		if (DEBUG)
			System.out.println("\tnew durInFrames = " + durInFrames);

		// we want to make a new buffer, only as long as we need
		int newBufferSize = durInFrames * frameSize;

		byte[] newBuffer = new byte[newBufferSize];
		for (int i = 0; i < newBufferSize; i++)
		{
			newBuffer[i] = oldBuffer[(startFrame * frameSize) + i];
		}

		// now we want to make a new audioFormat with the same information
		// except a different rate
		AudioFormat newAF = new AudioFormat(oldAFF.getFormat().getEncoding(), oldAFF.getFormat().getSampleRate() * rate, oldAFF.getFormat().getSampleSizeInBits(), oldAFF.getFormat().getChannels(), oldAFF.getFormat().getFrameSize(), oldAFF.getFormat().getFrameRate() * rate, oldAFF.getFormat()
				.isBigEndian());

		// now put that new AudioFormat into a new AudioFileFormat with
		// the changed duration in frames
		AudioFileFormat newAFF = new AudioFileFormat(oldAFF.getType(), newAF, durInFrames);

		/*
		 * change the values in this Sound
		 */
		setBuffer(newBuffer);
		setAudioFileFormat(newAFF);
		if (DEBUG)
		{
			System.out.println("playAtRateInRange(" + rate + ", " + startFrame + ", " + endFrame + ", " + isBlocking + ")");
			System.out.println("\t(length of sound = " + getAudioFileFormat().getFrameLength() + ")");
		}

		/*
		 * play the modified sound
		 */
		Playback playback = new Playback();
		playbacks.add(playback);
		playback.start();

		if (isBlocking)
			while (playback.isAlive())
			{
				;
			}// wait until the thread exits

		/*
		 * we need to wait until the thread is done with the values before we
		 * change them back. The playing flag is set to false until the loop
		 * begins in which data is actually written out. see Playback#run()
		 */
		while (!playback.getPlaying())
		{
			;
		}

		setBuffer(oldBuffer);// restore the buffer
		setAudioFileFormat(oldAFF);// restore the file format
	}

	/**
	 * Deletes the specified playback object from the Vector. This should only
	 * be called from within the run() method of an individual playback thread.
	 * 
	 * @see Playback#run()
	 */
	private void removePlayback( Playback playbackToRemove )
	{
		if (playbacks.contains(playbackToRemove))
		{
			playbacks.remove(playbackToRemove);
			playbackToRemove = null;
		}
	}

	/**
	 * The nested class <code>Playback</code> extends from <code>Thread</code>
	 * and allows for playback of this sound. The thread doesn't die until the
	 * sound is finished playing, however it is not blocking either. It will
	 * simply play the sound in the "background."
	 */
	public class Playback extends Thread
	{
		SourceDataLine line;
		boolean playing = false;

		private void shutDown( String message, Exception e )
		{
			if (message != null)
			{
				System.err.println(message);
				e.printStackTrace();
			}
			playing = false;
		}

		/**
		 * Stops this thread by breaking the while loop in the run method. Used,
		 * for example, by the "stop" button in the SoundView class.
		 */
		public void stopPlaying( )
		{
			playing = false;
		}

		public boolean getPlaying( )
		{
			return playing;
		}

		/**
		 * Starts this thread. Gets an AudioInputStream, and writes is out to a
		 * SourceDataLine. If a SoundView exists, upon creation of the
		 * SourceDataLine, the soundView is added as the LineListener. When the
		 * thread finishes the run method, it removes itself from the list of
		 * threads currently playing this sound.
		 * 
		 * @throws JavaSoundException
		 *             if there were problems playing the sound.
		 */
		public void run( )
		{

			// get something to play
			AudioInputStream audioInputStream = makeAIS();
			if (audioInputStream == null)
			{
				shutDown("There is no input stream to play", null);
				return;
			}

			// reset stream to the begining
			try
			{
				audioInputStream.reset();
			}
			catch (Exception e)
			{
				shutDown("Problems resetting the stream\n", e);
				return;
			}

			/*
			 * define the required attributes for the line make sure a
			 * compatible line is supported
			 */
			DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFileFormat.getFormat());
			if (!AudioSystem.isLineSupported(info))
			{
				shutDown("Line matching " + info + "not supported.", null);
				return;
			}

			// get and open the source data line for playback
			try
			{
				line = (SourceDataLine) AudioSystem.getLine(info);
				if (soundView != null)
					line.addLineListener(soundView);
				line.open(audioFileFormat.getFormat(), BUFFER_SIZE);
			}
			catch (LineUnavailableException e)
			{
				shutDown("Unable to open the line: ", e);
				return;
			}

			// play back the captured data
			int frameSizeInBytes = audioFileFormat.getFormat().getFrameSize();
			int bufferLengthInBytes = line.getBufferSize();
			int bufferLengthInFrames = bufferLengthInBytes / frameSizeInBytes;
			byte[] data = new byte[bufferLengthInBytes];
			int numBytesRead = 0;

			// start the source data line and begin playing
			line.start();
			playing = true;

			/*
			 * the loop that actually writes the data out
			 */
			while (playing)
			{
				try
				{
					if ((numBytesRead = audioInputStream.read(data)) == -1)
					{
						break;// end of audioInputStream
					}
					int numBytesRemaining = numBytesRead;
					while (numBytesRemaining > 0)
					{
						numBytesRemaining -= line.write(data, 0, numBytesRemaining);
					}// while
				}// try
				catch (Exception e)
				{
					shutDown("Error during playback: ", e);
					break;
				}// catch
			}// while

			/*
			 * we reached the end of the stream or an error occurred. if we were
			 * playing, then let the data play out, else, skip to stopping and
			 * closing the line.
			 */
			if (playing)
				line.drain();

			line.stop();
			line.close();
			line = null;
			shutDown(null, null);
			if (DEBUG)
				System.out.println("exiting run method");
			/*
			 * this thread is about to die. remove itself from the collection of
			 * threads playing this sound
			 */
			removePlayback(this);

		}// run()

	}// end class Playback

	/**************************************************************************/
	/********************** ACESSING SOUND INFORMATION ************************/
	/**************************************************************************/

	/**
	 * Returns an array containing all of the bytes in the specified frame.
	 * 
	 * @param frameNum
	 *            the index of the frame to access
	 * @return the array containing all of the bytes in frame
	 *         <code>frameNum</code>
	 * @throws JavaSoundException
	 *             if the frame number is invalid.
	 */
	public byte[] getFrame( int frameNum ) throws JavaSoundException
	{
		if (frameNum >= getAudioFileFormat().getFrameLength())
		{
			printError("That index " + (frameNum + 1) + ", does not exist. " + "There are only " + getAudioFileFormat().getFrameLength() + " frames in the entire sound");
		}

		int frameSize = getAudioFileFormat().getFormat().getFrameSize();
		byte[] theFrame = new byte[frameSize];
		for (int i = 0; i < frameSize; i++)
		{
			theFrame[i] = getBuffer()[frameNum * frameSize + i];
		}
		return theFrame;
	}

	/**
	 * Obtains the length of the audio data contained in the file, expressed in
	 * sample frames.
	 * 
	 * @return the number of sample frames of audio data in the file
	 */
	public int getLengthInFrames( )
	{
		return getAudioFileFormat().getFrameLength();
	}

	/**
	 * If this is a mono sound, obtains the single sample contained within this
	 * frame, else obtains the first (left) sample contained in the specified
	 * frame.
	 * 
	 * @param frameNum
	 *            the index of the frame to access
	 * @return an integer representation of the bytes contained within the
	 *         specified frame
	 * @throws JavaSoundException
	 *             if the frame number is invalid.
	 */
	public int getSample( int frameNum ) throws JavaSoundException
	{
		// Before we get started, lets make sure that frame exists
		if (frameNum >= getAudioFileFormat().getFrameLength())
		{
			printError("You are trying to access the sample at index: " + (frameNum + 1) + ", but there are only " + getAudioFileFormat().getFrameLength() + " samples in the file!");
		}
		if (frameNum < 0)
		{
			printError("You asked for the sample at index: " + (frameNum + 1) + ".  This number is less than one.  Please try" + "again using an index in the range [1," + getAudioFileFormat().getFrameLength() + "]");
		}

		AudioFormat format = getAudioFileFormat().getFormat();
		int sampleSizeInBits = format.getSampleSizeInBits();
		boolean isBigEndian = format.isBigEndian();

		byte[] theFrame = getFrame(frameNum);

		if (format.getEncoding().equals(AudioFormat.Encoding.PCM_SIGNED))
		{
			// since we're always returning the left sample,
			// we don't care if we're mono or stereo, left is
			// always first in the frame
			if (sampleSizeInBits == 8)// 8 bits == 1 byte
				return theFrame[0];
			else if (sampleSizeInBits == 16)
				return bytesToInt16(theFrame, 0, isBigEndian);
			else if (sampleSizeInBits == 24)
				return bytesToInt24(theFrame, 0, isBigEndian);
			else if (sampleSizeInBits == 32)
				return bytesToInt32(theFrame, 0, isBigEndian);
			else
			{
				printError("Unsupported audio encoding.  The sample " + "size is not recognized as a standard " + "format.");
				return -1;
			}
		}
		else if (format.getEncoding().equals(AudioFormat.Encoding.PCM_UNSIGNED))
		{
			if (sampleSizeInBits == 8)
				return unsignedByteToInt(theFrame[0]) - (int) Math.pow(2, 7);
			else if (sampleSizeInBits == 16)
				return unsignedByteToInt16(theFrame, 0, isBigEndian) - (int) Math.pow(2, 15);
			else if (sampleSizeInBits == 24)
				return unsignedByteToInt24(theFrame, 0, isBigEndian) - (int) Math.pow(2, 23);
			else if (sampleSizeInBits == 32)
				return unsignedByteToInt32(theFrame, 0, isBigEndian) - (int) Math.pow(2, 31);
			else
			{
				printError("Unsupported audio encoding.  The sample " + "size is not recognized as a standard " + "format.");
				return -1;
			}
		}
		else if (format.getEncoding().equals(AudioFormat.Encoding.ALAW))
		{
			return alaw2linear(buffer[0]);
		}
		else if (format.getEncoding().equals(AudioFormat.Encoding.ULAW))
		{
			return ulaw2linear(buffer[0]);
		}
		else
		{
			printError("unsupported audio encoding: " + format.getEncoding() + ".  Currently only PCM, " + "ALAW and ULAW are supported.  Please try again" + "with a different file.");
			return -1;
		}
	}// getSample(int)

	/**
	 * Obtains the left sample of the audio data contained at the specified
	 * frame.
	 * 
	 * @param frameNum
	 *            the index of the frame to access
	 * @return an int representation of the bytes contained in the specified
	 *         frame.
	 * @throws JavaSoundException
	 *             if the frameNumber is invalid
	 */
	public int getLeftSample( int frameNum ) throws JavaSoundException
	{
		// default is to getLeftSample

		return getSample(frameNum);

	}

	/**
	 * Obtains the right sample of the audio data contained at the specified
	 * frame.
	 * 
	 * @param frameNum
	 *            the index of the frame to access
	 * @return an int representation of the bytes contained in the specified
	 *         frame.
	 * @throws JavaSoundException
	 *             if the frameNumber is invalid, or the encoding isn't
	 *             supported.
	 */
	public int getRightSample( int frameNum ) throws JavaSoundException
	{
		// Before we get started, lets make sure that frame exists
		if (frameNum >= getAudioFileFormat().getFrameLength())
		{
			printError("You are trying to access the sample at index: " + (frameNum + 1) + ", but there are only " + getAudioFileFormat().getFrameLength() + " samples in the file!");
		}
		if (frameNum < 0)
		{
			printError("You asked for the sample at index: " + (frameNum + 1) + ".  This number is less than one.  Please try" + " again using an index in the range [1," + getAudioFileFormat().getFrameLength() + "].");
		}

		AudioFormat format = getAudioFileFormat().getFormat();
		int channels;
		if ((channels = format.getChannels()) == 1)
		{
			printError("Only stereo sounds have different right and left" + " samples.  You are using a mono sound, try " + "getSample(" + (frameNum + 1) + ") instead");
			return -1;
		}
		int sampleSizeInBits = format.getSampleSizeInBits();
		boolean isBigEndian = format.isBigEndian();

		byte[] theFrame = getFrame(frameNum);

		if (format.getEncoding().equals(AudioFormat.Encoding.PCM_SIGNED))
		{
			if (sampleSizeInBits == 8)// 8 bits == 1 byte
				return theFrame[1];
			else if (sampleSizeInBits == 16)
				return bytesToInt16(theFrame, 2, isBigEndian);
			else if (sampleSizeInBits == 24)
				return bytesToInt24(theFrame, 3, isBigEndian);
			else if (sampleSizeInBits == 32)
				return bytesToInt32(theFrame, 4, isBigEndian);
			else
			{
				printError("Unsupported audio encoding.  The sample" + " size is not recognized as a standard" + " format.");
				return -1;
			}
		}
		else if (format.getEncoding().equals(AudioFormat.Encoding.PCM_UNSIGNED))
		{
			if (sampleSizeInBits == 8)
				return unsignedByteToInt(theFrame[1]);
			else if (sampleSizeInBits == 16)
				return unsignedByteToInt16(theFrame, 2, isBigEndian);
			else if (sampleSizeInBits == 24)
				return unsignedByteToInt24(theFrame, 3, isBigEndian);
			else if (sampleSizeInBits == 32)
				return unsignedByteToInt32(theFrame, 4, isBigEndian);
			else
			{
				printError("Unsupported audio encoding.  The sample" + " size is not recognized as a standard" + " format.");
				return -1;
			}
		}
		else if (format.getEncoding().equals(AudioFormat.Encoding.ALAW))
		{
			return alaw2linear(buffer[1]);
		}
		else if (format.getEncoding().equals(AudioFormat.Encoding.ULAW))
		{
			return ulaw2linear(buffer[1]);
		}
		else
		{
			printError("unsupported audio encoding: " + format.getEncoding() + ".  Currently only PCM, " + "ALAW and ULAW are supported.  Please try again" + "with a different file.");
			return -1;
		}
	}

	/**
	 * Obtains the length of this sound in bytes. Note, that this number is not
	 * neccessarily the same as the length of this sound's file in bytes.
	 * 
	 * @return the sound length in bytes
	 */
	public int getLength( )
	{
		return buffer.length;
	}

	/**
	 * Obtains the number of channels of this sound.
	 * 
	 * @return the number of channels (1 for mono, 2 for stereo), or
	 *         <code>AudioSystem.NOT_SPECIFIED</code>
	 * @see AudioSystem#NOT_SPECIFIED
	 */
	public int getChannels( )
	{
		return getAudioFileFormat().getFormat().getChannels();
	}

	/**************************************************************************/
	/************************** CHANGING THE SOUND ****************************/
	/**************************************************************************/

	/**
	 * Changes the value of each byte of the specified frame.
	 * 
	 * @param frameNum
	 *            the index of the frame to change
	 * @param theFrame
	 *            the byte array that will be copied into this sound's buffer in
	 *            place of the specified frame.
	 *@throws JavaSoundException
	 *             if the frameNumber is invalid.
	 */
	public void setFrame( int frameNum, byte[] theFrame ) throws JavaSoundException
	{
		if (frameNum > getAudioFileFormat().getFrameLength())
		{
			printError("That frame, number " + frameNum + ", does not exist. " + "There are only " + getAudioFileFormat().getFrameLength() + " frames in the entire sound");
		}
		int frameSize = getAudioFileFormat().getFormat().getFrameSize();
		if (frameSize != theFrame.length)
			printError("Frame size doesn't match, line 383.  This should" + " never happen.  Please report the problem to a TA.");
		for (int i = 0; i < frameSize; i++)
		{
			buffer[frameNum * frameSize + i] = theFrame[i];
		}
	}

	/**
	 * Changes the value of the sample found at the specified frame. If this
	 * sound has more than one channel, then this defaults to setting only the
	 * first (left) sample.
	 * 
	 * @param frameNum
	 *            the index of the frame where the sample should be changed
	 * @param sample
	 *            an int representation of the new sample to put in this sound's
	 *            buffer at the specified frame
	 * @throws JavaSoundException
	 *             if the frameNumber is invalid, or another problem is
	 *             encountered
	 */
	public void setSample( int frameNum, int sample ) throws JavaSoundException
	{
		AudioFormat format = getAudioFileFormat().getFormat();
		int sampleSizeInBits = format.getSampleSizeInBits();
		boolean isBigEndian = format.isBigEndian();

		byte[] theFrame = getFrame(frameNum);

		if (format.getEncoding().equals(AudioFormat.Encoding.PCM_SIGNED))
		{
			if (sampleSizeInBits == 8)// 8 bits = 1 byte = first cell in array
			{
				theFrame[0] = (byte) sample;
				setFrame(frameNum, theFrame);
			}
			else if (sampleSizeInBits == 16)// 2 bytes, first 2 cells in array
			{
				intToBytes16(sample, theFrame, 0, isBigEndian);
				setFrame(frameNum, theFrame);
			}
			else if (sampleSizeInBits == 24)
			{
				intToBytes24(sample, theFrame, 0, isBigEndian);
				setFrame(frameNum, theFrame);
			}
			else if (sampleSizeInBits == 32)
			{
				intToBytes32(sample, theFrame, 0, isBigEndian);
				setFrame(frameNum, theFrame);
			}
			else
			{
				printError("Unsupported audio encoding.  The sample" + "size is not recognized as a standard format");
			}
		}// if format == PCM_SIGNED
		else if (format.getEncoding().equals(AudioFormat.Encoding.PCM_UNSIGNED))
		{
			if (sampleSizeInBits == 8)
			{
				theFrame[0] = intToUnsignedByte(sample);
				setFrame(frameNum, theFrame);
			}
			else if (sampleSizeInBits == 16)
			{
				intToUnsignedBytes16(sample, theFrame, 0, isBigEndian);
				setFrame(frameNum, theFrame);
			}
			else if (sampleSizeInBits == 24)
			{
				intToUnsignedBytes24(sample, theFrame, 0, isBigEndian);
				setFrame(frameNum, theFrame);
			}
			else if (sampleSizeInBits == 32)
			{
				intToUnsignedBytes32(sample, theFrame, 0, isBigEndian);
				setFrame(frameNum, theFrame);
			}

			else
			{
				printError("Unsupported audio encoding.  The sample" + " size is not recognized as a standard " + "format.");
			}
		}
		else if (format.getEncoding().equals(AudioFormat.Encoding.ALAW))
		{
			if ((sample > Short.MAX_VALUE) || (sample < Short.MIN_VALUE))
				printError("You are trying to set the sample value to: " + sample + ", but the maximum value for a sample" + " in this format is: " + Short.MAX_VALUE + ", and the minimum value is: " + Short.MIN_VALUE + ".  Please choose a value in that range.");
			theFrame[0] = linear2alaw((short) sample);
			setFrame(frameNum, theFrame);
		}
		else if (format.getEncoding().equals(AudioFormat.Encoding.ULAW))
		{

			if ((sample > Short.MAX_VALUE) || (sample < Short.MIN_VALUE))
				printError("You are trying to set the sample value to: " + sample + ", but the maximum value for a sample" + " in this format is: " + Short.MAX_VALUE + ", and the minimum value is: " + Short.MIN_VALUE + ".  Please choose a value in that range.");
			theFrame[0] = linear2ulaw((short) sample);
			setFrame(frameNum, theFrame);
		}
		else
		{
			printError("unsupported audio encoding: " + format.getEncoding() + ".  Currently only PCM, " + "ALAW and ULAW are supported.  Please try again" + "with a different file.");
		}
	}// setSample(int, int)

	public void setLeftSample( int frameNum, int sample ) throws JavaSoundException
	{
		setSample(frameNum, sample);
	}

	public void setRightSample( int frameNum, int sample ) throws JavaSoundException
	{
		AudioFormat format = getAudioFileFormat().getFormat();
		int sampleSizeInBits = format.getSampleSizeInBits();
		boolean isBigEndian = format.isBigEndian();

		if (format.getChannels() == 1)
			printError("this is a mono sound.  only stereo sounds have" + " different left and right samples.");

		byte[] theFrame = getFrame(frameNum);

		if (format.getEncoding().equals(AudioFormat.Encoding.PCM_SIGNED))
		{
			// right will always be the second in the frame
			if (sampleSizeInBits == 8)
			{
				theFrame[1] = (byte) sample;
				setFrame(frameNum, theFrame);
			}
			else if (sampleSizeInBits == 16)
			{
				intToBytes16(sample, theFrame, 2, isBigEndian);
				setFrame(frameNum, theFrame);
			}
			else if (sampleSizeInBits == 24)
			{
				intToBytes24(sample, theFrame, 3, isBigEndian);
				setFrame(frameNum, theFrame);
			}
			else if (sampleSizeInBits == 32)
			{
				intToBytes32(sample, theFrame, 4, isBigEndian);
				setFrame(frameNum, theFrame);
			}
			else
			{
				printError("Unsupported audio encoding.  The sample" + "size is not recognized as a standard format");
			}
		}// if format == PCM_SIGNED
		else if (format.getEncoding().equals(AudioFormat.Encoding.PCM_UNSIGNED))
		{
			if (sampleSizeInBits == 8)
			{
				theFrame[1] = intToUnsignedByte(sample);
				setFrame(frameNum, theFrame);
			}
			else if (sampleSizeInBits == 16)
			{
				intToUnsignedBytes16(sample, theFrame, 2, isBigEndian);
				setFrame(frameNum, theFrame);
			}
			else if (sampleSizeInBits == 24)
			{
				intToUnsignedBytes24(sample, theFrame, 3, isBigEndian);
				setFrame(frameNum, theFrame);
			}
			else if (sampleSizeInBits == 32)
			{
				intToUnsignedBytes32(sample, theFrame, 4, isBigEndian);
				setFrame(frameNum, theFrame);
			}
			else
			{
				printError("Unsupported audio encoding.  The sample" + " size is not recognized as a standard" + " format");
			}
		}
		else if (format.getEncoding().equals(AudioFormat.Encoding.ALAW))
		{
			if ((sample > Short.MAX_VALUE) || (sample < Short.MIN_VALUE))
				printError("You are trying to set the sample value to: " + sample + ", but the maximum value for a sample" + " in this format is: " + Short.MAX_VALUE + ", and the minimum value is: " + Short.MIN_VALUE + ".  Please choose a value in that range.");
			theFrame[1] = linear2alaw((short) sample);
			setFrame(frameNum, theFrame);
		}
		else if (format.getEncoding().equals(AudioFormat.Encoding.ULAW))
		{
			if ((sample > Short.MAX_VALUE) || (sample < Short.MIN_VALUE))
				printError("You are trying to set the sample value to: " + sample + ", but the maximum value for a sample" + " in this format is: " + Short.MAX_VALUE + ", and the minimum value is: " + Short.MIN_VALUE + ".  Please choose a value in that range.");
			theFrame[1] = linear2ulaw((short) sample);
			setFrame(frameNum, theFrame);
		}
		else
		{
			printError("unsupported audio encoding: " + format.getEncoding() + ".  Currently only PCM, " + "ALAW and ULAW are supported.  Please try again" + "with a different file.");
		}
	}// setRightSample(int, int)

	/**************************************************************************/
	/****************************** MISC FUNCTIONS ****************************/
	/**************************************************************************/

	/*
	 * conversion tools from tritonus (http://www.tritonus.org)
	 */

	/*
	 * TConversionTool.java
	 */

	/*
	 * Copyright (c) 1999,2000 by Florian Bomers <florian@bome.com> Copyright
	 * (c) 2000 by Matthias Pfisterer <matthias.pfisterer@gmx.de>
	 * 
	 * 
	 * This program is free software; you can redistribute it and/or modify it
	 * under the terms of the GNU Library General Public License as published by
	 * the Free Software Foundation; either version 2 of the License, or (at
	 * your option) any later version.
	 * 
	 * This program is distributed in the hope that it will be useful, but
	 * WITHOUT ANY WARRANTY; without even the implied warranty of
	 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Library
	 * General Public License for more details.
	 * 
	 * You should have received a copy of the GNU Library General Public License
	 * along with this program; if not, write to the Free Software Foundation,
	 * Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
	 */

	/**
	 * Converts 2 successive bytes starting at <code>byteOffset</code> in
	 * <code>buffer</code> to a signed integer sample with 16bit range.
	 * <p>
	 * For little endian, buffer[byteOffset] is interpreted as low byte, whereas
	 * it is interpreted as high byte in big endian.
	 * <p>
	 * This is a reference function.
	 */
	private static int bytesToInt16( byte[] buffer, int byteOffset, boolean bigEndian )
	{
		return bigEndian ? ((buffer[byteOffset] << 8) | (buffer[byteOffset + 1] & 0xFF)) :

		((buffer[byteOffset + 1] << 8) | (buffer[byteOffset] & 0xFF));
	}

	/**
	 * Converts 3 successive bytes starting at <code>byteOffset</code> in
	 * <code>buffer</code> to a signed integer sample with 24bit range.
	 * <p>
	 * For little endian, buffer[byteOffset] is interpreted as lowest byte,
	 * whereas it is interpreted as highest byte in big endian.
	 * <p>
	 * This is a reference function.
	 */
	private static int bytesToInt24( byte[] buffer, int byteOffset, boolean bigEndian )
	{
		return bigEndian ? ((buffer[byteOffset] << 16) // let Java handle
													   // sign-bit
				| ((buffer[byteOffset + 1] & 0xFF) << 8) // inhibit sign-bit
														 // handling
		| ((buffer[byteOffset + 2] & 0xFF))) :

		((buffer[byteOffset + 2] << 16) // let Java handle sign-bit
				| ((buffer[byteOffset + 1] & 0xFF) << 8) // inhibit sign-bit
														 // handling
		| (buffer[byteOffset] & 0xFF));
	}

	/**
	 * Converts a 4 successive bytes starting at <code>byteOffset</code> in
	 * <code>buffer</code> to a signed 32bit integer sample.
	 * <p>
	 * For little endian, buffer[byteOffset] is interpreted as lowest byte,
	 * whereas it is interpreted as highest byte in big endian.
	 * <p>
	 * This is a reference function.
	 */
	private static int bytesToInt32( byte[] buffer, int byteOffset, boolean bigEndian )
	{
		return bigEndian ? ((buffer[byteOffset] << 24) // let Java handle
													   // sign-bit
				| ((buffer[byteOffset + 1] & 0xFF) << 16) // inhibit sign-bit
														  // handling
				| ((buffer[byteOffset + 2] & 0xFF) << 8) // inhibit sign-bit
														 // handling
		| (buffer[byteOffset + 3] & 0xFF)) :

		((buffer[byteOffset + 3] << 24) // let Java handle sign-bit
				| ((buffer[byteOffset + 2] & 0xFF) << 16) // inhibit sign-bit
														  // handling
				| ((buffer[byteOffset + 1] & 0xFF) << 8) // inhibit sign-bit
														 // handling
		| (buffer[byteOffset] & 0xFF));
	}

	// ///////////////////// ULAW ///////////////////////////////////////////

	private static final boolean ZEROTRAP = true;
	private static final short BIAS = 0x84;
	private static final int CLIP = 32635;
	private static final int exp_lut1[] =
	{ 0, 0, 1, 1, 2, 2, 2, 2, 3, 3, 3, 3, 3, 3, 3, 3, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6,
			6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7,
			7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7 };

	/* u-law to linear conversion table */
	private static short[] u2l =
	{ -32124, -31100, -30076, -29052, -28028, -27004, -25980, -24956, -23932, -22908, -21884, -20860, -19836, -18812, -17788, -16764, -15996, -15484, -14972, -14460, -13948, -13436, -12924, -12412, -11900, -11388, -10876, -10364, -9852, -9340, -8828, -8316, -7932, -7676, -7420, -7164, -6908, -6652,
			-6396, -6140, -5884, -5628, -5372, -5116, -4860, -4604, -4348, -4092, -3900, -3772, -3644, -3516, -3388, -3260, -3132, -3004, -2876, -2748, -2620, -2492, -2364, -2236, -2108, -1980, -1884, -1820, -1756, -1692, -1628, -1564, -1500, -1436, -1372, -1308, -1244, -1180, -1116, -1052, -988,
			-924, -876, -844, -812, -780, -748, -716, -684, -652, -620, -588, -556, -524, -492, -460, -428, -396, -372, -356, -340, -324, -308, -292, -276, -260, -244, -228, -212, -196, -180, -164, -148, -132, -120, -112, -104, -96, -88, -80, -72, -64, -56, -48, -40, -32, -24, -16, -8, 0, 32124,
			31100, 30076, 29052, 28028, 27004, 25980, 24956, 23932, 22908, 21884, 20860, 19836, 18812, 17788, 16764, 15996, 15484, 14972, 14460, 13948, 13436, 12924, 12412, 11900, 11388, 10876, 10364, 9852, 9340, 8828, 8316, 7932, 7676, 7420, 7164, 6908, 6652, 6396, 6140, 5884, 5628, 5372, 5116,
			4860, 4604, 4348, 4092, 3900, 3772, 3644, 3516, 3388, 3260, 3132, 3004, 2876, 2748, 2620, 2492, 2364, 2236, 2108, 1980, 1884, 1820, 1756, 1692, 1628, 1564, 1500, 1436, 1372, 1308, 1244, 1180, 1116, 1052, 988, 924, 876, 844, 812, 780, 748, 716, 684, 652, 620, 588, 556, 524, 492, 460,
			428, 396, 372, 356, 340, 324, 308, 292, 276, 260, 244, 228, 212, 196, 180, 164, 148, 132, 120, 112, 104, 96, 88, 80, 72, 64, 56, 48, 40, 32, 24, 16, 8, 0 };

	private static short ulaw2linear( byte ulawbyte )
	{
		return u2l[ulawbyte & 0xFF];
	}

	/**
	 * Converts a linear signed 16bit sample to a uLaw byte. Ported to Java by
	 * fb. <BR>
	 * Originally by:<BR>
	 * Craig Reese: IDA/Supercomputing Research Center <BR>
	 * Joe Campbell: Department of Defense <BR>
	 * 29 September 1989 <BR>
	 */
	public static byte linear2ulaw( int sample )
	{
		int sign, exponent, mantissa, ulawbyte;

		if (sample > 32767)
			sample = 32767;
		else if (sample < -32768)
			sample = -32768;
		/* Get the sample into sign-magnitude. */
		sign = (sample >> 8) & 0x80; /* set aside the sign */
		if (sign != 0)
			sample = -sample; /* get magnitude */
		if (sample > CLIP)
			sample = CLIP; /* clip the magnitude */

		/* Convert from 16 bit linear to ulaw. */
		sample = sample + BIAS;
		exponent = exp_lut1[(sample >> 7) & 0xFF];
		mantissa = (sample >> (exponent + 3)) & 0x0F;
		ulawbyte = ~(sign | (exponent << 4) | mantissa);
		if (ZEROTRAP)
			if (ulawbyte == 0)
				ulawbyte = 0x02; /* optional CCITT trap */
		return ((byte) ulawbyte);
	}

	/*
	 * This source code is a product of Sun Microsystems, Inc. and is provided
	 * for unrestricted use. Users may copy or modify this source code without
	 * charge.
	 * 
	 * linear2alaw() - Convert a 16-bit linear PCM value to 8-bit A-law
	 * 
	 * linear2alaw() accepts an 16-bit integer and encodes it as A-law data.
	 * 
	 * Linear Input Code Compressed Code ------------------------
	 * --------------- 0000000wxyza 000wxyz 0000001wxyza 001wxyz 000001wxyzab
	 * 010wxyz 00001wxyzabc 011wxyz 0001wxyzabcd 100wxyz 001wxyzabcde 101wxyz
	 * 01wxyzabcdef 110wxyz 1wxyzabcdefg 111wxyz
	 * 
	 * For further information see John C. Bellamy's Digital Telephony, 1982,
	 * John Wiley & Sons, pps 98-111 and 472-476.
	 */
	private static final byte QUANT_MASK = 0xf; /* Quantization field mask. */
	private static final byte SEG_SHIFT = 4; /* Left shift for segment number. */
	private static final short[] seg_end =
	{ 0xFF, 0x1FF, 0x3FF, 0x7FF, 0xFFF, 0x1FFF, 0x3FFF, 0x7FFF };

	/*
	 * conversion table alaw to linear
	 */
	private static short[] a2l =
	{ -5504, -5248, -6016, -5760, -4480, -4224, -4992, -4736, -7552, -7296, -8064, -7808, -6528, -6272, -7040, -6784, -2752, -2624, -3008, -2880, -2240, -2112, -2496, -2368, -3776, -3648, -4032, -3904, -3264, -3136, -3520, -3392, -22016, -20992, -24064, -23040, -17920, -16896, -19968, -18944,
			-30208, -29184, -32256, -31232, -26112, -25088, -28160, -27136, -11008, -10496, -12032, -11520, -8960, -8448, -9984, -9472, -15104, -14592, -16128, -15616, -13056, -12544, -14080, -13568, -344, -328, -376, -360, -280, -264, -312, -296, -472, -456, -504, -488, -408, -392, -440, -424,
			-88, -72, -120, -104, -24, -8, -56, -40, -216, -200, -248, -232, -152, -136, -184, -168, -1376, -1312, -1504, -1440, -1120, -1056, -1248, -1184, -1888, -1824, -2016, -1952, -1632, -1568, -1760, -1696, -688, -656, -752, -720, -560, -528, -624, -592, -944, -912, -1008, -976, -816, -784,
			-880, -848, 5504, 5248, 6016, 5760, 4480, 4224, 4992, 4736, 7552, 7296, 8064, 7808, 6528, 6272, 7040, 6784, 2752, 2624, 3008, 2880, 2240, 2112, 2496, 2368, 3776, 3648, 4032, 3904, 3264, 3136, 3520, 3392, 22016, 20992, 24064, 23040, 17920, 16896, 19968, 18944, 30208, 29184, 32256, 31232,
			26112, 25088, 28160, 27136, 11008, 10496, 12032, 11520, 8960, 8448, 9984, 9472, 15104, 14592, 16128, 15616, 13056, 12544, 14080, 13568, 344, 328, 376, 360, 280, 264, 312, 296, 472, 456, 504, 488, 408, 392, 440, 424, 88, 72, 120, 104, 24, 8, 56, 40, 216, 200, 248, 232, 152, 136, 184,
			168, 1376, 1312, 1504, 1440, 1120, 1056, 1248, 1184, 1888, 1824, 2016, 1952, 1632, 1568, 1760, 1696, 688, 656, 752, 720, 560, 528, 624, 592, 944, 912, 1008, 976, 816, 784, 880, 848 };

	private static short alaw2linear( byte ulawbyte )
	{
		return a2l[ulawbyte & 0xFF];
	}

	public static byte linear2alaw( short pcm_val )
	/* 2's complement (16-bit range) */
	{
		byte mask;
		byte seg = 8;
		byte aval;

		if (pcm_val >= 0)
		{
			mask = (byte) 0xD5; /* sign (7th) bit = 1 */
		}
		else
		{
			mask = 0x55; /* sign bit = 0 */
			pcm_val = (short) (-pcm_val - 8);
		}

		/* Convert the scaled magnitude to segment number. */
		for (int i = 0; i < 8; i++)
		{
			if (pcm_val <= seg_end[i])
			{
				seg = (byte) i;
				break;
			}
		}

		/* Combine the sign, segment, and quantization bits. */
		if (seg >= 8) /* out of range, return maximum value. */
			return (byte) ((0x7F ^ mask) & 0xFF);
		else
		{
			aval = (byte) (seg << SEG_SHIFT);
			if (seg < 2)
				aval |= (pcm_val >> 4) & QUANT_MASK;
			else
				aval |= (pcm_val >> (seg + 3)) & QUANT_MASK;
			return (byte) ((aval ^ mask) & 0xFF);
		}
	}

	/**
	 * Converts a 16 bit sample of type <code>int</code> to 2 bytes in an array.
	 * <code>sample</code> is interpreted as signed (as Java does).
	 * <p>
	 * For little endian, buffer[byteOffset] is filled with low byte of sample,
	 * and buffer[byteOffset+1] is filled with high byte of sample + sign bit.
	 * <p>
	 * For big endian, this is reversed.
	 * <p>
	 * Before calling this function, it should be assured that
	 * <code>sample</code> is in the 16bit range - it will not be clipped.
	 * <p>
	 * This is a reference function.
	 */
	private static void intToBytes16( int sample, byte[] buffer, int byteOffset, boolean bigEndian )
	{
		if (bigEndian)
		{
			buffer[byteOffset++] = (byte) (sample >> 8);
			buffer[byteOffset] = (byte) (sample & 0xFF);
		}
		else
		{
			buffer[byteOffset++] = (byte) (sample & 0xFF);
			buffer[byteOffset] = (byte) (sample >> 8);
		}
	}

	/**
	 * Converts a 24 bit sample of type <code>int</code> to 3 bytes in an array.
	 * <code>sample</code> is interpreted as signed (as Java does).
	 * <p>
	 * For little endian, buffer[byteOffset] is filled with low byte of sample,
	 * and buffer[byteOffset+2] is filled with the high byte of sample + sign
	 * bit.
	 * <p>
	 * For big endian, this is reversed.
	 * <p>
	 * Before calling this function, it should be assured that
	 * <code>sample</code> is in the 24bit range - it will not be clipped.
	 * <p>
	 * This is a reference function.
	 */
	private static void intToBytes24( int sample, byte[] buffer, int byteOffset, boolean bigEndian )
	{
		if (bigEndian)
		{
			buffer[byteOffset++] = (byte) (sample >> 16);
			buffer[byteOffset++] = (byte) ((sample >>> 8) & 0xFF);
			buffer[byteOffset] = (byte) (sample & 0xFF);
		}
		else
		{
			buffer[byteOffset++] = (byte) (sample & 0xFF);
			buffer[byteOffset++] = (byte) ((sample >>> 8) & 0xFF);
			buffer[byteOffset] = (byte) (sample >> 16);
		}
	}

	/**
	 * Converts a 32 bit sample of type <code>int</code> to 4 bytes in an array.
	 * <code>sample</code> is interpreted as signed (as Java does).
	 * <p>
	 * For little endian, buffer[byteOffset] is filled with lowest byte of
	 * sample, and buffer[byteOffset+3] is filled with the high byte of sample +
	 * sign bit.
	 * <p>
	 * For big endian, this is reversed.
	 * <p>
	 * This is a reference function.
	 */
	private static void intToBytes32( int sample, byte[] buffer, int byteOffset, boolean bigEndian )
	{
		if (bigEndian)
		{
			buffer[byteOffset++] = (byte) (sample >> 24);
			buffer[byteOffset++] = (byte) ((sample >>> 16) & 0xFF);
			buffer[byteOffset++] = (byte) ((sample >>> 8) & 0xFF);
			buffer[byteOffset] = (byte) (sample & 0xFF);
		}
		else
		{
			buffer[byteOffset++] = (byte) (sample & 0xFF);
			buffer[byteOffset++] = (byte) ((sample >>> 8) & 0xFF);
			buffer[byteOffset++] = (byte) ((sample >>> 16) & 0xFF);
			buffer[byteOffset] = (byte) (sample >> 24);
		}
	}

	/*
	 * Byte<->Int conversions for unsigned pcm data were written by myself with
	 * help from Real's Java How-To:
	 * http://www.rgagnon.com/javadetails/java-0026.html
	 */

	private static int unsignedByteToInt( byte b )
	{
		/*
		 * & 0xFF while seemingly doing nothing to the individual bits, forces
		 * java to recognize the byte as unsigned. so, we return to the calling
		 * function a number between 0 and 256.
		 */
		return ((int) b & 0xFF);
	}

	private static int unsignedByteToInt16( byte[] buffer, int offset, boolean isBigEndian )
	{
		/*
		 * here, we want to take the first byte and shift it left 8 bits then
		 * concatenate on the 8 bits in the second byte. now we have a 16 bit
		 * number that java will recognize as unsigned, so we return a number in
		 * the range [0, 65536]
		 */

		if (isBigEndian)
		{
			return ((unsignedByteToInt(buffer[offset]) << 8) | unsignedByteToInt(buffer[offset + 1]));
		}
		else
		{
			return ((unsignedByteToInt(buffer[offset + 1]) << 8) | unsignedByteToInt(buffer[offset]));
		}

	}

	public static int unsignedByteToInt24( byte[] buffer, int offset, boolean isBigEndian )
	{
		if (isBigEndian)
		{
			return ((unsignedByteToInt(buffer[offset]) << 16) | (unsignedByteToInt(buffer[offset + 1]) << 8) | unsignedByteToInt(buffer[offset + 2]));
		}
		else
		{
			return ((unsignedByteToInt(buffer[offset + 2]) << 16) | (unsignedByteToInt(buffer[offset + 1]) << 8) | unsignedByteToInt(buffer[offset]));
		}
	}

	public static int unsignedByteToInt32( byte[] buffer, int offset, boolean isBigEndian )
	{
		if (isBigEndian)
		{
			return ((unsignedByteToInt(buffer[offset]) << 24) | (unsignedByteToInt(buffer[offset + 1]) << 16) | (unsignedByteToInt(buffer[offset + 2]) << 8) | unsignedByteToInt(buffer[offset + 3]));
		}
		else
		{
			return ((unsignedByteToInt(buffer[offset + 3]) << 24) | (unsignedByteToInt(buffer[offset + 2]) << 16) | (unsignedByteToInt(buffer[offset + 1]) << 8) | unsignedByteToInt(buffer[offset]));
		}
	}

	public static byte intToUnsignedByte( int sample )
	{
		/*
		 * does the reverse of the function above we have an integer that is
		 * signed, so we're in the range [-128, 127], we want to convert to an
		 * unsigned number in the range [0,256], then put that into an unsigned
		 * byte all while java tries to treat everythign as signed.
		 * 
		 * so.... say we want to set the sample value to -128 in our unsigned
		 * byte, this translates to 0, so we want java's representation of -128:
		 * 10000000 to instead be stored as 0: 00000000 so, we simply xor with
		 * -128, flipping the sign bit
		 * 
		 * another example we want to store the max value 127: 01111111
		 * translating into the unsigned range, the max is 256: 11111111 again,
		 * you can see all we need to change is the sign bit.
		 * 
		 * and lastly, for something in the middle: say we want to store the
		 * value 0: 00000000 translating into the unsigned range, we have the
		 * middle value 128: 10000000 again, we just want to flip the first bit
		 * 
		 * something a little more tricky... say we want to store the value 32
		 * now this translates to 32--128 = 160 in unsigned representation so we
		 * start with 32 = 00100000 and we want to go to 160 = 10100000
		 * 
		 * see, we just flip the sign bit, its the same as adding 128 which is
		 * how we translate between [-128,127] and [0,256].
		 */
		return ((byte) (sample ^ -128));
	}

	public static void intToUnsignedBytes16( int sample, byte[] buffer, int byteOffset, boolean bigEndian )
	{

		/*
		 * for this comment only, treat ^ not as XOR as we use it in java but as
		 * an exponent symbol like on a calculator, i thought 2^15 would be
		 * clearer than 32768. the theory here is very simmilar to the 8 bit
		 * conversion we did above. only now we have 16 bits we want to write
		 * into. so, we're going from the range [-2^15, 2^15-1] into the range
		 * [0, 2^16]. again, to translate, we just need to add 2^15 to our
		 * number, so we get the first byte, by shifting right 8 bits, (note:
		 * >>> is unsigned shift), and then XOR with -128 to flip the sign bit.
		 * for the second byte, we just want the last 8 bits of our integer, so
		 * we & with 0xff to tell java to treat this as unsigned, and just copy
		 * over the bit values.
		 */
		if (bigEndian)
		{
			buffer[byteOffset] = (byte) (sample >>> 8 ^ -128);
			buffer[byteOffset + 1] = (byte) (sample & 0xff);
		}
		else
		{
			buffer[byteOffset + 1] = (byte) (sample >>> 8 ^ -128);
			buffer[byteOffset] = (byte) (sample & 0xff);
		}
	}

	public static void intToUnsignedBytes24( int sample, byte[] buffer, int byteOffset, boolean bigEndian )
	{
		if (bigEndian)
		{
			buffer[byteOffset] = (byte) (sample >>> 16 ^ -128);
			buffer[byteOffset + 1] = (byte) (sample >>> 8);
			buffer[byteOffset + 2] = (byte) (sample & 0xff);
		}
		else
		{
			buffer[byteOffset + 2] = (byte) (sample >>> 16 ^ -128);
			buffer[byteOffset + 1] = (byte) (sample >>> 8);
			buffer[byteOffset] = (byte) (sample & 0xff);
		}
	}

	public static void intToUnsignedBytes32( int sample, byte[] buffer, int byteOffset, boolean bigEndian )
	{
		if (bigEndian)
		{
			buffer[byteOffset] = (byte) (sample >>> 24 ^ -128);
			buffer[byteOffset + 1] = (byte) (sample >>> 16);
			buffer[byteOffset + 2] = (byte) (sample >>> 8);
			buffer[byteOffset + 3] = (byte) (sample & 0xff);
		}
		else
		{
			buffer[byteOffset + 3] = (byte) (sample >>> 24 ^ -128);
			buffer[byteOffset + 2] = (byte) (sample >>> 16);
			buffer[byteOffset + 1] = (byte) (sample >>> 8);
			buffer[byteOffset] = (byte) (sample & 0xff);
		}
	}

	/**************************************************************************/
	/********************* SOME STRINGS ABOUT THE SOUND ***********************/
	/**************************************************************************/

	/**
	 * Obtains a string representation of this JavaSound.
	 * 
	 * @return a String representation of this JavaSound.
	 */
	public String toString( )
	{
		return getAudioFileFormat().getFormat().toString();
	}

	public String justATaste( )
	{
		return "Sorry, justATaste is not implemented at this time.";
	}

	public String justABufferTaste( byte[] b )
	{
		return "Sorry, justABufferTaste is not implemented at this time.";
	}

	public static void main( String args[] )
	{

		try
		{

			/*
			 * 16 bit, stereo, big endian (pretty short)
			 * 
			 * 
			 * JavaSound mysound1 = newJavaSound(
			 * "/Users/ellie/mediacomp/ellie/JavaSoundDemo/audio/22-new.aif");
			 * System.out.println("new file format: " + mysound1);
			 */

			/*
			 * 16 bit, mono, little endian (also pretty short)
			 * 
			 * JavaSound mysound2 = newJavaSound(
			 * "/Users/ellie/mediacomp/ellie/JavaSoundDemo/audio/1-welcome.wav"
			 * ); System.out.println("new file format: " + mysound2);
			 */

			/*
			 * writing to a file
			 * 
			 * 
			 * mysound2.writeToFile("/Users/ellie/Desktop/mysound2.wav");
			 */

			/*
			 * testing compatibility with old file, getFrame, getSample, etc.
			 * now we're 0-indexed... media.py will be 1-indexed WON'T COMPILE
			 * WITHOUT OldJavaSound.class and BackgroundCloser.class
			 * 
			 * OldJavaSound oldSound = new OldJavaSound();
			 * oldSound.loadFromFile(
			 * "/Users/ellie/Desktop/ellie/JavaSoundDemo/audio/1-welcome.wav");
			 * System.out.println("old file format: " + oldSound.audioFormat);
			 * 
			 * System.out.println("\nnew file frame 0: " +
			 * mysound2.getFrame(0)); System.out.println("\tnew file sample 0: "
			 * + mysound2.getSample(0)); System.out.println("old file frame 1: "
			 * + oldSound.getFrame(1));
			 * System.out.println("\told file sample 1: " +
			 * oldSound.getSample(1));
			 * 
			 * System.out.println("\new file frame 1: " + mysound2.getFrame(1));
			 * System.out.println("\tnew file sample 1: " +
			 * mysound2.getSample(1)); System.out.println("old file frame 2: " +
			 * oldSound.getFrame(2)); System.out.println("\told file sample 2: "
			 * + oldSound.getSample(2));
			 * 
			 * System.out.println("\nnew file frame 2: " +
			 * mysound2.getFrame(2));
			 * System.out.println("\tnew file sample 2: "+
			 * mysound2.getSample(2)); System.out.println("old file frame 3: " +
			 * oldSound.getFrame(3));
			 * System.out.println("\told file sample 3: "+
			 * oldSound.getSample(3));
			 */
			/*
			 * System.out.println("\nnew file set sample 2: 14");
			 * mysound2.setSample(2, 14);
			 * System.out.println("\tchecking value: " + mysound2.getSample(2));
			 */

			/*
			 * general blocking play, stereo
			 * 
			 * System.out.println("\nblocking play:  stereo");
			 * mysound1.blockingPlay();
			 */

			/*
			 * testing playAtRateDur
			 * 
			 * System.out.println("\nblocking - double the rate");
			 * mysound1.blockingPlayAtRateDur (2,
			 * mysound1.getAudioFileFormat().getFrameLength());
			 * 
			 * System.out.println("\nblocking - back to the original sound");
			 * mysound1.blockingPlay();
			 * 
			 * System.out.println("\nblocking - half the duration");
			 * mysound1.blockingPlayAtRateDur (1,
			 * mysound1.getAudioFileFormat().getFrameLength()/2);
			 * 
			 * System.out.println("\nnon-blocking - back to original sound");
			 * mysound1.play();
			 * 
			 * System.out.println("\nblocking - half the rate");
			 * mysound1.blockingPlayAtRateDur (.5,
			 * mysound1.getAudioFileFormat().getFrameLength());
			 * 
			 * System.out.println("\nblocking - only the middle ");
			 * mysound1.blockingPlayAtRateInRange(1, 35811, 71623);
			 */

			/*
			 * test for a really long sound ~2.5 minutes
			 * 
			 * System.out.println("\ncreating a new sound:  big yellow taxi");
			 * JavaSound longSound = new
			 * JavaSound("/Users/ellie/Desktop/ellie/Big Yellow Taxi.wav");
			 * 
			 * System.out.println("\n blocking - long wav");
			 * longSound.blockingPlay();
			 */

			/*
			 * testing those unsigned byte conversions
			 */

			JavaSound windowsSound = new JavaSound("/Users/ellie/Desktop/sound2.wav");

			System.out.println("getSample(28567): " + windowsSound.getSample(28567));

			windowsSound.setSample(28567, -32);

			System.out.println("is it the same?" + windowsSound.getSample(28567));

			// cannot do ws1, ws2, they are in "Microsoft ADPCM" format
			// Java cannot create an audioInputStream from the file
			// JavaSound ws1 =
			// new JavaSound("/Users/ellie/Desktop/audio/wmpaud7.wav");
			// System.out.println(ws1.getAudioFileFormat().getFormat());

			// JavaSound ws2 =
			// new JavaSound("/Users/ellie/Desktop/audio/wmpaud6.wav");
			// System.out.println(ws2.getAudioFileFormat().getFormat());

			// ws3,4,7,8 are all little-endian, and signed
			JavaSound ws3 = new JavaSound("/Users/ellie/Desktop/audio/startup.wav");
			System.out.println(ws3.getAudioFileFormat().getFormat());

			JavaSound ws4 = new JavaSound("/Users/ellie/Desktop/audio/ETUDE_16.WAV");
			System.out.println(ws4.getAudioFileFormat().getFormat());

			JavaSound ws7 = new JavaSound("/Users/ellie/Desktop/audio2/pcm_11.025_16_mono.wav");
			System.out.println(ws7.getAudioFileFormat().getFormat());

			JavaSound ws8 = new JavaSound("/Users/ellie/Desktop/audio2/pcm_11.025_16_stereo.wav");
			System.out.println(ws8.getAudioFileFormat().getFormat());

			// 5 and 6 are the only two windows saved in unsigned format:
			JavaSound ws5 = new JavaSound("/Users/ellie/Desktop/audio2/pcm_11.025_8_mono.wav");
			System.out.println(ws5.getAudioFileFormat().getFormat());

			// 6 is really 8kHz contrary to the filename i typed incorrectly
			JavaSound ws6 = new JavaSound("/Users/ellie/Desktop/audio2/pcm_11.025_8_stereo.wav");
			System.out.println(ws6.getAudioFileFormat().getFormat());

			System.out.println("");
			System.out.println(ws5.getSample(0));
			ws5.setSample(0, ws5.getSample(0));
			System.out.println(ws5.getSample(0));

			System.out.println("");
			System.out.println(ws5.getSample(1));
			ws5.setSample(1, ws5.getSample(1));
			System.out.println(ws5.getSample(1));

			System.out.println("");
			System.out.println(ws6.getSample(0));
			ws6.setSample(0, ws6.getSample(0));
			System.out.println(ws6.getSample(0));

			/*
			 * and now for other bit sizes and endiannesses
			 * 
			 * 
			 * JavaSound big8 = new JavaSound(8, true); JavaSound big16 = new
			 * JavaSound(16, true); JavaSound big24 = new JavaSound(24, true);
			 * JavaSound big32 = new JavaSound(32, true);
			 * 
			 * JavaSound little8 = new JavaSound(8, false); JavaSound little16 =
			 * new JavaSound(16, false); JavaSound little24 = new JavaSound(24,
			 * false); JavaSound little32 = new JavaSound(32, false);
			 * 
			 * System.out.println("LEFT\n\tbig8(0): " + big8.getLeftSample(0) +
			 * "\n\tbig16(0): " + big16.getLeftSample(0) + "\n\tbig24(0): " +
			 * big24.getLeftSample(0) + "\n\tbig32(0): " +
			 * big32.getLeftSample(0) + "\n\tlittle8(0): " +
			 * little8.getLeftSample(0) + "\n\tlittle16(0): " +
			 * little16.getLeftSample(0) + "\n\tlittle24(0): " +
			 * little24.getLeftSample(0) + "\n\tlittle32(0): " +
			 * little32.getLeftSample(0));
			 * 
			 * System.out.println("RIGHT\n\tbig8(0): " + big8.getRightSample(0)
			 * + "\n\tbig16(0): " + big16.getRightSample(0) + "\n\tbig24(0): " +
			 * big24.getRightSample(0) + "\n\tbig32(0): " +
			 * big32.getRightSample(0) + "\n\tlittle8(0): " +
			 * little8.getRightSample(0) + "\n\tlittle16(0): " +
			 * little16.getRightSample(0) + "\n\tlittle24(0): " +
			 * little24.getRightSample(0) + "\n\tlittle32(0): " +
			 * little32.getRightSample(0));
			 * 
			 * big8.setLeftSample(0,0); big16.setLeftSample(0,0);
			 * big24.setLeftSample(0,0); big32.setLeftSample(0,0);
			 * 
			 * little8.setLeftSample(0,0); little16.setLeftSample(0,0);
			 * little24.setLeftSample(0,0); little32.setLeftSample(0,0);
			 * 
			 * big8.setRightSample(0,0); big16.setRightSample(0,0);
			 * big24.setRightSample(0,0); big32.setRightSample(0,0);
			 * 
			 * little8.setRightSample(0,0); little16.setRightSample(0,0);
			 * little24.setRightSample(0,0); little32.setRightSample(0,0);
			 * 
			 * System.out.println("LEFT\n\tbig8(0): " + big8.getLeftSample(0) +
			 * "\n\tbig16(0): " + big16.getLeftSample(0) + "\n\tbig24(0): " +
			 * big24.getLeftSample(0) + "\n\tbig32(0): " +
			 * big32.getLeftSample(0) + "\n\tlittle8(0): " +
			 * little8.getLeftSample(0) + "\n\tlittle16(0): " +
			 * little16.getLeftSample(0) + "\n\tlittle24(0): " +
			 * little24.getLeftSample(0) + "\n\tlittle32(0): " +
			 * little32.getLeftSample(0));
			 * 
			 * System.out.println("RIGHT\n\tbig8(0): " + big8.getRightSample(0)
			 * + "\n\tbig16(0): " + big16.getRightSample(0) + "\n\tbig24(0): " +
			 * big24.getRightSample(0) + "\n\tbig32(0): " +
			 * big32.getRightSample(0) + "\n\tlittle8(0): " +
			 * little8.getRightSample(0) + "\n\tlittle16(0): " +
			 * little16.getRightSample(0) + "\n\tlittle24(0): " +
			 * little24.getRightSample(0) + "\n\tlittle32(0): " +
			 * little32.getRightSample(0));
			 * 
			 * //setting bounds big8.setLeftSample(0,0-(int)Math.pow(2,7));
			 * big16.setLeftSample(0,0-(int)Math.pow(2,15));
			 * big24.setLeftSample(0,0-(int)Math.pow(2,23));
			 * big32.setLeftSample(0, 0-(int)Math.pow(2,31));
			 * 
			 * little8.setLeftSample(0,0-(int)Math.pow(2,7));
			 * little16.setLeftSample(0,0-(int)Math.pow(2,15));
			 * little24.setLeftSample(0,0-(int)Math.pow(2, 23));
			 * little32.setLeftSample(0,0-(int)Math.pow(2, 31));
			 * 
			 * big8.setRightSample(0,(int)Math.pow(2,7)-1);
			 * big16.setRightSample(0,(int)Math.pow(2,15)-1);
			 * big24.setRightSample(0,(int)Math.pow(2,23)-1);
			 * big32.setRightSample(0,(int)Math.pow(2,31)-1);
			 * 
			 * little8.setRightSample(0,(int)Math.pow(2,7)-1);
			 * little16.setRightSample(0,(int)Math.pow(2,15)-1);
			 * little24.setRightSample(0,(int)Math.pow(2,23)-1);
			 * little32.setRightSample(0,(int)Math.pow(2,31)-1);
			 * 
			 * System.out.println("Set left to -2^x, right to 2^x-1");
			 * 
			 * System.out.println("LEFT\n\tbig8(0): " + big8.getLeftSample(0) +
			 * "\n\tbig16(0): " + big16.getLeftSample(0) + "\n\tbig24(0): " +
			 * big24.getLeftSample(0) + "\n\tbig32(0): " +
			 * big32.getLeftSample(0) + "\n\tlittle8(0): " +
			 * little8.getLeftSample(0) + "\n\tlittle16(0): " +
			 * little16.getLeftSample(0) + "\n\tlittle24(0): " +
			 * little24.getLeftSample(0) + "\n\tlittle32(0): " +
			 * little32.getLeftSample(0));
			 * 
			 * System.out.println("RIGHT\n\tbig8(0): " + big8.getRightSample(0)
			 * + "\n\tbig16(0): " + big16.getRightSample(0) + "\n\tbig24(0): " +
			 * big24.getRightSample(0) + "\n\tbig32(0): " +
			 * big32.getRightSample(0) + "\n\tlittle8(0): " +
			 * little8.getRightSample(0) + "\n\tlittle16(0): " +
			 * little16.getRightSample(0) + "\n\tlittle24(0): " +
			 * little24.getRightSample(0) + "\n\tlittle32(0): " +
			 * little32.getRightSample(0));
			 */

			System.out.println("\nexiting main");
		}
		catch (Exception e)
		{
			System.out.println(e.getMessage());
		}
		System.exit(0);

	}// main

}// end class JavaSound
