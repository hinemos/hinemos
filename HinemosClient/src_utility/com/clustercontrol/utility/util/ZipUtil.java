/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.fault.HinemosUnknown;

/**
 * ファイル圧縮を行うクラス<BR>
 *
 * @version 6.1.0
 * @since 5.0.a
 */
public class ZipUtil {

	private static Log m_log = LogFactory
			.getLog( ZipUtil.class );

	/**
	 * 指定されたファイルパスのリストを圧縮して1つのファイルに固める
	 * 
	 * @param inputFileNameList 圧縮対象のファイルパスのリスト
	 * @param outputFileName 出力先のファイルパス
	 * 
	 * @return 成功可否
	 */
	public static synchronized void archive(List<String> inputFileNameList, String outputFileName, String rootPath) throws Exception{
		m_log.debug("archive() output file = " + outputFileName);

		byte[] buf = new byte[128];
		BufferedInputStream in = null;
		ZipEntry entry = null;
		ZipOutputStream out = null;

		// 入力チェック
		if(outputFileName == null || "".equals(outputFileName)){
			HinemosUnknown e = new HinemosUnknown("archive output fileName is null ");
			m_log.info("archive() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		File zipFile = new File(outputFileName);
		if (zipFile.exists()) {
			if (zipFile.isFile() && zipFile.canWrite()) {
				m_log.debug("archive() output file = " + outputFileName + " is exists & file & writable. initialize file(delete)");
				if (!zipFile.delete())
					m_log.warn(String.format("Fail to delete file. %s", zipFile.getAbsolutePath()));
			}
			else{
				HinemosUnknown e = new HinemosUnknown("archive output fileName is directory or not writable ");
				m_log.info("archive() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
		}


		// メイン処理
		try {
			// 圧縮先ファイルへのストリームを開く
			out = new ZipOutputStream(new FileOutputStream(outputFileName));

			for(String inputFileName : inputFileNameList){
				m_log.debug("archive() input file name = " + inputFileName);

				// 圧縮元ファイルへのストリームを開く
				in = new BufferedInputStream(new FileInputStream(inputFileName));

				// エントリを作成する
				String fileName = inputFileName.substring(rootPath.length() + 1);
				m_log.debug("archive() entry file name = " + fileName);
				entry = new ZipEntry(fileName);

				out.putNextEntry(entry);
				// データを圧縮して書き込む
				int size;
				while ((size = in.read(buf, 0, buf.length)) != -1) {
					out.write(buf, 0, size);
				}
				// エントリと入力ストリームを閉じる
				out.closeEntry();
				in.close();
			}
			// 出力ストリームを閉じる out.flush();
			out.close();

		} catch (IOException e) {
			m_log.warn("archive() archive error : " + outputFileName
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			throw new HinemosUnknown("archive error " + outputFileName, e);

		} finally {

			if(in != null){
				try{
					in.close();
				} catch (IOException e) {
				}
			}

			if(out != null){
				try{
					out.close();
				} catch (IOException e) {
				}
			}
		}
	}

	/**
	 * 指定されたzipファイルを解凍して指定のディレクトリに展開する
	 * 
	 * @param zip 解凍対象のzipファイル
	 * @param decoPath 出力先のファイルパス
	 * 
	 * @return 成功可否
	 */
	public static synchronized void decompress(File zipFile, String decoPath) throws Exception{
		m_log.debug("decompress() input file = " + zipFile.getName());

		BufferedOutputStream out = null;
		BufferedInputStream in = null;
		ZipEntry entry = null;
		ZipFile zip = null;
		try {
			zip = new ZipFile(zipFile, ZipFile.OPEN_READ, Charset.forName("SJIS"));

			File baseDir = new File(decoPath);

			Enumeration<? extends ZipEntry> entries = zip.entries();

			while(entries.hasMoreElements()){
				entry = entries.nextElement();

				// 出力先ファイル
				File outFile = new File(baseDir, entry.getName());
				if (entry.isDirectory()) {
					// ZipEntry がディレクトリの場合はディレクトリを作成。
					if (!outFile.mkdirs())
						m_log.warn(String.format("Fail to create Directory. %s", outFile.getAbsolutePath()));

				} else {
					try {
						// ZipFile から 対象ZipEntry の InputStream を取り出す。
						InputStream is = zip.getInputStream(entry);
						// 効率よく読み込むため BufferedInputStream でラップする。
						in = new BufferedInputStream(is);

						if (!outFile.getParentFile().exists()) {
							// 出力先ファイルの保存先ディレクトリが存在しない場合は、
							// ディレクトリを作成しておく。
							if (!outFile.getParentFile().mkdirs())
								m_log.warn(String.format("Fail to create Directory. %s", outFile.getParentFile().getAbsolutePath()));
						}

						// 出力先 OutputStream を作成。
						out = new BufferedOutputStream(new FileOutputStream(outFile));

						// 入力ストリームから読み込み、出力ストリームへ書き込む。
						int ava;
						while ((ava = in.available()) > 0) {
							byte[] bs = new byte[ava];
							// 入力
							if(in.read(bs) < 0)
								break;

							// 出力
							out.write(bs);
						}
					} catch (FileNotFoundException e) {
						throw e;
					} catch (IOException e) {
						throw e;
					} finally {
						try {
							if (in != null)
								// ストリームは必ず close する。
								in.close();
						} catch (IOException e) {
						}
						try {
							if (out != null)
								// ストリームは必ず close する。
								out.close();
						} catch (IOException e) {
						}
					}
				}
			}
		} finally {
			if(zip != null){
				zip.close();
			}
			if (zipFile.exists()) {
				if (!zipFile.delete()) {
					m_log.warn("Failed delete zipfile=" + zipFile.getAbsolutePath());
				}
			}
		}
	}
}
