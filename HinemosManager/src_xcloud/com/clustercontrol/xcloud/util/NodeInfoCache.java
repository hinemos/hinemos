/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.util;

import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.fault.FacilityNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.repository.bean.DeviceSearchMessageInfo;
import com.clustercontrol.repository.bean.NodeInfoDeviceSearch;
import com.clustercontrol.repository.model.NodeDeviceInfo;
import com.clustercontrol.repository.model.NodeInfo;
import com.clustercontrol.repository.session.RepositoryControllerBean;
import com.clustercontrol.util.MessageConstant;
import com.clustercontrol.xcloud.CloudManagerException;
import com.clustercontrol.xcloud.common.CloudMessageConstant;
import com.clustercontrol.xcloud.common.ErrorCode;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

public class NodeInfoCache {

	public static abstract class NodeDeviceInfoMixin {
		@JsonIdentityReference(alwaysAsId = true)
		public abstract NodeInfo getNodeEntity();
	}

	@JsonIdentityInfo(generator=ObjectIdGenerators.PropertyGenerator.class, property = "facilityId")
	public static abstract class NodeInfoMixin {
	}

	public interface ModifyListener {
		void modified(NodeInfo nodeInfo) throws CloudManagerException;
	}
	
	private Map<String, Tuple> nodeInfoMap = new HashMap<>();

	public static NodeInfo getNodeInfo(String facilityId) throws FacilityNotFound, CloudManagerException {
		Session session = deque.get().peekFirst();
		if (session == null) {
			try {
				RepositoryControllerBean repositoryControllerBean = RepositoryControllerBeanWrapper.bean();
				return repositoryControllerBean.getNode(facilityId);
			} catch (HinemosUnknown e) {
				throw ErrorCode.HINEMOS_MANAGER_ERROR.cloudManagerFault(e);
			}
		} else {
			return session.cache.internalGetNodeInfo(facilityId);
		}
	}

	protected NodeInfo internalGetNodeInfo(String facilityId) throws FacilityNotFound, CloudManagerException {
		Tuple node = nodeInfoMap.get(facilityId);
		if (node != null)
			return node.get(0, NodeInfo.class);

		try {
			RepositoryControllerBean repositoryControllerBean = RepositoryControllerBeanWrapper.bean();

			NodeInfo origin = repositoryControllerBean.getNode(facilityId);
			node = new Tuple(origin.clone(), origin.clone());
		} catch (HinemosUnknown e) {
			throw ErrorCode.HINEMOS_MANAGER_ERROR.cloudManagerFault(e);
		}
		nodeInfoMap.put(facilityId, node);
		return node.get(0);
	}

	private static ThreadLocal<Deque<Session>> deque  = new ThreadLocal<Deque<Session>>() {
		protected Deque<Session> initialValue() {
			return new LinkedList<Session>();
		}
	};

	protected static class Session {
		Map<String, List<ModifyListener>> targets = new HashMap<>();
		NodeInfoCache cache = new NodeInfoCache();
	}

	public static enum SessoinType {
		Required,
		RequiredNew
	};

	public static class NodeInfoCacheScope implements AutoCloseable {
		private boolean doDiffCheck = true;
		private boolean initiate = false;
		
		public NodeInfoCacheScope(SessoinType sessionType) {
			this(sessionType, true);
		}
		
		public NodeInfoCacheScope(boolean doDiffCheck) {
			this(SessoinType.Required, doDiffCheck);
		}

		public NodeInfoCacheScope() {
			this(SessoinType.Required, true);
		}
		
		public NodeInfoCacheScope(SessoinType sessionType, boolean doDiffCheck) {
			switch(sessionType) {
			case Required:
			{
				Session session = deque.get().peekFirst();
				if (session == null) {
					session = new Session();
					deque.get().offerFirst(session);
					initiate = true;
				}
			}
			break;
			case RequiredNew:
			{
				Session session = new Session();
				deque.get().offerFirst(session);
				initiate = true;
			}
			break;
			default:
				break;

			}
			
			this.doDiffCheck = doDiffCheck;
		}

		public void modified(String facilityId) {
			modified(facilityId, null);
		}

		public void modified(String facilityId, ModifyListener listener) {
			Session session = deque.get().peekFirst();
			List<ModifyListener> listeners = session.targets.get(facilityId);
			if (listeners == null) {
				listeners = new ArrayList<>();
				session.targets.put(facilityId, listeners);
			}
			if (listener != null)
				listeners.add(listener);
		}

