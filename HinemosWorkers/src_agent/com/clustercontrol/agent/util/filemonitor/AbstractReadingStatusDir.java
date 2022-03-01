/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.agent.util.filemonitor;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.util.HinemosTime;

/**
 * 監視設定に紐づく読み込み状態ファイルを管理する
 *
 */
public abstract class AbstractReadingStatusDir<T extends AbstractFileMonitorInfoWrapper> {
	private static Log log = LogFactory.getLog(AbstractReadingStatusDir.class);

	private final String fileRsStatus;
	private final String dirPrefix;
	// 監視種別判定用のディレクトリ
	private final String monitorTypeDir;
	// 監視種別判定用のファイル
	private final String monitorType;

	// わざとアクセス修飾子無し（同一パッケージからのみアクセス可能）
	static final String FILES_KEY = "files";
	private static final String DIRECTORY_KEY = "directory";
	private static final String FILENAME_KEY = "filename";
	// わざとアクセス修飾子無し（同一パッケージからのみアクセス可能）
	static final String REG_DATE_KEY = "regdate";

	// 読み込み状態ファイルを配置する基本Path
	private String basePath;

	private FileMonitorConfig fileMonitorConfig;

	// 読み込み状態管理インスタンス
	private AbstractReadingStatusRoot<T> parent;
	
	protected T wrapper;

	/**
	 * 現在監視しているディレクトリ。
	 * 監視するディレクトリが、更新により変更されたか確認するために使用。
	 */
	private String directory;
	/**
	 * 指定された監視設定の登録日時。
	 * 新たに指定された監視設定が、同一IDでも、再作成されていないか確認するために使用。
	 */
	private Long regdate;
	/**
	 */
	private String filename;
	/**
	 * 現在監視しているディレクトリ内のファイル情報。
	 */
	private Map<String, AbstractReadingStatus<T>> statusMap = new HashMap<>();

	/**
	 * ログファイル最大監視数の到達を通知した時間
	 */
	private Long lastNotificationTime;

	/**
	 * プロパティの設定値
	 */
	private Long interval;

	/**
	 * ファイル名を正規表現でパターンマッチするか否か
	 */
	private boolean fileNameRegex;
	
	protected AbstractFileMonitorManager<T> fileMonitorManager;

	public AbstractReadingStatusDir(AbstractFileMonitorManager<T> fileMonitorManager, String fileRsStatus, String dirPrefix, String monitorTypeDir, String monitorType,
			AbstractReadingStatusRoot<T> parent, T wrapper, String basePath,
			FileMonitorConfig fileMonitorConfig, boolean fileNameRegex) {
		this.fileMonitorManager = fileMonitorManager;
		this.fileRsStatus = fileRsStatus;
		this.dirPrefix = dirPrefix;
		this.monitorTypeDir = monitorTypeDir;
		this.monitorType = monitorType;
		this.parent = parent;
		this.wrapper = wrapper;
		this.basePath = basePath;
		this.fileMonitorConfig = fileMonitorConfig;
		this.fileNameRegex = fileNameRegex;

		File rstatus = new File(new File(basePath, getDirName()), fileRsStatus);

		if (rstatus.exists()) {
			try (FileInputStream fi = new FileInputStream(rstatus)) {
				// ファイルを読み込む
				Properties props = new Properties();
				props.load(fi);
				directory = props.getProperty(DIRECTORY_KEY);
				regdate = Long.valueOf(props.getProperty(REG_DATE_KEY));
				filename = props.getProperty(FILENAME_KEY);
			} catch (FileNotFoundException e) {
				log.debug(e.getMessage(), e);
			} catch (IOException | NumberFormatException e) {
				log.warn(e.getMessage(), e);
			}
		}
		// プロパティ取得
		interval = fileMonitorConfig.getMaxFileNotifyInterval();
		update(true);
	}
	
	/**
	 * 指定した監視設定で、このインスタンスの内容を更新する。
	 * 
	 * @param mi
	 */
	public void update(T mi) {
		this.wrapper = mi;
		update(false);
	}

	/**
	 * このインスタンスが管理するファイル情報をファイルシステムと同期する。
	 * 
	 */
	public void update() {
		update(false);
	}

