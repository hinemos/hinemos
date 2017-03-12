package com.clustercontrol.agent.log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 各ファイルの読み込み状態を格納するクラス。
 */
public class ReadingStatusRoot {
	private static Log log = LogFactory.getLog(ReadingStatusRoot.class);

	private static final String file_rstatus = "rstatus.json";
	private static final String dir_files = "files";
	private static final String dir_prefix = "rs_";
	private static final String directory = "directory";
	private static final String filename = "filename";
	private static final String regdate = "regdate";

	/**
	 * 監視設定に紐づく読み込み状態ファイルを管理する
	 *
	 */
	public class ReadingStatusDir {
		//
		private MonitorInfoWrapper wrapper;

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
		private Map<String, ReadingStatus> statusMap = new HashMap<>();

		public ReadingStatusDir(MonitorInfoWrapper wrapper) {
			this.wrapper = wrapper;

			File rstatus = new File(new File(ReadingStatusRoot.this.storePath.getPath(), getDirName()), file_rstatus);

			if (rstatus.exists()) {
				try (FileInputStream fi = new FileInputStream(rstatus)) {
					// ファイルを読み込む
					Properties props = new Properties();
					props.load(fi);
					directory = props.getProperty(ReadingStatusRoot.directory);
					regdate = Long.valueOf(props.getProperty(ReadingStatusRoot.regdate));
					filename = props.getProperty(ReadingStatusRoot.filename);
				} catch (FileNotFoundException e) {
					log.debug(e.getMessage(), e);
				} catch (IOException | NumberFormatException e) {
					log.warn(e.getMessage(), e);
				}
			}
			update(true);
		}
		
		/**
		 * 指定した監視設定で、このインスタンスの内容を更新する。
		 * 
		 * @param mi
		 */
		public void update(MonitorInfoWrapper mi) {
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
			File statusDir = new File(ReadingStatusRoot.this.storePath.getPath(), getDirName());
			if (!statusDir.exists()) {
				if (!statusDir.mkdir()) {
					log.warn("update() : " + statusDir.getPath() + " is not created.");
					return;
				}
			}

			// 読み込み状態ファイル保存ディレクトリの存在確認。
			File filesDir = new File(statusDir, dir_files);
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
				if (!regdate.equals(wrapper.monitorInfo.getRegDate()) || (!new File(directory).getAbsolutePath()
						.equals(new File(wrapper.monitorInfo.getLogfileCheckInfo().getDirectory()).getAbsolutePath()))) {
					log.info(String.format("update() : ReadingStatus is clear. regdate=%d,dir=%s nextregdate=%d,nextdir=%s", regdate, directory,
							wrapper.monitorInfo.getRegDate(), wrapper.monitorInfo.getLogfileCheckInfo().getDirectory()));
					clearFiles();
					refresh = true;
				} else if (!filename.equals(wrapper.monitorInfo.getLogfileCheckInfo().getFileName())) {
					refresh = true;
				}
			} else {
				refresh = true;
			}

			// 読み込み状態ファイルの全体破棄の判断に必要な情報を保存
			if (refresh) {
				try (FileOutputStream fo = new FileOutputStream(new File(statusDir, file_rstatus))) {
					Properties props = new Properties();
					props.put(ReadingStatusRoot.directory, wrapper.monitorInfo.getLogfileCheckInfo().getDirectory());
					props.put(ReadingStatusRoot.filename, wrapper.monitorInfo.getLogfileCheckInfo().getFileName());
					props.put(ReadingStatusRoot.regdate, wrapper.monitorInfo.getRegDate().toString());
					props.store(fo, wrapper.monitorInfo.getMonitorId());
					
					directory = wrapper.monitorInfo.getLogfileCheckInfo().getDirectory();
					filename = wrapper.monitorInfo.getLogfileCheckInfo().getFileName();
					regdate = wrapper.monitorInfo.getRegDate();
				} catch (IOException e) {
					log.warn("update() : " + e.getMessage(), e);
				}
			}

