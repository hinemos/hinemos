/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.repository.api;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.glassfish.grizzly.http.server.Request;

import com.clustercontrol.accesscontrol.bean.FunctionConstant;
import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.SystemPrivilegeMode;
import com.clustercontrol.accesscontrol.model.SystemPrivilegeInfo;
import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.commons.util.HttpBasicAuthenticator;
import com.clustercontrol.fault.FacilityNotFound;
import com.clustercontrol.fault.HinemosException;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.repository.bean.LatestNodeConfigWrapper;
import com.clustercontrol.repository.bean.RestNodeList;
import com.clustercontrol.repository.bean.RestPackageList;
import com.clustercontrol.repository.bean.RestProcessList;
import com.clustercontrol.repository.model.NodeInfo;
import com.clustercontrol.repository.model.NodePackageInfo;
import com.clustercontrol.repository.model.NodeProcessInfo;
import com.clustercontrol.repository.session.RepositoryControllerBean;
import com.clustercontrol.util.DateUtil;

/**
 * ノード関連の情報を取得するRest-API.
 * 
 * @since 6.2.0
 * @version 6.2.0
 */
@Path("api/repository")
public class GetNodeInfo {

	// ログ出力用.
	/** ロガー */
	private static Log m_log = LogFactory.getLog(GetNodeInfo.class);
	private static Log m_opelog = LogFactory.getLog("HinemosOperation");
	/** ログ出力区切り文字 */
	private static final String DELIMITER = "() : ";

	private static final String RETURN_ERROR_MESSAGE = "%s parameter '%s' is invalid.";

	/**
	 * ノード一覧取得.<br>
	 * <br>
	 * マネージャーに登録されている全てのノードを取得する.
	 * 
	 * @param authzHeader
	 *            HTTPヘッダに含まれた認証情報.
	 * @param request
	 *            要求(ソースIP取得用).
	 * @return ノード一覧(JSON形式)
	 * 
	 *         <pre>
	 *         例：値が全てセットされている正常系
	 *          {
	 *           "node":[
	 *                    {
	 *                     "facilityId":"node1",
	 *                     "ipAddress":["0.0.0.0","255.255.255.255"]
	 *                    },
	 *                    {
	 *                     "facilityId":"node2",
	 *                     "ipAddress":["0.0.0.1","255.255.255.0"]
	 *                    },
	 *                    ...
	 *                  ]
	 *           }
	 *         </pre>
	 * 
	 *         <pre>
	 *         例：データ0件
	 *          {"node":null}
	 *         </pre>
	 * 
	 *         <pre>
	 *         例：nullの項目あり
	 *          {
	 *           "node":[
	 *                    {
	 *                     "facilityId":node1,
	 *                     "ipAddress":null
	 *                    },
	 *                    null,
	 *                    {
	 *                     "facilityId":node2,
	 *                     "ipAddress":[null,"255.255.255.255"]
	 *                    }
	 *                    ...
	 *                  ]
	 *           }
	 *         </pre>
	 * 
	 * @throws HinemosException
	 */
	@Path("/node")
	@GET
	public Response getNode(@HeaderParam(HttpHeaders.AUTHORIZATION) final String authzHeader,
			@Context Request request) {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
		m_log.debug(methodName + DELIMITER + "start");

		// 認証チェック.
		Response checkedAuthenticator = this.checkAuthenticator(authzHeader, request, methodName);
		if (checkedAuthenticator != null) {
			return checkedAuthenticator;
		}

		// DBから全件取得(取得不可はStatus=500で返却される、DBアクセス不可など).
		RepositoryControllerBean controller = new RepositoryControllerBean();
		List<NodeInfo> nodeList = null;
		try {
			nodeList = controller.getNodeNicList();
		} catch (HinemosUnknown e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
		}

		RestNodeList returnList = new RestNodeList(nodeList);

		// debugログ出力.
		m_log.debug(methodName + DELIMITER + String.format("return json=[%s]", returnList.toString()));

		return Response.ok(returnList, MediaType.APPLICATION_JSON).build();
	}

