/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.settings.system.action;

import java.io.FileInputStream;

import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.openapitools.client.model.AddHinemosPropertyRequest;
import org.openapitools.client.model.HinemosPropertyResponse;
import org.openapitools.client.model.ImportHinemosPropertyRecordRequest;
import org.openapitools.client.model.ImportHinemosPropertyRequest;
import org.openapitools.client.model.ImportHinemosPropertyResponse;

import org.openapitools.client.model.RecordRegistrationResponse;
import org.openapitools.client.model.RecordRegistrationResponse.ResultEnum;

import com.clustercontrol.common.util.CommonRestClientWrapper;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.RestConnectFailed;

import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.RestClientBeanUtil;
import com.clustercontrol.utility.settings.ConvertorException;
import com.clustercontrol.utility.settings.SettingConstants;
import com.clustercontrol.utility.settings.maintenance.xml.HinemosProperty;
import com.clustercontrol.utility.settings.maintenance.xml.HinemosPropertyInfo;
import com.clustercontrol.utility.settings.model.BaseAction;
import com.clustercontrol.utility.settings.system.conv.HinemosPropertyConv;
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
 * Hinemosプロパティ定義情報をインポート・エクスポート・削除するアクションクラス<br>
 * 
 * @version 6.1.0
 * @since 5.0.a
 * 
 */
