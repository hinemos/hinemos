/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.sdml.v1.utility;

import java.util.List;

import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.rest.endpoint.sdml.dto.ImportSdmlControlRecordRequest;
import com.clustercontrol.rest.endpoint.utility.controller.AbstractImportController;
import com.clustercontrol.rest.endpoint.utility.dto.RecordRegistrationResponse;
import com.clustercontrol.rest.endpoint.utility.dto.enumtype.ImportResultEnum;
import com.clustercontrol.rest.util.RestBeanUtil;
import com.clustercontrol.rest.util.RestCommonValitater;
import com.clustercontrol.sdml.model.SdmlControlSettingInfo;
import com.clustercontrol.sdml.v1.SdmlV1Option;
import com.clustercontrol.sdml.v1.session.SdmlControllerBean;

public class ImportSdmlControlV1Controller
		extends AbstractImportController<ImportSdmlControlRecordRequest, RecordRegistrationResponse> {

	public ImportSdmlControlV1Controller(boolean isRollbackIfAbnormal,
			List<ImportSdmlControlRecordRequest> importList) {
		super(isRollbackIfAbnormal, importList);
	}

	@Override
	public RecordRegistrationResponse proccssRecord(ImportSdmlControlRecordRequest importRec) throws Exception {

		RecordRegistrationResponse dtoRecRes = new RecordRegistrationResponse();
		dtoRecRes.setImportKeyValue(importRec.getImportKeyValue());

		RestCommonValitater.checkRequestDto(importRec.getImportData());
		importRec.getImportData().correlationCheck();

		// DTOからINFOへ変換
		SdmlControlSettingInfo infoReq = new SdmlControlSettingInfo();
		RestBeanUtil.convertBean(importRec.getImportData(), infoReq);
		infoReq.setVersion(SdmlV1Option.VERSION);

		// ControllerBean呼び出し
		if (importRec.getIsNewRecord()) {
			// 新規登録
			new SdmlControllerBean().addSdmlControlSetting(infoReq, true);
		} else {
			// 変更
			new SdmlControllerBean().modifySdmlControlSetting(infoReq, true);
		}

		dtoRecRes.setResult(ImportResultEnum.NORMAL);
		return dtoRecRes;
	}

	@Override
	protected RecordRegistrationResponse getRecordResponseInstance() {
		return new RecordRegistrationResponse();
	}

	@Override
	protected void addCallback(JpaTransactionManager jtm) {
		new SdmlControllerBean().addImportSdmlControlSettingCallback(jtm);
	}
}