	/**
	 * パッケージ情報取得.<br>
	 * <br>
	 * 引数で指定したノードのパッケージ情報を取得する.
	 * 
	 * @param authzHeader
	 *            HTTPヘッダに含まれた認証情報.
	 * @param request
	 *            要求(ソースIP取得用).
	 * @param facilityId
	 *            取得対象のファシリティID、パス内で必ず指定.
	 * @param date
	 *            取得対象の日付(YYYYMMDDhhmmss)、クエリパラメータで指定、指定なしの場合は最新のパッケージ情報を取得.
	 * @return パッケージ情報(JSON形式)<br>
	 *         "collected" : 指定日付より前のHinemosが最後に収集した日時<br>
	 *         "lastUpdated" : 指定日付より前のNodeで最後にパッケージが更新された日時<br>
	 * 
	 *         <pre>
	 *         例：値が全てセットされている正常系
	 *          {
	 *           "collected":"YYYYMMDDhhmmss",
	 *           "lastUpdated":"YYYYMMDDhhmmss",
	 *           "package":[
	 *                      {"name":"package-name",
	 *                       "version":"1.0.0",
	 *                       "release":"1",
	 *                       "vendor":"NTT DATA Intellilink",
	 *                       "installed":"YYYYMMDDhhmmss",
	 *                       "arch":"x86"
	 *                      },
	 *                      ...
	 *                     ]
	 *           }
	 *         </pre>
	 * 
	 *         <pre>
	 *         例：データ0件.
	 *          {
	 *           "collected":null,
	 *           "lastUpdated":null,
	 *           "package":null
	 *           }
	 *         </pre>
	 */
	@Path("/node/{facilityId}/packages")
	@GET
	public Response getPackages(@HeaderParam(HttpHeaders.AUTHORIZATION) final String authzHeader,
			@Context Request request, @PathParam("facilityId") String facilityId, @QueryParam("date") String dateStr)
			throws HinemosUnknown {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
		m_log.debug(methodName + DELIMITER + "start");

		// 認証チェック.
		Response checkedAuthenticator = this.checkAuthenticator(authzHeader, request, methodName);
		if (checkedAuthenticator != null) {
			return checkedAuthenticator;
		}

		// ミリ秒半端分も対象として取得する.
		Long dateMillis = null;
		String format = "yyyyMMddHHmmssSSS";
		if (dateStr != null && !dateStr.isEmpty()) {
			dateStr = dateStr + "999";
		}

		// 日付指定の場合、日付をDB検索用にLong値変換.
		if (dateStr != null && !dateStr.isEmpty()) {
			try {
				dateMillis = DateUtil.dateStrToMillis(dateStr, format);
			} catch (InvalidSetting e1) {
				String message = String.format("invalid date string. date(origin)=[%s], format=[%s]", dateStr, format);
				m_log.info(methodName + DELIMITER + message);
				return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(message).build();
			}
		}

		// DBからパッケージ情報を取得.
		RepositoryControllerBean controller = new RepositoryControllerBean();
		LatestNodeConfigWrapper<NodePackageInfo> nodePackage = null;
		try {
			nodePackage = controller.getNodePackageList(facilityId, dateMillis);
		} catch (FacilityNotFound e) {
			String message = String.format(RETURN_ERROR_MESSAGE, "path", facilityId) + " : " + e.getMessage();
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(message).build();
		} catch (HinemosUnknown | InvalidSetting e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
		}

		// DBから取得した値をRest返却用に変換.
		m_log.debug(methodName + DELIMITER + "get packages from DB.");
		RestPackageList returnList = new RestPackageList(nodePackage);

		// debugログ出力.
		m_log.debug(methodName + DELIMITER + String.format("return json=[%s]", returnList.toString()));

		return Response.ok(returnList, MediaType.APPLICATION_JSON).build();
	}

	/**
	 * プロセス情報取得.<br>
	 * <br>
	 * 引数で指定したノードのプロセス情報を取得する.<br>
	 * 日時指定は不可で、Hinemosが収集した最新のプロセス情報のみを取得可能<br>
	 * ※更新頻度が高いため過去情報を保存せず最新情報のみを保持.
	 * 
	 * 
	 * @param authzHeader
	 *            HTTPヘッダに含まれた認証情報.
	 * @param request
	 *            要求(ソースIP取得用).
	 * @param facilityId
	 *            取得対象のファシリティID、パス内で必ず指定.
	 * @return プロセス情報(JSON形式)<br>
	 *         "collected" : 指定日付より前のHinemosが最後に収集した日時<br>
	 *         "lastUpdated" : 収集日時と同じ<br>
	 * 
	 *         <pre>
	 *         例：値が全てセットされている正常系
	 *          {
	 *           "collected":"YYYYMMDDhhmmss",
	 *           "lastUpdated":"YYYYMMDDhhmmss",
	 *           "process":[
	 *                      {"name":"java",
	 *                       "path":"java -Djdk.xml.entityExpansionLimit=0 -javaagent:/opt/hinemos/lib/eclipselink.jar...",
	 *                       "user":"root",
	 *                       "pid":"19256",
	 *                       "startup":"YYYYMMDDhhmmss"
	 *                      },
	 *                      ...
	 *                     ]
	 *           }
	 *         </pre>
	 * 
	 *         <pre>
	 *         例：データ0件.
	 *          {
	 *           "collected":null,
	 *           "lastUpdated":null,
	 *           "process":null
	 *           }
	 *         </pre>
	 */
	@Path("/node/{facilityId}/processes")
	@GET
	public Response getProcesses(@HeaderParam(HttpHeaders.AUTHORIZATION) final String authzHeader,
			@Context Request request, @PathParam("facilityId") String facilityId) {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
		m_log.debug(methodName + DELIMITER + "start");

		// 認証チェック.
		Response checkedAuthenticator = this.checkAuthenticator(authzHeader, request, methodName);
		if (checkedAuthenticator != null) {
			return checkedAuthenticator;
		}

		// DBからプロセス情報を取得.
		RepositoryControllerBean controller = new RepositoryControllerBean();
		List<NodeProcessInfo> nodeProcess = null;
		try {
			nodeProcess = controller.getNodeProcessList(facilityId);
		} catch (FacilityNotFound e) {
			String message = String.format(RETURN_ERROR_MESSAGE, "path", facilityId) + " : " + e.getMessage();
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(message).build();
		} catch (HinemosUnknown | InvalidSetting e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
		}

		// DBから取得した値をRest返却用に変換.
		m_log.debug(methodName + DELIMITER + "get processes from DB.");
		RestProcessList returnList = new RestProcessList(nodeProcess);

		// debugログ出力.
		m_log.debug(methodName + DELIMITER + String.format("return json=[%s]", returnList.toString()));

		return Response.ok(returnList, MediaType.APPLICATION_JSON).build();
	}

