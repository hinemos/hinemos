/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.settings.infra.action;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openapitools.client.model.AddInfraManagementRequest;
import org.openapitools.client.model.ImportInfraManagementInfoRecordRequest;
import org.openapitools.client.model.ImportInfraManagementInfoRequest;
import org.openapitools.client.model.ImportInfraManagementInfoResponse;
import org.openapitools.client.model.InfraManagementInfoResponse;
import org.openapitools.client.model.RecordRegistrationResponse;
import org.openapitools.client.model.RecordRegistrationResponse.ResultEnum;

import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.RestConnectFailed;
import com.clustercontrol.infra.util.InfraRestClientWrapper;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.RestClientBeanUtil;
import com.clustercontrol.utility.constant.HinemosModuleConstant;
import com.clustercontrol.utility.settings.ConvertorException;
import com.clustercontrol.utility.settings.SettingConstants;
import com.clustercontrol.utility.settings.infra.conv.InfraSettingConv;
import com.clustercontrol.utility.settings.infra.xml.InfraManagement;
import com.clustercontrol.utility.settings.infra.xml.InfraManagementInfo;
import com.clustercontrol.utility.settings.model.BaseAction;
import com.clustercontrol.utility.settings.platform.action.ObjectPrivilegeAction;
import com.clustercontrol.utility.settings.ui.dialog.DeleteProcessDialog;
import com.clustercontrol.utility.settings.ui.dialog.UtilityDialogInjector;
import com.clustercontrol.utility.settings.ui.util.DeleteProcessMode;
import com.clustercontrol.utility.settings.ui.util.ImportProcessMode;
import com.clustercontrol.utility.util.Config;
import com.clustercontrol.utility.util.ImportClientController;
import com.clustercontrol.utility.util.ImportRecordConfirmer;
import com.clustercontrol.utility.util.UtilityDialogConstant;
import com.clustercontrol.utility.util.UtilityManagerUtil;
import com.clustercontrol.utility.util.UtilityRestClientWrapper;
import com.clustercontrol.utility.util.XmlMarshallUtil;

/**
 * 環境構築設定定義情報をインポート・エクスポート・削除するアクションクラス<br>
 * 
 * @version 6.0.0
 * @since 5.0.a
 */
public class InfraSettingAction extends BaseAction<InfraManagementInfoResponse, InfraManagementInfo, InfraManagement> {

	protected InfraSettingConv conv;
	protected List<String> objectList = new ArrayList<String>();
	public InfraSettingAction() throws ConvertorException {
		super();
		conv = new InfraSettingConv();
	}

	@Override
	protected String getActionName() {return "InfraManagement";}

