/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CsvUtil {
	public static final char SEPARATOR = ',';
	public static final char QUOTE_CHARACTER = '"';
	public static final String LINE_END = "\n";

	public final static List<String[]> parseCsv(BufferedReader reader) throws IOException {
		try {
			List<String[]> result = new ArrayList<>();
			
			String[] values = null;
			while ((values = parseNextCsvLine(reader)) != null) {
				result.add(values);
			}
			return result;
		} finally {
			reader.close();
		}
	}

	public final static String[] parseNextCsvLine(BufferedReader reader) throws IOException {
		String pending = null;
		String[] values = null;
		while (true) {
			String nextLine = reader.readLine();
			if (nextLine == null) {
				if (pending != null) {
					String[] t = new String[values.length + 1];
					System.arraycopy(values, 0, t, 0, values.length);
					t[values.length] = pending;
					values = t;
				}
				return values;
			} else {
				CsvParseResult parseResult = parsetCsvLine(nextLine, pending);
				
				if (pending != null) {
					String[] t = new String[values.length + parseResult.values.length];
					System.arraycopy(values, 0, t, 0, values.length);
					System.arraycopy(parseResult.values, 0, t, values.length, parseResult.values.length);
					values = t;
				} else {
					values = parseResult.values;
				}
				
				pending = parseResult.pending;
				
				if (pending == null) {
					return values;
				}
			}
		}
	}
	
	public static class CsvParseResult {
		public CsvParseResult(String[] values, String pending) {
			this.values = values;
			this.pending = pending;
		}
		public final String[] values;
		public final String pending;

		@Override
		public String toString() {
			return "CsvParseResult [values=" + Arrays.toString(values) + ", pending=" + pending + "]";
		}
	}

	public final static CsvParseResult parsetCsvLine(String csvLine, String pending) throws IOException {
		List<String> result = new ArrayList<String>();

		boolean inQuotes = false;
		StringBuilder sb = new StringBuilder(128);
		if (pending != null) {
			sb.append(pending);
			sb.append(LINE_END);
			pending = null;
			inQuotes = true;
		}

		for (int i = 0; i < csvLine.length(); ++i) {
			char c = csvLine.charAt(i);
			if (inQuotes) {
				if (c == QUOTE_CHARACTER) {
					if (csvLine.length() > (i + 1) && csvLine.charAt(i + 1) == QUOTE_CHARACTER) {
						sb.append(QUOTE_CHARACTER);
						++i;
					} else {
						inQuotes = false;
					}
				} else {
					sb.append(csvLine.charAt(i));
				}
			} else {
				if (c == QUOTE_CHARACTER) {
					inQuotes = true;
				} else if (c == SEPARATOR) {
					result.add(sb.toString());
					sb.setLength(0);
				} else {
					sb.append(csvLine.charAt(i));
				}
			}
		}

		if (inQuotes) {
			pending = sb.toString();
		} else {
			result.add(sb.toString());
		}

		return new CsvParseResult(result.stream().toArray(String[]::new), pending);
	}

	public static void writeCsvLine(PrintWriter pw, String[] nextLine) {
		if (nextLine == null)
			return;

		StringBuilder line = new StringBuilder(128);
		for (int i = 0; i < nextLine.length; ++i) {
			if (i != 0) {
				line.append(SEPARATOR);
			}

			String nextValue = nextLine[i];
			if (nextValue == null)
				continue;

			line.append(QUOTE_CHARACTER);
			if (nextValue.indexOf(QUOTE_CHARACTER) != -1) {
				StringBuilder value = new StringBuilder(128);
				for (int j = 0; j < nextValue.length(); ++j) {
					char nextChar = nextValue.charAt(j);
					if (nextChar == QUOTE_CHARACTER) {
						value.append(QUOTE_CHARACTER).append(nextChar);
					} else {
						value.append(nextChar);
					}
				}
				line.append(value);
			} else {
				line.append(nextValue);
			}
			line.append(QUOTE_CHARACTER);
		}

		line.append(LINE_END);
		pw.write(line.toString());
	}
}
