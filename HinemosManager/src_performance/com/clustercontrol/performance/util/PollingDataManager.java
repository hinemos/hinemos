package com.clustercontrol.performance.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.fault.FacilityNotFound;
import com.clustercontrol.performance.monitor.entity.CollectorItemCodeMstData;
import com.clustercontrol.repository.bean.DeviceTypeConstant;
import com.clustercontrol.repository.factory.NodeProperty;
import com.clustercontrol.repository.model.NodeDeviceInfo;
import com.clustercontrol.repository.model.NodeDiskInfo;
import com.clustercontrol.repository.model.NodeInfo;
import com.clustercontrol.repository.model.NodeNetworkInterfaceInfo;

/**
 * リソース監視用のポーリングのためのデータを扱うクラス
 * 
 * @version 4.0.0
 * @since 4.0.0
 *
 */
public class PollingDataManager {

	private static Log m_log = LogFactory.getLog( PollingDataManager.class );

	public static String ALL_DEVICE_NAME = "*ALL*";

	// コンストラクタ引数
	private String facilityId = null;
	private String itemCode = null;
	private boolean breakdownFlg = false;
	private String categoryCode = null;
	private String platformId = null;
	private String subPlatformId = null;

	// ポーリングの収集項目IDリスト(内訳を収集する場合のみ複数)
	private ArrayList<String> itemCodeList = null;
	// ポーリングのターゲットリスト(cc_collector_polling_mst.polling_target)
	private List<String> pollingTargets = null;
	// ポーリングの方法(SNMP/WBEMなど)
	private String collectMethod = null;
	// ポーリングの収集項目IDのデバイスタイプ(cc_collector_item_code_mst.device_type)
	private String deviceType = null;

	// facilityIdのノード情報
	private NodeInfo nodeInfo = null;

	public PollingDataManager(String facilityId, String itemCode, boolean breakdownFlg) throws FacilityNotFound{
		m_log.debug("PollingDataManager() facilityId = " + facilityId + ", itemCode = " + itemCode + ", breakdownFlg = " + breakdownFlg);
		this.facilityId = facilityId;
		this.itemCode = itemCode;
		this.breakdownFlg = breakdownFlg;

		try{
			////
			// itemCodeからcategory_codeを特定(cc_collector_item_code_mst)
			////
			m_log.debug("PollingDataManager() specify categoryCode");
			CollectorItemCodeMstData itemCodeMstData = CollectorMasterCache.getCategoryCodeMst(this.itemCode);
			this.categoryCode = itemCodeMstData.getCategoryCode();
			this.deviceType = itemCodeMstData.getDeviceType();
			m_log.debug("PollingDataManager() categoryCode = " + categoryCode);

			////
			// facilityIdからplatform_idとsub_platform_idを取得する(Repository)
			////
			m_log.debug("PollingDataManager() specify platformId and subPlatformId");

			// プラットフォームIDとサブプラットフォームID
			this.nodeInfo = NodeProperty.getProperty(this.facilityId);
			this.platformId = nodeInfo.getPlatformFamily();
			this.subPlatformId = nodeInfo.getSubPlatformFamily();
			m_log.debug("PollingDataManager() platformId = " + platformId + ", subPlatformId = " + subPlatformId);

			////
			// platform_idとsub_platform_idからcollect_methodを取得する(cc_collector_category_collect_mst)
			////
			m_log.debug("PollingDataManager() specify collectMethod");
			this.collectMethod = CollectorMasterCache.getCollectMethod(platformId, subPlatformId, categoryCode);
			m_log.debug("PollingDataManager() collectMethod = " + collectMethod);

			////
			// 内訳が有効の場合、itemCodeを親とするitemCode(children)を探し、それがcollect_method/platform_id/sub_platform_idで存在する場合に、対象に追加する
			// cc_collector_item_code_mst -> cc_collector_item_calc_method_mst
			////
			ArrayList<String> breakdownItemCodeList = new ArrayList<String>();
			if(this.breakdownFlg){
				breakdownItemCodeList = CollectorMasterCache.getBreakdownItemCodeList(itemCode, collectMethod, platformId, subPlatformId);
			}
			else{
				m_log.debug("PollingDataManager() not breakdown");
			}

			////
			// collect_method/platform_id/sub_platform_id/item_codeに対応するpolling_targetをmapに追加する(cc_collector_polling_mst)
			////
			m_log.debug("PollingDataManager() specify pollingTarget");
			breakdownItemCodeList.add(itemCode);
			this.itemCodeList = breakdownItemCodeList;
			this.pollingTargets = CollectorMasterCache.getPollingTarget(breakdownItemCodeList, collectMethod, platformId, subPlatformId);

		} catch (FacilityNotFound e) {
			// TODO: Internal Event?
			throw e;
		}
	}