public class HinemosPropertyAction extends
		BaseAction<HinemosPropertyResponse, HinemosPropertyInfo, HinemosProperty> {

	protected HinemosPropertyConv conv;

	public HinemosPropertyAction() throws ConvertorException {
		super();
		conv = new HinemosPropertyConv();
	}

	@Override
	protected String getActionName() {
		return "PlatformHinemosProperty";
	}

	@Override
	protected List<HinemosPropertyResponse> getList() throws Exception {
		return CommonRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName())
				.getHinemosPropertyList();
	}

	@Override
	protected void deleteInfo(HinemosPropertyResponse info)
			throws Exception {
		CommonRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName())
				.deleteHinemosProperty(info.getKey());
	}

	@Override
	protected String getKeyInfoD(HinemosPropertyResponse info) {
		return info.getKey();
	}

	@Override
	protected HinemosProperty newInstance() {
		return new HinemosProperty();
	}

	@Override
	protected void addInfo(HinemosProperty xmlInfo, HinemosPropertyResponse info)
			throws Exception {
		xmlInfo.addHinemosPropertyInfo(conv.getXmlInfo(info));
	}

	@Override
	protected void exportXml(HinemosProperty xmlInfo, String xmlFile) throws Exception {
		xmlInfo.setCommon(com.clustercontrol.utility.settings.platform.conv.CommonConv
				.versionMaintenanceDto2Xml(Config.getVersion()));
		xmlInfo.setSchemaInfo(
				conv.getSchemaVersion(com.clustercontrol.utility.settings.maintenance.xml.SchemaInfo.class));
		try (FileOutputStream fos = new FileOutputStream(xmlFile);
				OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");) {
			xmlInfo.marshal(osw);
		}
	}

	@Override
	protected List<HinemosPropertyInfo> getElements(HinemosProperty xmlInfo) {
		return Arrays.asList(xmlInfo.getHinemosPropertyInfo());
	}

	@Override
	protected int registElements(HinemosProperty xmlInfo) throws Exception {
		int ret = 0;
		ImportHinemosPropertyRecordConfirmer hinemosPropertyConfirmer = new ImportHinemosPropertyRecordConfirmer( log, xmlInfo.getHinemosPropertyInfo());
		int hinemosPropertyConfirmerRet = hinemosPropertyConfirmer.executeConfirm();
		if (hinemosPropertyConfirmerRet != 0) {
			ret = hinemosPropertyConfirmerRet;
		}
		
		if( hinemosPropertyConfirmerRet != SettingConstants.SUCCESS && hinemosPropertyConfirmerRet != SettingConstants.ERROR_CANCEL ){
			//変換エラーならUnmarshalXml扱いで処理打ち切り(キャンセルはキャンセル以前の選択結果を反映するので次に進む)
			log.warn(Messages.getString("SettingTools.UnmarshalXmlFailed"));
			return hinemosPropertyConfirmerRet;
		}
		
		// レコードの登録（Hinemosプロパティ）
		if (!(hinemosPropertyConfirmer.getImportRecDtoList().isEmpty())) {
			ImportHinemosPropertyClientController hinemosPropertyController = new ImportHinemosPropertyClientController(log,
					Messages.getString("platform.hinemos.property"), hinemosPropertyConfirmer.getImportRecDtoList(), true);
			int hinemosPropertyControllerRet = hinemosPropertyController.importExecute();
			if (hinemosPropertyControllerRet != 0) {
				ret = hinemosPropertyControllerRet;
			}
		}
		
		//重複確認でキャンセルが選択されていたら 以降の処理は行わない
		if (ImportProcessMode.getProcesstype() == UtilityDialogConstant.CANCEL) {
			return SettingConstants.ERROR_INPROCESS;
		}
		
		return ret;
	}
	@Override
	protected String getKeyInfoE(HinemosPropertyInfo info) {
		return info.getKey();
	}

	@Override
	protected HinemosProperty getXmlInfo(String filePath) throws Exception {
		return XmlMarshallUtil.unmarshall(HinemosProperty.class,new InputStreamReader(new FileInputStream(filePath), "UTF-8"));
	}

	@Override
	protected int checkSchemaVersion(HinemosProperty xmlInfo) throws Exception {
		/* スキーマのバージョンチェック */
		int res = conv.checkSchemaVersion(xmlInfo.getSchemaInfo().getSchemaType(),
				xmlInfo.getSchemaInfo().getSchemaVersion(), xmlInfo.getSchemaInfo().getSchemaRevision());
		com.clustercontrol.utility.settings.maintenance.xml.SchemaInfo sci = conv
				.getSchemaVersion(com.clustercontrol.utility.settings.maintenance.xml.SchemaInfo.class);

		boolean chkres = BaseAction.checkSchemaVersionResult(log, res, sci.getSchemaType(), sci.getSchemaVersion(),
				sci.getSchemaRevision());

		if (chkres) {
			return 0;
		} else {
			return SettingConstants.ERROR_SCHEMA_VERSION;
		}
	}

	@Override
	protected HinemosPropertyInfo[] getArray(HinemosProperty info) {
		return info.getHinemosPropertyInfo();
	}

	@Override
	protected int compare(HinemosPropertyResponse info1,
			HinemosPropertyResponse info2) {
		return info1.getKey().compareTo(info2.getKey());
	}

	@Override
	protected int sortCompare(HinemosPropertyInfo info1, HinemosPropertyInfo info2) {
		return info1.getKey().compareTo(info2.getKey());
	}

	@Override
	protected void setArray(HinemosProperty info, HinemosPropertyInfo[] infoList) {
		info.setHinemosPropertyInfo(infoList);
	}

	@Override
	protected void checkDelete(HinemosProperty xmlInfo) throws Exception {

		List<HinemosPropertyResponse> subList = getList();
		List<HinemosPropertyInfo> xmlElements = new ArrayList<>(getElements(xmlInfo));

		for (HinemosPropertyResponse mgrInfo : new ArrayList<>(subList)) {
			for (HinemosPropertyInfo xmlElement : new ArrayList<>(xmlElements)) {
				if (getKeyInfoD(mgrInfo).equals(getKeyInfoE(xmlElement))) {
					subList.remove(mgrInfo);
					xmlElements.remove(xmlElement);
					break;
				}
			}
		}

		if (subList.size() > 0) {
			for (HinemosPropertyResponse info : subList) {
				// マネージャのみに存在するデータがあった場合の削除方法を確認する
				if (!DeleteProcessMode.isSameprocess()) {
					String[] args = { getKeyInfoD(info) };
					DeleteProcessDialog dialog = UtilityDialogInjector.createDeleteProcessDialog(null,
							Messages.getString("message.delete.confirm4", args));
					DeleteProcessMode.setProcesstype(dialog.open());
					DeleteProcessMode.setSameprocess(dialog.getToggleState());
				}

				if (DeleteProcessMode.getProcesstype() == UtilityDialogConstant.DELETE) {
					try {
						deleteInfo(info);
						log.info(Messages.getString("SettingTools.SubSucceeded.Delete") + " : " + getKeyInfoD(info));
					} catch (Exception e1) {
						log.warn(Messages.getString("SettingTools.ClearFailed") + " : "
								+ HinemosMessage.replace(e1.getMessage()));
					}
				} else if (DeleteProcessMode.getProcesstype() == UtilityDialogConstant.SKIP) {
					log.info(Messages.getString("SettingTools.SubSucceeded.Skip") + " : " + getKeyInfoD(info));
				} else if (DeleteProcessMode.getProcesstype() == UtilityDialogConstant.CANCEL) {
					log.info(Messages.getString("SettingTools.SubSucceeded.Cancel"));
					return;
				}
			}
		}
	}

	@Override
	protected void importObjectPrivilege(List<String> objectList) {
		// Hinemosプロパティのオブジェクト権限同時インポートは行わない
	}

	/**
	 * Hinemosプロパティ インポート向けのレコード確認用クラス
	 * 
	 */
	protected class ImportHinemosPropertyRecordConfirmer extends ImportRecordConfirmer<HinemosPropertyInfo, ImportHinemosPropertyRecordRequest, String>{
		
		public ImportHinemosPropertyRecordConfirmer(Logger logger, HinemosPropertyInfo[] importRecDtoList) {
			super(logger, importRecDtoList);
		}
		
		@Override
		protected ImportHinemosPropertyRecordRequest convertDtoXmlToRestReq(HinemosPropertyInfo xmlDto)
				throws HinemosUnknown, InvalidSetting {
			
			HinemosPropertyResponse dto;
			try {
				dto = conv.getDTO(xmlDto);
			} catch (Exception e) {
				throw new HinemosUnknown(e);
			}
			ImportHinemosPropertyRecordRequest dtoRec = new ImportHinemosPropertyRecordRequest();
			
			AddHinemosPropertyRequest addHinemosPropertyRequest = new AddHinemosPropertyRequest();
			RestClientBeanUtil.convertBeanSimple(dto,addHinemosPropertyRequest);
			addHinemosPropertyRequest.setType(AddHinemosPropertyRequest.TypeEnum.fromValue(dto.getType().getValue()));
			
			dtoRec.setImportData(addHinemosPropertyRequest);
			dtoRec.setImportKeyValue(dtoRec.getImportData().getKey());
			
			return dtoRec;
		}

		@Override
		protected Set<String> getExistIdSet() throws Exception {
			Set<String> retSet = new HashSet<String>();
			List<HinemosPropertyResponse> hinemosPropertyInfoList = CommonRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).getHinemosPropertyList();
			for (HinemosPropertyResponse rec : hinemosPropertyInfoList) {
				retSet.add(rec.getKey());
			}
			return retSet;
		}
		@Override
		protected boolean isLackRestReq(ImportHinemosPropertyRecordRequest restDto) {
			return (restDto == null || restDto.getImportData().getKey() == null || restDto.getImportData().getKey().equals(""));
		}
		@Override
		protected String getKeyValueXmlDto(HinemosPropertyInfo xmlDto) {
			return xmlDto.getKey();
		}
		@Override
		protected String getId(HinemosPropertyInfo xmlDto) {
			return xmlDto.getKey();
		}
		@Override
		protected void setNewRecordFlg(ImportHinemosPropertyRecordRequest restDto, boolean flag) {
			restDto.setIsNewRecord(flag);
		}
	}
	
	/**
	 * Hinemosプロパティ インポート向けのレコード登録用クラス
	 * 
	 */
	protected static class ImportHinemosPropertyClientController extends ImportClientController<ImportHinemosPropertyRecordRequest, ImportHinemosPropertyResponse, RecordRegistrationResponse>{
		
		public ImportHinemosPropertyClientController(Logger logger, String importInfoName, List<ImportHinemosPropertyRecordRequest> importRecList ,boolean displayFailed) {
			super(logger, importInfoName,importRecList,displayFailed);
		}
		@Override
		protected List<RecordRegistrationResponse> getResRecList(ImportHinemosPropertyResponse importResponse) {
			return importResponse.getResultList();
		};

		@Override
		protected Boolean getOccurException(ImportHinemosPropertyResponse importResponse) {
			return importResponse.getIsOccurException();
		};

		@Override
		protected String getReqKeyValue(ImportHinemosPropertyRecordRequest importRec) {
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
		protected boolean isResSkip(RecordRegistrationResponse responseRec) {
			return (responseRec.getResult() == ResultEnum.SKIP) ;
		};

		@Override
		protected ImportHinemosPropertyResponse callImportWrapper(List<ImportHinemosPropertyRecordRequest> importRecList)
				throws HinemosUnknown, InvalidUserPass, InvalidRole, RestConnectFailed {
			ImportHinemosPropertyRequest reqDto = new ImportHinemosPropertyRequest();
			reqDto.setRecordList(importRecList);
			reqDto.setRollbackIfAbnormal(ImportProcessMode.isRollbackIfAbnormal());
			return UtilityRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).importHinemosProperty(reqDto);
		}

		@Override
		protected String getRestExceptionMessage(RecordRegistrationResponse responseRec) {
			if (responseRec.getExceptionInfo() != null) {
				return responseRec.getExceptionInfo().getException() +":"+ responseRec.getExceptionInfo().getMessage();
			}
			return null;
		};

		@Override
		protected void setResultLog( RecordRegistrationResponse responseRec ){
			String keyValue = getResKeyValue(responseRec);
			if ( isResNormal(responseRec) ) {
				log.info(Messages.getString("SettingTools.ImportSucceeded") + " : "+ this.importInfoName + ":" + keyValue);
			} else if(isResSkip(responseRec)){
				log.info(Messages.getString("SettingTools.SkipSystemRole") + " : " + this.importInfoName + ":" + keyValue);
			} else {
				log.warn(Messages.getString("SettingTools.ImportFailed") + " : "+ this.importInfoName + ":" + keyValue + " : "
						+ HinemosMessage.replace(getRestExceptionMessage(responseRec)));
			}
		}
	}

	@Override
	protected List<String> getImportObjects() {
		return null;
	}
}
