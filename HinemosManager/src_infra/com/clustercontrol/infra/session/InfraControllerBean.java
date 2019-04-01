/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.infra.session;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.persistence.EntityExistsException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.accesscontrol.util.RoleValidator;
import com.clustercontrol.commons.session.CheckFacility;
import com.clustercontrol.commons.util.HinemosSessionContext;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.FacilityNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InfraFileBeingUsed;
import com.clustercontrol.fault.InfraFileNotFound;
import com.clustercontrol.fault.InfraFileTooLarge;
import com.clustercontrol.fault.InfraManagementDuplicate;
import com.clustercontrol.fault.InfraManagementInvalid;
import com.clustercontrol.fault.InfraManagementNotFound;
import com.clustercontrol.fault.InfraModuleNotFound;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.NotifyDuplicate;
import com.clustercontrol.fault.NotifyNotFound;
import com.clustercontrol.fault.ObjectPrivilege_InvalidRole;
import com.clustercontrol.fault.SessionNotFound;
import com.clustercontrol.fault.UsedFacility;
import com.clustercontrol.infra.bean.AccessInfo;
import com.clustercontrol.infra.bean.InfraNodeInputConstant;
import com.clustercontrol.infra.bean.ModuleResult;
import com.clustercontrol.infra.factory.AsyncModuleWorker;
import com.clustercontrol.infra.factory.DownloadInfraFile;
import com.clustercontrol.infra.factory.ModifyInfraFile;
import com.clustercontrol.infra.factory.ModifyInfraManagement;
import com.clustercontrol.infra.factory.SelectInfraCheckResult;
import com.clustercontrol.infra.factory.SelectInfraManagement;
import com.clustercontrol.infra.model.InfraCheckResult;
import com.clustercontrol.infra.model.InfraFileInfo;
import com.clustercontrol.infra.model.InfraManagementInfo;
import com.clustercontrol.infra.util.InfraManagementValidator;
import com.clustercontrol.infra.util.QueryUtil;
import com.clustercontrol.notify.util.NotifyRelationCache;
import com.clustercontrol.platform.HinemosPropertyDefault;
import com.clustercontrol.util.MessageConstant;

/**
 * 環境構築機能の管理を行う Session Bean です。<BR>
 * クライアントからの Entity Bean へのアクセスは、Session Bean を介して行います。
 *
 * @version 5.1.0
 * @since 5.0.0
 */
public class InfraControllerBean implements CheckFacility {
	/** ログ出力のインスタンス。 */
	private static Log m_log = LogFactory.getLog( InfraControllerBean.class );
	private static final Object LOCK = new Object();

	/**
	 * 環境構築機能を作成します。
	 * @throws InfraManagementDuplicate 
	 *
	 */
	public boolean addInfraManagement(InfraManagementInfo info) 
			throws NotifyDuplicate, InvalidSetting, InvalidRole, HinemosUnknown, InfraManagementDuplicate, InfraManagementNotFound {
		JpaTransactionManager jtm = null;

		// 通知情報を登録
		boolean flag;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			
			//入力チェック
			InfraManagementValidator.validateInfraManagementInfo(info);
			
			//ユーザがオーナーロールIDに所属しているかチェック
			RoleValidator.validateUserBelongRole(info.getOwnerRoleId(),
					(String)HinemosSessionContext.instance().getProperty(HinemosSessionContext.LOGIN_USER_ID),
					(Boolean)HinemosSessionContext.instance().getProperty(HinemosSessionContext.IS_ADMINISTRATOR));

			ModifyInfraManagement proc = new ModifyInfraManagement();
			flag = proc.add(info, (String)HinemosSessionContext.instance().getProperty(HinemosSessionContext.LOGIN_USER_ID));

			jtm.commit();

			// コミット後にキャッシュクリア
			NotifyRelationCache.refresh();
		} catch (InfraManagementNotFound | NotifyDuplicate | HinemosUnknown | InvalidSetting e) {
			if (jtm != null){
				jtm.rollback();
			}
			throw e;
		} catch (EntityExistsException e) {
			if (jtm != null)
				jtm.rollback();
			throw new InfraManagementDuplicate(e.getMessage(), e);
		} catch (ObjectPrivilege_InvalidRole e) {
			if (jtm != null)
				jtm.rollback();
			throw new InvalidRole(e.getMessage(), e);
		} catch (Exception e){
			m_log.warn("addInfraManagement() : " + e.getClass().getSimpleName() +
					", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null)
				jtm.close();
		}

		return  flag;
	}

