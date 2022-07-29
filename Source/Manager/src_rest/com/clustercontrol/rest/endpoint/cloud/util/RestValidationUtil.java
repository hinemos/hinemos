/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.cloud.util;

import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.HinemosSessionContext;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.xcloud.CloudManagerException;
import com.clustercontrol.xcloud.Session;
import com.clustercontrol.xcloud.common.CloudMessageConstant;
import com.clustercontrol.xcloud.common.ErrorCode;
import com.clustercontrol.xcloud.util.CloudMessage;
import com.clustercontrol.xcloud.validation.CommonValidatorEx;

import jakarta.persistence.NoResultException;
import jakarta.persistence.Query;

/**
 * クラウド系APIのバリデーションを行うクラス(主にクエリパラメータ向け)
 */
public class RestValidationUtil {

	/**
	 * IDパラメータのバリデーションを行う。
	 */
	public static void identityValidate(String paramId, String param) throws CloudManagerException {
		try {
			CommonValidatorEx.validateId(CloudMessage.getMessage(paramId), param, 64);
		} catch (InvalidSetting e) {
			throw createValidationFault(e.getMessage());
		}
	}

	/**
	 * 必須パラメータのバリデーションを行う。
	 */
	public static void notNullValidate(String paramId, Object param) throws CloudManagerException {
		if (param == null) {
			throw createValidationFault(CloudMessageConstant.VALIDATION_NOTNULL.getMessage(CloudMessage.getMessage(paramId)));
		}
	}


	/**
	 * 必須パラメータが数値フォーマットかバリデーションを行う。
	 */
	public static void notNullIntegerValidate(String paramId, String param) throws CloudManagerException, InvalidSetting {
		if (param == null) {
			throw createValidationFault(CloudMessageConstant.VALIDATION_NOTNULL.getMessage(CloudMessage.getMessage(paramId)));
		}
		try {
			Integer.valueOf(param);
		} catch (NumberFormatException e) {
			throw new InvalidSetting();
		}
	}
	/**
	 * 文字列長のバリデーションを行う。
	 */
	public static void sizeValidate(String paramId, String param, Integer min, Integer max) throws CloudManagerException {
		if (min == null) {
			min = 0;
		}
		if (max == null) {
			max = Integer.MAX_VALUE;
		}
		try {
			CommonValidatorEx.validateString(CloudMessage.getMessage(paramId), param, false, min, max);
		}
		catch (InvalidSetting e) {
			throw createValidationFault(e.getMessage());
		}
	}

	private static CloudManagerException createValidationFault(String message) {
		CloudManagerException v = new CloudManagerException(message, ErrorCode.VALIDATION_ERROR.name());
		return v;
	}
	
	/**
	 * クラウドスコープが変更可能か確認する。
	 */
	public static void modifiableCloudScopeValidate(String cloudScopeId) throws CloudManagerException {
		if (
				!Boolean.TRUE.equals(HinemosSessionContext.instance().getProperty(HinemosSessionContext.IS_ADMINISTRATOR))
				) {
				try {
					HinemosEntityManager em = Session.current().getEntityManager();
					Query query1 = em.createNamedQuery("findCloudScopeByHinemosUser");
					query1.setParameter("cloudScopeId", cloudScopeId);
					query1.setParameter("userId", Session.current().getHinemosCredential().getUserId());

					query1.getSingleResult();
				}
				catch (NoResultException e1) {
					throw ErrorCode.CLOUDSCOPE_INVALID_CLOUDSCOPE_NOT_FOUND.cloudManagerFault(cloudScopeId);
				}
			}

	}
}
