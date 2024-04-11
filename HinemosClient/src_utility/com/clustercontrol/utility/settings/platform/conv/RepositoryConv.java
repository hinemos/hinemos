/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.settings.platform.conv;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Logger;
import org.openapitools.client.model.NodeCpuInfoResponse;
import org.openapitools.client.model.NodeDiskInfoResponse;
import org.openapitools.client.model.NodeFilesystemInfoResponse;
import org.openapitools.client.model.NodeGeneralDeviceInfoResponse;
import org.openapitools.client.model.NodeHostnameInfoResponse;
import org.openapitools.client.model.NodeInfoResponse;
import org.openapitools.client.model.NodeLicenseInfoResponse;
import org.openapitools.client.model.NodeMemoryInfoResponse;
import org.openapitools.client.model.NodeNetstatInfoResponse;
import org.openapitools.client.model.NodeNetworkInterfaceInfoResponse;
import org.openapitools.client.model.NodeNoteInfoResponse;
import org.openapitools.client.model.NodeOsInfoResponse;
import org.openapitools.client.model.NodePackageInfoResponse;
import org.openapitools.client.model.NodeProcessInfoResponse;
import org.openapitools.client.model.NodeProductInfoResponse;
import org.openapitools.client.model.NodeVariableInfoResponse;

import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.rpa.util.RpaConstants;
import com.clustercontrol.util.Messages;
import com.clustercontrol.utility.settings.model.BaseConv;
import com.clustercontrol.utility.settings.platform.action.RepositoryNodeAction;
import com.clustercontrol.utility.settings.platform.xml.CPUInfo;
import com.clustercontrol.utility.settings.platform.xml.DeviceInfo;
import com.clustercontrol.utility.settings.platform.xml.DiskInfo;
import com.clustercontrol.utility.settings.platform.xml.FSInfo;
import com.clustercontrol.utility.settings.platform.xml.HostnameInfo;
import com.clustercontrol.utility.settings.platform.xml.LicenseInfo;
import com.clustercontrol.utility.settings.platform.xml.MemoryInfo;
import com.clustercontrol.utility.settings.platform.xml.NetstatInfo;
import com.clustercontrol.utility.settings.platform.xml.NetworkInterfaceInfo;
import com.clustercontrol.utility.settings.platform.xml.NodeInfo;
import com.clustercontrol.utility.settings.platform.xml.NodeVariableInfo;
import com.clustercontrol.utility.settings.platform.xml.NoteInfo;
import com.clustercontrol.utility.settings.platform.xml.PackageInfo;
import com.clustercontrol.utility.settings.platform.xml.ProcessInfo;
import com.clustercontrol.utility.settings.platform.xml.ProductInfo;
import com.clustercontrol.utility.util.OpenApiEnumConverter;
import com.clustercontrol.version.util.VersionUtil;

/**
 * リポジトリ情報をJavaBeanとXML(Bean)のbindingとのやりとりを
 * 行うクラス<BR>
 * 
 * @version 6.1.0
 * @since 1.0.0
 * 
 */
public class RepositoryConv {
	
	/**
	 * 同一バイナリ化対応により、スキーマ情報はHinemosVersion.jarのVersionUtilクラスから取得されることになった。
	 * スキーマ情報の一覧はhinemos_version.properties.implに記載されている。
	 * スキーマ情報に変更がある場合は、まずbuild_common_version.properties.implを修正し、
	 * 対象のスキーマ情報が初回の修正であるならばhinemos_version.properties.implも修正する。
	 */
	static final private String scopeSchemaType=VersionUtil.getSchemaProperty("PLATFORM.REPOSITORYSCOPE.SCHEMATYPE");
	static final private String scopeSchemaVersion=VersionUtil.getSchemaProperty("PLATFORM.REPOSITORYSCOPE.SCHEMAVERSION");
	static final private String scopeSchemaRevision=VersionUtil.getSchemaProperty("PLATFORM.REPOSITORYSCOPE.SCHEMAREVISION");
	
	static final private String nodeSchemaType=VersionUtil.getSchemaProperty("PLATFORM.REPOSITORY.SCHEMATYPE");
	static final private String nodeSchemaVersion=VersionUtil.getSchemaProperty("PLATFORM.REPOSITORY.SCHEMAVERSION");
	static final private String nodeSchemaRevision=VersionUtil.getSchemaProperty("PLATFORM.REPOSITORY.SCHEMAREVISION");
	static private String schemaType="";
	static private String schemaVersion="";
	static private String schemaRevision="" ;
	
	/* ロガー */
	private  static Log log = LogFactory.getLog(RepositoryConv.class);

	static public int checkSchemaVersionScope(String type, String version ,String revision){
		
		schemaType=scopeSchemaType;
		schemaVersion=scopeSchemaVersion;
		schemaRevision=scopeSchemaRevision;
		
		return checkSchemaVersion(type, version ,revision);
	}
	
	static public int checkSchemaVersionNode(String type, String version ,String revision){
		
		schemaType=nodeSchemaType;
		schemaVersion=nodeSchemaVersion;
		schemaRevision=nodeSchemaRevision;
		
		return checkSchemaVersion(type, version ,revision);
	}
	
	
	/**
	 * XMLとツールの対応バージョンをチェック */
	static private int checkSchemaVersion(String type, String version ,String revision){
		return BaseConv.checkSchemaVersion(schemaType, schemaVersion, schemaRevision,
				type, version, revision);
	}

	static public com.clustercontrol.utility.settings.platform.xml.SchemaInfo getSchemaVersionScope(){

		schemaType=scopeSchemaType;
		schemaVersion=scopeSchemaVersion;
		schemaRevision=scopeSchemaRevision;
		
		return getSchemaVersionSub();
	}
	
	static public com.clustercontrol.utility.settings.platform.xml.SchemaInfo getSchemaVersionNode(){

		schemaType=nodeSchemaType;
		schemaVersion=nodeSchemaVersion;
		schemaRevision=nodeSchemaRevision;
		
		return getSchemaVersionSub();
	}
	
