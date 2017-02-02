/*

 Copyright (C) 2006 NTT DATA Corporation

 This program is free software; you can redistribute it and/or
 Modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation, version 2.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 */

package com.clustercontrol.performance.session;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityExistsException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.CollectorNotFound;
import com.clustercontrol.fault.FacilityNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.performance.bean.CollectMasterInfo;
import com.clustercontrol.performance.factory.OperateCollectCalcMaster;
import com.clustercontrol.performance.factory.OperateCollectCategoryCollectMaster;
import com.clustercontrol.performance.factory.OperateCollectCategoryMaster;
import com.clustercontrol.performance.factory.OperateCollectItemCalcMethodMaster;
import com.clustercontrol.performance.factory.OperateCollectItemCodeMaster;
import com.clustercontrol.performance.factory.OperateCollectPollingMaster;
import com.clustercontrol.performance.monitor.entity.CollectorCalcMethodMstData;
import com.clustercontrol.performance.monitor.entity.CollectorCategoryCollectMstData;
import com.clustercontrol.performance.monitor.entity.CollectorCategoryCollectMstPK;
import com.clustercontrol.performance.monitor.entity.CollectorCategoryMstData;
import com.clustercontrol.performance.monitor.entity.CollectorItemCalcMethodMstData;
import com.clustercontrol.performance.monitor.entity.CollectorItemCalcMethodMstPK;
import com.clustercontrol.performance.monitor.entity.CollectorItemCodeMstData;
import com.clustercontrol.performance.monitor.entity.CollectorPollingMstData;
import com.clustercontrol.performance.monitor.entity.CollectorPollingMstPK;
import com.clustercontrol.repository.bean.FacilityTreeAttributeConstant;
import com.clustercontrol.repository.entity.CollectorPlatformMstData;
import com.clustercontrol.repository.entity.CollectorSubPlatformMstData;
import com.clustercontrol.repository.model.CollectorPlatformMstEntity;
import com.clustercontrol.repository.model.CollectorSubPlatformMstEntity;
import com.clustercontrol.repository.model.ScopeInfo;
import com.clustercontrol.repository.session.RepositoryControllerBean;

/**
 * 収集項目マスタ情報を制御するSession Bean <BR>
 * 
 */
public class PerformanceCollectMasterControllerBean {

	private static Log m_log = LogFactory.getLog( PerformanceCollectMasterControllerBean.class );

