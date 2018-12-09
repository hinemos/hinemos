/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import org.eclipse.swt.browser.Browser;

public class HtmlLoaderUtil {

	public static void load(Browser browser, String htmlFile, String resourcePrefix) {
		browser.setText(getHtmlContent(htmlFile, resourcePrefix));
	}

	private static String getHtmlContent(String url, String resourcePrefix) {
		StringBuilder html = new StringBuilder();
		html.append(getTextFromResource(url, "UTF-8", resourcePrefix));
		inlineScripts(html, resourcePrefix);
		inlineCSS(html, resourcePrefix);
		return html.toString();
	}

	private static String getTextFromResource(String resourceName,
			String charset, String resourcePrefix) {
		try {
			return getTextFromResourceChecked(resourcePrefix + resourceName,
					charset);
		} catch (IOException exception) {
			String message = "Could not read text from resource: "
					+ resourceName;
			throw new IllegalArgumentException(message, exception);
		}
	}

	private static String getTextFromResourceChecked(String resourceName,
			String charset) throws IOException {
		InputStream inputStream = HtmlLoaderUtil.class.getClassLoader()
				.getResourceAsStream(resourceName);
		if (inputStream == null) {
			throw new IllegalArgumentException("Resource not found: "
					+ resourceName);
		}
		try {
			return getTextFromInputStream(inputStream, charset);
		} finally {
			inputStream.close();
		}
	}

	private static String getTextFromInputStream(InputStream inputStream,
			String charset) throws IOException {
		StringBuilder builder = new StringBuilder();
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				inputStream, charset));
		String line = reader.readLine();
		while (line != null) {
			builder.append(line);
			builder.append('\n');
			line = reader.readLine();
		}
		return builder.toString();
	}

	private static void inlineScripts(StringBuilder html, String resourcePrefix) {
		String srcAttrStr = "src=\"./";
		String quotStr = "\"";
		String tagStr = "<script ";
		String closingTagStr = "</script>";
		String newTagStr = "<script type=\"text/javascript\">";
		int offset = html.length();
		while ((offset = html.lastIndexOf(tagStr, offset)) != -1) {
			int closeTag = html.indexOf(closingTagStr, offset);
			int srcAttr = html.indexOf(srcAttrStr, offset);
			if (srcAttr != -1 && srcAttr < closeTag) {
				int srcAttrStart = srcAttr + srcAttrStr.length();
				int srcAttrEnd = html.indexOf(quotStr, srcAttrStart);
				if (srcAttrEnd != -1) {
					String filename = html.substring(srcAttrStart, srcAttrEnd);
					StringBuffer newScriptTag = new StringBuffer();
					newScriptTag.append(newTagStr);
					newScriptTag.append(getTextFromResource(filename, "UTF-8", resourcePrefix));
					newScriptTag.append(closingTagStr);
					html.replace(offset, closeTag + closingTagStr.length(),
							newScriptTag.toString());
				}
			}
			offset--;
		}
	}
	
	private static void inlineCSS(StringBuilder html, String resourcePrefix) {
		String srcAttrStr = "href=\"./";
		String quotStr = "\"";
		String tagStr = "<link ";
		String closingTagStr = "</link>";
		String newTagStr = "<style type=\"text/css\">";
		String newClosingTagStr = "</style>";
		int offset = html.length();
		while ((offset = html.lastIndexOf(tagStr, offset)) != -1) {
			int closeTag = html.indexOf(closingTagStr, offset);
			int srcAttr = html.indexOf(srcAttrStr, offset);
			if (srcAttr != -1 && srcAttr < closeTag) {
				int srcAttrStart = srcAttr + srcAttrStr.length();
				int srcAttrEnd = html.indexOf(quotStr, srcAttrStart);
				if (srcAttrEnd != -1) {
					String filename = html.substring(srcAttrStart, srcAttrEnd);
					StringBuffer newScriptTag = new StringBuffer();
					newScriptTag.append(newTagStr);
					newScriptTag.append(getTextFromResource(filename, "UTF-8", resourcePrefix));
					newScriptTag.append(newClosingTagStr);
					html.replace(offset, closeTag + closingTagStr.length(),
							newScriptTag.toString());
				}
			}
			offset--;
		}
	}
}