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
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.agent.Win32Error;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.platform.win32.WinBase.OVERLAPPED;
import com.sun.jna.platform.win32.WinNT.HANDLE;

public class RandomAccessFileWin {
	private static Log m_log = LogFactory.getLog(RandomAccessFileWin.class);

	public interface kernel32 extends Library {
		kernel32 INSTANCE = (kernel32) Native.loadLibrary("kernel32", kernel32.class);

		// CreateFile
		public static final int GENERIC_WRITE = 0x40000000;
		public static final int GENERIC_READ = 0x80000000;
		public static final int FILE_SHARE_READ = 0x1;
		public static final int FILE_SHARE_WRITE = 0x2;
		public static final int FILE_SHARE_DELETE = 0x4;
		public static final int CREATE_NEW = 1;
		public static final int CREATE_ALWAYS = 2;
		public static final int OPEN_EXISTING = 3;
		public static final int OPEN_ALWAYS = 4;
		public static final int TRUNCATE_EXISTING = 5;
		public static final int FILE_ATTRIBUTE_READONLY = 0x1;
		public static final int FILE_ATTRIBUTE_HIDDEN = 0x2;
		public static final int FILE_ATTRIBUTE_SYSTEM = 0x4;
		public static final int FILE_ATTRIBUTE_DIRECTORY = 0x10;
		public static final int FILE_ATTRIBUTE_ARCHIVE = 0x20;
		public static final int FILE_ATTRIBUTE_NORMAL = 0x80;

		// seek, getFilePointer
		public static final int FILE_BEGIN = 0;
		public static final int FILE_CURRENT = 1;
		public static final int FILE_END = 2;

		int GetLastError();

		/*
		VOID SetLastError(
				  DWORD dwErrCode	// スレッドごとのエラーコード
				);
		 */
		void SetLastError(
				int dwErrCode
				);


		/*
		HANDLE CreateFile(
				  LPCTSTR lpFileName,						  // ファイル名
				  DWORD dwDesiredAccess,					  // アクセスモード
				  DWORD dwShareMode,						  // 共有モード
				  LPSECURITY_ATTRIBUTES lpSecurityAttributes, // セキュリティ記述子
				  DWORD dwCreationDisposition,				  // 作成方法
				  DWORD dwFlagsAndAttributes,				  // ファイル属性
				  HANDLE hTemplateFile						  // テンプレートファイルのハンドル
				);
		 */
		HANDLE CreateFileA(
				byte[] lpFileName,
				int dwDesiredAccess,
				int dwShareMode,
				byte[] lpSecurityAttributes,
				int dwCreationDisposition,
				int dwFlagsAndAttributes,
				HANDLE hTemplateFile
				);
		HANDLE CreateFileW(
				byte[] lpFileName,
				int dwDesiredAccess,
				int dwShareMode,
				byte[] lpSecurityAttributes,
				int dwCreationDisposition,
				int dwFlagsAndAttributes,
				HANDLE hTemplateFile
				);

		/*
		BOOL ReadFile(
		HANDLE hFile,				 // ファイルのハンドル
		LPVOID lpBuffer,			 // データバッファ
		DWORD nNumberOfBytesToRead,	 // 読み取り対象のバイト数
		LPDWORD lpNumberOfBytesRead, // 読み取ったバイト数
		LPOVERLAPPED lpOverlapped	 // オーバーラップ構造体のバッファ
		);
		 */
		boolean ReadFile(
				HANDLE hFile,
				byte[] lpBuffer,
				int nNumberOfBytesToRead,
				byte[] lpNumberOfBytesRead,
				OVERLAPPED lpOverlapped
				);

		/*
		DWORD GetFileSize(
				  HANDLE hFile,			  // ファイルのハンドル
				  LPDWORD lpFileSizeHigh  // ファイルサイズの上位ワード
				);
		 */
		long GetFileSize(
				HANDLE hFile,
				byte[] lpFileSizeHigh
				);

		/*
		DWORD SetFilePointer(
				  HANDLE hFile,				   // ファイルのハンドル
				  LONG lDistanceToMove,		   // ポインタを移動するべきバイト数
				  PLONG lpDistanceToMoveHigh,  // ポインタを移動するべきバイト数
				  DWORD dwMoveMethod		   // 開始点
				);
		 */
		long SetFilePointer(
				HANDLE hFile,
				long lDistanceToMove,
				byte[] lpDistanceToMoveHigh,
				int dwMoveMethod
				);

		/*
		BOOL CloseHandle(
				  HANDLE hObject   // オブジェクトのハンドル
				);
		 */
		boolean CloseHandle(
				HANDLE hObject
				);
	}

	private HANDLE hFile = null;
	private long nPos = 0;

	public RandomAccessFileWin(File file, String mode) throws FileNotFoundException {
		initFileHandle(file.getAbsolutePath(), mode);
	}

	public RandomAccessFileWin(String name, String mode) throws FileNotFoundException {
		initFileHandle(name, mode);
	}

