/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.infra.dto;

import com.clustercontrol.infra.model.InfraModuleInfo;
import com.clustercontrol.rest.annotation.RestItemName;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertIdClassSet;
import com.clustercontrol.rest.annotation.validation.RestValidateInteger;
import com.clustercontrol.rest.annotation.validation.RestValidateString;
import com.clustercontrol.rest.dto.RequestDto;
import com.clustercontrol.util.MessageConstant;

@RestBeanConvertIdClassSet(infoClass = InfraModuleInfo.class, idName = "id")
public abstract class InfraModuleInfoRequest implements RequestDto {
	
	@RestItemName(value = MessageConstant.INFRA_MODULE_ID)
	@RestValidateString(maxLen = 64, minLen = 1)
	private String moduleId;
	
	@RestItemName(value = MessageConstant.INFRA_MODULE_NAME)
	@RestValidateString(maxLen = 256, minLen = 1)
	private String name;
	
	@RestItemName(value = MessageConstant.ORDER_NO)
	@RestValidateInteger(maxVal = Integer.MAX_VALUE, minVal = 0)
	private Integer orderNo;
	
	@RestItemName(value = MessageConstant.VALID_FLG)
	private Boolean validFlg;
	
	// コマンドモジュール：実行コマンドのリターンコードが0以外の場合、後続モジュールを実行しない
	// ファイル配布モジュール：ファイル配置に失敗した場合、後続モジュールを実行しない
	private Boolean stopIfFailFlg;
	
	// コマンドモジュール：実行前にチェックコマンドで確認する
	// ファイル配布モジュール：MD5が同じ場合、再転送しない
	private Boolean precheckFlg;
	
	@RestItemName(value = MessageConstant.INFRA_MODULE_EXEC_RETURN_PARAM_NAME)
	@RestValidateString(maxLen = 64, minLen = 0)
	private String execReturnParamName;

	public InfraModuleInfoRequest() {
	}

	public String getModuleId() {
		return moduleId;
	}

	public void setModuleId(String moduleId) {
		this.moduleId = moduleId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getOrderNo() {
		return orderNo;
	}

	public void setOrderNo(Integer orderNo) {
		this.orderNo = orderNo;
	}

	public Boolean getValidFlg() {
		return validFlg;
	}

	public void setValidFlg(Boolean validFlg) {
		this.validFlg = validFlg;
	}

	public Boolean getStopIfFailFlg() {
		return stopIfFailFlg;
	}

	public void setStopIfFailFlg(Boolean stopIfFailFlg) {
		this.stopIfFailFlg = stopIfFailFlg;
	}

	public Boolean getPrecheckFlg() {
		return precheckFlg;
	}

	public void setPrecheckFlg(Boolean precheckFlg) {
		this.precheckFlg = precheckFlg;
	}

	public String getExecReturnParamName() {
		return execReturnParamName;
	}

	public void setExecReturnParamName(String execReturnParamName) {
		this.execReturnParamName = execReturnParamName;
	}

	@Override
	public String toString() {
		return "InfraModuleInfoRequest [moduleId=" + moduleId + ", name=" + name + ", orderNo=" + orderNo
				+ ", validFlg=" + validFlg + ", stopIfFailFlg=" + stopIfFailFlg + ", precheckFlg=" + precheckFlg
				+ ", execReturnParamName=" + execReturnParamName + "]";
	}
}
