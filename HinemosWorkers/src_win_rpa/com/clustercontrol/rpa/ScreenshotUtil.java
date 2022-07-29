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

import javax.imageio.ImageIO;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.jobmanagement.rpa.util.RpaWindowsUtil;
import com.clustercontrol.util.HinemosTime;

public class ScreenshotUtil {

	/** ロガー */
	private static Log m_log = LogFactory.getLog(ScreenshotUtil.class);

	/**
	 * スクリーンショットの一時ファイル名<br>
	 * エージェントが途中のファイルを転送しないよう、<br>
	 * スクリーンショットを保存した後にリネームします。
	 */
	private static final String TEMP_FILE_NAME = "tmp-%s.png";

	/** スクリーンショットの画像形式 */
	private static final String SCREENSHOT_FILE_TYPE = "png";


	/**
	 * スクリーンショットを保存します。
	 * 
	 * @param fileName 
	 * @return
	 */
	public static boolean save(String fileName) {
		m_log.debug("save() : start. fileName=" + fileName);

		String screenshotDir = null;
		try {
			screenshotDir = RpaWindowsUtil.getScreenshotDirPath();
		} catch (HinemosUnknown | InterruptedException e) {
			m_log.warn("ScreenshotThread() : error occurred. e=" + e.getMessage(), e);
			return false;
		}
		if (screenshotDir == null || screenshotDir.isEmpty()) {
			m_log.warn("ScreenshotThread() : screenshotDir is null or empty. screenshotDir=" + screenshotDir);
			return false;
		}

		long time = HinemosTime.currentTimeMillis();
		File tempFile = new File(screenshotDir, String.format(TEMP_FILE_NAME, time));
		m_log.debug("save() : tempFile=" + tempFile);

		String newFileName = fileName;
		if (newFileName == null || newFileName.isEmpty()) {
			newFileName = String.format(RpaWindowsUtil.getScreenshotFileName(), time);
		}
		File newFile = new File(screenshotDir, newFileName);
		m_log.debug("save() : newFile=" + newFile);

		try {
			Rectangle screen =  new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()); 
			BufferedImage Image = new Robot().createScreenCapture(screen); 
			ImageIO.write(Image, SCREENSHOT_FILE_TYPE, tempFile);
			Files.move(tempFile.toPath(), newFile.toPath());
		} catch (AWTException | IOException e) {
			m_log.error("save() : taking screen shot failed, " + e.getMessage(), e);
			return false;
		}

		return true;
	}

}
