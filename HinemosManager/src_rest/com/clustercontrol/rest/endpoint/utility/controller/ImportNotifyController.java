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

import org.apache.log4j.Logger;

import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.NotifyDuplicate;
import com.clustercontrol.fault.NotifyNotFound;
import com.clustercontrol.notify.bean.NotifyTypeConstant;
import com.clustercontrol.notify.model.NotifyInfo;
import com.clustercontrol.notify.session.NotifyControllerBean;
import com.clustercontrol.notify.util.NotifyUtil;
import com.clustercontrol.repository.session.RepositoryControllerBean;
import com.clustercontrol.rest.dto.RequestDto;
import com.clustercontrol.rest.endpoint.notify.dto.AddCloudNotifyRequest;
import com.clustercontrol.rest.endpoint.notify.dto.AddCommandNotifyRequest;
import com.clustercontrol.rest.endpoint.notify.dto.AddEventNotifyRequest;
import com.clustercontrol.rest.endpoint.notify.dto.AddInfraNotifyRequest;
import com.clustercontrol.rest.endpoint.notify.dto.AddJobNotifyRequest;
import com.clustercontrol.rest.endpoint.notify.dto.AddLogEscalateNotifyRequest;
import com.clustercontrol.rest.endpoint.notify.dto.AddMailNotifyRequest;
import com.clustercontrol.rest.endpoint.notify.dto.AddMessageNotifyRequest;
import com.clustercontrol.rest.endpoint.notify.dto.AddRestNotifyRequest;
import com.clustercontrol.rest.endpoint.notify.dto.AddStatusNotifyRequest;
import com.clustercontrol.rest.endpoint.notify.dto.CloudNotifyLinkInfoKeyValueObjectRequest;
import com.clustercontrol.rest.endpoint.notify.dto.ModifyCloudNotifyRequest;
import com.clustercontrol.rest.endpoint.notify.dto.ModifyCommandNotifyRequest;
import com.clustercontrol.rest.endpoint.notify.dto.ModifyEventNotifyRequest;
import com.clustercontrol.rest.endpoint.notify.dto.ModifyInfraNotifyRequest;
import com.clustercontrol.rest.endpoint.notify.dto.ModifyJobNotifyRequest;
import com.clustercontrol.rest.endpoint.notify.dto.ModifyLogEscalateNotifyRequest;
import com.clustercontrol.rest.endpoint.notify.dto.ModifyMailNotifyRequest;
import com.clustercontrol.rest.endpoint.notify.dto.ModifyMessageNotifyRequest;
import com.clustercontrol.rest.endpoint.notify.dto.ModifyRestNotifyRequest;
import com.clustercontrol.rest.endpoint.notify.dto.ModifyStatusNotifyRequest;
import com.clustercontrol.rest.endpoint.notify.dto.enumtype.CommandSettingTypeEnum;
import com.clustercontrol.rest.endpoint.notify.dto.enumtype.NotifyTypeEnum;
import com.clustercontrol.rest.endpoint.utility.dto.ImportNotifyRecordRequest;
import com.clustercontrol.rest.endpoint.utility.dto.NotifyRequestForUtility;
import com.clustercontrol.rest.endpoint.utility.dto.RecordRegistrationResponse;
import com.clustercontrol.rest.endpoint.utility.dto.enumtype.ImportResultEnum;
import com.clustercontrol.rest.util.RestBeanUtil;
import com.clustercontrol.rest.util.RestCommonValitater;

public class ImportNotifyController extends AbstractImportController<ImportNotifyRecordRequest, RecordRegistrationResponse> {
	
	public ImportNotifyController(boolean isRollbackIfAbnormal, List<ImportNotifyRecordRequest> importList) {
		super(isRollbackIfAbnormal, importList);
	}
	
