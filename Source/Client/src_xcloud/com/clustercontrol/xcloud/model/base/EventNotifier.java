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



public class EventNotifier {
	private interface Command {
		void execute();
	}
	
	private Map<Object, List<?>> listenersMap = new HashMap<Object, List<?>>();
	private List<Command> commands = new ArrayList<Command>();
	protected int notifyingDepth = 0;

	public EventNotifier() {
	}

	public <L extends EventListener> void addEventListeners(final ListenerType<L> type, final L listener) {
		synchronized (listenersMap) {
			if (notifyingDepth == 0) {
				doAdd(type, listener);
			} else {
				commands.add(
					new Command() {
						@Override
						public void execute() {
							doAdd(type, listener);
						}
					});
			}
		}
	}
	
	private <L extends EventListener> void doAdd(ListenerType<L> type, L listener) {
		@SuppressWarnings("unchecked")
		List<L> list = (List<L>)listenersMap.get(type);
		if (list == null) {
			list = new ArrayList<L>();
			listenersMap.put(type, list);
		}
		list.add(listener);
	}

	public <L extends EventListener> void removeEventListeners(final ListenerType<L> type, final L listener) {
		synchronized (listenersMap) {
			if (notifyingDepth == 0) {
				doRemove(type, listener);
			} else {
				commands.add(
					new Command() {
						@Override
						public void execute() {
							doRemove(type, listener);
						}
					});
			}
		}
	}
	
	private <L extends EventListener> void doRemove(ListenerType<L> type, L listener) {
		@SuppressWarnings("unchecked")
		List<L> list = (List<L>)listenersMap.get(type);
		if (list != null) {
			list.remove(listener);
		}
	}

	public <S, L extends EventListener> void fireEvent(Event<S, L> event) {
		synchronized (listenersMap) {
			try {
				++notifyingDepth;
				@SuppressWarnings("unchecked")
				List<L> list = (List<L>)listenersMap.get(event.getAssociatedType());
				if (list != null) {
					for (L listener: list) {
						event.dispatch(listener);
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
}
