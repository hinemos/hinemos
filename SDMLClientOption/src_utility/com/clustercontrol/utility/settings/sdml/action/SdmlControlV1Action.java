/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.utility.settings.sdml.action;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.openapitools.client.model.AddSdmlControlSettingRequest;
import org.openapitools.client.model.ImportSdmlControlRecordRequest;
import org.openapitools.client.model.ImportSdmlControlRequest;
import org.openapitools.client.model.ImportSdmlControlResponse;
import org.openapitools.client.model.RecordRegistrationResponse;
import org.openapitools.client.model.RecordRegistrationResponse.ResultEnum;
import org.openapitools.client.model.SdmlControlSettingInfoResponse;

import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.RestConnectFailed;
import com.clustercontrol.sdml.util.SdmlRestClientWrapper;
import com.clustercontrol.sdml.util.SdmlUtilityRestClientWrapper;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.RestClientBeanUtil;
import com.clustercontrol.utility.difference.DiffUtil;
import com.clustercontrol.utility.difference.ResultA;
import com.clustercontrol.utility.difference.desc.sdml.SdmlControlV1Root;
import com.clustercontrol.utility.settings.ConvertorException;
import com.clustercontrol.utility.settings.SettingConstants;
import com.clustercontrol.utility.settings.model.BaseAction;
import com.clustercontrol.utility.settings.platform.action.ObjectPrivilegeAction;
import com.clustercontrol.utility.settings.sdml.SdmlUtilityConstant;
import com.clustercontrol.utility.settings.sdml.conv.SdmlControlV1Conv;
import com.clustercontrol.utility.settings.sdml.xml.Common;
import com.clustercontrol.utility.settings.sdml.xml.SchemaInfo;
import com.clustercontrol.utility.settings.sdml.xml.SdmlControlInfoV1;
import com.clustercontrol.utility.settings.sdml.xml.SdmlControlV1;
import com.clustercontrol.utility.settings.ui.dialog.DeleteProcessDialog;
import com.clustercontrol.utility.settings.ui.dialog.UtilityDialogInjector;
import com.clustercontrol.utility.settings.ui.util.DeleteProcessMode;
import com.clustercontrol.utility.settings.ui.util.ImportProcessMode;
import com.clustercontrol.utility.util.Config;
import com.clustercontrol.utility.util.ImportClientController;
import com.clustercontrol.utility.util.ImportRecordConfirmer;
import com.clustercontrol.utility.util.UtilityDialogConstant;
import com.clustercontrol.utility.util.UtilityManagerUtil;

/**
 * 設定インポートエクスポートのSDML制御設定(1.x)用のActionクラス
 *
 */
public class SdmlControlV1Action extends BaseAction<SdmlControlSettingInfoResponse, SdmlControlInfoV1, SdmlControlV1> {

	protected SdmlControlV1Conv conv;
	protected List<String> objectPrivList = new ArrayList<String>();

	private static final String xmlEncode = "UTF-8";

	public SdmlControlV1Action() throws ConvertorException {
		super();
		conv = new SdmlControlV1Conv();
	}

	@Override
	protected String getActionName() {
		return "SdmlControlV1";
	}

