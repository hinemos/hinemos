/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.hub.composite;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
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
import org.openapitools.client.model.LogFormatKeyResponse;
import org.openapitools.client.model.LogFormatResponse;

import com.clustercontrol.bean.PropertyDefineConstant;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.hub.action.GetLogFormatTableDefine;
import com.clustercontrol.hub.dialog.LogFormatDialog;
import com.clustercontrol.hub.util.HubRestClientWrapper;
import com.clustercontrol.hub.view.LogFormatView;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.RestConnectManager;
import com.clustercontrol.util.TimezoneUtil;
import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.viewer.CommonTableViewer;

/**
 * ログフォーマットコンポジットクラス<BR>
 *
 */
public class LogFormatComposite extends Composite {
	/** ログ */
	private static Log m_log = LogFactory.getLog(LogFormatComposite.class);
	/** テーブルビューア */
	private CommonTableViewer m_viewer = null;
	/** テーブル */
	private Table logFormatListTable = null;
	/** ラベル */
	private Label m_labelCount = null;
	/** カレンダID */
	private String m_formatId = null;

	/**
	 * このコンポジットが利用するテーブルビューアを取得します。<BR>
	 *
	 * @return テーブルビューア
	 */
	public TableViewer getTableViewer() {
		return m_viewer;
	}
	/**
	 * このコンポジットが利用するテーブルを取得します。<BR>
	 *
	 * @return テーブル
	 */
	public Table getTable() {
		return m_viewer.getTable();
	}
	/**
	 * フォーマットID
	 * @return m_formatId
	 */
	public String getFormatId() {
		return m_formatId;
	}

	/**
	 * フォーマットID
	 * @param formatId
	 */
	public void setFormatId(String formatId) {
		m_formatId = formatId;
	}
	/**
	 * コンストラクタ
	 *
	 * @param parent
	 * @param style
	 */
	public LogFormatComposite(Composite parent, int style) {
		super(parent, style);
		initialize();
	}

