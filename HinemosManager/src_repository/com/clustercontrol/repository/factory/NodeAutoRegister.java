/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.repository.factory;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import com.clustercontrol.accesscontrol.session.AccessControllerBean;
import com.clustercontrol.bean.IpAddressInfo;
import com.clustercontrol.commons.util.CommonValidator;
import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.commons.util.HinemosSessionContext;
import com.clustercontrol.commons.util.InternalIdCommon;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.AutoRegisterNodeSettingNotFound;
import com.clustercontrol.fault.FacilityDuplicate;
import com.clustercontrol.fault.FacilityNotFound;
import com.clustercontrol.fault.HinemosDbTimeout;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.RoleNotFound;
import com.clustercontrol.repository.bean.AutoRegisterResult;
import com.clustercontrol.repository.bean.AutoRegisterStatus;
import com.clustercontrol.repository.bean.DeviceTypeConstant;
import com.clustercontrol.repository.bean.FacilityConstant;
import com.clustercontrol.repository.bean.PlatformConstant;
import com.clustercontrol.repository.model.AutoRegisterNodeInfo;
import com.clustercontrol.repository.model.NodeInfo;
import com.clustercontrol.repository.model.NodeNetworkInterfaceInfo;
import com.clustercontrol.repository.session.AutoRegisterNodeControllerBean;
import com.clustercontrol.repository.session.RepositoryControllerBean;
import com.clustercontrol.repository.util.QueryUtil;
import com.clustercontrol.util.FileUtil;
import com.clustercontrol.util.HinemosTime;
import com.clustercontrol.util.MessageConstant;
import com.clustercontrol.util.NetworkInterfaceUtil;
import com.clustercontrol.util.apllog.AplLogger;

/**
 * 自動ノード登録の実行クラス.
 *
 * @version 6.2.0
 * @since 6.2.0
 */
public class NodeAutoRegister {

	// ログ出力関連
	/** ロガー */
	private static Logger logger = Logger.getLogger(NodeAutoRegister.class);
	/** ログ出力区切り文字 */
	private static final String DELIMITER = "() : ";

	// 自動登録でDBに登録される定数.
	/** 説明 */
	private static final String FACILITY_DESCRIPTION = "Automatically registered at ";

	/** 表示順 */
	private static final Integer FACILITY_DISPLAYSORTORDER = 100;

	// DB登録のチェック用桁数等.
	/** 連番桁数上限(20桁) */
	private static final int SEAUENTIAL_DIGITS_MAX = Long.valueOf(Long.MIN_VALUE).toString().length();

	/** DB上のfacility_name最大バイト数 */
	private static final int FACILITY_NAME_MAX_BYTE = 256;

	// 複数スレッド制御用.
	/** 連番制御用マップ{@code <設定キー, 最終連番>} */
	private static ConcurrentHashMap<Integer, Long> serialNumberMap = new ConcurrentHashMap<Integer, Long>();

	/** 連番ロック用オブジェクト */
	private static Object serialNumLock = new Object();

	// 登録済チェック処理向けフィールド.
	/** 有効なノードのMACアドレスリスト(16進数"01:02:03:04:05:06"の形式) */
	private LinkedList<String> validMacAddresses = new LinkedList<String>();

	// 登録処理向けフィールド.
	/** 有効なノードのNIFリスト(ALL0のMACアドレス含む) */
	private LinkedList<NodeNetworkInterfaceInfo> validNodeNifList = new LinkedList<NodeNetworkInterfaceInfo>();

	/** 接続元IPアドレス(ネットワーク構成によってはノードのIPから書き換えられている) */
	private InetAddress sourceIpAddress = null;

	/** ノードのOS(初回OS別スコープ登録用) */
	private String nodePlatform = null;

	/** 有効な設定情報マップ{@literal <priority, 自動登録設定>} */
	private Map<Integer, AutoRegisterNodeInfo> validSettingMap = null;

	/** 設定に紐づくサブネットマップ{@literal <priority, subnet>} */
	private Map<Integer, IpAddressInfo> subnetMap = null;

	/** 設定のキー(priority) */
	private LinkedList<Integer> settingPriority = null;

	// その他.
	/** ノードのMACアドレス文字列(ログ出力用) */
	private String forLogAddress = null;

