/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.repository.util;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.function.Supplier;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.RoleIdConstant;
import com.clustercontrol.bean.SnmpVersionConstant;
import com.clustercontrol.commons.util.CommonValidator;
import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.commons.util.InternalIdCommon;
import com.clustercontrol.fault.FacilityNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.maintenance.factory.HinemosPropertyInfoCache;
import com.clustercontrol.repository.model.NodeInfo;
import com.clustercontrol.repository.session.RepositoryControllerBean;
import com.clustercontrol.util.InternalIdAbstract;
import com.clustercontrol.util.Singletons;
import com.clustercontrol.util.SubnetAddress;
import com.clustercontrol.util.apllog.AplLogger;

/**
 * マルチテナント制御機能を提供します。
 */
public class MultiTenantSupport {
	private static final Log log = LogFactory.getLog(MultiTenantSupport.class);

	/** マルチテナント制御が有効か */
	private boolean enabled;
	
	/** テナントID -> テナントロールID */
	private SortedMap<String, Set<String>> tenantId2roleId;
	
	/** テナントロール -> テナントID */
	private Map<String, String> roleId2TenantId;

	/** テナントID -> アドレスグループ(サブネットアドレスのリスト) */
	private Map<String, List<SubnetAddress>> tenantId2AddrGroup;

	/** テナントID -> ファシリティIDのプレフィックス */
	private Map<String, String> tenantId2FacilityIdPrefix;

	/** 全てのテナントで共有されるアドレスグループ */
	private List<SubnetAddress> sharedAddrGroup;
	
	/** テナントロール設定エラー情報 */
	static class RolesSettingError {
		/** 設定値 */
		public String settingValue;
		/** テナントID */
		public String tenantId;
		/** 衝突したロールID */
		public String roleId;
		/** 先にロールを取ったテナントID */
		public String priorTenantId;
	}
	
	/** アドレスグループ設定エラー情報 */
	static class AddressGroupSettingError {
		/** 設定値 */
		public String settingValue;
		/** テナントID */
		public String tenantId;
		/** 問題のあるCIDR */
		public String cidr;
	}
	
	// 外部依存処理
	private External external;
	private static final External defaultExternal = new External();
	static class External {
		/** Hinemosプロパティ "multitenant.enable" */
		boolean getMultiTenantEnable() {
			return HinemosPropertyCommon.multitenant_enable.getBooleanValue();
		}

		/** Hinemosプロパティ "multitenant.scan.max" */
		int getMultiTenantScanMax() {
			return HinemosPropertyCommon.multitenant_scan_max.getNumericValue().intValue();
		}

		/** Hinemosプロパティ "multitenant.$.enable" */
		boolean getMultiTenantEnable(String tenantId) {
			return HinemosPropertyCommon.multitenant_$_enable.getBooleanValue(tenantId);
		}

		/** Hinemosプロパティ "multitenant.$.roles" */
		String getMultiTenantRoles(String tenantId) {
			return HinemosPropertyCommon.multitenant_$_roles.getStringValue(tenantId);
		}

		/** Hinemosプロパティ "multitenant.$.addressgroup" */
		String getMultiTenantAddressGroup(String tenantId) {
			return HinemosPropertyCommon.multitenant_$_addressgroup.getStringValue(tenantId);
		}

		/** Hinemosプロパティ "multitenant.$.nodesearch.facilityid.prefix" */
		String getMultiTenantNodeSearchFacilityIdPrefix(String tenantId) {
			return HinemosPropertyCommon.multitenant_$_nodesearch_facilityid_prefix.getStringValue(tenantId);
		}

		/** Hinemosプロパティ "multitenant.$.xcloud.snmp.user" */
		String getMultiTenantXcloudSnmpUser(String tenantId) {
			return HinemosPropertyCommon.multitenant_$_xcloud_snmp_user.getStringValue(tenantId);
		}

		/** Hinemosプロパティ "multitenant.$.xcloud.snmp.port" */
		Integer getMultiTenantXcloudSnmpPort(String tenantId) {
			return HinemosPropertyCommon.multitenant_$_xcloud_snmp_port.getIntegerValue(tenantId);
		}

		/** Hinemosプロパティ "multitenant.$.xcloud.snmp.community" */
		String getMultiTenantXcloudSnmpCommunity(String tenantId) {
			return HinemosPropertyCommon.multitenant_$_xcloud_snmp_community.getStringValue(tenantId);
		}

