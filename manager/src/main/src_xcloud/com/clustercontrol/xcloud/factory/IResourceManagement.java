/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.factory;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.clustercontrol.repository.model.NodeInfo;
import com.clustercontrol.repository.model.ScopeInfo;
import com.clustercontrol.xcloud.CloudManagerException;
import com.clustercontrol.xcloud.InternalManagerError;
import com.clustercontrol.xcloud.bean.AddInstanceRequest;
import com.clustercontrol.xcloud.bean.AddStorageRequest;
import com.clustercontrol.xcloud.bean.InstanceStatus;
import com.clustercontrol.xcloud.bean.ModifyInstanceRequest;
import com.clustercontrol.xcloud.bean.Option;
import com.clustercontrol.xcloud.bean.Tag;
import com.clustercontrol.xcloud.model.BackupedDataStore;
import com.clustercontrol.xcloud.model.CloudLoginUserEntity;
import com.clustercontrol.xcloud.model.DataType;
import com.clustercontrol.xcloud.model.EntityEntity;
import com.clustercontrol.xcloud.model.InstanceBackupEntryEntity;
import com.clustercontrol.xcloud.model.LocationEntity;
import com.clustercontrol.xcloud.model.StorageBackupEntryEntity;
import com.clustercontrol.xcloud.util.CloudMessage;

public interface IResourceManagement {
	public enum ErrorCode {
		//
		Resource_Fail_to_Modify("XCLOUD_CORE_MSG_RESOURCE_FAIL_TO_MODIFY"),
		
		InstanceSnapshot_Fail_to_Delete("XCLOUD_CORE_MSG_INSTANCESNAPSHOT_FAIL_TO_DELETE"),
		
		InstanceSnapsoht_Fail_to_snapshot("XCLOUD_CORE_MSG_INSTANCESNAPSOHT_FAIL_TO_SNAPSHOT"),
		StorageSnapsoht_Fail_to_snapshot("XCLOUD_CORE_MSG_INSTANCESNAPSOHT_FAIL_TO_SNAPSHOT"),
		//
		
		Resource_InvalidStorage_NotFound("XCLOUD_CORE_MSG_RESOURCE_FAIL_TO_MODIFY"),
		Resource_InvalidInstanceID_NotFound("XCLOUD_CORE_MSG_RESOURCE_INVALIDINSTANCEID_NOTFOUND"),
		Resource_InvalidImageID_NotFound("XCLOUD_CORE_MSG_RESOURCE_INVALIDIMAGEID_NOTFOUND"),
		Resource_InvalidSnapshot_NotFound("XCLOUD_CORE_MSG_RESOURCE_INVALIDSNAPSHOT_NOTFOUND"),
		Resource_Instance_Backup_Image_State_Failed("XCLOUD_CORE_MSG_INSTANCESNAPSOHT_FAIL_TO_SNAPSHOT");

		private String messageId;

		private ErrorCode(String messageId) {
			this.messageId = messageId;
		}

		public String getMessage(String key, Object... args) {
			MessageFormat messageFormat = new MessageFormat(key);
			return messageFormat.format(args);
		}

		public String getMessage() {
			return CloudMessage.getMessage(messageId);
		}

		public CloudManagerException cloudManagerFault(Exception e) {
			return new CloudManagerException(e);
		}
		
		public CloudManagerException cloudManagerFault(Object... args) {
			return new CloudManagerException(getMessage(messageId, args), this.name());
		}
	}

	public interface IStore {
		public static class StoreValue {
			private String resourceType;
			private String resourceId;
			private String value;

			public StoreValue(String resourceType, String resourceId,
					String value) {
				super();
				this.resourceType = resourceType;
				this.resourceId = resourceId;
				this.value = value;
			}
			public String getResourceType() {
				return resourceType;
			}
			public void setResourceType(String resourceType) {
				this.resourceType = resourceType;
			}
			public String getResourceId() {
				return resourceId;
			}
			public void setResourceId(String resourceId) {
				this.resourceId = resourceId;
			}
			public String getValue() {
				return value;
			}
			public void setValue(String value) {
				this.value = value;
			}
		}

		public List<StoreValue> getValues(String resourceType) throws CloudManagerException;
		public String get(String resourceType, String resourceId) throws CloudManagerException;
		public void put(String resourceType, String resourceId, String value) throws CloudManagerException;
		public void remove(String resourceType, String resourceId) throws CloudManagerException;
		public List<String> getIds(String resourceType) throws CloudManagerException;
	}
	
