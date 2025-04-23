/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.agent.cloud.log;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;
import com.clustercontrol.agent.util.AgentProperties;

public class CloudLogMonitorProperty {
	/*
	 * クラウドログ監視固有のエージェントプロパティ
	 */
	private static final String TMP_FILE_SIZE = "xcloud.log.tmpfile.max.size";
	private static final String TMP_FILE_ENCODE = "xcloud.log.tmpfile.encode";
	private static final String TIME_OFFSET = "xcloud.log.time.offset";
	private static final String VALID_DURATION = "xcloud.log.tmpfile.valid.duration";
	private static final String MAX_LOGSIZE = "xcloud.log.max.accuire.size";
	private static final String CON_TIMEOUT = "xcloud.log.connection.timeout";
	private static final String RETRY_THRESHOLD = "xcloud.log.connection.timeout.retry.threshold";
	private static final String MISSING_ENABLE = "xcloud.log.acquire.missing.enable";
	private static final String MISSING_PERIOD = "xcloud.log.acquire.missing.period";
	private static final String AWAIT_TERMINATION =  "xcloud.log.await.termination.period";
	private static final String AWS_CLIENT_RETRY = "xcloud.aws.client.connection.retry";
	private static final String AWS_MAX_CONNECTION = "xcloud.aws.client.config.maxConnections";
	private static final String AWS_CLIENT_PROTOCOL = "xcloud.aws.client.config.protocol";
	private static final String AWS_CLIENT_PROXY_DOMAIN = "xcloud.aws.client.config.proxyDomain";
	private static final String AWS_CLIENT_PROXY_HOST = "xcloud.aws.client.config.proxyHost";
	private static final String AWS_CLIENT_PROXY_PASSWORD = "xcloud.aws.client.config.proxyPassword";
	private static final String AWS_CLIENT_PROXY_PORT = "xcloud.aws.client.config.proxyPort";
	private static final String AWS_CLIENT_PROXY_USERNAME = "xcloud.aws.client.config.proxyUsername";
	private static final String AWS_CLIENT_PROXY_WORKSTATION = "xcloud.aws.client.config.proxyWorkstation";
	private static final String AZURE_CLIENT_RETRY = "xcloud.Azure.client.connection.retry";
	private static final String AZURE_CLIENT_PROXY_HOST = "xcloud.Azure.client.config.proxyHost";
	private static final String AZURE_CLIENT_PROXY_PASSWORD = "xcloud.Azure.client.config.proxyPassword";
	private static final String AZURE_CLIENT_PROXY_PORT = "xcloud.Azure.client.config.proxyPort";
	private static final String AZURE_CLIENT_PROXY_USERNAME = "xcloud.Azure.client.config.proxyUsername";
	private static final String AZURE_RETRY_INTERVAL = "xcloud.Azure.connection.timeout.retry.internval";

	private long maxFileSize;
	private String tmpFileEncode;
	private long timeOffset;
	private long validDuration;
	private long maxLogsize;
	private int connectionTimeout;
	private int retryThreshold;
	private boolean isMissingEnable;
	private long missingPeriod;
	private long awaitTerminationPeriod;
	private int aws_max_connection;
	private int aws_client_retry;
	private String aws_client_protocol;
	private String aws_client_proxy_domain;
	private String aws_client_proxy_host;
	private String aws_client_proxy_password;
	private int aws_client_proxy_port;
	private String aws_client_proxy_username;
	private String aws_client_proxy_workstation;

	private int azure_client_retry;
	private String azure_client_proxy_host;
	private String azure_client_proxy_password;
	private int azure_client_proxy_port;
	private String azure_client_proxy_username;
	private int azure_retry_interval;

	private static CloudLogMonitorProperty instance = new CloudLogMonitorProperty();
	private Log m_log = LogFactory.getLog(CloudLogMonitorProperty.class);

