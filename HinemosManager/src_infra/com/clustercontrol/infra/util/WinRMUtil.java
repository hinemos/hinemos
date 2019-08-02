/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.infra.util;

import intel.management.wsman.WsmanException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Calendar;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Logger;

import com.clustercontrol.accesscontrol.bean.UserIdConstant;
import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.infra.bean.ModuleNodeResult;
import com.clustercontrol.infra.bean.OkNgConstant;
import com.clustercontrol.infra.model.FileTransferModuleInfo;
import com.clustercontrol.util.HinemosTime;
import com.clustercontrol.util.XMLUtil;

public class WinRMUtil {
	private static Log log = LogFactory.getLog( WinRMUtil.class );
	
	private static String winReturnCode = "\n"; // Windowsなので、%nではなく\nとする

    public final static String WINRM_FILE_SPLIT = "~";

    public static ModuleNodeResult execCommand(String user, String password, String host, int port, String protocol, String command, int maxSize) {
		log.info("user=" + user + ", host=" + host + ", port=" + port + ", command=" + command);
		// log.debug("password=" + password);
		
		WinRs winRs = new WinRs(host, port, protocol, user, password);
		String shellId = null;
		try {
			shellId = winRs.openShell();

			String[] args = {};
			WinRsCommandOutput output = runCommand(winRs, command, args, shellId);
			
			String std = "out=" + output.getStdout().trim();
			String err = "err=" + output.getStderr().trim();
			String msg = String.format("command=%s%n", command);
			msg = msg + String.format("exitCode=%d%n", output.getExitCode());
			msg = msg + std.substring(0, Math.min(std.length(), (maxSize - msg.length() - 1) - Math.min((maxSize - msg.length() - 1) / 2, err.length()))) + "\n";
			msg = msg + err.substring(0, Math.min(err.length(), (maxSize - msg.length() - 1) / 2));

			return new ModuleNodeResult(
					output.getExitCode() == 0 ? OkNgConstant.TYPE_OK : OkNgConstant.TYPE_NG,
							(int) output.getExitCode(),
							XMLUtil.ignoreInvalidString(msg));
		} catch (WsmanException e) {
			WsmanException we = (WsmanException) e;
			log.warn("execCommand " + getMessage(we));
			return createResultFromException(e);
		} catch (Exception e) {
			log.warn("execCommand " + e.getMessage() + "(" + e.getClass().getName() + ")", e);
			return createResultFromException(e);
		} finally {
			closeShell(winRs, shellId);
		}
	}

	public static ModuleNodeResult recvFile(String user, String password, String host, int port, String protocol,
			String srcDir, String srcFilename, String dstDir, String dstFilename, 
			String owner, String perm) {

		WinRs winRs = new WinRs(host, port, protocol, user, password);
		String shellId = null;
		try {
			shellId = winRs.openShell();

			//バイナリファイルを対応するために、ファイルの内容をBase64でエンコードしてから、stdに出力する。
			String command = "powershell";
			String[] args = { 
					"-inputformat",
					"none",
					String.format("[System.convert]::ToBase64String([System.IO.File]::ReadAllBytes('%s'))",
							srcDir + File.separator + srcFilename) };
			WinRsCommandOutput output = runCommand(winRs, command, args, shellId);
			
			//std出力の内容にある改行を消して、元のバイナリに変更する。
			String base64Content = output.getStdout().replaceAll("\n", "");
			base64Content = output.getStdout().replaceAll("\r", "");
			byte[] binaryContent = Base64.decodeBase64(base64Content);
			writeToBinaryFile(dstDir + File.separator + dstFilename, binaryContent);

			return new ModuleNodeResult(OkNgConstant.TYPE_OK, 0, "exitCode=0");
		} catch (WsmanException e) {
			WsmanException we = (WsmanException) e;
			log.warn("recvFile " + getMessage(we));
			return createResultFromException(e);
		} catch (Exception e) {
			log.warn("recvFile " + e.getMessage() + "(" + e.getClass().getName() + ")", e);
			return createResultFromException(e);
		} finally {
			closeShell(winRs, shellId);
		}
	}
	
