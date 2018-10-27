/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.ws.xcloud;

import java.util.List;

import javax.activation.DataHandler;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.xml.bind.annotation.XmlMimeType;

import com.clustercontrol.fault.FacilityNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.xcloud.CloudManagerException;
import com.clustercontrol.xcloud.bean.AddCloudLoginUserRequest;
import com.clustercontrol.xcloud.bean.AddCloudScopeRequest;
import com.clustercontrol.xcloud.bean.AutoAssignNodePatternEntry;
import com.clustercontrol.xcloud.bean.AvailableRole;
import com.clustercontrol.xcloud.bean.BillingResult;
import com.clustercontrol.xcloud.bean.CloneBackupedInstanceRequest;
import com.clustercontrol.xcloud.bean.CloneBackupedStorageRequest;
import com.clustercontrol.xcloud.bean.CloudLoginUser;
import com.clustercontrol.xcloud.bean.CloudPlatform;
import com.clustercontrol.xcloud.bean.CloudScope;
import com.clustercontrol.xcloud.bean.CreateInstanceSnapshotRequest;
import com.clustercontrol.xcloud.bean.CreateStorageSnapshotRequest;
import com.clustercontrol.xcloud.bean.HRepository;
import com.clustercontrol.xcloud.bean.Instance;
import com.clustercontrol.xcloud.bean.InstanceBackup;
import com.clustercontrol.xcloud.bean.ModifyBillingSettingRequest;
import com.clustercontrol.xcloud.bean.ModifyCloudLoginUserRequest;
import com.clustercontrol.xcloud.bean.ModifyCloudScopeRequest;
import com.clustercontrol.xcloud.bean.ModifyInstanceRequest;
import com.clustercontrol.xcloud.bean.Network;
import com.clustercontrol.xcloud.bean.Option;
import com.clustercontrol.xcloud.bean.PlatformServiceCondition;
import com.clustercontrol.xcloud.bean.PlatformUser;
import com.clustercontrol.xcloud.bean.RoleRelation;
import com.clustercontrol.xcloud.bean.Storage;
import com.clustercontrol.xcloud.bean.StorageBackup;

/**
 * クラウド管理オプションの Web サービス API 定義。
 *
 * @version 6.0.a
 * @since 5.0.a
 */
@WebService(targetNamespace = "http://xcloud.ws.clustercontrol.com")
public interface CloudEndpoint {

	/**
	 * クラウド[コンピュート]ビュー : パワーオン - ジョブ(ノード指定)
	 * @param cloudScopeId
	 * @param locationId
	 * @param instanceIds
	 * @return
	 * @throws CloudManagerException
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 */
	@WebMethod
	String makePowerOnInstancesCommand(@WebParam(name = "cloudScopeId") String cloudScopeId, @WebParam(name = "locationId") String locationId, @WebParam(name = "instanceIds") List<String> instanceIds) throws CloudManagerException, InvalidUserPass, InvalidRole;

	/**
	 * クラウド[コンピュート]ビュー : パワーオン - ジョブ(スコープ指定)
	 * @param cloudScopeId
	 * @param facilityId
	 * @return
	 * @throws CloudManagerException
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 */
	@WebMethod
	String makePowerOnInstancesCommandUsingFacility(@WebParam(name = "cloudScopeId") String cloudScopeId, @WebParam(name = "facilityId") String facilityId) throws CloudManagerException, InvalidUserPass, InvalidRole;

	/**
	 * クラウド[コンピュート]ビュー : パワーオフ - ジョブ(ノード指定)
	 * @param cloudScopeId
	 * @param locationId
	 * @param instanceIds
	 * @return
	 * @throws CloudManagerException
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 */
	@WebMethod
	String makePowerOffInstancesCommand(@WebParam(name = "cloudScopeId") String cloudScopeId, @WebParam(name = "locationId") String locationId, @WebParam(name = "instanceIds") List<String> instanceIds) throws CloudManagerException, InvalidUserPass, InvalidRole;

	/**
	 * クラウド[コンピュート]ビュー : パワーオフ - ジョブ(スコープ指定)
	 * @param cloudScopeId
	 * @param facilityId
	 * @return
	 * @throws CloudManagerException
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 */
	@WebMethod
	String makePowerOffInstancesCommandUsingFacility(@WebParam(name = "cloudScopeId") String cloudScopeId, @WebParam(name = "facilityId") String facilityId) throws CloudManagerException, InvalidUserPass, InvalidRole;