	/**
	 * スキーマのバージョンを返します。
	 * @return
	 */
	static private com.clustercontrol.utility.settings.platform.xml.SchemaInfo getSchemaVersionSub(){
	
		com.clustercontrol.utility.settings.platform.xml.SchemaInfo schema =
			new com.clustercontrol.utility.settings.platform.xml.SchemaInfo();
		
		schema.setSchemaType(schemaType);
		schema.setSchemaVersion(schemaVersion);
		schema.setSchemaRevision(schemaRevision);
		
		return schema;
	}
	
	
	/**
	 * XMLから生成したオブジェクト（ノード情報、ホスト名情報、CPU情報、メモリ情報、
	 * ネットワークインタフェース情報、ディスク情報、ファイルシステム情報、
	 * ネットワーク接続情報、ライセンス情報、個別導入製品情報、
	 * 汎用デバイス情報、ノード変数情報、備考情報）から、
	 * ノードのプロパティオブジェクトを生成する<br>
	 * 
	 * @param xmlNodeInfo
	 *            ノード情報オブジェクト
	 * @param hostnameList
	 *            ホスト名情報オブジェクト
	 * @param cpuList
	 *            CPU情報オブジェクト
	 * @param memoryList
	 *            メモリ情報オブジェクト
	 * @param networkInterfaceList
	 *            ネットワークインタフェース情報オブジェクト
	 * @param diskList
	 *            ディスク情報オブジェクト
	 * @param fsList
	 *            ファイルシステム情報オブジェクト
	 * @param deviceList
	 *            汎用デバイス情報オブジェクト
	* @param netstatList
	 *            ネットワーク接続情報オブジェクト
	 * @param licenseList
	 *            ライセンス情報オブジェクト
	 * @param productList
	 *            個別導入製品情報オブジェクト
	 * @param variableList
	 *            ノード変数情報オブジェクト
	 * @param noteList
	 *            備考情報オブジェクト
	 * @return ノードのプロパティオブジェクト
	 */
	public static NodeInfoResponse convNodeXml2Dto(
		NodeInfo xmlNodeInfo, HostnameInfo[] hostnameList, CPUInfo[] cpuList, MemoryInfo[] memoryList,
		NetworkInterfaceInfo[] networkInterfaceList, DiskInfo[] diskList, FSInfo[] fsList, DeviceInfo[] deviceList,
		NodeVariableInfo[] variableList, NoteInfo[] noteList) throws InvalidSetting, HinemosUnknown {
	
		NodeInfoResponse  dto = new NodeInfoResponse();
		
		// 誤りがある場合の処理
		
		// ノード情報の格納
		if(xmlNodeInfo.getFacilityId() != null
				&& !"".equals(xmlNodeInfo.getFacilityId())){
			dto.setFacilityId(xmlNodeInfo.getFacilityId());
			log.debug("(FaclityId) : " + xmlNodeInfo.toString());
			
		}else{
			log.warn(Messages.getString("SettingTools.EssentialValueInvalid")
					+ "(FaclityId) : " + xmlNodeInfo.toString());
			
			//値に誤りがあることを示すためにFacilityIdに""をセット
			dto.setFacilityId("");
			return dto;
		}
		
		if(xmlNodeInfo.getFacilityName() != null
				&& !"".equals(xmlNodeInfo.getFacilityName())){
			dto.setFacilityName(xmlNodeInfo.getFacilityName());
		}else{
			log.warn(Messages.getString("SettingTools.EssentialValueInvalid")
					+ "(FaclityName) : " + xmlNodeInfo.getFacilityId());
			
			//値に誤りがあることを示すためにFacilityIdに""をセット
			dto.setFacilityId("");
			return dto;
		}
		
		dto.setOwnerRoleId(xmlNodeInfo.getOwnerRoleId());
		dto.setValid(xmlNodeInfo.getValidFlg());
		
		if(xmlNodeInfo.getDescription() != null){
			dto.setDescription(xmlNodeInfo.getDescription());
		}
		
		if(xmlNodeInfo.getPlatformFamily() != null
				&& !"".equals(xmlNodeInfo.getPlatformFamily())){
			dto.setPlatformFamily(xmlNodeInfo.getPlatformFamily());
		}else{
			log.warn(Messages.getString("SettingTools.EssentialValueInvalid")
					+ "(PlatformFamily) : " + xmlNodeInfo.getFacilityId());
			//値に誤りがあることを示すためにPlatformFamilyに""をセット
			dto.setPlatformFamily("");
			return dto;
		}
		
		if(xmlNodeInfo.getSubPlatformFamily() != null
				&& !"".equals(xmlNodeInfo.getSubPlatformFamily())){
			dto.setSubPlatformFamily(xmlNodeInfo.getSubPlatformFamily());
		}
		
		if(xmlNodeInfo.getHardwareType() != null){
			dto.setHardwareType(xmlNodeInfo.getHardwareType());
		}
		
		if(xmlNodeInfo.getIconImage() != null){
			dto.setIconImage(xmlNodeInfo.getIconImage());
		}

		// SNMP関連情報のセット
		if(xmlNodeInfo.hasSnmpPort()){
			dto.setSnmpPort((int)xmlNodeInfo.getSnmpPort());
		}

		if(xmlNodeInfo.hasSnmpTimeout()){
			dto.setSnmpTimeout((int)xmlNodeInfo.getSnmpTimeout());
		}

		if(xmlNodeInfo.hasSnmpRetryCount()){
			dto.setSnmpRetryCount((int)xmlNodeInfo.getSnmpRetryCount());
		}

		// WBEM関連情報のセット
		if(xmlNodeInfo.hasWbemPort()){
			dto.setWbemPort((int)xmlNodeInfo.getWbemPort());
		}

		if(xmlNodeInfo.hasWbemTimeout()){
			dto.setWbemTimeout((int)xmlNodeInfo.getWbemTimeout());
		}
		
		if(xmlNodeInfo.hasWbemRetryCount()){
			dto.setWbemRetryCount((int)xmlNodeInfo.getWbemRetryCount());
		}
		
		if (xmlNodeInfo.getIpAddressVersion() == 4
				|| xmlNodeInfo.getIpAddressVersion() == 6) {
			dto.setIpAddressVersion(OpenApiEnumConverter.integerToEnum((int)xmlNodeInfo.getIpAddressVersion(),NodeInfoResponse.IpAddressVersionEnum.class)) ;
		}
		
		if(xmlNodeInfo.getIpAddressV4() != null
				&& !"".equals(xmlNodeInfo.getIpAddressV4()) ){
			dto.setIpAddressV4(xmlNodeInfo.getIpAddressV4());
		}else{
			//IPアドレスバージョンが空もしくは4のときにはV4アドレスは必須
			if(xmlNodeInfo.getIpAddressVersion() != 6){
				log.warn(Messages.getString("SettingTools.EssentialValueInvalid")
						+ "(IPv4) : " + xmlNodeInfo.getFacilityId());
				//値に誤りがあることを示すためにFacilityIdに""をセット
				dto.setFacilityId("");
				return dto;
			}
		}
		
		if(xmlNodeInfo.getIpAddressV6() !=null
				&& !"".equals(xmlNodeInfo.getIpAddressV6())){
			dto.setIpAddressV6(xmlNodeInfo.getIpAddressV6());
		}else{
			//IPアドレスバージョンが6のときにはV6アドレスは必須
			if(xmlNodeInfo.getIpAddressVersion() == 6){
				log.warn(Messages.getString("SettingTools.EssentialValueInvalid")
						+ "(IPv6) : " + xmlNodeInfo.getFacilityId());
				//値に誤りがあることを示すためにFacilityIdに""をセット
				dto.setFacilityId("");
				return dto;
			}
			
		}
		
		if(xmlNodeInfo.getNodeName() != null
				&& !"".equals(xmlNodeInfo.getNodeName())){
			dto.setNodeName(xmlNodeInfo.getNodeName());
		}else{
				log.warn(Messages.getString("SettingTools.EssentialValueInvalid")
						+ "(NodeName) : " + xmlNodeInfo.getFacilityId());
				//値に誤りがあることを示すためにFacilityIdに""をセット
				dto.setFacilityId("");
				return dto;
		}

		if (dto.getNodeOsInfo() == null) {
			dto.setNodeOsInfo(new NodeOsInfoResponse());
			//dto.getNodeOsInfo().setFacilityId(dto.getFacilityId());
		}
		if(xmlNodeInfo.getOsName() != null
				&& !"".equals(xmlNodeInfo.getOsName())){
			dto.getNodeOsInfo().setOsName(xmlNodeInfo.getOsName());
		}
		
		if(xmlNodeInfo.getOsRelease() != null
				&& !"".equals(xmlNodeInfo.getOsRelease())){
			dto.getNodeOsInfo().setOsRelease(xmlNodeInfo.getOsRelease());
		}
		
		if(xmlNodeInfo.getOsVersion() != null
				&& !"".equals(xmlNodeInfo.getOsVersion())){
			dto.getNodeOsInfo().setOsVersion(xmlNodeInfo.getOsVersion());
		}
		
		if(xmlNodeInfo.getCharacterSet() != null
				&& !"".equals(xmlNodeInfo.getCharacterSet())){
			dto.getNodeOsInfo().setCharacterSet(xmlNodeInfo.getCharacterSet());
		}
		
		if(xmlNodeInfo.getStartupDateTime() != null
				&& !"".equals(xmlNodeInfo.getStartupDateTime())){
				dto.getNodeOsInfo().setStartupDateTime(xmlNodeInfo.getStartupDateTime());
		}
		
		if(xmlNodeInfo.getAdministrator() != null
				&& !"".equals(xmlNodeInfo.getAdministrator())){
			dto.setAdministrator(xmlNodeInfo.getAdministrator());
		}
		
		if(xmlNodeInfo.getContact() != null
				&& !"".equals(xmlNodeInfo.getContact())){
			dto.setContact(xmlNodeInfo.getContact());
		}
		
		//Hinemosエージェント関連情報セット
		if(xmlNodeInfo.hasAgentAwakePort()){
			dto.setAgentAwakePort((int)xmlNodeInfo.getAgentAwakePort());
		}
		
		//ジョブ関連情報のセット
		if(xmlNodeInfo.hasJobMultiplicity()){
			dto.setJobMultiplicity((int)xmlNodeInfo.getJobMultiplicity());
		}
		if(xmlNodeInfo.hasJobPriority()){
			dto.setJobPriority((int)xmlNodeInfo.getJobPriority());
		}
		
		/*ここ確認！*/
		
		dto.setAutoDeviceSearch(xmlNodeInfo.getAutoDeviceSearch());
		
		/*IPMI関連情報のセット*/
		if(xmlNodeInfo.getIpmiIpAddress() != null && !"".equals(xmlNodeInfo.getIpmiIpAddress())){
			dto.setIpmiIpAddress(xmlNodeInfo.getIpmiIpAddress());
		}
		
		if(xmlNodeInfo.hasIpmiPort()){
			dto.setIpmiPort((int)xmlNodeInfo.getIpmiPort());
		}
		
		if(xmlNodeInfo.getIpmiUser() != null){
			dto.setIpmiUser(xmlNodeInfo.getIpmiUser());
		}
		
		if(xmlNodeInfo.getIpmiUserPassword() != null && !"".equals(xmlNodeInfo.getIpmiUserPassword())){
			String name = Messages.getString("ipmi") +":"+ Messages.getString("ipmi.user.password");
			String str = checkString(name, xmlNodeInfo.getIpmiUserPassword(), 64);
			if(str == null){
				//値に誤りがあることを示すためにFacilityIdに""をセット
				dto.setFacilityId("");
				return dto;
			}
			dto.setIpmiUserPassword(xmlNodeInfo.getIpmiUserPassword());
		}
		
		if(xmlNodeInfo.hasIpmiTimeout()){
			dto.setIpmiTimeout((int)xmlNodeInfo.getIpmiTimeout());
		}
		
		if(xmlNodeInfo.hasIpmiRetryCount()){
			dto.setIpmiRetries((int)xmlNodeInfo.getIpmiRetryCount());
		}
		
		if(xmlNodeInfo.getIpmiProtocol() != null){
			dto.setIpmiProtocol(xmlNodeInfo.getIpmiProtocol());
		}
		
		if(xmlNodeInfo.getIpmiLevel() != null){
			dto.setIpmiLevel(xmlNodeInfo.getIpmiLevel());
		}
		
		/*WinRM関連情報のセット*/
		if(xmlNodeInfo.getWinrmUser() != null){
			dto.setWinrmUser(xmlNodeInfo.getWinrmUser());
		}
		
		if(xmlNodeInfo.getWinrmUserPassword() != null && !"".equals(xmlNodeInfo.getWinrmUserPassword())){
			String name = Messages.getString("winrm") +":"+ Messages.getString("winrm.user.password");
			String str = checkString(name, xmlNodeInfo.getWinrmUserPassword(), 64);
			if(str == null){
				//値に誤りがあることを示すためにFacilityIdに""をセット
				dto.setFacilityId("");
				return dto;
			}
			dto.setWinrmUserPassword(xmlNodeInfo.getWinrmUserPassword());
		}
		
		if(xmlNodeInfo.getWinrmVersion() != null){
			dto.setWinrmVersion(xmlNodeInfo.getWinrmVersion());
		}

		if(xmlNodeInfo.hasWinrmPort()){
			dto.setWinrmPort((int)xmlNodeInfo.getWinrmPort());
		}

		if(xmlNodeInfo.getWinrmProtocol() != null){
			dto.setWinrmProtocol(OpenApiEnumConverter.stringToEnum(xmlNodeInfo.getWinrmProtocol(),NodeInfoResponse.WinrmProtocolEnum .class));
		}
		

		if(xmlNodeInfo.hasWinrmTimeout()){
			dto.setWinrmTimeout((int)xmlNodeInfo.getWinrmTimeout());
		}
		
		if(xmlNodeInfo.hasWinrmRetryCount()){
			dto.setWinrmRetries((int)xmlNodeInfo.getWinrmRetryCount());
		}
		
		/*SNMP認証関連情報のセット*/
		if(xmlNodeInfo.getSnmpAuthPassword() != null && !"".equals(xmlNodeInfo.getSnmpAuthPassword())){
			String name = Messages.getString("snmp") +":"+ Messages.getString("snmp.auth.password");
			String str = checkString(name, xmlNodeInfo.getSnmpAuthPassword(), 64);
			if(str == null){
				//値に誤りがあることを示すためにFacilityIdに""をセット
				dto.setFacilityId("");
				return dto;
			}
			dto.setSnmpAuthPassword(xmlNodeInfo.getSnmpAuthPassword());
		}

		if(null != xmlNodeInfo.getSnmpAuthProtocol()){
			dto.setSnmpAuthProtocol(OpenApiEnumConverter.stringToEnum(xmlNodeInfo.getSnmpAuthProtocol(),NodeInfoResponse.SnmpAuthProtocolEnum .class));
		}

		if(xmlNodeInfo.getSnmpCommunity() != null){
			dto.setSnmpCommunity(xmlNodeInfo.getSnmpCommunity());
		}
		
		if(xmlNodeInfo.getSnmpPrivPassword() != null && !"".equals(xmlNodeInfo.getSnmpPrivPassword())){
			String name = Messages.getString("snmp") +":"+ Messages.getString("snmp.priv.password");
			String str = checkString(name, xmlNodeInfo.getSnmpPrivPassword(), 64);
			if(str == null){
				//値に誤りがあることを示すためにFacilityIdに""をセット
				dto.setFacilityId("");
				return dto;
			}
			dto.setSnmpPrivPassword(xmlNodeInfo.getSnmpPrivPassword());
		}
		
		if(null != xmlNodeInfo.getSnmpPrivProtocol()){
			dto.setSnmpPrivProtocol(OpenApiEnumConverter.stringToEnum(xmlNodeInfo.getSnmpPrivProtocol(),NodeInfoResponse.SnmpPrivProtocolEnum .class));
		}

		if(null != xmlNodeInfo.getSnmpSecurityLevel()){
			dto.setSnmpSecurityLevel(OpenApiEnumConverter.stringToEnum(xmlNodeInfo.getSnmpSecurityLevel(),NodeInfoResponse.SnmpSecurityLevelEnum .class));
		}

		if(xmlNodeInfo.getSnmpUser() != null && !"".equals(xmlNodeInfo.getSnmpUser())){
			dto.setSnmpUser(xmlNodeInfo.getSnmpUser());
		}
		
		if(xmlNodeInfo.hasSnmpVersion()){
			dto.setSnmpVersion(OpenApiEnumConverter.integerToEnum((int)xmlNodeInfo.getSnmpVersion(),NodeInfoResponse.SnmpVersionEnum .class));
		}
		

		/*SSH認証関連情報のセット*/
		if(xmlNodeInfo.getSshUserPassword() != null && !"".equals(xmlNodeInfo.getSshUserPassword())){
			String name = Messages.getString("ssh") +":"+ Messages.getString("ssh.user.password");
			String str = checkString(name, xmlNodeInfo.getSshUserPassword(), 64);
			if(str == null){
				//値に誤りがあることを示すためにFacilityIdに""をセット
				dto.setFacilityId("");
				return dto;
			}
			dto.setSshUserPassword(xmlNodeInfo.getSshUserPassword());
		}

		if(xmlNodeInfo.getSshPrivateKeyFilename() != null && !"".equals(xmlNodeInfo.getSshPrivateKeyFilename())){
			dto.setSshPrivateKeyFilepath(xmlNodeInfo.getSshPrivateKeyFilename());
		}
		
		if(xmlNodeInfo.getSshPrivateKeyPassphrase() != null && !"".equals(xmlNodeInfo.getSshPrivateKeyPassphrase())){
			String name = Messages.getString("ssh") +":"+ Messages.getString("ssh.private.key.passphrase");
			String str = checkString(name, xmlNodeInfo.getSshPrivateKeyPassphrase(), 1024);
			if(str == null){
				//値に誤りがあることを示すためにFacilityIdに""をセット
				dto.setFacilityId("");
				return dto;
			}
			dto.setSshPrivateKeyPassphrase(xmlNodeInfo.getSshPrivateKeyPassphrase());
		}
		
		if(xmlNodeInfo.hasSshPort()){
			dto.setSshPort((int)xmlNodeInfo.getSshPort());
		}
		if(xmlNodeInfo.hasSshTimeout()){
			dto.setSshTimeout((int)xmlNodeInfo.getSshTimeout());
		}

		if(xmlNodeInfo.getSshUser() != null){
			dto.setSshUser(xmlNodeInfo.getSshUser());
		}

		/*WBEM認証関連情報のセット*/
		if(xmlNodeInfo.getWbemUserPassword() != null && !"".equals(xmlNodeInfo.getWbemUserPassword())){
			String name = Messages.getString("wbem") +":"+ Messages.getString("wbem.user.password");
			String str = checkString(name, xmlNodeInfo.getWbemUserPassword(), 64);
			if(str == null){
				//値に誤りがあることを示すためにFacilityIdに""をセット
				dto.setFacilityId("");
				return dto;
			}
			dto.setWbemUserPassword(xmlNodeInfo.getWbemUserPassword());
		}

		if(xmlNodeInfo.getWbemProtocol() != null){
			dto.setWbemProtocol(OpenApiEnumConverter.stringToEnum(xmlNodeInfo.getWbemProtocol(),NodeInfoResponse.WbemProtocolEnum .class));
		}

		if(xmlNodeInfo.getWbemUser() != null){
			dto.setWbemUser(xmlNodeInfo.getWbemUser());
		}
		
		/*クラウド管理関連情報のセット*/
		if(xmlNodeInfo.getCloudService() != null && !"".equals(xmlNodeInfo.getCloudService())){
			dto.setCloudService(xmlNodeInfo.getCloudService());
		}
		
		if(xmlNodeInfo.getCloudScope() != null && !"".equals(xmlNodeInfo.getCloudScope())){
			dto.setCloudScope(xmlNodeInfo.getCloudScope());
		}
		
		if(xmlNodeInfo.getCloudResourceType() != null && !"".equals(xmlNodeInfo.getCloudResourceType())){
			dto.setCloudResourceType(xmlNodeInfo.getCloudResourceType());
		}
		
		if(xmlNodeInfo.getCloudResourceId() != null && !"".equals(xmlNodeInfo.getCloudResourceId())){
			dto.setCloudResourceId(xmlNodeInfo.getCloudResourceId());
		}
		
		if(xmlNodeInfo.getCloudLocation() != null && !"".equals(xmlNodeInfo.getCloudLocation())){
			dto.setCloudLocation(xmlNodeInfo.getCloudLocation());
		}
		
		if(xmlNodeInfo.getCloudResourceName() != null && !"".equals(xmlNodeInfo.getCloudResourceName())){
			dto.setCloudResourceName(xmlNodeInfo.getCloudResourceName());
		}
		if(xmlNodeInfo.hasCloudLogPriority()){
			dto.setCloudLogPriority((int)xmlNodeInfo.getCloudLogPriority());
		}
		
		/*RPA関連情報のセット*/
		if(xmlNodeInfo.getRpaLogDir() != null && !"".equals(xmlNodeInfo.getRpaLogDir())){
			dto.setRpaLogDir(xmlNodeInfo.getRpaLogDir());
		}
		if(xmlNodeInfo.getRpaManagementToolType() != null && !"".equals(xmlNodeInfo.getRpaManagementToolType())){
			dto.setRpaManagementToolType(xmlNodeInfo.getRpaManagementToolType());
		}
		if(xmlNodeInfo.getRpaResourceId() != null && !"".equals(xmlNodeInfo.getRpaResourceId())){
			dto.setRpaResourceId(xmlNodeInfo.getRpaResourceId());
		}
		if(xmlNodeInfo.getRpaUser() != null && !"".equals(xmlNodeInfo.getRpaUser())){
			dto.setRpaUser(xmlNodeInfo.getRpaUser());
		}
		if(xmlNodeInfo.getRpaExecEnvId() != null && !"".equals(xmlNodeInfo.getRpaExecEnvId())){
			dto.setRpaExecEnvId(xmlNodeInfo.getRpaExecEnvId());
		}
		
		// ホスト名情報の格納
		List<NodeHostnameInfoResponse> nodeHostnameList = dto.getNodeHostnameInfo();
		for (int i = 0; i < hostnameList.length; i++) {
			
			NodeHostnameInfoResponse nodeHostnameInfo = new NodeHostnameInfoResponse();
			
			if (hostnameList[i].getFacilityId().equals(xmlNodeInfo.getFacilityId())) {
				
				if(hostnameList[i].getHostname() != null
						&& !"".equals(hostnameList[i].getHostname())){
					
					nodeHostnameInfo.setHostname(hostnameList[i].getHostname());
					
				}else{
						log.warn(Messages.getString("SettingTools.EssentialValueInvalid")
								+ "(Hostname) : " + xmlNodeInfo.getFacilityId());
						continue;
				}
				
				nodeHostnameList.add(nodeHostnameInfo);
				
			}
		}
		

		// CPU情報の格納
		List<NodeCpuInfoResponse> nodeCPUList = dto.getNodeCpuInfo();
		for (int i = 0; i < cpuList.length; i++) {
			
			NodeCpuInfoResponse nodeCpuInfo = new NodeCpuInfoResponse();
			
			if (cpuList[i].getFacilityId().equals(xmlNodeInfo.getFacilityId())) {
				
				if(cpuList[i].getDeviceDisplayName() != null
						&& !"".equals(cpuList[i].getDeviceDisplayName())){
					
					nodeCpuInfo.setDeviceDisplayName(cpuList[i].getDeviceDisplayName());
					
				}else{
						log.warn(Messages.getString("SettingTools.EssentialValueInvalid")
								+ "(DeviceDisplayName) : " + xmlNodeInfo.getFacilityId());
				}
				
				if(cpuList[i].getDeviceName() != null
					&& !"".equals(cpuList[i].getDeviceName())){
					
					nodeCpuInfo.setDeviceName(cpuList[i].getDeviceName());
					
				}else{
					log.warn(Messages.getString("SettingTools.EssentialValueInvalid")
							+ "(DeviceName) : " + xmlNodeInfo.getFacilityId());
				}
				
				if(cpuList[i].getDeviceIndex() >= 0){
					nodeCpuInfo.setDeviceIndex((int)cpuList[i].getDeviceIndex());
				}else{
					log.warn(Messages.getString("SettingTools.EssentialValueInvalid")
							+ "(DeviceIndex) : " + xmlNodeInfo.getFacilityId());
				}
				
				if(cpuList[i].getDeviceType() != null
						&& !"".equals(cpuList[i].getDeviceType())){
					nodeCpuInfo.setDeviceType(cpuList[i].getDeviceType());
				}
				
				if(cpuList[i].getDeviceSize() >= 0 ){
					
					nodeCpuInfo.setDeviceSize(cpuList[i].getDeviceSize());
					
				}else{
					log.warn(Messages.getString("SettingTools.EssentialValueInvalid")
							+ "(DeviceSize) : " + xmlNodeInfo.getFacilityId());
				}
				
				if(cpuList[i].getDeviceSizeUnit() != null
						&& !"".equals(cpuList[i].getDeviceSizeUnit())){
					
					nodeCpuInfo.setDeviceSizeUnit(cpuList[i].getDeviceSizeUnit());
					
				}
				
				if(cpuList[i].getDeviceDescription() != null
						&& !"".equals(cpuList[i].getDeviceDescription())){
					
					nodeCpuInfo.setDeviceDescription(cpuList[i].getDeviceDescription());
					
				}
				
				if(cpuList[i].getCoreCount() >= 0) {
					nodeCpuInfo.setCoreCount((int)cpuList[i].getCoreCount());
				}
				
				if(cpuList[i].getThreadCount() >= 0) {
					nodeCpuInfo.setThreadCount((int)cpuList[i].getThreadCount());
				}
		
				if(cpuList[i].getClockCount() >= 0) {
					nodeCpuInfo.setClockCount((int)cpuList[i].getClockCount());
				}
				nodeCPUList.add(nodeCpuInfo);
				
			}
		}
		

		// メモリ情報の格納
		List<NodeMemoryInfoResponse> nodeMemoryList = dto.getNodeMemoryInfo();
		for (int i = 0; i < memoryList.length; i++) {
			
			NodeMemoryInfoResponse nodeMemoryInfo = new NodeMemoryInfoResponse();
			
			if (memoryList[i].getFacilityId().equals(xmlNodeInfo.getFacilityId())) {
				
				if(memoryList[i].getDeviceDisplayName() != null
						&& !"".equals(memoryList[i].getDeviceDisplayName())){
					
					nodeMemoryInfo.setDeviceDisplayName(memoryList[i].getDeviceDisplayName());
					
				}else{
						log.warn(Messages.getString("SettingTools.EssentialValueInvalid")
								+ "(DeviceDisplayName) : " + xmlNodeInfo.getFacilityId());
				}
				
				if(memoryList[i].getDeviceName() != null
					&& !"".equals(memoryList[i].getDeviceName())){
					
					nodeMemoryInfo.setDeviceName(memoryList[i].getDeviceName());
					
				}else{
					log.warn(Messages.getString("SettingTools.EssentialValueInvalid")
							+ "(DeviceName) : " + xmlNodeInfo.getFacilityId());
				}
				
				if(memoryList[i].getDeviceIndex() >= 0){
					nodeMemoryInfo.setDeviceIndex((int)memoryList[i].getDeviceIndex());
				}else{
					log.warn(Messages.getString("SettingTools.EssentialValueInvalid")
							+ "(DeviceIndex) : " + xmlNodeInfo.getFacilityId());
				}
				
				if(memoryList[i].getDeviceType() != null
						&& !"".equals(memoryList[i].getDeviceType())){
					nodeMemoryInfo.setDeviceType(memoryList[i].getDeviceType());
				}
				
				if(memoryList[i].getDeviceSize() >= 0 ){
					
					nodeMemoryInfo.setDeviceSize(memoryList[i].getDeviceSize());
					
				}else{
					log.warn(Messages.getString("SettingTools.EssentialValueInvalid")
							+ "(DeviceSize) : " + xmlNodeInfo.getFacilityId());
				}
				
				if(memoryList[i].getDeviceSizeUnit() != null
						&& !"".equals(memoryList[i].getDeviceSizeUnit())){
					
					nodeMemoryInfo.setDeviceSizeUnit(memoryList[i].getDeviceSizeUnit());
					
				}
				
				if(memoryList[i].getDeviceDescription() != null
						&& !"".equals(memoryList[i].getDeviceDescription())){
					
					nodeMemoryInfo.setDeviceDescription(memoryList[i].getDeviceDescription());
					
				}
				
				nodeMemoryList.add(nodeMemoryInfo);
				
			}
		}
		

		// ネットワークインタフェース情報の格納
		List<NodeNetworkInterfaceInfoResponse> nodeNetworkInterfaceList = dto.getNodeNetworkInterfaceInfo();
		for (int i = 0; i < networkInterfaceList.length; i++) {
			
			NodeNetworkInterfaceInfoResponse nodeNetworkInterfaceInfo = new NodeNetworkInterfaceInfoResponse();
			
			if (networkInterfaceList[i].getFacilityId().equals(xmlNodeInfo.getFacilityId())) {
				
				if(networkInterfaceList[i].getDeviceDisplayName() != null
						&& !"".equals(networkInterfaceList[i].getDeviceDisplayName())){
					
					nodeNetworkInterfaceInfo.setDeviceDisplayName(networkInterfaceList[i].getDeviceDisplayName());
					
				}else{
						log.warn(Messages.getString("SettingTools.EssentialValueInvalid")
								+ "(DeviceDisplayName) : " + xmlNodeInfo.getFacilityId());
				}
				
				if(networkInterfaceList[i].getDeviceName() != null
					&& !"".equals(networkInterfaceList[i].getDeviceName())){
					
					nodeNetworkInterfaceInfo.setDeviceName(networkInterfaceList[i].getDeviceName());
					
				}else{
					log.warn(Messages.getString("SettingTools.EssentialValueInvalid")
							+ "(DeviceName) : " + xmlNodeInfo.getFacilityId());
				}
				
				if(networkInterfaceList[i].getDeviceIndex() >= 0){
					nodeNetworkInterfaceInfo.setDeviceIndex((int)networkInterfaceList[i].getDeviceIndex());
				}else{
					log.warn(Messages.getString("SettingTools.EssentialValueInvalid")
							+ "(DeviceIndex) : " + xmlNodeInfo.getFacilityId());
				}
				
				if(networkInterfaceList[i].getDeviceType() != null
						&& !"".equals(networkInterfaceList[i].getDeviceType())){
					nodeNetworkInterfaceInfo.setDeviceType(networkInterfaceList[i].getDeviceType());
				}
				
				if(networkInterfaceList[i].getDeviceSize() >= 0 ){
					
					nodeNetworkInterfaceInfo.setDeviceSize(networkInterfaceList[i].getDeviceSize());
					
				}else{
					log.warn(Messages.getString("SettingTools.EssentialValueInvalid")
							+ "(DeviceSize) : " + xmlNodeInfo.getFacilityId());
				}
				
				if(networkInterfaceList[i].getDeviceSizeUnit() != null
						&& !"".equals(networkInterfaceList[i].getDeviceSizeUnit())){
					
					nodeNetworkInterfaceInfo.setDeviceSizeUnit(networkInterfaceList[i].getDeviceSizeUnit());
					
				}
				
				if(networkInterfaceList[i].getDeviceDescription() != null
						&& !"".equals(networkInterfaceList[i].getDeviceDescription())){
					
					nodeNetworkInterfaceInfo.setDeviceDescription(networkInterfaceList[i].getDeviceDescription());
					
				}
				
				if(networkInterfaceList[i].getDeviceNicIpAddress() != null
						&& !"".equals(networkInterfaceList[i].getDeviceNicIpAddress())){
					
					nodeNetworkInterfaceInfo.setNicIpAddress(networkInterfaceList[i].getDeviceNicIpAddress());
					
				}
				
				if(networkInterfaceList[i].getDeviceNicMacAddress() != null
						&& !"".equals(networkInterfaceList[i].getDeviceNicMacAddress())){
					
					nodeNetworkInterfaceInfo.setNicMacAddress(networkInterfaceList[i].getDeviceNicMacAddress());
					
				}
				
				nodeNetworkInterfaceList.add(nodeNetworkInterfaceInfo);
				
			}
		}
		
		// ディスク情報の格納
		List<NodeDiskInfoResponse> nodeDiskList = dto.getNodeDiskInfo();
		for (int i = 0; i < diskList.length; i++) {
			
			NodeDiskInfoResponse nodeDiskInfo = new NodeDiskInfoResponse();
			
			if (diskList[i].getFacilityId().equals(xmlNodeInfo.getFacilityId())) {
				
				if(diskList[i].getDeviceDisplayName() != null
						&& !"".equals(diskList[i].getDeviceDisplayName())){
					
					nodeDiskInfo.setDeviceDisplayName(diskList[i].getDeviceDisplayName());
					
				}else{
						log.warn(Messages.getString("SettingTools.EssentialValueInvalid")
								+ "(DeviceDisplayName) : " + xmlNodeInfo.getFacilityId());
				}
				
				if(diskList[i].getDeviceName() != null
					&& !"".equals(diskList[i].getDeviceName())){
					
					nodeDiskInfo.setDeviceName(diskList[i].getDeviceName());
					
				}else{
					log.warn(Messages.getString("SettingTools.EssentialValueInvalid")
							+ "(DeviceName) : " + xmlNodeInfo.getFacilityId());
				}
				
				if(diskList[i].getDeviceIndex() >= 0){
					nodeDiskInfo.setDeviceIndex((int)diskList[i].getDeviceIndex());
				}else{
					log.warn(Messages.getString("SettingTools.EssentialValueInvalid")
							+ "(DeviceIndex) : " + xmlNodeInfo.getFacilityId());
				}
				
				if(diskList[i].getDeviceType() != null
						&& !"".equals(diskList[i].getDeviceType())){
					nodeDiskInfo.setDeviceType(diskList[i].getDeviceType());
				}
				
				if(diskList[i].getDeviceSize() >= 0 ){
					
					nodeDiskInfo.setDeviceSize(diskList[i].getDeviceSize());
					
				}else{
					log.warn(Messages.getString("SettingTools.EssentialValueInvalid")
							+ "(DeviceSize) : " + xmlNodeInfo.getFacilityId());
				}
				
				if(diskList[i].getDeviceSizeUnit() != null
						&& !"".equals(diskList[i].getDeviceSizeUnit())){
					
					nodeDiskInfo.setDeviceSizeUnit(diskList[i].getDeviceSizeUnit());
					
				}
				
				if(diskList[i].getDeviceDescription() != null
						&& !"".equals(diskList[i].getDeviceDescription())){
					
					nodeDiskInfo.setDeviceDescription(diskList[i].getDeviceDescription());
					
				}
				
				if(diskList[i].getDeviceDiskRpm() >= 0){
					
					nodeDiskInfo.setDiskRpm((int)diskList[i].getDeviceDiskRpm());
					
				}else{
					log.warn(Messages.getString("SettingTools.EssentialValueInvalid")
							+ "(DeviceDiskRpm) : " + xmlNodeInfo.getFacilityId());
				}

				nodeDiskList.add(nodeDiskInfo);
				
			}
		}
		

		// ファイルシステム情報の格納
		List<NodeFilesystemInfoResponse> nodeFilesystemList = dto.getNodeFilesystemInfo();
		for (int i = 0; i < fsList.length; i++) {
			
			NodeFilesystemInfoResponse nodeFilesystemInfo = new NodeFilesystemInfoResponse();
			
			if (fsList[i].getFacilityId().equals(xmlNodeInfo.getFacilityId())) {
				if(fsList[i].getDeviceIndex() >= 0){
					nodeFilesystemInfo.setDeviceIndex((int)fsList[i].getDeviceIndex());
				}else{
					log.warn(Messages.getString("SettingTools.EssentialValueInvalid")
							+ "(DeviceIndex) : " + xmlNodeInfo.getFacilityId());
					continue;
				}
				
				if(fsList[i].getDeviceType() != null
						&& !"".equals(fsList[i].getDeviceType())){
					nodeFilesystemInfo.setDeviceType(fsList[i].getDeviceType());
				}
				
				if(fsList[i].getDeviceName() != null
					&& !"".equals(fsList[i].getDeviceName())){
					
					nodeFilesystemInfo.setDeviceName(fsList[i].getDeviceName());
					
				}else{
					log.warn(Messages.getString("SettingTools.EssentialValueInvalid")
							+ "(DeviceName) : " + xmlNodeInfo.getFacilityId());
				}
				
				if(fsList[i].getDeviceDisplayName() != null
						&& !"".equals(fsList[i].getDeviceDisplayName())){
					
					nodeFilesystemInfo.setDeviceDisplayName(fsList[i].getDeviceDisplayName());
					
				}else{
						log.warn(Messages.getString("SettingTools.EssentialValueInvalid")
								+ "(DeviceDisplayName) : " + xmlNodeInfo.getFacilityId());
				}
				
				if(fsList[i].getDeviceSize() >= 0 ){
					
					nodeFilesystemInfo.setDeviceSize(fsList[i].getDeviceSize());
					
				}else{
					log.warn(Messages.getString("SettingTools.EssentialValueInvalid")
							+ "(DeviceSize) : " + xmlNodeInfo.getFacilityId());
				}
				
				if(fsList[i].getDeviceSizeUnit() != null
						&& !"".equals(fsList[i].getDeviceSizeUnit())){
					
					nodeFilesystemInfo.setDeviceSizeUnit(fsList[i].getDeviceSizeUnit());
					
				}
				
				if(fsList[i].getDeviceDescription() != null
						&& !"".equals(fsList[i].getDeviceDescription())){
					
					nodeFilesystemInfo.setDeviceDescription(fsList[i].getDeviceDescription());
					
				}
				
				if(fsList[i].getDeviceFSType() != null
						&& !"".equals(fsList[i].getDeviceFSType())){
					nodeFilesystemInfo.setFilesystemType(fsList[i].getDeviceFSType());
				}
				
				nodeFilesystemList.add(nodeFilesystemInfo);
				
			}
		}
		
		// 汎用デバイス情報の格納
		List<NodeGeneralDeviceInfoResponse> nodeDeviceList = dto.getNodeDeviceInfo();
		for (int i = 0; i < deviceList.length; i++) {
			
			NodeGeneralDeviceInfoResponse nodeDeviceInfo = new NodeGeneralDeviceInfoResponse();
			
			if (deviceList[i].getFacilityId().equals(xmlNodeInfo.getFacilityId())) {
				
				if(deviceList[i].getDeviceDisplayName() != null
						&& !"".equals(deviceList[i].getDeviceDisplayName())){
					
					nodeDeviceInfo.setDeviceDisplayName(deviceList[i].getDeviceDisplayName());
					
				}else{
						log.warn(Messages.getString("SettingTools.EssentialValueInvalid")
								+ "(DeviceDisplayName) : " + xmlNodeInfo.getFacilityId());
				}
				
				if(deviceList[i].getDeviceName() != null
					&& !"".equals(deviceList[i].getDeviceName())){
					
					nodeDeviceInfo.setDeviceName(deviceList[i].getDeviceName());
					
				}else{
					log.warn(Messages.getString("SettingTools.EssentialValueInvalid")
							+ "(DeviceName) : " + xmlNodeInfo.getFacilityId());
				}
				

				if(deviceList[i].getDeviceIndex() >= 0 ){
					
					nodeDeviceInfo.setDeviceIndex((int)deviceList[i].getDeviceIndex());
					
				}else{
					log.warn(Messages.getString("SettingTools.EssentialValueInvalid")
							+ "(DeviceIndex) : " + xmlNodeInfo.getFacilityId());
				}
				
				if(deviceList[i].getDeviceType() != null
						&& !"".equals(deviceList[i].getDeviceType())){
					
					nodeDeviceInfo.setDeviceType(deviceList[i].getDeviceType());
					
				}
				
				if(deviceList[i].getDeviceSize() >= 0 ){
					
					nodeDeviceInfo.setDeviceSize(deviceList[i].getDeviceSize());
					
				}else{
					log.warn(Messages.getString("SettingTools.EssentialValueInvalid")
							+ "(DeviceSize) : " + xmlNodeInfo.getFacilityId());
				}
				
				if(deviceList[i].getDeviceSizeUnit() != null
						&& !"".equals(deviceList[i].getDeviceSizeUnit())){
					
					nodeDeviceInfo.setDeviceSizeUnit(deviceList[i].getDeviceSizeUnit());
					
				}
				
				if(deviceList[i].getDeviceDescription() != null
						&& !"".equals(deviceList[i].getDeviceDescription())){
					
					nodeDeviceInfo.setDeviceDescription(deviceList[i].getDeviceDescription());
					
				}
				
				nodeDeviceList.add(nodeDeviceInfo);
				
			}
		}
		
		// ノード変数情報の格納
		List<NodeVariableInfoResponse> nodeVariableList = dto.getNodeVariableInfo();
		for (int i = 0; i < variableList.length; i++) {
			
			NodeVariableInfoResponse nodeVariableInfo = new NodeVariableInfoResponse();
			
			if (variableList[i].getFacilityId().equals(xmlNodeInfo.getFacilityId())) {
				
				if(variableList[i].getNodeVariableName() != null
						&& !"".equals(variableList[i].getNodeVariableName())){
					
					nodeVariableInfo.setNodeVariableName(variableList[i].getNodeVariableName());
				}
				
				if(variableList[i].getNodeVariableValue() != null
						&& !"".equals(variableList[i].getNodeVariableValue())){
					
					nodeVariableInfo.setNodeVariableValue(variableList[i].getNodeVariableValue());
				}
				
				nodeVariableList.add(nodeVariableInfo);
				
			}
		}
		
		// 備考情報の格納
		List<NodeNoteInfoResponse> nodeNoteList = dto.getNodeNoteInfo();
		for (int i = 0; i < noteList.length; i++) {
			
			NodeNoteInfoResponse nodeNoteInfo = new NodeNoteInfoResponse();
			
			if (noteList[i].getFacilityId().equals(xmlNodeInfo.getFacilityId())) {
				
				if(noteList[i].getNoteId() >= 0){
					
					nodeNoteInfo.setNoteId((int)noteList[i].getNoteId());

				}else{
					log.warn(Messages.getString("SettingTools.EssentialValueInvalid")
							+ "(NoteId) : " + xmlNodeInfo.getFacilityId());
				}
				
				if(noteList[i].getNote() != null){
					
					nodeNoteInfo.setNote(noteList[i].getNote());
				}
				
				nodeNoteList.add(nodeNoteInfo);
				
			}
		}
		
		return dto;
		
	}

