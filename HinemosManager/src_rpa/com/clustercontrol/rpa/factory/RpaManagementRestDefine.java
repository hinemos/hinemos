/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rpa.factory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.classic.methods.HttpUriRequest;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpEntity;

import com.clustercontrol.rpa.factory.bean.RpaResourceInfo;
import com.clustercontrol.util.MessageConstant;

/**
 * RPA管理ツールのREST APIを定義するクラス<BR>
 * 本クラスを継承したクラスで各RPA管理ツールのAPIを定義する。
 */
public abstract class RpaManagementRestDefine {

	/*
	 * APIバージョン
	 * @see RpaManagementToolMst#apiVersion
	 */
	protected final int apiVersion;
	
	// コンストラクタでAPIバージョン、URL、アカウントID、パスワードを指定する。
	public RpaManagementRestDefine(int apiVersion) {
		this.apiVersion = apiVersion;
	}
	
	// --util
	/**
	 * ヘッダ、ボディからHTTPリクエストを作成する。
	 */
	protected HttpUriRequest createRequest(HttpUriRequest request, Header[] headers, HttpEntity entity) {
		request.setHeaders(headers);
		request.setEntity(entity);
		
		return request;
	}
	
	// -- Token取得処理の定義
	// Tokenを取得するHTTPリクエストを定義する(URL、メソッド)
	abstract protected HttpUriRequest createGenerateTokenRequest(String baseUrl);

	// Tokenを取得するHTTPリクエストヘッダを定義する
	abstract protected Header[] createGenerateTokenHeader(String tenantName);
	
	// Tokenを取得するHTTPリクエストボディを定義する
	abstract protected HttpEntity createGenerateTokenEntity(String accountId, String password, String tenantName);

	// Tokenを取得するHTTPリクエストを返す。
	protected HttpUriRequest getGenerateTokenRequest(String baseUrl, String accountId, String password, String tenantName) {
		return createRequest(createGenerateTokenRequest(baseUrl), 
				createGenerateTokenHeader(tenantName), 
				createGenerateTokenEntity(accountId, password, tenantName));
	}

	// HttpResponseからTokenを取得するハンドラを定義する。
	abstract protected RpaManagementRestResponseHandler<String> getGenerateTokenResponseHandler();
	
	/**
	 * 引数のクライアントでTokenを取得する。
	 * RpaManagementRestTokenCacheから呼び出されます。<br>
	 * @see RpaManagementRestTokenCache
	 */
	public String getToken(String baseUrl, String accountId, String password, String tenantName, HttpClient client) throws IOException {
		HttpUriRequest request = this.getGenerateTokenRequest(baseUrl, accountId, password, tenantName);
		return client.execute(request, this.getGenerateTokenResponseHandler());
	}
	
	/**
	 * Tokenの有効時間を返す。
	 * @return Tokenの有効時間（ミリ秒）
	 */
	abstract public long getTokenExpiredMillis();
	
	/**
	 * Tokenを取得するAPIリクエストのURLを取得する。
	 */
	public String getGenerateTokenRequestUrl(String baseUrl) {
		try {
			return createGenerateTokenRequest(baseUrl).getUri().toString();
		} catch (URISyntaxException e) {
			// baseUrlの形式が不正
			return "";
		}
	}
	
	// -- RPAリソース取得APIの定義
	/**
	 * 引数のクライアントでRPAリソース自動検知に必要な情報を取得する処理を定義する。
	 */
	abstract public List<RpaResourceInfo> getRpaResourceInfo(String baseUrl, String token, HttpClient client) throws IOException;

	
	/**
	 *  APIバージョンがリソース取得が有効なバージョンである場合はtrue, 無効なバージョンの場合はfalseを返す。
	 */
	abstract public boolean enabledRpaResourceDetection();

	// -- ヘルスチェックAPIの定義
	
	// ヘルスチェックAPIのHTTPリクエストを定義する(URL、メソッド)
	abstract protected HttpUriRequest createHealthCheckRequest(String baseUrl);

	// ヘルスチェックAPIのHTTPリクエストヘッダを定義する
	abstract protected Header[] createHealthCheckHeader(String token);
	
	// ヘルスチェックAPIのHTTPリクエストボディを定義する
	abstract protected HttpEntity createHealthCheckEntity();

	// ヘルスチェックAPIのHTTPリクエストを返す。
	protected HttpUriRequest getHealthCheckRequest(String baseUrl, String token) {
		return createRequest(createHealthCheckRequest(baseUrl), 
				createHealthCheckHeader(token),
				createHealthCheckEntity());
	}

	// ヘルスチェックAPIを取得する情報のハンドラを定義する。
	abstract protected RpaManagementRestResponseHandler<String> getHealthCheckResponseHandler();
	
	/**
	 * 引数のクライアントでヘルスチェックAPIを実行する。
	 */
	public String healthCheck(String baseUrl, String token, HttpClient client) throws IOException {
		HttpUriRequest request = this.getHealthCheckRequest(baseUrl, token);
		return client.execute(request, this.getHealthCheckResponseHandler());
	}
	
	/**
	 * ヘルスチェックAPIリクエストのURLを取得する。
	 */
	public String getHealthCheckRequestUrl(String baseUrl) {
		try {
			return createHealthCheckRequest(baseUrl).getUri().toString();
		} catch (URISyntaxException e) {
			// baseUrlの形式が不正
			return "";
		}
	}
	
