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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Hashtable;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.infra.bean.ModuleNodeResult;
import com.clustercontrol.infra.bean.OkNgConstant;
import com.clustercontrol.infra.model.CommandModuleInfo;
import com.clustercontrol.infra.model.FileTransferModuleInfo;
import com.clustercontrol.maintenance.util.HinemosPropertyUtil;
import com.clustercontrol.util.HinemosTime;
import com.clustercontrol.util.XMLUtil;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

/**
 * Jsch 関連のユーティリティクラス
 *
 * @version 6.0.1
 * @since 5.0.0
 */
public class JschUtil {
	
	private static Log m_log = LogFactory.getLog( JschUtil.class );
	
	private static final int BUFF_SIZE = 1024;
	
	static {
		try {
			// HostKeyチェックを行わない
			Hashtable<String, String> config = new Hashtable<>();
			config.put("StrictHostKeyChecking", "no");
			JSch.setConfig(config);
		} catch (Exception e) {
			m_log.warn("static " + e.getClass().getName() + ", " + e.getMessage());
		}
	}
	
	public static ModuleNodeResult execCommand(String user, String password, String host, int port, int timeout,
			String command, int maxSize, String keypath, String passphrase) {
		
		m_log.info("user=" + user + ", host=" + host + ", port=" + port + ", timeout=" + timeout + ", keypath=" + keypath + ", command=" + command);
		// m_log.debug("password=" + password + ", passphrase=" + passphrase);
		
		Session session = null;
		try {
			JSch jsch=new JSch();
			session = jsch.getSession(user, host, port);
			
			// connect session
			if (keypath != null && 0 < keypath.length()) {
				jsch.addIdentity(keypath, passphrase);
			} else {
				session.setPassword(password);
			}
			session.connect(timeout);
			
			return execCommand(session, command, maxSize, timeout);
		} catch(JSchException e) {
			String msg = e.getClass().getName() + ", " + e.getMessage();
			m_log.warn("execCommand : " + msg);
			return new ModuleNodeResult(OkNgConstant.TYPE_NG, -1, msg);
		} catch(Exception e){
			String msg = e.getClass().getName() + ", " + e.getMessage();
			m_log.warn("execCommand : " + msg, e);
			return new ModuleNodeResult(OkNgConstant.TYPE_NG, -1, msg);
		}
		finally {
			if (session != null) {
				session.disconnect();
			}
		}
	}
	
	private static  ModuleNodeResult execCommand(Session session, String command, int maxSize, int timeout)
			throws IOException, JSchException, HinemosUnknown {
		ChannelExec channel = (ChannelExec)session.openChannel("exec");
		channel.setCommand(command);
		try {
			// チャネル の接続前にストリームを取得しないと、受信内容をロストする可能性がある。
			InputStream stdIn = channel.getInputStream();
			InputStream errIn = channel.getErrStream();

			channel.connect(timeout);

			String std = "out=" + new String(getByteArray(channel, stdIn, maxSize, timeout)).trim();
			
			String err = "err=" + new String(getByteArray(channel, errIn, maxSize, timeout)).trim();
			
			String msg = String.format("command=%s%n", command);
			msg = msg + String.format("exitCode=%d%n", channel.getExitStatus());
			msg = msg + std.substring(0, Math.min(std.length(), (maxSize - msg.length() - 1) - Math.min((maxSize - msg.length() - 1) / 2, err.length()))) + "\n";
			msg = msg + err.substring(0, Math.min(err.length(), (maxSize - msg.length() - 1) / 2));

			return new ModuleNodeResult(channel.getExitStatus() == 0 ? OkNgConstant.TYPE_OK: OkNgConstant.TYPE_NG, channel.getExitStatus(), XMLUtil.ignoreInvalidString(msg));
		}
		finally {
			channel.disconnect();
		}
	}

	private static  String execCommandWithStdOut(Session session, String command, int timeout) throws JSchException, IOException, HinemosUnknown {
		ChannelExec channel = (ChannelExec)session.openChannel("exec");
		channel.setCommand(command);
		try {
			// チャネル の接続前にストリームを取得しないと、受信内容をロストする可能性がある。
			InputStream stdIn = channel.getInputStream();
			channel.connect(timeout);
			return new String(getByteArray(channel, stdIn, BUFF_SIZE, timeout)).trim();
		}
		finally {
			channel.disconnect();
		}
	}
	
