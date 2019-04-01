/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.nodemap.dialog;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.service.UISession;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;

import com.clustercontrol.bean.SizeConstant;
import com.clustercontrol.dialog.CommonDialog;
import com.clustercontrol.dialog.DateTimeDialog;
import com.clustercontrol.dialog.ValidateResult;
import com.clustercontrol.repository.bean.NodeConfigSettingItem;
import com.clustercontrol.nodemap.composite.NodeListConfigFilterItemComposite;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.TimezoneUtil;
import com.clustercontrol.ws.repository.NodeConfigFilterInfo;
import com.clustercontrol.ws.repository.NodeConfigFilterItemInfo;
import com.clustercontrol.ws.repository.NodeInfo;

/**
 * ノード検索ダイアログクラス<BR>
 *
 * @version 6.2.0
 */
public class NodeListFindDialog extends CommonDialog {

	/** shell */
	private Shell m_shell;

	/** 検索条件 */
	private NodeInfo m_nodeInfo;

	/** 親Composite */
	private Composite m_parentComposite;

	/** 構成情報Group */
	private Group m_nodeConfigGroup;

	/** 構成情報Composite */
	private Composite m_nodeConfigComposite;

	/** 構成情報 AND Radio */
	private Button m_nodeConfigAndRadio;

	/** 構成情報 OR Radio */
	private Button m_nodeConfigOrRadio;

	/** 構成情報 対象日時 CheckBox */
	private Button m_targetDatetimeCheck;

	/** 構成情報 対象日時 Text */
	private Text m_targetDatetimeText;

	/** 構成情報 対象日時 */
	private Long m_targetDatetime = 0L;

	/** 各構成情報条件Compositeマップ(表示順, NodeFilterCompositeInfo) */
	private ConcurrentHashMap<Integer, NodeFilterCompositeInfo> m_compositeMap = new ConcurrentHashMap<>();

	/** 各構成情報条件のチェック対象リスト(削除チェックボックスでチェックされている表示順を保持) */
	private List<Integer> m_infoDelCheckOnList = new ArrayList<>();

	/** 検索条件キャッシュマップ */
	private static Map<UISession, HashMap<String, NodeInfo>> filterCache = new ConcurrentHashMap<>();

	private static final int SCROLL_WIDTH = 20;

	/** 各構成情報条件の最大INDEX */
	private Integer m_maxIdx = 0;

	private String m_secondaryId = "";

	/**
	 * コンストラクタ
	 * 作成時
	 * @param parent 親シェル
	 */
	public NodeListFindDialog(Shell parent, String secondaryId) {
		super(parent);
		m_secondaryId = secondaryId;
	}

