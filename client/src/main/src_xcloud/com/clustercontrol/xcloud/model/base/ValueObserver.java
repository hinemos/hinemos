/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.model.base;




public interface ValueObserver<P> extends PropertyObserver<P> {
	public class ValueChangedEvent<P> extends PropEvent<P, ValueObserver<P>> {
		private P newValue;
		private P oldValue;
		public ValueChangedEvent(Object source, PropertyId<ValueObserver<P>> pid, P newValue, P oldValue) {
			super(source, pid);
			this.newValue = newValue;
			this.oldValue = oldValue;
		}
		public P getNewValue() {
			return newValue;
		}
		public P getOldValue() {
			return oldValue;
		}
		@Override
		public void dispatch(ValueObserver<P> observer) {
			if ((getNewValue() != null && !getNewValue().equals(getOldValue())) || getNewValue() != getOldValue()) {
				observer.propertyChanged(this);
			}
		}
	}
	void propertyChanged(ValueChangedEvent<P> event);
}
