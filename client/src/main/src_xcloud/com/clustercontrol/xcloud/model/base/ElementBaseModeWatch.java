/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.model.base;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.xcloud.model.CloudModelException;
import com.clustercontrol.xcloud.model.base.AnyPropertyObserver.ElementAddedEvent;
import com.clustercontrol.xcloud.model.base.AnyPropertyObserver.ElementRemovedEvent;
import com.clustercontrol.xcloud.model.base.AnyPropertyObserver.ValueChangedEvent;

public class ElementBaseModeWatch {
	
	private static final Log logger = LogFactory.getLog(ElementBaseModeWatch.class);
	
	public interface AnyPropertyWatcher extends AnyPropertyObserver {
		public void unwatched(IElement owning, IElement owned);
	}
	
	protected class ElementWatcher implements AnyPropertyObserver {
		protected ElementWatcher parent;
		protected List<ElementWatcher> children = new ArrayList<>();
		protected List<AnyPropertyWatcher> propWatcher = new ArrayList<>();
		
		public ElementWatcher(ElementWatcher parent) {
			this.parent = parent;
		}
		
		@Override
		public void elementAdded(ElementAddedEvent event) {
			synchronized (elementWatcherMap) {
				try {
					++notifyingDepth;
					Object added = event.getAddedElement();
					logger.debug(String.format("%s added %s to %s", event.getSource().getClass().getSimpleName(),
							added == null ? null: added.toString(), event.getRealPid().getPropertyName()));
					
					notifyAdded(this, event);
		
					if (!event.getRealPid().isComposite())
						return;
					
					if (
						(event.getRealPid().getPropertyType() instanceof Class) && IElement.class.isAssignableFrom(((Class<?>)event.getRealPid().getPropertyType())) ||
						(event.getRealPid().getPropertyType() instanceof ParameterizedType) && IElement.class.isAssignableFrom((Class<?>)((ParameterizedType)event.getRealPid().getPropertyType()).getRawType())
						) {
						setup((IElement)added, this);
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

		@Override
		public void elementRemoved(ElementRemovedEvent event) {
			synchronized (elementWatcherMap) {
				try {
					++notifyingDepth;
					Object removed = event.getRemovedElement();
					logger.debug(String.format("%s removed %s from %s", event.getSource().getClass().getSimpleName(),
							removed == null ? null: removed.toString(), event.getRealPid().getPropertyName()));
					
					if (event.getRealPid().isComposite()) {
						if (
							(event.getRealPid().getPropertyType() instanceof Class) && IElement.class.isAssignableFrom(((Class<?>)event.getRealPid().getPropertyType())) ||
							(event.getRealPid().getPropertyType() instanceof ParameterizedType) && IElement.class.isAssignableFrom((Class<?>)((ParameterizedType)event.getRealPid().getPropertyType()).getRawType())
							) {
							cleanup((IElement)removed, (IElement)event.getSource(), this);
						}
					}
	
					notifyRemoved(this, event);
				} finally {
					for (Command c: commands) {
						c.execute();
					}
					commands.clear();
					--notifyingDepth;
				}
			}
		}

		@Override
		public void propertyChanged(ValueChangedEvent event) {
			synchronized (elementWatcherMap) {
				try {
					++notifyingDepth;
					Object changed = event.getNewValue();
					logger.debug(String.format("%s changed %s to %s", event.getSource().getClass().getSimpleName(),
							changed == null ? null: changed.toString(), event.getRealPid().getPropertyName()));
		
					notifyChanged(this, event);
		
					if (!event.getRealPid().isComposite())
						return;
					
					if (
						(event.getRealPid().getPropertyType() instanceof Class) && IElement.class.isAssignableFrom(((Class<?>)event.getRealPid().getPropertyType())) ||
						(event.getRealPid().getPropertyType() instanceof ParameterizedType) && IElement.class.isAssignableFrom((Class<?>)((ParameterizedType)event.getRealPid().getPropertyType()).getRawType())
						) {
						cleanup((IElement)event.getOldValue(), (IElement)event.getSource(), this);
						setup((IElement)changed, this);
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
	
	protected interface Command {
		void execute();
	}

	protected List<Command> commands = new ArrayList<Command>();
	protected int notifyingDepth = 0;

	protected Map<IElement, ElementWatcher> elementWatcherMap = new HashMap<>();

	public ElementBaseModeWatch(IElement root) {
		setup(root, null);
	}
	
	protected void setup(IElement element, ElementWatcher owningAny) {
		if (element == null)
			return;
		
		logger.debug(String.format("setup %s", element.toString()));
		
		ElementWatcher ownedAny = new ElementWatcher(owningAny);
		element.addAnyPropertyObserver(ownedAny);
		
		if (owningAny != null)
			owningAny.children.add(ownedAny);
		
		elementWatcherMap.put(element, ownedAny);
		
		List<PropertyId<?>> pids = element.getPropertyIds();
		for (PropertyId<?> pid: pids) {
			if (!pid.isComposite())
				continue;
			
			Type type = pid.getPropertyType();
			if (!((type instanceof Class) && (IElement.class.isAssignableFrom((Class<?>)type))))
				continue;
			
			Class<?> observerClass = pid.getObserverClass();
			if (observerClass == ValueObserver.class) {
				try {
					setup((IElement)element.getProperty(pid), ownedAny);
				} catch(Exception e) {
					logger.warn(e.getMessage(), e);
				}
			} else if (observerClass == CollectionObserver.class) {
				try {
					IElement[] owneds = (IElement[])element.getProperty(pid);
					for (IElement owned: owneds) {
						setup(owned, ownedAny);
					}
				} catch(Exception e) {
					logger.warn(e.getMessage(), e);
				}
			}
		}
	}
	
	protected void cleanup(IElement owned, IElement owning, ElementWatcher owningAny) {
		if (owned == null)
			return;
		
		logger.debug(String.format("cleanup %s", owned.toString()));
		
		ElementWatcher ownedAny = elementWatcherMap.get(owned);
		
		List<PropertyId<?>> pids = owned.getPropertyIds();
		for (PropertyId<?> pid: pids) {
			if (!pid.isComposite())
				continue;
			
			Type type = pid.getPropertyType();
			if (!((type instanceof Class) && (IElement.class.isAssignableFrom((Class<?>)type))))
				continue;
			
			Class<?> observerClass = pid.getObserverClass();
			if (observerClass == ValueObserver.class) {
				try {
					cleanup((IElement)owned.getProperty(pid), owned, ownedAny);
				} catch(Exception e) {
					logger.warn(e.getMessage(), e);
				}
			} else if (observerClass == CollectionObserver.class) {
				try {
					IElement[] furtherOwneds = (IElement[])owned.getProperty(pid);
					for (IElement furtherOwned: furtherOwneds) {
						cleanup(furtherOwned, owned, ownedAny);
					}
				} catch(Exception e) {
					logger.warn(e.getMessage(), e);
				}
			}
		}
		
		for (AnyPropertyWatcher propWatcher: ownedAny.propWatcher) {
			propWatcher.unwatched(owning, owned);
		}
		ownedAny.propWatcher.clear();

		owned.removeAnyPropertyObserver(ownedAny);
		
		if (owningAny != null)
			owningAny.children.remove(ownedAny);
		
		elementWatcherMap.remove(owned);
	}
	
	public void addWatcher(final IElement element, final AnyPropertyWatcher o) {
		synchronized (elementWatcherMap) {
			if (notifyingDepth == 0) {
				doAdd(element, o);
			} else {
				commands.add(
					new Command() {
						@Override
						public void execute() {
							doAdd(element, o);
						}
					});
			}
		}
	}
	
	private void doAdd(IElement element, AnyPropertyWatcher o) {
		ElementWatcher watcher = elementWatcherMap.get(element);
		if (watcher != null) {
			watcher.propWatcher.add(o);
		} else {
			throw new CloudModelException(String.format("Not watch %s", element.toString()));
		}
	}
	
	public void removeWatcher(final IElement element, final AnyPropertyWatcher o) {
		synchronized (elementWatcherMap) {
			if (notifyingDepth == 0) {
				doRemove(element, o);
			} else {
				commands.add(
					new Command() {
						@Override
						public void execute() {
							doRemove(element, o);
						}
					});
			}
		}
	}

	protected void doRemove(IElement element, AnyPropertyWatcher o) {
		ElementWatcher watcher = elementWatcherMap.get(element);
		if (watcher != null) {
			watcher.propWatcher.remove(o);
		} else {
			throw new CloudModelException(String.format("Not watch %s", element.toString()));
		}
	}
	
	protected void notifyAdded(ElementWatcher watcher, ElementAddedEvent event) {
		if (watcher == null)
			return;
		for (AnyPropertyWatcher propWatcher: watcher.propWatcher) {
			propWatcher.elementAdded(event);
		}
		notifyAdded(watcher.parent, event);
	}
	
	protected void notifyRemoved(ElementWatcher watcher, ElementRemovedEvent event) {
		if (watcher == null)
			return;
		for (AnyPropertyWatcher propWatcher: watcher.propWatcher) {
			propWatcher.elementRemoved(event);
		}
		notifyRemoved(watcher.parent, event);
	}
	
	protected void notifyChanged(ElementWatcher watcher, ValueChangedEvent event) {
		if (watcher == null)
			return;
		for (AnyPropertyWatcher propWatcher: watcher.propWatcher) {
			propWatcher.propertyChanged(event);
		}
		notifyChanged(watcher.parent, event);
	}
}