	public static abstract class Element {
		public interface IVisitor {
			void visit(Folder folder) throws CloudManagerException;
			void visit(ResourceHolder holder) throws CloudManagerException;
			void visit(Location folder) throws CloudManagerException;
		}

		public interface ITransformer<T> {
			T transform(Folder folder) throws CloudManagerException;
			T transform(ResourceHolder holder) throws CloudManagerException;
			T transform(Location folder) throws CloudManagerException;
		}

		public static class Visitor implements IVisitor {
			@Override
			public void visit(Folder folder) throws CloudManagerException {
				throw new InternalManagerError();
			}
			@Override
			public void visit(ResourceHolder holder) throws CloudManagerException {
				throw new InternalManagerError();
			}
			@Override
			public void visit(Location folder) throws CloudManagerException {
				throw new InternalManagerError();
			}
		}
		
		public static class Transformer<T> implements ITransformer<T> {
			@Override
			public T transform(Folder folder) throws CloudManagerException {
				throw new InternalManagerError();
			}
			@Override
			public T transform(ResourceHolder holder) throws CloudManagerException {
				throw new InternalManagerError();
			}
			@Override
			public T transform(Location folder) throws CloudManagerException {
				throw new InternalManagerError();
			}
		}
		
		private Element parent;
		private String elementType;
		private String id;
		private String name;
		private List<ExtendedProperty> extendedProeperties = new ArrayList<>();

		public Element() {
		}

		public Element(Element parent, String elementType, String id, String resourceType, String name) {
			this.parent = parent;
			this.elementType = elementType;
			this.id = id;
			this.name = name;
		}

		public Location getLocation() {
			if (parent == null)
				return null;
			return parent.getLocation();
		}
		public Element getParent() {
			return parent;
		}
		public void setParent(Element parent) {
			this.parent = parent;
		}

		public String getElementType() {
			return elementType;
		}
		public void setElementType(String elementType) {
			this.elementType = elementType;
		}

		public String getId() {
			return id;
		}
		public void setId(String id) {
			this.id = id;
		}

		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}

		public List<ExtendedProperty> getExtendedProperties() {
			return extendedProeperties;
		}
		
		public abstract void visit(IVisitor visitor) throws CloudManagerException;

