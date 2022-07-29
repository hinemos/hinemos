/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.agent.cloud.log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openapitools.client.model.AgtMessageInfoRequest;
import org.openapitools.client.model.AgtMonitorInfoResponse;
import org.openapitools.client.model.AgtMonitorPluginStringInfoResponse;
import org.openapitools.client.model.AgtOutputBasicInfoRequest;
import org.openapitools.client.model.AgtRunInstructionInfoRequest;

import com.clustercontrol.agent.Agent;
import com.clustercontrol.agent.SendQueue;
import com.clustercontrol.agent.SendQueue.MessageSendableObject;
import com.clustercontrol.agent.cloud.log.util.CloudLogfileMonitorManager;
import com.clustercontrol.agent.util.RestCalendarUtil;
import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.bean.PriorityConstant;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.util.HinemosTime;
import com.clustercontrol.util.MessageConstant;
import com.clustercontrol.xcloud.bean.CloudConstant;

public class CloudLogMonitorUtil {
	protected static final String filepath = "/var/xcloud/";
	private static Log log = LogFactory.getLog(CloudLogMonitor.class);
	private static SendQueue sendQueue;

	public static void setSendQueue(SendQueue sendQueue) {
		CloudLogMonitorUtil.sendQueue = sendQueue;
	}

	/**
	 * 一時ファイルのストアパスを返却します。
	 * 
	 * @param monitorId
	 * @return
	 */
	public static String getFileStorePath(String monitorId) {
		String home = Agent.getAgentHome();
		String fileName = filepath + monitorId;
		return new File(new File(home), fileName).getAbsolutePath();
	}

	/**
	 * 一時ファイルのストアパスを返却します。
	 * 
	 * @param monitorId
	 * @return
	 */
	public static String getFileStorePathRoot() {
		String home = Agent.getAgentHome();
		String fileName = filepath;
		return new File(new File(home), fileName).getAbsolutePath();
	}

	/**
	 * クラウドログ監視の固有設定を探し出して、反映します。
	 * 
	 * @param settingConf
	 * @param list
	 */
	public static CloudLogMonitorConfig buildCloudLogSetting(List<AgtMonitorPluginStringInfoResponse> list) {

		CloudLogMonitorConfig settingConf = new CloudLogMonitorConfig();
		// プラットフォームは先に見つけとく
		settingConf.setPlatform(getCloudLogSetting(list, CloudConstant.cloudLog_platform));
		// その他設定項目の反映
		settingConf.setResourceGroup(getCloudLogSetting(list, CloudConstant.cloudLog_ResourceGroup));
		if (settingConf.getPlatform().equals(CloudConstant.platform_AWS)) {
			settingConf.setLogGroup(getCloudLogSetting(list, CloudConstant.cloudLog_LogGroup));
		} else {
			settingConf.setWorkspaceName(getCloudLogSetting(list, CloudConstant.cloudLog_LogGroup));
		}
		// AWSの場合、ログストリームは空も許容する
		if (settingConf.getPlatform().equals(CloudConstant.platform_AWS)) {
			String logStream = getCloudLogSetting(list, CloudConstant.cloudLog_LogStream);
			if (logStream != null && !logStream.isEmpty()) {
				// ログストリームには空白や「:*」以外の記号が含まれる可能性もあるので、分割以上の処理はしない
				settingConf.setLogSreams(new ArrayList<String>(Arrays.asList(logStream.split(":"))));
			}
		} else {
			settingConf.setTable(getCloudLogSetting(list, CloudConstant.cloudLog_LogStream));
		}
		settingConf.setPrefix(Boolean.parseBoolean(getCloudLogSetting(list, CloudConstant.cloudLog_isPrefix)));
		settingConf.setCol(getCloudLogSetting(list, CloudConstant.cloudLog_Col));
		settingConf.setAccess(getCloudLogSetting(list, CloudConstant.cloudLog_accessKey));
		settingConf.setSecret(getCloudLogSetting(list, CloudConstant.cloudLog_secretKey));
		settingConf.setLocation(getCloudLogSetting(list, CloudConstant.cloudLog_Location));
		settingConf.setReturnCode(getCloudLogSetting(list, CloudConstant.cloudLog_ReturnCode));
		try {
			String lastFireTime = getCloudLogSetting(list, CloudConstant.cloudLog_LastFireTime);
			// lastFireTimeが存在する場合のみパース
			// 初回実行時や監視ジョブの場合は存在しない
			if (lastFireTime != null && !lastFireTime.isEmpty()) {
				settingConf.setLastFireTime(
						(Long.parseLong(getCloudLogSetting(list, CloudConstant.cloudLog_LastFireTime))));
			}
		} catch (Exception e) {
			// パースできない文字が入ることは想定しない
			// ここに来たら初回実行扱いになる
			log.error("setCloudLogSetting: ", e);
		}
		// AWSの場合ログ取得遅延（秒）を取得
		if (settingConf.getPlatform().equals(CloudConstant.platform_AWS)) {
			try {
				settingConf.setOffset((Integer.parseInt(getCloudLogSetting(list, CloudConstant.cloudLog_Offset))));
			} catch (Exception e) {
				// パースできない文字が入ることは想定しない
				// 何らかの理由で到達してしまったら、0にする
				log.error("setCloudLogSetting: Failed parsing offset. Use value 0.", e);
				settingConf.setOffset(0);
			}
		}

		return settingConf;
	}