	/**
	 * クラウド[コンピュート]ビュー : 再起動 - ジョブ(ノード指定)
	 * @param cloudScopeId
	 * @param locationId
	 * @param instanceIds
	 * @return
	 * @throws CloudManagerException
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 */
	@WebMethod
	String makeRebootInstancesCommand(@WebParam(name = "cloudScopeId") String cloudScopeId, @WebParam(name = "locationId") String locationId, @WebParam(name = "instanceIds") List<String> instanceIds) throws CloudManagerException, InvalidUserPass, InvalidRole;

	/**
	 * クラウド[コンピュート]ビュー : 再起動- ジョブ(スコープ指定)
	 * @param cloudScopeId
	 * @param facilityId
	 * @return
	 * @throws CloudManagerException
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 */
	@WebMethod
	String makeRebootInstancesCommandUsingFacility(@WebParam(name = "cloudScopeId") String cloudScopeId, @WebParam(name = "facilityId") String facilityId) throws CloudManagerException, InvalidUserPass, InvalidRole;

	/**
	 * クラウド[コンピュート]ビュー : サスペンド - ジョブ(ノード指定)
	 * @param cloudScopeId
	 * @param locationId
	 * @param instanceIds
	 * @return
	 * @throws CloudManagerException
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 */
	@WebMethod
	String makeSuspendInstancesCommand(@WebParam(name = "cloudScopeId") String cloudScopeId, @WebParam(name = "locationId") String locationId, @WebParam(name = "instanceIds") List<String> instanceIds) throws CloudManagerException, InvalidUserPass, InvalidRole;

	/**
	 * クラウド[コンピュート]ビュー : サスペンド - ジョブ(スコープ指定)
	 * @param cloudScopeId
	 * @param facilityId
	 * @return
	 * @throws CloudManagerException
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 */
	@WebMethod
	String makeSuspendInstancesCommandUsingFacility(@WebParam(name = "cloudScopeId") String cloudScopeId, @WebParam(name = "facilityId") String facilityId) throws CloudManagerException, InvalidUserPass, InvalidRole;

	/**
	 * クラウド[コンピュート]ビュー : スナップショット - ジョブ(ノード指定)
	 * @param cloudScopeId
	 * @param locationId
	 * @param instanceId
	 * @return
	 * @throws CloudManagerException
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 */
	@WebMethod
	String makeSnapshotInstanceCommand(@WebParam(name = "cloudScopeId") String cloudScopeId, @WebParam(name = "locationId") String locationId, @WebParam(name = "instanceId") String instanceId) throws CloudManagerException, InvalidUserPass, InvalidRole;

	/**
	 * クラウド[コンピュート]ビュー : スコープ割当ルール
	 * @param cloudScopeId
	 * @param patterns
	 * @throws CloudManagerException
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 */
	@WebMethod
	void registAutoAssigneNodePattern(@WebParam(name = "cloudScopeId") String cloudScopeId, @WebParam(name = "AutoAssigneNodePatternEntries") List<AutoAssignNodePatternEntry> patterns) throws CloudManagerException, InvalidUserPass, InvalidRole;
	@WebMethod
	List<AutoAssignNodePatternEntry> getAutoAssigneNodePatterns(@WebParam(name = "cloudScopeId") String cloudScopeId) throws CloudManagerException, InvalidUserPass, InvalidRole;
	@WebMethod
	void clearAutoAssigneNodePattern(@WebParam(name = "cloudScopeId") String cloudScopeId) throws CloudManagerException, InvalidUserPass, InvalidRole;

	
	/**
	 * ラウド[ストレージ]ビュー : アタッチ - ジョブ(ノード指定)
	 * @param cloudScopeId
	 * @param locationId
	 * @param instanceId
	 * @param storageId
	 * @param options
	 * @return
	 * @throws CloudManagerException
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 */
	@WebMethod
	String makeAttachStorageCommand(@WebParam(name = "cloudScopeId") String cloudScopeId, @WebParam(name = "locationId") String locationId, @WebParam(name = "instanceId") String instanceId, @WebParam(name = "storageId") String storageId, @WebParam(name = "options") List<Option> options) throws CloudManagerException, InvalidUserPass, InvalidRole;
	/**
	 * クラウド[ストレージ]ビュー : デタッチ - ジョブ(ノード指定)
	 * @param cloudScopeId
	 * @param locationId
	 * @param storageId
	 * @return
	 * @throws CloudManagerException
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 */
	@WebMethod
	String makeDetachStorageCommand(@WebParam(name = "cloudScopeId") String cloudScopeId, @WebParam(name = "locationId") String locationId, @WebParam(name = "storageId") String storageId) throws CloudManagerException, InvalidUserPass, InvalidRole;

