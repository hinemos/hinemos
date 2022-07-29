/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.fault;

/**
 * commandTemplateIDが存在しない場合に利用するException
 */
public class CommandTemplateNotFound extends HinemosNotFound {

	private static final long serialVersionUID = -27999459006299333L;
	private String commandTemplateID = null;

	/**
	 * CommandTemplateNotFoundExceptionコンストラクタ
	 */
	public CommandTemplateNotFound() {
		super();
	}

	/**
	 * CommandTemplateNotFoundExceptionコンストラクタ
	 * @param messages
	 */
	public CommandTemplateNotFound(String messages) {
		super(messages);
	}

	/**
	 * CommandTemplateNotFoundExceptionコンストラクタ
	 * @param e
	 */
	public CommandTemplateNotFound(Throwable e) {
		super(e);
	}

	/**
	 * CommandTemplateNotFoundExceptionコンストラクタ
	 * @param messages
	 * @param e
	 */
	public CommandTemplateNotFound(String messages, Throwable e) {
		super(messages, e);
	}

	public String getCommandTemplateID() {
		return commandTemplateID;
	}

	public void setCommandTemplateID(String commandTemplateID) {
		this.commandTemplateID = commandTemplateID;
	}
}
