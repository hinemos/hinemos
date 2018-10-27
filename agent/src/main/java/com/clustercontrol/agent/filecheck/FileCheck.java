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
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.agent.AgentEndPointWrapper;
import com.clustercontrol.agent.util.AgentProperties;
import com.clustercontrol.agent.util.CalendarWSUtil;
import com.clustercontrol.agent.util.RandomAccessFileWrapper;
import com.clustercontrol.jobmanagement.bean.FileCheckConstant;
import com.clustercontrol.ws.agent.FacilityNotFound_Exception;
import com.clustercontrol.ws.agent.HinemosUnknown_Exception;
import com.clustercontrol.ws.agent.InvalidRole_Exception;
import com.clustercontrol.ws.agent.InvalidUserPass_Exception;
import com.clustercontrol.ws.agent.JobInfoNotFound_Exception;
import com.clustercontrol.ws.agent.JobMasterNotFound_Exception;
import com.clustercontrol.ws.agent.JobSessionDuplicate_Exception;
import com.clustercontrol.ws.agent.MonitorNotFound_Exception;
import com.clustercontrol.ws.calendar.CalendarInfo;
import com.clustercontrol.ws.jobmanagement.JobFileCheck;

/**
 * ログファイル監視<BR>
 */
public class FileCheck {

	// ロガー
	private static Log m_log = LogFactory.getLog(FileCheck.class);

	// 状態保持用のキャッシュ
	private ConcurrentHashMap<String, Long> fileTimestampCache = new ConcurrentHashMap<String, Long>();
	private ConcurrentHashMap<String, Long> fileSizeCache = new ConcurrentHashMap<String, Long>();
	private ConcurrentHashMap<String, Boolean> fileTimestampFlagCache = new ConcurrentHashMap<String, Boolean>();
	private ConcurrentHashMap<String, Boolean> fileSizeFlagCache = new ConcurrentHashMap<String, Boolean>();

	private boolean initFlag = true;

	private static int resultRetryCount = -1;
	private static int resultRetrySleep = -1;

	/** ディレクトリ名 */
	private String m_directory;

	private ArrayList<JobFileCheck> m_jobFileCheckList = new ArrayList<JobFileCheck>();

	/**
	 * コンストラクタ
	 * 
	 * @param queue
	 * @param props
	 * @param path
	 *            転送対象ログファイル
	 * @param flg
	 *            最初にファイルをチェック
	 */
	public FileCheck(String directory) {
		m_directory = directory;
	}

	public void setJobFileCheckList(ArrayList<JobFileCheck> jobFileCheckList) {
		this.m_jobFileCheckList = jobFileCheckList;
	}

	public Integer sizeJobFileCheckList() {
		if (m_jobFileCheckList == null) {
			return null;
		}
		return m_jobFileCheckList.size();
	}