	@Override
	public RecordRegistrationResponse proccssRecord( ImportNotifyRecordRequest importRec ) throws Exception {

		// 下位互換(6.2.x版データ)対応のための補完処理 
		supportBackwardCompatibility(importRec.getImportData());

		RecordRegistrationResponse dtoRecRes= new RecordRegistrationResponse();
		dtoRecRes.setImportKeyValue(importRec.getImportKeyValue());
		importRec.getImportData().correlationCheck();
		
		// ControllerBean呼び出し
		if(importRec.getIsNewRecord()){
			//新規登録
			callAddNotify(importRec.getImportData());
		}else{
			//変更
			callModifyNotfy(importRec.getImportData());
		}
		
		dtoRecRes.setResult(ImportResultEnum.NORMAL);
		return dtoRecRes;
	}
	
	@Override
	protected RecordRegistrationResponse getRecordResponseInstance(){
		return  new RecordRegistrationResponse();
	}
	
	private void callAddNotify(NotifyRequestForUtility importData) throws InvalidSetting, HinemosUnknown, NotifyDuplicate, InvalidRole{
		
		RequestDto requestDto = null;
		NotifyInfo infoReq = new NotifyInfo(importData.getNotifyId());
		
		// クラウド通知における文字列・クラスリストの変換処理の都合の変数
		String infoJsonData = null;
		String warnJsonData = null;
		String critJsonData = null;
		String unkJsonData = null;
		
		// NotifyTypeを参照し、各種通知のリクエストにdtoに変換
		switch (importData.getNotifyType().getCode()) {
		
		case NotifyTypeConstant.TYPE_COMMAND:
			// コマンド通知用
			AddCommandNotifyRequest addCommandRequest = new AddCommandNotifyRequest();
			RestBeanUtil.convertBeanSimple(importData, addCommandRequest);
			requestDto = addCommandRequest;
			break;
			
		case NotifyTypeConstant.TYPE_EVENT:
			// イベント通知用
			AddEventNotifyRequest addEventRequest = new AddEventNotifyRequest();
			RestBeanUtil.convertBeanSimple(importData, addEventRequest);
			requestDto = addEventRequest;
			break;
			
		case NotifyTypeConstant.TYPE_INFRA:
			// インフラ
			AddInfraNotifyRequest addInfraRequest = new AddInfraNotifyRequest();
			RestBeanUtil.convertBeanSimple(importData, addInfraRequest);
			requestDto = addInfraRequest;
			break;
			
		case NotifyTypeConstant.TYPE_JOB:
			// ジョブ
			AddJobNotifyRequest addJobNotifyRequest = new AddJobNotifyRequest();
			RestBeanUtil.convertBeanSimple(importData, addJobNotifyRequest);
			requestDto = addJobNotifyRequest;
			break;
			
		case NotifyTypeConstant.TYPE_LOG_ESCALATE:
			//　エスカレーション
			AddLogEscalateNotifyRequest addLogEscalateRequest = new AddLogEscalateNotifyRequest();
			RestBeanUtil.convertBeanSimple(importData, addLogEscalateRequest);
			requestDto = addLogEscalateRequest;
			break;
			
		case NotifyTypeConstant.TYPE_MAIL:
			// メール
			AddMailNotifyRequest addMailRequest = new AddMailNotifyRequest();
			RestBeanUtil.convertBeanSimple(importData, addMailRequest);
			requestDto = addMailRequest;
			break;
			
		case NotifyTypeConstant.TYPE_STATUS:
			// ステータス
			AddStatusNotifyRequest addStatusRequest = new AddStatusNotifyRequest();
			RestBeanUtil.convertBeanSimple(importData, addStatusRequest);
			requestDto = addStatusRequest;
			break;
			
		case NotifyTypeConstant.TYPE_REST:
			// REST
			AddRestNotifyRequest addRestRequest = new AddRestNotifyRequest();
			RestBeanUtil.convertBeanSimple(importData, addRestRequest);
			requestDto = addRestRequest;
			break;
		
		case NotifyTypeConstant.TYPE_MESSAGE:
			// メッセージ
			AddMessageNotifyRequest addMessageRequest = new AddMessageNotifyRequest();
			RestBeanUtil.convertBeanSimple(importData, addMessageRequest);
			requestDto = addMessageRequest;
			break;
		
		case NotifyTypeConstant.TYPE_CLOUD:
			// クラウド
			AddCloudNotifyRequest addCloudRequest = new AddCloudNotifyRequest();
			RestBeanUtil.convertBeanSimple(importData, addCloudRequest);
			// 個別変換
			// クラウド通知のディテール/データはDB上ではjsonで保存しているが
			// そのままではUtilityなどでの使い勝手が悪いので、コンポーネント間のやり取りでは
			// キーバリューオブジェクトのリストに変換している
			// ここではリスト→jsonの変換を実施
			if(addCloudRequest.getNotifyCloudInfo().getInfoKeyValueDataList() == null){
				addCloudRequest.getNotifyCloudInfo().setInfoKeyValueDataList(new ArrayList<CloudNotifyLinkInfoKeyValueObjectRequest>());
			}
			infoJsonData = NotifyUtil.getJsonStringForCloudNotify(addCloudRequest.getNotifyCloudInfo().getInfoKeyValueDataList());
			
			if(addCloudRequest.getNotifyCloudInfo().getWarnKeyValueDataList() == null){
				addCloudRequest.getNotifyCloudInfo().setWarnKeyValueDataList(new ArrayList<CloudNotifyLinkInfoKeyValueObjectRequest>());
			}
			warnJsonData = NotifyUtil.getJsonStringForCloudNotify(addCloudRequest.getNotifyCloudInfo().getWarnKeyValueDataList());
			
			if(addCloudRequest.getNotifyCloudInfo().getCritKeyValueDataList() == null){
				addCloudRequest.getNotifyCloudInfo().setCritKeyValueDataList(new ArrayList<CloudNotifyLinkInfoKeyValueObjectRequest>());
			}
			critJsonData = NotifyUtil.getJsonStringForCloudNotify(addCloudRequest.getNotifyCloudInfo().getCritKeyValueDataList());
			
			if(addCloudRequest.getNotifyCloudInfo().getUnkKeyValueDataList() == null){
				addCloudRequest.getNotifyCloudInfo().setUnkKeyValueDataList(new ArrayList<CloudNotifyLinkInfoKeyValueObjectRequest>());
			}
			unkJsonData = NotifyUtil.getJsonStringForCloudNotify(addCloudRequest.getNotifyCloudInfo().getUnkKeyValueDataList());
			
			//テキストスコープをファシリティIDから個別変換(NotifyCloudInfo.getScopeText()の処理を流用)
			String textScope = importData.getNotifyCloudInfo().getTextScope();
			if (textScope == null){
				try {
					textScope = new RepositoryControllerBean().getFacilityPath(importData.getNotifyCloudInfo().getFacilityId(), null);
				} catch (HinemosUnknown e) {
					Logger.getLogger(this.getClass()).debug(e.getMessage(), e);
				}
			}
			addCloudRequest.getNotifyCloudInfo().setTextScope(textScope);
			
			requestDto = addCloudRequest;
			break;

		default:
			return;
		}
		
		// DTOの項目相関チェック処理
		RestCommonValitater.checkRequestDto(requestDto);
		requestDto.correlationCheck();
		// DTOからINFOへ変換
		RestBeanUtil.convertBean(requestDto, infoReq);
		
		// クラウド通知の場合、個別変換処理を行う
		if(importData.getNotifyType().getCode() == NotifyTypeConstant.TYPE_CLOUD){
			// 情報
			infoReq.getNotifyCloudInfo().setInfoJsonData(infoJsonData);
			// 警告
			infoReq.getNotifyCloudInfo().setWarnJsonData(warnJsonData);
			// 危険
			infoReq.getNotifyCloudInfo().setCritJsonData(critJsonData);
			// 不明
			infoReq.getNotifyCloudInfo().setUnkJsonData(unkJsonData);
		}
		
		// ControllerBean呼び出し
		new NotifyControllerBean().addNotify(infoReq);
	}
	
