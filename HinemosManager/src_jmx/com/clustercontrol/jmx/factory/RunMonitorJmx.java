/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jmx.factory;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.SocketTimeoutException;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.SimpleType;
import javax.management.openmbean.TabularData;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.jmx.factory.MonitorJmxCache.MonitorJmxValue;
import com.clustercontrol.jmx.factory.MonitorJmxCache.MonitorJmxValuePK;
import com.clustercontrol.jmx.model.JmxCheckInfo;
import com.clustercontrol.jmx.model.JmxMasterInfo;
import com.clustercontrol.jmx.util.QueryUtil;
import com.clustercontrol.monitor.bean.ConvertValueConstant;
import com.clustercontrol.monitor.run.factory.RunMonitor;
import com.clustercontrol.monitor.run.factory.RunMonitorNumericValueType;
import com.clustercontrol.repository.model.NodeInfo;
import com.clustercontrol.repository.session.RepositoryControllerBean;
import com.clustercontrol.util.HinemosTime;
import com.clustercontrol.util.MessageConstant;

/**
 * JMX 監視 数値監視を実行するクラス<BR>
 *
 * @version 5.0.0
 * @since 5.0.0
 */
public class RunMonitorJmx extends RunMonitorNumericValueType {

	private static Log m_log = LogFactory.getLog( RunMonitorJmx.class );

	private static final String NaN = "NaN";

	private static final ThreadFactory daemonThreadFactory = new DaemonThreadFactory();

	/** メッセージ */
	private String m_message = "";

	/** JMX 監視情報 */
	private JmxCheckInfo jmx = null;

	/** 取得値の加工 */
	private int m_convertFlg = 0;

	/** 前回値がnull判定用フラグ */
	private boolean m_prevNullchk = false;

	/**例外 */
	private Exception exception;

	/**
	 * コンストラクタ
	 * 
	 */
	public RunMonitorJmx() {
		super();
	}

	/**
	 * マルチスレッドを実現するCallableTaskに渡すためのインスタンスを作成するメソッド
	 * 
	 */
	@Override
	protected RunMonitor createMonitorInstance() {
		return new RunMonitorJmx();
	}


