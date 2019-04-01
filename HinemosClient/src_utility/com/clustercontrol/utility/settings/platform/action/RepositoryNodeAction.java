/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.settings.platform.action;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.ws.WebServiceException;

import org.apache.log4j.Logger;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;

import com.clustercontrol.repository.util.RepositoryEndpointWrapper;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.utility.difference.CSVUtil;
import com.clustercontrol.utility.difference.DiffAnnotation;
import com.clustercontrol.utility.difference.DiffUtil;
import com.clustercontrol.utility.difference.ResultA;
import com.clustercontrol.utility.settings.ClearMethod;
import com.clustercontrol.utility.settings.ConvertorException;
import com.clustercontrol.utility.settings.DiffMethod;
import com.clustercontrol.utility.settings.ExportMethod;
import com.clustercontrol.utility.settings.ImportMethod;
import com.clustercontrol.utility.settings.SettingConstants;
import com.clustercontrol.utility.settings.model.BaseAction;
import com.clustercontrol.utility.settings.platform.conv.CommonConv;
import com.clustercontrol.utility.settings.platform.conv.RepositoryConv;
import com.clustercontrol.utility.settings.platform.xml.CPUInfo;
import com.clustercontrol.utility.settings.platform.xml.CPUList;
import com.clustercontrol.utility.settings.platform.xml.DeviceInfo;
import com.clustercontrol.utility.settings.platform.xml.DeviceList;
import com.clustercontrol.utility.settings.platform.xml.DiskInfo;
import com.clustercontrol.utility.settings.platform.xml.DiskList;
import com.clustercontrol.utility.settings.platform.xml.FSInfo;
import com.clustercontrol.utility.settings.platform.xml.FSList;
import com.clustercontrol.utility.settings.platform.xml.HostnameInfo;
import com.clustercontrol.utility.settings.platform.xml.HostnameList;
import com.clustercontrol.utility.settings.platform.xml.LicenseInfo;
import com.clustercontrol.utility.settings.platform.xml.LicenseList;
import com.clustercontrol.utility.settings.platform.xml.MemoryInfo;
import com.clustercontrol.utility.settings.platform.xml.MemoryList;
import com.clustercontrol.utility.settings.platform.xml.NetstatInfo;
import com.clustercontrol.utility.settings.platform.xml.NetstatList;
import com.clustercontrol.utility.settings.platform.xml.NetworkInterfaceInfo;
import com.clustercontrol.utility.settings.platform.xml.NetworkInterfaceList;
import com.clustercontrol.utility.settings.platform.xml.NodeInfo;
import com.clustercontrol.utility.settings.platform.xml.NodeVariableInfo;
import com.clustercontrol.utility.settings.platform.xml.NodeVariableList;
import com.clustercontrol.utility.settings.platform.xml.NoteInfo;
import com.clustercontrol.utility.settings.platform.xml.NoteList;
import com.clustercontrol.utility.settings.platform.xml.PackageInfo;
import com.clustercontrol.utility.settings.platform.xml.PackageList;
import com.clustercontrol.utility.settings.platform.xml.ProcessInfo;
import com.clustercontrol.utility.settings.platform.xml.ProcessList;
import com.clustercontrol.utility.settings.platform.xml.ProductInfo;
import com.clustercontrol.utility.settings.platform.xml.ProductList;
import com.clustercontrol.utility.settings.platform.xml.RepositoryNode;
import com.clustercontrol.utility.settings.ui.dialog.DeleteProcessDialog;
import com.clustercontrol.utility.settings.ui.dialog.UtilityProcessDialog;
import com.clustercontrol.utility.settings.ui.dialog.UtilityDialogInjector;
import com.clustercontrol.utility.settings.ui.util.DeleteProcessMode;
import com.clustercontrol.utility.settings.ui.util.ImportProcessMode;
import com.clustercontrol.utility.util.Config;
import com.clustercontrol.utility.util.UtilityDialogConstant;
import com.clustercontrol.utility.util.UtilityManagerUtil;
import com.clustercontrol.ws.repository.FacilityDuplicate_Exception;
import com.clustercontrol.ws.repository.HinemosUnknown_Exception;
import com.clustercontrol.ws.repository.InvalidRole_Exception;
import com.clustercontrol.ws.repository.InvalidSetting_Exception;
import com.clustercontrol.ws.repository.InvalidUserPass_Exception;
import com.clustercontrol.ws.repository.UsedFacility_Exception;

/**
 * リポジトリ-ノード-情報をインポート・エクスポート・削除するアクションクラス<br>
 *
 * @version 6.1.0
 * @since 1.0.0
 * 
 */
public class RepositoryNodeAction {

	protected static Logger log = Logger.getLogger(RepositoryNodeAction.class);

	/**
	 * コンストラクター <BR>
	 * SessionBeanを初期化します。
	 * @throws ConvertorException
	 */
	public RepositoryNodeAction() throws ConvertorException {
		super();
	}

	/**
	 * 情報をマネージャから削除します。<BR>
	 *
	 * @return 終了コード
	 */
	@ClearMethod
	public int clearRepositoryNode() {

		log.debug("Start Clear PlatformRepositoryNode ");

		// 返り値変数(条件付き正常終了用）
		int ret = 0;
		List<com.clustercontrol.ws.repository.NodeInfo> nodeList = null;

		// ノード情報一覧の取得
		try {
			nodeList = RepositoryEndpointWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).getNodeListAll();
		} catch (Exception e) {
			log.error(Messages.getString("SettingTools.FailToGetList") + " : " + HinemosMessage.replace(e.getMessage()));
			ret = SettingConstants.ERROR_INPROCESS;
			log.debug("End Clear PlatformRepositoryNode (Error)");
			return ret;
		}

		// ノード情報の削除
		List<String> ids = new ArrayList<>();
		for (com.clustercontrol.ws.repository.NodeInfo nodeInfo : nodeList) {
			ids.add(nodeInfo.getFacilityId());
		}

