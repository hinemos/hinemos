/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.model.base;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



public class PropChangedEventNotifier {
	public static final PropertyId<AnyPropertyObserver> anyPropertyId = new PropertyId<AnyPropertyObserver>("any"){};

	private interface Command {
		void execute();
	}

	private Map<Object, List<?>> listenersMap = new HashMap<Object, List<?>>();
	private List<Command> commands = new ArrayList<Command>();
	protected int notifyingDepth = 0;
	
	public PropChangedEventNotifier() {
	}

	public <P, O extends PropertyObserver<P>> void addPropertyObserver(final PropertyId<O> pid, final O observer) {
		synchronized (listenersMap) {
			if (notifyingDepth == 0) {
				doAdd(pid, observer);
			} else {
				commands.add(
					new Command() {
						@Override
						public void execute() {
							doAdd(pid, observer);
						}
					});
			}
		}
	}
	
	public <P, O extends PropertyObserver<P>> void addAnyPropertyObserver(AnyPropertyObserver observer) {
		addPropertyObserver(anyPropertyId, observer);
	}
	
	private void doAdd(Object pid, Object observer) {
		@SuppressWarnings("unchecked")
		List<Object> list = (List<Object>)listenersMap.get(pid);
		if (list == null) {
			list = new ArrayList<Object>();
			listenersMap.put(pid, list);
		}
		list.add(observer);
	}
	
	public <P, O extends PropertyObserver<P>> void removePropertyObserver(final PropertyId<O> pid, final O observer) {
		synchronized (listenersMap) {
			if (notifyingDepth == 0) {
				doRemove(pid, observer);
			} else {
				commands.add(
					new Command() {
						@Override
						public void execute() {
							doRemove(pid, observer);
						}
					});
			}
		}
	}
	
	public <P, O extends PropertyObserver<P>> void removeAnyPropertyObserver(AnyPropertyObserver observer) {
		removePropertyObserver(anyPropertyId, observer);
	}

	private void doRemove(Object pid, Object observer) {
		@SuppressWarnings("unchecked")
		List<Object> list = (List<Object>)listenersMap.get(pid);
		if (list != null) {
			list.remove(observer);
		}
	}
	
	public <P, O extends PropertyObserver<P>> boolean propObsContained(PropertyId<O> pid, O observer) {
		@SuppressWarnings("unchecked")
		List<Object> list = (List<Object>)listenersMap.get(pid);
		return list.contains(observer);
	}
	
	protected <P, O extends PropertyObserver<P>> void fireEvent(PropEvent<P, O> event) {
		synchronized (listenersMap) {
			try {
				++notifyingDepth;
				@SuppressWarnings("unchecked")
				List<O> list = (List<O>)listenersMap.get(event.getPropertyId());
				if (list != null) {
					for (O observer: list) {
						event.dispatch(observer);
					}
				}
			} finally {
				for (Command c: commands) {
					c.execute();
				}
				commands.clear();
				--notifyingDepth;
			}
		}
	}

	public <P> void fireValueChanged(Object source, PropertyId<ValueObserver<P>> pid, P newValue, P oldValue) {
		ValueObserver.ValueChangedEvent<P> event = new ValueObserver.ValueChangedEvent<P>(source, pid, newValue, oldValue);
		fireEvent(event);
		AnyPropertyObserver.ValueChangedEvent event2 = new AnyPropertyObserver.ValueChangedEvent(source, pid, newValue, oldValue);
		fireEvent(event2);
	}

	public <P> void fireElementAdded(Object source, PropertyId<CollectionObserver<P>> pid, P addedValue) {
		CollectionObserver.ElementAddedEvent<P> event = new CollectionObserver.ElementAddedEvent<P>(source, pid, addedValue);
		fireEvent(event);
		AnyPropertyObserver.ElementAddedEvent event2 = new AnyPropertyObserver.ElementAddedEvent(source, pid, addedValue);
		fireEvent(event2);
	}

	public <P> void fireElementRemoved(Object source, PropertyId<CollectionObserver<P>> pid, P removedValue) {
		CollectionObserver.ElementRemovedEvent<P> event = new CollectionObserver.ElementRemovedEvent<P>(source, pid, removedValue);
		fireEvent(event);
		AnyPropertyObserver.ElementRemovedEvent event2 = new AnyPropertyObserver.ElementRemovedEvent(source, pid, removedValue);
		fireEvent(event2);
	}
}