	/**
	 * JMX 経由で値を取得
	 * 
	 * @param facilityId ファシリティID
	 * @return 値取得に成功した場合、true
	 */
	@Override
	public boolean collect(String facilityId) {
		boolean result = false;

		if (m_now != null){
			m_nodeDate = m_now.getTime();
		}
		exception = null;

		NodeInfo node = null;
		if (!m_isMonitorJob) {
			node = nodeInfo.get(facilityId);
		} else {
			try {
				// ノードの属性取得
				node = new RepositoryControllerBean().getNode(facilityId);
			}
			catch(Exception e){
				m_message = MessageConstant.MESSAGE_COULD_NOT_GET_NODE_ATTRIBUTES.getMessage();
				return false;
			}
		}

		JMXServiceURL url = null;
		try {
			String rmiFormat = HinemosPropertyCommon.monitor_jmx_rmi_format.getStringValue();
			String urlStr = String.format(rmiFormat, node.getAvailableIpAddress(), jmx.getPort());
			m_log.debug("facilityId=" + facilityId + ", url=" + urlStr);
			url = new JMXServiceURL(urlStr);
		}catch (Exception e) {
			m_log.warn("fail to initialize JMXServiceURL : " + e.getMessage() + " (" + e.getClass().getName() + ")", e);
			exception = e;
			return result;
		}

		JMXConnector jmxc = null;
		try {
			Map<String, Object> env = new HashMap<>();

			if (jmx.getAuthUser() != null)
				env.put(JMXConnector.CREDENTIALS, new String[]{jmx.getAuthUser(), jmx.getAuthPassword()});

			System.setProperty("sun.rmi.transport.tcp.responseTimeout",
					Integer.toString(HinemosPropertyCommon.system_sun_rmi_transport_tcp_responseTimeout.getIntegerValue()));
			
			// monitor_jmx_connect_time_milisecondのデバッグ用Hinemosプロパティから接続タイムアウト時間を取得する。
			Long timeOut = HinemosPropertyCommon.monitor_jmx_connect_time_milisecond.getNumericValue();
			// プロパティ値が0以下であれば、sun側の接続後のプロパティ値の+3秒を接続タイムアウト時間とする。
			if (timeOut <= 0) {
				timeOut = HinemosPropertyCommon.system_sun_rmi_transport_tcp_responseTimeout.getNumericValue() + 3000;
			}
			
			jmxc = connectWithTimeout(url, env, timeOut, TimeUnit.MILLISECONDS);
			MBeanServerConnection mbsc = jmxc.getMBeanServerConnection();

			JmxMasterInfo jmxMasterInfo = QueryUtil.getJmxMasterInfoPK(jmx.getMasterId());
			Object value = mbsc.getAttribute(new ObjectName(jmxMasterInfo.getObjectName()), jmxMasterInfo.getAttributeName());
			m_value = Double.parseDouble(searchTargetValue(value, Arrays.asList(KeyParser.parseKeys(jmxMasterInfo.getKeys()))).toString());

			// 差分をとる場合
			if (m_convertFlg == ConvertValueConstant.TYPE_DELTA) {

				// 前回値を取得
				MonitorJmxValue valueEntity = null;
				Double prevValue = 0d;
				Long prevDate = 0l;

				if (!m_isMonitorJob) {
					// 監視ジョブ以外の場合
					// cacheより前回情報を取得
					valueEntity = MonitorJmxCache.getMonitorJmxValue(m_monitorId, facilityId);

					// 前回の取得値
					prevValue = valueEntity.getValue();
					if (valueEntity.getGetDate() != null) {
						prevDate = valueEntity.getGetDate();
					}
				} else {
					// 監視ジョブの場合
					valueEntity = new MonitorJmxValue(new MonitorJmxValuePK(m_monitorId, facilityId));
					if (m_prvData instanceof MonitorJmxValue) {
						// 前回値が存在する場合
						prevValue = ((MonitorJmxValue)m_prvData).getValue();
						prevDate = ((MonitorJmxValue)m_prvData).getGetDate();
					}
				}

				// JMX前回値情報を今回の取得値に更新
				valueEntity.setValue(m_value);
				valueEntity.setGetDate(m_nodeDate);
				
				if (!m_isMonitorJob) {
					// 監視処理時に対象の監視項目IDが有効、または収集が有効である場合にキャッシュを更新
					if (m_monitor.getMonitorFlg() || m_monitor.getCollectorFlg())
						MonitorJmxCache.update(m_monitorId, facilityId, valueEntity);
	
					int m_validSecond = HinemosPropertyCommon.monitor_jmx_valid_second.getIntegerValue();
					// 前回値取得時刻が取得許容時間よりも前だった場合、値取得失敗
					int tolerance = (m_runInterval + m_validSecond) * 1000;
	
					if(prevDate > m_nodeDate - tolerance){

						// 前回値がnullであれば監視失敗
						if (prevValue == null) {
							m_log.debug("collect() : prevValue is null");
							m_prevNullchk = true;
							return false;
						}

						m_value = m_value - prevValue;
					}
					else{
						if (prevDate != 0l) {
							DateFormat df = DateFormat.getDateTimeInstance();
							df.setTimeZone(HinemosTime.getTimeZone());
							String[] args = { df.format(new Date(prevDate))};
							m_message = MessageConstant.MESSAGE_TOO_OLD_TO_CALCULATE.getMessage(args);
							return false;
						}
						else {
							// ノード監視結果取得時刻に0を設定し、正常終了
							m_nodeDate = 0l;
						}
					}
				} else {
					m_value = m_value - prevValue;
					m_curData = valueEntity;
				}
			}

			m_log.debug(jmxMasterInfo.getName() + " : " + m_value + " " + jmxMasterInfo.getMeasure());

			result = true;
		} catch (NumberFormatException e) {
			m_log.info("collect() : " + e.getClass().getSimpleName() + ", " + e.getMessage());
			String[] args = { Double.toString(m_value) };
			m_message = MessageConstant.MESSAGE_COULD_NOT_GET_NUMERIC_VALUE.getMessage(args);
			return false;
		}catch (Exception e) {
			String message = e.getMessage();
			if (e instanceof NullPointerException) {
				m_log.warn("fail to access JMXService : " + message + " (" + e.getClass().getName() + ")", e);
			} else {
				m_log.warn("fail to access JMXService : " + message + " (" + e.getClass().getName() + ")");
			}
			if (message != null) {
				message = message.replaceAll("\n", "");
			}
			exception = e;
		} finally {
			try {
				if (jmxc != null) {
					jmxc.close();
				}
			} catch (IOException e) {
				m_log.info("fail to close JMXService : " + e.getMessage() + " (" + e.getClass().getName() + ")");
				exception = e;
				//値の取得後（resultはtrue）、closeで失敗時、通信が正常完了してない。監視結果が不明となるように配慮
				result = false;
			}
		}

		return result;
	}