	/**
	 * 環境構築機能を変更します。
	 * @throws InfraManagementDuplicate 
	 *
	 */
	public boolean modifyInfraManagement(InfraManagementInfo info) throws InfraManagementNotFound, NotifyDuplicate, NotifyNotFound, InvalidRole, HinemosUnknown, InvalidSetting, InfraManagementDuplicate {
		JpaTransactionManager jtm = null;

		// 通知情報を更新
		boolean flag;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			//入力チェック
			InfraManagementValidator.validateInfraManagementInfo(info);

			ModifyInfraManagement proc = new ModifyInfraManagement();
			flag = proc.modify(info, (String)HinemosSessionContext.instance().getProperty(HinemosSessionContext.LOGIN_USER_ID));

			jtm.commit();

			// コミット後にキャッシュクリア
			NotifyRelationCache.refresh();
		} catch (NotifyDuplicate | NotifyNotFound | HinemosUnknown | InvalidRole | InfraManagementNotFound | InvalidSetting e ){
			if (jtm != null){
				jtm.rollback();
			}
			throw e;
		} catch (EntityExistsException e) {
			if (jtm != null)
				jtm.rollback();
			throw new InfraManagementDuplicate(e.getMessage(), e);
		} catch (ObjectPrivilege_InvalidRole e) {
			if (jtm != null)
				jtm.rollback();
			throw new InvalidRole(e.getMessage(), e);
		} catch (Exception e){
			m_log.warn("modifyInfraManagement() : " + e.getClass().getSimpleName() +
					", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null)
				jtm.close();
		}
		return flag;
	}

	/**
	 */
	public boolean deleteInfraManagement(String[] infraManagementIds) throws InfraManagementNotFound, InvalidSetting, InvalidRole, HinemosUnknown {
		JpaTransactionManager jtm = null;

		// 通知情報を削除
		ModifyInfraManagement proc = new ModifyInfraManagement();
		boolean flag = true;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			for (String infraManagementId: infraManagementIds) {
				flag = flag && proc.delete(infraManagementId);
			}

			jtm.commit();

			// コミット後にキャッシュクリア
			NotifyRelationCache.refresh();
		} catch(HinemosUnknown | InvalidSetting | InvalidRole | InfraManagementNotFound e){
			if (jtm != null){
				jtm.rollback();
			}
			throw e;
		} catch (ObjectPrivilege_InvalidRole e) {
			if (jtm != null)
				jtm.rollback();
			throw new InvalidRole(e.getMessage(), e);
		} catch (Exception e){
			m_log.warn("deleteInfraManagement() : " + e.getClass().getSimpleName() +
					", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null)
				jtm.close();
		}
		return flag;
	}

	/**
	 */
	public InfraManagementInfo getInfraManagement(String infraManagementId) throws InvalidRole, HinemosUnknown, InfraManagementNotFound {
		JpaTransactionManager jtm = null;

		// 通知情報を取得
		SelectInfraManagement proc = new SelectInfraManagement();
		InfraManagementInfo info = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			info = proc.get(infraManagementId, null, ObjectPrivilegeMode.READ);
			jtm.commit();
		} catch (HinemosUnknown | InvalidRole | InfraManagementNotFound e){
			if (jtm != null){
				jtm.rollback();
			}
			throw e;
		} catch (ObjectPrivilege_InvalidRole e) {
			if (jtm != null)
				jtm.rollback();
			throw new InvalidRole(e.getMessage(), e);
		} catch (Exception e) {
			m_log.warn("getInfraManagement() : " + e.getClass().getSimpleName() +
					", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null)
				jtm.close();
		}

		return info;
	}

