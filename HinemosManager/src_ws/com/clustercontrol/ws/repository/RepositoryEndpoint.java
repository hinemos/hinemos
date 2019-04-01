/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.ws.repository;

import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.annotation.Resource;
import javax.xml.ws.WebServiceContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.FunctionConstant;
import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.SystemPrivilegeMode;
import com.clustercontrol.accesscontrol.model.SystemPrivilegeInfo;
import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.fault.FacilityDuplicate;
import com.clustercontrol.fault.FacilityNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.NodeConfigSettingDuplicate;
import com.clustercontrol.fault.NodeConfigSettingNotFound;
import com.clustercontrol.fault.SnmpResponseError;
import com.clustercontrol.fault.UsedFacility;
import com.clustercontrol.repository.bean.AgentStatusInfo;
import com.clustercontrol.repository.bean.FacilityTreeItem;
import com.clustercontrol.repository.bean.NodeInfoDeviceSearch;
import com.clustercontrol.repository.bean.RepositoryTableInfo;
import com.clustercontrol.repository.factory.NodeSearcher;
import com.clustercontrol.repository.model.FacilityInfo;
import com.clustercontrol.repository.model.NodeConfigSettingInfo;
import com.clustercontrol.repository.model.NodeInfo;
import com.clustercontrol.repository.model.ScopeInfo;
import com.clustercontrol.repository.session.NodeConfigSettingControllerBean;
import com.clustercontrol.repository.session.RepositoryControllerBean;
import com.clustercontrol.repository.util.RepositoryUtil;
import com.clustercontrol.util.HinemosTime;
import com.clustercontrol.util.StringBinder;
import com.clustercontrol.ws.util.HttpAuthenticator;

/**
 * リポジトリ操作用のWebAPIエンドポイント
 */
@javax.jws.WebService(targetNamespace = "http://repository.ws.clustercontrol.com")
public class RepositoryEndpoint {
	@Resource
	WebServiceContext wsctx;

	private static Log m_log = LogFactory.getLog( RepositoryEndpoint.class );
	private static Log m_opelog = LogFactory.getLog("HinemosOperation");

	/**
	 * echo(WebサービスAPI疎通用)
	 *
	 * 権限必要なし（ユーザ名チェックのみ実施）
	 *
	 * @param str
	 * @return
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	public String echo(String str) throws InvalidUserPass, InvalidRole, HinemosUnknown {
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		return str + ", " + str;
	}

	/**********************
	 * ファシリティツリーのメソッド群
	 **********************/
	/**
	 * ファシリティツリー（スコープツリー）取得を取得します。
	 * <BR>
	 * 取得したファシリティツリーには割り当てられたノードを含みます。<BR>
	 * このメソッドはクライアントの画面情報を作成するために
	 * 呼び出されます。クライアントのロケールを引数をして必要とします。<BR>
	 * （最上位のスコープという表記をscopeをいう表記を切り替えるため。）
	 *
	 * RepositoryRead権限が必要
	 *
	 * @version 1.0.0
	 * @since 1.0.0
	 *
	 * @param ownerRoleId オーナーロールID
	 * @return FacilityTreeItemの階層オブジェクト
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 */
	public FacilityTreeItem getFacilityTree(String ownerRoleId) throws InvalidUserPass, InvalidRole, HinemosUnknown {
		m_log.debug("getFacilityTree :");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.REPOSITORY, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		m_opelog.debug(HinemosModuleConstant.LOG_PREFIX_REPOSITORY + " Get, Method=getFacilityTree, User="
				+ HttpAuthenticator.getUserAccountString(wsctx));

		return new RepositoryControllerBean().getFacilityTree(ownerRoleId, Locale.getDefault());
	}

