/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.agent.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.util.BinaryUtil;
import com.clustercontrol.util.FileUtil;

/**
 * Agent.propertiesファイルに設定されているプロパティ値を
 * 取得するためのユーティリティクラス
 */
public class AgentProperties {
	/** ログ出力のインスタンス。 */
	private static Log m_log = LogFactory.getLog( AgentProperties.class );

	private static final Properties properties = new Properties();

	private static String propFileStr = null;

	private AgentProperties(){
	}

	/**
	 * Agent.propertiesファイル読み込みと初期化
	 * @param propFileName
	 */
	public static void init(String propFileName){
		m_log.debug("init() : propFileName = " + propFileName);
		FileInputStream inputStream = null;
		try {
			inputStream = new FileInputStream(propFileName);

			// プロパティファイルからキーと値のリストを読み込みます
			properties.load(inputStream);
			propFileStr = propFileName;
		} catch (FileNotFoundException e) {
			m_log.error(e.getMessage(), e);
		} catch (IOException e) {
			m_log.error(e.getMessage(), e);
		} finally {
			if(inputStream != null){
				try {
					inputStream.close();
				} catch (IOException e) {
					m_log.error(e.getMessage(), e);
				}
			}
		}
	}

	/**
	 * 指定されたキーを持つプロパティを、プロパティリストから探します。
	 * そのキーがプロパティリストにないと、デフォルトのプロパティリスト、
	 * さらにそのデフォルト値が繰り返し調べられます。
	 * そのプロパティが見つからない場合は、null が返されます。
	 * @param key プロパティキー
	 * @return 指定されたキー値を持つこのプロパティリストの値
	 */
	public static String getProperty(String key){
		m_log.debug(key + " = " + properties.getProperty(key));
		return properties.getProperty(key);
	}

	/**
	 * 指定されたキーを持つプロパティを、プロパティリストから探します。
	 * そのキーがプロパティリストにないと、デフォルトのプロパティリスト、
	 * さらにそのデフォルト値が繰り返し調べられます。
	 * そのプロパティが見つからない場合は、デフォルト値の引数が返されます。
	 * @param key プロパティキー
	 * @param defaultValue デフォルト値
	 * @return 指定されたキー値を持つこのプロパティリストの値
	 */
	public static String getProperty(String key, String defaultValue){
		m_log.debug(key + " = " + properties.getProperty(key) + ", defaultValue = " + defaultValue);
		return properties.getProperty(key, defaultValue);
	}

	/**
	 * Agent.propertiesのプロパティ情報をそのまま渡す
	 * @return
	 */
	public static Properties getProperties(){
		m_log.debug("getProperties() : call");
		return properties;
	}

	/**
	 * 指定されたキーと値をプロパティリストにセットする
	 * 
	 * @param key
	 * @param value
	 */
	public static void setProperty(String key, String value) {
		if (m_log.isDebugEnabled()) {
			m_log.debug("key:" + key + ", value:" + value);
		}
		properties.setProperty(key, value);
	}

