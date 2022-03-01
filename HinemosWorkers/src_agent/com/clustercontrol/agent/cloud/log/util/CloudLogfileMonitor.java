/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.agent.cloud.log.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.log4j.Logger;

import com.clustercontrol.agent.log.MonitorInfoWrapper;
import com.clustercontrol.agent.util.MonitorStringUtil;
import com.clustercontrol.agent.util.filemonitor.AbstractFileMonitor;
import com.clustercontrol.agent.util.filemonitor.AbstractFileMonitorManager;
import com.clustercontrol.agent.util.filemonitor.AbstractReadingStatus;
import com.clustercontrol.agent.util.filemonitor.FileMonitorConfig;
import com.clustercontrol.bean.PriorityConstant;
import com.clustercontrol.util.HinemosTime;
import com.clustercontrol.util.MessageConstant;

public class CloudLogfileMonitor extends AbstractFileMonitor<MonitorInfoWrapper> {
	List<CloudLogRawObject> rawObject;
	Logger log = Logger.getLogger(this.getClass());
	String logStreamName = "";
	boolean hasCarryover = false;
	boolean isInitialMatch = true;
	boolean resumeFlg = true;
	CloudLogRawObject prevLine = null;
	
	public CloudLogfileMonitor(AbstractFileMonitorManager<MonitorInfoWrapper> fileMonitorManager,
			MonitorInfoWrapper monitorInfo, AbstractReadingStatus<MonitorInfoWrapper> status,
			FileMonitorConfig fileMonitorConfig) {
		super(fileMonitorManager, monitorInfo, status, fileMonitorConfig);
	}

	/**
	 * 日時取得用にクラウドから取得したオブジェクトをセットします。
	 * 
	 * @param rawObject
	 */
	public void setRawObject(List<CloudLogRawObject> rawObject) {
		this.rawObject = rawObject;
		hasCarryover = !status.getCarryover().isEmpty();
	}
	
	public void setResumeFlg(boolean resumeFlg){
		this.resumeFlg = resumeFlg;
	}
	
	/**
	 * 監視設定IDを取得
	 * 監視ジョブの場合は監視ジョブに紐づくIDが返却される
	 * @return
	 */
	public String getMonitorId() {
		return m_wrapper.getId();
	}
	
