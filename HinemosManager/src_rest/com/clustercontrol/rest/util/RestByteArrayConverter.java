/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidSetting;

public class RestByteArrayConverter {
	private static Log m_log = LogFactory.getLog(RestByteArrayConverter.class);

	public static byte[] convertInputStreamToByteArray(InputStream is, int bufferSize) throws HinemosUnknown, InvalidSetting {
		if (is == null) {
			m_log.warn("inputstream is null.");
			throw new InvalidSetting();
		}
		try(ByteArrayOutputStream os = new ByteArrayOutputStream()) {
			byte[] buffer = new byte[bufferSize];
			int len;
			while ((len = is.read(buffer)) != -1) {
				os.write(buffer, 0, len);
			}
			return os.toByteArray();
		} catch (IOException e) {
			m_log.warn("failed convert inputstream to bytearry.");
			throw new HinemosUnknown(e);
		}
	}
	public static byte[] convertInputStreamToByteArray(InputStream is) throws HinemosUnknown, InvalidSetting {
		return convertInputStreamToByteArray(is, 1024);
	}

	public static File convertByteArrayToFile(byte[] filedata, RestTempFileType type) throws HinemosUnknown {
		if (filedata == null) {
			m_log.warn("filedata is null.");
			throw new HinemosUnknown();
		}
		java.nio.file.Path tempFile = null;
		try {
			tempFile = RestTempFileUtil.createTempFile(type);
			Files.write(tempFile, filedata);
		} catch (IOException e) {
			m_log.warn("failed convert byte array to file.");
			throw new HinemosUnknown(e);
		}
		return tempFile.toFile();
	}
}
