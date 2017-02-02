package com.clustercontrol.agent.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.fault.HinemosUnknown;

public class PropertiesFileUtil {
	/** ログ出力のインスタンス。 */
	private static Log m_log = LogFactory.getLog(PropertiesFileUtil.class);

	/** ログファイルの中身 */
	private static String m_propFile = "";

	/**
	 * Agent.Propertiesファイルのバックアップを取得後、指定したキーの値を書き換える
	 * 
	 * @param propFileName
	 * @param key
	 * @param value
	 */
	public static void replacePropertyFile(String propFileName, String key,
			String beforeValue, String afterValue) throws HinemosUnknown {
		m_log.debug("replace property file : " + propFileName);
		PrintWriter pw = null;
		boolean replaceFlag = false;
		LineNumberReader lnr = null;
		try {
			lnr = new LineNumberReader(new BufferedReader(
					new InputStreamReader(new FileInputStream(propFileName),
							"UTF-8")));
			// AgentPropertiesに既にキーがある場合は値を置換する
			// ない場合はキーと値を追加する
			if (beforeValue.equals(AgentProperties.getProperty(key))) {
				if (replaceLine(lnr, key, afterValue)) {
					replaceFlag = true;
				}
			} else {
				addLine(lnr, key, afterValue);
				replaceFlag = true;
			}
		} catch (Exception e) {
			m_log.warn(e.getMessage());
			throw new HinemosUnknown(propFileName, e);
		} finally {
			if (lnr != null)
				try {
					lnr.close();
				} catch (IOException e) {
					m_log.error(e.getMessage());
					throw new HinemosUnknown(propFileName, e);
				}
		}

		if (replaceFlag) {
			String backupPropFileName = propFileName + "_bak";
			File srcFile = new File(propFileName);
			File dstFile = new File(backupPropFileName);
			if (dstFile.exists()) {
				m_log.info("delete backup file : " + backupPropFileName);
				if (!dstFile.delete())
					m_log.warn("can not delete backup file : " + backupPropFileName);
			}
			m_log.info("move \"" + srcFile.getPath() + "\" to \""
					+ dstFile.getPath() + "\"");
			if (srcFile.renameTo(dstFile)) {
				m_log.info("rename success");
			} else {
				m_log.warn("rename failure");
			}

			try {
				pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(
						new FileOutputStream(propFileName), "UTF-8")));
				pw.print(m_propFile);
			} catch (Exception e) {
				m_log.warn(e.getMessage());
				throw new HinemosUnknown(propFileName, e);
			} finally {
				try {
					if (pw != null) {
						pw.close();
					}
				} catch (Exception e) {
					m_log.error(e.getMessage());
					throw new HinemosUnknown(propFileName, e);
				}
			}
		}
	}

	private static boolean replaceLine(LineNumberReader in, String key, String afterValue)
			throws IOException {
		String line;
		m_propFile = "";
		boolean ret = false;
		while ((line = in.readLine()) != null) {
			if (line.startsWith(key)) {
				m_log.info("replace the line : \"" + line + "\" to \"" + key
						+ "=" + afterValue + "\"");
				m_propFile = m_propFile + "#" + line + "\r\n";
				m_propFile = m_propFile + key + "=" + afterValue + "\r\n";
				ret = true;
			} else {
				m_propFile = m_propFile + line + "\r\n";
			}
		}
		return ret;
	}
	
	private static void addLine(LineNumberReader in, String key, String addValue) throws IOException {
		String line;
		m_propFile = "";
		while ((line = in.readLine()) != null) {
			m_propFile = m_propFile + line + "\r\n";
		}
		m_propFile = m_propFile + key + "=" + addValue + "\r\n";
		m_log.info("add the line : " + key + "=" + addValue);
	}
}