	/**
	 * コンストラクタ.
	 * 
	 * @param nodePlatform
	 *            ノードのプラットフォーム(OS)、空文字もOK(Otherとして登録)
	 * @param nodeNifList
	 *            ノードのNIFリスト、有効かどうか判定の上フィールドにセットされる.
	 * @param forLogAddress
	 *            ノードのMACアドレス文字列(ログ出力用).
	 * @param sourceIpAddress
	 *            接続元IPアドレス、適用する設定の判定用.
	 * @throws InvalidSetting
	 */
	public NodeAutoRegister(String nodePlatform, List<NodeNetworkInterfaceInfo> nodeNifList, String forLogAddress,
			InetAddress sourceIpAddress) throws InvalidSetting {
		this.nodePlatform = nodePlatform;
		this.forLogAddress = forLogAddress;
		this.sourceIpAddress = sourceIpAddress;
		this.setValidNifList(nodeNifList);
	}

	/**
	 * 有効なNIFを判定して設定する.
	 * 
	 * MACアドレスが存在するかどうか、DB登録用に主キーが存在するかをチェックしてフィールドにセット.
	 * 
	 * @param nodeNifList
	 *            ノードのNIFリスト、有効かどうか判定の上フィールドにセットされる.
	 * @throws InvalidSetting
	 */
	private void setValidNifList(List<NodeNetworkInterfaceInfo> nodeNifList) throws InvalidSetting {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();

		// NIFリスト存在チェック.
		if (nodeNifList == null || nodeNifList.isEmpty()) {
			InvalidSetting e = new InvalidSetting(methodName + DELIMITER + "Network Interfaces are empty.");
			logger.warn(methodName + DELIMITER + e.getClass().getSimpleName() + ", " + e.getMessage());
			this.putInternalLogMac();
			throw e;
		}

		// 有効判定してセット.
		for (NodeNetworkInterfaceInfo nif : nodeNifList) {
			if (nif == null) {
				continue;
			}

			// MACアドレス・主キー存在チェック.
			String macAddress = nif.getNicMacAddress();
			if (macAddress == null || macAddress.isEmpty() || nif.getDeviceIndex() == null
					|| nif.getDeviceName() == null || nif.getDeviceName().isEmpty()) {
				logger.debug(
						methodName + DELIMITER + "NodeNetworkInterfaceInfo from agent has empty fileds neccesally.");
				continue;
			}
			this.validNodeNifList.add(nif);

			// ALL 0のMACアドレスはデバイスのエラーで不正な設定になってる場合なのではじく.
			String allZeroMac = "00:00:00:00:00:00";
			boolean isAllZero = NetworkInterfaceUtil.equalsMacAddresses(allZeroMac, macAddress);

			if (isAllZero) {
				logger.info(methodName + DELIMITER
						+ "NodeNetworkInterfaceInfo from agent has invalid MAC address '00:00:00:00:00:00'.");
				continue;
			}
			this.validMacAddresses.add(macAddress);
			if (logger.isDebugEnabled()) {
				String ipAddress = nif.getNicIpAddress();
				if (ipAddress == null) {
					ipAddress = "null";
				}
				logger.debug(methodName + DELIMITER
						+ String.format(
								"add valid NodeNifList. deviceIndex=[%s], deviceName=[%s], MacAddress=[%s], IpAddress=[%s]",
								nif.getDeviceIndex(), nif.getDeviceName(), macAddress, ipAddress));
			}
		}

		// 有効な(DB登録可能な)MACアドレスなし → Exception throwして自動登録失敗とする.
		if (this.validMacAddresses.isEmpty()) {
			InvalidSetting e = new InvalidSetting(methodName + DELIMITER + "valid Mac Address isn't exist.");
			logger.warn(methodName + DELIMITER + e.getClass().getSimpleName() + ", " + e.getMessage());
			this.putInternalLogMac();
			throw e;
		}
	}

