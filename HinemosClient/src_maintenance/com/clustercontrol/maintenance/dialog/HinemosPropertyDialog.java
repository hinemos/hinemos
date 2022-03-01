/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.maintenance.dialog;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.openapitools.client.model.AddHinemosPropertyRequest;
import org.openapitools.client.model.HinemosPropertyResponse;
import org.openapitools.client.model.HinemosPropertyResponse.TypeEnum;

import com.clustercontrol.accesscontrol.bean.RoleIdConstant;
import com.clustercontrol.bean.PropertyDefineConstant;
import com.clustercontrol.bean.RequiredFieldColorConstant;
import com.clustercontrol.common.util.CommonRestClientWrapper;
import com.clustercontrol.composite.ManagerListComposite;
import com.clustercontrol.dialog.CommonDialog;
import com.clustercontrol.fault.HinemosPropertyDuplicate;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.maintenance.HinemosPropertyTypeConstant;
import com.clustercontrol.maintenance.HinemosPropertyTypeMessage;
import com.clustercontrol.maintenance.action.ModifyHinemosProperty;
import com.clustercontrol.maintenance.composite.HinemosPropertyComposite;
import com.clustercontrol.maintenance.util.HinemosPropertyBeanUtil;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.WidgetTestUtil;


/**
 * メンテナンス[共通設定の作成・変更]ダイアログクラスです。
 *
 * @version 5.0.0
 * @since 5.0.0
 */
public class HinemosPropertyDialog extends CommonDialog {

	public static final int WIDTH_TITLE = 2;
	public static final int WIDTH_TEXT = 8;

	// ログ
	private static Log m_log = LogFactory.getLog(HinemosPropertyComposite.class);

	// キー
	private Text m_key = null;
	// 値
	private Text m_value = null;
	// 真偽値コンボボックス
	private Combo m_blCombo = null;
	// 値種別
	private Text m_valueType = null;
	// 説明/
	private Text m_textDescription = null;
	// ダイアログ表示時の処理タイプ
	private final int mode;
	// 値種別
	private TypeEnum valueType = null;
	// 共通設定情報
	private HinemosPropertyResponse HinemosPropertyInfo;
	/** マネージャ名 */
	private String managerName = null;
	/** マネージャ名コンボボックス用コンポジット */
	private ManagerListComposite m_managerComposite = null;

	/**
	 * コンストラクタ
	 * 
	 * @param parent
	 *            親シェル
	 * @param managerName
	 *            マネージャ名
	 * @param valueType
	 *            値種別
	 * @param mode
	 *            ダイアログ表示時の処理タイプ
	 * @param info
	 *            共通設定情報
	 */
	public HinemosPropertyDialog(Shell parent, String managerName, TypeEnum valueType, int mode,
			HinemosPropertyResponse info) {
		super(parent);
		this.managerName = managerName;
		this.valueType = valueType;
		this.mode = mode;
		this.HinemosPropertyInfo = info;
	}

