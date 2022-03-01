/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rpa.model;

import java.io.Serializable;

import jakarta.persistence.Cacheable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * RPAツールのシナリオ実行コマンドマスタのEntity定義<br>
 * 
 */
@Entity
@Table(name = "cc_rpa_tool_run_command_mst", schema = "setting")
@Cacheable(true)
public class RpaToolRunCommandMst implements Serializable {
	// default serial version id, required for serializable classes.
	private static final long serialVersionUID = 1L;
	/** RPAツールID */
	private String rpaToolId;
	/** RPAツール名 */
	private String rpaToolName;
	/** シナリオ実行コマンド */
	private String execCommand;
	/** プロセス終了コマンド */
	private String destroyCommand;
	/** 実行ファイルパス（デフォルト） */
	private String exeFilepath;
	/** 表示順序 */
	private Integer orderNo;

	/** RPAツールID */
	@Id
	@Column(name = "rpa_tool_id")
	public String getRpaToolId() {
		return this.rpaToolId;
	}

	public void setRpaToolId(String rpaToolId) {
		this.rpaToolId = rpaToolId;
	}

	/** RPAツール名 */
	@Column(name = "rpa_tool_name")
	public String getRpaToolName() {
		return this.rpaToolName;
	}

	public void setRpaToolName(String rpaToolName) {
		this.rpaToolName = rpaToolName;
	}

	/** シナリオ実行コマンド */
	@Column(name = "exec_command")
	public String getExecCommand() {
		return execCommand;
	}

	public void setExecCommand(String execCommand) {
		this.execCommand = execCommand;
	}

	/** プロセス終了コマンド */
	@Column(name = "destroy_command")
	public String getDestroyCommand() {
		return destroyCommand;
	}

	public void setDestroyCommand(String destroyCommand) {
		this.destroyCommand = destroyCommand;
	}

	/** 実行ファイルパス（デフォルト） */
	@Column(name = "exe_filepath")
	public String getExeFilepath() {
		return exeFilepath;
	}

	public void setExeFilepath(String exeFilepath) {
		this.exeFilepath = exeFilepath;
	}

	/** 表示順序 */
	@Column(name = "order_no")
	public Integer getOrderNo() {
		return orderNo;
	}

	public void setOrderNo(Integer orderNo) {
		this.orderNo = orderNo;
	}
}
