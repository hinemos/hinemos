/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.snmp.factory;

import java.text.DateFormat;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.snmp4j.smi.SMIConstants;

import com.clustercontrol.fault.FacilityNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.monitor.run.factory.RunMonitor;
import com.clustercontrol.monitor.run.factory.RunMonitorNumericValueType;
import com.clustercontrol.repository.model.NodeInfo;
import com.clustercontrol.repository.session.RepositoryControllerBean;
import com.clustercontrol.monitor.bean.ConvertValueConstant;
import com.clustercontrol.snmp.factory.MonitorSnmpCache.MonitorSnmpValue;
import com.clustercontrol.snmp.factory.MonitorSnmpCache.MonitorSnmpValuePK;
import com.clustercontrol.snmp.model.SnmpCheckInfo;
import com.clustercontrol.snmp.util.QueryUtil;
import com.clustercontrol.snmp.util.RequestSnmp4j;
import com.clustercontrol.snmp.util.SnmpProperties;
import com.clustercontrol.util.HinemosTime;
import com.clustercontrol.util.MessageConstant;

/**
 * SNMP監視 数値監視を実行するファクトリークラス<BR>
 *
 * @version 4.0.0
 * @since 2.0.0
 */
public class RunMonitorSnmp extends RunMonitorNumericValueType {

	private static Log m_log = LogFactory.getLog( RunMonitorSnmp.class );

	/** SNMP監視情報 */
	private SnmpCheckInfo m_snmp = null;

	/** OID */
	private String m_snmpOid = null;

	/** 取得値の加工 */
	private int m_convertFlg = 0;

	/** オリジナルメッセージ */
	private String m_messageOrg = null;

	/** メッセージ */
	private String m_message = "";

	/**
	 * コンストラクタ
	 * 
	 */
	public RunMonitorSnmp() {
		super();
	}

	/**
	 * マルチスレッドを実現するCallableTaskに渡すためのインスタンスを作成するメソッド
	 * 
	 * @see com.clustercontrol.monitor.run.factory.RunMonitor#runMonitorInfo()
	 * @see com.clustercontrol.monitor.run.util.MonitorExecuteTask
	 */
	@Override
	protected RunMonitor createMonitorInstance() {
		return new RunMonitorSnmp();
	}

