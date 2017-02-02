/*
Copyright (C) 2016 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.
 */

package com.clustercontrol.jobmanagement.util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;

import com.clustercontrol.util.Messages;


/**
 * アイコンファイル用のユーティリティクラス。
 * @since 5.1.0
 */
public class JobmapIconImageUtil {

	// ログ
	private static Log m_log = LogFactory.getLog( JobmapIconImageUtil.class );

	private static final int MAX_FILESIZE = 256*1024;

	public static final int ICON_WIDTH = 32;
	public static final int ICON_HEIGHT = 32;

	/**
	 * ファイルデータ（画像データ）の取得
	 * 
	 * @param filepath ファイルパス
	 * @throws Exception
	 */
	public static byte[] getImageFileData(String filepath) throws Exception {

		// ファイルデータ取得
		byte[] filedata = getFileData(filepath);

		// イメージデータのファイルサイズチェック
		ImageData imageData = getImageData(filedata);
		if (imageData.width != ICON_WIDTH
				|| imageData.height != ICON_WIDTH) {
			String[] args = { Integer.toString(ICON_WIDTH),
					Integer.toString(ICON_HEIGHT) };
			throw new Exception(
					Messages.getString("message.job.141", args));
		}
		return filedata;
	}
	

	/**
	 * ファイルデータ取得
	 * @param filepath ファイルパス
	 * @throws Exception
	 */
	private static byte[] getFileData(String filepath) throws Exception {
		File file = new File(filepath);
		String filename = file.getName();
		int filesize = (int)file.length();
		if (filesize > MAX_FILESIZE) {
			m_log.warn("getFileData(), file size is too large");
			throw new Exception(Messages.getString("message.job.143"));
		}
		byte[] filedata = null;

		/*
		 * ファイルを読む
		 */
		FileInputStream stream = null;
		try {
			stream = new FileInputStream(filepath);
			filedata = new byte[filesize];
			int readsize = stream.read(filedata, 0, filesize);
			m_log.debug("UploadImage readsize = " + readsize + ", filesize = " + filesize);
			m_log.debug("path=" + filepath + ", name=" + filename);
		} catch (FileNotFoundException e) {
			m_log.warn("getFileData(), " + e.getMessage(), e);
			throw new Exception(Messages.getString("file.not.found"), e);
		} catch (Exception e) {
			m_log.warn("getFileData(), " + e.getMessage(), e);
			throw e;
		} finally {
			if (stream != null) {
				try {
					stream.close();
				} catch (IOException e) {
					m_log.warn("getFileData(), " + e.getMessage(), e);
				}
				stream = null;
			}
		}
		return filedata;
	}

	/**
	 * イメージデータ取得
	 * @param filedata
	 */
	private static ImageData getImageData(byte[] filedata) {
		ByteArrayInputStream byteStream = new ByteArrayInputStream(filedata);
		ImageLoader imageLoader = new ImageLoader();
		ImageData imageData = imageLoader.load(byteStream)[0];
		m_log.debug("image size x=" + imageData.width + ", y=" + imageData.height);
		return imageData;
	}

	/**
	 * アイコン画像取得
	 * 
	 * @param filedata ファイルデータ
	 * @return Imageオブジェクト
	 */
	public static Image getIconImage(byte[] filedata) {
		Image ret = null;
		if (filedata != null) {
			ByteArrayInputStream stream = new ByteArrayInputStream(filedata);
			ImageLoader imageLoader = new ImageLoader();
			ret = new Image(null, imageLoader.load(stream)[0]);
		}
		return ret;
	}
}