	/**
	 * スコープ情報がHinemosのシステムスコープと重複しているかどうかを確認する<br>
	 * 
	 * @param scopeInfo
	 *            スコープ情報オブジェクト
	 * @return システムスコープの場合はtrue, それ以外はfalseを返す
	 */
	public static boolean checkInternalScope(String facilityId) {

		if (facilityId.equals("INTERNAL") ||
				facilityId.equals("REGISTERED") ||
				facilityId.equals("UNREGISTERED") ||
				facilityId.equals("OS") ||
				facilityId.equals("OWNER") ||
				facilityId.equals("_PRIVATE_CLOUD") ||
				facilityId.equals("_PUBLIC_CLOUD") ||
				facilityId.equals(RpaConstants.RPA) ||
				facilityId.equals(RpaConstants.RPA_NO_MGR_UIPATH) ||
				facilityId.equals(RpaConstants.RPA_NO_MGR_WINACTOR) ||
				facilityId.equals("NODE_CONFIGURATION")) {
			return true;
		} else {
			return false;
		}

	}
	
	
	public static void convDto2Xml(NodeInfoResponse dto, NodeInfo nodeInfo) {

		nodeInfo.setFacilityId(dto.getFacilityId());
		nodeInfo.setFacilityName(dto.getFacilityName());
		
		nodeInfo.setDescription(dto.getDescription());
		nodeInfo.setOwnerRoleId(dto.getOwnerRoleId());
		nodeInfo.setPlatformFamily(dto.getPlatformFamily());
		nodeInfo.setSubPlatformFamily(dto.getSubPlatformFamily());
		nodeInfo.setValidFlg(dto.getValid());
		
		if (dto.getIconImage()!= null) {
			nodeInfo.setIconImage(dto.getIconImage());
		}
		
		//SNMP関連
		if (dto.getSnmpPort() != null && checkInteger(dto.getSnmpPort()) != null) {
			nodeInfo.setSnmpPort(checkInteger(dto.getSnmpPort()));
		}

		if (dto.getSnmpCommunity() != null) {
			nodeInfo.setSnmpCommunity(dto.getSnmpCommunity());
		}

		nodeInfo.setSnmpVersion(OpenApiEnumConverter.enumToInteger( dto.getSnmpVersion()));

		if (dto.getSnmpRetryCount() != null && checkInteger(dto.getSnmpRetryCount()) != null) {
			nodeInfo.setSnmpRetryCount(checkInteger(dto.getSnmpRetryCount()));
		}

		if (dto.getSnmpTimeout() != null && checkInteger(dto.getSnmpTimeout()) != null) {
			nodeInfo.setSnmpTimeout(checkInteger(dto.getSnmpTimeout()));
		}

		//WBEM関連
		if (dto.getWbemUser() != null) {
			nodeInfo.setWbemUser(dto.getWbemUser());
		}
		
		if (dto.getWbemUserPassword() != null) {
			nodeInfo.setWbemUserPassword(dto.getWbemUserPassword());
		}

		if (dto.getWbemPort() != null && checkInteger(dto.getWbemPort()) != null) {
			nodeInfo.setWbemPort(checkInteger(dto.getWbemPort()));
		}
		if (dto.getWbemProtocol() != null) {
			nodeInfo.setWbemProtocol(OpenApiEnumConverter.enumToString(dto.getWbemProtocol()));
		}
		if (dto.getWbemTimeout() != null && checkInteger(dto.getWbemTimeout()) != null) {
			nodeInfo.setWbemTimeout(checkInteger(dto.getWbemTimeout()));
		}
		if (dto.getWbemRetryCount() != null && checkInteger(dto.getWbemRetryCount()) != null) {
			nodeInfo.setWbemRetryCount(checkInteger(dto.getWbemRetryCount()));
		}
		
		nodeInfo.setIpAddressVersion(OpenApiEnumConverter.enumToInteger(dto.getIpAddressVersion()));
		nodeInfo.setIpAddressV4(dto.getIpAddressV4());
		nodeInfo.setIpAddressV6(dto.getIpAddressV6());
		nodeInfo.setNodeName(dto.getNodeName());
		nodeInfo.setHardwareType(dto.getHardwareType());
		nodeInfo.setAdministrator(dto.getAdministrator());
		nodeInfo.setContact(dto.getContact());

		String osName = null;
		String osRelease = null;
		String osVersion = null;
		String characterSet = null;
		String startupDateTime = null;
		if (dto.getNodeOsInfo() != null) {
			osName = dto.getNodeOsInfo().getOsName();
			osRelease = dto.getNodeOsInfo().getOsRelease();
			osVersion = dto.getNodeOsInfo().getOsVersion();
			characterSet = dto.getNodeOsInfo().getCharacterSet();
			startupDateTime = dto.getNodeOsInfo().getStartupDateTime();
		}
		nodeInfo.setOsName(osName);
		nodeInfo.setOsRelease(osRelease);
		nodeInfo.setOsVersion(osVersion);
		nodeInfo.setCharacterSet(characterSet);
		if (startupDateTime != null) {
			nodeInfo.setStartupDateTime(startupDateTime);
		}
		
		//Hinemosエージェント関連情報セット
		if (dto.getAgentAwakePort() != null && checkInteger(dto.getAgentAwakePort()) != null) {
			nodeInfo.setAgentAwakePort(checkInteger(dto.getAgentAwakePort()));
		}
		
		//ジョブ関連
		if (dto.getJobMultiplicity() != null && checkInteger(dto.getJobMultiplicity()) != null) {
			nodeInfo.setJobMultiplicity(checkInteger(dto.getJobMultiplicity()));
		}
		if (dto.getJobPriority() != null && checkInteger(dto.getJobPriority()) != null) {
			nodeInfo.setJobPriority(checkInteger(dto.getJobPriority()));
		}

		// IPMI関連
		if (dto.getIpmiIpAddress() != null) {
			nodeInfo.setIpmiIpAddress(dto.getIpmiIpAddress());
		}
		Integer ipmiPort = dto.getIpmiPort();
		if (checkInteger(ipmiPort) != null) {
			nodeInfo.setIpmiPort(ipmiPort.intValue());
		}

		if (dto.getIpmiUser() != null) {
			nodeInfo.setIpmiUser(dto.getIpmiUser());
		}
		
		if (dto.getIpmiUserPassword() != null) {
			nodeInfo.setIpmiUserPassword(dto.getIpmiUserPassword());
		}
		Integer ipmiTimeout = dto.getIpmiTimeout();
		if (checkInteger(ipmiTimeout) != null) {
			nodeInfo.setIpmiTimeout(ipmiTimeout.intValue());
		}
		Integer ipmiRetryCount = dto.getIpmiRetries();
		if (checkInteger(ipmiRetryCount) != null) {
			nodeInfo.setIpmiRetryCount(ipmiRetryCount.intValue());
		}

		if (dto.getIpmiProtocol() != null) {
			nodeInfo.setIpmiProtocol(dto.getIpmiProtocol());
		}

		if (dto.getIpmiLevel() != null) {
			nodeInfo.setIpmiLevel(dto.getIpmiLevel());
		}

		// WinRM関連
		if (dto.getWinrmUser() != null) {
			nodeInfo.setWinrmUser(dto.getWinrmUser());
		}
		
		if (dto.getWinrmUserPassword() != null) {
			nodeInfo.setWinrmUserPassword(dto.getWinrmUserPassword());
		}

		if (dto.getWinrmVersion() != null) {
			nodeInfo.setWinrmVersion(dto.getWinrmVersion());
		}

		Integer winrmPort = dto.getWinrmPort();
		if (checkInteger(winrmPort) != null) {
			nodeInfo.setWinrmPort(winrmPort.intValue());
		}

		if (dto.getWinrmProtocol() != null) {
			nodeInfo.setWinrmProtocol(OpenApiEnumConverter.enumToString(dto.getWinrmProtocol()));
		}
		Integer winrmTimeout = dto.getWinrmTimeout();
		if (checkInteger(winrmTimeout) != null) {
			nodeInfo.setWinrmTimeout(winrmTimeout.intValue());
		}

		Integer winrmRetryCount = dto.getWinrmRetries();
		if (checkInteger(winrmRetryCount) != null) {
			nodeInfo.setWinrmRetryCount(winrmRetryCount.intValue());
		}

		nodeInfo.setAutoDeviceSearch(dto.getAutoDeviceSearch());
		
		/*SNMP認証関連情報のセット*/
		if(dto.getSnmpAuthPassword() != null){
			nodeInfo.setSnmpAuthPassword(dto.getSnmpAuthPassword());
		}

		if(dto.getSnmpAuthProtocol() != null){
			nodeInfo.setSnmpAuthProtocol(OpenApiEnumConverter.enumToString(dto.getSnmpAuthProtocol()));
		}
		
		if(dto.getSnmpCommunity() != null){
			nodeInfo.setSnmpAuthProtocol(OpenApiEnumConverter.enumToString(dto.getSnmpAuthProtocol()));
		}
		
		nodeInfo.setSnmpPort(dto.getSnmpPort());
		nodeInfo.setSnmpRetryCount(dto.getSnmpRetryCount());
		nodeInfo.setSnmpTimeout(dto.getSnmpTimeout());
		
		if(dto.getSnmpPrivPassword() != null){
			nodeInfo.setSnmpPrivPassword(dto.getSnmpPrivPassword());
		}
		
		if(dto.getSnmpPrivProtocol() != null){
			nodeInfo.setSnmpPrivProtocol(OpenApiEnumConverter.enumToString(dto.getSnmpPrivProtocol()));
		}
		
		if(dto.getSnmpSecurityLevel() != null){
			nodeInfo.setSnmpSecurityLevel(OpenApiEnumConverter.enumToString(dto.getSnmpSecurityLevel()));
		}

		if(dto.getSnmpUser() != null){
			nodeInfo.setSnmpUser(dto.getSnmpUser());
		}
		
		if(dto.getSnmpVersion() != null){
			nodeInfo.setSnmpVersion(OpenApiEnumConverter.enumToInteger(dto.getSnmpVersion()));
		}
		

		/*SSH認証関連情報のセット*/
		if(dto.getSshUserPassword() != null){
			nodeInfo.setSshUserPassword(dto.getSshUserPassword());
		}

		if(dto.getSshPrivateKeyFilepath() != null){
			nodeInfo.setSshPrivateKeyFilename(dto.getSshPrivateKeyFilepath());
		}
		
		if(dto.getSshPrivateKeyPassphrase() != null){
			nodeInfo.setSshPrivateKeyPassphrase(dto.getSshPrivateKeyPassphrase());
		}
		
		nodeInfo.setSshPort(dto.getSshPort());
		nodeInfo.setSshTimeout(dto.getSshTimeout());

		if(dto.getSshUser() != null){
			nodeInfo.setSshUser(dto.getSshUser());
		}

		/*WBEM認証関連情報のセット*/
		if(dto.getWbemUserPassword() != null){
			nodeInfo.setWbemUserPassword(dto.getWbemUserPassword());
		}

		if(dto.getWbemProtocol() != null){
			nodeInfo.setWbemProtocol(OpenApiEnumConverter.enumToString(dto.getWbemProtocol()));
		}
		
		nodeInfo.setWbemPort(dto.getWbemPort());
		nodeInfo.setWbemTimeout(dto.getWbemTimeout());
		nodeInfo.setWbemRetryCount(dto.getWbemRetryCount());

		if(dto.getWbemUser() != null){
			nodeInfo.setWbemUser(dto.getWbemUser());
		}
		
		/*クラウド管理関連情報のセット*/
		if(dto.getCloudService() != null){
			nodeInfo.setCloudService(dto.getCloudService());
		}
		
		if(dto.getCloudScope() != null){
			nodeInfo.setCloudScope(dto.getCloudScope());
		}
		
		if(dto.getCloudResourceType() != null){
			nodeInfo.setCloudResourceType(dto.getCloudResourceType());
		}
		
		if(dto.getCloudResourceId() != null){
			nodeInfo.setCloudResourceId(dto.getCloudResourceId());
		}
		
		if(dto.getCloudLocation() != null){
			nodeInfo.setCloudLocation(dto.getCloudLocation());
		}
		
		if(dto.getCloudResourceName() != null){
			nodeInfo.setCloudResourceName(dto.getCloudResourceName());
		}
		nodeInfo.setCloudLogPriority(dto.getCloudLogPriority());

		/*RPA関連情報のセット*/
		if(dto.getRpaLogDir() != null){
			nodeInfo.setRpaLogDir(dto.getRpaLogDir());
		}
		if(dto.getRpaManagementToolType() != null){
			nodeInfo.setRpaManagementToolType(dto.getRpaManagementToolType());
		}
		if(dto.getRpaResourceId() != null){
			nodeInfo.setRpaResourceId(dto.getRpaResourceId());
		}
		if(dto.getRpaUser() != null){
			nodeInfo.setRpaUser(dto.getRpaUser());
		}
		if(dto.getRpaExecEnvId() != null){
			nodeInfo.setRpaExecEnvId(dto.getRpaExecEnvId());
		}

	}

