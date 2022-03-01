/*******************************************************************************
 * Copyright (c) 2009, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.workbench.renderers.swt;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Named;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.model.application.ui.MContext;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.advanced.MPlaceholder;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.IPresentationEngine;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * Create an element from a reference
 */
public class ElementReferenceRenderer extends SWTPartRenderer {
	@Inject
	@Named(WorkbenchRendererFactory.SHARED_ELEMENTS_STORE)
	private Map<MUIElement, Set<MPlaceholder>> renderedMap;

	@Inject
	private IPresentationEngine renderingEngine;

	@Override
	public Object createWidget(final MUIElement element, Object parent) {
		MPlaceholder ph = (MPlaceholder) element;
		final MUIElement ref = ph.getRef();
		ref.setCurSharedRef(ph);

		Set<MPlaceholder> renderedRefs = renderedMap.get(ref);
		if (renderedRefs == null) {
			renderedRefs = new HashSet<MPlaceholder>();
			renderedMap.put(ref, renderedRefs);
		}

		if (!renderedRefs.contains(ph))
			renderedRefs.add(ph);

		Composite newComp = new Composite((Composite) parent, SWT.NONE);
		newComp.setLayout(new FillLayout());

		// if the placeholder is *not* in the currently active perspective
		// then don't re-parent the current view
		int phLoc = modelService.getElementLocation(ph);
		if (phLoc == EModelService.IN_ACTIVE_PERSPECTIVE
				|| phLoc == EModelService.IN_SHARED_AREA
				|| phLoc == EModelService.OUTSIDE_PERSPECTIVE) {
			Control refWidget = (Control) ref.getWidget();
			if (refWidget == null) {
				ref.setToBeRendered(true);
				refWidget = (Control) renderingEngine.createGui(ref, newComp,
						getContextForParent(ref));
			} else {
				if (refWidget.getParent() != newComp) {
					refWidget.setParent(newComp);
				}
			}

			if (ref instanceof MContext) {
				IEclipseContext context = ((MContext) ref).getContext();
				IEclipseContext newParentContext = getContext(ph);
				if (context.getParent() != newParentContext) {
					context.setParent(newParentContext);
				}
			}
		}

		return newComp;
	}

	@Override
	public void disposeWidget(MUIElement element) {
		MPlaceholder ph = (MPlaceholder) element;
		MUIElement refElement = ph.getRef();
		Control refCtrl = (Control) refElement.getWidget();

		// Remove the element ref from the rendered list
		Set<MPlaceholder> refs = renderedMap.get(refElement);
		refs.remove(ph);

		IEclipseContext curContext = modelService.getContainingContext(ph);

		if (refs.size() == 0) {
			// Ensure that the image is the 'original' image for this
			// part. See bug 347471 for details
			if (refElement instanceof MPart) {
				MPart thePart = (MPart) refElement;
				String imageURI = thePart.getIconURI();
				thePart.setIconURI(null);
				thePart.setIconURI(imageURI);
			}

			renderingEngine.removeGui(refElement);
		} else {
			// Ensure that the dispose of the element reference doesn't cascade
			// to dispose the 'real' part
			if (refCtrl != null && !refCtrl.isDisposed()) {
				MPlaceholder currentRef = refElement.getCurSharedRef();
				if (currentRef == ph) {
					// Find another *rendered* ref to pass the part on to
					for (MPlaceholder aPH : refs) {
						Composite phComp = (Composite) aPH.getWidget();
						if (phComp == null || phComp.isDisposed())
							continue;

						// Reparent the context(s) (if any)
						IEclipseContext newParentContext = modelService
								.getContainingContext(aPH);
						List<MContext> allContexts = modelService.findElements(
								refElement, null, MContext.class, null);
						for (MContext ctxtElement : allContexts) {
							IEclipseContext theContext = ctxtElement
									.getContext();
							// this may be null if it hasn't been rendered yet
							if (theContext != null) {
								if (theContext.getParent() == curContext) {
									// about to reparent the context, if we're
									// the active child of the current parent,
									// deactivate ourselves first
									if (curContext.getActiveChild() == theContext) {
										theContext.deactivate();
									}
									theContext.setParent(newParentContext);
								}
							}
						}

						// reset the 'cur' ref
						refElement.setCurSharedRef(aPH);

						// Reparent the widget
						refCtrl.setParent(phComp);
						break;
					}
				} else if (currentRef != null) {
					Composite phComp = (Composite) currentRef.getWidget();
					if (phComp == null || phComp.isDisposed()) {
						super.disposeWidget(element);
						return;
					}

					// Reparent the context(s) (if any)
					IEclipseContext newParentContext = modelService
							.getContainingContext(currentRef);
					List<MContext> allContexts = modelService.findElements(
							refElement, null, MContext.class, null);
					for (MContext ctxtElement : allContexts) {
						IEclipseContext theContext = ctxtElement.getContext();
						// this may be null if it hasn't been rendered yet
						if (theContext != null
								&& theContext.getParent() == curContext) {
							// about to reparent the context, if we're the
							// active child of the current parent, deactivate
							// ourselves first
							if (curContext.getActiveChild() == theContext) {
								theContext.deactivate();
							}
							theContext.setParent(newParentContext);
						}
					}
				}
			}
		}

		super.disposeWidget(element);
	}
}
