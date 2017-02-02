/*

Copyright (C) 2010 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */
package com.clustercontrol.fault;

/**
 * mailTemplateIDが重複している場合に利用するException
 * @version 3.2.0
 */
public class MailTemplateDuplicate extends HinemosException {

	private static final long serialVersionUID = -5612525221540116629L;
	private String m_mailTemplateId = null;

	/**
	 * MailTemplateDuplicateExceptionコンストラクタ
	 */
	public MailTemplateDuplicate() {
		super();
	}

	/**
	 * MailTemplateDuplicateExceptionコンストラクタ
	 * @param messages
	 */
	public MailTemplateDuplicate(String messages) {
		super(messages);
	}

	/**
	 * MailTemplateDuplicateExceptionコンストラクタ
	 * @param e
	 */
	public MailTemplateDuplicate(Throwable e) {
		super(e);
	}

	/**
	 * MailTemplateDuplicateExceptionコンストラクタ
	 * @param messages
	 * @param e
	 */
	public MailTemplateDuplicate(String messages, Throwable e) {
		super(messages, e);
	}

	public String getMailTemplateId() {
		return m_mailTemplateId;
	}

	public void setMailTemplateId(String mailTemplateId) {
		m_mailTemplateId = mailTemplateId;
	}
}
