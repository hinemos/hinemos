/*

Copyright (C) 2006 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.jobmanagement;

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
		}
		return -1;
	}
}