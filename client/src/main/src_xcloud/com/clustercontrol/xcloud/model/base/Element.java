/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.model.base;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.clustercontrol.xcloud.model.CloudModelException;


public abstract class Element implements IElement {
	protected Element owner; 

	protected static Map<Class<?>, List<PropertyId<?>>> propertyIdMap = new HashMap<>(); 
	
	/**
	 * プロパティ変更通知処理をカプセル化したクラス。
	 */
	protected final PropChangedEventNotifier propChangedNotifier = new PropChangedEventNotifier();

	/**
	 * イベント通知処理をカプセル化したクラス。
	 */
	protected final EventNotifier eventNotifire = new EventNotifier();
	
	public Element() {
	}

	@Override
	public <L extends EventListener> void addEventListener(ListenerType<L> type, L listener) {
		eventNotifire.addEventListeners(type, listener);
	}
	@Override
	public <L extends EventListener> void removeEventListener(ListenerType<L> type, L listener) {
		eventNotifire.removeEventListeners(type, listener);
	}

	/**
	 * 指定したイベントの通知を実施する。
	 * 
	 * @param <S>
	 * @param <L>
	 * @param event
	 */
	protected <S, L extends EventListener> void fireEvent(Event<S, L> event) {
		eventNotifire.fireEvent(event);
	}

	@Override
	public <P, O extends PropertyObserver<P>> void addPropertyObserver(PropertyId<O> pid, O observer) {
		propChangedNotifier.addPropertyObserver(pid, observer);
	}
	@Override
	public <P, O extends PropertyObserver<P>> void removePropertyObserver(PropertyId<O> pid, O observer) {
		propChangedNotifier.removePropertyObserver(pid, observer);
	}
	@Override
	public <P, O extends PropertyObserver<P>> boolean propObsContained(PropertyId<O> pid, O observer) {
		return propChangedNotifier.propObsContained(pid, observer);
	}

	@Override
	public void addAnyPropertyObserver(AnyPropertyObserver observer) {
		propChangedNotifier.addAnyPropertyObserver(observer);
	}

	@Override
	public void removeAnyPropertyObserver(AnyPropertyObserver observer) {
		propChangedNotifier.removeAnyPropertyObserver(observer);
	}
	/**
	 * シンプルプロパティの変更通知を実施する。
	 * 
	 * @param <P>
	 * @param source
	 * @param pid
	 * @param newValue
	 * @param oldValue
	 */
	public <P> void firePropertyChanged(Object source, PropertyId<ValueObserver<P>> pid, P newValue, P oldValue) {
		propChangedNotifier.fireValueChanged(this, pid, newValue, oldValue);
	}
	
	/**
	 * コレクションプロパティの要素追加通知を実施する。
	 * 
	 * @param <P>
	 * @param source
	 * @param pid
	 * @param addedValue
	 */
	public <P> void firePropertyAdded(PropertyId<CollectionObserver<P>> pid, P addedValue) {
		propChangedNotifier.fireElementAdded(this, pid, addedValue);
	}
	
	/**
	 * コレクションプロパティの要素削除通知を実施する。
	 * 
	 * @param <P>
	 * @param source
	 * @param pid
	 * @param removedValue
	 */
	public <P> void firePropertyRemoved(PropertyId<CollectionObserver<P>> pid, P removedValue) {
		propChangedNotifier.fireElementRemoved(this, pid, removedValue);
	}
	
	protected <T, P extends T> void internalSetProperty(PropertyId<ValueObserver<T>> pid, P newValue, Supplier<P> getter, Consumer<P> setter) {
		T prop = getter.get();

		if (prop == null) {
			if (newValue == null) {
				return;
			}
		} else {
			if (prop.equals(newValue)) {
				return;
			}
		}

		if (pid.isComposite()) {
			if (prop instanceof Element) {
				Element element = (Element)prop;
				element.setOwner(null);
			}

			if (newValue instanceof Element) {
				Element element = (Element)newValue;
				element.setOwner(this);
			}
		}
		
		setter.accept(newValue);
		
		// 更新分を通知。
		firePropertyChanged(this, pid, newValue, (T)prop);
	}
	
