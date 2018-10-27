/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmap.view;

import com.clustercontrol.jobmanagement.view.JobHistoryView;

/**
 * HinemosClientプロジェクトのJobHistoryViewからJobMapViewが参照できないようなので、
 * JobMapプロジェクトにJobHistoryViewと同様のものを作る。
 *
 */
public class JobHistoryViewM extends JobHistoryView {
	/** ビューID */
	public static final String ID = JobHistoryViewM.class.getName();
}