	/**
	 * 初期化処理<BR>
	 *
	 */
	private void initialize() {
		GridLayout layout = new GridLayout(1, true);
		this.setLayout(layout);
		layout.marginHeight = 0;
		layout.marginWidth = 0;

		//ログフォーマット一覧テーブル作成
		logFormatListTable = new Table(this, SWT.H_SCROLL | SWT.V_SCROLL
				| SWT.FULL_SELECTION | SWT.MULTI);
		WidgetTestUtil.setTestId(this, null, logFormatListTable);
		logFormatListTable.setHeaderVisible(true);
		logFormatListTable.setLinesVisible(true);

		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalSpan = 1;
		logFormatListTable.setLayoutData(gridData);

		m_labelCount = new Label(this, SWT.RIGHT);
		WidgetTestUtil.setTestId(this, "count", m_labelCount);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		m_labelCount.setLayoutData(gridData);

		m_viewer = new CommonTableViewer(logFormatListTable);
				
		m_viewer.createTableColumn(GetLogFormatTableDefine.get(),
				GetLogFormatTableDefine.SORT_COLUMN_INDEX1,
				GetLogFormatTableDefine.SORT_COLUMN_INDEX2,
				GetLogFormatTableDefine.SORT_ORDER);

		for (int i = 0; i < logFormatListTable.getColumnCount(); i++){
			logFormatListTable.getColumn(i).setMoveable(true);
		}
		m_viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			/**
			 * 選択変更時に呼び出されます。<BR>
			 * ログ管理の各ビューのテーブルビューアを選択した際に、<BR>
			 * 選択した行の内容でビューのアクションの有効・無効を設定します。
			 * 
			 * @param event 選択変更イベント
			 * 
			 * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
			 */
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				
				String id = null;
				
				//監視[ログフォーマット]ビューのインスタンスを取得
				IWorkbenchPage page = PlatformUI.getWorkbench()
						.getActiveWorkbenchWindow().getActivePage();
				
				IViewPart viewPart = page.findView(LogFormatView.ID);

				//選択アイテムを取得
				StructuredSelection selection = (StructuredSelection) event.getSelection();

				if ( viewPart != null && selection != null) {
					if ( selection.getFirstElement() != null) {
						ArrayList<?> info = (ArrayList<?>) selection.getFirstElement();
						id = (String) info.get(GetLogFormatTableDefine.FORMAT_ID);
						//収集IDを設定
						LogFormatComposite.this.setFormatId(id);
					}
					LogFormatView view = (LogFormatView) viewPart.getAdapter(LogFormatView.class);
					if (view == null) {
						m_log.info("selection changed: view is null"); 
						return;
					}
					//ビューのボタン（アクション）の使用可/不可を設定する
					view.setEnabledAction(selection.size(), event.getSelection());
				}
			}
		});
		// ダブルクリックリスナの追加
		m_viewer.addDoubleClickListener(new IDoubleClickListener() {
			/**
			 * 
			 * @param event
			 */
			@Override
			public void doubleClick(DoubleClickEvent event) {
				String managerName = "";
				String formatId = "";

				if (((StructuredSelection) event.getSelection()).getFirstElement() != null) {
					ArrayList<?> info = (ArrayList<?>) ((StructuredSelection) event.getSelection()).getFirstElement();

					managerName = (String)info.get(GetLogFormatTableDefine.MANAGER_NAME);
					formatId = (String) info.get(GetLogFormatTableDefine.FORMAT_ID);
				}

				if(formatId != null){
					// ダイアログ名を取得
					LogFormatDialog dialog = new LogFormatDialog(
							LogFormatComposite.this.getShell(),managerName, formatId, PropertyDefineConstant.MODE_MODIFY);
					
					// ダイアログにて変更が選択された場合、入力内容をもって登録を行う。
					if (dialog.open() == IDialogConstants.OK_ID) {
						LogFormatComposite.this.update();
					}
				}
			}
		});
	}

	/**
	 * 更新処理<BR>
	 *
	 */
	@Override
	public void update() {
		List<LogFormatResponse> list = null;

		//ログフォーマット一覧情報取得
		Map<String, List<LogFormatResponse>> dispDataMap= new ConcurrentHashMap<String, List<LogFormatResponse>>();
		for(String managerName : RestConnectManager.getActiveManagerSet()) {
			HubRestClientWrapper wrapper = HubRestClientWrapper.getWrapper(managerName);
			try {
				list = wrapper.getLogFormatListByOwnerRole(null);
			} catch (InvalidRole e) {
				MessageDialog.openInformation(null, 
						Messages.getString("message"), 
						Messages.getString("message.accesscontrol.16"));
			} catch (Exception e) {
				// 上記以外の例外
				Logger.getLogger(this.getClass()).warn("update(), " + e.getMessage(), e);
				MessageDialog.openError(
						null, 
						Messages.getString("error"), 
						Messages.getString("message.hinemos.failure.unexpected") + ", " + e.getMessage());
			}
			//一覧が空の場合
			if (list == null) {
				list = Collections.emptyList();
			}
			dispDataMap.put(managerName, list);
		}

		List<Object> listInput = new ArrayList<Object>();
		for(Map.Entry<String, List<LogFormatResponse>> map: dispDataMap.entrySet()) {
			for (LogFormatResponse format : map.getValue()) {
				ArrayList<Object> obj = new ArrayList<Object>();
				obj.add(map.getKey());
				obj.add(format.getLogFormatId());
				obj.add(format.getDescription());
				
				// findbugs対応 文字列の連結方式をStringBuilderを利用する方法に変更
				StringBuilder formatKeys = new StringBuilder("");
				for (LogFormatKeyResponse key : format.getKeyPatternList()) {
					formatKeys.append( key.getKey() + ", ");
				}
				//末尾の", "を削除
				if (formatKeys.toString().endsWith(", ")) {
					formatKeys.setLength(formatKeys.length() - 1);
				}
				obj.add(formatKeys.toString());
				obj.add(format.getOwnerRoleId());
				obj.add(format.getRegUser());
				try {
					obj.add(TimezoneUtil.getSimpleDateFormat().parse(format.getRegDate()));
				} catch (ParseException e) {
					// ここには入らない想定
					m_log.warn("invalid regDate.", e);
				}
				obj.add(format.getUpdateUser());
				try {
					obj.add(TimezoneUtil.getSimpleDateFormat().parse(format.getUpdateDate()));
				} catch (ParseException e) {
					// ここには入らない想定
					m_log.warn("invalid updateDate.", e);
				}
				obj.add(null);				

				listInput.add(obj);
			}
		}

		m_viewer.setInput(listInput);

		Object[] args = { Integer.valueOf(listInput.size()) };
		m_labelCount.setText(Messages.getString("records", args));
	}
}