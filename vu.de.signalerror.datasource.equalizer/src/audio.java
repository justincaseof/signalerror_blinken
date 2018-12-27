//package org.pscode.ui.audiotrace;
//
//import javax.swing.JPanel;
//import javax.swing.JComponent;
//import javax.swing.Timer;
//    
//    import java.applet.Applet;
//    import java.awt.*;
//    import java.awt.event.*;
//    import java.awt.geom.*;
//    import java.awt.Point;
//    import java.awt.Dimension;
//    import java.awt.Insets;
//014    import java.awt.Graphics;
//015    import java.awt.Graphics2D;
//016    import java.awt.BasicStroke;
//017    import java.awt.EventQueue;
//018    import java.awt.geom.Rectangle2D;
//019    
//020    import java.awt.MultipleGradientPaint;
//021    import java.awt.RadialGradientPaint;
//022    import java.awt.LinearGradientPaint;
//023    
//024    import java.awt.image.BufferedImage;
//025    
//026    import java.awt.geom.GeneralPath;
//027    import java.awt.geom.Point2D;
//028    
//029    import java.text.DecimalFormat;
//030    
//031    import java.util.Arrays;
//032    import java.util.logging.Logger;
//033    import java.util.logging.Level;
//034    
//035    import javax.sound.sampled.TargetDataLine;
//036    import javax.sound.sampled.AudioFormat;
//037    import javax.sound.sampled.LineUnavailableException;
//038    import javax.sound.sampled.*;
//039    
//040    import java.io.*;
//041    import java.util.HashMap;
//042    
//043    //import org.pscode.ui.audiotrace.*;
//044    import org.pscode.ui.license.*;
//045    
//046    import org.pscode.xui.param.Parameter;
//047    import org.pscode.xui.param.Configurable;
//048    
//049    /** The AudioPlotPanel (APP) provides a trace of an audio signal.
//050    It is very configurable.  Besides configuring the class from code,
//051    it can be configured using the AudioTraceColorsPanel (ATCP) and 
//052    AudioTraceOptionsPanel (ATOP) and the methods of the AudioTraceMenu
//053    to provide a JMenu that pops those two panels, and allows configuration
//054    of volume (trace) options and full-screen mode.
//055    
//056    Note that the APP can be used without the configuration classes, so long as 
//057    you do not intend to offer the user the option to change the rendering options.
//058    
//059    Versions
//060    <ul>
//061    <li>2009-09-26 Improvements to the plot
//062    <ul>
//063    <li>Trimmed first point of lissajous plots since it always started in the middle of the screen
//064    <li>Improved plot timing for the buffered plots (where we have the entire sound wave in memory)
//065    to show not what is about to come up, but 50/50 what has just played and is about to be played.
//066    </ul>
//067    <li>2008-06-01 First release of the APP
//068    </ul>
//069    @version 2009-09-26
//070    @author Andrew Thompson */
//071    public class AudioPlotPanel extends JPanel implements Runnable, Configurable {
//072    
//073            /* start of the attributes relevant specifically to a TargetDataLine */
//074            /** A reference to the sound source of interest. */
//075            TargetDataLine line;
//076    
//077            /** The 'number' of this line, as informed by the class that
//078            configures it. */
//079            int lineNumber = -1;
//080    
//081            Thread t;
//082            long threadSleep = 20l;
//083            boolean run = true;
//084            /* and of the attributes relevant specifically to a TargetDataLine */
//085    
//086            /* start of the attributes relevant specifically to a data buffer */
//087            /** A buffer containing the bytes of the entire sound sample */
//088            byte[] buffer;
//089    
//090            int lastPosition;
//091    
//092            int lastRoundNumber;
//093            /* end of the attributes relevant specifically to a data buffer */
//094    
//095            /** Represents the no. samples multiplied by the frame size,
//096            the number of bytes in the block. */
//097            int blockSize;
//098    
//099            /** Local buffer of sound data to be rendered. */
//100            byte[] renderBuffer;
//101    
//102            AudioFormat format;
//103            /** Obtained from AudioFormat but stored as primitive for speed. */
//104            int channels = 0;
//105            int frameSize = 0;
//106            int bitDepth = 0;
//107            boolean signed = true;
//108            boolean bigEndian = true;
//109    
//110            int maxLevel = 1;
//111            boolean showVolume = true;
//112    
//113            boolean paintGradient = true;
//114            MultipleGradientPaint[] gradient = new MultipleGradientPaint[2];
//115            float[] fractions = {
//116                    0.01f,
//117                    0.99f
//118            };
//119    
//120            /** For double buffering. */
//121            BufferedImage biMainTrace;
//122            /** used to manipulate the image */
//123            BufferedImage biOldTraces;
//124            /** used to manipulate the image */
//125            BufferedImage biFinalTrace;
//126    
//127            /** Averages the channels into a single (mono), value. */
//128            //boolean forceMono;
//129    
//130            /** Use a different color for each channel of the signal. */
//131            Color[] mainColor = {
//132                    new Color( 0, 51, 204),
//133    /*              new Color( 51, 255, 51),
//134                    new Color( 255, 255, 0),
//135                    new Color( 255, 0, 0),*/
//136                    new Color( 255, 0, 255)
//137            };
//138    
//139            Color[] outerColor = {
//140                    new Color( 255, 0, 51),
//141    /*              new Color( 255, 255, 0),
//142                    new Color( 255, 255, 0),
//143                    new Color( 255, 0, 0),*/
//144                    new Color( 255, 0, 255)
//145            };
//146    
//147            BasicStroke stroke = new BasicStroke(2,
//148                    BasicStroke.CAP_ROUND,
//149                    BasicStroke.JOIN_ROUND);
//150    
//151            long lastGainChange = 0;
//152            long startTime;
//153            public static int MAX_GAIN = 5;
//154    
//155            /** Loops through the gain settings, if true. */
//156            boolean loopGain = false;
//157    
//158            /** Amplifies (trace gets bigger) the raw signal by
//159            'gain' before tracing. */
//160            int gain = 1;
//161    
//162            /** We grab sound data in blocks of 'sampleSize'.  Low values
//163            make for a fast display update, but larger values show the
//164            signal more clearly, I would recommend values between 100 &
//165            400 here. */
//166            static int sampleSize;
//167    
//168            /** Determines the rate of fade of earlier traces.
//169            0 for no fade, 255 for complete clear. */
//170            int fadeRate;   
//171    
//172            /** The size of the step at which earlier signals scroll
//173            off-screen. */
//174            int scrollStep;
//175    
//176            /** Step amount to zoom the old traces. */
//177            int zoomStep;
//178    
//179            /** Will draw a pseudo-lissajous plot if true, else standard
//180            oscilloscope trace. */
//181            boolean lissajous = true;
//182            /** Scaling factor for gradient gain. */
//183            int gradientGain = 3;
//184            int gradientScaling = -1;
//185    
//            DecimalFormat df;
//    
//            int barWidth = 10;
//            int barHeight = 100;
//            int pad = 10;
//            GradientPaint gpBarLeft;
//            GradientPaint gpBarRight;
//193            boolean paintGradientsByTraceColor = true;
//194            int barTransparency = 160;
//195    
//196            Timer timer;
//197    
//198            public AudioPlotPanel() {
//199                    this((TargetDataLine)null);
//200            }
//201    
//202            /** Construct the plot panel. */
//203            public AudioPlotPanel(TargetDataLine line) {
//204                    ActionListener al = new ActionListener(){
//205                            public void actionPerformed(ActionEvent ae) {
//206                                    try {
//207                                            License.isLicensed((JComponent)AudioPlotPanel.this);
//208                                    } catch(NoClassDefFoundError showMessage) {
//209                                            System.err.println(showMessage.getMessage() + " missing from classpath");
//210                                            setMessage( "AudioPlot courtesy of pscode.org ", 5000 );
//211                                    } catch(Throwable showMessage) {
//212                                            setMessage( showMessage.getMessage() + " ", 5000 );
//213                                    }
//214                            }
//215                    };
//216                    timer = new Timer(10, al);
//217    
//218                    df = new DecimalFormat("0.00000");
//219                    if (line!=null) {
//220                            setLine(line);
//221                    }
//222    
//223                    setPreferredSize(new Dimension( 300,120 ));
//224                    setSampleSize(64);
//225                    setBackground(Color.BLACK);
//226            }
//227    
//228            public boolean getGradientsByTraceColor() {
//229                    return paintGradientsByTraceColor;
//230            }
//231    
//232            public void setGradientsByTraceColor() {
//233                    setGradientsByTraceColor(paintGradientsByTraceColor);
//234            }
//235    
//236            public void setGradientsByTraceColor(boolean paintGradientsByTraceColor) {
//237                    this.paintGradientsByTraceColor = paintGradientsByTraceColor;
//238    int yLo = getHeight()-pad;
//239    int yHi = getHeight()-(pad+barHeight);
//240                    if (!paintGradientsByTraceColor) {
//241                            Color low = new Color(0,255,0,barTransparency);
//242                            Color hi = new Color(255,0,0,barTransparency);
//243                            gpBarLeft = new GradientPaint(
//244                                    new Point2D.Double(0, yLo),
//245                                    low,
//246                                    new Point2D.Double(0, getHeight()-(pad+barHeight)),
//247                                    hi
//248                            );
//249                            gpBarRight = gpBarLeft;
//250                    } else {
//251                            gpBarLeft = new GradientPaint(
//252                                    new Point2D.Double(0, yLo),
//253                                    getTransparentBasedOnColor(mainColor[0],barTransparency),
//254                                    new Point2D.Double(0, yHi),
//255                                    getTransparentBasedOnColor(outerColor[0],barTransparency) );
//256                            gpBarRight = new GradientPaint(
//257                                    new Point2D.Double(0, yLo),
//258                                    getTransparentBasedOnColor(mainColor[1],barTransparency),
//259                                    new Point2D.Double(0, yHi),
//260                                    getTransparentBasedOnColor(outerColor[1],barTransparency) );
//261                    }
//262            }
//263    
//264            Parameter[] params;
//265    
//266            public Parameter[] getParams(Applet applet) {
//267    
//268                    if (params==null) {
//269                            Parameter[] temp = {
//270    new Parameter(applet, "color.scheme.name","String","The name of one of the predefined plot color schemes","default"),
//271    new Parameter(applet, "trace.lissajous","boolean","Choose the lissajous plotting mode if present","false"),
//272    new Parameter(applet, "trace.sample.size","int","The number of blocks of frames to paint in each pass 1 (fine) - 10 (broad)", "6","1","12"),
//273    new Parameter(applet, "trace.gain","int","Set the visual gain (height) of the trace", "1","1","5"),
//274    new Parameter(applet, "trace.gradient.gain","int","Set the gain (width) of the lissajous trace", "2","1","5"),
//275    new Parameter(applet, "trace.thickness","int","Set the initial stroke size of the traces", "15","1","50"),
//276    new Parameter(applet, "trace.fade","int","Set the fade rate of old traces from", "65", "0", "100"),
//277    new Parameter(applet, "trace.scroll","int","Set the scroll rate of old traces", "25", "-50", "50"),
//278    new Parameter(applet, "trace.zoom","int","Set the zoom rate of old traces", "50", "-100", "100"),
//279    new Parameter(applet, "volume.show","boolean","Any value to show volume bars, default if omitted is no volume","true"),
//280    new Parameter(applet, "volume.match.colors","boolean","Match the colors used for the traces, default is false","false"),
//281    new Parameter(applet, "volume.boost","boolean","Visually boost the displayed volume of traces, default is false","true")
//282                            };
//283                            params = temp;
//284                    }
//285                    return params;
//286            }
//287    
//288            private HashMap<String, Parameter> paramsMap;
//289    
//290            public HashMap getParamMap(Applet applet) {
//291                    if (paramsMap==null) {
//292                            paramsMap = new HashMap<String, Parameter>();
//293                            params = getParams(applet);
//294                            for (Parameter param : params) {
//295                                    paramsMap.put( param.getName(), param );
//296                            }
//297                    }
//298                    return paramsMap;
//299            }
//300    
//301            public void configure(Applet applet) {
//302    
//303                    HashMap<String, Parameter> prm = getParamMap(applet);
//304    
//305                    String colorScheme = prm.get("color.scheme.name").getStringValue();
//306                    try {
//307                            TraceColorOptions tco = new TraceColorOptions();
//308                            TraceColorOptions[] tcos = tco.getDefaultColorOptions();
//309                            setColorScheme( 0 );
//310    
//311                            if (colorScheme!=null) {
//312                                            colorSchemeIndex = 0;
//313                                            int ii = 0;
//314    //                                      setColorScheme( tcos[0] );
//315                                            for (TraceColorOptions tco2 : tcos) {
//316                                                    if ( tco2.getShortName().equals(colorScheme) ) {
//317                                                            logger.log(Level.CONFIG, "Setting color scheme to: " + tco2);
//318                                                            colorSchemeIndex = ii;
//319                                                            setColorScheme( tco2 );
//320                                                            break;
//321                                                    }
//322                                                    ii++;
//323                                            }
//324                            }
//325                    } catch(IOException ioe) {
//326                            ioe.printStackTrace();
//327                    }
//328    
//329                    setShowVolume( prm.get("volume.show").getBooleanValue().booleanValue() );
//330                    setScaledVolume( prm.get("volume.boost").getBooleanValue().booleanValue() );
//331                    setGradientsByTraceColor( prm.get("volume.match.colors").getBooleanValue().booleanValue() );
//332    
//333                    setLissajous( prm.get("trace.lissajous").getBooleanValue().booleanValue() );
//334    
//335                    setSampleSize( prm.get("trace.sample.size").getIntegerValue().intValue()*32 );
//336                    setStrokeSize( prm.get("trace.thickness").getIntegerValue().intValue() );
//337                    setFadeRate( prm.get("trace.fade").getIntegerValue().intValue() );
//338                    setScrollStep( prm.get("trace.scroll").getIntegerValue().intValue() );
//339                    setZoomStep( prm.get("trace.zoom").getIntegerValue().intValue() );
//340                    setGain( prm.get("trace.gain").getIntegerValue().intValue() );
//341            }
//342    
//343            int colorSchemeIndex;
//344    
//345            public int getColorSchemeIndex() {
//346                    return colorSchemeIndex;
//347            }
//348    
//349            public void setColorSchemeIndex(int i) {
//350                    colorSchemeIndex = i;
//351            }
//352    
//353            public Color getTransparentBasedOnColor(Color c, int t) {
//354                    return new Color(c.getRed(), c.getGreen(), c.getBlue(), t);
//355            }
//356    
//357            /** Construct the plot panel. */
//358            public void setSampleSize(int sampleSize) {
//359                    this.sampleSize = sampleSize;
//360                    refreshBufferSize();
//361                    //arrangeCopyBuffer();
//362            }
//363    
//364            public void refreshBufferSize() {
//365                    blockSize = sampleSize*frameSize;
//366                    renderBuffer = new byte[blockSize];
//367            }
//368    
//369            public void setFormat( AudioFormat af ) {
//370                    format = af;
//371                    channels = af.getChannels();
//372                    frameSize = af.getFrameSize();
//373                    bitDepth = af.getSampleSizeInBits();
//374                    signed = !af.getEncoding().toString().toLowerCase().endsWith("unsigned");
//375                    bigEndian = af.isBigEndian();
//376                    //setSampleSize(sampleSize);
//377                    refreshBufferSize();
//378            }
//379    
//380            /* Start of the methods relevant specifically to a TargetDataLine */
//381            public void start() {
//382                    t = new Thread(this);
//383                    startTime = System.currentTimeMillis();
//384                    t.start();
//385            }
//386    
//387            /** Read data from the TargetDataLine, then paint it
//388            to screen. */
//389            public void run() {
//390                    while(line!=null && run) {
//391                            line.read(renderBuffer,0,sampleSize*frameSize);
//392                            repaint();
//393                            try {
//394                                    Thread.sleep(threadSleep);
//395                            } catch(InterruptedException ie) {
//396                                    // no problem, wake and continue
//397                            }
//398                    }
//399            }
//400    
//401            public void stop() throws Throwable {
//402                    run = false;
//403                    line.stop();
//404                    line.flush();
//405                    line.close();
//406                    //t.join();
//407                    biMainTrace.flush();
//408                    biOldTraces.flush();
//409                    biFinalTrace.flush();
//410            }
//411    
//412            public void setThreadSleep(long millis) {
//413                    threadSleep = millis;
//414            }
//415    
//416            public void setLine(TargetDataLine line) {
//417                    this.line = line;
//418                    try {
//419                            line.open();
//420                            line.start();
//421                            setFormat( line.getFormat() );
//422                    } catch(LineUnavailableException lue) {
//423                            System.err.println("Unable to open line!");
//424                            lue.printStackTrace();
//425                    }
//426            }
//427    
//428            /** Informs the class what 'line number' this represents. */
//429            public void setLineNumber(int lineNumber) {
//430                    this.lineNumber = lineNumber;
//431            }
//432            /* End of the methods relevant specifically to a TargetDataLine */
//433    
//434            /* Start of the methods relevant specifically to a buffer */
//435            public void setBuffer(byte[] buffer) {
//436                    this.buffer = buffer;
//437                    lastPosition = 0;
//438                    //arrangeCopyBuffer();
//439            }
//440    
//441            public void setFramePosition(int framePosition) {
//442                    setByteCount( framePosition*frameSize );
//443            }
//444    
//445            String message;
//446            Font font = new Font(Font.SANS_SERIF, Font.BOLD, 18);
//447            long messageStart;
//448            boolean showMessage = false;
//449            int messageDisplayTime;
//450            int messageFadeTime;
//451    
//452            public void setMessage(String message, int messageDisplayTime) {
//453                    this.message = message.trim() + " ";
//454                    this.messageDisplayTime = messageDisplayTime;
//455                    this.messageFadeTime = messageFadeTime;
//456                    messageStart = System.currentTimeMillis();
//457                    showMessage = true;
//458                    repaint();
//459            }
//460    
//461            public void setByteCount(int byteCount) {
//462                    if ( blockSize<1 ) return;
//463    
//464                    int roundNumber = (int)(byteCount/blockSize);
//465                    if ( lastPosition < (roundNumber*blockSize)+(blockSize/2) ) {
//466                            /* Method 1 */
//467    
//468                            int first;
//469                            int last;
//470                            if ( ((roundNumber+1)*blockSize)<buffer.length ) {
//471                                    first = ((roundNumber-(sampleSize/2))*blockSize);
//472                                    if (first<0) {
//473                                            first=0;
//474                                    }
//475                                    last = ((roundNumber+(sampleSize/2))*blockSize);
//476                            } else {
//477                                    first = buffer.length-blockSize;
//478                                    last = buffer.length;
//479                            }
//480    /*
//481                            // for some reason, this makes the sound choppy..
//482                            if (first<0) {
//483                                    return;
//484                            }
//485    */
//486    refreshBufferSize();
//487                            renderBuffer = Arrays.copyOfRange(
//488                                    buffer,
//489                                    first,
//490                                    last );
//491                            repaint();
//492    
//493                            logger.log( Level.FINE, "Dropped frames: " +
//494                                    (roundNumber-lastRoundNumber) );
//495                            lastRoundNumber = roundNumber;
//496                    }
//497            }
//498    
//499            Logger logger = Logger.getAnonymousLogger();
//500    
//501            public void copyIntoRenderBuffer(int first, int last) {
//502                    renderBuffer = new byte[last-first];
//503                    for (int ii=first; ii<last; ii++) {
//504                            renderBuffer[ii-first] = buffer[ii];
//505                    }
//506            }
//507            /* End of the methods relevant specifically to a buffer */
//508    
//509            /* From here on, the methods are specific to the panel and  */
//510    
//511            public void setColor(Color c, int index) {
//512                    mainColor[index] = c;
//513                    setGradientsByTraceColor();
//514            }
//515    
//516            public Color getColor(int index) {
//517                    return mainColor[index];
//518            }
//519    
//520            public void setColorScheme(int i) {
//521                    try {
//522                            colorSchemeIndex = i;
//523                            TraceColorOptions tco = new TraceColorOptions();
//524                            TraceColorOptions[] tcos = tco.getDefaultColorOptions();
//525                            setColorScheme(tcos[i]);
//526                    } catch(Exception e) {
//527                            e.printStackTrace();
//528                    }
//529            }
//530    
//531    /*
//532    //audioPlotPanel.setBackground( traceColorOptions.getColors()[0] );
//533    
//534    left.setBackground( traceColorOptions.getColors()[1] );
//535    //audioPlotPanel.setColor( traceColorOptions.getColors()[1], 0 );
//536    
//537    right.setBackground( traceColorOptions.getColors()[2] );
//538    //audioPlotPanel.setColor( traceColorOptions.getColors()[2], 1 );
//539    
//540    leftSecondary.setBackground( traceColorOptions.getColors()[3] );
//541    //audioPlotPanel.setOuterColor( traceColorOptions.getColors()[3], 0 );
//542    
//543    rightSecondary.setBackground( traceColorOptions.getColors()[4] );
//544    //audioPlotPanel.setOuterColor( traceColorOptions.getColors()[4], 1 );
//545    */
//546    
//547            private void setColorScheme(TraceColorOptions tco) {
//548                    logger.log(Level.FINE, tco.toString() );
//549                    setBackground( tco.getColors()[0] );
//550                    setOuterColor( tco.getColors()[3], 0 );
//551                    setOuterColor( tco.getColors()[4], 1 );
//552                    setColor( tco.getColors()[1], 0 );
//553                    setColor( tco.getColors()[2], 1 );
//554                    clearTrace();
//555                    repaint();
//556            }
//557    
//558            public void setOuterColor(Color c, int index) {
//559                    outerColor[index] = c;
//560                    setGradientsByTraceColor();
//561            }
//562    
//563            public Color getOuterColor(int index) {
//564                    return outerColor[index];
//565            }
//566    
//567            public void setPaintGradient(boolean paintGradient) {
//568                    this.paintGradient = paintGradient;
//569            }
//570    
//571            public void refreshGradients() {
//572                    for (int ii=0; ii<2; ii++) {
//573                            Color[] colors = {
//574                                    getColor(ii),
//575                                    getOuterColor(ii)
//576                            };
//577                            setGradient(colors, ii);
//578                    }
//579            }
//580    
//581            private void setGradient(Color[] colors, int index) {
//582                    int width = getWidth();
//583                    int height = getHeight();
//584                    if (width<1 || height<1) {
//585                            return;
//586                    }
//587                    Point center = new Point(getWidth()/2, getHeight()/2);
//588                    MultipleGradientPaint.CycleMethod cycle = MultipleGradientPaint.CycleMethod.NO_CYCLE;
//589                    if (lissajous) {
//590                            float radius = (float)(getWidth()/2);
//591                            gradient[index] = new RadialGradientPaint(
//592                                    center,
//593                                    radius,
//594                                    fractions,
//595                                    colors,
//596                                    cycle);
//597                    } else {
//598                            Point end = new Point(getWidth()/2,getHeight());
//599                            gradient[index] = new LinearGradientPaint(
//600                                    center,
//601                                    end,
//602                                    fractions,
//603                                    colors,
//604                                    MultipleGradientPaint.CycleMethod.REFLECT);
//605                    }
//606            }
//607    
//608            RenderingHints renderingHints;
//609    
//610            public void setRenderingHints(RenderingHints renderingHints) {
//611                    this.renderingHints = renderingHints;
//612            }
//613    
//614            public MultipleGradientPaint getGradient(int index) {
//615                    return gradient[index];
//616            }
//617    
//618            public void setStrokeSize(int size) {
//619                    stroke = new BasicStroke(size,
//620                            BasicStroke.CAP_ROUND,
//621                            BasicStroke.JOIN_ROUND);
//622            }
//623    
//624            void iterateGain() {
//625                    if (gain==MAX_GAIN) {
//626                            gain = 1;
//627                    } else {
//628                            gain++;
//629                    }
//630                    setGain( gain );
//631            }
//632    
//633            public void setScrollStep(int step) {
//634                    scrollStep = step;
//635            }
//636    
//637            public void setZoomStep(int step) {
//638                    zoomStep = step;
//639            }
//640    
//641            public void setGain(int gain) {
//642                    this.gain = gain;
//643                    lastGainChange = System.currentTimeMillis();
//644            }
//645    
//646            public void setGainLoop(boolean loopGain) {
//647                    this.loopGain = loopGain;
//648            }
//649    
//650            public boolean getGainLoop() {
//651                    return loopGain;
//652            }
//653    
//654            public int getGain() {
//655                    return gain;
//656            }
//657    
//658            public int getSampleSize() {
//659                    return sampleSize;
//660            }
//661    
//662            public int getGradientGain() {
//663                    return gradientGain;
//664            }
//665    
//666            public void setLissajous(boolean lissajous) {
//667                    this.lissajous = lissajous;
//668                    refreshGradients();
//669            }
//670    
//671            public void setGradientGain(int gradientGain){
//672                    this.gradientGain = gradientGain;
//673            }
//674    
//675            public void setFadeRate(int fadeRate) {
//676                    this.fadeRate = fadeRate;
//677            }
//678    
//679            public void setShowVolume(boolean showVolume) {
//680                    this.showVolume = showVolume;
//681            }
//682    
//683            public boolean getShowVolume() {
//684                    return showVolume;
//685            }
//686    
//687            boolean clear = false;
//688    
//689            public void clearTrace() {
//690    
//691                    clear = true;
//692                    repaint();
//693                    clear = false;
//694    
//695    /*
//696                    Runnable r = new Runnable() {
//697                            public void run() {
//698                                    repaint();
//699                            }
//700                    };
//701                    EventQueue.invokeLater( r );
//702    */
//703            }
//704    
//705            /** Paint a trace of the current waveform to screen. */
//706            public void paintComponent(Graphics g) {
//707                    if (timer==null) {
//708                            return;
//709                    } else {
//710                            if (!timer.isRunning()) {
//711                                    timer.setDelay(1000*60*10);
//712                                    //timer.setDelay(10*60*10);
//713                                    timer.restart();
//714                            }
//715                    }
//716                    if( clear ) {
//717                            super.paintComponent(g);
//718                            g.fillRect(0,0,getWidth(),getHeight());
//719                    } else {
//720                            renderPlot();
//721                            super.paintComponent(g);
//722                            g.drawImage(biFinalTrace,0,0,this);
//723                    }
//724                    g.dispose();
//725            }
//726    
//727            public void renderPlot() {
//728                    Graphics2D g1, g2, g3;
//729    
//730                    gradientScaling = (gradientScaling<1 ?
//731                            getWidth()/10 : gradientScaling);
//732    
//733    //              int pd = (int)stroke.getLineWidth();
//734                    if (biMainTrace==null ||
//735                            biMainTrace.getWidth()!=getWidth() ||
//736                            biMainTrace.getHeight()!=getHeight() ) {
//737    
//738                            biMainTrace = (BufferedImage)createImage(
//739                                    getWidth(),
//740                                    getHeight());
//741                            g1 = biMainTrace.createGraphics();
//742    
//743                            biOldTraces = (BufferedImage)createImage(
//744                                    getWidth(),
//745                                    getHeight());
//746                            g2 = biOldTraces.createGraphics();
//747                            g2.setColor(getBackground());
//748                            g2.fillRect(0,0,getWidth(), getHeight());
//749    
//750                            biFinalTrace = (BufferedImage)createImage(
//751                                    getWidth(),
//752                                    getHeight());
//753    
//754                            refreshGradients();
//755                            setGradientsByTraceColor();
//756                    }
//757                    g1 = biMainTrace.createGraphics();
//758    g1.getRenderingHints().add(renderingHints);
//759                    g2 = biOldTraces.createGraphics();
//760    g2.getRenderingHints().add(renderingHints);
//761                    g3 = biFinalTrace.createGraphics();
//762    g3.getRenderingHints().add(renderingHints);
//763    
//764                    g1.drawImage(biOldTraces,0,0,this);
//765    
//766                    g1.setColor( getBackground() );
//767                    g1.setStroke(stroke);
//768    
//769                    GeneralPath[] gp = new GeneralPath[ channels ];
//770                    for ( int ii=0; ii< gp.length; ii++ ) {
//771                            gp[ii] = new GeneralPath(
//772                                    GeneralPath.WIND_NON_ZERO,
//773                                    sampleSize);
//774                    }
//775                    double[] lastSignalSize = null;
//776                    double[] leftLevels = new double[sampleSize];
//777                    double[] rightLevels = new double[sampleSize];
//778                    for (int ii=0; ii<(int)(sampleSize); ii++) {
//779                            byte[] frameSample = new byte[frameSize];
//780                            for (int jj=0; jj<frameSize; jj++) {
//781                                    frameSample[jj] = renderBuffer[
//782                                            (
//783                                            (ii)*
//784                                            channels*
//785                                            bitDepth/8
//786                                            )
//787                                            +
//788                                            jj];
//789                            }
//790    
//791                            double[] signalSize =
//792                                    frameToSignedDoubles( frameSample );
//793                            if (signalSize.length>1) {
//794                                    leftLevels[ii] = signalSize[0];
//795                                    rightLevels[ii] = signalSize[1];
//796                            }
//797    
//798                            if (lastSignalSize==null) {
//799                                    lastSignalSize = signalSize;
//800                            }
//801                            for (int jj=0; jj<signalSize.length; jj++) {
//802                                    if (lissajous) {
//803                                            addLissajousPoint(gp[jj], ii,
//804                                                    signalSize[jj], lastSignalSize[jj]);
//805                                    } else {
//806                                            addTracePoint(gp[jj], ii, signalSize[jj]);
//807                                    }
//808                            }
//809                            lastSignalSize = signalSize;
//810                    }
//811    
//812                    if (lissajous) {
//813                            GeneralPath[] temp = new GeneralPath[gp.length];
//814                            for ( int ii=0; ii< temp.length; ii++ ) {
//815                                    temp[ii] = new GeneralPath(
//816                                            GeneralPath.WIND_NON_ZERO,
//817                                            sampleSize-1);
//818                            }
//819                            for (int ii=0; ii<gp.length; ii++) {
//820                                    GeneralPath gPath = gp[ii];
//821                                    PathIterator pit = gPath.getPathIterator(null);
//822                                    // ingore the first point
//823                                    pit.next();
//824                                    double[] point = new double[2];
//825                                    pit.currentSegment(point);
//826                                    temp[ii].moveTo( point[0], point[1] );
//827                                    while(!pit.isDone()) {
//828                                            pit.currentSegment(point);
//829                                            temp[ii].lineTo( point[0], point[1] );
//830                                            pit.next();
//831                                    }
//832                            }
//833                            gp = temp;
//834                    }
//835    
//836                    // iterate the array backwards, so that the most important
//837                    // traces (left and right main channels - cross fingers)
//838                    // are topmost
//839                    for ( int ii=gp.length-1; ii>-1; ii-- ) {
//840                            //g1.setColor(mainColor[ii]);
//841                            MultipleGradientPaint grad = gradient[ii];
//842                            if (grad==null) {
//843                                    setColor(getColor(ii), ii);
//844                                    grad = gradient[ii];
//845                            }
//846                            if (paintGradient) {
//847                                    g1.setPaint( grad );
//848                                    g1.draw(gp[ii]);
//849                            } else {
//850                                    g1.setColor( mainColor[ii] );
//851                                    g1.draw(gp[ii]);
//852                            }
//853                    }
//854    
//855                    g1.dispose();
//856    
//857                    /* Paint the earlier traces, but possibly zoom
//858                    or scroll as well. */
//859                    int lissajousXOffset = (lissajous ? zoomStep*2 : 0);
//860                    g2.drawImage(biMainTrace,
//861                            -lissajousXOffset/2,
//862                            -scrollStep,
//863                            getWidth()+lissajousXOffset,
//864                            getHeight()+zoomStep,
//865                            this);
//866                    Color bg = getBackground();
//867                    g2.setColor( new Color(bg.getRed(), bg.getGreen(), bg.getBlue(),fadeRate) );
//868                    g2.fillRect(0,0,getWidth(), getHeight());
//869                    g2.dispose();
//870    
//871                    g3.drawImage(biMainTrace,0,0,this);
//872    
//873                    // paint the gain setting for 2 seconds after changing
//874                    long timeSinceLastGainChange =
//875                            System.currentTimeMillis() - lastGainChange;
//876                    if (timeSinceLastGainChange<20 && timeSinceLastGainChange<2000) {
//877                            g3.setColor(Color.white);
//878                            g3.drawString( "Gain " + gain, 10, 20 );
//879                    }
//880    
//881                    if (loopGain && timeSinceLastGainChange>4000) {
//882                            iterateGain();
//883                    }
//884    
//885                    // paint the 'line number' for the first few seconds
//886                    // after starting
//887                    long timeSinceStart = System.currentTimeMillis() - startTime;
//888                    if ( lineNumber>-1 && timeSinceStart<7000 ) {
//889                            g3.setColor(Color.white);
//890                            g3.drawString( "Line " + lineNumber, 10, getHeight()-10 );
//891                    }
//892    
//893                    if (showMessage) {
//894                            g3.setFont( getIdealFont(message, g3, font) );
//895    
//896                            FontMetrics fm = g3.getFontMetrics();
//897                            Rectangle2D r = fm.getStringBounds( message, g3 );
//898    
//899                            int x = (int)(getWidth()-r.getWidth())/2;       
//900                            int y = 5;
//901                            int pad = 5;
//902                            g3.setColor(textBackDrop);
//903                            g3.fillRect(x-pad,y,(int)(r.getWidth())+pad,(int)(r.getHeight())+pad);
//904    
//905                            g3.setColor(Color.black);
//906    
//907                            g3.drawString( message, x, y+(int)(r.getHeight()) );
//908                            if (System.currentTimeMillis()-messageStart>messageDisplayTime) {
//909                                    showMessage = false;
//910                            }
//911                    }
//912    
//913                    if (showVolume) {
//914                            int startX = getWidth()-barWidth-pad;
//915                            int startY = getHeight()-barHeight-pad;
//916    
//917                            int rmsLevelHeight = (int)(scaledVolume(leftLevels)*100d);
//918                            int leftHeight = rmsLevelHeight;
//919                            int rightHeight = (int)(scaledVolume(rightLevels)*100d);
//920    
//921                            g3.setColor(getColorForLevel(rightHeight));
//922                            g3.fillRect(startX,startY,barWidth,barHeight);
//923                            g3.setColor( getColorForLevel(leftHeight) );
//924                            g3.fillRect(pad,startY,barWidth,barHeight);
//925    
//926                            if ( leftHeight>barHeight ) {
//927                                    System.err.println("ERROR! " + leftHeight);
//928                            }
//929    
//930                            if ( rightHeight>barHeight ) {
//931                                    System.err.println("ERROR! " + rightHeight);
//932                            }
//933    
//934    //                      g3.setColor(faintGreen);
//935    g3.setPaint(gpBarLeft);
//936                            g3.fillRect(startX,startY+barHeight-rightHeight,barWidth,rightHeight);
//937    g3.setPaint(gpBarRight);
//938                            g3.fillRect(pad,startY+barHeight-leftHeight,barWidth,leftHeight);
//939    
//940                            g3.setColor(Color.gray);
//941                            g3.drawRect(startX,startY,barWidth,barHeight);
//942                            g3.drawRect(pad,startY,barWidth,barHeight);
//943                    }
//944                    g3.dispose();
//945            }
//946    
//947            public Font getIdealFont(String message, Graphics g, Font f) {
//948                    g.setFont(f);
//949                    FontMetrics fm = g.getFontMetrics();
//950                    Rectangle2D r = fm.getStringBounds( message, g );
//951                    // shirnk font to fit a max 80% screen width
//952                    if (r.getWidth()>getWidth()*8/10) {
//953                            return getIdealFont( message, g, f.deriveFont((float)(f.getSize()-2)));
//954                    }
//955                    return f;
//956            }
//957    
//958            Color textBackDrop = new Color(255,255,255,100);
//959            int lowValue = 230;
//960            Color faintGreen = new Color(0,255,0,160);
//961            Color fainterWhite = new Color(255,255,255,80);
//962            Color fainterYellow = new Color(255,255,lowValue,80);
//963            Color fainterRed = new Color(255,lowValue,lowValue,80);
//964    
//965            public Color getColorForLevel(double level) { 
//966                    Color color;
//967                    if (level>4) {
//968                            color = fainterWhite;
//969                    } else if (level>1) {
//970                            color = fainterYellow;
//971                    } else {
//972                            color = fainterRed;
//973                    }
//974                    return color;
//975            }
//976    
//977            boolean scaledVolume = true;
//978    
//979            public void setScaledVolume(boolean scaledVolume) {
//980                    this.scaledVolume = scaledVolume;
//981            }
//982    
//983            public boolean getScaledVolume() {
//984                    return scaledVolume;
//985            }
//986    
//987            public double scaledVolume(double[] raw) {
//988                    double volumeRMS = volumeRMS(raw);
//989                    if (scaledVolume) {
//990                            return Math.pow(volumeRMS, 0.5d);
//991                    } else {
//992                            return volumeRMS;
//993                    }
//994            }
//995    
//996            /** Computes the RMS volume of a group of signal sizes ranging from -1 to 1. */
//997            public double volumeRMS(double[] raw) {
//998                    double sum = 0d;
//999                    if (raw.length==0) {
//1000                            return sum;
//1001                    } else {
//1002                            for (int ii=0; ii<raw.length; ii++) {
//1003                                    sum += raw[ii];
//1004                            }
//1005                    }
//1006                    double average = sum/raw.length;
//1007    
//1008                    double[] meanSquare = new double[raw.length];
//1009                    double sumMeanSquare = 0d;
//1010                    for (int ii=0; ii<raw.length; ii++) {
//1011                            sumMeanSquare += Math.pow(raw[ii]-average,2d);
//1012                            meanSquare[ii] = sumMeanSquare;
//1013                    }
//1014                    double averageMeanSquare = sumMeanSquare/raw.length;
//1015                    double rootMeanSquare = Math.pow(averageMeanSquare,0.5d);
//1016    
//1017                    return rootMeanSquare;
//1018            }
//1019    
//1020            /** Plots the amplitude of the signal against time.
//1021            AKA an oscilloscope trace. */
//1022            public void addTracePoint(
//1023                    GeneralPath gp,
//1024                    int sampleProgress,
//1025                    double signalSize) {
//1026                    int xScaled = (int)(
//1027                            sampleProgress*(
//1028                            (double)getWidth()/
//1029                            (double)(sampleSize-1)));
//1030                    int yScaled =
//1031                            (int)(
//1032                                    ((signalSize*gain)*
//1033                                    (double)getHeight()/2)+
//1034                                    (getHeight()/2) );
//1035                    if (sampleProgress==0) {
//1036                            gp.moveTo( xScaled, yScaled );
//1037                    } else {
//1038                            gp.lineTo( xScaled, yScaled );
//1039                    }
//1040            }
//1041    
//1042            /** Plots the amplitude of the signal against
//1043            its own gradient. */
//1044            public void addLissajousPoint(
//1045                    GeneralPath gp,
//1046                    int sampleProgress,
//1047                    double signalSize,
//1048                    double lastSignalSize) {
//1049                    double angle =
//1050                            getGradientToAngle(lastSignalSize, signalSize);
//1051    //logger.log(Level.INFO, "Angle in Rads: " + angle/Math.PI);
//1052    /*
//1053                    int xScaled = (int)(angle*(2d/Math.PI)*getWidth()/4d)+
//1054                            (getWidth()/2);
//1055                    int yScaled =
//1056                            (int)(
//1057                                    ((signalSize*gain)*
//1058                                    (double)getHeight())+
//1059                                    (getHeight()/2) );
//1060    */
//1061                    double yMag =
//1062                            //Math.cos(angle)*
//1063                            gain*
//1064                            signalSize*
//1065                            //getScalingFactor(sampleProgress)*
//1066                            (double)getHeight()/2;
//1067    
//1068                    double angleFactor = //Math.tan(angle)/120;
//1069                            (Math.atan(angle)*2)/Math.PI;
//1070    /*                      (angle==0 ?
//1071                            0
//1072                            :
//1073                            (angle/Math.abs(angle))* // gives the sign
//1074                            Math.log10( Math.abs( Math.tan( angle ) ))
//1075                            );*/
//1076                    int xScaled = (int)
//1077    /*                      (
//1078                            (
//1079                            Math.sin(angle)*
//1080                            yMag*
//1081                            getWidth()
//1082                            )
//1083                            //                      *
//1084                            //(Math.abs(signalSize)/signalSize)
//1085                            */
//1086                            //(angle*(2d/Math.PI)*getWidth()/4d)
//1087                            (int)
//1088                            (
//1089                            //(angleFactor*getWidth()/2d)
//1090                            ((angle*getWidth())/Math.PI)
//1091                            //*Math.log( Math.abs((Math.tan(angle))) )
//1092                            )
//1093    +
//1094                            (getWidth()/2);
//1095    logger.log(Level.FINEST, "xScaled: " + xScaled);
//1096                    int yScaled = (int)
//1097                            (
//1098                                    yMag
//1099                            )+
//1100                            (getHeight()/2);
//1101    
//1102                    if (sampleProgress==0) {
//1103                            gp.moveTo( xScaled, yScaled );
//1104                    } else {
//1105    /*                      if (lastX2>0) {
//1106                                    gp.moveTo(lastX2, lastY2);
//1107                                    gp.quadTo(
//1108                                            (float)lastX1,
//1109                                            (float)lastY1,
//1110                                            (float)xScaled,
//1111                                            (float)yScaled
//1112                                            );
//1113                            } else {*/
//1114                                    gp.lineTo( xScaled, yScaled );
//1115                            //}
//1116                    }
//1117            }
//1118    
//1119    
//1120            /** Returns the arctan of the gradient. */
//1121            public double getGradientToAngle(
//1122                    double height1,
//1123                    double height2) {
//1124                    return Math.atan((height2-height1)
//1125                            *gradientGain*gradientScaling);
//1126            }
//1127    
//1128            /** This provides a scaling factor intended to allow
//1129            both ends of the lissajous trace to align.  */
//1130            public double getScalingFactor(int progress) {
//1131                    double progressFraction = ((double)sampleSize-(double)progress)/(double)sampleSize;
//1132                    double height  = (double)getHeight();
//1133                    return (height+(progressFraction*zoomStep))/height;
//1134            }
//1135    
//1136            /** Converts a single frame of audio bytes to signed doubles
//1137            ranging from -1 to 1. It will produce one sample value for
//1138            each channel.
//1139    
//1140            Though frame will usually be two channels - stereo, it might (theoretically)
//1141            range from one - mono, to five - for the 5.1 channel sound
//1142            supported by some video formats.
//1143    
//1144            This method is quite fragile in that it presumes a
//1145            stereo, 16 bit, little-endian audio signal.  It is
//1146            expressed in 4 bytes, arranged as follows.
//1147    
//1148             | byte index | Channel | L/S |
//1149             |____________________________|
//1150             |     0      |    1    |  S  |
//1151             |     1      |    1    |  L  |
//1152             |     2      |    2    |  S  |
//1153             |     3      |    2    |  L  |
//1154    
//1155            No other configurations available for further testing.
//1156    
//1157            Update 1.  After hooking directly into AudioInputStream's,
//1158            we were able to test and correct for a lot of different
//1159            sound stream types.  This version supports both of
//1160            - big/little endian
//1161            - 16 bit or 8 bit
//1162            - stereo or mono
//1163            - signed or unsigned
//1164    
//1165            To correctly plot the trace, the encoding must be PCM, but it
//1166            seems the AudioSystem prefers PCM when it comes to playing a
//1167            sound in any case, and it provides methods to to that.
//1168    
//1169            @param renderBuffer bytearray The bytes of a single audio frame
//1170            @return An array of doubles ranging from -1 to 1, representing
//1171            the audio signal strength of a single frame sample.
//1172            */
//1173            public double[] frameToSignedDoubles(byte[] renderBuffer) {
//1174                    double[] d = new double[channels];
//1175                    double divisor = Math.pow(2,bitDepth-1);
//1176                    if (signed) {
//1177                            if (bitDepth/8==2) {
//1178                                    if (bigEndian) {
//1179                                            for (int cc = 0; cc < channels; cc++) {
//1180                                                    d[cc] = (renderBuffer[cc*2]*256 + (renderBuffer[cc*2+1] & 0xFF))/divisor;
//1181                                            }
//1182                                    } else {
//1183                                            for (int cc = 0; cc < channels; cc++) {
//1184                                                    d[cc] = (renderBuffer[cc*2+1]*256 + (renderBuffer[cc*2] & 0xFF))/divisor;
//1185                                            }
//1186                                    }
//1187                            } else {
//1188                                    for (int cc = 0; cc < channels; cc++) {
//1189                                            d[cc] = (renderBuffer[cc] & 0xFF)/divisor;
//1190                                    }
//1191                            }
//1192                    } else {
//1193                            if (bitDepth/8==2) {
//1194                                    if (bigEndian) {
//1195                                            for (int cc = 0; cc < channels; cc++) {
//1196                                                    d[cc] = (renderBuffer[cc*2]*256 + (renderBuffer[cc*2+1] - 0x80))/divisor;
//1197                                            }
//1198                                    } else {
//1199                                            for (int cc = 0; cc < channels; cc++) {
//1200                                                    d[cc] = (renderBuffer[cc*2+1]*256 + (renderBuffer[cc*2] - 0x80))/divisor;
//1201                                            }
//1202                                    }
//1203                            } else {
//1204                                    for (int cc = 0; cc < channels; cc++) {
//1205                                            if ( renderBuffer[cc]>0 ) {
//1206                                                    d[cc] = (renderBuffer[cc] - 0x80)/divisor;
//1207                                            } else {
//1208                                                    d[cc] = (renderBuffer[cc] + 0x80)/divisor;
//1209                                            }
//1210                                    }
//1211                            }
//1212                    }
//1213    
//1214                    return d;
//1215            }
//1216    }
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
