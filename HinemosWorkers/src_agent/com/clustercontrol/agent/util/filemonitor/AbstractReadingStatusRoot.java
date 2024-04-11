/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.agent.util.filemonitor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 各ファイルの読み込み状態を格納するクラス。
 */
public abstract class AbstractReadingStatusRoot<T extends AbstractFileMonitorInfoWrapper> {
	private static Log log = LogFactory.getLog(AbstractReadingStatusRoot.class);

	protected final String fileRsStatus;
	protected final String dirPrefix;
	protected final String lastLinePrefix = AbstractReadingStatusDir.REG_DATE_KEY;
	// 監視種別判定用のディレクトリ
	protected final String monitorTypeDir;
	// 監視種別判定用のファイル
	protected final String monitorType;
	/** 前回監視対象 */
	private Map<String, List<File>> preMonitorMap = new ConcurrentHashMap<>();

	private Map<String, AbstractReadingStatusDir<T>> statusMap = new HashMap<>();
	private final File storePath;
	private FileMonitorConfig fileMonitorConfig;
	private int newMonitorCounter = 0;

	protected AbstractFileMonitorManager<T> fileMonitorManager;
	
	public AbstractReadingStatusRoot(AbstractFileMonitorManager<T> fileMonitorManager, String fileRsStatus, String dirPrefix, String monitorTypeDir, String monitorType,
			List<T> miList, String baseDirectory, FileMonitorConfig fileMonitorConfig) {
		this.fileMonitorManager = fileMonitorManager;
		this.fileRsStatus = fileRsStatus;
		this.dirPrefix = dirPrefix;
		this.monitorTypeDir = monitorTypeDir;
		this.monitorType = monitorType;
		// ファイルの読み込み状態を格納したディレクトリを確認
		storePath = new File(baseDirectory);
		this.fileMonitorConfig = fileMonitorConfig;
		update(miList, null);
	}

	/**
	 * ファイル状態情報を格納した全てのディレクトリ情報を取得する。
	 * 
	 * @return
	 */
	public Collection<AbstractReadingStatusDir<T>> getReadingStatusDirList(){
		return statusMap.values();
	}

	/**
	 * 監視設定に対応するファイル状態情報を格納したディレクトリ情報を取得する。
	 * 
	 * @param monitorId
	 * @return
	 */
	public AbstractReadingStatusDir<T> getReadingStatusDir(String monitorId) {
		return statusMap.get(monitorId);
	}

	/**
	 * ファイルシステムを確認し、管理するファイル構成を更新
	 * 
	 */
	public void update() {
		clearCounter();

		preMonitorMap = new ConcurrentHashMap<>();
		for (AbstractReadingStatusDir<T> dir: statusMap.values()) {
			checkMonitorFile(dir.wrapper);
		}

		for (AbstractReadingStatusDir<T> dir: statusMap.values()) {
			dir.update();
		}
	}

