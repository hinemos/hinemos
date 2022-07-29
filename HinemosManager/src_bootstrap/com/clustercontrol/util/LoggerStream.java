/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.util;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.logging.Log;

public class LoggerStream extends OutputStream {
	private final Log log;

	public LoggerStream(Log log)
	{
		super();

		this.log = log;
	}

	@Override
	public void write(byte[] b) throws IOException
	{
		String string = new String(b);
		if (!string.trim().isEmpty())
			log.info(string);
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException
	{
		String string = new String(b, off, len);
		if (!string.trim().isEmpty())
			log.info(string);
	}

	@Override
	public void write(int b) throws IOException
	{
		String string = String.valueOf((char) b);
		if (!string.trim().isEmpty())
			log.info(string);
	}
}