	private void update(boolean init) {
		// 監視設定に関わる情報を格納するルートディレクトリの存在確認。
		File statusDir = new File(basePath, getDirName());
		if (!statusDir.exists()) {
			if (!statusDir.mkdir()) {
				log.warn("update() : " + statusDir.getPath() + " is not created.");
				return;
			}
		}

		// 監視種別を判定するためのディレクトリと空ファイルを用意
		try {
			File monTypeDir = new File(statusDir, monitorTypeDir);
			if (!monTypeDir.exists()) {
				if (!monTypeDir.mkdir()) {
					log.warn("update() : " + monTypeDir.getPath() + " is not created.");
					return;
				}
			}
			File monTypeFile = new File(monTypeDir, monitorType);
			if (!monTypeFile.exists()) {
				if (!monTypeFile.createNewFile()) {
					log.warn("update() : " + monTypeFile.getPath() + " is not created.");
					return;
				}
			}
		} catch (IOException e) {
			log.warn("update() : " + e.getMessage(), e);
			return;
		}

		// 読み込み状態ファイル保存ディレクトリの存在確認。
		File filesDir = new File(statusDir, FILES_KEY);
		if (!filesDir.exists()) {
			if (!filesDir.mkdir()) {
				log.warn("update() : " + filesDir.getPath() + " is not created.");
				return;
			}
		}

		boolean refresh = false;
		if (directory != null && regdate != null && filename != null) {
			// 監視設定が再作成されている場合は、、ファイル読み込み情報を破棄
			// 監視先ディレクトリが変更されている場合は、ファイル読み込み情報を破棄
			if (!regdate.equals(wrapper.getRegDate()) || (!new File(directory).getAbsolutePath()
					.equals(new File(wrapper.getDirectory()).getAbsolutePath()))) {
				log.info(String.format("update() : ReadingStatus is clear. regdate=%d,dir=%s nextregdate=%d,nextdir=%s", regdate, directory,
						wrapper.getRegDate(), wrapper.getDirectory()));
				clearFiles();
				refresh = true;
			} else if (!filename.equals(wrapper.getFileName())) {
				refresh = true;
			}
		} else {
			refresh = true;
		}

		// 読み込み状態ファイルの全体破棄の判断に必要な情報を保存
		if (refresh) {
			try (FileOutputStream fo = new FileOutputStream(new File(statusDir, fileRsStatus))) {
				Properties props = new Properties();
				props.put(DIRECTORY_KEY, wrapper.getDirectory());
				props.put(FILENAME_KEY, wrapper.getFileName());
				props.put(REG_DATE_KEY, wrapper.getRegDate().toString());
				props.store(fo, wrapper.getId());
				
				directory = wrapper.getDirectory();
				filename = wrapper.getFileName();
				regdate = wrapper.getRegDate();
			} catch (IOException e) {
				log.warn("update() : " + e.getMessage(), e);
			}
		}

		// 監視対象ファイル検索用のパターン作成
		FileFilter fileFilter = null;
		try {
			if (fileNameRegex) {
				fileFilter = new FileFilter() {
					Pattern pattern = Pattern.compile(wrapper.getFileName(), Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
					@Override
					public boolean accept(File f) {
						// ファイル、ファイル名でフィルタリングする(正規表現によるパターンマッチ)
						return	f.isFile() && pattern.matcher(f.getName()).matches();
					}
				};
			} else {
				fileFilter = new FileFilter() {
					@Override
					public boolean accept(File f) {
						// ファイル、ファイル名でフィルタリングする(完全一致)
						return	f.isFile() && wrapper.getFileName().equals(f.getName());
					}
				};
			}
		} catch (Exception e) {
			if (init) {
				// 不正ファイルパターン。処理が継続できないので、処理を戻す。
				log.warn("update() : " + e.getMessage());
			}
			return;
		}

		// シーク対象のファイル一覧を取得。
		File directory = new File(wrapper.getDirectory()).getAbsoluteFile();
		File[] seekFiles = directory.listFiles(fileFilter);
		if (seekFiles == null) {
			if (init)
				//null が返されるパターンは、なんらかの異常を示している。
				log.warn("update() : " + wrapper.getDirectory() + " does not have a reference permission");
			return;
		}

		Map<String, AbstractReadingStatus<T>> rsMap = new HashMap<>(statusMap);
		boolean isReachedMaxFiles = false;//最大監視対象数に到達したか
		for (File file : seekFiles) {
			// ファイルが存在しているか確認。
			log.debug("update() : " + wrapper.getId() + ", file=" + file.getName());
			// 最大監視対象数に達していないか？
			if (!parent.isMonitorFile(wrapper, file)) {
				log.warn("update() : too many files for logfile. not-monitoring file=" + file.getName());
				isReachedMaxFiles = true;
				continue;
			}

			AbstractReadingStatus<T> found = rsMap.get(file.getName());

			// 新規に検知したファイルなら、ファイル状態情報を新たに追加
			if (found == null) {
				AbstractReadingStatus<T> status = createReadingStatus(wrapper.getId(), file, fileMonitorConfig.getFirstPartDataCheckSize(), new File(filesDir.getPath(), file.getName()), refresh);
				statusMap.put(file.getName(), status); 
			} else {
				rsMap.remove(file.getName());
			}
		}
		// 最大監視対象数に達している場合通知する。プロパティで0が設定された場合は通知しない
		// また、監視設定が有効な場合に限り通知する
		if (isReachedMaxFiles && interval != 0 && wrapper.getMonitorFlg()) {
			log.debug("isReachedMaxFiles=" + isReachedMaxFiles + ":interval=" + interval + ":lastNotificationTime="
					+ lastNotificationTime);
			if (lastNotificationTime == null || (HinemosTime.currentTimeMillis() - lastNotificationTime) > interval) {
				if (lastNotificationTime != null) {
					log.debug("NowTime=" + HinemosTime.currentTimeMillis() + ":diffTime="
							+ (HinemosTime.currentTimeMillis() - lastNotificationTime));
				}
				sendMessageByMaxFilesOver();
				lastNotificationTime = HinemosTime.currentTimeMillis();
			}
		}

		// 既に存在しないファイルかパターンマッチしないファイル情報なので削除
		for (Map.Entry<String, AbstractReadingStatus<T>> e : rsMap.entrySet()) {
			e.getValue().clear();
			statusMap.remove(e.getKey());
		}

		// ゴミファイルの掃除
		File[] filesFiles = filesDir.listFiles();
		if (filesFiles == null) {
			// null が返されるパターンは、なんらかの異常を示している。
			log.warn("update() : " + statusDir.getPath() + " does not have a reference permission");
			return;
		}

		List<File> rsFileList = new ArrayList<>(Arrays.asList(filesFiles));
		for (String fileName : statusMap.keySet()) {
			String[] fileNames = { fileName + ".t", fileName + ".f"};
			for(String fn : fileNames) {
				Iterator<File> iter = rsFileList.iterator();
				while (iter.hasNext()) {
					File f = iter.next();
					if (f.getName().equals(fn)) {
						iter.remove();
						break;
					}
				}
			}
		}

		for (File f : rsFileList) {
			if (!f.delete()) {
				log.warn("update() : not delete rsFile. file=" + f.getAbsolutePath());
			}
			log.debug("update() : " + String.format("delete readingstatus: filename = %s", f.getName()));
		}
	}

	/**
	 * このインスタンスが管理するファイル情報を取得する。
	 * 
	 * @return
	 */
	public Collection<AbstractReadingStatus<T>> list() {
		return statusMap.values();
	}

	/**
	 * 
	 */
	private void clearFiles() {
		for (AbstractReadingStatus<T> s : statusMap.values()) {
			s.clear();
		}
		statusMap.clear();
	}

	/**
	 * 
	 */
	public void clear() {
		clearFiles();
		
		// 監視設定に関わる情報を格納するルートディレクトリを削除。
		File statusDir = new File(basePath, getDirName());
		AbstractReadingStatusRoot.recursiveDeleteFile(statusDir);
	}

	/**
	 * このインスタンスが管理するディレクトリ名を取得する。
	 * 
	 * @return
	 */
	public String getDirName() {
		return dirPrefix + wrapper.getId();
	}

	/**
	 * このインスタンスが従う監視設定を取得する。
	 * 
	 * @return
	 */
	public T getMonitorInfo() {
		return wrapper;
	}

	/**
	 * 最大ファイル数超過に伴いメッセージをマネージャに送信する
	 */
	public abstract void sendMessageByMaxFilesOver();

	public abstract AbstractReadingStatus<T> createReadingStatus(String monitorId, File filePath,
			int firstPartDataCheckSize, File rsFilePath, boolean tail);
}