		/** Hinemosプロパティ "multitenant.$.xcloud.snmp.version" */
		String getMultiTenantXcloudSnmpVersion(String tenantId) {
			return HinemosPropertyCommon.multitenant_$_xcloud_snmp_version.getStringValue(tenantId);
		}

		/** Hinemosプロパティ "multitenant.$.xcloud.snmp.securitylevel" */
		String getMultiTenantXcloudSnmpSecuritylevel(String tenantId) {
			return HinemosPropertyCommon.multitenant_$_xcloud_snmp_securitylevel.getStringValue(tenantId);
		}
		
		/** Hinemosプロパティ "multitenant.$.xcloud.snmp.auth.password" */
		String getMultiTenantXcloudSnmpAuthPassword(String tenantId) {
			return HinemosPropertyCommon.multitenant_$_xcloud_snmp_auth_password.getStringValue(tenantId);
		}
		
		/** Hinemosプロパティ "multitenant.$.xcloud.snmp.priv.password" */
		String getMultiTenantXcloudSnmpPrivPassword(String tenantId) {
			return HinemosPropertyCommon.multitenant_$_xcloud_snmp_priv_password.getStringValue(tenantId);
		}
		
		/** Hinemosプロパティ "multitenant.$.xcloud.snmp.auth.protocol" */
		String getMultiTenantXcloudSnmpAuthProtocol(String tenantId) {
			return HinemosPropertyCommon.multitenant_$_xcloud_snmp_auth_protocol.getStringValue(tenantId);
		}

		/** Hinemosプロパティ "multitenant.$.xcloud.snmp.priv.protocol" */
		String getMultiTenantXcloudSnmpPrivProtocol(String tenantId) {
			return HinemosPropertyCommon.multitenant_$_xcloud_snmp_priv_protocol.getStringValue(tenantId);
		}

		/** Hinemosプロパティ "multitenant.$.xcloud.snmp.timeout" */
		Integer getMultiTenantXcloudSnmpTimeout(String tenantId) {
			return HinemosPropertyCommon.multitenant_$_xcloud_snmp_timeout.getIntegerValue(tenantId);
		}

		/** Hinemosプロパティ "multitenant.$.xcloud.snmp.retries" */
		Integer getMultiTenantXcloudSnmpRetries(String tenantId) {
			return HinemosPropertyCommon.multitenant_$_xcloud_snmp_retries.getIntegerValue(tenantId);
		}

		/** Hinemosプロパティ "xcloud.ipaddress.notavailable" */
		String getXcloudIpAddressNotAvailable() {
			return HinemosPropertyCommon.xcloud_ipaddress_notavailable.getStringValue();
		}
		
		NodeInfo getNodeInfo(RepositoryControllerBean bean, String facilityId) throws FacilityNotFound, HinemosUnknown {
			return bean.getNode(facilityId);
		}
		
		void putInternalEvent(InternalIdAbstract internalId, String[] msgArgs) {
			AplLogger.put(internalId, msgArgs);
		}
	}

	/**
	 * Hinemosプロパティのキャッシュ更新に合わせてシングルトンインスタンスを再生成するよう設定します。
	 */
	public static MultiTenantSupport prepareSingleton() {
		log.info("Preparing...");
		HinemosPropertyInfoCache.addRefreshedEventListener("MultiTenantSupportReload",
				() -> Singletons.update(MultiTenantSupport.class));
		return Singletons.get(MultiTenantSupport.class);
	}

	/**
	 * インスタンスを生成するたびに、Hinemosプロパティを走査して各種内部情報を構築するため、それなりのコストが予想されます。
	 * 個別にインスタンスを作ることができないわけでありませんが、シングルトン {@link Singletons#get(Class)} を使用してください。
	 * {@link #prepareSingleton()} を実行しておくことで、設定変更で自動的にシングルトンが更新されます。 
	 */
	public MultiTenantSupport() {
		this(defaultExternal);
	}

