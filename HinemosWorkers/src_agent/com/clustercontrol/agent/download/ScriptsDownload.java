package com.clustercontrol.agent.download;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Authenticator;
import java.net.PasswordAuthentication;

import javax.activation.DataHandler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.agent.AgentEndPointWrapper;
import com.clustercontrol.agent.EndpointManager;
import com.clustercontrol.agent.util.AgentProperties;

/**
 * クラウド管理オプションのテンプレート機能で使用されるクラス。<BR>
 * ジョブのコマンドから直接起動されるため、エージェントからは呼び出されない。
 */
public class ScriptsDownload {
	// ロガー
	private static Log m_log = LogFactory.getLog(ScriptsDownload.class);

	private static final Integer DEFAULT_CONNECT_TIMEOUT = 10;
	private static final Integer DEFAULT_REQUEST_TIMEOUT = 60;

	private static final String DEFAULT_PROXY_HOST = "";
	private static final int DEFAULT_PROXY_PORT = 8081;
	private static final String DEFAULT_PROXY_USER = "";
	private static final String DEFAULT_PROXY_PASSWORD = "";

	private static String agentHome = "";

	private static void download(String filename) {
		m_log.debug("download() : start ");
		FileOutputStream fileOutputStream = null;
		try {
			DataHandler handler = AgentEndPointWrapper
					.downloadScripts(filename);
			String dirPath = agentHome + "var/cloud/";
			File dir = new File(dirPath);
			if (!dir.exists()) {
				echo("create directory: " + dir.getPath());
				if (!dir.mkdirs())
					throw new InternalError("can not create directory, directoryPath : " + dirPath);
			}

			File file = new File(dirPath + filename);
			echo("create file: " + file.getPath());
			if (!file.createNewFile())
				throw new InternalError("can not create file, filename : " + filename);
			
			fileOutputStream = new FileOutputStream(file);
			handler.writeTo(fileOutputStream);
			file.setExecutable(true, false);
		} catch (Exception e) {
			m_log.warn("download error " + filename + ", " + e);
			System.err.println("download error " + filename + " "
					+ e.getMessage());

			System.exit(1);
		} finally {
			if (fileOutputStream != null) {
				try {
					fileOutputStream.close();
				} catch (IOException e) {
					m_log.warn("download " + filename + ", " + e);
				}
			}
		}
	}

	public static void main(String args[]) {

		echo("ScriptsDownload Process start.");

		// プロパティファイル読み込み初期化
		echo("configuration.");
		AgentProperties.init(args[0]);

		File file = new File(args[0]);
		agentHome = file.getParentFile().getParent() + "/";

		int connectTimeout = DEFAULT_CONNECT_TIMEOUT;
		int requestTimeout = DEFAULT_REQUEST_TIMEOUT;

		// プロキシ設定
		String proxyHost = DEFAULT_PROXY_HOST;
		int proxyPort = DEFAULT_PROXY_PORT;
		String proxyUser = DEFAULT_PROXY_USER;
		String proxyPassword = DEFAULT_PROXY_PASSWORD;

		try {
			String strConnect = AgentProperties.getProperty("connect.timeout");
			if (strConnect != null) {
				requestTimeout = Integer.parseInt(strConnect);
			}
			String strRequest = AgentProperties.getProperty("request.timeout");
			if (strRequest != null) {
				requestTimeout = Integer.parseInt(strRequest);
			}
			String strProxyHost = AgentProperties
					.getProperty("http.proxy.host");
			if (strProxyHost != null) {
				proxyHost = strProxyHost;
			}
			String strProxyPort = AgentProperties
					.getProperty("http.proxy.port");
			if (strProxyPort != null) {
				proxyPort = Integer.parseInt(strProxyPort);
			}
			String strProxyUser = AgentProperties
					.getProperty("http.proxy.user");
			if (strProxyUser != null) {
				proxyUser = strProxyUser;
			}
			String strProxyPassword = AgentProperties
					.getProperty("http.proxy.password");
			if (strProxyPassword != null) {
				proxyPassword = strProxyPassword;
			}
		} catch (Exception e) {
			m_log.warn(e.getMessage());
		}

		if (!"".equals(proxyHost)) {
			System.setProperty("http.proxyHost", proxyHost);
			System.setProperty("http.proxyPort", Integer.toString(proxyPort));
			BasicAuth basicAuth = new BasicAuth(proxyUser, proxyPassword);
			Authenticator.setDefault(basicAuth);
			m_log.info("proxy.host=" + System.getProperty("http.proxyHost")
					+ ", proxy.port=" + System.getProperty("http.proxyPort")
					+ ", proxy.user=" + proxyUser);
		}

		try {
			EndpointManager.init(AgentProperties.getProperty("user"),
					AgentProperties.getProperty("password"),
					AgentProperties.getProperty("managerAddress"),
					connectTimeout, requestTimeout);
		} catch (Exception e) {
			m_log.error("EndpointManager.init error : " + e.getMessage(), e);
			m_log.error("current-dir="
					+ (new File(".")).getAbsoluteFile().getParent());
		}
		for (int i = 1; i < args.length; i++) {
			echo("download target script " + args[i] + " start.");
			download(args[i]);
			echo("download target script " + args[i] + " finish.");
		}

		echo("ScriptsDownload Process finish.");
	}

	private static void echo(String str) {
		m_log.info(str);
		System.out.println(str);
	}
}

class BasicAuth extends Authenticator {
	private String username;
	private String password;

	public BasicAuth(String username, String password) {
		this.username = username;
		this.password = password;
	}

	protected PasswordAuthentication getPasswordAuthentication() {
		return new PasswordAuthentication(username, password.toCharArray());
	}
}
