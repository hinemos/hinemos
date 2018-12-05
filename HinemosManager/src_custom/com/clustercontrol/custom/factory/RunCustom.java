/*


This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.custom.factory;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.bean.PriorityConstant;
import com.clustercontrol.collect.bean.Sample;
import com.clustercontrol.collect.util.CollectDataUtil;
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
import com.clustercontrol.maintenance.util.HinemosPropertyUtil;
import com.clustercontrol.monitor.bean.ConvertValueConstant;
import com.clustercontrol.monitor.run.model.MonitorInfo;
import com.clustercontrol.monitor.run.model.MonitorJudgementInfo;
import com.clustercontrol.monitor.run.model.MonitorNumericValueInfo;
import com.clustercontrol.monitor.run.util.QueryUtil;
import com.clustercontrol.monitor.session.MonitorSettingControllerBean;
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

	private HashMap<Integer, MonitorJudgementInfo> thresholds = new HashMap<>();

	/**
	 * コンストラクタ<br/>
	 * @param result 各エージェントから送信されるコマンドの実行結果情報
	 * @throws HinemosUnknown 予期せぬ内部エラーが発生した場合
	 * @throws MonitorNotFound コマンドの実行結果に対応する監視設定が存在しない場合
	 */
	public RunCustom(CommandResultDTO result) throws HinemosUnknown, MonitorNotFound {
		this.result = result;

		// MAIN
		try {
			if (m_log.isDebugEnabled()) {
				m_log.debug("received command result : " + result);
			}
			Collection<MonitorNumericValueInfo> ct 
			= QueryUtil.getMonitorNumericValueInfoFindByMonitorId(result.getMonitorId(), ObjectPrivilegeMode.NONE);
			thresholds = new HashMap<>();
			Iterator<MonitorNumericValueInfo> itr = ct.iterator();
			MonitorNumericValueInfo entity = null;
			while(itr.hasNext()) {
				entity = itr.next();
				MonitorJudgementInfo monitorJudgementInfo = new MonitorJudgementInfo();
				monitorJudgementInfo.setMonitorId(entity.getId().getMonitorId());
				monitorJudgementInfo.setPriority(entity.getId().getPriority());
				monitorJudgementInfo.setMessage(entity.getMessage());
				monitorJudgementInfo.setThresholdLowerLimit(entity.getThresholdLowerLimit());
				monitorJudgementInfo.setThresholdUpperLimit(entity.getThresholdUpperLimit());
				thresholds.put(entity.getId().getPriority(), monitorJudgementInfo);
			}
		} catch (Exception e) {
			HinemosUnknown e1 = new HinemosUnknown("unexpected internal failure occurred. [" + result + "]", e);
			m_log.warn("RunCustom() unexpected internal failure occurred. [" + result + "] : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			throw e1;
		}
	}

	/**
	 * 閾値判定を行い、監視結果を通知する。<br/>
	 * @throws HinemosUnknown 予期せぬ内部エラーが発生した場合
	 * @throws MonitorNotFound 該当する監視設定が存在しない場合
	 * @throws CustomInvalid 監視設定に不整合が存在する場合
	 */
	@Override
	public void monitor() throws HinemosUnknown, MonitorNotFound, CustomInvalid {
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

			if (result.getTimeout() || result.getStdout() == null || "".equals(result.getStdout()) || result.getResults() == null) {
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
						notify(PriorityConstant.TYPE_UNKNOWN, monitor, result.getFacilityId(), facilityPath, null, msg, msgOrig,HinemosModuleConstant.MONITOR_CUSTOM_N);
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
							valueEntity = MonitorCustomCache.getMonitorCustomValue(monitor.getMonitorId(), monitor.getFacilityId(), key);
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
								valueEntity = new MonitorCustomValue(new MonitorCustomValuePK(monitor.getMonitorId(), monitor.getFacilityId(), key));
							}
						}
						// 前回値情報を今回の取得値に更新
						valueEntity.setValue(result.getResults().get(key));
						valueEntity.setGetDate(result.getCollectDate());
						if (!isMonitorJob) {
							// 監視ジョブ以外
							// 監視処理時に対象の監視項目IDが有効である場合にキャッシュを更新
							MonitorCustomCache.update(monitor.getMonitorId(), monitor.getFacilityId(), key, valueEntity);
							
							m_validSecond = HinemosPropertyUtil.getHinemosPropertyNum("monitor.custom.valid.second", Long.valueOf(15)).intValue();
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
									return;
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
								notify(priority, monitor, result.getFacilityId(), facilityPath, key, msg, msgOrig,HinemosModuleConstant.MONITOR_CUSTOM_N);
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
										notify(PriorityConstant.TYPE_UNKNOWN, monitor, result.getFacilityId(), facilityPath, null, msg, msgOrig, HinemosModuleConstant.MONITOR_CUSTOM_N);
										return;
									}
								}
								else{
									if (prevDate != 0l) {
										DateFormat df = DateFormat.getDateTimeInstance();
										df.setTimeZone(HinemosTime.getTimeZone());
										String[] args = {df.format(new Date(prevDate))};
										msg = MessageConstant.MESSAGE_TOO_OLD_TO_CALCULATE.getMessage(args);
										notify(PriorityConstant.TYPE_UNKNOWN, monitor, result.getFacilityId(), facilityPath, null, msg, msgOrig, HinemosModuleConstant.MONITOR_CUSTOM_N);
										return;
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
								notify(priority, monitor, result.getFacilityId(), facilityPath, key, msg, msgOrig, HinemosModuleConstant.MONITOR_CUSTOM_N);
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

					if (!isMonitorJob && monitor.getCollectorFlg()) {	// collector each value
						Sample sample = new Sample(result.getCollectDate()==null?null:new Date(result.getCollectDate()), monitor.getMonitorId());
						boolean overlapCheck = false;
						// 差分判定
						if (monitor.getCustomCheckInfo().getConvertFlg() == ConvertValueConstant.TYPE_NO) {
							sample.set(result.getFacilityId(), monitor.getItemName(), (Double)result.getResults().get(key), CollectedDataErrorTypeConstant.NOT_ERROR, key);
						} else if (monitor.getCustomCheckInfo().getConvertFlg() == ConvertValueConstant.TYPE_DELTA) {
							sample.set(result.getFacilityId(), monitor.getItemName(), value, CollectedDataErrorTypeConstant.NOT_ERROR, key);
						}
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
							sampleList.add(sample);
						}
					}
				}
				if(!sampleList.isEmpty()){
					CollectDataUtil.put(sampleList);
				}
				if (isMonitorJob || monitor.getMonitorFlg()) {	// notify invalid lines of stdout
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
							notify(PriorityConstant.TYPE_UNKNOWN, monitor, result.getFacilityId(), facilityPath, lineNum.toString(), msg, msgOrig,HinemosModuleConstant.MONITOR_CUSTOM_N);
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
