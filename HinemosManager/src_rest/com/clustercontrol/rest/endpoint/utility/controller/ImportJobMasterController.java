/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.utility.controller;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.commons.util.HinemosSessionContext;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.jobmanagement.bean.JobTreeItem;
import com.clustercontrol.jobmanagement.session.JobControllerBean;
import com.clustercontrol.jobmanagement.util.JobValidator;
import com.clustercontrol.rest.endpoint.jobmanagement.JobRestEndpoints;
import com.clustercontrol.rest.endpoint.utility.dto.ImportJobMasterRecordRequest;
import com.clustercontrol.rest.endpoint.utility.dto.RecordRegistrationResponse;
import com.clustercontrol.rest.util.RestCommonConverter;
import com.clustercontrol.rest.util.RestCommonValitater;
import com.clustercontrol.rest.endpoint.utility.dto.enumtype.ImportResultEnum;
import com.clustercontrol.util.MessageConstant;

public class ImportJobMasterController extends AbstractImportController<ImportJobMasterRecordRequest, RecordRegistrationResponse> {
	private static Log m_log = LogFactory.getLog(ImportNodeController.class);
	private String remoteAddr = null;

	public ImportJobMasterController(boolean isRollbackIfAbnormal, List<ImportJobMasterRecordRequest> importList) {
		super(isRollbackIfAbnormal, importList);
	}
	public void setRemoteAddr(String remoteAddr){
		this.remoteAddr = remoteAddr;
	}

	@Override
	protected RecordRegistrationResponse proccssRecord(ImportJobMasterRecordRequest importRec) throws Exception {

		RecordRegistrationResponse dtoRecRes= new RecordRegistrationResponse();
		dtoRecRes.setImportKeyValue(importRec.getImportKeyValue());

		RestCommonValitater.checkRequestDto(importRec.getImportData());
		importRec.getImportData().correlationCheck();
		// DTOからINFOへ変換
		JobTreeItem reqItem = JobRestEndpoints.convertJobTreeItemFromDto(importRec.getImportData());
		//JobUnit存在チェック
		if(importRec.getIsNewRecord()){
			//add
			try{
				JobValidator.validateJobId(reqItem.getData().getJobunitId(), reqItem.getData().getId(),true);
				throw new InvalidSetting( reqItem.getData().getJobunitId()+" is exist");// 
			} catch (InvalidSetting e) {
				m_log.info("proccssRecord() : jobunit " + reqItem.getData().getJobunitId() + " is new");
			}
		}else{
			//Modify
			try{
				JobValidator.validateJobId(reqItem.getData().getJobunitId(), reqItem.getData().getId(),true);
				m_log.info("proccssRecord() : jobunit " + reqItem.getData().getJobunitId() + " is exist");
			} catch (InvalidSetting e) {
				throw new InvalidSetting( reqItem.getData().getJobunitId()+" is not exist");
			}
		}
		//編集ロック取得(取得したロックは更新の正常完了時には自動的に開放されるので注意)
		Long updateTime = null;
		if(importRec.getEditLockData().getUpdateTime()!=null){
			updateTime = RestCommonConverter.convertDTStringToHinemosTime(importRec.getEditLockData().getUpdateTime(), MessageConstant.UPDATE_DATE.getMessage());
		}
		Integer editSession = new JobControllerBean().getEditLock(reqItem.getData().getJobunitId(), updateTime, importRec.getEditLockData().getForceFlag(), HinemosSessionContext.getLoginUserId(), this.remoteAddr);
		if( m_log.isDebugEnabled() ){
			m_log.debug("proccssRecord() : getEditLock . sessionid="+editSession);
		}
		
		//登録（新規/変更）
		try{
			new JobControllerBean().registerJobunit(reqItem);
			if( m_log.isDebugEnabled() ){
				m_log.debug("proccssRecord() : registerJobunit is completed");
			}
		}catch(Exception e){
			//異常発生時は、取得した編集ロックを開放してから異常を返す
			try{
				new JobControllerBean().releaseEditLock(editSession,reqItem.getData().getJobunitId(), HinemosSessionContext.getLoginUserId(), this.remoteAddr);
			}catch(Exception ex){
				//ロック解放時に発生した異常はログだけ出力して握りつぶす(通常ありえない)
				m_log.error("proccssRecord() : releaseEditLock error . message="+ ex.getMessage() ,ex);
			}
			throw e;
		}
		
		dtoRecRes.setResult(ImportResultEnum.NORMAL);
		return dtoRecRes;
	}
	
	@Override
	protected RecordRegistrationResponse getRecordResponseInstance(){
		return  new RecordRegistrationResponse();
	}
	
	// JobControllerBean().registerJobunit が自身より 上位でのトランザクション制御に対応していないので 
	// ・Controller側でのトランザクション制御を行わない
	// ・上記に伴い「１件でもエラーの場合、全件ロールバック」 への対応を行なわない（異常レコードをスキップのみ）
	// ・isRollbackIfAbnormal制御フラグは無視
	@Override
	public void importExecute(){
		for( ImportJobMasterRecordRequest importRec : importList){
			RecordRegistrationResponse importRes = proccssRecordWithCatch(importRec);
			resultList.add(importRes);
		}
	}
	

}
