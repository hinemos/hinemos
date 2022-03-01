/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.filtersetting.composite;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabFolder2Adapter;
import org.eclipse.swt.custom.CTabFolderEvent;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;

import com.clustercontrol.ClusterControlPlugin;
import com.clustercontrol.bean.Property;
import com.clustercontrol.filtersetting.bean.FilterSettingConstant;
import com.clustercontrol.util.Messages;
import com.clustercontrol.viewer.PropertySheet;

/**
 * タブで複数フィルタ条件の入力が行えるコンポジットです。
 * <p>
 * フィルタの種類に依存する実装に関しては、親のコンポジット({@link FilterComposite}のサブクラス)に集中させるため、
 * このクラスはフィルタの種類ごとにサブクラス化せず、{@link Diversity}を通じて親が注入します。
 */
public class FilterConditionsComposite extends Composite {

	/** {@link TabControlRef} を {@link TabItem#setData(String, Object)} でタブへ紐づける際のキー */
	private static final String KEY_CTRL_REF = FilterConditionsComposite.class.getName() + "#CTRL_REF";

	/** 親から注入された処理 */
	private Diversity diversity;

	/** true: タブの総入れ替え中を示す。このときイベントハンドリングを抑制する。 */
	private boolean replacingTabs;

	private CTabFolder tabFolder;

	/** 「追加」タブ。null は追加タブが存在していないことを示す。 */
	private CTabItem tabAppender;

	/**
	 * {@link FilterConditionsComposite}をnewするクラスで実装する必要のある処理です。
	 */
	public interface Diversity {
		/**
		 * フィルタ条件タブを新規追加する際の条件入力値を生成して返します。
		 */
		public FilterCondition createNewConditionProperty();

		/**
		 * 補足説明テキストを返します。
		 */
		public String getNotesMessage();
	}

	/**
	 * 条件設定をまとめてやり取りするためのDTOです。
	 */
	public static class FilterCondition {
		/** 説明文 */
		public String description;
		/** 条件に一致するものを表示しない (true:On, false:Off) */
		public boolean negative;
		/** プロパティシートで入力する各種条件 */
		public Property property;

		public FilterCondition(String description, boolean negative, Property property) {
			this.description = description;
			this.negative = negative;
			this.property = property;
		}
	}

	/**
	 * タブに配置したコントロールへのアクセスを簡単にするため、参照を保持します。
	 */
	private static class TabControlRef {
		Text txtDescription;
		Button chkNegative;
		Property property;
	}