	/**
	 * ダイアログエリアを生成します。
	 *
	 * @param parent
	 *            親コンポジット
	 */
	@Override
	protected void customizeDialog(Composite parent) {
		Shell shell = this.getShell();

		// タイトル
		shell.setText(Messages.getString("dialog.hinemos.property.settings.modify"));

		// 変数として利用されるラベル
		Label label = null;
		// 変数として利用されるグリッドデータ
		GridData gridData = null;

		// レイアウト
		GridLayout layout = new GridLayout(1, true);

		layout.marginWidth = 10;
		layout.numColumns = 10;
		layout.marginBottom = -10;
		// セル間の間隔
		layout.verticalSpacing = 10;
		layout.makeColumnsEqualWidth = true;
		parent.setLayout(layout);

		/*
		 * マネージャ
		 */
		Label labelManager = new Label(parent, SWT.LEFT);
		WidgetTestUtil.setTestId(this, "manager", labelManager);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TITLE;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		labelManager.setLayoutData(gridData);
		labelManager.setText(Messages.getString("facility.manager") + " : ");
		if (this.mode == PropertyDefineConstant.MODE_MODIFY) {
			this.m_managerComposite = new ManagerListComposite(parent, SWT.NONE, false);
		} else {
			this.m_managerComposite = new ManagerListComposite(parent, SWT.NONE, true);
		}
		WidgetTestUtil.setTestId(this, "managerComposite", this.m_managerComposite);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TEXT;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_managerComposite.setLayoutData(gridData);

		if (this.managerName != null) {
			this.m_managerComposite.setText(this.managerName);
		}

		/*
		 * キー
		 */
		// ラベル
		label = new Label(parent, SWT.NONE);
		WidgetTestUtil.setTestId(this, null, label);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TITLE;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("hinemos.property.key") + " : ");
		// テキスト
		this.m_key = new Text(parent, SWT.BORDER | SWT.LEFT);
		WidgetTestUtil.setTestId(this, "id", m_key);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TEXT;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_key.setLayoutData(gridData);
		if (this.mode == PropertyDefineConstant.MODE_MODIFY || this.mode == PropertyDefineConstant.MODE_SHOW) {
			this.m_key.setEnabled(false);
		}
		this.m_key.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		/*
		 * 値
		 */
		// ラベル
		label = new Label(parent, SWT.NONE);
		WidgetTestUtil.setTestId(this, "value", label);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TITLE;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("value") + " : ");
		// テキスト
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		if (TypeEnum.BOOLEAN.equals(this.valueType)) {
			gridData.horizontalSpan = 2;
			this.m_blCombo = new Combo(parent, SWT.DROP_DOWN | SWT.READ_ONLY);
			WidgetTestUtil.setTestId(this, null, m_blCombo);
			this.m_blCombo.add(HinemosPropertyTypeConstant.BOOL_TRUE);
			this.m_blCombo.add(HinemosPropertyTypeConstant.BOOL_FALSE);
			this.m_blCombo.setLayoutData(gridData);
			this.m_blCombo.addModifyListener(new ModifyListener() {
				@Override
				public void modifyText(ModifyEvent arg0) {
					update();
				}
			});
			// 余白
			label = new Label(parent, SWT.NONE);
			WidgetTestUtil.setTestId(this, "blank", label);
			gridData = new GridData();
			gridData.horizontalSpan = 6;
			gridData.horizontalAlignment = GridData.FILL;
			gridData.grabExcessHorizontalSpace = true;
			label.setLayoutData(gridData);
		} else {
			gridData.horizontalSpan = WIDTH_TEXT;
			this.m_value = new Text(parent, SWT.BORDER | SWT.LEFT);
			WidgetTestUtil.setTestId(this, "value", m_value);
			this.m_value.setLayoutData(gridData);
			this.m_value.addModifyListener(new ModifyListener() {
				@Override
				public void modifyText(ModifyEvent arg0) {
					update();
				}
			});
		}

		/*
		 * 値種別
		 */
		// ラベル
		label = new Label(parent, SWT.NONE);
		WidgetTestUtil.setTestId(this, "valuetype", label);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TITLE;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("hinemos.property.value_type") + " : ");
		// テキスト
		this.m_valueType = new Text(parent, SWT.BORDER | SWT.LEFT);
		WidgetTestUtil.setTestId(this, "valuetype", m_valueType);
		this.m_valueType.setText(HinemosPropertyTypeMessage.typeToString(this.valueType));
		this.m_valueType.setEnabled(false);
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_valueType.setLayoutData(gridData);
		this.m_valueType.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		// 余白
		label = new Label(parent, SWT.NONE);
		WidgetTestUtil.setTestId(this, "blank2", label);
		gridData = new GridData();
		gridData.horizontalSpan = 6;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);

		/*
		 * 説明
		 */
		// ラベル
		label = new Label(parent, SWT.NONE);
		WidgetTestUtil.setTestId(this, "description", label);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TITLE;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("description") + " : ");
		// テキスト
		this.m_textDescription = new Text(parent, SWT.BORDER | SWT.LEFT);
		WidgetTestUtil.setTestId(this, "description", m_textDescription);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TEXT;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_textDescription.setLayoutData(gridData);
		this.m_textDescription.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		// ラインを引く
		Label line = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);
		WidgetTestUtil.setTestId(this, "line", line);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalSpan = 15;
		line.setLayoutData(gridData);

		// サイズを最適化
		// グリッドレイアウトを用いた場合、こうしないと横幅が画面いっぱいになる
		shell.pack();
		shell.setSize(new Point(550, shell.getSize().y));

		// 画面中央に配置
		Display display = shell.getDisplay();
		shell.setLocation((display.getBounds().width - shell.getSize().x) / 2,
				(display.getBounds().height - shell.getSize().y) / 2);

		// 最後に変更であれば情報を表示
		this.setInputData();
	}

	private void update() {
		// キーが必須項目であることを明示
		if (this.m_key.getEnabled() && "".equals(this.m_key.getText())) {
			this.m_key.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		} else {
			this.m_key.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
	}

	/**
	 * 選択されているデータをセットする。
	 */
	protected void setInputData() {

		if (this.HinemosPropertyInfo != null) {
			if (this.HinemosPropertyInfo.getKey() != null) {
				this.m_key.setText(this.HinemosPropertyInfo.getKey());
			}

			if (this.HinemosPropertyInfo.getValue() != null) {
				if(TypeEnum.BOOLEAN.equals(this.HinemosPropertyInfo.getType())){
					this.m_blCombo.setText(this.HinemosPropertyInfo.getValue());
				} else {
					this.m_value.setText(this.HinemosPropertyInfo.getValue());
				}
			}
			if (this.HinemosPropertyInfo.getDescription() != null) {
				this.m_textDescription.setText(this.HinemosPropertyInfo.getDescription());
			}
		}

		// 各項目が必須項目であることを明示
		this.update();
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
	 * 入力値をマネージャに登録します。
	 *
	 * @return true：正常、false：異常
	 *
	 * @see com.clustercontrol.dialog.CommonDialog#action()
	 */
	@Override
	protected boolean action() {
		boolean result = false;

		if (HinemosPropertyInfo == null) {
			this.HinemosPropertyInfo = new HinemosPropertyResponse();
		}

		HinemosPropertyInfo.setKey(m_key.getText());
		HinemosPropertyInfo.setType(valueType);

		if (TypeEnum.STRING.equals(valueType)) {
			HinemosPropertyInfo.setValue(m_value.getText());
		} else if (TypeEnum.NUMERIC.equals(valueType)) {
			if (m_value.getText() == null || m_value.getText().trim().length() == 0) {
				HinemosPropertyInfo.setValue(null);
			} else {
				try {
					Long.parseLong(m_value.getText().trim());
					HinemosPropertyInfo.setValue(m_value.getText().trim());
				} catch (NumberFormatException e) {
					m_log.info("action() setValueNumeric, " + e.getMessage());
					Object[] args = { Messages.getString("hinemos.property.value"), Long.MIN_VALUE, Long.MAX_VALUE };
					MessageDialog.openError(null, Messages.getString("failed"),
							Messages.getString("message.common.4", args));

					return false;
				}
			}
		} else {
			HinemosPropertyInfo.setValue(m_blCombo.getText());
		}

		HinemosPropertyInfo.setDescription(m_textDescription.getText());
		HinemosPropertyInfo.setOwnerRoleId(RoleIdConstant.ADMINISTRATORS);

		String managerName = this.m_managerComposite.getText();
		CommonRestClientWrapper wrapper = CommonRestClientWrapper.getWrapper(managerName);
		if (this.mode == PropertyDefineConstant.MODE_ADD) {
			// 作成の場合
			String[] args = { HinemosPropertyInfo.getKey(), managerName };

			try {
				AddHinemosPropertyRequest request = HinemosPropertyBeanUtil.convertToAddHinemosPropertyRequest(HinemosPropertyInfo);
				wrapper.addHinemosProperty(request);
				MessageDialog.openInformation(null, Messages.getString("successful"),
						Messages.getString("message.hinemos.property.2", args));

				result = true;
			} catch (HinemosPropertyDuplicate e) {
				// キーが重複している場合、エラーダイアログを表示する
				MessageDialog.openInformation(null, Messages.getString("message"),
						Messages.getString("message.hinemos.property.10", args));
			} catch (Exception e) {
				String errMessage = "";
				if (e instanceof InvalidRole) {
					MessageDialog.openInformation(null, Messages.getString("message"),
							Messages.getString("message.accesscontrol.16"));
				} else {
					errMessage = ", " + HinemosMessage.replace(e.getMessage());
				}
				MessageDialog.openError(null, Messages.getString("failed"),
						Messages.getString("message.hinemos.property.3", args) + errMessage);
			}

		} else if (this.mode == PropertyDefineConstant.MODE_MODIFY) {
			// 変更の場合
			result = new ModifyHinemosProperty().modify(m_managerComposite.getText(), m_key.getText(), HinemosPropertyInfo);
		}

		return result;
	}
}