	@Override
	protected List<InfraManagementInfoResponse> getList()	throws Exception {
		return InfraRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).getInfraManagementList(null);
	}

	@Override
	protected void deleteInfo(InfraManagementInfoResponse info) throws Exception {
		InfraRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).deleteInfraManagement(info.getManagementId());
	}

	@Override
	protected String getKeyInfoD(InfraManagementInfoResponse info) {
		return info.getManagementId();
	}

	@Override
	protected InfraManagement newInstance() {
		return new InfraManagement();
	}

	@Override
	protected void addInfo(InfraManagement xmlInfo,	InfraManagementInfoResponse info)	throws Exception {
		xmlInfo.addInfraManagementInfo(conv.getXmlInfo(info));
	}

	@Override
	protected void exportXml(InfraManagement xmlInfo, String xmlFile) throws Exception {
		xmlInfo.setCommon(com.clustercontrol.utility.settings.platform.conv.CommonConv.versionInfraDto2Xml(Config.getVersion()));
		xmlInfo.setSchemaInfo(conv.getSchemaVersion(com.clustercontrol.utility.settings.infra.xml.SchemaInfo.class));
		try(FileOutputStream fos = new FileOutputStream(xmlFile);
			OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");){
			xmlInfo.marshal(osw);
		}
	}

	@Override
	protected List<InfraManagementInfo> getElements(InfraManagement xmlInfo) {
		return Arrays.asList(xmlInfo.getInfraManagementInfo());
	}

	@Override
	protected int registElements(InfraManagement xmlInfo){
		int ret =0;
		// 定義の登録
		ImportRecordConfirmer<InfraManagementInfo, ImportInfraManagementInfoRecordRequest, String> confirmer = new ImportRecordConfirmer<InfraManagementInfo, ImportInfraManagementInfoRecordRequest, String>(
				log, xmlInfo.getInfraManagementInfo()) {
			@Override
			protected ImportInfraManagementInfoRecordRequest convertDtoXmlToRestReq(InfraManagementInfo xmlDto) throws InvalidSetting, HinemosUnknown {
				InfraManagementInfoResponse dto = conv.getDTO(xmlDto);
				ImportInfraManagementInfoRecordRequest dtoRec = new ImportInfraManagementInfoRecordRequest();
				dtoRec.setImportData( new AddInfraManagementRequest() );
				RestClientBeanUtil.convertBean(dto, dtoRec.getImportData());
				dtoRec.setImportKeyValue(dtoRec.getImportData().getManagementId());
				return dtoRec;
			}
			@Override
			protected Set<String> getExistIdSet() throws Exception {
				Set<String> retSet = new HashSet<String>();
				List<InfraManagementInfoResponse> infoList = InfraRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).getInfraManagementList(null);
				for (InfraManagementInfoResponse rec : infoList) {
					retSet.add(rec.getManagementId() );
				}
				return retSet;
			}
			@Override
			protected boolean isLackRestReq(ImportInfraManagementInfoRecordRequest restDto) {
				return (restDto == null || restDto.getImportData().getManagementId() == null);
			}
			@Override
			protected String getKeyValueXmlDto(InfraManagementInfo xmlDto) {
				return xmlDto.getManagementId();
			}
			@Override
			protected String getId(InfraManagementInfo xmlDto) {
				return xmlDto.getManagementId();
			}
			@Override
			protected void setNewRecordFlg(ImportInfraManagementInfoRecordRequest restDto, boolean flag) {
				restDto.setIsNewRecord(flag);
			}
		};
		int confirmRet = confirmer.executeConfirm();
		if( confirmRet != SettingConstants.SUCCESS && confirmRet != SettingConstants.ERROR_CANCEL ){
			//変換エラーならUnmarshalXml扱いで処理打ち切り(キャンセルはキャンセル以前の選択結果を反映するので次に進む)
			log.warn(Messages.getString("SettingTools.UnmarshalXmlFailed"));
			return confirmRet;
		}
		
		// 更新単位の件数毎にインポートメソッドを呼び出し、結果をログ出力
		// API異常発生時はそこで中断、レコード個別の異常発生時はユーザ選択次第で続行
		ImportClientController<ImportInfraManagementInfoRecordRequest, ImportInfraManagementInfoResponse, RecordRegistrationResponse> importController = new ImportClientController<ImportInfraManagementInfoRecordRequest, ImportInfraManagementInfoResponse, RecordRegistrationResponse>(
				log, Messages.getString("infra.management"), confirmer.getImportRecDtoList(),true) {
			@Override
			protected List<RecordRegistrationResponse> getResRecList(ImportInfraManagementInfoResponse importResponse) {
				return importResponse.getResultList();
			};
			@Override
			protected Boolean getOccurException(ImportInfraManagementInfoResponse importResponse) {
				return importResponse.getIsOccurException();
			};
			@Override
			protected String getReqKeyValue(ImportInfraManagementInfoRecordRequest importRec) {
				return importRec.getImportKeyValue();
			};
			@Override
			protected String getResKeyValue(RecordRegistrationResponse responseRec) {
				return responseRec.getImportKeyValue();
			};
			@Override
			protected boolean isResNormal(RecordRegistrationResponse responseRec) {
				return (responseRec.getResult() == ResultEnum.NORMAL) ;
			};
			@Override
			protected ImportInfraManagementInfoResponse callImportWrapper(List<ImportInfraManagementInfoRecordRequest> importRecList)
					throws HinemosUnknown, InvalidUserPass, InvalidRole, RestConnectFailed {
				ImportInfraManagementInfoRequest reqDto = new ImportInfraManagementInfoRequest();
				reqDto.setRecordList(importRecList);
				reqDto.setRollbackIfAbnormal(ImportProcessMode.isRollbackIfAbnormal());
				return UtilityRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).importInfraManagementInfo(reqDto) ;
			}
			@Override
			protected String getRestExceptionMessage(RecordRegistrationResponse responseRec) {
				if (responseRec.getExceptionInfo() != null) {
					return responseRec.getExceptionInfo().getException() +":"+ responseRec.getExceptionInfo().getMessage();
				}
				return null;
			};
		};
		ret = importController.importExecute();

		//重複確認でキャンセルが選択されていたら 以降の処理は行わない
		if (ImportProcessMode.getProcesstype() == UtilityDialogConstant.CANCEL) {
			return SettingConstants.ERROR_INPROCESS;
		}
		// オブジェクト権限のインポート対象を設定
		for( RecordRegistrationResponse rec: importController.getImportSuccessList() ){
			objectList.add(rec.getImportKeyValue());
		}
		return ret;
	}

	protected List<String> getImportObjects(){
		return objectList;
	}

	@Override
	protected String getKeyInfoE(InfraManagementInfo info) {
		return info.getManagementId();
	}

	@Override
	protected InfraManagement getXmlInfo(String filePath) throws Exception {
		// FIXME
		// #5672 attibute である execCommand, checkCommand に含まれる改行が半角スペースに変更されてしまう事象への暫定対処
		// 本質的には execCommand, checkCommand を attibute ではなく element として定義する必要があるが、
		// マイナーバージョンでは修正前の XML が取り込めなくなってしまうため対応できない
		// 次期メジャーバージョンでは element への変換を検討すること
		StringBuffer sb = new StringBuffer();
		try (BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(filePath), "UTF-8"))) {
			String line = in.readLine();
			while ((line = in.readLine()) != null) {
				sb.append("&#xA;");
				sb.append(line);
			}
		}
		// XML宣言後の改行は元に戻す
		String xml = sb.toString().replaceFirst("&#xA;", System.getProperty("line.separator"));
		return XmlMarshallUtil.unmarshall(InfraManagement.class,new StringReader(xml));
	}

	@Override
	protected int checkSchemaVersion(InfraManagement xmlInfo) throws Exception {
		/*スキーマのバージョンチェック*/
		int res = conv.checkSchemaVersion(
				xmlInfo.getSchemaInfo().getSchemaType(),
				xmlInfo.getSchemaInfo().getSchemaVersion(),
				xmlInfo.getSchemaInfo().getSchemaRevision());
		com.clustercontrol.utility.settings.monitor.xml.SchemaInfo sci = 
				conv.getSchemaVersion(com.clustercontrol.utility.settings.monitor.xml.SchemaInfo.class);
		
		if (!BaseAction.checkSchemaVersionResult(this.getLogger(), res, sci.getSchemaType(), sci.getSchemaVersion(), sci.getSchemaRevision())) {
			return SettingConstants.ERROR_SCHEMA_VERSION;
		}
		return 0;
	}

	@Override
	protected InfraManagementInfo[] getArray(InfraManagement info) {
		return info.getInfraManagementInfo();
	}

	@Override
	protected int compare(
			InfraManagementInfoResponse info1,
			InfraManagementInfoResponse info2) {
		return info1.getManagementId().compareTo(info2.getManagementId());
	}

	@Override
	protected int sortCompare(InfraManagementInfo info1,
			InfraManagementInfo info2) {
		return info1.getManagementId().compareTo(info2.getManagementId());
	}

	@Override
	protected void setArray(InfraManagement info, InfraManagementInfo[] infoList) {
		info.setInfraManagementInfo(infoList);
	}

	@Override
	protected void checkDelete(InfraManagement xmlInfo) throws Exception{
		
		List<InfraManagementInfoResponse> subList = getList();
		List<InfraManagementInfo> xmlElements = new ArrayList<>(getElements(xmlInfo));
		
		for(InfraManagementInfoResponse mgrInfo: new ArrayList<>(subList)){
			for(InfraManagementInfo xmlElement: new ArrayList<>(xmlElements)){
				if(getKeyInfoD(mgrInfo).equals(getKeyInfoE(xmlElement))){
					subList.remove(mgrInfo);
					xmlElements.remove(xmlElement);
					break;
				}
			}
		}
		
		if(subList.size() > 0){
			for(InfraManagementInfoResponse info: subList){
				//マネージャのみに存在するデータがあった場合の削除方法を確認する
				if(!DeleteProcessMode.isSameprocess()){
					String[] args = {getKeyInfoD(info)};
					DeleteProcessDialog dialog = UtilityDialogInjector.createDeleteProcessDialog(
							null, Messages.getString("message.delete.confirm4", args));
					DeleteProcessMode.setProcesstype(dialog.open());
					DeleteProcessMode.setSameprocess(dialog.getToggleState());
				}
			    
			    if(DeleteProcessMode.getProcesstype() == UtilityDialogConstant.DELETE){
			    	try {
			    		deleteInfo(info);
						log.info(Messages.getString("SettingTools.SubSucceeded.Delete") + " : " + getKeyInfoD(info));
					} catch (Exception e1) {
						log.warn(Messages.getString("SettingTools.ClearFailed") + " : " + HinemosMessage.replace(e1.getMessage()));
					}
			    } else if(DeleteProcessMode.getProcesstype() == UtilityDialogConstant.SKIP){
			    	log.info(Messages.getString("SettingTools.SubSucceeded.Skip") + " : " + getKeyInfoD(info));
			    } else if(DeleteProcessMode.getProcesstype() == UtilityDialogConstant.CANCEL){
			    	log.info(Messages.getString("SettingTools.SubSucceeded.Cancel"));
			    }
			}
		}
	}
	
	/**
	 * オブジェクト権限同時インポート
	 * 
	 * @param objectType
	 * @param objectIdList
	 */
	@Override
	protected void importObjectPrivilege(List<String> objectList){
		if(ImportProcessMode.isSameObjectPrivilege()){
			ObjectPrivilegeAction.importAccessExtraction(
					ImportProcessMode.getXmlObjectPrivilege(),
					HinemosModuleConstant.INFRA,
					objectList,
					getLogger());
		}
	}
}