	/**
	 * Agent.propertiesで指定されたオフセットを含めたHinemos時刻を返却します。
	 * 
	 * @return
	 */
	public static long getTimeWithOffset() {
		return HinemosTime.currentTimeMillis() + CloudLogMonitorProperty.getInstance().getTimeOffset();
	}

	/**
	 * 異常回復時にマネージャでINTERNALを出力します。
	 * 
	 * @param config
	 * @param shouldNotify
	 */
	public static void notifyRecovery(CloudLogMonitorConfig config, boolean shouldNotify) {
		;
		if (shouldNotify) {
			sendMessage(config, PriorityConstant.TYPE_INFO, MessageConstant.AGENT.getMessage(),
					MessageConstant.MESSAGE_CLOUD_LOG_MONITOR_RECOVERED_FAILURE.getMessage(), "");
		}

	}

	/**
	 * 異常発生時にマネージャでINTERNALを出力します
	 * 
	 * @param config
	 * @param priority
	 * @param app
	 * @param msg
	 * @param msgOrg
	 */
	public static void sendMessage(CloudLogMonitorConfig config, int priority, String app, String msg, String msgOrg) {
		log.debug("Send message called");
		sendMessage(config.getMonInfo(), priority, app, msg, msgOrg, config.getMonitorId(), config.getRunInfoReq());
	}

	/**
	 * Hinemosマネージャへ情報を通知します。<BR>
	 */
	public static void sendMessage(AgtMonitorInfoResponse monitor, int priority, String app, String msg, String msgOrg,
			String monitorId, AgtRunInstructionInfoRequest runInstructionInfo) {

		// 監視が無効 or カレンダ非稼働なら通知はしない
		if (!shouldSendMessage(monitor)) {
			log.info("sendMessage: Suppressed. " + monitor.getMonitorId() + "," + priority + "," + app + "," + msg + ","
					+ msgOrg + "," + monitorId);
			return;
		}
		sendMessage(priority, app, msg, msgOrg, monitorId, runInstructionInfo);
	}

	/**
	 * Hinemosマネージャへ情報を通知します。<BR>
	 */
	public static void sendMessage(int priority, String app, String msg, String msgOrg, String monitorId,
			AgtRunInstructionInfoRequest runInstructionInfo) {

		// ログ出力情報
		MessageSendableObject sendme = new MessageSendableObject();
		sendme.body = new AgtOutputBasicInfoRequest();
		sendme.body.setPluginId(HinemosModuleConstant.MONITOR_CLOUD_LOG);
		sendme.body.setPriority(priority);
		sendme.body.setApplication(app);
		sendme.body.setMessage(msg);
		sendme.body.setMessageOrg(msgOrg);

		sendme.body.setGenerationDate(HinemosTime.getDateInstance().getTime());
		sendme.body.setMonitorId(monitorId);
		sendme.body.setFacilityId(""); // マネージャがセットする。
		sendme.body.setScopeText(""); // マネージャがセットする。
		sendme.body.setRunInstructionInfo(runInstructionInfo);

		sendQueue.put(sendme);
	}