	/**
	 */
	public List<InfraManagementInfo> getInfraManagementList() throws InvalidRole, HinemosUnknown {
		JpaTransactionManager jtm = null;

		// 通知一覧を取得
		SelectInfraManagement proc = new SelectInfraManagement();
		List<InfraManagementInfo> list = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			list = proc.getList();
			jtm.commit();
		} catch (InvalidRole e) {
			jtm.rollback();
			throw e;
		} catch (ObjectPrivilege_InvalidRole e) {
			if (jtm != null)
				jtm.rollback();
			throw new InvalidRole(e.getMessage(), e);
		} catch (Exception e) {
			m_log.warn("getInfraManagementList() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null)
				jtm.close();
		}

		return list;
	}

	/**
	 *
	 */
	public List<InfraManagementInfo> getInfraManagementListByOwnerRole(String ownerRoleId) throws InvalidRole, HinemosUnknown {
		JpaTransactionManager jtm = null;

		// 通知一覧を取得
		SelectInfraManagement proc = new SelectInfraManagement();
		List<InfraManagementInfo> list = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			list = proc.getListByOwnerRole(ownerRoleId);
			jtm.commit();
		} catch (ObjectPrivilege_InvalidRole e) {
			if (jtm != null)
				jtm.rollback();
			throw new InvalidRole(e.getMessage(), e);
		} catch (Exception e) {
			m_log.warn("getInfraManagementListByOwnerRole() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null)
				jtm.close();
		}

		return list;
	}

	/**
	 * 参照環境構築モジュールの選択対象一覧を返す
	 * 
	 * @param ownerRoleId オーナーロールID
	 * @return 参照環境構築モジュールの選択対象の環境構築IDリスト
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	public List<String> getReferManagementIdList(String ownerRoleId) throws InvalidRole, HinemosUnknown {
		JpaTransactionManager jtm = null;

		List<String> list = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			list = new SelectInfraManagement().getReferManagementIdList(ownerRoleId);
			jtm.commit();
		} catch (ObjectPrivilege_InvalidRole e) {
			if (jtm != null)
				jtm.rollback();
			throw new InvalidRole(e.getMessage(), e);
		} catch (Exception e) {
			m_log.warn("getReferManagementIdList() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null)
				jtm.close();
		}

		return list;
	}

	/**
	 * ログイン情報ダイアログ用のアクセス情報を作成する
	 * 
	 * @param managementId 環境構築ID
	 * @param moduleIdList モジュールIDリスト
	 * @return アクセス情報のリスト
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 * @throws InfraManagementNotFound
	 */
	public List<AccessInfo> createAccessInfoListForDialog(String managementId, List<String> moduleIdList)
			throws InvalidRole, HinemosUnknown, InfraManagementNotFound {
		JpaTransactionManager jtm = null;

		// アクセス情報を作成する
		List<AccessInfo> list = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			list = new SelectInfraManagement().createAccessInfoList(managementId, InfraNodeInputConstant.TYPE_DIALOG, null, moduleIdList);
			jtm.commit();
		} catch (HinemosUnknown | InvalidRole | InfraManagementNotFound e){
			if (jtm != null){
				jtm.rollback();
			}
			throw e;
		} catch (Exception e) {
			m_log.warn("createAccessInfoList() : " + e.getClass().getSimpleName() +
					", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null)
				jtm.close();
		}

		return list;
	}

