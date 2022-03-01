/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.agent.filecheck;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openapitools.client.model.AgtCalendarInfoResponse;
import org.openapitools.client.model.AgtJobFileCheckRequest;
import org.openapitools.client.model.AgtJobFileCheckResponse;
import org.openapitools.client.model.SetFileCheckResultRequest;
import org.openapitools.client.model.SetFileCheckResultResponse;

import com.clustercontrol.agent.Agent;
import com.clustercontrol.agent.AgentRestClientWrapper;
import com.clustercontrol.agent.util.AgentProperties;
import com.clustercontrol.agent.util.RandomAccessFileWrapper;
import com.clustercontrol.agent.util.RestAgentBeanUtil;
import com.clustercontrol.agent.util.RestCalendarUtil;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.jobmanagement.bean.FileCheckConstant;

/**
 * ファイルチェックの対象ファイルごとに生成されるクラスです。
 * 
 */
public class FileCheck {

	// ロガー
	private static Log m_log = LogFactory.getLog(FileCheck.class);

	// 状態保持用のキャッシュ
	private ConcurrentHashMap<String, Long> fileTimestampCache = new ConcurrentHashMap<String, Long>();
	private ConcurrentHashMap<String, Long> fileSizeCache = new ConcurrentHashMap<String, Long>();
	private ConcurrentHashMap<String, Boolean> fileTimestampFlagCache = new ConcurrentHashMap<String, Boolean>();
	private ConcurrentHashMap<String, Boolean> fileSizeFlagCache = new ConcurrentHashMap<String, Boolean>();
	// 判定持ち越し用のキャッシュ
	private ConcurrentHashMap<String, FileCheckInfo> checkCarriedOverCache = new ConcurrentHashMap<>();

	private boolean initFlag = true;
	// ジョブからの呼び出しかどうか
	private boolean isJob = false;
	// 判定を通過したファイルチェックの情報
	private FileCheckInfo passedFileCheck = null;

	private static int resultRetryCount = -1;
	private static int resultRetrySleep = -1;

	/** ディレクトリ名 */
	private String m_directory;

	private Map<String, FileCheckInfo> fileCheckInfoMap = new ConcurrentHashMap<>();

	/**
	 * コンストラクタ
	 * 
	 * @param directory
	 */
	public FileCheck(String directory) {
		m_directory = directory;
	}

	/**
	 * コンストラクタ
	 * 
	 * @param directory
	 * @param isJob
	 * @param fileCeckInfo
	 */
	public FileCheck(String directory, boolean isJob, FileCheckInfo fileCeckInfo) {
		m_directory = directory;
		this.isJob = isJob;
		fileCheckInfoMap.put(fileCeckInfo.getKey(), fileCeckInfo);
	}

	/**
	 * ファイルチェック実行契機情報の設定
	 * 
	 * @param jobFileCheckList
	 */
	public void setJobFileCheckList(List<AgtJobFileCheckResponse> jobFileCheckList) {
		this.fileCheckInfoMap = new ConcurrentHashMap<>();
		if (jobFileCheckList == null) {
			return;
		}
		for (AgtJobFileCheckResponse jobFileCheck : jobFileCheckList) {
			FileCheckInfo info = new FileCheckInfo(jobFileCheck.getId());
			info.setFileNamePattern(jobFileCheck.getFileName());
			// ファイルチェック実行契機の場合はいずれか一つのみtrueになる
			if (jobFileCheck.getEventType() == FileCheckConstant.TYPE_CREATE) {
				info.setCreateValidFlg(true);
			} else if (jobFileCheck.getEventType() == FileCheckConstant.TYPE_MODIFY) {
				info.setModifyValidFlg(true);
			} else if (jobFileCheck.getEventType() == FileCheckConstant.TYPE_DELETE) {
				info.setDeleteValidFlg(true);
			}
			info.setModifyType(jobFileCheck.getModifyType());
			info.setNotJudgeFileInUseFlg(jobFileCheck.getCarryOverJudgmentFlg());
			info.setAgtJobFileCheckResponse(jobFileCheck);
			fileCheckInfoMap.put(info.getKey(), info);
		}
	}

