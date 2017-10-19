package com.clustercontrol.infra.model;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.commons.util.CommonValidator;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InfraManagementNotFound;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.infra.bean.AccessInfo;
import com.clustercontrol.infra.bean.ModuleNodeResult;
import com.clustercontrol.infra.bean.OkNgConstant;
import com.clustercontrol.infra.bean.SendMethodConstant;
import com.clustercontrol.infra.util.InfraJdbcExecutor;
import com.clustercontrol.infra.util.JschUtil;
import com.clustercontrol.infra.util.QueryUtil;
import com.clustercontrol.infra.util.WinRMUtil;
import com.clustercontrol.maintenance.util.HinemosPropertyUtil;
import com.clustercontrol.platform.HinemosPropertyDefault;
import com.clustercontrol.repository.model.NodeInfo;
import com.clustercontrol.repository.util.RepositoryUtil;
import com.clustercontrol.util.HinemosTime;
import com.clustercontrol.util.MessageConstant;
import com.clustercontrol.util.StringBinder;

@XmlType(namespace = "http://infra.ws.clustercontrol.com")
@Entity
@Table(name="cc_infra_file_transfer_module_info", schema="setting")
@Inheritance
@DiscriminatorValue(FileTransferModuleInfo.typeName)
@Cacheable(true)
public class FileTransferModuleInfo extends InfraModuleInfo<FileTransferModuleInfo> {
	public static class FileInfo {
		private String name;
		private String value;
		
		public FileInfo() {
		}
		
		public FileInfo(String name, String value) {
			setName(name);
			setValue(value);
		}
		
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		
		public String getValue() {
			return value;
		}
		public void setValue(String value) {
			this.value = value;
		}
	}
	/**
	 * File.separatorは、javaが実行されるプラットフォームの情報から選別される。
	 */
	public static String SEPARATOR = File.separator;

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static final String typeName = "FileTransferModule";

	private String destPath;
	private int sendMethodType;
	private String destOwner;
	private String destAttribute;
	private Boolean backupIfExistFlg;
	
	private List<FileTransferVariableInfo> fileTransferVariableInfoEntities = new ArrayList<>();
	
	private String fileId;
	
	public FileTransferModuleInfo() {
	}

	public FileTransferModuleInfo(InfraManagementInfo parent, String moduleId) {
		super(parent, moduleId);
	}
	
	@Column(name="dest_path")
	public String getDestPath() {
		return destPath;
	}
	public void setDestPath(String destPath) {
		this.destPath = destPath;
	}
	
	@Column(name="send_method_type")
	public int getSendMethodType() {
		return sendMethodType;
	}
	public void setSendMethodType(int sendMethodType) {
		this.sendMethodType = sendMethodType;
	}

	@Column(name="dest_owner")
	public String getDestOwner() {
		return destOwner;
	}
	public void setDestOwner(String destOwner) {
		this.destOwner = destOwner;
	}
	
	@Column(name="dest_attribute")
	public String getDestAttribute() {
		return destAttribute;
	}
	public void setDestAttribute(String destAttribute) {
		this.destAttribute = destAttribute;
	}
	
