package com.clustercontrol.agent;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.ws.agent.AgentEndpointService;
import com.clustercontrol.ws.agenthub.AgentHubEndpointService;
import com.clustercontrol.ws.cloud.CloudCommonEndpointService;

/**
 * Hinemosマネージャとの通信をするクラス。
 * HAのような複数マネージャ対応のため、このクラスを実装する。
 * 
 * Hinemosマネージャと通信できない場合は、WebServiceExceptionがthrowされる。
 * WebServiceExeptionが出力された場合は、もう一台のマネージャと通信する。
 */
public class EndpointManager {

	// ログ
	private static Log m_log = LogFactory.getLog( EndpointManager.class );

	private static EndpointList endpointList;
	private final static String AGENT = AgentEndpointService.class.getSimpleName();
	private final static String AGENTHUB = AgentHubEndpointService.class.getSimpleName();
	private final static String CLOUD_COMMON = CloudCommonEndpointService.class.getSimpleName();

	private static class EndpointList {
		/*
		 * endpointListはマネージャ一覧を示す。
		 * これは初期化時のみ変更可能(add可能)。
		 * 
		 * lastSuccessEndpointは最後に成功したendpointを示す。
		 * endpointList.contains(lastSuccessEndpoint)は必ずtrueになる。
		 */
		private ArrayList<HashMap<String, EndpointSetting>> endpointList =
				new ArrayList<HashMap<String, EndpointSetting>>();
		private HashMap<String, EndpointSetting> lastSuccessEndpoint = null;

		private void add(HashMap<String, EndpointSetting> endpointMap) {
			if (lastSuccessEndpoint == null) {
				lastSuccessEndpoint = endpointMap;
			}
			endpointList.add(endpointMap);
		}

		private ArrayList<EndpointSetting> getList(String key) {
			ArrayList<EndpointSetting> list = new ArrayList<EndpointSetting>();
			list.add(lastSuccessEndpoint.get(key));
			for (HashMap<String, EndpointSetting> endpoint : endpointList) {
				if (!endpoint.equals(lastSuccessEndpoint)) {
					list.add(endpoint.get(key));
				}
			}
			return list;
		}

		/*
		 * 当面、使用予定なし。
		 */
		@SuppressWarnings("unused")
		@Deprecated
		private void setLastSuccess(HashMap<String, EndpointSetting> endpoint) {
			if (!endpointList.contains(endpoint)) {
				m_log.warn("setLastSuccess(), Error:!endpointList.contains(endpoint)");
			}
			lastSuccessEndpoint = endpoint;
			m_log.debug("set lastSuccessEndpoint. new one is " +
					lastSuccessEndpoint);
		}

		/**
		 * リストの順番を変えるメソッド。
		 * endpointListの順番で、lastSuccesEndpointをローテートする。
		 * @param endpoint
		 */
		private void changeEndpoint() {
			m_log.debug("changeEndpoint");
			boolean flag = false;
			for (HashMap<String, EndpointSetting> e : endpointList) {
				if (flag) {
					lastSuccessEndpoint = e;
					flag = false;
					return;
				}
				if (lastSuccessEndpoint.equals(e)) {
					flag = true;
				}
			}
			lastSuccessEndpoint = endpointList.get(0);
		}

		/**
		 * ログアウト
		 */
		private void logout() {
			for (HashMap<String, EndpointSetting> map : endpointList) {
				for (Entry<String, EndpointSetting> key : map.entrySet()) {
					EndpointSetting setting = key.getValue();
					setting.logout();
				}
			}
		}

		private int size() {
			return endpointList.size();
		}
	}

	public static class EndpointSetting {
		private String key;
		private String urlPrefix;
		private Object endpoint;
		private String wsdlSuffix = "?wsdl";

		private EndpointSetting(String key, String urlPrefix) {
			this.key = key;
			this.urlPrefix = urlPrefix;
		}

		private void logout() {
			endpoint = null;
		}

