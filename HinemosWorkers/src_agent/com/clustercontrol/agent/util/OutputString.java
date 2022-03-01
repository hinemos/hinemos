/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.agent.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openapitools.client.model.AgtJobOutputInfoResponse;

import com.clustercontrol.jobmanagement.bean.JobOutputType;

public class OutputString {
	private static Log log = LogFactory.getLog(OutputString.class);

	public static class OutputFile {
		public boolean isAvailable = false;
		public String filename = "";
		public boolean isSuccess = true;
		public String errorMessage;
		public int outputLength = 0;
	}

	public OutputFile stdout;
	public OutputFile stderr;

	public OutputString(AgtJobOutputInfoResponse normalJobOutputInfo, AgtJobOutputInfoResponse errorJobOutputInfo) {
		stdout = new OutputFile();
		if (normalJobOutputInfo != null) {
			try {
				stdout.filename = getOutputFilename(
						normalJobOutputInfo.getDirectory(),
						normalJobOutputInfo.getFileName(),
						normalJobOutputInfo.getAppendFlg());
				stdout.isAvailable = true;
			} catch (Exception e) {
				stdout.isSuccess = false;
				stdout.errorMessage = e.getMessage();
			}
		}
		stderr = new OutputFile();
		if (errorJobOutputInfo != null) {
			try {
				stderr.filename = getOutputFilename(
						errorJobOutputInfo.getDirectory(),
						errorJobOutputInfo.getFileName(),
						errorJobOutputInfo.getAppendFlg());
				stderr.isAvailable = true;
			} catch (Exception e) {
				stderr.isSuccess = false;
				stderr.errorMessage = e.getMessage();
			}
		}
	}

	public boolean isSameFileAndNotNull() {
		if (!stdout.isAvailable) {
			return false;
		} else if (!stderr.isAvailable) {
			return false;
		} else if (stdout.filename.equals(stderr.filename)) {
			return true;
		}
		return false;
	}

	public List<Integer> getResultStatus() {
		List<Integer> errorResultList = new ArrayList<>();
		if (!stderr.isSuccess) {
			errorResultList.add(JobOutputType.STDERR.getCode());
		}
		if (!stdout.isSuccess) {
			errorResultList.add(JobOutputType.STDOUT.getCode());
		}
		return errorResultList;
	}

	private String getOutputFilename(String dirname, String filename, boolean isAppned) throws Exception {
		String file = "";
		try {
			File outputDir = new File(dirname);
			if (!outputDir.exists()) {
				log.warn("getOutputFilename() : " + outputDir.getPath() + " is not exist.");
				throw new IOException(outputDir.getPath() + " is not exist");
			}
			File outputFile = new File(dirname, filename);
			if (!outputFile.exists()) {
				if (!outputFile.createNewFile()) {
					log.warn("getOutputFilename() : " + outputFile.getAbsolutePath() + " is not created.");
					throw new IOException(outputFile.getAbsolutePath() + " is not created");
				} else {
					file = outputFile.getAbsolutePath();
				}
			} else {
				if (!isAppned) {
					try (FileWriter fo = new FileWriter(outputFile, false)) {
						fo.write("");
					}
				} else {
					try (FileWriter fo = new FileWriter(outputFile, true)) {
						fo.write(System.getProperty("line.separator"));
					}
				}
				file = outputFile.getAbsolutePath();
			}
		} catch (IOException e) {
			log.warn("getOutputFilename() : directory=" + dirname + ", filename=" + filename+ " is unavailable.");
			throw e;
		}
		return file;
	}
}