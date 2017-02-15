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

package com.clustercontrol.plugin.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import com.clustercontrol.plugin.api.HinemosPlugin;

/**
 * JAX-WSによるWEBサービスの初期化(publish)/停止(stop)を制御するオプション用の基底プラグイン.
 * 
 */
public abstract class WebServiceOptionPlugin extends WebServicePlugin implements HinemosPlugin {

	protected boolean isOption(String keyFile, String md5) {
		boolean ret = false;

		String etcdir = System.getProperty("hinemos.manager.etc.dir");
		String keyPath = etcdir + File.separator + keyFile;
		FileReader fileReader = null;

		try {
			fileReader = new FileReader(keyPath);
			char[] cbuf = new char[128];
			if (fileReader.read(cbuf, 0, 128) < 0)
				throw new InternalError();
			String str = new String(cbuf);
			if (str != null && str.startsWith(md5)) {
				log.info("option is activated. (key file : " + keyFile + ") : ");
				ret = true;
			} else {
				log.info("option is not activated. (illegal key file : " + keyFile + ") : ");
			}
		} catch (FileNotFoundException e) {
			log.info("option is not activated. file not found. (" + keyFile + ") : " + e.getMessage());
		} catch (Exception e){
			log.warn("option is not activated. file not readable. (" + keyFile + ")", e);
		} finally {
			try {
				if (fileReader != null) {
					fileReader.close();
				}
			} catch (IOException e) {
			}
		}

		return ret;
	}

}