		public Object getEndpoint() {

			if (endpoint != null) {
				return endpoint;
			}
			String tmpKey = null;
			tmpKey = AGENT;
			if (tmpKey.equals(key)) {
				String urlStr = urlPrefix + tmpKey + wsdlSuffix;
				AgentEndpointService tmpEndpointService = null;
				try {
					tmpEndpointService = new AgentEndpointService(
							new URL(urlStr),
							new QName("http://agent.ws.clustercontrol.com", tmpKey));
					endpoint = tmpEndpointService.getAgentEndpointPort();
					setBindingProvider(endpoint, username, password, urlStr);
				} catch (MalformedURLException e) {
					m_log.warn("getEndpoint():AccessEndpointService, " + e.getMessage(), e);
				}
			}
			tmpKey = AGENTHUB;
			if (tmpKey.equals(key)) {
				String urlStr = urlPrefix + tmpKey + wsdlSuffix;
				AgentHubEndpointService tmpEndpointService = null;
				try {
					tmpEndpointService = new AgentHubEndpointService(
							new URL(urlStr), new QName("http://agenthub.ws.clustercontrol.com", tmpKey));
					endpoint = tmpEndpointService.getAgentHubEndpointPort();
					setBindingProvider(endpoint, username, password, urlStr);
				} catch (MalformedURLException e) {
					m_log.warn("getEndpoint() : AgentHubEndpointService, " + e.getMessage(), e);
				}
			}
			tmpKey = CLOUD_COMMON;
			if (tmpKey.equals(key)) {
				String urlStr = urlPrefix + tmpKey + wsdlSuffix;
				CloudCommonEndpointService tmpEndpointService = null;
				try {
					tmpEndpointService = new CloudCommonEndpointService(
							new URL(urlStr), new QName("http://cloud.ws.clustercontrol.com", tmpKey));
					endpoint = tmpEndpointService.getCloudCommonEndpointPort();
					setBindingProvider(endpoint, username, password, urlStr);
				} catch (MalformedURLException e) {
					m_log.warn("getEndpoint() : CloudCommonEndpointService, " + e.getMessage(), e);
				}
			}
			return endpoint;
		}
	}

	private static String username = "";
	private static String password = "";
	private static int m_httpConnectTimeout = Agent.DEFAULT_CONNECT_TIMEOUT;
	private static int m_httpRequestTimeout = Agent.DEFAULT_REQUEST_TIMEOUT;

	public static void init(String user, String pass, String managerAddressList,
			int httpConnectTimeout, int httpRequestTimeout) throws MalformedURLException {
		username = user;
		password = pass;
		endpointList = new EndpointList();
		m_httpConnectTimeout = httpConnectTimeout;
		m_httpRequestTimeout = httpRequestTimeout;

		for (String managerAddress : managerAddressList.split(",")) {

			// TODO ユーザ/パスワードチェックを実装する必要あり。
			HashMap<String, EndpointSetting> map = new HashMap<String, EndpointSetting>();
			String wsdlPrefix = managerAddress.trim();

			map.put(AGENT, new EndpointSetting(AGENT, wsdlPrefix));
			map.put(AGENTHUB, new EndpointSetting(AGENTHUB, wsdlPrefix));
			map.put(CLOUD_COMMON, new EndpointSetting(CLOUD_COMMON, wsdlPrefix));

			endpointList.add(map);
		}
		m_log.info("manager instance = " + endpointList.size());
	}

	private static void setBindingProvider(Object o, String user, String password, String urlStr) {
		BindingProvider bp = (BindingProvider)o;
		bp.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, urlStr);
		bp.getRequestContext().put(BindingProvider.USERNAME_PROPERTY, user);
		bp.getRequestContext().put(BindingProvider.PASSWORD_PROPERTY, password);
		// bp.getRequestContext().put(BindingProviderProperties.CONNECT_TIMEOUT, 30);
		// bp.getRequestContext().put(BindingProviderProperties.REQUEST_TIMEOUT, 30);
		m_log.info("connect.timeout=" + m_httpConnectTimeout +
				", request.timeout=" + m_httpRequestTimeout);
		bp.getRequestContext().put("com.sun.xml.internal.ws.connect.timeout", m_httpConnectTimeout);
		bp.getRequestContext().put("com.sun.xml.internal.ws.request.timeout", m_httpRequestTimeout);

	}

	/**
	 *  使用可能な順番でAgentEndpointを返す。
	 * @return
	 */
	public static ArrayList<EndpointSetting> getAgentEndpoint() {
		return endpointList.getList(AGENT);
	}

	/**
	 *  使用可能な順番でAgentEndpointを返す。
	 * @return
	 */
	public static ArrayList<EndpointSetting> getAgentHubEndpoint() {
		return endpointList.getList(AGENTHUB);
	}
	
	/**
	 *  使用可能な順番でCloudCommonEndpointを返す。
	 * @return
	 */
	public static ArrayList<EndpointSetting> getCloudCommonEndpoint() {
		return endpointList.getList(CLOUD_COMMON);
	}

	/**
	 *  Endpointの利用時にWebServiceExceptionが出たらこのメソッドを呼ぶこと。
	 */
	public static void changeEndpoint() {
		endpointList.changeEndpoint();
	}

	/**
	 * ログアウト
	 */
	public static void logout() {
		m_log.debug("logout");
		username = null;
		password = null;
		if (endpointList == null) {
			return;
		}
		endpointList.logout();
	}
}
