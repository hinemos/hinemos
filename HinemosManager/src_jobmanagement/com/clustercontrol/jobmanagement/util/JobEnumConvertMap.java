/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype.ConditionTypeEnum;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype.DecisionObjectEnum;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype.EndStatusSelectEnum;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype.JobTypeEnum;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype.OperationEndDelayEnum;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype.OperationMultipleEnum;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype.OperationStartDelayEnum;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype.PrioritySelectEnum;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype.ReferJobSelectTypeEnum;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype.WaitStatusEnum;

public class JobEnumConvertMap {

	private Map<Integer, JobTypeEnum> jobTypeEnumMap = null;
	private Map<Integer, ReferJobSelectTypeEnum> referJobSelectTypeEnumMap = null;
	private Map<Integer, PrioritySelectEnum> prioritySelectEnumMap = null;
	private Map<Integer, EndStatusSelectEnum> endStatusSelectEnumMap = null;
	private Map<Integer, WaitStatusEnum> waitStatusEnumMap = null;
	private Map<Integer, DecisionObjectEnum> decisionObjectEnumMap = null;
	private Map<Integer, ConditionTypeEnum> conditionTypeEnumMap = null;
	private Map<Integer, OperationMultipleEnum> operationMultipleEnumMap = null;
	private Map<Integer, OperationEndDelayEnum> operationEndDelayEnumMap = null;
	private Map<Integer, OperationStartDelayEnum> operationStartDelayEnumMap = null;

	public JobEnumConvertMap() {
	}

	public PrioritySelectEnum getPrioritySelectEnum(Integer item) {
		if (item == null) {
			return null;
		}
		if (prioritySelectEnumMap != null) {
			return prioritySelectEnumMap.get(item);
		}
		prioritySelectEnumMap = new ConcurrentHashMap<>();
		for (PrioritySelectEnum jobEnum : PrioritySelectEnum.values()) {
			prioritySelectEnumMap.put(jobEnum.getCode(), jobEnum);
		}
		return prioritySelectEnumMap.get(item);
	}

	public EndStatusSelectEnum getEndStatusSelectEnum (Integer item) {
		if (item == null) {
			return null;
		}
		if (endStatusSelectEnumMap != null) {
			return endStatusSelectEnumMap.get(item);
		}
		endStatusSelectEnumMap = new ConcurrentHashMap<>();
		for (EndStatusSelectEnum jobEnum : EndStatusSelectEnum.values()) {
			endStatusSelectEnumMap.put(jobEnum.getCode(), jobEnum);
		}
		return endStatusSelectEnumMap.get(item);
	}

	public WaitStatusEnum getWaitStatusEnum(Integer item) {
		if (item == null) {
			return null;
		}
		if (waitStatusEnumMap != null) {
			return waitStatusEnumMap.get(item);
		}
		waitStatusEnumMap = new ConcurrentHashMap<>();
		for (WaitStatusEnum jobEnum : WaitStatusEnum.values()) {
			waitStatusEnumMap.put(jobEnum.getCode(), jobEnum);
		}
		return waitStatusEnumMap.get(item);
	}

	public DecisionObjectEnum getDecisionObjectEnum(Integer item) {
		if (item == null) {
			return null;
		}
		if (decisionObjectEnumMap != null) {
			return decisionObjectEnumMap.get(item);
		}
		decisionObjectEnumMap = new ConcurrentHashMap<>();
		for (DecisionObjectEnum jobEnum : DecisionObjectEnum.values()) {
			decisionObjectEnumMap.put(jobEnum.getCode(), jobEnum);
		}
		return decisionObjectEnumMap.get(item);

	}

	public ConditionTypeEnum getConditionTypeEnum(Integer item) {
		if (item == null) {
			return null;
		}
		if (conditionTypeEnumMap != null) {
			return conditionTypeEnumMap.get(item);
		}
		conditionTypeEnumMap = new ConcurrentHashMap<>();
		for (ConditionTypeEnum jobEnum : ConditionTypeEnum.values()) {
			conditionTypeEnumMap.put(jobEnum.getCode(), jobEnum);
		}
		return conditionTypeEnumMap.get(item);
	}

	public OperationMultipleEnum getOperationMultipleEnum(Integer item) {
		if (item == null) {
			return null;
		}
		if (operationMultipleEnumMap != null) {
			return operationMultipleEnumMap.get(item);
		}
		operationMultipleEnumMap = new ConcurrentHashMap<>();
		for (OperationMultipleEnum jobEnum : OperationMultipleEnum.values()) {
			operationMultipleEnumMap.put(jobEnum.getCode(), jobEnum);
		}
		return operationMultipleEnumMap.get(item);
	}

	public OperationEndDelayEnum getOperationEndDelayEnum(Integer item) {
		if (item == null) {
			return null;
		}
		if (operationEndDelayEnumMap != null) {
			return operationEndDelayEnumMap.get(item);
		}
		operationEndDelayEnumMap = new ConcurrentHashMap<>();
		for (OperationEndDelayEnum jobEnum : OperationEndDelayEnum.values()) {
			operationEndDelayEnumMap.put(jobEnum.getCode(), jobEnum);
		}
		return operationEndDelayEnumMap.get(item);

	}

	public OperationStartDelayEnum getOperationStartDelayEnum(Integer item) {
		if (item == null) {
			return null;
		}
		if (operationStartDelayEnumMap != null) {
			return operationStartDelayEnumMap.get(item);
		}
		operationStartDelayEnumMap = new ConcurrentHashMap<>();
		for (OperationStartDelayEnum jobEnum : OperationStartDelayEnum.values()) {
			operationStartDelayEnumMap.put(jobEnum.getCode(), jobEnum);
		}
		return operationStartDelayEnumMap.get(item);
	}

	public JobTypeEnum getJobTypeEnum(Integer item) {
		if (item == null) {
			return null;
		}
		if (jobTypeEnumMap != null) {
			return jobTypeEnumMap.get(item);
		}
		jobTypeEnumMap = new ConcurrentHashMap<>();
		for (JobTypeEnum jobEnum : JobTypeEnum.values()) {
			jobTypeEnumMap.put(jobEnum.getCode(), jobEnum);
		}
		return jobTypeEnumMap.get(item);
	}

	public ReferJobSelectTypeEnum getReferJobSelectTypeEnum(Integer item) {
		if (item == null) {
			return null;
		}
		if (referJobSelectTypeEnumMap != null) {
			return referJobSelectTypeEnumMap.get(item);
		}
		referJobSelectTypeEnumMap = new ConcurrentHashMap<>();
		for (ReferJobSelectTypeEnum jobEnum : ReferJobSelectTypeEnum.values()) {
			referJobSelectTypeEnumMap.put(jobEnum.getCode(), jobEnum);
		}
		return referJobSelectTypeEnumMap.get(item);
	}
}
