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

import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.rest.endpoint.cloud.CloudRestEndpoints;
import com.clustercontrol.rest.endpoint.cloud.dto.AddCloudLoginUserRequest;
import com.clustercontrol.rest.endpoint.utility.dto.ImportCloudScopeRecordRequest;
import com.clustercontrol.rest.endpoint.utility.dto.RecordRegistrationResponse;
import com.clustercontrol.rest.endpoint.utility.dto.enumtype.ImportResultEnum;
import com.clustercontrol.rest.util.RestBeanUtil;
import com.clustercontrol.rest.util.RestCommonValitater;
import com.clustercontrol.xcloud.CloudManagerException;
import com.clustercontrol.xcloud.Session;
import com.clustercontrol.xcloud.bean.AddCloudScopeRequest.ITransformer;
import com.clustercontrol.xcloud.bean.AddPrivateCloudScopeRequest;
import com.clustercontrol.xcloud.bean.AddPublicCloudScopeRequest;
import com.clustercontrol.xcloud.bean.CloudScope;
import com.clustercontrol.xcloud.bean.ModifyPrivateCloudScopeRequest;
import com.clustercontrol.xcloud.bean.ModifyPublicCloudScopeRequest;
import com.clustercontrol.xcloud.bean.PrivateCloudScope;
import com.clustercontrol.xcloud.bean.PrivateLocation;
import com.clustercontrol.xcloud.bean.PublicCloudScope;
import com.clustercontrol.xcloud.factory.CloudManager;
import com.clustercontrol.xcloud.factory.ICloudOption;
import com.clustercontrol.xcloud.factory.IPrivateCloudOption;
import com.clustercontrol.xcloud.factory.IPublicCloudOption;
import com.clustercontrol.xcloud.model.CloudLoginUserEntity;
import com.clustercontrol.xcloud.model.CloudScopeEntity;
import com.clustercontrol.xcloud.model.PrivateCloudScopeEntity;
import com.clustercontrol.xcloud.model.PublicCloudScopeEntity;

public class ImportCloudScopeController extends AbstractImportControllerForCloud<ImportCloudScopeRecordRequest, RecordRegistrationResponse> {

	private static Log m_log = LogFactory.getLog(ImportCloudScopeController.class);

	public ImportCloudScopeController(boolean isRollbackIfAbnormal, List<ImportCloudScopeRecordRequest> importList) {
		super(isRollbackIfAbnormal, importList);
	}

