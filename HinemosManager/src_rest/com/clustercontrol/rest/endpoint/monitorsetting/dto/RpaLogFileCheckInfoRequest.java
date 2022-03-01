/*
 * 
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.monitorsetting.dto;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.RpaToolMasterNotFound;
import com.clustercontrol.rest.annotation.RestItemName;
import com.clustercontrol.rest.annotation.validation.RestValidateString;
import com.clustercontrol.rest.annotation.validation.RestValidateString.CheckType;
import com.clustercontrol.rest.dto.RequestDto;
import com.clustercontrol.rpa.util.QueryUtil;
import com.clustercontrol.util.MessageConstant;

public class RpaLogFileCheckInfoRequest implements RequestDto {
	/** 環境毎のRPAツールID */
	@RestItemName(MessageConstant.RPA_TOOL_ID)
	@RestValidateString(notNull=true, type=CheckType.ID, minLen=1, maxLen=64)
	private String rpaToolEnvId;
	/** ディレクトリ */
	@RestItemName(MessageConstant.DIRECTORY)
	@RestValidateString(notNull=true, minLen=1, maxLen=1024)
	private String directory;
	/** ファイル名(正規表現) */
	@RestItemName(MessageConstant.FILE_NAME)
	@RestValidateString(notNull=true, minLen=1, maxLen=1024)
	private String fileName;
	/** ファイルエンコーディング */
	@RestItemName(MessageConstant.FILE_ENCODING)
	@RestValidateString(notNull=true, minLen=1, maxLen=32)
	private String fileEncoding;

	public RpaLogFileCheckInfoRequest() {
	}
	public String getDirectory() {
		return directory;
	}
	public void setDirectory(String directory) {
		this.directory = directory;
	}
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public String getFileEncoding() {
		return fileEncoding;
	}
	public void setFileEncoding(String fileEncoding) {
		this.fileEncoding = fileEncoding;
	}
	public String getRpaToolEnvId() {
		return rpaToolEnvId;
	}
	public void setRpaToolEnvId(String rpaToolEnvId) {
		this.rpaToolEnvId = rpaToolEnvId;
	}

	@Override
	public void correlationCheck() throws InvalidSetting {
		// 正規表現チェック
		try {
			Pattern.compile(getFileName());
		} catch (PatternSyntaxException  ex) {
			InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_REGEX_INVALID.getMessage(MessageConstant.FILE_NAME.getMessage()));
			throw e;
		}
		
		// RPAツールIDの存在チェック
		try {
			QueryUtil.getRpaToolEnvMstPK(rpaToolEnvId);
		} catch (RpaToolMasterNotFound ex) {
			InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_NOT_FOUND.getMessage(
					MessageConstant.RPA_TOOL_ID.getMessage(), rpaToolEnvId));
			throw e;
		}
		
	}
}