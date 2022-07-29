/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.utility.controller;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.monitor.run.model.MonitorInfo;
import com.clustercontrol.monitor.run.model.MonitorNumericValueInfo;
import com.clustercontrol.monitor.run.model.MonitorNumericValueInfoPK;
import com.clustercontrol.monitor.session.MonitorSettingControllerBean;
import com.clustercontrol.rest.endpoint.monitorsetting.MonitorsettingRestEndpoints;
import com.clustercontrol.rest.endpoint.monitorsetting.dto.*;
import com.clustercontrol.rest.endpoint.monitorsetting.dto.enumtype.PriorityChangeFailureTypeEnum;
import com.clustercontrol.rest.endpoint.monitorsetting.dto.enumtype.PriorityChangeJudgmentTypeEnum;
import com.clustercontrol.rest.endpoint.utility.dto.ImportMonitorCommonRecordRequest;
import com.clustercontrol.rest.endpoint.utility.dto.MonitorInfoRequestForUtility;
import com.clustercontrol.rest.endpoint.utility.dto.RecordRegistrationResponse;
import com.clustercontrol.rest.util.RestBeanUtil;
import com.clustercontrol.rest.util.RestCommonValitater;
import com.clustercontrol.rest.endpoint.utility.dto.enumtype.ImportResultEnum;

public class ImportMonitorCommonController extends AbstractImportController<ImportMonitorCommonRecordRequest, RecordRegistrationResponse> {

	private static Log m_log = LogFactory.getLog(ImportMonitorCommonController.class);
	
	private static final String JmxUrlFormatNameDefault =  "Default";
	public ImportMonitorCommonController(boolean isRollbackIfAbnormal, List<ImportMonitorCommonRecordRequest> importList) {
		super(isRollbackIfAbnormal, importList);
	}
	
	@Override
	protected RecordRegistrationResponse proccssRecord( ImportMonitorCommonRecordRequest importRec) throws Exception {
		if (importRec.getMonitorModule() == null) {
			// クライアント側のチェックで異常があった場合
			// 監視項目種別が設定されていない空のデータが作られるケースがあるため（クラウド系監視のみ）ここで対処
			String msg = null;
			if (importRec.getImportData() != null && importRec.getImportData().getDescription() != null
					&& !importRec.getImportData().getDescription().isEmpty()) {
				// Descriptionにエラーメッセージが格納されている
				msg = importRec.getImportData().getDescription();
			} else {
				// 想定外
				msg = "MonitorModule is null.";
				m_log.error("proccssRecord() " + msg);
			}
			throw new InvalidSetting(msg);
		}
		RecordRegistrationResponse dtoRecRes= new RecordRegistrationResponse();
		dtoRecRes.setImportKeyValue(importRec.getImportKeyValue());
		
		//共通DTOの内容を個別チェック向けに 各監視専用のRequestDTOへ変換
		AbstractMonitorRequest requestDto = convertDto(importRec);
		RestCommonValitater.checkRequestDto(requestDto);
		requestDto.correlationCheck();
		MonitorInfo infoReq = new MonitorInfo();
		
		convertBeanForMonitorInfo(requestDto, infoReq);
		
		MonitorsettingRestEndpoints.updateInfo(requestDto, infoReq);
		if(importRec.getIsNewRecord()){
			//add
			new MonitorSettingControllerBean().addMonitor(infoReq);
		}else{
			//Modify
			new MonitorSettingControllerBean().modifyMonitor(infoReq);
		}
		
		dtoRecRes.setResult(ImportResultEnum.NORMAL);
		return dtoRecRes;
	}
	
	@Override
	protected RecordRegistrationResponse getRecordResponseInstance(){
		return  new RecordRegistrationResponse();
	}
	