	public Integer sizeJobFileCheckList() {
		if (fileCheckInfoMap == null) {
			return null;
		}
		return fileCheckInfoMap.size();
	}

	public FileCheckInfo getPassedFileCheck() {
		return this.passedFileCheck;
	}

	/**
	 * 判定を通過したかどうか
	 * 
	 * @return
	 */
	public boolean isPassed() {
		return passedFileCheck != null;
	}

	/**
	 * スレッドの動作メソッド<BR>
	 * 
	 */
	public void run() {
		m_log.debug("check start. directory=" + m_directory);

		List<FileCheckInfo> passedList = new ArrayList<>();

		// 1. ファイルオープン
		File directory = new File(m_directory);
		if (!directory.isDirectory()) {
			m_log.warn(m_directory + " is not directory");
			runEndDirectoryError();
			return;
		}
		File[] files = directory.listFiles();
		if (files == null) {
			m_log.warn(m_directory + " does not have a reference permission");
			runEndDirectoryError();
			return;
		}
		List<File> fileList = new ArrayList<>();
		
		for (File file : files) {
			if (!file.isFile()) {
				m_log.debug(file.getName() + " is not file");
				continue;
			}
			fileList.add(file);
		}

		// 2. ファイルの生存チェック
		List<String> filenameList = new ArrayList<>();
		for (File file : fileList) {
			filenameList.add(file.getName());
		}
		for (String filename : fileTimestampCache.keySet()) {
			if (!filenameList.contains(filename)) {
				fileTimestampCache.remove(filename);
				fileTimestampFlagCache.remove(filename);
				fileSizeCache.remove(filename);
				fileSizeFlagCache.remove(filename);
				for (FileCheckInfo check : fileCheckInfoMap.values()) {
					if (check.isDeleteValidFlg() &&
							matchFile(check, filename)) {
						m_log.info("passedList.add [" + filename + "] (delete)");
						FileCheckInfo passed = getCopy(check);
						passed.setPassedFileName(filename);
						passed.setPassedEventType(FileCheckConstant.RESULT_DELETE);
						passedList.add(passed);
					}
				}
			}
		}

		// 3. 最終更新日時のチェック
		for (File file : fileList) {
			String filename = file.getName();
			Long newTimestamp = file.lastModified();
			Long oldTimestamp = fileTimestampCache.get(filename);
			if (oldTimestamp == null) {
				fileTimestampCache.put(filename, newTimestamp);
				fileTimestampFlagCache.put(filename, false);
				for (FileCheckInfo check : fileCheckInfoMap.values()) {
					if (check.isCreateValidFlg() &&
							matchFile(check, filename)) {
						FileCheckInfo passed = getCopy(check);
						passed.setPassedFileName(filename);
						if (initFlag && check.isCreateBeforeInitFlg()) {
							// 開始前に存在するファイルも対象にする場合
							m_log.info("passedList.add [" + filename + "] (exists)");
							passed.setPassedEventType(FileCheckConstant.RESULT_EXIST);
						} else {
							m_log.info("passedList.add [" + filename + "] (create)");
							passed.setPassedEventType(FileCheckConstant.RESULT_CREATE);
						}
						passedList.add(passed);
					}
				}
			} else if (!oldTimestamp.equals(newTimestamp)) {
				m_log.info("timestamp : " + oldTimestamp + "->" + newTimestamp + " (" + filename+ ")");
				fileTimestampCache.put(filename, newTimestamp);
				fileTimestampFlagCache.put(filename, true);
			} else {
				if (fileTimestampFlagCache.get(filename) != null &&
						fileTimestampFlagCache.get(filename)) {
					// 変更中から変更なしに遷移したタイミングでキックする。
					for (FileCheckInfo check : fileCheckInfoMap.values()) {
						if (check.isModifyValidFlg() &&
								check.getModifyType() == FileCheckConstant.TYPE_MODIFY_TIMESTAMP &&
								matchFile(check, filename)) {
							m_log.info("passedList.add [" + filename + "] (timestamp)");
							FileCheckInfo passed = getCopy(check);
							passed.setPassedFileName(filename);
							passed.setPassedEventType(FileCheckConstant.RESULT_MODIFY_TIMESTAMP);
							passed.setFileTimestamp(newTimestamp);
							passedList.add(passed);
						}
					}
				}
				fileTimestampFlagCache.put(filename, false);
			}
		}

		// 4. ファイルサイズのチェック
		for (File file : fileList) {
			String filename = file.getName();
			RandomAccessFileWrapper fr = null;
			try {
				fr = new RandomAccessFileWrapper(file, "r");
				Long newSize = fr.length();
				Long oldSize = fileSizeCache.get(filename);
				if (oldSize == null) {
					fileSizeCache.put(filename, newSize);
					fileSizeFlagCache.put(filename, false);
				} else if (!oldSize.equals(newSize)) {
					m_log.info("size : " + oldSize + "->" + newSize + " (" + filename + ")");
					fileSizeCache.put(filename, newSize);
					fileSizeFlagCache.put(filename, true);
				} else {
					if (fileSizeFlagCache.get(filename) != null &&
							fileSizeFlagCache.get(filename)) {
						// 変更中から変更なしに遷移したタイミングでキックする。
						for (FileCheckInfo check : fileCheckInfoMap.values()) {
							if (check.isModifyValidFlg() &&
									check.getModifyType() == FileCheckConstant.TYPE_MODIFY_FILESIZE &&
									matchFile(check, filename)) {
								m_log.info("passedList.add [" + filename + "] (filesize)");
								FileCheckInfo passed = getCopy(check);
								passed.setPassedFileName(filename);
								passed.setPassedEventType(FileCheckConstant.RESULT_MODIFY_FILESIZE);
								passed.setFileSize(newSize);
								passedList.add(passed);
							}
						}
					}
					fileSizeFlagCache.put(filename, false);
				}
			} catch (IOException e) {
				m_log.info("run() : IOException: " + e.getMessage());
			} catch (Exception e) {
				m_log.warn("run() : IOException: " + e.getMessage());
			} finally {
				if (fr != null) {
					try {
						fr.close();
					} catch (final Exception e) {
						m_log.debug("run() : " + e.getMessage());
					}
				}
			}
		}

		Iterator<FileCheckInfo> itr = passedList.iterator();
		while (itr.hasNext()) {
			// チェックを通過したファイルをこの先の処理へ進ませるか選別する
			FileCheckInfo info = itr.next();
			if (info.getPassedEventType() != FileCheckConstant.RESULT_EXIST) {
				// 「存在」以外は1回目のチェック時は通過しない
				if (initFlag) {
					m_log.info("skip [" + info.getPassedFileName() + "] (already exists from the first check)");
					itr.remove();
					continue;
				}
			}
			// 判定持ち越しフラグが有効、かつ通過した判定が「削除」以外の場合
			if (info.isNotJudgeFileInUseFlg() && info.getPassedEventType() != FileCheckConstant.RESULT_DELETE) {
				// 判定持ち越し用キャッシュに追加し、一旦通過リストから除外する
				// ※すでにキャッシュに登録済みの場合は上書きする（最終的な判定を用いるため）
				checkCarriedOverCache.put(info.getCarryOverKey(), info);
				itr.remove();
			}
		}

		// 5. 他プロセスによりファイルが使用中か確認する。
		for (String key : checkCarriedOverCache.keySet()) {
			FileCheckInfo info = checkCarriedOverCache.get(key);
			File file = new File(new File(m_directory), info.getPassedFileName());

			if (!file.exists() || !file.isFile()) {
				// 判定持ち越し中に削除された場合は判定からやり直すため除外する
				checkCarriedOverCache.remove(key);
				continue;
			}

			if (fileCheckInfoMap.get(info.getKey()) == null) {
				// すでに有効な設定ではない場合除外する（実行契機の設定変更を想定）
				checkCarriedOverCache.remove(key);
				continue;
			}

			// 他プロセスにより使用されているかチェック
			if (checkCarryOver(file)) {
				// 使用されている場合は判定を持ち越し
				continue;
			}
			// 使用されていなければキャッシュから削除し、通過リストに追加
			checkCarriedOverCache.remove(key);
			passedList.add(info);
		}

		// 6. 判定終了後の処理を実施する
		runEnd(passedList);
	}

