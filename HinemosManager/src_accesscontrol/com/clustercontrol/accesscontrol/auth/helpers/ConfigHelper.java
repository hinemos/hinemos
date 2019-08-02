/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.accesscontrol.auth.helpers;

import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.function.Function;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 設定情報を扱うためのヘルパーメソッドを提供します。
 *
 */
public class ConfigHelper {
	private static final Log log = LogFactory.getLog(ConfigHelper.class);

	/** パスワードなど秘匿が必要な値のログ出力はこのマスクパターンに置換されます */
	private static final String MASK_TEXT = "*****";
	
	private ConfigHelper() {
		// インスタンス生成禁止
	}

	/**
	 * プロパティファイルを読み込んで、指定されたオブジェクトのpublicフィールドへセットします。
	 * 
	 * @param container このオブジェクトのpublicフィールドへ値をセットします。
	 * @param path プロパティファイルのパス。
	 * @param filedNameMapper プロパティのキー名を引数に取り、値をセットすべきフィールド名を返す関数です。
	 *                        マッピングエラーの場合はnullを返してください。
	 * @param secretFieldRegex この正規表現パターンにマッチするフィールド名への値はログ出力時にマスクします。
	 * @throws LoadFailedException 読み込みに失敗しました。
	 */
	public static void loadProperties(Object container, Path path, Function<String, String> filedNameMapper,
			String secretFieldRegex) {
		try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
			loadProperties(container, reader, filedNameMapper, secretFieldRegex);
		} catch (IOException e) {
			log.warn("loadStandardProperties: Failed to open. [" + path + "] "
					+ e.getClass().getName() + ", " + e.getMessage());
			throw new LoadFailedException(e);
		}
	}
	
	/**
	 * プロパティファイルを読み込んで、指定されたオブジェクトのpublicフィールドへセットします。
	 * 
	 * @param container このオブジェクトのpublicフィールドへ値をセットします。
	 * @param reader プロパティファイルを読み込むためのReader。クローズは読み出し元で行ってください。
	 * @param filedNameMapper プロパティのキー名を引数に取り、値をセットすべきフィールド名を返す関数です。
	 *                        マッピングエラーの場合はnullを返してください。
	 * @param secretFieldRegex この正規表現パターンにマッチするフィールド名への値はログ出力時にマスクします。
	 * @throws LoadFailedException 読み込みに失敗しました。
	 */
	public static void loadProperties(Object container, Reader reader, Function<String, String> filedNameMapper,
			String secretFieldRegex) {
		try {
			loadProperties0(container, reader, filedNameMapper, secretFieldRegex);
		} catch (Exception e) {
			log.warn("loadProperties: Abort, " + e.getClass().getName() + ", " + e.getMessage());
			throw new LoadFailedException(e);
		}
	}

	private static void loadProperties0(Object container, Reader reader, Function<String, String> filedNameMapper,
			String secretFieldRegex) throws IOException, IllegalAccessException {
		// プロパティファイル全体をロード
		Properties prop = new Properties();
		prop.load(reader);

		// フィールドのリストを作成
		Map<String, Field> fieldMap = new HashMap<>();
		for (Field f : container.getClass().getFields()) {
			fieldMap.put(f.getName(), f);
		}
		
		// マスクフィールド用Pattern
		Pattern secretFieldPattern = null;
		if (secretFieldRegex != null) {
			secretFieldPattern = Pattern.compile(secretFieldRegex);
		}

		// プロパティファイルに記載の設定ごとにループ
		for (Entry<Object, Object> entry : prop.entrySet()) {
			String key = entry.getKey().toString();
			String value = entry.getValue().toString();

			String fldName = filedNameMapper.apply(key);
			if (fldName == null) {
				log.warn("loadProperties: Invalid key. [" + key + "]");
				continue;
			}

			String logValue = value;
			if (secretFieldPattern != null && secretFieldPattern.matcher(fldName).matches()) {
				logValue = MASK_TEXT;
			}

			Field f = fieldMap.get(fldName);
			if (f != null) {
				try {
					f.set(container, convertType(f.getType(), value));
					log.info("loadProperties: Set. [" + fldName + " = " + logValue + "]");
				} catch (Throwable e) {
					log.warn("loadProperties: Failed to set. [" + fldName + " = " + logValue + "], "
							+ e.getClass().getName() + ", " + e.getMessage());
					throw e;
				}
			} else {
				log.warn("loadProperties: Field not found. [" + fldName + "]");
			}
		}
	}

	/**
	 * 文字列 value を type クラスの値に変換して返します。
	 */
	protected static Object convertType(Class<?> type, String value) {
		if (type == Integer.TYPE || type == Integer.class) {
			return Integer.valueOf(value);
		} else if (type == Long.TYPE || type == Long.class) {
			return Long.valueOf(value);
		} else if (type == Double.TYPE || type == Double.class) {
			return Double.valueOf(value);
		} else if (type == Boolean.TYPE || type == Boolean.class) {
			value = value.trim().toLowerCase(); 
			return Boolean.valueOf(value.equals("true") || value.equals("t") || value.equals("1")
					|| value.equals("on") || value.equals("yes"));
		} else if (type == String.class) {
			return value;
		} else {
			// この例外が出た場合は、タイプ変換の追加実装が必要です。
			throw new RuntimeException("Unsupported type, must be implemented! [" + type.getName() + "]");
		}
	}

	/**
	 * 設定ファイルの読み込み中に何らかのエラーが発生し、読み込みを完了できなかったことを表します。
	 */
	public static class LoadFailedException extends RuntimeException {
		private static final long serialVersionUID = 1L;

		public LoadFailedException() {
			super();
		}

		public LoadFailedException(String message, Throwable cause) {
			super(message, cause);
		}

		public LoadFailedException(String message) {
			super(message);
		}

		public LoadFailedException(Throwable cause) {
			super(cause);
		}
	}

}
