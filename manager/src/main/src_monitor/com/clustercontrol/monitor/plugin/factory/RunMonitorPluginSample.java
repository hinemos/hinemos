package com.clustercontrol.monitor.plugin.factory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.fault.FacilityNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.monitor.run.factory.RunMonitor;
import com.clustercontrol.monitor.run.factory.RunMonitorNumericValueType;

public class RunMonitorPluginSample extends RunMonitorNumericValueType {

	private static Log m_log = LogFactory.getLog( RunMonitorPluginSample.class );

	/** 不明メッセージ */
	private String m_unKnownMessage = null;

	/** メッセージ **/
	private String m_message = null;

	/** オリジナルメッセージ */
	private String m_messageOrg = null;

	/**
	 * コンストラクタ
	 */
	public RunMonitorPluginSample() {
		super();
	}

	/**
	 *  マルチスレッドを実現するCallableTaskに渡すためのインスタンスを作成するメソッド
	 */
	@Override
	protected RunMonitor createMonitorInstance() throws HinemosUnknown {
		return new RunMonitorPluginSample();
	}

	/**
	 * 監視のメイン処理
	 */
	@Override
	public boolean collect(String facilityId) throws FacilityNotFound,
			HinemosUnknown {
		m_log.info("collect() facilityId = " + facilityId);

		// 説明:
		// 参考：RunMonitorSql(SQL監視-数値)
		//
		// 概要：
		// 1.引数のファシリティID(facilityId)に対する監視を行う
		// 2.監視結果を以下の変数に設定する


		// set Generation Date
		if (m_now != null) {
			m_nodeDate = m_now.getTime();
		}

		m_value = 10;//監視対象の数値
		m_message = "sample message";
		m_messageOrg = "sample original message";
		m_unKnownMessage = "sample unknown message";//不明時
		boolean result = true;//監視が成功か否か

		return result;
	}

	@Override
	protected void setCheckInfo() throws MonitorNotFound {
	}

	/**
	 * メッセージ
	 */
	@Override
	public String getMessage(int key) {
		if(m_message == null || "".equals(m_message)){
			return m_unKnownMessage;
		}
		return m_message;
	}

	/**
	 * オリジナルメッセージ
	 */
	@Override
	public String getMessageOrg(int key) {
		return m_messageOrg;
	}

}