	private SimpleType<?>[] validTypes = {
		SimpleType.BYTE,
		SimpleType.SHORT,
		SimpleType.INTEGER,
		SimpleType.LONG,
		SimpleType.FLOAT,
		SimpleType.DOUBLE
	};

	private Object searchTargetValue(Object value, List<Object> keys) throws Exception {
		if (value instanceof CompositeData) {
			if (keys.isEmpty())
				throw new Exception("not found value according to keys.");
			return searchTargetValue(((CompositeData)value).get(keys.get(0).toString()), keys.subList(1, keys.size()));
		}
		else if (value instanceof TabularData) {
			if (keys.isEmpty())
				throw new Exception("not found value according to keys.");
			return searchTargetValue(((TabularData)value).get((Object[])keys.get(0)), keys.subList(1, keys.size()));
		}
		else {
			for (SimpleType<?> t: validTypes) {
				if (t.isValue(value))
					return value;
			}
			throw new Exception("value type id invalid. " + value.getClass());
		}
	}

	@Override
	protected void setCheckInfo() throws MonitorNotFound {
		if (jmx == null)
			// JMX 監視情報を取得
			jmx = QueryUtil.getMonitorJmxInfoPK(m_monitorId);

		// 取得値の加工を設定
		m_convertFlg = jmx.getConvertFlg().intValue();
	}

	/**
	 * メッセージを取得します。
	 */
	@Override
	public String getMessage(int result) {
		String message;
		if(exception == null){
			if(Double.isNaN(m_value)){
				message = NaN;
			}
			else {
				if (!"".equals(m_message.trim()))return m_message;
				String name = "?";
				try {
					JmxMasterInfo jmxMasterInfo = QueryUtil.getJmxMasterInfoPK(jmx.getMasterId());
					name = jmxMasterInfo.getName();
				} catch (MonitorNotFound e) {
					m_log.warn("not found : " + jmx.getMasterId());
				}
				if (m_prevNullchk) {
					message = name + " : ";
					return message;
				}
				message = name + " : " + NumberFormat.getNumberInstance().format(m_value);
			}
		}
		else {
			String name = "?";
			try {
				JmxMasterInfo jmxMasterInfo = QueryUtil.getJmxMasterInfoPK(jmx.getMasterId());
				name = jmxMasterInfo.getName();
			} catch (MonitorNotFound e) {
				m_log.warn("not found : " + jmx.getMasterId());
			}
			message = name + " : " + MessageConstant.MESSAGE_COULD_NOT_GET_VALUE_JMX.getMessage();
		}
		return message;
	}

	/**
	 * オリジナルメッセージを取得します。
	 */
	@Override
	public String getMessageOrg(int result) {
		String message;
		if(exception == null){
			if(Double.isNaN(m_value)){
				message = NaN;
			}
			else {
				String name = "?";
				try {
					JmxMasterInfo jmxMasterInfo = QueryUtil.getJmxMasterInfoPK(jmx.getMasterId());
					name = jmxMasterInfo.getName();
				} catch (MonitorNotFound e) {
					m_log.warn("not found : " + jmx.getMasterId());
				}
				if (m_prevNullchk) {
					message = name + " : ";
					m_prevNullchk = !m_prevNullchk;
					return message;
				}
				message = name + " : " + NumberFormat.getNumberInstance().format(m_value);
			}
		}
		else {
			String name = "?";
			try {
				JmxMasterInfo jmxMasterInfo = QueryUtil.getJmxMasterInfoPK(jmx.getMasterId());
				name = jmxMasterInfo.getName();
			} catch (MonitorNotFound e) {
				m_log.warn("not found : " + jmx.getMasterId());
			}
			message = name + " : " + exception.getMessage();
		}
		return message;
	}