	public static ModuleNodeResult sendFile(String user, String password, String host, int port, String protocol,
			String srcDir, String srcFilename, String dstDir, String dstFilename,
			String owner, String perm, boolean isBackupIfExistFlg, int cnt)  {
		
		WinRs winRs = new WinRs(host, port, protocol, user, password);
		String shellId = null;
		try {
			shellId = winRs.openShell();

			String tempFilePath = getTempFilePath(winRs, shellId);
			String dstFilePath = dstDir + File.separator + dstFilename;
			
			//一時ファイルにアップロード
			WinRsCommandOutput output = null;
			String msg = "";
			if (cnt > 1) {
				for (int i = 1; i <= cnt; i++) {
					// 複数ファイル
					output = downloadToTempFile(winRs, shellId, FileTransferModuleInfo.getSplitFileName(tempFilePath, i), 
							FileTransferModuleInfo.getSplitFileName(srcFilename, i));
					msg = "out=" + output.getStdout().trim() + "\n" + "err=" + output.getStderr().trim();
					if (output.getExitCode() != 0) {
						log.warn("sendFile() code="+output.getExitCode()+",out="+output.getStdout()+",err="+output.getStderr());
						return new ModuleNodeResult(OkNgConstant.TYPE_NG, 1, XMLUtil.ignoreInvalidString(msg));
					}
				}
			} else {
				// 1ファイル
				output = downloadToTempFile(winRs, shellId, tempFilePath, srcFilename);
				msg = "out=" + output.getStdout().trim() + "\n" + "err=" + output.getStderr().trim();
				if (output.getExitCode() != 0) {
					log.warn("sendFile() code="+output.getExitCode()+",out="+output.getStdout()+",err="+output.getStderr());
					return new ModuleNodeResult(OkNgConstant.TYPE_NG, 1, XMLUtil.ignoreInvalidString(msg));
				}
			}
						
			//一時ファイルをディコードして、送信先ファイルに保存
			output = decodeTempFile(winRs, shellId, tempFilePath, dstFilePath, isBackupIfExistFlg, cnt);
			msg = "out=" + output.getStdout().trim() + "\n" + "err=" + output.getStderr().trim();
			// PowershellでOOME発生の場合、ExitCode=0で返ってくるのでエラー出力も確認している
			if (output.getExitCode() != 0 || output.getStderr().trim().contains("System.OutOfMemoryException")) {
				log.warn("sendFile() code="+output.getExitCode()+",out="+output.getStdout()+",err="+output.getStderr());
				return new ModuleNodeResult(OkNgConstant.TYPE_NG, 1, XMLUtil.ignoreInvalidString(msg));
			}
			return new ModuleNodeResult(OkNgConstant.TYPE_OK, 0,
					dstFilename + " was transfered. (" + dstDir + ")");
		} catch (WsmanException e) {
			WsmanException we = (WsmanException) e;
			log.warn("sendFile " + getMessage(we), e);
			return createResultFromException(e);
		} catch (Exception e) {
			log.warn("sendFile " + e.getMessage() + "(" + e.getClass().getName() + ")", e);
			return createResultFromException(e);
		} finally {
			closeShell(winRs, shellId);
		}
	}