	private void callModifyNotfy(NotifyRequestForUtility importData) throws InvalidSetting, HinemosUnknown, NotifyDuplicate, InvalidRole, NotifyNotFound{
		
		RequestDto requestDto = null;
		NotifyInfo infoReq = new NotifyInfo(importData.getNotifyId());
		
		// クラウド通知における文字列・クラスリストの変換処理の都合の変数
		String infoJsonData = null;
		String warnJsonData = null;
		String critJsonData = null;
		String unkJsonData = null;
		
		// NotifyTypeを参照し、各種通知のリクエストにdtoに変換
		switch (importData.getNotifyType().getCode()) {
		
		case NotifyTypeConstant.TYPE_COMMAND:
			// コマンド通知用
			ModifyCommandNotifyRequest modifyCommandRequest = new ModifyCommandNotifyRequest();
			RestBeanUtil.convertBeanSimple(importData, modifyCommandRequest);
			requestDto = modifyCommandRequest;
			break;
			
		case NotifyTypeConstant.TYPE_EVENT:
			// イベント通知用
			ModifyEventNotifyRequest modifyEventRequest = new ModifyEventNotifyRequest();
			RestBeanUtil.convertBeanSimple(importData, modifyEventRequest);
			requestDto = modifyEventRequest;
			break;
			
		case NotifyTypeConstant.TYPE_INFRA:
			// インフラ
			ModifyInfraNotifyRequest modifyInfraRequest = new ModifyInfraNotifyRequest();
			RestBeanUtil.convertBeanSimple(importData, modifyInfraRequest);
			requestDto = modifyInfraRequest;
			
			break;
		case NotifyTypeConstant.TYPE_JOB:
			// ジョブ
			ModifyJobNotifyRequest modifyJobRequest = new ModifyJobNotifyRequest();
			RestBeanUtil.convertBeanSimple(importData, modifyJobRequest);
			requestDto = modifyJobRequest;
			break;
			
		case NotifyTypeConstant.TYPE_LOG_ESCALATE:
			//　エスカレーション
			ModifyLogEscalateNotifyRequest modifyLogEscalateRequest = new ModifyLogEscalateNotifyRequest();
			RestBeanUtil.convertBeanSimple(importData, modifyLogEscalateRequest);
			requestDto = modifyLogEscalateRequest;
			break;
			
		case NotifyTypeConstant.TYPE_MAIL:
			// メール
			ModifyMailNotifyRequest modifyMailRequest = new ModifyMailNotifyRequest();
			RestBeanUtil.convertBeanSimple(importData, modifyMailRequest);
			requestDto = modifyMailRequest;
			break;
			
		case NotifyTypeConstant.TYPE_STATUS:
			// ステータス
			ModifyStatusNotifyRequest modifyStatusRequest = new ModifyStatusNotifyRequest();
			RestBeanUtil.convertBeanSimple(importData, modifyStatusRequest);
			requestDto = modifyStatusRequest;
			break;

		case NotifyTypeConstant.TYPE_REST:
			// REST
			ModifyRestNotifyRequest modifyRestRequest = new ModifyRestNotifyRequest();
			RestBeanUtil.convertBeanSimple(importData, modifyRestRequest);
			requestDto = modifyRestRequest;
			break;
		
		case NotifyTypeConstant.TYPE_MESSAGE:
			// メッセージ
			ModifyMessageNotifyRequest modifyMessageRequest = new ModifyMessageNotifyRequest();
			RestBeanUtil.convertBeanSimple(importData, modifyMessageRequest);
			requestDto = modifyMessageRequest;
			break;
		
		case NotifyTypeConstant.TYPE_CLOUD:
			// クラウド
			ModifyCloudNotifyRequest modifyCloudRequest = new ModifyCloudNotifyRequest();
			RestBeanUtil.convertBeanSimple(importData, modifyCloudRequest);
			// 個別変換
			// クラウド通知のディテール/データはDB上ではjsonで保存しているが
			// そのままではUtilityなどでの使い勝手が悪いので、コンポーネント間のやり取りでは
			// キーバリューオブジェクトのリストに変換している
			// ここではリスト→jsonの変換を実施
			if(modifyCloudRequest.getNotifyCloudInfo().getInfoKeyValueDataList() == null){
				modifyCloudRequest.getNotifyCloudInfo().setInfoKeyValueDataList(new ArrayList<CloudNotifyLinkInfoKeyValueObjectRequest>());
			}
			infoJsonData = NotifyUtil.getJsonStringForCloudNotify(modifyCloudRequest.getNotifyCloudInfo().getInfoKeyValueDataList());
			
			if(modifyCloudRequest.getNotifyCloudInfo().getWarnKeyValueDataList() == null){
				modifyCloudRequest.getNotifyCloudInfo().setWarnKeyValueDataList(new ArrayList<CloudNotifyLinkInfoKeyValueObjectRequest>());
			}
			warnJsonData = NotifyUtil.getJsonStringForCloudNotify(modifyCloudRequest.getNotifyCloudInfo().getWarnKeyValueDataList());
			
			if(modifyCloudRequest.getNotifyCloudInfo().getCritKeyValueDataList() == null){
				modifyCloudRequest.getNotifyCloudInfo().setCritKeyValueDataList(new ArrayList<CloudNotifyLinkInfoKeyValueObjectRequest>());
			}
			critJsonData = NotifyUtil.getJsonStringForCloudNotify(modifyCloudRequest.getNotifyCloudInfo().getCritKeyValueDataList());
			
			if(modifyCloudRequest.getNotifyCloudInfo().getUnkKeyValueDataList() == null){
				modifyCloudRequest.getNotifyCloudInfo().setUnkKeyValueDataList(new ArrayList<CloudNotifyLinkInfoKeyValueObjectRequest>());
			}
			unkJsonData = NotifyUtil.getJsonStringForCloudNotify(modifyCloudRequest.getNotifyCloudInfo().getUnkKeyValueDataList());
			
			requestDto = modifyCloudRequest;
			break;

		default:
			return;
		}
		
		// DTOの項目相関チェック処理
		RestCommonValitater.checkRequestDto(requestDto);
		requestDto.correlationCheck();
		// DTOからINFOへ変換
		RestBeanUtil.convertBean(requestDto, infoReq);
		
		// クラウド通知の場合、個別変換処理を行う
		if(importData.getNotifyType().getCode() == NotifyTypeConstant.TYPE_CLOUD){
			// 情報
			infoReq.getNotifyCloudInfo().setInfoJsonData(infoJsonData);
			// 警告
			infoReq.getNotifyCloudInfo().setWarnJsonData(warnJsonData);
			// 危険
			infoReq.getNotifyCloudInfo().setCritJsonData(critJsonData);
			// 不明
			infoReq.getNotifyCloudInfo().setUnkJsonData(unkJsonData);
		}
		
		// ControllerBean呼び出し
		new NotifyControllerBean().modifyNotify(infoReq);
	}
	// 下位互換(6.2.x版データ)対応のための補完処理 
	//  下位版には存在しない必須項目を バージョンアップ時の初期値で補完
	private void supportBackwardCompatibility(NotifyRequestForUtility target){
		if (target == null) {
			return;
		}
		if( target.getNotifyType() == NotifyTypeEnum.COMMAND && target.getNotifyCommandInfo() !=null ){
			if( target.getNotifyCommandInfo().getCommandSettingType() == null ){
				target.getNotifyCommandInfo().setCommandSettingType(CommandSettingTypeEnum.DIRECT_COMMAND);
			}
		}
	}
}
