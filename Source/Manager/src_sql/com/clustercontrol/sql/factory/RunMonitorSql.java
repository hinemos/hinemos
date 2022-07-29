/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.sql.factory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.monitor.run.factory.RunMonitor;
import com.clustercontrol.monitor.run.factory.RunMonitorNumericValueType;
import com.clustercontrol.repository.util.RepositoryUtil;
import com.clustercontrol.sql.model.SqlCheckInfo;
import com.clustercontrol.sql.util.AccessDB;
import com.clustercontrol.sql.util.QueryUtil;
import com.clustercontrol.util.MessageConstant;
import com.clustercontrol.util.StringBinder;

/**
 * SQL監視 数値監視設定を実行するファクトリークラス<BR>
 *
 * @version 4.0.0
 * @since 2.0.0
 */
public class RunMonitorSql extends RunMonitorNumericValueType {

	private static Log m_log = LogFactory.getLog( RunMonitorSql.class );

	/** SQL監視情報 */
	private SqlCheckInfo m_sql = null;

	/** 接続文字列 */
	private String m_url = null;

	/** ユーザ */
	private String m_user = null;

	/** パスワード */
	private String m_password = null;

	/** クエリ */
	private String m_query = null;

	/** JDBCドライバ */
	private String m_jdbcDriver = null;

	/** 不明メッセージ */
	private String m_unKnownMessage = null;

	/** メッセージ **/
	private String m_message = null;

	/** オリジナルメッセージ */
	private String m_messageOrg = null;

	/**
	 * コンストラクタ
	 * 
	 */
	public RunMonitorSql() {
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
		return new RunMonitorSql();
	}

