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

package com.clustercontrol.sql.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * DB操作クラス
 *
 * @version 2.1.0
 * @since 2.1.0
 */
public class AccessDB{
	private static Log m_log = LogFactory.getLog( AccessDB.class );

	/** Connectionへの参照 */
	private Connection m_connection = null;

	/** Statementへの参照 */
	private Statement m_statement = null;

	/** JDBCドライバ */
	private String m_jdbcDriver = null;

	/** 接続文字列 */
	private String m_url = null;

	/** ユーザ */
	private String m_user = null;

	/** パスワード */
	private String m_password = null;

	/** connect properties */
	private JdbcDriverProperties jdbcProps = null;

	/**
	 * コンストラクタ
	 *
	 * @param driver JDBCドライバ
	 * @param url 接続文字列
	 * @param user ユーザ
	 * @param password パスワード
	 * @throws SQLException
	 * @throws ClassNotFoundException
	 */
	public AccessDB(String driver, String url, String user, String password) throws SQLException, ClassNotFoundException {
		this.m_jdbcDriver = driver;
		this.m_url = url;
		this.m_user = user;
		this.m_password = password;
		this.jdbcProps = new JdbcDriverUtil().getJdbcDriverProperty(driver);

		initial();
	}

	/**
	 * DB接続の初期処理
	 *
	 * @throws SQLException
	 * @throws ClassNotFoundException
	 */
	private void initial() throws SQLException, ClassNotFoundException
	{
		//JDBCドライバのロード
		try {
			Class.forName(m_jdbcDriver);
		} catch (ClassNotFoundException e) {
			m_log.info("initial() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}

		Properties prop = jdbcProps.getProperties();
		prop.put("user", m_user);
		prop.put("password", m_password);

		try {
			if (jdbcProps.isLoginTimeoutEnable()) {
				DriverManager.setLoginTimeout(jdbcProps.getLoginTimeout());
				m_log.debug("enabled loginTimeout (" + jdbcProps.getLoginTimeout() + " [sec]) for \"" + m_url + "\".");
			} else {
				m_log.debug("disabled loginTimeout for \"" + m_url + "\".");
			}
			m_connection = DriverManager.getConnection(m_url, prop);

			//SQL文を実行するためのStatementクラスを作成
			m_statement = m_connection.createStatement(
					ResultSet.TYPE_SCROLL_INSENSITIVE,
					ResultSet.CONCUR_READ_ONLY);

		} catch (SQLException e) {
			m_log.info("initial() database access failure : url = " + m_url + ", : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			try {
				if(m_statement != null)
					m_statement.close();
			} catch (SQLException se) {
				m_log.info("initial() database closing failure : url = " + m_url + ", "
						+ se.getClass().getSimpleName() + ", " + se.getMessage());
			}
			try {
				if(m_connection != null)
					m_connection.close();
			} catch (SQLException se) {
				m_log.info("initial() database closing failure : url = " + m_url + ", "
						+ se.getClass().getSimpleName() + ", " + se.getMessage());
			}
			throw e;
		}
	}

	/**
	 * 指定されたSQL文を実行（読み込み）
	 *
	 * @param sql SQL文
	 * @return
	 * @throws SQLException
	 */
	public ResultSet read(String sql) throws SQLException
	{
		ResultSet result = null;
		try
		{
			result = m_statement.executeQuery(sql);

		}catch (SQLException e) {
			m_log.info("read() database query failure : url = " + m_url + ", "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		return result;
	}

	/**
	 * DB接続の終了処理
	 *
	 */
	public void terminate(){

		try {
			if(m_statement != null)
				m_statement.close();
		} catch (SQLException e) {
			m_log.info("terminate() database closing failure : url = " + m_url + ", "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
		}

		try {
			if(m_connection != null)
				m_connection.close();
		} catch (SQLException e) {
			m_log.info("terminate() database closing failure : url = " + m_url + ", "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
		}
	}
}