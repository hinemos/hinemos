/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmap.composite;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.dialogs.IDialogConstants;
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
import org.openapitools.client.model.JobmapIconImageInfoResponse;

import com.clustercontrol.bean.PropertyDefineConstant;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.jobmap.action.GetJobMapImageListTableDefine;
import com.clustercontrol.jobmap.dialog.JobMapImageDialog;
import com.clustercontrol.jobmap.util.JobmapIconImageCacheEntry;
import com.clustercontrol.jobmap.util.JobmapImageCacheUtil;
import com.clustercontrol.jobmap.view.JobMapImageListView;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.RestConnectManager;
import com.clustercontrol.util.UIManager;
import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.viewer.CommonTableViewer;

/**
 * ジョブマップ用イメージファイル一覧コンポジットクラス<BR>
 *
 * @version 2.0.0
 * @since 2.0.0
 */
public class JobMapImageListComposite extends Composite {

	// ログ
	private static Log m_log = LogFactory.getLog( JobMapImageListComposite.class );
	/** テーブルビューア */
	private CommonTableViewer m_viewer = null;
	/** 表示内容ラベル */
	private Label m_statuslabel = null;
	/** アイコンID */
	private String m_iconId = null;

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
	 * アイコンID
	 * @return m_iconId
	 */
	public String getIconId() {
		return m_iconId;
	}

	/**
	 * アイコンID
	 * @param iconId
	 */
	public void setIconId(String iconId) {
		m_iconId = iconId;
	}
	/**
	 * コンストラクタ
	 *
	 * @param parent
	 * @param style
	 * @since 2.0.0
	 */
	public JobMapImageListComposite(Composite parent, int style) {
		super(parent, style);
		initialize();
	}

	/**
	 * 初期化処理<BR>
	 *
	 * @since 2.0.0
	 */
	private void initialize() {
		GridLayout layout = new GridLayout(1, true);
		this.setLayout(layout);
		layout.marginHeight = 0;
		layout.marginWidth = 0;

		// ジョブマップ用アイコンイメージ一覧テーブル作成
		Table table = new Table(this, SWT.H_SCROLL | SWT.V_SCROLL
				| SWT.FULL_SELECTION | SWT.MULTI);
		WidgetTestUtil.setTestId(this, null, table);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		this.m_statuslabel = new Label(this, SWT.RIGHT);
		WidgetTestUtil.setTestId(this, "m_statuslabel", m_statuslabel);
		this.m_statuslabel.setText("");
		GridData statuslabelGridData = new GridData();
		statuslabelGridData.horizontalAlignment = GridData.FILL;
		statuslabelGridData.verticalAlignment = GridData.FILL;
		this.m_statuslabel.setLayoutData(statuslabelGridData);

		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalSpan = 1;
		table.setLayoutData(gridData);

		m_viewer = new CommonTableViewer(table);
		m_viewer.createTableColumn(GetJobMapImageListTableDefine.get(),
				GetJobMapImageListTableDefine.SORT_COLUMN_INDEX1,
				GetJobMapImageListTableDefine.SORT_COLUMN_INDEX2,
				GetJobMapImageListTableDefine.SORT_ORDER);

		for (int i = 0; i < table.getColumnCount(); i++){
			table.getColumn(i).setMoveable(true);
		}
		m_viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				StructuredSelection selection = (StructuredSelection) event.getSelection();

				// アイコンIDを取得
				if (selection.getFirstElement() != null) {
					ArrayList<?> info = (ArrayList<?>) selection.getFirstElement();
					String iconId = (String) info.get(GetJobMapImageListTableDefine.ICON_ID);
					// アイコンIDを設定
					setIconId(iconId);
				}

				//アクティブページを手に入れる
				IWorkbenchPage page = PlatformUI.getWorkbench()
						.getActiveWorkbenchWindow().getActivePage();

				// ジョブマップ用アイコンイメージビューのボタン（アクション）の使用可/不可を設定する
				IViewPart viewPart = page.findView(JobMapImageListView.ID);
				if (viewPart != null) {
					JobMapImageListView view =
							(JobMapImageListView) viewPart.getAdapter(JobMapImageListView.class);
					//ボタン（アクション）の使用可/不可を設定する
					view.setEnabledAction(selection.size(), selection);
				}
			}
		});
		// ダブルクリックリスナの追加
		m_viewer.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				String managerName = null;
				String iconId = null;

				// アイコンIDを取得
				if (((StructuredSelection) event.getSelection()).getFirstElement() != null) {
					ArrayList<?> info = (ArrayList<?>) ((StructuredSelection) event
							.getSelection()).getFirstElement();
					managerName = (String) info.get(GetJobMapImageListTableDefine.MANAGER_NAME);
					iconId = (String) info.get(GetJobMapImageListTableDefine.ICON_ID);
				}

				if(iconId != null){
					// ダイアログを生成
					JobMapImageDialog dialog = null;

					dialog = new JobMapImageDialog(getShell(), managerName, iconId, PropertyDefineConstant.MODE_MODIFY);

					// ダイアログにて変更が選択された場合、入力内容をもって登録を行う。
					if (dialog.open() == IDialogConstants.OK_ID) {
						update();
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
		List<JobmapIconImageCacheEntry> list = null;

		// ジョブマップ用アイコンファイル一覧情報取得
		Map<String, List<JobmapIconImageCacheEntry>> dispDataMap= new ConcurrentHashMap<>();
		Map<String, String> errorMsgs = new ConcurrentHashMap<>();
		for(String managerName : RestConnectManager.getActiveManagerSet()) {
			try {
				list = JobmapImageCacheUtil.getJobmapIconImageList(managerName);
			} catch (InvalidRole e) {
				// 権限なし
				errorMsgs.put( managerName, Messages.getString("message.accesscontrol.16") );
			} catch (Exception e) {
				// 上記以外の例外
				m_log.warn("update(), " + HinemosMessage.replace(e.getMessage()), e);
				errorMsgs.put( managerName, Messages.getString("message.hinemos.failure.unexpected") + ", " + HinemosMessage.replace(e.getMessage()));
			}
			if(list == null){
				list = new ArrayList<>();
			}

			dispDataMap.put(managerName, list);
		}

		//メッセージ表示
		if( 0 < errorMsgs.size() ){
			UIManager.showMessageBox(errorMsgs, true);
		}

		ArrayList<Object> listInput = new ArrayList<Object>();
		for(Map.Entry<String, List<JobmapIconImageCacheEntry>> map: dispDataMap.entrySet()) {
			for (JobmapIconImageCacheEntry entry : map.getValue()) {
				JobmapIconImageInfoResponse info = entry.getJobmapIconImage();
				ArrayList<Object> obj = new ArrayList<Object>();
				obj.add(entry.getFiledata());
				obj.add(map.getKey());
				obj.add(info.getIconId());
				obj.add(info.getDescription());
				obj.add(info.getOwnerRoleId());
				obj.add(info.getCreateUser());
				obj.add(info.getCreateTime());
				obj.add(info.getUpdateUser());
				obj.add(info.getUpdateTime());
				obj.add(null);
				listInput.add(obj);
			}
		}
		m_viewer.setInput(listInput);

		// 合計欄更新
		String[] args = { String.valueOf(listInput.size()) };
		this.m_statuslabel.setText(Messages.getString("records", args));
	}
}
