/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.custom.factory;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.bean.PriorityConstant;
import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.custom.bean.CommandResultDTO;
import com.clustercontrol.custom.bean.CustomConstant.CommandExecType;
import com.clustercontrol.fault.CustomInvalid;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.hub.bean.StringSample;
import com.clustercontrol.hub.util.CollectStringDataUtil;
import com.clustercontrol.jobmanagement.bean.MonitorJobEndNode;
import com.clustercontrol.jobmanagement.bean.RunStatusConstant;
import com.clustercontrol.jobmanagement.util.MonitorJobWorker;
import com.clustercontrol.monitor.run.model.MonitorInfo;
import com.clustercontrol.monitor.run.model.MonitorStringValueInfo;
import com.clustercontrol.monitor.session.MonitorSettingControllerBean;
import com.clustercontrol.notify.bean.OutputBasicInfo;
import com.clustercontrol.repository.session.RepositoryControllerBean;
import com.clustercontrol.util.HinemosTime;
import com.clustercontrol.util.MessageConstant;

/**
 * コマンド監視(String)の監視処理を実装したクラス<br/>
 *
 * @version 6.0.0
 * @since 6.0.0
 */
public class RunCustomString extends RunCustomBase {

	private static Log m_log = LogFactory.getLog(RunCustomString.class);
	
	private static volatile Integer maxStringLen;

	/**
	 * コンストラクタ<br/>
	 * 
	 * @param result
	 *            各エージェントから送信されるコマンドの実行結果情報
	 */
	public RunCustomString(CommandResultDTO result) {
		this.result = result;
	}