	/*
	 * エージェントプロパティの読込
	 */
	private CloudLogMonitorProperty() {

		try {
			maxFileSize = Long.parseLong(AgentProperties.getProperty(TMP_FILE_SIZE, "5000000"));
		} catch (Exception e) {
			maxFileSize = 5000000;
			m_log.warn("CloudLogMonitorProperty(): Failed loading" + TMP_FILE_SIZE + ". Use default");
		}
		tmpFileEncode = AgentProperties.getProperty(TMP_FILE_ENCODE, "UTF-8");
		try {
			timeOffset = Long.parseLong(AgentProperties.getProperty(TIME_OFFSET, "0"));
		} catch (Exception e) {
			timeOffset = 0;
			m_log.warn("CloudLogMonitorProperty(): Failed loading" + TIME_OFFSET + ". Use default");
		}
		try {
			validDuration = Long.parseLong(AgentProperties.getProperty(VALID_DURATION, "86400000"));
		} catch (Exception e) {
			validDuration = 86400000;
			m_log.warn("CloudLogMonitorProperty(): Failed loading" + VALID_DURATION + ". Use default");
		}
		try {
			maxLogsize = Long.parseLong(AgentProperties.getProperty(MAX_LOGSIZE, "10000000"));
		} catch (Exception e) {
			maxLogsize = 10000000;
			m_log.warn("CloudLogMonitorProperty(): Failed loading" + MAX_LOGSIZE + ". Use default");
		}
		try {
			connectionTimeout = Integer.parseInt(AgentProperties.getProperty(CON_TIMEOUT, "30000"));
		} catch (Exception e) {
			connectionTimeout = 30000;
			m_log.warn("CloudLogMonitorProperty(): Failed loading" + CON_TIMEOUT + ". Use default");
		}
		try {
			retryThreshold = Integer.parseInt(AgentProperties.getProperty(RETRY_THRESHOLD, "2"));
		} catch (Exception e) {
			retryThreshold = 2;
			m_log.warn("CloudLogMonitorProperty(): Failed loading" + RETRY_THRESHOLD + ". Use default");
		}
		isMissingEnable = Boolean.parseBoolean(AgentProperties.getProperty(MISSING_ENABLE, "true"));
		try {
			missingPeriod = Long.parseLong(AgentProperties.getProperty(MISSING_PERIOD, "3600000"));
		} catch (Exception e) {
			missingPeriod = 3600000;
			m_log.warn("CloudLogMonitorProperty(): Failed loading" + MISSING_PERIOD + ". Use default");
		}
		try {
			awaitTerminationPeriod = Long.parseLong(AgentProperties.getProperty(AWAIT_TERMINATION, "15000"));
		} catch (Exception e) {
			awaitTerminationPeriod = 15000;
			m_log.warn("CloudLogMonitorProperty(): Failed loading" + AWAIT_TERMINATION + ". Use default");
		}
		try {
			aws_max_connection = Integer.parseInt(AgentProperties.getProperty(AWS_MAX_CONNECTION, "50"));
		} catch (Exception e) {
			aws_max_connection = 50;
			m_log.warn("CloudLogMonitorProperty(): Failed loading" + AWS_MAX_CONNECTION + ". Use default");
		}
		try {
			aws_client_retry = Integer.parseInt(AgentProperties.getProperty(AWS_CLIENT_RETRY, "2"));

		} catch (Exception e) {
			aws_client_retry = 2;
			m_log.warn("CloudLogMonitorProperty(): Failed loading" + AWS_CLIENT_RETRY + ". Use default");
		}
		aws_client_protocol = AgentProperties.getProperty(AWS_CLIENT_PROTOCOL, "https");
		aws_client_proxy_domain = AgentProperties.getProperty(AWS_CLIENT_PROXY_DOMAIN, "");
		aws_client_proxy_host = AgentProperties.getProperty(AWS_CLIENT_PROXY_HOST, "");
		aws_client_proxy_password = AgentProperties.getProperty(AWS_CLIENT_PROXY_PASSWORD, "");
		try {
			aws_client_proxy_port = Integer.parseInt(AgentProperties.getProperty(AWS_CLIENT_PROXY_PORT, "0"));
			
			// プロキシのポート番号に負数が指定された場合、
			// AWS SDK for Javaの内部処理がOSの環境変数"HTTPS_PROXY"のポート番号を参照しにいく
			// "HTTPS_PROXY"の利用を避けるため、0に変換する
			// プロキシ番号に0を指定すると、AWS SDK for Javaは、"HTTPS_PROXY"を参照しなくなる
			if (aws_client_proxy_port < 0) {
				aws_client_proxy_port = 0;
				m_log.info("CloudLogMonitorProperty(): Negative number specified for proxy port is converted to 0 to disable proxy.");
			}
			
		} catch (Exception e) {
			aws_client_proxy_port = 0;
			m_log.warn("CloudLogMonitorProperty(): Failed loading" + AWS_CLIENT_PROXY_PORT + ". Use default");
		}
		aws_client_proxy_username = AgentProperties.getProperty(AWS_CLIENT_PROXY_USERNAME, "");
		aws_client_proxy_workstation = AgentProperties.getProperty(AWS_CLIENT_PROXY_WORKSTATION, "");

		try {
			azure_client_retry = Integer.parseInt(AgentProperties.getProperty(AZURE_CLIENT_RETRY, "2"));

		} catch (Exception e) {
			azure_client_retry = 2;
			m_log.warn("CloudLogMonitorProperty(): Failed loading" + AZURE_CLIENT_RETRY + ". Use default");
		}
		azure_client_proxy_host = AgentProperties.getProperty(AZURE_CLIENT_PROXY_HOST, "");
		azure_client_proxy_password = AgentProperties.getProperty(AZURE_CLIENT_PROXY_PASSWORD, "");
		try {
			azure_client_proxy_port = Integer.parseInt(AgentProperties.getProperty(AZURE_CLIENT_PROXY_PORT, "-1"));

		} catch (Exception e) {
			azure_client_proxy_port = -1;
			m_log.warn("CloudLogMonitorProperty(): Failed loading" + AZURE_CLIENT_PROXY_PORT + ". Use default");
		}
		azure_client_proxy_username = AgentProperties.getProperty(AZURE_CLIENT_PROXY_USERNAME, "");
		try {
			azure_retry_interval = Integer.parseInt(AgentProperties.getProperty(AZURE_RETRY_INTERVAL, "1000"));

		} catch (Exception e) {
			azure_retry_interval = 1000;
			m_log.warn("CloudLogMonitorProperty(): Failed loading" + AZURE_RETRY_INTERVAL + ". Use default");
		}
	}

