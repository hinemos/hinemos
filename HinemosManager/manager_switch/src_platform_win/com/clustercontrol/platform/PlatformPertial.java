/*

Copyright (C) 2017 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.platform;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.util.CommandCreator.PlatformType;

/**
 * 環境差分のある値や処理を格納するクラス（Windows）<BR>
 *
 * @version 6.0.0
 * @since 6.0.0
 */
public class PlatformPertial {
	
	public static final Log log = LogFactory.getLog(PlatformPertial.class);
	
	// コマンド実行時の自身のプラットフォーム種別
	private static final PlatformType SELF_PLATFORM_TYPE = PlatformType.WINDOWS;
	
	public static PlatformType getPlatformType() {
		return SELF_PLATFORM_TYPE;
	}
	
	public static void setupHostname() {
		String hostname = null;
		// hinemos.cfgより値を取得する。未設定ならhostnameを利用する。
		String etcDir = System.getProperty("hinemos.manager.etc.dir");
		if (etcDir != null) {
			File config = new File (etcDir, "hinemos.cfg");
			FileReader fr = null;
			BufferedReader br = null;
			try {
				fr = new FileReader(config.getAbsolutePath());
				br = new BufferedReader(fr);
		 
				String line;
				while ((line = br.readLine()) != null) {
					line = line.trim();
					if (line.trim().startsWith("MANAGER_HOST")) {
						hostname = line.split("=")[1];
						break;
					}
				}
		 	} catch (FileNotFoundException e) {
		 		log.warn("configuration file not found." + config.getAbsolutePath(), e);
		 	} catch (IOException e) {
		 		log.warn("configuration read error." + config.getAbsolutePath(), e);
		 	} finally {
		 		if (br != null) {
	 				try {
						br.close();
					} catch (IOException e) {
					}
	 			}
				
				if (fr != null) {
	 				try {
	 					fr.close();
					} catch (IOException e) {
					}
	 			}
		 	}
		}
		
		if (hostname == null || hostname.length() == 0) {
			Runtime runtime = Runtime.getRuntime();
			Process process = null;
			InputStreamReader is = null;
			BufferedReader br = null;
			
			try {
				process = runtime.exec("hostname");
				
				is = new InputStreamReader(process.getInputStream());
				br = new BufferedReader(is);
				process.waitFor();
				
				if (br != null) {
					hostname = br.readLine();
				}
				
			} catch (IOException | InterruptedException e) {
				log.warn("command execute error.", e);
			} finally {
				if(process != null){
					process.destroy();
				}
				
				if (br != null) {
	 				try {
						br.close();
					} catch (IOException e) {
					}
	 			}
				
				if (is != null) {
	 				try {
						is.close();
					} catch (IOException e) {
					}
	 			}
		 	}
		}

		if (hostname == null) {
			hostname = "";
		}
		
		System.setProperty("hinemos.manager.hostname", hostname);
	}

}