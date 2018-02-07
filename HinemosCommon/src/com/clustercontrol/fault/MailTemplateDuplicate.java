/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
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
