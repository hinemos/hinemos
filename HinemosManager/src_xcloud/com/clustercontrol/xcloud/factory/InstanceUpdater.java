/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.factory;

import static com.clustercontrol.xcloud.common.CloudConstants.Event_Instance;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;

import org.apache.log4j.Logger;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.fault.FacilityDuplicate;
import com.clustercontrol.fault.FacilityNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.hinemosagent.util.AgentConnectUtil;
import com.clustercontrol.repository.model.NodeDiskInfo;
import com.clustercontrol.repository.model.NodeInfo;
import com.clustercontrol.repository.model.NodeNetworkInterfaceInfo;
import com.clustercontrol.util.MessageConstant;
import com.clustercontrol.xcloud.CloudManagerException;
import com.clustercontrol.xcloud.InternalManagerError;
import com.clustercontrol.xcloud.PluginException;
import com.clustercontrol.xcloud.Session;
import com.clustercontrol.xcloud.Session.PostCommitAction;
import com.clustercontrol.xcloud.bean.InstanceStatus;
import com.clustercontrol.xcloud.bean.Tag;
import com.clustercontrol.xcloud.bean.TagType;
import com.clustercontrol.xcloud.common.CloudConstants;
import com.clustercontrol.xcloud.common.ErrorCode;
import com.clustercontrol.xcloud.factory.IResourceManagement.Instance;
import com.clustercontrol.xcloud.factory.monitors.InstanceMonitorService;
import com.clustercontrol.xcloud.model.CloudLoginUserEntity;
import com.clustercontrol.xcloud.model.CloudScopeEntity;
import com.clustercontrol.xcloud.model.DataType;
import com.clustercontrol.xcloud.model.ExtendedProperty;
import com.clustercontrol.xcloud.model.FacilityAdditionEntity;
import com.clustercontrol.xcloud.model.InstanceBackupEntity;
import com.clustercontrol.xcloud.model.InstanceBackupEntryEntity;
import com.clustercontrol.xcloud.model.InstanceEntity;
import com.clustercontrol.xcloud.model.LocationEntity;
import com.clustercontrol.xcloud.model.LocationResourceEntity;
import com.clustercontrol.xcloud.persistence.PersistenceUtil;
import com.clustercontrol.xcloud.persistence.PersistenceUtil.TransactionScope;
import com.clustercontrol.xcloud.persistence.TransactionException;
import com.clustercontrol.xcloud.persistence.Transactional;
import com.clustercontrol.xcloud.util.Cidr;
import com.clustercontrol.xcloud.util.CloudMessageUtil;
import com.clustercontrol.xcloud.util.CloudUtil;
import com.clustercontrol.xcloud.util.CollectionComparator;
import com.clustercontrol.xcloud.util.FacilityIdUtil;
import com.clustercontrol.xcloud.util.NodeInfoCache;
import com.clustercontrol.xcloud.util.RepositoryControllerBeanWrapper;

public class InstanceUpdater {
	private static final Logger logger = Logger.getLogger(InstanceUpdater.class);
	
	private interface InstanceUpdatorCallback {
		void onFound(LocationEntity location, CloudLoginUserEntity user, InstanceEntity instance, IResourceManagement.Instance platform);
		void onUpdate(LocationEntity location, CloudLoginUserEntity user, InstanceEntity instance, IResourceManagement.Instance platform);
		void onDelete(LocationEntity location, CloudLoginUserEntity user, InstanceEntity instance);
	}
	
	private static class ReentrantLockPool {
		private static class LockStatus {
			public int referrence = 0;
			public ReentrantLock lock = new ReentrantLock();
		}
		
		private Map<Object, LockStatus> map = new HashMap<>();
		
		public synchronized ReentrantLock acquire(Object key) {
			LockStatus status = map.get(key);
			if (status == null) {
				status = new LockStatus();
				map.put(key, status);
			}
			status.referrence++;
			return status.lock;
		}
		
		public synchronized void release(Object key) {
			LockStatus status = map.get(key);
			if (status != null) {
				status.referrence--;
				if (status.referrence <= 0) {
					map.remove(key);
				}
			}
		}
	}
	
	private static ReentrantLockPool lockPool = new ReentrantLockPool();
	private List<Cidr> ciderList = null;
	
	private boolean nodeAssign = false;
	private boolean removeMissing = false;
	private boolean nodeRegist = HinemosPropertyCommon.xcloud_autoregist_node_instance.getBooleanValue();
	private boolean nodeDelete = HinemosPropertyCommon.xcloud_autodelete_node_instance.getBooleanValue();
	private boolean instanceDelete = HinemosPropertyCommon.xcloud_autodelete_instance.getBooleanValue();
	
	public InstanceUpdater() {
	}
	
	public InstanceUpdater setNodeAssine(boolean nodeAssign) {
		this.nodeAssign = nodeAssign;
		return this;
	}
	
	public InstanceUpdater setNodeRegist(boolean nodeRegist) {
		this.nodeRegist = nodeRegist;
		return this;
	}
	
	public InstanceUpdater setNodeDelete(boolean nodeDelete) {
		this.nodeDelete = nodeDelete;
		return this;
	}
	
	public InstanceUpdater setInstanceDelete(boolean instanceDelete) {
		this.instanceDelete = instanceDelete;
		return this;
	}
	
	public InstanceUpdater setRemoveMissing(boolean removeMissing) {
		this.removeMissing = removeMissing;
		return this;
	}
	
	protected List<Cidr> getCider() {
		if (ciderList == null) {
			try {
				String cidrConfig = HinemosPropertyCommon.xcloud_ipaddress_cidr.getStringValue();
				List<String> cidrStrList = Arrays.asList(cidrConfig.split(","));
				
				List<Cidr> cidrList = new ArrayList<Cidr>();
				for (String cidrStr : cidrStrList) {
					cidrList.add(new Cidr(cidrStr.trim()));
				}
				ciderList = cidrList;
			}
			catch (Exception e) {
				throw new InternalManagerError(e);
			}
		}
		return ciderList;
	}
	
	protected String selectIp(List<String> ips) {
		if (ips.isEmpty())
			return HinemosPropertyCommon.xcloud_ipaddress_notavailable.getStringValue();
		
		List<Cidr> cidrList = getCider();
		for (Cidr cidr : cidrList) {
			for (String ip: ips) {
				if (cidr.matches(ip)) {
					return ip;
				}
			}
		}
		
		return ips.get(0);
	}
	
