/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.util;

import java.io.File;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class FileUtil {

	// クラス共通フィールド.
	/** ロガー */
	private static Log log = LogFactory.getLog(FileUtil.class);
	/** ログ出力区切り文字 */
	private static final String DELIMITER = "() : ";

	/**
	 * ディレクトリリスト追加(サブディレクトリ含む).<br>
	 * <br>
	 * 
	 * 引数指定のList{@literal <File>}にディレクトリを追加.
	 * 
	 * @param dir
	 *            探索対象ディレクトリ.
	 * @param dirList
	 *            追加先ディレクトリリスト.
	 * @param max
	 *            上限ディレクトリ数.
	 * 
	 */
	public static void addDirList(File dir, List<File> dirList, long max) {

		File[] fileDirArray = dir.listFiles();
		if (fileDirArray == null || fileDirArray.length <= 0 || dirList == null) {
			return;
		} else if (dirList.size() >= max) {
			return;
		} else {
			// 取得したファイルリストがディレクトリか判定.
			for (File fileDir : fileDirArray) {
				if (!fileDir.exists()) {
					// 存在しない場合は無視.
					continue;
				} else if (fileDir.isDirectory()) {
					// ディレクトリの場合はサブディレクトリ検索.
					dirList.add(fileDir);
					addDirList(fileDir, dirList, max);
				} else {
					// ファイルの場合は無視.
					continue;
				}
			}
			// ループ終わったら処理終了.
			return;
		}
	}

	/**
	 * ファイルリスト追加.<br>
	 * <br>
	 * 引数指定のList{@literal <File>}にファイルを追加.<br>
	 * ※ディレクトリは追加しない.<br>
	 * 
	 * @param dir
	 *            探索対象ディレクトリ.
	 * @param dirList
	 *            追加先ファイルリスト.
	 * @param namePattern
	 *            検索対象ファイル名(正規表現)、null/空文字の場合は全件取得.
	 * @param max
	 *            上限ファイル数.
	 * @param inSubdir
	 *            サブディレクトリ含む場合true.
	 * 
	 */
	public static void addFileList(File dir, List<File> fileList, String namePattern, long max, boolean inSubdir) {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();

		// 監視対象ファイル検索用のパターン作成
		Pattern pattern = null;
		if (namePattern != null && !namePattern.isEmpty()) {
			try {
				pattern = Pattern.compile(namePattern, Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
			} catch (Exception e) {
				// 不正ファイルパターン、処理が継続できないので、処理を戻す.
				log.warn(methodName + DELIMITER + e.getMessage(), e);
				return;
			}
		}

		File[] fileDirArray = dir.listFiles();
		if (fileDirArray == null || fileDirArray.length <= 0) {
			return;
		} else if (fileList.size() >= max) {
			return;
		} else {
			// 取得したファイルリストがディレクトリか判定.
			for (File fileDir : fileDirArray) {
				if (!fileDir.exists()) {
					// 存在しない場合は無視.
					continue;
				} else if (fileDir.isDirectory()) {
					// ディレクトリの場合はサブディレクトリ検索.
					if (inSubdir) {
						addFileList(fileDir, fileList, namePattern, max, inSubdir);
					}
				} else {
					// ファイルの場合は追加.
					if (namePattern != null && !namePattern.isEmpty()) {
						Matcher matcher = pattern.matcher(fileDir.getName());
						if (!matcher.matches()) {
							// パターンマッチしなかった場合はskip.
							log.debug(methodName + DELIMITER + String.format("don't match. filename=%s, pattern=%s",
									fileDir.getName(), namePattern));
							continue;
						}
					}
					fileList.add(fileDir);
					continue;
				}
			}
			// ループ終わったら処理終了.
			return;
		}
	}

	/**
	 * long値ゼロパディング.<br>
	 * <br>
	 * 引数で渡したlong値を指定の桁数になるよう0パディングした文字列を返却する.<br>
	 * ※ファイル名に連番等をつけたい場合を想定したutil.<br>
	 * 
	 * @param paddingValue
	 *            パディング対象のlong値.
	 * @param keta
	 *            パディング後の桁数.
	 * 
	 * @return 0パディングされた文字列(指定のlong値が意図した桁数以上の場合はそのまま文字列変換して返却).
	 * 
	 */
	public static String paddingZero(long paddingValue, int keta) {

		if (String.valueOf(paddingValue).length() >= keta) {
			// 指定のlong値の桁数が意図した桁数以上の場合はそのまま文字列変換して返却.
			return String.valueOf(paddingValue);
		}

		StringBuffer sbuf = new StringBuffer(keta);
		int zeroKeta = keta - String.valueOf(paddingValue).length();
		for (int i = 0; i < zeroKeta; i++) {
			sbuf.append("0");
		}
		sbuf.append(String.valueOf(paddingValue));
		return sbuf.toString();
	}

	/**
	 * ファイル名取得.<br>
	 * <br>
	 * Linux/Windowsのファイルパスからファイル名のみを取得する.<br>
	 * ※java.io.File.getNameはシステム依存するため、こちらの部品を使う
	 * 
	 * @param filePath
	 *            ファイルパス(Linux/Windows)
	 * 
	 * @return ファイル名、引数不正はnull返却.
	 * 
	 */
	public static String getFileName(String filePath) {

		// 引数チェック.
		if (filePath == null || filePath.isEmpty()) {
			return null;
		}

		String onlyFileName = null;
		int lastIndex = -1;

		// Windowsの場合はファイル名に"/"不可なので先に"/"(Linuxファイルパス)判定.
		lastIndex = filePath.lastIndexOf("/");
		if (lastIndex > 0) {
			onlyFileName = filePath.substring(lastIndex + 1);
			return onlyFileName;
		}

		// "/"が含まれていない場合は"\"(Windwsファイルパス)判定.
		lastIndex = filePath.lastIndexOf("\\");
		if (lastIndex > 0) {
			onlyFileName = filePath.substring(lastIndex + 1);
			return onlyFileName;
		}

		// 区切り文字が含まれていない場合はファイルパスではなくファイル名を渡されたと判断.
		return filePath;
	}

	/**
	 * ファイル名変換.<br>
	 * <br>
	 * ファイル名に使用不可の記号を引数指定の文字列に変換する.<br>
	 * 拡張子・Soap通信時にエラーの原因となる記号も変換対象に含める.<br>
	 * 
	 * @param inName
	 *            変換対象の文字列.
	 * @param replaceStr
	 *            ファイル名使用不可の記号の変換.
	 * 
	 * @return ファイル名として変換された文字列、引数不正はnull返却.
	 * 
	 */
	public static String fittingFileName(String inName, String replaceStr) {

		// 引数チェック.
		if (inName == null || inName.isEmpty() || replaceStr == null || replaceStr.isEmpty()) {
			return null;
		}

		String fileName = "";
		StringBuffer sbuf = new StringBuffer(fileName.length());

		// ファイル名変換.
		for (int i = 0; i < inName.length(); i++) {
			// 文字列判定.
			if (inName.charAt(i) == '\\' || inName.charAt(i) == ':' || inName.charAt(i) == '*'
					|| inName.charAt(i) == '?' || inName.charAt(i) == '.' || inName.charAt(i) == '<'
					|| inName.charAt(i) == '>' || inName.charAt(i) == '|' || inName.charAt(i) == '\n'
					|| inName.charAt(i) == '\t' || inName.charAt(i) == ' ' || inName.charAt(i) == '%') {
				// ファイル名に利用できない文字列の場合は置換.
				sbuf.append(replaceStr);
			} else {
				// 利用できる文字列なのでそのまま.
				sbuf.append(inName.charAt(i));
			}
		}
		fileName = sbuf.toString();

		return fileName;
	}
}
