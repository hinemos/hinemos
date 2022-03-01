/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.custom.factory;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.bean.PriorityConstant;
import com.clustercontrol.collect.bean.Sample;
import com.clustercontrol.collect.util.CollectDataUtil;
import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.commons.util.NotifyGroupIdGenerator;
import com.clustercontrol.custom.bean.CommandResultDTO;
import com.clustercontrol.custom.bean.CustomConstant.CommandExecType;
import com.clustercontrol.custom.factory.MonitorCustomCache.MonitorCustomValue;
import com.clustercontrol.custom.factory.MonitorCustomCache.MonitorCustomValuePK;
import com.clustercontrol.fault.CustomInvalid;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.jobmanagement.bean.MonitorJobEndNode;
import com.clustercontrol.jobmanagement.bean.RunStatusConstant;
import com.clustercontrol.jobmanagement.util.MonitorJobWorker;
import com.clustercontrol.monitor.bean.ConvertValueConstant;
import com.clustercontrol.monitor.run.bean.MonitorNumericType;
import com.clustercontrol.monitor.run.bean.MonitorRunResultInfo;
import com.clustercontrol.monitor.run.bean.MonitorTypeConstant;
import com.clustercontrol.monitor.run.model.MonitorInfo;
import com.clustercontrol.monitor.run.model.MonitorJudgementInfo;
import com.clustercontrol.monitor.run.util.CollectMonitorManagerUtil;
import com.clustercontrol.monitor.run.util.MonitorJudgementInfoCache;
import com.clustercontrol.monitor.run.util.CollectMonitorManagerUtil.CollectMonitorDataInfo;
import com.clustercontrol.monitor.session.MonitorSettingControllerBean;
import com.clustercontrol.notify.bean.OutputBasicInfo;
import com.clustercontrol.performance.bean.CollectedDataErrorTypeConstant;
import com.clustercontrol.repository.session.RepositoryControllerBean;
import com.clustercontrol.util.HinemosTime;
import com.clustercontrol.util.MessageConstant;

/**
 * コマンド監視(数値)の監視処理を実装したクラス<br/>
 * 
 * @version 6.0.0
 * @since 4.0.0
 */
public class RunCustom extends RunCustomBase{

	private static Log m_log = LogFactory.getLog( RunCustom.class );

	private TreeMap<Integer, MonitorJudgementInfo> thresholds = new TreeMap<>();

	/**
	 * コンストラクタ<br/>
	 * @param result 各エージェントから送信されるコマンドの実行結果情報
	 * @throws HinemosUnknown 予期せぬ内部エラーが発生した場合
	 * @throws MonitorNotFound コマンドの実行結果に対応する監視設定が存在しない場合
	 */
	public RunCustom(CommandResultDTO result) throws HinemosUnknown, MonitorNotFound {
		this.result = result;

		thresholds = MonitorJudgementInfoCache.getMonitorJudgementMap(
			result.getMonitorId(), MonitorTypeConstant.TYPE_NUMERIC, MonitorNumericType.TYPE_BASIC.getType());
	}