	/**
	 * AWSのクライアント設定を作成 クライアント設定はAgent.propertiesで指定する
	 * 
	 * @return
	 */
	public ClientConfiguration createClientConfiguration() {
		ClientConfiguration conf = new ClientConfiguration();
		conf.setConnectionTimeout(connectionTimeout);
		conf.setMaxConnections(aws_max_connection);
		conf.setMaxErrorRetry(aws_client_retry);
		conf.setProtocol(Protocol.HTTP.toString().equals(aws_client_protocol) ? Protocol.HTTP : Protocol.HTTPS);
		conf.setProxyDomain(aws_client_proxy_domain);
		conf.setProxyHost(aws_client_proxy_host);
		conf.setProxyPassword(aws_client_proxy_password);
		conf.setProxyPort(aws_client_proxy_port);
		conf.setProxyUsername(aws_client_proxy_username);
		conf.setProxyWorkstation(aws_client_proxy_workstation);
		conf.setSocketTimeout(connectionTimeout);
		return conf;
	}

	public long getMaxFileSize() {
		return maxFileSize;
	}

	public String getTmpFileEncode() {
		return tmpFileEncode;
	}

	public long getTimeOffset() {
		return timeOffset;
	}

	public long getValidDuration() {
		return validDuration;
	}

	public long getMaxLogsize() {
		return maxLogsize;
	}

