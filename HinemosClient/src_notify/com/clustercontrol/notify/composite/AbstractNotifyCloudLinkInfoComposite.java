/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.notify.composite;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.openapitools.client.model.CloudNotifyLinkInfoKeyValueObjectResponse;

import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.notify.dialog.NotifyCloudLinkDataValueDialog;
import com.clustercontrol.util.Messages;

/**
 * 連携情報のアブストラクトコンポジット<BR>
 * AWS,Azure共通の処理、情報を定義<BR>
 */
public abstract class AbstractNotifyCloudLinkInfoComposite extends Composite {

	/** 通知一覧 コンポジット。 */
	protected NotifyCloudLinkDataInfoComposite notifyCloudLinkDataInfoComposite = null;

	/** 追加 ボタン。 */
	protected Button buttonAdd = null;

	/** 変更 ボタン。 */
	protected Button buttonModify = null;

	/** 削除 ボタン。 */
	protected Button buttonDelete = null;

	// 連携情報
	protected int priority;
	protected String eventBus = "";
	protected String accessKey = "";
	protected String dataVersion = "";
	protected String detailType = "";
	protected String source = "";
	protected Map<String, String> linkInfoDataMap = new ConcurrentHashMap<String, String>();

	protected boolean isUpdated = false;
	protected Text m_detailType;
	protected Text m_source;
	protected Text m_endpoint;
	protected Text m_accessKey;
	protected Text m_subject;
	protected Text m_eventType;
	protected Text m_dataVersion;
	protected Text m_eventBus;
	//GCP Specific fields
	protected Text m_projectId;
	protected Text m_topicId;
	protected Text m_message;
	protected Text m_orderingKey;
	protected Button m_useOrdering = null;

	/** シェル */
	protected Shell m_shell = null;
	protected Composite m_composite = null;
	/**
	 * ダイアログのインスタンスを返します。
	 *
	 * @param parent
	 *            親のシェルオブジェクト
	 */
	public AbstractNotifyCloudLinkInfoComposite(Composite parent, int style) {
		super(parent, style);
		m_shell = parent.getShell();
		m_composite = parent;
		initialize();
	}

	public int getPriority() {
		return priority;
	}

	public String getAccessKey() {
		return accessKey;
	}

	public String getEventBus() {
		return eventBus;
	}

	public String getDataVersion() {
		return dataVersion;
	}

	public String getDetailType() {
		return detailType;
	}

	public List<CloudNotifyLinkInfoKeyValueObjectResponse> getDataList() {
		// Mapデータをリストに変換
		List<CloudNotifyLinkInfoKeyValueObjectResponse> list = new ArrayList<CloudNotifyLinkInfoKeyValueObjectResponse>();
		
		// データが無ければからのリストを返却
		if (linkInfoDataMap != null) {
			for (Entry<String, String> e : linkInfoDataMap.entrySet()) {
				CloudNotifyLinkInfoKeyValueObjectResponse r = new CloudNotifyLinkInfoKeyValueObjectResponse();
				r.setName(e.getKey());
				r.setValue(e.getValue());
				list.add(r);
			}
		}

		return list;
	}

	public String getSource() {
		return source;
	}

	// 連携情報をセット
	public void setLinkInfoData(int priority, String eventBus, String accessKey, String dataVersion, String detailType,
			List<CloudNotifyLinkInfoKeyValueObjectResponse> dataList, String source) throws HinemosUnknown {
		this.priority = priority;
		this.eventBus = eventBus;
		this.accessKey = accessKey;
		this.dataVersion = dataVersion;
		this.detailType = detailType;
		// この先の処理ではマップが便利なので変換
		for (CloudNotifyLinkInfoKeyValueObjectResponse r : dataList) {
			this.linkInfoDataMap.put(r.getName(), r.getValue());
		}
		this.source = source;

		isUpdated = true;
	}