	private AbstractMonitorRequest convertDto(ImportMonitorCommonRecordRequest importRec ) throws HinemosUnknown{
		
		AbstractMonitorRequest ret = null;
		switch (importRec.getMonitorModule()) {
			case HinemosModuleConstant.MONITOR_AGENT : ret = new  AddAgentMonitorRequest(); break;
			case HinemosModuleConstant.MONITOR_BINARYFILE_BIN : ret = new  AddBinaryfileMonitorRequest(); break;
			case HinemosModuleConstant.MONITOR_CLOUD_SERVICE_BILLING : ret = new  AddCloudserviceBillingMonitorRequest(); break;
			case HinemosModuleConstant.MONITOR_CLOUD_SERVICE_BILLING_DETAIL : ret = new  AddCloudserviceBillingDetailMonitorRequest(); break;
			case HinemosModuleConstant.MONITOR_CLOUD_SERVICE_CONDITION : ret = new  AddCloudserviceMonitorRequest(); break;
			case HinemosModuleConstant.MONITOR_CLOUD_LOG : ret = new  AddCloudLogMonitorRequest(); break;
			case HinemosModuleConstant.MONITOR_CORRELATION : ret = new  AddCorrelationMonitorRequest(); break;
			case HinemosModuleConstant.MONITOR_CUSTOM_N : ret = new  AddCustomNumericMonitorRequest(); break;
			case HinemosModuleConstant.MONITOR_CUSTOM_S : ret = new  AddCustomStringMonitorRequest(); break;
			case HinemosModuleConstant.MONITOR_CUSTOMTRAP_N : ret = new  AddCustomtrapNumericMonitorRequest(); break;
			case HinemosModuleConstant.MONITOR_CUSTOMTRAP_S : ret = new  AddCustomtrapStringMonitorRequest(); break;
			case HinemosModuleConstant.MONITOR_HTTP_N : ret = new  AddHttpNumericMonitorRequest(); break;
			case HinemosModuleConstant.MONITOR_HTTP_S : ret = new  AddHttpStringMonitorRequest(); break;
			case HinemosModuleConstant.MONITOR_HTTP_SCENARIO : ret = new  AddHttpScenarioMonitorRequest(); break;
			case HinemosModuleConstant.MONITOR_INTEGRATION : ret = new  AddIntegrationMonitorRequest(); break;
			case HinemosModuleConstant.MONITOR_JMX : ret = new  AddJmxMonitorRequest();break;
			case HinemosModuleConstant.MONITOR_LOGCOUNT : ret = new  AddLogcountMonitorRequest(); break;
			case HinemosModuleConstant.MONITOR_LOGFILE : ret = new  AddLogfileMonitorRequest(); break;
			case HinemosModuleConstant.MONITOR_PCAP_BIN : ret = new  AddPacketcaptureMonitorRequest(); break;
			case HinemosModuleConstant.MONITOR_PERFORMANCE : ret = new  AddPerformanceMonitorRequest(); break;
			case HinemosModuleConstant.MONITOR_PING : ret = new  AddPingMonitorRequest(); break;
			case HinemosModuleConstant.MONITOR_PORT : ret = new  AddServiceportMonitorRequest(); break;
			case HinemosModuleConstant.MONITOR_PROCESS : ret = new  AddProcessMonitorRequest(); break;
			case HinemosModuleConstant.MONITOR_SNMP_N : ret = new  AddSnmpNumericMonitorRequest(); break;
			case HinemosModuleConstant.MONITOR_SNMP_S : ret = new  AddSnmpStringMonitorRequest(); break;
			case HinemosModuleConstant.MONITOR_SNMPTRAP : ret = new  AddSnmptrapMonitorRequest(); break;
			case HinemosModuleConstant.MONITOR_SQL_N : ret = new  AddSqlNumericMonitorRequest(); break;
			case HinemosModuleConstant.MONITOR_SQL_S : ret = new  AddSqlStringMonitorRequest(); break;
			case HinemosModuleConstant.MONITOR_SYSTEMLOG : ret = new  AddSystemlogMonitorRequest(); break;
			case HinemosModuleConstant.MONITOR_WINEVENT : ret = new  AddWineventMonitorRequest(); break;
			case HinemosModuleConstant.MONITOR_WINSERVICE : ret = new  AddWinserviceMonitorRequest(); break;
			case HinemosModuleConstant.MONITOR_RPA_LOGFILE : ret = new  AddRpaLogfileMonitorRequest(); break;
			case HinemosModuleConstant.MONITOR_RPA_MGMT_TOOL_SERVICE : ret = new  AddRpaManagementToolMonitorRequest(); break;
		}
		
		// 下位互換(6.2.x版データ)対応のための補完処理 
		supportBackwardCompatibility(importRec.getImportData());

		RestBeanUtil.convertBeanSimple(importRec.getImportData(), ret);
		return ret; 
	}
	