	public FilterConditionsComposite(Composite parent, Diversity diversity) {
		super(parent, SWT.NONE);
		this.diversity = diversity;
		replacingTabs = false;

		GridLayout layout = new GridLayout(1, false);
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		setLayout(layout);

		// タブ
		tabFolder = new CTabFolder(this, SWT.BORDER);
		tabFolder.setUnselectedCloseVisible(false);
		tabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		GridLayout loTabFolder = new GridLayout(1, false);
		loTabFolder.marginWidth = 0;
		loTabFolder.marginHeight = 0;
		tabFolder.setLayout(loTabFolder);

		tabFolder.addCTabFolder2Listener(new CTabFolder2Adapter() {
			@Override
			// タブが「閉じる前」に呼ばれる
			public void close(CTabFolderEvent event) {
				if (replacingTabs) return;  // 入れ替え処理中は何もしない

				CTabItem[] tabs = tabFolder.getItems();
				// 残るのが1タブになる場合は閉じるボタンを無効にする
				if (tabs.length == 3) {  // 3 = 残るタブ + クローズ予定タブ + appenderタブ
					// 左端にあるクローズ予定ではないタブを探す
					for (CTabItem tab : tabs) {
						if (event.item != tab) {
							tab.setShowClose(false);
						}
					}
				}
				// タブのテキストを振りなおす
				int num = 1;
				for (CTabItem tab : tabs) {
					if (tab != event.item && tab != tabAppender) {
						tab.setText(formatTabText(num++));
					}
				}
				// appenderタブが削除されているなら復活させる
				if (tabAppender == null) {
					createAppenderTab(tabFolder);
				}
			}
		});

		tabFolder.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				if (replacingTabs) return;  // 入れ替え処理中は何もしない

				// 「タブ追加」が選ばれた場合はタブ追加
				if (event.item == tabAppender) {
					CTabItem tabItem = appendNewTab(tabFolder, diversity.createNewConditionProperty());
					// 追加したタブを選択
					tabFolder.setSelection(tabItem);
					// 最大数に到達したら追加操作用タブを削除
					if (tabFolder.getItemCount() == FilterSettingConstant.CONDITION_COUNT_MAX + 1) {
						tabAppender.dispose();
						tabAppender = null;
					}
					// タブ追加により条件タブの数は必ず2以上になる。
					// 追加前に条件タブの数が1つだった場合、閉じるボタンが無効になっているので有効化する。
					tabFolder.getItem(0).setShowClose(true);
				}
			}
		});
	}

	/**
	 * 新しいフィルタ条件を設定します。
	 */
	public void setConditions(List<FilterCondition> conditions) {
		// メソッド終わりまでイベントハンドラでのタブ操作を抑制する
		replacingTabs = true;

		// 全タブを削除
		for (CTabItem tab : tabFolder.getItems()) {
			tab.dispose();
		}
		tabAppender = null;

		// 引数でタブを再作成
		for (FilterCondition fc : conditions) {
			appendNewTab(tabFolder, fc);
		}

		// 条件タブが1つだけなら、閉じるボタンを無効化
		if (conditions.size() == 1) {
			tabFolder.getItem(0).setShowClose(false);
		}

		// 条件タブ追加の余地があるなら、追加操作用タブを作成
		if (conditions.size() < FilterSettingConstant.CONDITION_COUNT_MAX) {
			createAppenderTab(tabFolder);
		}

		// 最初のタブをアクティブにする
		tabFolder.setSelection(0);

		replacingTabs = false;
	}

	/**
	 * 現在のフィルタ条件を返します。
	 */
	public List<FilterCondition> getConditions() {
		List<FilterCondition> conditions = new ArrayList<>();
		for (CTabItem tab : tabFolder.getItems()) {
			if (tab == tabAppender) continue;
			TabControlRef ref = (TabControlRef) tab.getData(KEY_CTRL_REF);
			conditions.add(new FilterCondition(
					ref.txtDescription.getText(),
					ref.chkNegative.getSelection(),
					ref.property));
		}
		return conditions;
	}

	/**
	 * タブに表示する文字列を返します。
	 */
	private String formatTabText(int position) {
		return " " + Messages.getString("monitor.rule") + String.format("%2d", position);
	}

	/**
	 * 「追加」タブを作成します。
	 */
	private void createAppenderTab(CTabFolder tabFolder) {
		if (tabAppender != null) return;

		CTabItem tabItem = new CTabItem(tabFolder, SWT.NONE);
		tabItem.setImage(ClusterControlPlugin.getDefault().getImageRegistry().get(ClusterControlPlugin.IMG_EXPAND));
		tabItem.setText(Messages.getString("add"));
		tabAppender = tabItem;
	}

	/**
	 * 新しい条件入力タブを作成します。
	 */
	private CTabItem appendNewTab(CTabFolder tabFolder, FilterCondition condition) {
		TabControlRef ctrlRef = new TabControlRef();

		Composite tabComposite = new Composite(tabFolder, SWT.NONE);
		tabComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		tabComposite.setLayout(new GridLayout(2, false));

		// 説明文
		Label lblDescription = new Label(tabComposite, SWT.NONE);
		lblDescription.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
		lblDescription.setText(Messages.getString("fltset.description") + " : ");

		Text txtDescription = new Text(tabComposite, SWT.BORDER | SWT.LEFT);
		txtDescription.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		txtDescription.setText(condition.description == null ? "" : condition.description);
		ctrlRef.txtDescription = txtDescription;

		// 条件反転
		Button chkNegative = new Button(tabComposite, SWT.CHECK);
		chkNegative.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
		chkNegative.setText(Messages.getString("fltset.negate.caption"));
		chkNegative.setSelection(condition.negative);
		ctrlRef.chkNegative = chkNegative;

		// 条件n
		Label lblConditions = new Label(tabComposite, SWT.NONE);
		lblConditions.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
		String notesMessage = diversity.getNotesMessage();
		if (notesMessage != null && notesMessage.length() > 0) {
			notesMessage = "(" + notesMessage + ")";
		}
		lblConditions.setText(Messages.getString("fltset.conditions") + " :    " + notesMessage);

		Tree table = new Tree(tabComposite, SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.MULTI | SWT.BORDER);
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1);
		gd.heightHint = 300;
		table.setLayoutData(gd);

		PropertySheet propertySheet = new PropertySheet(table);
		propertySheet.setInput(condition.property);
		propertySheet.expandAll();
		ctrlRef.property = condition.property;

		int num = 1 + tabFolder.getItemCount() - (tabAppender == null ? 0 : 1);
		CTabItem tabItem = new CTabItem(tabFolder, SWT.CLOSE, num - 1);
		tabItem.setControl(tabComposite);
		tabItem.setText(formatTabText(num));
		tabItem.setData(KEY_CTRL_REF, ctrlRef);

		return tabItem;
	}
}
