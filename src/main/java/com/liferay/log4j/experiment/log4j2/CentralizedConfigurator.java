package com.liferay.log4j.experiment.log4j2;

import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.AbstractConfiguration;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.Node;
import org.apache.logging.log4j.core.config.Reconfigurable;
import org.apache.logging.log4j.core.config.composite.DefaultMergeStrategy;
import org.apache.logging.log4j.core.config.composite.MergeStrategy;

import java.util.ArrayList;
import java.util.List;

public class CentralizedConfigurator implements Reconfigurable {

	public CentralizedConfigurator(
		LoggerContext loggerContext, AbstractConfiguration coreConfiguration) {

		_loggerContext = loggerContext;

		_rootNode = coreConfiguration.getRootNode();

		_coreConfiguration = coreConfiguration;

		_configurations.add(coreConfiguration);
	}

	public void addConfiguration(AbstractConfiguration configuration) {
		_mergeConfiguration(configuration);

		_configurations.add(configuration);

		_loggerContext.onChange(this);
	}

	public void addConfigurations(List<AbstractConfiguration> configurations) {
		for (AbstractConfiguration configuration : configurations) {
			_mergeConfiguration(configuration);
		}

		_configurations.addAll(configurations);

		_loggerContext.onChange(this);
	}

	@Override
	public Configuration reconfigure() {
		return new CentralizedConfiguration(_loggerContext, _rootNode);
	}

	private void _mergeConfiguration(AbstractConfiguration configuration) {
		_mergeStrategy.mergeRootProperties(_rootNode, configuration);

		_setupConfiguration(configuration);

		final Node sourceRootNode = configuration.getRootNode();

		_mergeStrategy.mergConfigurations(
			_rootNode, sourceRootNode, _coreConfiguration.getPluginManager());
	}

	private void _setupConfiguration(AbstractConfiguration configuration) {
		configuration.setPluginManager(_coreConfiguration.getPluginManager());

		configuration.setScriptManager(_coreConfiguration.getScriptManager());

		configuration.setup();
	}

	private final AbstractConfiguration _coreConfiguration;
	private final List<AbstractConfiguration> _configurations =
		new ArrayList<>();
	private final LoggerContext _loggerContext;
	private final MergeStrategy _mergeStrategy = new DefaultMergeStrategy();
	private final Node _rootNode;

}