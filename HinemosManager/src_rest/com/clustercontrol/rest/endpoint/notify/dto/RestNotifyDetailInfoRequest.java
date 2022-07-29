/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.notify.dto;

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.rest.annotation.RestItemName;
import com.clustercontrol.rest.annotation.validation.RestValidateObject;
import com.clustercontrol.rest.dto.RequestDto;
import com.clustercontrol.util.MessageConstant;

public class RestNotifyDetailInfoRequest implements RequestDto {
	
	@RestItemName(value = MessageConstant.NOTIFY_VALID_FLG_INFO)
	@RestValidateObject(notNull = true)
	private Boolean infoValidFlg;

	@RestItemName(value = MessageConstant.NOTIFY_VALID_FLG_WARN)
	@RestValidateObject(notNull = true)
	private Boolean warnValidFlg;

	@RestItemName(value = MessageConstant.NOTIFY_VALID_FLG_CRIT)
	@RestValidateObject(notNull = true)
	private Boolean criticalValidFlg;

	@RestItemName(value = MessageConstant.NOTIFY_VALID_FLG_UNKNOWN)
	@RestValidateObject(notNull = true)
	private Boolean unknownValidFlg;

	private String infoRestAccessId;
	private String warnRestAccessId;
	private String criticalRestAccessId;
	private String unknownRestAccessId;

	public RestNotifyDetailInfoRequest() {
	}

	public Boolean getInfoValidFlg() {
		return infoValidFlg;
	}

	public void setInfoValidFlg(Boolean infoValidFlg) {
		this.infoValidFlg = infoValidFlg;
	}

	public Boolean getWarnValidFlg() {
		return warnValidFlg;
	}

	public void setWarnValidFlg(Boolean warnValidFlg) {
		this.warnValidFlg = warnValidFlg;
	}

	public Boolean getCriticalValidFlg() {
		return criticalValidFlg;
	}

	public void setCriticalValidFlg(Boolean criticalValidFlg) {
		this.criticalValidFlg = criticalValidFlg;
	}

	public Boolean getUnknownValidFlg() {
		return unknownValidFlg;
	}

	public void setUnknownValidFlg(Boolean unknownValidFlg) {
		this.unknownValidFlg = unknownValidFlg;
	}

	public String getInfoRestAccessId() {
		return infoRestAccessId;
	}

	public void setInfoRestAccessId(String infoRestAccessId) {
		this.infoRestAccessId = infoRestAccessId;
	}

	public String getWarnRestAccessId() {
		return warnRestAccessId;
	}

	public void setWarnRestAccessId(String warnRestAccessId) {
		this.warnRestAccessId = warnRestAccessId;
	}

	public String getCriticalRestAccessId() {
		return criticalRestAccessId;
	}

	public void setCriticalRestAccessId(String criticalRestAccessId) {
		this.criticalRestAccessId = criticalRestAccessId;
	}

	public String getUnknownRestAccessId() {
		return unknownRestAccessId;
	}

	public void setUnknownRestAccessId(String unknownRestAccessId) {
		this.unknownRestAccessId = unknownRestAccessId;
	}

	@Override
	public void correlationCheck() throws InvalidSetting {
		//通知が有効なら対応するRESTアクセスIDは必須入力
		if(infoValidFlg != null && infoValidFlg ==  true){
			if(infoRestAccessId == null || infoRestAccessId.isEmpty() ){
				String[] args ={ MessageConstant.INFO.getMessage() };
				throw new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_REST_ACCESS_ID.getMessage(args) );
			}
		}
		if(warnValidFlg != null && warnValidFlg ==  true){
			if(warnRestAccessId == null || warnRestAccessId.isEmpty() ){
				String[] args ={ MessageConstant.WARNING.getMessage() };
				throw new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_REST_ACCESS_ID.getMessage(args) );
			}
		}
		if(criticalValidFlg != null && criticalValidFlg ==  true){
			if(criticalRestAccessId == null || criticalRestAccessId.isEmpty() ){
				String[] args ={ MessageConstant.CRITICAL.getMessage() };
				throw new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_REST_ACCESS_ID.getMessage(args) );
			}
		}
		if(unknownValidFlg != null && unknownValidFlg ==  true){
			if(unknownRestAccessId == null || unknownRestAccessId.isEmpty() ){
				String[] args ={ MessageConstant.UNKNOWN.getMessage() };
				throw new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_REST_ACCESS_ID.getMessage(args) );
			}
		}
	}

}