	@OneToMany(mappedBy="fileTransferModuleInfoEntity", cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	public List<FileTransferVariableInfo> getFileTransferVariableList() {
		return this.fileTransferVariableInfoEntities;
	}
	public void setFileTransferVariableList(List<FileTransferVariableInfo> fileTransferVariableInfoEntities) {
		this.fileTransferVariableInfoEntities = fileTransferVariableInfoEntities;
	}
	
	@Column(name="file_id")
	public String getFileId() {
		return fileId;
	}
	public void setFileId(String fileId) {
		this.fileId = fileId;
	}

	@Column(name="backup_if_exist_flg")
	public Boolean getBackupIfExistFlg() {
		return backupIfExistFlg;
	}
	public void setBackupIfExistFlg(Boolean backupIfExistFlg) {
		this.backupIfExistFlg = backupIfExistFlg;
	}

	@Override
	public String getModuleTypeName() {
		return typeName;
	}
	
	@Override
	protected void validateSub(InfraManagementInfo infraManagementInfo) throws InvalidSetting, InvalidRole {
		// fileId
		CommonValidator.validateString(MessageConstant.INFRA_MODULE_PLACEMENT_FILE.getMessage(), getFileId(), false, 0, 256);
		// ファイル情報存在チェック
		try {
			QueryUtil.getInfraFileInfoPK_OR(getFileId(), ObjectPrivilegeMode.READ, infraManagementInfo.getOwnerRoleId());
		} catch (InvalidRole e) {
			throw e;
		} catch (Exception e) {
			InvalidSetting e1 = new InvalidSetting(MessageConstant.MESSAGE_NOT_FOUND_PLACEMENT_FILE.getMessage() +
					" Target file info is not exist! fileId = " + fileId);
			throw e1;
		}
		
		// destPath
		CommonValidator.validateString(MessageConstant.INFRA_MODULE_PLACEMENT_PATH.getMessage(), getDestPath(), false, 0, 1024);
		
		// destOwner
		CommonValidator.validateString(MessageConstant.INFRA_MODULE_TRANSFER_METHOD_OWNER.getMessage(), getDestOwner(), false, 0, 256);
		
		// destAttribute
		CommonValidator.validateString(MessageConstant.INFRA_MODULE_TRANSFER_METHOD_SCP_FILE_ATTRIBUTE.getMessage(), getDestAttribute(), false, 0, 64);
		
		// precheckFlg : not backupIfExistFlg
		
		// sendMehodType
		boolean match = false;
		for (int type: SendMethodConstant.getTypeList()) {
			if (type == getSendMethodType()) {
				match = true;
				break;
			}
		}
		if (!match) {
			InvalidSetting e = new InvalidSetting("SendMethodType must be SCP(0) / WinRM(1).");
			Logger.getLogger(this.getClass()).info("validateSub() : " + e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		
		// fileTransferVariableList
		if(getFileTransferVariableList() != null){
			for(FileTransferVariableInfo fileTransferVariableInfo : getFileTransferVariableList()){
				//name
				CommonValidator.validateString(MessageConstant.INFRA_MANAGEMENT_SEARCH_WORDS.getMessage(), fileTransferVariableInfo.getName(), false, 0, 256);
				
				//value
				CommonValidator.validateString(MessageConstant.INFRA_MANAGEMENT_REPLACEMENT_WORDS.getMessage(), fileTransferVariableInfo.getValue(), false, 0, 256);
			}
		}
	}
	
	@Override
	public void beforeRun(String sessionId) throws HinemosUnknown {
		// 送信するファイルの元になるファイルを作成
		InfraJdbcExecutor.selectFileContent(getFileId(), sessionId + "-" + getModuleId());
	}
	
	@Override
	public void afterRun(String sessionId) {
		// beforeRunで作成したファイルを削除
		File file = new File(createTempFilePath(sessionId));
		if (!file.delete())
			Logger.getLogger(this.getClass()).debug("Fail to delete " + file.getAbsolutePath());
	}
	
	private String createTempFilePath(String sessionId) {
		String exportDirectory = HinemosPropertyUtil.getHinemosPropertyStr("infra.export.dir",
				HinemosPropertyDefault.getString(HinemosPropertyDefault.StringKey.INFRA_EXPORT_DIR));
		String filepath = exportDirectory + "/" + sessionId + "-" + getModuleId();
		return filepath;
	}

	@Override
	public boolean canPrecheck(InfraManagementInfo management, NodeInfo node, AccessInfo access, String sessionId) throws HinemosUnknown, InvalidUserPass {
		return this.getPrecheckFlg();
	}

	@Override
	public ModuleNodeResult run(InfraManagementInfo management, NodeInfo node, AccessInfo access, String sessionId) throws HinemosUnknown, InvalidUserPass {
		String postfixStr = node.getFacilityId();
		String infraDirectory = HinemosPropertyUtil.getHinemosPropertyStr("infra.transfer.dir", 
				HinemosPropertyDefault.getString(HinemosPropertyDefault.StringKey.INFRA_TRANSFER_DIR));
		InfraFileInfo fileEntity = null;
		JpaTransactionManager jtm = new JpaTransactionManager();
		try {
			fileEntity = QueryUtil.getInfraFileInfoPK(getFileId(), ObjectPrivilegeMode.NONE);
		} catch (InvalidRole e) {
			// ここは通らない
			throw new HinemosUnknown(e.getMessage());
		} catch (InfraManagementNotFound e) {
			throw new HinemosUnknown(e.getMessage());
		} finally {
			jtm.close();
		}
		String fileName = fileEntity.getFileName();
		String srcDir = infraDirectory + SEPARATOR;
		String srcFile = fileName;
		
		File orgFile = new File(createTempFilePath(sessionId));

		// 一時ファイルの作成(文字列を置換する)
		srcDir += "send" + SEPARATOR;
		srcFile += "." + postfixStr;
		try {
			createFile(orgFile, srcDir + srcFile, getFileTransferVariableList(), node);
			return send(
					node.getFacilityId(),
					node.getAvailableIpAddress(),
					node.getWinrmProtocol(),
					access,
					srcDir,
					srcFile,
					getDestPath(),
					fileName,
					getDestOwner(),
					getDestAttribute()
					);
		} finally {
			try {
				Files.delete(Paths.get(srcDir, srcFile));
			} catch (IOException e) {
			}
		}
	}
	
	@Override
	public ModuleNodeResult check(InfraManagementInfo management, NodeInfo node, AccessInfo access, String sessionId, boolean verbose) throws HinemosUnknown, InvalidUserPass {
		ModuleNodeResult ret = new ModuleNodeResult(OkNgConstant.TYPE_NG, -1, "failed checking");
		ret.setFacilityId(node.getFacilityId());

		//// sendフォルダにファイルを生成
		String postfixStr = node.getFacilityId();
		String infraDirectory = HinemosPropertyUtil.getHinemosPropertyStr("infra.transfer.dir",
				HinemosPropertyDefault.getString(HinemosPropertyDefault.StringKey.INFRA_TRANSFER_DIR));
		InfraFileInfo fileEntity = null;
		JpaTransactionManager jtm = new JpaTransactionManager();
		try {
			fileEntity = QueryUtil.getInfraFileInfoPK(getFileId(), ObjectPrivilegeMode.NONE);
		} catch (InvalidRole e) {
			// ここは通らない
			throw new HinemosUnknown(e.getMessage());
		} catch (InfraManagementNotFound e) {
			throw new HinemosUnknown(e.getMessage());
		} finally {
			jtm.close();
		}
		String fileName = fileEntity.getFileName();
		String srcDir = infraDirectory + SEPARATOR;
		String srcFile = fileName;
		String sendMd5 = null;
		File file = null;
		
		File orgFile = new File(createTempFilePath(sessionId));
		long fileSize = orgFile.length();
		String INFRA_TRANSFER_FILESIZE = "infra.transfer.filesize";
		boolean isDiscarded = false;

		///// New File /////
		// 20481より大きい場合はファイルの中身を比較表示できません。MD5は比較できます。
		// 20481という数字は、クライアントで利用している比較ライブラリ(mergely)に依存しています。
		long maxFilesize = HinemosPropertyUtil.getHinemosPropertyNum(INFRA_TRANSFER_FILESIZE, Long.valueOf(20481));
		// 一時ファイルの作成(文字列を置換する)
		srcDir += "send" + SEPARATOR;
		srcFile += "." + postfixStr;

		createFile(orgFile, srcDir + srcFile, getFileTransferVariableList(), node);
		file = new File(srcDir + srcFile);
		if(file.exists()) {
			FileDataSource source = new FileDataSource(file);
			DataHandler handler = new DataHandler(source);
			ret.setNewFilename(fileName);
			if (fileSize < maxFilesize) {
				ret.setNewFile(handler);
			} else {
				isDiscarded = true;
			}
			sendMd5 = getCheckSum(srcDir + srcFile);
		} else {
			// sendは必ず存在するはずなので、ここには到達しないはず。
			Logger.getLogger(this.getClass()).warn("check : file not found (send)[" + file.getAbsolutePath() + "]");
		}
		
		/*
		 * verbose=true
		 * 		Hinemosクライアントよりチェックをクリックして、差分表示を見たいとき
		 * 		→ファイル配布先から古いファイルを持ってくる必要あり。
		 * 		→持ってきてからこちらでMD5をチェックする。
		 * verbose=false
		 * 		Hinemosクライアントよりチェックをクリックして、差分表示を見ないとき(モジュールビューでOK,NGを更新したいとき)
		 * 		Hinemosクライアントより実行をクリックして、ファイル配布前にMD5にチェックをしたとき
		 * 		→ファイル配布先から古いファイルを持ってこない。
		 * 		→ファイル配布先でMD5をチェックする。
		 */
		Logger.getLogger(this.getClass()).info("managementId=" + management.getManagementId() + ", fileId=" + fileId + ", verbose=" + verbose);
		if (!verbose) {
			return isSameMd5(
							node.getFacilityId(),
							node.getAvailableIpAddress(),
							node.getWinrmProtocol(),
							access,
							srcDir,
							srcFile,
							getDestPath(),
							fileName
							);
		} else {
			///// Old File /////
			// recvフォルダにファイルを生成
			srcDir = infraDirectory + SEPARATOR + "recv" + SEPARATOR;
			ModuleNodeResult ret2 = recv(
				node.getFacilityId(),
				node.getAvailableIpAddress(),
				node.getWinrmProtocol(),
				access,
				getDestPath(),
				fileName,
				srcDir,
				srcFile,
				getDestOwner(),
				getDestAttribute()
				);
			if (ret2.getResult() == OkNgConstant.TYPE_NG) {
				return ret2;
			}
			
			String recvMd5 = null;
			file = new File(srcDir + srcFile);
			if(file.exists()) {
				FileDataSource source = new FileDataSource(file);
				DataHandler handler = new DataHandler(source);
				ret.setOldFilename(fileName);
				if (file.length() < maxFilesize) {
					ret.setOldFile(handler);
				} else {
					isDiscarded = true;
				}
				recvMd5 = getCheckSum(srcDir + srcFile);
			} else {
				Logger.getLogger(this.getClass()).info("check : file not found (recv)[" + file.getAbsolutePath() + "]");
			}
			
			ret.setFileDiscarded(isDiscarded);
			
			//// 中身の比較
			if (sendMd5 != null && recvMd5 != null) {
				if (sendMd5.equals(recvMd5)) {
					ret.setResult(OkNgConstant.TYPE_OK);
					ret.setMessage("equal file. MD5=" + sendMd5);
				} else {
					ret.setMessage("not equal file. MD5(new)=" + sendMd5 + ", MD5(old)=" + recvMd5);
				}
			} else if (sendMd5 == null && recvMd5 == null) {
				ret.setMessage("both files are not found..."); // sendは必ず存在するはずなので、ここには到達しないはず。 
			} else if (sendMd5 == null && recvMd5 != null) {
				ret.setMessage("new file is not found..."); // sendは必ず存在するはずなので、ここには到達しないはず。 
			} else if (sendMd5 != null && recvMd5 == null) {
				ret.setMessage("old file is not found."); 
			}
			return ret;
		}
	}
	
	private static Object replaceFileLock = new Object();
	
	// runとcheckから呼ばれる
	private static void createFile(File orgFile, String dstFilepath, List<FileTransferVariableInfo> list, NodeInfo node) throws HinemosUnknown {
		if(list.isEmpty()) {
			//文字列置換がない場合はそのままコピーする
			try {
				synchronized (replaceFileLock) {
					Files.copy(orgFile.toPath(), new File(dstFilepath).toPath(), StandardCopyOption.REPLACE_EXISTING);
				}
				Logger.getLogger(FileTransferModuleInfo.class).info("copy : " + dstFilepath + ", os.size=" + orgFile.length());
			} catch (IOException e) {
				HinemosUnknown ex = new HinemosUnknown("createFile " + e.getClass().getName() + ", " + e.getMessage(), e);
				Logger.getLogger(FileTransferModuleInfo.class).warn(ex.getMessage(), e);
				throw ex;
			}
		} else {
			ArrayList<FileInfo> list2 = new ArrayList<>();
			for (FileTransferVariableInfo info : list) {
				FileInfo info2 = new FileInfo(info.getName(), info.getValue());
				
				String str = info2.getValue();
				
				Map<String, String> nodeParameter = RepositoryUtil.createNodeParameter(node);
				StringBinder strbinder = new StringBinder(nodeParameter);
				Logger.getLogger(FileTransferModuleInfo.class).debug("replaceNodeVariable() before : " + str);
				str = strbinder.bindParam(str);
				Logger.getLogger(FileTransferModuleInfo.class).debug("replaceNodeVariable() after : " + str);
				info2.setValue( str );
				list2.add(info2); 
			}
			synchronized (replaceFileLock) {
				replaceFile(orgFile, dstFilepath, list2);
			}
		}
	}
	
	
	/**
	 * ファイルの中身を書き換えて、新しいファイル(dstFilepath)を作成する。
	 * メモリあふれを防ぐために、このメソッドの呼び出し時は、synchronizedすること。
	 * @param srcFilepath
	 * @param dstFilepath
	 * @param list
	 * @return
	 * @throws HinemosUnknown 
	 */
	private static void replaceFile(File orgFile, String dstFilepath, List<FileInfo> list) throws HinemosUnknown {
		OutputStream os = null;
		FileInputStream fis = null;
		Logger.getLogger(FileTransferModuleInfo.class).info("replaceFile : " + dstFilepath + ", os.size=" + orgFile.length());
		try {
			fis = new FileInputStream(orgFile);
			os = Files.newOutputStream(Paths.get(dstFilepath));
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ByteArrayOutputStream bos1 = new ByteArrayOutputStream();
			byte[] buf = new byte[1024*1024];
			while (true) {
				int read = fis.read(buf);
				if (read < 0) {
					break;
				}
				for (int i = 0; i < read; ++i) {
					byte b = buf[i];
					if (b == '\r' || b == '\n') {
						if (bos.size() > 0) {
							byte[] byteArray = bos.toByteArray();
							bos.reset();
		
							//replace
							for (FileInfo info : list) {
								byteArray = replace(byteArray, info.getName(), info.getValue());
							}
							bos1.write(byteArray);
						}
	
						//write \r or \n
						bos1.write(b);
					} else {
						bos.write(b);
					}
				}
				if(bos1.size() > 0) {
					os.write(bos1.toByteArray());
					bos1.reset();
				}
				if(bos.size() > 0) {
					byte[] byteArray = bos.toByteArray();
					for (FileInfo info : list) {
						byteArray = replace(byteArray, info.getName(), info.getValue());
					}
					os.write(byteArray);
					bos.reset();
				}
			}
		} catch (IOException e) {
			HinemosUnknown exception = new HinemosUnknown("createFile " + e.getClass().getName() + ", " + e.getMessage(), e);
			Logger.getLogger(FileTransferModuleInfo.class).warn(exception.getMessage(), e);
			throw exception;
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException e) {
				}
			}
			if (os != null) {
				try {
					os.close();
				} catch (IOException e) {
				}
			}
		}
	}
	
	private static byte[] replace(byte[] byteArray, String name, String value) throws IOException {
		ByteArrayOutputStream bos = null;
		try {
			bos = new ByteArrayOutputStream();
			byte[] nameByteArray = name.getBytes();
			int i= 0;
			while (i <= byteArray.length - nameByteArray.length) {
				if (compareByteArray(byteArray, i, nameByteArray)) {
					bos.write(value.getBytes());
					i += nameByteArray.length;
				} else {
					bos.write(byteArray[i]);
					i++;
				}
			}
			if (i < byteArray.length) {
				bos.write(byteArray, i, byteArray.length - i);
			}
		
			return bos.toByteArray();
		} catch (IOException e) {
			throw e;
		} finally {
			if (bos != null) {
				bos.close();
			}
		}
	}

	private static boolean compareByteArray(byte[] byteArray1, int byteArray1Offset, byte[] byteArray2) {
		if (byteArray1.length - byteArray1Offset < byteArray2.length) {
			return false;
		}
		
		for (int i = 0; i < byteArray2.length; i++) {
			if (byteArray1[byteArray1Offset + i] != byteArray2[i]) {
				return false;
			}
		}
		
		return true;
	}
	
	private ModuleNodeResult send(String facilityId, String host, String protocol, AccessInfo access,
			String srcDir, String srcFile, String dstDir, String dstFile, String owner, String perm) {
		ModuleNodeResult ret = null;
		
		long start = HinemosTime.currentTimeMillis();
		switch (getSendMethodType()) {
		case SendMethodConstant.TYPE_SCP:
			ret = JschUtil.sendFile(access.getSshUser(), access.getSshPassword(), host, access.getSshPort(), access.getSshTimeout(),
					srcDir, srcFile, dstDir, dstFile,
					owner, perm, getBackupIfExistFlg(), access.getSshPrivateKeyFilepath(), access.getSshPrivateKeyPassphrase());
			break;
		case SendMethodConstant.TYPE_WINRM:
			ret = WinRMUtil.sendFile(access.getWinRmUser(), access.getWinRmPassword(), host, access.getWinRmPort(), protocol,
					srcDir, srcFile, dstDir, dstFile,
					owner, perm, getBackupIfExistFlg());
			break;
		default:
			String msg = String.format("AccessMethodType is invalid. value = %d", getSendMethodType());
			Logger.getLogger(FileTransferModuleInfo.class).warn("send : " + msg);
			ret = new ModuleNodeResult(OkNgConstant.TYPE_NG, -1, msg);
		}
		ret.setFacilityId(facilityId);
		
		Logger.getLogger(FileTransferModuleInfo.class).info("send facilityId=" + facilityId + ", " + srcDir + "/" + srcFile + " -> " + dstDir + "/" + dstFile +
				", result=" + ret.getResult() +
				", time=" + (HinemosTime.currentTimeMillis() - start) + "ms" );
		return ret;
	}
	
	private ModuleNodeResult isSameMd5(String facilityId, String host, String protocol, AccessInfo access,
			String srcDir, String srcFile, String dstDir, String dstFile) {
		ModuleNodeResult ret = null;
		switch (getSendMethodType()) {
		case SendMethodConstant.TYPE_SCP:
			ret = JschUtil.isSameMd5(access.getSshUser(), access.getSshPassword(), host, access.getSshPort(), access.getSshTimeout(),
					srcDir, srcFile, dstDir, dstFile, access.getSshPrivateKeyFilepath() , access.getSshPrivateKeyPassphrase());
			break;
		case SendMethodConstant.TYPE_WINRM:
			ret = WinRMUtil.isSameMd5(access.getWinRmUser(), access.getWinRmPassword(), host, access.getWinRmPort(), protocol,
					srcDir, srcFile, dstDir, dstFile);
			break;
		default:
			String msg = String.format("AccessMethodType is invalid. value = %d", getSendMethodType());
			Logger.getLogger(FileTransferModuleInfo.class).warn("isSameMd5 : " + msg);
			ret = new ModuleNodeResult(OkNgConstant.TYPE_NG, -1, msg);
		}
		ret.setFacilityId(facilityId);
		return ret;
	}

	private ModuleNodeResult recv(String facilityId, String host, String protocol, AccessInfo access, 
			String srcDir, String srcFile, String dstDir, String dstFile, String owner, String perm) {
		ModuleNodeResult ret = null;
		long start = HinemosTime.currentTimeMillis();
		switch (getSendMethodType()) {
		case SendMethodConstant.TYPE_SCP:
			ret = JschUtil.recvFile(access.getSshUser(), access.getSshPassword(), host, access.getSshPort(), access.getSshTimeout(),
					srcDir, srcFile, dstDir, dstFile, owner, perm, access.getSshPrivateKeyFilepath(), access.getSshPrivateKeyPassphrase());
			break;
		case SendMethodConstant.TYPE_WINRM:
			ret = WinRMUtil.recvFile(access.getWinRmUser(), access.getWinRmPassword(), host, access.getWinRmPort(), protocol, srcDir, srcFile, dstDir, dstFile, owner, perm);
			break;
		default:
			String msg = String.format("AccessMethodType is invalid. value = %d", getSendMethodType());
			Logger.getLogger(FileTransferModuleInfo.class).warn("recv : " + msg);
			ret = new ModuleNodeResult(OkNgConstant.TYPE_NG, -1, msg);
		}
		ret.setFacilityId(facilityId);
		Logger.getLogger(FileTransferModuleInfo.class).info("recv facilityId=" + facilityId + ", " + srcDir + "/" + srcFile + " -> " + dstDir + "/" + dstFile +
				", result=" + ret.getResult() +
				", time=" + (HinemosTime.currentTimeMillis() - start) + "ms" );
		return ret;
	}
		

	
	public static String getCheckSum(String path) {
		String checksum = null;
		try (FileInputStream inputStream = new FileInputStream(path)) {
			MessageDigest md = MessageDigest.getInstance("md5");
			byte[] readData = new byte[256];
			int len;
			while ((len = inputStream.read(readData)) >=0) {
				md.update(readData, 0, len);
			}
			checksum = byte2String(md.digest());
		} catch (Exception e) {
			Logger.getLogger(FileTransferModuleInfo.class).warn("getCheckSum error. " + e.getMessage(), e);
		}
		return checksum;
	}
	
	private static String byte2String(byte[] digest) {
		StringBuilder hashString = new StringBuilder();
		for (int i = 0; i < digest.length; i++) {
			int d = digest[i];
			if (d < 0) {//負の値を補正
				d += 256;
			}
			if (d < 16) {//1けたは2けたする
				hashString.append("0");
			}
			hashString.append(Integer.toString(d, 16));//16進数2けたにする
		}
		return hashString.toString();
	}

	@Override
	protected Class<FileTransferModuleInfo> getEntityClass() {
		return FileTransferModuleInfo.class;
	}

	@Override
	protected void overwriteCounterEntity(InfraManagementInfo management, FileTransferModuleInfo module, HinemosEntityManager em) {
		module.setDestOwner(getDestOwner());
		module.setDestAttribute(getDestAttribute());
		module.setDestPath(getDestPath());
		module.setSendMethodType(getSendMethodType());
		module.setBackupIfExistFlg(getBackupIfExistFlg());
		module.setFileId(getFileId());

		List<FileTransferVariableInfo> webVariableList = new ArrayList<FileTransferVariableInfo>(getFileTransferVariableList());
		List<FileTransferVariableInfo> dbVariableList = new ArrayList<FileTransferVariableInfo>(module.getFileTransferVariableList());
		
		Iterator<FileTransferVariableInfo> webVariableIter = webVariableList.iterator();
		while (webVariableIter.hasNext()) {
			FileTransferVariableInfo webVariable = webVariableIter.next();
			
			Iterator<FileTransferVariableInfo> dbVariableIter = dbVariableList.iterator();
			while (dbVariableIter.hasNext()) {
				FileTransferVariableInfo dbVariable = dbVariableIter.next();
				if (webVariable.getName().equals(dbVariable.getId().getName())) {
					dbVariable.setValue(webVariable.getValue());
					
					webVariableIter.remove();
					dbVariableIter.remove();
					break;
				}
			}
		}
		
		for (FileTransferVariableInfo webVariable: webVariableList) {
			FileTransferVariableInfo dbVariable = new FileTransferVariableInfo(module, webVariable.getName());
			dbVariable.setValue(webVariable.getValue());
		}
		
		for (FileTransferVariableInfo dbVariable: dbVariableList) {
			module.getFileTransferVariableList().remove(dbVariable);
			em.remove(dbVariable);
		}
	}
	
	@Override
	public void onPersist(HinemosEntityManager em) {
		super.onPersist(em);
	}
}