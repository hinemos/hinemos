package com.clustercontrol.accesscontrol.util;

import java.util.List;

import javax.xml.ws.WebServiceException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.util.EndpointUnit;
import com.clustercontrol.util.EndpointManager;
import com.clustercontrol.util.EndpointUnit.EndpointSetting;
import com.clustercontrol.ws.access.AccessEndpoint;
import com.clustercontrol.ws.access.AccessEndpointService;
import com.clustercontrol.ws.access.FacilityDuplicate_Exception;
import com.clustercontrol.ws.access.FacilityNotFound_Exception;
import com.clustercontrol.ws.access.HinemosUnknown_Exception;
import com.clustercontrol.ws.access.InvalidRole_Exception;
import com.clustercontrol.ws.access.InvalidSetting_Exception;
import com.clustercontrol.ws.access.InvalidUserPass_Exception;
import com.clustercontrol.ws.access.JobMasterNotFound_Exception;
import com.clustercontrol.ws.access.ObjectPrivilegeFilterInfo;
import com.clustercontrol.ws.access.ObjectPrivilegeInfo;
import com.clustercontrol.ws.access.PrivilegeDuplicate_Exception;
import com.clustercontrol.ws.access.RoleDuplicate_Exception;
import com.clustercontrol.ws.access.RoleInfo;
import com.clustercontrol.ws.access.RoleNotFound_Exception;
import com.clustercontrol.ws.access.SystemPrivilegeInfo;
import com.clustercontrol.ws.access.UnEditableRole_Exception;
import com.clustercontrol.ws.access.UnEditableUser_Exception;
import com.clustercontrol.ws.access.UsedFacility_Exception;
import com.clustercontrol.ws.access.UsedObjectPrivilege_Exception;
import com.clustercontrol.ws.access.UsedOwnerRole_Exception;
import com.clustercontrol.ws.access.UsedRole_Exception;
import com.clustercontrol.ws.access.UsedUser_Exception;
import com.clustercontrol.ws.access.UserDuplicate_Exception;
import com.clustercontrol.ws.access.UserInfo;
import com.clustercontrol.ws.access.UserNotFound_Exception;
import com.clustercontrol.ws.accesscontrol.RoleTreeItem;

/**
 * Hinemosマネージャとの通信をするクラス。
 * HAのような複数マネージャ対応のため、このクラスを実装する。
 *
 * Hinemosマネージャと通信できない場合は、WebServiceExceptionがthrowされる。
 * WebServiceExeptionが出力された場合は、もう一台のマネージャと通信する。
 */
public class AccessEndpointWrapper {

	// ログ
	private static Log m_log = LogFactory.getLog( AccessEndpointWrapper.class );

	private EndpointUnit endpointUnit;

	public AccessEndpointWrapper(EndpointUnit endpointUnit) {
		this.endpointUnit = endpointUnit;
	}

	public static AccessEndpointWrapper getWrapper(String managerName) {
		return new AccessEndpointWrapper(EndpointManager.getActive(managerName));
	}

	public static AccessEndpointWrapper getWrapperLoginCheck(String managerName) {
		return new AccessEndpointWrapper(EndpointManager.get(managerName));
	}

	private static List<EndpointSetting<AccessEndpoint>> getAccessEndpoint(EndpointUnit endpointUnit) {
		return endpointUnit.getEndpoint(AccessEndpointService.class, AccessEndpoint.class);
	}