	public static Collection<HostnameInfo> convHostnameDto2Xml(NodeInfoResponse dto){
		
		ArrayList<HostnameInfo> hostnameList = new ArrayList<HostnameInfo>();
		
		HostnameInfo xmlHostnameInfo = null;
		
		// ノード内ホスト名情報の取得
		List<NodeHostnameInfoResponse> nodeHostnameInfoList = dto.getNodeHostnameInfo();
		Iterator<NodeHostnameInfoResponse> itrNodeHostnameInfoList = nodeHostnameInfoList.iterator();
		while (itrNodeHostnameInfoList.hasNext()) {

			NodeHostnameInfoResponse nodeHostnameInfo = itrNodeHostnameInfoList.next();
			
			if (nodeHostnameInfo.getHostname() != null && !nodeHostnameInfo.getHostname().equals("")) {

				xmlHostnameInfo = new HostnameInfo();

				xmlHostnameInfo.setFacilityId(dto.getFacilityId());

				xmlHostnameInfo.setHostname(nodeHostnameInfo.getHostname());

				hostnameList.add(xmlHostnameInfo);
			}

		}

		return hostnameList;
	}

	public static Collection<CPUInfo> convCPUDto2Xml(NodeInfoResponse dto){
		
		ArrayList<CPUInfo> cpuList = new ArrayList<CPUInfo>();
		
		CPUInfo xmlCPUInfo = null;
		
		// ノード内CPU情報の取得
		List<NodeCpuInfoResponse> nodeCpuInfoList = dto.getNodeCpuInfo();
		Iterator<NodeCpuInfoResponse> itrNodeCpuInfoList = nodeCpuInfoList.iterator();
		while (itrNodeCpuInfoList.hasNext()) {

			NodeCpuInfoResponse nodeCpuInfo = itrNodeCpuInfoList.next();
			
			if (nodeCpuInfo.getDeviceName() != null && !nodeCpuInfo.getDeviceName().equals("")) {
				xmlCPUInfo = new CPUInfo();
				xmlCPUInfo.setFacilityId(dto.getFacilityId());
				xmlCPUInfo.setDeviceDisplayName(nodeCpuInfo.getDeviceDisplayName());
				xmlCPUInfo.setDeviceName(nodeCpuInfo.getDeviceName());
				Integer deviceIndex = nodeCpuInfo.getDeviceIndex();
				if (checkInteger(deviceIndex) != null) {
					xmlCPUInfo.setDeviceIndex(deviceIndex.intValue());
				}
				xmlCPUInfo.setDeviceType(nodeCpuInfo.getDeviceType());
				Long deviceSize = nodeCpuInfo.getDeviceSize();
				if (checkLong(deviceSize) != null) {
					xmlCPUInfo.setDeviceSize(deviceSize.longValue());
				}
				xmlCPUInfo.setDeviceSizeUnit(nodeCpuInfo.getDeviceSizeUnit());
				xmlCPUInfo.setDeviceDescription(nodeCpuInfo.getDeviceDescription());
				xmlCPUInfo.setCoreCount(nodeCpuInfo.getCoreCount());
				xmlCPUInfo.setThreadCount(nodeCpuInfo.getThreadCount());
				xmlCPUInfo.setClockCount(nodeCpuInfo.getClockCount());
				cpuList.add(xmlCPUInfo);
			}

		}

		return cpuList;
	}

