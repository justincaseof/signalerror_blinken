package vu.de.signalerror.base;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.Properties;

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.log.LogEntry;
import org.osgi.service.log.LogListener;
import org.osgi.service.log.LogReaderService;
import org.osgi.service.log.LogService;

public class ApplicationMaster implements LogListener
{
	private static Logger logger;

	private ComponentContext componentContext;

	private File workDirBase;

	private File configDir;

	private File logDir;

	private CompositeConfiguration applicationConfig;

	private PropertiesConfiguration userSettings;

	protected void activate( ComponentContext componentContext )
	{
		System.err.println("[" + getClass().getName() + "] " + "Activating...");
		this.componentContext = componentContext;
		try
		{
			String workDirBaseName = System.getProperty("osgi.configuration.area");
			if (workDirBaseName.startsWith("file:"))
			{
				workDirBaseName = new URL(workDirBaseName).getPath();
			}
			workDirBase = new File(workDirBaseName);
			configDir = new File(workDirBase, "../config");
			logDir = new File(workDirBase, "../log");

			// initialize Log4J Logging
			initializeLog4JLogging();

			// initialize configuration
			initializeConfig();

			// initialize user settings
			initializeUserSettings();

			logger.debug("Activated!");
			return;
		}
		catch (Exception e)
		{
			System.err.println("Activation FAILED: ");
			e.printStackTrace();
			return;
		}
	}

	protected void deactivate( ComponentContext componentContext )
	{
		this.componentContext = null;
	}

	private void initializeLog4JLogging( )
	{
		try
		{
			String logConfigFile = getConfigDirectory().toString() + "/log4j.properties";

			StringBuffer defaultRollingLogFile = new StringBuffer();
			defaultRollingLogFile.append(getLogDirectory().getAbsolutePath());
			defaultRollingLogFile.append(File.separator);
			defaultRollingLogFile.append("default.log");
			System.setProperty("DefaultRollingLogFile", defaultRollingLogFile.toString());

			Properties logProperties = new Properties();
			logProperties.load(new FileInputStream(logConfigFile));
			// logProperties.putAll(privateLogProperties);

			PropertyConfigurator.configure(logProperties);

			ApplicationMaster.logger = Logger.getLogger(ApplicationMaster.class);

			logger.debug("Successfully initialized log4j.");
		}
		catch (Exception e)
		{
			System.err.println("Error initializing log4j:");
			e.printStackTrace();
		}
	}

	private void initializeConfig( )
	{
		this.applicationConfig = new CompositeConfiguration();
		try
		{
			applicationConfig.addConfiguration(new PropertiesConfiguration(new File(configDir, "main.properties")));
		}
		catch (ConfigurationException e)
		{
			logger.error("Could not load main properties: ", e);
		}
	}

	private void initializeUserSettings( )
	{
		try
		{
			// look if there already is a usersettings-file
			// load it or create new one.
			File userSettingsFile = new File(configDir, ".usersettings");
			if (!userSettingsFile.exists())
				userSettingsFile.createNewFile();

			userSettings = new PropertiesConfiguration(userSettingsFile);
			userSettings.setAutoSave(true);

			// append last start.
			userSettings.setProperty("laststart", new Date(System.currentTimeMillis()));
		}
		catch (IOException ioe)
		{
			logger.error("Could not write usersettings-file");
		}
		catch (ConfigurationException ce)
		{
			logger.error("Could not load main properties: ", ce);
		}
	}

	public File getLogDirectory( )
	{
		return logDir;
	}

	public File getConfigDirectory( )
	{
		return configDir;
	}

	public Configuration getConfigurtation( )
	{
		return applicationConfig;
	}

	public Configuration getUserSettings( )
	{
		return userSettings;
	}

	public void setLogReaderService( LogReaderService logReaderService )
	{
		logReaderService.addLogListener(this);
	}

	public void unsetLogReaderService( LogReaderService logReaderService )
	{
		logReaderService.removeLogListener(this);
	}

	@Override
	public void logged( LogEntry entry )
	{
		String message = entry.getMessage();
		switch (entry.getLevel())
		{
		case LogService.LOG_DEBUG:
			logger.debug(message, entry.getException());
			break;
		case LogService.LOG_INFO:
			logger.info(message, entry.getException());
			break;
		case LogService.LOG_WARNING:
			logger.warn(message, entry.getException());
			break;
		case LogService.LOG_ERROR:
			logger.error(message, entry.getException());
			break;

		default:
			logger.fatal("unrecognized LogLevel: " + entry.getLevel());
			break;
		}
	}

}
