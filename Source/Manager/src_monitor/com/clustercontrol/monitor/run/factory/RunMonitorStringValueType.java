/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.monitor.run.factory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.monitor.run.bean.MonitorTypeConstant;
import com.clustercontrol.monitor.run.model.MonitorJudgementInfo;
import com.clustercontrol.monitor.run.util.MonitorJudgementInfoCache;

/**
 * 文字列監視を実行する抽象クラス<BR>
 * <p>
 * 文字列監視を行う各監視管理クラスで継承してください。
 *
 * @version 3.0.0
 * @since 2.1.0
 */
abstract public class RunMonitorStringValueType extends RunMonitor{

	/** ログ出力のインスタンス。 */
	private static Log m_log = LogFactory.getLog( RunMonitorStringValueType.class );

	/** 監視取得値 */
	protected String m_value;

	/**
	 * コンストラクタ。
	 */
	public RunMonitorStringValueType() {
		super();
	}

	/* (非 Javadoc)
	 * @see com.clustercontrol.monitor.run.factory.RunMonitor#collect(java.lang.String)
	 */
	@Override
	public abstract boolean collect(String facilityId) throws HinemosUnknown;

	@Override
	public int getCheckResult(boolean ret, Object value) {
		throw new UnsupportedOperationException("forbidden to call getCheckResult() method");
	}

	/**
	 * 判定結果を返します。
	 * <p>
	 * 判定情報マップにセットしてある各順序のパターンマッチ表現から、
	 * 監視取得値がどのパターンマッチ表現にマッチするか判定し、マッチした順序を返します。
	 * 
	 * @see com.clustercontrol.monitor.run.ejb.entity.MonitorStringValueInfoBean#getOrder_no()
	 * @see com.clustercontrol.monitor.run.bean.MonitorStringValueInfo
	 */
	@Override
	public int getCheckResult(boolean ret) {

		// -1 = 値取得失敗。　　-2 = どれにもマッチせず。
		int result = -2;

		// 値取得の失敗時
		if(!ret){
			result = -1;
			return result;
		}

		// 値取得の成功時
		Pattern pattern = null;
		Matcher matcher = null;

		int orderNo = 0;
		// 文字列監視判定情報で順番にフィルタリング
		for (MonitorJudgementInfo info: m_judgementInfoList.values()) {

			++orderNo;
			if(m_log.isDebugEnabled()){
				m_log.debug("getCheckResult() value = " + m_value
						+ ", monitorId = " + info.getMonitorId()
						+ ", orderNo = " + orderNo
						+ ", pattern = " + info.getPattern());
			}

			// この設定が有効な場合
			if (info != null && info.getValidFlg()) {
				try {
					String patternText = info.getPattern();

					// 大文字・小文字を区別しない場合
					if(info.getCaseSensitivityFlg()){
						pattern = Pattern.compile(patternText, Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
					}
					// 大文字・小文字を区別する場合
					else{
						pattern = Pattern.compile(patternText, Pattern.DOTALL);
					}
					if (m_value == null) {
						m_log.debug("getCheckResult(): monitorId=" + info.getMonitorId() + ", facilityId=" + m_facilityId +
								", value=null");
						result = -1;
						return result;
					}
					matcher = pattern.matcher(m_value);

					// パターンマッチ表現でマッチング
					if (matcher.matches()) {
						result = orderNo;

						m_log.debug("getCheckResult() true : description=" + info.getDescription() + ", value=" + m_value);
						m_log.debug("getCheckResult() true : message=" + info.getMessage());

						break;
					}
				} catch(PatternSyntaxException e){
					m_log.info("getCheckResult(): PatternSyntax is not valid." +
							" description="+info.getDescription() +
							", patternSyntax="+info.getPattern() + ", value=" + m_value + " : "
							+ e.getClass().getSimpleName() + ", " + e.getMessage());
					result = -1;
				} catch (Exception  e) {
					m_log.warn("getCheckResult(): PatternSyntax is not valid." +
							" description="+info.getDescription() +
							", patternSyntax="+info.getPattern() + ", value=" + m_value + " : "
							+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
					result = -1;
				}
			}
		}
		return result;
	}


	/**
	 * 判定情報を設定します。
	 * <p>
	 * <ol>
	 * <li>監視情報より判定情報を取得します。</li>
	 * <li>取得した文字列監視の判定情報を、順序をキーに判定情報マップにセットします。</li>
	 * </ol>
	 * 
	 * @see com.clustercontrol.monitor.run.ejb.entity.MonitorStringValueInfoBean#getOrder_no()
	 * @see com.clustercontrol.monitor.run.bean.MonitorStringValueInfo
	 */
	@Override
	protected void setJudgementInfo() {
		// 文字列監視判定値、ログ出力メッセージ情報を取得
		m_judgementInfoList = MonitorJudgementInfoCache.getMonitorJudgementMap(
				m_monitorId, MonitorTypeConstant.TYPE_STRING);
	}

	/**
	 * パターンマッチ表現を返します。
	 * 
	 * @param key 各監視種別（真偽値，数値，文字列）の判定結果のキー
	 * @return パターンマッチ表現
	 * @since 4.0.0
	 */
	public String getPatternText(int key){
		return m_judgementInfoList.get(key).getPattern();
	}

	/**
	 * 処理タイプを返します。
	 * 
	 * @param key 各監視種別（真偽値，数値，文字列）の判定結果のキー
	 * @return 処理タイプ
	 * @since 4.0.0
	 */
	public boolean getProcessType(int key){
		return m_judgementInfoList.get(key).getProcessType();
	}
}