	/**
	 * クラウド[ストレージ]ビュー : スナップショット - ジョブ(ノード指定)
	 * @param cloudScopeId
	 * @param locationId
	 * @param storageId
	 * @return
	 * @throws CloudManagerException
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 */
	@WebMethod
	String makeSnapshotStorageCommand(@WebParam(name = "cloudScopeId") String cloudScopeId, @WebParam(name = "locationId") String locationId, @WebParam(name = "storageId") String storageId) throws CloudManagerException, InvalidUserPass, InvalidRole;


	/**
	 * 操作が利用可能かを確認する
	 * 
	 * @param cloudScopeId
	 * @param methodName
	 * @throws CloudManagerException
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 */
	@WebMethod
	void checkCallable(@WebParam(name = "cloudScopeId") String cloudScopeId, @WebParam(name = "methodName") String methodName) throws CloudManagerException, InvalidUserPass, InvalidRole;
	
	/**
	 * プラットフォームIDを指定してクラウドプラットフォーム情報を取得する。
	 * 
	 * @param cloudPlatformeId
	 * @return
	 * @throws CloudManagerException
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 */
	@WebMethod
	CloudPlatform getCloudPlatform(@WebParam(name = "cloudPlatformeId") String cloudPlatformeId) throws CloudManagerException, InvalidUserPass, InvalidRole;

	/**
	 * クラウドプラットフォームの一覧を取得する。
	 * 
	 * @return
	 * @throws CloudManagerException
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 */
	@WebMethod
	List<CloudPlatform> getAllCloudPlatforms() throws CloudManagerException, InvalidUserPass, InvalidRole;

	/**
	 * クラウドスコープを作成する。
	 * 
	 * @param request
	 * @return
	 * @throws CloudManagerException
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 */
	@WebMethod
	CloudScope addCloudScope(@WebParam(name = "request") AddCloudScopeRequest request) throws CloudManagerException, InvalidUserPass, InvalidRole;

	/**
	 * クラウドスコープを削除する。
	 * 
	 * @param cloudScopeId
	 * @throws CloudManagerException
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 */
	@WebMethod
	void removeCloudScope(@WebParam(name = "cloudScopeId") String cloudScopeId) throws CloudManagerException, InvalidUserPass, InvalidRole;

	/**
	 * クラウドスコープを更新する。
	 * 
	 * @param request
	 * @return
	 * @throws CloudManagerException
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 */
	@WebMethod
	CloudScope modifyCloudScope(@WebParam(name = "request") ModifyCloudScopeRequest request) throws CloudManagerException, InvalidUserPass, InvalidRole;

	/**
	 * クラウドスコープ情報を取得する。
	 * 
	 * @param cloudScopeId
	 * @return
	 * @throws CloudManagerException
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 */
	@WebMethod
	CloudScope getCloudScope(@WebParam(name = "cloudScopeId") String cloudScopeId) throws CloudManagerException, InvalidUserPass, InvalidRole;

	/**
	 * クラウドスコープ一覧を取得する。
	 * 
	 * @return
	 * @throws CloudManagerException
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 */
	@WebMethod
	List<CloudScope> getAllCloudScopes() throws CloudManagerException, InvalidUserPass, InvalidRole;

	/**
	 * クラウドスコープ一覧を取得する。
	 * 
	 * @return
	 * @throws CloudManagerException
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 */
	@WebMethod
	List<CloudScope> getCloudScopesByRole(@WebParam(name = "roleId") String roleId) throws CloudManagerException, InvalidUserPass, InvalidRole;

	/**
	 * クラウドユーザの一覧を取得する。
	 * 
	 * @param cloudScopeId
	 * @return
	 * @throws CloudManagerException
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 */
	@WebMethod
	List<PlatformUser> getAvailablePlatformUsers(@WebParam(name = "cloudScopeId") String cloudScopeId) throws CloudManagerException, InvalidUserPass, InvalidRole;

	/**
	 * ログインユーザを追加する。
	 * 
	 * @param request
	 * @return
	 * @throws CloudManagerException
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 */
	@WebMethod
	CloudLoginUser addCloudLoginUser(@WebParam(name = "request") AddCloudLoginUserRequest request) throws CloudManagerException, InvalidUserPass, InvalidRole;

	/**
	 * 指定したクラウドユーザを取得する。
	 * 
	 * @param cloudScopeId
	 * @param cloudLoginUserId
	 * @return
	 * @throws CloudManagerException
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 */
	@WebMethod
	CloudLoginUser getCloudLoginUser(@WebParam(name = "cloudScopeId") String cloudScopeId, @WebParam(name = "cloudLoginUserId") String cloudLoginUserId) throws CloudManagerException, InvalidUserPass, InvalidRole;

