/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.hub.dialog;

import java.util.Locale;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;

import com.clustercontrol.bean.DataRangeConstant;
import com.clustercontrol.bean.Property;
import com.clustercontrol.bean.PropertyDefineConstant;
import com.clustercontrol.dialog.CommonDialog;
import com.clustercontrol.hub.action.DownloadCollectedData;
import com.clustercontrol.hub.composite.LogSearchComposite;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.PropertyUtil;
import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.viewer.PropertySheet;
import com.clustercontrol.ws.hub.StringData;

/**
 * 収集蓄積[レコードの詳細]ダイアログクラス<BR>
 *
 * @version 6.1.0
 * @since 6.1.0
 */
public class RecordInfoDialog extends CommonDialog {

	// ログ.
	private static Log m_log = LogFactory.getLog(RecordInfoDialog.class);

	/** 選択されたアイテム */
	private StringData selectData = null;
	/** プロパティシート */
	private PropertySheet propertySheet = null;
	/** プロパティ */
	private Property property = null;
	/** ダウンロードボタン */
	private Button btnDownload = null;
	/** バイナリデータ */
	private boolean isBinary;

	// ダウンロード処理向け
	/** 親シェル */
	private Shell parentShell;
	/** マネージャー名 */
	private String manager;

	// プロパティID.
	private static final String PROPERTY_ID_TIME = "time";
	private static final String PROPERTY_ID_FACILITY_ID = "facilityId";
	private static final String PROPERTY_ID_MONITOR_ID = "monitorId";
	private static final String PROPERTY_ID_MESSAGE = "message";
	private static final String PROPERTY_ID_TAGS = "tags";

	/**
	 * インスタンスを返します。
	 *
	 * @param parent
	 *            親のシェルオブジェクト
	 */
	public RecordInfoDialog(Shell parentShell, String manager, StringData selectData, boolean isBinary) {
		super(parentShell);
		setShellStyle(getShellStyle() | SWT.RESIZE | SWT.MAX);
		this.parentShell = parentShell;
		this.manager = manager;
		this.selectData = selectData;
		this.isBinary = isBinary;
	}

	/**
	 * ダイアログの初期サイズを返します。
	 *
	 * @return 初期サイズ
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(500, 260);
	}

	/**
	 * ダイアログエリアを生成します。
	 *
	 * @param parent
	 *            親のコンポジット
	 */
	@Override
	protected void customizeDialog(Composite parent) {
		Shell shell = this.getShell();

		// タイトル
		shell.setText(Messages.getString("dialog.hub.info.records"));

		// レイアウト
		GridLayout layout = new GridLayout(1, true);
		layout.marginWidth = 10;
		layout.marginHeight = 10;
		parent.setLayout(layout);

		/*
		 * 属性プロパティシート
		 */
		GridData gridData = null;

		// プロパティシート
		Tree table = new Tree(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.SINGLE | SWT.BORDER);
		WidgetTestUtil.setTestId(this, null, table);

		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalSpan = 1;
		table.setLayoutData(gridData);

		this.propertySheet = new PropertySheet(table);
		this.propertySheet.setSize(170, 280);

		// プロパティ取得及び設定
		property = null;
		if (selectData != null) {
			try {
				property = this.columnToProperty(selectData, Locale.getDefault());
				this.propertySheet.setInput(property);
			} catch (Exception e) {
				m_log.warn("customizeDialog() getEventInfo, " + e.getMessage(), e);
				MessageDialog.openError(null, Messages.getString("failed"),
						Messages.getString("message.hinemos.failure.unexpected") + ", "
								+ HinemosMessage.replace(e.getMessage()));
			}
		}

		// ラインを引く
		Label line = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);
		WidgetTestUtil.setTestId(this, "line", line);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalSpan = 1;
		line.setLayoutData(gridData);