	/**
	 * 計算方法定義を登録します。
	 * 
	 * @param data 計算方法定義情報
	 * @return 登録に成功した場合、true
	 * @throws HinemosUnknown
	 */
	public boolean addCollectCalcMaster(CollectorCalcMethodMstData data) throws HinemosUnknown {
		JpaTransactionManager jtm = null;

		// 計算ロジック情報を登録
		OperateCollectCalcMaster ope = new OperateCollectCalcMaster();
		boolean ret = false;

		try{
			jtm = new JpaTransactionManager();
			jtm.begin();

			ret = ope.add(data);

			jtm.commit();
		} catch (EntityExistsException e) {
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} catch (Exception e){
			m_log.warn("addCollectCalcMaster() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null)
				jtm.close();
		}

		return ret;

	}

	/**
	 * カテゴリ毎の収集方法定義を登録します。
	 * 
	 * @param data カテゴリ毎の収集方法定義情報
	 * @return 登録に成功した場合、true
	 * @throws HinemosUnknown
	 */
	public boolean addCollectCategoryCollectMaster(CollectorCategoryCollectMstData data) throws HinemosUnknown {
		JpaTransactionManager jtm = null;

		//カテゴリ毎の収集方法定義を登録
		OperateCollectCategoryCollectMaster ope = new OperateCollectCategoryCollectMaster();
		boolean ret = false;

		try{
			jtm = new JpaTransactionManager();
			jtm.begin();

			ret = ope.add(data);

			jtm.commit();
		} catch (EntityExistsException e) {
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} catch (Exception e){
			m_log.warn("addCollectCategoryCollectMaster() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null)
				jtm.close();
		}

		return ret;

	}

	/**
	 * 収集カテゴリ定義を登録します。
	 * 
	 * @param data 収集カテゴリ定義情報
	 * @return 登録に成功した場合、true
	 * @throws HinemosUnknown
	 */
	public boolean addCollectCategoryMaster(CollectorCategoryMstData data) throws HinemosUnknown {
		JpaTransactionManager jtm = null;

		// 収集カテゴリ定義を登録
		OperateCollectCategoryMaster ope = new OperateCollectCategoryMaster();
		boolean ret = false;

		try{
			jtm = new JpaTransactionManager();
			jtm.begin();

			ret = ope.add(data);

			jtm.commit();
		} catch (EntityExistsException e) {
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		}catch(Exception e){
			m_log.warn("addCollectCategoryMaster() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null)
				jtm.close();
		}

		return ret;

	}

	/**
	 * 収集項目毎の計算方法を登録します。
	 * 
	 * @param data 収集項目毎の計算方法定義情報
	 * @return 登録に成功した場合、true
	 * @throws HinemosUnknown
	 */
	public boolean addCollectItemCalcMethodMaster(CollectorItemCalcMethodMstData data) throws HinemosUnknown {
		JpaTransactionManager jtm = null;

		// 収集項目毎の計算方法を登録
		OperateCollectItemCalcMethodMaster ope = new OperateCollectItemCalcMethodMaster();
		boolean ret = false;

		try{
			jtm = new JpaTransactionManager();
			jtm.begin();

			ret = ope.add(data);

			jtm.commit();
		} catch (EntityExistsException e) {
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} catch (Exception e) {
			m_log.warn("addCollectItemCalcMethodMaster() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null)
				jtm.close();
		}

		return ret;
	}

	/**
	 * 収集項目コードを登録します。
	 * 
	 * @param data 収集項目コード情報
	 * @return 登録に成功した場合、true
	 * @throws HinemosUnknown
	 */
	public boolean addCollectItemCodeMaster(CollectorItemCodeMstData data) throws HinemosUnknown {
		JpaTransactionManager jtm = null;

		// 収集項目コードを登録
		OperateCollectItemCodeMaster ope = new OperateCollectItemCodeMaster();
		boolean ret = false;

		try{
			jtm = new JpaTransactionManager();
			jtm.begin();

			ret = ope.add(data);

			jtm.commit();
		} catch (EntityExistsException e) {
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} catch (Exception e) {
			m_log.warn("addCollectItemCodeMaster() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null)
				jtm.close();
		}

		return ret;

	}

	/**
	 * 収集方法・プラットフォーム毎の収集項目を登録します。
	 * 
	 * @param data 収集方法・プラットフォーム毎の収集項目情報
	 * @return 登録に成功した場合、true
	 * @throws HinemosUnknown
	 */
	public boolean addCollectPollingMaster(CollectorPollingMstData data) throws HinemosUnknown {
		JpaTransactionManager jtm = null;

		// 収集方法・プラットフォーム毎の収集項目を登録
		OperateCollectPollingMaster ope = new OperateCollectPollingMaster();
		boolean ret = false;

		try{
			jtm = new JpaTransactionManager();
			jtm.begin();

			ret = ope.add(data);

			jtm.commit();
		} catch (EntityExistsException e) {
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		}catch(Exception e){
			m_log.warn("addCollectPollingMaster() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null)
				jtm.close();
		}

		return ret;

	}

	/**
	 * プラットフォーム定義を登録します。
	 * 
	 * @param data プラットフォーム定義情報
	 * @return 登録に成功した場合、true
	 * @throws HinemosUnknown
	 */
	public boolean addCollectPlatformMaster(CollectorPlatformMstData data) throws HinemosUnknown {
		JpaTransactionManager jtm = null;

		try{
			jtm = new JpaTransactionManager();
			jtm.begin();

			// プラットフォーム定義情報を登録
			RepositoryControllerBean repositoryControllerBean = new RepositoryControllerBean();
			repositoryControllerBean.addCollectorPratformMst(data);
			
			String osParentFacilityId = FacilityTreeAttributeConstant.OS_PARENT_SCOPE;
			ScopeInfo osParentScopeInfo = repositoryControllerBean.getScope(osParentFacilityId);
			ScopeInfo scopeInfo = new ScopeInfo();
			scopeInfo.setFacilityId(data.getPlatformId());
			scopeInfo.setFacilityName(data.getPlatformName());
			scopeInfo.setDescription(data.getPlatformName());
			scopeInfo.setFacilityType(osParentScopeInfo.getFacilityType());
			scopeInfo.setIconImage(osParentScopeInfo.getIconImage());
			scopeInfo.setOwnerRoleId(osParentScopeInfo.getOwnerRoleId());
			repositoryControllerBean.addScope(osParentFacilityId, scopeInfo, data.getOrderNo());

			jtm.commit();
		}catch(EntityExistsException e){
			// プラットフォーム情報の追加に失敗した場合。
			// 既にあるものは残したままで追加させるロジックのため、ロールバックはせず追加できなかったことをフラグで示す。
			if (jtm != null)
				jtm.rollback();
			return false;
		}catch(Exception e){
			m_log.warn("addCollectPlatformMaster() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null)
				jtm.close();
		}

		return true;

	}

	/**
	 * サブプラットフォーム定義を登録します。
	 * 
	 * @param data サブプラットフォーム定義情報
	 * @return 登録に成功した場合、true
	 * @throws HinemosUnknown
	 */
	public boolean addCollectSubPlatformMaster(CollectorSubPlatformMstData data) throws HinemosUnknown {
		JpaTransactionManager jtm = null;

		try{
			jtm = new JpaTransactionManager();
			jtm.begin();

			// サブプラットフォーム定義情報を登録
			new RepositoryControllerBean().addCollectorSubPlatformMst(data);

			jtm.commit();
		}catch(EntityExistsException e){
			// サブプラットフォーム情報の追加に失敗した場合。
			// 既にあるものは残したままで追加させるロジックのため、ロールバックはせず追加できなかったことをフラグで示す。
			if (jtm != null)
				jtm.rollback();
			return false;
		}catch(Exception e){
			m_log.warn("addCollectSubPlatformMaster() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null)
				jtm.close();
		}

		return true;

	}

	/**
	 * 収集項目マスタデータを一括で登録します。
	 * 
	 * @return 登録に成功した場合、true
	 * @throws HinemosUnknown
	 */
	public boolean addCollectMaster(CollectMasterInfo collectMasterInfo) throws HinemosUnknown {
		JpaTransactionManager jtm = null;
		boolean rtnFlg = true;

		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			m_log.debug("addCollectCategoryMaster");
			for(CollectorCategoryMstData data : collectMasterInfo.getCollectorCategoryMstDataList()){
				rtnFlg = rtnFlg && addCollectCategoryMaster(data);
			}
			m_log.debug("addCollectItemCodeMaster");
			for(CollectorItemCodeMstData data : collectMasterInfo.getCollectorItemCodeMstDataList()){
				rtnFlg = rtnFlg && addCollectItemCodeMaster(data);
			}
			m_log.debug("addCollectCalcMaster");
			for(CollectorCalcMethodMstData data : collectMasterInfo.getCollectorCalcMethodMstDataList()){
				rtnFlg = rtnFlg && addCollectCalcMaster(data);
			}
			m_log.debug("addCollectItemCalcMethodMaster");
			for(CollectorItemCalcMethodMstData data : collectMasterInfo.getCollectorItemCalcMethodMstDataList()){
				rtnFlg = rtnFlg && addCollectItemCalcMethodMaster(data);
			}
			m_log.debug("addCollectPollingMaster");
			for(CollectorPollingMstData data : collectMasterInfo.getCollectorPollingMstDataList()){
				rtnFlg = rtnFlg && addCollectPollingMaster(data);
			}
			m_log.debug("addCollectCategoryCollectMaster");
			for(CollectorCategoryCollectMstData data : collectMasterInfo.getCollectorCategoryCollectMstDataList()){
				rtnFlg = rtnFlg && addCollectCategoryCollectMaster(data);
			}

			jtm.commit();
		} catch (HinemosUnknown e) {
			jtm.rollback();
			rtnFlg = false;
			throw e;
		} catch (Exception e) {
			m_log.warn("addCollectMaster() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			rtnFlg = false;
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null)
				jtm.close();
		}

		return rtnFlg;
	}

	/**
	 * 計算方法定義を削除します。
	 * 
	 * @param data 計算方法定義情報
	 * @return 削除に成功した場合、true
	 * @throws CollectorNotFound
	 * @throws HinemosUnknown
	 */
	public boolean deleteCollectCalcMaster(String calcMethod) throws CollectorNotFound, HinemosUnknown {
		JpaTransactionManager jtm = null;

		// 計算ロジック情報を削除
		OperateCollectCalcMaster ope = new OperateCollectCalcMaster();
		boolean ret = false;

		try{
			jtm = new JpaTransactionManager();
			jtm.begin();

			ret = ope.delete(calcMethod);

			jtm.commit();
		} catch (CollectorNotFound e) {
			jtm.rollback();
			throw e;
		} catch (Exception e){
			m_log.warn("deleteCollectCalcMaster() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null)
				jtm.close();
		}

		return ret;

	}

	/**
	 * 計算方法定義を全て削除します。
	 * 
	 * @return 削除に成功した場合、true
	 * @throws HinemosUnknown
	 */
	public boolean deleteCollectCalcMasterAll() throws HinemosUnknown {
		JpaTransactionManager jtm = null;

		// 計算ロジック情報を削除
		OperateCollectCalcMaster ope = new OperateCollectCalcMaster();
		boolean ret = false;

		try{
			jtm = new JpaTransactionManager();
			jtm.begin();

			ret = ope.deleteAll();

			jtm.commit();
		} catch (Exception e){
			m_log.warn("deleteCollectCalcMasterAll() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null)
				jtm.close();
		}

		return ret;

	}

	/**
	 * カテゴリ毎の収集方法定義を削除します。
	 * 
	 * @param data カテゴリ毎の収集方法定義情報
	 * @return 削除に成功した場合、true
	 * @throws CollectorNotFound
	 * @throws HinemosUnknown
	 */
	public boolean deleteCollectCategoryCollectMaster(CollectorCategoryCollectMstPK pk) throws CollectorNotFound, HinemosUnknown {
		JpaTransactionManager jtm = null;

		//カテゴリ毎の収集方法定義を削除
		OperateCollectCategoryCollectMaster ope = new OperateCollectCategoryCollectMaster();

		boolean ret = false;

		try{
			jtm = new JpaTransactionManager();
			jtm.begin();

			ret = ope.delete(pk);

			jtm.commit();
		} catch (CollectorNotFound e) {
			jtm.rollback();
			throw e;
		} catch (Exception e){
			m_log.warn("deleteCollectCategoryCollectMaster() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null)
				jtm.close();
		}

		return ret;

	}

	/**
	 * カテゴリ毎の収集方法定義を全て削除します。
	 * 
	 * @return 削除に成功した場合、true
	 * @throws HinemosUnknown
	 */
	public boolean deleteCollectCategoryCollectMasterAll() throws HinemosUnknown {
		JpaTransactionManager jtm = null;

		//カテゴリ毎の収集方法定義を削除
		OperateCollectCategoryCollectMaster ope = new OperateCollectCategoryCollectMaster();
		boolean ret = false;

		try{
			jtm = new JpaTransactionManager();
			jtm.begin();

			ret = ope.deleteAll();

			jtm.commit();
		} catch (Exception e){
			m_log.warn("deleteCollectCategoryCollectMasterAll() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null)
				jtm.close();
		}

		return ret;

	}

	/**
	 * 収集カテゴリ定義を削除します。
	 * 
	 * @param data 収集カテゴリ定義情報
	 * @return 削除に成功した場合、true
	 * @throws CollectorNotFound
	 * @throws HinemosUnknown
	 */
	public boolean deleteCollectCategoryMaster(String categoryCode) throws CollectorNotFound, HinemosUnknown {
		JpaTransactionManager jtm = null;

		// 収集カテゴリ定義を削除
		OperateCollectCategoryMaster ope = new OperateCollectCategoryMaster();
		boolean ret = false;

		try{
			jtm = new JpaTransactionManager();
			jtm.begin();

			ret = ope.delete(categoryCode);

			jtm.commit();
		}catch(CollectorNotFound e){
			jtm.rollback();
			throw e;
		} catch (Exception e){
			m_log.warn("deleteCollectCategoryMaster() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null)
				jtm.close();
		}

		return ret;

	}

	/**
	 * 収集カテゴリ定義を全て削除します。
	 * 
	 * @return 削除に成功した場合、true
	 * @throws HinemosUnknown
	 */
	public boolean deleteCollectCategoryMasterAll() throws HinemosUnknown {
		JpaTransactionManager jtm = null;

		// 収集カテゴリ定義を削除
		OperateCollectCategoryMaster ope = new OperateCollectCategoryMaster();
		boolean ret = false;

		try{
			jtm = new JpaTransactionManager();
			jtm.begin();

			ret = ope.deleteAll();

			jtm.commit();
		} catch (Exception e){
			m_log.warn("deleteCollectCategoryMasterAll() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null)
				jtm.close();
		}

		return ret;

	}

	/**
	 * 収集項目毎の計算方法を削除します。
	 * 
	 * @param data 収集項目毎の計算方法定義情報
	 * @return 削除に成功した場合、true
	 * @throws CollectorNotFound
	 * @throws HinemosUnknown
	 */
	public boolean deleteCollectItemCalcMethodMaster(CollectorItemCalcMethodMstPK pk) throws CollectorNotFound, HinemosUnknown {
		JpaTransactionManager jtm = null;

		// 収集項目毎の計算方法を削除
		OperateCollectItemCalcMethodMaster ope = new OperateCollectItemCalcMethodMaster();
		boolean ret = false;

		try{
			jtm = new JpaTransactionManager();
			jtm.begin();

			ret = ope.delete(pk);

			jtm.commit();
		} catch (CollectorNotFound e){
			jtm.rollback();
			throw e;
		} catch (Exception e){
			m_log.warn("deleteCollectItemCalcMethodMaster() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null)
				jtm.close();
		}

		return ret;

	}

	/**
	 * 収集項目毎の計算方法を全て削除します。
	 * 
	 * @return 削除に成功した場合、true
	 * @throws HinemosUnknown
	 */
	public boolean deleteCollectItemCalcMethodMasterAll() throws HinemosUnknown {
		JpaTransactionManager jtm = null;

		// 収集項目毎の計算方法を削除
		OperateCollectItemCalcMethodMaster ope = new OperateCollectItemCalcMethodMaster();
		boolean ret = false;

		try{
			jtm = new JpaTransactionManager();
			jtm.begin();

			ret = ope.deleteAll();

			jtm.commit();
		} catch (Exception e) {
			m_log.warn("deleteCollectItemCalcMethodMasterAll() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null)
				jtm.close();
		}

		return ret;

	}

	/**
	 * 収集項目コードを削除します。
	 * 
	 * @param data 収集項目コード情報
	 * @return 削除に成功した場合、true
	 * @throws CollectorNotFound
	 * @throws HinemosUnknown
	 */
	public boolean deleteCollectItemCodeMaster(String itemCode) throws CollectorNotFound, HinemosUnknown {
		JpaTransactionManager jtm = null;

		// 収集項目コードを削除
		OperateCollectItemCodeMaster ope = new OperateCollectItemCodeMaster();
		boolean ret = false;

		try{
			jtm = new JpaTransactionManager();
			jtm.begin();

			ret = ope.delete(itemCode);

			jtm.commit();
		} catch (CollectorNotFound e) {
			jtm.rollback();
			throw e;
		} catch (Exception e) {
			m_log.warn("deleteCollectItemCodeMaster() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null)
				jtm.close();
		}

		return ret;

	}

	/**
	 * 収集項目コードを全て削除します。
	 * 
	 * @param data 収集項目コード情報
	 * @return 削除に成功した場合、true
	 * @throws HinemosUnknown
	 */
	public boolean deleteCollectItemCodeMasterAll() throws HinemosUnknown {
		JpaTransactionManager jtm = null;

		// 収集項目コードを削除
		OperateCollectItemCodeMaster ope = new OperateCollectItemCodeMaster();
		boolean ret = false;

		try{
			jtm = new JpaTransactionManager();
			jtm.begin();

			ret = ope.deleteAll();

			jtm.commit();
		} catch (Exception e) {
			m_log.warn("deleteCollectItemCodeMasterAll() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null)
				jtm.close();
		}

		return ret;

	}

	/**
	 * 収集方法毎の収集項目を削除します。
	 * 
	 * @param data 収集方法・プラットフォーム毎の収集項目情報
	 * @return 削除に成功した場合、true
	 * @throws CollectorNotFound
	 * @throws HinemosUnknown
	 */
	public boolean deleteCollectPollingMaster(CollectorPollingMstPK pk) throws CollectorNotFound, HinemosUnknown {
		JpaTransactionManager jtm = null;

		// 収集方法・プラットフォーム毎の収集項目を削除
		OperateCollectPollingMaster ope = new OperateCollectPollingMaster();
		boolean ret = false;

		try{
			jtm = new JpaTransactionManager();
			jtm.begin();

			ret = ope.delete(pk);

			jtm.commit();
		} catch (CollectorNotFound e) {
			jtm.rollback();
			throw e;
		} catch (Exception e) {
			m_log.warn("deleteCollectPollingMaster() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null)
				jtm.close();
		}

		return ret;

	}

	/**
	 * 収集方法毎の収集項目を全て削除します。
	 * 
	 * @return 削除に成功した場合、true
	 * @throws HinemosUnknown
	 */
	public boolean deleteCollectPollingMasterAll() throws HinemosUnknown {
		JpaTransactionManager jtm = null;

		// 収集方法・プラットフォーム毎の収集項目を削除
		OperateCollectPollingMaster ope = new OperateCollectPollingMaster();
		boolean ret = false;

		try{
			jtm = new JpaTransactionManager();
			jtm.begin();

			ret = ope.deleteAll();

			jtm.commit();
		} catch (Exception e) {
			m_log.warn("deleteCollectPollingMasterAll() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null)
				jtm.close();
		}

		return ret;

	}

	/**
	 * プラットフォーム定義を削除します。
	 * 
	 * @param platformId プラットフォームID
	 * @return 削除に成功した場合、true
	 * @throws HinemosUnknown
	 */
	public boolean deleteCollectPlatformMaster(String platformId) throws HinemosUnknown {
		JpaTransactionManager jtm = null;

		try{
			jtm = new JpaTransactionManager();
			jtm.begin();

			// プラットフォーム定義情報を登録
			RepositoryControllerBean repositoryControllerBean = new RepositoryControllerBean();
			repositoryControllerBean.deleteCollectorPratformMst(platformId);
			repositoryControllerBean.deleteScope(new String[] {platformId});

			jtm.commit();
		}catch(FacilityNotFound e){
			jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		}catch(HinemosUnknown e){
			jtm.rollback();
			throw e;
		}catch(Exception e){
			m_log.warn("deleteCollectPlatformMaster() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null)
				jtm.close();
		}

		return true;

	}

	/**
	 * サブプラットフォーム定義を削除します。
	 * 
	 * @param subPlatformId サブプラットフォームID
	 * @return 削除に成功した場合、true
	 * @throws HinemosUnknown
	 */
	public boolean deleteCollectSubPlatformMaster(String subPlatformId) throws HinemosUnknown {
		JpaTransactionManager jtm = null;

		try{
			jtm = new JpaTransactionManager();
			jtm.begin();

			// サブプラットフォーム定義情報を登録
			new RepositoryControllerBean().deleteCollectorSubPratformMst(subPlatformId);

			jtm.commit();
		}catch(FacilityNotFound e){
			jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		}catch(HinemosUnknown e){
			jtm.rollback();
			throw e;
		}catch(Exception e){
			m_log.warn("deleteCollectSubPlatformMaster() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null)
				jtm.close();
		}

		return true;

	}

	/**
	 * 収集項目のマスタ情報を全て削除します。
	 * 
	 * @return 削除に成功した場合、true
	 * @throws HinemosUnknown
	 */
	public boolean deleteCollectMasterAll() throws HinemosUnknown {
		JpaTransactionManager jtm = null;

		// 収集項目に関連する全てのマスタ情報を削除
		OperateCollectCategoryCollectMaster opeCategoryCollect = new OperateCollectCategoryCollectMaster();
		OperateCollectPollingMaster opePolling = new OperateCollectPollingMaster();
		OperateCollectItemCalcMethodMaster opeItemCalc = new OperateCollectItemCalcMethodMaster();
		OperateCollectItemCodeMaster opeItemCode = new OperateCollectItemCodeMaster();
		OperateCollectCategoryMaster opeCategory = new OperateCollectCategoryMaster();
		OperateCollectCalcMaster opeCalc = new OperateCollectCalcMaster();

		boolean ret = false;

		try{
			jtm = new JpaTransactionManager();
			jtm.begin();

			ret = opeCategoryCollect.deleteAll() && opePolling.deleteAll() && opeItemCalc.deleteAll() && opeItemCode.deleteAll() && opeCategory.deleteAll() && opeCalc.deleteAll();

			jtm.commit();
		}catch(Exception e){
			m_log.warn("deleteCollectMasterAll() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null)
				jtm.close();
		}

		return ret;

	}

	/**
	 * 全計算方法定義を取得します。
	 * 
	 * @return 全計算方法定義
	 * @throws HinemosUnknown
	 */
	public ArrayList<CollectorCalcMethodMstData> getCollectCalcMasterList() throws HinemosUnknown {
		JpaTransactionManager jtm = null;
		ArrayList<CollectorCalcMethodMstData> ret = null;

		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			ret = new OperateCollectCalcMaster().findAll();
			jtm.commit();
		} catch (Exception e) {
			m_log.warn("getCollectCalcMasterList() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null)
				jtm.close();
		}

		return ret;
	}

	/**
	 * カテゴリを取得します。
	 * 
	 * @return カテゴリ定義
	 * @throws HinemosUnknown
	 */
	public ArrayList<CollectorCategoryMstData> getCollectCategoryList() throws HinemosUnknown {
		JpaTransactionManager jtm = null;
		ArrayList<CollectorCategoryMstData> ret = null;

		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			ret = new OperateCollectCategoryMaster().findAll();
			jtm.commit();
		} catch (Exception e) {
			m_log.warn("getCollectCategoryList() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null)
				jtm.close();
		}

		return ret;
	}

	/**
	 * 収集項目コードを取得します。
	 * 
	 * @return 収集項目コード
	 * @throws HinemosUnknown
	 */
	public ArrayList<CollectorItemCodeMstData> getCollectItemCodeMasterList() throws HinemosUnknown {
		JpaTransactionManager jtm = null;
		ArrayList<CollectorItemCodeMstData> ret = null;

		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			ret = new OperateCollectItemCodeMaster().findAll();
			jtm.commit();
		} catch (Exception e) {
			m_log.warn("getCollectItemCodeMasterList() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null)
				jtm.close();
		}

		return ret;
	}

	/**
	 * 収集項目定義を取得します。
	 * 
	 * @return 収集項目定義
	 * @throws HinemosUnknown
	 */
	public ArrayList<CollectorItemCalcMethodMstData> getCollectItemCalcMasterList() throws HinemosUnknown {
		JpaTransactionManager jtm = null;
		ArrayList<CollectorItemCalcMethodMstData> ret = null;

		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			ret = new OperateCollectItemCalcMethodMaster().findAll();
			jtm.commit();
		} catch (Exception e) {
			m_log.warn("getCollectItemCalcMasterList() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null)
				jtm.close();
		}

		return ret;
	}

	/**
	 * ポーリング対象定義を取得します。
	 * 
	 * @return ポーリング対象定義
	 * @throws HinemosUnknown
	 */
	public ArrayList<CollectorPollingMstData> getCollectPollingList() throws HinemosUnknown {
		JpaTransactionManager jtm = null;
		ArrayList<CollectorPollingMstData> ret = null;

		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			ret = new OperateCollectPollingMaster().findAll();
			jtm.commit();
		} catch (Exception e) {
			m_log.warn("getCollectPollingList() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null)
				jtm.close();
		}

		return ret;
	}

	/**
	 * カテゴリ毎の収集方法を取得します。
	 * 
	 * @return カテゴリ毎の収集方法
	 * @throws HinemosUnknown
	 */
	public ArrayList<CollectorCategoryCollectMstData> getCollectCategoryCollectList() throws HinemosUnknown {
		JpaTransactionManager jtm = null;
		ArrayList<CollectorCategoryCollectMstData> ret = null;

		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			ret = new OperateCollectCategoryCollectMaster().findAll();
			jtm.commit();
		} catch (Exception e) {
			m_log.warn("getCollectCategoryCollectList() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null)
				jtm.close();
		}

		return ret;
	}

	/**
	 * プラットフォーム定義を取得します。
	 * 
	 * @return プラットフォーム定義のリスト
	 * @throws HinemosUnknown
	 */
	public List<CollectorPlatformMstData> getCollectPlatformMaster() throws HinemosUnknown {
		JpaTransactionManager jtm = null;
		ArrayList<CollectorPlatformMstData> platformMstList = new ArrayList<CollectorPlatformMstData>();

		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			// プラットフォームマスタのリストを取得
			for(CollectorPlatformMstEntity bean : new RepositoryControllerBean().getCollectorPlatformMstList()){
				platformMstList.add(new CollectorPlatformMstData(
						bean.getPlatformId(),
						bean.getPlatformName(),
						bean.getOrderNo().shortValue()
						));
			}

			jtm.commit();
		} catch (Exception e) {
			m_log.warn("getCollectPlatformMaster() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null)
				jtm.close();
		}

		return platformMstList;
	}

	/**
	 * サブプラットフォーム定義を取得します。
	 * 
	 * @return サブプラットフォーム定義のリスト
	 * @throws HinemosUnknown
	 */
	public List<CollectorSubPlatformMstData> getCollectSubPlatformMaster() throws HinemosUnknown {
		JpaTransactionManager jtm = null;
		ArrayList<CollectorSubPlatformMstData> subPlatformMstList = new ArrayList<CollectorSubPlatformMstData>();

		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			// サブプラットフォームマスタのリストを取得
			for(CollectorSubPlatformMstEntity bean : new RepositoryControllerBean().getCollectorSubPlatformMstList()){
				subPlatformMstList.add(new CollectorSubPlatformMstData(
						bean.getSubPlatformId(),
						bean.getSubPlatformName(),
						bean.getType(),
						bean.getOrderNo()
						));
			}

			jtm.commit();
		} catch (Exception e) {
			m_log.warn("getCollectSubPlatformMaster() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null)
				jtm.close();
		}

		return subPlatformMstList;
	}

	/**
	 * 収集マスタ情報を一括で取得します。
	 * 
	 * @return 収集マスタ情報
	 * @throws HinemosUnknown
	 */
	public CollectMasterInfo getCollectMasterInfo() throws HinemosUnknown {
		JpaTransactionManager jtm = null;

		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			CollectMasterInfo info = new CollectMasterInfo();
			info.setCollectorCategoryMstDataList(getCollectCategoryList());
			info.setCollectorItemCodeMstDataList(getCollectItemCodeMasterList());
			info.setCollectorItemCalcMethodMstDataList(getCollectItemCalcMasterList());
			info.setCollectorPollingMstDataList(getCollectPollingList());
			info.setCollectorCategoryCollectMstDataList(getCollectCategoryCollectList());
			info.setCollectorCalcMethodMstDataList(getCollectCalcMasterList());

			jtm.commit();
			
			return info;
		} catch (HinemosUnknown e) {
			jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} catch (Exception e) {
			m_log.warn("getCollectMasterInfo() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null)
				jtm.close();
		}
	}
}