	/**
	 * 閾値判定を行い、監視結果を通知する。<br/>
	 * @throws HinemosUnknown 予期せぬ内部エラーが発生した場合
	 * @throws MonitorNotFound 該当する監視設定が存在しない場合
	 * @throws CustomInvalid 監視設定に不整合が存在する場合
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
		String executeDate  = "";
		String exitDate = "";
		String collectDate = "";
		String msg = "";
		String msgOrig = "";
		double value = -1;

		boolean isMonitorJob = result.getRunInstructionInfo() != null;

		// MAIN
		try {
			monitor = new MonitorSettingControllerBean().getMonitor(result.getMonitorId());

			facilityPath = new RepositoryControllerBean().getFacilityPath(result.getFacilityId(), null);
			executeDate = dateFormat.format(result.getExecuteDate());
			exitDate = dateFormat.format(result.getExitDate());
			collectDate = dateFormat.format(result.getCollectDate());

			if (result.getTimeout() || result.getStdout() == null || "".equals(result.getStdout()) || "\n".equals(result.getStdout()) || result.getResults() == null) {
				if (m_log.isDebugEnabled()) {
					m_log.debug("command monitoring : timeout or no stdout [" + result + "]");
				}

				// if command execution failed (timeout or no stdout)
				if (isMonitorJob || monitor.getMonitorFlg() || monitor.getPredictionFlg()) {
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
						if (monitor.getMonitorFlg()) {
							rtn.add(createOutputBasicInfo(PriorityConstant.TYPE_UNKNOWN, monitor, result.getFacilityId(), facilityPath, null, msg, msgOrig,HinemosModuleConstant.MONITOR_CUSTOM_N,
									null, null));
						}
						if (monitor.getPredictionFlg()) {
							// キーが特定できないため将来予測監視も「不明」とする
							rtn.add(createOutputBasicInfo(PriorityConstant.TYPE_UNKNOWN, monitor, result.getFacilityId(), facilityPath, 
									CollectMonitorManagerUtil.getPredictionDisplayName(""),
									msg, msgOrig,HinemosModuleConstant.MONITOR_CUSTOM_N,
									monitor.getPredictionApplication(),
									CollectMonitorManagerUtil.getPredictionNotifyGroupId(NotifyGroupIdGenerator.generate(monitor))));
						}
						if (monitor.getChangeFlg()) {
							// キーが特定できないため変化点監視も「不明」とする
							rtn.add(createOutputBasicInfo(PriorityConstant.TYPE_UNKNOWN, monitor, result.getFacilityId(), facilityPath, 
									CollectMonitorManagerUtil.getChangeDisplayName(""),
									msg, msgOrig,HinemosModuleConstant.MONITOR_CUSTOM_N,
									monitor.getChangeApplication(),
									CollectMonitorManagerUtil.getChangeNotifyGroupId(NotifyGroupIdGenerator.generate(monitor))));
						}
					} else {
						// 監視ジョブ
						this.monitorJobEndNodeList.add(new MonitorJobEndNode(result.getRunInstructionInfo(),
								HinemosModuleConstant.MONITOR_CUSTOM_N,
								makeJobOrgMessage(monitor, msgOrig),
								"",
								RunStatusConstant.END,
								MonitorJobWorker.getReturnValue(result.getRunInstructionInfo(), PriorityConstant.TYPE_UNKNOWN)));
					}
				}
			} else {
				List<Sample> sampleList= new ArrayList<Sample>();
				// if command stdout was returned
				for (String key : result.getResults().keySet()) {
					if (m_log.isDebugEnabled()) {
						m_log.debug("command monitoring : judgement values [" + result + ", key = " + key + "]");
					}
					
					// 前回値に関するパラメータを宣言
					MonitorCustomValue valueEntity = null;
					Double prevValue = 0d;
					Long prevDate = 0l;
					int m_validSecond = Integer.MIN_VALUE;
					int tolerance = Integer.MIN_VALUE;
					
					// 差分値をとる場合は、値の精査を行う。
					if (monitor.getCustomCheckInfo().getConvertFlg() == ConvertValueConstant.TYPE_DELTA) {
						// cacheより前回情報を取得
						if (!isMonitorJob) {
							// 監視ジョブ以外
							valueEntity = MonitorCustomCache.getMonitorCustomValue(monitor.getMonitorId(), result.getFacilityId(), key);
							prevValue = (Double)valueEntity.getValue();
							// 前回の取得値
							if (valueEntity.getGetDate() != null) {
								prevDate = valueEntity.getGetDate();
							}
						} else {
							// 監視ジョブ
							valueEntity = (MonitorCustomValue)MonitorJobWorker.getPrevMonitorValue(result.getRunInstructionInfo());
							if (valueEntity != null) {
								// 前回値が存在する場合
								prevValue = (Double)valueEntity.getValue();
								prevDate = valueEntity.getGetDate();
							} else {
								valueEntity = new MonitorCustomValue(new MonitorCustomValuePK(monitor.getMonitorId(), result.getFacilityId(), key));
							}
						}
						// 前回値情報を今回の取得値に更新
						valueEntity.setValue(result.getResults().get(key));
						valueEntity.setGetDate(result.getCollectDate());
						if (!isMonitorJob) {
							// 監視ジョブ以外
							// 監視処理時に対象の監視項目IDが有効である場合にキャッシュを更新
							MonitorCustomCache.update(monitor.getMonitorId(), result.getFacilityId(), key, valueEntity);
							
							m_validSecond = HinemosPropertyCommon.monitor_custom_valid_second.getIntegerValue();
							// 前回値取得時刻が取得許容時間よりも前だった場合、値取得失敗
							tolerance = (monitor.getRunInterval() + m_validSecond) * 1000;
							
							if(prevDate > result.getCollectDate() - tolerance){
								if (prevValue != null) {
									value = (Double)result.getResults().get(key) - prevValue;
								}
							}
							else{
								if (prevDate == 0l) {
									// 差分処理の初回取得処理のため、処理終了
									return rtn;
								}
							}
						}
					}
					
					if (isMonitorJob || monitor.getMonitorFlg()) {	// monitor each value
						// 差分判定
						if (monitor.getCustomCheckInfo().getConvertFlg() == ConvertValueConstant.TYPE_NO) {
							priority = judgePriority((Double)result.getResults().get(key));	

							msg = "VALUE : " + key + "=" + result.getResults().get(key);
							msgOrig = "VALUE : " + key + "=" + result.getResults().get(key) + "\n\n"
									+ "COMMAND : " + result.getCommand() + "\n"
									+ "COLLECTION DATE : " + collectDate + "\n"
									+ "executed at " + executeDate + "\n"
									+ "exited (or timeout) at " + exitDate + "\n"
									+ "EXIT CODE : " + (result.getExitCode() != null ? result.getExitCode() : "timeout") + "\n\n"
									+ "[STDOUT]\n" + result.getStdout() + "\n\n"
									+ "[STDERR]\n" + result.getStderr() + "\n";

							if (!isMonitorJob) {
								// 監視ジョブ以外
								rtn.add(createOutputBasicInfo(priority, monitor, result.getFacilityId(), facilityPath, key, msg, msgOrig,HinemosModuleConstant.MONITOR_CUSTOM_N,
										null, null));
							} else {
								// 監視ジョブ
								this.monitorJobEndNodeList.add(new MonitorJobEndNode(
										result.getRunInstructionInfo(), 
										HinemosModuleConstant.MONITOR_CUSTOM_N,
										makeJobOrgMessage(monitor, msgOrig),
										"",
										RunStatusConstant.END,
										MonitorJobWorker.getReturnValue(result.getRunInstructionInfo(), priority)));
							}

						} else if (monitor.getCustomCheckInfo().getConvertFlg() == ConvertValueConstant.TYPE_DELTA) {
							// 取得した値と前回情報の差分をとり、閾値判定を行う。
							if (!isMonitorJob) {
								// 前回値取得時刻が取得許容時間よりも前だった場合、値取得失敗
								if(prevDate > result.getCollectDate() - tolerance){
									if (prevValue == null) {
										m_log.debug("collect() : prevValue is null");
										rtn.add(createOutputBasicInfo(PriorityConstant.TYPE_UNKNOWN, monitor, result.getFacilityId(), facilityPath, null, msg, msgOrig, HinemosModuleConstant.MONITOR_CUSTOM_N,
												null, null));
										return rtn;
									}
								}
								else{
									if (prevDate != 0l) {
										DateFormat df = DateFormat.getDateTimeInstance();
										df.setTimeZone(HinemosTime.getTimeZone());
										String[] args = {df.format(new Date(prevDate))};
										msg = MessageConstant.MESSAGE_TOO_OLD_TO_CALCULATE.getMessage(args);
										rtn.add(createOutputBasicInfo(PriorityConstant.TYPE_UNKNOWN, monitor, result.getFacilityId(), facilityPath, null, msg, msgOrig, HinemosModuleConstant.MONITOR_CUSTOM_N,
												null, null));
										return rtn;
									}
								}
								priority = judgePriority(value);
	
								msg = "DIFF VALUE : " + key + "=" + value;
								msgOrig = "DIFF VALUE : " + key + "=" + value + "\n"
										+ "CURRENT VALUE : " + key + "=" + result.getResults().get(key) + "\n"
										+ "PREVIOUS VALUE : " + key + "=" + prevValue + "\n\n"
										+ "COMMAND : " + result.getCommand() + "\n"
										+ "COLLECTION DATE : " + collectDate + "\n"
										+ "executed at " + executeDate + "\n"
										+ "exited (or timeout) at " + exitDate + "\n"
										+ "EXIT CODE : " + (result.getExitCode() != null ? result.getExitCode() : "timeout") + "\n\n"
										+ "[STDOUT]\n" + result.getStdout() + "\n\n"
										+ "[STDERR]\n" + result.getStderr() + "\n";
								rtn.add(createOutputBasicInfo(priority, monitor, result.getFacilityId(), facilityPath, key, msg, msgOrig, HinemosModuleConstant.MONITOR_CUSTOM_N,
										null, null));
							} else {
								if (prevDate != 0l) {
									// 前回値が存在する場合
									value = (Double)result.getResults().get(key) - prevValue;
									priority = judgePriority(value);
									
									msg = "DIFF VALUE : " + key + "=" + value;
									msgOrig = "DIFF VALUE : " + key + "=" + value + "\n"
											+ "CURRENT VALUE : " + key + "=" + result.getResults().get(key) + "\n"
											+ "PREVIOUS VALUE : " + key + "=" + prevValue + "\n\n"
											+ "COMMAND : " + result.getCommand() + "\n"
											+ "COLLECTION DATE : " + collectDate + "\n"
											+ "executed at " + executeDate + "\n"
											+ "exited (or timeout) at " + exitDate + "\n"
											+ "EXIT CODE : " + (result.getExitCode() != null ? result.getExitCode() : "timeout") + "\n\n"
											+ "[STDOUT]\n" + result.getStdout() + "\n\n"
											+ "[STDERR]\n" + result.getStderr() + "\n";
									// 監視ジョブ
									this.monitorJobEndNodeList.add(new MonitorJobEndNode(
											result.getRunInstructionInfo(), 
											HinemosModuleConstant.MONITOR_CUSTOM_N,
											makeJobOrgMessage(monitor, msgOrig),
											"",
											RunStatusConstant.END,
											MonitorJobWorker.getReturnValue(result.getRunInstructionInfo(), priority)));
								} else {
									// 前回値が存在しない場合
									MonitorJobWorker.addPrevMonitorValue(result.getRunInstructionInfo(), valueEntity);
								}
							}
						}
					}

					if (!isMonitorJob 
							&& (monitor.getCollectorFlg() 
								|| monitor.getPredictionFlg()
								|| monitor.getChangeFlg())) {
						boolean overlapCheck = false;
						// keyの重複チェック
						for (Sample lSample : sampleList){
							// カスタム監視ではcollectedSamplesの1要素に対してperfDataは1つのため、以下で対応
							if (lSample.getMonitorId().equals(monitor.getMonitorId())
									&& lSample.getDateTime().getTime() == result.getCollectDate()
									&& lSample.getPerfDataList().get(0).getFacilityId().equals(result.getFacilityId())
									&& lSample.getPerfDataList().get(0).getDisplayName().equals(key)
									&& lSample.getPerfDataList().get(0).getItemName().equals(monitor.getItemName())) {
								overlapCheck = true;
								break;
							}
						}
						if (!overlapCheck) {
							Double latestValue = null;
							if (monitor.getCustomCheckInfo().getConvertFlg() == ConvertValueConstant.TYPE_NO) {
								latestValue = (Double)result.getResults().get(key);
							} else if (monitor.getCustomCheckInfo().getConvertFlg() == ConvertValueConstant.TYPE_DELTA) {
								latestValue = value;
							}

							// 将来予測監視、変化量監視の処理を行う
							CollectMonitorDataInfo collectMonitorDataInfo  
								= CollectMonitorManagerUtil.calculateChangePredict(null, monitor, result.getFacilityId(),
								key, monitor.getItemName(), result.getCollectDate(), latestValue);

							// 将来予測もしくは変更点監視が有効な場合、通知を行う
							Double average = null;
							Double standardDeviation = null;
							if (collectMonitorDataInfo != null) {
								if (collectMonitorDataInfo.getChangeMonitorRunResultInfo() != null) {
									// 変化量監視の通知
									MonitorRunResultInfo collectResult = collectMonitorDataInfo.getChangeMonitorRunResultInfo();
									rtn.add(createOutputBasicInfo(collectResult.getPriority(), 
											monitor, 
											collectResult.getFacilityId(), 
											facilityPath, 
											collectResult.getDisplayName(), 
											collectResult.getMessage(),
											collectResult.getMessageOrg(), 
											HinemosModuleConstant.MONITOR_CUSTOM_N,
											collectResult.getApplication(),
											collectResult.getNotifyGroupId()));
								}
								if (collectMonitorDataInfo.getPredictionMonitorRunResultInfo() != null) {
									// 将来予測監視の通知
									MonitorRunResultInfo collectResult = collectMonitorDataInfo.getPredictionMonitorRunResultInfo();
									rtn.add(createOutputBasicInfo(collectResult.getPriority(), 
											monitor, 
											collectResult.getFacilityId(), 
											facilityPath, 
											collectResult.getDisplayName(), 
											collectResult.getMessage(),
											collectResult.getMessageOrg(), 
											HinemosModuleConstant.MONITOR_CUSTOM_N,
											collectResult.getApplication(),
											collectResult.getNotifyGroupId()));
								}
								average = collectMonitorDataInfo.getAverage();
								standardDeviation = collectMonitorDataInfo.getStandardDeviation();
							}

							if (monitor.getCollectorFlg()) {	// collector each value
								Date sampleDateTime = null;
								if (result.getCollectDate() != null) {
									sampleDateTime = new Date(result.getCollectDate());
								}
								Sample sample = new Sample(sampleDateTime, monitor.getMonitorId());
								// 差分判定
								if (monitor.getCustomCheckInfo().getConvertFlg() == ConvertValueConstant.TYPE_NO) {
									sample.set(result.getFacilityId(), monitor.getItemName(), (Double)result.getResults().get(key), 
										average, standardDeviation, CollectedDataErrorTypeConstant.NOT_ERROR, key);
								} else if (monitor.getCustomCheckInfo().getConvertFlg() == ConvertValueConstant.TYPE_DELTA) {
									sample.set(result.getFacilityId(), monitor.getItemName(), value, 
										average, standardDeviation, CollectedDataErrorTypeConstant.NOT_ERROR, key);
								}
								sampleList.add(sample);
							}
						}
					}
				}
				if(!sampleList.isEmpty()){
					CollectDataUtil.put(sampleList);
				}
				if (isMonitorJob || monitor.getMonitorFlg() || monitor.getPredictionFlg()) {	// notify invalid lines of stdout
					for (Integer lineNum : result.getInvalidLines().keySet()) {
						if (m_log.isDebugEnabled()) {
							m_log.debug("command monitoring : notify invalid result [" + result + ", lineNum = " + lineNum + "]");
						}
						msg = "FAILURE : invalid line found (not 2 column or duplicate) - (line " + lineNum + ") " + result.getInvalidLines().get(lineNum);
						msgOrig = "FAILURE : invalid line found (not 2 column or duplicate) - (line " + lineNum + ") " + result.getInvalidLines().get(lineNum) + "\n\n"
								+ "COMMAND : " + result.getCommand() + "\n"
								+ "COLLECTION DATE : " + collectDate + "\n"
								+ "executed at " + executeDate + "\n"
								+ "exited (or timeout) at " + exitDate + "\n"
								+ "EXIT CODE : " + (result.getExitCode() != null ? result.getExitCode() : "timeout") + "\n\n"
								+ "[STDOUT]\n" + result.getStdout() + "\n\n"
								+ "[STDERR]\n" + result.getStderr() + "\n";

						if (!isMonitorJob) {
							// 監視ジョブ以外
							if (monitor.getMonitorFlg()) {
								rtn.add(createOutputBasicInfo(PriorityConstant.TYPE_UNKNOWN, monitor, result.getFacilityId(), 
										facilityPath, lineNum.toString(), msg, msgOrig,HinemosModuleConstant.MONITOR_CUSTOM_N, null, null));
							}
							if (monitor.getPredictionFlg()) {
								// キーが特定できないため、将来予測監視も「不明」とする。
								rtn.add(createOutputBasicInfo(PriorityConstant.TYPE_UNKNOWN, monitor, result.getFacilityId(), 
										facilityPath, CollectMonitorManagerUtil.getPredictionDisplayName(lineNum.toString()), 
										msg, msgOrig,HinemosModuleConstant.MONITOR_CUSTOM_N, 
										monitor.getPredictionApplication(),
										CollectMonitorManagerUtil.getPredictionNotifyGroupId(NotifyGroupIdGenerator.generate(monitor))));
							}
							if (monitor.getChangeFlg()) {
								// キーが特定できないため、変化点監視も「不明」とする。
								rtn.add(createOutputBasicInfo(PriorityConstant.TYPE_UNKNOWN, monitor, result.getFacilityId(), 
										facilityPath, CollectMonitorManagerUtil.getChangeDisplayName(lineNum.toString()), 
										msg, msgOrig,HinemosModuleConstant.MONITOR_CUSTOM_N, 
										monitor.getChangeApplication(),
										CollectMonitorManagerUtil.getChangeNotifyGroupId(NotifyGroupIdGenerator.generate(monitor))));
							}
						} else {
							// 監視ジョブ
							this.monitorJobEndNodeList.add(new MonitorJobEndNode(
									result.getRunInstructionInfo(),
									HinemosModuleConstant.MONITOR_CUSTOM_N,
									makeJobOrgMessage(monitor, msgOrig),
									"",
									RunStatusConstant.END,
									MonitorJobWorker.getReturnValue(result.getRunInstructionInfo(), PriorityConstant.TYPE_UNKNOWN)));
						}
					}
				}
			}
			// 通知情報を返す
			return rtn;
		} catch (MonitorNotFound e) {
			m_log.warn("unexpected internal failure occurred. [" + result + "]");
			throw e;
		} catch (CustomInvalid e) {
			m_log.warn("unexpected internal failure occurred. [" + result + "]");
			throw e;
		} catch (HinemosUnknown e) {
			m_log.warn("unexpected internal failure occurred. [" + result + "]");
			throw e;
		} catch (Exception e) {
			m_log.warn("unexpected internal failure occurred. [" + result + "]", e);
			throw new HinemosUnknown("unexpected internal failure occurred. [" + result + "]", e);
		}
	}

	/**
	 * 監視値を閾値判定して重要度を返す。<br/>
	 * @param value 監視値(Double.NaNも許容する)
	 * @return 重要度(PriorityConstant.INFOなど)
	 * @throws CustomInvalid 監視設定に不整合が存在する場合
	 */
	private int judgePriority(Double value) throws CustomInvalid {
		// Local Variables
		int priority = PriorityConstant.TYPE_UNKNOWN;

		// MAIN
		if (Double.isNaN(value)) {
			// if user defined not a number
			priority = PriorityConstant.TYPE_UNKNOWN;
		} else {
			// if numeric value is defined
			if (thresholds.containsKey(PriorityConstant.TYPE_INFO) && thresholds.containsKey(PriorityConstant.TYPE_WARNING)) {
				if (value >= thresholds.get(PriorityConstant.TYPE_INFO).getThresholdLowerLimit()
						&& value < thresholds.get(PriorityConstant.TYPE_INFO).getThresholdUpperLimit()) {
					return PriorityConstant.TYPE_INFO;
				} else if (value >= thresholds.get(PriorityConstant.TYPE_WARNING).getThresholdLowerLimit()
						&& value < thresholds.get(PriorityConstant.TYPE_WARNING).getThresholdUpperLimit()) {
					return PriorityConstant.TYPE_WARNING;
				} else {
					priority = PriorityConstant.TYPE_CRITICAL;
				}
			} else {
				// if threshold is not defined
				CustomInvalid e = new CustomInvalid("configuration of command monitor is not valid. [" + result + "]");
				m_log.info("judgePriority() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
		}

		return priority;
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
				monitorInfo.getCustomCheckInfo().getTimeout().toString(),
				""};
		if(monitorInfo.getCustomCheckInfo().getConvertFlg() == ConvertValueConstant.TYPE_NO){
			// 何もしない
			args[4] = MessageConstant.CONVERT_NO.getMessage();
		} else if (monitorInfo.getCustomCheckInfo().getConvertFlg() == ConvertValueConstant.TYPE_DELTA) {
			// 差分をとる
			args[4] = MessageConstant.DELTA.getMessage();
		}
		
		return MessageConstant.MESSAGE_JOB_MONITOR_ORGMSG_CUSTOM_N.getMessage(args)
				+ "\n" + orgMsg;
	}

}
