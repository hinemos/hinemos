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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.bean.PriorityConstant;
import com.clustercontrol.util.BinaryUtil;
import com.clustercontrol.util.MessageConstant;

/**
 * ファイルの読み込み状態用の抽象クラス
 */
public abstract class AbstractReadingStatus<T extends AbstractFileMonitorInfoWrapper> {
	private static Log log = LogFactory.getLog(AbstractReadingStatus.class);

	private static final String PREFIX_BINARY_KEY = "prefixBinary";
	private static final String POSITION_KEY = "position";
	private static final String CARRYOVER_KEY = "carryover";
	private static final String PREV_SIZE_KEY = "prevSize";
	private static final String UPDATE_COUNT_KEY = "updateCount";

	// 読み込み状態ファイルのパス
	protected final File rsFilePath;

	protected boolean initialized;

	protected boolean tail;

	protected boolean rsFileDispatchFlag = true;

	protected String monitorId;

	// 読み込んでいるファイルのパス
	protected final File filePath;

	// ファイル変更詳細チェック（冒頭データ比較）サイズ（byte）
	protected int firstPartDataCheckSize;

	// 読み込んでいるファイルの先頭バイナリ
	protected List<Byte> prefixBinary = new ArrayList<Byte>();

	// 読み込んでいるファイルの先頭バイナリの16進数文字列
	protected String prefixBinString = "";

	// 次回読み込み時までの持ち越し分
	protected String carryover = "";

	// ファイルハンドラのポジション
	protected long position = 0;

	// 前のファイルサイズ
	protected long prevSize = 0;

	// ファイル更新カウンタ
	public long updateCount = 0;

	private AbstractFileMonitorManager<T> fileMonitorManager;

	public AbstractReadingStatus(AbstractFileMonitorManager<T> fileMonitorManager, String monitorId, File filePath, int firstPartDataCheckSize, File rsFilePath, boolean tail) {
		this.fileMonitorManager = fileMonitorManager;
		this.filePath = filePath;
		this.firstPartDataCheckSize = firstPartDataCheckSize;
		this.monitorId = monitorId;
		this.rsFilePath = rsFilePath;
		this.tail = tail;
		initialize();
	}

	public File getFilePath() {
		return filePath;
	}

	public List<Byte> getPrefixBinary() {
		return prefixBinary;
	}

	public void setPrefixBinary(List<Byte> prefixBinary) {
		this.prefixBinary = prefixBinary;
	}

	public String getPrefixBinString() {
		return prefixBinString;
	}

	public void setPrefixBinString(String prefixBinString) {
		this.prefixBinString = prefixBinString;
	}

	public String getCarryover() {
		return carryover;
	}

	public void setCarryOver(String carryover) {
		this.carryover = carryover;
	}

	public long getPosition() {
		return position;
	}

	public void setPosition(long position) {
		this.position = position;
	}

	public long getPrevSize() {
		return prevSize;
	}

	public void setPrevSize(long prevSize) {
		this.prevSize = prevSize;
	}

	public long getUpdateCount() {
		return updateCount;
	}

	public void setUpdateCount(long updateCount) {
		this.updateCount = updateCount;
	}