	/**
	 * スレッドの動作メソッド<BR>
	 * 
	 */
	public void run() {
		m_log.debug("check start. directory=" + m_directory);

		ArrayList<JobFileCheck> kickList = new ArrayList<JobFileCheck>();

		// 1. ファイルオープン
		File directory = new File(m_directory);
		if (!directory.isDirectory()) {
			m_log.warn(m_directory + " is not directory");
			return;
		}
		File[] files = directory.listFiles();
		if (files == null) {
			m_log.warn(m_directory + " does not have a reference permission");
			return;
		}
		ArrayList<File> fileList = new ArrayList<File>();
		
		for (File file : files) {
			if (!file.isFile()) {
				m_log.debug(file.getName() + " is not file");
				continue;
			}
			fileList.add(file);
		}

		// 2. ファイルの生存チェック
		ArrayList<String> filenameList = new ArrayList<String> ();
		for (File file : fileList) {
			filenameList.add(file.getName());
		}
		for (String filename : fileTimestampCache.keySet()) {
			if (!filenameList.contains(filename)) {
				fileTimestampCache.remove(filename);
				fileTimestampFlagCache.remove(filename);
				fileSizeCache.remove(filename);
				fileSizeFlagCache.remove(filename);
				for (JobFileCheck check : m_jobFileCheckList) {
					if (check.getEventType() == FileCheckConstant.TYPE_DELETE &&
							matchFile(check, filename)) {
						m_log.info("kickList.add [" + filename + "] (delete)");
						JobFileCheck kick = getCopy(check);
						kick.setFileName(filename);
						kickList.add(kick);
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
				for (JobFileCheck check : m_jobFileCheckList) {
					if (check.getEventType() == FileCheckConstant.TYPE_CREATE &&
							matchFile(check, filename)) {
						m_log.info("kickList.add [" + filename + "] (create)");
						JobFileCheck kick = getCopy(check);
						kick.setFileName(filename);
						kickList.add(kick);
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
					for (JobFileCheck check : m_jobFileCheckList) {
						if (check.getEventType() == FileCheckConstant.TYPE_MODIFY &&
								check.getModifyType() == FileCheckConstant.TYPE_MODIFY_TIMESTAMP &&
								matchFile(check, filename)) {
							m_log.info("kickList.add [" + filename + "] (timestamp)");
							JobFileCheck kick = getCopy(check);
							kick.setFileName(filename);
							kickList.add(kick);
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
						for (JobFileCheck check : m_jobFileCheckList) {
							if (check.getEventType() == FileCheckConstant.TYPE_MODIFY &&
									check.getModifyType() == FileCheckConstant.TYPE_MODIFY_FILESIZE &&
									matchFile(check, filename)) {
								m_log.info("kickList.add [" + filename + "] (filesize)");
								JobFileCheck kick = getCopy(check);
								kick.setFileName(filename);
								kickList.add(kick);
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

		// 1回目のチェック時はジョブをキックしない。
		if (initFlag) {
			initFlag = false;
			return;
		}

		// 5. Jobをキックする。
		for (JobFileCheck jobFileCheck : kickList) {
			m_log.info("kick " + jobFileCheck.getId());
			String calendarId = jobFileCheck.getCalendarId();
			CalendarInfo calendarInfo = jobFileCheck.getCalendarInfo();
			boolean run = true;
			if (calendarId != null && calendarInfo == null) {
				m_log.info("unknown error : id=" + jobFileCheck.getId() + "calendarId=" + calendarId);
			}
			if (calendarInfo != null) {
				run = CalendarWSUtil.isRun(calendarInfo);
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

	private boolean matchFile (JobFileCheck check, String filename) {
		Pattern pattern = null;
		String patternText = check.getFileName();
		// 大文字・小文字を区別しない場合
		pattern = Pattern.compile(patternText, Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
		// 大文字・小文字を区別する場合
		// pattern = Pattern.compile(patternText, Pattern.DOTALL);
		Matcher matcher = pattern.matcher(filename);

		return matcher.matches();
	}

	private JobFileCheck getCopy(JobFileCheck org) {
		JobFileCheck ret = new JobFileCheck();
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
		ret.setValid(org.isValid());
		return ret;
	}

	/**
	 * ファイルチェック結果をマネージャに送信する。
	 * 送信に失敗した場合、1分後にリトライする。
	 */
	private static String jobFileCheckResultRetry(JobFileCheck jobFileCheck)
			throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, MonitorNotFound_Exception, JobMasterNotFound_Exception, FacilityNotFound_Exception, JobInfoNotFound_Exception {
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

		for (int i = 0; i < resultRetryCount; i++) {
			try {
				return AgentEndPointWrapper.jobFileCheckResult(jobFileCheck);
			}  catch (JobSessionDuplicate_Exception e) {
				m_log.warn("sendNotifyRetry " + e.getMessage());
			} catch (Throwable t) {
				try {
					Thread.sleep(resultRetrySleep);
				} catch (InterruptedException e1) {
					m_log.warn("sendNotifyRetry " + e1.getMessage());
				}
			}
		}
		m_log.warn("give up jobFileCheckResultRetry. Maybe, manager is down");
		return null;
	}
}