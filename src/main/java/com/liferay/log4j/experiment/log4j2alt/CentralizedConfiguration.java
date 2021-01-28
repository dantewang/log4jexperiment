package com.liferay.log4j.experiment.log4j2alt;

import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.AbstractConfiguration;
import org.apache.logging.log4j.core.config.AppenderRef;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.LoggerConfig;

import java.util.Map;
import java.util.Objects;

public class CentralizedConfiguration extends AbstractConfiguration {

	public CentralizedConfiguration(LoggerContext loggerContext) {
		super(loggerContext, ConfigurationSource.COMPOSITE_SOURCE);
	}

	public void addConfiguration(AbstractConfiguration configuration) {
		if (configuration.getState() != State.INITIALIZING) {
			return;
		}

		configuration.initialize();

		// DefaultMergeStrategy:
		// Properties from all configurations are aggregated.
		// Duplicate properties replace those in previous configurations.

		Map<String, String> properties = getProperties();

		properties.putAll(configuration.getProperties());

		// DefaultMergeStrategy:
		// Filters are aggregated under a CompositeFilter if more than one
		// Filter is defined. Since Filters are not named duplicates may be
		// present.

		addFilter(configuration.getFilter());

		_aggregateScripts(configuration);
		_aggregateAppenders(configuration);
		_aggregateLoggerConfigs(configuration);

		_updateLoggers();
	}

	@Override
	public void start() {
		LoggerConfig rootLoggerConfig = getRootLogger();

		rootLoggerConfig.start();

		addLogger("", rootLoggerConfig);

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

	private void _aggregateLoggerConfigs(AbstractConfiguration configuration) {

		// DefaultMergeStrategy:
		// Loggers are all aggregated.
		// See _aggregateLoggerConfig(LoggerConfig, LoggerConfig)

		_aggregateLoggerConfig(getRootLogger(), configuration.getRootLogger());

		Map<String, LoggerConfig> newLoggerConfigs = configuration.getLoggers();

		for (Map.Entry<String, LoggerConfig> newLoggerConfigEntry :
			newLoggerConfigs.entrySet()) {

			String name = newLoggerConfigEntry.getKey();

			// Skip root logger

			if (Objects.equals(name, "")) {
				continue;
			}

			LoggerConfig currentLoggerConfig = getLoggerConfig(name);

			LoggerConfig newLoggerConfig = newLoggerConfigEntry.getValue();

			if (currentLoggerConfig != null) {
				_aggregateLoggerConfig(currentLoggerConfig, newLoggerConfig);
			}

			addLogger(name, newLoggerConfig);

			newLoggerConfig.start();
		}
	}

	private void _aggregateLoggerConfig(
		LoggerConfig currentLoggerConfig, LoggerConfig newLoggerConfig) {

		// Logger attributes are individually merged with duplicates being
		// replaced by those in later configurations.

		currentLoggerConfig.setLevel(newLoggerConfig.getLevel());
		currentLoggerConfig.setAdditive(newLoggerConfig.isAdditive());

		// Filters on a Logger are aggregated under a CompositeFilter if more
		// than one Filter is defined. Since Filters are not named duplicates
		// may be present.

		currentLoggerConfig.addFilter(newLoggerConfig.getFilter());

		// Appender references on a Logger are aggregated with duplicates being
		// replaced by those in later configurations.
		// Filters under Appender references included or discarded depending on
		// whether their parent Appender reference is kept or discarded.

		Map<String, Appender> currentLoggerConfigAppenders =
			newLoggerConfig.getAppenders();

		Map<String, Appender> newLoggerConfigAppenders =
			newLoggerConfig.getAppenders();

		for (AppenderRef appenderRef : newLoggerConfig.getAppenderRefs()) {
			Appender appender = currentLoggerConfigAppenders.get(
				appenderRef.getRef());

			// Existing appender must be removed first as the internal data
			// structure holding appenders does not allow replacing an existing
			// appender

			if (appender != null) {
				currentLoggerConfig.removeAppender(appenderRef.getRef());
			}

			currentLoggerConfig.addAppender(
				newLoggerConfigAppenders.get(appenderRef.getRef()),
				appenderRef.getLevel(), appenderRef.getFilter());
		}
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