	/**
	 * ファイル監視で取得した文字列とクラウドサービスから取得した情報を照らし合わせて、
	 * ログのタイムスタンプを取得します。
	 * 
	 * 大まかな処理の流れは以下の通りです。(詳細は各ナンバリングのコメントを参照)
	 * 1．エージェント起動後の初回監視、かつキャリーオーバがあるか確認
	 * 　→キャリーオーバがある場合、その分のタイムスタンプが取得できないので、タイムスタンプが取得できないと判定
	 * 2．今回取得した文字列を改行で区切り、一番長い文字列を取得
	 * 3．区切り条件により1行が複数の文字列に区切られた場合を考慮し、前回合致した行と今回取得した文字列が合致するか確認
	 * 4．2で取得した文字列と今回クラウドサービスから取得した文字列を比較し合致する要素があるか確認
	 * 5．3，4のどちらでもタイムスタンプを取得できなかった場合は、現在時刻を出力日時として扱う
	 */
	@Override
	protected void patternMatchAndSendManager(String line) {
		
		// 区切り文字で区切った文字列とクラウドサービスから取得した
		// 文字列を比較して取得日時を判別する
		Date orgDate = null;
		String endPattern = m_wrapper.monitorInfo.getLogfileCheckInfo().getPatternTail();
		String startPattern = m_wrapper.monitorInfo.getLogfileCheckInfo().getPatternHead();
		
		// 1．エージェント起動後の初回監視、かつキャリーオーバがあるか確認
		// エージェント起動直後の初回監視でキャリーオーバが存在する場合、
		// 前回エージェント実行時のログが含まれるため、日時情報の取得ができない。
		// そのため、以下の条件をすべて満たす場合、日時情報の取得はせず、現在時刻を使用する
		// ・区切り文字に到達していないキャリーオーバがあること（区切り条件が改行以外）
		// ・エージェント起動後に初めて区切り文字が出現したタイミングであること
		if (isInitialMatch && hasCarryover && resumeFlg) {
			log.warn(
					"patternMatchAndSendManager(): Initial run with carryover. Current time is used as generation date for line :"
							+ line);
			// 通知
			sendMessageByDateNotFound(line);
			
			// ここに至るケースでもログストリーム名は見つけられるはずなので取得
			for (CloudLogRawObject r : rawObject) {
				// nullだった時点でAzureなのでそれ以上の確認は不要
				if (r.getStreamName() == null) {
					break;
				} else if (getFilePath().contains(DigestUtils.md5Hex(r.getStreamName()))) {
					logStreamName = r.getStreamName();
					break;
				}
			}
			
			// 現在時刻にセット
			orgDate = HinemosTime.getDateInstance();

		} else {
			// 2．今回取得した文字列を改行で区切り、一番長い文字列を取得
			String[] lines = line.split("\n");
			String longestLine = "";

			int tmpSize = 0;
			// 区切った文字列の中から一番長い文字列を取得
			for (int i = 0; i < lines.length; i++) {
				if (lines[i].length() > tmpSize) {
					longestLine = lines[i];
					tmpSize = lines[i].length();
				}
			}
			log.debug("patternMatchAndSendManager(): longestLine=" + longestLine);
			
			// 3．区切り条件により1行が複数の文字列に区切られた場合を考慮し、
			// 前回合致した行と今回取得した文字列が合致するか確認
			//
			// 以下のようなパターンを考慮
			// -------------------------------------------------
			// 区切り条件：先頭パターン
			// 先頭パターン：start
			// 今回取得文字列：start log1 start log2 start log3
			// -------------------------------------------------
			boolean withPrevLine = false;
			if (prevLine != null && prevLine.getMes().contains(longestLine)) {
				orgDate = prevLine.getDate();
				logStreamName = prevLine.getStreamName();
				withPrevLine = true;
				log.debug("patternMatchAndSendManager(): Line [" + longestLine + "] matched with prevLine "
						+ prevLine.getMes());
				// 合致した文字列があった場合、そこまでは不要になるので削除
				int index = prevLine.getMes().indexOf(longestLine);
				String repStr = prevLine.getMes().substring(index + longestLine.length());
				prevLine.setMes(repStr);
				log.debug("patternMatchAndSendManager(): after Replacement :" + prevLine.getMes());
			}

			if (!withPrevLine) {
				// 4．2で取得した文字列と今回クラウドサービスから取得した文字列を比較し合致する要素があるか確認
				orgDate = getOriginalDate(longestLine);
			}
			
			// nullの場合は現在時刻が受信日時になる
			if (orgDate == null) {
				log.warn("patternMatchAndSendManager(): date not found. Use current time. Line: "
						+ Arrays.toString(lines));
				sendMessageByDateNotFound(line);
				
				// 何らかの理由で日時情報が取得できなかった場合、
				// 持ち越された行は不要なので削除しておく
				// また、今回取得した行が確認済みだと次回の区切り文字が出現した際の
				// 日時情報の取得に影響があるので、未確認に戻しておく
				clearRawObjects();
				prevLine = null;
				
				// 現在時刻にセット
				orgDate = HinemosTime.getDateInstance();

			} else if (!withPrevLine) {
				log.debug("patternMatchAndSendManager(): orgDate=" + orgDate);
				// 先頭、終端パターンの場合のみ
				if (!endPattern.isEmpty()) {
					boolean containTail = false;
					Pattern pattern = Pattern.compile(endPattern);
					if (lines.length == 0) {
						Matcher matcher = pattern.matcher(line);
						containTail = matcher.find();
					} else {
						Matcher matcher = pattern.matcher(lines[lines.length - 1]);
						containTail = matcher.find();
					}
					// 最大読み取り文字数によって、区切り文字まで読み取られていない場合は、
					// 区切り文字が出現する行までrawObjectを確認済みにしておく
					if (!containTail) {
						findPattern(endPattern, true);
					}
				} else if (!startPattern.isEmpty()) {
					findPattern(startPattern, false);
				}
			}
		}

		MonitorStringUtil.patternMatch(m_wrapper.formatLine(line, fileMonitorConfig.getFilMessageLength()),
				m_wrapper.getMonitorInfo(), m_wrapper.getRunInstructionInfo(), orgDate, logStreamName);

		isInitialMatch = false;
		resumeFlg = false;
	}

