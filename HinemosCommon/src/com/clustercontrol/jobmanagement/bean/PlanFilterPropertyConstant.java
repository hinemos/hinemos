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

package com.clustercontrol.jobmanagement.bean;

/**
 * ジョブ[スケジュール予定]ビューのフィルタ用文字列定義を定数として定義するクラス<BR
 * 
 * 
 */
public class PlanFilterPropertyConstant {
	/** 日時（開始） */
	public static final String FROM_DATE = "fromDate";

	/** 日時（終了） */
	public static final String TO_DATE = "toDate";

	/** スケジュールID */
	public static final String JOBKICK_ID = "jobkickId";
}