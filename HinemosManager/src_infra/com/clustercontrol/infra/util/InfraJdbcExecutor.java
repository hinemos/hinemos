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

package com.clustercontrol.infra.util;

import javax.activation.DataHandler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InfraFileTooLarge;
import com.clustercontrol.platform.infra.InfraJdbcExecutorSupport;
import com.clustercontrol.util.HinemosTime;

/**
 * JDBCドライバを用いて、高速にinsertまたはupdateの一括処理を行うクラス
 */
public class InfraJdbcExecutor {
	private static final Log log = LogFactory.getLog(InfraJdbcExecutor.class);
	
	/**
	 * クエリを実行する
	 * @param query insertまたはupdate
	 * @throws InfraFileTooLarge 
	 * @throws Exception 
	 */
	public static void insertFileContent(String fileId, DataHandler handler) throws HinemosUnknown, InfraFileTooLarge {
		long start = HinemosTime.currentTimeMillis();
		
		try {
			InfraJdbcExecutorSupport.execInsertFileContent(fileId, handler);
		} catch (InfraFileTooLarge e) {
			throw e;
		} catch (HinemosUnknown e) {
			throw e;
		}
		long time = HinemosTime.currentTimeMillis() - start;
		String message = String.format("Execute [insertFileContent]: %dms",time);
		if (time > 3000) {
			log.warn(message);
		} else if (time > 1000) {
			log.info(message);
		} else {
			log.debug(message);
		}
	}

	public static String selectFileContent(String fileId, String fileName) throws HinemosUnknown {
		long start = HinemosTime.currentTimeMillis();
		
		try {
			String filePath = InfraJdbcExecutorSupport.execSelectFileContent(fileId, fileName);
			
			long time = HinemosTime.currentTimeMillis() - start;
			String message = String.format("Execute [selectFileContent]: %dms",time);
			if (time > 3000) {
				log.warn(message);
			} else if (time > 1000) {
				log.info(message);
			} else {
				log.debug(message);
			}
			
			return filePath;
		} catch (HinemosUnknown e) {
			throw e;
		}
	}
}
