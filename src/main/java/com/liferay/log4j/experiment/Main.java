package com.liferay.log4j.experiment;

public class Main {

	public static void main(String[] args) {

		ClassLoader classLoader = Main.class.getClassLoader();

		// 1.x

		Log4J1Util.printTestLogs();

		Log4J1Util.configLog4J1(
			classLoader.getResource("portal-log4j1-ext.xml"));

		Log4J1Util.printTestLogs();

		// 2.x

		Log4J2Util.printTestLogs("1");

		//Log4J2Util.configLog4J2(
		//	classLoader.getResource("portal-log4j2-ext.xml"));

		Log4J2Util.printTestLogs("2");
	}

}