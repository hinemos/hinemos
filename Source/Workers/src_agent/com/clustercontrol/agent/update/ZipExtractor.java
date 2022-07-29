/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.agent.update;

import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * zipファイルを展開します。
 * 
 * @since 6.2.0
 */
public class ZipExtractor {

	// 展開後のファイルサイズがこれを超えるようなら異常とみなす
	private static final long SIZE_LIMIT = 200 * 1024 * 1024;
	// ファイル出力バッファサイズ (適当)
	private static final int BUFFER_SIZE = 8 * 1024;
	
	private static final Log log = LogFactory.getLog(ZipExtractor.class);
	
	private byte[] buffer;

	/**
	 * コンストラクタ。
	 */
	public ZipExtractor() {
		buffer = new byte[BUFFER_SIZE];
	}
	
	/**
	 * zipファイルの展開を実行します。
	 * 
	 * @param zipFile 展開するzipファイル。
	 * @param destDir 展開先ディレクトリ。
	 * @throws ExtractFailureException 展開処理が正常終了しなかった。
	 */
	public void execute(Path zipFile, Path destDir) throws ExtractFailureException {
		log.info("execute: file=" + zipFile);
		try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile.toFile()), StandardCharsets.UTF_8)) {
			ZipEntry ze;
			while ((ze = zis.getNextEntry()) != null) {
				Path saveFile = destDir.resolve(ze.getName()).normalize();
				log.info("execute: - " + saveFile);
				// ディレクトリは処理不要
				if (ze.isDirectory()) continue;

				// 展開先ディレクトリをさかのぼってないか？
				if (!saveFile.startsWith(destDir)) {
					throw new ExtractFailureException("Entry=" + ze.getName() + " is out of dir.");
				}

				// ディレクトリを作ってファイルを書き込む
				long total = 0;
				Path saveDir = saveFile.getParent();
				if (saveDir == null) {
					// destDirが空だったらここに来るかも。
					throw new ExtractFailureException("No parent. savefile=" + saveFile);
				}
				Files.createDirectories(saveDir);
				try (BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(saveFile.toFile()))) {
					int n;
					while ((n = zis.read(buffer)) > 0) {
						total += n;
						if (total > SIZE_LIMIT) {
							throw new ExtractFailureException(
									"Entry=" + ze.getName() + " exceeds " + SIZE_LIMIT + " bytes.");
						}
						os.write(buffer, 0, n);
					}
				}
			}
		} catch (ExtractFailureException e) {
			throw e;
		} catch (Exception e) {
			throw new ExtractFailureException("Failed to extract file=" + zipFile, e);
		}
	}
}
