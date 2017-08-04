
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

package com.clustercontrol.collect.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.fault.HinemosUnknown;

/**
 * ファイル圧縮を行うクラス<BR>
 *
 * @version 4.0.0
 * @since 4.0.0
 */
public class ZipCompresser {

	private static Log m_log = LogFactory
			.getLog( ZipCompresser.class );

	/**
	 * 指定されたファイルパスのリストを圧縮して1つのファイルに固める
	 * 
	 * @param inputFileNameList 圧縮対象のファイルパスのリスト
	 * @param outputFileName 出力先のファイルパス
	 * 
	 * @return 成功可否
	 */
	protected static synchronized void archive(ArrayList<String> inputFileNameList, String outputFileName) throws HinemosUnknown{
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
					m_log.debug("Fail to delete " + zipFile.getAbsolutePath());
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
				String fileName = (new File(inputFileName)).getName();
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
}