	public static ModuleNodeResult isSameMd5(String user, String password, String host, int port, String protocol,
			String srcDir, String srcFilename, String dstDir, String dstFilename) {
		ModuleNodeResult ret = new ModuleNodeResult(OkNgConstant.TYPE_NG, -1, "unknown");
		
		FileInputStream srcFis = null;
		WinRs winRs = new WinRs(host, port, protocol, user, password);
		String shellId = null;		
		try {
			shellId = winRs.openShell();

			String srcFilePath = srcDir + File.separator + srcFilename;
			/**
			 * 環境構築機能の対象サーバが、Windowsの場合に、
			 * Linux用のファイルセパレータの「/」が指定されてもWindow側で良しなに処理してくれる。
			 */
			String dstFilePath = dstDir + File.separator + dstFilename;

			srcFis = new FileInputStream(new File(srcFilePath));
			String srcMd5 = DigestUtils.md5Hex(srcFis);
			
			String command = "powershell";
			String[] args = {
					"-inputformat",
					"none",
					"-encodedCommand",
					Base64.encodeBase64String(getMd5Script(dstFilePath).getBytes("UTF-16LE")) };
			
			WinRsCommandOutput output = runCommand(winRs, command, args, shellId);
			String dstMd5 = output.getStdout();
			
			//XX-XX-XX形のMD5をXXXXXに、大文字から小文字に変換
			dstMd5 = dstMd5.trim().replaceAll("-", "").toLowerCase();
			
			if(srcMd5.equals(dstMd5)) {
				ret.setResult(OkNgConstant.TYPE_OK);
				ret.setStatusCode(0);
				ret.setMessage("MD5s are same");
			} else {
				ret.setResult(OkNgConstant.TYPE_NG);
				ret.setStatusCode(0);
				ret.setMessage("MD5s differ : src=" + srcMd5 + ", dst=" + dstMd5);
			}
		} catch (WsmanException e) {
			WsmanException we = (WsmanException) e;
			log.warn("isSameMd5 " + getMessage(we));
			ret.setMessage(e.getMessage());
		} catch (Exception e) {
			log.warn("isSameMd5 : " + e.getMessage(), e);
			ret.setMessage(e.getMessage());
		} finally {
			if (srcFis != null) {
				try {
					srcFis.close();
				} catch (IOException e) {
				}
			}
		}
		
		return ret;
	}

	private static String getMessage(WsmanException we) {
		String str = "";
		try {
			str += we.getCode();
		} catch (Exception e) {
			Logger.getLogger(WinRMUtil.class).debug(e.getMessage(), e);
		}
		str += ",";
		try {
			str += we.getSubCode();
		} catch (Exception e) {
			Logger.getLogger(WinRMUtil.class).debug(e.getMessage(), e);
		}
		str += ",";
		try {
			str += we.getReason();
		} catch (Exception e) {
			Logger.getLogger(WinRMUtil.class).debug(e.getMessage(), e);
		}
		str += ",";
		try {
			str += we.getDetail();
		} catch (Exception e) {
			Logger.getLogger(WinRMUtil.class).debug(e.getMessage(), e);
		}
		
		return str;
	}
	
	private static String getMd5Script(String dstFilePath) {
		StringBuffer buffer = new StringBuffer();
		buffer.append(String.format("$filePath=\"%s\"" + winReturnCode, dstFilePath));
		buffer.append("$md5=New-Object -TypeName System.Security.Cryptography.MD5CryptoServiceProvider" + winReturnCode);
		buffer.append("$hash=[System.BitConverter]::ToString($md5.ComputeHash([System.IO.File]::ReadAllBytes($filePath)))" + winReturnCode);
		buffer.append("$hash" + winReturnCode);
		
		return buffer.toString();
	}

	private static void closeShell(WinRs winRs, String shellId) {
		if (shellId != null) {
			try {
				winRs.closeShell(shellId);
			} catch (WsmanException e) {
			}
		}
	}
	
	private static ModuleNodeResult createResultFromException(Exception e) {
		String message = null;
		if (e instanceof WsmanException) {
			message = getMessage((WsmanException)e);
		} else {
			message = e.getClass().getName() + ", " + e.getMessage();
		}
		return new ModuleNodeResult(OkNgConstant.TYPE_NG, -1, message);
	}

	private static WinRsCommandOutput decodeTempFile(WinRs winRs, String shellId,
			String tempFilePath, String dstFilePath, boolean isBackupIfExistFlg, int cnt)
			throws UnsupportedEncodingException, WsmanException {
		
		String command = "powershell";
		String[] args = {
				"-inputformat",
				"none",
				"-encodedCommand",
				Base64.encodeBase64String(getDecodeScript(tempFilePath,
						dstFilePath, isBackupIfExistFlg, cnt).getBytes("UTF-16LE")) };
		return runCommand(winRs, command, args, shellId);
	}

