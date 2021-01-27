package com.liferay.log4j.experiment.log4j2alt;

import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.AbstractConfiguration;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.LoggerConfig;

import java.lang.reflect.Field;
import java.util.Map;

public class CentralizedConfiguration extends AbstractConfiguration {

	public CentralizedConfiguration(LoggerContext loggerContext) {
		super(loggerContext, ConfigurationSource.COMPOSITE_SOURCE);
	}

	public void addConfiguration(AbstractConfiguration configuration) {
		if (configuration.getState() != State.INITIALIZING) {
			return;
		}

		configuration.initialize();

		// TODO: check DefaultMergeStrategy's merge policy: copied below

		/**
		 * The default merge strategy for composite configurations.
		 * <p>
		 * The default merge strategy performs the merge according to the following rules:
		 * <ol>
		 * <li>Aggregates the global configuration attributes with those in later configurations replacing those in previous
		 * configurations with the exception that the highest status level and the lowest monitorInterval greater than 0 will
		 * be used.</li>
		 * <li>Properties from all configurations are aggregated. Duplicate properties replace those in previous
		 * configurations.</li>
		 * <li>Filters are aggregated under a CompositeFilter if more than one Filter is defined. Since Filters are not named
		 * duplicates may be present.</li>
		 * <li>Scripts and ScriptFile references are aggregated. Duplicate definitions replace those in previous
		 * configurations.</li>
		 * <li>Appenders are aggregated. Appenders with the same name are replaced by those in later configurations, including
		 * all of the Appender's subcomponents.</li>
		 * <li>Loggers are all aggregated. Logger attributes are individually merged with duplicates being replaced by those
		 * in later configurations. Appender references on a Logger are aggregated with duplicates being replaced by those in
		 * later configurations. Filters on a Logger are aggregated under a CompositeFilter if more than one Filter is defined.
		 * Since Filters are not named duplicates may be present. Filters under Appender references included or discarded
		 * depending on whether their parent Appender reference is kept or discarded.</li>
		 * </ol>
		 */

		// TODO: create diff tree for those hard to merge in instances,
		//  for example, appenders has many types, and the layout can not be
		//  changed in an appender instance.
		//  new nodes and merged nodes; unchanged nodes are not included

		_mergeAppenders(configuration);
		_mergeLoggerConfigs(configuration);

		_updateLoggers();
	}

	@Override
	public void start() {
		setStarted();
	}

	private void _mergeAppenders(AbstractConfiguration configuration) {
		Map<String, Appender> appenders = getAppenders();

		Map<String, Appender> newAppenders = configuration.getAppenders();

		for (Map.Entry<String, Appender> newAppenderEntry :
				newAppenders.entrySet()) {

			Appender newAppender = newAppenderEntry.getValue();

			Appender currentAppender = appenders.put(
				newAppenderEntry.getKey(), newAppender);

			newAppender.start();

			if (currentAppender != null) {
				currentAppender.stop();
			}
		}
	}

	private void _mergeLoggerConfigs(AbstractConfiguration configuration) {
		LoggerConfig newRootLoggerConfig = configuration.getRootLogger();

		if (newRootLoggerConfig != null) {
			try {
				Field field = AbstractConfiguration.class.getDeclaredField(
					"root");

				field.setAccessible(true);

				field.set(this, newRootLoggerConfig);
			}
			catch (NoSuchFieldException | IllegalAccessException e) {
				e.printStackTrace();
			}
		}

		Map<String, LoggerConfig> newLoggerConfigs = configuration.getLoggers();

		for (Map.Entry<String, LoggerConfig> newLoggerConfigEntry :
			newLoggerConfigs.entrySet()) {

			LoggerConfig currentLoggerConfig = getLoggerConfig(
				newLoggerConfigEntry.getKey());

			// TODO: merge instead of replacement, LoggerConfig is mutable

			if (currentLoggerConfig != null) {
				removeLogger(newLoggerConfigEntry.getKey());

				currentLoggerConfig.stop();
			}

			LoggerConfig newLoggerConfig = newLoggerConfigEntry.getValue();

			addLogger(newLoggerConfigEntry.getKey(), newLoggerConfig);

			newLoggerConfig.start();
		}
	}

	private void _updateLoggers() {

		// TODO: lock the configLock in LoggerContext

		LoggerContext loggerContext = getLoggerContext();

		loggerContext.updateLoggers();
	}

}