	public static Collection<MemoryInfo> convMemoryDto2Xml(NodeInfoResponse dto){
		
		ArrayList<MemoryInfo> memoryList = new ArrayList<MemoryInfo>();
		
		MemoryInfo xmlMemoryInfo = null;
		
		// ノード内メモリ情報の取得
		List<NodeMemoryInfoResponse> nodeMemoryInfoList = dto.getNodeMemoryInfo();
		Iterator<NodeMemoryInfoResponse> itrNodeMemoryInfoList = nodeMemoryInfoList.iterator();
		while (itrNodeMemoryInfoList.hasNext()) {

			NodeMemoryInfoResponse nodeMemoryInfo = itrNodeMemoryInfoList.next();
			
			if (nodeMemoryInfo.getDeviceName() != null && !nodeMemoryInfo.getDeviceName().equals("")) {
				xmlMemoryInfo = new MemoryInfo();
				xmlMemoryInfo.setFacilityId(dto.getFacilityId());
				xmlMemoryInfo.setDeviceDisplayName(nodeMemoryInfo.getDeviceDisplayName());
				xmlMemoryInfo.setDeviceName(nodeMemoryInfo.getDeviceName());

				Integer deviceIndex = nodeMemoryInfo.getDeviceIndex();
				if (checkInteger(deviceIndex) != null) {
					xmlMemoryInfo.setDeviceIndex(deviceIndex.intValue());
				}
				xmlMemoryInfo.setDeviceType(nodeMemoryInfo.getDeviceType());
				Long deviceSize = nodeMemoryInfo.getDeviceSize();
				if (checkLong(deviceSize) != null) {
					xmlMemoryInfo.setDeviceSize(deviceSize.longValue());
				}
				xmlMemoryInfo.setDeviceSizeUnit(nodeMemoryInfo.getDeviceSizeUnit());
				xmlMemoryInfo.setDeviceDescription(nodeMemoryInfo.getDeviceDescription());
				memoryList.add(xmlMemoryInfo);
			}

		}

		return memoryList;
	}

