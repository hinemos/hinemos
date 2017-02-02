/*

Copyright (C) 2012 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

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