	public InstanceEntity addNewInstanceWithLock(LocationEntity location, CloudLoginUserEntity user, String instanceName, String memo, List<Tag> tags, IResourceManagement.Instance platformInstance) throws CloudManagerException {
		logger.info("Add new instance "+instanceName+"...");
		
		Object key = Arrays.asList(Session.current().get(CloudScopeEntity.class).getId(), location.getLocationId());
		ReentrantLock lock = lockPool.acquire(key);
		lock.lock();
		try {
			InstanceEntity instanceEntity = convertPlatformInstance(location, instanceName, memo, tags, platformInstance);
			addHinemosNode(location, instanceEntity, platformInstance);
			
			// ストレージ情報を作成。
			IStorages storages = CloudManager.singleton().getStorages(user, location);
			storages.setCachedInstanceEntity(instanceEntity).setDoDiffCheck(false).setCacheResourceManagement(this.cacheRm);
			storages.updateStorages(platformInstance.getStorages());
			
			InstanceMonitorService.getSingleton().startMonitor(
					Session.current().get(CloudScopeEntity.class).getId(),
					location.getLocationId(),
					instanceEntity.getResourceId(),
					Session.current().getContext(),
					InstanceStatus.running, InstanceStatus.terminated, InstanceStatus.stopped);
			
			return instanceEntity;
		} finally {
			lock.unlock();
			lockPool.release(key);
		}
	}
	
	protected InstanceEntity internalAddExistedInstance(LocationEntity location, CloudLoginUserEntity user, IResourceManagement.Instance platformInstance) throws CloudManagerException {
		InstanceEntity instanceEntity = convertPlatformInstance(location,
				platformInstance.getName() == null || platformInstance.getName().isEmpty() ? platformInstance.getResourceId(): platformInstance.getName(),
				null, CloudUtil.emptyList(Tag.class), platformInstance);
		logger.info("Add existed instance "+instanceEntity.getResourceId()+" as "+instanceEntity.getFacilityId()+"..." );
		
		if (nodeRegist)
			addHinemosNode(location, instanceEntity, platformInstance);
		
		// 以下をコメントアウトした理由は、上記で追加したInstanceEntityをEntityManager経由で取得できないため更新が正しく行われないため。
		try {
			IStorages storages = CloudManager.singleton().getStorages(user, location).setCacheResourceManagement(this.cacheRm);
			storages.setCachedInstanceEntity(instanceEntity).setDoDiffCheck(false);
			if (storages.getStorages(platformInstance.getStorages()).size() != platformInstance.getStorages().size())
				storages.updateStorages(platformInstance.getStorages());
		} catch (CloudManagerException e) {
			Logger logger = Logger.getLogger(this.getClass());
			logger.warn(e.getMessage(), e);
		}
		
		return instanceEntity;
	}

	protected InstanceEntity convertPlatformInstance(LocationEntity location, String instanceName, String memo, List<Tag> tags, IResourceManagement.Instance platformInstance) throws CloudManagerException {
		CloudScopeEntity cloudScope = Session.current().get(CloudScopeEntity.class);
		
		// DB に追加する情報を作成。
		InstanceEntity instanceEntity = new InstanceEntity();
		instanceEntity.setCloudScopeId(cloudScope.getId());
		instanceEntity.setLocationId(location.getLocationId());
		instanceEntity.setResourceId(platformInstance.getResourceId());
		// DBの桁数に合わせてカットする
		instanceEntity.setName(CloudUtil.truncateString(instanceName, CloudUtil.INSTANCE_NAME_MAX_BYTE));
		instanceEntity.setInstanceStatus(platformInstance.getInstanceStatus());
		instanceEntity.setCloudScope(cloudScope);
		instanceEntity.setMemo(Session.current().get(ICloudOption.class).getCloudSpec().isInstanceMemoEnabled() ? platformInstance.getMemo(): memo);
		
		instanceEntity.setPlatform(platformInstance.getPlatform());
		instanceEntity.setInstanceStatusAsPlatform(platformInstance.getInstanceStatusAsPlatform());
		instanceEntity.setResourceTypeAsPlatform(platformInstance.getResourceTypeAsPlatform());
		instanceEntity.setIpAddresses(platformInstance.getIpAddresses());
		
		for (IResourceManagement.ExtendedProperty entry: platformInstance.getExtendedProperty()) {
			ExtendedProperty ep = new ExtendedProperty();
			ep.setName(entry.getName());
			ep.setDataType(entry.getDataType());
			ep.setValue(entry.getValue());
			instanceEntity.getExtendedProperties().put(entry.getName(), ep);
		}
		
		for (Tag t: tags) {
			switch (t.getTagType()) {
			case LOCAL:
				com.clustercontrol.xcloud.model.ResourceTag tentity = new com.clustercontrol.xcloud.model.ResourceTag();
				tentity.setTagType(t.getTagType());
				tentity.setKey(t.getKey());
				tentity.setValue(t.getValue());
				instanceEntity.getTags().put(tentity.getKey(), tentity);
				break;
			default:
				break;
			}
		}
		
		for (Tag t: platformInstance.getTags()) {
			com.clustercontrol.xcloud.model.ResourceTag tentity = new com.clustercontrol.xcloud.model.ResourceTag();
			tentity.setTagType(t.getTagType());
			tentity.setKey(t.getKey());
			tentity.setValue(t.getValue());
			instanceEntity.getTags().put(tentity.getKey(), tentity);
		}
		
		InstanceBackupEntity backup = new InstanceBackupEntity();
		backup.setCloudScopeId(instanceEntity.getCloudScopeId());
		backup.setLocationId(instanceEntity.getLocationId());
		backup.setInstanceId(instanceEntity.getResourceId());
		instanceEntity.setBackup(backup);
		
		// 非同期で動作するインスタンス作成や別の更新処理が動作していない場合に、以下の処理を継続する。
		// AWS インスタンスに対応するインスタンス情報を追加。
		try {
			HinemosEntityManager em = Session.current().getEntityManager();
			PersistenceUtil.persist(em,instanceEntity);
			PersistenceUtil.persist(em, instanceEntity.getBackup());
		} catch (EntityExistsException e) {
			// ユーザーが実施するインスタンス作成および自動検知によるインスタンス作成の同期をとり、
			// インスタンス ID を自動採番しているので、この例外が起きた場合は不具合
			throw new InternalManagerError(e.getMessage(), e);
		}
		
		return instanceEntity;
	}
	
	public InstanceEntity addExistedInstanceWithLock(LocationEntity location, CloudLoginUserEntity user, IResourceManagement.Instance platformInstance) throws CloudManagerException {
		Object key = Arrays.asList(Session.current().get(CloudScopeEntity.class).getId(), location.getLocationId());
		ReentrantLock lock = lockPool.acquire(key);
		try {
			lock.lock();
			return internalAddExistedInstance(location, user, platformInstance);
		} finally {
			lock.unlock();
			lockPool.release(key);
		}
	}
	
