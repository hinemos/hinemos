/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.util;

import org.openapitools.client.model.CheckPublishResponse;

import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.RestConnectFailed;
import com.clustercontrol.xcloud.CloudManagerException;

/**
 * エンタープライズ機能 / クラウド・VM管理機能利用有無確認エンドポイントのインターフェース
 */
public interface ICheckPublishRestClientWrapper {
	CheckPublishResponse checkPublish() throws RestConnectFailed, InvalidUserPass, InvalidRole, HinemosUnknown, CloudManagerException;
}