	/**
	 * 指定したクラウドユーザを削除する。
	 * 
	 * @param cloudScopeId
	 * @param cloudLoginUserId
	 * @throws CloudManagerException
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 */
	@WebMethod
	void removeCloudLoginUser(@WebParam(name = "cloudScopeId") String cloudScopeId, @WebParam(name = "cloudLoginUserId") String cloudLoginUserId) throws CloudManagerException, InvalidUserPass, InvalidRole;

	/**
	 * クラウドユーザの一覧を取得する。
	 * 
	 * @param cloudScopeId
	 * @return
	 * @throws CloudManagerException
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 */
	@WebMethod
	List<CloudLoginUser> getAllCloudLoginUsers(@WebParam(name = "cloudScopeId") String cloudScopeId) throws CloudManagerException, InvalidUserPass, InvalidRole;

	/**
	 * 指定したクラウドユーザの設定を変更する。
	 * 
	 * @param request
	 * @return
	 * @throws CloudManagerException
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 */
	@WebMethod
	CloudLoginUser modifyCloudLoginUser(@WebParam(name = "request") ModifyCloudLoginUserRequest request) throws CloudManagerException, InvalidUserPass, InvalidRole;

	@WebMethod
	List<String> getCloudLoginUserPriority(@WebParam(name = "cloudScopeId") String cloudScopeId) throws CloudManagerException, InvalidUserPass, InvalidRole;
	@WebMethod
	void modifyCloudLoginUserPriority(@WebParam(name = "cloudScopeId") String cloudScopeId, @WebParam(name = "cloudLoginUserIds") List<String> cloudLoginUserIds) throws CloudManagerException, InvalidUserPass, InvalidRole;
	@WebMethod
	CloudLoginUser modifyCloudLoginUserRoleRelation(@WebParam(name = "cloudScopeId") String cloudScopeId, @WebParam(name = "cloudLoginUserId") String cloudLoginUserId, @WebParam(name = "roleRelations") List<RoleRelation> roleRelations) throws CloudManagerException, InvalidUserPass, InvalidRole;
	
	
	
	/**
	 * 対象のクラウド からインスタンスを削除し、ノードが紐づいている場合は、併せて削除される。
	 * 
	 * @param cloudScopeId
	 * @param locationId
	 * @param instanceIds
	 * @throws CloudManagerException
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 */
	@WebMethod
	void removeInstances(@WebParam(name = "cloudScopeId") String cloudScopeId, @WebParam(name = "locationId") String locationId, @WebParam(name = "instanceIds") List<String> instanceIds) throws CloudManagerException, InvalidUserPass, InvalidRole;

	/**
	 * インスタンスの一覧を取得する。
	 * 
	 * @param cloudScopeId
	 * @param locationId
	 * @return
	 * @throws CloudManagerException
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 */
	@WebMethod
	List<Instance> getAllInstances(@WebParam(name = "cloudScopeId") String cloudScopeId, @WebParam(name = "locationId") String locationId) throws CloudManagerException, InvalidUserPass, InvalidRole;

	/**
	 * インスタンスの一覧を取得する。
	 * 
	 * @param cloudScopeId
	 * @param locationId
	 * @param instanceIds
	 * @return
	 * @throws CloudManagerException
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 */
	@WebMethod
	List<Instance> getInstances(@WebParam(name = "cloudScopeId") String cloudScopeId, @WebParam(name = "locationId") String locationId, @WebParam(name = "instanceIds") List<String> instanceIds) throws CloudManagerException, InvalidUserPass, InvalidRole;

	
	/**
	 * 指定したインスタンスを起動する。
	 * 
	 * @param cloudScopeId
	 * @param locationId
	 * @param instanceIds
	 * @throws CloudManagerException
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 */
	@WebMethod
	void powerOnInstances(@WebParam(name = "cloudScopeId") String cloudScopeId, @WebParam(name = "locationId") String locationId, @WebParam(name = "instanceIds") List<String> instanceIds) throws CloudManagerException, InvalidUserPass, InvalidRole;

	/**
	 * 指定したインスタンスを起動する。
	 * 
	 * @param cloudScopeId
	 * @param facilityId
	 * @return
	 * @throws CloudManagerException
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 */
	@WebMethod
	List<Instance> powerOnInstancesUsingFacility(@WebParam(name = "cloudScopeId") String cloudScopeId, @WebParam(name = "facilityId") String facilityId) throws CloudManagerException, InvalidUserPass, InvalidRole;

	/**
	 * 指定したインスタンスを停止する。
	 * 
	 * @param cloudScopeId
	 * @param locationId
	 * @param instanceIds
	 * @throws CloudManagerException
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 */
	@WebMethod
	void powerOffInstances(@WebParam(name = "cloudScopeId") String cloudScopeId, @WebParam(name = "locationId") String locationId, @WebParam(name = "instanceIds") List<String> instanceIds) throws CloudManagerException, InvalidUserPass, InvalidRole;