	public int getConnectionTimeout() {
		return connectionTimeout;
	}

	public int getRetryThreshold() {
		return retryThreshold;
	}

	public boolean isMissingEnable() {
		return isMissingEnable;
	}

	public long getMissingPeriod() {
		return missingPeriod;
	}


	public long getAwaitTerminationPeriod(){
		return awaitTerminationPeriod ;
	}
	
	public int getAws_max_connection() {
		return aws_max_connection;
	}

	public String getAws_client_protocol() {
		return aws_client_protocol;
	}

	public String getAws_client_proxy_domain() {
		return aws_client_proxy_domain;
	}

	public String getAws_client_proxy_host() {
		return aws_client_proxy_host;
	}

	public String getAws_client_proxy_password() {
		return aws_client_proxy_password;
	}

	public int getAws_client_proxy_port() {
		return aws_client_proxy_port;
	}

	public String getAws_client_proxy_username() {
		return aws_client_proxy_username;
	}

	public String getAws_client_proxy_workstation() {
		return aws_client_proxy_workstation;
	}

	public String getAzure_client_proxy_host() {
		return azure_client_proxy_host;
	}

	public String getAzure_client_proxy_password() {
		return azure_client_proxy_password;
	}

	public int getAzure_client_proxy_port() {
		return azure_client_proxy_port;
	}

	public String getAzure_client_proxy_username() {
		return azure_client_proxy_username;
	}

	public int getAzure_retry_interval() {
		return azure_retry_interval;
	}

	public int getAwsClientRetry() {
		return aws_client_retry;
	}

	public int getAzureClientRetry() {
		return azure_client_retry;
	}

	public static CloudLogMonitorProperty getInstance() {
		return instance;
	}

	public String commonProps() {
		return String.format(
				"xcloud.log.tmpfile.max.size: %s" + "xcloud.log.tmpfile.encode: %s" + "%nxcloud.log.time.offset: %s"
						+ "%nxcloud.log.max.accuire.size: %s" + "%nxcloud.log.connection.timeout: %s"
						+ "%nxcloud.log.connection.timeout.retry.threshold: %s"
						+ "%nxcloud.log.acquire.missing.enable: %s" + "%nxcloud.log.acquire.missing.period: %s",
				maxFileSize, tmpFileEncode, timeOffset, maxLogsize, connectionTimeout, retryThreshold, isMissingEnable,
				missingPeriod);

	}

	public String awsProps() {
		return String.format(
				"xcloud.aws.client.config.maxConnections: %s" + "%nxcloud.aws.client.config.protocol: %s"
						+ "%nxcloud.aws.client.config.proxyDomain: %s" + "%nxcloud.aws.client.config.proxyHost: %s"
						+ "%nxcloud.aws.client.config.proxyPassword: %s" + "%nxcloud.aws.client.config.proxyPort: %s"
						+ "%nud.aws.client.config.proxyUsername: %s" + "%nxcloud.aws.client.config.proxyWorkstation: %s"
						+ "%nxcloud.aws.client.connection.retry: %s",
				aws_max_connection, aws_client_protocol, aws_client_proxy_domain, aws_client_proxy_host,
				aws_client_proxy_password, aws_client_proxy_port, aws_client_proxy_username,
				aws_client_proxy_workstation, aws_client_retry);
	}

	public String azureProps() {
		return String.format("xcloud.Azure.client.config.proxyHost: %s"
				+ "%nxcloud.Azure.client.config.proxyPassword: %s" + "%nxcloud.Azure.client.config.proxyPort: %s"
				+ "%nxcloud.Azure.client.config.proxyUsername: %s"
				+ "%nxcloud.Azure.connection.timeout.retry.internval %s" + "%nxcloud.Azure.client.connection.retry: %s",
				azure_client_proxy_host, azure_client_proxy_password, azure_client_proxy_port,
				azure_client_proxy_username, azure_retry_interval, azure_client_retry);
	}
}