	public InstanceEntity addExistedInstanceWithTryLock(LocationEntity location, CloudLoginUserEntity user, IResourceManagement.Instance platformInstance) throws CloudManagerException {
		Object key = Arrays.asList(Session.current().get(CloudScopeEntity.class).getId(), location.getLocationId());
		ReentrantLock lock = lockPool.acquire(key);
		try {
			if (lock.tryLock()) {
				try {
					return internalAddExistedInstance(location, user, platformInstance);
				} finally {
					lock.unlock();
				}
			} else {
				// ロックの取得に失敗。
				throw ErrorCode.CLOUDINSTANCE_NOT_ACQUIRE_LOCK.cloudManagerFault(platformInstance.getResourceId());
			}
		} finally {
			lockPool.release(key);
		}
	}
	
	protected void updateNetworkInterfaces(final NodeInfo nodeInfo, List<IResourceManagement.NetworkInterface> nis) throws CloudManagerException {
		if (nodeInfo.getNodeNetworkInterfaceInfo() == null)
			nodeInfo.setNodeNetworkInterfaceInfo(new ArrayList<NodeNetworkInterfaceInfo>());
		
		CollectionComparator.compare(nodeInfo.getNodeNetworkInterfaceInfo(), nis, new CollectionComparator.Comparator<NodeNetworkInterfaceInfo, IResourceManagement.NetworkInterface>() {
			@Override
			public boolean match(NodeNetworkInterfaceInfo o1, IResourceManagement.NetworkInterface o2) throws CloudManagerException {
				return "vnic".equals(o1.getDeviceType()) && o2.getId().equals(o1.getDeviceName());
			}
			@Override
			public void matched(NodeNetworkInterfaceInfo o1, IResourceManagement.NetworkInterface o2) throws CloudManagerException {
				o1.setDeviceName(o2.getId());
				
				if (o2.getDeviceIndex() == null) {
					Set<Integer> indexs = new HashSet<Integer>();
					for (NodeDiskInfo diskInfo: nodeInfo.getNodeDiskInfo()) {
						indexs.add(diskInfo.getDeviceIndex());
					}
					
					for (int i = 0; i < Integer.MAX_VALUE; ++i) {
						if (!indexs.contains(i)) {
							o1.setDeviceIndex(i);
							break;
						}
					}
				} else {
					o1.setDeviceIndex(o2.getDeviceIndex());
				}
				
				o1.setNicIpAddress(o2.getIpAddress() == null ? "": o2.getIpAddress());
				o1.setNicMacAddress(o2.getMacAddress() == null ? "": o2.getMacAddress());
			}
			
			@Override
			public void afterO1(NodeNetworkInterfaceInfo o1) throws CloudManagerException {
				if (o1.getDeviceType().equals("vnic")){
					nodeInfo.getNodeNetworkInterfaceInfo().remove(o1);
					
					logger.info(String.format("Remove NIC. FacilityID=%s,deviceName=%s", nodeInfo.getFacilityId(), o1.getDeviceName()));
				}
			}
			
			@Override
			public void afterO2(IResourceManagement.NetworkInterface o2) throws CloudManagerException {
				NodeNetworkInterfaceInfo ni = new NodeNetworkInterfaceInfo();
				ni.setDeviceType("vnic");
				ni.setDeviceName(o2.getId());
				ni.setDeviceDisplayName(o2.getDisplayName() == null || o2.getDisplayName().isEmpty() ? o2.getDeviceName(): o2.getDisplayName());
				
				if (o2.getDeviceIndex() == null) {
					Set<Integer> indexs = new HashSet<Integer>();
					for (NodeDiskInfo diskInfo: nodeInfo.getNodeDiskInfo()) {
						indexs.add(diskInfo.getDeviceIndex());
					}
					
					for (int i = 0; i < Integer.MAX_VALUE; ++i) {
						if (!indexs.contains(i)) {
							ni.setDeviceIndex(i);
							break;
						}
					}
				} else {
					ni.setDeviceIndex(o2.getDeviceIndex());
				}
				
				ni.setDeviceSize(0L);
				ni.setDeviceSizeUnit("");
				
				ni.setNicIpAddress(o2.getIpAddress() == null ? "": o2.getIpAddress());
				ni.setNicMacAddress(o2.getMacAddress() == null ? "": o2.getMacAddress());
				
				ni.setDeviceDescription("");
				nodeInfo.getNodeNetworkInterfaceInfo().add(ni);
				
				logger.info(String.format("Add NIC. FacilityID=%s,deviceName=%s", nodeInfo.getFacilityId(), ni.getDeviceName()));
			}
		});
	}
	
	private static class CallbackRegistAgent implements PostCommitAction {
		private String facilityId;
		
		public CallbackRegistAgent(String facilityId) {
			this.facilityId = facilityId;
		}
		
		@Override
		public void postCommit() throws TransactionException {
			// Try to fish agent
			if (HinemosPropertyCommon.xcloud_agent_connection_enable.getBooleanValue())
				AgentRegister.asyncRegistAgent(facilityId);
		}
	}