	/**
	 * 指定したインスタンスを停止する。
	 * 
	 * @param cloudScopeId
	 * @param facilityId
	 * @return
	 * @throws CloudManagerException
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 */
	@WebMethod
	List<Instance> powerOffInstancesUsingFacility(@WebParam(name = "cloudScopeId") String cloudScopeId, @WebParam(name = "facilityId") String facilityId) throws CloudManagerException, InvalidUserPass, InvalidRole;

	/**
	 * 指定したインスタンスをサスペンドする。
	 * 
	 * @param cloudScopeId
	 * @param locationId
	 * @param instanceIds
	 * @throws CloudManagerException
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 */
	@WebMethod
	void suspendInstances(@WebParam(name = "cloudScopeId") String cloudScopeId, @WebParam(name = "locationId") String locationId, @WebParam(name = "instanceIds") List<String> instanceIds) throws CloudManagerException, InvalidUserPass, InvalidRole;

	/**
	 * 指定したインスタンスをサスペンドする。
	 * 
	 * @param cloudScopeId
	 * @param facilityId
	 * @return
	 * @throws CloudManagerException
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 */
	@WebMethod
	List<Instance> suspendInstancesUsingFacility(@WebParam(name = "cloudScopeId") String cloudScopeId, @WebParam(name = "facilityId") String facilityId) throws CloudManagerException, InvalidUserPass, InvalidRole;

	/**
	 * 指定したインスタンスを再起動する。
	 * 
	 * @param cloudScopeId
	 * @param locationId
	 * @param instanceIds
	 * @throws CloudManagerException
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 */
	@WebMethod
	void rebootInstances(@WebParam(name = "cloudScopeId") String cloudScopeId, @WebParam(name = "locationId") String locationId, @WebParam(name = "instanceIds") List<String> instanceIds) throws CloudManagerException, InvalidUserPass, InvalidRole;

	/**
	 * 指定したインスタンスを再起動する。
	 * 
	 * @param cloudScopeId
	 * @param facilityId
	 * @return
	 * @throws CloudManagerException
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 */
	@WebMethod
	List<Instance> rebootInstancesUsingFacility(@WebParam(name = "cloudScopeId") String cloudScopeId, @WebParam(name = "facilityId") String facilityId) throws CloudManagerException, InvalidUserPass, InvalidRole;

	/**
	 * スナップショットを作成する。
	 * 
	 * @param cloudScopeId
	 * @param locationId
	 * @param request
	 * @return
	 * @throws CloudManagerException
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 */
	@WebMethod
	InstanceBackup snapshotInstance(@WebParam(name = "cloudScopeId") String cloudScopeId, @WebParam(name = "locationId") String locationId, @WebParam(name = "request") CreateInstanceSnapshotRequest request) throws CloudManagerException, InvalidUserPass, InvalidRole;

	/**
	 * スナップショットを削除する。
	 * 
	 * @param cloudScopeId
	 * @param locationId
	 * @param isntanceId
	 * @param instanceSnapshotIds
	 * @throws CloudManagerException
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 */
	@WebMethod
	void deleteInstanceSnapshots(@WebParam(name = "cloudScopeId") String cloudScopeId, @WebParam(name = "locationId") String locationId, @WebParam(name = "instanceId") String isntanceId, @WebParam(name = "instanceSnapshotIds") List<String> instanceSnapshotIds) throws CloudManagerException, InvalidUserPass, InvalidRole;

	/**
	 * スナップショットからインスタンスをクローンする。
	 * 
	 * @param cloudScopeId
	 * @param locationId
	 * @param request
	 * @return
	 * @throws CloudManagerException
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 */
	@WebMethod
	Instance cloneBackupedInstance(@WebParam(name = "cloudScopeId") String cloudScopeId, @WebParam(name = "locationId") String locationId, @WebParam(name = "request") CloneBackupedInstanceRequest request) throws CloudManagerException, InvalidUserPass, InvalidRole;

	@WebMethod
	List<InstanceBackup> getInstanceBackups(@WebParam(name = "cloudScopeId") String cloudScopeId, @WebParam(name = "locationId") String locationId, @WebParam(name = "instanceIds") List<String> instanceIds) throws CloudManagerException, InvalidUserPass, InvalidRole;