	/**
	 * ダイアログエリアを生成します。
	 *
	 * @param parent 親コンポジット
	 */
	@Override
	protected void customizeDialog(Composite parent) {
		m_shell = this.getShell();

		// タイトル
		m_shell.setText(Messages.getString("dialog.nodemap.find.nodes"));

		// レイアウト
		GridLayout layout = new GridLayout(5, false);
		layout.marginWidth = 10;
		layout.marginHeight = 10;
		parent.setLayout(layout);
		this.m_parentComposite = parent;

		GridData gridData = new GridData(SWT.V_SCROLL);
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_parentComposite.setLayoutData(gridData);

		// 構成情報 (Group)
		m_nodeConfigGroup = new Group(parent, SWT.NONE);
		m_nodeConfigGroup.setText(Messages.getString("node.config"));
		gridData = new GridData();
		gridData.horizontalSpan = 5;
		m_nodeConfigGroup.setLayoutData(gridData);
		m_nodeConfigGroup.setLayout(new GridLayout(2, false));

		// 構成情報説明 (Label)
		Label label = new Label(m_nodeConfigGroup, SWT.NONE);
		label.setText(Messages.getString("message.repository.62"));
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		label.setLayoutData(gridData);

		// 判定対象の条件関係（Group）
		Group nodeConfigAndOrGroup = new Group(m_nodeConfigGroup, SWT.NONE);
		nodeConfigAndOrGroup.setText(Messages.getString("condition.between.objects"));
		nodeConfigAndOrGroup.setLayout(new GridLayout(2, false));
		gridData = new GridData();
		gridData.widthHint = 300;
		nodeConfigAndOrGroup.setLayoutData(gridData);

		// 文字列条件_AND（ラジオ）
		this.m_nodeConfigAndRadio = new Button(nodeConfigAndOrGroup, SWT.RADIO);
		this.m_nodeConfigAndRadio.setLayoutData(new GridData(120, SizeConstant.SIZE_BUTTON_HEIGHT));
		this.m_nodeConfigAndRadio.setText(Messages.getString("and"));
		this.m_nodeConfigAndRadio.setSelection(true);
		
		// 文字列条件_AND（ラジオ）
		this.m_nodeConfigOrRadio = new Button(nodeConfigAndOrGroup, SWT.RADIO);
		this.m_nodeConfigOrRadio.setLayoutData(new GridData(120, SizeConstant.SIZE_BUTTON_HEIGHT));
		this.m_nodeConfigOrRadio.setText(Messages.getString("or"));

		// 対象日時（Group）
		Group nodeTargetDatetimeGroup = new Group(m_nodeConfigGroup, SWT.NONE);
		nodeTargetDatetimeGroup.setText(Messages.getString("target.datetime"));
		nodeTargetDatetimeGroup.setLayout(new GridLayout(3, false));
		gridData = new GridData();
		gridData.widthHint = 300;
		nodeTargetDatetimeGroup.setLayoutData(gridData);

		// 対象日時（チェックボックス）
		m_targetDatetimeCheck = new Button(nodeTargetDatetimeGroup, SWT.CHECK);
		m_targetDatetimeCheck.setLayoutData(new GridData(30, SizeConstant.SIZE_BUTTON_HEIGHT));
		m_targetDatetimeCheck.setSelection(false);

		// 対象日時 (Text)
		m_targetDatetimeText = new Text(nodeTargetDatetimeGroup, SWT.BORDER);
		m_targetDatetimeText.setLayoutData(new GridData(140, SizeConstant.SIZE_TEXT_HEIGHT));
		// 日時データの場合は、日時ダイアログからの入力しか受け付けない
		m_targetDatetimeText.setEditable(false);
			
		// 対象日時 (Button)
		Button targetDatetimeButton = new Button(nodeTargetDatetimeGroup, SWT.NONE);
		targetDatetimeButton.setText(Messages.getString("calendar.button"));
		targetDatetimeButton.setLayoutData(new GridData(30, SizeConstant.SIZE_BUTTON_HEIGHT));
		targetDatetimeButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				DateTimeDialog dialog = new DateTimeDialog(m_shell);
				if (m_targetDatetimeText.getText().length() > 0) {
					Date date = new Date(m_targetDatetime);
					dialog.setDate(date);
				}
				if (dialog.open() == IDialogConstants.OK_ID) {
					//取得した日時をLong型で保持
					m_targetDatetime = dialog.getDate().getTime();
					//ダイアログより取得した日時を"yyyy/MM/dd HH:mm:ss"の形式に変換
					SimpleDateFormat sdf = TimezoneUtil.getSimpleDateFormat();
					String tmp = sdf.format(dialog.getDate());
					m_targetDatetimeText.setText(tmp);
				}
			}
		});
		
		// 構成情報（Composite）
		m_nodeConfigComposite = new Composite(m_nodeConfigGroup, SWT.NONE);
		m_nodeConfigComposite.setLayout(new GridLayout(1, false));
		gridData = new GridData();
		gridData.horizontalSpan = 5;
		gridData.widthHint = 645;
		m_nodeConfigComposite.setLayoutData(gridData);

		// Composite作成
		NodeInfo nodeInfo = getOrInitFilterCache();
		if (nodeInfo != null) {
			if (nodeInfo.getNodeConfigFilterList() != null) {
				for (NodeConfigFilterInfo filterInfo : nodeInfo.getNodeConfigFilterList()) {
					addInputComposite(filterInfo);
					m_nodeConfigComposite.layout();
					m_nodeConfigGroup.layout();
					NodeListFindDialog.this.m_parentComposite.layout();
					getScrolledComposite().setMinSize(getAreaComposite().computeSize(0, 0));
					getScrolledComposite().setMinSize(getAreaComposite().computeSize(SWT.DEFAULT, SWT.DEFAULT));
					getScrolledComposite().setMinWidth(
							getAreaComposite().computeSize(SWT.DEFAULT, SWT.DEFAULT).x - SCROLL_WIDTH);
				}
			}
			if (nodeInfo.getNodeConfigTargetDatetime() != null
					&& nodeInfo.getNodeConfigTargetDatetime() != 0L) {
				m_targetDatetimeCheck.setSelection(true);
				m_targetDatetime = nodeInfo.getNodeConfigTargetDatetime();
				//ダイアログより取得した日時を"yyyy/MM/dd HH:mm:ss"の形式に変換
				SimpleDateFormat sdf = TimezoneUtil.getSimpleDateFormat();
				String tmp = sdf.format(m_targetDatetime);
				m_targetDatetimeText.setText(tmp);
			}
		}

		// サイズ最適化
		adjustDialog();

		if (nodeInfo != null) {
			// 値設定
			setInputData(nodeInfo);
		}

