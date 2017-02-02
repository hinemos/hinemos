/*

Copyright (C) 2016 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */
package com.clustercontrol.hub.session;

import com.clustercontrol.jobmanagement.model.JobSessionEntity;
import com.clustercontrol.notify.monitor.model.EventLogEntity;

/**
 * Transfer を継承したインスタンスは、TransferFactory から作成され、
 * 転送処理として対応するデータ毎の実装が施されている
 * 
 */
public interface Transfer extends AutoCloseable {
	
	interface TrasferCallback<T> {
		void onTransferred(T event) throws TransferException;
	}
	
	
	/**
	 * プラグイン ID を取得する
	 * 
	 * @return
	 */
	String getDestTypeId();
	
	
	/**
	 * イベントを転送する。
	 * 
	 * @param events
	 * @return
	 * @throws TransferException
	 */
	EventLogEntity transferEvents(Iterable<EventLogEntity> events, TrasferCallback<EventLogEntity> callback) throws TransferException;
	
	/**
	 * ジョブを転送する。
	 * 
	 * @param sessions
	 * @return
	 * @throws TransferException
	 */
	JobSessionEntity transferJobs(Iterable<JobSessionEntity> sessions, TrasferCallback<JobSessionEntity> callback) throws TransferException;
	
	/**
	 * 文字列情報を転送する。
	 * 
	 * @param string
	 * @return
	 * @throws TransferException
	 */
	TransferStringData transferStrings(Iterable<TransferStringData> string, TrasferCallback<TransferStringData> callback) throws TransferException;
	
	/**
	 * 数値情報を転送する。
	 * 
	 * @param numerics
	 * @return
	 * @throws TransferException
	 */
	TransferNumericData transferNumerics(Iterable<TransferNumericData> numerics, TrasferCallback<TransferNumericData> callback) throws TransferException;
}