	/**
	 * run()終了時の処理<BR>
	 * 
	 * @param passedList
	 */
	private void runEnd(List<FileCheckInfo> passedList) {
		// 初回フラグを折る
		initFlag = false;
		if (passedList.size() == 0) {
			return;
		}

		if (isJob) {
			// Jobの結果を送信する。

			// 複数ファイルが同じタイミングで判定を通過する可能性があるため最初の1件のみ取得する
			// どれが通過するかは保証しない
			passedFileCheck = passedList.get(0);
			if (passedList.size() > 1) {
				StringBuilder sb = new StringBuilder();
				for (FileCheckInfo info : passedList) {
					if (sb.length() > 0) {
						sb.append(",");
					}
					sb.append(info.getPassedFileName());
				}
				m_log.info("run() : passedFile=" + passedFileCheck.getPassedFileName() + ", list=" + sb.toString());
			}

		} else {
			// Jobをキックする。
			for (FileCheckInfo passedCheck : passedList) {
				AgtJobFileCheckResponse jobFileCheck = passedCheck.getAgtJobFileCheckResponse();
				jobFileCheck.setFileName(passedCheck.getPassedFileName());

				m_log.info("kick " + jobFileCheck.getId());
				String calendarId = jobFileCheck.getCalendarId();
				AgtCalendarInfoResponse calendarInfo = jobFileCheck.getCalendarInfo();
				boolean run = true;
				if (calendarId != null && calendarInfo == null) {
					m_log.info("unknown error : id=" + jobFileCheck.getId() + "calendarId=" + calendarId);
				}
				if (calendarInfo != null) {
					run = RestCalendarUtil.isRun(calendarInfo);
				}

				if (!run) {
					m_log.info("not exec(calendar) : id=" + jobFileCheck.getId() + "calendarId=" + calendarId);
					continue;
				}
				try {
					String sessionId = jobFileCheckResultRetry(jobFileCheck);
					String jobunitId = jobFileCheck.getJobunitId();
					String jobId = jobFileCheck.getJobId();
					m_log.info("jobFileCheckResult sessionId=" + sessionId +
							", jobunitId=" + jobunitId + ", jobId=" + jobId);
				} catch (Exception e) {
					m_log.warn("run(jobFileCheckResult) : " + e.getClass().getSimpleName() +
							", " + e.getMessage(), e);
				}
			}
		}
	}