	/**
	 * インスタンスに対応したノードを登録します。
	 * 
	 * @param location
	 * @param instanceEntity インスタンスの情報。ノード登録に成功した場合はファシリティIDが設定されます。
	 * @param platformInstance
	 * @return 登録したノードの情報。 以下の想定済みの理由でノード登録できなかった場合は例外を投げずに null を返します。
	 *         <ul>
	 *           <li>自動登録時のマルチテナント制御によるIPアドレス制限
	 *         </ul>
	 * @throws CloudManagerException
	 */
	protected NodeInfo addHinemosNode(LocationEntity location, InstanceEntity instanceEntity, IResourceManagement.Instance platformInstance) throws CloudManagerException {
		CloudScopeEntity cloudScope = Session.current().get(CloudScopeEntity.class);
		
		NodeInfo nodeInfo = CloudUtil.createNodeInfo(
				FacilityIdUtil.getResourceId(Session.current().get(CloudScopeEntity.class).getPlatformId(), Session.current().get(CloudScopeEntity.class).getCloudScopeId(), instanceEntity.getResourceId()),
				instanceEntity.getName() == null || instanceEntity.getName().isEmpty() ? instanceEntity.getResourceId(): instanceEntity.getName(),
				instanceEntity.getPlatform() == Instance.Platform.unknown ? Instance.Platform.other.label() : instanceEntity.getPlatform().label(),
				cloudScope.getPlatformId(),
				platformInstance.getHostName() != null ? platformInstance.getHostName(): instanceEntity.getResourceId(),
				ActionMode.isAutoDetection() ? "Hinemos Auto Registered": "",
				cloudScope.getPlatformId(),
				cloudScope.getId(),
				platformInstance.getName(),
				platformInstance.getResourceTypeAsPlatform(),
				instanceEntity.getResourceId(),
				location.getLocationId(),
				cloudScope.getOwnerRoleId());
		
		if (HinemosPropertyCommon.xcloud_node_property_cloud_service_update.getBooleanValue()) {
			nodeInfo.setValid(InstanceStatus.running == instanceEntity.getInstanceStatus());
		} else {
			nodeInfo.setValid(false);
		}
		nodeInfo.setIpAddressV4(selectIp(instanceEntity.getIpAddresses()));
		
		updateNetworkInterfaces(nodeInfo, platformInstance.getNetworkInterfaces());
		
		if (platformInstance.canDecorate())
			platformInstance.decorate(nodeInfo);
		
		try (AddedEventNotifier<NodeInfo> notifier = new AddedEventNotifier<>(NodeInfo.class, CloudConstants.Node_Instance, nodeInfo)) {
			try {
				RepositoryControllerBeanWrapper.bean().addNode(nodeInfo);
				Session.current().getEntityManager().flush();
				
				logger.info(String.format("Add node. FacilityID=%s, InstanceId=%s, autoRegist=%b", nodeInfo.getFacilityId(),
						instanceEntity.getResourceId(), ActionMode.isAutoDetection()));
				
				FacilityAdditionEntity fa = new FacilityAdditionEntity();
				fa.setFacilityId(nodeInfo.getFacilityId());
				fa.setCloudScopeId(cloudScope.getCloudScopeId());
				fa.setType(instanceEntity.getResourceType());
				ExtendedProperty ep = new ExtendedProperty();
				ep.setName(CloudConstants.EPROP_Instance);
				ep.setDataType(DataType.string);
				ep.setValue(instanceEntity.getResourceId());
				fa.getExtendedProperties().put(CloudConstants.EPROP_Instance, ep);
				PersistenceUtil.persist(Session.current().getEntityManager(), fa);
				
				notifier.setCompleted();
			} catch (FacilityDuplicate e) {
				try {
					// 既に存在するノードに関しては、クラウドサービスに関連付ける。
					NodeInfo nodeInfoFull = RepositoryControllerBeanWrapper.bean().getNodeFull(nodeInfo.getFacilityId());
					RepositoryControllerBeanWrapper.bean().modifyNode(nodeInfoFull);
					Session.current().getEntityManager().flush();
					
					logger.info(String.format("Link node. FacilityID=%s, InstanceId=%s, autoRegist=%b", nodeInfoFull.getFacilityId(),
							instanceEntity.getResourceId(), ActionMode.isAutoDetection()));
					
					HinemosEntityManager em = Session.current().getEntityManager();
					FacilityAdditionEntity fa = em.find(FacilityAdditionEntity.class, nodeInfoFull.getFacilityId(), ObjectPrivilegeMode.READ);
					if (fa == null) {
						fa = new FacilityAdditionEntity();
						fa.setFacilityId(nodeInfoFull.getFacilityId());
						PersistenceUtil.persist(Session.current().getEntityManager(),fa);
					}
					
					fa.setCloudScopeId(cloudScope.getCloudScopeId());
					fa.setType(instanceEntity.getResourceType());
					ExtendedProperty ep = new ExtendedProperty();
					ep.setName(CloudConstants.EPROP_Instance);
					ep.setDataType(DataType.string);
					ep.setValue(instanceEntity.getResourceId());
					fa.getExtendedProperties().put(CloudConstants.EPROP_Instance, ep);
					
					notifier.setCompleted();
				} catch (InvalidSetting | InvalidRole | FacilityNotFound | HinemosUnknown e1) {
					logger.warn(String.format("addHinemosNode[FD](%s, %s, %s, %s) %s", cloudScope.getCloudScopeId(), instanceEntity.getResourceId(), instanceEntity.getResourceType(), ActionMode.isAutoDetection(), e.getMessage()));
					if (isContinuableViolation(e1)) return null;
					throw ErrorCode.HINEMOS_MANAGER_ERROR.cloudManagerFault(e1);
				}
			} catch (InvalidSetting | HinemosUnknown e) {
				logger.warn(String.format("addHinemosNode(%s, %s, %s, %s) %s", cloudScope.getCloudScopeId(), instanceEntity.getResourceId(), instanceEntity.getResourceType(), ActionMode.isAutoDetection(), e.getMessage()));
				if (isContinuableViolation(e)) return null;
				throw ErrorCode.HINEMOS_MANAGER_ERROR.cloudManagerFault(e);
			}
		}
		instanceEntity.setFacilityId(nodeInfo.getFacilityId());
		
		Session.current().addPostCommitAction(new CallbackRegistAgent(nodeInfo.getFacilityId()));
		
		return nodeInfo;
	}
	
	/**
	 * ノード登録時に生じた例外が、後続処理継続可能なものかどうかを判定して返します。
	 */
	static boolean isContinuableViolation(Exception e) {
		// 自動登録時であること
		if (!ActionMode.isAutoDetection()) return false;
		// InvalidSettingであること
		if (!(e instanceof InvalidSetting)) return false;
		// メッセージ内容チェック
		if (e.getMessage().startsWith("$[" + MessageConstant.MESSAGE_MULTI_TENANT_IPADDRESS_OUT_OF_BOUNDS.toString())) {
			return true;
		}
		return false;
	}
	