	@Override
	public  RecordRegistrationResponse proccssRecord( ImportCloudScopeRecordRequest importRec) throws Exception {

		RecordRegistrationResponse dtoRecRes= new RecordRegistrationResponse();
		dtoRecRes.setImportKeyValue(importRec.getImportKeyValue());

		RestCommonValitater.checkRequestDto(importRec.getImportData());
		importRec.getImportData().correlationCheck();
		Boolean isPublic = CloudRestEndpoints.isPublic(importRec.getImportData().getPlatformId());
		importRec.getImportData().getAccount().setPublic(isPublic);

		// メインアカウントのチェック
		CloudManager.singleton().optionExecute(importRec.getImportData().getPlatformId(), new CloudManager.OptionExecutor() {
			@Override
			public void execute(ICloudOption option) throws CloudManagerException {
				if (isPublic) {
					option.visit(new ICloudOption.IVisitor() {
						@Override
						public void visit(IPrivateCloudOption cloudOption) throws CloudManagerException {
							throw new CloudManagerException();
						}

						@Override
						public void visit(IPublicCloudOption cloudOption) throws CloudManagerException {
							cloudOption.validCredentialAsAccount(importRec.getImportData().getAccount().getCredential());
						}
					});

				} else {
					option.visit(new ICloudOption.IVisitor() {
						@Override
						public void visit(IPrivateCloudOption cloudOption) throws CloudManagerException {
							List<PrivateLocation> location = new ArrayList<>();
							try {
								RestBeanUtil.convertBean(importRec.getImportData().getPrivateLocations(), location);
								cloudOption.validCredentialAsAccount(importRec.getImportData().getAccount().getCredential(), location);
							} catch (Exception e) {
								throw new CloudManagerException(e.getMessage());
							}
						}
						@Override
						public void visit(IPublicCloudOption cloudOption) throws CloudManagerException {
							throw new CloudManagerException();
						}
					});
				}
			}
		});

		//CloudScope情報
		if(importRec.getIsNewRecord()){
			//新規登録
			com.clustercontrol.xcloud.bean.AddCloudScopeRequest infoReq;
			if (isPublic) {
				infoReq = new AddPublicCloudScopeRequest();
			} else {
				infoReq = new AddPrivateCloudScopeRequest();
			}
			RestBeanUtil.convertBean(importRec.getImportData(), infoReq);

			infoReq.transform(new ITransformer<CloudScope>() {
				@Override
				public CloudScope transform(AddPublicCloudScopeRequest request)
						throws CloudManagerException, InvalidRole {
					return new PublicCloudScope(CloudManager.singleton().getCloudScopes().addPublicCloudScope(request));
				}

				@Override
				public CloudScope transform(AddPrivateCloudScopeRequest request)
						throws CloudManagerException, InvalidRole {
					return new PrivateCloudScope(
							CloudManager.singleton().getCloudScopes().addPrivateCloudScope(request));
				}
			});
		}else{
			//変更
			com.clustercontrol.xcloud.bean.ModifyCloudScopeRequest infoReq;
			if (isPublic) {
				infoReq = new ModifyPublicCloudScopeRequest();
			} else {
				infoReq = new ModifyPrivateCloudScopeRequest();
			}
			RestBeanUtil.convertBean(importRec.getImportData(), infoReq);
			infoReq.setCloudScopeId(importRec.getImportData().getCloudScopeId());

			CloudManager.singleton().getCloudScopes().modifyCloudScope(infoReq)
			.transform(new CloudScopeEntity.ITransformer<CloudScope>() {
				@Override
				public CloudScope transform(PublicCloudScopeEntity scope) throws CloudManagerException {
					return new PublicCloudScope(scope);
				}

				@Override
				public CloudScope transform(PrivateCloudScopeEntity scope) throws CloudManagerException {
					return new PrivateCloudScope(scope);
				}
			});
			
		}

		// クラウドスコープ登録後にサブアカウントのチェック
		for(AddCloudLoginUserRequest rec : importRec.getSubUserList()){
			rec.setPublic(isPublic);
			rec.correlationCheck();

			CloudScopeEntity scope = CloudManager.singleton().getCloudScopes().getCloudScope(importRec.getImportData().getCloudScopeId());
			// クラウド側にユーザー情報が存在するか確認。
			scope.optionExecute(new CloudScopeEntity.OptionExecutor() {
				@Override
				public void execute(CloudScopeEntity scope, ICloudOption option) throws CloudManagerException {
					option.getUserManagement(scope).validCredentialAsUser(rec.getCredential());
				}
			});
		}

		//サブユーザ(メインユーザ以外を全削除したのち再登録）
		//削除(メイン以外のユーザーを一旦全部削除)
		List<CloudLoginUserEntity> userList = CloudManager.singleton().getLoginUsers()
				.getCloudLoginUserByCloudScopeAndHinemosUser(importRec.getImportData().getCloudScopeId(),
						Session.current().getHinemosCredential().getUserId());
		for(CloudLoginUserEntity userRec : userList){
			if( !( importRec.getImportData().getAccount().getLoginUserId().equals(userRec.getLoginUserId()) ) ){
				CloudManager.singleton().getLoginUsers()
						.removeCloudLoginUser(importRec.getImportData().getCloudScopeId(), userRec.getLoginUserId());
			}
		}
		//追加		
		for(AddCloudLoginUserRequest addRec : importRec.getSubUserList()){
			com.clustercontrol.xcloud.bean.AddCloudLoginUserRequest infoReq = new com.clustercontrol.xcloud.bean.AddCloudLoginUserRequest();
			RestBeanUtil.convertBean(addRec, infoReq);
			infoReq.setCloudScopeId(importRec.getImportData().getCloudScopeId());
			CloudManager.singleton().getLoginUsers().addUser(infoReq);
		}

		//ユーザー優先順を更新
		CloudManager.singleton().getLoginUsers().modifyCloudLoginUserPriority(importRec.getImportData().getCloudScopeId(),
				importRec.getPriorityArrayList());

		//課金情報
		com.clustercontrol.xcloud.bean.ModifyBillingSettingRequest infoReq = new com.clustercontrol.xcloud.bean.ModifyBillingSettingRequest();
		RestBeanUtil.convertBean(importRec.getBillingSetting(), infoReq);
		infoReq.setCloudScopeId(importRec.getImportData().getCloudScopeId());
		CloudManager.singleton().getCloudScopes().modifyBillingSetting(infoReq);
		

		dtoRecRes.setResult(ImportResultEnum.NORMAL);
		return dtoRecRes;
	}

	@Override
	protected RecordRegistrationResponse getRecordResponseInstance(){
		return  new RecordRegistrationResponse();
	}
}