	@Override
	protected String makeJobOrgMessage(String orgMsg, String msg) {
		if (m_monitor == null || m_monitor.getJmxCheckInfo() == null) {
			return "";
		}
		JmxMasterInfo jmxMasterInfo = null;
		try {
			jmxMasterInfo = QueryUtil.getJmxMasterInfoPK(m_monitor.getJmxCheckInfo().getMasterId());
		} catch (MonitorNotFound e) {
			return "";
		}

		String[] args = {
				String.valueOf(jmxMasterInfo.getName()),
				String.valueOf(m_monitor.getJmxCheckInfo().getPort()),
				String.valueOf(m_monitor.getJmxCheckInfo().getAuthUser()),
				""};
		if(m_convertFlg == ConvertValueConstant.TYPE_NO){
			// 何もしない
			args[3] = MessageConstant.CONVERT_NO.getMessage();
		} else if (m_convertFlg == ConvertValueConstant.TYPE_DELTA) {
			// 差分をとる
			args[3] = MessageConstant.DELTA.getMessage();
		}
		return MessageConstant.MESSAGE_JOB_MONITOR_ORGMSG_JMX.getMessage(args)
				+ "\n" + orgMsg
				+ "\n" + msg;
	}
	
	/**
	 * 接続タイムアウトを配慮したJMXのアクセス実装
	 * @param url
	 * @param env
	 * @param timeout
	 * @param unit
	 * @return
	 * @throws IOException
	 */
	public static JMXConnector connectWithTimeout(final JMXServiceURL url, final Map<String, Object> env, long timeout, TimeUnit unit) throws IOException {
		// Queueによる実行の制御を行う
		final BlockingQueue<Object> jmxmonitor = new ArrayBlockingQueue<Object>(1);
		ExecutorService executor = Executors.newSingleThreadExecutor(daemonThreadFactory);
		executor.submit(new Runnable() {
			public void run() {
				try {
					// 監視対象へ接続
					JMXConnector connector = JMXConnectorFactory.connect(url, env);
					if (m_log.isTraceEnabled()) {
						m_log.trace("JMX monitor url : " + url + ", timeout : " + timeout + "(s)");
					}
					if (!jmxmonitor.offer(connector)) {
						connector.close();
					}
				} catch (Throwable t) {
					jmxmonitor.offer(t);
				}
			}
		});
		Object result;
		try {
			result = jmxmonitor.poll(timeout, unit);
			if (result == null) {
				if (!jmxmonitor.offer("")) {
					result = jmxmonitor.take();
				}
			}
		} catch (InterruptedException e) {
			throw initCause(new InterruptedIOException(e.getMessage()), e);
		} finally {
			// 監視を強制終了する
			executor.shutdown();
		}
		// 結果がnullの場合はTimeOut
		if (result == null) {
			m_log.warn("Jmx monitor is connect timed out");
			throw new SocketTimeoutException("Connect timed out: " + url);
		}
		// 結果がJMXConnectorだった場合は、値を返却
		if (result instanceof JMXConnector) {
			return (JMXConnector) result;
		}
		
		try {
			// 上記以外はエクセプションなのでふるい分ける
			throw (Throwable) result;
		} catch (IOException e) {
			throw e;
		} catch (RuntimeException e) {
			throw e;
		} catch (Error e) {
			throw e;
		} catch (Throwable e) {
			throw new IOException(e.toString(), e);
		}
	}
	
	/**
	 * Throwable causeパラメータを持たない例外の回避用
	 * @param wrapper
	 * @param wrapped
	 * @return
	 */
	private static <T extends Throwable> T initCause(T wrapper, Throwable wrapped) {
		wrapper.initCause(wrapped);
		return wrapper;
	}
	
	private static class DaemonThreadFactory implements ThreadFactory {
		public Thread newThread(Runnable r) {
			Thread t = Executors.defaultThreadFactory().newThread(r);
			t.setDaemon(true);
			t.setName("RunMonitorJmx-thread-%d");
			return t;
		}
	}
}
