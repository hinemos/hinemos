/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.infra.model;

import java.io.Serializable;
import java.util.Map;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.EntityExistsException;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import com.clustercontrol.bean.PatternConstant;
import com.clustercontrol.commons.util.CommonValidator;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.infra.bean.AccessInfo;
import com.clustercontrol.infra.bean.ModuleNodeResult;
import com.clustercontrol.repository.model.NodeInfo;
import com.clustercontrol.util.MessageConstant;

@XmlType(namespace = "http://infra.ws.clustercontrol.com")
@Entity
@Table(name="cc_infra_module_info", schema="setting")
@Inheritance
@DiscriminatorColumn(name="module_type")
@Cacheable(true)
@XmlSeeAlso({FileTransferModuleInfo.class, CommandModuleInfo.class, ReferManagementModuleInfo.class})
public abstract class InfraModuleInfo<E extends InfraModuleInfo<?>> implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private InfraModuleInfoPK id;
	private String name;
	private Integer orderNo;
	private Boolean validFlg;
	private Boolean stopIfFailFlg;
	private Boolean precheckFlg;
	private String execReturnParamName;

	private InfraManagementInfo infraManagementInfoEntity;
	
	public InfraModuleInfo() {
	}

	public InfraModuleInfo(InfraManagementInfo parent, String moduleId) {
		this.setId(new InfraModuleInfoPK(parent.getManagementId(), moduleId));
		setInfraManagementInfoEntity(parent);
		parent.getModuleList().add(this);
	}
	
	@XmlTransient
	@EmbeddedId
	public InfraModuleInfoPK getId() {
		if (id == null)
			id = new InfraModuleInfoPK();
		return id;
	}
	public void setId(InfraModuleInfoPK id) {
		this.id = id;
	}
	
	@Transient
	public String getManagementId() {
		return getId().getManagementId();
	}
	public void setManagementId(String managementId) {
		getId().setManagementId(managementId);
	}
	
	@Transient
	public String getModuleId() {
		return getId().getModuleId();
	}
	public void setModuleId(String moduleId) {
		getId().setModuleId(moduleId);
	}
	
	@Column(name="order_no")
	public Integer getOrderNo() {
		return orderNo;
	}
	public void setOrderNo(Integer orderNo) {
		this.orderNo = orderNo;
	}

	@Column(name="name")
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	@XmlTransient
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="management_id", insertable=false, updatable=false)
	public InfraManagementInfo getInfraManagementInfoEntity() {
		return this.infraManagementInfoEntity;
	}
	public void setInfraManagementInfoEntity(InfraManagementInfo infraManagementInfoEntity) {
		this.infraManagementInfoEntity = infraManagementInfoEntity;
	}
	
	@Column(name="valid_flg")
	public Boolean getValidFlg() {
		return validFlg;
	}
	public void setValidFlg(Boolean validFlg) {
		this.validFlg = validFlg;
	}

	@Column(name="stop_if_fail_flg")
	public Boolean getStopIfFailFlg() {
		return stopIfFailFlg;
	}
	public void setStopIfFailFlg(Boolean stopIfFailFlg) {
		this.stopIfFailFlg = stopIfFailFlg;
	}

	@Column(name="precheck_flg")
	public Boolean getPrecheckFlg() {
		return precheckFlg;
	}
	public void setPrecheckFlg(Boolean precheckFlg) {
		this.precheckFlg = precheckFlg;
	}

	@Column(name="exec_return_param_name")
	public String getExecReturnParamName() {
		return execReturnParamName;
	}
	public void setExecReturnParamName(String execReturnParamName) {
		this.execReturnParamName = execReturnParamName;
	}

	public abstract String getModuleTypeName();
	
	public void addCounterEntity(InfraManagementInfo management) throws HinemosUnknown, EntityExistsException {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			E module = getEntityClass().newInstance();
	
			module.setId(new InfraModuleInfoPK(management.getManagementId(), getModuleId()));
			jtm.checkEntityExists(InfraModuleInfo.class, module.getId());
			module.setName(getName());
			module.setOrderNo(management.getModuleList().size());
			module.setValidFlg(getValidFlg());
			module.setStopIfFailFlg(getStopIfFailFlg());
			module.setPrecheckFlg(getPrecheckFlg());
			module.setExecReturnParamName(getExecReturnParamName());
			
			management.getModuleList().add(module);
	
			overwriteCounterEntity(management, module, em);
			
			em.persist(module);
		} catch (InstantiationException | IllegalAccessException e) {
			throw new HinemosUnknown(e.getMessage(), e);
		} catch (EntityExistsException e) {
			throw e;
		}
	}
	
	protected abstract Class<E> getEntityClass();
	
	public void modifyCounterEntity(InfraManagementInfo management, InfraModuleInfo<?> module, Integer orderNo) throws HinemosUnknown {
		if (!module.getId().getModuleId().equals(this.getModuleId())) {
			throw new HinemosUnknown("Not match moduleIds between web and db on modifying infra module.");
		}
		module.setName(getName());
		module.setOrderNo(orderNo);
		module.setValidFlg(getValidFlg());
		module.setStopIfFailFlg(getStopIfFailFlg());
		module.setPrecheckFlg(getPrecheckFlg());
		module.setExecReturnParamName(getExecReturnParamName());
		
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			overwriteCounterEntity(management, getEntityClass().cast(module), em);
		} catch(ClassCastException e) {
			throw new HinemosUnknown(e.getMessage(), e);
		}
	}
	
	public void onPersist(HinemosEntityManager em) {
	}
	
	public void onRemove(HinemosEntityManager em) {
	}

	public void persistSelf(HinemosEntityManager em) {
		onPersist(em);
		em.persist(this);
	}
	
	public void removeSelf(HinemosEntityManager em) throws InvalidRole, HinemosUnknown {
		onPersist(em);
		em.remove(this);
	}
	
	public void validate(InfraManagementInfo infraManagementInfo) throws InvalidSetting, InvalidRole {
		CommonValidator.validateId(MessageConstant.INFRA_MODULE_ID.getMessage(), getModuleId(), 64);
		CommonValidator.validateString(MessageConstant.INFRA_MODULE_NAME.getMessage(), getName(), true, 1, 64);

		// execReturnParamName
		if (this instanceof CommandModuleInfo || this instanceof FileTransferModuleInfo) {
			CommonValidator.validateString(MessageConstant.INFRA_MODULE_EXEC_RETURN_PARAM_NAME.getMessage(), getExecReturnParamName(), false, 0, 64);
			// IDのパターンは許容しない
			if(getExecReturnParamName() != null 
					&& !getExecReturnParamName().isEmpty()
					&& !getExecReturnParamName().matches(PatternConstant.HINEMOS_ID_PATTERN)){
				InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_INPUT_PARAMID_ILLEGAL_CHARACTERS.getMessage(
						MessageConstant.INFRA_MODULE_EXEC_RETURN_PARAM_NAME.getMessage(), getExecReturnParamName()));
				throw e;
			}
		}

		// validFlg : not implemented
		// proceedIfFailFlg : not implemented
		
		validateSub(infraManagementInfo);
	}
	
	protected abstract void overwriteCounterEntity(InfraManagementInfo management, E module, HinemosEntityManager em);

	protected abstract void validateSub(InfraManagementInfo infraManagementInfo) throws InvalidSetting, InvalidRole;

	public abstract boolean canPrecheck(InfraManagementInfo management, NodeInfo node, AccessInfo access, String sessionId) throws HinemosUnknown, InvalidUserPass;

	/**
	 * 実行モジュール実行
	 * @param management 環境構築設定
	 * @param node 実行対象ノード
	 * @param access ログイン情報
	 * @param sessionId セッションID（サブセッションの場合は「セッションID:モジュールID:モジュールID...」）
	 * @param paramMap 環境構築変数マップ
	 * @return 実行結果
	 * @throws HinemosUnknown
	 * @throws InvalidUserPass
	 */
	public abstract ModuleNodeResult run(InfraManagementInfo management, NodeInfo node, AccessInfo access, String sessionId, Map<String, String> paramMap) throws HinemosUnknown, InvalidUserPass;

	/**
	 * チェックモジュール実行
	 * @param management 環境構築設定
	 * @param node 実行対象ノード
	 * @param access ログイン情報
	 * @param sessionId セッションID（サブセッションの場合は「セッションID:モジュールID:モジュールID...」）
	 * @param paramMap 環境構築変数マップ
	 * @param verbose
	 * @return 実行結果
	 * @throws HinemosUnknown
	 * @throws InvalidUserPass
	 */
	public abstract ModuleNodeResult check(InfraManagementInfo management, NodeInfo node, AccessInfo access, String sessionId, Map<String, String> paramMap, boolean verbose) throws HinemosUnknown, InvalidUserPass;
	
	public abstract void beforeRun(String sessionId) throws HinemosUnknown;
	
	public abstract void afterRun(String sessionId) throws HinemosUnknown;
}