	/**
	 * OID値を取得
	 * 
	 * @param facilityId ファシリティID
	 * @return 値取得に成功した場合、true
	 * @throws HinemosUnknown
	 */
	@Override
	public boolean collect(String facilityId) throws HinemosUnknown {

		if (m_now != null) {
			m_nodeDate = m_now.getTime();
		}

		// メッセージを設定
		m_message = "";
		m_messageOrg = MessageConstant.OID.getMessage() + " : " + m_snmpOid;

		NodeInfo info = null;
		try {
			// ノードの属性取得
			info = new RepositoryControllerBean().getNode(facilityId);
		}
		catch(FacilityNotFound e){
			m_message = MessageConstant.MESSAGE_COULD_NOT_GET_NODE_ATTRIBUTES.getMessage();
			m_messageOrg = m_messageOrg + " (" + e.getMessage() + ")";
			resetCache(m_monitorId, facilityId);
			return false;
		}

		// SNMP値取得
		RequestSnmp4j m_request = new RequestSnmp4j();

		m_log.debug("version=" + info.getSnmpVersion());
		boolean result = false;
		try {
			result = m_request.polling(
					info.getAvailableIpAddress(),
					info.getSnmpCommunity(),
					info.getSnmpPort(),
					m_snmpOid,
					info.getSnmpVersion(),
					info.getSnmpTimeout(),
					info.getSnmpRetryCount(),
					info.getSnmpSecurityLevel(),
					info.getSnmpUser(),
					info.getSnmpAuthPassword(),
					info.getSnmpPrivPassword(),
					info.getSnmpAuthProtocol(),
					info.getSnmpPrivProtocol()
					);
		} catch (Exception e) {
			m_message = MessageConstant.MESSAGE_COULD_NOT_GET_NODE_ATTRIBUTES.getMessage();
			m_messageOrg = m_message + ", " + e.getMessage() + " (" + e.getClass().getName() + ")";
			if (e instanceof NumberFormatException) {
				m_log.warn(m_messageOrg);
			} else {
				m_log.warn(m_messageOrg, e);
			}
			resetCache(m_monitorId, facilityId);
			return false;
		}

		if(result){

			// 今回の取得値
			double value = -1;
			try {
				if (m_request.getValue() == null) {
					m_log.debug("collect() : m_request.getValue() is null");
					return false;
				}
				value = Double.parseDouble(m_request.getValue());
			} catch (NumberFormatException e) {
				m_log.info("collect() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				String[] args = { m_request.getValue() };
				m_message = MessageConstant.MESSAGE_COULD_NOT_GET_NUMERIC_VALUE.getMessage(args);
				resetCache(m_monitorId, facilityId);
				return false;
			} catch (Exception e) {
				m_log.warn("collect() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
				String[] args = { m_request.getValue() };
				m_message = MessageConstant.MESSAGE_COULD_NOT_GET_NUMERIC_VALUE.getMessage(args);
				resetCache(m_monitorId, facilityId);
				return false;
			}
			long date = m_request.getDate();

			// 加工しない場合
			if(m_convertFlg == ConvertValueConstant.TYPE_NO){
				m_value = value;
				m_nodeDate = date;
			}
			// 差分をとる場合
			else if(m_convertFlg == ConvertValueConstant.TYPE_DELTA){
				// 前回値を取得
				MonitorSnmpValue valueEntity = null;
				Double prevValue = 0d;
				Long prevDate = 0l;
				m_value = -1d;
				if (!m_isMonitorJob) {
					// 監視ジョブ以外の場合
					// cacheより前回情報を取得
					valueEntity = MonitorSnmpCache.getMonitorSnmpValue(m_monitorId, facilityId);

					// 前回の取得値
					prevValue = valueEntity.getValue();
					if (valueEntity.getGetDate() != null) {
						prevDate = valueEntity.getGetDate();
					}
				} else {
					// 監視ジョブの場合
					valueEntity = new MonitorSnmpValue(new MonitorSnmpValuePK(m_monitorId, facilityId));
					if (m_prvData instanceof MonitorSnmpValue) {
						// 前回値が存在する場合
						prevValue = ((MonitorSnmpValue)m_prvData).getValue();
						prevDate = ((MonitorSnmpValue)m_prvData).getGetDate();
					}
				}

				if (prevValue != null) {
					if (prevValue > value) {
						if (m_request.getType() == SMIConstants.SYNTAX_COUNTER32) {
							value += ((double)Integer.MAX_VALUE + 1) * 2;
						} else if (m_request.getType() == SMIConstants.SYNTAX_COUNTER64) {
							value += ((double)Long.MAX_VALUE + 1) * 2;
						}
					}
				}

				// SNMP前回値情報を今回の取得値に更新
				valueEntity.setValue(Double.valueOf(value));
				valueEntity.setGetDate(date);

				if (!m_isMonitorJob) {
					// 監視処理時に対象の監視項目IDが有効、または収集が有効である場合にキャッシュを更新
					if (m_monitor.getMonitorFlg() || m_monitor.getCollectorFlg())
						MonitorSnmpCache.update(m_monitorId, facilityId, valueEntity);

					// 前回値取得時刻がSNMP取得許容時間よりも前だった場合、値取得失敗
					int tolerance = (m_runInterval + SnmpProperties.getProperties().getValidSecond()) * 1000;

					if(prevDate > date - tolerance){

						// 前回値がnullであれば監視失敗
						if (prevValue == null) {
							m_log.debug("collect() : prevValue is null");
							return false;
						}

						m_value = value - prevValue;
						m_nodeDate = m_request.getDate();
					}
					else{
						if (prevDate != 0l) {
							DateFormat df = DateFormat.getDateTimeInstance();
							df.setTimeZone(HinemosTime.getTimeZone());
							String[] args = {df.format(new Date(prevDate))};
							m_message = MessageConstant.MESSAGE_TOO_OLD_TO_CALCULATE.getMessage(args);
							return false;
						}
						else {
							// ノード監視結果取得時刻に0を設定し、正常終了
							m_nodeDate = 0l;
						}
					}
				} else {
					m_value = value - prevValue;
					m_nodeDate = m_request.getDate();
					m_curData = valueEntity;
				}

			}
			m_message = MessageConstant.SELECT_VALUE.getMessage() + " : " + m_value;
		}
		else{
			m_message = m_request.getMessage();
			resetCache(m_monitorId, facilityId);
		}
		return result;
	}

	/* (非 Javadoc)
	 * SNMP監視情報を設定
	 * @see com.clustercontrol.monitor.run.factory.OperationNumericValueInfo#setMonitorAdditionInfo()
	 */
	@Override
	protected void setCheckInfo() throws MonitorNotFound {

		// SNMP監視情報を取得
		m_snmp = QueryUtil.getMonitorSnmpInfoPK(m_monitorId);

		// SNMP監視情報を設定
		m_snmpOid = m_snmp.getSnmpOid().trim();
		m_convertFlg = m_snmp.getConvertFlg().intValue();
	}

	/* (非 Javadoc)
	 * ノード用メッセージを取得
	 * @see com.clustercontrol.monitor.run.factory.OperationMonitor#getMessage(int)
	 */
	@Override
	public String getMessage(int id) {
		return m_message;
	}

	/* (非 Javadoc)
	 * ノード用オリジナルメッセージを取得
	 * @see com.clustercontrol.monitor.run.factory.OperationMonitor#getMessageOrg(int)
	 */
	@Override
	public String getMessageOrg(int id) {
		return m_messageOrg;
	}

	/**
	 * SNMPが取得できなかった場合、キャッシュをnull更新する。
	 * @param m_monitorId 監視項目ID
	 * @param facilityId ファシリティID
	 */
	private void resetCache(String m_monitorId, String facilityId) {

		MonitorSnmpValue valueEntity = null;

		// keyに紐づくキャッシュが存在しない場合は更新しない。
		if (MonitorSnmpCache.getMonitorSnmpValue(m_monitorId, facilityId) == null) return;
		valueEntity = new MonitorSnmpValue(new MonitorSnmpValuePK(m_monitorId, facilityId));
		MonitorSnmpCache.update(m_monitorId, facilityId, valueEntity);
	}

	@Override
	protected String makeJobOrgMessage(String orgMsg, String msg) {
		String[] args = {""};
		if(m_convertFlg == ConvertValueConstant.TYPE_NO){
			// 何もしない
			args[0] = MessageConstant.CONVERT_NO.getMessage();
		} else if (m_convertFlg == ConvertValueConstant.TYPE_DELTA) {
			// 差分をとる
			args[0] = MessageConstant.DELTA.getMessage();
		}
		return MessageConstant.MESSAGE_JOB_MONITOR_ORGMSG_SNMP_N.getMessage(args)
				+ "\n" + orgMsg
				+ "\n" + msg;
	}
}
