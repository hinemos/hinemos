/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rpa;

import java.awt.AWTException;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.imageio.ImageIO;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.util.HinemosTime;

public class ScreenshotUtil {
	/** ロガー */
	private static Log m_log = LogFactory.getLog(ScreenshotUtil.class);
	/** RPAツールエグゼキューター連携用ファイル出力先フォルダ */
	protected static final String roboFileDir = System.getProperty("hinemos.agent.rpa.dir");
	/**
	 * スクリーンショットの一時ファイル名<br>
	 * エージェントが途中のファイルを転送しないよう、<br>
	 * スクリーンショットを保存した後にリネームします。
	 */
	private String tempFileName = "tmp-%s.png";
	/** スクリーンショットのファイル名 */
	private String screenshotFileName = "screenshot-%s.png";
	/** スクリーンショットの画像形式 */
	private final String screenshotFileType = "png";
	
	/**
	 * スクリーンショットの取得を行います。
	 */
	public boolean save(String fileName) {
		long time = HinemosTime.currentTimeMillis();
		tempFileName = String.format(tempFileName, time);
		File tempFile = new File(roboFileDir, tempFileName);
		try {
			if (fileName != null) {
				screenshotFileName = fileName;
			} else {
				screenshotFileName = String.format(screenshotFileName, time);
			}
			Rectangle screen =  new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()); 
			BufferedImage Image = new Robot().createScreenCapture(screen); 
			ImageIO.write(Image, screenshotFileType, tempFile); 
			Files.move(tempFile.toPath(), Paths.get(roboFileDir, screenshotFileName));
			return true;
		} catch (AWTException | IOException e) {
			m_log.error("save() : taking screen shot failed, " + e.getMessage(), e);
		} 
		return false;
	}

	/**
	 * スクリーンショットの取得を行います。
	 * ファイル名には現在時刻が入ります。
	 * @return
	 */
	public boolean save() {
		return save(null);
	}
	
	/**
	 * スクリーンショットのファイル名を返します。
	 * @return スクリーンショットのファイル名
	 */
	public String getScreenshotFileName() {
		return screenshotFileName;
	}
}
