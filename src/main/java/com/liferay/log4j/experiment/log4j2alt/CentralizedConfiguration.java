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

		_aggregateAttributes(configuration);
		_aggregateProperties(configuration);
		_aggregateFilters(configuration);
		_aggregateScripts(configuration);
		_aggregateAppenders(configuration);
		_aggregateLoggerConfigs(configuration);

		_updateLoggers();
	}

	@Override
	public void start() {
		setStarted();
	}

	private void _aggregateAppenders(AbstractConfiguration configuration) {

		// DefaultMergeStrategy:
		// Appenders are aggregated.
		// Appenders with the same name are replaced by those in later
		// configurations, including all of the Appender's subcomponents.

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

	private void _aggregateAttributes(AbstractConfiguration configuration) {

		// DefaultMergeStrategy:
		// Aggregates the global configuration attributes with those in later
		// configurations replacing those in previous configurations with the
		// exception that the highest status level and the lowest
		// monitorInterval greater than 0 will be used.

	}

	private void _aggregateFilters(AbstractConfiguration configuration) {

		// DefaultMergeStrategy:
		// Filters are aggregated under a CompositeFilter if more than one
		// Filter is defined. Since Filters are not named duplicates may be
		// present.

	}

	private void _aggregateLoggerConfigs(AbstractConfiguration configuration) {

		// TODO: implement merge strategy
		// DefaultMergeStrategy:
		// Loggers are all aggregated.
		// Logger attributes are individually merged with duplicates being
		// replaced by those in later configurations.
		// Appender references on a Logger are aggregated with duplicates being
		// replaced by those in later configurations.
		// Filters on a Logger are aggregated under a CompositeFilter if more
		// than one Filter is defined. Since Filters are not named duplicates
		// may be present.
		// Filters under Appender references included or discarded depending on
		// whether their parent Appender reference is kept or discarded.

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

			if (currentLoggerConfig != null) {
				removeLogger(newLoggerConfigEntry.getKey());

				currentLoggerConfig.stop();
			}

			LoggerConfig newLoggerConfig = newLoggerConfigEntry.getValue();

			addLogger(newLoggerConfigEntry.getKey(), newLoggerConfig);

			newLoggerConfig.start();
		}
	}

	private void _aggregateProperties(AbstractConfiguration configuration) {

		// DefaultMergeStrategy:
		// Properties from all configurations are aggregated.
		// Duplicate properties replace those in previous configurations.

	}

	private void _aggregateScripts(AbstractConfiguration configuration) {

		// DefaultMergeStrategy:
		// Scripts and ScriptFile references are aggregated.
		// Duplicate definitions replace those in previous configurations.

	}

	private void _updateLoggers() {

		// TODO: lock the configLock in LoggerContext

		LoggerContext loggerContext = getLoggerContext();

		loggerContext.updateLoggers();
	}

}