	private static byte[] getByteArray(ChannelExec channel, InputStream in, int maxSize, int timeout) throws IOException, HinemosUnknown {
		int pos = 0;
		byte [] buffer = new byte[maxSize];
		boolean loop = true;
		
		long start  = HinemosTime.currentTimeMillis();
		while (loop) {
			while(in.available() > 0 && pos < maxSize) {
				int len = in.read(buffer, pos, maxSize - pos);
				if (len < 0) {
					loop = false;
					break;
				}
				pos += len;
				start  = HinemosTime.currentTimeMillis();
			}
			
			if (pos >= maxSize) {
				loop = false;
			} else if (channel.isClosed()) {
				if (in.available() <= 0) {
					loop = false;
				}
			} else {
				if ((HinemosTime.currentTimeMillis() - start) > timeout)
					throw new HinemosUnknown("Jsch command is spent too much time.");
					
				try { Thread.sleep(500); } catch (Exception ee) {}
			}
		}
		return pos >= maxSize ? buffer: Arrays.copyOfRange(buffer, 0, pos);
	}
	/**
	 * 管理対象からHinemosマネージャにファイルを送付(SCP)
	 * @param user
	 * @param password
	 * @param host
	 * @param port
	 * @param remoteFile
	 * @param localFile
	 * @param destDir
	 * @param owner
	 * @param perm
	 * @param keypath
	 * @param passphrase
	 * @return
	 */
	public static ModuleNodeResult recvFile(String user, String password, String host, int port, int timeout,
			String srcDir, String srcFilename, String dstDir, String dstFilename, 
			String owner, String perm, String keypath, String passphrase) {

		Session session = null;
		ChannelExec channel = null;
		OutputStream out = null;
		InputStream in = null;
		
		try {
			JSch jsch=new JSch();
			session = jsch.getSession(user, host, port);
			
			if (keypath != null && 0 < keypath.length()) {
				jsch.addIdentity(keypath, passphrase);
			} else {
				session.setPassword(password);
			}
			session.connect(timeout);
		
			/**
			 * ここのsrcFilePathには、File.separatorを使用しないこと。
			 * 
			 * File.separatorは、javaが実行されるプラットフォームの情報から選別される。
			 * 環境構築機能はマネージャのプラットフォームで実行されるため、
			 * 宛先のパスにFileTransferModuleInfo.SEPARATOR(File.separator)を
			 * 使用してしまうと不適正なセパレータが使用されてしまう場合がある。
			 * 例えば、マネージャのプラットフォームがWindowsで、対象サーバがLinuxの場合、
			 * Linux用のファイルセパレータを指定したいのに、Windowsのセパレータである「\」を指定してしまう。
			 */
			// exec 'scp -f rfile' remotely
			String srcFilePath = srcDir + "/" + srcFilename;
			String command = "scp -f " + srcFilePath;
			channel = (ChannelExec)session.openChannel("exec");
			channel.setCommand(command);
		
			// get I/O streams for remote scp
			out = channel.getOutputStream();
			in = channel.getInputStream();
		
			channel.connect(timeout);
		
			byte[] buf=new byte[1024];
		
			// send '\0'
			buf[0] = 0;
			out.write(buf, 0, 1);
			out.flush();
			
			String header = "C0644";
			
			while(true){
				ModuleNodeResult ack = checkAck(channel, in, timeout);
				if (ack.getStatusCode() != 'C') {
					return ack;
				}
				// read "C0644 "
				if (in.read(buf, 0, header.length()) != header.length())
					return new ModuleNodeResult(OkNgConstant.TYPE_NG, -1, "don't read \"C0644\"");
		
				long filesize = 0L;
				while(true){
					if(in.read(buf, 0, 1) < 0){
						// error
						break; 
					}
					if(buf[0] == ' '){
						break;
					}
					filesize = filesize * 10L + (long)(buf[0] - '0');
				}

				for (int i = 0;; i++) {
					if (in.read(buf, i, 1) != 1)
						return new ModuleNodeResult(OkNgConstant.TYPE_NG, -1, "read error");
						
					if (buf[i] == (byte) 0x0a) {
						break;
					}
				}

				// send '\0'
				buf[0] = 0;
				out.write(buf, 0, 1);
				out.flush();
		
				// read a content of localFile
				FileOutputStream fos = new FileOutputStream(dstDir + FileTransferModuleInfo.SEPARATOR + dstFilename);
				int foo;
				while(true){
					if(buf.length < filesize) {
						foo = buf.length;
					} else {
						foo = (int)filesize;
					}
					foo = in.read(buf, 0, foo);
					if(foo < 0){
						// error 
						break;
					}
					fos.write(buf, 0, foo);
					filesize -= foo;
					if(filesize == 0L) {
						break;
					}
				}
				fos.close();
				fos=null;
		
				ack = checkAck(channel, in, timeout);
				if (ack.getResult() != OkNgConstant.TYPE_NG) {
					return ack;
				}
		
				// send '\0'
				buf[0] = 0;
				out.write(buf, 0, 1);
				out.flush();
			}
		} catch(JSchException e) {
			String msg = e.getClass().getName() + ", " + e.getMessage();
			m_log.warn("recvFile : " + msg);
			return new ModuleNodeResult(OkNgConstant.TYPE_NG, -1, msg);
		} catch(Exception e){
			String msg = e.getClass().getName() + ", " + e.getMessage();
			m_log.warn("recvFile : " + msg, e);
			return new ModuleNodeResult(OkNgConstant.TYPE_NG, -1, msg);
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {}
			}
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {}
			}
			if (channel != null) {
				channel.disconnect();
			}
			if (session != null) {
				session.disconnect();
			}
		}
	}
	
	/**
	 * Hinemosマネージャから管理対象にファイルを送付(SCP)
	 * @param user
	 * @param password
	 * @param host
	 * @param port
	 * @param srcFilename
	 * @param dstDir
	 * @param owner
	 * @param perm
	 * @param isBackupIfExistFlg
	 * @param notTransferIfSameMd5 
	 * @return
	 */
	public static ModuleNodeResult sendFile(String user, String password, String host, int port, int timeout,
			String srcDir, String srcFilename, String dstDir, String dstFilename,
			String owner, String perm, boolean isBackupIfExistFlg, String keypath, String passphrase) {
		Session session = null;
		ChannelExec channel = null;
		OutputStream out = null;
		InputStream in = null;
		
		m_log.info("srcDir=" + srcDir + ", srcFile=" + srcFilename + 
				", destDir=" + dstDir + ", dstFile=" + dstFilename);
		
		try{
			File srcFile = new File(srcDir + FileTransferModuleInfo.SEPARATOR + srcFilename);

			JSch jsch = new JSch();
			session = jsch.getSession(user, host, port);
			if (keypath != null && keypath.length() > 0) {
				jsch.addIdentity(keypath, passphrase);
			} else {
				session.setPassword(password);
			}
			session.connect(timeout);
			/**
			 * ここのdstFilePathには、File.separatorを使用しないこと。
			 * 
			 * File.separatorは、javaが実行されるプラットフォームの情報から選別される。
			 * 環境構築機能はマネージャのプラットフォームで実行されるため、
			 * 宛先のパスにFileTransferModuleInfo.SEPARATOR(File.separator)を
			 * 使用してしまうと不適正なセパレータが使用されてしまう場合がある。
			 * 例えば、マネージャのプラットフォームがWindowsで、対象サーバがLinuxの場合、
			 * Linux用のファイルセパレータを指定したいのに、Windowsのセパレータである「\」を指定してしまう。
			 */
			String dstFilePath = dstDir + "/" + dstFilename;

			if (isBackupIfExistFlg) {
				// ファイルが存在する場合は、
				ModuleNodeResult result = execCommand2(session, "test -e %s", timeout, dstFilePath);
				// mvコマンドで複製する
				if (result.getResult() == OkNgConstant.TYPE_OK) {
					Calendar now = HinemosTime.getCalendarInstance(); 
					result = execCommand2(
							session,
							"mv %s %s.%04d%02d%02d%02d%02d%02d",
							timeout,
							dstFilePath,
							dstFilePath,
							now.get(Calendar.YEAR),
							now.get(Calendar.MONTH) + 1,
							now.get(Calendar.DAY_OF_MONTH),
							now.get(Calendar.HOUR_OF_DAY),
							now.get(Calendar.MINUTE),
							now.get(Calendar.SECOND));
					if (result.getResult() != OkNgConstant.TYPE_OK) {
						return result;
					}
				}
			}
			
			// ファイルの送信
			ModuleNodeResult ack = null;
			channel = (ChannelExec)session.openChannel("exec");
			channel.setCommand(String.format("scp -t %s", dstFilePath));

			// チャネル の接続前にストリームを取得しないと、受信内容をロストする可能性がある。
			out = channel.getOutputStream();
			in = channel.getInputStream();

			channel.connect(timeout);
			ack = checkAck(channel, in, timeout);
			if (ack.getResult() != OkNgConstant.TYPE_OK) {
				return ack;
			}
			// send "C0644 filesize filename", where filename should not include '/'
			String C0644 = String.format("C0644 %d %s%n", srcFile.length(), srcFile.getName());
			out.write(C0644.getBytes());
			out.flush();
			ack = checkAck(channel, in, timeout);
			if (ack.getResult() != OkNgConstant.TYPE_OK) {
				return ack;
			}
			// send a content of lfile
			try (FileInputStream fis = new FileInputStream(srcFile)) {
				byte[] buf = new byte[1024];
				while(true){
					int len = fis.read(buf, 0, buf.length);
					if (len <= 0) {
						break;
					}
					out.write(buf, 0, len);
				}
			}
			// send '\0'
			out.write(new byte[]{0}, 0, 1);
			out.flush();
			ack = checkAck(channel, in, timeout);
			if (ack.getResult() != OkNgConstant.TYPE_OK) {
				return ack;
			}

			// 権限変更
			if (perm != null && !perm.isEmpty()) {
				ModuleNodeResult result = execCommand2(session, "chmod %s %s", timeout, perm, dstFilePath);
				if (result.getResult() != OkNgConstant.TYPE_OK)
					return result;
			}

			// ファイルオーナー変更
			if (owner != null && !owner.isEmpty()) {
				ModuleNodeResult result = execCommand2(session, "chown %s %s", timeout, owner, dstFilePath, timeout);
				if (result.getResult() != OkNgConstant.TYPE_OK)
					return result;
			}
			
			// メッセージ変更
			if (ack.getResult() == OkNgConstant.TYPE_OK) {
				ack.setMessage(dstFilename + " was transfered. (" + dstDir + ")");
			}
			
			return ack;
		} catch(JSchException e) {
			String msg = e.getClass().getName() + ", " + e.getMessage();
			m_log.warn("sendFile : " + msg);
			return new ModuleNodeResult(OkNgConstant.TYPE_NG, -1, msg);
		} catch(Exception e){
			String msg = e.getClass().getName() + ", " + e.getMessage();
			m_log.warn("sendFile : " + msg, e);
			return new ModuleNodeResult(OkNgConstant.TYPE_NG, -1, msg);
		}
		finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {}
			}
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {}
			}
			if (channel != null) {
				channel.disconnect();
			}
			if (session != null) {
				session.disconnect();
			}
		}
	}

	public static ModuleNodeResult isSameMd5(String user, String password, String host, int port, int timeout,
			String srcDir, String srcFilename, String dstDir, String dstFilename, String keypath, String passphrase) {
		Session session = null;
		ModuleNodeResult ret = new ModuleNodeResult(OkNgConstant.TYPE_NG, -1, "unknown");
		
		FileInputStream srcFis = null;
		try {
			JSch jsch = new JSch();
			session = jsch.getSession(user, host, port);
			if (keypath != null && keypath.length() > 0) {
				jsch.addIdentity(keypath, passphrase);
			} else {
				session.setPassword(password);
			}
			session.connect(timeout);

			File srcFile = new File(srcDir + FileTransferModuleInfo.SEPARATOR + srcFilename);
			srcFis = new FileInputStream(srcFile);
			String srcMd5 = DigestUtils.md5Hex(srcFis);
			m_log.debug("srcMd5: " + srcMd5);
			/**
			 * ここのdstFilePathには、File.separatorを使用しないこと。
			 * 
			 * File.separatorは、javaが実行されるプラットフォームの情報から選別される。
			 * 環境構築機能はマネージャのプラットフォームで実行されるため、
			 * 宛先のパスにFileTransferModuleInfo.SEPARATOR(File.separator)を
			 * 使用してしまうと不適正なセパレータが使用されてしまう場合がある。
			 * 例えば、マネージャのプラットフォームがWindowsで、対象サーバがLinuxの場合、
			 * Linux用のファイルセパレータを指定したいのに、Windowsのセパレータである「\」を指定してしまう。
			 */
			String dstFilePath = dstDir + "/" + dstFilename;
			String md5command = HinemosPropertyUtil.getHinemosPropertyStr("infra.command.md5", "md5sum \"%s\" | awk '{print $1}'");
			String dstMd5 = execCommandWithStdOut(session, String.format(md5command, dstFilePath), timeout);
			m_log.debug("dstMd5: " + dstMd5);
			
			if(srcMd5.equals(dstMd5)) {
				ret.setResult(OkNgConstant.TYPE_OK);
				ret.setStatusCode(0);
				ret.setMessage("MD5s are same");
			} else {
				ret.setResult(OkNgConstant.TYPE_NG);
				ret.setStatusCode(0);
				ret.setMessage("MD5s differ : src=" + srcMd5 + ", dst=" + dstMd5);
			}
		} catch(JSchException e) {
			String msg = e.getClass().getName() + ", " + e.getMessage();
			m_log.warn("isSameMd5 : " + msg);
			return new ModuleNodeResult(OkNgConstant.TYPE_NG, -1, msg);
		} catch(Exception e){
			String msg = e.getClass().getName() + ", " + e.getMessage();
			m_log.warn("isSameMd5 : " + msg, e);
			return new ModuleNodeResult(OkNgConstant.TYPE_NG, -1, msg);
		} finally {
			if (srcFis != null) {
				try {
					srcFis.close();
				} catch (IOException e) {
				}
			}
			if (session != null) {
				session.disconnect();
			}
		}
		
		return ret;
		
	}

	private static ModuleNodeResult execCommand2(Session session, String format, int timeout, Object...args) throws IOException, HinemosUnknown, JSchException {
		return JschUtil.execCommand(session, String.format(format, args), BUFF_SIZE, timeout);
	}
	
	private static ModuleNodeResult checkAck(ChannelExec channel, InputStream in, int timeout) throws IOException, HinemosUnknown {
		byte[] b = JschUtil.getByteArray(channel, in, 1, timeout);
		if (b.length != 1) {
			return new ModuleNodeResult(OkNgConstant.TYPE_NG, -1, "failed to execute scp");
		}
		
		int ret = b[0];

		// b may be 0 for success,
		//          1 for error,
		//          2 for fatal error,
		//          -1
		if (ret == 0) {
			return new ModuleNodeResult(OkNgConstant.TYPE_OK, ret, "exitCode=" + ret);
		} else if (ret != 1 && ret != 2) {
			return new ModuleNodeResult(OkNgConstant.TYPE_OK, ret, "exitCode=" + ret);
		} else {
			// エラーの場合は切断する
			channel.disconnect();
			String message = String.format("exitCode=%d%nerr = %s", ret,
					new String(JschUtil.getByteArray(channel, in, BUFF_SIZE, timeout)));
			m_log.warn("checkAck : " + message);
			return new ModuleNodeResult(OkNgConstant.TYPE_NG, ret, message);
		}
	}
	
	public static void main(String args[]) {
		ModuleNodeResult result = null;
		String user = "root";
		String pass = "";
		String keypath = null;
		String passphrase = "";
		String host = "";
		String orgFilename = "build.xml"; // HinemosManagerプロジェクト直下のファイル名
		int port = 22;
		int timeout = 5 * 1000;
		
		// コマンド実行
		System.out.println(HinemosTime.getDateString() + " === exec command ===");
		result = execCommand(user, pass, host, port, timeout, "hostname", CommandModuleInfo.MESSAGE_SIZE, keypath, passphrase);
		System.out.println(HinemosTime.getDateString() + " result, ok/ng=" + (result.getResult() == OkNgConstant.TYPE_OK ? "OK" : "NG") +  
				", message=" + result.getMessage());
		
		// ファイル配布
		System.out.println(HinemosTime.getDateString() + " === send file ===");
		String remoteFilename = orgFilename + ".remoteaaa";
		result = sendFile(user, pass, host, port, timeout, ".", orgFilename, ".", remoteFilename, "hinemos", "0644", true, keypath, passphrase);
		System.out.println(HinemosTime.getDateString() + " result, ok/ng=" + (result.getResult() == OkNgConstant.TYPE_OK ? "OK" : "NG") +  
				", message=" + result.getMessage());
		
		// ファイル受信
		System.out.println(HinemosTime.getDateString() + " === receive file ===");
		String localFilename = orgFilename + ".local";
		result = recvFile(user, pass, host, port, timeout, ".", remoteFilename, ".", localFilename, "hinemos", "0644", keypath, passphrase);
		System.out.println(HinemosTime.getDateString() + " result, ok/ng=" + (result.getResult() == OkNgConstant.TYPE_OK ? "OK" : "NG") +  
				", message=" + result.getMessage());
		
		// ファイルサイズとMD5Sumのチェック
		System.out.println(HinemosTime.getDateString() + " === compare ===");
		long orgFilesize = new File(orgFilename).length();
		long newfilesize = new File(localFilename).length();
		if (orgFilesize == newfilesize) {
			String orgCheckSum = FileTransferModuleInfo.getCheckSum(orgFilename);
			String newCheckSum = FileTransferModuleInfo.getCheckSum(localFilename);
			if (orgCheckSum.equals(newCheckSum)) {
				System.out.println(HinemosTime.getDateString() + " result, ok/ng=OK, size=" + orgFilesize);
			} else {
				System.out.println(HinemosTime.getDateString() + " result, ok/ng=NG, org.checksum=" + orgCheckSum + ", new.checksum=" + newCheckSum);
			}
		} else {
			System.out.println(HinemosTime.getDateString() + " result, ok/ng=NG, org.size=" + orgFilesize + ", new.size=" + newfilesize);
		}
	}
}