	protected boolean initialize() {

		// 初回起動時のみReadingStatusファイルの最新情報を取得する。
		organizeReadingStatus();
		rsFileDispatchFlag = getLastUpdateRsTempFlag();
		File lastReadingStatusFile = new File(getRsTempFilePath());
		Path rsTempFilePath = Paths.get(getRsTempFilePath());
		Path rsFilePath = Paths.get(this.rsFilePath.getAbsolutePath());

		if (java.nio.file.Files.exists(rsTempFilePath)) {
			try (FileInputStream fi = new FileInputStream(lastReadingStatusFile)) {
				// ファイルを読み込む
				Properties props = new Properties();
				props.load(fi);

				prefixBinString = props.getProperty(PREFIX_BINARY_KEY);
				position = Long.parseLong(props.getProperty(POSITION_KEY));
				carryover = props.getProperty(CARRYOVER_KEY);
				prevSize = Long.parseLong(props.getProperty(PREV_SIZE_KEY));
				if(updateCount != 0){
					updateCount = Long.parseLong(props.getProperty(UPDATE_COUNT_KEY));
				}

				// prefixBinStringを元にprefixBinary 初期化.
				if (prefixBinString == null || prefixBinString.isEmpty()) {
					prefixBinary = new ArrayList<Byte>();
					if (log.isDebugEnabled()) {
						log.debug("initialize() : " + filePath + ".prefixBinString is empty");
					}
				} else {
					prefixBinary = BinaryUtil.stringToList(prefixBinString, 1, 1);
					if (log.isDebugEnabled()) {
						log.debug("initialize() : " + filePath + ".prefixBinary size = " + prefixBinary.size());
					}
				}

				initialized = true;
			} catch (NumberFormatException | IOException e) {
				log.warn(e.getMessage(), e);
			}
		} else {
			try {
				if (log.isDebugEnabled()) {
					log.debug("initialize() : reading status file is nothing :" + rsFilePath + " tail=" + tail);
				}
				if (tail) {
					prevSize = this.filePath.length();
					position = prevSize;
					prefixBinary = getCurrentPrefix();
					prefixBinString = BinaryUtil.listToString(prefixBinary, 1);
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
	 * ReadingStatusファイルの整理を行う。
	 * ２つあるReadingStatusファイル(true/false)をチェックし、破損してれば削除する。
	 * ReadingStatusファイルが破損している場合はその旨の通知を行う。
	 */
	protected void organizeReadingStatus() {
		File rsTrueFile = new File(getRsTempFilePath(true));
		File rsFalseFile = new File(getRsTempFilePath(false));
		boolean isTrueFileInValid = false;
		boolean isFalseFileInValid = false;
		boolean asCurrent = false;
		boolean trueFileExists = java.nio.file.Files.exists(Paths.get(getRsTempFilePath(true)));
		boolean falseFileExists = java.nio.file.Files.exists(Paths.get(getRsTempFilePath(false)));
		long trueUpdateCount = 0;
		long falseUpdateCount = 0;
		if (trueFileExists) {
			trueUpdateCount = getUpdateCount(rsTrueFile);
		}
		if (falseFileExists) {
			falseUpdateCount = getUpdateCount(rsFalseFile);
		}

		if( trueFileExists && falseFileExists ){
			if (trueUpdateCount > falseUpdateCount) {
				// trueファイルとfalseファイルのカウンタによって判定
				asCurrent = true;
			} else if (trueUpdateCount == falseUpdateCount){
				// カウンタの値に差がない場合はタイムスタンプによって判定
				if (rsTrueFile.lastModified() >= rsFalseFile.lastModified()) {
					asCurrent = true;
				}else{
					asCurrent = false;
				}
			} else {
				asCurrent = false;
			}
		} else if (trueFileExists) {
			asCurrent = true;
		}

		if (!trueFileExists) {
			log.info("organizeRsFlag():" + rsTrueFile.getAbsolutePath() + " is nothing");
		} else {
			// ReadingStatusファイル(true)ファイルが破損しているか(0バイトファイルもしくは無効な内容)どうかの判定し、該当なら削除
			if (rsTrueFile.length() == 0 || !(isValidContentFile(rsTrueFile))) {
				log.info("organizeRsFlag():" + rsTrueFile.getAbsolutePath() + " is 0 bytes or invalid file." + " size="
						+ rsTrueFile.length() + " content=" + printFile(rsTrueFile));
				if (!rsTrueFile.delete()) {
					log.info("organizeReadingStatus: Failed to delete.");
				}
				isTrueFileInValid = true;
			}
		}

		if (!falseFileExists) {
			log.info("organizeRsFlag():" + rsFalseFile.getAbsolutePath() + " is nothing");
		} else {
			// ReadingStatusファイル(false)ファイルが破損しているか(0バイトファイルもしくは無効な内容)どうかの判定し、該当なら削除
			if (rsFalseFile.length() == 0 || !(isValidContentFile(rsFalseFile))) {
				log.info("organizeRsFlag():" + rsFalseFile.getAbsolutePath() + " is 0 bytes or invalid file." + " size="
						+ rsFalseFile.length() + " content=" + printFile(rsFalseFile));
				//findbugs対応 戻り値をチェックしてログを出力とした。
				boolean ret =rsFalseFile.delete();
				if(!ret){
					log.debug("organizeRsFlag(): rsFalseFile.delete() is false");
				}
				isFalseFileInValid = true;
			}
		}

		if( ( isTrueFileInValid && isFalseFileInValid ) || 
				( isTrueFileInValid && !falseFileExists ) || 
				( !trueFileExists && isFalseFileInValid ) ){
			// 全てのReadingStatusファイル が不正な場合
			// 現時点からの差分を監視する旨を通知
			String[] messageArgs = { filePath.getName() };
			fileMonitorManager.sendMessage(filePath.getAbsolutePath(), PriorityConstant.TYPE_WARNING,
					MessageConstant.AGENT.getMessage(),
					MessageConstant.MESSAGE_LOG_FILE_REBUILD_READING_STATUS.getMessage(messageArgs),
					"rsfile has broken ," + rsTrueFile.getAbsolutePath() + "," + rsFalseFile.getAbsolutePath(),
					monitorId, null);
			// 監視対象ファイルについて、現時点以後の差分を監視するようにフラグを調整
			tail = true;
		} else if ((asCurrent == true && isTrueFileInValid) || (asCurrent == false && isFalseFileInValid)) {
			// 最新のReadingStatusファイル のみ破損の場合
			// バックアップからの差分を監視する旨を通知
			String breakPath = "";
			if (asCurrent == true) {
				breakPath = rsTrueFile.getAbsolutePath();
			} else {
				breakPath = rsFalseFile.getAbsolutePath();
			}

			String[] messageArgs = { filePath.getName() };
			fileMonitorManager.sendMessage(filePath.getAbsolutePath(), PriorityConstant.TYPE_WARNING,
					MessageConstant.AGENT.getMessage(),
					MessageConstant.MESSAGE_LOG_FILE_USE_BACKUP_READING_STATUS.getMessage(messageArgs),
					"current rsfile has broken ," + breakPath, monitorId, null);
		} else if ((asCurrent == false && isTrueFileInValid) || (asCurrent == true && isFalseFileInValid)) {
			// バックアップのReadingStatusファイルのみ破損の場合
			// バックアップが破損していたが、動作に影響はないことを通知
			String breakPath = "";
			if (asCurrent == true) {
				breakPath = rsFalseFile.getAbsolutePath();
			} else {
				breakPath = rsTrueFile.getAbsolutePath();
			}
			String[] messageArgs = { filePath.getName() };
			fileMonitorManager.sendMessage(filePath.getAbsolutePath(), PriorityConstant.TYPE_INFO,
					MessageConstant.AGENT.getMessage(),
					MessageConstant.MESSAGE_LOG_FILE_BREAK_BACKUP_READING_STATUS.getMessage(messageArgs),
					"backup rsfile has broken ," + breakPath, monitorId, null);
		}

	}

	/**
	 * ReadingStatus一時ファイルの最新ファイルを判断する
	 * 
	 * @return 最新ファイルのフラグ
	 */
	private boolean getLastUpdateRsTempFlag() {
		File rsTrueFile = new File(getRsTempFilePath(true));
		File rsFalseFile = new File(getRsTempFilePath(false));
		boolean trueFileExists = java.nio.file.Files.exists(Paths.get(getRsTempFilePath(true)));
		boolean falseFileExists = java.nio.file.Files.exists(Paths.get(getRsTempFilePath(false)));

		if (trueFileExists && falseFileExists) {
			// ファイルが破損しているか(0バイトファイルもしくは無効な内容)どうかの判定
			if (rsTrueFile.length() == 0 || !isValidContentFile(rsTrueFile)) {
				log.info(rsTrueFile.getAbsolutePath() + " is 0 bytes or invalid file." + " size=" + rsTrueFile.length()
						+ " content=" + printFile(rsTrueFile));
				return false;
			}
			if (rsFalseFile.length() == 0 || !isValidContentFile(rsFalseFile)) {
				log.info(rsFalseFile.getAbsolutePath() + " is 0 bytes or invalid file." + " size="
						+ rsFalseFile.length() + " content=" + printFile(rsFalseFile));
				return true;
			}
			// どちらが最新かの判定
			long rsTrueFileLastModified = getUpdateCount(rsTrueFile);
			long rsFalseFileLastModified = getUpdateCount(rsFalseFile);
			log.debug("rsTrueFileLastModified = " + rsTrueFileLastModified);
			log.debug("rsFalseFileLastModified = " + rsFalseFileLastModified);
			if (rsTrueFileLastModified > rsFalseFileLastModified) {
				// trueファイルとfalseファイルのカウンタによって判定
				if (rsTrueFile.lastModified() >= rsFalseFile.lastModified()) {
					return true;
				} else {
					return false;
				}
			} else if (rsTrueFileLastModified < rsFalseFileLastModified) {
				// trueファイルとfalseファイルのカウンタによって判定
				if (rsTrueFile.lastModified() <= rsFalseFile.lastModified()) {
					return false;
				} else {
					return true;
				}
			} else {
				// カウンタの値に差がない場合はタイムスタンプによって判定
				if (rsTrueFile.lastModified() >= rsFalseFile.lastModified()) {
					return true;
				} else {
					return false;
				}
			}
		} else if (trueFileExists) {
			log.debug("rsTrueFile is exists.");
			return true;
		} else {
			log.debug("rsFalseFile is not exists. or rsFalseFile and rsTrueFile is not exists.");
			return false;
		}
	}

	/**
	 * 有効な内容のReadingStatusファイルである確認する
	 * 
	 * @return 有効な場合はtrue。そうでない場合はfalse
	 */
	private boolean isValidContentFile(File f) {
		boolean isValid = true;
		try (FileInputStream fi = new FileInputStream(f)) {
			// ファイルを読み込む
			Properties props = new Properties();
			props.load(fi);

			// 指定のプロパティが見つからない場合無効と判断する
			if (props.getProperty(PREFIX_BINARY_KEY) == null
					|| props.getProperty(POSITION_KEY) == null
					|| props.getProperty(CARRYOVER_KEY) == null
					|| props.getProperty(PREV_SIZE_KEY) == null) {
				isValid = false;
			}

		} catch (Exception e) {
			isValid = false;
			log.warn(e.getMessage(), e);
		}
		return isValid;
	}

	/**
	 * ファイルの内容を表示する
	 * 
	 * @return ファイルの内容
	 */
	private String printFile(File f) {
		StringBuffer buffer = new StringBuffer();
		try (BufferedReader reader = new BufferedReader(new FileReader(f))) {
			String line;
			while ((line = reader.readLine()) != null) {
				buffer.append(line + System.getProperty("line.separator"));
			}
		} catch (IOException e) {
			log.warn(f.getAbsolutePath() + "," + e.getMessage());
		}
		return buffer.toString();
	}

	/**
	 * 交互に更新するReadingStatus(true/false)ファイルパスの取得
	 * 
	 * @return 最新のReadingStatusファイルパス
	 */
	private String getRsTempFilePath() {
		return getRsTempFilePath(rsFileDispatchFlag);
	}

	private String getRsTempFilePath(boolean flag) {
		String rsFileTempPath = null;
		if (flag) {
			rsFileTempPath = rsFilePath + ".t";
		} else {
			rsFileTempPath = rsFilePath + ".f";
		}
		log.debug("rsFileTempPath is " + rsFileTempPath);
		return rsFileTempPath;
	}

	/**
	 * ファイル状態の情報を書き出す。
	 */
	public void store() {
		File rsTrueFile = new File(getRsTempFilePath(true));
		File rsFalseFile = new File(getRsTempFilePath(false));
		boolean trueFileExists = java.nio.file.Files.exists(Paths.get(getRsTempFilePath(true)));
		boolean falseFileExists = java.nio.file.Files.exists(Paths.get(getRsTempFilePath(false)));
		long trueUpdateCount = 0;
		long falseUpdateCount = 0;
		if (trueFileExists) {
			trueUpdateCount = getUpdateCount(rsTrueFile);
		}
		if (falseFileExists) {
			falseUpdateCount = getUpdateCount(rsFalseFile);
		}
		// trueファイルとfalseファイルのカウンタによって判定(エージェントの再起動後)
		if (trueUpdateCount > falseUpdateCount) {
			if (rsTrueFile.lastModified() >= rsFalseFile.lastModified()) {
				rsFileDispatchFlag = false;
			} else {
				rsFileDispatchFlag = true;
			}
		} else if (trueUpdateCount < falseUpdateCount) {
			if (rsTrueFile.lastModified() <= rsFalseFile.lastModified()) {
				rsFileDispatchFlag = true;
			} else {
				rsFileDispatchFlag = false;
			}
		} else if (trueUpdateCount == falseUpdateCount) {
			// カウンタの値に差がない場合はタイムスタンプによって判定
			if (rsTrueFile.lastModified() >= rsFalseFile.lastModified()) {
				rsFileDispatchFlag = false;
			} else {
				rsFileDispatchFlag = true;
			}
		}

		// 交互に作成する ReadingStatus(true/false)ファイルのフラグ反転後、更新。
		// store() を呼び出すたびにフラグは必ず更新する。
		String rsFileTempPath = getRsTempFilePath();
		rsFileDispatchFlag = !rsFileDispatchFlag;

		File tmpFilePath = new File(rsFileTempPath);
		try (FileOutputStream fi = new FileOutputStream(tmpFilePath)) {
			Properties props = new Properties();

			props.put(PREFIX_BINARY_KEY, String.valueOf(prefixBinString));
			props.put(POSITION_KEY, String.valueOf(position));
			props.put(CARRYOVER_KEY, carryover);
			props.put(PREV_SIZE_KEY, String.valueOf(prevSize));
			props.put(UPDATE_COUNT_KEY, String.valueOf(++updateCount));
			props.store(fi, filePath.getAbsolutePath());
		} catch (IOException e) {
			log.warn(e.getMessage(), e);
		}
	}

	/**
	 * ファイル状態情報を格納しているファイルを削除する
	 */
	public void clear() {
		File rsTrueFile = new File(getRsTempFilePath(true));
		boolean trueFileExists = java.nio.file.Files.exists(Paths.get(getRsTempFilePath(true)));
		boolean falseFileExists = java.nio.file.Files.exists(Paths.get(getRsTempFilePath(false)));
		
		if (trueFileExists) {
			log.debug("rsTrueFile is exists. execute delete.");
			if (!rsTrueFile.delete()) {
				log.warn(String.format("ReadingStatus.clear() :don't delete file. path=%s", rsTrueFile.getName()));
			}
		}
		File rsFalseFile = new File(getRsTempFilePath(false));
		if (falseFileExists) {
			log.debug("rsFalseFile is exists. execute delete.");
			if (!rsFalseFile.delete()) {
				log.warn(String.format("ReadingStatus.clear() :don't delete file. path=%s", rsFalseFile.getName()));
			}
		}
	}

	/**
	 * ファイル状態情報を初期化する。
	 */
	public void reset() {
		prefixBinary = new ArrayList<Byte>();
		prefixBinString = "";
		position = 0;
		prevSize = 0;
		carryover = "";
		updateCount = 0;
		store();
	}

	public List<Byte> getCurrentPrefix() throws IOException {
		File monFile = filePath;
		if (log.isDebugEnabled()) {
			log.debug("getCurrentPrefix() :start. :" + monFile);
		}
		// 指定バイト数だけ読込むため配列長をAgentPropertiesで指定.
		byte[] monFileByteArray = new byte[firstPartDataCheckSize];
		List<Byte> firstPartOfFile;
		if (monFile.length() == 0) {
			if (log.isDebugEnabled()) {
				log.debug("getCurrentPrefix() : " + filePath + " is size 0.");
			}
			return new ArrayList<Byte>();
		}
		try (FileInputStream fi = new FileInputStream(monFile)) {
			// 監視対象ファイルの先頭から作成したバイト配列長分だけ読込む.
			int readed = fi.read(monFileByteArray);
			if (readed != monFileByteArray.length && readed != monFile.length() && readed != -1) {
				// 読込み長は prefix最大長 or ファイル全長 or ファイル末尾(-1) のいずれか想定なので
				// 違った場合は警告を出力
				log.warn("getCurrentPrefix() : " + monFile + " monFileByteArray length too short : readed=" + readed
						+ " monFile.length()=" + monFile.length() + " monFileByteArray.length="
						+ monFileByteArray.length);
			}
			firstPartOfFile = BinaryUtil.arrayToList(monFileByteArray);
			// 指定バイト数より短いならListを読み込めた長さに調整
			if (0 < readed && readed < firstPartDataCheckSize) {
				firstPartOfFile = firstPartOfFile.subList(0, readed);
			}
			fi.close();
		} catch (IOException e) {
			log.warn("getCurrentPrefix() :" + e.getMessage(), e);
			throw e;
		}
		if (log.isDebugEnabled()) {
			log.debug("getCurrentPrefix() :" + monFile + " , firstPartOfFile size = " + firstPartOfFile.size());
		}
		return firstPartOfFile;
	}

	/**
	 * 状態情報をローテーションする
	 */
	public void rotate() {
		prefixBinary = new ArrayList<Byte>();
		prefixBinString = "";
		position = 0;
		prevSize = 0;
		updateCount = 0;
		store();
	}

	public boolean isInitialized() {
		if (!initialized)
			initialize();
		return initialized;
	}

	private long getUpdateCount(File rsFile){
		long retUpdateCount = 0;
		try (FileInputStream fi = new FileInputStream(rsFile)) {
			// ファイルを読み込む
			Properties props = new Properties();
			props.load(fi);
			retUpdateCount = Long.parseLong(props.getProperty(UPDATE_COUNT_KEY));
		} catch (IOException | NumberFormatException e) {
			log.warn("UpdateCount Nothing. file :" + rsFile.getName());
			log.debug(e.getMessage(), e);
		}
		return retUpdateCount;
	}

}