	/**
	 * 外部依存処理を注入してインスタンスを生成します。
	 * 単体テスト用です。
	 */
	MultiTenantSupport(External external) {
		this.external = external;

		enabled = external.getMultiTenantEnable();
		if (!enabled) {
			log.info("Disabled.");
			return;
		}
		
		// テナントロール設定の読み取り
		tenantId2roleId = new TreeMap<>();
		roleId2TenantId = new HashMap<>();
		{
			List<RolesSettingError> errors = new ArrayList<>();
			initTenantRoleMaps(tenantId2roleId, roleId2TenantId, errors, external.getMultiTenantScanMax(),
					(tenantId) -> external.getMultiTenantEnable(tenantId),
					(tenantId) -> external.getMultiTenantRoles(tenantId));
	
			// 設定エラーをINTERNALイベントで通知
			for (RolesSettingError error : errors) {
				external.putInternalEvent(InternalIdCommon.MULTI_TENANT_SYS_001,
						new String[] {
								"multitenant.$.roles".replace("$", error.tenantId),
								error.settingValue,
								error.roleId,
								error.priorTenantId,
								error.tenantId
								});
			}
		}

		// テナントアドレスグループ設定の読み取り
		tenantId2AddrGroup = new HashMap<>();
		{
			List<AddressGroupSettingError> errors = new ArrayList<>();
			initTenantId2AddrGroup(tenantId2AddrGroup, errors, tenantId2roleId.keySet(),
					(tenantId) -> external.getMultiTenantAddressGroup(tenantId));
	
			// 設定エラーをINTERNALイベントで通知
			for (AddressGroupSettingError error : errors) {
				external.putInternalEvent(InternalIdCommon.MULTI_TENANT_SYS_002,
						new String[] {
								"multitenant.$.addressgroup".replace("$", error.tenantId),
								error.settingValue,
								error.cidr
								});
			}
		}

		// 共有アドレスグループ設定 (現在はxcloudの無効アドレスのみ)
		sharedAddrGroup = initSharedAddrGroup(() ->
				Arrays.asList(external.getXcloudIpAddressNotAvailable()));

		// ファシリティIDプレフィックス設定の読み取り
		tenantId2FacilityIdPrefix = new HashMap<>();
		{
			for (String tenantId : tenantId2roleId.keySet()) {
				String prefix = external.getMultiTenantNodeSearchFacilityIdPrefix(tenantId).trim();
				if (prefix.length() > 0) {
					try {
						// 長さを厳密に制限するメリットは特にないため、ファシリティIDの最大長(512)まで許容する。
						// 実際に512文字のプレフィックスを付けたらノード登録に必ず失敗する。
						CommonValidator.validateId("", prefix, 512);
					} catch (InvalidSetting e) {
						external.putInternalEvent(InternalIdCommon.MULTI_TENANT_SYS_003,
								new String[] {
										"multitenant.$.nodesearch.facilityid.prefix".replace("$", tenantId),
										prefix
										});
						// 空文字列にする
						prefix = "";
					}
				}
				tenantId2FacilityIdPrefix.put(tenantId, prefix);
			}
		}
		
		log.debug("Setting Report\n" + String.join("\n", reportSetting()));
	}
	