	private static String getDecodeScript(String tempFilePath, String dstFilePath, boolean isBackupIfExistFlg, int cnt) {
		StringBuffer buffer = new StringBuffer();
		buffer.append(String.format("$tmp_file_path = [System.IO.Path]::GetFullPath('%s')" + winReturnCode, tempFilePath));
		buffer.append(String.format("$dest_file_path = [System.IO.Path]::GetFullPath('%s')" + winReturnCode, dstFilePath));
		buffer.append(String.format("$backup_file_ext = '%s'" + winReturnCode, getBackupFileExt()));
		buffer.append(String.format("$is_backup = $%s" + winReturnCode, isBackupIfExistFlg ? "TRUE" : "FALSE"));
		buffer.append("$dest_dir = ([System.IO.Path]::GetDirectoryName($dest_file_path))" + winReturnCode);
		buffer.append("if (-not(Test-Path $dest_dir)) {" + winReturnCode);
		buffer.append("   echo \"[ERROR] $dest_dir : No such directory\"" + winReturnCode);
		buffer.append("   exit 1" + winReturnCode);
		buffer.append("}\n");
		buffer.append("if (Test-Path $dest_file_path) {" + winReturnCode);
		buffer.append("    if ($is_backup) {" + winReturnCode);
		buffer.append("        Move $dest_file_path \"$dest_file_path.$backup_file_ext\"" + winReturnCode);
		buffer.append("    }" + winReturnCode);
		buffer.append("    rm $dest_file_path" + winReturnCode);
		buffer.append("}" + winReturnCode);
		buffer.append("$writer = [System.IO.File]::OpenWrite($dest_file_path)" + winReturnCode);
		buffer.append("$bufferSize = 10 * 1024 * 1024 # should be a multiplier of 4" + winReturnCode);
		buffer.append("$buffer = New-Object char[] $bufferSize" + winReturnCode);
		if (cnt > 1) {
			buffer.append(String.format("for($idx=0; $idx -le %d; $idx++) {" + winReturnCode, cnt));
			buffer.append("    $f = [System.Xml.XmlReader]::create($tmp_file_path + '" + WINRM_FILE_SPLIT + "' + $idx)" + winReturnCode);
			buffer.append("    while ($f.read()) {" + winReturnCode);
			buffer.append("        switch ($f.NodeType) {" + winReturnCode);
			buffer.append("            ([System.Xml.XmlNodeType]::Element) {" + winReturnCode);
			buffer.append("                if ($f.Name -eq \"return\") {" + winReturnCode);
			buffer.append("                    $null = $f.read()" + winReturnCode);
			buffer.append("                    while (($len = $f.ReadValueChunk($buffer, 0 ,$bufferSize)) -ne 0) {" + winReturnCode);
			buffer.append("                        $bytes = [Convert]::FromBase64CharArray($buffer, 0, $len)" + winReturnCode);
			buffer.append("                        $writer.Write($bytes, 0, $bytes.Length)" + winReturnCode);
			buffer.append("                    }" + winReturnCode);
			buffer.append("                }" + winReturnCode);
			buffer.append("                break" + winReturnCode);
			buffer.append("            }" + winReturnCode);
			buffer.append("        }" + winReturnCode);
			buffer.append("    }" + winReturnCode);
			buffer.append("}" + winReturnCode);
		} else {
			buffer.append("$f = [System.Xml.XmlReader]::create($tmp_file_path)" + winReturnCode);
			buffer.append("while ($f.read()) {" + winReturnCode);
			buffer.append("    switch ($f.NodeType) {" + winReturnCode);
			buffer.append("        ([System.Xml.XmlNodeType]::Element) {" + winReturnCode);
			buffer.append("            if ($f.Name -eq \"return\") {" + winReturnCode);
			buffer.append("                $null = $f.read()" + winReturnCode);
			buffer.append("                while (($len = $f.ReadValueChunk($buffer, 0 ,$bufferSize)) -ne 0) {" + winReturnCode);
			buffer.append("                    $bytes = [Convert]::FromBase64CharArray($buffer, 0, $len)" + winReturnCode);
			buffer.append("                    $writer.Write($bytes, 0, $bytes.Length)" + winReturnCode);
			buffer.append("                }" + winReturnCode);
			buffer.append("            }" + winReturnCode);
			buffer.append("            break" + winReturnCode);
			buffer.append("        }" + winReturnCode);
			buffer.append("    }" + winReturnCode);
			buffer.append("}" + winReturnCode);
		}
		buffer.append("$writer.Dispose()" + winReturnCode);
		String str = buffer.toString();
		log.debug(str);
		return str;
	}

