/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.traputil.ui.views.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.menus.UIElement;
import org.openapitools.client.model.ModifySnmptrapMonitorRequest;
import org.openapitools.client.model.MonitorInfoResponse;
import org.openapitools.client.model.TrapValueInfoRequest;
import org.openapitools.client.model.TrapValueInfoRequest.PriorityAnyVarBindEnum;

import com.clustercontrol.accesscontrol.util.ObjectBean;
import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.fault.RestConnectFailed;
import com.clustercontrol.fault.UrlNotFound;
import com.clustercontrol.monitor.util.MonitorsettingRestClientWrapper;import com.clustercontrol.monitor.view.MonitorListView;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.RestClientBeanUtil;
import com.clustercontrol.utility.traputil.bean.SnmpTrapMasterInfo;
import com.clustercontrol.utility.traputil.dialog.ImportDialog;
import com.clustercontrol.utility.util.OpenApiEnumConverter;import com.clustercontrol.utility.util.UtilityManagerUtil;
import com.clustercontrol.utility.util.UtilityRestClientWrapper;

/**
 * SNMPTrapをインポートするダイアログを開くためのクライアント側アクションクラス<BR>
 *
 * @version 6.1.0
 * @since 5.0.a
 */
public class ImportCommand extends AbstractHandler implements IElementUpdater {
	
	/** アクションID */
	public static final String ID = "com.clustercontrol.enterprise.utility.traputil.ui.views.commands.ImportCommand"; //$NON-NLS-1$
	private IWorkbenchWindow window;
	/** ビュー */
	private IWorkbenchPart viewPart;

	/**
	 * Dispose
	 */
	@Override
	public void dispose() {
		this.viewPart = null;
		this.window = null;
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		this.window = HandlerUtil.getActiveWorkbenchWindow(event);
		// In case this action has been disposed
		if( null == this.window || !isEnabled() ){
			return null;
		}

		// 選択アイテムの取得
		this.viewPart = HandlerUtil.getActivePart(event);
		MonitorListView view = this.viewPart.getAdapter(MonitorListView.class);
		
		if(view != null){
			if (view.getSelectMonitorTypeId() != null && view.getSelectedNum() == 1 && view.getSelectMonitorTypeId().equals(HinemosModuleConstant.MONITOR_SNMPTRAP)) {
				ObjectBean bean = view.getSelectedObjectBeans().get(0);
				
				try {
					UtilityRestClientWrapper wrapper = UtilityRestClientWrapper.getWrapper(bean.getManagerName());
					boolean isPublish = wrapper.checkPublish().getPublish();
					if (!isPublish) {
						MessageDialog.openWarning(
								null,
								Messages.getString("warning"),
								Messages.getString("message.expiration.term.invalid"));
					}
				} catch (InvalidRole | InvalidUserPass e) {
					MessageDialog.openInformation(null, Messages.getString("message"),
							e.getMessage());
					return null;
				} catch (HinemosUnknown e) {
					if(UrlNotFound.class.equals(e.getCause().getClass())) {
						MessageDialog.openInformation(null, Messages.getString("message"),
								Messages.getString("message.expiration.term"));
						return null;
					} else {
						MessageDialog.openInformation(null, Messages.getString("message"),
								e.getMessage());
						return null;
					}
				} catch (Exception e) {
					// キーファイルを確認できませんでした。処理を終了します。
					// Key file not found. This process will be terminated.
					MessageDialog.openInformation(null, Messages.getString("message"),
							Messages.getString("message.expiration.term"));
					return null;
				}
				
				MonitorInfoResponse info;
				try {
					info = MonitorsettingRestClientWrapper.getWrapper(bean.getManagerName()).getMonitor(bean.getObjectId());
				} catch (HinemosUnknown | InvalidRole | InvalidUserPass | MonitorNotFound | RestConnectFailed e) {
					MessageDialog.openError(null, com.clustercontrol.util.Messages.getString("failed"),
							Messages.getString("message.traputil.25") + "\n\n" + HinemosMessage.replace(e.getMessage()));
					return null;
				}
				
				try {
					ImportDialog dialog = new ImportDialog(viewPart.getSite().getShell());
					dialog.setManagerName(bean.getManagerName());
					dialog.setMonitorId(bean.getObjectId());
					if(dialog.open() == Dialog.OK && dialog.getMibList() != null && !dialog.getMibList().isEmpty()){
						List<TrapValueInfoRequest> traps = new ArrayList<>();
						TrapValueInfoRequest trapInfo = null;
						for(SnmpTrapMasterInfo mib: dialog.getMibList()){
							trapInfo = new TrapValueInfoRequest();
							trapInfo.setMib(mib.getMib());
							trapInfo.setDescription(mib.getDescr());
							trapInfo.setLogmsg(mib.getLogmsg());
							trapInfo.setTrapOid(mib.getTrapOid());
							trapInfo.setUei(mib.getUei());
							trapInfo.setGenericId(mib.getGenericId());
							PriorityAnyVarBindEnum priorityAnyVarBindEnum = OpenApiEnumConverter.integerToEnum(mib.getPriority(), PriorityAnyVarBindEnum.class);
							trapInfo.setPriorityAnyVarBind(priorityAnyVarBindEnum);
							trapInfo.setSpecificId(mib.getSpecificId());
							trapInfo.setFormatVarBinds("");
							trapInfo.setProcVarbindSpecified(false);
							trapInfo.setValidFlg(true);
							trapInfo.setVersion(TrapValueInfoRequest.VersionEnum.V1);
							traps.add(trapInfo);
						}
						ModifySnmptrapMonitorRequest modifySnmptrapMonitorRequest = new ModifySnmptrapMonitorRequest();
						RestClientBeanUtil.convertBean(info, modifySnmptrapMonitorRequest);
						modifySnmptrapMonitorRequest.getTrapCheckInfo().getMonitorTrapValueInfoEntities().addAll(traps);
						
						MonitorsettingRestClientWrapper.getWrapper(bean.getManagerName()).modifySnmptrapMonitor(info.getMonitorId(),modifySnmptrapMonitorRequest);
					}
					return true;
				} catch (HinemosUnknown | InvalidSetting | MonitorNotFound | InvalidUserPass | InvalidRole | RestConnectFailed e) {
					MessageDialog.openError(null, com.clustercontrol.util.Messages.getString("failed"),
							Messages.getString("message.traputil.5") + "\n\n" + HinemosMessage.replace(e.getMessage()));
					return null;
				}
			} else {
				MessageDialog.openError(null, com.clustercontrol.util.Messages.getString("failed"),
						Messages.getString("message.traputil.24"));
			}
		}
		
		return null;
	}

	@Override
	public void updateElement(UIElement element, @SuppressWarnings("rawtypes") Map parameters) {
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		// page may not start at state restoring
		if( null != window ){
			IWorkbenchPage page = window.getActivePage();
			if( null != page ){
				IWorkbenchPart part = page.getActivePart();

				boolean editEnable = false;
				if (part instanceof MonitorListView) {
					// Enable button when 1 item is selected
					MonitorListView view = (MonitorListView) part;
					if (view.getSelectMonitorTypeId() != null && view.getSelectedNum() == 1) {
						if (view.getSelectMonitorTypeId().equals(HinemosModuleConstant.MONITOR_SNMPTRAP)) {
							editEnable = true;
						}
					}
				}
				this.setBaseEnabled(editEnable);
			}
		}
	}
}