		// 画面中央に
		Display display = shell.getDisplay();
		shell.setLocation((display.getBounds().width - shell.getSize().x) / 2,
				(display.getBounds().height - shell.getSize().y) / 2);
	}

	/**
	 * 選択したカラムをプロパティに変換.<BR>
	 * <p>
	 * <ol>
	 * <li>フィルタ項目毎にID, 名前, 処理定数（
	 * {@link com.clustercontrol.bean.PropertyDefineConstant}）を指定し、 プロパティ（
	 * {@link com.clustercontrol.bean.Property}）を生成します。</li>
	 * <li>各項目のプロパティに値を設定し、ツリー状に定義します。</li>
	 * </ol>
	 * 
	 * <p>
	 * プロパティに定義する項目は、下記の通りです。
	 * <p>
	 * <ul>
	 * <li>プロパティ（親。ダミー）</li>
	 * <ul>
	 * <li>時刻（子。テキスト）</li>
	 * <li>ファシリティID（子。テキスト）</li>
	 * <li>監視ID（子。テキスト）</li>
	 * <li>メッセージ（子。テキストエリア）</li>
	 * <li>タグ（子。テキストエリア）</li>
	 * </ul>
	 * </ul>
	 * 
	 * @param columnList
	 *            選択レコードのカラム
	 * @param locale
	 *            ロケール情報
	 */
	private Property columnToProperty(StringData stringData, Locale locale) {
		// 受信日時
		Property time = new Property(PROPERTY_ID_TIME,
				Messages.getString("view.hub.log.search.result.culumn.timestamp", locale),
				PropertyDefineConstant.EDITOR_TEXT);
		// ファシリティID
		Property facilityId = new Property(PROPERTY_ID_FACILITY_ID,
				Messages.getString("view.hub.log.search.result.culumn.facility", locale),
				PropertyDefineConstant.EDITOR_TEXT);
		// 監視項目ID
		Property monitorId = new Property(PROPERTY_ID_MONITOR_ID,
				Messages.getString("view.hub.log.search.result.culumn.monitor", locale),
				PropertyDefineConstant.EDITOR_TEXT);
		// メッセージ
		Property message = new Property(PROPERTY_ID_MESSAGE,
				Messages.getString("view.hub.log.search.result.culumn.original.message", locale),
				PropertyDefineConstant.EDITOR_TEXTAREA, DataRangeConstant.TEXT);
		// タグ
		Property tag = new Property(PROPERTY_ID_TAGS,
				Messages.getString("view.hub.log.search.result.culumn.etc", locale),
				PropertyDefineConstant.EDITOR_TEXTAREA, DataRangeConstant.TEXT);

		// 値を初期化
		if (LogSearchComposite.ViewColumn.time.getProvider().getText(stringData) != null) {
			time.setValue(LogSearchComposite.ViewColumn.time.getProvider().getText(stringData));
		} else {
			time.setValue("");
		}
		if (LogSearchComposite.ViewColumn.facility.getProvider().getText(stringData) != null) {
			facilityId.setValue(LogSearchComposite.ViewColumn.facility.getProvider().getText(stringData));
		} else {
			facilityId.setValue("");
		}
		if (LogSearchComposite.ViewColumn.monitor.getProvider().getText(stringData) != null) {
			monitorId.setValue(LogSearchComposite.ViewColumn.monitor.getProvider().getText(stringData));
		} else {
			monitorId.setValue("");
		}
		if (LogSearchComposite.ViewColumn.original_message.getProvider().getText(stringData) != null) {
			message.setValue(LogSearchComposite.ViewColumn.original_message.getProvider().getText(stringData));
		} else {
			message.setValue("");
		}
		if (LogSearchComposite.ViewColumn.etc.getProvider().getText(stringData) != null) {
			tag.setValue(LogSearchComposite.ViewColumn.etc.getProvider().getText(stringData));
		} else {
			tag.setValue("");
		}

		// 変更の可/不可を設定
		time.setModify(PropertyDefineConstant.MODIFY_NG);
		facilityId.setModify(PropertyDefineConstant.MODIFY_NG);
		monitorId.setModify(PropertyDefineConstant.MODIFY_NG);
		message.setModify(PropertyDefineConstant.MODIFY_NG);
		tag.setModify(PropertyDefineConstant.MODIFY_NG);

		Property property = new Property(null, null, "");

		// 初期表示ツリーを構成。
		property.removeChildren();
		property.addChildren(time);
		property.addChildren(facilityId);
		property.addChildren(monitorId);
		property.addChildren(message);
		property.addChildren(tag);
		return property;
	}

	/**
	 * 入力値を保持したプロパティを返します。<BR>
	 * プロパティシートよりプロパティを取得します。
	 *
	 * @return プロパティ
	 *
	 * @see com.clustercontrol.viewer.PropertySheet#getInput()
	 */
	public Property getInputData() {
		if (property != null) {
			Property copy = PropertyUtil.copy(property);
			return copy;
		} else {
			return null;
		}
	}

	/**
	 * 入力値を保持したプロパティを設定します。
	 *
	 * @param property
	 *            プロパティ
	 */
	public void setInputData(Property property) {
		propertySheet.setInput(property);
	}

	/**
	 * ボタンを作成.
	 *
	 * @param parent
	 *            親のコンポジット（ボタンバー）
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {

		// ダウンロードボタン.
		btnDownload = this.createButton(parent, IDialogConstants.PROCEED_ID,
				Messages.getString("view.hub.binary.download"), true);
		btnDownload.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// 選択中のレコードをダウンロード.
				if (isBinary) {
					new DownloadCollectedData().executeBinaryRecord(parentShell, manager, selectData);
				} else {
					new DownloadCollectedData().executeTextRecord(parentShell, selectData);
				}
			}
		});
		// 閉じる(cancel)ボタン
		this.createButton(parent, IDialogConstants.CANCEL_ID, Messages.getString("cancel"), false);

	}

}
