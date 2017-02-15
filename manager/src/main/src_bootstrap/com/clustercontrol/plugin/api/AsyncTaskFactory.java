/*

Copyright (C) 2012 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.plugin.api;

/**
 * 非同期処理の管理をAsyncWorkerPluginに委譲する場合に必要となる非同期処理のRunnableクラスのFactoryインタフェース<br/>
 * 1. AsyncTaskFactoryを実装したFactoryクラスを作成する<br/>
 * 2. ${HINEMOS_ETC_DIR}/async-worker.propertiesにFactoryクラスを登録する<br/>
 * 3. AsyncWorkerPlugin#addTask(Runnable r, Serializable param, boolean persist)により非同期処理を登録する<br/>
 */
public interface AsyncTaskFactory {

	/**
	 * 非同期処理の実行単位となるRunnableを返すメソッド<br/>
	 * @param param 実行に必要となるparameter
	 * @return 非同期処理の実態となるRunnableインスタンス
	 */
	public Runnable createTask(Object param);

}