	/**
	 * SQL数を取得
	 * 
	 * @param facilityId ファシリティID
	 * @return 値取得に成功した場合、true
	 */
	@Override
	public boolean collect(String facilityId) {
		// set Generation Date
		if (m_now != null) {
			m_nodeDate = m_now.getTime();
		}

		boolean result = false;

		AccessDB access = null;
		ResultSet rSet = null;

		String url = m_url;

		try {
			// 変数を置換したURLの生成
			if (nodeInfo != null && nodeInfo.containsKey(facilityId)) {
				int maxReplaceWord = HinemosPropertyCommon.replace_param_max.getIntegerValue().intValue();
				ArrayList<String> inKeyList = StringBinder.getKeyList(m_url, maxReplaceWord);
				Map<String, String> nodeParameter = RepositoryUtil.createNodeParameter(nodeInfo.get(facilityId), inKeyList);
				StringBinder strbinder = new StringBinder(nodeParameter);
				url = strbinder.bindParam(m_url);
				if (m_log.isTraceEnabled()) m_log.trace("jdbc request. (nodeInfo = " + nodeInfo + ", facilityId = " + facilityId + ", url = " + url + ")");
			}

			// DB接続初期処理
			access = new AccessDB(
					m_jdbcDriver,
					url,
					m_user,
					m_password);

			// SQL文を実行し、結果を取り出す。
			if(m_query.length() >= 6){
				String work = m_query.substring(0, 6);
				if( work.equalsIgnoreCase("SELECT")){
					rSet = access.read(m_query);

					//1レコード目の1カラム目のデータを取得
					rSet.first();
					double count = rSet.getDouble(1);
					m_value = count;

					//レコード件数を取得
					rSet.last();
					int number = rSet.getRow();

					NumberFormat numberFormat = NumberFormat.getNumberInstance();
					m_message = MessageConstant.SELECT_VALUE.getMessage() + " : " + m_value;
					m_messageOrg = MessageConstant.RECORD_VALUE.getMessage() + " : " + numberFormat.format(m_value) + ", " +
							MessageConstant.RECORDS_NUMBER.getMessage() + " : " + numberFormat.format(number);
					m_messageOrg += "\n" + MessageConstant.CONNECTION_URL.getMessage() + " : " + url;

					result = true;
				}
				else{
					//SELECT文以外はエラー
					m_log.info("collect(): " + MessageConstant.MESSAGE_PLEASE_SET_SELECT_STATEMENT_IN_SQL.getMessage());
					m_unKnownMessage = MessageConstant.MESSAGE_PLEASE_SET_SELECT_STATEMENT_IN_SQL.getMessage();
					m_messageOrg = MessageConstant.SQL_STRING.getMessage() + " : " + m_query;
					m_messageOrg += "\n" + MessageConstant.CONNECTION_URL.getMessage() + " : " + url;
				}
			}
			else{
				//SELECT文以外はエラー
				m_log.info("collect(): " + MessageConstant.MESSAGE_PLEASE_SET_SELECT_STATEMENT_IN_SQL.getMessage());
				m_unKnownMessage = MessageConstant.MESSAGE_PLEASE_SET_SELECT_STATEMENT_IN_SQL.getMessage();
				m_messageOrg = MessageConstant.SQL_STRING.getMessage() + " : " + m_query;
				m_messageOrg += "\n" + MessageConstant.CONNECTION_URL.getMessage() + " : " + url;
			}
		} catch (ClassNotFoundException e) {
			m_log.debug("collect() : " + e.getClass().getSimpleName() + ", " + e.getMessage());
			m_unKnownMessage = MessageConstant.MESSAGE_CANNOT_FIND_JDBC_DRIVER.getMessage();
			m_messageOrg = MessageConstant.SQL_STRING.getMessage() + " : " + m_query + " (" + e.getMessage() + ")";
			m_messageOrg += "\n" + MessageConstant.CONNECTION_URL.getMessage() + " : " + url;
		} catch (SQLException e) {
			// SQL実行エラー
			m_log.info("collect() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			m_unKnownMessage = MessageConstant.MESSAGE_FAILED_TO_EXECUTE_SQL.getMessage();
			m_messageOrg = MessageConstant.SQL_STRING.getMessage() + " : " + m_query + " (" + e.getMessage() + ")";
			m_messageOrg += "\n" + MessageConstant.CONNECTION_URL.getMessage() + " : " + url;
		} finally {
			try {
				if(rSet != null){
					rSet.close();
				}
				if(access != null){
					// DB接続終了処理
					access.terminate();
				}
			} catch (SQLException e) {
				m_log.warn("collect() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			}
		}
		return result;
	}

	/* (non-Javadoc)
	 * SQL監視情報を設定
	 * @see com.clustercontrol.monitor.run.factory.OperationNumericValueInfo#setMonitorAdditionInfo()
	 */
	@Override
	protected void setCheckInfo() throws MonitorNotFound {

		// SQL監視情報を取得
		if (!m_isMonitorJob) {
			m_sql = QueryUtil.getMonitorSqlInfoPK(m_monitorId);
		} else {
			m_sql = QueryUtil.getMonitorSqlInfoPK(m_monitor.getMonitorId());
		}

		// SQL監視情報を設定
		m_url = m_sql.getConnectionUrl().trim();
		m_user = m_sql.getUser().trim();
		m_password = m_sql.getPassword().trim();
		m_query = m_sql.getQuery().trim();
		m_jdbcDriver = m_sql.getJdbcDriver().trim();
	}

	/* (非 Javadoc)
	 * ノード用メッセージを取得
	 * @see com.clustercontrol.monitor.run.factory.RunMonitor#getMessage(int)
	 */
	@Override
	public String getMessage(int id) {

		if(m_message == null || "".equals(m_message)){
			return m_unKnownMessage;
		}
		return m_message;
	}

	/* (非 Javadoc)
	 * ノード用オリジナルメッセージを取得
	 * @see com.clustercontrol.monitor.run.factory.RunMonitor#getMessageOrg(int)
	 */
	@Override
	public String getMessageOrg(int id) {
		return m_messageOrg;
	}

	@Override
	protected String makeJobOrgMessage(String orgMsg, String msg) {
		if (m_monitor == null || m_monitor.getSqlCheckInfo() == null) {
			return "";
		}
		String[] args = {
				String.valueOf(m_monitor.getSqlCheckInfo().getJdbcDriver()),
				String.valueOf(m_monitor.getSqlCheckInfo().getUser()),
				String.valueOf(m_monitor.getSqlCheckInfo().getQuery())};
		return MessageConstant.MESSAGE_JOB_MONITOR_ORGMSG_SQL.getMessage(args)
				+ "\n" + orgMsg;
	}
}
