/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.agent.update;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.Arrays;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * tar.gzファイルを展開します。
 * <p>
 * tarコマンドを呼び出すため、標準のWindowsでは動作しません。
 *
 * @since 6.2.0
 */
public class TarExtractor {
	
	private static final Log log = LogFactory.getLog(TarExtractor.class);

	/**
	 * tar.gzファイルの展開を実行します。
	 * 
	 * @param tarFile 展開するtar.gzファイル。
	 * @param destDir 展開先ディレクトリ。
	 * @throws ExtractFailureException 展開処理が正常終了しなかった。
	 */
	public void execute(Path tarFile, Path destDir) throws ExtractFailureException {
		int exitcode;
		try {
			String[] command = new String[] { "tar", "--overwrite", "--no-same-owner", "-C", destDir.toString(),
					"-zxpvf", tarFile.toString() };
			log.info("execute: Command=" + Arrays.toString(command));

			ProcessBuilder pb = new ProcessBuilder(command);
			pb.redirectErrorStream(true); // STDERR -> STDOUT

			Process proc = pb.start();
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()))) {
				String line;
				while ((line = reader.readLine()) != null) {
					log.info("execute: >" + line);
				}
			}
			exitcode = proc.waitFor();
		} catch (Exception e) {
			throw new ExtractFailureException("Failed to extract file=" + tarFile, e);
		}

		if (exitcode != 0) {
			throw new ExtractFailureException("Failed to extract file=" + tarFile + ", exit=" + exitcode);
		}
	}
}
