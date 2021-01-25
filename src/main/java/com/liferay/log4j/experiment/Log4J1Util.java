package com.liferay.log4j.experiment;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;


import java.net.URL;

public class Log4J1Util {

	public static void printTestLogs() {
		Logger logger = LogManager.getLogger(Log4J1Util.class);

		logger.warn("Hello World");
	}

	public static void configLog4J1(URL url) {
		DOMConfigurator domConfigurator = new DOMConfigurator();

		domConfigurator.doConfigure(url, LogManager.getLoggerRepository());
	}

	static {
		ClassLoader classLoader = Main.class.getClassLoader();

		configLog4J1(classLoader.getResource("portal-log4j1.xml"));
	}

}