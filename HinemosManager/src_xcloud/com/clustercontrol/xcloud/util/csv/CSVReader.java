/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.util.csv;

import java.io.Closeable;
import java.io.IOException;
import java.io.Reader;
import java.util.List;

public class CSVReader implements Closeable {
	public CSVReader(Reader reader) {
	}
	public List<String[]> readAll() throws IOException {
		throw new UnsupportedOperationException();
	}
	public String[] readNext() throws IOException {
		throw new UnsupportedOperationException();
	}
	public void close() throws IOException{
	}
}