	/**
	 * 有効なテナントIDをスキャンしつつ、
	 * テナントIDとテナントロールIDを相互に引くためのマップを作成して返します。
	 * <p>
	 * テナント間でロールIDが衝突(同じロールIDを指定)した場合、
	 * テナントIDのリストでより先に列挙されるほうへ紐付けます。
	 * 
	 * @param tenantId2RoleId テナントIDからテナントロールIDを引くためのマップ。空のMapインスタンスを渡す。
	 * @param roleId2TenantId テナントロールIDからテナントIDを引くためのマップ。空のMapインスタンスを渡す。
	 * @param errors 設定エラーを受け取るリスト。空のListインスタンスを渡す。[0]:テナントID [1]:設定値
	 * @param maxId テナントIDの最大番号。
	 * @param enableSettingProvider テナントIDを渡すと、有効/無効の設定値を返す関数。
	 * @param rolesSettingProvider テナントIDに紐づくロールIDの設定値を返す関数。
	 */
	static void initTenantRoleMaps(Map<String, Set<String>> tenantId2RoleId, Map<String, String> roleId2TenantId,
			List<RolesSettingError> errors, int maxId, Function<String, Boolean> enableSettingProvider,
			Function<String, String> rolesSettingProvider) {

		// 有効なテナントIDを走査しつつ、tenantId->roleId のエントリ(キーのみ)を生成する
		for (int i = 1; i <= maxId; ++i) {
			String tenantId = i < 10 ? ("0" + i) :  String.valueOf(i);
			if (enableSettingProvider.apply(tenantId)) {
				tenantId2RoleId.put(tenantId, new HashSet<>());
			}
		}

		// 上記で求めたテナントIDのリストを元に、
		// roleId->tenantId のエントリを生成し、tenantId->roleId のエントリ(値)を補完する
		for (Entry<String, Set<String>> entry : tenantId2RoleId.entrySet()) {
			String tenantId = entry.getKey();
			Set<String> roleIds = entry.getValue();
			Map<String, String> roleId2TenantIdDelta = new HashMap<>();
			String rolesSetting = rolesSettingProvider.apply(tenantId);
			for (String roleId : rolesSetting.split(",")) {
				roleId = roleId.trim();
				if (roleId.length() == 0) continue;
				if (!roleId2TenantId.containsKey(roleId)) {
					roleId2TenantIdDelta.put(roleId, tenantId);
					roleIds.add(roleId);
				} else {
					// ロールIDの衝突あり
					// - このテナント設定は初期値(どのロールとも関連なし)にする
					roleId2TenantIdDelta.clear();
					roleIds.clear();

					RolesSettingError err = new RolesSettingError();
					err.settingValue = rolesSetting;
					err.tenantId = tenantId;
					err.priorTenantId = roleId2TenantId.get(roleId);
					err.roleId = roleId;
					errors.add(err);

					log.warn("initRoleId2TenantId: Role ID confliction, roleId=" + roleId
							+ ", tenantId(later)=" + tenantId + ", tenantId(prior)=" + err.priorTenantId);
					break;	// 次のテナントへ
				}
			}
			// roleId2TenantIdを更新する。
			roleId2TenantId.putAll(roleId2TenantIdDelta);
		}
	}

	/**
	 * テナントIDからアドレスグループを引くためのマップを作成して返します。
	 * 
	 * @param tenantId2RoleId テナントIDからアドレスグループを引くためのマップ。空のMapインスタンスを渡す。
	 * @param errors 設定エラーを受け取るリスト。空のListインスタンスを渡す。[0]:テナントID [1]:設定値
	 * @param tenantIds テナントIDのリスト。
	 * @param addrgroupSettingProvider テナントIDに紐づくアドレスグループの設定値を返す関数。
	 */
	static void initTenantId2AddrGroup(Map<String, List<SubnetAddress>> tenantId2AddrGroup,
			List<AddressGroupSettingError> errors, Iterable<String> tenantIds,
			Function<String, String> addrgroupSettingProvider) {
		for (String tenantId : tenantIds) {
			List<SubnetAddress> addrGroup = new ArrayList<>();
			tenantId2AddrGroup.put(tenantId, addrGroup);
			String addrGroupSetting = addrgroupSettingProvider.apply(tenantId);
			for (String cidr : addrGroupSetting.split(",")) {
				cidr = cidr.trim();
				if (cidr.length() == 0) continue;
				try {
					addrGroup.add(new SubnetAddress(cidr));
				} catch (UnknownHostException e) {
					// 不正なCIDRあり
					// - このテナント設定は初期値(アドレスグループ指定なし)にする
					addrGroup.clear();

					AddressGroupSettingError err = new AddressGroupSettingError();
					err.settingValue = addrGroupSetting;
					err.tenantId = tenantId;
					err.cidr = cidr;
					errors.add(err);

					log.warn("initTenantId2AddrGroup: Invalid CIDR, tenantId=" + tenantId + ", cidr=" + cidr);
					break;  // 次のテナントへ
				}
			}
		}
	}

	/**
	 * 共有アドレスグループを初期化します。
	 */
	static List<SubnetAddress> initSharedAddrGroup(Supplier<List<String>> addressListProvider) {
		List<SubnetAddress> rtn = new ArrayList<>();
		for (String cidr : addressListProvider.get()) {
			try {
				rtn.add(new SubnetAddress(cidr));
			} catch (UnknownHostException e) {
				log.warn("initSharedAddrGroup: Invalid shared address=" + cidr);
			}
		}
		return rtn;
	}

