/*

Copyright (C) 2014 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.infra.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.activation.DataHandler;

public class InfraFileUtil {
	public static byte[] getByteArrayFromDataHandler(DataHandler dataHandler) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		dataHandler.writeTo(bos);
		return bos.toByteArray();
	}
}