	private void convertBeanForMonitorInfo(AbstractMonitorRequest requestDto,MonitorInfo infoReq) throws InvalidSetting, HinemosUnknown{
		
		RestBeanUtil.convertBean(requestDto, infoReq);
		//数値監視の場合MonitorNumericValueInfoを個別セット
		if(requestDto instanceof AbstractAddNumericMonitorRequest){
			List<MonitorNumericValueInfo> monitorNumericValueInfoList = new ArrayList<MonitorNumericValueInfo>();
			
			for(MonitorNumericValueInfoRequest tmp: ((AbstractAddNumericMonitorRequest)requestDto).getNumericValueInfo()){
				MonitorNumericValueInfo monitorNumericValueInfo = new MonitorNumericValueInfo();
				RestBeanUtil.convertBean(tmp,monitorNumericValueInfo);
				
				//PKクラスの個別セット
				MonitorNumericValueInfoPK pk = new MonitorNumericValueInfoPK();
				pk.setMonitorId(((AbstractAddNumericMonitorRequest) requestDto).getMonitorId());
				if(tmp.getMonitorNumericType() == null){
					pk.setMonitorNumericType("");
				} else {
					pk.setMonitorNumericType(tmp.getMonitorNumericType().getCode());
				}
				pk.setPriority(tmp.getPriority().getCode());
				monitorNumericValueInfo.setId(pk);
				monitorNumericValueInfoList.add(monitorNumericValueInfo);
			}
			infoReq.setNumericValueInfo(monitorNumericValueInfoList);
		} 
	}

	// 下位互換(6.2.x版データ)対応のための補完処理 
	//  下位版には存在しない必須項目を バージョンアップ時の初期値で補完
	//  変換先で不要な項目だった場合 DTOに対応項目がないためコンバートされず引き継がれない想定
	private void supportBackwardCompatibility(MonitorInfoRequestForUtility target){
		if (target == null) {
			return;
		}

		// 通知向け重要度変化オプション
		if (target.getPriorityChangeJudgmentType() == null) {
			if (m_log.isDebugEnabled()) {
				m_log.debug("supportBackwardCompatibility() : Complement priorityChangeJudgmentType . MonitorId="+ target.getMonitorId());
			}
			target.setPriorityChangeJudgmentType(PriorityChangeJudgmentTypeEnum.NOT_PRIORITY_CHANGE);
		}
		if (target.getPriorityChangeFailureType() == null) {
			if (m_log.isDebugEnabled()) {
				m_log.debug("supportBackwardCompatibility() : Complement priorityChangeFailureType . MonitorId="+ target.getMonitorId());
			}
			target.setPriorityChangeFailureType(PriorityChangeFailureTypeEnum.NOT_PRIORITY_CHANGE);
		}
		// JMX監視URLフォーマットパターン
		if (target.getJmxCheckInfo() != null && target.getJmxCheckInfo().getUrlFormatName() == null) {
			if (m_log.isDebugEnabled()) {
				m_log.debug("supportBackwardCompatibility() : Complement urlFormatName. MonitorId="+ target.getMonitorId());
			}
			target.getJmxCheckInfo().setUrlFormatName(JmxUrlFormatNameDefault);
		}
	}
	
}
