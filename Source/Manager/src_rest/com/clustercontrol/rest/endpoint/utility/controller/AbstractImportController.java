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

import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.rest.endpoint.utility.dto.AbstractImportRecordRequest;
import com.clustercontrol.rest.endpoint.utility.dto.RecordRegistrationExceptionResponse;
import com.clustercontrol.rest.endpoint.utility.dto.RecordRegistrationResponse;
import com.clustercontrol.rest.endpoint.utility.dto.enumtype.ImportResultEnum;

/**
 * 
 * import時のトランザクション制御の共通化するためのコントローラークラス
 *
 * @param <T> import向けのレコード用Dto
 * @param <V> import向けの各レコードの処理結果返却用Dto
 */
public abstract class AbstractImportController<T extends AbstractImportRecordRequest<?>,V extends RecordRegistrationResponse> {
	List<T> importList;
	List<V> resultList =new ArrayList<V>() ;
	boolean isRollbackIfAbnormal;
	boolean occurException =false;

	public AbstractImportController( boolean isRollbackIfAbnormal, List<T> importList) {
		this.importList=importList;
		this.isRollbackIfAbnormal=isRollbackIfAbnormal;
	}

	public void importExecute(){

		if (isRollbackIfAbnormal){
			//"異常時はロールバック"なら １つでも異常が出れば全ロールバック
			JpaTransactionManager jtm = new JpaTransactionManager();
			try {
				jtm.begin();
				for( T importRec : importList){
					V importRes =proccssRecordWithCatch(importRec);
					resultList.add(importRes);
					if (importRes.getResult() == ImportResultEnum.ABEND ) {
						this.occurException = true;
					}
				}
				if (this.occurException == false) {
					jtm.commit();
				} else {
					jtm.rollback();
					//ロールバックしたので、個別では正常終了orスキップとなったフラグをすべて異常に変更
					for( V importRes : resultList){
						importRes.setResult(ImportResultEnum.ABEND);
					}
				}
			}finally{
				if (jtm != null) {
					jtm.close();
				}
			}
		}else{
			//"異常時はロールバック"以外は レコード単位でコミット制御
			for( T importRec : importList){
				JpaTransactionManager jtm = new JpaTransactionManager();
				try {
					jtm.begin();
					V importRes =proccssRecordWithCatch(importRec);
					resultList.add(importRes);
					if (!(importRes.getResult() == ImportResultEnum.ABEND)) {
						jtm.commit();
					}else{
						jtm.rollback();
						this.occurException = true;
					}
				}finally{
					if (jtm != null) {
						jtm.close();
					}
				}
			}
		}
	}
	protected V proccssRecordWithCatch( T importRec ){
		try{
			V dtoRecRes =proccssRecord(importRec);
			return dtoRecRes;
		}catch (Throwable e){
			V dtoRecRes = getRecordResponseInstance();
			dtoRecRes.setResult(ImportResultEnum.ABEND);
			dtoRecRes.setImportKeyValue(importRec.getImportKeyValue());
			dtoRecRes.setExceptionInfo(new RecordRegistrationExceptionResponse(e));
			return dtoRecRes;
		}
	}

	public boolean getOccurException(){
		return this.occurException;
	}
	public List<V> getResultList(){
		return this.resultList;
	}
	
	protected abstract V proccssRecord( T importRec ) throws Exception;
	protected abstract V getRecordResponseInstance() ;


}