	/**
	 * 指定されたIDの監視設定について、マネージャへの情報通知をすべきなら true、すべきでないなら false を返します。
	 * <p>
	 * 判定仕様について
	 * <ul>
	 * <li>監視ジョブの場合は、常に通知すべきと判定します。
	 * <li>現在時刻がカレンダ非稼働期間の場合は、通知すべきでないと判定します。
	 * <li>監視と収集のいずれかが有効な場合は、通知すべきと判定します。<br/>
	 * つまり、監視が無効で収集のみ有効な場合でも、通知すべきと判定します。
	 * 収集のみ有効な場合であっても、ログファイルを読み取り、必要ならマネージャへその情報を送信しており、
	 * その過程で生じたエラーなどは通知すべきだろうという根拠によるものです。
	 * <li>判定に必要な情報が存在しない場合(何らかの理由により未初期化になってしまっている場合など)は、 通知すべきと判定します。<br/>
	 * これはできる限り情報を握り潰すべきではないという根拠によるものです。
	 * </ul>
	 */
	public static boolean shouldSendMessage(AgtMonitorInfoResponse mon) {
		// 状況的にありえないケースもあるかもしれないが、念のためにしっかりと null チェックする。
		// 判定に必要な情報が null の場合は、情報を握り潰さないように true とする。
		if (mon == null) {
			log.debug("shouldSendMessage: True; monitorId is null.");
			return true;
		}

		String monitorId = mon.getMonitorId();

		// カレンダ非稼働なら、有効/無効は関係なく false
		if (mon.getCalendar() != null && !RestCalendarUtil.isRun(mon.getCalendar())) {
			log.debug("shouldSendMessage: False; Disabled by the calendar. monitorId=" + monitorId + ", calendarId="
					+ mon.getCalendarId());
			return false;
		}
		// 監視か収集のいずれかが有効なら true、そうでなければ false
		if (mon.getMonitorFlg().booleanValue() || mon.getCollectorFlg().booleanValue()) {
			log.debug("shouldSendMessage: True; Enabled. monitorId=" + monitorId + ", flags=" + mon.getMonitorFlg()
					+ ", " + mon.getCollectorFlg());
			return true;
		} else {
			log.debug("shouldSendMessage: False; Disabled. monitorId=" + monitorId);
			return false;
		}
	}

	/**
	 * 最終実行日時をマネージャに返却します。
	 * 
	 * @param config
	 * @param date
	 */
	public static void sendLastFireTime(CloudLogMonitorConfig config, long date) {
		AgtMessageInfoRequest logmsg = new AgtMessageInfoRequest();
		logmsg.setGenerationDate(date);
		CloudLogResultForwarder.getInstance().add(null, logmsg, config.getMonInfo(), null, config.getRunInfo());
	}

	// キーに一致する設定情報を返却
	public static String getCloudLogSetting(List<AgtMonitorPluginStringInfoResponse> list, String key) {
		for (AgtMonitorPluginStringInfoResponse sInfo : list) {
			if (sInfo.getKey().equals(key)) {
				return sInfo.getValue();
			}
		}
		return "";
	}

	/**
	 * 不要になった古い一時ファイルを削除します
	 * 
	 * @param file
	 * @throws IOException
	 */
	public static void deleteOldFiles(File file) throws IOException {
		deleteFiles(file, true);
		// ファイル監視によるファイルへの参照を更新
		CloudLogfileMonitorManager.getInstance().clearReadingStatus();
	}
	
	/**
	 * 一時ファイルのみを削除します(ディレクトリは削除しない)
	 * @param file
	 * @throws IOException
	 */
	public static void deleteOnlyTmpFiles(File file) throws IOException {
		deleteFiles(file, false);
	}