	private void initFileHandle(String name, String mode) throws FileNotFoundException {
		byte[] lpFileNameTmp;
		String charsetName = "MS932";
		try {
			lpFileNameTmp = name.getBytes(charsetName);
		} catch (UnsupportedEncodingException e) {
			throw new FileNotFoundException(charsetName + ", " + e.getMessage());
		}

		byte[] lpFileName = new byte[lpFileNameTmp.length + 1];
		for (int i = 0; i < lpFileNameTmp.length; i++) {
			lpFileName[i] = lpFileNameTmp[i];
		}
		lpFileName[lpFileNameTmp.length] = 0;

		int dwDesiredAccess = kernel32.GENERIC_READ;
		int dwShareMode =
				kernel32.FILE_SHARE_READ | kernel32.FILE_SHARE_WRITE | kernel32.FILE_SHARE_DELETE;
		int dwCreationDisposition = kernel32.OPEN_EXISTING;
		int dwFlagsAndAttributes = kernel32.FILE_ATTRIBUTE_NORMAL;
		HANDLE hTemplateFile = null;
		kernel32.INSTANCE.SetLastError(0);
		hFile = kernel32.INSTANCE.CreateFileA(lpFileName, dwDesiredAccess, dwShareMode,
				null, dwCreationDisposition, dwFlagsAndAttributes, hTemplateFile);
		int error = kernel32.INSTANCE.GetLastError();
		if (error == 2) {
			throw new FileNotFoundException (name + " is not found");
		} else if (error != 0) {
			throw new FileNotFoundException (name + " error=" + Win32Error.getMessage(error));
		}
		// System.out.println("handle=" + hFile.hashCode() + ", " + hFile.toString() +
		//		", " + hFile.nativeType());
		nPos = 0;
	}

	public long length() throws IOException {
		byte[] lpFileSizeHigh = new byte[4];
		kernel32.INSTANCE.SetLastError(0);
		long length = kernel32.INSTANCE.GetFileSize(hFile, lpFileSizeHigh);
		int error = kernel32.INSTANCE.GetLastError();
		if (error != 0) {
			throw new IOException ("length error=" + Win32Error.getMessage(error));
		}
		long highLength = dword2int(lpFileSizeHigh);

		return length + (highLength << 32);
	}

	public void seek(long pos) throws IOException {
		kernel32.INSTANCE.SetLastError(0);
		ByteBuffer byteBuffer = ByteBuffer.allocate(8);
		byte[] posArray = byteBuffer.putLong(pos).array();
		byte[] iPosLowArray = new byte[4];
		for (int i = 0; i < iPosLowArray.length; i++) {
			iPosLowArray[i] = posArray[i + 4];
		}
		long retPos = kernel32.INSTANCE.SetFilePointer(hFile, ByteBuffer.wrap(iPosLowArray).getInt(), int2dword((int)(pos >> 32)), kernel32.FILE_BEGIN);
		if (retPos != pos) {
			int error = kernel32.INSTANCE.GetLastError();
			throw new IOException ("seek pos=" + pos + ", retPos=" + retPos + ", error=" + Win32Error.getMessage(error));
		}
		int error = kernel32.INSTANCE.GetLastError();
		if (error != 0) {
			throw new IOException ("seek error=" + Win32Error.getMessage(error));
		}
		nPos = pos;
		m_log.trace("seek nPos=" + nPos);
	}

	public long getFilePointer() {
		/*
		 * 環境によっては、SetFilePointerが正常に動作しない場合がある。
		 * そのため、nPosを利用して、ポインタを保持する。
		kernel32.INSTANCE.SetLastError(0);
		long pos = kernel32.INSTANCE.SetFilePointer(hFile, 0, null, kernel32.FILE_CURRENT);
		int error = kernel32.INSTANCE.GetLastError();
		if (error != 0 || pos == -1) {
			throw new IOException ("seek error=" + Win32Error.getMessage(error));
		}
		m_log.debug("Win32API: h=" + hFile + " getFilePointer() pos=" + pos);
		return pos;
		 */
		m_log.trace("getFilePointer nPos=" + nPos);
		return nPos;
	}

	public int read(byte[] b) throws IOException {
		int nNumberOfBytesToRead = b.length;
		byte[] lpNumberOfBytesRead = new byte[4]; // 読み込んだ文字数
		kernel32.INSTANCE.SetLastError(0);
		kernel32.INSTANCE.ReadFile(
				hFile, b, nNumberOfBytesToRead, lpNumberOfBytesRead, null);
		int error = kernel32.INSTANCE.GetLastError();
		if (error != 0) {
			throw new IOException ("read error=" + Win32Error.getMessage(error));
		}

		int readbyte = dword2int(lpNumberOfBytesRead);
		if (readbyte == 0) {
			/*
			 * 戻り値が 0 以外で、実際に読み取ったバイト数が 0 の場合、
			 * 読み取り操作を開始する時点でファイルポインタがファイルの終わり（EOF）を超えていたことを示します。
			 */
			return -1;
		}
		m_log.trace("read nPos=" + nPos + ", readbyte=" + readbyte);
		nPos += readbyte;
		return readbyte;
	}

	public void close() throws IOException {
		kernel32.INSTANCE.SetLastError(0);
		kernel32.INSTANCE.CloseHandle(hFile);
		int error = kernel32.INSTANCE.GetLastError();
		if (error != 0) {
			throw new IOException ("close error=" + Win32Error.getMessage(error));
		}
		hFile = null;
		nPos = 0;
	}

	private int dword2int (byte[] b) {
		int n = (b[3] & 0xff);
		n = ((n << 8) + (b[2] & 0xff));
		n = ((n << 8) + (b[1] & 0xff));
		n = ((n << 8) + (b[0] & 0xff));
		return n;
	}

	private byte[] int2dword (int i) {
		byte[] b = new byte[4];
		b[0] = (byte)(i & 0xff);
		b[1] = (byte) ((i >>> 8) & 0xff);
		b[2] = (byte) ((i >>> 16) & 0xff);
		b[3] = (byte) ((i >>> 24) & 0xff);
		return b;
	}
}
