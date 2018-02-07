/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.nodemap.util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.rap.rwt.SingletonUtil;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;

import com.clustercontrol.nodemap.messages.Messages;
import com.clustercontrol.ws.nodemap.BgFileNotFound_Exception;
import com.clustercontrol.ws.nodemap.IconFileNotFound_Exception;

/**
 * 画像ファイルを管理するクラス。
 * @since 1.0.0
 */
public class ImageManager {

	// ログ
	private static Log m_log = LogFactory.getLog( ImageManager.class );

	private static final int MAX_FILESIZE = 256*1024;

	public static final int MIN_ICON_WIDTH = 16;
	public static final int MIN_ICON_HEIGHT = 16;
	public static final int MAX_ICON_WIDTH = 64;
	public static final int MAX_ICON_HEIGHT = 64;

	public static final int MIN_BG_WIDTH = 16;
	public static final int MIN_BG_HEIGHT = 16;
	public static final int MAX_BG_WIDTH = 1920;
	public static final int MAX_BG_HEIGHT = 1200;

	// イメージのクライアントキャッシュ
	private ConcurrentHashMap <String, ConcurrentHashMap<String, Image>> iconImageMap;
	private ConcurrentHashMap <String, ConcurrentHashMap<String, Image>> bgImageMap;

	private ImageManager(){}
	
	private static ImageManager getInstance() {
		return SingletonUtil.getSessionInstance(ImageManager.class);
	}
	
		/**
	 * 背景画像のアップロード
	 * @param filepath
	 * @throws Exception
	 */
	public static void bgUpload (String managerName, String filepath, String filename) throws Exception {
		// ファイルデータ取得
		byte[] filedata = getFileData(filepath);

		// イメージデータ取得
		ImageData imageData = getImageData(filedata);
		if (imageData.width < MIN_BG_WIDTH || imageData.height < MIN_BG_HEIGHT){
			String[] args = { Integer.toString(MIN_BG_WIDTH), Integer.toString(MIN_BG_HEIGHT) };
			throw new Exception(
					com.clustercontrol.nodemap.messages.Messages.getString("file.bg.too.small", args));
		} else if (MAX_BG_WIDTH < imageData.width || MAX_BG_HEIGHT < imageData.height) {
			String[] args = { Integer.toString(MAX_BG_WIDTH), Integer.toString(MAX_BG_HEIGHT) };
			throw new Exception(
					com.clustercontrol.nodemap.messages.Messages.getString("file.bg.too.large", args));
		}

		// ファイルアップロード
		NodeMapEndpointWrapper wrapper = NodeMapEndpointWrapper.getWrapper(managerName);
		wrapper.setBgImage(filename, filedata);

		// ファイルをアップロードした場合は、クライアントのキャッシュから消す。
		ImageManager.clearBg(managerName, filename);
	}


	/**
	 * アイコン画像のアップロード
	 * @param filepath
	 * @throws Exception
	 */
	public static void iconUpload (String managerName, String filepath, String filename) throws Exception {
		// ファイルデータ取得
		byte[] filedata = getFileData(filepath);

		// イメージデータ取得
		ImageData imageData = getImageData(filedata);
		if (imageData.width < MIN_ICON_WIDTH
				|| imageData.height < MIN_ICON_WIDTH) {
			String[] args = { Integer.toString(MIN_ICON_WIDTH),
					Integer.toString(MIN_ICON_HEIGHT) };
			throw new Exception(
					com.clustercontrol.nodemap.messages.Messages.getString("file.icon.too.small", args));
		} else if (MAX_ICON_WIDTH < imageData.width
				|| MAX_ICON_HEIGHT < imageData.height) {
			String[] args = { Integer.toString(MAX_ICON_WIDTH),
					Integer.toString(MAX_ICON_HEIGHT) };
			throw new Exception(
					com.clustercontrol.nodemap.messages.Messages.getString("file.icon.too.large", args));
		}

		// ファイルアップロード
		NodeMapEndpointWrapper wrapper = NodeMapEndpointWrapper.getWrapper(managerName);
		wrapper.setIconImage(filename, filedata);

		// ファイルをアップロードした場合は、クライアントのキャッシュから消す。
		ImageManager.clearIcon(managerName, filename);
	}
	