	/**
	 * ダイアログエリアを生成します。
	 *
	 * @param parent
	 *            親のコンポジット
	 *
	 * @see com.clustercontrol.notify.composite.NotifyListComposite
	 */
	protected void initialize() {
		Shell shell = m_shell;

		// タイトル
		shell.setText(Messages.getString("dialog.notify.cloud.link.info"));

		// レイアウト
		GridLayout layout = null;
		
		/*
		 * クラウド通知
		 */
		Composite eventGroup = null;

		eventGroup = createComposite(m_composite);

		// テーブル
		Label label = new Label(eventGroup, SWT.NONE);
		GridData gridData = new GridData();
		gridData.horizontalSpan = 3;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(getTableMessage());
		
		this.notifyCloudLinkDataInfoComposite = new NotifyCloudLinkDataInfoComposite(eventGroup, SWT.BORDER);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalSpan = 5;
		gridData.heightHint = SWT.MIN;
		this.notifyCloudLinkDataInfoComposite.setLayoutData(gridData);
		this.notifyCloudLinkDataInfoComposite.update();

		// 操作ボタン
		Composite composite = new Composite(eventGroup, SWT.NONE);
		layout = new GridLayout(1, true);
		layout.numColumns = 1;
		composite.setLayout(layout);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalSpan = 1;
		composite.setLayoutData(gridData);

		// 追加ボタン
		this.buttonAdd = this.createButton(composite, Messages.getString("add"));
		this.buttonAdd.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				NotifyCloudLinkDataValueDialog dialog = new NotifyCloudLinkDataValueDialog(getShell());
				if (dialog.open() == IDialogConstants.OK_ID) {
					ArrayList<Object> tmplist = dialog.getInputData();

					if (linkInfoDataMap.containsKey(tmplist.get(0))) {
						MessageDialog.openWarning(null, Messages.getString("warning"),
								Messages.getString("message.notify.cloud.1"));
					} else {
						linkInfoDataMap.put((String) tmplist.get(0), (String) tmplist.get(1));
					}

					notifyCloudLinkDataInfoComposite.setInfoLinkDataList(linkInfoDataMap);
					notifyCloudLinkDataInfoComposite.update();

				}
			}
		});

		// 変更ボタン
		new Label(composite, SWT.NONE);
		this.buttonModify = this.createButton(composite, Messages.getString("modify"));
		this.buttonModify.addSelectionListener(new SelectionAdapter() {
			@SuppressWarnings("unchecked")
			@Override
			public void widgetSelected(SelectionEvent e) {
				NotifyCloudLinkDataValueDialog dialog = new NotifyCloudLinkDataValueDialog(getShell());
				// 選択済みアイテムを取得
				List<?> tmpList = notifyCloudLinkDataInfoComposite.getSelectedItem();
				ArrayList<Object> objList = null;
				if (tmpList != null && !tmpList.isEmpty()) {
					// 複数選択されていても最初の一つのみ有効
					objList = (ArrayList<Object>) tmpList.get(0);
				}

				if (objList != null && !objList.isEmpty()) {
					dialog.setInputData(objList);
					if (dialog.open() == IDialogConstants.OK_ID) {
						ArrayList<?> info = dialog.getInputData();

						if (linkInfoDataMap.replace((String) info.get(0), (String) info.get(1)) == null) {
							linkInfoDataMap.remove(objList.get(0));
							linkInfoDataMap.put((String) info.get(0), (String) info.get(1));
						}
						notifyCloudLinkDataInfoComposite.setInfoLinkDataList(linkInfoDataMap);
						notifyCloudLinkDataInfoComposite.update();
					}
				} else {
					MessageDialog.openWarning(null, Messages.getString("warning"),
							Messages.getString("message.notify.cloud.2"));
				}
			}
		});

		// 削除ボタン
		new Label(composite, SWT.NONE);
		this.buttonDelete = this.createButton(composite, Messages.getString("delete"));
		this.buttonDelete.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// 選択済みアイテムを取得
				List<?> objList = notifyCloudLinkDataInfoComposite.getSelectedItem();

				if (objList == null || objList.isEmpty()) {
					MessageDialog.openWarning(null, Messages.getString("warning"),
							Messages.getString("message.notify.cloud.2"));
					return;
				}

				for (Object o : objList) {
					ArrayList<?> tmp = (ArrayList<?>) o;

					if (tmp.isEmpty()) {
						continue;
					} else {
						linkInfoDataMap.remove((String) tmp.get(0));

					}
				}
				notifyCloudLinkDataInfoComposite.setInfoLinkDataList(linkInfoDataMap);
				notifyCloudLinkDataInfoComposite.update();

			}

		});

		// テーブルにダブルクリックリスナの追加
		notifyCloudLinkDataInfoComposite.getTableViewer().addDoubleClickListener(new IDoubleClickListener() {
			@SuppressWarnings("unchecked")
			@Override
			public void doubleClick(DoubleClickEvent event) {
				NotifyCloudLinkDataValueDialog dialog = new NotifyCloudLinkDataValueDialog(getShell());
				//選択済みアイテムを取得
				List<?> tmpList = notifyCloudLinkDataInfoComposite.getSelectedItem();
				ArrayList<Object>objList=null;
				if(tmpList!=null && !tmpList.isEmpty()){
					//複数選択されていても最初の一つのみ有効
					objList  =  (ArrayList<Object>) tmpList.get(0);
				}
				
				if (objList != null && !objList.isEmpty()) {
					dialog.setInputData(objList);
					if (dialog.open() == IDialogConstants.OK_ID) {
						ArrayList<?> info = dialog.getInputData();

						if (linkInfoDataMap.replace((String) info.get(0), (String) info.get(1)) == null) {
							linkInfoDataMap.remove(objList.get(0));
							linkInfoDataMap.put((String) info.get(0), (String) info.get(1));
						}
						notifyCloudLinkDataInfoComposite.setInfoLinkDataList(linkInfoDataMap);
						notifyCloudLinkDataInfoComposite.update();
					}
				} else {
					MessageDialog.openWarning(null, Messages.getString("warning"),
							Messages.getString("message.notify.cloud.2"));
				}
			}
		});
		
		// 空行
		label = new Label(m_composite, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = 15;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);

		// ラインを引く
		Label line = new Label(m_composite, SWT.SEPARATOR | SWT.HORIZONTAL);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalSpan = 8;
		line.setLayoutData(gridData);

		if (isUpdated) {
			reflectInputData();
		}

		update();
	}

	/**
	 * ボタンを返します。
	 *
	 * @param parent
	 *            親のコンポジット
	 * @param label
	 *            ラベル文字列
	 * @return 生成されたボタン
	 */
	private Button createButton(Composite parent, String label) {
		Button button = new Button(parent, SWT.NONE);

		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		button.setLayoutData(gridData);

		button.setText(label);

		return button;
	}

	/*
	 * 以下はAWS、Azure個別の処理になるので各クラスで実装
	 */
	public abstract void reflectInputData();

	public abstract void setInputData();

	public abstract void update();

	public abstract boolean isValidate();

	public abstract Composite createComposite(Composite parent);
	
	public abstract String getTableMessage();

}
