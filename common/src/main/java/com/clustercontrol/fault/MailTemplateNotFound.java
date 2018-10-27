/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.fault;

/**
 * メールテンプレートが存在しない場合に利用するException
 * @version 3.2.0
 */
public class MailTemplateNotFound extends HinemosException {

	private static final long serialVersionUID = 7178896192545432794L;
	private String m_mailTemplateId = null;

	/**
	 * MailTemplateNotFoundExceptionコンストラクタ
	 */
	public MailTemplateNotFound() {
		super();
	}

	/**
	 * MailTemplateNotFoundExceptionコンストラクタ
	 * @param messages
	 * @param e
	 */
	public MailTemplateNotFound(String messages, Throwable e) {
		super(messages, e);
	}

	/**
	 * MailTemplateNotFoundExceptionコンストラクタ
	 * @param messages
	 */
	public MailTemplateNotFound(String messages) {
		super(messages);
	}

	/**
	 * MailTemplateNotFoundExceptionコンストラクタ
	 * @param e
	 */
	public MailTemplateNotFound(Throwable e) {
		super(e);
	}

	/**
	 * メンテナンスIDを返します。
	 * @return メンテナンスID
	 */
	public String getMailTemplateId() {
		return m_mailTemplateId;
	}

	/**
	 * メンテナンスIDを設定します。
	 * @param mailTemplateId メンテナンスID
	 */
	public void setMailTemplateId(String mailTemplateId) {
		m_mailTemplateId = mailTemplateId;
	}




}
