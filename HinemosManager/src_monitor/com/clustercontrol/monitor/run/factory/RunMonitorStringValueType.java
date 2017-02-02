/*

Copyright (C) 2006 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.monitor.run.factory;

import java.util.Collection;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.monitor.run.model.MonitorJudgementInfo;
import com.clustercontrol.monitor.run.model.MonitorStringValueInfo;
import com.clustercontrol.monitor.run.util.QueryUtil;

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
		m_log.debug("setJudgementInfo() start");

		// 文字列監視判定値、ログ出力メッセージ情報を取得
		Collection<MonitorStringValueInfo> ct
		= QueryUtil.getMonitorStringValueInfoFindByMonitorId(m_monitorId, ObjectPrivilegeMode.NONE);
		Iterator<MonitorStringValueInfo> itr = ct.iterator();

		m_judgementInfoList = new TreeMap<Integer, MonitorJudgementInfo>();
		MonitorStringValueInfo entity = null;
		while(itr.hasNext()){

			entity = itr.next();
			MonitorJudgementInfo monitorJudgementInfo = new MonitorJudgementInfo();
			monitorJudgementInfo.setMonitorId(entity.getId().getMonitorId());
			monitorJudgementInfo.setPriority(entity.getPriority());
			monitorJudgementInfo.setMessage(entity.getMessage());
			monitorJudgementInfo.setCaseSensitivityFlg(entity.getCaseSensitivityFlg());
			monitorJudgementInfo.setDescription(entity.getDescription());
			monitorJudgementInfo.setPattern(entity.getPattern());
			monitorJudgementInfo.setProcessType(entity.getProcessType());
			monitorJudgementInfo.setValidFlg(entity.getValidFlg());
			m_judgementInfoList.put(entity.getId().getOrderNo(), monitorJudgementInfo);

			if(m_log.isDebugEnabled()){
				m_log.debug("setJudgementInfo() MonitorStringValue OrderNo = " + entity.getId().getOrderNo().intValue());
				m_log.debug("setJudgementInfo() MonitorStringValue Description = " + entity.getDescription());
				m_log.debug("setJudgementInfo() MonitorStringValue Pattern = " + entity.getPattern());
				m_log.debug("setJudgementInfo() MonitorStringValue ProcessType = " + entity.getProcessType().booleanValue());
				m_log.debug("setJudgementInfo() MonitorStringValue Priority = " + entity.getPriority().intValue());
				m_log.debug("setJudgementInfo() MonitorStringValue Message = " + entity.getMessage());
				m_log.debug("setJudgementInfo() MonitorStringValue CaseSensitivityFlg = " + entity.getCaseSensitivityFlg());
				m_log.debug("setJudgementInfo() MonitorStringValue ValidFlg = " + entity.getValidFlg());
			}
		}

		m_log.debug("setJudgementInfo() end");
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