	/**
	 * ファシリティツリー（スコープツリー）取得を取得します。
	 * <BR>
	 * 取得したファシリティツリーには割り当てられたノードを含みます。<BR>
	 * このメソッドはクライアントの画面情報を作成するために
	 * 呼び出されます。クライアントのロケールを引数をして必要とします。<BR>
	 *
	 * RepositoryRead権限が必要
	 *
	 * @version 1.0.0
	 * @since 1.0.0
	 *
	 * @param facilityId ファシリティID
	 * @param ownerRoleId オーナーロールID
	 * @return FacilityTreeItemの階層オブジェクト
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 */
	public FacilityTreeItem getExecTargetFacilityTreeByFacilityId(String facilityId, String ownerRoleId) throws InvalidUserPass, InvalidRole, HinemosUnknown {
		m_log.debug("getExecTargetFacilityTreeByFacilityId :");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.REPOSITORY, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		msg.append(", FacilityID=");
		msg.append(facilityId);
		m_opelog.debug(HinemosModuleConstant.LOG_PREFIX_REPOSITORY + " Get, Method=getExecTargetFacilityTreeByFacilityId, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());

		return new RepositoryControllerBean().getExecTargetFacilityTree(facilityId, ownerRoleId, Locale.getDefault());
	}

	/**
	 * ファシリティツリー（ノードツリー）取得を取得します。
	 * <BR>
	 * 取得したファシリティツリーには参照可能なノードが割り当てられています。<BR>
	 * このメソッドはクライアントの画面情報を作成するために
	 * 呼び出されます。クライアントのロケールを引数をして必要とします。<BR>
	 * （最上位のスコープという表記をscopeをいう表記を切り替えるため。）
	 *
	 * RepositoryRead権限が必要
	 *
	 * @param ownerRoleId オーナーロールID
	 * @return FacilityTreeItemの階層オブジェクト
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 */
	public FacilityTreeItem getNodeFacilityTree(String ownerRoleId) throws InvalidUserPass, InvalidRole, HinemosUnknown {
		m_log.debug("getNodeFacilityTree :");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.REPOSITORY, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		m_opelog.debug(HinemosModuleConstant.LOG_PREFIX_REPOSITORY + " Get, Method=getNodeFacilityTree, User="
				+ HttpAuthenticator.getUserAccountString(wsctx));

		return new RepositoryControllerBean().getNodeFacilityTree(Locale.getDefault(), ownerRoleId);
	}

	/**********************
	 * ノードのメソッド群(getter)
	 **********************/

	/**
	 * ノード一覧を取得します。<BR>
	 * リポジトリに登録されているすべてのノードを取得します。<BR>
	 * 戻り値はNodeInfoのArrayListで、NodeInfoには
	 * ノードの下記情報のみ格納されています。
	 * ・ファシリティID
	 * ・ファシリティ名
	 * ・IPアドレスバージョン、IPv4, Ipv6
	 * ・説明
	 * getNodeFacilityIdListを利用すること。（getNodeと組み合わせて利用する。）
	 *
	 * RepositoryRead権限が必要
	 *
	 * @version 1.0.0
	 * @since 1.0.0
	 *
	 * @return NodeInfoの配列
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 */
	public ArrayList<NodeInfo> getNodeListAll() throws InvalidUserPass, InvalidRole, HinemosUnknown {
		m_log.debug("getNodeListAll : ");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.REPOSITORY, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		m_opelog.debug(HinemosModuleConstant.LOG_PREFIX_REPOSITORY + " Get, Method=getNodeListAll, User="
				+ HttpAuthenticator.getUserAccountString(wsctx));

		return new RepositoryControllerBean().getNodeList();
	}
	/**
	 * ノード一覧を取得します。<BR>
	 *
	 * クライアントなどで検索した場合に呼ばれ、該当するノード一覧を取得します。<BR>
	 * 引数はNodeInfoであり、"ファシリティID"、"ファシリティ名"、"説明"、
	 * "IPアドレス"、"OS名"、"OSリリース"、"管理者"、"連絡先"が１つ以上含まれており、
	 * その条件を元に該当するノードを戻り値とします。<BR>
	 * 戻り値はNodeInfoのArrayListで、NodeInfoには
	 * ノードの"ファシリティID"、"ファシリティ名"、"説明"のみ格納されています。<BR>
	 *
	 * RepositoryRead権限が必要
	 *
	 * @param property　検索条件のプロパティ
	 * @return NodeInfoの配列
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 */
	public ArrayList<NodeInfo> getFilterNodeList(NodeInfo property) throws InvalidUserPass, InvalidRole, HinemosUnknown {
		m_log.debug("getFilterNodeList : nodeInfo=" + property);
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.REPOSITORY, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		if (property != null) {
			StringBuffer msg = new StringBuffer();
			msg.append(", FacilityID=");
			msg.append(property.getFacilityId());
			m_opelog.debug(HinemosModuleConstant.LOG_PREFIX_REPOSITORY + " Get, Method=getFilterNodeList, User="
					+ HttpAuthenticator.getUserAccountString(wsctx)
					+ msg.toString());
		}

		return new RepositoryControllerBean().getFilterNodeList(property);
	}

	/**
	 *
	 * 監視・ジョブ等の処理を実行する対象となる、ファシリティIDのリストを取得します。
	 * 引数で指定されたファシリティIDが、ノードかスコープによって、以下のようなリストを取得します。
	 *
	 * ノードの場合
	 *   引数で指定されたfacilityIdが格納されたArrauList
	 *   ただし、管理対象（有効/無効フラグが真）の場合のみ
	 *
	 * スコープの場合
	 *   配下に含まれるノードのファシリティIDが格納されたArrayList
	 *   ただし、管理対象（有効/無効フラグが真）のみ
	 *
	 * RepositoryRead権限が必要
	 *
	 * @param facilityId 処理を実行する対象となるファシリティID
	 * @param ownerRoleId オーナーロールID
	 * @return 有効なノードのリスト（有効なノードがひとつも含まれない場合は空のリスト）
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 */
	public ArrayList<String> getExecTargetFacilityIdList(String facilityId, String ownerRoleId) throws InvalidUserPass, InvalidRole, HinemosUnknown {
		m_log.debug("getExecTargetFacilityIdList : facilityId=" + facilityId + ", ownerRoleId" + ownerRoleId);
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.REPOSITORY, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		msg.append(", FacilityID=");
		msg.append(facilityId);
		m_opelog.debug(HinemosModuleConstant.LOG_PREFIX_REPOSITORY + " Get, Method=getExecTargetFacilityIdList, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());

		return new RepositoryControllerBean().getExecTargetFacilityIdList(facilityId, ownerRoleId);
	}


	/**
	 * ノードの詳細プロパティを取得します。<BR>
	 *
	 * faciliyIDで指定されるノードのプロパティを取得します。<BR>
	 * 以下の詳細情報を含む
	 * ・OS情報
	 * ・汎用デバイス情報
	 * ・CPU情報
	 * ・メモリ情報
	 * ・NIC情報
	 * ・ディスク情報
	 * ・ファイルシステム情報
	 * ・ホスト名情報
	 * ・備考情報
	 * ・ノード変数情報
	 *
	 * RepositoryRead権限が必要
	 *
	 * @param facilityId ファシリティID
	 * @return ノード詳細プロパティ
	 * @throws FacilityNotFound
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 */
	public NodeInfo getNode(String facilityId) throws FacilityNotFound, InvalidUserPass, InvalidRole, HinemosUnknown {
		m_log.debug("getNode : facilityId=" + facilityId);
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.REPOSITORY, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		msg.append(", FacilityID=");
		msg.append(facilityId);
		m_opelog.debug(HinemosModuleConstant.LOG_PREFIX_REPOSITORY + " Get, Method=getNode, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());

		return new RepositoryControllerBean().getNode(facilityId);
	}


	/**
	 * ノードの詳細プロパティを取得します。<BR>
	 *
	 * faciliyIDで指定されるノードのプロパティを取得します。<BR>
	 *
	 * RepositoryRead権限が必要
	 *
	 * @param facilityId ファシリティID
	 * @return ノード詳細プロパティ
	 * @throws FacilityNotFound
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 */
	public NodeInfo getNodeFull(String facilityId) throws FacilityNotFound, InvalidUserPass, InvalidRole, HinemosUnknown {
		m_log.debug("getNodeFull : facilityId=" + facilityId);
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.REPOSITORY, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		msg.append(", FacilityID=");
		msg.append(facilityId);
		m_opelog.debug(HinemosModuleConstant.LOG_PREFIX_REPOSITORY + " Get, Method=getNodeFull, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());

		return new RepositoryControllerBean().getNodeFull(facilityId);
	}


	/**
	 * ノードの詳細プロパティを取得します。<BR>
	 *
	 * 対象日時時点のノードのプロパティを取得します。<BR>
	 *
	 * RepositoryRead権限が必要
	 *
	 * @param facilityId ファシリティID
	 * @param targetDatetime 対象日時
	 * @param nodeFilterInfo 構成情報検索条件
	 * @return ノード詳細プロパティ
	 * @throws FacilityNotFound
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 */
	public NodeInfo getNodeFullByTargetDatetime(String facilityId, Long targetDatetime, NodeInfo nodeFilterInfo) throws FacilityNotFound, InvalidUserPass, InvalidRole, HinemosUnknown {
		m_log.debug("getNodeFullByTargetDatetime : facilityId=" + facilityId);
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.REPOSITORY, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		msg.append(", FacilityID=");
		msg.append(facilityId);
		msg.append(", TargetDatetime=");
		if (targetDatetime == null) {
			msg.append("null");
		} else {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.US);
			sdf.setTimeZone(HinemosTime.getTimeZone());
			msg.append(sdf.format(new Date(targetDatetime)));
		}
		m_opelog.debug(HinemosModuleConstant.LOG_PREFIX_REPOSITORY + " Get, Method=getNodeFullByTargetDatetime, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());

		return new RepositoryControllerBean().getNodeFull(facilityId, targetDatetime, nodeFilterInfo);
	}


	/**
	 * ファシリティパスを取得します。<BR>
	 *
	 * 第一引数がノードの場合は、パスではなく、ファシリティ名。<BR>
	 * 第一引数がスコープの場合は、第二引数との相対的なファシリティパスを取得します。<BR>
	 * (例　○○スコープ>××システム>DBサーバ)<BR>
	 *
	 * RepositoryRead権限が必要
	 *
	 * @param facilityId ファシリティID
	 * @param parentFacilityId 上位のファシリティID
	 * @return String ファシリティパス
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 */
	public String getFacilityPath(String facilityId, String parentFacilityId) throws InvalidUserPass, InvalidRole, HinemosUnknown {
		m_log.debug("getFacilityPath : facilityId=" + facilityId +
				", parentFacilityId=" + parentFacilityId);
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.REPOSITORY, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		msg.append(", FacilityID=");
		msg.append(facilityId);
		msg.append(", ParentFacilityID=");
		msg.append(parentFacilityId);
		m_opelog.debug(HinemosModuleConstant.LOG_PREFIX_REPOSITORY + " Get, Method=getFacilityPath, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());

		return new RepositoryControllerBean().getFacilityPath(facilityId, parentFacilityId);
	}


	/**
	 * SNMPを利用してノードの情報を取得します。<BR>
	 *
	 *
	 * クライアントからSNMPで検出を行った際に呼び出されるメソッドです。<BR>
	 * SNMPポーリングにより、ノード詳細プロパティをセットし、クライアントに返す。
	 * 戻り値はNodeInfo
	 *
	 * RepositoryRead権限が必要
	 *
	 * @param pollingData ポーリング対象のIPアドレス、コミュニティ名、バージョン、ポート、ファシリティID、セキュリティレベル、ユーザー名、認証パスワード、暗号化パスワード、認証プロトコル、暗号化プロトコル
	 * @param locale クライアントのロケール
	 * @return ノード情報（更新情報）
	 * @throws HinemosUnknown
	 * @throws FacilityNotFound
	 * @throws UnknownHostException
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 */
	public NodeInfoDeviceSearch getNodePropertyBySNMP(String ipAddress,
			int port, String community, int version, String facilityID,
			String securityLevel, String user, String authPass,
			String privPass, String authProtocol, String privProtocol)
			throws HinemosUnknown, InvalidUserPass, InvalidRole,
			SnmpResponseError {
		m_log.debug("getNodePropertyBySNMP : ipAddress=" + ipAddress
				+ ", port=" + port + ", community=" + community + ", version="
				+ version + ", securityLevel=" + securityLevel + ", user=" + user + ", authPassword=" + authPass
				+ ", privPassword=" + privPass + ", authProtocol=" + authProtocol + ", privProtocol=" + privProtocol);
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(
				FunctionConstant.REPOSITORY, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		msg.append(", IPAddress=");
		msg.append(ipAddress);
		msg.append(", Port=");
		msg.append(port);
		msg.append(", Community=");
		msg.append(community);
		msg.append(", Version=");
		msg.append(version);
		msg.append(", securityLevel=");
		msg.append(securityLevel);
		msg.append(", user=");
		msg.append(user);
		msg.append(", authPassword=");
		msg.append(authPass);
		msg.append(", privPassword=");
		msg.append(privPass);
		msg.append(", authProtocol=");
		msg.append(authProtocol);
		msg.append(", privProtocol=");
		msg.append(privProtocol);
		m_opelog.debug(HinemosModuleConstant.LOG_PREFIX_REPOSITORY + " Get, Method=getNodePropertyBySNMP, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());

		return new RepositoryControllerBean().getNodePropertyBySNMP(ipAddress,
				port, community, version, facilityID, securityLevel, user,
				authPass, privPass, authProtocol, privProtocol);
	}


	/**********************
	 * ノードのメソッド群(getter以外)
	 **********************/
	/**
	 * ノードを新規に追加します。<BR>
	 * またこのメソッドで組み込みスコープである"登録済みノード"スコープにも
	 * 割り当てが行われます。
	 *
	 * RepositoryAdd権限が必要
	 *
	 * @param nodeinfo 追加するノード情報のプロパティ
	 * @throws FacilityDuplicate
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 */
	public void addNode(NodeInfo nodeInfo) throws FacilityDuplicate, InvalidUserPass, InvalidRole, HinemosUnknown, InvalidSetting {
		m_log.debug("addNode : nodeInfo=" + nodeInfo);
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.REPOSITORY, SystemPrivilegeMode.ADD));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		if(nodeInfo != null){
			msg.append(", FacilityID=");
			msg.append(nodeInfo.getFacilityId());
		}

		try {
			long startTime = HinemosTime.currentTimeMillis();
			new RepositoryControllerBean().addNode(nodeInfo);
			m_log.info(String.format("addNode: %dms", HinemosTime.currentTimeMillis() - startTime));
		} catch (Exception e) {
			m_opelog.warn(HinemosModuleConstant.LOG_PREFIX_REPOSITORY + " Add Node Failed, Method=addNode, User="
					+ HttpAuthenticator.getUserAccountString(wsctx)
					+ msg.toString());
			throw e;
		}
		m_opelog.info(HinemosModuleConstant.LOG_PREFIX_REPOSITORY + " Add Node, Method=addNode, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());
	}


	/**
	 * ノードを変更します。<BR>
	 * 引数のpropertyには変更する属性のみを設定してください。<BR>
	 *
	 * RepositoryWrite権限が必要
	 *
	 * @version 2.0.0
	 * @since 1.0.0
	 *
	 * @param property　変更するノード情報のプロパティ
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 * @throws InvalidSetting
	 */
	public void modifyNode(NodeInfo property) throws InvalidUserPass, InvalidRole, HinemosUnknown, InvalidSetting {
		m_log.debug("modifyNode : nodeInfo=" + property);
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.REPOSITORY, SystemPrivilegeMode.MODIFY));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		if(property != null){
			msg.append(", FacilityID=");
			msg.append(property.getFacilityId());
		}

		try {
			new RepositoryControllerBean().modifyNode(property);
		} catch (Exception e) {
			m_opelog.warn(HinemosModuleConstant.LOG_PREFIX_REPOSITORY + " Change Node Failed, Method=modifyNode, User="
					+ HttpAuthenticator.getUserAccountString(wsctx)
					+ msg.toString());
			throw e;
		}
		m_opelog.info(HinemosModuleConstant.LOG_PREFIX_REPOSITORY + " Change Node, Method=modifyNode, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());
	}


	/**
	 * ノード情報を削除します。<BR>
	 *
	 * faciityIDで指定されたノードをリポジトリから削除します。
	 *
	 * RepositoryWrite権限が必要
	 *
	 * @version 1.0.0
	 * @since 1.0.0
	 *
	 * @param facilityIds ファシリティIDの配列
	 * @throws UsedFacility
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 */
	public void deleteNode(String[] facilityIds) throws UsedFacility, InvalidUserPass, InvalidRole, HinemosUnknown {
		m_log.debug("deleteNode : facilityId=" + Arrays.toString(facilityIds));
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.REPOSITORY, SystemPrivilegeMode.MODIFY));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);
		StringBuffer msg = new StringBuffer();

		try {
			for (String facilityId : facilityIds) {
				// 認証済み操作ログ
				msg.append(", FacilityID=");
				msg.append(facilityId);
			}
			if( facilityIds.length > 0 ){//対象があれば削除を行う
				new RepositoryControllerBean().deleteNode(facilityIds);
			}else{ //対象がなければその旨をログに設定する。
				msg.append(", no node deleted");
			}
		} catch (Exception e) {
			m_opelog.warn(HinemosModuleConstant.LOG_PREFIX_REPOSITORY + " Delete Node Failed, Method=deleteNode, User="
					+ HttpAuthenticator.getUserAccountString(wsctx)
					+ msg.toString());
			throw e;
		}
		m_opelog.info(HinemosModuleConstant.LOG_PREFIX_REPOSITORY + " Delete Node, Method=deleteNode, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());
	}

	/**********************
	 * スコープのメソッド群
	 **********************/
	/**
	 * ファシリティ一覧を取得します。<BR>
	 * あるスコープを指定してその直下にあるファシリティを取得します。<BR>
	 * このメソッドは引数としてそのスコープのファシリティIDを要求します。<BR>
	 * 戻り値はArrayListで中のScopeInfoには子の
	 * "ファシリティID"、"ファシリティ名"、"説明"のみ格納されています。<BR>
	 *
	 * RepositoryWrite権限が必要
	 *
	 * @version 1.0.0
	 * @since 1.0.0
	 *
	 * @param parentFacilityId　スコープのファシリティID
	 * @return ScopeInfoの配列
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 */
	public ArrayList<FacilityInfo> getFacilityList(String parentFacilityId) throws InvalidUserPass, InvalidRole, HinemosUnknown {
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.REPOSITORY, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		msg.append(", ParentFacilityID=");
		msg.append(parentFacilityId);
		m_opelog.debug(HinemosModuleConstant.LOG_PREFIX_REPOSITORY + " Get, Method=getScopeList, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());

		return new RepositoryControllerBean().getFacilityList(parentFacilityId);
	}

	/**
	 * スコープ用プロパティ情報を取得します。<BR>
	 *
	 * RepositoryRead権限が必要
	 *
	 * @param facilityId ファシリティID
	 * @param locale クライアントのロケール
	 * @return スコープのプロパティ情報（ファシリティID、ファシリティ名、説明）
	 * @throws HinemosUnknown
	 * @throws FacilityNotFound
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 */
	public ScopeInfo getScope(String facilityId) throws FacilityNotFound, HinemosUnknown, InvalidUserPass, InvalidRole {
		m_log.debug("getScope : facilityId=" + facilityId);
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.REPOSITORY, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		msg.append(", FacilityID=");
		msg.append(facilityId);
		m_opelog.debug(HinemosModuleConstant.LOG_PREFIX_REPOSITORY + " Get, Method=getScope, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());

		return new RepositoryControllerBean().getScope(facilityId);
	}

	/**
	 * スコープを新規に追加します。<BR>
	 *
	 * parentFacilityIdで指定されるスコープの下にpropertyで指定されるスコープを
	 * 追加します。<BR>
	 * 引数propertyには、"ファシリティID"、"ファシリティ名"、"説明"（任意）を含める必要があります。
	 *
	 * RepositoryAdd権限が必要
	 *
	 * @param parentFacilityId
	 * @param property
	 * @throws FacilityDuplicate
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 */
	public void addScope(String parentFacilityId, ScopeInfo property) throws FacilityDuplicate, InvalidUserPass, InvalidRole, HinemosUnknown, InvalidSetting {
		m_log.debug("addScope : parentFacilityId=" + parentFacilityId +
				", scopeInfo=" + property);
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.REPOSITORY, SystemPrivilegeMode.ADD));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		msg.append(", ParentFacilityID=");
		msg.append(parentFacilityId);
		if(property != null){
			msg.append(", FacilityID=");
			msg.append(property.getFacilityId());
		}

		try {
			new RepositoryControllerBean().addScope(parentFacilityId, property);
		} catch (Exception e) {
			m_opelog.warn(HinemosModuleConstant.LOG_PREFIX_REPOSITORY + " Add Scope Failed, Method=addScope, User="
					+ HttpAuthenticator.getUserAccountString(wsctx)
					+ msg.toString());
			throw e;
		}
		m_opelog.info(HinemosModuleConstant.LOG_PREFIX_REPOSITORY + " Add Scope, Method=addScope, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());
	}

	/**
	 * スコープの情報を変更します。<BR>
	 *
	 * 引数propertyで指定した内容でスコープ情報を更新します。<BR>
	 * 引数propertyには、"ファシリティID"、"ファシリティ名"、"説明"（任意）を含める必要があります。
	 * propertyに含まれるファシリティIDに対応するスコープの情報が変更されます。<BR>
	 *
	 * RepositoryWrite権限が必要
	 *
	 * @param property
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 */
	public void modifyScope(ScopeInfo property) throws InvalidUserPass, InvalidRole, HinemosUnknown, InvalidSetting {
		m_log.debug("modifyScope : scopeInfo=" + property);
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.REPOSITORY, SystemPrivilegeMode.MODIFY));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		if(property != null){
			msg.append(", FacilityID=");
			msg.append(property.getFacilityId());
		}
		try {
			new RepositoryControllerBean().modifyScope(property);
		} catch (Exception e) {
			m_opelog.warn(HinemosModuleConstant.LOG_PREFIX_REPOSITORY + " Change Scope Failed, Method=modifyScope, User="
					+ HttpAuthenticator.getUserAccountString(wsctx)
					+ msg.toString());
			throw e;
		}
		m_opelog.info(HinemosModuleConstant.LOG_PREFIX_REPOSITORY + " Change Scope, Method=modifyScope, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());
	}

	/**
	 * スコープ情報を削除します。<BR>
	 *
	 * faciityIDで指定されたスコープをリポジトリから削除します。
	 *
	 * RepositoryWrite権限が必要
	 *
	 * @param facilityIds ファシリティIDの配列
	 * @throws UsedFacility
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 */
	public void deleteScope(String[] facilityIds) throws UsedFacility, InvalidUserPass, InvalidRole, HinemosUnknown {
		m_log.debug("deleteScope : facilityId=" + Arrays.toString(facilityIds));
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.REPOSITORY, SystemPrivilegeMode.MODIFY));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		msg.append(", FacilityID=");
		msg.append(Arrays.toString(facilityIds));

		try {
			new RepositoryControllerBean().deleteScope(facilityIds);
		} catch (Exception e) {
			m_opelog.warn(HinemosModuleConstant.LOG_PREFIX_REPOSITORY + " Delete Scope Failed, Method=deleteScope, User="
					+ HttpAuthenticator.getUserAccountString(wsctx)
					+ msg.toString());
			throw e;
		}
		m_opelog.info(HinemosModuleConstant.LOG_PREFIX_REPOSITORY + " Delete Scope, Method=deleteScope, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());
	}



	/**********************
	 * ノード割り当てのメソッド群
	 **********************/
	/**
	 * 割当ノード一覧を取得します。<BR>
	 *
	 * あるファシリティIDの配下または直下のノード一覧を取得します。<BR>
	 * 引数がノードの場合は、そのノードのファシリティIDを返す。<BR>
	 * このメソッドでは、引数levelで直下または配下を制御します。<BR>
	 * 戻り値はNodeInfoのArrayListで、NodeInfoには
	 * ノードの"ファシリティID"、"ファシリティ名"、"説明"のみ格納されています。<BR>
	 *
	 * RepositoryRead権限が必要
	 *
	 * @param parentFacilityId
	 * @param level　取得レベル 0:ALL(配下) 1:ONE_LEVEL（直下）
	 * @return NodeInfoの配列
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 */
	public ArrayList<NodeInfo> getNodeList(String parentFacilityId, int level) throws InvalidUserPass, InvalidRole, HinemosUnknown {
		m_log.debug("getNodeList : parentFacilityId=" + parentFacilityId + ", level=" + level);
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.REPOSITORY, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		msg.append(", ParentFacilityID=");
		msg.append(parentFacilityId);
		msg.append(", Level=");
		msg.append(level);
		m_opelog.debug(HinemosModuleConstant.LOG_PREFIX_REPOSITORY + " Get, Method=getNodeList, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());

		return new RepositoryControllerBean().getNodeList(parentFacilityId, level);
	}


	/**
	 * 割当スコープ一覧を取得します。<BR>
	 * 割り当てスコープ一覧とは、あるノードが属しているスコープすべてを
	 * 一覧表示したものです。
	 * クライアントの割り当てスコープビューの表示データとなります。
	 * 戻り値はArrayListのArrayListで中のArrayListには"スコープ"が最上位からの
	 * スコープパス表記で（Stringで）格納されています。
	 * 外のArrayListには、そのレコードが順に格納されています。
	 *
	 * RepositoryRead権限が必要
	 *
	 * @version 1.0.0
	 * @since 1.0.0
	 *
	 * @param facilityId ノードのファシリティID
	 * @return Stringの配列
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	public ArrayList<String> getNodeScopeList(String facilityId) throws InvalidUserPass, InvalidRole, HinemosUnknown {
		m_log.debug("getNodeScopeList : facilityId=" + facilityId);
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.REPOSITORY, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		msg.append(", FacilityID=");
		msg.append(facilityId);
		m_opelog.debug(HinemosModuleConstant.LOG_PREFIX_REPOSITORY + " Get, Method=getNodeScopeList, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());

		return new RepositoryControllerBean().getNodeScopeList(facilityId);
	}

	/**
	 * ファシリティIDリストを取得します。<BR>
	 *
	 * 引数に指定した親ファシリティIDの配下または直下のファシリティ（スコープ、ノード）の
	 * リストを取得します。<BR>
	 * 戻り値は ファシリティID（String）のArrayList
	 *
	 * RepositoryRead権限が必要
	 *
	 * @param parentFacilityId ファシリティID
	 * @param level　取得レベル 0:ALL(配下) 1:ONE_LEVEL（直下）
	 * @return ファシリティIDの配列
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 */
	public ArrayList<String> getFacilityIdList(String parentFacilityId, int level) throws InvalidUserPass, InvalidRole, HinemosUnknown {
		m_log.debug("getFacilityIdList : parentFacilityId=" + parentFacilityId + ", level=" + level);
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.REPOSITORY, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		msg.append(", ParentFacilityID=");
		msg.append(parentFacilityId);
		msg.append(", Level=");
		msg.append(level);
		m_opelog.debug(HinemosModuleConstant.LOG_PREFIX_REPOSITORY + " Get, Method=getFacilityIdList, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());

		return new RepositoryControllerBean().getFacilityIdList(parentFacilityId, level);
	}


	/**
	 * ノードのファシリティIDリストを取得します。<BR>
	 *
	 * 引数に指定した親ファシリティIDの配下または直下のファシリティ（ノード）の
	 * リストを取得します。<BR>
	 * 戻り値は ファシリティID（String）のArrayList
	 *
	 * RepositoryRead権限が必要
	 *
	 * @param parentFacilityId ファシリティID
	 * @param ownerRoleId オーナーロールID
	 * @param level  取得レベル 0:ALL(配下) 1:ONE_LEVEL（直下）
	 * @return ファシリティIDの配列
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 */
	public ArrayList<String> getNodeFacilityIdList(String parentFacilityId, String ownerRoleId, int level) throws InvalidUserPass, InvalidRole, HinemosUnknown {
		m_log.debug("getNodeFacilityIdList : parentFacilityId=" + parentFacilityId + ", ownerRoleId=" + ownerRoleId + ", level=" + level);
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.REPOSITORY, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		msg.append(", ParentFacilityID=");
		msg.append(parentFacilityId);
		msg.append(", Level=");
		msg.append(level);
		m_opelog.debug(HinemosModuleConstant.LOG_PREFIX_REPOSITORY + " Get, Method=getNodeFacilityIdList, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());

		return new RepositoryControllerBean().getNodeFacilityIdList(parentFacilityId, ownerRoleId, level);
	}

	/**
	 * スコープへのノードの割り当てを行います。<BR>
	 *
	 * parentFacilityIdで指定されるスコープにfacilityIdsで指定されるノード群を
	 * 割り当てます。
	 *
	 * RepositoryWrite権限が必要
	 *
	 * @param parentFacilityId　ノードを割り当てるスコープ
	 * @param facilityIds 割り当てさせるノード(群)
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 * @throws InvalidSetting
	 */
	public void assignNodeScope(String parentFacilityId, String[] facilityIds) throws InvalidUserPass, InvalidRole, HinemosUnknown,InvalidSetting {
		m_log.debug("assignNodeScope : parentFacilityId=" + parentFacilityId + ", facilityIds=" + Arrays.toString(facilityIds));
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.REPOSITORY, SystemPrivilegeMode.MODIFY));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		msg.append(", ParentFacilityID=");
		msg.append(parentFacilityId);
		if (facilityIds != null) {
			msg.append(", FacilityID=");
			msg.append(Arrays.toString(facilityIds));
		}

		try {
			new RepositoryControllerBean().assignNodeScope(parentFacilityId, facilityIds);
		} catch (Exception e) {
			m_opelog.warn(HinemosModuleConstant.LOG_PREFIX_REPOSITORY + " Assign Node Failed, Method=assignNodeScope, User="
					+ HttpAuthenticator.getUserAccountString(wsctx)
					+ msg.toString());
			throw e;
		}
		m_opelog.info(HinemosModuleConstant.LOG_PREFIX_REPOSITORY + " Assign Node, Method=assignNodeScope, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());
	}

	/**
	 * ノードをスコープから削除します。（割り当てを解除します。）<BR>
	 * parentFacilityIdで指定されるスコープからfacilityIdsで指定されるノード群を
	 * 削除（割り当て解除）します。
	 *
	 * RepositoryWrite権限が必要
	 *
	 * @param parentFacilityId ノードを取り除くスコープ
	 * @param facilityIds 取り除かれるノード（群）
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 * @throws InvalidSetting
	 */
	public void releaseNodeScope(String parentFacilityId, String[] facilityIds) throws InvalidUserPass, InvalidRole, HinemosUnknown,InvalidSetting {
		m_log.debug("releaseNodeScope : parentFacilityId=" + parentFacilityId + ", facilityIds=" + Arrays.toString(facilityIds));
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.REPOSITORY, SystemPrivilegeMode.MODIFY));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		msg.append(", ParentFacilityID=");
		msg.append(parentFacilityId);
		if (facilityIds != null) {
			msg.append(", FacilityID=");
			msg.append(Arrays.toString(facilityIds));
		}

		try {
			new RepositoryControllerBean().releaseNodeScope(parentFacilityId, facilityIds);
		} catch (Exception e) {
			m_opelog.warn(HinemosModuleConstant.LOG_PREFIX_REPOSITORY + " Release Node Failed, Method=releaseNodeScope, User="
					+ HttpAuthenticator.getUserAccountString(wsctx)
					+ msg.toString());
			throw e;
		}
		m_opelog.info(HinemosModuleConstant.LOG_PREFIX_REPOSITORY + " Release Node, Method=releaseNodeScope, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());
	}


	/**********************
	 * その他のメソッド群
	 **********************/
	/**
	 * ファシリティがノードかどうかをチェックします。<BR>
	 *
	 * ファシリティIDに対応するものがノードかチェックし、結果をbooleanで返します。
	 *
	 * RepositoryRead権限が必要
	 *
	 * @param facilityId　ファシリティID
	 * @return true：ノード　false:ノードではない（スコープ）
	 * @throws FacilityNotFound
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 */
	public boolean isNode(String facilityId) throws FacilityNotFound, InvalidUserPass, InvalidRole, HinemosUnknown {
		m_log.debug("isNode : facilityId=" + facilityId);
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.REPOSITORY, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		msg.append(", FacilityID=");
		msg.append(facilityId);
		m_opelog.debug(HinemosModuleConstant.LOG_PREFIX_REPOSITORY + " Check, Method=isNode, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());

		return new RepositoryControllerBean().isNode(facilityId);
	}

	/**
	 * ノード作成変更時に、利用可能プラットフォームを表示するためのメソッド。
	 *
	 * RepositoryRead権限が必要
	 *
	 * @return ArrayList
	 * @throws HinemosUnknown
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 */
	public ArrayList<RepositoryTableInfo> getPlatformList() throws HinemosUnknown, InvalidUserPass, InvalidRole {
		m_log.debug("getPlatformList : ");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.REPOSITORY, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		m_opelog.debug(HinemosModuleConstant.LOG_PREFIX_REPOSITORY + " Get, Method=getPlatformList, User="
				+ HttpAuthenticator.getUserAccountString(wsctx));

		return new RepositoryControllerBean().getPlatformList();
	}

	/**
	 * ノード作成変更時に、利用可能な仮想化ソリューションを表示するためのメソッド。
	 *
	 * RepositoryRead権限が必要
	 *
	 * @version 3.2.0
	 * @since 3.2.0
	 * @return ArrayList
	 * @throws HinemosUnknown
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 */
	public ArrayList<RepositoryTableInfo> getCollectorSubPlatformTableInfoList() throws HinemosUnknown, InvalidUserPass, InvalidRole {
		m_log.debug("getCollectorSubPlatformMstList : ");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.REPOSITORY, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		m_opelog.debug(HinemosModuleConstant.LOG_PREFIX_REPOSITORY + " Get, Method=getCollectorSubPlatformMstList, User="
				+ HttpAuthenticator.getUserAccountString(wsctx));

		return new RepositoryControllerBean().getCollectorSubPlatformTableInfoList();
	}

	/**
	 * ノードの「管理対象」フラグを変更します。
	 *
	 * RepositoryWrite権限が必要
	 *
	 * @return ArrayList
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 * @throws InvalidSetting
	 * @throws FacilityNotFound
	 */
	public void setValid(String facilityId, boolean validFlag) throws InvalidUserPass, InvalidRole, HinemosUnknown, FacilityNotFound, InvalidSetting {
		m_log.debug("setValid : ");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.REPOSITORY, SystemPrivilegeMode.MODIFY));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		msg.append(", FacilityID=");
		msg.append(facilityId);
		msg.append(", ValidFlag=");
		msg.append(validFlag);

		try {
			NodeInfo info = new RepositoryControllerBean().getNodeFull(facilityId);
			info.setValid(validFlag);
			new RepositoryControllerBean().modifyNode(info);
		} catch (Exception e) {
			m_opelog.warn(HinemosModuleConstant.LOG_PREFIX_REPOSITORY + " Change Valid Failed, Method=setValid, User="
					+ HttpAuthenticator.getUserAccountString(wsctx)
					+ msg.toString());
			throw e;
		}
		m_opelog.info(HinemosModuleConstant.LOG_PREFIX_REPOSITORY + " Change Valid, Method=setValid, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());
	}

	/**
	 * ノード作成変更時に、利用可能な仮想化プロトコルを表示するためのメソッド。
	 *
	 * RepositoryRead権限が必要
	 *
	 * @return ArrayList
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 */
	public ArrayList<String> getVmProtocolMstList() throws InvalidUserPass, InvalidRole, HinemosUnknown {
		m_log.debug("getVmProtocolMstList : ");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.REPOSITORY, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		m_opelog.debug(HinemosModuleConstant.LOG_PREFIX_REPOSITORY + " Get, Method=getVmProtocolMstList, User="
				+ HttpAuthenticator.getUserAccountString(wsctx));

		return new RepositoryControllerBean().getVmProtocolMstList();
	}
	/**
	 * リポジトリの最終更新時刻を取得
	 *
	 * RepositoryRead権限が必要
	 *
	 * @return
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 * @throws HinemosUnknown
	 * @throws InvalidRoleException
	 * @throws InvalidUserPassException
	 */
	public Long getLastUpdate() throws InvalidUserPass, InvalidRole, HinemosUnknown {
		m_log.debug("getLastUpdate : ");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.REPOSITORY, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		m_opelog.debug(HinemosModuleConstant.LOG_PREFIX_REPOSITORY + " Get, Method=getLastUpdate, User="
				+ HttpAuthenticator.getUserAccountString(wsctx));
		
		return new RepositoryControllerBean().getLastUpdate().getTime();
	}

	/**
	 * エージェントの状態を返します。<BR>
	 *
	 * RepositoryRead権限が必要
	 *
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 */
	public List<AgentStatusInfo> getAgentStatusList() throws InvalidUserPass, InvalidRole, HinemosUnknown {
		m_log.debug("getAgentStatusList : ");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.REPOSITORY, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		m_opelog.debug(HinemosModuleConstant.LOG_PREFIX_REPOSITORY + " Get, Method=getAgentStatusList, User="
				+ HttpAuthenticator.getUserAccountString(wsctx));

		return new RepositoryControllerBean().getAgentStatusList();
	}

	/**
	 * エージェントを再起動 or アップデートします。
	 *
	 * RepositoryExecute権限が必要
	 *
	 * @param facilityId　ファシリティID
	 * @param agentCommand エージェントに実行するコマンド。
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 * @see com.clustercontrol.repository.bean.AgentCommandConstant
	 */
	public void restartAgent(ArrayList<String> facilityIdList, int agentCommand) throws InvalidUserPass, InvalidRole, HinemosUnknown {
		m_log.debug("getAgentStatusList : ");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.REPOSITORY, SystemPrivilegeMode.EXEC));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		msg.append(", FacilityID=");
		for (String facilityId : facilityIdList) {
			msg.append(facilityId);
			msg.append(",");
		}
		msg.append(" AgentCommand=");
		msg.append(agentCommand);

		try {
			new RepositoryControllerBean().restartAgent(facilityIdList, agentCommand);
		} catch (Exception e) {
			m_opelog.warn(HinemosModuleConstant.LOG_PREFIX_REPOSITORY + " Restart Agent Failed, Method=restartAgent, User="
					+ HttpAuthenticator.getUserAccountString(wsctx)
					+ msg.toString());
			throw e;
		}
		m_opelog.info(HinemosModuleConstant.LOG_PREFIX_REPOSITORY + " Restart Agent, Method=restartAgent, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());
	}

	/**
	 * 指定された文字列内に存在するノード変数を置換した文字列を返します。
	 *
	 * @param facilityId ファシリティID
	 * @param replaceObject 置換対象
	 * @return ret 置換後の文字列
	 * @throws HinemosUnkown
	 */
	public String replaceNodeVariable(String facilityId, String replaceObject) throws InvalidUserPass, InvalidRole, HinemosUnknown {
		m_log.debug("replaceNodeVariable() before : " + replaceObject);

		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.REPOSITORY, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		String ret = replaceObject;

		// 対象となっているノード変数を取得
		Map<String, NodeInfo> nodeInfo = new HashMap<String, NodeInfo>();
		try {
			synchronized (this) {
				nodeInfo.put(facilityId, new RepositoryControllerBean().getNode(facilityId));
			}
		} catch (FacilityNotFound e) {
			// 何もしない
		}

		if (nodeInfo != null && nodeInfo.containsKey(facilityId)) {
			int maxReplaceWord = HinemosPropertyCommon.replace_param_max.getIntegerValue().intValue();
			ArrayList<String> inKeyList = StringBinder.getKeyList(replaceObject, maxReplaceWord);
			Map<String, String> nodeParameter = RepositoryUtil.createNodeParameter(nodeInfo.get(facilityId), inKeyList);
			StringBinder strbinder = new StringBinder(nodeParameter);
			ret = strbinder.bindParam(replaceObject);

			m_log.debug("replaceNodeVariable() after : " + ret);
		}

		return ret;
	}

	/**
	 * SNMPを利用してノードの情報を検索します。<BR>
	 *
	 *
	 * クライアントからノードサーチを行った際に呼び出されるメソッドです。<BR>
	 * SNMPポーリングにより、検索結果をセットし、クライアントに返す。
	 *
	 * RepositoryRead権限が必要
	 *
	 * @param pollingData オーナーロールID、ポーリング対象のIPアドレス(開始)、ポーリング対象のIPアドレス(終了)、コミュニティ名、バージョン、ポート、ファシリティID、セキュリティレベル、ユーザー名、認証パスワード、暗号化パスワード、認証プロトコル、暗号化プロトコル
	 * @return ノード情報（更新情報）
	 * @throws HinemosUnknown
	 * @throws FacilityNotFound
	 * @throws UnknownHostException
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 * @throws InvalidSetting
	 * @throws FacilityDuplicate
	 */
	public List<NodeInfoDeviceSearch> searchNodesBySNMP(String ownerRoleId, String ipAddressFrom, String ipAddressTo,
			int port, String community, int version, String facilityID,
			String securityLevel, String user, String authPass,
			String privPass, String authProtocol, String privProtocol)
			throws HinemosUnknown, InvalidUserPass, InvalidRole, FacilityDuplicate, InvalidSetting {

		m_log.debug("searchNodesBySNMP : ipAddressFrom=" + ipAddressFrom
				+ ", ipAddressTo=" + ipAddressTo
				+ ", port=" + port + ", community=" + community + ", version="
				+ version + ", securityLevel=" + securityLevel + ", user=" + user + ", authPassword=" + authPass
				+ ", privPassword=" + privPass + ", authProtocol=" + authProtocol + ", privProtocol=" + privProtocol);

		//権限チェック
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(
				FunctionConstant.REPOSITORY, SystemPrivilegeMode.ADD));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		msg.append(", ipAddressFrom=");
		msg.append(ipAddressFrom);
		msg.append(", ipAddressTo=");
		msg.append(ipAddressTo);
		msg.append(", Port=");
		msg.append(port);
		msg.append(", Community=");
		msg.append(community);
		msg.append(", Version=");
		msg.append(version);
		msg.append(", securityLevel=");
		msg.append(securityLevel);
		msg.append(", user=");
		msg.append(user);
		// msg.append(", authPassword="); msg.append(authPass);
		// msg.append(", privPassword="); msg.append(privPass);
		msg.append(", authProtocol=");
		msg.append(authProtocol);
		msg.append(", privProtocol=");
		msg.append(privProtocol);
		m_opelog.info(HinemosModuleConstant.LOG_PREFIX_REPOSITORY + " Get, Method=searchNodesBySNMP, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());

		//SNMPによる検索・登録
		NodeSearcher bean = new NodeSearcher();
		return bean.searchNode(ownerRoleId, ipAddressFrom, ipAddressTo, port,
				community, version, facilityID, securityLevel, user, authPass,
				privPass, authProtocol, privProtocol);
	}


	/**
	 * 対象構成情報を新規に追加します。<BR>
	 *
	 * RepositoryAdd権限が必要
	 *
	 * @param info 追加する対象構成情報
	 * @throws NodeConfigSettingDuplicate
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 * @throws InvalidSetting
	 */
	public void addNodeConfigSettingInfo(NodeConfigSettingInfo info) 
			throws NodeConfigSettingDuplicate, InvalidUserPass, InvalidRole, HinemosUnknown, InvalidSetting {
		m_log.debug("addNodeConfigSettingInfo : nodeConfigSettingInfo=" + info);
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.REPOSITORY, SystemPrivilegeMode.ADD));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		if(info != null){
			msg.append(", SettingID=");
			msg.append(info.getSettingId());
		}

		try {
			long startTime = HinemosTime.currentTimeMillis();
			new NodeConfigSettingControllerBean().addNodeConfigSettingInfo(info);
			m_log.info(String.format("addNodeConfigSettingInfo: %dms", HinemosTime.currentTimeMillis() - startTime));
		} catch (Exception e) {
			m_opelog.warn(HinemosModuleConstant.LOG_PREFIX_REPOSITORY + " Add NodeConfigSettingInfo Failed, Method=addNodeConfigSettingInfo, User="
					+ HttpAuthenticator.getUserAccountString(wsctx)
					+ msg.toString());
			throw e;
		}
		m_opelog.info(HinemosModuleConstant.LOG_PREFIX_REPOSITORY + " Add NodeConfigSettingInfo, Method=addNodeConfigSettingInfo, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());
	}


	/**
	 * 対象構成情報を変更します。<BR>
	 *
	 * RepositoryWrite権限が必要
	 *
	 * @param info　変更する対象構成情報
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 * @throws InvalidSetting
	 */
	public void modifyNodeConfigSettingInfo(NodeConfigSettingInfo info)
			throws InvalidUserPass, InvalidRole, HinemosUnknown, InvalidSetting {
		m_log.debug("modifyNodeConfigSettingInfo : nodeConfigSettingInfo=" + info);
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.REPOSITORY, SystemPrivilegeMode.MODIFY));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		if(info != null){
			msg.append(", SettingID=");
			msg.append(info.getSettingId());
		}

