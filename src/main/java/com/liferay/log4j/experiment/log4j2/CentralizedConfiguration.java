package com.liferay.log4j.experiment.log4j2;

import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.AbstractConfiguration;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.Node;

public class CentralizedConfiguration extends AbstractConfiguration {

	public CentralizedConfiguration(
		LoggerContext loggerContext, Node rootNode) {

		super(loggerContext, ConfigurationSource.COMPOSITE_SOURCE);

		this.rootNode = rootNode;
	}

}