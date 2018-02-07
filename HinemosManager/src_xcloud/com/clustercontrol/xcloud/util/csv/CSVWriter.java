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
import java.io.Writer;
import java.sql.SQLException;
import java.util.List;

public class CSVWriter implements Closeable {
    public CSVWriter(Writer writer) {
    }
    public void writeAll(List<String[]> allLines)  {
    }
    public void writeAll(java.sql.ResultSet rs, boolean includeColumnNames)  throws SQLException, IOException {
    }
    public void writeNext(String[] nextLine) {
    }
    public void flush() throws IOException {
    } 
    public void close() throws IOException {
    }
}