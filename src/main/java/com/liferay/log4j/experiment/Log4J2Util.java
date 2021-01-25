package com.liferay.log4j.experiment;

import com.liferay.log4j.experiment.log4j2.CentralizedConfiguration;
import com.liferay.log4j.experiment.log4j2.CentralizedConfigurator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.AbstractConfiguration;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.xml.XmlConfiguration;
import org.apache.logging.log4j.core.config.xml.XmlConfigurationFactory;
import org.apache.logging.log4j.core.impl.Log4jContextFactory;
import org.apache.logging.log4j.core.selector.BasicContextSelector;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class Log4J2Util {

	public static void printTestLogs(String message) {
		Logger logger = LogManager.getLogger(Log4J2Util.class);

		logger.warn(message);
	}

	public static void configLog4J2(URL url) {
		try (InputStream inputStream = url.openStream()) {
			XmlConfiguration xmlConfiguration = new XmlConfiguration(
				_loggerContext, new ConfigurationSource(inputStream, url));

			_centralizedConfigurator.addConfiguration(xmlConfiguration);
		}
		catch (IOException ioException) {
			ioException.printStackTrace();
		}
	}

	private static final LoggerContext _loggerContext;
	private static final CentralizedConfigurator _centralizedConfigurator;

	static {
		LogManager.setFactory(
			new Log4jContextFactory(new BasicContextSelector()));

		ClassLoader classLoader = Log4J2Util.class.getClassLoader();

		URL url = classLoader.getResource("portal-log4j2.xml");

		try (InputStream inputStream = url.openStream()) {
			_loggerContext = Configurator.initialize(
				classLoader, new ConfigurationSource(inputStream, url));

			AbstractConfiguration configuration =
				(AbstractConfiguration)_loggerContext.getConfiguration();

			CentralizedConfigurator centralizedConfigurator =
				new CentralizedConfigurator(_loggerContext, configuration);

			_loggerContext.onChange(centralizedConfigurator);

			_centralizedConfigurator = centralizedConfigurator;
		}
		catch (IOException ioException) {
			throw new ExceptionInInitializerError(ioException);
		}
	}

}