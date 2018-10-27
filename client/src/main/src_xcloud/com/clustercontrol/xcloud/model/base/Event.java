/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.model.base;

/**
 * イベントの基底クラス。
 *
 * @param <S>
 * @param <L>
 */
public abstract class Event <S, L extends EventListener> {
	private S source;
	public Event(S source) {
		this.source = source;
	}
	/**
	 * イベントの発行者を取得。
	 * 
	 * @return
	 */
	public S getSource() {
		return source;
	}
	public abstract void dispatch(L listener);
	public abstract ListenerType<L> getAssociatedType();
}