		@Override
		public void close() throws CloudManagerException {
			if (!initiate)
				return;

			Session session = deque.get().pollFirst();
			RepositoryControllerBean repositoryControllerBean = RepositoryControllerBeanWrapper.bean();
			for (Map.Entry<String, List<ModifyListener>> entry: session.targets.entrySet()) {
				Tuple nodeInfo = session.cache.nodeInfoMap.get(entry.getKey());
				if (nodeInfo == null)
					continue;

				ObjectMapper om = new ObjectMapper();
				om.addMixIn(NodeDeviceInfo.class, NodeDeviceInfoMixin.class);
				om.addMixIn(NodeInfo.class, NodeInfoMixin.class);

				ObjectWriter ow = om.writer();
				try {
					String modified = ow.writeValueAsString(nodeInfo.get(0));
					String copied = ow.writeValueAsString(nodeInfo.get(1));
					if (!modified.equals(copied)) {
						repositoryControllerBean.modifyNode(nodeInfo.get(0, NodeInfo.class));
						
						NodeInfoDeviceSearch device = new NodeInfoDeviceSearch();
						device.setNodeInfo(nodeInfo.get(0, NodeInfo.class));
						device.equalsNodeInfo(nodeInfo.get(1, NodeInfo.class));
						
						for (ModifyListener listener: entry.getValue()) {
							listener.modified(nodeInfo.get(0, NodeInfo.class));
						}
						
						if (doDiffCheck) {
							//差分チェックを行う
							List<DeviceSearchMessageInfo> diffMasages = diffNodeInfo(nodeInfo.get(1, NodeInfo.class), nodeInfo.get(0, NodeInfo.class));

							//出力メッセージの作成
							String details ="";
							for (DeviceSearchMessageInfo msgInfo : diffMasages) {
								details = details.length() > 0 ? details + ", " : details;
								details = details + msgInfo.getItemName() + " "
										+ MessageConstant.LASTTIME.getMessage() + ":" + msgInfo.getLastVal() + " "
										+ MessageConstant.THISTIME.getMessage() + ":" + msgInfo.getThisVal();
							}
							//インターナルイベントへの通知
							String msg = CloudMessageConstant.EXECUTED_AUTO_SEARCH.getMessage() + " " + details;

							try {
								CloudUtil.notifyInternalMessage(
										CloudUtil.Priority.INFO,
										CloudMessageUtil.pluginId_cloud,
										HinemosModuleConstant.SYSYTEM,
										"",
										nodeInfo.get(0, NodeInfo.class).getFacilityId(),
										CloudMessageConstant.AUTODETECTION.getMessage(),
										msg,
										"");
							} catch (Exception e ) {
								//internal event(auto detection failed)
								String errorMsg = CloudMessageConstant.EXECUTED_AUTO_SEARCH_FAILED.getMessage() + " " + details;
								Logger.getLogger(this.getClass()).warn(e.getMessage(), e);
								CloudUtil.notifyInternalMessage(
										CloudUtil.Priority.WARNING,
										CloudMessageUtil.pluginId_cloud,
										HinemosModuleConstant.SYSYTEM,
										"",
										nodeInfo.get(0, NodeInfo.class).getFacilityId(),
										CloudMessageConstant.AUTODETECTION.getMessage(),
										errorMsg,
										"");
							}
						}
					}
				} catch (JsonProcessingException e) {
					//internal event(auto detection failed)
					String errorMsg = CloudMessageConstant.EXECUTED_AUTO_SEARCH_FAILED.getMessage();
					Logger.getLogger(this.getClass()).warn(e.getMessage(), e);
					CloudUtil.notifyInternalMessage(
							CloudUtil.Priority.WARNING,
							CloudMessageUtil.pluginId_cloud,
							HinemosModuleConstant.SYSYTEM,
							"",
							nodeInfo.get(0, NodeInfo.class).getFacilityId(),
							"Auto detection failed",
							errorMsg,
							errorMsg);
				} catch (HinemosUnknown | InvalidSetting | InvalidRole e) {
					//internal event(auto detection failed)
					String errorMsg = CloudMessageConstant.EXECUTED_AUTO_SEARCH_FAILED.getMessage();
					Logger.getLogger(this.getClass()).warn(ErrorCode.HINEMOS_MANAGER_ERROR.cloudManagerFault(e).getMessage());
					CloudUtil.notifyInternalMessage(
							CloudUtil.Priority.WARNING,
							CloudMessageUtil.pluginId_cloud,
							HinemosModuleConstant.SYSYTEM,
							"",
							nodeInfo.get(0, NodeInfo.class).getFacilityId(),
							"Auto detection failed",
							errorMsg,
							errorMsg);
				}
			}
		}

