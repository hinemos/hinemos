/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.activation.DataSource;

/**
 * InputStream から DataHandler を取得するための DataSource の実装クラス
 * 
 */
public class RestDataSource implements DataSource {

	private InputStream inputStream;
	private String fileName;

	public RestDataSource(InputStream inputStream, String fileName) {
		this.inputStream = inputStream;
		this.fileName = fileName;
	}

	@Override
	public String getContentType() {
		return "*/*";
	}

	@Override
	public InputStream getInputStream() throws IOException {
		return inputStream;
	}

	@Override
	public String getName() {
		return fileName;
	}

	@Override
	public OutputStream getOutputStream() throws IOException {
		throw new UnsupportedOperationException();
	}
}
