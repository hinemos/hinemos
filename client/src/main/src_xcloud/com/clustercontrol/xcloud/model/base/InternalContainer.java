/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.model.base;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.clustercontrol.xcloud.model.CloudModelException;


public abstract class InternalContainer<C extends IInternalComponent<?, S>, S> extends Element implements IInternalContainer<C, S> {
	private List<C> components;
	
	public boolean isInitialized() {
		return components != null;
	}

	public List<C> getComponents() {
		if (!isInitialized()) {
			update();
		}
		return components;
	}

	protected abstract List<S> getSources() throws CloudModelException;

	protected abstract C createComponent(S source) throws CloudModelException;
	
	@Override
	public void update() throws CloudModelException {
		List<S> sources = Collections.emptyList();
		try {
			sources = getSources();
		}
		catch (Exception e) {
			throw new CloudModelException(e);
		}

		internalUpdate(sources);
	}

	protected abstract void fireComponentAdded(C addedValue);
	
	protected abstract void fireComponentRemoved(C removedValue);
	
	public void internalUpdate(List<S> sources) throws CloudModelException {
		if (isInitialized()) {
			List<S> sourcesTemp = new ArrayList<>(sources);
			List<C> componentsTemp = new ArrayList<>(components);
			Iterator<S> sourcesIter = sourcesTemp.iterator();
			while (sourcesIter.hasNext()) {
				S source = sourcesIter.next();
				
				Iterator<C> componentsIter = componentsTemp.iterator();
				while (componentsIter.hasNext()) {
					C component = componentsIter.next();
					
					if (component.equalValues(source)) {
						component.internalUpdate(source);

						sourcesIter.remove();
						componentsIter.remove();
						break;
					}
				}
			}

			for (C component: componentsTemp) {
				getComponents().remove(component);
				fireComponentRemoved(component);
			}

			for (S source: sourcesTemp) {
				C component = createComponent(source);
				getComponents().add(component);
				fireComponentAdded(component);
			}
			fireEvent(new UpdateEvent(this));
		} else {
			List<C> tempComponents = new ArrayList<>();
			for (S source: sources) {
				tempComponents.add(createComponent(source));
			}
			components = tempComponents;
		}
	}
}
