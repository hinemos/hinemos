package com.clustercontrol.commons.util;

import java.util.List;
import java.util.Map;

import javax.persistence.EntityGraph;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.FlushModeType;
import javax.persistence.LockModeType;
import javax.persistence.Query;
import javax.persistence.StoredProcedureQuery;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.metamodel.Metamodel;

import net.sf.jpasecurity.jpql.parser.JpqlBrackets;
import net.sf.jpasecurity.jpql.parser.JpqlFrom;
import net.sf.jpasecurity.jpql.parser.JpqlParser;
import net.sf.jpasecurity.jpql.parser.JpqlSelect;
import net.sf.jpasecurity.jpql.parser.JpqlStatement;
import net.sf.jpasecurity.jpql.parser.JpqlUpdate;
import net.sf.jpasecurity.jpql.parser.JpqlWhere;
import net.sf.jpasecurity.jpql.parser.Node;
import net.sf.jpasecurity.jpql.parser.ToStringVisitor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.annotation.HinemosObjectPrivilege;
import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.accesscontrol.bean.RoleIdConstant;
import com.clustercontrol.accesscontrol.jpql.compile.QueryPreparator;
import com.clustercontrol.accesscontrol.model.ObjectPrivilegeInfo;
import com.clustercontrol.accesscontrol.model.ObjectPrivilegeInfoPK;
import com.clustercontrol.accesscontrol.model.ObjectPrivilegeTargetInfo;
import com.clustercontrol.accesscontrol.util.UserRoleCache;
import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.fault.ObjectPrivilege_InvalidRole;
import com.clustercontrol.jobmanagement.factory.CreateJobSession;

public class HinemosEntityManager implements EntityManager {

	private static Log m_log = LogFactory.getLog(HinemosEntityManager.class);

	private EntityManager em;

	public EntityManager getEntityManager() {
		return em;
	}

	public void setEntityManager(EntityManager em) {
		this.em = em;
	}

	@Override
	public void clear() {
		em.clear();
	}

	@Override
	public void close() {
		em.close();
	}

	@Override
	public boolean contains(Object arg0) {
		return em.contains(arg0);
	}

	@Override
	public Query createNamedQuery(String arg0) {
		return em.createNamedQuery(arg0);
	}

	/**
	 * オブジェクト権限チェックを含めたEntityManager#createNamedQuery()の実装<br/>
	 * オーナーロールによるチェック
	 */
	public <T> TypedQuery<T> createNamedQuery_OR(String name, Class<T> resultClass, ObjectPrivilegeMode mode, String ownerRoleId) {
		if (ObjectPrivilegeMode.NONE.equals(mode)) {
			// オブジェクト権限チェックを行わない場合、オブジェクト権限による絞込みを行わない。
			return em.createNamedQuery(name, resultClass);
		}
		if(RoleIdConstant.isAdministratorRole(ownerRoleId)) {
			// オーナーロールが特権ロールの場合、オブジェクト権限による絞込みを行わない。
			return em.createNamedQuery(name, resultClass);
		}
		String query_before = JpaUtil.getJpqlString(em.createNamedQuery(name, resultClass));
		return getQuery(query_before, resultClass, resultClass, mode, ownerRoleId);
	}

	public <T> TypedQuery<T> createNamedQuery_OR(String name, Class<T> resultClass, String ownerRoleId) {
		return createNamedQuery_OR(name, resultClass, ObjectPrivilegeMode.READ, ownerRoleId);
	}

	/**
	 * オブジェクト権限チェックを含めたEntityManager#createNamedQuery()の実装<br/>
	 */
	public <T> TypedQuery<T> createNamedQuery(String name, Class<T> resultClass, ObjectPrivilegeMode mode) {
		if (ObjectPrivilegeMode.NONE.equals(mode)) {
			// オブジェクト権限チェックを行わない場合、オブジェクト権限による絞込みを行わない。
			return em.createNamedQuery(name, resultClass);
		}
		Boolean isAdministrator = (Boolean)HinemosSessionContext.instance().getProperty(HinemosSessionContext.IS_ADMINISTRATOR);
		if (isAdministrator != null && isAdministrator) {
			// ADMINISTRATORSロールに所属している場合、オブジェクト権限による絞込みを行わない。
			return em.createNamedQuery(name, resultClass);
		}
		String query_before = JpaUtil.getJpqlString(em.createNamedQuery(name, resultClass));
		return getQuery(query_before, resultClass, resultClass, mode);
	}