	/**
	 * 指定したファシリティIDが利用されているか確認する
	 *
	 *
	 */
	@Override
	public void isUseFacilityId(String facilityId) throws UsedFacility {
		JpaTransactionManager jtm = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			// ファシリティIDが使用されている設定を取得する。
			Collection<InfraManagementInfo> ct = QueryUtil.getInfraManagementInfoFindByFacilityId_NONE(facilityId);

			if(ct != null && ct.size() > 0) {
				// ID名を取得する
				StringBuilder sb = new StringBuilder();
				sb.append(MessageConstant.INFRA_MANAGEMENT.getMessage() + " : ");
				for (InfraManagementInfo entity : ct) {
					sb.append(entity.getManagementId());
					sb.append(", ");
				}
				UsedFacility e = new UsedFacility(sb.toString());
				m_log.info("isUseFacilityId() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
			jtm.commit();
		} catch (UsedFacility e) {
			jtm.rollback();
			throw e;
		} catch (Exception e) {
			m_log.warn("isUseFacilityId() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
		} finally {
			if (jtm != null)
				jtm.close();
		}
	}

	/**
	 * セッション作成
	 * 
	 * @param infraManagementId 環境構築ID
	 * @param moduleIdList モジュールIDリスト
	 * @param nodeInputType ログイン情報設定種別（InfraNodeInputConstant）
	 * @param accessList ログイン情報リスト
	 * @return アクセス情報のリスト
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 * @throws InfraManagementNotFound
	 */
	public String createSession(String infraManagementId, List<String> moduleIdList, Integer nodeInputType, List<AccessInfo> accessList)
			throws InfraManagementNotFound, InfraModuleNotFound, InfraManagementInvalid, InvalidRole, HinemosUnknown, FacilityNotFound, InvalidSetting {

		// モジュール件数の確認
		new SelectInfraManagement().checkInfraModuleCount(infraManagementId, moduleIdList);

		if (nodeInputType == InfraNodeInputConstant.TYPE_INFRA_PARAM
				|| nodeInputType == InfraNodeInputConstant.TYPE_NODE_PARAM) {
			// ログイン情報種別が環境構築変数、もしくはノードプロパティから取得、かつ通知からの遷移の場合
			List<String> facilityIdList = null;
			if (accessList != null) {
				facilityIdList = new ArrayList<>();
				for (AccessInfo accessInfo : accessList) {
					facilityIdList.add(accessInfo.getFacilityId());
				}
			}
			accessList = new SelectInfraManagement().createAccessInfoList(infraManagementId, nodeInputType, facilityIdList, moduleIdList);
		}
		return AsyncModuleWorker.createSession(infraManagementId, moduleIdList, accessList);
	}

	public boolean deleteSession(String sessionId) throws InfraManagementNotFound, InvalidRole, HinemosUnknown {
		return AsyncModuleWorker.deleteSession(sessionId);
	}
	
	/**
	 * 
	 * @param sessionId
	 * @return
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InfraManagementNotFound
	 * @throws InfraModuleNotFound
	 * @throws InvalidUserPass
	 * @throws SessionNotFound
	 */
	public ModuleResult runInfraModule(String sessionId)
			throws HinemosUnknown, InvalidRole, InfraManagementNotFound, InfraModuleNotFound, InvalidUserPass, SessionNotFound {
		JpaTransactionManager jtm = null;
		ModuleResult ret = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			ret = AsyncModuleWorker.runInfraModule(sessionId);
			jtm.commit();
		} catch (HinemosUnknown e) {
			jtm.rollback();
			throw e;
		} finally {
			if (jtm != null) {
				jtm.close();
			}
		}
		return ret;
	}

	/**
	 * 
	 * @param sessionId
	 * @return
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InfraManagementNotFound
	 * @throws InfraModuleNotFound
	 * @throws InvalidUserPass
	 * @throws SessionNotFound
	 */
	public ModuleResult checkInfraModule(String sessionId, boolean verbose)
			throws HinemosUnknown, InvalidRole, InfraManagementNotFound, InfraModuleNotFound, InvalidUserPass, SessionNotFound {
		JpaTransactionManager jtm = null;
		ModuleResult ret = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			ret = AsyncModuleWorker.checkInfraModule(sessionId, verbose);
			jtm.commit();
		} catch (HinemosUnknown e) {
			jtm.rollback();
			throw e;
		} finally {
			if (jtm != null) {
				jtm.close();
			}
		}
		return ret;
	}
	