	public InstanceEntity updateInstanceEntity(LocationEntity location, InstanceEntity instanceEntity, IResourceManagement.Instance platformInstance) throws CloudManagerException {
		// 登録済みの情報を更新。
		String instanceName = null;
		if (platformInstance.getName() == null) {
			instanceName = platformInstance.getResourceId();
		} else {
			instanceName = platformInstance.getName();
		}
		// DBの桁数に合わせてカットする
		instanceEntity.setName(CloudUtil.truncateString(instanceName, CloudUtil.INSTANCE_NAME_MAX_BYTE));
		instanceEntity.setInstanceStatus(platformInstance.getInstanceStatus());
		instanceEntity.setInstanceStatusAsPlatform(platformInstance.getInstanceStatusAsPlatform());
		instanceEntity.setIpAddresses(platformInstance.getIpAddresses());
		instanceEntity.setPlatform(platformInstance.getPlatform());
		
		CollectionComparator.compare(platformInstance.getExtendedProperty(), instanceEntity.getExtendedProperties().entrySet(),
				new CollectionComparator.Comparator<IResourceManagement.ExtendedProperty, Map.Entry<String, ExtendedProperty>>() {
					@Override
					public boolean match(IResourceManagement.ExtendedProperty o1, Entry<String, ExtendedProperty> o2) throws CloudManagerException {
						return o1.getName().equals(o2.getKey());
					}
					@Override
					public void matched(IResourceManagement.ExtendedProperty o1, Entry<String, ExtendedProperty> o2) throws CloudManagerException {
						o2.getValue().setDataType(o1.getDataType());
						o2.getValue().setValue(o1.getValue());
					}
					@Override
					public void afterO1(IResourceManagement.ExtendedProperty o1) throws CloudManagerException {
						ExtendedProperty ep = new ExtendedProperty();
						ep.setName(o1.getName());
						ep.setDataType(o1.getDataType());
						ep.setValue(o1.getValue());
						instanceEntity.getExtendedProperties().put(o1.getName(), ep);
					}
					@Override
					public void afterO2(Entry<String, ExtendedProperty> o2) throws CloudManagerException {
						instanceEntity.getExtendedProperties().remove(o2.getKey());
					}
		});
		
		if (Session.current().get(ICloudOption.class).getCloudSpec().isInstanceMemoEnabled())
			instanceEntity.setMemo(platformInstance.getMemo());
		
		// タグは、クラウドとの比較なので、platformInstance のタグには、タグ種別がクラウドか自動しか入っていないという前提。
		CollectionComparator.compare(instanceEntity.getTags().values(), platformInstance.getTags(), new CollectionComparator.Comparator<com.clustercontrol.xcloud.model.ResourceTag, Tag>() {
			public boolean match(com.clustercontrol.xcloud.model.ResourceTag o1, Tag o2) throws CloudManagerException {
				return o1.getTagType() == o2.getTagType() && o1.getKey().equals(o2.getKey());
			}
			public void matched(com.clustercontrol.xcloud.model.ResourceTag o1, Tag o2) throws CloudManagerException {if (o1.getTagType() != TagType.LOCAL) o1.setValue(o2.getValue());}
			public void afterO1(com.clustercontrol.xcloud.model.ResourceTag o1) throws CloudManagerException {
				if (o1.getTagType() == TagType.LOCAL)
					return;
				instanceEntity.getTags().remove(o1.getKey());
			}
			public void afterO2(Tag o2) throws CloudManagerException {
				if (o2.getTagType() == TagType.LOCAL)
					return;
				com.clustercontrol.xcloud.model.ResourceTag tentity = new com.clustercontrol.xcloud.model.ResourceTag();
				tentity.setTagType(o2.getTagType());
				tentity.setKey(o2.getKey());
				tentity.setValue(o2.getValue());
				instanceEntity.getTags().put(tentity.getKey(), tentity);
			}
		});
		
		String targetFacilityId = null;
		String targetFacilityIpAddress = null;
		Integer targetFacilityIpAddressVersion = null;
		if (instanceEntity.getFacilityId() != null) {
			try (NodeInfoCache.NodeInfoCacheScope scope = new NodeInfoCache.NodeInfoCacheScope(NodeInfoCache.SessoinType.RequiredNew)) {
				
				// ノード情報の更新処理
				NodeInfo nodeInfo = NodeInfoCache.getNodeInfo(instanceEntity.getFacilityId());
				StringBuilder changeLog = new StringBuilder();
				
				if (HinemosPropertyCommon.xcloud_node_property_platformfamily_update.getBooleanValue()) {
					String oldValue = nodeInfo.getPlatformFamily();
					String newValue = instanceEntity.getPlatform().label();
 					if(newValue != Instance.Platform.unknown.label() &&
 							((null==oldValue && null!=newValue) || (null!=oldValue && !oldValue.equals(newValue)))){
 						nodeInfo.setPlatformFamily(newValue);
						changeLog.append("PlatformFamily:").append(oldValue).append("->").append(newValue).append(";");
					}
				}
				
				String oldSubPlatformFamily = null;
				if (HinemosPropertyCommon.xcloud_node_property_subplatformfamily_update.getBooleanValue()) {
					oldSubPlatformFamily = nodeInfo.getSubPlatformFamily();
					String newValue = instanceEntity.getCloudScope().getPlatformId();
					if((null==oldSubPlatformFamily && null!=newValue) || (null!=oldSubPlatformFamily && !oldSubPlatformFamily.equals(newValue))){
						// この時点では値のセットのみを行い、ログの追加は行わない
						nodeInfo.setSubPlatformFamily(newValue);
					}
				}
				
				if (HinemosPropertyCommon.xcloud_node_property_nodename_update.getBooleanValue()) {
					String oldValue = nodeInfo.getNodeName();
					String newValue = platformInstance.getHostName() != null ? platformInstance.getHostName(): instanceEntity.getResourceId();
					if((null==oldValue && null!=newValue) || (null!=oldValue && !oldValue.equals(newValue))){

						// ノード名更新の要否を判定(クラウド自動検知が優先、または対象ノードの自動デバイスサーチが無効の場合に更新する)
						boolean isPropertyUpdate = false;
						String pirorityName = HinemosPropertyCommon.repository_node_config_nodename_update_priority.getStringValue();
						switch(pirorityName){
						case "device_search":
							isPropertyUpdate = false;
							break;
						case "xcloud_auto_detection":
							isPropertyUpdate = true;
							break;
						default:
							isPropertyUpdate = false;
							logger.info("invalid property value. name = repository.node.config.nodename.update.priority, value = " + pirorityName);
							break;
						}
						
						if (isPropertyUpdate || // クラウド自動検知優先
								HinemosPropertyCommon.repository_device_search_interval.getIntegerValue() <= 0 || // 自動デバイスサーチが無効
								!HinemosPropertyCommon.repository_device_search_prop_basic_network.getBooleanValue() || // 自動デバイスサーチのネットワーク更新が無効
								!nodeInfo.getAutoDeviceSearch()){ // 対象ノードの自動デバイスサーチが無効
							
							nodeInfo.setNodeName(newValue);
							changeLog.append("NodeName:").append(oldValue).append("->").append(newValue).append(";");
						}
					}
				}
				
				if (HinemosPropertyCommon.xcloud_node_property_cloud_service_update.getBooleanValue()) {
					String oldValue = nodeInfo.getCloudService();
					String newValue = instanceEntity.getCloudScope().getPlatformId();
					if((null==oldValue && null!=newValue) || (null!=oldValue && !oldValue.equals(newValue))){
						nodeInfo.setCloudService(newValue);
						changeLog.append("CloudService:").append(oldValue).append("->").append(newValue).append(";");
					}
				}
				if (HinemosPropertyCommon.xcloud_node_property_cloud_scope_update.getBooleanValue()) {
					String oldValue = nodeInfo.getCloudScope();
					String newValue = instanceEntity.getCloudScope().getCloudScopeId();
					if((null==oldValue && null!=newValue) || (null!=oldValue && !oldValue.equals(newValue))){
						nodeInfo.setCloudScope(newValue);
						changeLog.append("CloudScope:").append(oldValue).append("->").append(newValue).append(";");
					}
				}
				if (HinemosPropertyCommon.xcloud_node_property_cloud_resourcename_update.getBooleanValue()) {
					String oldValue = nodeInfo.getCloudResourceName();
					// DBの桁数に合わせてカットする
					String newValue = CloudUtil.truncateString(platformInstance.getName(), com.clustercontrol.repository.util.RepositoryUtil.NODE_CLOUD_RESOURCE_NAME_MAX_BYTE);
					if((null==oldValue && null!=newValue) || (null!=oldValue && !oldValue.equals(newValue))){
						nodeInfo.setCloudResourceName(newValue);
						changeLog.append("CloudResourceName:").append(oldValue).append("->").append(newValue).append(";");
					}
				}
				if (HinemosPropertyCommon.xcloud_node_property_cloud_resourcetype_update.getBooleanValue()) {
					String oldValue = nodeInfo.getCloudResourceType();
					String newValue = instanceEntity.getResourceTypeAsPlatform();
					if((null==oldValue && null!=newValue) || (null!=oldValue && !oldValue.equals(newValue))){
						nodeInfo.setCloudResourceType(newValue);
						changeLog.append("CloudResourceType:").append(oldValue).append("->").append(newValue).append(";");
					}
				}
				if (HinemosPropertyCommon.xcloud_node_property_cloud_resourceid_update.getBooleanValue()) {
					String oldValue = nodeInfo.getCloudResourceId();
					String newValue = instanceEntity.getResourceId();
					if((null==oldValue && null!=newValue) || (null!=oldValue && !oldValue.equals(newValue))){
						nodeInfo.setCloudResourceId(newValue);
						changeLog.append("CloudResourceId:").append(oldValue).append("->").append(newValue).append(";");
					}
				}
				if (HinemosPropertyCommon.xcloud_node_property_cloud_location_update.getBooleanValue()) {
					String oldValue = nodeInfo.getCloudLocation();
					String newValue = instanceEntity.getLocationId();
					if((null==oldValue && null!=newValue) || (null!=oldValue && !oldValue.equals(newValue))){
						nodeInfo.setCloudLocation(newValue);
						changeLog.append("CloudLocation:").append(oldValue).append("->").append(newValue).append(";");
					}
				}
				
				if (HinemosPropertyCommon.xcloud_node_property_cloud_ipaddress_update.getBooleanValue()) {
					String oldValue = nodeInfo.getIpAddressV4();
					String newValue = selectIp(instanceEntity.getIpAddresses());
					if (null==newValue || newValue.equals(HinemosPropertyCommon.xcloud_ipaddress_notavailable.getStringValue())) {
						// この場合はIPアドレスを更新しない
					} else {
						// findbugs対応 不要なnullチェックを排除できる形に比較を修正（ここにくる時点でnewValueはnullではあり得ない）
						if(!newValue.equals(oldValue)){
							nodeInfo.setIpAddressV4(newValue);
							changeLog.append("IpAddressV4:").append(oldValue).append("->").append(newValue).append(";");
						}

						int newIpProto = 4; // Hard-code
						if(newIpProto != nodeInfo.getIpAddressVersion()){
							changeLog.append("IpAddressVersion:").append(nodeInfo.getIpAddressVersion()).append("->").append(newIpProto).append(";");
							nodeInfo.setIpAddressVersion(newIpProto);
						}
					}
				}
				
				if (HinemosPropertyCommon.xcloud_node_property_cloud_validflag_update.getBooleanValue()) {
					boolean isExcluded = false;
					// node_prop_validflag_excludestates("state1,state2,state3"形式)にあった場合は、更新しない
					if(!HinemosPropertyCommon.xcloud_node_property_cloud_validflag_excludestates.getStringValue().isEmpty()){
						String[] excludestates = HinemosPropertyCommon.xcloud_node_property_cloud_validflag_excludestates.getStringValue().split("\\s*,\\s*", 0);
						if(Arrays.asList(excludestates).contains(instanceEntity.getInstanceStatus().label())){
							isExcluded = true;
						}
					}
					if(!isExcluded){
						boolean oldValue = nodeInfo.getValid();
						boolean newValue = InstanceStatus.running == instanceEntity.getInstanceStatus();

						if(oldValue!=newValue){
							nodeInfo.setValid(newValue);
							changeLog.append("ValidFlg:").append(oldValue).append("->").append(newValue).append(";");
						}
					}
				}
				
				if (HinemosPropertyCommon.xcloud_node_property_nic_update.getBooleanValue()) {
					updateNetworkInterfaces(nodeInfo, platformInstance.getNetworkInterfaces());
				}
				
				if (platformInstance.canDecorate())
					platformInstance.decorate(nodeInfo);
				
				// AzureVMSSの場合はdecorate()内にてサブプラットフォームを再設定するため、decorate()後に処理開始前の値と比較する
				if (HinemosPropertyCommon.xcloud_node_property_subplatformfamily_update.getBooleanValue()) {
					String newValue = nodeInfo.getSubPlatformFamily();

					if((null==oldSubPlatformFamily && null!=newValue) || (null!=oldSubPlatformFamily && !oldSubPlatformFamily.equals(newValue))){
						// 値はセット済みなのでログの追加のみ行う
						changeLog.append("SubPlatformFamily:").append(oldSubPlatformFamily).append("->").append(newValue).append(";");
					}
				}

				if (0<changeLog.length()) {
					logger.info(String.format("Update node properties. FacilityID=%s, InstanceId=%s, autoRegist=%b, log=%s", nodeInfo.getFacilityId(),
							instanceEntity.getResourceId(), ActionMode.isAutoDetection(), changeLog.toString()));
				}
				
//				// ストレージ情報を作成。
//				IStorages storages = CloudManager.singleton().getStorages(getUser(), getLocation());
//				storages.updateStorages(platformInstance.getStorages());
				
				scope.modified(instanceEntity.getFacilityId(), new NodeInfoCache.ModifyListener() {
					@Override
					public void modified(NodeInfo info) throws CloudManagerException {
						try {
							CloudManager.singleton().getObjMonitor().firePreModifiedEvent(NodeInfo.class, CloudConstants.Node_Instance, info);
						} catch (CloudManagerException e) {
							throw e;
						} catch (PluginException e) {
							throw new CloudManagerException(e);
						}
					}
				});
				// do
				//対象としない場合は別に探さなくてもよい
				if(nodeInfo.getValid()) {
					targetFacilityId = nodeInfo.getFacilityId();
					targetFacilityIpAddressVersion = nodeInfo.getIpAddressVersion();
					targetFacilityIpAddress = nodeInfo.getAvailableIpAddress();
				}
			} catch (FacilityNotFound e) {
				if (nodeRegist) {
					NodeInfo nodeInfo = addHinemosNode(location, instanceEntity, platformInstance);
					if (nodeInfo != null) {
						targetFacilityId = nodeInfo.getFacilityId();
						targetFacilityIpAddressVersion = nodeInfo.getIpAddressVersion();
						targetFacilityIpAddress = nodeInfo.getAvailableIpAddress();
					}
 				} else {
 					instanceEntity.setFacilityId(null);
 				}
			}
		} else {
			if (nodeAssign){
				NodeInfo nodeInfo = addHinemosNode(location, instanceEntity, platformInstance);
				targetFacilityId = nodeInfo.getFacilityId();
				targetFacilityIpAddressVersion = nodeInfo.getIpAddressVersion();
				targetFacilityIpAddress = nodeInfo.getAvailableIpAddress();
			}
		}
		
		// Try to fish agent
		if(null!=targetFacilityId && HinemosPropertyCommon.xcloud_autoupdate_agent.getBooleanValue()){
			// cidr filter
			boolean isMatch = false;
			String autoupdateAgentCidr = HinemosPropertyCommon.xcloud_autoupdate_agent_cidr.getStringValue();
			if ("0.0.0.0/32".equals(autoupdateAgentCidr)) {
				// 設定値が 0.0.0.0/32 の場合は全通し
				isMatch = true;
			} else if (targetFacilityIpAddressVersion != null && targetFacilityIpAddressVersion.intValue() == 6) {
				// 対象がIPv6の場合は全通し
				isMatch = true;
			} else {
				try {
					List<String> cidrStrList = Arrays.asList(autoupdateAgentCidr.split(","));
					
					for (String cidrStr : cidrStrList) {
						Cidr cidr = new Cidr(cidrStr);
						isMatch = cidr.matches(targetFacilityIpAddress);
						if (isMatch) {
							break;
						}
					}
				} catch (UnknownHostException e) {
					// エラーの場合は警告ログを出力し、cidrによるフィルタは無効(マッチしたこと)にする
					logger.warn(e);
					isMatch = true;
				}
			}
			if(isMatch && !AgentConnectUtil.isValidAgent(targetFacilityId)) {
				try {
					if(AgentConnectUtil.sendManagerDiscoveryInfo(targetFacilityId)){
						logger.info("Found agent. facilityId=" + targetFacilityId);
					}else{
						logger.trace("No agent found. facilityId=" + targetFacilityId);
					}
				} catch (IOException e) {
					logger.error(e);
				}
			}
		}
		
		return instanceEntity;
	}
	