	/**
	 * インスタンスを更新する。
	 * 
	 * @param cloudScopeId
	 * @param locationId
	 * @param request
	 * @return
	 * @throws CloudManagerException
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 */
	@WebMethod
	Instance modifyInstance(@WebParam(name = "cloudScopeId") String cloudScopeId, @WebParam(name = "locationId") String locationId, @WebParam(name = "request") ModifyInstanceRequest request) throws CloudManagerException, InvalidUserPass, InvalidRole;
	

	/**
	 * ストレージをアタッチする。
	 * 
	 * @param cloudScopeId
	 * @param locationId
	 * @param instanceId
	 * @param storageId
	 * @param options
	 * @throws CloudManagerException
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 */
	@WebMethod
	void attachStorage(@WebParam(name = "cloudScopeId") String cloudScopeId, @WebParam(name = "locationId") String locationId, @WebParam(name = "instanceId") String instanceId, @WebParam(name = "storageId") String storageId, @WebParam(name = "options") List<Option> options) throws CloudManagerException, InvalidUserPass, InvalidRole ;

	/**
	 * ストレージをデタッチする。
	 * 
	 * @param cloudScopeId
	 * @param locationId
	 * @param storageIds
	 * @throws CloudManagerException
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 */
	@WebMethod
	void detachStorage(@WebParam(name = "cloudScopeId") String cloudScopeId, @WebParam(name = "locationId") String locationId, @WebParam(name = "storageIds") List<String> storageIds) throws CloudManagerException, InvalidUserPass, InvalidRole ;
	

	/**
	 * ストレージを削除する。
	 * 
	 * @param cloudScopeId
	 * @param locationId
	 * @param storageIds
	 * @throws CloudManagerException
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 */
	@WebMethod
	void removeStorages(@WebParam(name = "cloudScopeId") String cloudScopeId, @WebParam(name = "locationId") String locationId, @WebParam(name = "storageIds") List<String> storageIds) throws CloudManagerException, InvalidUserPass, InvalidRole;

	/**
	 * ストレージの一覧を取得する。
	 * 
	 * @param cloudScopeId
	 * @param locationId
	 * @return
	 * @throws CloudManagerException
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 */
	@WebMethod
	List<Storage> getAllStorages(@WebParam(name = "cloudScopeId") String cloudScopeId, @WebParam(name = "locationId") String locationId) throws CloudManagerException, InvalidUserPass, InvalidRole;

	/**
	 * ストレージの一覧を取得する。
	 * 
	 * @param cloudScopeId
	 * @param locationId
	 * @param storageIds
	 * @return
	 * @throws CloudManagerException
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 */
	@WebMethod
	List<Storage> getStorages(@WebParam(name = "cloudScopeId") String cloudScopeId, @WebParam(name = "locationId") String locationId, @WebParam(name = "storageIds") List<String> storageIds) throws CloudManagerException, InvalidUserPass, InvalidRole;
	

	/**
	 * ストレージのスナップショットを作成する。
	 * 
	 * @param cloudScopeId
	 * @param locationId
	 * @param request
	 * @return
	 * @throws CloudManagerException
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 */
	@WebMethod
	StorageBackup snapshotStorage(@WebParam(name = "cloudScopeId") String cloudScopeId, @WebParam(name = "locationId") String locationId, @WebParam(name = "request") CreateStorageSnapshotRequest request) throws CloudManagerException, InvalidUserPass, InvalidRole;

	/**
	 * ストレージのスナップショットを削除する。
	 * 
	 * @param cloudScopeId
	 * @param locationId
	 * @param storageId
	 * @param storageSnapshotIds
	 * @throws CloudManagerException
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 */
	@WebMethod
	void deleteStorageSnapshots(@WebParam(name = "cloudScopeId") String cloudScopeId, @WebParam(name = "locationId") String locationId, @WebParam(name = "storageId") String storageId, @WebParam(name = "storageSnapshotIds") List<String> storageSnapshotIds) throws CloudManagerException, InvalidUserPass, InvalidRole;

	/**
	 * スナップショットからストレージをクローンする。
	 * 
	 * @param cloudScopeId
	 * @param locationId
	 * @param request
	 * @return
	 * @throws CloudManagerException
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 */
	@WebMethod
	Storage cloneBackupedStorage(@WebParam(name = "cloudScopeId") String cloudScopeId, @WebParam(name = "locationId") String locationId, @WebParam(name = "request") CloneBackupedStorageRequest request) throws CloudManagerException, InvalidUserPass, InvalidRole;

	/**
	 * ストレージのスナップショットを取得する。
	 * 
	 * @param cloudScopeId
	 * @param locationId
	 * @param storageIds
	 * @return
	 * @throws CloudManagerException
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 */
	@WebMethod
	List<StorageBackup> getStorageBackups(@WebParam(name = "cloudScopeId") String cloudScopeId, @WebParam(name = "locationId") String locationId, @WebParam(name = "storageIds") List<String> storageIds) throws CloudManagerException, InvalidUserPass, InvalidRole;

