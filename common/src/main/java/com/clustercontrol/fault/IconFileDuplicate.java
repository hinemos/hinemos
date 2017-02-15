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
 * アイコンファイルが重複している場合に利用するException
 * @version 3.2.0
 */
public class IconFileDuplicate extends HinemosException {

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
