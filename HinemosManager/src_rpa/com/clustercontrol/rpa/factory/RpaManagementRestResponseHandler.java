/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rpa.factory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.stream.Collectors;

import org.apache.hc.client5.http.ClientProtocolException;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.apache.hc.core5.http.message.StatusLine;

/**
 * RPA管理ツールREST API レスポンスハンドラの共通処理を定義するクラス
 * @param <T>
 */
public abstract class RpaManagementRestResponseHandler<T> implements HttpClientResponseHandler<T> {

	// レスポンスハンドラの共通処理
	@Override
	public T handleResponse(ClassicHttpResponse response) throws IOException {
		if (response.getCode() >= 300){
			// ステータスコード300以上の場合はClientProtocolExceptionをthrow
			throw new ClientProtocolException(getMessage(response));
		} else {
			// 200番台ステータスコードの場合レスポンスから抽出したデータを返す。
			return handleRpaManagementResponse(response);
		}
	}
	
	// レスポンスからの情報抽出処理を定義する。
	public abstract T handleRpaManagementResponse(ClassicHttpResponse response) throws IOException;
	
	// レスポンスからメッセージを作成
	protected String getMessage(ClassicHttpResponse response) {
		Header length = response.getFirstHeader("Content-Length");
		if (length != null && Integer.parseInt(length.getValue()) != 0) {
			try (BufferedReader br = new BufferedReader(new InputStreamReader(response.getEntity().getContent()))) {
				String content = br.lines().collect(Collectors.joining());
				return String.format("code=%d, message=%s, content=%s, headers=%s", response.getCode(), new StatusLine(response).toString(), content.toString(), Arrays.toString(response.getHeaders()));
			} catch(IOException e) {
				return String.format("code=%d, message=%s, headers=%s", response.getCode(), new StatusLine(response).toString(), Arrays.toString(response.getHeaders()));
			}
		} else {
			return String.format("code=%d, message=%s, headers=%s", response.getCode(), new StatusLine(response).toString(), Arrays.toString(response.getHeaders()));
		}
	}
}
