/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
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
