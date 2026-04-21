/*
 * Copyright (c) 2026 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.monitor.dialog;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;

import com.clustercontrol.dialog.CommonDialog;
import com.clustercontrol.repository.RepositoryPerspective;
import com.clustercontrol.repository.composite.NodeAttributeComposite;
import com.clustercontrol.repository.view.NodeListView;
import com.clustercontrol.util.Messages;
import com.clustercontrol.xcloud.ui.dialogs.DialogConstants;

/*
 * リポジトリ[プロパティ]ビューの表示内容をダイアログとして表示
 * またリポジトリ[ノード]ビューで表示中のノードをフォーカスする
 */
public class NodePropertyDialog extends CommonDialog {
	/** ログ */
	private static Log log = LogFactory.getLog(NodePropertyDialog.class);
	
	protected NodeAttributeComposite composite;
	
	protected final String managerName;
	protected final String facilityId;
	
	public NodePropertyDialog(Shell parent, String managerName, String facilityId) {
		super(Objects.requireNonNull(parent));
		this.managerName = Objects.requireNonNull(managerName);
		this.facilityId = Objects.requireNonNull(facilityId);
	}
	
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		
		newShell.setText(Messages.getString("dialog.monitor.show.node.propetry"));
	}
	
	@Override
	protected void customizeDialog(Composite parent) {
		GridData gd_parent = new GridData(SWT.FILL, SWT.FILL, true, false);
		parent.setLayoutData(gd_parent);
		
		GridLayout layout = new GridLayout(1, false);
		layout.marginHeight = 10;
		layout.marginWidth = 10;
		layout.marginRight = 10;
		layout.marginLeft = 10;
		parent.setLayout(layout);
		
		composite = new NodeAttributeComposite(parent, SWT.BORDER);
		GridData data = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		data.heightHint = 400;
		composite.setLayoutData(data);
		composite.update(managerName, facilityId);
		
		Button b = new Button(parent, SWT.PUSH);
		b.setText(Messages.getString("message.show.in.repository.node.view"));
		b.setLayoutData(new GridData(SWT.RIGHT, SWT.FILL, true, true, 1, 1));
		b.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				IWorkbench workbench = PlatformUI.getWorkbench();
				IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
				if (window == null) {
					log.warn("IWorkbenchWindow is null. Aborting processing.");
					return;
				}
				
				IViewPart destView;
				try {
					IWorkbenchPage page = workbench.showPerspective(RepositoryPerspective.class.getName(), window);
					destView = page.showView(NodeListView.ID);
				} catch (WorkbenchException e) {
					log.warn("Failed to open a NodeListView.", e);
					return;
				}
				
				if (destView instanceof NodeListView) {
					NodeListView nodeListView = (NodeListView)destView;
					nodeListView.update();
					
					Collection<?> list = nodeListView.getAllElements();
					Optional<List<?>> node = list.stream().filter(List.class::isInstance).map(e->(List<?>)e)
							.filter(e->getFacilityIdInNodeListView(e).filter(id->facilityId.equals(id)).isPresent()).findFirst().map(e->(List<?>)e);
					
					if (node.isPresent()) {
						nodeListView.selectReveal(new StructuredSelection((Object)node.get()));
						nodeListView.setFocus();
					} else {
						openNotFoundFacilityNotifyDialog(managerName, facilityId);
					}
				} else {
					log.warn(String.format("An unexpected view was opened. view=%s", destView.getClass().getName()));
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, DialogConstants.OK_ID, Messages.getString("close"), true);
	}
	
	protected static void openNotFoundFacilityNotifyDialog(String managerName, String facilityId) {
		MessageDialog.openWarning(
				null,
				Messages.getString("warning"),
				Messages.getString("message.could.not.find.node.in.view", new Object[]{managerName, facilityId}));
	}
	
	protected static Optional<String> getFacilityIdInNodeListView(List<?> event) {
		return getElementIfString(event, 1);
	}
	
	protected static Optional<String> getElementIfString(List<?> event, int index) {
		if (index >= event.size()) {
			return Optional.empty();
		}
		Object prop = event.get(index);
		if (prop instanceof String) {
			return Optional.of((String)prop);
		}
		return Optional.empty();
	}
}