//		this.update();
	}

	/**
	 * ダイアログエリアを調整します。
	 *
	 */
	public void adjustDialog(){
		// サイズを最適化
		// 画面中央に
		Display display = m_shell.getDisplay();
		m_shell.setLocation((display.getBounds().width - m_shell.getSize().x) / 2,
				(display.getBounds().height - m_shell.getSize().y) / 2);

		//ダイアログのサイズ調整（pack:resize to be its preferred size）
		m_shell.pack();
		m_shell.setSize(new Point(m_shell.getSize().x, 600));
	}

	/**
	 * 検索条件を入力欄に設定します。
	 *
	 * @param nodeInfo 検索条件
	 *
	 */
	public void setInputData(NodeInfo nodeInfo) {
		// 構成情報　判定対象の条件関係
		if (nodeInfo.isNodeConfigFilterIsAnd() != null) {
			m_nodeConfigAndRadio.setSelection(nodeInfo.isNodeConfigFilterIsAnd());
			m_nodeConfigOrRadio.setSelection(!nodeInfo.isNodeConfigFilterIsAnd());
		}
	}

	/**
	 * 入力値を設定します。
	 *
	 * @return true：正常、false：異常
	 *
	 * @see com.clustercontrol.dialog.CommonDialog#action()
	 */

	@Override
	protected boolean action() {
		boolean result = true;

		// 検索条件
		m_nodeInfo = createFilterCache();

		return result;
	}

	/**
	 * 入力値チェックをします。
	 *
	 * @return 検証結果
	 */
	@Override
	protected ValidateResult validate() {
		return super.validate();
	}

	/**
	 * 入力値を保持したデータモデルを生成します。
	 *
	 * @return データモデル
	 */
	public NodeInfo getInputData() {
		NodeInfo cloneInfo = new NodeInfo();

		// 構成情報検索条件
		cloneInfo.setNodeConfigFilterIsAnd(m_nodeInfo.isNodeConfigFilterIsAnd());

		// 構成情報対象日時
		cloneInfo.setNodeConfigTargetDatetime(m_nodeInfo.getNodeConfigTargetDatetime());

		// 構成情報検索条件
		cloneInfo.getNodeConfigFilterList().clear();
		List<NodeConfigFilterInfo> tmpNodeConfigFilterList = new ArrayList<>();
		for (NodeConfigFilterInfo originalInfo : m_nodeInfo.getNodeConfigFilterList()) {
			NodeConfigFilterInfo nodeConfigFilterInfo = new NodeConfigFilterInfo();
			nodeConfigFilterInfo.setNodeConfigSettingItemName(originalInfo.getNodeConfigSettingItemName());
			nodeConfigFilterInfo.setExists(originalInfo.isExists());
			nodeConfigFilterInfo.getItemList().clear();
			if (originalInfo.getItemList() != null) {
				List<NodeConfigFilterItemInfo> tmpItemList = new ArrayList<>();
				for (NodeConfigFilterItemInfo originalItemInfo : originalInfo.getItemList()) {
					NodeConfigFilterItemInfo nodeConfigFilterItemInfo = new NodeConfigFilterItemInfo();
					nodeConfigFilterItemInfo.setItemName(originalItemInfo.getItemName());
					nodeConfigFilterItemInfo.setMethod(originalItemInfo.getMethod());
					nodeConfigFilterItemInfo.setItemValue(originalItemInfo.getItemValue());
					tmpItemList.add(nodeConfigFilterItemInfo);
				}
				nodeConfigFilterInfo.getItemList().addAll(tmpItemList);
			}
			tmpNodeConfigFilterList.add(nodeConfigFilterInfo);
		}
		cloneInfo.getNodeConfigFilterList().addAll(tmpNodeConfigFilterList);

		return cloneInfo;
	}

	/**
	 * 既存のボタンに加え、追加ボタン、削除ボタンを追加します。<BR>
	 * 追加ボタンがクリックされた場合、 検索条件の構成情報の入力欄を追加します。
	 * 削除ボタンがクリックされた場合、検索条件の構成情報のうち、選択されたものを削除します。
	 *
	 * @param parent 親のComposite（Button Bar）
	 *
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		// 追加ボタン
		this.createButton(parent, IDialogConstants.OPEN_ID, Messages.getString("add"), false);
		this.getButton(IDialogConstants.OPEN_ID).addSelectionListener(
				new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						addInputComposite(new NodeConfigFilterInfo());
						m_nodeConfigComposite.layout();
						m_nodeConfigGroup.layout();
						NodeListFindDialog.this.m_parentComposite.layout();
						getScrolledComposite().setMinSize(getAreaComposite().computeSize(0, 0));
						getScrolledComposite().setMinSize(
								getAreaComposite().computeSize(SWT.DEFAULT, SWT.DEFAULT));
						getScrolledComposite().setMinWidth(getAreaComposite().getSize().x - SCROLL_WIDTH);
						getScrolledComposite().layout();
					}
				});

		// 削除ボタン
		this.createButton(parent, IDialogConstants.OPEN_ID, Messages.getString("delete"), false);
		this.getButton(IDialogConstants.OPEN_ID).addSelectionListener(
				new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						if (delSelectInputComposite()) {
							m_nodeConfigComposite.layout();
							m_nodeConfigGroup.layout();
							NodeListFindDialog.this.m_parentComposite.layout();
							getScrolledComposite().setMinSize(getAreaComposite().computeSize(0, 0));
							getScrolledComposite().setMinSize(getAreaComposite().computeSize(SWT.DEFAULT, SWT.DEFAULT));
							getScrolledComposite().setMinWidth(getAreaComposite().getSize().x - SCROLL_WIDTH);
							getScrolledComposite().layout(); 
						}
					}
				});

		super.createButtonsForButtonBar(parent);
	}

	/**
	 * ＯＫボタンテキスト取得
	 *
	 * @return ＯＫボタンのテキスト
	 */
	@Override
	protected String getOkButtonText() {
		return Messages.getString("ok");
	}

	/**
	 * キャンセルボタンテキスト取得
	 *
	 * @return キャンセルボタンのテキスト
	 */
	@Override
	protected String getCancelButtonText() {
		return Messages.getString("cancel");
	}

	/**
	 * 検索条件欄を追加する
	 * 
	 */
	private void addInputComposite(NodeConfigFilterInfo filterInfo) {

		Integer idx = ++m_maxIdx;

		// 構成情報条件（Composite）
		Composite composite = new Composite(m_nodeConfigComposite, SWT.BORDER);
		composite.setLayout(new GridLayout(4, false));
		GridData gridData = new GridData();
		gridData.widthHint = 630;
		composite.setLayoutData(gridData);

		// 削除チェックボックス（Button）
		Button delCheck = new Button(composite, SWT.CHECK);
		delCheck.setData(idx);
		delCheck.setLayoutData(new GridData(50, SizeConstant.SIZE_BUTTON_HEIGHT));
		delCheck.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Button check = (Button) e.getSource();
				if (check.getSelection()) {
					// チェックされた場合
					m_infoDelCheckOnList.add((Integer)check.getData());
				} else {
					// チェックをはずした場合
					m_infoDelCheckOnList.remove((Integer)check.getData());
				}
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// 処理なし
			}
		});

		// 構成情報条件 (Label)
		Label label = new Label(composite, SWT.LEFT);
		label.setText(Messages.getString("node.config.exits.condition.1"));
		GridData testLabelGridData = new GridData(130, SizeConstant.SIZE_TEXT_HEIGHT);
		label.setLayoutData(testLabelGridData);

		// 構成情報Exists/Not Exists条件（Composite）
		Composite existConditionComposite = new Composite(composite, SWT.BORDER);
		existConditionComposite.setLayout(new GridLayout(2, false));
		existConditionComposite.setLayoutData(new GridData());

		// 構成情報Exists条件ラジオ（Radio）
		Button nodeConfigExistsRadio = new Button(existConditionComposite, SWT.RADIO);
		nodeConfigExistsRadio.setLayoutData(new GridData(70, SizeConstant.SIZE_BUTTON_HEIGHT));
		nodeConfigExistsRadio.setText(Messages.getString("node.config.exits.condition.exists"));
		nodeConfigExistsRadio.setSelection(true);
		if (filterInfo.isExists() != null) {
			nodeConfigExistsRadio.setSelection(filterInfo.isExists());
		}

		// 構成情報Not Exists条件ラジオ（Radio）
		Button nodeConfigNotExistsRadio = new Button(existConditionComposite, SWT.RADIO);
		nodeConfigNotExistsRadio.setText(Messages.getString("node.config.exits.condition.notexists"));
		nodeConfigNotExistsRadio.setLayoutData(new GridData(70, SizeConstant.SIZE_BUTTON_HEIGHT));
		if (filterInfo.isExists() != null) {
			nodeConfigNotExistsRadio.setSelection(!filterInfo.isExists());
		}

		// 構成情報条件 (Label)
		label = new Label(composite, SWT.LEFT);
		label.setText(Messages.getString("node.config.exits.condition.2"));
		label.setLayoutData(new GridData(150, SizeConstant.SIZE_TEXT_HEIGHT));

		// 構成情報条件 (Label)
		label = new Label(composite, SWT.LEFT);
		label.setText(Messages.getString("search.target") + " : ");
		label.setLayoutData(new GridData(100, SizeConstant.SIZE_TEXT_HEIGHT));

		// 構成情報条件 (ComboBox)
		Combo combo = new Combo(composite, SWT.DROP_DOWN | SWT.READ_ONLY);
		gridData = new GridData(250, SizeConstant.SIZE_COMBO_HEIGHT);
		gridData.horizontalSpan = 2;
		combo.setLayoutData(gridData);
		for (NodeConfigSettingItem item : NodeConfigSettingItem.values()) {
			combo.add(item.displayName());
			combo.setData(item.displayName(), item.name());
		}
		combo.setData(idx);
		combo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// 入力欄作成
				String displayName = ((Combo)e.getSource()).getText();
				Integer idx = (Integer)((Combo)e.getSource()).getData();
				NodeListConfigFilterItemComposite itemComposite = m_compositeMap.get(idx).getItemComposite();
				NodeConfigFilterInfo filterInfo = new NodeConfigFilterInfo();
				filterInfo.setNodeConfigSettingItemName((String)((Combo)e.getSource()).getData(displayName));
				itemComposite.createItemsComposite(filterInfo);
				m_nodeConfigComposite.layout();
				m_nodeConfigGroup.layout();
				NodeListFindDialog.this.m_parentComposite.layout();
				getScrolledComposite().setMinSize(getAreaComposite().computeSize(0, 0));
				getScrolledComposite().setMinSize(getAreaComposite().computeSize(SWT.DEFAULT, SWT.DEFAULT));
				getScrolledComposite().setMinWidth(
						getAreaComposite().getSize().x - SCROLL_WIDTH);
				getScrolledComposite().layout();
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
		});

		NodeListConfigFilterItemComposite itemComposite = new NodeListConfigFilterItemComposite(composite, SWT.NONE, filterInfo);
		itemComposite.setLayoutData(new GridData(10, 0));
		
		// 既に構成情報種別が設定されている場合
		if (filterInfo.getNodeConfigSettingItemName() != null) {
			NodeConfigSettingItem nodeConfigSettingItem = NodeConfigSettingItem.nameToType(filterInfo.getNodeConfigSettingItemName());
			combo.setText(nodeConfigSettingItem.displayName());
			
			filterInfo.setNodeConfigSettingItemName(nodeConfigSettingItem.name());
			itemComposite.createItemsComposite(filterInfo);
			itemComposite.setInputData();
			m_nodeConfigComposite.layout();
			m_nodeConfigGroup.layout();
			NodeListFindDialog.this.m_parentComposite.layout();
			getScrolledComposite().setMinSize(getAreaComposite().computeSize(0, 0));
			getScrolledComposite().setMinSize(getAreaComposite().computeSize(SWT.DEFAULT, SWT.DEFAULT));
			getScrolledComposite().setMinWidth(
					getAreaComposite().computeSize(SWT.DEFAULT, SWT.DEFAULT).x - SCROLL_WIDTH);
		}

		// マップに設定
		m_compositeMap.put(idx, new NodeFilterCompositeInfo(composite, nodeConfigExistsRadio, itemComposite));
		itemComposite.layout();
	}

	/**
	 * 選択された検索条件欄を削除する
	 * 
	 */
	private boolean delSelectInputComposite() {
		boolean rtn = false;

		if (m_infoDelCheckOnList.size() == 0) {
			return rtn;
		}

		rtn = true;

		// 選択された構成情報検索条件を削除
		for (Integer idx : m_infoDelCheckOnList) {
			NodeFilterCompositeInfo compositeInfo = m_compositeMap.get(idx);
			for (Widget widget : compositeInfo.getComposite().getChildren()) {
				if (widget instanceof NodeListConfigFilterItemComposite) {
					((NodeListConfigFilterItemComposite)widget).dropItemsComposite();
					((NodeListConfigFilterItemComposite)widget).setSize(0, 0);
				}
				widget.dispose();
			}
			compositeInfo.getComposite().setSize(0, 0);
			compositeInfo.getComposite().layout();
			compositeInfo.getComposite().dispose();
			m_compositeMap.remove(idx);
		}
		m_infoDelCheckOnList.clear();
		return rtn;
	}

	/**
	 * Initialize a filter info
	 */
	private NodeInfo createFilterCache() {
		NodeInfo nodeInfo = new NodeInfo();
		// 構成情報
		if (m_compositeMap.size() > 0) {
			for (Map.Entry<Integer, NodeFilterCompositeInfo> entry : m_compositeMap.entrySet()) {
				NodeFilterCompositeInfo filterCompositeInfo = entry.getValue();
				NodeListConfigFilterItemComposite itemComposite = filterCompositeInfo.getItemComposite();
				itemComposite.createInputData();
				NodeConfigFilterInfo nodeConfigFilterInfo = itemComposite.getNodeConfigFilterInfo();
				nodeConfigFilterInfo.setExists(filterCompositeInfo.getIsExistsRadio().getSelection());
				nodeInfo.getNodeConfigFilterList().add(itemComposite.getNodeConfigFilterInfo());
			}
		}
		// 構成情報 AND/OR
		nodeInfo.setNodeConfigFilterIsAnd(m_nodeConfigAndRadio.getSelection());
		// 構成情報 対象日時
		if (m_targetDatetimeCheck.getSelection()) {
			nodeInfo.setNodeConfigTargetDatetime(m_targetDatetime);
		} else {
			nodeInfo.setNodeConfigTargetDatetime(0L);
		}
		if (!filterCache.containsKey(RWT.getUISession())) {
			filterCache.put(RWT.getUISession(), new HashMap<>());
		}
		filterCache.get(RWT.getUISession()).put(m_secondaryId, nodeInfo);
		return nodeInfo;
	}

	/**
	 * Get the cached filter info if existed,
	 * or initialize one while not.
	 */
	private NodeInfo getOrInitFilterCache() {
		NodeInfo nodeInfo = null;

		if (!filterCache.containsKey(RWT.getUISession())
				|| !filterCache.get(RWT.getUISession()).containsKey(m_secondaryId)) {
			nodeInfo = createFilterCache();
		} else {
			nodeInfo = filterCache.get(RWT.getUISession()).get(m_secondaryId);
		}
		return nodeInfo;
	}

	/**
	 * Remove the cached filter.
	 */
	public static void removeFilterCache(String secondaryId) {
		if (filterCache.containsKey(RWT.getUISession())
				&& filterCache.get(RWT.getUISession()).containsKey(secondaryId)) {
			filterCache.get(RWT.getUISession()).remove(secondaryId);
		}
	}

	/**
	 * マネージャコンボボックス更新時の処理
	 * 
	 */
	protected void updateManagerName(){
	}

	/**
	 * CompositeBean
	 * 
	 */
	public static class NodeFilterCompositeInfo {
		// Composite
		private Composite m_composite;
		// 検索条件Exists/NotExistsラジオ
		private Button m_isExistsRadio;
		// 検索条件Composite
		private NodeListConfigFilterItemComposite m_itemComposite;

		NodeFilterCompositeInfo(Composite composite, Button isExistsRadio, NodeListConfigFilterItemComposite itemComposite) {
			this.m_composite = composite;
			this.m_isExistsRadio = isExistsRadio;
			this.m_itemComposite = itemComposite;
		}

		public Composite getComposite() {
			return m_composite;
		}

		public Button getIsExistsRadio() {
			return m_isExistsRadio;
		}

		public NodeListConfigFilterItemComposite getItemComposite() {
			return m_itemComposite;
		}
	}
}