	/**
	 * 指定した監視設定に従って、管理するファイル構成を更新
	 * 
	 * @param miList
	 */
	public void update(List<T> miList, List<T> beforeList) {
		clearCounter();

		List<File> miDirList;
		if (storePath.exists()) {
			File[] files = storePath.listFiles();
			miDirList = files == null ? Collections.<File>emptyList(): new ArrayList<>(Arrays.asList(files));
		} else {
			miDirList = new ArrayList<>();
			if (!storePath.mkdirs()) {
				log.warn(storePath.getPath() + " is not created.");
				return;
			}
		}

		// ファイルチェック
		preMonitorMap = new ConcurrentHashMap<>();
		for (T wrapper: miList) {
			checkMonitorFile(wrapper);
		}

		Map<String, AbstractReadingStatusDir<T>> curStatusMap = new HashMap<>(statusMap);
		Map<String, AbstractReadingStatusDir<T>> newStatusMap = new HashMap<>();
		for (T wrapper : miList) {
			AbstractReadingStatusDir<T> dir = curStatusMap.get(wrapper.getId());

			if (dir == null) {
				dir = createReadingStatusDir(wrapper, storePath.getPath(), fileMonitorConfig);
			} else {
				dir.update(wrapper);
				curStatusMap.remove(wrapper.getId());
			}
			
			newStatusMap.put(wrapper.getId(), dir);

			String miDirName = dir.getDirName();

			// 不要なファイルを確認。
			Iterator<File> iter = miDirList.iterator();
			while (iter.hasNext()) {
				File f = iter.next();
				if (!f.isDirectory())
					continue;

				if (!f.getName().equals(miDirName))
					continue;
				
				iter.remove();
				break;
			}
		}
		
		for (AbstractReadingStatusDir<T> dir: curStatusMap.values()) {
			String miDirName = dir.getDirName();
			dir.clear();

			// 不要なファイルを確認。
			Iterator<File> iter = miDirList.iterator();
			while (iter.hasNext()) {
				File f = iter.next();
				if (!f.isDirectory())
					continue;

				if (!f.getName().equals(miDirName))
					continue;
				
				iter.remove();
				break;
			}
		}
		
		statusMap = newStatusMap;
		
		List<String> beforeRsDirList = new ArrayList<String>();
		// 前回監視時点の監視設定リストがない場合は監視種別判定用のファイルを見て判定する
		if (beforeList == null || beforeList.isEmpty()) {
			File[] files = this.storePath.listFiles();
			if (files == null) {
				return;
			}
			for (File dir : files) {
				File monTypeDir = new File(dir, monitorTypeDir);
				if (monTypeDir.exists()) {
					File monTypeFile = new File(monTypeDir, monitorType);
					if (monTypeFile.exists()) {
						log.info("update() : monitor type is " + monitorType + ". " + dir.getName());
						beforeRsDirList.add(dir.getName());
					}
				} else {
					// パッチ適用後の初回は監視種別判定用のファイルがないためrsstatus.jsonの中身で判定する
					log.info("update() : monitor type directory does not exist. " + dir.getName());
					try (BufferedReader reader = new BufferedReader(new FileReader(new File(dir, fileRsStatus)))) {
						String line;
						boolean isLogfile = false;
						while ((line = reader.readLine()) != null) {
							isLogfile = line.startsWith(lastLinePrefix);
						}
						if (isLogfile) {
							log.info("update() : " + dir.getName() + " is readindstatus for logfile monitor.");
							beforeRsDirList.add(dir.getName());
						}
					} catch (IOException e) {
						log.warn("update() : " + e.getMessage(), e);
						continue;
					}
				}
			}
		} else {
			// 前回監視時点の監視設定のRSディレクトリリストを取得する.
			for (AbstractFileMonitorInfoWrapper beforeWrapper : beforeList) {
				beforeRsDirList.add(dirPrefix + beforeWrapper.getId());
			}
		}
		
		// 不要なファイルを削除
		for (File f : miDirList) {
			// 前回監視時点で存在していたディレクトリのみを削除(ログファイル監視以外のRSディレクトリ削除防止).
			if (beforeRsDirList.contains(f.getName())) {
				if (!recursiveDeleteFile(f)) {
					log.warn("faile to delete " + f.getPath());
				}
				continue;
			}
			// 先頭にプレフィックスついてないディレクトリはゴミなので削除.
			if(!f.getName().contains(dirPrefix)){
				if (!recursiveDeleteFile(f)) {
					log.warn("faile to delete " + f.getPath());
				}
			}
		}
	}

	/**
	 * 指定したファイルあるいは、ディレクトリを削除する。
	 * ディレクトリの場合は、再帰的に削除。
	 * 
	 * @param file
	 * @return
	 */
	public static boolean recursiveDeleteFile(final File file) {
		// 存在しない場合は処理終了
		if (!file.exists()) {
			return true;
		}
		// 対象がディレクトリの場合は再帰処理
		if (file.isDirectory()) {
			File[] files = file.listFiles();
			if (files != null) {
				for (File child : files) {
					recursiveDeleteFile(child);
				}
			}
		}
		// 対象がファイルもしくは配下が空のディレクトリの場合は削除する
		return file.delete();
	}