	private Date getOriginalDate(String line) {
		// ファイル読み込みが発生しているのにも関わらず、
		// rawObjectがnullになることは想定していないが念のため
		if (rawObject == null) {
			log.warn("getOriginalDate(): rawObject is null. Line: " + line);
			return null;
		}

		log.debug("getOriginalDate(): Line=" + line);
		
		// 既に確認済みの要素の削除用
		ArrayList<CloudLogRawObject>removeList=new ArrayList<CloudLogRawObject>();
		Date orgDate = null;

		for (CloudLogRawObject r : rawObject) {
			log.debug("getOriginalDate(): Raw Line: " + r.getMes() + "Detected: " + r.hasDetected());
			// awsの場合別ログストリームの情報は無視する
			if (r.getStreamName() != null && !getFilePath().contains(DigestUtils.md5Hex(r.getStreamName()))) {
				continue;
			}
			// 既に検出済みの行は無視する
			if (r.hasDetected()) {
				continue;
			}
			// オブジェクトを確認済みにセット
			r.setDetected(true);
			removeList.add(r);

			if (r.getMes().contains(line)) {
				orgDate = r.getDate();
				logStreamName = r.getStreamName();
				// 今回合致した行が次の文字列でも合致する可能性があるため、時間監視用に保存
				prevLine = new CloudLogRawObject(r.getDate(), r.getMes(), r.getStreamName());
				int index = prevLine.getMes().indexOf(line);
				String repStr = prevLine.getMes().substring(index + line.length());
				prevLine.setMes(repStr);
				log.debug("getOriginalDate(): after Replacement :" + prevLine.getMes());
				break;
			}
		}
		// 日時が見つかった場合、確認済みの要素は不要なので削除
		if(orgDate != null){
			rawObject.removeAll(removeList);
		}

		return orgDate;
	}

	/**
	 * パターンに一致するまでrawObjectを確認済みにセットする
	 * @param pattern
	 * @param mode
	 */
	private void findPattern(String patternStr, boolean mode) {
		// rawObjectがnullになることは想定していないが念のため
		if (rawObject == null) {
			log.warn("findPattern(): rawObject is null.");
			return;
		}
		
		Pattern pattern = Pattern.compile(patternStr);
		log.debug("findPattern(): Find pattern=" + patternStr);

		for (CloudLogRawObject r : rawObject) {
			log.debug("findPattern(): Raw Line: " + r.getMes() + "Detected: " + r.hasDetected());
			// awsの場合別ログストリームの情報は無視する
			if (r.getStreamName() != null && !getFilePath().contains(DigestUtils.md5Hex(r.getStreamName()))) {
				continue;
			}
			// 既に検出済みの行は無視する
			if (r.hasDetected()) {
				continue;
			}

			Matcher matcher = pattern.matcher(r.getMes());
			// 終端パターンの場合
			if (mode) {
				// 終端パターンに合致するまでオブジェクトを確認済みにセットする
				r.setDetected(true);
				// 終端パターンまでの行に次回監視文字列が合致する可能性があるので、保存
				if (prevLine != null) {
					prevLine.setMes(prevLine.getMes() + r.getMes());
				}
				if (matcher.find()) {
					log.debug("findPattern(): match pattern=" + patternStr);
					break;
				}
			} else {
				// 先頭パターンの場合は、パターンに合致しなかったらオブジェクトを確認済みにセットする
				if (matcher.find()) {
					log.debug("findPattern(): match pattern=" + patternStr);
					break;
				} else {
					// オブジェクトを確認済みにセット
					r.setDetected(true);
					// 先頭パターンまでの行に次回監視文字列が合致する可能性があるので、保存
					if (prevLine != null) {
						prevLine.setMes(prevLine.getMes() + r.getMes());
					}
				}
			}
		}
	}
	
