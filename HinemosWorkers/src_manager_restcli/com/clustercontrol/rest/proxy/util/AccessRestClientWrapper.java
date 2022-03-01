/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.proxy.util;

import org.openapitools.client.model.LoginRequest;
import org.openapitools.client.model.LoginResponse;
import org.openapitools.client.model.LogoutResponse;

import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.RestConnectFailed;
import com.clustercontrol.rest.ApiException;
import com.clustercontrol.rest.client.DefaultApi;

public class AccessRestClientWrapper {

	private DefaultApi apiClient;

	public AccessRestClientWrapper(DefaultApi apiClient) {
		this.apiClient = apiClient;
	}

	public LoginResponse loginByUrl(LoginRequest req)
			throws RestConnectFailed, HinemosUnknown, InvalidRole, InvalidUserPass, InvalidSetting {
		try {
			LoginResponse res = apiClient.accessLogin(req);
			return res;
		} catch (ApiException e) {
			try {
				throw ExceptionUtil.conversionApiException(e);
			} catch (RestConnectFailed connectFail) { // マネージャ接続エラー
				throw connectFail;
			} catch (HinemosUnknown | InvalidRole | InvalidUserPass | InvalidSetting def) {// 想定内例外
				throw def;
			} catch (Exception unknown) { // 想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
				throw new HinemosUnknown(unknown);
			}
		}
	}

	public LogoutResponse logout() throws RestConnectFailed, HinemosUnknown, InvalidRole, InvalidUserPass {
		try {
			LogoutResponse res = apiClient.accessLogout();
			return res;
		} catch (ApiException e) {
			try {
				throw ExceptionUtil.conversionApiException(e);
			} catch (RestConnectFailed connectFail) { // マネージャ接続エラー
				throw connectFail;
			} catch (HinemosUnknown | InvalidRole | InvalidUserPass def) {// 想定内例外
				throw def;
			} catch (Exception unknown) { // 想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
				throw new HinemosUnknown(unknown);
			}
		}
	}
}