	/**
	 * 自動登録メイン処理.<br>
	 * <br>
	 * 登録済かどうかをチェックして自動登録を行う.<br>
	 * 
	 * @return 登録結果
	 * @throws HinemosDbTimeout
	 * @throws HinemosUnknown
	 * @throws InvalidSetting
	 */
	public AutoRegisterResult autoRegister() throws HinemosDbTimeout, HinemosUnknown, InvalidSetting {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();

		// 処理結果.
		AutoRegisterResult result = null;

		JpaTransactionManager jtm = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			// 登録済チェック.
			String registeredId = this.checkRegistered();
			if (registeredId != null && !registeredId.isEmpty()) {
				result = new AutoRegisterResult(AutoRegisterStatus.EXIST, registeredId);
				logger.info(methodName + DELIMITER + String.format(
						"skip to register because the node has been registered. registeredId=[%s]", registeredId));
				return result;
			}

			// 自動登録設定リストの取得.
			this.getValidSettings();

			// 取得した設定の内ソースIPに合致する範囲を特定.
			AutoRegisterNodeInfo registerSet = this.identifySetting();
			if (registerSet == null) {
				// 取得できなかった=自動登録対象外.
				String message = String.format(
						"valid setting isn't exist to register node automatically."
								+ " the IP address of transmission source isn't within the 'source_network' on 'setting.cc_cfg_auto_register_node'."
								+ " MAC addresses=[%s], sourceIP=[%s]",
						this.forLogAddress, this.sourceIpAddress.getHostAddress());
				logger.info(methodName + DELIMITER + message);
				String[] args = { this.sourceIpAddress.getHostAddress() };
				AplLogger.put(InternalIdCommon.PLT_REP_AREG_SYS_001, args);
				throw new InvalidSetting(message);
			}

			// 取得した登録設定に従って自動登録実行.
			String registeredFacilityId = this.registerNode(registerSet);
			jtm.commit();
			result = new AutoRegisterResult(AutoRegisterStatus.REGISTERED, registeredFacilityId);
		} catch (HinemosUnknown e) {
			if (jtm != null) {
				jtm.rollback();
			}
			throw e;
		} catch (Exception e) {
			if (jtm != null) {
				jtm.rollback();
			}
			throw e;
		} finally {
			if (jtm != null) {
				jtm.close();
			}
		}