	private boolean matchFile(FileCheckInfo check, String filename) {
		Pattern pattern = null;
		String patternText = check.getFileNamePattern();
		// 大文字・小文字を区別しない場合
		pattern = Pattern.compile(patternText, Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
		// 大文字・小文字を区別する場合
		// pattern = Pattern.compile(patternText, Pattern.DOTALL);
		Matcher matcher = pattern.matcher(filename);

		return matcher.matches();
	}

	/**
	 * チェック対象に指定されたディレクトリ自体に異常があった場合の終了処理
	 */
	private void runEndDirectoryError() {
		// ディレクトリごと削除された場合も削除として検知するためキャッシュを確認する
		List<FileCheckInfo> passedList = new ArrayList<>();
		for (String filename : fileTimestampCache.keySet()) {
			for (FileCheckInfo check : fileCheckInfoMap.values()) {
				if (check.isDeleteValidFlg() && matchFile(check, filename)) {
					m_log.info("passedList.add [" + filename + "] (delete)");
					FileCheckInfo passed = getCopy(check);
					passed.setPassedFileName(filename);
					passed.setPassedEventType(FileCheckConstant.RESULT_DELETE);
					passedList.add(passed);
				}
			}
		}

		// キャッシュをクリアする
		fileTimestampCache.clear();
		fileTimestampFlagCache.clear();
		fileSizeCache.clear();
		fileSizeFlagCache.clear();
		checkCarriedOverCache.clear(); // 判定持ち越しもクリアする

		// 終了時の共通処理を実施する
		runEnd(passedList);
	}

	/**
	 * 対象ファイルの判定を持ち越すかどうか判定する
	 * 
	 * @return true:持ち越す, false:持ち越さない
	 */
	private boolean checkCarryOver(File file) {
		boolean rtn = true;

		try {
			// 使用中か判定
			rtn = FileInUseCheck.isInUse(file);

		} catch (Exception e) {
			m_log.warn("checkCarryOver() : " + e.getClass().getSimpleName() + ", " + e.getMessage());
			// エラーの場合は持ち越す
			return true;
		}
		if (rtn) {
			m_log.info("checkCarryOver() : " + file.getName() + " is used by other process. Carry over.");
		} else {
			m_log.debug("checkCarryOver() : passed. file=" + file.getName());
		}
		return rtn;
	}

	private FileCheckInfo getCopy(FileCheckInfo org) {
		FileCheckInfo ret = new FileCheckInfo(org.getKey());
		ret.setFileNamePattern(org.getFileNamePattern());
		ret.setCreateValidFlg(org.isCreateValidFlg());
		ret.setCreateBeforeInitFlg(org.isCreateBeforeInitFlg());
		ret.setDeleteValidFlg(org.isDeleteValidFlg());
		ret.setModifyValidFlg(org.isModifyValidFlg());
		ret.setModifyType(org.getModifyType());
		ret.setNotJudgeFileInUseFlg(org.isNotJudgeFileInUseFlg());
		if (org.getAgtJobFileCheckResponse() != null) {
			ret.setAgtJobFileCheckResponse(getCopy(org.getAgtJobFileCheckResponse()));
		}
		return ret;
	}

	private AgtJobFileCheckResponse getCopy(AgtJobFileCheckResponse org) {
		AgtJobFileCheckResponse ret = new AgtJobFileCheckResponse();
		ret.setCalendarId(org.getCalendarId());
		ret.setCalendarInfo(org.getCalendarInfo());
		ret.setCreateTime(org.getCreateTime());
		ret.setDirectory(org.getDirectory());
		ret.setEventType(org.getEventType());
		ret.setFacilityId(org.getFacilityId());
		ret.setFileName(org.getFileName());
		ret.setId(org.getId());
		ret.setJobId(org.getJobId());
		ret.setJobName(org.getJobName());
		ret.setJobunitId(org.getJobunitId());
		ret.setModifyType(org.getModifyType());
		ret.setName(org.getName());
		ret.setType(org.getType());
		ret.setUpdateTime(org.getUpdateTime());
		ret.setUpdateUser(org.getUpdateUser());
		ret.setValid(org.getValid());
		return ret;
	}

	/**
	 * ファイルチェック結果をマネージャに送信する。
	 * 送信に失敗した場合、1分後にリトライする。
	 */
	private static String jobFileCheckResultRetry(AgtJobFileCheckResponse jobFileCheck) {
		if (resultRetryCount < 0) {
			String str = AgentProperties.getProperty("job.filecheck.sender.tries", "15");
			resultRetryCount = Integer.parseInt(str);
			m_log.info("filecheck.retry.count=" + resultRetryCount);
		}
		if (resultRetrySleep < 0) {
			String str = AgentProperties.getProperty("job.filecheck.sender.interval", "60000");
			resultRetrySleep = Integer.parseInt(str);
			m_log.info("filecheck.retry.sleep=" + resultRetrySleep);
		}

		// ユニークIDを生成して、ファイル名の後ろへ付与する
		FileCheckEventId uid = new FileCheckEventId();
		jobFileCheck.setFileName(jobFileCheck.getFileName() + "/" + uid.getBase64());

		// リクエスト情報を構築
		SetFileCheckResultRequest req = new SetFileCheckResultRequest();
		req.setAgentInfo(Agent.getAgentInfoRequest());
		req.setJobFileCheckRequest(new AgtJobFileCheckRequest());
		try {
			RestAgentBeanUtil.convertBean(jobFileCheck, req.getJobFileCheckRequest());
		} catch (HinemosUnknown never) {
			throw new RuntimeException(never);
		}

		for (int i = 0; i < resultRetryCount; i++) {
			try {
				SetFileCheckResultResponse result = AgentRestClientWrapper.setFileCheckResult(jobFileCheck.getId(), req);
				return result.getSessionId();
			} catch (Exception t) {
				try {
					Thread.sleep(resultRetrySleep);
				} catch (InterruptedException e) {
					m_log.warn("sendNotifyRetry " + e.getMessage());
				}
			}
		}
		m_log.warn("give up jobFileCheckResultRetry. Maybe, manager is down");
		return null;
	}
}