		/** Cloud 固有のノードプロパティ,ホスト名の差分をチェックし、差分メッセージを返す<br> 
		 *  ※NodeInfoDeviceSearchクラスのequalsNodeInfoメソッド呼び出しにより、
		 *  各デバイスの差分情報については通常差分チェックが行われる<br>
		 *  ※デバイスサーチはプロパティの変更により更新を行わないという事ができ、
		 *  更新を行わない場合、差分チェックし、差分メッセージの格納は行われないので更新対象は全チェック
		 *  */
		private static List<DeviceSearchMessageInfo> diffNodeInfo(NodeInfo copied, NodeInfo modified) {
			List<DeviceSearchMessageInfo> result = new ArrayList<>();

			//HW Property
			if (!copied.getPlatformFamily().equals(modified.getPlatformFamily())) {
				result.add(makeMassageInfo(MessageConstant.PLATFORM_FAMILY_NAME.getMessage(),
						copied.getPlatformFamily(), modified.getPlatformFamily()));
			}
			if (!copied.getSubPlatformFamily().equals(modified.getSubPlatformFamily())) {
				result.add(makeMassageInfo(MessageConstant.SUB_PLATFORM_FAMILY_NAME.getMessage(),
						copied.getSubPlatformFamily(), modified.getSubPlatformFamily()));
			}
			if (!copied.getNodeName().equals(modified.getNodeName())) {
				result.add(makeMassageInfo(MessageConstant.NODE_NAME.getMessage(),
						copied.getNodeName(), modified.getNodeName()));
			}

			//Cloud Property
			if (!copied.getCloudService().equals(modified.getCloudService())) {
				result.add(makeMassageInfo(MessageConstant.CLOUD_SERVICE.getMessage(),
						copied.getCloudService(), modified.getCloudService()));
			}
			if (!copied.getCloudScope().equals(modified.getCloudScope())) {
				result.add(makeMassageInfo(MessageConstant.CLOUD_SCOPE.getMessage(),
						copied.getCloudScope(), modified.getCloudScope()));
			}
			if (!copied.getCloudResourceName().equals(modified.getCloudResourceName())) {
				result.add(makeMassageInfo(MessageConstant.CLOUD_RESOURCE_NAME.getMessage(),
						copied.getCloudResourceName(), modified.getCloudResourceName()));
			}
			if (!copied.getCloudResourceType().equals(modified.getCloudResourceType())) {
				result.add(makeMassageInfo(MessageConstant.CLOUD_RESOURCE_TYPE.getMessage(),
						copied.getCloudResourceType(), modified.getCloudResourceType()));
			}
			if (!copied.getCloudResourceId().equals(modified.getCloudResourceId())) {
				result.add(makeMassageInfo(MessageConstant.CLOUD_RESOURCE_ID.getMessage(),
						copied.getCloudResourceId(), modified.getCloudResourceId()));
			}
			if (!copied.getCloudLocation().equals(modified.getCloudLocation())) {
				result.add(makeMassageInfo(MessageConstant.CLOUD_LOCATION.getMessage(),
						copied.getCloudLocation(), modified.getCloudLocation()));
			}

			if (!copied.getValid().equals(modified.getValid())) {
				result.add(makeMassageInfo(CloudMessageConstant.CLOUD_STATUS.getMessage(),
						copied.getValid().toString(), modified.getValid().toString()));
			}

			//network Property
			if (!copied.getIpAddressV4().equals(modified.getIpAddressV4())) {
				result.add(makeMassageInfo(MessageConstant.IP_ADDRESS_V4.getMessage(),
						copied.getIpAddressV4(), modified.getIpAddressV4()));
			}
			if (!copied.getIpAddressVersion().equals(modified.getIpAddressVersion())) {
				result.add(makeMassageInfo(MessageConstant.IP_ADDRESS_VERSION.getMessage(),
						copied.getIpAddressVersion().toString(), modified.getIpAddressVersion().toString()));
			}

			//network device(vnic)
			result.addAll(diffDeviceInfo(
					"vnic",
					MessageConstant.NETWORK_INTERFACE_LIST.getMessage(),
					copied.getNodeNetworkInterfaceInfo(),
					modified.getNodeNetworkInterfaceInfo()
					));
			
			//disk device(vdisk)
			result.addAll(diffDeviceInfo(
					"vdisk",
					MessageConstant.DISK_LIST.getMessage(),
					copied.getNodeDiskInfo(),
					modified.getNodeDiskInfo()
					));
			return result;
		}
		