	private static String getBackupFileExt() {
		Calendar now = HinemosTime.getCalendarInstance(); 
		return String.format("%04d%02d%02d%02d%02d%02d",
				now.get(Calendar.YEAR),
				now.get(Calendar.MONTH) + 1,
				now.get(Calendar.DAY_OF_MONTH),
				now.get(Calendar.HOUR_OF_DAY),
				now.get(Calendar.MINUTE),
				now.get(Calendar.SECOND));
	}

	private static String getTempFilePath(WinRs winRs, String shellId) throws WsmanException {
		//一時フォルダを取得
		String command = "echo";
		String[] args = {"%TEMP%"};
		WinRsCommandOutput output = runCommand(winRs, command, args, shellId);
		String tempDir = output.getStdout().trim();

		return tempDir + File.separator + ("winrs_" + HinemosTime.currentTimeMillis());
	}

	private static WinRsCommandOutput runCommand(WinRs winRs, String command, String[] args, String shellId) throws WsmanException {
		StringBuilder stdout = new StringBuilder();
		StringBuilder stderr = new StringBuilder();
		long exitCode = 0;
		WinRsCommandState state = WinRsCommandState.Running;
		String commandId = winRs.runCommand(shellId, command, args);
		do {
			WinRsCommandOutput output;
			output = winRs.getCommandOutput(shellId, commandId);
			stdout.append(output.getStdout());
			stderr.append(output.getStderr());
			exitCode = output.getExitCode();
			state = output.getState();
		} while (state == WinRsCommandState.Running);
		winRs.cleanupCommand(shellId, commandId);

		return new WinRsCommandOutput(stdout.toString(), stderr.toString(), exitCode, state);
	}
	