	/**
	 * 認証チェック.<br>
	 * <br>
	 * HTTPヘッダに設定されたBasic認証情報を元にリポジトリ参照権限を持つユーザーかどうかを判定する.<br>
	 * 
	 * @param authzHeader
	 *            HTTPヘッダ内の認証情報(Basic認証)
	 * @param request
	 *            要求ヘッダ(操作ログ出力時のソースIP取得用)
	 * @param methodName
	 *            呼び出し元メソッド名(操作ログ出力用)
	 * @return 認証チェックエラー時のResponse、認証チェックOKであればnullを返却
	 * 
	 */
	private Response checkAuthenticator(final String authzHeader, Request request, String methodName) {
		// HttpBasic認証でユーザー名とパスワード送信される前提..
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.REPOSITORY, SystemPrivilegeMode.READ));
		String userAccount;
		try {
			HttpBasicAuthenticator.authCheck(authzHeader, systemPrivilegeList);
			userAccount = HttpBasicAuthenticator.getUserAccountString(authzHeader, request.getRemoteAddr());
		} catch (InvalidUserPass e) {
			// ユーザー名/パスワード不正.
			return Response.status(Response.Status.UNAUTHORIZED).entity(e.getMessage()).build();
		} catch (InvalidRole e) {
			// アクセス権限なしのユーザー.
			return Response.status(Response.Status.FORBIDDEN).entity(e.getMessage()).build();
		} catch (HinemosUnknown e) {
			// DB接続不可など.
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
		}

		// 認証済操作ログの出力.
		m_opelog.debug(String.format("%s Get, Method=%s, User=%s", HinemosModuleConstant.LOG_PREFIX_REPOSITORY,
				methodName, userAccount));

		return null;
	}

	// 以下試験時の切り分け用メソッド.
	/**
	 * テスト用メソッド.<br>
	 * <br>
	 * 接続できていればStatusCode200で"connected test OK"という文字列がJSONとして返却されます. <br>
	 * 返却されない場合は環境の問題、もしくはHinemosPropertyの設定が不正な可能性があります.
	 */
	@Path("/node/test")
	@GET
	public Response testConnection() {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
		m_log.debug(methodName + DELIMITER + "start");
		return Response.ok("connected test OK", MediaType.APPLICATION_JSON).build();
	}

	/**
	 * 認証情報テスト用メソッド.<br>
	 * <br>
	 * 正しい認証情報が送信できていれば<br>
	 * StatusCode200で"authenticated test OK" という文字列がJSONとして返却されます. <br>
	 * 返却されない場合は"/node/test"で接続できていることを確認の上、エラー内容を確認ください.
	 */
	@Path("/node/test/authenticator")
	@GET
	public Response testAuthenticator(@HeaderParam(HttpHeaders.AUTHORIZATION) final String authzHeader,
			@Context Request request) {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
		m_log.debug(methodName + DELIMITER + "start");

		// 認証チェック.
		Response checkedAuthenticator = this.checkAuthenticator(authzHeader, request, methodName);
		if (checkedAuthenticator != null) {
			return checkedAuthenticator;
		}

		return Response.ok("authenticated test OK", MediaType.APPLICATION_JSON).build();
	}
}