	/**
	 * 閾値判定を行い、監視結果を通知する。<br/>
	 * 
	 * @throws HinemosUnknown
	 *             予期せぬ内部エラーが発生した場合
	 * @throws MonitorNotFound
	 *             該当する監視設定が存在しない場合
	 * @throws CustomInvalid
	 *             監視設定に不整合が存在する場合
	 */
	@Override
	public List<OutputBasicInfo> monitor() throws HinemosUnknown, MonitorNotFound, CustomInvalid {

		List<OutputBasicInfo> rtn = new ArrayList<>();

		// Local Variables
 		MonitorInfo monitor = null;

		int priority = PriorityConstant.TYPE_UNKNOWN;
		String facilityPath = "";
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		dateFormat.setTimeZone(HinemosTime.getTimeZone());
		String msg = "";
		String msgOrig = "";
		
		boolean isMonitorJob = result.getRunInstructionInfo() != null;
		
		// MAIN
		try {
			monitor = new MonitorSettingControllerBean().getMonitor(result.getMonitorId());
			
			String executeDate = dateFormat.format(result.getExecuteDate());
			String exitDate = dateFormat.format(result.getExitDate());
			String collectDate = dateFormat.format(result.getCollectDate());
			
			facilityPath = new RepositoryControllerBean().getFacilityPath(result.getFacilityId(), null);
			if (result.getTimeout() || result.getStdout() == null || result.getStdout().isEmpty()
					|| result.getResults() == null) {
				if (m_log.isDebugEnabled()) {
					m_log.debug("command monitoring : timeout or no stdout [" + result + "]");
				}
				// if command execution failed (timeout or no stdout)
				if (isMonitorJob || monitor.getMonitorFlg()) {
					msg = "FAILURE : command execution failed (timeout, no stdout or not unexecutable command)...";
					
					msgOrig = "FAILURE : command execution failed (timeout, no stdout or unexecutable command)...\n\n"
							+ "COMMAND : " + result.getCommand() + "\n"
							+ "COLLECTION DATE : " + collectDate + "\n"
							+ "executed at " + executeDate + "\n"
							+ "exited (or timeout) at " + exitDate + "\n"
							+ "EXIT CODE : " + (result.getExitCode() != null ? result.getExitCode() : "timeout") + "\n\n"
							+ "[STDOUT]\n" + result.getStdout() + "\n"
							+ "[STDERR]\n" + result.getStderr() + "\n";

					
					
					// 監視ジョブ以外
					if (!isMonitorJob) {
						rtn.add(createOutputBasicInfo(PriorityConstant.TYPE_UNKNOWN, monitor, result.getFacilityId(), facilityPath, null, msg,
								msgOrig, HinemosModuleConstant.MONITOR_CUSTOM_S, null, null));
					} else {
						// 監視ジョブ
						this.monitorJobEndNodeList.add(new MonitorJobEndNode(
								result.getRunInstructionInfo(),
								HinemosModuleConstant.MONITOR_CUSTOM_S,
								makeJobOrgMessage(monitor, msgOrig),
								"",
								RunStatusConstant.END,
								MonitorJobWorker.getReturnValue(result.getRunInstructionInfo(), PriorityConstant.TYPE_UNKNOWN)));
					}
				}
			} else {
				// if command stdout was returned Monitor
				msg = result.getStdout();
				// Stdoutの末尾の改行を消す
				Pattern patternMsg = Pattern.compile("\r\n$|\n$");
				Matcher matcherMsg = patternMsg.matcher(msg);
				msg = matcherMsg.replaceAll("");

				
				if (m_log.isDebugEnabled()) {
					m_log.debug("command monitoring : values [" + msg + "]");
				}
				
				// 監視ジョブ以外の場合、収集
				if (!isMonitorJob && monitor.getCollectorFlg()) {	// collector each value
					List<StringSample> sampleList= new ArrayList<StringSample>();
					StringSample sample = new StringSample(new Date(HinemosTime.currentTimeMillis()),
							monitor.getMonitorId());
					// ログメッセージ
					sample.set(result.getFacilityId(), "custom", msg);
					
					sampleList.add(sample);
					if(!sampleList.isEmpty()){
						CollectStringDataUtil.store(sampleList);
					}
				}
				
				// 監視
				int orderNo = 0;
				if (isMonitorJob || monitor.getMonitorFlg()) { // monitor each value
					for (MonitorStringValueInfo stringValueInfo : monitor.getStringValueInfo()) {
						++orderNo;
						if (m_log.isDebugEnabled()) {
							m_log.info(String.format(
									"monitoring (monitorId = %s, orderNo = %d, patten = %s, enabled = %s, casesensitive = %s)",
									monitor.getMonitorId(), orderNo, stringValueInfo.getPattern(),
									stringValueInfo.getValidFlg(), stringValueInfo.getCaseSensitivityFlg()));
						}
						if (!stringValueInfo.getValidFlg()) {
							// 無効化されているルールはスキップする
							continue;
						}
						// パターンマッチを実施
						if (m_log.isDebugEnabled()) {
							m_log.debug(String.format("filtering customtrap (regex = %s, Msg = %s",
									stringValueInfo.getPattern(), msg));
						}
						try {
							Pattern pattern = null;
							if (stringValueInfo.getCaseSensitivityFlg()) {
								// 大文字・小文字を区別しない場合
								pattern = Pattern.compile(stringValueInfo.getPattern(),
										Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
							} else {
								// 大文字・小文字を区別する場合
								pattern = Pattern.compile(stringValueInfo.getPattern(), Pattern.DOTALL);
							}
							Matcher matcher = pattern.matcher(msg);
							if (matcher.matches()) {
								if (stringValueInfo.getProcessType()) {
									msgOrig = MessageConstant.LOGFILE_PATTERN.getMessage() + "=" + stringValueInfo.getPattern() + "\n" 
											+ MessageConstant.LOGFILE_LINE.getMessage() +"=" + msg + "\n\n"
											+ "COMMAND : " + result.getCommand() + "\n"
											+ "COLLECTION DATE : " + collectDate + "\n"
											+ "executed at " + executeDate + "\n"
											+ "exited (or timeout) at " + exitDate + "\n"
											+ "EXIT CODE : " + (result.getExitCode() != null ? result.getExitCode() : "timeout") + "\n\n"
											+ "[STDOUT]\n" + result.getStdout() + "\n\n"
											+ "[STDERR]\n" + result.getStderr() + "\n";
									
									msg = makeMsg(stringValueInfo, msg);
									
									priority=stringValueInfo.getPriority();
									if (!isMonitorJob) {
										// 監視ジョブ以外
										rtn.add(createOutputBasicInfo(priority, monitor, result.getFacilityId(), facilityPath,
												stringValueInfo.getPattern(), msg, msgOrig,
												HinemosModuleConstant.MONITOR_CUSTOM_S, null, null));
									} else {
										// 監視ジョブ
										this.monitorJobEndNodeList.add(new MonitorJobEndNode(
												result.getRunInstructionInfo(), 
												HinemosModuleConstant.MONITOR_CUSTOM_S,
												makeJobOrgMessage(monitor, msgOrig),
												"",
												RunStatusConstant.END,
												MonitorJobWorker.getReturnValue(result.getRunInstructionInfo(), priority)));
									}
								} else {
									m_log.debug(String.format("not ProcessType (regex = %s, Msg = %s",
											stringValueInfo.getPattern(),
											result.getStdout()));
								}
								break;// Syslog監視と同じで処理を抜ける
							}
						} catch (RuntimeException e) {
							m_log.warn("filtering failure. (regex = " + stringValueInfo.getPattern() + ") . "
									+ e.getMessage(), e);
						}
					}
				}
			}
			// 通知情報を返す
			return rtn;
		} catch (MonitorNotFound | CustomInvalid | HinemosUnknown e) {
			m_log.warn("unexpected internal failure occurred. [" + result + "]");
			throw e;
		} catch (Exception e) {
			m_log.warn("unexpected internal failure occurred. [" + result + "]", e);
			throw new HinemosUnknown("unexpected internal failure occurred. [" + result + "]", e);
		}
	}

	/**
	 * メッセージを作成します。
	 * 
	 * @param stringValueInfo　MonitorStringValueInfo
	 * @param msg
	 * @return
	 */
	private String makeMsg(MonitorStringValueInfo stringValueInfo, String msg) {
		String updateMsg = "";
		if (null != msg) {
			updateMsg = stringValueInfo.getMessage().replace("#[LOG_LINE]", msg);
			// DBよりオリジナルメッセージ出力文字数を取得
			int maxLen = HinemosPropertyCommon.monitor_log_line_max_length.getIntegerValue();
			if (maxStringLen == null || maxStringLen != maxLen) {
				synchronized(RunCustomString.class) {
					if (maxStringLen == null || maxStringLen != maxLen) {
						m_log.info("monitor.log.line.max.length = " + maxLen);
						maxStringLen = maxLen;
					}
				}
			}
			
			if (updateMsg.length() > maxLen) {
				updateMsg = updateMsg.substring(0, maxLen);
			}
		}
		return updateMsg;
	}

	private String makeJobOrgMessage(MonitorInfo monitorInfo, String orgMsg) {
		if (monitorInfo == null || monitorInfo.getCustomCheckInfo() == null) {
			return "";
		}
		String[] args = {
				monitorInfo.getCustomCheckInfo().getCommandExecType() == CommandExecType.SELECTED 
					? monitorInfo.getCustomCheckInfo().getSelectedFacilityId() : "",
				monitorInfo.getCustomCheckInfo().getSpecifyUser() 
					? monitorInfo.getCustomCheckInfo().getEffectiveUser() : "",
				monitorInfo.getCustomCheckInfo().getCommand(),
				monitorInfo.getCustomCheckInfo().getTimeout().toString()};
		
		return MessageConstant.MESSAGE_JOB_MONITOR_ORGMSG_CUSTOM_S.getMessage(args)
				+ "\n" + orgMsg;
	}
}