	@Override
	public <T> TypedQuery<T> createNamedQuery(String name, Class<T> resultClass) {
		return createNamedQuery(name, resultClass, ObjectPrivilegeMode.READ);
	}

	@Override
	public Query createNativeQuery(String arg0) {
		return em.createNativeQuery(arg0);
	}

	@Override
	public Query createNativeQuery(String arg0, @SuppressWarnings("rawtypes") Class arg1) {
		return em.createNativeQuery(arg0, arg1);
	}

	@Override
	public Query createNativeQuery(String arg0, String arg1) {
		return em.createNativeQuery(arg0, arg1);
	}

	@Override
	@Deprecated
	public Query createQuery(String arg0) {
		return em.createQuery(arg0);
	}

	@Override
	@Deprecated
	public <T> TypedQuery<T> createQuery(CriteriaQuery<T> arg0) {
		return em.createQuery(arg0);
	}


	/**
	 * オブジェクト権限チェックを含めたEntityManager#createNamedQuery()の実装<br/>
	 * オーナーロールによるチェック
	 */
	public <T> TypedQuery<T> createQuery_OR(String qlString, Class<T> resultClass, ObjectPrivilegeMode mode, String ownerRoleId) {
		if (ObjectPrivilegeMode.NONE.equals(mode)) {
			// オブジェクト権限チェックを行わない場合、オブジェクト権限による絞込みを行わない。
			return em.createQuery(qlString, resultClass);
		}
		if(RoleIdConstant.isAdministratorRole(ownerRoleId)) {
			// オーナーロールが特権ロールの場合、オブジェクト権限による絞込みを行わない。
			return em.createQuery(qlString, resultClass);
		}
		return getQuery(qlString, resultClass, resultClass, mode, ownerRoleId);
	}

	public <T> TypedQuery<T> createQuery_OR(String qlString, Class<T> resultClass, String ownerRoleId) {
		return createQuery_OR(qlString, resultClass, ObjectPrivilegeMode.READ, ownerRoleId);
	}


	/**
	 * オブジェクト権限チェックを含めたEntityManager#createNamedQuery()の実装<br/>
	 */
	public <T> TypedQuery<T> createQuery(String qlString, Class<T> resultClass, ObjectPrivilegeMode mode) {
		return  createQuery(qlString, resultClass, resultClass, mode);
	}

	@Override
	public <T> TypedQuery<T> createQuery(String qlString, Class<T> resultClass) {
		return createQuery(qlString, resultClass, resultClass, ObjectPrivilegeMode.READ);
	}

	public <T> TypedQuery<T> createQuery(String qlString, Class<T> resultClass, Class<?> entityClass) {
		return createQuery(qlString, resultClass, entityClass, ObjectPrivilegeMode.READ);
	}

	/**
	 * オブジェクト権限チェックを含めたEntityManager#createNamedQuery()の実装<br/>
	 * 戻り値がEntityクラス以外の場合に使用する。
	 */
	public <T> TypedQuery<T> createQuery(String qlString, Class<T> resultClass, Class<?> entityClass, ObjectPrivilegeMode mode) {
		if (ObjectPrivilegeMode.NONE.equals(mode)) {
			// オブジェクト権限チェックを行わない場合、オブジェクト権限による絞込みを行わない。
			return em.createQuery(qlString, resultClass);
		}
		Boolean isAdministrator = (Boolean)HinemosSessionContext.instance().getProperty(HinemosSessionContext.IS_ADMINISTRATOR);
		if (isAdministrator != null && isAdministrator) {
			// ADMINISTRATORSロールに所属している場合、オブジェクト権限による絞込みを行わない。
			return em.createQuery(qlString, resultClass);
		}
		return getQuery(qlString, resultClass, entityClass, mode);
	}

	@Override
	public void detach(Object arg0) {
		em.detach(arg0);
	}


	/**
	 * オブジェクト権限チェックを含めたEntityManager#find()の実装<br/>
	 * オーナーロールID有り
	 */
	public <T> T find_OR(Class<T> entityClass, Object primaryKey, ObjectPrivilegeMode mode, String ownerRoleId) throws ObjectPrivilege_InvalidRole {
		if (!ObjectPrivilegeMode.NONE.equals(mode)) {
			// オブジェクト権限チェックを行う場合
			objectPrivilegeCheck_OR(entityClass, primaryKey, mode, ownerRoleId);	// オブジェクト権限チェック
		}
		return em.find(entityClass, primaryKey);
	}