	public static Collection<NetworkInterfaceInfo> convNetworkInterfaceDto2Xml(NodeInfoResponse dto){
		
		ArrayList<NetworkInterfaceInfo> networkInterfaceList = new ArrayList<NetworkInterfaceInfo>();
		
		NetworkInterfaceInfo xmlNetworkInterfaceInfo = null;
		
		// ノード内ネットワークインタフェース情報の取得
		List<NodeNetworkInterfaceInfoResponse> nodeNetworkInterfaceInfoList = dto.getNodeNetworkInterfaceInfo();
		Iterator<NodeNetworkInterfaceInfoResponse> itrNodeNetworkInterfaceInfoList = nodeNetworkInterfaceInfoList.iterator();
		while (itrNodeNetworkInterfaceInfoList.hasNext()) {

			NodeNetworkInterfaceInfoResponse nodeNetworkInterfaceInfo = itrNodeNetworkInterfaceInfoList.next();
			
			if (nodeNetworkInterfaceInfo.getDeviceName() != null && !nodeNetworkInterfaceInfo.getDeviceName().equals("")) {
				xmlNetworkInterfaceInfo = new NetworkInterfaceInfo();
				xmlNetworkInterfaceInfo.setFacilityId(dto.getFacilityId());
				xmlNetworkInterfaceInfo.setDeviceDisplayName(nodeNetworkInterfaceInfo.getDeviceDisplayName());
				xmlNetworkInterfaceInfo.setDeviceName(nodeNetworkInterfaceInfo.getDeviceName());
				Integer deviceIndex = nodeNetworkInterfaceInfo.getDeviceIndex();
				if (checkInteger(deviceIndex) != null) {
					xmlNetworkInterfaceInfo.setDeviceIndex(deviceIndex.intValue());
				}
				xmlNetworkInterfaceInfo.setDeviceType(nodeNetworkInterfaceInfo.getDeviceType());
				Long deviceSize = nodeNetworkInterfaceInfo.getDeviceSize();
				if (checkLong(deviceSize) != null) {
					xmlNetworkInterfaceInfo.setDeviceSize(deviceSize.longValue());
				}
				xmlNetworkInterfaceInfo.setDeviceSizeUnit(nodeNetworkInterfaceInfo.getDeviceSizeUnit());
				xmlNetworkInterfaceInfo.setDeviceDescription(nodeNetworkInterfaceInfo.getDeviceDescription());
				xmlNetworkInterfaceInfo.setDeviceNicIpAddress(nodeNetworkInterfaceInfo.getNicIpAddress());
				xmlNetworkInterfaceInfo.setDeviceNicMacAddress(nodeNetworkInterfaceInfo.getNicMacAddress());
				networkInterfaceList.add(xmlNetworkInterfaceInfo);
			}

		}

		return networkInterfaceList;
	}