	/**
	 * ヘルスチェックで取得する情報名のMessageConstantを定義する。
	 */
	abstract public MessageConstant getHealthCheckInfo();
	
	
	// リクエストからURLをメッセージとして出力
	protected String getRequestMessage(HttpUriRequest request) {
		return String.format("URL=%s, ", request.getRequestUri());
	}


	// -- シナリオ実行APIの定義
	
	// シナリオ実行APIのHTTPリクエストを定義する(URL、メソッド)
	abstract protected HttpUriRequest createRunRequest(String baseUrl, Integer runType);

	// シナリオ実行APIのHTTPリクエストヘッダを定義する
	abstract protected Header[] createRunHeader(String token);
	
	// シナリオ実行APIのHTTPリクエストボディを定義する
	abstract protected HttpEntity createRunEntity(Map<String, Object> requestData, Integer runType);

	// シナリオ実行APIのHTTPリクエストを返す。
	protected HttpUriRequest getRunRequest(String baseUrl, String token, Map<String, Object> requestData, Integer runType) {
		return createRequest(createRunRequest(baseUrl, runType), 
				createRunHeader(token),
				createRunEntity(requestData, runType));
	}

	// シナリオ実行APIを取得する情報のハンドラを定義する。
	abstract protected RpaManagementRestResponseHandler<String> getRunResponseHandler(Map<String, Object> requestData);
	
	// シナリオ実行APIへ渡すリクエストデータを必要に応じて変更する
	abstract protected Map<String, Object> adjustRunRequestData(Map<String, Object> requestData);

	/**
	 * 引数のクライアントでシナリオ実行APIを実行する。
	 * @return RPA管理ツールの実行識別子(ex. job_id)
	 */
	public String run(String baseUrl, String token, Map<String, Object> requestData, Integer runType, HttpClient client) throws IOException {
		// 必要に応じてリクエストデータを加工
		Map<String, Object> runRequestData = adjustRunRequestData(requestData);
		HttpUriRequest request = this.getRunRequest(baseUrl, token, runRequestData, runType);
		return client.execute(request, this.getRunResponseHandler(runRequestData));
	}

	// -- シナリオ結果確認APIの定義
	
	// シナリオ結果確認APIのHTTPリクエストを定義する(URL、メソッド)
	abstract protected HttpUriRequest createCheckRequest(String baseUrl, String runIdentifier);

	// シナリオ結果確認APIのHTTPリクエストヘッダを定義する
	abstract protected Header[] createCheckHeader(String token);
	
	// シナリオ結果確認APIのHTTPリクエストボディを定義する
	abstract protected HttpEntity createCheckEntity(String runIdentifier);

	// シナリオ結果確認APIのHTTPリクエストを返す。
	protected HttpUriRequest getCheckRequest(String baseUrl, String token, String runIdentifier) {
		return createRequest(createCheckRequest(baseUrl, runIdentifier), 
				createCheckHeader(token),
				createCheckEntity(runIdentifier));
	}

	// シナリオ結果確認APIを取得する情報のハンドラを定義する。
	abstract protected RpaManagementRestResponseHandler<String> getCheckResponseHandler();
	
	/**
	 * 引数のクライアントでシナリオ結果確認APIを実行する。
	 * @return 実行ステータス
	 */
	public String check(String baseUrl, String token, String runIdentifier, HttpClient client) throws IOException {
		HttpUriRequest request = this.getCheckRequest(baseUrl, token, runIdentifier);
		return client.execute(request, this.getCheckResponseHandler());
	}

	// -- シナリオ実行キャンセルAPIの定義
	
	// シナリオ実行キャンセルAPIのHTTPリクエストを定義する(URL、メソッド)
	abstract protected HttpUriRequest createCancelRequest(String baseUrl, String runIdentifier);

	// シナリオ実行キャンセルAPIのHTTPリクエストヘッダを定義する
	abstract protected Header[] createCancelHeader(String token);
	
	// シナリオ実行キャンセルAPIのHTTPリクエストボディを定義する
	abstract protected HttpEntity createCancelEntity(String runIdentifier, Integer stopMode);

	// シナリオ実行キャンセルAPIのHTTPリクエストを返す。
	protected HttpUriRequest getCancelRequest(String baseUrl, String token, String runIdentifier, Integer stopMode) {
		return createRequest(createCancelRequest(baseUrl, runIdentifier), 
				createCancelHeader(token),
				createCancelEntity(runIdentifier, stopMode));
	}

	// シナリオ実行キャンセルAPIを取得する情報のハンドラを定義する。
	abstract protected RpaManagementRestResponseHandler<String> getCancelResponseHandler();
	
	/**
	 * 引数のクライアントでシナリオ実行キャンセルAPIを実行する。
	 * @return 実行ステータス
	 */
	public String cancel(String baseUrl, String token, String runIdentifier, Integer stopMode, HttpClient client) throws IOException {
		HttpUriRequest request = this.getCancelRequest(baseUrl, token, runIdentifier, stopMode);
		return client.execute(request, this.getCancelResponseHandler());
	}
	
	/**
	 * 指定された停止種別が利用可能かどうかを返します。
	 * @param stopType 停止種別
	 * @return true : 利用可能 / false : 利用不可能
	 */
	public boolean checkRpaStopType(int stopType) {
		return true;
	}
}
