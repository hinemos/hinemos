/*

 Copyright (C) 2014 NTT DATA Corporation

 This program is free software; you can redistribute it and/or
 Modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation, version 2.

 This program is distributed in the hope that it will be
 useful, but WITHOUT ANY WARRANTY; without even the implied
 warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 PURPOSE.  See the GNU General Public License for more details.

 */
package com.clustercontrol.infra.bean;

import javax.activation.DataHandler;
import javax.xml.bind.annotation.XmlType;

/**
 * モジュールの実行結果を格納する。
 *
 * @version 5.0.0
 * @since 5.0.0
 */
@XmlType(namespace = "http://infra.ws.clustercontrol.com")
public class ModuleNodeResult {
	private int runCheckType;
	private String facilityId;
	private int result;
	private int statusCode;
	private String message;
	private String oldFilename;
	private DataHandler oldFile;
	private String newFilename;
	private DataHandler newFile;
	private boolean fileDiscarded;
	
	// for jaxws
	public ModuleNodeResult() {
		
	}
	
	public ModuleNodeResult(String facilityId, int result, int statusCode, String message) {
		this.facilityId = facilityId;
		this.result = result;
		this.statusCode = statusCode;
		this.message = message;
	}

	public ModuleNodeResult(int result, int statusCode, String message) {
		this.result = result;
		this.statusCode = statusCode;
		this.message = message;
	}

	public int getRunCheckType() {
		return runCheckType;
	}
	public void setRunCheckType(int runCheckType) {
		this.runCheckType = runCheckType;
	}
	
	public String getFacilityId() {
		return facilityId;
	}
	public void setFacilityId(String facilityId) {
		this.facilityId = facilityId;
	}

	public int getResult() {
		return result;
	}
	public void setResult(int result) {
		this.result = result;
	}

	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}

	public int getStatusCode() {
		return statusCode;
	}

	public void setStatusCode(int statusCode) {
		this.statusCode = statusCode;
	}
	
	public String getOldFilename() {
		return oldFilename;
	}
	
	public void setOldFilename(String oldFilename) {
		this.oldFilename = oldFilename;
	}
	
	public DataHandler getOldFile() {
		return oldFile;
	}
	
	public void setOldFile(DataHandler oldFile) {
		this.oldFile = oldFile;
	}
	
	public String getNewFilename() {
		return newFilename;
	}
	
	public void setNewFilename(String newFilename) {
		this.newFilename = newFilename;
	}
	
	public DataHandler getNewFile() {
		return newFile;
	}
	
	public void setNewFile(DataHandler newFile) {
		this.newFile = newFile;
	}

	public boolean isFileDiscarded() {
		return fileDiscarded;
	}

	public void setFileDiscarded(boolean fileDiscarded) {
		this.fileDiscarded = fileDiscarded;
	}
}