	public static Collection<DiskInfo> convDiskDto2Xml(NodeInfoResponse  dto){
		
		ArrayList<DiskInfo> diskList = new ArrayList<DiskInfo>();
		
		DiskInfo xmlDiskInfo = null;
		
		// ノード内ディスク情報の取得
		List<NodeDiskInfoResponse> nodeDiskInfoList = dto.getNodeDiskInfo();
		Iterator<NodeDiskInfoResponse> itrNodeDiskInfoList = nodeDiskInfoList.iterator();
		while (itrNodeDiskInfoList.hasNext()) {

			NodeDiskInfoResponse nodeDiskInfo = itrNodeDiskInfoList.next();
			
			if (nodeDiskInfo.getDeviceName() != null && !nodeDiskInfo.getDeviceName().equals("")) {
				xmlDiskInfo = new DiskInfo();
				xmlDiskInfo.setFacilityId(dto.getFacilityId());
				xmlDiskInfo.setDeviceDisplayName(nodeDiskInfo.getDeviceDisplayName());
				xmlDiskInfo.setDeviceName(nodeDiskInfo.getDeviceName());
				xmlDiskInfo.setDeviceIndex(nodeDiskInfo.getDeviceIndex());
				xmlDiskInfo.setDeviceType(nodeDiskInfo.getDeviceType());
				Long deviceSize = nodeDiskInfo.getDeviceSize();
				if (checkLong(deviceSize) != null) {
					xmlDiskInfo.setDeviceSize(deviceSize.longValue());
				}
				xmlDiskInfo.setDeviceSizeUnit(nodeDiskInfo.getDeviceSizeUnit());
				xmlDiskInfo.setDeviceDescription(nodeDiskInfo.getDeviceDescription());
				Integer deviceDiskRpm = nodeDiskInfo.getDiskRpm();
				if (checkInteger(deviceDiskRpm) != null) {
					xmlDiskInfo.setDeviceDiskRpm(deviceDiskRpm.intValue());
				}

				diskList.add(xmlDiskInfo);
			}

		}

		return diskList;
	}

	public static Collection<FSInfo> convFSDto2Xml(NodeInfoResponse  dto){
		
		ArrayList<FSInfo> fsList = new ArrayList<FSInfo>();
		
		FSInfo xmlFilesystemInfo = null;
		
		// ノード内ファイルシステム情報の取得
		List<NodeFilesystemInfoResponse> nodeFilesystemInfoList = dto.getNodeFilesystemInfo();
		Iterator<NodeFilesystemInfoResponse> itrNodeFilesystemInfoList = nodeFilesystemInfoList.iterator();
		while (itrNodeFilesystemInfoList.hasNext()) {
			NodeFilesystemInfoResponse nodeFilesystemInfo = itrNodeFilesystemInfoList.next();
			
			if (nodeFilesystemInfo.getDeviceName() != null && !nodeFilesystemInfo.getDeviceName().equals("")) {
				xmlFilesystemInfo = new FSInfo();
				xmlFilesystemInfo.setFacilityId(dto.getFacilityId());
				Integer deviceIndex = nodeFilesystemInfo.getDeviceIndex();
				if (checkInteger(deviceIndex) != null) {
					xmlFilesystemInfo.setDeviceIndex(deviceIndex.intValue());
				}
				xmlFilesystemInfo.setDeviceType(nodeFilesystemInfo.getDeviceType());
				xmlFilesystemInfo.setDeviceName(nodeFilesystemInfo.getDeviceName());
				xmlFilesystemInfo.setDeviceDisplayName(nodeFilesystemInfo.getDeviceDisplayName());

				Long deviceSize = nodeFilesystemInfo.getDeviceSize();
				if (checkLong(deviceSize) != null) {
					xmlFilesystemInfo.setDeviceSize(deviceSize.longValue());
				}
				xmlFilesystemInfo.setDeviceSizeUnit(nodeFilesystemInfo.getDeviceSizeUnit());
				xmlFilesystemInfo.setDeviceDescription(nodeFilesystemInfo.getDeviceDescription());
				xmlFilesystemInfo.setDeviceFSType(nodeFilesystemInfo.getFilesystemType());
				fsList.add(xmlFilesystemInfo);
			}

		}

		return fsList;
	}
	

