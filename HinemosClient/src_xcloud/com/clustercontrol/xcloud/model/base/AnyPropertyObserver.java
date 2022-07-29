/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.model.base;


public interface AnyPropertyObserver extends PropertyObserver<Object> {
	public class ElementAddedEvent extends PropEvent<Object, AnyPropertyObserver> {
		private Object addedElement;
		private PropertyId<?> realPid;
		public ElementAddedEvent(Object source, PropertyId<?> realPid, Object addedElement) {
			super(source, PropChangedEventNotifier.anyPropertyId);
			this.addedElement = addedElement;
			this.realPid = realPid;
		}
		public PropertyId<?> getRealPid() {
			return realPid;
		}
		public Object getAddedElement() {
			return addedElement;
		}
		@Override
		public void dispatch(AnyPropertyObserver observer) {
			observer.elementAdded(this);
		}
	}
	
	public class ElementRemovedEvent extends PropEvent<Object, AnyPropertyObserver> {
		private Object removedValue;
		private PropertyId<?> realPid;
		public ElementRemovedEvent(Object source, PropertyId<?> realPid, Object addedValue) {
			super(source, PropChangedEventNotifier.anyPropertyId);
			this.removedValue = addedValue;
			this.realPid = realPid;
		}
		public Object getRemovedElement() {
			return removedValue;
		}
		public PropertyId<?> getRealPid() {
			return realPid;
		}
		@Override
		public void dispatch(AnyPropertyObserver observer) {
			observer.elementRemoved(this);
		}
	}
	void elementAdded(ElementAddedEvent event);
	void elementRemoved(ElementRemovedEvent event);

	public class ValueChangedEvent extends PropEvent<Object, AnyPropertyObserver> {
		private Object newValue;
		private Object oldValue;
		private PropertyId<?> realPid;
		public ValueChangedEvent(Object source, PropertyId<?> realPid, Object newValue, Object oldValue) {
			super(source, PropChangedEventNotifier.anyPropertyId);
			this.newValue = newValue;
			this.oldValue = oldValue;
			this.realPid = realPid;
		}
		public Object getNewValue() {
			return newValue;
		}
		public Object getOldValue() {
			return oldValue;
		}
		public PropertyId<?> getRealPid() {
			return realPid;
		}
		@Override
		public void dispatch(AnyPropertyObserver observer) {
			if ((getNewValue() != null && !getNewValue().equals(getOldValue())) || getNewValue() != getOldValue()) {
				observer.propertyChanged(this);
			}
		}
	}
	void propertyChanged(ValueChangedEvent event);
}
