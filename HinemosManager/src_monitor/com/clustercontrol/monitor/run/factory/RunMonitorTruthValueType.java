/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.monitor.run.factory;

import com.clustercontrol.fault.FacilityNotFound;
import com.clustercontrol.fault.HinemosDbTimeout;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.monitor.run.bean.MonitorTypeConstant;
import com.clustercontrol.monitor.run.bean.TruthConstant;
import com.clustercontrol.monitor.run.util.MonitorJudgementInfoCache;

/**
 * 真偽値監視を実行する抽象クラス<BR>
 * <p>
 * 真偽値監視を行う各監視管理クラスで継承してください。
 *
 * @version 3.0.0
 * @since 2.0.0
 */
abstract public class RunMonitorTruthValueType extends RunMonitor{

	/** 監視取得値 */
	protected boolean m_value;

	/**
	 * コンストラクタ。
	 * 
	 */
	protected RunMonitorTruthValueType() {
		super();
	}

	@Override
	public int getCheckResult(boolean ret, Object value) {
		throw new UnsupportedOperationException("forbidden to call getCheckResult() method");
	}

	/* (非 Javadoc)
	 * @see com.clustercontrol.monitor.run.factory.RunMonitor#collect(java.lang.String)
	 */
	@Override
	public abstract boolean collect(String facilityId) throws FacilityNotFound, InvalidSetting, HinemosUnknown, HinemosDbTimeout;

	/**
	 * 判定結果を返します。
	 * <p>
	 * 監視取得値の真偽値定数を返します。
	 * 
	 * @see com.clustercontrol.monitor.run.bean.TruthConstant
	 */
	@Override
	public int getCheckResult(boolean ret) {

		int result = -1;
		//		int result = m_failurePriority;

		// 値取得の成功時
		if(ret){
			if(m_value){
				// 真
				result = TruthConstant.TYPE_TRUE;
			}
			else{
				// 偽
				result = TruthConstant.TYPE_FALSE;
			}
		}
		return result;
	}

	/**
	 * 判定情報を設定します。
	 * <p>
	 * <ol>
	 * <li>監視情報より判定情報を取得します。</li>
	 * <li>取得した真偽値監視の判定情報を、真偽値定数をキーに判定情報マップにセットします。</li>
	 * </ol>
	 * 
	 * @see com.clustercontrol.monitor.run.bean.TruthConstant
	 * @see com.clustercontrol.monitor.run.bean.MonitorTruthValueInfo
	 */
	@Override
	protected void setJudgementInfo() {
		// 真偽値監視判定値、ログ出力メッセージ情報を取得
		m_judgementInfoList = MonitorJudgementInfoCache.getMonitorJudgementMap(
				m_monitorId, MonitorTypeConstant.TYPE_TRUTH);
	}
}
