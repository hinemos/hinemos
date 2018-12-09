/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.model.cloud;

import java.util.List;

import com.clustercontrol.xcloud.model.base.PropertyId;
import com.clustercontrol.xcloud.model.base.ValueObserver;
import com.clustercontrol.xcloud.model.repository.IInstanceNode;

public interface IInstance extends IResource {
	public interface p {
		static final PropertyId<ValueObserver<String>> facilityId = new PropertyId<ValueObserver<String>>("facilityId"){};
		static final PropertyId<ValueObserver<String>> status = new PropertyId<ValueObserver<String>>("status"){};
		static final PropertyId<ValueObserver<String>> nativeStatus = new PropertyId<ValueObserver<String>>("nativeStatus"){};
		static final PropertyId<ValueObserver<String>> name = new PropertyId<ValueObserver<String>>("name"){};
		static final PropertyId<ValueObserver<String>> platform = new PropertyId<ValueObserver<String>>("platform"){};
		static final PropertyId<ValueObserver<String>> memo = new PropertyId<ValueObserver<String>>("memo"){};
		static final PropertyId<ValueObserver<String>> scopeId = new PropertyId<ValueObserver<String>>("scopeId"){};
		static final PropertyId<ValueObserver<Long>> updateDate = new PropertyId<ValueObserver<Long>>("updateDate"){};
		static final PropertyId<ValueObserver<String>> updateUser = new PropertyId<ValueObserver<String>>("updateUser"){};
		static final PropertyId<ValueObserver<List<String>>> ipAddresses  = new PropertyId<ValueObserver<List<String>>>("ipAddresses"){};
		static final PropertyId<ValueObserver<List<Tag>>> tags = new PropertyId<ValueObserver<List<Tag>>>("tags"){};
		static final PropertyId<ValueObserver<IInstanceBackup>> backup = new PropertyId<ValueObserver<IInstanceBackup>>("backup", true){};
	}

	public static class Tag {
		private String key;
		private String type;
		private String value;
		public String getKey() {return key;}
		public void setKey(String key) {this.key = key;}
		public String getType() {return type;}
		public void setType(String type) {this.type = type;}
		public String getValue() {return value;}
		public void setValue(String value) {this.value = value;}
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((key == null) ? 0 : key.hashCode());
			result = prime * result + ((type == null) ? 0 : type.hashCode());
			result = prime * result + ((value == null) ? 0 : value.hashCode());
			return result;
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Tag other = (Tag) obj;
			if (key == null) {
				if (other.key != null)
					return false;
			} else if (!key.equals(other.key))
				return false;
			if (type == null) {
				if (other.type != null)
					return false;
			} else if (!type.equals(other.type))
				return false;
			if (value == null) {
				if (other.value != null)
					return false;
			} else if (!value.equals(other.value))
				return false;
			return true;
		}
		@Override
		public String toString() {
			return "Tag [key=" + key + ", type=" + type + ", value=" + value
					+ "]";
		}
	}
	
	String getFacilityId();	
	String getName();
	String getId();	
	String getPlatform();
	String getMemo();	
	String getStatus();	
	String getNativeStatus();
	Long getRegDate();	
	String getRegUser();
	Long getUpdateDate();
	String getUpdateUser();
	List<String> getIpAddresses();
	List<Tag> getTags();
	
	IComputeResources getCloudComputeManager();
	
	IInstanceNode[] getCounterNodes();
	
	IInstanceBackup getBackup();
}
