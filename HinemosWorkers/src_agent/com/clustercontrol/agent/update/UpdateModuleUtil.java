package com.clustercontrol.agent.update;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.activation.DataHandler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.agent.Agent;
import com.clustercontrol.agent.AgentEndPointWrapper;
import com.clustercontrol.util.HinemosTime;
import com.clustercontrol.ws.agent.HinemosUnknown_Exception;
import com.clustercontrol.ws.agent.InvalidRole_Exception;
import com.clustercontrol.ws.agent.InvalidUserPass_Exception;

public class UpdateModuleUtil {
	// ロガー
	private static Log m_log = LogFactory.getLog(UpdateModuleUtil.class);

	// ファイルパスとMD5の組
	private static HashMap <String, String> agentMap = null;
	
	// ファイルのダウンロードが行われた場合はtrue(一回の起動時に複数回ダウンロードしないようにする）
	private static boolean isDownload = false;
	
	private static final String agentLibDir = Agent.getAgentHome() + "lib/";
	
	/**
	 * Hinemosマネージャからファイルを取得するメソッド。
	 * MD5を確認してから、Hinemosマネージャの/opt/hinemos/lib/agent/?を
	 * Hinemosエージェントの/opt/hinemos_agent/download/にダウンロードする。
	 * 1つ以上のファイルをダウンロードしたときはtrueを返す。
	 * @throws HinemosUnknown_Exception
	 * @throws IOException
	 * @throws InvalidUserPass_Exception
	 * @throws InvalidRole_Exception
	 */
	public static boolean update() throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception {
		m_log.info("update() : start ");
		
		if (isDownload) {
			m_log.info("update() : download already done.");
			return false;
		}
		
		// マネージャからリストを取得する。
		HashMap<String, String> managerMap = AgentEndPointWrapper.getAgentLibMap();	
		boolean ret = false;
		for (Entry<String, String> entry : managerMap.entrySet()) {
			String md5Manager = entry.getValue();
			String filePath = entry.getKey();
			String agentLibFilePath = filePath.replace("/", File.separator).replace("\\", File.separator);
			String md5Agent = getAgentMap().get(agentLibFilePath);
			m_log.debug("update() : filename=" + agentLibFilePath +
					", manager=" + md5Manager +
					", agent=" + md5Agent);
			if (!md5Manager.equals(md5Agent)) {
				// バックアップを取得する。(AGENT_HOME/lib/*)
				backup(filePath);

				// ファイルを作成する。(AGENT_HOME/download/*)
				FileOutputStream fileOutputStream = null;
				try {
					DataHandler handler = AgentEndPointWrapper.downloadModule(filePath);
					
					File srcFile = new File(new File(Agent.getAgentHome(), "download"), agentLibFilePath);
					
					createDir(srcFile);
					if (!srcFile.createNewFile())
						throw new InternalError("can not create file, filename : " + agentLibFilePath);
					fileOutputStream = new FileOutputStream(srcFile);
					handler.writeTo(fileOutputStream);
					m_log.info("update() : download filename=" + srcFile.getAbsolutePath() +
							", md5(manager)=" + md5Manager + ", md5(agent)=" + md5Agent);
				} catch (IOException e) {
					m_log.warn("update() : IOException, " + e.getMessage());
				} finally {
					if (fileOutputStream != null) {
						try {
							fileOutputStream.close();
						} catch (IOException e) {
							m_log.warn("update() : IOException,, " + e.getMessage());
						}
					}
				}
				// ファイルがダウンロードされたのでこれ以上ほかのトピックを元にダウンロードが実行されないようにする
				isDownload = true;
				ret = true;
			}
		}
		return ret;
	}

	private synchronized static HashMap<String, String> getAgentMap() {
		if (agentMap != null) {
			return agentMap;
		}
		agentMap = new HashMap<String, String>();
		File dir = new File(agentLibDir);
		return putAgentLibMap(agentMap, dir);
	}
	
