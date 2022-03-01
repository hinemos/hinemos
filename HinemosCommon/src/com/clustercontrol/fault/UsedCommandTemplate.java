/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.fault;


/**
 * コマンドテンプレートが通知で利用されている場合に利用するException
 */
public class UsedCommandTemplate extends HinemosUsed {
	private static final long serialVersionUID = 1L;

	/**
	 * コマンド通知テンプレートID
	 */
	private String commandTemplateId = "";

	/**
	 * UsedCommandTemplateコンストラクタ
	 */
	public UsedCommandTemplate() {
		super();
	}

	/**
	 * UsedCommandTemplateコンストラクタ
	 * @param messages
	 */
	public UsedCommandTemplate(String messages) {
		super(messages);
	}

	/**
	 * UsedCommandTemplateコンストラクタ
	 * @param commandTemplateId
	 * @param messages
	 */
	public UsedCommandTemplate(String commandTemplateId, String messages) {
		super(messages);
	}

	public String getCommandTemplateId() {
		return commandTemplateId;
	}

	public void setCommandTemplateId(String commandTemplateId) {
		this.commandTemplateId = commandTemplateId;
	}
}