	public List<InfraCheckResult> getCheckResultList(String managementId) throws HinemosUnknown, InvalidRole {
		JpaTransactionManager jtm = null;

		SelectInfraCheckResult select = new SelectInfraCheckResult();
		List<InfraCheckResult> resultList = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			resultList = select.getListByManagementId(managementId);
			jtm.commit();
		} catch (HinemosUnknown | InvalidRole e){
			if (jtm != null){
				jtm.rollback();
			}
			throw e;
		} catch (ObjectPrivilege_InvalidRole e) {
			if (jtm != null)
				jtm.rollback();
			throw new InvalidRole(e.getMessage(), e);
		} catch (Exception e) {
			m_log.warn("getCheckResultList() : " + e.getClass().getSimpleName() +
					", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null)
				jtm.close();
		}

		return resultList;
	}

	public void addInfraFile(InfraFileInfo fileInfo, DataHandler fileContent) throws InvalidRole, HinemosUnknown, InfraFileTooLarge, InfraManagementDuplicate {
		synchronized (LOCK) {
			JpaTransactionManager jtm = null;
			try {
				jtm = new JpaTransactionManager();
				jtm.begin();
				
				//入力チェック
				InfraManagementValidator.validateInfraFileInfo(fileInfo);
				
				//ユーザがオーナーロールIDに所属しているかチェック
				RoleValidator.validateUserBelongRole(fileInfo.getOwnerRoleId(),
						(String)HinemosSessionContext.instance().getProperty(HinemosSessionContext.LOGIN_USER_ID),
						(Boolean)HinemosSessionContext.instance().getProperty(HinemosSessionContext.IS_ADMINISTRATOR));
				
				if (fileContent == null) {
					InvalidSetting e = new InvalidSetting("fileContent is not defined.");
					m_log.info("addInfraFile() : "
							+ e.getClass().getSimpleName() + ", " + e.getMessage());
					throw e;
				}
	
				String userId = (String)HinemosSessionContext.instance().getProperty(HinemosSessionContext.LOGIN_USER_ID);
				new ModifyInfraFile().add(fileInfo, fileContent, userId);
	
				jtm.commit();
			} catch (EntityExistsException e) {
				if (jtm != null)
					jtm.rollback();
				throw new InfraManagementDuplicate(e.getMessage(), e);
			} catch (ObjectPrivilege_InvalidRole e) {
				if (jtm != null)
					jtm.rollback();
				throw new InvalidRole(e.getMessage(), e);
			} catch (InfraFileTooLarge e) {
				jtm.rollback();
				throw e;
			} catch (InvalidSetting e) {
				//本来はInvalidSettingをそのままthrowすべきだが、本IFで
				//InvalidSettingをthrows宣言していないため、
				//IFの互換性を考慮し、HinemosUnknownとすうる
				if (jtm != null)
					jtm.rollback();
				throw new HinemosUnknown(e.getMessage(), e);
			} catch (Exception e){
				m_log.warn("addInfraFile() : " + e.getClass().getSimpleName() +
						", " + e.getMessage(), e);
				if (jtm != null)
					jtm.rollback();
				throw new HinemosUnknown(e.getMessage(), e);
			} finally {
				if (jtm != null) {
					jtm.close();
				}
			}
		}
	}

	public List<InfraFileInfo> getInfraFileList() {
		JpaTransactionManager jtm = null;

		List<InfraFileInfo> list = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			
			list = QueryUtil.getAllInfraFile();
			
			jtm.commit();
		} finally {
			if (jtm != null)
				jtm.close();
		}

