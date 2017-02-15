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