	public int checkLogin( EndpointUnit endpointUnit ) throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception{
		WebServiceException wse = null;
		for (EndpointSetting<AccessEndpoint> endpointSetting : getAccessEndpoint(endpointUnit)) {
			m_log.info("endpointSetting " + getAccessEndpoint(endpointUnit).size());
			try {
				AccessEndpoint endpoint = endpointSetting.getEndpoint();
				int timezoneOffset = endpoint.checkLogin();
				return timezoneOffset;
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("checkLogin(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}

	public UserInfo getUserInfo( String userId ) throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<AccessEndpoint> endpointSetting : getAccessEndpoint(endpointUnit)) {
			try {
				AccessEndpoint endpoint = endpointSetting.getEndpoint();
				return endpoint.getUserInfo(userId);
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("getUserInfo(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}

	public UserInfo getOwnUserInfo() throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, UserNotFound_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<AccessEndpoint> endpointSetting : getAccessEndpoint(endpointUnit)) {
			try {
				AccessEndpoint endpoint = endpointSetting.getEndpoint();
				return endpoint.getOwnUserInfo();
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("getOwnUserInfo(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}

	public List<UserInfo> getUserInfoList() throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, UserNotFound_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<AccessEndpoint> endpointSetting : getAccessEndpoint(endpointUnit)) {
			try {
				AccessEndpoint endpoint = endpointSetting.getEndpoint();
				return endpoint.getUserInfoList();
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("getUserInfoList(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}

	public void addUserInfo(UserInfo userInfo) throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, UserDuplicate_Exception, InvalidSetting_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<AccessEndpoint> endpointSetting : getAccessEndpoint(endpointUnit)) {
			try {
				AccessEndpoint endpoint = endpointSetting.getEndpoint();
				endpoint.addUserInfo(userInfo);
				return;
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("addUserInfo(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}

	public void modifyUserInfo(UserInfo userInfo) throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, UserNotFound_Exception, InvalidSetting_Exception, UnEditableUser_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<AccessEndpoint> endpointSetting : getAccessEndpoint(endpointUnit)) {
			try {
				AccessEndpoint endpoint = endpointSetting.getEndpoint();
				endpoint.modifyUserInfo(userInfo);
				return;
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("modifyUserInfo(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}

	public void deleteUserInfo(List<String> userIdList) throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, UserNotFound_Exception, UsedUser_Exception, UnEditableUser_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<AccessEndpoint> endpointSetting : getAccessEndpoint(endpointUnit)) {
			try {
				AccessEndpoint endpoint = endpointSetting.getEndpoint();
				endpoint.deleteUserInfo(userIdList);
				return;
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("deleteUserInfo(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}

	public boolean isPermission(SystemPrivilegeInfo systemPrivilegeInfo) throws HinemosUnknown_Exception, InvalidUserPass_Exception, InvalidRole_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<AccessEndpoint> endpointSetting : getAccessEndpoint(endpointUnit)) {
			try {
				AccessEndpoint endpoint = endpointSetting.getEndpoint();
				return endpoint.isPermission(systemPrivilegeInfo);
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("isPermission(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}

	public void changeOwnPassword(String passwordHash) throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, UserNotFound_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<AccessEndpoint> endpointSetting : getAccessEndpoint(endpointUnit)) {
			try {
				AccessEndpoint endpoint = endpointSetting.getEndpoint();
				endpoint.changeOwnPassword(passwordHash);
				return;
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("changeOwnPassword(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}

	public void changePassword(String userId, String passwordHash) throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, UserNotFound_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<AccessEndpoint> endpointSetting : getAccessEndpoint(endpointUnit)) {
			try {
				AccessEndpoint endpoint = endpointSetting.getEndpoint();
				endpoint.changePassword(userId, passwordHash);
				return;
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("changePassword(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}

	public List<RoleInfo> getRoleInfoList() throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, UserNotFound_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<AccessEndpoint> endpointSetting : getAccessEndpoint(endpointUnit)) {
			try {
				AccessEndpoint endpoint = endpointSetting.getEndpoint();
				return endpoint.getRoleInfoList();
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("getRoleInfoList(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}

	public RoleInfo getRoleInfo(String roleId) throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, RoleNotFound_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<AccessEndpoint> endpointSetting : getAccessEndpoint(endpointUnit)) {
			try {
				AccessEndpoint endpoint = endpointSetting.getEndpoint();
				return endpoint.getRoleInfo(roleId);
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("getRoleInfo(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}

	public void addRoleInfo(RoleInfo roleInfo) throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, RoleDuplicate_Exception, FacilityDuplicate_Exception, InvalidSetting_Exception, UnEditableRole_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<AccessEndpoint> endpointSetting : getAccessEndpoint(endpointUnit)) {
			try {
				AccessEndpoint endpoint = endpointSetting.getEndpoint();
				endpoint.addRoleInfo(roleInfo);
				return;
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("addRoleInfo(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}

	public void modifyRoleInfo(RoleInfo roleInfo) throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, RoleNotFound_Exception, FacilityNotFound_Exception, InvalidSetting_Exception, UnEditableRole_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<AccessEndpoint> endpointSetting : getAccessEndpoint(endpointUnit)) {
			try {
				AccessEndpoint endpoint = endpointSetting.getEndpoint();
				endpoint.modifyRoleInfo(roleInfo);
				return;
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("modifyRoleInfo(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}

	public void deleteRoleInfo(List<String> roleIdList) throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, RoleNotFound_Exception, UsedFacility_Exception, FacilityNotFound_Exception, UnEditableRole_Exception, UsedRole_Exception, UsedOwnerRole_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<AccessEndpoint> endpointSetting : getAccessEndpoint(endpointUnit)) {
			try {
				AccessEndpoint endpoint = endpointSetting.getEndpoint();
				endpoint.deleteRoleInfo(roleIdList);
				return;
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("deleteRoleInfo(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}

	public RoleTreeItem getRoleTree()
			throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, UserNotFound_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<AccessEndpoint> endpointSetting : getAccessEndpoint(endpointUnit)) {
			try {
				AccessEndpoint endpoint = endpointSetting.getEndpoint();
				RoleTreeItem top = endpoint.getRoleTree();
				// TreeItemに親を設定する
				RoleTreeItem root = top.getChildren().get(0);
				root.setParent(top);
				List<RoleTreeItem> roleList = root.getChildren();
				for (RoleTreeItem role : roleList) {
					role.setParent(root);
					List<RoleTreeItem> userList = role.getChildren();
					for (RoleTreeItem user : userList) {
						user.setParent(role);
					}
				}
				return top;
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("getJobTree(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}

	public List<String> getOwnerRoleIdList() throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, UserNotFound_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<AccessEndpoint> endpointSetting : getAccessEndpoint(endpointUnit)) {
			try {
				AccessEndpoint endpoint = endpointSetting.getEndpoint();
				return endpoint.getOwnerRoleIdList();
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("getOwnUserInfo(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}

	public void assignUserRole(String roleId, List<String> userIds) throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, UserDuplicate_Exception, InvalidSetting_Exception, UnEditableRole_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<AccessEndpoint> endpointSetting : getAccessEndpoint(endpointUnit)) {
			try {
				AccessEndpoint endpoint = endpointSetting.getEndpoint();
				endpoint.assignUserRole(roleId, userIds);
				return;
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("assignUserRole(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}

	public List<SystemPrivilegeInfo> getSystemPrivilegeInfoListByRoleId(String roleId) throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, UserNotFound_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<AccessEndpoint> endpointSetting : getAccessEndpoint(endpointUnit)) {
			try {
				AccessEndpoint endpoint = endpointSetting.getEndpoint();
				return endpoint.getSystemPrivilegeInfoListByRoleId(roleId);
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("getSystemPrivilegeInfoListByRoleId(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}

	public List<SystemPrivilegeInfo> getSystemPrivilegeInfoListByUserId(String userId) throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, UserNotFound_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<AccessEndpoint> endpointSetting : getAccessEndpoint(endpointUnit)) {
			try {
				AccessEndpoint endpoint = endpointSetting.getEndpoint();
				return endpoint.getSystemPrivilegeInfoListByUserId(userId);
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("getSystemPrivilegeInfoListByUserId(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}

	public List<SystemPrivilegeInfo> getSystemPrivilegeInfoListByEditType(String editType) throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, UserNotFound_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<AccessEndpoint> endpointSetting : getAccessEndpoint(endpointUnit)) {
			try {
				AccessEndpoint endpoint = endpointSetting.getEndpoint();
				return endpoint.getSystemPrivilegeInfoListByEditType(editType);
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("getSystemPrivilegeInfoListByEditType(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}

	public void replaceSystemPrivilegeRole(String roleId, List<SystemPrivilegeInfo> systemPrivileges) throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, UserDuplicate_Exception, InvalidSetting_Exception, UnEditableRole_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<AccessEndpoint> endpointSetting : getAccessEndpoint(endpointUnit)) {
			try {
				AccessEndpoint endpoint = endpointSetting.getEndpoint();
				endpoint.replaceSystemPrivilegeRole(roleId, systemPrivileges);
				return;
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("assignUserRole(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}

	public List<ObjectPrivilegeInfo> getObjectPrivilegeInfoList(ObjectPrivilegeFilterInfo objectPrivilegeFilterInfo) throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<AccessEndpoint> endpointSetting : getAccessEndpoint(endpointUnit)) {
			try {
				AccessEndpoint endpoint = endpointSetting.getEndpoint();
				return endpoint.getObjectPrivilegeInfoList(objectPrivilegeFilterInfo);
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("getObjectPrivilegeInfoList(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}

	public void replaceObjectPrivilegeInfo(String objectType, String objectId, List<ObjectPrivilegeInfo> objectPrivilegeInfoList)
			throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, InvalidSetting_Exception, PrivilegeDuplicate_Exception, UsedObjectPrivilege_Exception, JobMasterNotFound_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<AccessEndpoint> endpointSetting : getAccessEndpoint(endpointUnit)) {
			try {
				AccessEndpoint endpoint = endpointSetting.getEndpoint();
				endpoint.replaceObjectPrivilegeInfo(objectType, objectId, objectPrivilegeInfoList);
				return;
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("replaceObjectPrivilegeInfo(), " + e.getMessage());
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}

}