	public List<InstanceEntity> transactionalUpdateInstanceEntities(LocationEntity location, CloudLoginUserEntity user, List<InstanceEntity> instances, List<IResourceManagement.Instance> platformInstances) throws CloudManagerException {
		return  transactionalUpdateInstanceEntities(location, user, instances, platformInstances, new InstanceUpdatorCallback() {
			@Override
			public void onFound(LocationEntity location, CloudLoginUserEntity user, InstanceEntity instance, Instance platform) {
			}
			@Override
			public void onUpdate(LocationEntity location, CloudLoginUserEntity user, InstanceEntity instance, Instance platform) {
			}
			@Override
			public void onDelete(LocationEntity location, CloudLoginUserEntity user, InstanceEntity instance) {
			}
		});
	}
	
	/**
	 * 
	 * @param instances in Hinemos DB
	 * @param platformInstances on AWS
	 * @return
	 * @throws CloudManagerException
	 */
	public List<InstanceEntity> transactionalUpdateInstanceEntities(LocationEntity location, CloudLoginUserEntity user, List<InstanceEntity> instances, List<IResourceManagement.Instance> platformInstances, InstanceUpdatorCallback callback) throws CloudManagerException {
		if(logger.isDebugEnabled()){
			logger.debug(String.format("Transactional update. Compare %d instances with %d platform instances...", instances.size(), instances.size()));
		}
		
		final List<LocationResourceEntity.LocationResourceEntityPK> updateds = new ArrayList<>();
		CollectionComparator.compare(instances, platformInstances, new CollectionComparator.Comparator<InstanceEntity, IResourceManagement.Instance>() {
			@Override
			public boolean match(InstanceEntity o1, IResourceManagement.Instance o2) throws CloudManagerException {
				return o1.getResourceId().equals(o2.getResourceId());
			}
			@Override
			public void matched(InstanceEntity o1, IResourceManagement.Instance o2) throws CloudManagerException {
				try (TransactionScope scope = new TransactionScope(Transactional.TransactionOption.RequiredNew)) {
					HinemosEntityManager em = Session.current().getEntityManager();
					
					//　EntityManager が切り替わっているので、再度取得。
					InstanceEntity instance = em.find(InstanceEntity.class, o1.getId(), ObjectPrivilegeMode.READ);
					logger.debug("Update "+instance.getResourceId());
					
					InstanceEntity updated = updateInstanceEntity(location, instance, o2);
					if (updated != null){
						logger.debug("Add "+instance.getResourceId());
						updateds.add(updated.getId());
					}
					
					scope.complete();
				} catch (CloudManagerException e) {
					CloudMessageUtil.notify_AutoUpadate_Error_InstanceOperator(Session.current().get(CloudScopeEntity.class).getId(), o2.getResourceId(), e);
				} catch (Exception e) {
					CloudMessageUtil.notify_AutoUpadate_Error_InstanceOperator(Session.current().get(CloudScopeEntity.class).getId(), o2.getResourceId(), e);
				}
			}
			@Override
			public void afterO1(InstanceEntity o1) throws CloudManagerException {
				try (TransactionScope scope = new TransactionScope(Transactional.TransactionOption.RequiredNew)) {
					HinemosEntityManager em = Session.current().getEntityManager();
					
					//　EntityManager が切り替わっているので、再度取得。
					InstanceEntity instance = em.find(InstanceEntity.class, o1.getId(), ObjectPrivilegeMode.READ);
					if (instance != null) {
						logger.debug("AfterO1 "+instance.getResourceId());
						
						InstanceEntity updated = disableInstanceEntity(location, user, instance);
						if (updated != null) {
							updateds.add(updated.getId());
						}
					}
					scope.complete();
				} catch (CloudManagerException e) {
					CloudMessageUtil.notify_AutoUpadate_Error_InstanceOperator(Session.current().get(CloudScopeEntity.class).getCloudScopeId(), o1.getResourceId(), e);
				} catch (Exception e) {
					CloudMessageUtil.notify_AutoUpadate_Error_InstanceOperator(Session.current().get(CloudScopeEntity.class).getCloudScopeId(), o1.getResourceId(), e);
				}			}
			@Override
			public void afterO2(IResourceManagement.Instance o2) throws CloudManagerException {
				try (TransactionScope scope = new TransactionScope(Transactional.TransactionOption.RequiredNew)) {
					InstanceEntity updated = addExistedInstanceWithLock(location, user, o2);
					logger.debug("AfterO2 "+updated.getResourceId());
					
					if (updated != null)
						updateds.add(updated.getId());
					scope.complete();
				} catch (CloudManagerException e) {
					CloudMessageUtil.notify_AutoUpadate_Error_InstanceOperator(Session.current().get(CloudScopeEntity.class).getId(), o2.getResourceId(), e);
				} catch (Exception e) {
					CloudMessageUtil.notify_AutoUpadate_Error_InstanceOperator(Session.current().get(CloudScopeEntity.class).getId(), o2.getResourceId(), e);
				}
			}
		});
		
		// メモリ肥大化対応。
		List<InstanceEntity> updatedInstances = new ArrayList<>();
		// Why fetching instances in a new transaction?
		try (TransactionScope scope = new TransactionScope(Transactional.TransactionOption.RequiredNew)) {
			HinemosEntityManager em = Session.current().getEntityManager();
			
			for (LocationResourceEntity.LocationResourceEntityPK pk: updateds) {
				try {
					InstanceEntity instance = em.find(InstanceEntity.class, pk, ObjectPrivilegeMode.READ);
					logger.debug("updatedInstances PK:" + pk.getResourceId()+ "->" + (null==instance? "null": instance.getResourceId()));
					if (instance != null)
						updatedInstances.add(instance);
				} catch(Exception e) {
					CloudMessageUtil.notify_AutoUpadate_Error_InstanceOperator(Session.current().get(CloudScopeEntity.class).getId(), pk.getResourceId(), e);
				}
			}
			scope.complete();
		} catch (Exception e) {
			logger.warn(e.getMessage(), e);
		}
		return updatedInstances;
	}
	
