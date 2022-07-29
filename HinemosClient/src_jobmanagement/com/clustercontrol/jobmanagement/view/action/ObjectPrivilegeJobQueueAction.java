/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.view.action;

import com.clustercontrol.view.action.ObjectPrivilegeAction;

/**
 * ジョブキュー(同時実行制御キュー)のオブジェクト権限変更を行います。
 *
 * @since 6.2.0
 */
public class ObjectPrivilegeJobQueueAction extends ObjectPrivilegeAction {
	public static final String ID = ObjectPrivilegeJobQueueAction.class.getName();
}