		return list;
	}

	public List<InfraFileInfo> getInfraFileListByOwnerRoleId(String ownerRoleId) {
		JpaTransactionManager jtm = null;

		List<InfraFileInfo> list = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			
			list = QueryUtil.getAllInfraFile_OR(ownerRoleId);
			
			jtm.commit();
		} finally {
			if (jtm != null)
				jtm.close();
		}

		return list;
	}

	public void deleteInfraFileList(List<String> fileIdList) throws InvalidRole, HinemosUnknown, InfraFileNotFound, InfraFileBeingUsed {
		JpaTransactionManager jtm = null;
		ModifyInfraFile proc = new ModifyInfraFile();
		
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			
			for (String fileId : fileIdList) {
				proc.delete(fileId);
			}
			
			jtm.commit();
		} finally {
			if (jtm != null)
				jtm.close();
		}
	}

	public DataHandler downloadInfraFile(String fileId, String fileName) throws InvalidSetting, InfraFileNotFound, HinemosUnknown, IOException {
		synchronized (LOCK) {
			DataHandler dh = null;
			JpaTransactionManager jtm = null;
			try {
				jtm = new JpaTransactionManager();
				jtm.begin();
				
				//入力チェック
				if (fileId == null) {
					InvalidSetting e = new InvalidSetting("fileId is not defined.");
					m_log.info("downloadInfraFile() : "
							+ e.getClass().getSimpleName() + ", " + e.getMessage());
					throw e;
				}
				
				dh = new DownloadInfraFile().download(fileId, fileName);
	
				jtm.commit();
			} finally {
				if (jtm != null) {
					jtm.close();
				}
			}
			
			return dh;
		}
	}
	
	public void deleteDownloadedInfraFile(String fileName) {
		String exportDirectory = HinemosPropertyDefault.infra_export_dir.getStringValue();
		File file = new File(exportDirectory + "/" + fileName);
		if (!file.delete())
			m_log.debug("Fail to delete " + file.getAbsolutePath());
	}

	public void modifyInfraFile(InfraFileInfo fileInfo, DataHandler fileContent) throws InvalidRole, HinemosUnknown, InfraFileTooLarge {
		synchronized (LOCK) {
			JpaTransactionManager jtm = null;
			try {
				jtm = new JpaTransactionManager();
				jtm.begin();
				
				//入力チェック
				InfraManagementValidator.validateInfraFileInfo(fileInfo);
				
				String userId = (String)HinemosSessionContext.instance().getProperty(HinemosSessionContext.LOGIN_USER_ID);
				new ModifyInfraFile().modify(fileInfo, fileContent, userId);
	
				jtm.commit();
			} catch (ObjectPrivilege_InvalidRole e) {
				if (jtm != null)
					jtm.rollback();
				throw new InvalidRole(e.getMessage(), e);
			} catch (InfraFileTooLarge e) {
				jtm.rollback();
				throw e;
			} catch (InvalidSetting e) {
				//本来はInvalidSettingをそのままthrowすべきだが、本IFで
				//InvalidSettingをthrows宣言していないため、
				//IFの互換性を考慮し、HinemosUnknownとすうる
				if (jtm != null)
					jtm.rollback();
				throw new HinemosUnknown(e.getMessage(), e);
			} catch (Exception e){
				m_log.warn("modifyInfraFile() : " + e.getClass().getSimpleName() +
						", " + e.getMessage(), e);
				if (jtm != null)
					jtm.rollback();
				throw new HinemosUnknown(e.getMessage(), e);
			} finally {
				if (jtm != null) {
					jtm.close();
				}
			}
		}
	}

	public DataHandler downloadTransferFile(String fileName) {
		m_log.info("downloadTransferFile fileName="+fileName);
		String infraDirectory = HinemosPropertyDefault.infra_transfer_dir.getStringValue()
				+ File.separator + "send" + File.separator;
		FileDataSource fileData = new FileDataSource(infraDirectory + fileName);
		return new DataHandler(fileData);
	}
}