		try {
			new NodeConfigSettingControllerBean().modifyNodeConfigSettingInfo(info);
		} catch (Exception e) {
			m_opelog.warn(HinemosModuleConstant.LOG_PREFIX_REPOSITORY + " Change NodeConfigSettingInfo Failed, Method=modifyNodeConfigSettingInfo, User="
					+ HttpAuthenticator.getUserAccountString(wsctx)
					+ msg.toString());
			throw e;
		}
		m_opelog.info(HinemosModuleConstant.LOG_PREFIX_REPOSITORY + " Change NodeConfigSettingInfo, Method=modifyNodeConfigSettingInfo, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());
	}


	/**
	 * 対象構成情報を削除します。<BR>
	 *
	 * settingIDで指定されたノードをリポジトリから削除します。
	 *
	 * RepositoryWrite権限が必要
	 *
	 * @param settingIds SettingIdの配列
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	public void deleteNodeConfigSettingInfo(String[] settingIds)
			throws InvalidUserPass, InvalidRole, HinemosUnknown {
		m_log.debug("deleteNodeConfigSettingInfo : settingId=" + Arrays.toString(settingIds));
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.REPOSITORY, SystemPrivilegeMode.MODIFY));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);
		StringBuffer msg = new StringBuffer();

		// 認証済み操作ログ
		msg.append(", SettingID=");
		msg.append(Arrays.toString(settingIds));

		try {
			new NodeConfigSettingControllerBean().deleteNodeConfigSettingInfo(settingIds);
		} catch (Exception e) {
			m_opelog.warn(HinemosModuleConstant.LOG_PREFIX_REPOSITORY + " Delete NodeConfigSettingInfo Failed, Method=deleteNodeConfigSettingInfo, User="
					+ HttpAuthenticator.getUserAccountString(wsctx)
					+ msg.toString());
			throw e;
		}
		m_opelog.info(HinemosModuleConstant.LOG_PREFIX_REPOSITORY + " Delete NodeConfigSettingInfo, Method=deleteNodeConfigSettingInfo, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());
	}


	/**
	 * 対象構成情報の収集を有効化/無効化します。<BR>
	 *
	 * RepositoryWrite権限が必要
	 *
	 * @param settingId　変更する対象構成ID
	 * @param validFlag　true:有効、false:無効
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 * @throws InvalidSetting
	 * @throws NodeConfigSettingNotFound
	 */
	public void setStatusNodeConfigSetting(String settingId, boolean validFlag)
			throws InvalidUserPass, InvalidRole, HinemosUnknown, InvalidSetting, NodeConfigSettingNotFound {
		m_log.debug("setStatusNodeConfigSetting : settingId=" + settingId + ", validFlag" + validFlag);
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.REPOSITORY, SystemPrivilegeMode.MODIFY));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		msg.append(", SettingId=");
		msg.append(settingId);
		msg.append(", ValidFlag=");
		msg.append(validFlag);

		try {
			new NodeConfigSettingControllerBean().setStatusNodeConfigSetting(settingId, validFlag);
		} catch (Exception e) {
			m_opelog.warn(HinemosModuleConstant.LOG_PREFIX_REPOSITORY + " Change  Valid Failed, Method=setStatusNodeConfigSetting, User="
					+ HttpAuthenticator.getUserAccountString(wsctx)
					+ msg.toString());
			throw e;
		}
		m_opelog.info(HinemosModuleConstant.LOG_PREFIX_REPOSITORY + " Change Valid, Method=setStatusNodeConfigSetting, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());
	}


	/**
	 * 対象構成情報を取得します。<BR>
	 *
	 * settingIDで指定される対象構成情報を取得します。<BR>
	 *
	 * RepositoryRead権限が必要
	 *
	 * @param settingId 対象構成情報ID
	 * @return 対象構成情報
	 * @throws NodeConfigSettingNotFound
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	public NodeConfigSettingInfo getNodeConfigSettingInfo(String settingId)
			throws NodeConfigSettingNotFound, InvalidUserPass, InvalidRole, HinemosUnknown {
		m_log.debug("getNodeConfigSettingInfo : settingId=" + settingId);
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.REPOSITORY, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		msg.append(", SettingID=");
		msg.append(settingId);
		m_opelog.debug(HinemosModuleConstant.LOG_PREFIX_REPOSITORY + " Get, Method=getNodeConfigSettingInfo, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());
		
		return new NodeConfigSettingControllerBean().getNodeConfigSettingInfo(settingId);
	}


	/**
	 * 対象構成情報一覧を取得します。<BR>
	 *
	 * 対象構成情報を取得します。<BR>
	 *
	 * RepositoryRead権限が必要
	 *
	 * @return 対象構成情報一覧
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	public List<NodeConfigSettingInfo> getNodeConfigSettingList()
			throws InvalidUserPass, InvalidRole, HinemosUnknown {
		m_log.debug("getNodeConfigSettingList");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.REPOSITORY, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		m_opelog.debug(HinemosModuleConstant.LOG_PREFIX_REPOSITORY + " Get, Method=getNodeConfigSettingList, User="
				+ HttpAuthenticator.getUserAccountString(wsctx));
		
		return new NodeConfigSettingControllerBean().getNodeConfigSettingList();
	}

	/**
	 * 引数で渡された設定に従い、構成情報収集を即時実行します。<BR>
	 *
	 * RepositoryExec権限が必要
	 *
	 * @param settingId 構成情報設定ID
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 */
	public Long runCollectNodeConfig(String settingId) throws  FacilityNotFound, InvalidUserPass, InvalidRole, NodeConfigSettingNotFound, HinemosUnknown {
		m_log.debug("runCollectNodeConfig");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.REPOSITORY, SystemPrivilegeMode.EXEC));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		m_opelog.debug(HinemosModuleConstant.LOG_PREFIX_REPOSITORY + " Get, Method=runCollectNodeConfig, User="
				+ HttpAuthenticator.getUserAccountString(wsctx));
		StringBuffer msg = new StringBuffer();
		if(settingId != null){
			msg.append(", SettingID=");
			msg.append(settingId);
		}

		Long loadDistributionTime = null;
		try {
			long startTime = HinemosTime.currentTimeMillis();
			loadDistributionTime = new NodeConfigSettingControllerBean().runCollectNodeConfig(settingId);
			m_log.info(String.format("runCollectNodeConfig: %dms", HinemosTime.currentTimeMillis() - startTime));
		} catch (Exception e) {
			m_opelog.warn(HinemosModuleConstant.LOG_PREFIX_REPOSITORY
					+ " Run to Collect NodeConfigInfo Failed, Method=runCollectNodeConfig, User="
					+ HttpAuthenticator.getUserAccountString(wsctx) + msg.toString());
			throw e;
		}
		m_opelog.info(HinemosModuleConstant.LOG_PREFIX_REPOSITORY
				+ " Run to Collect NodeConfigInfo , Method=runCollectNodeConfig, User="
				+ HttpAuthenticator.getUserAccountString(wsctx) + msg.toString());
		return loadDistributionTime;
	}
}