	public <T> T find_OR(Class<T> entityClass, Object primaryKey, String ownerRoleId) {
		return find_OR(entityClass, primaryKey, ObjectPrivilegeMode.READ, ownerRoleId);
	}

	/**
	 * オブジェクト権限チェックを含めたEntityManager#find()の実装<br/>
	 */
	public <T> T find_OR(Class<T> entityClass, Object primaryKey, Map<String, Object> properties, ObjectPrivilegeMode mode, String ownerRoleId) throws ObjectPrivilege_InvalidRole {
		if (!ObjectPrivilegeMode.NONE.equals(mode)) {
			// オブジェクト権限チェックを行う場合
			objectPrivilegeCheck_OR(entityClass, primaryKey, mode, ownerRoleId);	// オブジェクト権限チェック
		}
		return em.find(entityClass, primaryKey, properties);
	}

	public <T> T find_OR(Class<T> entityClass, Object primaryKey, Map<String, Object> properties, String ownerRoleId) throws ObjectPrivilege_InvalidRole {
		return find_OR(entityClass, primaryKey, properties, ObjectPrivilegeMode.READ, ownerRoleId);
	}

	/**
	 * オブジェクト権限チェックを含めたEntityManager#find()の実装<br/>
	 */
	public <T> T find_OR(Class<T> entityClass, Object primaryKey, LockModeType lockMode, ObjectPrivilegeMode mode, String ownerRoleId) {
		if (!ObjectPrivilegeMode.NONE.equals(mode)) {
			// オブジェクト権限チェックを行う場合
			objectPrivilegeCheck_OR(entityClass, primaryKey, mode, ownerRoleId);	// オブジェクト権限チェック
		}
		return em.find(entityClass, primaryKey, lockMode);
	}

	public <T> T find_OR(Class<T> entityClass, Object primaryKey, LockModeType lockMode, String ownerRoleId) {
		return find_OR(entityClass, primaryKey, lockMode, ObjectPrivilegeMode.READ, ownerRoleId);
	}

	public <T> T find_OR(Class<T> entityClass, Object primaryKey, LockModeType lockMode,
			Map<String, Object> properties, ObjectPrivilegeMode mode, String ownerRoleId) {
		if (!ObjectPrivilegeMode.NONE.equals(mode)) {
			// オブジェクト権限チェックを行う場合
			objectPrivilegeCheck_OR(entityClass, primaryKey, mode, ownerRoleId);	// オブジェクト権限チェック
		}
		return em.find(entityClass, primaryKey, lockMode, properties);
	}

	public <T> T find_OR(Class<T> entityClass, Object primaryKey, LockModeType lockMode,
			Map<String, Object> properties, String ownerRoleId) {
		return find_OR(entityClass, primaryKey, lockMode, properties, ObjectPrivilegeMode.READ, ownerRoleId);
	}

	/**
	 * オブジェクト権限チェックを含めたEntityManager#find()の実装<br/>
	 */
	public <T> T find(Class<T> entityClass, Object primaryKey, ObjectPrivilegeMode mode) throws ObjectPrivilege_InvalidRole {
		if (!ObjectPrivilegeMode.NONE.equals(mode)) {
			// オブジェクト権限チェックを行う場合
			objectPrivilegeCheck(entityClass, primaryKey, mode);	// オブジェクト権限チェック
		}
		return em.find(entityClass, primaryKey);
	}

	// 使用しない
	// find(Class<T> entityClass, Object primaryKey, ObjectPrivilegeMode mode)を使用すること
	@Override
	@Deprecated
	/**
	 * @see #find(Class<T>, Object, ObjectPrivilegeMode)
	 */
	public <T> T find(Class<T> entityClass, Object primaryKey) {
		return find(entityClass, primaryKey, ObjectPrivilegeMode.READ);
	}

	/**
	 * オブジェクト権限チェックを含めたEntityManager#find()の実装<br/>
	 */
	public <T> T find(Class<T> entityClass, Object primaryKey, Map<String, Object> properties, ObjectPrivilegeMode mode) throws ObjectPrivilege_InvalidRole {
		if (!ObjectPrivilegeMode.NONE.equals(mode)) {
			// オブジェクト権限チェックを行う場合
			objectPrivilegeCheck(entityClass, primaryKey, mode);	// オブジェクト権限チェック
		}
		return em.find(entityClass, primaryKey, properties);
	}

	@Override
	public <T> T find(Class<T> entityClass, Object primaryKey, Map<String, Object> properties) throws ObjectPrivilege_InvalidRole {
		return find(entityClass, primaryKey, properties, ObjectPrivilegeMode.READ);
	}