	/**
	 * 収集方法(SNMP/WBEMなど)を返却
	 * @return
	 */
	public String getCollectMethod(){
		return collectMethod;
	}

	/**
	 * ポーリング対象の文字列(SNMPはOID/WBEMはClassName.propertyName)を返却
	 * 
	 * @return
	 */
	public List<String> getPollingTargets(){
		return pollingTargets;
	}

	/**
	 * ポーリング対象の収集項目IDのリスト(内訳も収集する場合は複数項目あり)
	 * 
	 * @return
	 */
	public ArrayList<String> getItemCodeList() {
		return itemCodeList;
	}

	/**
	 * デバイス種別
	 * 
	 * @return
	 */
	public String getDeviceType() {
		return deviceType;
	}

	/**
	 * 対象ノードのプラットフォーム
	 * 
	 * @return
	 */
	public String getPlatformId(){
		return nodeInfo.getPlatformFamily();
	}

	/**
	 * 対象ノードのサブプラットフォーム
	 * 
	 * @return
	 */
	public String getSubPlatformId(){
		return nodeInfo.getSubPlatformFamily();
	}

	/**
	 * 対象ノードのファシリティ名
	 * 
	 * @return
	 */
	public String getFacilityName(){
		return nodeInfo.getFacilityName();
	}

	/**
	 * 対象のデバイス情報
	 * 
	 * @return
	 */
	public List<? extends NodeDeviceInfo> getDeviceList(){
		List<? extends NodeDeviceInfo> deviceList = null;

		if(DeviceTypeConstant.DEVICE_CPU.equals(getDeviceType())){
			deviceList = nodeInfo.getNodeCpuInfo();
		}
		else if(DeviceTypeConstant.DEVICE_MEM.equals(getDeviceType())){
			deviceList = nodeInfo.getNodeMemoryInfo();
		}
		else if(DeviceTypeConstant.DEVICE_NIC.equals(getDeviceType())){
			List<NodeNetworkInterfaceInfo> nicList = new ArrayList<NodeNetworkInterfaceInfo>();
			for (NodeNetworkInterfaceInfo nic : nodeInfo.getNodeNetworkInterfaceInfo()) {
				if (DeviceTypeConstant.DEVICE_NIC.equals(nic.getDeviceType())) {
					nicList.add(nic);
				}
			}
			deviceList = nicList;
		}
		else if(DeviceTypeConstant.DEVICE_VIRT_NIC.equals(getDeviceType())){
			List<NodeNetworkInterfaceInfo> nicList = new ArrayList<NodeNetworkInterfaceInfo>();
			for (NodeNetworkInterfaceInfo nic : nodeInfo.getNodeNetworkInterfaceInfo()) {
				if (DeviceTypeConstant.DEVICE_VIRT_NIC.equals(nic.getDeviceType())) {
					nicList.add(nic);
				}
			}
			deviceList = nicList;
		}
		else if(DeviceTypeConstant.DEVICE_DISK.equals(getDeviceType())){
			List<NodeDiskInfo> diskList = new ArrayList<NodeDiskInfo>();
			for (NodeDiskInfo disk : nodeInfo.getNodeDiskInfo()) {
				if (DeviceTypeConstant.DEVICE_DISK.equals(disk.getDeviceType())) {
					diskList.add(disk);
				}
			}
			deviceList = diskList;
		}
		else if(DeviceTypeConstant.DEVICE_VIRT_DISK.equals(getDeviceType())){
			List<NodeDiskInfo> diskList = new ArrayList<NodeDiskInfo>();
			for (NodeDiskInfo disk : nodeInfo.getNodeDiskInfo()) {
				if (DeviceTypeConstant.DEVICE_VIRT_DISK.equals(disk.getDeviceType())) {
					diskList.add(disk);
				}
			}
			deviceList = diskList;
		}
		else if(DeviceTypeConstant.DEVICE_FILESYSTEM.equals(getDeviceType())){
			deviceList = nodeInfo.getNodeFilesystemInfo();
		}
		else if(DeviceTypeConstant.DEVICE_GENERAL.equals(getDeviceType())){
			deviceList = nodeInfo.getNodeDeviceInfo();
		}

		if(deviceList == null){
			deviceList = new ArrayList<NodeDeviceInfo>();
		}

		return deviceList;
	}
}