			Pattern pattern = null;
			try {
				pattern = Pattern.compile(wrapper.monitorInfo.getLogfileCheckInfo().getFileName(),
						Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
			} catch (Exception e) {
				if (init)
					// 不正ファイルパターン。処理が継続できないので、処理を戻す。
					log.warn("update() : " + e.getMessage());
				return;
			}

			// シーク対象のファイル一覧を取得。
			File directory = new File(wrapper.monitorInfo.getLogfileCheckInfo().getDirectory()).getAbsoluteFile();
			File[] seekFiles = directory.listFiles();
			if (seekFiles == null) {
				if (init)
					//null が返されるパターンは、なんらかの異常を示している。
					log.warn("update() : " + wrapper.monitorInfo.getLogfileCheckInfo().getDirectory() + " does not have a reference permission");
				return;
			}

			Map<String, ReadingStatusRoot.ReadingStatus> rsMap = new HashMap<>(statusMap);
			for (File file : seekFiles) {
				// ファイルが存在しているか確認。
				log.debug("update() : " + wrapper.getId() + ", file=" + file.getName());
				if (!file.isFile()) {
					log.debug(file.getName() + " is not file");
					continue;
				}
				// ファイルパターンに一致するか？
				Matcher matcher = pattern.matcher(file.getName());
				if (!matcher.matches()) {
					log.debug("update() : don't match. filename=" + file.getName() + ", pattern="
							+ wrapper.monitorInfo.getLogfileCheckInfo().getFileName());
					continue;
				}
				// 最大監視対象数に達していないか？
				if (!ReadingStatusRoot.this.incrementCounter()) {
					log.warn("update() : too many files for logfile. not-monitoring file=" + file.getName());
					continue;
				}

				ReadingStatusRoot.ReadingStatus found = rsMap.get(file.getName());

				// 新規に検知したファイルなら、ファイル状態情報を新たに追加
				if (found == null) {
					ReadingStatus status = new ReadingStatus(this, file, new File(filesDir.getPath(), file.getName()), refresh);
					statusMap.put(file.getName(), status);
				} else {
					rsMap.remove(file.getName());
				}
			}

			// 既に存在しないファイルかパターンマッチしないファイル情報なので削除
			for (Map.Entry<String, ReadingStatusRoot.ReadingStatus> e : rsMap.entrySet()) {
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
				Iterator<File> iter = rsFileList.iterator();
				while (iter.hasNext()) {
					File f = iter.next();
					if (f.getName().equals(fileName)) {
						iter.remove();
						break;
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
		public Collection<ReadingStatus> list() {
			return statusMap.values();
		}

		/**
		 * 
		 */
		private void clearFiles() {
			for (ReadingStatus s : statusMap.values()) {
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
			File statusDir = new File(ReadingStatusRoot.this.storePath.getPath(), getDirName());
			recursiveDeleteFile(statusDir);
		}

		/**
		 * このインスタンスが管理するディレクトリ名を取得する。
		 * 
		 * @return
		 */
		public String getDirName() {
			return dir_prefix + wrapper.getId();
		}

		/**
		 * このインスタンスが従う監視設定を取得する。
		 * 
		 * @return
		 */
		public MonitorInfoWrapper getMonitorInfo() {
			return wrapper;
		}
	}

	private static final String prefix = "prefix";
	private static final String position = "position";
	private static final String carryover = "carryover";
	private static final String prevSize = "prevSize";

	/**
	 * 読み込み状態情報を格納し、ファイルへ保存する。
	 *
	 */
	public static class ReadingStatus {
		// 読み込み状態ファイルのパス
		public final File rsFilePath;

		// 読み込んでいるファイルのパス
		public final File filePath;

		// 読み込んでいるファイルの先頭文字列
		public String prefix = "";

		// 次回読み込み時までの持ち越し分
		public String carryover = "";

		// ファイルハンドラのポジション
		public long position = 0;

		// 前のファイルサイズ
		public long prevSize = 0;
		
		private final ReadingStatusDir parent;
		
		private boolean initialized;
		
		private boolean tail;
		
		public ReadingStatus(ReadingStatusDir parent, File filePath, File rsFilePath, boolean tail) {
			this.parent = parent;
			this.filePath = filePath;
			this.rsFilePath = rsFilePath;
			this.tail = tail;
			initialize();
		}
		
		private boolean initialize() {
			if (this.rsFilePath.exists()) {
				try (FileInputStream fi = new FileInputStream(this.rsFilePath)) {
					// ファイルを読み込む
					Properties props = new Properties();
					props.load(fi);
					
					prefix = props.getProperty(ReadingStatusRoot.prefix);
					position = Long.parseLong(props.getProperty(ReadingStatusRoot.position));
					carryover = props.getProperty(ReadingStatusRoot.carryover);
					prevSize = Long.parseLong(props.getProperty(ReadingStatusRoot.prevSize));
					
					initialized = true;
				} catch (NumberFormatException | IOException e) {
					log.warn(e.getMessage(), e);
				}
			} else {
				try {
					if (tail) {
						try (BufferedReader newFile = new BufferedReader(new InputStreamReader(new FileInputStream(filePath), this.parent.wrapper.monitorInfo.getLogfileCheckInfo().getFileEncoding()))) {
							char[] newFirstPartOfFile = new char[LogfileMonitorConfig.firstPartDataCheckSize];
							
							int num = newFile.read(newFirstPartOfFile);
							prevSize = this.filePath.length();
							position = prevSize;
							
							if (num > 0)
								prefix = new String(newFirstPartOfFile, 0, num);
							
						}
					}
					store();
					
					initialized = true;
				} catch (IOException e) {
					log.warn(e.getMessage(), e);
				}
			}
			return initialized;
		}
		
		/**
		 * ファイル状態の情報を書き出す。
		 */
		public void store() {
			try (FileOutputStream fi = new FileOutputStream(rsFilePath)) {
				Properties props = new Properties();
				props.put(ReadingStatusRoot.prefix, prefix);
				props.put(ReadingStatusRoot.position, String.valueOf(position));
				props.put(ReadingStatusRoot.carryover, carryover);
				props.put(ReadingStatusRoot.prevSize, String.valueOf(prevSize));
				props.store(fi, filePath.getAbsolutePath());
			} catch (IOException e) {
				log.warn(e.getMessage(), e);
			}
		}

		/**
		 * ファイル状態情報を格納しているファイルを削除する
		 */
		public void clear() {
			if (!rsFilePath.delete())
				log.warn(String.format("ReadingStatus.clear() :don't delete file. path=%s", rsFilePath.getName()));
		}
		
		/**
		 * ファイル状態情報を初期化する。
		 */
		public void reset() {
			prefix = "";
			position = 0;
			prevSize = 0;
			carryover = "";
			store();
		}
		
		/**
		 * ファイル状態情報をローテーションする。
		 */
		public void rotate() {
			prefix = "";
			position = 0;
			prevSize = 0;
//			carryover = "";
			store();
		}
		
		public boolean isInitialized() {
			if (!initialized)
				initialize();
			return initialized;
		}
	}

	private Map<String, ReadingStatusDir> statusMap = new HashMap<>();
	private final File storePath;
	private int counter = 0;

	public ReadingStatusRoot(List<MonitorInfoWrapper> miList, String baseDirectory) {
		// ファイルの読み込み状態を格納したディレクトリを確認
		storePath = new File(baseDirectory);
		update(miList);
	}

	/**
	 * ファイル状態情報を格納した全てのディレクトリ情報を取得する。
	 * 
	 * @return
	 */
	public Collection<ReadingStatusDir> getReadingStatusDirList() {
		return statusMap.values();
	}

	/**
	 * 監視設定に対応するファイル状態情報を格納したディレクトリ情報を取得する。
	 * 
	 * @param monitorId
	 * @return
	 */
	public ReadingStatusDir getReadingStatusDir(String monitorId) {
		return statusMap.get(monitorId);
	}

	/**
	 * ファイルシステムを確認し、管理するファイル構成を更新
	 * 
	 */
	public void update() {
		clearCounter();

		for (ReadingStatusDir dir: statusMap.values()) {
			dir.update();
		}
	}

	/**
	 * 指定した監視設定に従って、管理するファイル構成を更新
	 * 
	 * @param miList
	 */
	public void update(List<MonitorInfoWrapper> miList) {
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
		
		Map<String, ReadingStatusDir> curStatusMap = new HashMap<>(statusMap);
		Map<String, ReadingStatusDir> newStatusMap = new HashMap<>();
		for (MonitorInfoWrapper wrapper: miList) {
			ReadingStatusDir dir = curStatusMap.get(wrapper.getId());

			if (dir == null) {
				dir = new ReadingStatusDir(wrapper);
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
		
		for (ReadingStatusDir dir: curStatusMap.values()) {
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
		
		// 不要なファイルを削除
		for (File f : miDirList) {
			if (f.getName().startsWith(dir_prefix)) {
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
	private static boolean recursiveDeleteFile(final File file) {
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

	public int getCounter() {
		return counter;
	}

	private boolean incrementCounter() throws IllegalStateException {
		if (counter >= LogfileMonitorConfig.fileMaxFiles) {
			return false;
		}
		++counter;
		return true;
	}

	private void clearCounter() {
		log.debug("clear counter.");
		counter = 0;
	}
}