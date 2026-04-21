/*
 * Copyright (c) 2026 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.monitor.view.action;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.menus.UIElement;
import org.openapitools.client.model.NodeInfoResponse;

import com.clustercontrol.fault.FacilityNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.RestConnectFailed;
import com.clustercontrol.monitor.dialog.NodePropertyDialog;
import com.clustercontrol.monitor.view.EventView;
import com.clustercontrol.monitor.view.StatusView;
import com.clustercontrol.repository.util.RepositoryRestClientWrapper;
import com.clustercontrol.util.Messages;

/*
 * 監視履歴[ステータス]あるいは監視履歴[イベント]で選択された項目のファシリティIDから、リポジトリパースペクティブで該当ノードを選択してフォーカスを移動する
 *
 */
public class ShowNodePropertyAction extends AbstractHandler implements IElementUpdater {
	/* ログ */
	private static Log log = LogFactory.getLog(ShowNodePropertyAction.class);

	/* アクションID */
	public static final String ID = ShowNodePropertyAction.class.getName();

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
		if (window == null) {
			log.warn("IWorkbenchWindow is null. Aborting processing.");
			return null;
		}
		
		// 監視履歴[ステータス]あるいは監視履歴[イベント]用に各ビューで選択された項目からファシリティIDを取得する関数オブジェクトを作成
		Function<List<?>, Optional<String>> getFacilityIdInView;
		
		IWorkbenchPart srcView = HandlerUtil.getActivePart(event);
		if (srcView instanceof EventView) {
			getFacilityIdInView = c->getElementIfString(c, 7);
		} else if (srcView instanceof StatusView) {
			getFacilityIdInView = c->getElementIfString(c, 5);
		} else {
			log.warn(String.format("The retrieved active view is not supported by this action. view=%s", srcView.getClass().getName()));
			return null;
		}
		
		// 選択項目が見つかったか？
		IStructuredSelection selection = (IStructuredSelection)HandlerUtil.getCurrentSelection(event);
		if (selection == null) {
			log.warn(String.format("IStructuredSelection is null. Aborting processing. view=%s", srcView.getClass().getName()));
			return null;
		}
		Object selected = selection.getFirstElement();
		if (selected == null || !(selected instanceof List<?>)) {
			log.warn(String.format("No element is selected. Aborting processing. view=%s", srcView.getClass().getName()));
			return null;
		}
		
		// マネージャ名取得
		Optional<String> m = getManagerNameInEventView((List<?>)selected);
		if (!m.isPresent()) {
			log.warn(String.format("Failed to retrieve the manager name. Aborting processing. view=%s, selected=%s", srcView.getClass().getName(), selected));
			return null;
		}
		String managerName = m.get();
			
		// ファシリティID取得
		Optional<String> f = getFacilityIdInView.apply((List<?>)selected);
		if (!f.isPresent()) {
			log.warn(String.format("Failed to retrieve the facility Id. Aborting processing. view=%s, selected=%s", srcView.getClass().getName(), selected));
			return null;
		}
		String facilityId = f.get();
		
		// 該当するノードを取得
		NodeInfoResponse node;
		try {
			RepositoryRestClientWrapper wrapper = RepositoryRestClientWrapper.getWrapper(managerName);
			node = wrapper.getNodeFull(facilityId);
		} catch(FacilityNotFound e1) {
			openNotFoundFacilityNotifyDialog(managerName, facilityId);
			return null;
		} catch(HinemosUnknown | InvalidUserPass | InvalidRole | RestConnectFailed e1) {
			log.warn(String.format("Failed to retrieve the selected node. Aborting processing. view=%s, managerName=%s, facilityId=%s", srcView.getClass().getName(), managerName, facilityId), e1);
			throw new ExecutionException(e1.getMessage(), e1);
		}
		if (node == null) {
			openNotFoundFacilityNotifyDialog(managerName, facilityId);
			return null;
		}
		
		// ノードの属性表示ダイアログを表示
		NodePropertyDialog dialog = new NodePropertyDialog(
				HandlerUtil.getActiveWorkbenchWindow(event).getShell(), managerName, facilityId);
		try {
			dialog.open();
		} catch (Exception e) {
			log.warn(e.getMessage(), e); 
			throw new ExecutionException(e.getMessage(), e);
		}
		return null;
	}
	
	protected static Optional<String> getManagerNameInEventView(List<?> event) {
		return getElementIfString(event, 0);
	}
	
	protected static void openNotFoundFacilityNotifyDialog(String managerName, String facilityId) {
		MessageDialog.openWarning(
				null,
				Messages.getString("warning"),
				Messages.getString("message.no.node.found.for.facility.id.in.manager", new Object[]{managerName, facilityId}));
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
	
	/**
	 * Dispose
	 */
	@Override
	public void dispose() {
	}

	@Override
	public void updateElement(UIElement element, @SuppressWarnings("rawtypes") Map parameters) {
		// 監視履歴[ステータス]あるいは監視履歴[イベント]で項目が選択されていない場合、無効表示にする
		IWorkbenchPartSite site = (IWorkbenchPartSite)element.getServiceLocator().getService(IWorkbenchPartSite.class);
		if (!(EventView.class.isInstance(site.getPart()) || StatusView.class.isInstance(site.getPart()))) {
			setBaseEnabled(false);
			return;
		}
			
		ISelectionProvider provider = site.getSelectionProvider();
		if (provider == null) {
			setBaseEnabled(false);
			return;
		}
			
		ISelection selection = provider.getSelection();
		setBaseEnabled(!(selection == null || selection.isEmpty()));
	}
}
