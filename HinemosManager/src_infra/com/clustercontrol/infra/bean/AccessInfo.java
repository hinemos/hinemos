/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.infra.bean;

import javax.xml.bind.annotation.XmlType;

@XmlType(namespace = "http://infra.ws.clustercontrol.com")
public class AccessInfo {
	private String facilityId;
	private String moduleId;
	private String sshUser;
	private String sshPassword;
	private String sshPrivateKeyFilepath;
	private String sshPrivateKeyPassphrase;
	private int sshPort;
	private int sshTimeout;
	private String winRmUser;
	private String winRmPassword;
	private int winRmPort;
	private int winRmTimeout;

	/** モジュールID区切り文字 */
	public final static String MODULEID_DELIMITER = "#";
	
	public String getFacilityId() {
		return facilityId;
	}
	public void setFacilityId(String facilityId) {
		this.facilityId = facilityId;
	}
	public String getModuleId() {
		return moduleId;
	}
	public void setModuleId(String moduleId) {
		this.moduleId = moduleId;
	}
	public String getSshUser() {
		return sshUser;
	}
	public void setSshUser(String sshUser) {
		this.sshUser = sshUser;
	}
	public String getSshPassword() {
		return sshPassword;
	}
	public void setSshPassword(String sshPassword) {
		this.sshPassword = sshPassword;
	}
	public String getSshPrivateKeyFilepath() {
		return sshPrivateKeyFilepath;
	}
	public void setSshPrivateKeyFilepath(String sshPrivateKeyFilepath) {
		this.sshPrivateKeyFilepath = sshPrivateKeyFilepath;
	}
	public String getSshPrivateKeyPassphrase() {
		return sshPrivateKeyPassphrase;
	}
	public void setSshPrivateKeyPassphrase(String sshPrivateKeyPassphrase) {
		this.sshPrivateKeyPassphrase = sshPrivateKeyPassphrase;
	}
	public int getSshPort() {
		return sshPort;
	}
	public void setSshPort(int sshPort) {
		this.sshPort = sshPort;
	}
	public int getSshTimeout() {
		return sshTimeout;
	}
	public void setSshTimeout(int sshTimeout) {
		this.sshTimeout = sshTimeout;
	}
	
	public String getWinRmUser() {
		return winRmUser;
	}
	public void setWinRmUser(String winRmUser) {
		this.winRmUser = winRmUser;
	}
	public String getWinRmPassword() {
		return winRmPassword;
	}
	public void setWinRmPassword(String winRmPassword) {
		this.winRmPassword = winRmPassword;
	}
	public int getWinRmPort() {
		return winRmPort;
	}
	public void setWinRmPort(int winRmPort) {
		this.winRmPort = winRmPort;
	}
	public int getWinRmTimeout() {
		return winRmTimeout;
	}
	public void setWinRmTimeout(int winRmTimeout) {
		this.winRmTimeout = winRmTimeout;
	}
	
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((facilityId == null) ? 0 : facilityId.hashCode());
		result = prime * result
				+ ((moduleId == null) ? 0 : moduleId.hashCode());
		result = prime * result
				+ ((sshUser == null) ? 0 : sshUser.hashCode());
		result = prime * result
				+ ((sshPassword == null) ? 0 : sshPassword.hashCode());
		result = prime * result
				+ ((sshPrivateKeyFilepath == null) ? 0 : sshPrivateKeyFilepath.hashCode());
		result = prime * result
				+ ((sshPrivateKeyPassphrase == null) ? 0 : sshPrivateKeyPassphrase.hashCode());
		result = prime * result
				+ ((winRmUser == null) ? 0 : winRmUser.hashCode());
		result = prime * result
				+ ((winRmPassword == null) ? 0 : winRmPassword.hashCode());
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}

		AccessInfo other = (AccessInfo) obj;
		if (facilityId == null) {
			if (other.facilityId != null) {
				return false;
			}
		} else if (!facilityId.equals(other.facilityId)) {
			return false;
		}
		if (moduleId == null) {
			if (other.moduleId != null) {
				return false;
			}
		} else if (!moduleId.equals(other.moduleId)) {
			return false;
		}
		if (sshUser == null) {
			if (other.sshUser != null) {
				return false;
			}
		} else if (!sshUser.equals(other.sshUser)) {
			return false;
		}
		if (sshPassword == null) {
			if (other.sshPassword != null) {
				return false;
			}
		} else if (!sshPassword.equals(other.sshPassword)) {
			return false;
		}
		if (sshPrivateKeyFilepath == null) {
			if (other.sshPrivateKeyFilepath != null) {
				return false;
			}
		} else if (!sshPrivateKeyFilepath.equals(other.sshPrivateKeyFilepath)) {
			return false;
		}
		if (sshPrivateKeyPassphrase == null) {
			if (other.sshPrivateKeyPassphrase != null) {
				return false;
			}
		} else if (!sshPrivateKeyPassphrase.equals(other.sshPrivateKeyPassphrase)) {
			return false;
		}
		if (sshPort != other.sshPort) {
			return false;
		}
		if (sshTimeout != other.sshTimeout) {
			return false;
		}
		if (winRmUser == null) {
			if (other.winRmUser != null) {
				return false;
			}
		} else if (!winRmUser.equals(other.winRmUser)) {
			return false;
		}
		if (winRmPassword == null) {
			if (other.winRmPassword != null) {
				return false;
			}
		} else if (!winRmPassword.equals(other.winRmPassword)) {
			return false;
		}
		if (winRmPort != other.winRmPort) {
			return false;
		}
		if (winRmTimeout != other.winRmTimeout) {
			return false;
		}
		return true;
	}
	
	@Override
	public String toString() {
		return "AccessInfo ["
				+ "facilityId=" + facilityId +
				", moduleId=" + moduleId +
				", sshUser=" + sshUser +
				", sshPassword=" + sshPassword +
				", sshPrivateKeyFilepath=" + sshPrivateKeyFilepath +
				", sshPrivateKeyPassphrase=" + sshPrivateKeyPassphrase +
				", sshPort=" + sshPort +
				", sshTimeout=" + sshTimeout +
				", winRmUser=" + winRmUser +
				", winRmPassword=" + winRmPassword +
				", winRmPort=" + winRmPort +
				", winRmTimeout=" + winRmTimeout +
				"]";
	}
}