	@WebMethod
	HRepository updateLocationRepository(@WebParam(name = "cloudScopeId") String cloudScopeId, @WebParam(name = "locationId") String locationId) throws CloudManagerException, InvalidUserPass, InvalidRole;

	/**
	 * プラットフォームのリポジトリ情報を取得する。
	 * 
	 * @return
	 * @throws CloudManagerException
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 */
	@WebMethod
	HRepository getRepository() throws CloudManagerException, InvalidUserPass, InvalidRole;

	/**
	 * プラットフォームのリポジトリ情報を取得する。
	 * 
	 * @param roleId
	 * @return
	 * @throws CloudManagerException
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 */
	@WebMethod
	HRepository getRepositoryByRole(@WebParam(name = "roleId") String roleId) throws CloudManagerException, InvalidUserPass, InvalidRole;

	/**
	 * プラットフォームのサービス状態の一覧を取得する。
	 * 
	 * @param cloudScopeId
	 * @return
	 * @throws CloudManagerException
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 */
	@WebMethod
	List<PlatformServiceCondition> getPlatformServiceConditions(@WebParam(name = "cloudScopeId") String cloudScopeId) throws CloudManagerException, InvalidUserPass, InvalidRole;

	/**
	 * プラットフォームのサービス状態の一覧を取得する。
	 * 
	 * @param cloudScopeId
	 * @param roleId
	 * @return
	 * @throws CloudManagerException
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 */
	@WebMethod
	List<PlatformServiceCondition> getPlatformServiceConditionsByRole(@WebParam(name = "cloudScopeId") String cloudScopeId, @WebParam(name = "roleId") String roleId) throws CloudManagerException, InvalidUserPass, InvalidRole;

	/**
	 * プラットフォームのサービス状態の一覧を取得する。
	 * 
	 * @param cloudScopeId
	 * @param locationId
	 * @return
	 * @throws CloudManagerException
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 */
	@WebMethod
	List<PlatformServiceCondition> getPlatformServiceConditionsByLocation(@WebParam(name = "cloudScopeId") String cloudScopeId, @WebParam(name = "locationId") String locationId) throws CloudManagerException, InvalidUserPass, InvalidRole;

	/**
	 * プラットフォームのサービス状態の一覧を取得する。
	 * 
	 * @param cloudScopeId
	 * @param locationId
	 * @param roleId
	 * @return
	 * @throws CloudManagerException
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 */
	@WebMethod
	List<PlatformServiceCondition> getPlatformServiceConditionsByLocationAndRole(@WebParam(name = "cloudScopeId") String cloudScopeId, @WebParam(name = "locationId") String locationId,@WebParam(name = "roleId") String roleId) throws CloudManagerException, InvalidUserPass, InvalidRole;

	@WebMethod
	List<String> getManagerFacilityIds() throws CloudManagerException, InvalidUserPass, InvalidRole;

	/**
	 * ネットワーク一覧を取得する。
	 * 
	 * @param cloudScopeId
	 * @param locationId
	 * @return
	 * @throws CloudManagerException
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 */
	@WebMethod
	List<Network> getAllNetworks(@WebParam(name = "cloudScopeId") String cloudScopeId, @WebParam(name = "locationId") String locationId) throws CloudManagerException, InvalidUserPass, InvalidRole;
	
	/**
	 * 課金設定を更新する。
	 * 
	 * @param request
	 * @throws CloudManagerException
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 */
	@WebMethod
	void modifyBillingSetting(@WebParam(name = "request") ModifyBillingSettingRequest request) throws CloudManagerException, InvalidUserPass, InvalidRole;

	/**
	 * 課金詳細情報を取得する。
	 * 
	 * @param cloudScopeId
	 * @param year
	 * @param month
	 * @return
	 * @throws CloudManagerException
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 */
	@WebMethod
	BillingResult getBillingDetailsByCloudScope(@WebParam(name = "cloudScopeId") String cloudScopeId, @WebParam(name = "year") int year, @WebParam(name = "month") int month) throws CloudManagerException, InvalidUserPass, InvalidRole;

	/**
	 * 課金詳細情報を取得する。
	 * 
	 * @param facilityId
	 * @param year
	 * @param month
	 * @return
	 * @throws CloudManagerException
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 */
	@WebMethod
	BillingResult getBillingDetailsByFacility(@WebParam(name = "facilityId") String facilityId, @WebParam(name = "year") int year, @WebParam(name = "month") int month) throws CloudManagerException, InvalidUserPass, InvalidRole;