	private static WinRsCommandOutput downloadToTempFile(WinRs winRs, String shellId, String tempFilePath, String filename) throws WsmanException, UnsupportedEncodingException, HinemosUnknown {
		String command = "powershell";
		String[] args = {
				"-inputformat",
				"none",
				"-encodedCommand",
				Base64.encodeBase64String(getDownloadScript(tempFilePath,
						filename).getBytes("UTF-16LE")) };
		WinRsCommandOutput output = runCommand(winRs, command, args, shellId);

		return output;
	}
	
	
	private static String getDownloadScript(String tempFilePath, String fileName) throws HinemosUnknown {
		String url = HinemosPropertyCommon.infra_transfer_winrm_url.getStringValue();
		String pass = HinemosPropertyCommon.infra_transfer_agent_password.getStringValue();
		String account = UserIdConstant.AGENT + ":" + pass;
		boolean sslTrustall = HinemosPropertyCommon.infra_winrm_ssl_trustall.getBooleanValue();
		
		if (!url.endsWith("/")) {
			url += "/";
			log.debug("getDownloadScript() : infra.transfer.winrm.url is not ended with '/' , so appended. url = " + url);
		}
		
		StringBuffer buffer = new StringBuffer();
		buffer.append(String.format("$temp_file_path = [System.IO.Path]::GetFullPath('%s')" + winReturnCode, tempFilePath));
		buffer.append(String.format("$url = \"%sHinemosWS/InfraEndpoint?wsdl\"" + winReturnCode, url));
		buffer.append("$soap = @'" + winReturnCode);
		buffer.append("<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\">" + winReturnCode);
		buffer.append("    <soap:Body>" + winReturnCode);
		buffer.append("        <downloadTransferFile xmlns=\"http://infra.ws.clustercontrol.com\">" + winReturnCode);
		buffer.append(String.format("            <arg0 xmlns=\"\">%s</arg0>" + winReturnCode, fileName));
		buffer.append("        </downloadTransferFile>" + winReturnCode);
		buffer.append("    </soap:Body>" + winReturnCode);
		buffer.append("</soap:Envelope>" + winReturnCode);
		buffer.append("'@" + winReturnCode);
		buffer.append("$user = \"hinemos\"" + winReturnCode);
		buffer.append("$pass = \"hinemos\"" + winReturnCode);
		buffer.append(String.format("$bytes = [System.Text.Encoding]::ASCII.GetBytes(\"%s\")" + winReturnCode, account));
		buffer.append("$base64 = [System.Convert]::ToBase64String($bytes)" + winReturnCode);
		buffer.append("$headers = @{ Authorization = \"Basic $base64\" }" + winReturnCode);
		buffer.append("$progressPreference = 'silentlyContinue'" + winReturnCode);
		if (sslTrustall && url.startsWith("https")){
			dlScriptHttpsEdit(buffer);
		}
		buffer.append("Invoke-WebRequest $url -ContentType \"text/xml\" -Body $soap -Method Post -Headers $headers -OutFile $temp_file_path" + winReturnCode);
		String str = buffer.toString();
		log.debug(str);
		return str;
	}
	
	private static void writeToBinaryFile(String filePath,
			byte[] binaryContent) throws IOException {
		Path path = Paths.get(filePath);
		Files.write(path, binaryContent);
	}
	
	/**
	* HTTPS接続時の証明書検証回避プロパティを追加
	* @param buffer
	* @return
	*/
	private static void dlScriptHttpsEdit(StringBuffer buffer) {
		buffer.append("if (-not ([System.Management.Automation.PSTypeName]'ServerCertificateValidationCallback').Type)" + winReturnCode);
		buffer.append("{" + winReturnCode);
		buffer.append("$certCallback = @\"" + winReturnCode);
		buffer.append("    using System;" + winReturnCode);
		buffer.append("    using System.Net;" + winReturnCode);
		buffer.append("    using System.Net.Security;" + winReturnCode);
		buffer.append("    using System.Security.Cryptography.X509Certificates;" + winReturnCode);
		buffer.append("    public class ServerCertificateValidationCallback" + winReturnCode);
		buffer.append("    {" + winReturnCode);
		buffer.append("        public static void Ignore()" + winReturnCode);
		buffer.append("        {" + winReturnCode);
		buffer.append("            if(ServicePointManager.ServerCertificateValidationCallback ==null)" + winReturnCode);
		buffer.append("            {" + winReturnCode);
		buffer.append("                ServicePointManager.ServerCertificateValidationCallback += " + winReturnCode);
		buffer.append("                    delegate" + winReturnCode);
		buffer.append("                    (" + winReturnCode);
		buffer.append("                        Object obj, " + winReturnCode);
		buffer.append("                        X509Certificate certificate, " + winReturnCode);
		buffer.append("                        X509Chain chain, " + winReturnCode);
		buffer.append("                        SslPolicyErrors errors" + winReturnCode);
		buffer.append("                    )" + winReturnCode);
		buffer.append("                    {" + winReturnCode);
		buffer.append("                        return true;" + winReturnCode);
		buffer.append("                    };" + winReturnCode);
		buffer.append("            }" + winReturnCode);
		buffer.append("        }" + winReturnCode);
		buffer.append("    }" + winReturnCode);
		buffer.append("\"@" + winReturnCode);
		buffer.append("    Add-Type $certCallback" + winReturnCode);
		buffer.append(" }" + winReturnCode);
		buffer.append("[ServerCertificateValidationCallback]::Ignore()" + winReturnCode);
	}
}