		public abstract <T> T transform(ITransformer<T> tranformer) throws CloudManagerException;
	}
	
	public static class Folder extends Element {
		private List<Element> elements = new ArrayList<>();

		public Folder() {
			super();
		}
		
		public List<Element> getElements() {
			return Collections.unmodifiableList(elements);
		}
		public void addElement(Element element) {
			element.setParent(this);
			this.elements.add(element);
		}
		public void removeElement(Element element) {
			element.setParent(null);
			this.elements.remove(element);
		}

		public boolean canDecorate() {
			return false;
		}

		public void decorate(ScopeInfo scopeInfo) {
		}
		
		@Override
		public void visit(IVisitor visitor) throws CloudManagerException {
			visitor.visit(this);
		}

		@Override
		public <T> T transform(ITransformer<T> transformer) throws CloudManagerException {
			return transformer.transform(this);
		}
	}

	public static class ResourceHolder extends Element {
		private Resource resource;

		public ResourceHolder() {
			super();
		}

		public Resource getResource() {
			return resource;
		}
		public void setResource(Resource resource) {
			this.resource = resource;
			resource.getHolders().add(this);
		}

		@Override
		public void visit(IVisitor visitor) throws CloudManagerException {
			visitor.visit(this);
		}

		@Override
		public <T> T transform(ITransformer<T> transformer) throws CloudManagerException {
			return transformer.transform(this);
		}
	}
	
	public static class Location extends Folder {
		private LocationEntity locationEntity;
		private List<Instance> instances = new ArrayList<>();
		private List<Entity> entities = new ArrayList<>();

		public Location() {
			super();
		}

		public Location getLocation() {
			return this;
		}

		public LocationEntity getLocationEntity() {
			return locationEntity;
		}
		public void setLocationEntity(LocationEntity locationEntity) {
			this.locationEntity = locationEntity;
		}

		public List<Instance> getInstances() {
			return instances;
		}
		public void setInstances(List<Instance> instances) {
			this.instances = instances;
		}

		public List<Entity> getEntities() {
			return entities;
		}
		public void setEntities(List<Entity> entities) {
			this.entities = entities;
		}

		@Override
		public void visit(IVisitor visitor) throws CloudManagerException {
			visitor.visit(this);
		}
		@Override
		public <T> T transform(ITransformer<T> transformer) throws CloudManagerException {
			return transformer.transform(this);
		}
	}
	
	public static class ExtendedProperty {
		private String name;
		private DataType dataType;
		private String value;
		
		public ExtendedProperty() {
		}
		
		public ExtendedProperty(String name, String value) {
			this.name = name;
			this.value = value;
		}

		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public DataType getDataType() {
			return dataType;
		}
		public void setDataType(DataType dataType) {
			this.dataType = dataType;
		}
		public String getValue() {
			return value;
		}
		public void setValue(String value) {
			this.value = value;
		}
	}
	
	public static class FolderRef {
		private String elementType;
		private String id;
		
		public String getElementType() {
			return elementType;
		}
		public void setElementType(String elementType) {
			this.elementType = elementType;
		}
		
		public String getId() {
			return id;
		}
		public void setId(String id) {
			this.id = id;
		}
	}

	public static abstract class Resource {
		public static interface IVisitor {
			void visit(Instance instance) throws CloudManagerException;
			void visit(Storage storage) throws CloudManagerException;
			void visit(InstanceSnapshot instanceBackup) throws CloudManagerException;
			void visit(StorageSnapshot storageBackup) throws CloudManagerException;
			void visit(Entity entity) throws CloudManagerException;
			void visit(Network network) throws CloudManagerException;
		}
		public static interface ITransformer<T> {
			T transform(Instance instance) throws CloudManagerException;
			T transform(Storage storage) throws CloudManagerException;
			T transform(InstanceSnapshot instanceBackup) throws CloudManagerException;
			T transform(StorageSnapshot storageBackup) throws CloudManagerException;
			T transform(Entity entity) throws CloudManagerException;
			T transform(Network network) throws CloudManagerException;
		}
		
		public static class Visitor implements IVisitor {
			@Override
			public void visit(Instance instance) throws CloudManagerException {
				throw new InternalManagerError();
			}
			@Override
			public void visit(Storage storage) throws CloudManagerException {
				throw new InternalManagerError();
			}
			@Override
			public void visit(InstanceSnapshot instanceBackup) throws CloudManagerException {
				throw new InternalManagerError();
			}
			@Override
			public void visit(StorageSnapshot storageBackup) throws CloudManagerException {
				throw new InternalManagerError();
			}
			@Override
			public void visit(Entity entity) throws CloudManagerException {
				throw new InternalManagerError();
			}
			@Override
			public void visit(Network entity) throws CloudManagerException {
				throw new InternalManagerError();
			}
		}

		public static class Transformer<T> implements ITransformer<T> {
			@Override
			public T transform(Instance instance) throws CloudManagerException {
				throw new InternalManagerError();
			}
			@Override
			public T transform(Storage storage) throws CloudManagerException {
				throw new InternalManagerError();
			}
			@Override
			public T transform(InstanceSnapshot instanceBackup) throws CloudManagerException {
				throw new InternalManagerError();
			}
			@Override
			public T transform(StorageSnapshot storageBackup) throws CloudManagerException {
				throw new InternalManagerError();
			}
			@Override
			public T transform(Entity entity) throws CloudManagerException {
				throw new InternalManagerError();
			}
			@Override
			public T transform(Network entity) throws CloudManagerException {
				throw new InternalManagerError();
			}
		}
		
		public static enum ResourceType {
			Instance,
			Storage,
			InstanceSnapshot,
			InstanceBackup,
			Image,
			Snapshot,
			Entity,
			Network
		}
		
		private Location location;
		private List<ResourceHolder> holders = new ArrayList<>();
		private List<FolderRef> folders = new ArrayList<>();
		private ResourceType resourceType;
		private String resourceTypeAsPlatform;
		private String resourceId;
		private String name;
		private List<ExtendedProperty> extendedProeperty = new ArrayList<>();
		
		public Resource(ResourceType resourceType, String resourceTypeAsPlatform, String resourceId, String name) {
			this.resourceType = resourceType;
			this.resourceTypeAsPlatform = resourceTypeAsPlatform;
			this.resourceId = resourceId;
			this.name = name;
		}
		
		public Location getLocation() {
			if (location == null) {
				for (ResourceHolder holder: getHolders()) {
					Location parentLocation = holder.getLocation();
					if (parentLocation != null) {
						return parentLocation;
					}
				}
			}
			return location;
		}
		public void setLocation(Location location) {
			this.location = location;
		}

		public List<ResourceHolder> getHolders() {
			return holders;
		}
		public void setHolders(List<ResourceHolder> holders) {
			this.holders = holders;
		}
		
		public List<FolderRef> getFolders() {
			return folders;
		}
		public void setFolderIdsIds(List<FolderRef> folders) {
			this.folders = folders;
		}
		
		public String getResourceId() {
			return resourceId;
		}
		public void setResourceId(String resourceId) {
			this.resourceId = resourceId;
		}
		
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}

		public ResourceType getResourceType() {
			return resourceType;
		}

		public String getResourceTypeAsPlatform() {
			return resourceTypeAsPlatform;
		}
		public void setResourceTypeAsPlatform(String resourceTypeAsPlatform) {
			this.resourceTypeAsPlatform = resourceTypeAsPlatform;
		}
		
		public List<ExtendedProperty> getExtendedProperty() {
			return extendedProeperty;
		}

		public abstract void visit(IVisitor visitor) throws CloudManagerException;

		public abstract <T> T transform(ITransformer<T> transformer) throws CloudManagerException;
	}
	
	public static class NetworkInterface {
		private String id;
		private String deviceName;
		private String displayName;
		private Integer deviceIndex;
		private String ipAddress;
		private String macAddress;
		
		public String getId() {
			return id;
		}
		public void setId(String id) {
			this.id = id;
		}
		public String getDeviceName() {
			return deviceName;
		}
		public void setDeviceName(String deviceName) {
			this.deviceName = deviceName;
		}
		public String getDisplayName() {
			return displayName;
		}
		public void setDisplayName(String displayName) {
			this.displayName = displayName;
		}
		public String getIpAddress() {
			return ipAddress;
		}
		public void setIpAddress(String ipAddress) {
			this.ipAddress = ipAddress;
		}
		public String getMacAddress() {
			return macAddress;
		}
		public void setMacAddress(String macAddress) {
			this.macAddress = macAddress;
		}
		public Integer getDeviceIndex() {
			return deviceIndex;
		}
		public void setDeviceIndex(Integer deviceIndex) {
			this.deviceIndex = deviceIndex;
		}
	}
	
	public static class Instance extends Resource {
		public static enum Platform {
			windows("WINDOWS"),
			linux("LINUX"),
			other("OTHER"),
			unknown("unknown");

			private final String label;

			private Platform(String label) {
				this.label = label;
			}

			public String label() {
				return label;
			}
		}
		
		private InstanceStatus state;
		private String statusAsPlatform;
		private Platform platform;
		private String hostName;
		private List<String> ipAddresses = new ArrayList<>();
		private String memo;
		private List<String> storageIds = new ArrayList<>();
		private List<NetworkInterface> nis = new ArrayList<>();
		private List<Tag> tags = new ArrayList<>();
		
		public Instance() {
			super(ResourceType.Instance, null, null, null);
		}
		
		public InstanceStatus getInstanceStatus() {
			return state;
		}
		public void setInstanceStatus(InstanceStatus state) {
			this.state = state;
		}
		
		public String getInstanceStatusAsPlatform() {
			return statusAsPlatform;
		}
		public void setInstanceStatusAsPlatform(String statusAsPlatform) {
			this.statusAsPlatform = statusAsPlatform;
		}
		
		public Platform getPlatform() {
			return platform;
		}
		public void setPlatform(Platform platform) {
			this.platform = platform;
		}
		
		public List<String> getIpAddresses() {
			return ipAddresses;
		}
		public void setIpAddresses(List<String> ipAddresses) {
			this.ipAddresses = ipAddresses;
		}
		
		public String getHostName() {
			return hostName;
		}
		public void setHostName(String hostName) {
			this.hostName = hostName;
		}

		public List<String> getStorages() {
			return storageIds;
		}
		public void setStorages(List<String> storageIds) {
			this.storageIds = storageIds;
		}
		
		public List<Tag> getTags() {
			return tags;
		}
		public void setTags(List<Tag> tags) {
			this.tags = tags;
		}
		
		@Override
		public void visit(IVisitor visitor) throws CloudManagerException {
			visitor.visit(this);
		}
		
		@Override
		public <T> T transform(ITransformer<T> transformer) throws CloudManagerException {
			return transformer.transform(this);
		}
		
		public boolean canDecorate() {
			return false;
		}
		public void decorate(NodeInfo nodeInfo) {
		}

		public String getMemo() {
			return memo;
		}
		public void setMemo(String memo) {
			this.memo = memo;
		}

		public List<NetworkInterface> getNetworkInterfaces() {
			return nis;
		}
		public void setNetworkInterfaces(List<NetworkInterface> nics) {
			this.nis = nics;
		}
	}
	
	public static class Storage extends Resource {
		public static enum StorageStatus {
			processing,
			available,
			in_use,
			deleted,
			unknown,
			notfound;
		}

		private Integer size;
		private String storageType;
		private StorageStatus storageStatus;
		private String statusAsPlatform;
		
		private String targetInstanceId;
		private String deviceName;

		private List<Tag> tags = new ArrayList<>();

		public Storage() {
			super(ResourceType.Storage, null, null, null);
		}

		public Integer getSize() {
			return size;
		}
		public void setSize(Integer size) {
			this.size = size;
		}

		public StorageStatus getStorageStatus() {
			return storageStatus;
		}
		public void setStorageStatus(StorageStatus storageStatus) {
			this.storageStatus = storageStatus;
		}

		public String getStatusAsPlatform() {
			return statusAsPlatform;
		}
		public void setStatusAsPlatform(String statusAsPlatform) {
			this.statusAsPlatform = statusAsPlatform;
		}
		
		@Override
		public void visit(IVisitor visitor) throws CloudManagerException {
			visitor.visit(this);
		}
		
		@Override
		public <T> T transform(ITransformer<T> transformer) throws CloudManagerException {
			return transformer.transform(this);
		}
		
		public List<Tag> getTags() {
			return tags;
		}
		public void setTags(List<Tag> tags) {
			this.tags = tags;
		}

		public String getStorageType() {
			return storageType;
		}
		public void setStorageType(String storageType) {
			this.storageType = storageType;
		}

		public String getTargetInstanceId() {
			return targetInstanceId;
		}

		public void setTargetInstanceId(String targetInstanceId) {
			this.targetInstanceId = targetInstanceId;
		}

		public String getDeviceName() {
			return deviceName;
		}

		public void setDeviceName(String deviceName) {
			this.deviceName = deviceName;
		}
	}

	public static class StorageSnapshot {
		public static enum StorageSnapshotStatusType {
			completed("completed"),
			pending("pending"),
			unknown("unknown"),
			missing("missing");

			private final String label;

			private StorageSnapshotStatusType(String label) {
				this.label = label;
			}

			public String label() {
				return label;
			}

			public static StorageSnapshotStatusType byLabel(String label) {
				String name = label.replace('-', '_');
				return valueOf(name);
			}
		}
		
		private String description;
		private StorageSnapshotStatusType state;
		private String statusAsPlatform;
		private Long createTime;
		private String resourceId;
		private String name;
		
		public StorageSnapshot() {
		}

		public String getResourceId() {
			return resourceId;
		}
		public void setResourceId(String resourceId) {
			this.resourceId = resourceId;
		}
		
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		
		public String getDescription() {
			return description;
		}
		public void setDescription(String description) {
			this.description = description;
		}
		
		public StorageSnapshotStatusType getState() {
			return state;
		}
		public void setStatus(StorageSnapshotStatusType state) {
			this.state = state;
		}
		
		public String getStatusAsPlatform() {
			return statusAsPlatform;
		}
		public void setStatusAsPlatform(String statusAsPlatform) {
			this.statusAsPlatform = statusAsPlatform;
		}
		
		public Long getCreateTime() {
			return createTime;
		}
		public void setCreateTime(Long createTime) {
			this.createTime = createTime;
		}
	}

	public static class InstanceSnapshot {
		public static enum InstanceSnapshotStatusType {
			completed("completed"),
			pending("pending"),
			unknown("unknown"),
			missing("missing");

			private final String label;

			private InstanceSnapshotStatusType(String label) {
				this.label = label;
			}

			public String label() {
				return label;
			}

			public static InstanceSnapshotStatusType byLabel(String label) {
				String name = label.replace('-', '_');
				return valueOf(name);
			}
		}

		private String description;
		private InstanceSnapshotStatusType state;
		private String statusAsPlatform;
		private Long createTime;
		private String resourceId;
		private String name;

		public InstanceSnapshot() {
		}

		public String getResourceId() {
			return resourceId;
		}
		public void setResourceId(String resourceId) {
			this.resourceId = resourceId;
		}
		
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		
		public String getDescription() {
			return description;
		}
		public void setDescription(String description) {
			this.description = description;
		}
		
		public InstanceSnapshotStatusType getState() {
			return state;
		}
		public void setStatus(InstanceSnapshotStatusType state) {
			this.state = state;
		}

		public String getStatusAsPlatform() {
			return statusAsPlatform;
		}
		public void setStatusAsPlatform(String statusAsPlatform) {
			this.statusAsPlatform = statusAsPlatform;
		}

		public Long getCreateTime() {
			return createTime;
		}
		public void setCreateTime(Long createTime) {
			this.createTime = createTime;
		}
	}
	
	public static class Entity extends Resource {
		private List<Tag> tags = new ArrayList<>();
		private String memo;

		public Entity() {
			super(ResourceType.Entity, null, null, null);
		}
		
		@Override
		public void visit(IVisitor visitor) throws CloudManagerException {
			visitor.visit(this);
		}

		@Override
		public <T> T transform(ITransformer<T> transformer) throws CloudManagerException {
			return transformer.transform(this);
		}
		public boolean canDecorate() {
			return false;
		}
		public void decorate(EntityEntity entity, NodeInfo nodeInfoInfo) {
		}
		
		public List<Tag> getTags() {
			return tags;
		}
		public void setTags(List<Tag> tags) {
			this.tags = tags;
		}

		public String getMemo() {
			return memo;
		}
		public void setMemo(String memo) {
			this.memo = memo;
		}
	}
	
	public static class Network extends Resource {
		protected List<String> attachedInstanceIds = new ArrayList<>();
		
		public Network() {
			super(ResourceType.Network, null, null, null);
		}

		public List<String> getAttachedInstanceIds() {
			return attachedInstanceIds;
		}
		public void setAttachedInstanceIds(List<String> attachedInstanceIds) {
			this.attachedInstanceIds = attachedInstanceIds;
		}
		
		@Override
		public void visit(IVisitor visitor) throws CloudManagerException {
			visitor.visit(this);
		}

		@Override
		public <T> T transform(ITransformer<T> visitor) throws CloudManagerException {
			return null;
		}
	}
	
	void setAccessDestination(LocationEntity location, CloudLoginUserEntity cloudLoginUser);

	CloudLoginUserEntity getCloudLoginUser();
	LocationEntity getLocation();

	void disconnect();

	Location getResourceHierarchy() throws CloudManagerException;

	public Instance addInstance(AddInstanceRequest request) throws CloudManagerException;
	void removeInstances(List<String> instanceIds) throws CloudManagerException;
	List<Instance> getInstances(List<String> instanceIds) throws CloudManagerException;
	Instance modifyInstance(ModifyInstanceRequest request) throws CloudManagerException;
	void powerOnInstances(List<String> instanceIds) throws CloudManagerException;
	void powerOffInstances(List<String> instanceIds) throws CloudManagerException;
	void suspendInstances(List<String> instanceIds) throws CloudManagerException;
	void rebootInstances(List<String> instanceIds) throws CloudManagerException;

	// インスタンスバックアップ
	InstanceSnapshot takeInstanceSnapshot(String instanceId, String name, String description, BackupedDataStore backup, List<Option> options) throws CloudManagerException;
	void deleteInstanceSnapshots(List<InstanceBackupEntryEntity> entries) throws CloudManagerException;
	List<InstanceSnapshot> getInstanceSnapshots(List<InstanceBackupEntryEntity> entries) throws CloudManagerException;
	Instance cloneBackupedInstance(InstanceBackupEntryEntity entry, Map<String, String> backupedData, List<Option> options) throws CloudManagerException;

	// ストレージ関連
	Storage addStorage(AddStorageRequest request) throws CloudManagerException;
	void attachStorage(String instanceId, String storageId, List<Option> options) throws CloudManagerException;
	void detachStorage(String instanceId, String storageId) throws CloudManagerException;
	void removeStorages(List<String> storageIds) throws CloudManagerException;
	List<Storage> getStorages(List<String> storageIds) throws CloudManagerException;

	// ストレージバックアップ
	StorageSnapshot takeStorageSnapshot(String storageId, String name, String description, BackupedDataStore backup, List<Option> options) throws CloudManagerException;
	void deleteStorageSnapshots(List<StorageBackupEntryEntity> entries) throws CloudManagerException;
	List<StorageSnapshot> getStorageSnapshots(List<StorageBackupEntryEntity> entries) throws CloudManagerException;
	Storage cloneBackupedStorage(StorageBackupEntryEntity entry, Map<String, String> backupedData, List<Option> options) throws CloudManagerException;
	
	List<Network> getNetworks() throws CloudManagerException;
}
