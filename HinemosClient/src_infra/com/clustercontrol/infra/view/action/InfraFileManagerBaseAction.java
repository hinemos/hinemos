package com.clustercontrol.infra.view.action;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.menus.UIElement;

import com.clustercontrol.infra.action.GetInfraFileManagerTableDefine;
import com.clustercontrol.infra.view.InfraFileManagerView;

public abstract class InfraFileManagerBaseAction extends AbstractHandler
		implements IElementUpdater {


	/* ログ */
	private static Log m_log = LogFactory.getLog(InfraFileManagerBaseAction.class);

	protected IWorkbenchWindow window;

	/** ビュー */
	protected IWorkbenchPart viewPart;

	public InfraFileManagerBaseAction() {
		super();
	}

	/**
	 * Dispose
	 */
	@Override
	public void dispose() {
		this.viewPart = null;
		this.window = null;
	}

	@Override
	public void updateElement(UIElement element, @SuppressWarnings("rawtypes") Map parameters) {

		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if ( window == null ) {
			return;
		}

		// page may not start at state restoring
		IWorkbenchPage page = window.getActivePage();
		if ( page == null ) {
			return;
		}

		boolean enable = false;
		IWorkbenchPart part = page.getActivePart();

		if (part instanceof InfraFileManagerView) {
			InfraFileManagerView infraFileManagerView = null;
			try {
				infraFileManagerView = (InfraFileManagerView) part.getAdapter(InfraFileManagerView.class);
			} catch (Exception e) { 
				m_log.info("execute " + e.getMessage());
				return;
			}
			
			if (infraFileManagerView == null) {
				m_log.info("execute: view is null"); 
				return;
			}

			StructuredSelection selection = null;
			if (infraFileManagerView.getComposite().getTableViewer().getSelection() instanceof StructuredSelection) {
				selection = (StructuredSelection) infraFileManagerView.getComposite().getTableViewer().getSelection();
			}

			if (selection != null && selection.size() == 1) {
				enable = true;
			}
		}
		this.setBaseEnabled(enable);
	}

	protected InfraFileManagerView getView(ExecutionEvent event) {
		this.window = HandlerUtil.getActiveWorkbenchWindow(event);
		if( null == this.window || !isEnabled() ){
			return null;
		}

		this.viewPart = HandlerUtil.getActivePart(event);
		if(!(viewPart instanceof InfraFileManagerView)){
			return null;
		}

		InfraFileManagerView view = null;
		try {
			view = (InfraFileManagerView) viewPart.getAdapter(InfraFileManagerView.class);
		} catch (Exception e) { 
			m_log.info("execute " + e.getMessage()); 
			return null; 
		}
		return view;
	}

	protected List<String> getSelectedInfraFileIdList(InfraFileManagerView view) {
		StructuredSelection selection = null;
		if(view.getComposite().getTableViewer().getSelection() instanceof StructuredSelection){
			selection = (StructuredSelection) view.getComposite().getTableViewer().getSelection();
		}

		List<String> fileIds = new ArrayList<String>();
		if(selection != null){
			for(Object object: selection.toList()){
				String fileId = (String) ((ArrayList<?>)object).get(GetInfraFileManagerTableDefine.FILE_ID);
				fileIds.add(fileId);
			}
		}
		return fileIds;
	}
}