	/**
	 * 課金詳細情報をダウンロードする。
	 * 
	 * @param cloudScopeId
	 * @param year
	 * @param month
	 * @return
	 * @throws CloudManagerException
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 */
	@WebMethod
	@XmlMimeType("application/octet-stream")
	DataHandler downloadBillingDetailsByCloudScope(@WebParam(name = "cloudScopeId") String cloudScopeId, @WebParam(name = "year") int year, @WebParam(name = "month") int month) throws CloudManagerException, InvalidUserPass, InvalidRole;

	/**
	 * 課金詳細情報をダウンロードする。
	 * 
	 * @param facilityId
	 * @param year
	 * @param month
	 * @return
	 * @throws CloudManagerException
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 */
	@WebMethod
	@XmlMimeType("application/octet-stream")
	DataHandler downloadBillingDetailsByFacility(@WebParam(name = "facilityId") String facilityId, @WebParam(name = "year") int year, @WebParam(name = "month") int month) throws CloudManagerException, InvalidUserPass, InvalidRole;

	/**
	 * 課金詳細情報を更新する。
	 * 
	 * @param cloudScopeId
	 * @throws CloudManagerException
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 */
	@WebMethod
	void refreshBillingDetails(@WebParam(name = "cloudScopeId") String cloudScopeId) throws CloudManagerException, InvalidUserPass, InvalidRole;

	/**
	 * プラットフォームのサービス一覧を取得する。
	 * 
	 * @param cloudScopeId
	 * @return
	 * @throws CloudManagerException
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 */
	@WebMethod
	List<String> getPlatformServices(@WebParam(name = "cloudScopeId") String cloudScopeId) throws CloudManagerException, InvalidUserPass, InvalidRole;
	
	/**
	 * プラットフォームごとのサービスを管理するクラス。
	 */
	public static class PlatformServices {
		private String platformId;
		private String serviceId;
		public String getPlatformId() {
			return platformId;
		}
		public void setPlatformId(String platformId) {
			this.platformId = platformId;
		}
		public String getServiceId() {
			return serviceId;
		}
		public void setServiceId(String serviceId) {
			this.serviceId = serviceId;
		}
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((platformId == null) ? 0 : platformId.hashCode());
			result = prime * result + ((serviceId == null) ? 0 : serviceId.hashCode());
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
			PlatformServices other = (PlatformServices) obj;
			if (platformId == null) {
				if (other.platformId != null)
					return false;
			} else if (!platformId.equals(other.platformId))
				return false;
			if (serviceId == null) {
				if (other.serviceId != null)
					return false;
			} else if (!serviceId.equals(other.serviceId))
				return false;
			return true;
		}
	}
	/**
	 * プラットフォームのサービス一覧を取得する。
	 * @param facilityId
	 * @param roleId
	 * @return
	 * @throws CloudManagerException
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 * @throws FacilityNotFound
	 * @throws HinemosUnknown
	 */
	@WebMethod
	List<PlatformServices> getAvailablePlatformServices(@WebParam(name = "facilityId") String facilityId, @WebParam(name = "roleId") String roleId) throws CloudManagerException, InvalidUserPass, InvalidRole,FacilityNotFound, HinemosUnknown;
	
	/**
	 * プラットフォームのサービス一覧を取得する。
	 * @param facilityId
	 * @param roleId
	 * @return
	 * @throws CloudManagerException
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 * @throws FacilityNotFound
	 * @throws HinemosUnknown
	 */
	@WebMethod
	List<PlatformServices> getAvailablePlatformServicesByUnlimited(@WebParam(name = "facilityId") String facilityId, @WebParam(name = "roleId") String roleId) throws CloudManagerException, InvalidUserPass, InvalidRole,FacilityNotFound, HinemosUnknown;
	
	/**
	 * マニュアルノード登録を行う。
	 * 
	 * @param cloudScopeId
	 * @param locationId
	 * @param instanceId
	 * @throws CloudManagerException
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 */
	@WebMethod
	void assignNodeToInstance(@WebParam(name = "cloudScopeId") String cloudScopeId, @WebParam(name = "locationId") String locationId, @WebParam(name = "instanceId") String instanceId) throws CloudManagerException, InvalidUserPass, InvalidRole;
	
	/**
	 * クラウドユーザーに紐付け可能なロールの一覧を取得する。
	 * 
	 * @return
	 * @throws CloudManagerException
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 */
	@WebMethod
	List<AvailableRole> getAvailableRoles() throws CloudManagerException, InvalidUserPass, InvalidRole;

	@WebMethod
	String getVersion() throws CloudManagerException, InvalidUserPass, InvalidRole;
}