	/**
	 * ファイルデータ取得
	 * @param filepath
	 * @throws Exception
	 */
	private static byte[] getFileData(String filepath) throws Exception {
		File file = new File(filepath);
		String filename = file.getName();
		int filesize = (int)file.length();
		if (filesize > MAX_FILESIZE) {
			m_log.warn("getFileData(), file size is too large");
			throw new Exception(Messages.getString("file.too.large"));
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

		/*
		 * ファイルのサイズ(16x16-64x64, 16x16-1920x1200のチェック)
		 */
		ByteArrayInputStream byteStream = new ByteArrayInputStream(filedata);
		ImageLoader imageLoader = new ImageLoader();
		ImageData imageData = imageLoader.load(byteStream)[0];
		m_log.debug("image size x=" + imageData.width + ", y=" + imageData.height);

		return imageData;
	}
	
	/**
	 * アイコン画像ロード
	 * @param iconImage
	 * @return
	 * @throws Exception
	 */
	public static Image loadIcon(String managerName, String iconImage) throws Exception {
		Image ret = null;
		ConcurrentHashMap<String, Image> map = null;
		
		String filename = "node";
		if (iconImage != null && iconImage.length() > 0) {
			filename = iconImage;
		}
		/*
		 * キャッシュに存在する場合は、キャッシュの値を返す。
		 */
		if (getInstance().iconImageMap == null) {
			getInstance().iconImageMap = new ConcurrentHashMap<String, ConcurrentHashMap<String, Image>>();
		}
		
		map = getInstance().iconImageMap.get(managerName);
		if (map == null) {
			map = new ConcurrentHashMap<String, Image>();
		} else {
			ret = map.get(filename);
			if (ret != null) {
				return ret;
			}
		}
		
		/*
		 * iconImageからbyte[]を取得する。
		 */
		try {
			NodeMapEndpointWrapper wrapper = NodeMapEndpointWrapper.getWrapper(managerName);
			byte[] filedata = wrapper.getIconImage(filename);
			ByteArrayInputStream stream = new ByteArrayInputStream(filedata);
			ImageLoader imageLoader = new ImageLoader();
			ret = new Image(null, imageLoader.load(stream)[0]);
		} catch (IconFileNotFound_Exception e) {
			// ファイルが存在しない場合は、nodeアイコンを見せる。
			m_log.warn("iconfile(" + filename + ") is not found at IconImage.");
			if ("node".equals(filename)) {
				return null;
			} else {
				return loadIcon(managerName, "node");
			}
		}

		map.put(filename, ret);
		getInstance().iconImageMap.put(managerName, map);
		return ret;
	}

	/**
	 * アイコンキャッシュクリア
	 */
	public static void clearIcon() {
		m_log.debug("IconImageLoader clear");
		getInstance().iconImageMap.clear();
	}

	/**
	 * アイコンキャッシュクリア
	 * @param filename
	 */
	public static void clearIcon(String managerName, String filename) {
		ConcurrentHashMap<String, Image> map = getInstance().iconImageMap.get(managerName);
		if (map != null) {
			map.remove(filename);
		}
	}

	/**
	 * 背景のロード
	 * @param iconImage
	 * @return
	 * @throws Exception
	 */
	public static Image loadBg(String managerName, String iconImage) throws Exception {
		Image ret = null;
		ConcurrentHashMap<String, Image> map = null;

		String filename = "default";
		if (iconImage != null && iconImage.length() > 0) {
			filename = iconImage;
		}
		/*
		 * キャッシュに存在する場合は、キャッシュの値を返す。
		 */
		if (getInstance().bgImageMap == null) {
			getInstance().bgImageMap = new ConcurrentHashMap<String, ConcurrentHashMap<String, Image>>();
		}

		map = getInstance().bgImageMap.get(managerName);
		if (map == null) {
			map = new ConcurrentHashMap<String, Image>();
		} else {
			ret = map.get(iconImage);
			if (ret != null) {
				return ret;
			}
		}
		/*
		 * BgImageからbyte[]を取得する。
		 */
		try {
			NodeMapEndpointWrapper wrapper = NodeMapEndpointWrapper.getWrapper(managerName);
			byte[] filedata = wrapper.getBgImage(filename);
			ByteArrayInputStream stream = new ByteArrayInputStream(filedata);
			ImageLoader imageLoader = new ImageLoader();
			ret = new Image(null, imageLoader.load(stream)[0]);
		} catch (BgFileNotFound_Exception e) {
			m_log.warn("image file (" + filename + ") is not found at BgImage.");
			if ("default".equals(filename)) {
				return null;
			} else {
				return loadBg(managerName, "default");
			}
		} catch (Exception e) {
			throw e;
		}

		map.put(filename, ret);
		getInstance().bgImageMap.put(managerName, map);
		return ret;
	}

	/**
	 * 背景キャッシュクリア
	 */
	public static void clearBg() {
		m_log.debug("BgImageLoader clear");
		getInstance().bgImageMap.clear();
	}

	/**
	 * 背景キャッシュクリア
	 * @param filename
	 */
	public static void clearBg(String managerName, String filename) {
		ConcurrentHashMap<String, Image> map = getInstance().bgImageMap.get(managerName);
		if (map != null) {
			map.remove(filename);
		}
	}
}