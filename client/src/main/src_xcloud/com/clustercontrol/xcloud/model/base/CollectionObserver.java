/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.model.base;



public interface CollectionObserver<P> extends PropertyObserver<P> {
	public class ElementAddedEvent<P> extends PropEvent<P, CollectionObserver<P>> {
		private P addedElement;
		public ElementAddedEvent(Object source, PropertyId<CollectionObserver<P>> pid, P addedElement) {
			super(source, pid);
			this.addedElement = addedElement;
		}
		public P getAddedElement() {
			return addedElement;
		}
		@Override
		public void dispatch(CollectionObserver<P> observer) {
			observer.elementAdded(this);
		}
	}
	
	public class ElementRemovedEvent<P> extends PropEvent<P, CollectionObserver<P>> {
		private P removedValue;
		public ElementRemovedEvent(Object source, PropertyId<CollectionObserver<P>> pid, P addedValue) {
			super(source, pid);
			this.removedValue = addedValue;
		}
		public P getRemovedElement() {
			return removedValue;
		}
		@Override
		public void dispatch(CollectionObserver<P> observer) {
			observer.elementRemoved(this);
		}
	}
	void elementAdded(ElementAddedEvent<P> event);
	void elementRemoved(ElementRemovedEvent<P> event);
}
