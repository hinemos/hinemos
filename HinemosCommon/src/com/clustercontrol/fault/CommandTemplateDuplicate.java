/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.fault;

/**
 * commandTemplateIDが重複している場合に利用するException
 */
public class CommandTemplateDuplicate extends HinemosDuplicate {

	private static final long serialVersionUID = -62246995094283628L;

	private String commandTemplateId = null;

	/**
	 * CommandTemplateDuplicateExceptionコンストラクタ
	 */
	public CommandTemplateDuplicate() {
		super();
	}

	/**
	 * CommandTemplateDuplicateExceptionコンストラクタ
	 * @param messages
	 */
	public CommandTemplateDuplicate(String messages) {
		super(messages);
	}

	/**
	 * CommandTemplateDuplicateExceptionコンストラクタ
	 * @param e
	 */
	public CommandTemplateDuplicate(Throwable e) {
		super(e);
	}

	/**
	 * CommandTemplateDuplicateExceptionコンストラクタ
	 * @param messages
	 * @param e
	 */
	public CommandTemplateDuplicate(String messages, Throwable e) {
		super(messages, e);
	}

	public String getCommandTemplateId() {
		return commandTemplateId;
	}

	public void setCommandTemplateId(String commandTemplateId) {
		this.commandTemplateId = commandTemplateId;
	}
}