	/**
	 * オブジェクト権限チェックを含めたEntityManager#find()の実装<br/>
	 */
	public <T> T find(Class<T> entityClass, Object primaryKey, LockModeType lockMode, ObjectPrivilegeMode mode) {
		if (!ObjectPrivilegeMode.NONE.equals(mode)) {
			// オブジェクト権限チェックを行う場合
			objectPrivilegeCheck(entityClass, primaryKey, mode);	// オブジェクト権限チェック
		}
		return em.find(entityClass, primaryKey, lockMode);
	}

	@Override
	public <T> T find(Class<T> entityClass, Object primaryKey, LockModeType lockMode) {
		return find(entityClass, primaryKey, lockMode, ObjectPrivilegeMode.READ);
	}

	public <T> T find(Class<T> entityClass, Object primaryKey, LockModeType lockMode,
			Map<String, Object> properties, ObjectPrivilegeMode mode) {
		if (!ObjectPrivilegeMode.NONE.equals(mode)) {
			// オブジェクト権限チェックを行う場合
			objectPrivilegeCheck(entityClass, primaryKey, mode);	// オブジェクト権限チェック
		}
		return em.find(entityClass, primaryKey, lockMode, properties);
	}

	@Override
	public <T> T find(Class<T> entityClass, Object primaryKey, LockModeType lockMode,
			Map<String, Object> properties) {
		return find(entityClass, primaryKey, lockMode, properties, ObjectPrivilegeMode.READ);
	}

	@Override
	public void flush() {
		em.flush();
	}

	@Override
	public CriteriaBuilder getCriteriaBuilder() {
		return em.getCriteriaBuilder();
	}

	@Override
	public Object getDelegate() {
		return em.getDelegate();
	}

	@Override
	public EntityManagerFactory getEntityManagerFactory() {
		return em.getEntityManagerFactory();
	}

	@Override
	public FlushModeType getFlushMode() {
		return em.getFlushMode();
	}

	@Override
	public LockModeType getLockMode(Object arg0) {
		return em.getLockMode(arg0);
	}

	@Override
	public Metamodel getMetamodel() {
		return em.getMetamodel();
	}

	@Override
	public Map<String, Object> getProperties() {
		return em.getProperties();
	}

	@Override
	public <T> T getReference(Class<T> arg0, Object arg1) {
		return em.getReference(arg0, arg1);
	}

	@Override
	public EntityTransaction getTransaction() {
		return em.getTransaction();
	}

	@Override
	public boolean isOpen() {
		return em.isOpen();
	}

	@Override
	public void joinTransaction() {
		em.joinTransaction();
	}

	@Override
	public void lock(Object arg0, LockModeType arg1) {
		em.lock(arg0, arg1);
	}

	@Override
	public void lock(Object arg0, LockModeType arg1, Map<String, Object> arg2) {
		em.lock(arg0, arg1, arg2);
	}

	@Override
	public <T> T merge(T arg0) {
		return em.merge(arg0);
	}

	@Override
	public void persist(Object arg0) {
		em.persist(arg0);
	}

	@Override
	public void refresh(Object arg0) {
		em.refresh(arg0);
	}

	@Override
	public void refresh(Object arg0, Map<String, Object> arg1) {
		em.refresh(arg0, arg1);
	}

	@Override
	public void refresh(Object arg0, LockModeType arg1) {
		em.refresh(arg0, arg1);
	}

	@Override
	public void refresh(Object arg0, LockModeType arg1, Map<String, Object> arg2) {
		em.refresh(arg0, arg1, arg2);
	}

	@Override
	public void remove(Object arg0) {
		em.remove(arg0);
	}

	@Override
	public void setFlushMode(FlushModeType arg0) {
		em.setFlushMode(arg0);
	}

	@Override
	public void setProperty(String arg0, Object arg1) {
		em.setProperty(arg0, arg1);
	}

	@Override
	public <T> T unwrap(Class<T> arg0) {
		return em.unwrap(arg0);
	}