		return result;
	}

	/**
	 * 登録済チェック処理.<br>
	 * <br>
	 * MACアドレスをキーにノードNIF情報テーブルを検索して登録済かどうかをチェックする.<br>
	 * 
	 * @return 登録済のFacilityID、未登録の場合はnull返却
	 * 
	 * @throws HinemosDbTimeout
	 * @throws HinemosUnknown
	 */
	private String checkRegistered() {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
		String registeredID = null;

		for (String macAddress : this.validMacAddresses) {
			// 登録されているMACアドレスを検索.
			logger.trace(methodName + DELIMITER + String
					.format("prepared to check registering MAC-address." + " MAC-address(node)=[%s]", macAddress));
			List<NodeNetworkInterfaceInfo> nifList = QueryUtil.getNodeNetworkInterfaceInfoByMacAddress(macAddress);
			if (nifList != null && !nifList.isEmpty()) {
				// 取得できたので登録されていると判定.
				logger.info(methodName + DELIMITER
						+ String.format("registered MAC-address. MAC-address(node)=[%s]", macAddress));
				for (NodeNetworkInterfaceInfo nif : nifList) {
					if (nif.getFacilityId() == null || nif.getFacilityId().isEmpty()) {
						// ここ通るのは不正データ、DBの項目定義が変わらない限りはありえない想定.
						continue;
					}
					registeredID = nif.getFacilityId();
					break;
				}
				return registeredID;
			} else {
				// 取得不可なので次を検索.
				logger.trace(methodName + DELIMITER + String
						.format("get false to check registering MAC-address." + " MAC-address(node)=[%s]", macAddress));
				continue;
			}
		}

		// 全アドレスについてチェック完了.
		logger.debug(methodName + DELIMITER
				+ String.format("not registered MAC-address. MAC-address=[%s]", this.forLogAddress));
		return null;

	}

	/**
	 * 有効な自動登録設定を取得.<br>
	 * 
	 * @throws InvalidSetting
	 *             DBに登録されている設定内容が0件(1件以上有効な設定が存在すれば自動登録実行).
	 * @throws HinemosDbTimeout
	 *             設定取得時に発行するクエリタイムアウト.
	 */
	private void getValidSettings() throws InvalidSetting {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
		String message = null;

		// 有効な設定のみDBから取得.
		List<AutoRegisterNodeInfo> settingList = QueryUtil.getValidAutoRegisterNodeInfo();

		// DBから取得した設定が0件.
		if (settingList == null || settingList.isEmpty()) {
			message = String.format("failed to get settings to register node automatically. MAC addresses=[%s]",
					this.forLogAddress);
			logger.warn(methodName + DELIMITER + message);
			this.putInternalLogMac();
			throw new InvalidSetting(message);
		}

		// 取得した設定内容の整合性をチェック.
		this.checkValidSetting(settingList);

		// チェック結果、有効な設定が0件.
		if (this.validSettingMap == null || this.validSettingMap.isEmpty()) {
			message = "failed to get valid settings to register node automatically."
					+ " all of the settings on 'setting.cc_cfg_auto_register_node' are invalid.";
			logger.warn(methodName + DELIMITER + message);
			this.putInternalLogMac();
			throw new InvalidSetting(message);
		}

	}

	/**
	 * Internalエラー出力.<br>
	 * <br>
	 * ユーザー通知用の文言を出しわけしない場合の共通出力処理.<br>
	 * ※warnログの文言でエラー箇所は特定できるようにすること.
	 */
	private void putInternalLogMac() {
		String[] args = { MessageConstant.MAC_ADDRESS.getMessage(), this.forLogAddress };
		AplLogger.put(InternalIdCommon.PLT_REP_AREG_SYS_002, args);
	}

	/**
	 * 有効な自動登録設定の設定内容の整合性チェック.<br>
	 * <br>
	 * 自動登録機能は隠し機能として画面から設定不可でユーザーが直接DBに設定をInsertする想定のため<br>
	 * DBの項目定義では防げないデータ不整合について設定取得時にチェックする.<br>
	 * <br>
	 * 以下、チェック内容<br>
	 * <br>
	 * IP文字列形式チェック<br>
	 * →接続元ネットワークの文字列が正しい形式か(IPv4またはIPv6でプリフィックス指定有)、不整合はログ出力して設定無効とみなす.<br>
	 * <br>
	 * プリフィックス文字列形式チェック <br>
	 * →HinemosのID規則にマッチするかチェック、マッチしない場合は設定無効としてスキップ.<br>
	 * <br>
	 * ロールID存在チェック <br>
	 * →存在するロールIDかチェック、存在しない場合は設定無効としてスキップ.<br>
	 * <br>
	 * 下位無効チェック <br>
	 * → 上位のネットワークに下位が包含されて実質無効となっていないかのチェック、ログ出力のみ行う.<br>
	 * <br>
	 * プリフィックス文字列重複チェック <br>
	 * → 重複時はログ出力のみ行う.<br>
	 * 
	 * @param originSettingList
	 *            チェック対象の設定リスト(DB取得時に優先順位と接続元ネットワーク昇順でソートされている前提)
	 * @return チェック結果OKとなった設定マップ{@literal <priority, 設定>}
	 * 
	 */
	private void checkValidSetting(List<AutoRegisterNodeInfo> originSettingList) {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
		String message = null;

		// チェック結果格納用の変数生成.
		this.validSettingMap = new HashMap<Integer, AutoRegisterNodeInfo>();
		this.subnetMap = new HashMap<Integer, IpAddressInfo>();
		this.settingPriority = new LinkedList<Integer>();
		int keyPriority = 0;

		// 設定内容を1件ずつチェック.
		for (AutoRegisterNodeInfo setting : originSettingList) {

			// 接続元ネットワークの文字列が正しいか(共通部品内でチェック).
			IpAddressInfo subnet = null;
			try {
				subnet = NetworkInterfaceUtil.getIpAddressInfo(setting.getSourceNetwork());
			} catch (InvalidSetting | HinemosUnknown e) {
				subnet = null;
			}

			// 無効な文字列の設定はスキップ.
			if (subnet == null) {
				message = String
						.format("skip to get the setting on 'setting.cc_cfg_auto_register_node', because its 'source_network' is invalid."
								+ " key=[%d], source_network=[%s]", setting.getOrderNo(), setting.getSourceNetwork());
				logger.info(methodName + DELIMITER + message);
				continue;
			}

			// プリフィックスがFacilityIDとして不正な文字列ふくまれてる場合はスキップ.
			try {
				CommonValidator.validateId(MessageConstant.FACILITY_ID.getMessage(), setting.getPrefix(), 256);
			} catch (InvalidSetting e1) {
				message = String
						.format("skip to get the setting on 'setting.cc_cfg_auto_register_node', because its 'prefix' is invalid."
								+ " key=[%d], prefix=[%s]", setting.getOrderNo(), setting.getPrefix());
				logger.info(methodName + DELIMITER + message);
				continue;
			}

			// ロールID実在チェック.
			String roleId = setting.getOwnerRoleId();
			if (roleId == null || roleId.isEmpty()) {
				message = String
						.format("skip to get the setting on 'setting.cc_cfg_auto_register_node', because its 'owner_role_id' is empty."
								+ " key=[%d]", setting.getOrderNo());
				logger.info(methodName + DELIMITER + message);
				continue;
			}
			try {
				AccessControllerBean accessController = new AccessControllerBean();
				accessController.getRoleInfo(roleId);
			} catch (HinemosUnknown | RoleNotFound e1) {
				message = String
						.format("skip to get the setting on 'setting.cc_cfg_auto_register_node', because its 'owner_role_id' is invalid."
								+ " key=[%d], owner_role_id=[%s]", setting.getOrderNo(), roleId);
				logger.info(methodName + DELIMITER + message);
				continue;
			}

			// 下位無効チェック.
			if (!this.subnetMap.isEmpty()) {
				boolean containUpper = false;
				for (Entry<Integer, IpAddressInfo> upperSubnet : this.subnetMap.entrySet()) {
					try {
						containUpper = NetworkInterfaceUtil.checkContainSubnet(subnet, upperSubnet.getValue());
					} catch (InvalidSetting | HinemosUnknown e) {
						// 次の上位をチェック.
						continue;
					}
					if (containUpper) {
						// この時点で無効なのでログ出力(設定はそのまま残す).
						message = String.format(
								"the 'source_network' is invalid because upper setting contains it."
										+ " key=[%d], source_network=[%s], key(upper)=[%d], source_network(upper)=[%s]",
								setting.getOrderNo(), setting.getSourceNetwork(), upperSubnet.getKey(),
								upperSubnet.getValue().getOriginIpAddress());
						logger.info(methodName + DELIMITER + message);
						break;
					}

				}
			}

			// プリフィックス文字列重複チェック.
			if (!this.validSettingMap.isEmpty()) {
				for (Entry<Integer, AutoRegisterNodeInfo> upperSetting : this.validSettingMap.entrySet()) {
					// 重複してるのでログ出力(設定はそのまま残す).
					if (setting.getPrefix().equals(upperSetting.getValue().getPrefix())) {
						message = String.format(
								"the 'prefix' is duplicate."
										+ "prefix=[%s], key(upper)=[%d], source_network(upper)=[%s],"
										+ " key(lower)=[%d], source_network(lower)=[%s]",
								setting.getPrefix(), upperSetting.getKey(), upperSetting.getValue().getSourceNetwork(),
								setting.getOrderNo(), setting.getSourceNetwork());
						logger.info(methodName + DELIMITER + message);
						break;
					}
				}
			}

			// 優先順位をキーに有効な設定としてマップ追加.
			keyPriority = setting.getOrderNo().intValue();
			this.validSettingMap.put(keyPriority, setting);
			this.subnetMap.put(keyPriority, subnet);
			this.settingPriority.addLast(keyPriority);
		}
	}

	/**
	 * ソースIPが適用される設定を取得.<br>
	 */
	private AutoRegisterNodeInfo identifySetting() {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();

		// ソースIPが範囲に含まれるか確認.
		for (Integer key : this.settingPriority) {
			IpAddressInfo subnet = this.subnetMap.get(key);
			boolean containIp = false;
			logger.trace(methodName + DELIMITER
					+ String.format(
							"prepared to check the setting for source-ip."
									+ " setting-key=[%d], setting-source_network=[%s], source-ip=[%s]",
							key, subnet.getOriginIpAddress(), this.sourceIpAddress.getHostAddress()));
			try {
				containIp = NetworkInterfaceUtil.checkContainSubnet(this.sourceIpAddress, subnet);
			} catch (InvalidSetting | HinemosUnknown e) {
				// 次の設定を見ればいいのでスキップ.
				logger.trace(methodName + DELIMITER
						+ String.format(
								"this setting is invalid."
										+ " setting-key=[%d], setting-source_network=[%s], source-ip=[%s]",
								key, subnet.getOriginIpAddress(), this.sourceIpAddress.getHostAddress()));
				continue;
			}
			if (containIp) {
				AutoRegisterNodeInfo resultSetting = this.validSettingMap.get(key);
				logger.debug(methodName + DELIMITER
						+ String.format(
								"identified the setting to register node automatically."
										+ " key=[%d], source_network=[%s], source-ip=[%s]",
								key, resultSetting.getSourceNetwork(), this.sourceIpAddress.getHostAddress()));
				return resultSetting;
			}
			logger.trace(methodName + DELIMITER
					+ String.format(
							"the ip isn't contained the setting."
									+ " setting-key=[%d], setting-source_network=[%s], source-ip=[%s]",
							key, subnet.getOriginIpAddress(), this.sourceIpAddress.getHostAddress()));
		}

		// 適用される範囲がなくてループ終了.
		return null;
	}

	/**
	 * 引数の設定に従ってDBに登録実行.<br>
	 * 
	 * @param autoRegisterSetting
	 *            自動登録設定
	 * 
	 * @throws HinemosUnknown
	 * @throws InvalidSetting
	 */
	private String registerNode(AutoRegisterNodeInfo autoRegisterSetting) throws HinemosUnknown, InvalidSetting {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
		String message = null;

		// 登録情報をセット.
		NodeInfo nodeInfo = this.setNodeInfo(autoRegisterSetting);

		// ノード登録用にログインユーザーを自動登録用の管理者ユーザーに設定.
		String user = HinemosPropertyCommon.repository_auto_device_user.getStringValue();
		HinemosSessionContext.instance().setProperty(HinemosSessionContext.LOGIN_USER_ID, user);
		HinemosSessionContext.instance().setProperty(HinemosSessionContext.IS_ADMINISTRATOR,
				new AccessControllerBean().isAdministrator());

		// ノード登録.
		RepositoryControllerBean controller = new RepositoryControllerBean();
		try {
			controller.addNode(nodeInfo, true, true);
		} catch (FacilityDuplicate | InvalidSetting | HinemosUnknown e) {
			message = String.format("failed to register the node automatically." + " MAC addresses=[%s]",
					this.forLogAddress);
			logger.warn(methodName + DELIMITER + message);
			this.putInternalLogMac();
			throw new HinemosUnknown(message);
		} catch (Exception e) {
			message = String.format("failed to register the node automatically." + " MAC addresses=[%s]",
					this.forLogAddress);
			logger.warn(methodName + DELIMITER + message, e);
			this.putInternalLogMac();
			throw new HinemosUnknown(message);
		}

		// 登録済最終連番を設定テーブルに反映.
		try {
			AutoRegisterNodeControllerBean.modifyAutoRegisterSetting(autoRegisterSetting);
		} catch (AutoRegisterNodeSettingNotFound e) {
			// 登録済最終連番が更新できなくてもノード登録ができてれば問題なく動作するので握りつぶす.
			logger.info(methodName + DELIMITER + "skip to update 'lastSerialNumber' on 'cc_cfg_auto_register_node'.");
		}

		return nodeInfo.getFacilityId();
	}

	/**
	 * 引数の設定に従ってDB登録するためにNodeInfoの内容を設定する.<br>
	 * 
	 * @param autoRegisterSetting
	 *            自動登録設定(最終登録済連番を更新).
	 * @return DB登録用のNodeInfo
	 */
	private NodeInfo setNodeInfo(AutoRegisterNodeInfo autoRegisterSetting) throws InvalidSetting {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();

		// FacilityID・Name以外のFacilityInfoの項目.
		NodeInfo nodeInfo = new NodeInfo();
		nodeInfo.setFacilityType(FacilityConstant.TYPE_NODE);
		nodeInfo.setDescription(FACILITY_DESCRIPTION + HinemosTime.getDateString());
		nodeInfo.setDisplaySortOrder(FACILITY_DISPLAYSORTORDER);
		nodeInfo.setIconImage("");
		nodeInfo.setValid(Boolean.TRUE);
		nodeInfo.setOwnerRoleId(autoRegisterSetting.getOwnerRoleId());

		// OS別スコープ登録用にプラットフォーム登録.
		if (this.nodePlatform == null || this.nodePlatform.isEmpty()) {
			nodeInfo.setPlatformFamily(PlatformConstant.DEFAULT);
		} else if (PlatformConstant.SOLARIS.equals(this.nodePlatform)) {
			// Solarisの場合はサポート対象かチェック.
			boolean containSolaris = false;
			try {
				QueryUtil.getCollectorPlatformMstPK(PlatformConstant.SOLARIS);
				containSolaris = true;
				logger.debug(methodName + DELIMITER + "As a result of checking, the node is solaris.");
			} catch (FacilityNotFound e) {
				// ログ出力.
				logger.warn(methodName + DELIMITER + "failed to get flag of solaris.", e);
			}
			if (containSolaris) {
				nodeInfo.setPlatformFamily(PlatformConstant.SOLARIS);
			} else {
				nodeInfo.setPlatformFamily(PlatformConstant.DEFAULT);
			}
		} else {
			nodeInfo.setPlatformFamily(this.nodePlatform);
		}
		logger.debug(methodName + DELIMITER + String.format(
				"set the platformFamily to register automatically. platformFamily=[%s]", nodeInfo.getPlatformFamily()));

		// FacilityIDを採番.
		String facilityId = this.createFacilityId(autoRegisterSetting);
		nodeInfo.setFacilityId(facilityId);
		String facilityName = facilityId;
		if (facilityName.length() > FACILITY_NAME_MAX_BYTE) {
			facilityName = facilityName.substring(0, FACILITY_NAME_MAX_BYTE);
			logger.debug(methodName + DELIMITER
					+ String.format(
							"cut the name of facility into %d lengths to register automatically."
									+ " name(cut)=[%s], name(origin)[%s]",
							FACILITY_NAME_MAX_BYTE, facilityName, facilityId));
		}
		nodeInfo.setFacilityName(facilityName);
		nodeInfo.setNodeName(facilityName);

		// NIC情報(MACアドレスを全量必ず登録).
		String ipAddress = "";
		for (NodeNetworkInterfaceInfo nif : this.validNodeNifList) {
			nif.setFacilityId(facilityId);
			nif.setDeviceType(DeviceTypeConstant.DEVICE_NIC);
			nif.setDeviceDisplayName(nif.getDeviceName());
			// 代表1件登録用のIPアドレス
			String tmpIpAddress = nif.getNicIpAddress();
			if (tmpIpAddress != null && !tmpIpAddress.isEmpty()) {
				// ローカル・ループバック・アドレスより固有アドレス優先.
				if (!(tmpIpAddress.startsWith("127") || tmpIpAddress.equals("::1")
						|| tmpIpAddress.equals("0:0:0:0:0:0:0:1"))) {
					ipAddress = tmpIpAddress;
				}
				if (ipAddress.isEmpty()) {
					ipAddress = tmpIpAddress;
				}
			}
		}
		nodeInfo.setNodeNetworkInterfaceInfo(this.validNodeNifList);

		// NIC情報から取得したIPアドレスを1件代表して登録.
		nodeInfo.setIpmiIpAddress(ipAddress);
		IpAddressInfo ipInfo = new IpAddressInfo();
		try {
			ipInfo = NetworkInterfaceUtil.getIpAddressInfo(ipAddress);
		} catch (HinemosUnknown e) {
			logger.warn(methodName + DELIMITER + e.getMessage(), e);
		}
		switch (ipInfo.getVersion()) {
		case IPV4:
			nodeInfo.setIpAddressVersion(Integer.valueOf(4));
			nodeInfo.setIpAddressV4(ipAddress);
			break;
		case IPV6:
			nodeInfo.setIpAddressVersion(Integer.valueOf(6));
			nodeInfo.setIpAddressV6(ipAddress);
			break;
		default:
			logger.warn(methodName + DELIMITER + "failed to set Ip address in nodeInfo.");
			break;
		}

		return nodeInfo;
	}

	/**
	 * 引数の設定に従ってFacilityIDを採番する.<br>
	 * 
	 * @param autoRegisterSetting
	 *            設定情報、登録済最終連番の登録用に上書きする.
	 * @return 登録用ファシリティID(自動採番)
	 * @throws InvalidSetting
	 *             空き連番がなくて採番不可.
	 */
	private String createFacilityId(AutoRegisterNodeInfo autoRegisterSetting) throws InvalidSetting {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();

		// FacilityIDの先頭文字列(プリフィックス(区切り文字はプリフィックスに含める)).
		String prefix = autoRegisterSetting.getPrefix();

		// 登録済の連番と重複しないかチェックして採番する.
		Long dbLastSerialNum = autoRegisterSetting.getLastSerialNumber();
		if (dbLastSerialNum == null) {
			dbLastSerialNum = Long.valueOf(0);
		}

		Long serialNumber = this.incrementalSerial(dbLastSerialNum, autoRegisterSetting.getOrderNo(),
				autoRegisterSetting.getSourceNetwork());
		int loopMax = HinemosPropertyCommon.auto_register_node_incremental_loop.getIntegerValue();
		String facilityId = null;
		int keta = HinemosPropertyCommon.auto_register_node_seaquential_digits.getIntegerValue();
		if (keta > SEAUENTIAL_DIGITS_MAX) {
			// 連番=Long値=Max20桁なので、超える場合はログ出力して上限値を設定する.
			logger.info(methodName + DELIMITER
					+ String.format(
							" hinemos-property 'auto.register.node.seaquential.digits' is over max value."
									+ " auto.register.node.seaquential.digits=[%d], max value=[%d]",
							keta, SEAUENTIAL_DIGITS_MAX));
			keta = SEAUENTIAL_DIGITS_MAX;
		}
		boolean decided = false;

		for (int i = 0; i < loopMax; i++) {
			// 0パディングしてFacilityIDを採番.
			String paddingNum = FileUtil.paddingZero(serialNumber.longValue(), keta);
			facilityId = prefix + paddingNum;
			NodeInfo registeredInfo = null;

			// DBアクセスして登録済のFacilityIDに重複しているNoがないか確認する.
			try {
				registeredInfo = QueryUtil.getNodePK(facilityId);
			} catch (FacilityNotFound e) {
				decided = true;
				logger.debug(methodName + DELIMITER + String.format("decided facilityID." + " ID=[%s]", facilityId));
				break;
			}

			logger.debug(methodName + DELIMITER
					+ String.format("facilityID is duplicated." + " ID=[%s]", registeredInfo.getFacilityId()));

			// 連番をインクリメント.
			serialNumber = this.incrementalSerial(serialNumber, autoRegisterSetting.getOrderNo(),
					autoRegisterSetting.getSourceNetwork());
		}

		if (!decided) {
			// 空き連番がなくて採番不可.
			String message = String.format(
					"failed to create facility ID, because there are no free number. Please delete unnecessary nodes."
							+ " key(primary)=[%d], source_network=[%s], last_serial_number=[%d]",
					autoRegisterSetting.getOrderNo(), autoRegisterSetting.getSourceNetwork(),
					autoRegisterSetting.getLastSerialNumber());
			logger.warn(methodName + DELIMITER + message);
			this.putInternalLogIp();
			throw new InvalidSetting(message);
		}

		// 最終登録済連番をセットしておく(まだDB登録はしない).
		autoRegisterSetting.setLastSerialNumber(serialNumber);
		return facilityId;
	}

	/**
	 * 連番が最大値に達したら1にリセットする.<br>
	 */
	private Long incrementalSerial(Long lastLocalNum, Integer key, String sourceNetwork) {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();

		Long returnNum;
		synchronized (serialNumLock) {

			// 処理中の他スレッドと連番重複しないようにする.
			Long staticLastSerialNum = serialNumberMap.get(key);
			Long lasNum;
			int threadPool = HinemosPropertyCommon.ws_agent_threadpool_size.getIntegerValue();
			if (staticLastSerialNum != null && staticLastSerialNum.longValue() > lastLocalNum.longValue()) {
				lasNum = staticLastSerialNum;
			} else if (staticLastSerialNum != null && threadPool > staticLastSerialNum.longValue()
					&& (Long.MAX_VALUE - threadPool) < lastLocalNum.longValue()) {
				lasNum = staticLastSerialNum;
			} else {
				lasNum = lastLocalNum;
			}

			// 最大値に達している場合は1にリセットする.
			if (Long.MAX_VALUE == lasNum) {
				logger.info(methodName + DELIMITER + String.format("serial number in facilityID reached maximum value."
						+ " key(priority)=[%d], source_network=[%s]", key, sourceNetwork));

				returnNum = Long.valueOf(1L);
			} else {
				returnNum = Long.valueOf(lasNum.longValue() + 1L);
			}
			serialNumberMap.put(key, returnNum);
		}
		return returnNum;
	}

	/**
	 * Internalエラー出力.<br>
	 * <br>
	 * ユーザー通知用の文言を出しわけしない場合の共通出力処理.<br>
	 * ※warnログの文言でエラー箇所は特定できるようにすること.
	 */
	private void putInternalLogIp() {
		String[] args = { MessageConstant.SOURCE_IP.getMessage(), this.sourceIpAddress.getHostAddress() };
		AplLogger.put(InternalIdCommon.PLT_REP_AREG_SYS_002, args);
	}
}
