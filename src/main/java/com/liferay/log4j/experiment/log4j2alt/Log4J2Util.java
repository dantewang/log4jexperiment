package com.liferay.log4j.experiment.log4j2alt;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.xml.XmlConfiguration;
import org.apache.logging.log4j.core.impl.Log4jContextFactory;
import org.apache.logging.log4j.core.selector.BasicContextSelector;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class Log4J2Util {

	public static void printTestLogs(String message) {
		Logger logger = LogManager.getLogger(Log4J2Util.class);

		logger.error(message);
		logger.warn(message);
	}

	public static void configLog4J2(URL url) {
		try (InputStream inputStream = url.openStream()) {
			XmlConfiguration xmlConfiguration = new XmlConfiguration(
				_loggerContext, new ConfigurationSource(inputStream, url));

			_centralizedConfiguration.addConfiguration(xmlConfiguration);
		}
		catch (IOException ioException) {
			ioException.printStackTrace();
		}
	}

	public static void configLog4J2(InputStream inputStream) {
		try {
			XmlConfiguration xmlConfiguration = new XmlConfiguration(
				_loggerContext, new ConfigurationSource(inputStream));

			_centralizedConfiguration.addConfiguration(xmlConfiguration);
		}
		catch (IOException ioException) {
			ioException.printStackTrace();
		}
	}

	private static final LoggerContext _loggerContext;
	private static final CentralizedConfiguration _centralizedConfiguration;

	static {
		LogManager.setFactory(
			new Log4jContextFactory(new BasicContextSelector()));

		ClassLoader classLoader = Log4J2Util.class.getClassLoader();

		URL url = classLoader.getResource("portal-log4j2.xml");

		LoggerContext loggerContext = (LoggerContext)LogManager.getContext();

		try (InputStream inputStream = url.openStream()) {
			XmlConfiguration xmlConfiguration = new XmlConfiguration(
				loggerContext, new ConfigurationSource(inputStream, url));

			CentralizedConfiguration centralizedConfiguration =
				new CentralizedConfiguration(loggerContext);

			loggerContext.setConfiguration(centralizedConfiguration);

			centralizedConfiguration.addConfiguration(xmlConfiguration);

			_loggerContext = loggerContext;
			_centralizedConfiguration = centralizedConfiguration;
		}
		catch (IOException ioException) {
			throw new ExceptionInInitializerError(ioException);
		}
	}

}