package com.liferay.log4j.experiment;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class Main {

	public static void main(String[] args) {

		ClassLoader classLoader = Main.class.getClassLoader();

		// 1.x

		Log4J1Util.printTestLogs();

		long timestamp = System.currentTimeMillis();

		Log4J1Util.configLog4J1(
			classLoader.getResource("log4j1/portal-log4j1-ext.xml"));

		for (int i = 0; i < 10000; i++) {
			Log4J1Util.configLog4J1(_getLog4J1Ext(i));
		}

		timestamp = System.currentTimeMillis() - timestamp;

		System.out.println(timestamp);

		Log4J1Util.printTestLogs();

		// 2.x

		Log4J2Util.printTestLogs("1");

		timestamp = System.currentTimeMillis();

		Log4J2Util.configLog4J2(
			classLoader.getResource("log4j2/portal-log4j2-ext.xml"));

		for (int i = 0; i < 10000; i++) {
			Log4J2Util.configLog4J2(_getLog4J2Ext(i));
		}

		timestamp = System.currentTimeMillis() - timestamp;

		System.out.println(timestamp);

		Log4J2Util.printTestLogs("2");
	}

	private static InputStream _getLog4J1Ext(int i) {
		StringBuilder sb = new StringBuilder();

		sb.append("<?xml version=\"1.0\"?>");
		sb.append("<!DOCTYPE log4j:configuration SYSTEM \"log4j.dtd\">");
		sb.append("<log4j:configuration xmlns:log4j=");
		sb.append("\"http://jakarta.apache.org/log4j/\">");

		sb.append("<category name=\"");
		sb.append(i);
		sb.append("\"><priority value=\"INFO\" />");

		sb.append("</category></log4j:configuration>");

		return new ByteArrayInputStream(sb.toString().getBytes());
	}

	private static InputStream _getLog4J2Ext(int i) {
		StringBuilder sb = new StringBuilder();

		sb.append("<?xml version=\"1.0\"?>");
		sb.append("<Configuration><Loggers>");

		sb.append("<Logger level=\"INFO\" name=\"");
		sb.append(i);
		sb.append("\" />");

		sb.append("</Loggers></Configuration>");

		return new ByteArrayInputStream(sb.toString().getBytes());
	}

}