		/** DeviceInfoを継承しているクラスについて差分をチェックし、差分メッセージを作成し返す*/
		private static List<DeviceSearchMessageInfo> diffDeviceInfo(String deviceType, String deviceItemName, List<? extends NodeDeviceInfo> before, List<? extends NodeDeviceInfo> after) {
			List<DeviceSearchMessageInfo> result = new ArrayList<>();
			
			Map<String, NodeDeviceInfo> afterMap = new HashMap<>();
			for (NodeDeviceInfo nic : after) {
				if (!nic.getDeviceType().equals(deviceType))
					continue;
				afterMap.put(nic.getDeviceName(), nic);
			}
			Map<String, NodeDeviceInfo> beforeMap = new HashMap<>();
			for (NodeDeviceInfo nic : before) {
				if (!nic.getDeviceType().equals(deviceType))
					continue;
				beforeMap.put(nic.getDeviceName(), nic);
			}

			if (!beforeMap.equals(afterMap)) {
				if (beforeMap.size() == 0 && afterMap.size() != 0) {
					//前回ノード情報:なし 今回ノード情報:あり
					result.add(makeMassageInfo(
							deviceItemName,
							MessageConstant.NONEXISTENT.getMessage(), 
							MessageConstant.EXISTENT.getMessage()
							));
				} else if (beforeMap.size() != 0 && afterMap.size() == 0) {
					//前回ノード情報:あり 今回ノード情報:なし
					result.add(makeMassageInfo(
							deviceItemName,
							MessageConstant.EXISTENT.getMessage(),
							MessageConstant.NONEXISTENT.getMessage()
							));
				} else {
					//比較
					for (NodeDeviceInfo vnic : afterMap.values()) {
						if (beforeMap.containsKey(vnic.getDeviceName())) {
							NodeDeviceInfo copiedDevice = beforeMap.get(vnic.getDeviceName());
							if (!copiedDevice.getDeviceDescription().equals(vnic.getDeviceDescription())) {
								result.add(makeMassageInfo(
										deviceItemName + "." + MessageConstant.DESCRIPTION.getMessage(),
										copiedDevice.getDeviceDescription(),
										vnic.getDeviceDescription()));
							}
							if (!copiedDevice.getDeviceDisplayName().equals(vnic.getDeviceDisplayName())) {
								result.add(makeMassageInfo(
										deviceItemName + "." + MessageConstant.DEVICE_DISPLAY_NAME.getMessage(),
										copiedDevice.getDeviceDisplayName(),
										vnic.getDeviceDisplayName()));
							}
							if (!copiedDevice.getDeviceIndex().equals(vnic.getDeviceIndex())) {
								result.add(makeMassageInfo(
										deviceItemName + "." + MessageConstant.DEVICE_INDEX.getMessage(),
										copiedDevice.getDeviceIndex().toString(),
										vnic.getDeviceIndex().toString()));
							}
							if (!copiedDevice.getDeviceName().equals(vnic.getDeviceName())) {
								result.add(makeMassageInfo(
										deviceItemName + "." + MessageConstant.DEVICE_NAME.getMessage(),
										copiedDevice.getDeviceName(),
										vnic.getDeviceName()));
							}
							if (!copiedDevice.getDeviceSize().equals(vnic.getDeviceSize())) {
								result.add(makeMassageInfo(
										deviceItemName + "." + MessageConstant.DEVICE_SIZE.getMessage(),
										copiedDevice.getDeviceSize().toString(),
										vnic.getDeviceSize().toString()));
							}
							if (!copiedDevice.getDeviceSizeUnit().equals(vnic.getDeviceSizeUnit())) {
								result.add(makeMassageInfo(
										deviceItemName + "." + MessageConstant.DEVICE_SIZE_UNIT.getMessage(),
										copiedDevice.getDeviceSizeUnit().toString(),
										vnic.getDeviceSizeUnit().toString()));
							}
							if (!copiedDevice.getDeviceType().equals(vnic.getDeviceType())) {
								result.add(makeMassageInfo(
										deviceItemName + "." + MessageConstant.DEVICE_TYPE.getMessage(),
										copiedDevice.getDeviceType(),
										vnic.getDeviceType()));
							}
						} else {
							result.add(makeMassageInfo(
									deviceItemName + "." + MessageConstant.DEVICE_NAME.getMessage() + "." + vnic.getDeviceIndex(),
									MessageConstant.NONEXISTENT.getMessage(),
									vnic.getDeviceName()));
						}
					}
				}
			}
			return result;
		}

		private static DeviceSearchMessageInfo makeMassageInfo(String item, String lastVal, String thisVal) {
			DeviceSearchMessageInfo msgInfo = new DeviceSearchMessageInfo();
			msgInfo.setItemName(item);
			msgInfo.setLastVal(lastVal);
			msgInfo.setThisVal(thisVal);
			return msgInfo;
		}
	}
}
