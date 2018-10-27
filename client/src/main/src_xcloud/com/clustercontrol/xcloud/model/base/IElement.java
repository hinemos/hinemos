/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.model.base;

import java.util.List;

import org.eclipse.core.runtime.IAdaptable;

/**
 * 全ての要素の基底定義。
 *
 */
public interface IElement extends IAdaptable {
	/**
	 * イベントリスナーの追加。
	 * 
	 * @param <L>
	 * @param type
	 * @param listener
	 */
	<L extends EventListener> void addEventListener(ListenerType<L> type, L listener);

	/**
	 * イベントリスナーの削除。
	 * 
	 * @param <L>
	 * @param type
	 * @param listener
	 */
	<L extends EventListener> void removeEventListener(ListenerType<L> type, L listener);

	/**
	 * プロパティオブザーバーの追加。
	 * 
	 * @param <P>
	 * @param <O>
	 * @param pid
	 * @param observer
	 */
	<P, O extends PropertyObserver<P>> void addPropertyObserver(PropertyId<O> pid, O observer);

	/**
	 * プロパティオブザーバーの削除。
	 * 
	 * @param <P>
	 * @param <O>
	 * @param pid
	 * @param observer
	 */
	<P, O extends PropertyObserver<P>> void removePropertyObserver(PropertyId<O> pid, O observer);
	
	<P, O extends PropertyObserver<P>> boolean propObsContained(PropertyId<O> pid, O observer);
	
	void addAnyPropertyObserver(AnyPropertyObserver observer);

	void removeAnyPropertyObserver(AnyPropertyObserver observer);
	
	void setData(Object key, Object value);
	Object getData(Object key);
	<T> T getData(Object key, Class<T> clazz);
	
	
	List<PropertyId<?>> getPropertyIds();
	
	<O extends PropertyObserver<P>, P> P getTypedProperty(PropertyId<O> pid);

	Object getProperty(PropertyId<?> pid);

	IElement getOwner();
}