	public static Collection<DeviceInfo> convDeviceDto2Xml(NodeInfoResponse  dto ){
		
		ArrayList<DeviceInfo> deviceList = new ArrayList<DeviceInfo>();
		
		DeviceInfo xmlDeviceInfo = null;
		
		// ノード内汎用デバイス情報の取得
		List<NodeGeneralDeviceInfoResponse> nodeDeviceInfoList = dto.getNodeDeviceInfo();
		Iterator<NodeGeneralDeviceInfoResponse> itrNodeDeviceInfoList = nodeDeviceInfoList.iterator();
		while (itrNodeDeviceInfoList.hasNext()) {
			
			NodeGeneralDeviceInfoResponse nodeDeviceInfo = itrNodeDeviceInfoList.next();
			
			if (nodeDeviceInfo.getDeviceName() != null && !nodeDeviceInfo.getDeviceName().equals("")) {
				xmlDeviceInfo = new DeviceInfo();
				xmlDeviceInfo.setFacilityId(dto.getFacilityId());
				xmlDeviceInfo.setDeviceDisplayName(nodeDeviceInfo.getDeviceDisplayName());
				xmlDeviceInfo.setDeviceName(nodeDeviceInfo.getDeviceName());
				xmlDeviceInfo.setDeviceDescription(nodeDeviceInfo.getDeviceDescription());
				xmlDeviceInfo.setDeviceIndex(nodeDeviceInfo.getDeviceIndex());

				xmlDeviceInfo.setDeviceType(nodeDeviceInfo.getDeviceType());
				Long deviceSize = nodeDeviceInfo.getDeviceSize();
				if (checkLong(deviceSize) != null) {
					xmlDeviceInfo.setDeviceSize(deviceSize.longValue());
				}
				xmlDeviceInfo.setDeviceSizeUnit(nodeDeviceInfo.getDeviceSizeUnit());
				xmlDeviceInfo.setDeviceType(nodeDeviceInfo.getDeviceType());
				deviceList.add(xmlDeviceInfo);
			}
		}

		return deviceList;
	}
	
	public static Collection<NodeVariableInfo> convVariableDto2Xml(NodeInfoResponse  dto){
		
		ArrayList<NodeVariableInfo> variableList = new ArrayList<NodeVariableInfo>();
		
		NodeVariableInfo xmlVariableInfo = null;
		
		// ノード内ノード変数情報の取得
		List<NodeVariableInfoResponse> nodeVariableInfoList = dto.getNodeVariableInfo();
		Iterator<NodeVariableInfoResponse> itrNodeVariableInfoList = nodeVariableInfoList.iterator();
		while (itrNodeVariableInfoList.hasNext()) {

			NodeVariableInfoResponse nodeVariableInfo = itrNodeVariableInfoList.next();
			
			if (nodeVariableInfo.getNodeVariableName() != null && !nodeVariableInfo.getNodeVariableName().equals("")) {
				xmlVariableInfo = new NodeVariableInfo();
				xmlVariableInfo.setFacilityId(dto.getFacilityId());
				xmlVariableInfo.setNodeVariableName(nodeVariableInfo.getNodeVariableName());
				xmlVariableInfo.setNodeVariableValue(nodeVariableInfo.getNodeVariableValue());
				variableList.add(xmlVariableInfo);
			}

		}

		return variableList;
	}

	public static Collection<NoteInfo> convNoteDto2Xml(NodeInfoResponse  dto){
		
		ArrayList<NoteInfo> noteList = new ArrayList<NoteInfo>();
		
		NoteInfo xmlNoteInfo = null;
		
		// ノード内備考情報の取得
		List<NodeNoteInfoResponse> nodeNoteInfoList = dto.getNodeNoteInfo();
		Iterator<NodeNoteInfoResponse> itrNodeNoteInfoList = nodeNoteInfoList.iterator();
		while (itrNodeNoteInfoList.hasNext()) {

			NodeNoteInfoResponse nodeNoteInfo = itrNodeNoteInfoList.next();

			xmlNoteInfo = new NoteInfo();
			xmlNoteInfo.setFacilityId(dto.getFacilityId());
			xmlNoteInfo.setNoteId(nodeNoteInfo.getNoteId());
			xmlNoteInfo.setNote(nodeNoteInfo.getNote());
			noteList.add(xmlNoteInfo);
		}

		return noteList;
	}

	public static List<NetstatInfo> convNetstatDto2Xml(
			NodeInfoResponse  dto) {
		List<NetstatInfo> netstatList = new ArrayList<NetstatInfo>();
		
		// ノード内ネットワーク接続情報の取得
		for (NodeNetstatInfoResponse nodeNetstatInfo : dto.getNodeNetstatInfo()) {
			NetstatInfo xmlNetstatInfo = new NetstatInfo();
			xmlNetstatInfo.setFacilityId(dto.getFacilityId());
			xmlNetstatInfo.setProtocol(nodeNetstatInfo.getProtocol());
			xmlNetstatInfo.setLocalIpAddress(nodeNetstatInfo.getLocalIpAddress());
			xmlNetstatInfo.setLocalPort(nodeNetstatInfo.getLocalPort());
			xmlNetstatInfo.setForeignIpAddress(nodeNetstatInfo.getForeignIpAddress());
			xmlNetstatInfo.setForeignPort(nodeNetstatInfo.getForeignPort());
			xmlNetstatInfo.setProcessName(nodeNetstatInfo.getProcessName());
			if (nodeNetstatInfo.getPid() != null) {
				xmlNetstatInfo.setPid(nodeNetstatInfo.getPid());
			}
			xmlNetstatInfo.setStatus(nodeNetstatInfo.getStatus());
			netstatList.add(xmlNetstatInfo);
		}

		return netstatList;
	}

	public static List<PackageInfo> convPackageDto2Xml(
			NodeInfoResponse  dto) {
		List<PackageInfo> packageList = new ArrayList<>();
		// ノード内パッケージ情報の取得
		for (NodePackageInfoResponse nodePackageInfo : dto.getNodePackageInfo()) {
			PackageInfo xmlPakageInfo = new PackageInfo();
			xmlPakageInfo.setFacilityId(dto.getFacilityId());
			xmlPakageInfo.setPackageId(nodePackageInfo.getPackageId());
			xmlPakageInfo.setPackageName(nodePackageInfo.getPackageName());
			xmlPakageInfo.setVersion(nodePackageInfo.getVersion());
			xmlPakageInfo.setRelease(nodePackageInfo.getRelease());
			xmlPakageInfo.setInstallDate(nodePackageInfo.getInstallDate());
			xmlPakageInfo.setVendor(nodePackageInfo.getVendor());
			xmlPakageInfo.setArchitecture(nodePackageInfo.getArchitecture());
			packageList.add(xmlPakageInfo);
		}
		return packageList;
	}

	public static List<ProductInfo> convProductDto2Xml(
			NodeInfoResponse  dto) {
		List<ProductInfo> productList = new ArrayList<>();
		// ノード内個別導入製品情報の取得
		for (NodeProductInfoResponse nodeProductInfo : dto.getNodeProductInfo()) {
			ProductInfo xmlProductInfo = new ProductInfo();
			xmlProductInfo.setFacilityId(dto.getFacilityId());
			xmlProductInfo.setProductName(nodeProductInfo.getProductName());
			xmlProductInfo.setVersion(nodeProductInfo.getVersion());
			xmlProductInfo.setPath(nodeProductInfo.getPath());
			productList.add(xmlProductInfo);
		}
		return productList;
	}

	public static List<LicenseInfo> convLicenseDto2Xml(
			NodeInfoResponse  dto) {
		List<LicenseInfo> lisenceList = new ArrayList<>();
		// ノード内ライセンス情報の取得
		for (NodeLicenseInfoResponse nodeProductInfo : dto.getNodeLicenseInfo()) {
			LicenseInfo xmlProductInfo = new LicenseInfo();
			xmlProductInfo.setFacilityId(dto.getFacilityId());
			xmlProductInfo.setProductName(nodeProductInfo.getProductName());
			xmlProductInfo.setVendor(nodeProductInfo.getVendor());
			xmlProductInfo.setVendorContact(nodeProductInfo.getVendorContact());
			xmlProductInfo.setSerialNumber(nodeProductInfo.getSerialNumber());
			xmlProductInfo.setCount(nodeProductInfo.getCount());
			xmlProductInfo.setExpirationDate(nodeProductInfo.getExpirationDate());
			lisenceList.add(xmlProductInfo);
		}
		return lisenceList;
	}

	public static List<ProcessInfo> convProcessDto2Xml(
			NodeInfoResponse  dto) {
		List<ProcessInfo> processList = new ArrayList<>();
		// ノード内プロセス情報の取得
		for (NodeProcessInfoResponse nodeProcessInfo : dto.getNodeProcessInfo()) {
			ProcessInfo xmlProcessInfo = new ProcessInfo();
			xmlProcessInfo.setFacilityId(dto.getFacilityId());
			xmlProcessInfo.setProcessName(nodeProcessInfo.getProcessName());
			xmlProcessInfo.setPid(nodeProcessInfo.getPid());
			xmlProcessInfo.setExecUser(nodeProcessInfo.getExecUser());
			xmlProcessInfo.setPath(nodeProcessInfo.getPath());
			xmlProcessInfo.setStartupDateTime(nodeProcessInfo.getStartupDateTime());
			processList.add(xmlProcessInfo);
		}
		return processList;
	}

	/**
	 * Integerのインスタンスかチェックし、違っていればnullを返す
	 * 
	 * @param value
	 * @return
	 */
	protected static Integer checkInteger(Object value) {
		if (value != null && value instanceof Integer) {
			return (Integer) value;
		} else {
			return null;
		}
	}
	
	/**
	 * Longのインスタンスかチェックし、違っていればnullを返す
	 * 
	 * @param value
	 * @return
	 */
	protected static Long checkLong(Object value) {
		if (value != null && value instanceof Long) {
			return (Long) value;
		} else {
			return null;
		}
	}
	
	/**
	 * 文字列の長さチェック
	 * 
	 * @param value
	 * @return
	 */
	private static String checkString(String name, String str, int size){
		// 設定インポート失敗時のエラーダイアログにおけるDetais表示にエラーの詳細を含めるために、
		// クラス共通とは別途にloggerを取得（呼び出し元であるActionクラスのログのみがDetais表示の対象であるため）
		Logger logger = Logger.getLogger(RepositoryNodeAction.class);
		if(name != null && str!=null && str.length() > size){
			logger.warn(Messages.getString("SettingTools.InvalidSetting") 
					+ Messages.getString("validation.character_limit_max.message", new String[]{name, Integer.toString(size)}) );
			return null;
		}
		return str;
	}
}
