/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.util;

import java.io.File;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class FileUtil {
	private static Log log = LogFactory.getLog(FileUtil.class);
	
	public static synchronized void moveAllFiles2OtherDir(String srcPath, String destPath){
		File dir1 = new File(srcPath);
		File dir2 = new File(destPath);
		
		if(!dir1.isDirectory() || !dir2.isDirectory()){
			if(log.isDebugEnabled()){
				log.debug(dir1.getName() + " and/or " + dir2.getName() + " is/are not dirctory. ");
			}
			return;
		}
		File[] files = dir1.listFiles();
		if (null == files){
			log.debug("dir1.listFiles() == null");
			return;
		}
		for(File file: files){
			try {
				File tmpFile = new File(dir2.getAbsolutePath() + File.separator + file.getName());
	            if (file.renameTo(tmpFile)) {
	    			if(log.isDebugEnabled()){
	    				log.debug(file.getName() + " is moved. ");
	    			}
	            } else {
    				log.error(file.getName() + " is not moved. ");
	            }
	        } catch (SecurityException e) {
				log.error(file.getName() + " is not moved. " + e.getMessage());
	        } catch (NullPointerException e) {
				log.error(file.getName() + " is not moved. " + e.getMessage());
	        }
		}
	}

	public static synchronized void moveFile2OtherDir(String srcPath, String destPath){
		File src = new File(srcPath);
		File dest = new File(destPath);
		
		if(src.isDirectory()){
			if(log.isDebugEnabled()){
				log.debug(src.getName() + "is not a file. ");
			}
			return;
		}

		File tmpFile = null;
		if(dest.isDirectory()){
			tmpFile = new File(dest.getAbsolutePath() + File.separator + src.getName());
		} else {
			tmpFile = dest;
		}
	
		try {
            if (src.renameTo(tmpFile)) {
    			if(log.isDebugEnabled()){
    				log.debug(src.getName() + " is moved. ");
    			}
            } else {
				log.error(src.getName() + " is not moved. ");
            }
        } catch (SecurityException e) {
			log.error(src.getName() + " is not moved. " + e.getMessage());
        } catch (NullPointerException e) {
			log.error(src.getName() + " is not moved. " + e.getMessage());
        }
	}

	public static synchronized void addFiles2List(File dir, List<String> fileNameList){
		File[] files = dir.listFiles();
		if (null == files){
			log.debug("dir.listFiles() == null");
			return;
		}
		for(File file: files){
			if(file.isDirectory()){
				addFiles2List(file, fileNameList);
			} else {
				fileNameList.add(file.getAbsolutePath());
			}
		}
	}
}
