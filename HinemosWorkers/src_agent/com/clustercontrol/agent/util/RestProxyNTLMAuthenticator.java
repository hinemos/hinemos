/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.agent.util;

import java.io.IOException;
import java.util.List;

import com.burgstaller.okhttp.digest.DigestAuthenticator;

import okhttp3.Authenticator;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;

public class RestProxyNTLMAuthenticator implements Authenticator {
    final RestProxyNTLMEngineImpl engine = new RestProxyNTLMEngineImpl();
    private final String domain;
    private final String username;
    private final String password;
    private final String ntlmMsg1;

    public RestProxyNTLMAuthenticator(String username, String password, String domain) {
        this.domain = domain;
        this.username = username;
        this.password = password;
        String localNtlmMsg1 = null;
        try {
            localNtlmMsg1 = engine.generateType1Msg(null, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        ntlmMsg1 = localNtlmMsg1;
    }

	@Override
	public Request authenticate(Route arg0, Response arg1) throws IOException {
        final List<String> WWWAuthenticate = arg1.headers().values(DigestAuthenticator.PROXY_AUTH);
        if (WWWAuthenticate.contains("NTLM")) {
            return arg1.request().newBuilder().header("Proxy-Authorization", "NTLM " + ntlmMsg1).build();
        }
        String ntlmMsg3 = null;
        try {
            ntlmMsg3 = engine.generateType3Msg(username, password, domain, "hinemos", WWWAuthenticate.get(0).substring(5));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return arg1.request().newBuilder().header("Proxy-Authorization", "NTLM " + ntlmMsg3).build();
	}
}