	/**
	 * このインスタンスが保持している設定情報を文字列のリストにして返します。
	 */
	private List<String> reportSetting() {
		List<String> r = new ArrayList<>();
		r.add(tenantId2roleId.size() + " tenants, SharedAddressGroup=" + sharedAddrGroup);
		for (Entry<String, Set<String>> entry : tenantId2roleId.entrySet()) {
			StringBuilder sb = new StringBuilder();
			sb.append("Tenant'" + entry.getKey() + "': ");
			sb.append("Roles=" + entry.getValue());
			sb.append(" AddressGroup=" + tenantId2AddrGroup.get(entry.getKey()));
			r.add(sb.toString());
		}
		return r;
	}
	
	/**
	 * マルチテナント制御が有効な場合は true を返します。
	 */
	public boolean isEnabled() {
		return enabled;
	}

	/**
	 * 指定されたテナントロールに紐づくテナントのIDを返します。
	 * 該当するテナントがない(＝テナントロールではない)場合は null を返します。
	 * マルチテナント制御が無効な場合も null を返します。
	 */
	public String getTenantId(String roleId) {
		if (!enabled) return null;
		return roleId2TenantId.get(roleId);
	}

	/**
	 * IPアドレスの有効性を判定します。
	 * <ul>
	 * <li>マルチテナント制御が無効な場合は true を返します。
	 * <li>指定されたテナントロールが ADMINISTRATORS の場合は true を返します。
	 * <li>指定されたIPアドレスが、指定されたテナントロールに紐づくテナントのアドレスグループ、
	 *     あるいは共有アドレスグループ(※)に含まれる場合は true を返します。
	 * <li>指定されたのがテナントロールでない場合は、指定されたIPアドレスの内容に関わらず false を返します。
	 * </ul>
	 * <p>※ 現在は xcloud の「無効なIPアドレス」のみがこれに属します。
	 */
	public boolean containsAddressGroup(String roleId, byte[] addr) {
		if (!enabled) return true;
		
		// ADMINISTRATORS ロールなら　true
		if (RoleIdConstant.ADMINISTRATORS.equals(roleId)) return true;

		// 共有アドレスグループなら true
		for (SubnetAddress sa : sharedAddrGroup) {
			if (sa.contains(addr)) return true;
		}
		
		// ロールIDがblankでないことを呼び出し元は保証すべし
		if (roleId == null || roleId.length() == 0) {
			throw new IllegalArgumentException("roleId is empty.");
		}

		// 該当するテナントがない場合はfalse
		String tenantId = roleId2TenantId.get(roleId);
		if (tenantId == null) return false;

		// テナントアドレスグループに含まれるなら true
		List<SubnetAddress> addrGroup = tenantId2AddrGroup.get(tenantId);
		if (addrGroup == null) return false;
		for (SubnetAddress subnet : addrGroup) {
			if (subnet.contains(addr)) return true;
		}
		return false;
	}

	/**
	 * 指定されたテナントロールに紐づくテナントの設定に従って、ノードサーチが自動生成するノードのファシリティIDを修飾して返します。
	 * マルチテナント制御が無効な場合は無加工のファシリティIDを返します。
	 */
	public String decorateNodeSearchedFacilityId(String roleId, String facilityId) {
		if (!enabled) return facilityId;

		// 該当するテナントがない場合は修飾なし
		String tenantId = roleId2TenantId.get(roleId);
		if (tenantId == null) return facilityId;

		// プレフィックスを付与して返す
		return tenantId2FacilityIdPrefix.get(tenantId) + facilityId;
	}
	
