/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.model.base;

import com.clustercontrol.xcloud.model.CloudModelException;

public interface IUpdatable extends IElement {
	/**
	 * 要素の更新タイミングを観測するオブジェクトの定義。<br>
	 * addEventListener で観測対象の要素へ追加する。
	 * 
	 */
	interface ElementListerner extends EventListener {
		/**
		 * 要素が更新されたタイミングで呼び出されます。
		 * 
		 * @param event
		 */
		void elementUpdated(UpdateEvent event);
	}

	/**
	 * 内部の情報を最新にする。
	 */
	void update() throws CloudModelException;
}
