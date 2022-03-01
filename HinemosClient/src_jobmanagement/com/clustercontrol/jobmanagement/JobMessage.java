/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement;

import com.clustercontrol.jobmanagement.util.JobInfoWrapper;

import com.clustercontrol.jobmanagement.bean.JobConstant;
import com.clustercontrol.util.Messages;

/**
 * ジョブ種別（ユニット、ネット、ジョブ）の定数クラス<BR>
 *
 * @version 4.1.0
 * @since 1.0.0
 */
public class JobMessage {

	/** ジョブユニット(ジョブの種別) */
	public static final String STRING_JOBUNIT = Messages.getString("jobunit");

	/** ジョブネット(ジョブの種別) */
	public static final String STRING_JOBNET = Messages.getString("jobnet");

	/** ジョブ(ジョブの種別) */
	public static final String STRING_JOB = Messages.getString("command.job");

	/** ファイル転送ジョブ(ジョブの種別) */
	public static final String STRING_FILEJOB = Messages.getString("forward.file.job");

	/** 参照ジョブ(ジョブの種別) */
	public static final String STRING_REFERJOB = Messages.getString("refer.job");

	/** 参照ジョブネット(ジョブの種別) */
	public static final String STRING_REFERJOBNET= Messages.getString("refer.jobnet");

	/** 承認ジョブ(ジョブの種別) */
	public static final String STRING_APPROVALJOB= Messages.getString("approval.job");

	/** 監視ジョブ(ジョブの種別) */
	public static final String STRING_MONITORJOB= Messages.getString("monitor.job");

	/** ファイルチェックジョブ(ジョブの種別) */
	public static final String STRING_FILECHECKJOB= Messages.getString("filecheck.job");

	/** ジョブ連携送信ジョブ(ジョブの種別) */
	public static final String STRING_JOBLINKSENDJOB= Messages.getString("joblink.send.job");

	/** ジョブ連携待機ジョブ(ジョブの種別) */
	public static final String STRING_JOBLINKRCVJOB= Messages.getString("joblink.rcv.job");

	/** リソース制御ジョブ(ジョブの種別) */
	public static final String STRING_RESOURCEJOB= Messages.getString("resource.control.job");

	/** RPAシナリオジョブ(ジョブの種別) */
	public static final String STRING_RPAJOB= Messages.getString("rpa.job");

	/**
	 * 種別から文字列に変換する
	 *
	 * @param type
	 * @return
	 */
	public static String typeToString(int type) {
		if (type == JobConstant.TYPE_COMPOSITE) {
			return JobConstant.STRING_COMPOSITE;
		} else if (type == JobConstant.TYPE_JOBUNIT) {
			return STRING_JOBUNIT;
		} else if (type == JobConstant.TYPE_JOBNET) {
			return STRING_JOBNET;
		} else if (type == JobConstant.TYPE_JOB) {
			return STRING_JOB;
		} else if (type == JobConstant.TYPE_FILEJOB) {
			return STRING_FILEJOB;
		} else if (type == JobConstant.TYPE_REFERJOB) {
			return STRING_REFERJOB;
		} else if (type == JobConstant.TYPE_REFERJOBNET) {
			return STRING_REFERJOBNET;
		} else if (type == JobConstant.TYPE_APPROVALJOB) {
			return STRING_APPROVALJOB;
		} else if (type == JobConstant.TYPE_MONITORJOB) {
			return STRING_MONITORJOB;
		} else if (type == JobConstant.TYPE_RESOURCEJOB) {
			return STRING_RESOURCEJOB;
		} else if (type == JobConstant.TYPE_FILECHECKJOB) {
			return STRING_FILECHECKJOB;
		} else if (type == JobConstant.TYPE_JOBLINKSENDJOB) {
			return STRING_JOBLINKSENDJOB;
		} else if (type == JobConstant.TYPE_JOBLINKRCVJOB) {
			return STRING_JOBLINKRCVJOB;
		} else if (type == JobConstant.TYPE_RPAJOB) {
			return STRING_RPAJOB;
		}
		return "";
	}

	/**
	 * 文字列から種別に変換する
	 *
	 * @param type
	 * @return
	 */
	public static int stringToType(String string) {
		if (string.equals(JobConstant.STRING_COMPOSITE)) {
			return JobConstant.TYPE_COMPOSITE;
		} else if (string.equals(STRING_JOBUNIT)) {
			return JobConstant.TYPE_JOBUNIT;
		} else if (string.equals(STRING_JOBNET)) {
			return JobConstant.TYPE_JOBNET;
		} else if (string.equals(STRING_JOB)) {
			return JobConstant.TYPE_JOB;
		} else if (string.equals(STRING_FILEJOB)) {
			return JobConstant.TYPE_FILEJOB;
		} else if (string.equals(STRING_REFERJOB)) {
			return JobConstant.TYPE_REFERJOB;
		} else if (string.equals(STRING_REFERJOBNET)) {
			return JobConstant.TYPE_REFERJOBNET;
		} else if (string.equals(STRING_APPROVALJOB)) {
			return JobConstant.TYPE_APPROVALJOB;
		} else if (string.equals(STRING_MONITORJOB)) {
			return JobConstant.TYPE_MONITORJOB;
		} else if (string.equals(STRING_FILECHECKJOB)) {
			return JobConstant.TYPE_FILECHECKJOB;
		} else if (string.equals(STRING_JOBLINKSENDJOB)) {
			return JobConstant.TYPE_JOBLINKSENDJOB;
		} else if (string.equals(STRING_JOBLINKRCVJOB)) {
			return JobConstant.TYPE_JOBLINKRCVJOB;
		} else if (string.equals(STRING_RESOURCEJOB)) {
			return JobConstant.TYPE_RESOURCEJOB;
		} else if (string.equals(STRING_RPAJOB)) {
			return JobConstant.TYPE_RPAJOB;
		}
		return -1;
	}



