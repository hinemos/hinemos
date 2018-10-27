/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.rap.rwt.SingletonUtil;

public class ClientPathUtil {
	private static Log log = LogFactory.getLog( ClientPathUtil.class );
	
	static class StreamReader extends Thread {
		private InputStream is;
		private StringWriter sw= new StringWriter();

		public StreamReader(InputStream is) {
			this.is = is;
		}

		public void run() {
			try {
				int c;
				while ((c = is.read()) != -1)
					sw.write(c);
			}
			catch (IOException e) { 
			}
		}

		public String getResult() {
			return sw.toString();
		}
	}

	public static final String readRegistry(String location, String key){
		try {
			Process process = Runtime.getRuntime().exec("reg query " + location + " /v " + key);
			StreamReader reader = new StreamReader(process.getInputStream());
			reader.start();
			process.waitFor();
			reader.join();
			String output = reader.getResult();

			// Output has the following format:
			// \n<Version information>\n\n<key>\t<registry type>\t<value>
			String sp = "    ";
			if (output.contains("\t")) {
				sp = "\t";
			}

			// Parse out the value
			String[] parsed = output.split(sp);
			return parsed[parsed.length-1].trim();
		} catch (InterruptedException | IOException e) {
			return null;
		}
		catch (Exception e) {
			log.debug(e.getMessage());
			return null;
		}

	}

	public static String getDefaultPath(){
		if (System.getProperty("os.name").toLowerCase().startsWith("windows")) {
			
			String location = String.format("HKLM\\Software\\Hinemos%s\\HinemosWeb", Config.hinemosVersion);
			String value = ClientPathUtil.readRegistry(location, "DataHome");

			log.debug("OS:" +System.getProperty("os.name") +
					"registorykey: " + location + ", value: DataHome" +
					"result: " + value);
			
			value = !value.endsWith(File.separator) ? value + File.separator : value;
			return value + "utility" + File.separator + "tmp";

		} else {
			return "../../utility/tmp";
		}
	}
	
	public static String getDefaultXMLPath(){
		return getDefaultPath() + File.separator + "xml";
	}
	
	public static String getDefaultXMLDiffPath(){
		return getDefaultPath() + File.separator + "diff";
	}
	
	public static ClientPathUtil getInstance(){
		return SingletonUtil.getSessionInstance(ClientPathUtil.class);
	}
	
	private ClientPathUtil() {}
	
	private Map<String, TempPath> tmpPathes = new HashMap<>();
	
	public String getTempPath(String parentPath){
		if(tmpPathes.containsKey(parentPath)){
			return File.separator + tmpPathes.get(parentPath).tmpPath;
		}
		
		return "";
	}
	
	public boolean lock(String parentPath){
		log.debug("tmpPathes=" + tmpPathes + " parentPath=" + parentPath);
		if(!tmpPathes.containsKey(parentPath)){
			TempPath path = new TempPath();
			path.tmpPath = RandomStringUtils.randomAlphanumeric(8);
			path.fullPath = parentPath + File.separator + path.tmpPath;
			path.busyness = true;
			File dirPath = new File(path.fullPath);
			if(!dirPath.exists()){
				if (!dirPath.mkdirs())
					log.warn(String.format("Fail to create Directory. %s", dirPath.getAbsolutePath()));
			}
			tmpPathes.put(parentPath, path);
			return true;
		} else if(!isBussy(parentPath)){
			// 基本ここは通らないはず
			tmpPathes.get(parentPath).busyness = true;
			return true;
		}
		return false;
	}
	
	public boolean unlock(String parentPath){
		log.debug("tmpPathes=" + tmpPathes + " parentPath=" + parentPath);
		if(tmpPathes.containsKey(parentPath) && isBussy(parentPath)){
			File path = new File(tmpPathes.get(parentPath).fullPath);
			delete(path);
			tmpPathes.remove(parentPath);
			return true;
		}
		return false;
	}
	
	public boolean isBussy(String parentPath){
		if(tmpPathes.containsKey(parentPath)){
			return tmpPathes.get(parentPath).busyness;
		}
		return false;
	}
	
	private static class TempPath{
		String tmpPath;
		String fullPath;
		boolean busyness = false;
	}
	
	public synchronized void delete(File file){
		if(file.exists() == false) {
			return;
		}

		if(file.isFile()) {
			if (!file.delete())
				log.warn(String.format("Fail to delete file. %s", file.getAbsolutePath()));
		} else if(file.isDirectory()){
			File[] files = file.listFiles();
			if(files != null){
				for(int i=0; i<files.length; i++) {
					delete( files[i] );
				}
			}
			if (!file.delete())
				log.warn(String.format("Fail to delete Directory. %s", file.getAbsolutePath()));
		}
	}
	
	public void unlockAll(){
		for(TempPath tmpPath: tmpPathes.values()){
			File path = new File(tmpPath.fullPath);
			delete(path);
		}
		tmpPathes.clear();
	}
}
