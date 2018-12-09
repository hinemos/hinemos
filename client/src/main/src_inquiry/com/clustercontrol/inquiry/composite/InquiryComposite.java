/*
* Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
*
* Hinemos (http://www.hinemos.info/)
*
* See the LICENSE file for licensing information.
*/

package com.clustercontrol.inquiry.composite;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;

import com.clustercontrol.inquiry.action.GetInquiryTableDefine;
import com.clustercontrol.inquiry.util.InquiryEndpointWrapper;
import com.clustercontrol.inquiry.util.InquiryUtil;
import com.clustercontrol.inquiry.view.InquiryView;
import com.clustercontrol.util.EndpointManager;
import com.clustercontrol.util.TimezoneUtil;
import com.clustercontrol.util.UIManager;
import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.viewer.CommonTableViewer;
import com.clustercontrol.ws.inquiry.InquiryTarget;

/**
 * 遠隔管理コンポジットクラスです。
 *
 * @version 6.1.0
 * @since 6.1.0
 */
public class InquiryComposite extends Composite {

	// ログ
	private static Log m_log = LogFactory.getLog( InquiryComposite.class );

	// ----- instance フィールド ----- //

	/** テーブルビューア */
	private CommonTableViewer tableViewer = null;

	// ----- コンストラクタ ----- //

	/**
	 * インスタンスを返します。
	 *
	 * @param parent 親のコンポジット
	 * @param style スタイル
	 */
	public InquiryComposite(Composite parent, int style) {
		super(parent, style);

		this.initialize();
	}

	// ----- instance メソッド ----- //

	/**
	 * コンポジットを生成・構築します。
	 */
	private void initialize() {
		GridLayout layout = new GridLayout(1, true);
		this.setLayout(layout);
		layout.marginHeight = 0;
		layout.marginWidth = 0;

		Table table = new Table(this, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL
				| SWT.FULL_SELECTION);
		WidgetTestUtil.setTestId(this, null, table);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		table.setLayoutData(gridData);

		// テーブルビューアの作成
		this.tableViewer = new CommonTableViewer(table);
		this.tableViewer.createTableColumn(GetInquiryTableDefine.get(),
				GetInquiryTableDefine.SORT_COLUMN_INDEX1,
				GetInquiryTableDefine.SORT_COLUMN_INDEX2,
				GetInquiryTableDefine.SORT_ORDER);

		for (int i = 0; i < table.getColumnCount(); i++){
			table.getColumn(i).setMoveable(true);
		}

		this.getTableViewer().addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				//メンテナンス[共通設定情報]ビューのインスタンスを取得
				IWorkbenchPage page = PlatformUI.getWorkbench()
						.getActiveWorkbenchWindow().getActivePage();
				IViewPart viewPart = page.findView(InquiryView.ID);
				//選択アイテムを取得
				StructuredSelection selection = (StructuredSelection) event.getSelection();

				if ( viewPart != null) {
					InquiryView view = (InquiryView) viewPart.getAdapter(InquiryView.class);
					if (view == null) {
						m_log.info("selection changed: view is null");
						return;
					}
					if (selection != null) {
						//ビューのボタン（アクション）の使用可/不可を設定する
						view.setEnabledAction(selection.size(), event.getSelection());
					}
				}
			}
		});
	}

	/**
	 * tableViewerを返します。
	 *
	 * @return tableViewer
	 */
	public CommonTableViewer getTableViewer() {
		return this.tableViewer;
	}

	/**
	 * このコンポジットが利用するテーブルを返します。
	 *
	 * @return テーブル
	 */
	public Table getTable() {
		return this.tableViewer.getTable();
	}

	/**
	 * コンポジットを更新します。
	 * <p>
	 *
	 */
	@Override
	public void update() {
		Map<String, String> errorMsgs = new HashMap<>();
		List<Object> listInput = new ArrayList<>();
		//設定情報取得
		for(String managerName : EndpointManager.getActiveManagerSet()) {
			List<InquiryTarget> infraManagementList;
			InquiryEndpointWrapper wrapper = InquiryEndpointWrapper.getWrapper(managerName);
			try {
				infraManagementList = wrapper.getInquiryTargetList();
			} catch (Exception e) {
				errorMsgs.put(managerName, e.getMessage());
				continue;
			}

			for (InquiryTarget target : infraManagementList) {
				List<Object> list = new ArrayList<>();
				list.add(managerName);
				list.add(target.getId());
				list.add(target.getDisplayName());
				Calendar latestCreateCal = Calendar.getInstance(TimezoneUtil.getTimeZone());
				if (target.getStarTime() != null) {
					latestCreateCal.setTimeInMillis(target.getStarTime());
					list.add(latestCreateCal.getTime());
				} else {
					list.add(null);
				}
				Calendar latestFinishCal = Calendar.getInstance(TimezoneUtil.getTimeZone());
				if (target.getEndTime() != null) {
					latestFinishCal.setTimeInMillis(target.getEndTime());
					list.add(latestFinishCal.getTime());
				} else {
					list.add(null);
				}
				list.add(InquiryUtil.convertStatus(target.getStatus()));
				list.add(null);
				listInput.add(list);
			}
		}

		//メッセージ表示
		if( 0 < errorMsgs.size() ){
			UIManager.showMessageBox(errorMsgs, true);
		}

		// テーブル更新
		this.tableViewer.setInput(listInput);
	}
}