/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.fault;

/**
 * アイコンファイルが重複している場合に利用するException
 * @version 3.2.0
 */
public class IconFileDuplicate extends HinemosDuplicate {

	private static final long serialVersionUID = 1L;
	private String m_filename = null;

	/**
	 * IconFileDuplicateExceptionコンストラクタ
	 */
	public IconFileDuplicate() {
		super();
	}

	/**
	 * IconFileDuplicateExceptionコンストラクタ
	 * @param messages
	 */
	public IconFileDuplicate(String messages) {
		super(messages);
	}

	/**
	 * IconFileDuplicateExceptionコンストラクタ
	 * @param e
	 */
	public IconFileDuplicate(Throwable e) {
		super(e);
	}

	/**
	 * IconFileDuplicateExceptionコンストラクタ
	 * @param messages
	 * @param e
	 */
	public IconFileDuplicate(String messages, Throwable e) {
		super(messages, e);
	}

	public String getFilename() {
		return m_filename;
	}

	public void setFilename(String filename) {
		m_filename = filename;
	}
}