	private synchronized static HashMap<String, String> putAgentLibMap(HashMap<String, String> agentMap, File dir) {
		File[] files = dir.listFiles();
		if (files == null) {
			m_log.info(String.format("files is null, %s=%s",dir.getName(), dir.getAbsolutePath()));
			return agentMap;
		}
		
		for (File file : files) {
			if (file.isDirectory()) {
				putAgentLibMap(agentMap, file);
			} else {
				if (file.isFile()) {
					File agentLibDir = new File(Agent.getAgentHome(), "lib");
					String libPath = file.getAbsolutePath().replace(agentLibDir.getAbsolutePath() + File.separator, "");
					agentMap.put(libPath, getMD5(file.getAbsolutePath()));
				}
			}
		}
		return agentMap;
	}

	public static void setAgentLibMd5() throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception {
		m_log.info("setAgentLibMd5");
		AgentEndPointWrapper.setAgentLibMd5(getAgentMap());
	}

	/**
	 * MD5を取得する。
	 * @param filepath
	 * @return
	 */
	private static String getMD5(String filepath) {
		MessageDigest md = null;
		DigestInputStream inStream = null;
		byte[] digest = null;
		try {
			md = MessageDigest.getInstance("MD5");
			inStream = new DigestInputStream(
					new BufferedInputStream(new FileInputStream(filepath)), md);
			while (inStream.read() != -1) {}
			digest = md.digest();
		} catch (Exception e) {
			m_log.error("getMD5() : filepath=" + filepath + ", " + e.getClass(), e);
		} finally {
			if (inStream != null) {
				try {
					inStream.close();
				} catch (Exception e) {
					m_log.error("getMD5() : close " + e.getClass());
				}
			}
			if(digest == null)
				throw new InternalError("digest is null");
		}
		return hashByte2MD5(digest);
	}

	private static String hashByte2MD5(byte []input) {
		StringBuffer ret = new StringBuffer();
		for (byte b : input) {
			if ((0xff & b) < 0x10) {
				ret.append("0" + Integer.toHexString((0xFF & b)));
			} else {
				ret.append(Integer.toHexString(0xFF & b));
			}
		}
		return ret.toString();
	}

	/**
	 * ファイルをバックアップする。
	 * @param filePath
	 */
	private static void backup(String libPath) {
		FileInputStream inStream = null;
		FileOutputStream outStream = null;
		FileChannel srcChannel = null;
		FileChannel destChannel = null;
		
		File agentLibDir = new File(Agent.getAgentHome(), "lib");
		File srcFile = new File(agentLibDir, libPath);
		if(!srcFile.exists()) {
			return;
		}
		
		String srcPath = srcFile.getAbsolutePath();
		
		Date date1 = HinemosTime.getDateInstance();
		SimpleDateFormat sdf1 = new SimpleDateFormat("yyyyMMdd-HHmmss");
		sdf1.setTimeZone(HinemosTime.getTimeZone());
		
		File destFile =new File(agentLibDir, "backup"+ File.separator + libPath + "_" + sdf1.format(date1));
		String destPath = destFile.getAbsolutePath();
		
		createDir(destFile);
		
		try {
			inStream = new FileInputStream(srcPath);
			srcChannel = inStream.getChannel();
			outStream = new FileOutputStream(destPath);
			destChannel = outStream.getChannel();
			srcChannel.transferTo(0, srcChannel.size(), destChannel);
		} catch (IOException e) {
			m_log.warn("IOException(" + srcPath + "," + destPath + ") " + e.getMessage());
		} finally {
			if (srcChannel != null) {
				try {
					srcChannel.close();
				} catch (IOException e) {
				}
			}
			if (inStream != null) {
				try {
					inStream.close();
				} catch (IOException e) {
				}
			}
			if (destChannel != null) {
				try {
					destChannel.close();
				} catch (IOException e) {
				}
			}
			if (outStream != null) {
				try {
					outStream.close();
				} catch (IOException e) {
				}
			}
		}
	}
	
	private static void createDir(File dir) {
		if(!dir.getParentFile().exists()) {
			dir.getParentFile().mkdirs();
		}
	}
	
	public static void main (String args[]) {
		String filename = "hinemos_install.log";
		System.out.println("filename=" + filename);
		System.out.println("md5=" + getMD5(filename));

		File file = new File(filename);
		System.out.println("p=" + file.getParent());
		System.out.println("p=" + file.getParentFile());
	}
}