		try {
			RepositoryEndpointWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).deleteNode(ids);
			log.info(Messages.getString("SettingTools.ClearSucceeded") + " : " + ids.toString());
		} catch (HinemosUnknown_Exception e) {
			log.warn(Messages.getString("SettingTools.ClearFailed") + " : " + HinemosMessage.replace(e.getMessage()));
			ret = SettingConstants.ERROR_INPROCESS;
		} catch (InvalidUserPass_Exception e) {
			log.warn(Messages.getString("SettingTools.InvalidUserPass") + " : " + HinemosMessage.replace(e.getMessage()));
			ret = SettingConstants.ERROR_INPROCESS;
		} catch (InvalidRole_Exception e) {
			log.warn(Messages.getString("SettingTools.InvalidRole") + " : " + HinemosMessage.replace(e.getMessage()));
			ret = SettingConstants.ERROR_INPROCESS;
		} catch (UsedFacility_Exception e) {
			log.warn(Messages.getString("SettingTools.UsedFacility") + " : " + HinemosMessage.replace(e.getMessage()));
			ret = SettingConstants.ERROR_INPROCESS;
		} catch (WebServiceException e) {
			log.warn(Messages.getString("SettingTools.ClearFailed") + " : " + HinemosMessage.replace(e.getMessage()));
			ret = SettingConstants.ERROR_INPROCESS;
		} catch (Exception e) {
			log.warn(Messages.getString("SettingTools.ClearFailed") + " : " + HinemosMessage.replace(e.getMessage()));
			ret = SettingConstants.ERROR_INPROCESS;
		}
		
		// 処理の終了
		if (ret == 0) {
			log.info(Messages.getString("SettingTools.ClearCompleted"));
		} else {
			log.error(Messages.getString("SettingTools.EndWithErrorCode"));
		}
		
		log.debug("End Clear PlatformRepositoryNode ");
		return ret;
	}

	/**
	 * 情報をマネージャから取得し、XMLに出力します。<BR>
	 *
	 * @param 出力するXMLファイル
	 * @return 終了コード
	 */
	@ExportMethod
	public int exportRepositoryNode(String xmlNode, String xmlHostname, String xmlCPU, String xmlMemory,
									String xmlNetworkInterface, String xmlDisk, String xmlFS,
									String xmlDevice, String xmlNetstat, String xmlProcess,
									String xmlPackage, String xmlProduct, String xmlLicense, String xmlVariable,
									String xmlNote) {

		log.debug("Start Export PlatformRepositoryNode ");

		// 返り値変数(条件付き正常終了用）
		int ret = 0;

		HostnameList hostname = new HostnameList();
		CPUList cpu = new CPUList();
		MemoryList memory = new MemoryList();
		NetworkInterfaceList networkInterface = new NetworkInterfaceList();
		DiskList disk = new DiskList();
		FSList fs = new FSList();
		DeviceList device = new DeviceList();
		NetstatList netstat = new NetstatList();
		ProcessList process = new ProcessList();
		PackageList pack = new PackageList();
		ProductList product = new ProductList();
		LicenseList license = new LicenseList();
		NodeVariableList variable = new NodeVariableList();
		NoteList note = new NoteList();

		List<com.clustercontrol.ws.repository.NodeInfo> nodeInfoList;

		// ノード情報一覧の取得
		try {
			nodeInfoList = RepositoryEndpointWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).getNodeListAll();
			Collections.sort(nodeInfoList, new Comparator<com.clustercontrol.ws.repository.NodeInfo>() {
				@Override
				public int compare(
						com.clustercontrol.ws.repository.NodeInfo info1,
						com.clustercontrol.ws.repository.NodeInfo info2) {
					return info1.getFacilityId().compareTo(info2.getFacilityId());
				}
			});
		} catch (Exception e) {
			log.error(Messages.getString("SettingTools.FailToGetList") + " : " + HinemosMessage.replace(e.getMessage()));
			ret = SettingConstants.ERROR_INPROCESS;
			log.debug("End Export PlatformRepositoryNode (Error)");
			return ret;
		}

		NodeInfo[] xmlNodeInfo = new NodeInfo[nodeInfoList.size()];
		List<HostnameInfo> hostnameList = new ArrayList<HostnameInfo>();
		List<CPUInfo> cpuList = new ArrayList<CPUInfo>();
		List<MemoryInfo> memoryList = new ArrayList<MemoryInfo>();
		List<NetworkInterfaceInfo> networkInterfaceList = new ArrayList<NetworkInterfaceInfo>();
		List<DiskInfo> diskList = new ArrayList<DiskInfo>();
		List<FSInfo> fsList = new ArrayList<FSInfo>();
		List<DeviceInfo> deviceList = new ArrayList<DeviceInfo>();
		List<NetstatInfo> netstatList = new ArrayList<>();
		List<ProcessInfo> processList = new ArrayList<>();
		List<PackageInfo> packList = new ArrayList<>();
		List<ProductInfo> productList = new ArrayList<>();
		List<LicenseInfo> licenseList = new ArrayList<>();
		List<NodeVariableInfo> variableList = new ArrayList<NodeVariableInfo>();
		List<NoteInfo> noteList = new ArrayList<NoteInfo>();

		// ノード情報の取得
		log.info("facilityIdList.size() : " + nodeInfoList.size());
		for (int i = 0; i < nodeInfoList.size(); i++) {
			xmlNodeInfo[i] = new NodeInfo();
			// ノード情報の取得
			com.clustercontrol.ws.repository.NodeInfo nodeInfo;
			try {
				nodeInfo = RepositoryEndpointWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).getNodeFull(nodeInfoList.get(i).getFacilityId());
			} catch (Exception e) {
				log.error(Messages.getString("SettingTools.ExportFailed") + " : " + HinemosMessage.replace(e.getMessage()));
				ret = SettingConstants.ERROR_INPROCESS;
				log.debug("End Export PlatformRepositoryNode (Error)");
				return ret;
			}

			//Hinemos DTOからXMLへの変換
			RepositoryConv.convDto2Xml(nodeInfo, xmlNodeInfo[i]);
			//デバイス、ファイルシステムはCollectionで返ってくるので、1つのArrayListに足しこむ
			hostnameList.addAll(RepositoryConv.convHostnameDto2Xml(nodeInfo));
			cpuList.addAll(RepositoryConv.convCPUDto2Xml(nodeInfo));
			memoryList.addAll(RepositoryConv.convMemoryDto2Xml(nodeInfo));
			networkInterfaceList.addAll(RepositoryConv.convNetworkInterfaceDto2Xml(nodeInfo));
			diskList.addAll(RepositoryConv.convDiskDto2Xml(nodeInfo));
			fsList.addAll(RepositoryConv.convFSDto2Xml(nodeInfo));
			deviceList.addAll(RepositoryConv.convDeviceDto2Xml(nodeInfo));
			netstatList.addAll(RepositoryConv.convNetstatDto2Xml(nodeInfo));
			processList.addAll(RepositoryConv.convProcessDto2Xml(nodeInfo));
			packList.addAll(RepositoryConv.convPackageDto2Xml(nodeInfo));
			productList.addAll(RepositoryConv.convProductDto2Xml(nodeInfo));
			licenseList.addAll(RepositoryConv.convLicenseDto2Xml(nodeInfo));
			variableList.addAll(RepositoryConv.convVariableDto2Xml(nodeInfo));
			noteList.addAll(RepositoryConv.convNoteDto2Xml(nodeInfo));

			log.info(Messages.getString("SettingTools.ExportSucceeded") + " : " + xmlNodeInfo[i].getFacilityId());

		}

		// XML Beanに情報を格納
		RepositoryNode node = new RepositoryNode();
		node.setNodeInfo(xmlNodeInfo);

		hostname.setHostnameInfo(new HostnameInfo[hostnameList.size()]);
		for (int i = 0; i < hostnameList.size(); i++) {
			hostname.setHostnameInfo(i, hostnameList.get(i));
		}

		cpu.setCPUInfo(new CPUInfo[cpuList.size()]);
		for (int i = 0; i < cpuList.size(); i++) {
			cpu.setCPUInfo(i, cpuList.get(i));
		}

		memory.setMemoryInfo(new MemoryInfo[memoryList.size()]);
		for (int i = 0; i < memoryList.size(); i++) {
			memory.setMemoryInfo(i, memoryList.get(i));
		}

		networkInterface.setNetworkInterfaceInfo(new NetworkInterfaceInfo[networkInterfaceList.size()]);
		for (int i = 0; i < networkInterfaceList.size(); i++) {
			networkInterface.setNetworkInterfaceInfo(i, networkInterfaceList.get(i));
		}

		disk.setDiskInfo(new DiskInfo[diskList.size()]);
		for (int i = 0; i < diskList.size(); i++) {
			disk.setDiskInfo(i, diskList.get(i));
		}

		fs.setFSInfo(new FSInfo[fsList.size()]);
		for (int i = 0; i < fsList.size(); i++) {
			fs.setFSInfo(i, fsList.get(i));
		}

		device.setDeviceInfo(new DeviceInfo[deviceList.size()]);
		for (int i = 0; i < deviceList.size(); i++) {
			device.setDeviceInfo(i, deviceList.get(i));
		}

		netstat.setNetstatInfo(new NetstatInfo[netstatList.size()]);
		for (int i = 0; i < netstatList.size(); i++) {
			netstat.setNetstatInfo(i, netstatList.get(i));
		}

		process.setProcessInfo(new ProcessInfo[processList.size()]);
		for (int i = 0; i < processList.size(); i++) {
			process.setProcessInfo(i, processList.get(i));
		}

		pack.setPackageInfo(new PackageInfo[packList.size()]);
		for (int i = 0; i < packList.size(); i++) {
			pack.setPackageInfo(i, packList.get(i));
		}

		product.setProductInfo(new ProductInfo[productList.size()]);
		for (int i = 0; i < productList.size(); i++) {
			product.setProductInfo(i, productList.get(i));
		}

		license.setLicenseInfo(new LicenseInfo[licenseList.size()]);
		for (int i = 0; i < licenseList.size(); i++) {
			license.setLicenseInfo(i, licenseList.get(i));
		}

		variable.setNodeVariableInfo(new NodeVariableInfo[variableList.size()]);
		for (int i = 0; i < variableList.size(); i++) {
			variable.setNodeVariableInfo(i, variableList.get(i));
		}

		note.setNoteInfo(new NoteInfo[noteList.size()]);
		for (int i = 0; i < noteList.size(); i++) {
			note.setNoteInfo(i, noteList.get(i));
		}

		// XMLファイルに出力
		try {
			node.setCommon(CommonConv.versionPlatformDto2Xml(Config.getVersion()));
			node.setSchemaInfo(RepositoryConv.getSchemaVersionNode());
			try(FileOutputStream fos = new FileOutputStream(xmlNode);
				OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");){
				node.marshal(osw);
			}

			hostname.setCommon(CommonConv.versionPlatformDto2Xml(Config.getVersion()));
			hostname.setSchemaInfo(RepositoryConv.getSchemaVersionNode());
			try(FileOutputStream fos = new FileOutputStream(xmlHostname);
				OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");){
				hostname.marshal(osw);
			}

			cpu.setCommon(CommonConv.versionPlatformDto2Xml(Config.getVersion()));
			cpu.setSchemaInfo(RepositoryConv.getSchemaVersionNode());
			try(FileOutputStream fos = new FileOutputStream(xmlCPU);
				OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");){
				cpu.marshal(osw);
			}

			memory.setCommon(CommonConv.versionPlatformDto2Xml(Config.getVersion()));
			memory.setSchemaInfo(RepositoryConv.getSchemaVersionNode());
			try(FileOutputStream fos = new FileOutputStream(xmlMemory);
				OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");){
				memory.marshal(osw);
			}
			
			networkInterface.setCommon(CommonConv.versionPlatformDto2Xml(Config.getVersion()));
			networkInterface.setSchemaInfo(RepositoryConv.getSchemaVersionNode());
			try(FileOutputStream fos = new FileOutputStream(xmlNetworkInterface);
				OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");){
				networkInterface.marshal(osw);
			}
			
			disk.setCommon(CommonConv.versionPlatformDto2Xml(Config.getVersion()));
			disk.setSchemaInfo(RepositoryConv.getSchemaVersionNode());
			try(FileOutputStream fos = new FileOutputStream(xmlDisk);
				OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");){
				disk.marshal(osw);
			}
			
			device.setCommon(CommonConv.versionPlatformDto2Xml(Config.getVersion()));
			device.setSchemaInfo(RepositoryConv.getSchemaVersionNode());
			try(FileOutputStream fos = new FileOutputStream(xmlDevice);
				OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");){
				device.marshal(osw);
			}
			
			fs.setCommon(CommonConv.versionPlatformDto2Xml(Config.getVersion()));
			fs.setSchemaInfo(RepositoryConv.getSchemaVersionNode());
			try(FileOutputStream fos = new FileOutputStream(xmlFS);
				OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");){
				fs.marshal(osw);
			}
			
			netstat.setCommon(CommonConv.versionPlatformDto2Xml(Config.getVersion()));
			netstat.setSchemaInfo(RepositoryConv.getSchemaVersionNode());
			try(FileOutputStream fos = new FileOutputStream(xmlNetstat);
				OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");){
				netstat.marshal(osw);
			}
			
			process.setCommon(CommonConv.versionPlatformDto2Xml(Config.getVersion()));
			process.setSchemaInfo(RepositoryConv.getSchemaVersionNode());
			try(FileOutputStream fos = new FileOutputStream(xmlProcess);
				OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");){
				process.marshal(osw);
			}
			
			pack.setCommon(CommonConv.versionPlatformDto2Xml(Config.getVersion()));
			pack.setSchemaInfo(RepositoryConv.getSchemaVersionNode());
			try(FileOutputStream fos = new FileOutputStream(xmlPackage);
				OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");){
				pack.marshal(osw);
			}
			
			product.setCommon(CommonConv.versionPlatformDto2Xml(Config.getVersion()));
			product.setSchemaInfo(RepositoryConv.getSchemaVersionNode());
			try(FileOutputStream fos = new FileOutputStream(xmlProduct);
				OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");){
				product.marshal(osw);
			}
			
			license.setCommon(CommonConv.versionPlatformDto2Xml(Config.getVersion()));
			license.setSchemaInfo(RepositoryConv.getSchemaVersionNode());
			try(FileOutputStream fos = new FileOutputStream(xmlLicense);
					OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");){
				license.marshal(osw);
			}
			
			variable.setCommon(CommonConv.versionPlatformDto2Xml(Config.getVersion()));
			variable.setSchemaInfo(RepositoryConv.getSchemaVersionNode());
			try(FileOutputStream fos = new FileOutputStream(xmlVariable);
				OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");){
				variable.marshal(osw);
			}

			note.setCommon(CommonConv.versionPlatformDto2Xml(Config.getVersion()));
			note.setSchemaInfo(RepositoryConv.getSchemaVersionNode());
			try(FileOutputStream fos = new FileOutputStream(xmlNote);
				OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");){
				note.marshal(osw);
			}
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			log.error(Messages.getString("SettingTools.MarshalXmlFailed") + e.getMessage());
			ret = SettingConstants.ERROR_INPROCESS;
		} catch (Exception e) {
			log.error(Messages.getString("SettingTools.MarshalXmlFailed"), e);
			ret = SettingConstants.ERROR_INPROCESS;
		}

		// 処理の終了
		if (ret == 0) {
			log.info(Messages.getString("SettingTools.ExportCompleted"));
		} else {
			log.error(Messages.getString("SettingTools.EndWithErrorCode"));
		}
		
		log.debug("End Export PlatformRepositoryNode ");
		return ret;
	}

	/**
	 * XMLの情報をマネージャに投入します。<BR>
	 *
	 * @param 入力するXMLファイル
	 * @return 終了コード
	 */
	@ImportMethod
	public int importRepositoryNode(String xmlNode, String xmlHostname, String xmlCPU, String xmlMemory,
									String xmlNetworkInterface, String xmlDisk, String xmlFS,
									String xmlDevice, String xmlNetstat, String xmlPackage,
									String xmlProcess, String xmlProduct, String xmlLicense, String xmlVariable,
									String xmlNote) {

		log.debug("Start Import PlatformRepositoryNode ");

		if(ImportProcessMode.getProcesstype() == UtilityDialogConstant.CANCEL){
	    	getLogger().info(Messages.getString("SettingTools.ImportSucceeded.Cancel"));
	    	getLogger().debug("End Import PlatformRepositoryNode (Cancel)");
			return SettingConstants.ERROR_INPROCESS;
	    }
		
		// 返り値変数(条件付き正常終了用）
		int ret = 0;
		RepositoryNode node = null;
		NodeInfo xmlNodeInfo = null;

		HostnameList hostname = null;
		CPUList cpu = null;
		MemoryList memory = null;
		NetworkInterfaceList networkInterface = null;
		DiskList disk = null;
		FSList fs = null;
		DeviceList device = null;
		NetstatList netstat = null;
		LicenseList license = null;
		ProductList product = null;
		NodeVariableList variable = null;
		NoteList note = null;
		
		// XMLファイルからの読み込み
		try {
			node = RepositoryNode.unmarshal(new InputStreamReader(new FileInputStream(xmlNode), "UTF-8"));
			hostname = HostnameList.unmarshal(new InputStreamReader(new FileInputStream(xmlHostname), "UTF-8"));
			cpu = CPUList.unmarshal(new InputStreamReader(new FileInputStream(xmlCPU), "UTF-8"));
			memory = MemoryList.unmarshal(new InputStreamReader(new FileInputStream(xmlMemory), "UTF-8"));
			networkInterface = NetworkInterfaceList.unmarshal(new InputStreamReader(new FileInputStream(xmlNetworkInterface), "UTF-8"));
			disk = DiskList.unmarshal(new InputStreamReader(new FileInputStream(xmlDisk), "UTF-8"));
			fs = FSList.unmarshal(new InputStreamReader(new FileInputStream(xmlFS), "UTF-8"));
			device = DeviceList.unmarshal(new InputStreamReader(new FileInputStream(xmlDevice), "UTF-8"));
			netstat = NetstatList.unmarshal(new InputStreamReader(new FileInputStream(xmlNetstat), "UTF-8"));
			license = LicenseList.unmarshal(new InputStreamReader(new FileInputStream(xmlLicense), "UTF-8"));
			product = ProductList.unmarshal(new InputStreamReader(new FileInputStream(xmlProduct), "UTF-8"));
			variable = NodeVariableList.unmarshal(new InputStreamReader(new FileInputStream(xmlVariable), "UTF-8"));
			note = NoteList.unmarshal(new InputStreamReader(new FileInputStream(xmlNote), "UTF-8"));
		} catch (MarshalException | ValidationException e) {
			log.warn(Messages.getString("SettingTools.UnmarshalXmlFailed"), e);
			ret = SettingConstants.ERROR_INPROCESS;
			log.debug("End Import PlatformRepositoryNode (Error)");
			return ret;
		} catch (Exception e) {
			log.error(Messages.getString("SettingTools.UnmarshalXmlFailed"), e);
			ret = SettingConstants.ERROR_INPROCESS;
			log.debug("End Import PlatformRepositoryNode (Error)");
			return ret;
		}

		/*スキーマのバージョンチェック*/
		if(!checkSchemaVersionNode(node.getSchemaInfo())){
			ret = SettingConstants.ERROR_SCHEMA_VERSION;
			return ret;
		}

		// ノード情報の登録
		for (int i = 0; i < node.getNodeInfoCount(); i++) {
			xmlNodeInfo = node.getNodeInfo()[i];
			com.clustercontrol.ws.repository.NodeInfo nodeInfo = RepositoryConv.convNodeXml2Dto(xmlNodeInfo, hostname.getHostnameInfo(), cpu.getCPUInfo(),
													  memory.getMemoryInfo(), networkInterface.getNetworkInterfaceInfo(),
													  disk.getDiskInfo(), fs.getFSInfo(), device.getDeviceInfo(),
													  netstat.getNetstatInfo(), license.getLicenseInfo(), product.getProductInfo(),
													  variable.getNodeVariableInfo(), note.getNoteInfo());

			if (nodeInfo.getFacilityId() != null && !nodeInfo.getFacilityId().equals("")) {
				try {
					RepositoryEndpointWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).addNode(nodeInfo);
					log.info(Messages.getString("SettingTools.ImportSucceeded") + " : " + nodeInfo.getFacilityId());
				} catch (FacilityDuplicate_Exception e) {
					//重複時、インポート処理方法を確認する
					if(!ImportProcessMode.isSameprocess()){
						String[] args = {nodeInfo.getFacilityId()};
						UtilityProcessDialog dialog = UtilityDialogInjector.createImportProcessDialog(
								null, Messages.getString("message.import.confirm2", args));
					    ImportProcessMode.setProcesstype(dialog.open());
					    ImportProcessMode.setSameprocess(dialog.getToggleState());
					}
				    
				    if(ImportProcessMode.getProcesstype() == UtilityDialogConstant.UPDATE){
				    	try {
				    		RepositoryEndpointWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).modifyNode(nodeInfo);
							log.info(Messages.getString("SettingTools.ImportSucceeded.Update") + " : " + nodeInfo.getFacilityId());
						} catch (Exception e1) {
							log.warn(Messages.getString("SettingTools.ImportFailed") + " : " + HinemosMessage.replace(e1.getMessage()));
							ret = SettingConstants.ERROR_INPROCESS;
						}
				    } else if(ImportProcessMode.getProcesstype() == UtilityDialogConstant.SKIP){
				    	log.info(Messages.getString("SettingTools.ImportSucceeded.Skip") + " : " + nodeInfo.getFacilityId());
				    } else if(ImportProcessMode.getProcesstype() == UtilityDialogConstant.CANCEL){
				    	log.info(Messages.getString("SettingTools.ImportSucceeded.Cancel"));
				    	ret = SettingConstants.ERROR_INPROCESS;
				    	break;
				    }
				} catch (HinemosUnknown_Exception e) {
					log.info(Messages.getString("SettingTools.ImportFailed") + " : " + HinemosMessage.replace(e.getMessage()));
					ret = SettingConstants.ERROR_INPROCESS;
				} catch (InvalidUserPass_Exception e) {
					log.info(Messages.getString("SettingTools.InvalidUserPass") + " : " + HinemosMessage.replace(e.getMessage()));
					ret = SettingConstants.ERROR_INPROCESS;
				} catch (InvalidRole_Exception e) {
					log.info(Messages.getString("SettingTools.InvalidRole") + " : " + HinemosMessage.replace(e.getMessage()));
					ret = SettingConstants.ERROR_INPROCESS;
				} catch (WebServiceException e) {
					log.warn(Messages.getString("SettingTools.ImportFailed") + " : " + HinemosMessage.replace(e.getMessage()));
					ret = SettingConstants.ERROR_INPROCESS;
				} catch (InvalidSetting_Exception e) {
					log.warn(Messages.getString("SettingTools.ImportFailed") + " : " + HinemosMessage.replace(e.getMessage()));
					ret = SettingConstants.ERROR_INPROCESS;
				}
			}else{
				//必須項目が入っていない場合には、facilityIDを""で返す。
				//そのためここに入る。
				log.warn(Messages.getString("SettingTools.ImportFailed") + " : "
						+ xmlNodeInfo.getFacilityId());
			}
		}

		checkDelete(node);
		
		// 処理の終了
		if (ret == 0) {
			log.info(Messages.getString("SettingTools.ImportCompleted"));
		} else {
			log.error(Messages.getString("SettingTools.EndWithErrorCode"));
		}
		
		log.debug("End Import PlatformRepositoryNode ");
		return ret;
	}
	
	
	/**
	 * スキーマのバージョンチェックを行い、メッセージを出力する。<BR>
	 * ※メッセージ詳細に出力するためは本クラスのloggerにエラー内容を出力する必要がある
	 * 
	 * @param XMLファイルのスキーマ
	 * @return チェック結果
	 */
	private boolean checkSchemaVersionNode(com.clustercontrol.utility.settings.platform.xml.SchemaInfo schmaversion) {
		/*スキーマのバージョンチェック*/
		int res = RepositoryConv.checkSchemaVersionNode(schmaversion.getSchemaType(),
					schmaversion.getSchemaVersion(),
					schmaversion.getSchemaRevision());
		com.clustercontrol.utility.settings.platform.xml.SchemaInfo sci = RepositoryConv.getSchemaVersionNode();
		
		return BaseAction.checkSchemaVersionResult(this.getLogger(), res, sci.getSchemaType(), sci.getSchemaVersion(), sci.getSchemaRevision());
	}
	
	@DiffAnnotation("{\"type\":\"Root\", \"funcName\":\"HostnameList_funcName\"}")
	public static class HostnameRoot {
		public Map<String, Hostname> hostnameList = new HashMap<String, Hostname>();

		@DiffAnnotation("{\"type\":\"Comparison\"}")
		public Hostname[] getHostnameList() {
			List<Hostname> list = new ArrayList<Hostname>(hostnameList.values());
			Collections.sort(list, new Comparator<Hostname>() {
				@Override
				public int compare(Hostname o1, Hostname o2) {
					return o1.id.compareTo(o2.id);
				}
			});
			return list.toArray(new Hostname[0]);
		}
	}

	@DiffAnnotation("{\"type\":\"Element\"}")
	public static class Hostname {
		public String id;

		public List<HostnameInfo> hostnameInfo = new ArrayList<HostnameInfo>();

		@DiffAnnotation("{\"type\":\"PrimaryKey\"}")
		public String getId() {
			return id;
		}

		@DiffAnnotation(value = {"{\"type\":\"Column\", \"columnName\":\"NodeInfo_facilityName\"}", "{\"type\":\"Array\"}"})
		public HostnameInfo[] getHostnameInfo() {
			return hostnameInfo.toArray(new HostnameInfo[0]);
		}
	}

	@DiffAnnotation(value={
		"{\"type\":\"Root\", \"funcName\":\"CPUList_funcName\"}",
		"{\"type\":\"OrderBy\"," +
		"\"props\":[" +
			"\"CpuInfo\"," +
			"\"CpuInfo.*.DeviceName\"," +
			"\"CpuInfo.*.DeviceIndex\"," +
			"\"CpuInfo.*.DeviceType\"," +
			"\"CpuInfo.*.DeviceSize\"," +
			"\"CpuInfo.*.DeviceSizeUnit\"," +
			"\"CpuInfo.*.DeviceDescription\"" +
		"]}"
	})
	public static class CPURoot {
		public Map<String, Cpu> cpuList = new HashMap<String, Cpu>();

		@DiffAnnotation("{\"type\":\"Comparison\"}")
		public Cpu[] getCPUList() {
			List<Cpu> list = new ArrayList<Cpu>(cpuList.values());
			Collections.sort(list, new Comparator<Cpu>() {
				@Override
				public int compare(Cpu o1, Cpu o2) {
					return o1.id.compareTo(o2.id);
				}
			});
			return list.toArray(new Cpu[0]);
		}
	}

	@DiffAnnotation("{\"type\":\"Element\"}")
	public static class Cpu {
		public String id;

		public List<CPUInfo> cpuInfo = new ArrayList<CPUInfo>();

		@DiffAnnotation("{\"type\":\"PrimaryKey\"}")
		public String getId() {
			return id;
		}
		@DiffAnnotation(value = {"{\"type\":\"Column\", \"columnName\":\"CPUInfo_deviceDisplayName\"}", "{\"type\":\"Array\"}"})
		public CPUInfo[] getCpuInfo() {
			return cpuInfo.toArray(new CPUInfo[0]);
		}
	}

	@DiffAnnotation(value={
		"{\"type\":\"Root\", \"funcName\":\"MemoryList_funcName\"}",
		"{\"type\":\"OrderBy\"," +
		"\"props\":[" +
			"\"MemoryInfo\"," +
			"\"MemoryInfo.*.DeviceName\"," +
			"\"MemoryInfo.*.DeviceIndex\"," +
			"\"MemoryInfo.*.DeviceType\"," +
			"\"MemoryInfo.*.DeviceSize\"," +
			"\"MemoryInfo.*.DeviceSizeUnit\"," +
			"\"MemoryInfo.*.DeviceDescription\"" +
		"]}"
	})
	public static class MemoryRoot {
		public Map<String, Memory> memoryList = new HashMap<String, Memory>();

		@DiffAnnotation("{\"type\":\"Comparison\"}")
		public Memory[] getMemoryList() {
			List<Memory> list = new ArrayList<Memory>(memoryList.values());
			Collections.sort(list, new Comparator<Memory>() {
				@Override
				public int compare(Memory o1, Memory o2) {
					return o1.id.compareTo(o2.id);
				}
			});
			return list.toArray(new Memory[0]);
		}
	}
	@DiffAnnotation("{\"type\":\"Element\"}")
	public static class Memory {
		public String id;

		public List<MemoryInfo> memoryInfo = new ArrayList<MemoryInfo>();

		@DiffAnnotation("{\"type\":\"PrimaryKey\"}")
		public String getId() {
			return id;
		}

		@DiffAnnotation(value = {"{\"type\":\"Column\", \"columnName\":\"MemoryInfo_deviceDisplayName\"}", "{\"type\":\"Array\"}"})
		public MemoryInfo[] getMemoryInfo() {
			return memoryInfo.toArray(new MemoryInfo[0]);
		}
	}

	@DiffAnnotation(value={
		"{\"type\":\"Root\", \"funcName\":\"NetworkInterfaceList_funcName\"}",
		"{\"type\":\"OrderBy\"," +
		"\"props\":[" +
			"\"NetworkInterfaceInfo\"," +
			"\"NetworkInterfaceInfo.*.DeviceName\"," +
			"\"NetworkInterfaceInfo.*.DeviceIndex\"," +
			"\"NetworkInterfaceInfo.*.DeviceType\"," +
			"\"NetworkInterfaceInfo.*.DeviceSize\"," +
			"\"NetworkInterfaceInfo.*.DeviceSizeUnit\"," +
			"\"NetworkInterfaceInfo.*.DeviceDescription\"," +
			"\"NetworkInterfaceInfo.*.DeviceNicIpAddress\"," +
			"\"NetworkInterfaceInfo.*.DeviceNicMacAddress\"" +
		"]}"
	})
	public static class NetworkInterfaceRoot {
		public Map<String, NetworkInterface> networkInterfaceList = new HashMap<String, NetworkInterface>();

		@DiffAnnotation("{\"type\":\"Comparison\"}")
		public NetworkInterface[] getNetworkInterfaceList() {
			List<NetworkInterface> list = new ArrayList<NetworkInterface>(networkInterfaceList.values());
			Collections.sort(list, new Comparator<NetworkInterface>() {
				@Override
				public int compare(NetworkInterface o1, NetworkInterface o2) {
					return o1.id.compareTo(o2.id);
				}
			});
			return list.toArray(new NetworkInterface[0]);
		}
	}

	@DiffAnnotation("{\"type\":\"Element\"}")
	public static class NetworkInterface {
		public String id;

		public List<NetworkInterfaceInfo> networkInterfaceInfo = new ArrayList<NetworkInterfaceInfo>();

		@DiffAnnotation("{\"type\":\"PrimaryKey\"}")
		public String getId() {
			return id;
		}

		@DiffAnnotation(value = {"{\"type\":\"Column\", \"columnName\":\"NetworkInterfaceInfo_deviceDisplayName\"}", "{\"type\":\"Array\"}"})
		public NetworkInterfaceInfo[] getNetworkInterfaceInfo() {
			return networkInterfaceInfo.toArray(new NetworkInterfaceInfo[0]);
		}
	}

	@DiffAnnotation(value={
		"{\"type\":\"Root\", \"funcName\":\"DiskList_funcName\"}",
		"{\"type\":\"OrderBy\"," +
		"\"props\":[" +
			"\"DiskInfo\"," +
			"\"DiskInfo.*.DeviceName\"," +
			"\"DiskInfo.*.DeviceIndex\"," +
			"\"DiskInfo.*.DeviceType\"," +
			"\"DiskInfo.*.DeviceSize\"," +
			"\"DiskInfo.*.DeviceSizeUnit\"," +
			"\"DiskInfo.*.DeviceDescription\"," +
			"\"DiskInfo.*.DeviceDiskRpm\"" +
		"]}"
	})
	public static class DiskRoot {
		public Map<String, Disk> diskList = new HashMap<String, Disk>();

		@DiffAnnotation("{\"type\":\"Comparison\"}")
		public Disk[] getDiskList() {
			List<Disk> list = new ArrayList<Disk>(diskList.values());
			Collections.sort(list, new Comparator<Disk>() {
				@Override
				public int compare(Disk o1, Disk o2) {
					return o1.id.compareTo(o2.id);
				}
			});
			return list.toArray(new Disk[0]);
		}
	}

	@DiffAnnotation("{\"type\":\"Element\"}")
	public static class Disk {
		public String id;

		public List<DiskInfo> diskInfo = new ArrayList<DiskInfo>();

		@DiffAnnotation("{\"type\":\"PrimaryKey\"}")
		public String getId() {
			return id;
		}

		@DiffAnnotation(value = {"{\"type\":\"Column\", \"columnName\":\"DiskInfo_deviceDisplayName\"}", "{\"type\":\"Array\"}"})
		public DiskInfo[] getDiskInfo() {
			return diskInfo.toArray(new DiskInfo[0]);
		}
	}

	@DiffAnnotation(value={
		"{\"type\":\"Root\", \"funcName\":\"FSList_funcName\"}",
		"{\"type\":\"OrderBy\"," +
		"\"props\":[" +
			"\"FsInfo\"," +
			"\"FsInfo.*.DeviceName\"," +
			"\"FsInfo.*.DeviceIndex\"," +
			"\"FsInfo.*.DeviceType\"," +
			"\"FsInfo.*.DeviceSize\"," +
			"\"FsInfo.*.DeviceSizeUnit\"," +
			"\"FsInfo.*.DeviceDescription\"," +
			"\"FsInfo.*.DeviceFSType\"" +
		"]}"
	})
	public static class FSRoot {
		public Map<String, Fs> fsList = new HashMap<String, Fs>();

		@DiffAnnotation("{\"type\":\"Comparison\"}")
		public Fs[] getFsList() {
			List<Fs> list = new ArrayList<Fs>(fsList.values());
			Collections.sort(list, new Comparator<Fs>() {
				@Override
				public int compare(Fs o1, Fs o2) {
					return o1.id.compareTo(o2.id);
				}
			});
			return list.toArray(new Fs[0]);
		}
	}

	@DiffAnnotation("{\"type\":\"Element\"}")
	public static class Fs {
		public String id;

		public List<FSInfo> fsInfo = new ArrayList<FSInfo>();

		@DiffAnnotation("{\"type\":\"PrimaryKey\"}")
		public String getId() {
			return id;
		}

		@DiffAnnotation(value = {"{\"type\":\"Column\", \"columnName\":\"FSInfo_deviceDisplayName\"}", "{\"type\":\"Array\"}"})
		public FSInfo[] getFsInfo() {
			return fsInfo.toArray(new FSInfo[0]);
		}
	}

	@DiffAnnotation(value={
		"{\"type\":\"Root\", \"funcName\":\"DeviceList_funcName\"}",
		"{\"type\":\"OrderBy\"," +
		"\"props\":[" +
			"\"DeviceInfo\"," +
			"\"DeviceInfo.*.DeviceName\"," +
			"\"DeviceInfo.*.DeviceIndex\"," +
			"\"DeviceInfo.*.DeviceType\"," +
			"\"DeviceInfo.*.DeviceSize\"," +
			"\"DeviceInfo.*.DeviceSizeUnit\"," +
			"\"DeviceInfo.*.DeviceDescription\"" +
		"]}"
	})
	public static class DeviceRoot {
		public Map<String, Device> deviceList = new HashMap<String, Device>();

		@DiffAnnotation("{\"type\":\"Comparison\"}")
		public Device[] getDeviceList() {
			List<Device> list = new ArrayList<Device>(deviceList.values());
			Collections.sort(list, new Comparator<Device>() {
				@Override
				public int compare(Device o1, Device o2) {
					return o1.id.compareTo(o2.id);
				}
			});
			return list.toArray(new Device[0]);
		}
	}

	@DiffAnnotation("{\"type\":\"Element\"}")
	public static class Device {
		public String id;

		public List<DeviceInfo> deviceInfo = new ArrayList<DeviceInfo>();

		@DiffAnnotation("{\"type\":\"PrimaryKey\"}")
		public String getId() {
			return id;
		}

		@DiffAnnotation(value = {"{\"type\":\"Column\", \"columnName\":\"DeviceInfo_deviceDisplayName\"}", "{\"type\":\"Array\"}"})
		public DeviceInfo[] getDeviceInfo() {
			return deviceInfo.toArray(new DeviceInfo[0]);
		}
	}

	@DiffAnnotation(value={
			"{\"type\":\"Root\", \"funcName\":\"NetstatList_funcName\"}",
			"{\"type\":\"OrderBy\"," +
					"\"props\":[" +
					"\"NetstatInfo\"," +
					"\"NetstatInfo.*.protocol\"," +
					"\"NetstatInfo.*.localIpAddress\"," +
					"\"NetstatInfo.*.localPort\"," +
					"\"NetstatInfo.*.foreignIpAddress\"," +
					"\"NetstatInfo.*.foreignPort\"," +
					"\"NetstatInfo.*.processName\"," +
					"\"NetstatInfo.*.pid\"," +
					"\"NetstatInfo.*.status\"" +
					"]}"
	})
	public static class NetstatRoot {
		public Map<String, Netstat> netstatList = new HashMap<>();

		@DiffAnnotation("{\"type\":\"Comparison\"}")
		public Netstat[] getNetstatList() {
			List<Netstat> list = new ArrayList<Netstat>(netstatList.values());
			Collections.sort(list, new Comparator<Netstat>() {
				@Override
				public int compare(Netstat o1, Netstat o2) {
					return o1.id.compareTo(o2.id);
				}
			});
			return list.toArray(new Netstat[0]);
		}
	}

	@DiffAnnotation("{\"type\":\"Element\"}")
	public static class Netstat {
		public String id;

		public List<NetstatInfo> netstatInfo = new ArrayList<>();

		@DiffAnnotation("{\"type\":\"PrimaryKey\"}")
		public String getId() {
			return id;
		}

		@DiffAnnotation(value = {"{\"type\":\"Column\", \"columnName\":\"DeviceInfo_deviceDisplayName\"}", "{\"type\":\"Array\"}"})
		public NetstatInfo[] getNetstatInfo() {
			return netstatInfo.toArray(new NetstatInfo[0]);
		}
	}

	@DiffAnnotation(value={
			"{\"type\":\"Root\", \"funcName\":\"ProcessList_funcName\"}",
			"{\"type\":\"OrderBy\"," +
					"\"props\":[" +
					"\"ProcessInfo\"," +
					"\"ProcessInfo.*.processName\"," +
					"\"ProcessInfo.*.pid\"," +
					"\"ProcessInfo.*.path\"," +
					"\"ProcessInfo.*.execUser\"," +
					"\"ProcessInfo.*.startupDateTime\"" +
					"]}"
	})
	public static class ProcessRoot {
		public Map<String, Process> processList = new HashMap<>();

		@DiffAnnotation("{\"type\":\"Comparison\"}")
		public Process[] getProcessList() {
			List<Process> list = new ArrayList<Process>(processList.values());
			Collections.sort(list, new Comparator<Process>() {
				@Override
				public int compare(Process o1, Process o2) {
					return o1.id.compareTo(o2.id);
				}
			});
			return list.toArray(new Process[0]);
		}
	}

	@DiffAnnotation("{\"type\":\"Element\"}")
	public static class Process {
		public String id;

		public List<ProcessInfo> processInfo = new ArrayList<>();

		@DiffAnnotation("{\"type\":\"PrimaryKey\"}")
		public String getId() {
			return id;
		}

		@DiffAnnotation(value = {"{\"type\":\"Column\", \"columnName\":\"ProcessInfo_processName\"}", "{\"type\":\"Array\"}"})
		public ProcessInfo[] getProcessInfo() {
			return processInfo.toArray(new ProcessInfo[0]);
		}
	}
	
	@DiffAnnotation(value={
			"{\"type\":\"Root\", \"funcName\":\"PackageList_funcName\"}",
			"{\"type\":\"OrderBy\"," +
					"\"props\":[" +
					"\"PackageInfo\"," +
					"\"PackageInfo.*.packageId\"," +
					"\"PackageInfo.*.packageName\"," +
					"\"PackageInfo.*.version\"," +
					"\"PackageInfo.*.release\"," +
					"\"PackageInfo.*.installDate\"," +
					"\"PackageInfo.*.vendor\"," +
					"\"PackageInfo.*.architecture\"" +
					"]}"
	})
	public static class PackageRoot {
		public Map<String, Package> packageList = new HashMap<>();

		@DiffAnnotation("{\"type\":\"Comparison\"}")
		public Package[] getPackageList() {
			List<Package> list = new ArrayList<Package>(packageList.values());
			Collections.sort(list, new Comparator<Package>() {
				@Override
				public int compare(Package o1, Package o2) {
					return o1.id.compareTo(o2.id);
				}
			});
			return list.toArray(new Package[0]);
		}
	}

	@DiffAnnotation("{\"type\":\"Element\"}")
	public static class Package {
		public String id;

		public List<PackageInfo> packageInfo = new ArrayList<>();

		@DiffAnnotation("{\"type\":\"PrimaryKey\"}")
		public String getId() {
			return id;
		}

		@DiffAnnotation(value = {"{\"type\":\"Column\", \"columnName\":\"PackageInfo_packageId\"}", "{\"type\":\"Array\"}"})
		public PackageInfo[] getPackageInfo() {
			return packageInfo.toArray(new PackageInfo[0]);
		}
	}
	
	@DiffAnnotation(value={
			"{\"type\":\"Root\", \"funcName\":\"ProductList_funcName\"}",
			"{\"type\":\"OrderBy\"," +
					"\"props\":[" +
					"\"ProductInfo\"," +
					"\"ProductInfo.*.productName\"," +
					"\"ProductInfo.*.version\"," +
					"\"ProductInfo.*.path\"" +
					"]}"
	})
	public static class ProductRoot {
		public Map<String, Product> productList = new HashMap<>();

		@DiffAnnotation("{\"type\":\"Comparison\"}")
		public Product[] getProductList() {
			List<Product> list = new ArrayList<Product>(productList.values());
			Collections.sort(list, new Comparator<Product>() {
				@Override
				public int compare(Product o1, Product o2) {
					return o1.id.compareTo(o2.id);
				}
			});
			return list.toArray(new Product[0]);
		}
	}

	@DiffAnnotation("{\"type\":\"Element\"}")
	public static class Product {
		public String id;

		public List<ProductInfo> productInfo = new ArrayList<>();

		@DiffAnnotation("{\"type\":\"PrimaryKey\"}")
		public String getId() {
			return id;
		}

		@DiffAnnotation(value = {"{\"type\":\"Column\", \"columnName\":\"ProductInfo_productName\"}", "{\"type\":\"Array\"}"})
		public ProductInfo[] getProductInfo() {
			return productInfo.toArray(new ProductInfo[0]);
		}
	}
	
	@DiffAnnotation(value={
			"{\"type\":\"Root\", \"funcName\":\"LicenseList_funcName\"}",
			"{\"type\":\"OrderBy\"," +
					"\"props\":[" +
					"\"LicenseInfo\"," +
					"\"LicenseInfo.*.productName\"," +
					"\"LicenseInfo.*.vendor\"," +
					"\"LicenseInfo.*.serialNumber\"," +
					"\"LicenseInfo.*.count\"," +
					"\"LicenseInfo.*.expirationDate\"" +
					"]}"
	})
	public static class LicenseRoot {
		public Map<String, License> licenseList = new HashMap<>();

		@DiffAnnotation("{\"type\":\"Comparison\"}")
		public License[] getLicenseList() {
			List<License> list = new ArrayList<License>(licenseList.values());
			Collections.sort(list, new Comparator<License>() {
				@Override
				public int compare(License o1, License o2) {
					return o1.id.compareTo(o2.id);
				}
			});
			return list.toArray(new License[0]);
		}
	}

	@DiffAnnotation("{\"type\":\"Element\"}")
	public static class License {
		public String id;

		public List<LicenseInfo> licenseInfo = new ArrayList<>();

		@DiffAnnotation("{\"type\":\"PrimaryKey\"}")
		public String getId() {
			return id;
		}

		@DiffAnnotation(value = {"{\"type\":\"Column\", \"columnName\":\"LicenseInfo_productName\"}", "{\"type\":\"Array\"}"})
		public LicenseInfo[] getLicenseInfo() {
			return licenseInfo.toArray(new LicenseInfo[0]);
		}
	}
	
	@DiffAnnotation("{\"type\":\"Root\", \"funcName\":\"NodeVariableList_funcName\"}")
	public static class NodeVariableRoot {
		public Map<String, NodeVariable> nodeVariableList = new HashMap<String, NodeVariable>();

		@DiffAnnotation("{\"type\":\"Comparison\"}")
		public NodeVariable[] getNodeVariableList() {
			List<NodeVariable> list = new ArrayList<NodeVariable>(nodeVariableList.values());
			Collections.sort(list, new Comparator<NodeVariable>() {
				@Override
				public int compare(NodeVariable o1, NodeVariable o2) {
					return o1.id.compareTo(o2.id);
				}
			});
			return list.toArray(new NodeVariable[0]);
		}
	}

	@DiffAnnotation("{\"type\":\"Element\"}")
	public static class NodeVariable {
		public String id;

		public List<NodeVariableInfo> nodeVariableInfo = new ArrayList<NodeVariableInfo>();

		@DiffAnnotation("{\"type\":\"PrimaryKey\"}")
		public String getId() {
			return id;
		}

		@DiffAnnotation(value = {"{\"type\":\"Column\", \"columnName\":\"NodeVariableInfo_nodeVariableName\"}", "{\"type\":\"Array\"}"})
		public NodeVariableInfo[] getNodeVariableInfo() {
			return nodeVariableInfo.toArray(new NodeVariableInfo[0]);
		}
	}

	@DiffAnnotation("{\"type\":\"Root\", \"funcName\":\"NoteList_funcName\"}")
	public static class NoteRoot {
		public Map<String, Note> noteList = new HashMap<String, Note>();

		@DiffAnnotation("{\"type\":\"Comparison\"}")
		public Note[] getNoteList() {
			List<Note> list = new ArrayList<Note>(noteList.values());
			Collections.sort(list, new Comparator<Note>() {
				@Override
				public int compare(Note o1, Note o2) {
					return o1.id.compareTo(o2.id);
				}
			});
			return list.toArray(new Note[0]);
		}
	}

	@DiffAnnotation("{\"type\":\"Element\"}")
	public static class Note {
		public String id;

		public List<NoteInfo> noteInfo = new ArrayList<NoteInfo>();

		@DiffAnnotation("{\"type\":\"PrimaryKey\"}")
		public String getId() {
			return id;
		}
		@DiffAnnotation(value = {"{\"type\":\"Column\", \"columnName\":\"NoteInfo_noteId\"}", "{\"type\":\"Array\"}"})
		public NoteInfo[] getNote() {
			return noteInfo.toArray(new NoteInfo[0]);
		}
	}
	/**
	 *差分比較処理を行います。
	 * XMLファイル２つを比較する。
	 * 差分がある場合：差分をＣＳＶファイルで出力する。
	 * 差分がない場合：出力しない。
	 * 				   または、すでに存在している同一名のＣＳＶファイルを削除する。
	 * @param xmlNode1
	 * @param xmlHostname1
	 * @param xmlCPU1
	 * @param xmlMemory1
	 * @param xmlNetworkInterface1
	 * @param xmlDisk1
	 * @param xmlFS1
	 * @param xmlDevice1
	 * @param xmlVariable1
	 * @param xmlNote1
	 * @param xmlNode2
	 * @param xmlHostname2
	 * @param xmlCPU2
	 * @param xmlMemory2
	 * @param xmlNetworkInterface2
	 * @param xmlDisk2
	 * @param xmlFS2
	 * @param xmlDevice2
	 * @param xmlVariable2
	 * @param xmlNote2
	 * @return 終了コード
	 */
	@DiffMethod
	public int diffXml(
			String xmlNode1,
			String xmlHostname1,
			String xmlCPU1,
			String xmlMemory1,
			String xmlNetworkInterface1,
			String xmlDisk1,
			String xmlFS1,
			String xmlDevice1,
			String xmlNetstat1,
			String xmlProcess1,
			String xmlPackage1,
			String xmlProduct1,
			String xmlLicense1,
			String xmlVariable1,
			String xmlNote1,
			String xmlNode2,
			String xmlHostname2,
			String xmlCPU2,
			String xmlMemory2,
			String xmlNetworkInterface2,
			String xmlDisk2,
			String xmlFS2,
			String xmlDevice2,
			String xmlNetstat2,
			String xmlProcess2,
			String xmlPackage2,
			String xmlProduct2,
			String xmlLicense2,
			String xmlVariable2,
			String xmlNote2) {

		log.debug("Start Differrence PlatformRepositoryNode ");

		// 返り値変数(条件付き正常終了用）
		int ret = 0;
		// test
		boolean[] diffFlg = new boolean[1];
		diffFlg[0] = false;
		
		RepositoryNode node1 = null;
		RepositoryNode node2 = null;

		HostnameList hostname1 = null;
		HostnameList hostname2 = null;

		CPUList cpu1 = null;
		CPUList cpu2 = null;

		MemoryList memory1 = null;
		MemoryList memory2 = null;

		NetworkInterfaceList networkInterface1 = null;
		NetworkInterfaceList networkInterface2 = null;

		DiskList disk1 = null;
		DiskList disk2 = null;

		FSList fs1 = null;
		FSList fs2 = null;

		DeviceList device1 = null;
		DeviceList device2 = null;

		NetstatList netstat1 = null;
		NetstatList netstat2 = null;

		ProcessList process1 = null;
		ProcessList process2 = null;

		PackageList package1 = null;
		PackageList package2 = null;

		ProductList product1 = null;
		ProductList product2 = null;

		LicenseList license1 = null;
		LicenseList license2 = null;

		NodeVariableList variable1 = null;
		NodeVariableList variable2 = null;

		NoteList note1 = null;
		NoteList note2 = null;

		// XMLファイルからの読み込み
		try {
			node1 = RepositoryNode.unmarshal(new InputStreamReader(new FileInputStream(xmlNode1), "UTF-8"));
			node2 = RepositoryNode.unmarshal(new InputStreamReader(new FileInputStream(xmlNode2), "UTF-8"));

			hostname1 = HostnameList.unmarshal(new InputStreamReader(new FileInputStream(xmlHostname1), "UTF-8"));
			hostname2 = HostnameList.unmarshal(new InputStreamReader(new FileInputStream(xmlHostname2), "UTF-8"));

			cpu1 = CPUList.unmarshal(new InputStreamReader(new FileInputStream(xmlCPU1), "UTF-8"));
			cpu2 = CPUList.unmarshal(new InputStreamReader(new FileInputStream(xmlCPU2), "UTF-8"));

			memory1 = MemoryList.unmarshal(new InputStreamReader(new FileInputStream(xmlMemory1), "UTF-8"));
			memory2 = MemoryList.unmarshal(new InputStreamReader(new FileInputStream(xmlMemory2), "UTF-8"));

			networkInterface1 = NetworkInterfaceList.unmarshal(new InputStreamReader(new FileInputStream(xmlNetworkInterface1), "UTF-8"));
			networkInterface2 = NetworkInterfaceList.unmarshal(new InputStreamReader(new FileInputStream(xmlNetworkInterface2), "UTF-8"));

			disk1 = DiskList.unmarshal(new InputStreamReader(new FileInputStream(xmlDisk1), "UTF-8"));
			disk2 = DiskList.unmarshal(new InputStreamReader(new FileInputStream(xmlDisk2), "UTF-8"));

			fs1 = FSList.unmarshal(new InputStreamReader(new FileInputStream(xmlFS1), "UTF-8"));
			fs2 = FSList.unmarshal(new InputStreamReader(new FileInputStream(xmlFS2), "UTF-8"));

			device1 = DeviceList.unmarshal(new InputStreamReader(new FileInputStream(xmlDevice1), "UTF-8"));
			device2 = DeviceList.unmarshal(new InputStreamReader(new FileInputStream(xmlDevice2), "UTF-8"));

			netstat1 = NetstatList.unmarshal(new InputStreamReader(new FileInputStream(xmlNetstat1), "UTF-8"));
			netstat2 = NetstatList.unmarshal(new InputStreamReader(new FileInputStream(xmlNetstat2), "UTF-8"));

			process1 = ProcessList.unmarshal(new InputStreamReader(new FileInputStream(xmlProcess1), "UTF-8"));
			process2 = ProcessList.unmarshal(new InputStreamReader(new FileInputStream(xmlProcess2), "UTF-8"));

			package1 = PackageList.unmarshal(new InputStreamReader(new FileInputStream(xmlPackage1), "UTF-8"));
			package2 = PackageList.unmarshal(new InputStreamReader(new FileInputStream(xmlPackage2), "UTF-8"));

			product1 = ProductList.unmarshal(new InputStreamReader(new FileInputStream(xmlProduct1), "UTF-8"));
			product2 = ProductList.unmarshal(new InputStreamReader(new FileInputStream(xmlProduct2), "UTF-8"));

			license1 = LicenseList.unmarshal(new InputStreamReader(new FileInputStream(xmlLicense1), "UTF-8"));
			license2 = LicenseList.unmarshal(new InputStreamReader(new FileInputStream(xmlLicense2), "UTF-8"));

			variable1 = NodeVariableList.unmarshal(new InputStreamReader(new FileInputStream(xmlVariable1), "UTF-8"));
			variable2 = NodeVariableList.unmarshal(new InputStreamReader(new FileInputStream(xmlVariable2), "UTF-8"));

			note1 = NoteList.unmarshal(new InputStreamReader(new FileInputStream(xmlNote1), "UTF-8"));
			note2 = NoteList.unmarshal(new InputStreamReader(new FileInputStream(xmlNote2), "UTF-8"));

			sort(node1);
			sort(node2);

		} catch (FileNotFoundException e) {
			log.warn(e.getMessage());
			ret = SettingConstants.ERROR_INPROCESS;
			return ret;
		} catch (MarshalException | ValidationException | UnsupportedEncodingException e) {
			log.warn(Messages.getString("SettingTools.UnmarshalXmlFailed") + e.getMessage());
			ret = SettingConstants.ERROR_INPROCESS;
			return ret;
		} catch (Exception e) {
			log.error(Messages.getString("SettingTools.UnmarshalXmlFailed"), e);
			ret = SettingConstants.ERROR_INPROCESS;
			log.debug("End Differrence PlatformRepositoryNode (Error)");
			return ret;
		}

		/*スキーマのバージョンチェック*/
		if(!checkSchemaVersionNode(node1.getSchemaInfo())){
			ret = SettingConstants.ERROR_SCHEMA_VERSION;
			return ret;
		}
		if(!checkSchemaVersionNode(node2.getSchemaInfo())){
			ret = SettingConstants.ERROR_SCHEMA_VERSION;
			return ret;
		}

		//リポジトリノード差分比較
		//例外が発生したら、trueが返され、エラー種別をリターンする。
		if(output2(node1, node2, RepositoryNode.class,xmlNode2,diffFlg)){
			log.error(Messages.getString("SettingTools.EndWithErrorCode") );
			return SettingConstants.ERROR_INPROCESS;
		}
		if (diffFlg[0]){
			diffFlg[0] = false;
			ret += SettingConstants.SUCCESS_DIFF_1;
		}

		//ホスト名一覧差分比較
		HostnameRoot hostnameRoot1 = new HostnameRoot();
		for (HostnameInfo info: hostname1.getHostnameInfo()) {
			Hostname scope = hostnameRoot1.hostnameList.get(info.getFacilityId());
			if (scope == null) {
				scope = new Hostname();
				scope.id = info.getFacilityId();
				hostnameRoot1.hostnameList.put(info.getFacilityId(), scope);
			}
			scope.hostnameInfo.add(info);
		}
		HostnameRoot hostnameRoot2 = new HostnameRoot();
		for (HostnameInfo info: hostname2.getHostnameInfo()) {
			Hostname scope = hostnameRoot2.hostnameList.get(info.getFacilityId());
			if (scope == null) {
				scope = new Hostname();
				scope.id = info.getFacilityId();
				hostnameRoot2.hostnameList.put(info.getFacilityId(), scope);
			}
			scope.hostnameInfo.add(info);
		}
		//差分判定、csv出力判定を行う。正常に処理が行われたら、falseが返される。
		//例外が発生したら、trueが返され、エラー種別をリターンする。
		if(output2(hostnameRoot1, hostnameRoot2, HostnameRoot.class,xmlHostname2,diffFlg)){
			log.error(Messages.getString("SettingTools.EndWithErrorCode") );
			return SettingConstants.ERROR_INPROCESS;
		}
		if (diffFlg[0]){
			diffFlg[0] = false;
			ret += SettingConstants.SUCCESS_DIFF_2;
		}


		//CPU一覧差分比較
		CPURoot cpuRoot1 = new CPURoot();
		for (CPUInfo info: cpu1.getCPUInfo()) {
			Cpu scope = cpuRoot1.cpuList.get(info.getFacilityId());
			if (scope == null) {
				scope = new Cpu();
				scope.id = info.getFacilityId();
				cpuRoot1.cpuList.put(info.getFacilityId(), scope);
			}
			scope.cpuInfo.add(info);
		}
		CPURoot cpuRoot2 = new CPURoot();
		for (CPUInfo info: cpu2.getCPUInfo()) {
			Cpu scope = cpuRoot2.cpuList.get(info.getFacilityId());
			if (scope == null) {
				scope = new Cpu();
				scope.id = info.getFacilityId();
				cpuRoot2.cpuList.put(info.getFacilityId(), scope);
			}
			scope.cpuInfo.add(info);
		}
		//差分判定、csv出力判定を行う。正常に処理が行われたら、falseが返される。
		//例外が発生したら、trueが返され、エラー種別をリターンする。
		if(output2(cpuRoot1, cpuRoot2, CPURoot.class,xmlCPU2,diffFlg)){
			log.error(Messages.getString("SettingTools.EndWithErrorCode") );
			return SettingConstants.ERROR_INPROCESS;
		}
		if (diffFlg[0]){
			diffFlg[0] = false;
			ret += SettingConstants.SUCCESS_DIFF_3;
		}


		//メモリ情報一覧差分比較
		MemoryRoot memoryRoot1 = new MemoryRoot();
		for (MemoryInfo info: memory1.getMemoryInfo()) {
			Memory scope = memoryRoot1.memoryList.get(info.getFacilityId());
			if (scope == null) {
				scope = new Memory();
				scope.id = info.getFacilityId();
				memoryRoot1.memoryList.put(info.getFacilityId(), scope);
			}
			scope.memoryInfo.add(info);
		}
		MemoryRoot memoryRoot2 = new MemoryRoot();
		for (MemoryInfo info: memory2.getMemoryInfo()) {
			Memory scope = memoryRoot2.memoryList.get(info.getFacilityId());
			if (scope == null) {
				scope = new Memory();
				scope.id = info.getFacilityId();
				memoryRoot2.memoryList.put(info.getFacilityId(), scope);
			}
			scope.memoryInfo.add(info);
		}
		//差分判定、csv出力判定を行う。正常に処理が行われたら、falseが返される。
		//例外が発生したら、trueが返され、エラー種別をリターンする。
		if(output2(memoryRoot1, memoryRoot2, MemoryRoot.class,xmlMemory2,diffFlg)){
			log.error(Messages.getString("SettingTools.EndWithErrorCode") );
			return SettingConstants.ERROR_INPROCESS;
		}
		if (diffFlg[0]){
			diffFlg[0] = false;
			ret += SettingConstants.SUCCESS_DIFF_4;
		}


		//ネットワークインタフェース情報一覧差分比較
		NetworkInterfaceRoot networkInterfaceRoot1 = new NetworkInterfaceRoot();
		for (NetworkInterfaceInfo info: networkInterface1.getNetworkInterfaceInfo()) {
			NetworkInterface scope = networkInterfaceRoot1.networkInterfaceList.get(info.getFacilityId());
			if (scope == null) {
				scope = new NetworkInterface();
				scope.id = info.getFacilityId();
				networkInterfaceRoot1.networkInterfaceList.put(info.getFacilityId(), scope);
			}
			scope.networkInterfaceInfo.add(info);
		}
		NetworkInterfaceRoot networkInterfaceRoot2 = new NetworkInterfaceRoot();
		for (NetworkInterfaceInfo info: networkInterface2.getNetworkInterfaceInfo()) {
			NetworkInterface scope = networkInterfaceRoot2.networkInterfaceList.get(info.getFacilityId());
			if (scope == null) {
				scope = new NetworkInterface();
				scope.id = info.getFacilityId();
				networkInterfaceRoot2.networkInterfaceList.put(info.getFacilityId(), scope);
			}
			scope.networkInterfaceInfo.add(info);
		}
		//差分判定、csv出力判定を行う。正常に処理が行われたら、falseが返される。
		//例外が発生したら、trueが返され、エラー種別をリターンする。
		if(output2(networkInterfaceRoot1, networkInterfaceRoot2, NetworkInterfaceRoot.class,xmlNetworkInterface2,diffFlg)){
			log.error(Messages.getString("SettingTools.EndWithErrorCode") );
			return SettingConstants.ERROR_INPROCESS;
		}
		if (diffFlg[0]){
			diffFlg[0] = false;
			ret += SettingConstants.SUCCESS_DIFF_5;
		}


		//ディスク情報一覧差分比較
		DiskRoot diskRoot1 = new DiskRoot();
		for (DiskInfo info: disk1.getDiskInfo()) {
			Disk scope = diskRoot1.diskList.get(info.getFacilityId());
			if (scope == null) {
				scope = new Disk();
				scope.id = info.getFacilityId();
				diskRoot1.diskList.put(info.getFacilityId(), scope);
			}
			scope.diskInfo.add(info);
		}
		DiskRoot diskRoot2 = new DiskRoot();
		for (DiskInfo info: disk2.getDiskInfo()) {
			Disk scope = diskRoot2.diskList.get(info.getFacilityId());
			if (scope == null) {
				scope = new Disk();
				scope.id = info.getFacilityId();
				diskRoot2.diskList.put(info.getFacilityId(), scope);
			}
			scope.diskInfo.add(info);
		}
		//差分判定、csv出力判定を行う。正常に処理が行われたら、falseが返される。
		//例外が発生したら、trueが返され、エラー種別をリターンする。
		if(output2(diskRoot1, diskRoot2, DiskRoot.class,xmlDisk2,diffFlg)){
			log.error(Messages.getString("SettingTools.EndWithErrorCode") );
			return SettingConstants.ERROR_INPROCESS;
		}
		if (diffFlg[0]){
			diffFlg[0] = false;
			ret += SettingConstants.SUCCESS_DIFF_6;
		}


		//ファイルシステム定義一覧差分比較
		FSRoot fsRoot1 = new FSRoot();
		for (FSInfo info: fs1.getFSInfo()) {
			Fs scope = fsRoot1.fsList.get(info.getFacilityId());
			if (scope == null) {
				scope = new Fs();
				scope.id = info.getFacilityId();
				fsRoot1.fsList.put(info.getFacilityId(), scope);
			}
			scope.fsInfo.add(info);
		}
		FSRoot fsRoot2 = new FSRoot();
		for (FSInfo info: fs2.getFSInfo()) {
			Fs scope = fsRoot2.fsList.get(info.getFacilityId());
			if (scope == null) {
				scope = new Fs();
				scope.id = info.getFacilityId();
				fsRoot2.fsList.put(info.getFacilityId(), scope);
			}
			scope.fsInfo.add(info);
		}
		//差分判定、csv出力判定を行う。正常に処理が行われたら、falseが返される。
		//例外が発生したら、trueが返され、エラー種別をリターンする。
		if(output2(fsRoot1, fsRoot2, FSRoot.class,xmlFS2,diffFlg)){
			log.error(Messages.getString("SettingTools.EndWithErrorCode") );
			return SettingConstants.ERROR_INPROCESS;
		}
		if (diffFlg[0]){
			diffFlg[0] = false;
			ret += SettingConstants.SUCCESS_DIFF_7;
		}

		//デバイス定義一覧差分比較
		DeviceRoot deviceRoot1 = new DeviceRoot();
		for (DeviceInfo info: device1.getDeviceInfo()) {
		Device scope = deviceRoot1.deviceList.get(info.getFacilityId());
			if (scope == null) {
				scope = new Device();
				scope.id = info.getFacilityId();
				deviceRoot1.deviceList.put(info.getFacilityId(), scope);
			}
			scope.deviceInfo.add(info);
		}
		DeviceRoot deviceRoot2 = new DeviceRoot();
		for (DeviceInfo info: device2.getDeviceInfo()) {
			Device scope = deviceRoot2.deviceList.get(info.getFacilityId());
			if (scope == null) {
				scope = new Device();
				scope.id = info.getFacilityId();
				deviceRoot2.deviceList.put(info.getFacilityId(), scope);
			}
			scope.deviceInfo.add(info);
		}
		//差分判定、csv出力判定を行う。正常に処理が行われたら、falseが返される。
		//例外が発生したら、trueが返され、エラー種別をリターンする。
		if(output2(deviceRoot1, deviceRoot2, DeviceRoot.class,xmlDevice2,diffFlg)){
			log.error(Messages.getString("SettingTools.EndWithErrorCode") );
			return SettingConstants.ERROR_INPROCESS;
		}
		if (diffFlg[0]){
			diffFlg[0] = false;
			ret += SettingConstants.SUCCESS_DIFF_8;
		}

		//ノード変数一覧差分比較
		NodeVariableRoot nodeVariableRoot1 = new NodeVariableRoot();
		for (NodeVariableInfo info: variable1.getNodeVariableInfo()) {
			NodeVariable scope = nodeVariableRoot1.nodeVariableList.get(info.getFacilityId());
			if (scope == null) {
				scope = new NodeVariable();
				scope.id = info.getFacilityId();
				nodeVariableRoot1.nodeVariableList.put(info.getFacilityId(), scope);
			}
			scope.nodeVariableInfo.add(info);
		}
		NodeVariableRoot nodeVariableRoot2 = new NodeVariableRoot();
		for (NodeVariableInfo info: variable2.getNodeVariableInfo()) {
			NodeVariable scope = nodeVariableRoot2.nodeVariableList.get(info.getFacilityId());
			if (scope == null) {
				scope = new NodeVariable();
				scope.id = info.getFacilityId();
				nodeVariableRoot2.nodeVariableList.put(info.getFacilityId(), scope);
			}
			scope.nodeVariableInfo.add(info);
		}
		//差分判定、csv出力判定を行う。正常に処理が行われたら、falseが返される。
		//例外が発生したら、trueが返され、エラー種別をリターンする。
		if(output2(nodeVariableRoot1, nodeVariableRoot2, NodeVariableRoot.class,xmlVariable2,diffFlg)){
			log.error(Messages.getString("SettingTools.EndWithErrorCode") );
			return SettingConstants.ERROR_INPROCESS;
		}
		if (diffFlg[0]){
			diffFlg[0] = false;
			ret += SettingConstants.SUCCESS_DIFF_9;
		}

		//備考一覧差分比較
		NoteRoot noteRoot1 = new NoteRoot();
		for (NoteInfo info: note1.getNoteInfo()) {
			Note scope = noteRoot1.noteList.get(info.getFacilityId());
			if (scope == null) {
				scope = new Note();
				scope.id = info.getFacilityId();
				noteRoot1.noteList.put(info.getFacilityId(), scope);
			}
			scope.noteInfo.add(info);
		}
		NoteRoot noteRoot2 = new NoteRoot();
		for (NoteInfo info: note2.getNoteInfo()) {
			Note scope = noteRoot2.noteList.get(info.getFacilityId());
			if (scope == null) {
				scope = new Note();
				scope.id = info.getFacilityId();
				noteRoot2.noteList.put(info.getFacilityId(), scope);
			}
			scope.noteInfo.add(info);
		}
		//差分判定、csv出力判定を行う。正常に処理が行われたら、falseが返される。
		//例外が発生したら、trueが返され、エラー種別をリターンする。
		if(output2(noteRoot1, noteRoot2,NoteRoot.class, xmlNote2,diffFlg)){
			log.error(Messages.getString("SettingTools.EndWithErrorCode") );
			return SettingConstants.ERROR_INPROCESS;
		}
		if (diffFlg[0]){
			diffFlg[0] = false;
			ret += SettingConstants.SUCCESS_DIFF_10;
		}

		//ネットワーク情報
		NetstatRoot netstatRoot1 = new NetstatRoot();
		for (NetstatInfo info: netstat1.getNetstatInfo()) {
			Netstat scope = netstatRoot1.netstatList.get(info.getFacilityId());
			if (scope == null) {
				scope = new Netstat();
				scope.id = info.getFacilityId();
				netstatRoot1.netstatList.put(info.getFacilityId(), scope);
			}
			scope.netstatInfo.add(info);
		}
		NetstatRoot netstatRoot2 = new NetstatRoot();
		for (NetstatInfo info: netstat2.getNetstatInfo()) {
			Netstat scope = netstatRoot2.netstatList.get(info.getFacilityId());
			if (scope == null) {
				scope = new Netstat();
				scope.id = info.getFacilityId();
				netstatRoot2.netstatList.put(info.getFacilityId(), scope);
			}
			scope.netstatInfo.add(info);
		}
		//差分判定、csv出力判定を行う。正常に処理が行われたら、falseが返される。
		//例外が発生したら、trueが返され、エラー種別をリターンする。
		if(output2(netstatRoot1, netstatRoot2, NetstatRoot.class,xmlNetstat2,diffFlg)){
			log.error(Messages.getString("SettingTools.EndWithErrorCode") );
			return SettingConstants.ERROR_INPROCESS;
		}
		if (diffFlg[0]){
			diffFlg[0] = false;
			ret += SettingConstants.SUCCESS_DIFF_11;
		}
		
		//プロセス情報
		ProcessRoot processRoot1 = new ProcessRoot();
		for (ProcessInfo info: process1.getProcessInfo()) {
			Process scope = processRoot1.processList.get(info.getFacilityId());
			if (scope == null) {
				scope = new Process();
				scope.id = info.getFacilityId();
				processRoot1.processList.put(info.getFacilityId(), scope);
			}
			scope.processInfo.add(info);
		}
		ProcessRoot processRoot2 = new ProcessRoot();
		for (ProcessInfo info: process2.getProcessInfo()) {
			Process scope = processRoot2.processList.get(info.getFacilityId());
			if (scope == null) {
				scope = new Process();
				scope.id = info.getFacilityId();
				processRoot2.processList.put(info.getFacilityId(), scope);
			}
			scope.processInfo.add(info);
		}
		//差分判定、csv出力判定を行う。正常に処理が行われたら、falseが返される。
		//例外が発生したら、trueが返され、エラー種別をリターンする。
		if(output2(processRoot1, processRoot2, ProcessRoot.class,xmlProcess2,diffFlg)){
			log.error(Messages.getString("SettingTools.EndWithErrorCode") );
			return SettingConstants.ERROR_INPROCESS;
		}
		if (diffFlg[0]){
			diffFlg[0] = false;
			ret += SettingConstants.SUCCESS_DIFF_12;
		}

		//パッケージ情報
		PackageRoot packageRoot1 = new PackageRoot();
		for (PackageInfo info: package1.getPackageInfo()) {
			Package scope = packageRoot1.packageList.get(info.getFacilityId());
			if (scope == null) {
				scope = new Package();
				scope.id = info.getFacilityId();
				packageRoot1.packageList.put(info.getFacilityId(), scope);
			}
			scope.packageInfo.add(info);
		}
		PackageRoot packageRoot2 = new PackageRoot();
		for (PackageInfo info: package2.getPackageInfo()) {
			Package scope = packageRoot2.packageList.get(info.getFacilityId());
			if (scope == null) {
				scope = new Package();
				scope.id = info.getFacilityId();
				packageRoot2.packageList.put(info.getFacilityId(), scope);
			}
			scope.packageInfo.add(info);
		}
		//差分判定、csv出力判定を行う。正常に処理が行われたら、falseが返される。
		//例外が発生したら、trueが返され、エラー種別をリターンする。
		if(output2(packageRoot1, packageRoot2, PackageRoot.class,xmlPackage2,diffFlg)){
			log.error(Messages.getString("SettingTools.EndWithErrorCode") );
			return SettingConstants.ERROR_INPROCESS;
		}
		if (diffFlg[0]){
			diffFlg[0] = false;
			ret += SettingConstants.SUCCESS_DIFF_13;
		}

		//個別導入製品情報
		ProductRoot productRoot1 = new ProductRoot();
		for (ProductInfo info: product1.getProductInfo()) {
			Product scope = productRoot1.productList.get(info.getFacilityId());
			if (scope == null) {
				scope = new Product();
				scope.id = info.getFacilityId();
				productRoot1.productList.put(info.getFacilityId(), scope);
			}
			scope.productInfo.add(info);
		}
		ProductRoot productRoot2 = new ProductRoot();
		for (ProductInfo info: product2.getProductInfo()) {
			Product scope = productRoot2.productList.get(info.getFacilityId());
			if (scope == null) {
				scope = new Product();
				scope.id = info.getFacilityId();
				productRoot2.productList.put(info.getFacilityId(), scope);
			}
			scope.productInfo.add(info);
		}
		//差分判定、csv出力判定を行う。正常に処理が行われたら、falseが返される。
		//例外が発生したら、trueが返され、エラー種別をリターンする。
		if(output2(productRoot1, productRoot2, ProductRoot.class,xmlProduct2,diffFlg)){
			log.error(Messages.getString("SettingTools.EndWithErrorCode") );
			return SettingConstants.ERROR_INPROCESS;
		}
		if (diffFlg[0]){
			diffFlg[0] = false;
			ret += SettingConstants.SUCCESS_DIFF_14;
		}

		//ライセンス情報
		LicenseRoot licenseRoot1 = new LicenseRoot();
		for (LicenseInfo info: license1.getLicenseInfo()) {
			License scope = licenseRoot1.licenseList.get(info.getFacilityId());
			if (scope == null) {
				scope = new License();
				scope.id = info.getFacilityId();
				licenseRoot1.licenseList.put(info.getFacilityId(), scope);
			}
			scope.licenseInfo.add(info);
		}
		LicenseRoot licenseRoot2 = new LicenseRoot();
		for (LicenseInfo info: license2.getLicenseInfo()) {
			License scope = licenseRoot2.licenseList.get(info.getFacilityId());
			if (scope == null) {
				scope = new License();
				scope.id = info.getFacilityId();
				licenseRoot2.licenseList.put(info.getFacilityId(), scope);
			}
			scope.licenseInfo.add(info);
		}
		//差分判定、csv出力判定を行う。正常に処理が行われたら、falseが返される。
		//例外が発生したら、trueが返され、エラー種別をリターンする。
		if(output2(licenseRoot1, licenseRoot2, LicenseRoot.class,xmlLicense2,diffFlg)){
			log.error(Messages.getString("SettingTools.EndWithErrorCode") );
			return SettingConstants.ERROR_INPROCESS;
		}
		if (diffFlg[0]){
			diffFlg[0] = false;
			ret += SettingConstants.SUCCESS_DIFF_15;
		}

		// 処理の終了
		if ((ret >= SettingConstants.SUCCESS) && (ret<=SettingConstants.SUCCESS_MAX)){
			log.info(Messages.getString("SettingTools.DiffCompleted"));
		}else{
			log.error(Messages.getString("SettingTools.EndWithErrorCode") );
		}
		
		log.debug("End Differrence PlatformRepositoryNode");

		return ret;
	}

	/**
	 * 差分比較を行う処理をまとめたもの。
	 *
	 * 差分がある場合、csvファイルを出力。
	 * 差分がない場合、出力しない。または、すでに存在する同一名のcsvファイルを削除する。
	 * @param obj1
	 * @param obj2
	 * @param thisClass
	 * @param filePath2
	 * @return 正常動作：false, 例外発生：true
	 */
	private boolean output2(Object obj1, Object obj2, Class<?> thisClass, String filePath2,boolean[] diffFlg){
		FileOutputStream fos = null;
		try {
			ResultA resultA = new ResultA();
			//比較処理に渡す
			boolean diff = DiffUtil.diffCheck2(obj1, obj2, thisClass, resultA);
			assert resultA.getResultBs().size() == 1;
			
			if (diff){
				diffFlg[0] = true;
			}
			//差分がある場合、ＣＳＶファイル作成
			if (diff || DiffUtil.isAll()) {
				CSVUtil.CSVSerializer csvSerializer = CSVUtil.createCSVSerializer();
				fos = new FileOutputStream(filePath2 + ".csv");
				csvSerializer.write(fos, resultA.getResultBs().values().iterator().next());
			}
			//差分がない場合、すでに作成済みのＣＳＶファイルがあれば、削除
			else {
				File f = new File(filePath2 + ".csv");
				if (f.exists()) {
					if (!f.delete())
						log.warn(String.format("Fail to delete file. %s", f.getAbsolutePath()));
				}
			}
		}
		catch (Exception e) {
			getLogger().error("unexpected: ", e);
			return true;
		}
		finally {
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
				}
			}
		}
		return false;
	}

	private void sort(RepositoryNode repositoryNode) {
		NodeInfo[] infoList = repositoryNode.getNodeInfo();
		Arrays.sort(
			infoList,
			new Comparator<NodeInfo>() {
				@Override
				public int compare(NodeInfo info1, NodeInfo info2) {
					return info1.getFacilityId().compareTo(info2.getFacilityId());
				}
			});
		 repositoryNode.setNodeInfo(infoList);
	}

	public Logger getLogger() {
		return log;
	}
	

	protected void checkDelete(RepositoryNode node){
		List<com.clustercontrol.ws.repository.NodeInfo> subList = null;
		try {
			subList = RepositoryEndpointWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).getNodeListAll();
		}
		catch (Exception e) {
			getLogger().error(Messages.getString("SettingTools.FailToGetList") + " : " + HinemosMessage.replace(e.getMessage()));
			getLogger().debug(e.getMessage(), e);
		}
		
		if(subList == null || subList.size() <= 0){
			return;
		}
		
		List<NodeInfo> xmlElementList = new ArrayList<>(Arrays.asList(node.getNodeInfo()));
		for(com.clustercontrol.ws.repository.NodeInfo mgrInfo: new ArrayList<>(subList)){
			for(NodeInfo xmlElement: new ArrayList<>(xmlElementList)){
				if(mgrInfo.getFacilityId().equals(xmlElement.getFacilityId()) || RepositoryConv.checkInternalScope(mgrInfo.getFacilityId())){
					subList.remove(mgrInfo);
					xmlElementList.remove(xmlElement);
					break;
				}
			}
		}
		
		if(subList.size() > 0){
			for(com.clustercontrol.ws.repository.NodeInfo info: subList){
				//マネージャのみに存在するデータがあった場合の削除方法を確認する
				if(!DeleteProcessMode.isSameprocess()){
					String[] args = {info.getFacilityId()};
					DeleteProcessDialog dialog = UtilityDialogInjector.createDeleteProcessDialog(
							null, Messages.getString("message.delete.confirm4", args));
					DeleteProcessMode.setProcesstype(dialog.open());
					DeleteProcessMode.setSameprocess(dialog.getToggleState());
				}
			    
			    if(DeleteProcessMode.getProcesstype() == UtilityDialogConstant.DELETE){
			    	try {
			    		List<String> args = new ArrayList<>();
			    		args.add(info.getFacilityId());
			    		RepositoryEndpointWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).deleteNode(args);
			    		getLogger().info(Messages.getString("SettingTools.SubSucceeded.Delete") + " : " + info.getFacilityId());
					} catch (Exception e1) {
						getLogger().warn(Messages.getString("SettingTools.ClearFailed") + " : " + HinemosMessage.replace(e1.getMessage()));
					}
			    } else if(DeleteProcessMode.getProcesstype() == UtilityDialogConstant.SKIP){
			    	getLogger().info(Messages.getString("SettingTools.SubSucceeded.Skip") + " : " + info.getFacilityId());
			    } else if(DeleteProcessMode.getProcesstype() == UtilityDialogConstant.CANCEL){
			    	getLogger().info(Messages.getString("SettingTools.SubSucceeded.Cancel"));
			    	return;
			    }
			}
		}
	}
}
