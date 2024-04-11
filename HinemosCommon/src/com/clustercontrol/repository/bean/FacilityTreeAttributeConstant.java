/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.repository.bean;

import java.util.HashSet;
import java.util.Set;

/**
 * スコープ情報の属性名定義クラス<BR>
 *
 * @version 3.1.0
 * @since 1.0.0
 */
public class FacilityTreeAttributeConstant {
	public static final String FACILITYID = "ccFacilityId";
	public static final String TREETYPE = "ccTreeType";
	public static final String BUILTIN = "ccFacilityBuiltIn";
	public static final String SORT_VALUE="ccFacilitySortValue";

	public static final String REGISTERED_SCOPE="REGISTERED";
	public static final String REGISTERED_SCOPE_TEXT="Registered_Nodes";
	public static final String UNREGISTERED_SCOPE="UNREGISTERED";
	public static final String UNREGISTERED_SCOPE_TEXT="Unregistered_Node";

	public static final String INTERNAL_SCOPE="INTERNAL";
	public static final String INTERNAL_SCOPE_TEXT="Hinemos_Internal";

	public static final String OWNER_SCOPE="OWNER";
	public static final String OWNER_ADMIN_SCOPE="ADMINISTRATORS";
	public static final String OWNER_ALL_SCOPE="ALL_USERS";	
	
	/**
	 * OSスコープ
	 */
	public static final String OS_PARENT_SCOPE="OS";
	public static final String OS_LINUX_SCOPE="LINUX";
	public static final String OS_WINDOWS_SCOPE="WINDOWS";
	public static final String OS_ANDROID_SCOPE="ANDROID";
	public static final String OS_NW_SCOPE="NW_EQUIPMENT";
	public static final String OS_OTHER_SCOPE="OTHER";
	
	public static final String AWS_AWS="AWS"; 
	public static final String AWS_ELB="ELB"; 
	public static final String AWS_RDS="RDS"; 
	public static final String AWS_ALB="ALB"; 
	public static final String AWS_NLB="NLB"; 
	
	public static final String AZURE_AZURE="AZURE";
	public static final String AZURE_VMSS="VMSS";
	public static final String AZURE_WEBAPP="WEBAPP";
	public static final String AZURE_FUNCTIONAPP="FUNCTIONAPP";
	public static final String AZURE_SQLDB="SQLDB";

	public static final String HYPERV="Hyper-V";
	public static final String VMWARE_VSPHERE = "VMware vSphere";
	public static final String VMWARE_VCENTER = "vCenter";
	public static final String VMWARE_ESXI = "ESXi";

	public static final String NODE_CONFIGURATION_SCOPE="NODE_CONFIGURATION";

	public static final String VM_SCOPE="VM";
	public static final String VM_SCOPE_TEXT="Vm_Node";
	public static final String UNALLOCATED_SCOPE="UNALLOCATED";
	public static final String UNALLOCATED_SCOPE_TEXT="Unallocated_Node";

	/**
	 * RPAスコープ
	 */
	public static final String RPA = "_RPA";
	public static final String RPA_NO_MGR_WINACTOR = "_RPA_NO_MGR_WINACTOR"; 
	public static final String RPA_NO_MGR_UIPATH = "_RPA_NO_MGR_UIPATH"; 
	
	/**
	 * クラウドスコープ
	 */
	public final static String PUBLIC_ROOT_ID = "_PUBLIC_CLOUD";
	public final static String PRIVATE_ROOT_ID = "_PRIVATE_CLOUD";
	
	public static final String VALID="ccValid";
	
	private static Set<String> buildinScopeFacilityIdSet;
	
	static {
		buildinScopeFacilityIdSet = new HashSet<String>();
		buildinScopeFacilityIdSet.add(REGISTERED_SCOPE);
		buildinScopeFacilityIdSet.add(UNREGISTERED_SCOPE);
		buildinScopeFacilityIdSet.add(INTERNAL_SCOPE);
		buildinScopeFacilityIdSet.add(VM_SCOPE);
		buildinScopeFacilityIdSet.add(UNALLOCATED_SCOPE);
		buildinScopeFacilityIdSet.add(OS_PARENT_SCOPE);
		buildinScopeFacilityIdSet.add(NODE_CONFIGURATION_SCOPE);
		buildinScopeFacilityIdSet.add(OWNER_SCOPE);
		buildinScopeFacilityIdSet.add(OWNER_ADMIN_SCOPE);
		buildinScopeFacilityIdSet.add(OWNER_ALL_SCOPE);
		buildinScopeFacilityIdSet.add(OS_LINUX_SCOPE);
		buildinScopeFacilityIdSet.add(OS_WINDOWS_SCOPE);
		buildinScopeFacilityIdSet.add(OS_ANDROID_SCOPE);
		buildinScopeFacilityIdSet.add(OS_NW_SCOPE);
		buildinScopeFacilityIdSet.add(OS_OTHER_SCOPE);
		buildinScopeFacilityIdSet.add(RPA);
		buildinScopeFacilityIdSet.add(RPA_NO_MGR_WINACTOR);
		buildinScopeFacilityIdSet.add(RPA_NO_MGR_UIPATH);
		buildinScopeFacilityIdSet.add(PUBLIC_ROOT_ID);
		buildinScopeFacilityIdSet.add(PRIVATE_ROOT_ID);
		buildinScopeFacilityIdSet.add(AWS_AWS);
		buildinScopeFacilityIdSet.add(AWS_ELB);
		buildinScopeFacilityIdSet.add(AWS_RDS);
		buildinScopeFacilityIdSet.add(AWS_ALB);
		buildinScopeFacilityIdSet.add(AWS_NLB);
		buildinScopeFacilityIdSet.add(AZURE_AZURE);
		buildinScopeFacilityIdSet.add(AZURE_VMSS);
		buildinScopeFacilityIdSet.add(AZURE_WEBAPP);
		buildinScopeFacilityIdSet.add(AZURE_FUNCTIONAPP);
		buildinScopeFacilityIdSet.add(AZURE_SQLDB);
		buildinScopeFacilityIdSet.add(HYPERV);
		buildinScopeFacilityIdSet.add(VMWARE_VSPHERE);
		buildinScopeFacilityIdSet.add(VMWARE_VCENTER);
		buildinScopeFacilityIdSet.add(VMWARE_ESXI);
	}
	
	public static Set<String> getBuiltinScopeFacilityIdSet() {
		return buildinScopeFacilityIdSet;
	}
	
	/**
	 * 組み込みスコープかどうかを判定するメソッド<br>
	 * 
	 * @param facilityId ファシリティID
	 * @return 組み込みスコープの場合はtrue, それ以外はfalse
	 */
	public static boolean isBuiltinScope(String facilityId) {
		return buildinScopeFacilityIdSet.contains(facilityId);
	}
}
