/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.infra.composite;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.openapitools.client.model.InfraFileInfoResponse;

import com.clustercontrol.bean.PropertyDefineConstant;
import com.clustercontrol.infra.action.GetInfraFileManagerTableDefine;
import com.clustercontrol.infra.dialog.InfraFileDialog;
import com.clustercontrol.infra.util.InfraFileUtil;
import com.clustercontrol.infra.util.InfraRestClientWrapper;
import com.clustercontrol.infra.view.InfraFileManagerView;
import com.clustercontrol.util.DateTimeStringConverter;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.RestConnectManager;
import com.clustercontrol.util.UIManager;
import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.viewer.CommonTableViewer;

/**
 * 環境構築[ファイルマネージャ]ビュー用のコンポジットクラスです。
 *
 * @version 5.0.0
 * @since 5.0.0
 */
public class InfraFileManagerComposite extends Composite implements ISelectionChangedListener, IDoubleClickListener {

	// ログ
	private static Log m_log = LogFactory.getLog( InfraFileManagerComposite.class );

	/** テーブルビューアー */
	private CommonTableViewer m_viewer = null;
	/** 件数用ラベル */
	private Label m_labelCount = null;

	/**
	 * コンストラクタ
	 *
	 * @param parent 親のコンポジット
	 * @param style スタイル
	 *
	 * @see org.eclipse.swt.SWT
	 * @see org.eclipse.swt.widgets.Composite#Composite(Composite parent, int style)
	 * @see #initialize()
	 */
	public InfraFileManagerComposite(Composite parent, int style) {
		super(parent, style);
		initialize();
	}

	/**
	 * コンポジットを配置します。
	 */
	private void initialize() {
		GridLayout layout = new GridLayout(1, true);
		this.setLayout(layout);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		
		Table table = new Table(this, SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.MULTI);
		WidgetTestUtil.setTestId( this, null, table );

		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalSpan = 1;
		table.setLayoutData(gridData);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		m_labelCount = new Label(this, SWT.RIGHT);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		m_labelCount.setLayoutData(gridData);

		m_viewer = new CommonTableViewer(table);
		m_viewer.createTableColumn(GetInfraFileManagerTableDefine.get(),
				GetInfraFileManagerTableDefine.SORT_COLUMN_INDEX1,
				GetInfraFileManagerTableDefine.SORT_COLUMN_INDEX2,
				GetInfraFileManagerTableDefine.SORT_ORDER);
		// 列移動が可能に設定
		for (int i = 0; i < table.getColumnCount(); i++) {
			table.getColumn(i).setMoveable(true);
		}

		m_viewer.addSelectionChangedListener(this);
		m_viewer.addDoubleClickListener(this);

		update();
	}

	/**
	 * テーブルビューアーを更新します。<BR>
	 * 共通テーブルビューアーにセットします。
	 *
	 */
	public void update() {
		//環境構築ファイル一覧取得
		List<InfraFileInfoResponse> infoList = null;

		Map<String, List<InfraFileInfoResponse>> dispMap = new ConcurrentHashMap<String, List<InfraFileInfoResponse>>();
		Map<String, String> errorMsgs = new ConcurrentHashMap<>();
		int size = 0;
		for(String managerName : RestConnectManager.getActiveManagerSet()) {
			InfraRestClientWrapper wrapper = InfraRestClientWrapper.getWrapper(managerName);
			try {
				infoList = wrapper.getInfraFileList(null);
				dispMap.put(managerName, infoList);
				size += infoList.size();
				
			} catch (Exception e) {
				m_log.warn("update() getInfraFileList, " + e.getMessage());
				errorMsgs.put(managerName, Messages.getString(HinemosMessage.replace(e.getMessage())));
			}
		}

		//メッセージ表示
		if( 0 < errorMsgs.size() ){
			UIManager.showMessageBox(errorMsgs, true);
		}

		ArrayList<Object> listInput = new ArrayList<Object>();
		for(Map.Entry<String, List<InfraFileInfoResponse>> entry : dispMap.entrySet()) {
			String managerName = entry.getKey();
			for (InfraFileInfoResponse fileInfo : entry.getValue()) {
				ArrayList<Object> a = new ArrayList<Object>();
				a.add(managerName);
				a.add(fileInfo.getFileId());
				a.add(fileInfo.getFileName());
				a.add(fileInfo.getOwnerRoleId());
				a.add(fileInfo.getCreateUserId());
				a.add(DateTimeStringConverter.parseDateString(fileInfo.getCreateDatetime()));
				a.add(fileInfo.getModifyUserId());
				a.add(DateTimeStringConverter.parseDateString(fileInfo.getModifyDatetime()));
				a.add(null);
				listInput.add(a);
			}
		}

		m_viewer.setInput(listInput);
		m_labelCount.setText(Messages.getString("records",
				new Object[] { size }));
	}

	/**
	 * このコンポジットが利用するテーブルビューアを返します。
	 *
	 * @return テーブルビューア
	 */
	public TableViewer getTableViewer() {
		return m_viewer;
	}

	/**
	 * このコンポジットが利用するテーブルを返します。
	 *
	 * @return テーブル
	 */
	public Table getTable() {
		return m_viewer.getTable();
	}

	@Override
	public void selectionChanged(SelectionChangedEvent event) {
		IWorkbenchPage page = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getActivePage();

		StructuredSelection selection = (StructuredSelection) event.getSelection();
		IViewPart viewPart = page.findView(InfraFileManagerView.ID);
		if ( viewPart != null && selection != null ) {
			InfraFileManagerView view = (InfraFileManagerView) viewPart.getAdapter(InfraFileManagerView.class);
			if (view == null) {
				m_log.info("selection changed: view is null"); 
				return;
			}
			view.setEnabledAction(selection.size(), selection);
		}
	}

	@Override
	public void doubleClick(DoubleClickEvent event) {
		IWorkbenchPage page = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getActivePage();

		StructuredSelection selection = (StructuredSelection) event.getSelection();
		IViewPart viewPart = page.findView(InfraFileManagerView.ID);
		if (viewPart == null || selection == null) {
			return;
		}
		
		//アップロードダイアログを開く
		String managerName = InfraFileUtil.getManagerName(selection);
		InfraFileDialog dialog = new InfraFileDialog(
				viewPart.getSite().getShell(), 
				managerName, 
				PropertyDefineConstant.MODE_MODIFY, 
				InfraFileUtil.getSelectedInfraFileInfo(selection));
		dialog.open();

		// 更新
		this.update();
	}
}