	/**
	 * 
	 * オブジェクト権限チェック
	 * 
	 * @param entityClass 検索対象のEntityクラス
	 * @param primaryKey 検索対象の主キー
	 * @param mode オブジェクト権限のモード(READ、WRITE、EXEC)
	 * @param ownerRoleId
	 */
	private <T> void objectPrivilegeCheck_OR(Class<T> entityClass, Object primaryKey, ObjectPrivilegeMode mode, String ownerRoleId) {

		// エンティティ取得
		HinemosObjectPrivilege hinemosObjectPrivilege = entityClass.getAnnotation(HinemosObjectPrivilege.class);

		// エンティティにAnnotationが設定されていない場合はオブジェクト権限チェックはしない
		if (hinemosObjectPrivilege == null) {
			return;
		}

		if(RoleIdConstant.isAdministratorRole(ownerRoleId)) {
			// オーナーロールが特権ロールの場合、オブジェクト権限による絞込みを行わない。
			return;
		}

		// オーナーロールにユーザのロールが設定されている場合は実装を返す
		// チェック対象のEntityはObjectPrivilegeTargetEntityを継承していないとエラー
		T before_entity = em.find(entityClass, primaryKey);
		if (!(before_entity instanceof ObjectPrivilegeTargetInfo)) {
			// オブジェクト権限エラー
			ObjectPrivilege_InvalidRole e = new ObjectPrivilege_InvalidRole(
					"targetClass=" + entityClass.getSimpleName()
					+ ", pk=" + primaryKey.toString()
					+ ", ownerRoleId=" + ownerRoleId);
			m_log.info("checkObjectPrivilege() object privilege invalid. 1 : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		} else {
			// ジョブの場合、jobunitId='ROOT'はチェック対象外
			if (hinemosObjectPrivilege.objectType().equals(HinemosModuleConstant.JOB_MST)
					&& ((ObjectPrivilegeTargetInfo)before_entity).getObjectId().equals(CreateJobSession.TOP_JOBUNIT_ID)) {
				return;
			}
			// オーナーロールIDを確認する
			String entityOwnerRoleId = ((ObjectPrivilegeTargetInfo)before_entity).getOwnerRoleId();
			// 所属ロールの場合はオブジェクト権限有り
			if (ownerRoleId.equals(entityOwnerRoleId)) {
				return;		// オブジェクト権限有り
			}
		}

		// オブジェクト権限テーブルを確認する
		String objectType = hinemosObjectPrivilege.objectType();
		// オブジェクト権限テーブルに所属ロールのデータが存在するかの確認
		ObjectPrivilegeInfoPK objectPrivilegeEntityPK
		= new ObjectPrivilegeInfoPK(objectType, ((ObjectPrivilegeTargetInfo)before_entity).getObjectId(), ownerRoleId, mode.name());
		ObjectPrivilegeInfo objectPrivilegeEntity = em.find(ObjectPrivilegeInfo.class, objectPrivilegeEntityPK);
		if (objectPrivilegeEntity != null) {
			return;		// オブジェクト権限有り
		}

		// オブジェクト権限エラー
		ObjectPrivilege_InvalidRole e = new ObjectPrivilege_InvalidRole(
				"targetClass=" + entityClass.getSimpleName()
				+ ", pk=" + primaryKey.toString()
				+ ", ownerRoleId=" + ownerRoleId);
		m_log.info("checkObjectPrivilege() object privilege invalid. 2 : "
				+ e.getClass().getSimpleName() + ", " + e.getMessage());
		throw e;
	}

	/**
	 * 
	 * オブジェクト権限チェック
	 * 
	 * @param entityClass 検索対象のEntityクラス
	 * @param primaryKey 検索対象の主キー
	 * @param mode オブジェクト権限のモード(READ、WRITE、EXEC)
	 */
	private <T> void objectPrivilegeCheck(Class<T> entityClass, Object primaryKey, ObjectPrivilegeMode mode) {

		// エンティティ取得
		HinemosObjectPrivilege hinemosObjectPrivilege = entityClass.getAnnotation(HinemosObjectPrivilege.class);

		// エンティティにAnnotationが設定されていない場合はオブジェクト権限チェックはしない
		if (hinemosObjectPrivilege == null) {
			return;
		}

		// ADMINISTRATORSロールに所属している場合はオブジェクト権限チェックはしない
		Boolean isAdministrator = (Boolean)HinemosSessionContext.instance().getProperty(HinemosSessionContext.IS_ADMINISTRATOR);
		if (isAdministrator != null && isAdministrator) {
			return;
		}

		// ユーザ情報の取得
		String loginUser = (String)HinemosSessionContext.instance().getProperty(HinemosSessionContext.LOGIN_USER_ID);

		// ユーザ情報が取得できない場合はオブジェクト権限チェックはしない
		if (loginUser == null || "".equals(loginUser.trim())) {
			return;
		}
		List<String> roleIdList = UserRoleCache.getRoleIdList(loginUser);

		// オーナーロールにユーザのロールが設定されている場合は実装を返す
		T before_entity = em.find(entityClass, primaryKey);
		// データ未存在の場合は処理を終了する
		if (before_entity == null) {
			return;
		}
		// チェック対象のEntityはObjectPrivilegeTargetEntityを継承していないとエラー
		if (!(before_entity instanceof ObjectPrivilegeTargetInfo)) {
			// オブジェクト権限エラー
			ObjectPrivilege_InvalidRole e = new ObjectPrivilege_InvalidRole(
					"user=" + loginUser
					+ ", targetClass=" + entityClass.getSimpleName()
					+ ", pk=" + primaryKey.toString());
			m_log.info("checkObjectPrivilege() object privilege invalid. 3 : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		} else {
			// ジョブの場合、jobunitId='ROOT'はチェック対象外
			if (hinemosObjectPrivilege.objectType().equals(HinemosModuleConstant.JOB_MST)
					&& ((ObjectPrivilegeTargetInfo)before_entity).getObjectId().equals(CreateJobSession.TOP_JOBUNIT_ID)) {
				return;
			}
			String ownerRoleId = ((ObjectPrivilegeTargetInfo)before_entity).getOwnerRoleId();
			// 所属ロールの場合はオブジェクト権限有り
			if (roleIdList.contains(ownerRoleId)) {
				return;		// オブジェクト権限有り
			}
		}

		String objectType = hinemosObjectPrivilege.objectType();
		// オブジェクト権限テーブルにデータが存在するかの確認
		// 所属ロールが設定されている場合はオブジェクト権限有り
		boolean existsflg = false;
		for (String roleId : roleIdList) {
			ObjectPrivilegeInfoPK objectPrivilegeEntityPK
			= new ObjectPrivilegeInfoPK(objectType, ((ObjectPrivilegeTargetInfo)before_entity).getObjectId(), roleId, mode.name());
			ObjectPrivilegeInfo objectPrivilegeEntity = em.find(ObjectPrivilegeInfo.class, objectPrivilegeEntityPK);
			if (objectPrivilegeEntity != null) {
				existsflg = true;	// オブジェクト権限有り
				break;
			}
		}
		if (!existsflg) {
			// オブジェクト権限エラー
			ObjectPrivilege_InvalidRole e = new ObjectPrivilege_InvalidRole(
					"user=" + loginUser
					+ ", targetClass=" + entityClass.getSimpleName()
					+ ", pk=" + primaryKey.toString());

			// 設定によっては、監視[イベント]ビューの更新の度に下記ログが大量に出力され続けるため、
			// debugに変更する。
			m_log.debug("checkObjectPrivilege() object privilege invalid. 4 : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}

		return;
	}

	/**
	 * オブジェクト権限を付加したQueryの取得
	 * 
	 * @param jpql JPQL文
	 * @param resultClass 戻り値の型
	 * @param entityClass オブジェクト権限チェックの対象Entityクラス
	 * @param mode オブジェクト権限(READ, WRITE, EXEC)
	 * @return オブジェクト権限を付加したTypedQuery
	 */
	private <T> TypedQuery<T> getQuery(String jpql, Class<T> resultClass, Class<?> entityClass, ObjectPrivilegeMode mode) {
		return getQuery(jpql, resultClass, entityClass, mode, null);
	}

	/**
	 * オブジェクト権限を付加したQueryの取得
	 * 
	 * @param beforeJpql JPQL文
	 * @param resultClass 戻り値の型
	 * @param entityClass オブジェクト権限チェックの対象Entityクラス
	 * @param mode オブジェクト権限(READ, WRITE, EXEC)
	 * @param ownerRoleId オーナーロールID
	 * @return オブジェクト権限を付加したTypedQuery
	 */
	private <T> TypedQuery<T> getQuery(String beforeJpql, Class<T> resultClass, Class<?> entityClass, ObjectPrivilegeMode mode, String ownerRoleId) {

		TypedQuery<T> typedQuery = null;

		HinemosObjectPrivilege hinemosObjectPrivilege = entityClass.getAnnotation(HinemosObjectPrivilege.class);
		// エンティティにAnnotationが設定されていない場合はそのままの実装を返す
		if (hinemosObjectPrivilege == null) {
			return em.createQuery(beforeJpql, resultClass);
		}

		// ユーザ情報をもとにオブジェクト権限を行う場合
		if (ownerRoleId == null) {
			String loginUser = (String)HinemosSessionContext.instance().getProperty(HinemosSessionContext.LOGIN_USER_ID);
			// ユーザ情報が取得できない場合はそのままの実装を返す
			if (loginUser == null || "".equals(loginUser.trim())) {
				return em.createQuery(beforeJpql, resultClass);
			}

			// オブジェクト権限チェックを含むJPQLに変換
			String afterJpql = getObjectPrivilegeJPQL(beforeJpql, entityClass, mode, null);
			List<String> roleIds = UserRoleCache.getRoleIdList(loginUser);
			afterJpql = afterJpql.replaceAll(":roleIds", HinemosEntityManager.getParamNameString("roleId", roleIds.toArray(new String[roleIds.size()])));
			typedQuery = em.createQuery(afterJpql, resultClass);
			HinemosEntityManager.appendParam(typedQuery, "roleId", roleIds.toArray(new String[roleIds.size()]));

		} else {
			String afterJpql = getObjectPrivilegeJPQL(beforeJpql, entityClass, mode, ownerRoleId);
			typedQuery = em.createQuery(afterJpql, resultClass);

		}

		return typedQuery;
	}

	/**
	 * JPQLにオブジェクト権限チェックを入れて返す<br/>
	 * 
	 * @param jpqlString JPQL文
	 * @param entityClass オブジェクト権限チェック対象のEntityクラス
	 * @param mode オブジェクト権限(READ, WRITE, EXEC)
	 * @return
	 */
	private String getObjectPrivilegeJPQL(String jpqlString, Class<?> entityClass, ObjectPrivilegeMode mode, String ownerRoleId) {

		String rtnString = "";
		try {
			HinemosObjectPrivilege hinemosObjectPrivilege = entityClass.getAnnotation(HinemosObjectPrivilege.class);
			String objectType = hinemosObjectPrivilege.objectType();

			// JPQLの構文解析
			JpqlParser jpqlParser = new JpqlParser();
			JpqlFrom jpqlFrom = null;
			JpqlWhere jpqlWhere = null;
			JpqlStatement statement = jpqlParser.parseQuery(jpqlString);

			if (statement.jjtGetChild(0) instanceof JpqlSelect
					|| statement.jjtGetChild(0) instanceof JpqlUpdate) {

				if (statement.jjtGetChild(0) instanceof JpqlSelect) {
					JpqlSelect jpqlSelect = (JpqlSelect)statement.jjtGetChild(0);
					for(int i=0 ; i<jpqlSelect.jjtGetNumChildren() ; i++ ) {
						if (jpqlSelect.jjtGetChild(i) instanceof JpqlFrom) {
							jpqlFrom = (JpqlFrom)jpqlSelect.jjtGetChild(i);
						} else if (jpqlSelect.jjtGetChild(i) instanceof JpqlWhere) {
							jpqlWhere = (JpqlWhere)jpqlSelect.jjtGetChild(i);
							break;
						}
					}
				}
				else if (statement.jjtGetChild(0) instanceof JpqlUpdate) {
					JpqlUpdate jpqlUpdate = (JpqlUpdate)statement.jjtGetChild(0);
					for(int i=0 ; i<jpqlUpdate.jjtGetNumChildren() ; i++ ) {
						if (jpqlUpdate.jjtGetChild(i) instanceof JpqlFrom) {
							jpqlFrom = (JpqlFrom)jpqlUpdate.jjtGetChild(i);
						} else if (jpqlUpdate.jjtGetChild(i) instanceof JpqlWhere) {
							jpqlWhere = (JpqlWhere)jpqlUpdate.jjtGetChild(i);
							break;
						}
					}
				}
				// オブジェクト権限チェックのJPQLを挿入
				Node jpqlExists = null;
				if (ownerRoleId == null) {
					jpqlExists = QueryPreparator.createObjectPrivilegeExists(objectType, mode);
				} else {
					jpqlExists = QueryPreparator.createObjectPrivilegeExists(objectType, mode, ownerRoleId);
				}
				if (jpqlWhere == null) {
					jpqlWhere = QueryPreparator.createWhere(jpqlExists);
					Node parent = jpqlFrom.jjtGetParent();
					for (int i = parent.jjtGetNumChildren(); i > 2; i--) {
						parent.jjtAddChild(parent.jjtGetChild(i - 1), i);
					}
					parent.jjtAddChild(jpqlWhere, 2);
				} else {
					Node condition = jpqlWhere.jjtGetChild(0);
					if (!(condition instanceof JpqlBrackets)) {
						condition = QueryPreparator.createBrackets(condition);
					}
					Node and = QueryPreparator.createAnd(condition, jpqlExists);
					and.jjtSetParent(jpqlWhere);
					jpqlWhere.jjtSetChild(and, 0);
				}
			}

			ToStringVisitor v = new ToStringVisitor();
			statement.jjtAccept(v, null);
			rtnString = statement.toString();
			m_log.debug("getObjectPrivilegeJPQL() jpql = " + rtnString);
		} catch (Exception e) {
			m_log.warn("getObjectPrivilegeJPQL() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
		}
		return rtnString;
	}

	/**
	 * JPQLのIN句に設定するパラメータ名を取得
	 * 
	 *  JPQLでは、Listでパラメータ値を設定することが可能だが、
	 *  JPASecurityでの構文解析時に上記が正常に動かない。
	 *  そのための対応。
	 * 
	 *  例） paramName = aaa
	 *     paramValue = {1,3,5,7}とすると
	 *     ":aaa0, :aaa1, :aaa2, :aaa3"が返される。
	 * 
	 * @param paramName パラメータ名
	 * @param paramValue パラメータ値
	 * @return JPQLのIN句に設定するパラメータ名
	 */
	public static String getParamNameString(String paramName, String[] paramValues) {
		String rtnString = "";
		int count = paramValues.length;
		if (count > 0) {
			StringBuffer sb = new StringBuffer();
			for (int i = 0 ; i < count ; i++) {
				if (i > 0) {
					sb.append(",");
				}
				sb.append(":" + paramName + i);
			}
			rtnString = sb.toString();
		}
		return rtnString;
	}

	public static String getParamNameString(String paramName, List<String> paramValueList) {
		return getParamNameString(paramName, paramValueList.toArray(new String[0]));
	}


	/**
	 * JPQLのIN句にパラメータ値を設定したTypedQueryを返す。
	 * 
	 *  JPQLでは、Listでパラメータ値を設定することが可能だが、
	 *  JPASecurityでの構文解析時に上記が正常に動かない。
	 *  そのための対応。
	 * 
	 *  例） paramName = aaa
	 *     paramValue = {1,3,5,7}とすると
	 *     :aaa0 に 1、:aaa2に3、．．．と値を設定して返す。
	 * 
	 * @param query TypedQuery
	 * @param paramName パラメータ名
	 * @param paramValue パラメータ値
	 * @return JPQLのIN句に値を設定したTypedQuery
	 */
	public static <T> TypedQuery<T> appendParam(TypedQuery<T> query, String paramName, String[] paramValues) {
		TypedQuery<T> typedQuery = query;
		int count = paramValues.length;
		if (count > 0) {
			for (int i = 0 ; i < count ; i++) {
				typedQuery = typedQuery.setParameter(paramName + i, paramValues[i]);
			}
		}
		return typedQuery;
	}

	@Override
	public <T> EntityGraph<T> createEntityGraph(Class<T> arg0) {
		return em.createEntityGraph(arg0);
	}

	@Override
	public EntityGraph<?> createEntityGraph(String arg0) {
		return em.createEntityGraph(arg0);
	}

	@Override
	public StoredProcedureQuery createNamedStoredProcedureQuery(String arg0) {
		return em.createNamedStoredProcedureQuery(arg0);
	}

	@Override
	public Query createQuery(@SuppressWarnings("rawtypes") CriteriaUpdate arg0) {
		return em.createQuery(arg0);
	}

	@Override
	public Query createQuery(@SuppressWarnings("rawtypes") CriteriaDelete arg0) {
		return em.createQuery(arg0);
	}

	@Override
	public StoredProcedureQuery createStoredProcedureQuery(String arg0) {
		return em.createStoredProcedureQuery(arg0);
	}

	@Override
	public StoredProcedureQuery createStoredProcedureQuery(String arg0, @SuppressWarnings("rawtypes") Class... arg1) {
		return em.createStoredProcedureQuery(arg0, arg1);
	}

	@Override
	public StoredProcedureQuery createStoredProcedureQuery(String arg0,
			String... arg1) {
		return em.createStoredProcedureQuery(arg0, arg1);
	}

	@Override
	public EntityGraph<?> getEntityGraph(String arg0) {
		return em.getEntityGraph(arg0);
	}

	@Override
	public <T> List<EntityGraph<? super T>> getEntityGraphs(Class<T> arg0) {
		return em.getEntityGraphs(arg0);
	}

	@Override
	public boolean isJoinedToTransaction() {
		return em.isJoinedToTransaction();
	}
}