	/**
	 * 指定されたテナントロールに紐づくテナントの設定に従って、xcloudが自動生成するノードにプロパティを格納して返します。
	 * マルチテナント制御が無効な場合はそのまま返します。
	 */
	public NodeInfo replaceNodeProperties(NodeInfo nodeInfo) {
		if (!enabled) {
			return nodeInfo;
		}
		// 該当するテナントがない場合はそのまま返す
		String tenantId = roleId2TenantId.get(nodeInfo.getOwnerRoleId());
		if (tenantId == null) {
			return nodeInfo;
		}
		
		// テナント別設定がある場合はそちらを格納する
		if (external.getMultiTenantXcloudSnmpUser(tenantId).length() != 0) {
			nodeInfo.setSnmpUser(external.getMultiTenantXcloudSnmpUser(tenantId));
		}
		if (external.getMultiTenantXcloudSnmpPort(tenantId).intValue() != 0) {
			nodeInfo.setSnmpPort(external.getMultiTenantXcloudSnmpPort(tenantId));
		}
		if (external.getMultiTenantXcloudSnmpCommunity(tenantId).length() != 0) {
			nodeInfo.setSnmpCommunity(external.getMultiTenantXcloudSnmpCommunity(tenantId));
		}
		if (external.getMultiTenantXcloudSnmpVersion(tenantId).length() != 0) {
			nodeInfo.setSnmpVersion(SnmpVersionConstant.stringToType(external.getMultiTenantXcloudSnmpVersion(tenantId)));
		}
		if (external.getMultiTenantXcloudSnmpSecuritylevel(tenantId).length() != 0) {
			nodeInfo.setSnmpSecurityLevel(external.getMultiTenantXcloudSnmpSecuritylevel(tenantId));
		}
		if (external.getMultiTenantXcloudSnmpAuthPassword(tenantId).length() != 0) {
			nodeInfo.setSnmpAuthPassword(external.getMultiTenantXcloudSnmpAuthPassword(tenantId));
		}
		if (external.getMultiTenantXcloudSnmpPrivPassword(tenantId).length() != 0) {
			nodeInfo.setSnmpPrivPassword(external.getMultiTenantXcloudSnmpPrivPassword(tenantId));
		}
		if (external.getMultiTenantXcloudSnmpAuthProtocol(tenantId).length() != 0) {
			nodeInfo.setSnmpAuthProtocol(external.getMultiTenantXcloudSnmpAuthProtocol(tenantId));
		}
		if (external.getMultiTenantXcloudSnmpPrivProtocol(tenantId).length() != 0) {
			nodeInfo.setSnmpPrivProtocol(external.getMultiTenantXcloudSnmpPrivProtocol(tenantId));
		}
		if (external.getMultiTenantXcloudSnmpTimeout(tenantId).intValue() != 0) {
			nodeInfo.setSnmpTimeout(external.getMultiTenantXcloudSnmpTimeout(tenantId));
		}
		if (external.getMultiTenantXcloudSnmpRetries(tenantId).intValue() != 0) {
			nodeInfo.setSnmpRetryCount(external.getMultiTenantXcloudSnmpRetries(tenantId));
		}
		return nodeInfo;
	}

	/**
	 * 指定されたIPアドレスが、指定されたノードのテナントアドレスグループの範囲内かをチェックし、
	 * 範囲内となったノードのファシリティIDだけを返します。
	 * マルチテナント制御が無効な場合はファシリティIDのリストをそのまま返します。
	 * 
	 * @param facilityIds ノードのファシリティIDのリスト。
	 * @param ipAddress 判定に使用するIPアドレス。
	 * @return フィルタ後のファシリティIDのリスト。
	 */
	public Set<String> filterNodes(Set<String> facilityIds, String ipAddress) {
		if (!enabled) return facilityIds;

		// IPアドレスをバイト列へ変換
		byte[] addrBytes;
		try {
			addrBytes = InetAddress.getByName(ipAddress).getAddress();
		} catch (UnknownHostException e) {
			// 不正なIPアドレスなら全ノード不許可
			log.warn("filterNodes: Invalid IP address = " + ipAddress);
			return null;
		}
	
		// ノードのオーナーロールIDから、テナントアドレスグループ範囲内かどうかを判定
		Set<String> acceptedList = new HashSet<>();
		RepositoryControllerBean repo = new RepositoryControllerBean();
		for (String facilityId : facilityIds) {
			try {
				NodeInfo node = external.getNodeInfo(repo, facilityId);
				if (containsAddressGroup(node.getOwnerRoleId(), addrBytes)) {
					acceptedList.add(facilityId);
				} else {
					log.debug("filterNodes: Remove the node."
							+ " facilityId=" + facilityId
							+ ", ipAddress=" + ipAddress
							+ ", ownerRoleId=" + node.getOwnerRoleId());
				}
			} catch (FacilityNotFound e) {
				log.warn("filterNodes: Cannot find the node. facilityId=" + facilityId);
			} catch (HinemosUnknown e) {
				log.warn("filterNodes: An error occurred while finding the node. facilityId=" + facilityId);
			}
		}

		return (acceptedList.size() == 0) ? null : acceptedList;
	}

}