	private static void deleteFiles(File file, boolean onlyOld) throws IOException {
		if (!file.exists()) {
			return;
		}

		if (file.isDirectory()) {
			File[] entries = file.listFiles();
			if (entries != null) {
				for (File entry : entries) {
					if (onlyOld) {
						if (entry.lastModified() != 0 && entry.lastModified() < HinemosTime.currentTimeMillis()
								- CloudLogMonitorProperty.getInstance().getValidDuration()) {
							log.info("deleteFiles(): delete old file=" + entry.getPath());
							if (!entry.delete()) {
								throw new IOException("Failed to delete " + entry);
							}
						}
					} else {
						log.info("deleteFiles(): delete file=" + entry.getPath());
						if (!entry.delete()) {
							throw new IOException("Failed to delete " + entry);
						}
					}
				}
			}
		}
	}

	/**
	 * 一時ファイルをゼロバイトにします。
	 * 
	 * @param file
	 * @throws IOException
	 */
	public static void truncateTmpFilesRecursive(File file) {
		if (!file.exists()) {
			return;
		}
		if (file.isDirectory()) {
			File[] entries = file.listFiles();
			if (entries != null) {
				for (File entry : entries) {
					truncateTmpFilesRecursive(entry);
				}
			}
		} else {
			log.info("truncateTmpFilesRecursive(): truncate file=" + file.getPath());
			FileWriter fw = null;
			try {
				fw = new FileWriter(file, false);
				fw.write("");
				fw.flush();
			} catch (IOException e) {
				log.warn("truncateTmpFilesRecursive(): failed to truncate file", e);
			} finally {
				if (fw != null) {
					try {
						fw.close();
					} catch (IOException e) {
						log.warn("truncateTmpFilesRecursive(): failed to close file writer", e);
					}
				}
			}
		}
	}

	/**
	 * 一時ファイルディレクトリを全削除します。
	 * 
	 * @param file
	 * @throws IOException
	 */
	public static void deleteDirectoryRecursive(File file) throws IOException {
		if (!file.exists()) {
			return;
		}
		if (file.isDirectory()) {
			File[] entries = file.listFiles();
			if (entries != null) {
				for (File entry : entries) {
					deleteDirectoryRecursive(entry);
				}
			}
		}
		log.info("deleteDirectoryRecursive(): delete file=" + file.getPath());
		if (!file.delete()) {
			throw new IOException("Failed to delete " + file);
		}
	}
	
	/**
	 * エージェント停止時に監視設定が削除、無効化された場合などに
	 * 残ったままになってしまっている一時ファイルを削除するメソッド
	 * エージェント起動時に呼ばれる
	 */
	public static void deleteGarbageFiles() {
		// 一時ファイルのルートディレクトリを取得
		File rootDir = new File(getFileStorePathRoot());
		// 一度もクラウドログ監視が実行されていない場合、ここにくる
		if (!rootDir.exists()) {
			log.info("deleteGarbageFiles(): directory does not exists=" + rootDir.getPath());
			return;
		}

		// 一時ファイルのディレクトリ一覧を取得
		File[] entries = rootDir.listFiles();
		if (entries == null || entries.length == 0) {
			log.debug("deleteGarbageFiles(): No directories to delete.");
			return;
		}

		for (File file : entries) {
			try {
				deleteDirectoryRecursive(file);
			} catch (IOException e) {
				log.warn("deleteGarbageFiles(): Filed to delete directory=" + file.getPath());
			}
		}

	}
	
	/**
	 * 一時ファイルのディレクトリを作成します。
	 * @throws HinemosUnknown 
	 */
	public static void createTmpFileDir(String filePath) throws HinemosUnknown {
		File directory = new File(CloudLogMonitorUtil.getFileStorePathRoot());
		// 一時ファイル用のルートディレクトリ作成
		if (!directory.exists()) {
			if (!directory.mkdir()) {
				log.error("createRootDire(): failed creating root dir");
				throw new HinemosUnknown("failed creating root dir");
			}
		}
		directory = new File(filePath);
		// 一時ファイル用のディレクトリ作成
		if (!directory.exists()) {
			if (!directory.mkdir()) {
				log.error("createRootDire(): failed creating tmp file dir");
				throw new HinemosUnknown("failed creating tmp file dir");
			}
		}

	}
}