	public InstanceEntity disableInstanceEntity(LocationEntity location, CloudLoginUserEntity user, InstanceEntity instance) throws CloudManagerException {
		if (instanceDelete || (instance.getInstanceStatus().equals(InstanceStatus.missing)) && removeMissing) {
			try (RemovedEventNotifier<InstanceEntity> notifier = new RemovedEventNotifier<>(InstanceEntity.class, Event_Instance, instance)) {
				// ノードと紐づいていないなら、削除。
				HinemosEntityManager em = Session.current().getEntityManager();
				em.remove(instance.getBackup());
				em.remove(instance);
				
				// クラウド側のインスタンスがないので自動削除。
				if (instance.getFacilityId() != null) {
					try {
						if (nodeDelete) {
							logger.info(String.format("Delete node. autoRegist=%b,FacilityID=%s,InstanceId=%s", ActionMode.isAutoDetection(), instance.getFacilityId(), instance.getResourceId()));
							try{
								RepositoryControllerBeanWrapper.bean().deleteNode(new String[]{instance.getFacilityId()});
							}catch(HinemosUnknown e){
								// Check if the node does not exist
								try{
									NodeInfoCache.getNodeInfo(instance.getFacilityId());
									// If it is caused by other problems
									throw e;
								}catch(FacilityNotFound e2){
									// This might caused by manually node delete while auto node registering is off
									logger.warn("NodeNotFound - Clear instance's facilityId. instanceId=" + instance.getId()
											+ ",facilityId=" + instance.getFacilityId());
									instance.setFacilityId(null);
								}
							}
						} else {
							TypedQuery<FacilityAdditionEntity> query = em.createNamedQuery(FacilityAdditionEntity.findParentFacilityAdditionsOfFacility, FacilityAdditionEntity.class);
							query.setParameter("facilityId", instance.getFacilityId());
							
							List<FacilityAdditionEntity> additions;
							try {
								additions = query.getResultList();
							} catch(NoResultException e) {
								return null;
							}
							
							for (FacilityAdditionEntity addition: additions) {
								ExtendedProperty ep = addition.getExtendedProperties().get(CloudConstants.EPROP_CloudScope);
								if (ep == null || !ep.getValue().equals(Session.current().get(CloudScopeEntity.class).getId()))
									continue;
								
								RepositoryControllerBeanWrapper.bean().releaseNodeScope(Session.current().get(CloudScopeEntity.class).getId(), new String[]{instance.getFacilityId()});
							}
						}
					} catch (Exception e) {
						throw ErrorCode.AUTOUPDATE_NOT_DELETE_FACILITY.cloudManagerFault(e, instance.getCloudScopeId(), instance.getLocationId(), instance.getFacilityId(), CloudManagerException.getMessage(e));
					}
				}
				notifier.completed();
				
				if (HinemosPropertyCommon.xcloud_autodelete_node_backupimage.getBooleanValue()) {
					IResourceManagement management = getResourceManagement(location, user);
					// This method is called during instance update and the UnsupportedOperationException(on ESXi only) should be ignored
					try{
						management.deleteInstanceSnapshots(instance.getBackup().getEntries());
					} catch(UnsupportedOperationException e) {}
				} else {
					if (!instance.getBackup().getEntries().isEmpty()) {
						StringBuffer backupIDs = new StringBuffer();
						for (InstanceBackupEntryEntity ent : instance.getBackup().getEntries()) {
							if (backupIDs.length() > 0) {
								backupIDs.append(",");
							}
							backupIDs.append(ent.getEntryId());
						}
						
						logger.info(String.format("%s(ID=%s) was deleted, but %d snapshots are not deleted.ID=%s",
								instance.getName(), instance.getResourceId(), instance.getBackup().getEntries().size(), backupIDs));
					}
				}
			}
		} else {
			if (instance.getInstanceStatus() != InstanceStatus.missing)
				instance.setInstanceStatus(InstanceStatus.missing);
			return instance;
		}
		return null;
	}
	
	public static InstanceUpdater updator() {
		return new InstanceUpdater();
	}
	
	private CacheResourceManagement cacheRm;
	
	protected IResourceManagement getResourceManagement(LocationEntity location, CloudLoginUserEntity user) {
		if (cacheRm == null) {
			ICloudOption option = Session.current().get(ICloudOption.class);
			return option.getResourceManagement(location, user);
		} else {
			return cacheRm;
		}
	}

	public InstanceUpdater setCacheResourceManagement(CacheResourceManagement rm) {
		this.cacheRm = rm;
		return this;
	}
}