	@Override
	protected List<SdmlControlSettingInfoResponse> getList() throws Exception {
		return SdmlRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName())
				.getSdmlControlSettingListV1(null);
	}

	@Override
	protected void deleteInfo(SdmlControlSettingInfoResponse info) throws Exception {
		SdmlRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName())
				.deleteSdmlControlSettingV1(info.getApplicationId());
	}

	@Override
	protected String getKeyInfoD(SdmlControlSettingInfoResponse info) {
		return info.getApplicationId();
	}

	@Override
	protected SdmlControlV1 newInstance() {
		return new SdmlControlV1();
	}

	@Override
	protected void addInfo(SdmlControlV1 xmlInfo, SdmlControlSettingInfoResponse info) throws Exception {
		xmlInfo.addSdmlControlInfoV1(conv.getXmlInfo(info));
	}

	@Override
	protected void exportXml(SdmlControlV1 xmlInfo, String xmlFile) throws Exception {
		xmlInfo.setCommon(getVersionXml());
		xmlInfo.setSchemaInfo(conv.getSchemaVersion(SchemaInfo.class));
		try (FileOutputStream fos = new FileOutputStream(xmlFile);
				OutputStreamWriter osw = new OutputStreamWriter(fos, xmlEncode);) {
			xmlInfo.marshal(osw);
		}
	}

	@Override
	protected List<SdmlControlInfoV1> getElements(SdmlControlV1 xmlInfo) {
		return Arrays.asList(xmlInfo.getSdmlControlInfoV1());
	}

	@Override
	protected int registElements(SdmlControlV1 xmlInfo) throws Exception {
		int ret = 0;
		ImportSdmlRecordConfirmer confirmer = new ImportSdmlRecordConfirmer(log, xmlInfo.getSdmlControlInfoV1());
		int confirmRet = confirmer.executeConfirm();
		if (confirmRet != SettingConstants.SUCCESS && confirmRet != SettingConstants.ERROR_CANCEL) {
			// 変換エラーならUnmarshalXml扱いで処理打ち切り(キャンセルはキャンセル以前の選択結果を反映するので次に進む)
			log.warn(Messages.getString("SettingTools.UnmarshalXmlFailed"));
			return confirmRet;
		}

		// 更新単位の件数毎にインポートメソッドを呼び出し、結果をログ出力
		// API異常発生時はそこで中断、レコード個別の異常発生時はユーザ選択次第で続行
		ImportSdmlClientController importController = new ImportSdmlClientController(
				log,
				SdmlUtilityConstant.STRING_SDML_CONTROL_V1,
				confirmer.getImportRecDtoList(),
				true);

		ret = importController.importExecute();

		// 重複確認でキャンセルが選択されていたら 以降の処理は行わない
		if (ImportProcessMode.getProcesstype() == UtilityDialogConstant.CANCEL) {
			return SettingConstants.ERROR_INPROCESS;
		}
		// オブジェクト権限のインポート対象を設定
		for (RecordRegistrationResponse rec : importController.getImportSuccessList()) {
			objectPrivList.add(rec.getImportKeyValue());
		}
		return ret;
	}

	@Override
	protected List<String> getImportObjects() {
		return objectPrivList;
	}

	@Override
	protected String getKeyInfoE(SdmlControlInfoV1 info) {
		return info.getApplicationId();
	}

	@Override
	protected SdmlControlV1 getXmlInfo(String filePath) throws Exception {
		return SdmlControlV1.unmarshal(new InputStreamReader(new FileInputStream(filePath), xmlEncode));
	}

	@Override
	protected int checkSchemaVersion(SdmlControlV1 xmlInfo) throws Exception {
		// スキーマのバージョンチェック
		int res = conv.checkSchemaVersion(xmlInfo.getSchemaInfo().getSchemaType(),
				xmlInfo.getSchemaInfo().getSchemaVersion(), xmlInfo.getSchemaInfo().getSchemaRevision());
		SchemaInfo sci = conv.getSchemaVersion(SchemaInfo.class);

		if (!BaseAction.checkSchemaVersionResult(this.getLogger(), res, sci.getSchemaType(), sci.getSchemaVersion(),
				sci.getSchemaRevision())) {
			return SettingConstants.ERROR_SCHEMA_VERSION;
		}
		return 0;
	}

	@Override
	protected SdmlControlInfoV1[] getArray(SdmlControlV1 info) {
		return info.getSdmlControlInfoV1();
	}

	@Override
	protected int compare(SdmlControlSettingInfoResponse info1, SdmlControlSettingInfoResponse info2) {
		return info1.getApplicationId().compareTo(info2.getApplicationId());
	}

	@Override
	protected int sortCompare(SdmlControlInfoV1 info1, SdmlControlInfoV1 info2) {
		return info1.getApplicationId().compareTo(info2.getApplicationId());
	}

	@Override
	protected void setArray(SdmlControlV1 xmlInfo, SdmlControlInfoV1[] infoList) {
		xmlInfo.setSdmlControlInfoV1(infoList);
	}

	@Override
	protected void checkDelete(SdmlControlV1 xmlInfo) throws Exception {
		List<SdmlControlSettingInfoResponse> subList = getList();
		List<SdmlControlInfoV1> xmlElements = new ArrayList<>(getElements(xmlInfo));

		for (SdmlControlSettingInfoResponse mgrInfo : new ArrayList<>(subList)) {
			for (SdmlControlInfoV1 xmlElement : new ArrayList<>(xmlElements)) {
				if (getKeyInfoD(mgrInfo).equals(getKeyInfoE(xmlElement))) {
					subList.remove(mgrInfo);
					xmlElements.remove(xmlElement);
					break;
				}
			}
		}

		if (subList.size() > 0) {
			for (SdmlControlSettingInfoResponse info : subList) {
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
	protected void importObjectPrivilege(List<String> objectList) throws Exception {
		if (ImportProcessMode.isSameObjectPrivilege()) {
			ObjectPrivilegeAction.importAccessExtraction(
					ImportProcessMode.getXmlObjectPrivilege(),
					SdmlUtilityConstant.OBJECT_TYPE_SDML,
					objectList,
					getLogger());
		}
	}

	@Override
	protected boolean diffCheck(SdmlControlV1 xmlInfo1,SdmlControlV1 xmlInfo2, ResultA resultA) {
		// 比較用のクラスに変換する
		SdmlControlV1Root root1 = SdmlControlV1Root.getCopiedInstance(xmlInfo1);
		SdmlControlV1Root root2 = SdmlControlV1Root.getCopiedInstance(xmlInfo2);

		return DiffUtil.diffCheck2(root1, root2, root1.getClass(), resultA);
	}

	/**
	 * レコード確認用クラス
	 */
	private class ImportSdmlRecordConfirmer
			extends ImportRecordConfirmer<SdmlControlInfoV1, ImportSdmlControlRecordRequest, String> {

		public ImportSdmlRecordConfirmer(Logger logger, SdmlControlInfoV1[] importRecDtoList) {
			super(logger, importRecDtoList);
		}

		@Override
		protected ImportSdmlControlRecordRequest convertDtoXmlToRestReq(SdmlControlInfoV1 xmlDto)
				throws HinemosUnknown, InvalidSetting {
			SdmlControlSettingInfoResponse dto = conv.getDTO(xmlDto);
			ImportSdmlControlRecordRequest dtoRec = new ImportSdmlControlRecordRequest();
			dtoRec.setImportData(new AddSdmlControlSettingRequest());
			RestClientBeanUtil.convertBean(dto, dtoRec.getImportData());
			dtoRec.setImportKeyValue(dtoRec.getImportData().getApplicationId());
			return dtoRec;
		}

		@Override
		protected Set<String> getExistIdSet() throws Exception {
			Set<String> retSet = new HashSet<String>();
			List<SdmlControlSettingInfoResponse> infoList = SdmlRestClientWrapper
					.getWrapper(UtilityManagerUtil.getCurrentManagerName()).getSdmlControlSettingListV1(null);
			for (SdmlControlSettingInfoResponse rec : infoList) {
				retSet.add(rec.getApplicationId());
			}
			return retSet;
		}

		@Override
		protected boolean isLackRestReq(ImportSdmlControlRecordRequest restDto) {
			return (restDto == null || restDto.getImportData().getApplicationId() == null
					|| restDto.getImportData().getApplicationId().isEmpty());
		}

		@Override
		protected String getKeyValueXmlDto(SdmlControlInfoV1 xmlDto) {
			return xmlDto.getApplicationId();
		}

		@Override
		protected String getId(SdmlControlInfoV1 xmlDto) {
			return xmlDto.getApplicationId();
		}

		@Override
		protected void setNewRecordFlg(ImportSdmlControlRecordRequest restDto, boolean flag) {
			restDto.setIsNewRecord(flag);
		}
	}

	/**
	 * レコード登録用クラス
	 */
	private static class ImportSdmlClientController extends
			ImportClientController<ImportSdmlControlRecordRequest, ImportSdmlControlResponse, RecordRegistrationResponse> {

		public ImportSdmlClientController(Logger logger, String importInfoName,
				List<ImportSdmlControlRecordRequest> importRecList, boolean displayFailed) {
			super(logger, importInfoName, importRecList, displayFailed);
		}

		@Override
		protected List<RecordRegistrationResponse> getResRecList(ImportSdmlControlResponse importResponse) {
			return importResponse.getResultList();
		};

		@Override
		protected Boolean getOccurException(ImportSdmlControlResponse importResponse) {
			return importResponse.getIsOccurException();
		};

		@Override
		protected String getReqKeyValue(ImportSdmlControlRecordRequest importRec) {
			return importRec.getImportKeyValue();
		};

		@Override
		protected String getResKeyValue(RecordRegistrationResponse responseRec) {
			return responseRec.getImportKeyValue();
		};

		@Override
		protected boolean isResNormal(RecordRegistrationResponse responseRec) {
			return (responseRec.getResult() == ResultEnum.NORMAL);
		};

		@Override
		protected ImportSdmlControlResponse callImportWrapper(List<ImportSdmlControlRecordRequest> importRecList)
				throws HinemosUnknown, InvalidUserPass, InvalidRole, RestConnectFailed {
			ImportSdmlControlRequest reqDto = new ImportSdmlControlRequest();
			reqDto.setRecordList(importRecList);
			reqDto.setRollbackIfAbnormal(ImportProcessMode.isRollbackIfAbnormal());
			return SdmlUtilityRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName())
					.importSdmlControlSettingV1(reqDto);
		}

		@Override
		protected String getRestExceptionMessage(RecordRegistrationResponse responseRec) {
			if (responseRec.getExceptionInfo() != null) {
				return responseRec.getExceptionInfo().getException() + ":"
						+ responseRec.getExceptionInfo().getMessage();
			}
			return null;
		};
	}

	private Common getVersionXml() {
		Hashtable<String, String> ver = Config.getVersion();

		Common com = new Common();
		com.setHinemosVersion(ver.get("hinemosVersion"));
		com.setToolVersion(ver.get("toolVersion"));
		com.setGenerator(ver.get("generator"));
		com.setAuthor(System.getProperty("user.name"));
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		com.setGenerateDate(sdf.format(new Date()));
		com.setRuntimeHost(ver.get("runtimeHost"));
		com.setConnectedManager(ver.get("connectedManager"));

		return com;
	}
}