	/**
	 * 対象ファイルが監視対象かチェックします。
	 * update(boolean init)を呼び出す前に行う
	 */
	public void checkMonitorFile(final T wrapper) {
		// 読み込み状態ファイル保存ディレクトリの存在確認。
		File statusDir = new File(storePath.getPath(), dirPrefix + wrapper.getId());
		if (!statusDir.exists()) {
			// 存在しない場合は処理終了
			return;
		}
		File filesDir = new File(statusDir, AbstractReadingStatusDir.FILES_KEY);
		if (!filesDir.exists()) {
			// 存在しない場合は処理終了
			return;
		}
		File dir = new File(wrapper.getDirectory());
		if (!dir.isDirectory()) {
			if (log.isDebugEnabled()) {
				log.debug("checkMonitorFile() : " + wrapper.getDirectory() + " is not directory. ID=" + wrapper.getId());
			}
			return;
		}

		// ビジターでシーク対象ファイル一覧を取得する
		Pattern pattern = Pattern.compile(wrapper.getFileName(), Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
		PreMonitorListFileVisitor fv = new PreMonitorListFileVisitor(pattern, filesDir);
		Path path = dir.getAbsoluteFile().toPath();
		try {
			// 辿る階層はログファイル監視設定のディレクトリ直下のみ
			Files.walkFileTree(path, EnumSet.of(FileVisitOption.FOLLOW_LINKS), 1, fv);
		} catch (IOException e) {
			log.warn("checkMonitorFile() : " + e.getMessage());
			return;
		}

		preMonitorMap.put(wrapper.getId(), fv.getPreMonitorList());
	}

	/**
	 * 監視ファイルが今回の監視対象か判定します。
	 * <ul>
	 * <li>監視ファイルが前回監視対象に含まれるか
	 * <li>新たに追加されたファイルが今回監視ファイルにあたるか
	 * </ul>
	 * 
	 * @param wrapper
	 * @param file
	 * @return true:監視対象、false:監視対象外
	 * @throws IllegalStateException
	 */
	public boolean isMonitorFile(T wrapper, File file) throws IllegalStateException {
		List<File> fileList = preMonitorMap.get(wrapper.getId());
		// 渡されたファイルが前回監視対象に含まれる場合、監視対象
		if (fileList != null && fileList.contains(file)) {
			log.debug("isMonitorFile() file is pre monitor : id=" + wrapper.getId() + ", file=" + file.getName());
			return true;
		}
		// 監視設定に紐づくファイルの総数を算出
		int preFileTotalCounter = 0;
		for (Map.Entry<String, List<File>> pre : preMonitorMap.entrySet()) {
			if (pre.getValue() != null) {
				preFileTotalCounter += pre.getValue().size();
			}
		}
		// 新規監視対象で前回監視数と新規監視数が監視最大数を超えていない場合、監視対象
		if ((preFileTotalCounter + newMonitorCounter) < fileMonitorConfig.getFileMaxFiles()) {
			if (log.isDebugEnabled()) {
				log.debug("isMonitorFile() file is new monitor : id=" + wrapper.getId() + ", file=" + file.getName()
						+ " ,newMonitorCounter= " + newMonitorCounter + " preFileTotalCounter:" + preFileTotalCounter);
			}
			++newMonitorCounter;
			return true;
		}
		return false;
	}

	private void clearCounter() {
		log.debug("clear counter.");
		newMonitorCounter = 0;
	}

	public abstract AbstractReadingStatusDir<T> createReadingStatusDir(T wrapper, String basePath,
			FileMonitorConfig fileMonitorConfig);
	
	// 監視対象ファイルを取得するビジター(checkMonitorFile用)
	private static class PreMonitorListFileVisitor implements FileVisitor<Path> {

		List<File> preMonitorList = new ArrayList<>();
		
		private Pattern pattern;
		
		private File filesDir;

		public PreMonitorListFileVisitor(final Pattern pattern, final File filesDir) {
			this.pattern = pattern;
			this.filesDir = filesDir;
		}
				
		public List<File> getPreMonitorList() {
			return preMonitorList;
		}
				
		@Override
		public FileVisitResult postVisitDirectory(Path dir, IOException e) throws IOException {
			return FileVisitResult.CONTINUE;
		}

		@Override
		public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
			// findbugs 対応(nullチェック)
			Path filePath = file.getFileName();
			if(filePath == null) {
				// 想定外の状況
				// スキップして次へいく
				log.warn("update() : file.getFileName() is null");
				return FileVisitResult.CONTINUE;
			}
			String fileName = filePath.toString();
			log.debug("visitFile() : file = " + fileName);

			// ファイル以外の場合は何もしない
			if(!file.toFile().isFile()) {
				return FileVisitResult.CONTINUE;
			}

			// 監視設定のファイル名にマッチしない場合何もしない
			if(!pattern.matcher(fileName).matches()) {
				return FileVisitResult.CONTINUE;
			}

			// 監視対象ファイルが存在し、過去に監視された実績がある場合は今回も監視対象とする
			String rsFileTrueTempPath = new File(filesDir.getPath(), file.toFile().getName()) + ".t";
			String rsFileFalseTempPath = new File(filesDir.getPath(), file.toFile().getName()) + ".f";
			if (java.nio.file.Files.exists(Paths.get(rsFileTrueTempPath)) || java.nio.file.Files.exists(Paths.get(rsFileFalseTempPath))) {
				preMonitorList.add(file.toFile());
			}

			return FileVisitResult.CONTINUE;
		}

		@Override
		public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
			return FileVisitResult.CONTINUE;
		}

		@Override
		public FileVisitResult visitFileFailed(Path file, IOException e) throws IOException {
			return FileVisitResult.CONTINUE;
		}
	}

}