	/**
	 * 日時情報が取得できなかった場合に持ち越された行を削除する
	 */
	private void clearRawObjects() {
		// rawObjectがnullになることは想定していないが念のため
		if (rawObject == null) {
			log.warn("clearRawObjects(): rawObject is null.");
			return;
		}
		
		ArrayList<CloudLogRawObject> tmpList = new ArrayList<CloudLogRawObject>();
		for (CloudLogRawObject r : rawObject) {
			// awsの場合別ログストリームの情報は削除しない
			if (r.getStreamName() != null && !getFilePath().contains(DigestUtils.md5Hex(r.getStreamName()))) {
				tmpList.add(r);
				continue;
			}
			// 持ち越しされた行は無視
			if (r.isCarryOver()) {
				continue;
			}
			
			// 持ち越しされていない行は未確認に戻す
			r.setDetected(false);
			tmpList.add(r);
		}
		
		rawObject = new ArrayList<>(tmpList);
	}
	
	@Override
	protected void sendMessageByFileOpenFileNotFoundException(FileNotFoundException e) {
		String[] args = { getFilePath() };
		sendMessage(PriorityConstant.TYPE_WARNING, MessageConstant.AGENT.getMessage(),
				MessageConstant.CLOUDLOG_MSG_TMP_FILE_OPEN_FAILED.getMessage(),
				MessageConstant.CLOUDLOG_TMP_FILE.getMessage(args));
	}

	@Override
	protected void sendMessageByFileOpenSecurityException(SecurityException e) {
		String[] args = { getFilePath() };
		sendMessage(PriorityConstant.TYPE_WARNING, MessageConstant.AGENT.getMessage(),
				MessageConstant.CLOUDLOG_MSG_TMP_FILE_OPEN_FAILED.getMessage(),
				MessageConstant.CLOUDLOG_TMP_FILE.getMessage(args) + "\n" + e.getMessage());
	}

	@Override
	protected void sendMessageByFileOpenIOException(IOException e) {
		String[] args = { getFilePath() };
		sendMessage(PriorityConstant.TYPE_WARNING, MessageConstant.AGENT.getMessage(),
				MessageConstant.CLOUDLOG_MSG_TMP_FILE_OPEN_FAILED.getMessage(),
				MessageConstant.CLOUDLOG_TMP_FILE.getMessage(args));
	}

	@Override
	protected void sendMessageByFileReadIOException(IOException e) {
		String[] args = { getFilePath() };
		sendMessage(PriorityConstant.TYPE_WARNING, MessageConstant.AGENT.getMessage(),
				MessageConstant.CLOUDLOG_MSG_TMP_FILE_OPEN_FAILED.getMessage(),
				MessageConstant.CLOUDLOG_TMP_FILE.getMessage(args) + "\n" + e.getMessage());
	}

	@Override
	protected void sendMessageByFileSizeOver(long fileSize) {
		// message.log.agent.1=ログファイル「{0}」
		// message.log.agent.3=ファイルサイズが上限を超えました
		// message.log.agent.5=ファイルサイズ「{0} byte」
		String[] args1 = { getFilePath() };
		String[] args2 = { String.valueOf(fileSize) };
		sendMessage(PriorityConstant.TYPE_WARNING, MessageConstant.AGENT.getMessage(),
				MessageConstant.MESSAGE_CLOUD_LOG_TMP_FILE_SIZE_EXCEEDED_UPPER_BOUND.getMessage(),
				MessageConstant.CLOUDLOG_TMP_FILE.getMessage(args1) + ", "
						+ MessageConstant.MESSAGE_LOG_FILE_SIZE_BYTE.getMessage(args2));
	}
	
	protected void sendMessageByDateNotFound(String line) {
		sendMessage(PriorityConstant.TYPE_WARNING, MessageConstant.AGENT.getMessage(),
				MessageConstant.MESSAGE_CLOUD_LOG_MONITOR_DATE_NOT_FOUND.getMessage(), line);
	}
}