	/**
	 * 種別(Enum)から文字列に変換する
	 *
	 * @param type
	 * @return
	 */
	public static String typeEnumValueToString(String type) {
		if (type.equals(JobInfoWrapper.TypeEnum.COMPOSITE.getValue())) {
			return JobConstant.STRING_COMPOSITE;
		} else if (type.equals(JobInfoWrapper.TypeEnum.JOBUNIT.getValue())) {
			return STRING_JOBUNIT;
		} else if (type.equals(JobInfoWrapper.TypeEnum.JOBNET.getValue())) {
			return STRING_JOBNET;
		} else if (type.equals(JobInfoWrapper.TypeEnum.JOB.getValue())) {
			return STRING_JOB;
		} else if (type.equals(JobInfoWrapper.TypeEnum.FILEJOB.getValue())) {
			return STRING_FILEJOB;
		} else if (type.equals(JobInfoWrapper.TypeEnum.REFERJOB.getValue())) {
			return STRING_REFERJOB;
		} else if (type.equals(JobInfoWrapper.TypeEnum.REFERJOBNET.getValue())) {
			return STRING_REFERJOBNET;
		} else if (type.equals(JobInfoWrapper.TypeEnum.APPROVALJOB.getValue())) {
			return STRING_APPROVALJOB;
		} else if (type.equals(JobInfoWrapper.TypeEnum.MONITORJOB.getValue())) {
			return STRING_MONITORJOB;
		} else if (type.equals(JobInfoWrapper.TypeEnum.FILECHECKJOB.getValue())) {
			return STRING_FILECHECKJOB;
		} else if (type.equals(JobInfoWrapper.TypeEnum.JOBLINKSENDJOB.getValue())) {
			return STRING_JOBLINKSENDJOB;
		} else if (type.equals(JobInfoWrapper.TypeEnum.JOBLINKRCVJOB.getValue())) {
			return STRING_JOBLINKRCVJOB;
		} else if (type.equals(JobInfoWrapper.TypeEnum.RESOURCEJOB.getValue())) {
			return STRING_RESOURCEJOB;
		} else if (type.equals(JobInfoWrapper.TypeEnum.RPAJOB.getValue())) {
			return STRING_RPAJOB;
		}
		return "";
	}

	/**
	 * 文字列から種別に変換する
	 *
	 * @param type
	 * @return
	 */
	public static String stringToTypeEnumValue(String string) {
		if (string.equals(JobConstant.STRING_COMPOSITE)) {
			return JobInfoWrapper.TypeEnum.COMPOSITE.getValue();
		} else if (string.equals(STRING_JOBUNIT)) {
			return JobInfoWrapper.TypeEnum.JOBUNIT.getValue();
		} else if (string.equals(STRING_JOBNET)) {
			return JobInfoWrapper.TypeEnum.JOBNET.getValue();
		} else if (string.equals(STRING_JOB)) {
			return JobInfoWrapper.TypeEnum.JOB.getValue();
		} else if (string.equals(STRING_FILEJOB)) {
			return JobInfoWrapper.TypeEnum.FILEJOB.getValue();
		} else if (string.equals(STRING_REFERJOB)) {
			return JobInfoWrapper.TypeEnum.REFERJOB.getValue();
		} else if (string.equals(STRING_REFERJOBNET)) {
			return JobInfoWrapper.TypeEnum.REFERJOBNET.getValue();
		} else if (string.equals(STRING_APPROVALJOB)) {
			return JobInfoWrapper.TypeEnum.APPROVALJOB.getValue();
		} else if (string.equals(STRING_MONITORJOB)) {
			return JobInfoWrapper.TypeEnum.MONITORJOB.getValue();
		} else if (string.equals(STRING_FILECHECKJOB)) {
			return JobInfoWrapper.TypeEnum.FILECHECKJOB.getValue();
		} else if (string.equals(STRING_JOBLINKSENDJOB)) {
			return JobInfoWrapper.TypeEnum.JOBLINKSENDJOB.getValue();
		} else if (string.equals(STRING_JOBLINKRCVJOB)) {
			return JobInfoWrapper.TypeEnum.JOBLINKRCVJOB.getValue();
		} else if (string.equals(STRING_RESOURCEJOB)) {
			return JobInfoWrapper.TypeEnum.RESOURCEJOB.getValue();
		} else if (string.equals(STRING_RPAJOB)) {
			return JobInfoWrapper.TypeEnum.RPAJOB.getValue();
		}
		return null;
	}


}