	protected <T, P extends T> void internalAddProperty(PropertyId<CollectionObserver<T>> pid, P addValue, List<P> props) {
		if (pid.isComposite()) {
			if (props.contains(addValue))
				throw new CloudModelException(String.format("Already related. %s", addValue.toString()));
		}
		
		props.add(addValue);
		
		if (pid.isComposite()) {
			if (addValue instanceof Element) {
				Element element = (Element)addValue;
				element.setOwner(this);
			}
		}
		
		// 更新分を通知。
		firePropertyAdded(pid, addValue);
	}

	protected <T, P extends T> void internalRemoveProperty(PropertyId<CollectionObserver<T>> pid, P removeValue, List<P> props) {
		props.remove(removeValue);
		
		if (pid.isComposite() && removeValue instanceof Element) {
			Element element = (Element)removeValue;
			element.setOwner(null);
		}
		
		// 更新分を通知。
		firePropertyRemoved(pid, removeValue);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Object getAdapter(@SuppressWarnings("rawtypes") Class adapter) {
		return null;
	}

	@Override
	public List<PropertyId<?>> getPropertyIds() {
		List<PropertyId<?>> pids = propertyIdMap.get(this.getClass());
		if (pids == null) {
			Set<PropertyId<?>> set = new LinkedHashSet<>();
			for (Class<?> clazz = this.getClass(); clazz != Object.class; clazz = clazz.getSuperclass()) {
				for (Class<?> i: clazz.getInterfaces()) {
					for (Class<?> j: i.getDeclaredClasses()) {
						if (j.isInterface() && j.getSimpleName().equals("p")) {
							for (Field f: j.getFields()) {
								try {
									Object p = f.get(null);
									set.add((PropertyId<?>)p);
								} catch (IllegalArgumentException | IllegalAccessException e) {
									e.printStackTrace();
								}
							}
						}
					}
				}
			}
			pids = new ArrayList<>(set);
			propertyIdMap.put(this.getClass(), pids);
		}
		return pids;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <O extends PropertyObserver<P>, P> P getTypedProperty(PropertyId<O> pid) {
		return (P)getProperty(pid);
	}
	
	public Object getProperty(PropertyId<?> pid) {
		String getMethodName = "get" + pid.getPropertyName().substring(0, 1).toUpperCase() + pid.getPropertyName().substring(1);
		try {
			for (Class<?> clazz = this.getClass(); clazz != Object.class; clazz = clazz.getSuperclass()) {
				try {
					return clazz.getMethod(getMethodName).invoke(this);
				} catch(NoSuchMethodException e) {
				}
			}
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			throw new CloudModelException(e.getMessage(), e);
		}
		throw new CloudModelException(new NoSuchMethodException(String.format("%s has not %s method.", this.getClass().getName(), getMethodName)));
	}

	protected Field getField(PropertyId<?> pid) {
		Field f = null;
		for (Class<?> clazz = this.getClass(); clazz != Object.class; clazz = clazz.getSuperclass()) {
			try {
				f = clazz.getDeclaredField(pid.getPropertyName());
				break;
			} catch (NoSuchFieldException e) {
				
			} catch (Exception e) {
				throw new CloudModelException(e);
			}
		}
		if (f == null)
			throw new IllegalStateException(String.format("not found field named \"%s\" on %s.", pid.getPropertyName(),this.getClass().getSimpleName()));
		return f;
	}
	
	private Map<Object, Object> dataMap; 
	
	@Override
	public Object getData(Object key) {
		if (dataMap == null)
			return null;
		return dataMap.get(key);
	}
	
	@SuppressWarnings("unchecked")
	public <T> T getData(Object key, Class<T> clazz) {
		if (dataMap == null)
			return null;
		return (T)dataMap.get(key);
	}
	
	@Override
	public void setData(Object key, Object value) {
		if (dataMap == null)
			dataMap = new HashMap<>();
		dataMap.put(key, value);
	}

	@Override
	public Element getOwner() {
		return owner;
	}
	public void setOwner(Element owner) {
		this.owner = owner;
	}
}
