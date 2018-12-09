/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.agent.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class RandomAccessFileWrapper {
	// ロガー
	private static Log m_log = LogFactory.getLog(RandomAccessFileWrapper.class);

	private boolean flag = false;
	private RandomAccessFileWin winFile = null;
	private RandomAccessFile linuxFile = null;

	public RandomAccessFileWrapper(File file, String mode) throws FileNotFoundException {
		initWrapper(file, mode, isWin());
	}

	public RandomAccessFileWrapper(String name, String mode) throws FileNotFoundException {
		initWrapper(name, mode, isWin());
	}

	// 単体試験用
	private RandomAccessFileWrapper(String name, String mode, boolean winFlag) throws FileNotFoundException {
		initWrapper(name, mode, winFlag);
	}

	private void initWrapper(File file, String mode, boolean winFlag) throws FileNotFoundException {
		if (winFlag) {
			winFile = new RandomAccessFileWin(file, mode);
			flag = true;
		} else {
			linuxFile = new RandomAccessFile(file, mode);
			flag = false;
		}
	}

	private void initWrapper(String name, String mode, boolean winFlag) throws FileNotFoundException {
		if (winFlag){
			winFile = new RandomAccessFileWin(name, mode);
			flag = true;
		} else {
			linuxFile = new RandomAccessFile(name, mode);
			flag = false;
		}
	}

	public long length() throws IOException {
		if (flag) {
			return winFile.length();
		} else {
			return linuxFile.length();
		}
	}

	public void seek(long pos) throws IOException {
		if (flag) {
			winFile.seek(pos);
		} else {
			linuxFile.seek(pos);
		}
	}

	public long getFilePointer() throws IOException {
		if (flag) {
			return winFile.getFilePointer();
		} else {
			return linuxFile.getFilePointer();
		}
	}

	public int read(byte[] b) throws IOException {
		if (flag) {
			return winFile.read(b);
		} else {
			return linuxFile.read(b);
		}
	}

	public void close() throws IOException {
		if (flag) {
			winFile.close();
		} else {
			linuxFile.close();
		}
	}

	private boolean isWin() {
		String flagStr = AgentProperties.getProperty("monitor.logfile.random.access.file", "linux");
		if (flagStr != null && "windows".equals(flagStr)) {
			m_log.debug("isWin : mode=windows");
			return true;
		} else {
			m_log.debug("isWin : mode=default");
			return false;
		}
	}

	// ここから下は単体試験
	public static void main(String args[]) {
		boolean verboseFlag = false;
		int success = 0;
		int failure = 0;
		for (int i = 0; i < 1000; i++) {
			// ASCIIのファイル
			if (test("hoge.log", verboseFlag)) {
				success ++;
			} else {
				failure ++;
			}
			System.out.println("========== " + i);

			// ASCIIのファイル
			if (test("out2.log", verboseFlag)) {
				success ++;
			} else {
				failure ++;
			}

			System.out.println("========== " + i);
			// マルチバイトのファイル名(中身もマルチバイト)
			if (test("あいうえおかきくけこ.txt", verboseFlag)) {
				success ++;
			} else {
				failure ++;
			}
			System.out.println("========== " + i);
		}

		System.out.println("");
		System.out.println("end. success=" + success + ", failure=" + failure);
	}

	private static boolean test(String filename, boolean verboseFlag) {
		boolean ret = false;
		try {
			// Thread.sleep(1);
			RandomAccessFileWrapper wrapper = null;
			// java.io.RandomAccessFile
			wrapper = new RandomAccessFileWrapper(filename, "r", false);
			String result1 = wrapper.testSub(verboseFlag);
			if (verboseFlag) {
				System.out.println("------------");
			}
			// com.clustercontrol.agent.log.RandomAccessFileWin  (JNA(win))
			wrapper = new RandomAccessFileWrapper(filename, "r", true);
			String result2 = wrapper.testSub(verboseFlag);

			if (result1.equals(result2)) {
				System.out.println("OK!! " + filename);
				ret = true;
			} else {
				System.out.println("NG!! " + filename);
			}
		} catch (Exception e) {
			System.out.println(e);
			// e.printStackTrace();
		};
		return ret;
	}

	private String testSub(boolean verboseFlag) throws IOException{
		String ret = "";
		String message = null;

		// length
		message = "file length = " + this.length();
		ret += message;
		if (verboseFlag) {
			System.out.println(message);
		}

		// seek
		this.seek(3);

		// read file
		int nNumberOfBytesToRead = 128;
		byte[] lpBuffer = new byte[nNumberOfBytesToRead];
		int readbyte = this.read(lpBuffer);
		message = "readbyte=" + readbyte;
		ret += message;
		if (verboseFlag) {
			System.out.println(message);
		}
		try {
			message = "line=";
			ret += message;
			if (verboseFlag) {
				System.out.println(message);
			}
			message = new String(lpBuffer, "MS932");
			ret += message;
			if (verboseFlag) {
				System.out.println(message);
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		// getFilePointer
		message = "pointer=" + this.getFilePointer();
		ret += message;
		if (verboseFlag) {
			System.out.println(message);
		}

		// read
		readbyte = this.read(lpBuffer);
		message = "readbyte=" + readbyte;
		ret += message;
		if (verboseFlag) {
			System.out.println(message);
		}
		try {
			message = "line=";
			ret += message;
			if (verboseFlag) {
				System.out.println(message);
			}
			message = new String(lpBuffer, "windows-31j");
			ret += message;
			if (verboseFlag) {
				System.out.println(message);
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		// close
		this.close();
		return ret;
	}
}
