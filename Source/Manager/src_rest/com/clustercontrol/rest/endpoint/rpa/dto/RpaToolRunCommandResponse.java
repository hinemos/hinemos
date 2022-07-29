/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.rpa.dto;

public class RpaToolRunCommandResponse {
	/** RPAツールID */
	private String rpaToolId;
	/** RPAツール名 */
	private String rpaToolName;
	/** 実行ファイルパス */
	private String exeFilepath;
	/** 表示順序 */
	private Integer orderNo;

	public RpaToolRunCommandResponse() {
	}

	/** RPAツールID */
	public String getRpaToolId() {
		return this.rpaToolId;
	}

	public void setRpaToolId(String rpaToolId) {
		this.rpaToolId = rpaToolId;
	}

	/** RPAツール名 */
	public String getRpaToolName() {
		return this.rpaToolName;
	}

	public void setRpaToolName(String rpaToolName) {
		this.rpaToolName = rpaToolName;
	}

	/** 実行ファイルパス */
	public String getExeFilepath() {
		return exeFilepath;
	}

	public void setExeFilepath(String exeFilepath) {
		this.exeFilepath = exeFilepath;
	}

	/** 表示順序 */
	public Integer getOrderNo() {
		return orderNo;
	}

	public void setOrderNo(Integer orderNo) {
		this.orderNo = orderNo;
	}
}