	/**
	 * 指定されたキーと値をプロパティリストにセットしてファイルのプロパティ値も更新する. <br>
	 * 古い値はコメント化して新しい値をコメントの下に追記する.
	 * 
	 * @param key
	 * @param value
	 */
	public static void updateProperty(String key, String value) {

		if (key == null || key.isEmpty() || value == null) {
			m_log.warn("failed to update Agent.properties, because key or value is null.");
			return;
		}
		setProperty(key, value);

		if (propFileStr == null || propFileStr.isEmpty()) {
			m_log.warn("failed to update Agent.properties, because 'propFileStr' is null.");
			return;
		}

		// properties.store()だとコメント等が消えてしまうので前の値のコメント化と新しい値の追記.
		Path propFilePath = new File(propFileStr).toPath();
		FileChannel fc = null;
		try {
			fc = FileChannel.open(propFilePath, StandardOpenOption.READ, StandardOpenOption.WRITE);

			// キーの1文字分のバイト数を求めて読込最小単位とする.
			String propertyChar = getProperty("common.property.charset", "UTF-8");
			ArrayList<Byte> keyBinary = new ArrayList<Byte>(BinaryUtil.arrayToList(key.getBytes(propertyChar)));
			int keyCharByte = keyBinary.size() / key.length();
			int keyCount = key.length();
			if ((keyBinary.size() % key.length()) > 0) {
				keyCount = keyCount + 1;
			}

			// コメント化に必要な変数.
			ArrayList<Byte> preChar = new ArrayList<Byte>();
			ArrayList<Byte> readKey = new ArrayList<Byte>();
			String commentMark = "#";
			byte[] commentByteArray = commentMark.getBytes(propertyChar);
			ArrayList<Byte> commentBinary = new ArrayList<Byte>(BinaryUtil.arrayToList(commentByteArray));
			long lastCommentEnd = 0L;

			// 1文字ずつ読込んでキーと一致するバイナリを探索してコメント化.
			insertComment: while (true) {
				ByteBuffer tmpBuf = ByteBuffer.allocate(keyCharByte);
				int reading = fc.read(tmpBuf);
				// ファイル終端.
				if (reading < 0) {
					break insertComment;
				}

				// 読込んだ文字列をバイナリ変換.
				ArrayList<Byte> readChar = new ArrayList<Byte>(BinaryUtil.arrayToList(tmpBuf.array()));
				long readedAfterTop = fc.position();

				// キーのバイナリと前方一致する場合はキーの文字数分(1文字目は読込済)読込んで判定.
				keySearch: if (readKey.isEmpty() && BinaryUtil.forwardMatch(keyBinary, readChar)) {
					readKey.addAll(readChar);
					keyAdd: for (int i = 0; i < keyCount - 1; i++) {
						tmpBuf = ByteBuffer.allocate(keyCharByte);
						reading = fc.read(tmpBuf);
						if (reading < 0) {
							break keyAdd;
						}
						ArrayList<Byte> readKeyChar = new ArrayList<Byte>(BinaryUtil.arrayToList(tmpBuf.array()));
						readKey.addAll(readKeyChar);
					}
					// キーではなかったので初期化して次の文字へ.
					if (!BinaryUtil.equals(keyBinary, readKey)) {
						readKey.clear();
						fc.position(readedAfterTop);
						break keySearch;
					}

					// コメント化されてるキーなので改行位置を取得して次の行へ.
					if (commentBinary.equals(preChar)) {
						lastCommentEnd = FileUtil.searchLineEnd(fc, propertyChar);
						readKey.clear();
						fc.position(lastCommentEnd);
						break keySearch;
					}

					// コメント化が必要なので末尾から読込んでコメントサイズ分後ろにずらす形でコメント化して終了.
					FileUtil.insertWords(fc, readedAfterTop - keyCharByte, commentMark, propertyChar);
					lastCommentEnd = FileUtil.searchLineEnd(fc, propertyChar);
					m_log.info(String.format("succeded to insert '%s' as comment into Agent.properties. key:%s",
							commentMark, key));
					break insertComment;
				}
				// 次の文字へ.
				preChar = readChar;
			}

			// 旧keyのコメント行の次行に挿入、旧keyない場合はファイル末尾に追記.
			String newValueSet = "\n# added automatically the following property by Agent.\n" + key + "=" + value
					+ "\n";
			if (lastCommentEnd > 0) {
				fc.position(lastCommentEnd);
			} else {
				fc.position(fc.size());
			}
			FileUtil.insertWords(fc, fc.position(), newValueSet, propertyChar);

		} catch (FileNotFoundException e) {
			m_log.error(e.getMessage(), e);
			return;
		} catch (IOException e) {
			m_log.error(e.getMessage(), e);
			return;
		} catch (InvalidSetting e) {
			m_log.error(e.getMessage(), e);
			return;
		} finally {
			if (fc != null) {
				try {
					fc.close();
				} catch (IOException e) {
					m_log.error(e.getMessage(), e);
				}
			}
		}
		m_log.info("succeded to update Agent.properties. key:" + key + ", value:" + value);
	}
}
