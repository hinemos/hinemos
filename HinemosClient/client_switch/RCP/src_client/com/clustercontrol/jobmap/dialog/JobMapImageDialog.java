/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmap.dialog;

import java.io.File;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.draw2d.ColorConstantsWrapper;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.openapitools.client.model.JobmapIconImageInfoResponse;

import com.clustercontrol.bean.DataRangeConstant;
import com.clustercontrol.bean.PropertyDefineConstant;
import com.clustercontrol.bean.RequiredFieldColorConstant;
import com.clustercontrol.bean.SizeConstant;
import com.clustercontrol.composite.ManagerListComposite;
import com.clustercontrol.composite.RoleIdListComposite;
import com.clustercontrol.composite.RoleIdListComposite.Mode;
import com.clustercontrol.composite.action.StringVerifyListener;
import com.clustercontrol.dialog.CommonDialog;
import com.clustercontrol.dialog.ValidateResult;
import com.clustercontrol.fault.IconFileDuplicate;
import com.clustercontrol.fault.IconFileNotFound;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.jobmanagement.util.JobmapIconImageUtil;
import com.clustercontrol.jobmap.util.JobMapRestClientWrapper;
import com.clustercontrol.jobmap.util.JobmapImageCacheUtil;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.WidgetTestUtil;

/**
 * ジョブマップ用イメージファイル作成・変更ダイアログクラス(RCP)<BR>
 *
 * @version 6.0.a
 */
public class JobMapImageDialog extends CommonDialog {

	// ログ
	private static Log m_log = LogFactory.getLog( JobMapImageDialog.class );

	// イメージファイル登録済みメッセージ
	private static final String REGISTERED = Messages.getString("jobmap.image.registered");

	// イメージファイル最大サイズ
	private long maxFileSize = 2 * 1024 * 1024;
	
	/** ジョブマップ用アイコンファイル情報 */
	private JobmapIconImageInfoResponse m_jobmapIconImage = null;
	/** ジョブマップ用アイコン画像ファイル */
	private File m_iconImageFile = null;
	/** ジョブマップ用アイコン画像ファイルデータ */
	private byte[] m_iconImageFiledata = null;
	/** マネージャ名 */
	private String m_managerName = null;
	/** アイコンID */
	private String m_iconId = null;

	/** アイコンID用テキスト */
	private Text m_txtIconId = null;
	/** 説明用テキスト */
	private Text m_txtDescription = null;
	/** イメージファイルパス用テキスト */
	private Text m_txtImageFilePath = null;
	/** イメージファイルパス用ボタン */
	private Button m_btnImageFilePath = null;
	/** イメージファイル用ラベル */
	private Label m_lblImage;
	/** オーナーロールID用テキスト */
	private RoleIdListComposite m_roleIdListComposite = null;
	/** マネージャ名コンボボックス用コンポジット */
	private ManagerListComposite m_managerComposite = null;

	/** シェル */
	private Shell m_shell = null;

	/** 
	 * 変更用ダイアログ判別フラグ
	 * 作成：MODE_ADD = 0;
	 * 変更：MODE_MODIFY = 1;
	 * */
	private int m_mode = PropertyDefineConstant.MODE_ADD;

	/**
	 * コンストラクタ（変更時）
	 *
	 * @param parent 親のシェルオブジェクト
	 */
	public JobMapImageDialog(
			Shell parent, 
			String managerName, 
			String iconId, 
			int mode) {
		super(parent);
		this.m_managerName = managerName;
		this.m_iconId = iconId;
		this.m_mode = mode;
	}

	/**
	 * コンストラクタ（新規登録時）
	 *
	 * @param parent 親のシェルオブジェクト
	 */
	public JobMapImageDialog(Shell parent, 	String managerName) {
		super(parent);
		this.m_managerName = managerName;
		this.m_jobmapIconImage = new JobmapIconImageInfoResponse();
	}

	/**
	 * ダイアログエリアを生成します。
	 *
	 * @param parent 親のインスタンス
	 */
	@Override
	protected void customizeDialog(Composite parent) {
		this.m_shell = this.getShell();
		this.m_shell.setText(Messages.getString("dialog.jobmap.image.create.modify"));

		Label label = null;

		/**
		 * レイアウト設定
		 * ダイアログ内のベースとなるレイアウトが全てを変更
		 */
		RowLayout layout = new RowLayout();
		layout.type = SWT.VERTICAL;
		layout.spacing = 0;
		layout.marginWidth = 10;
		layout.marginHeight = 10;
		layout.marginBottom = 0;
		layout.fill = true;
		parent.setLayout(layout);

		// Composite
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(3, false));

		// マネージャ（ラベル）
		label = new Label(composite, SWT.LEFT);
		label.setText(Messages.getString("facility.manager") + " : ");
		label.setLayoutData(new GridData(100, SizeConstant.SIZE_LABEL_HEIGHT));

		// マネージャ（Composite）
		if(this.m_mode == PropertyDefineConstant.MODE_MODIFY){
			this.m_managerComposite = new ManagerListComposite(composite, SWT.NONE, false);
		} else {
			this.m_managerComposite = new ManagerListComposite(composite, SWT.NONE, true);
			this.m_managerComposite.getComboManagerName().addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					String managerName = m_managerComposite.getText();
					m_roleIdListComposite.createRoleIdList(managerName);
				}
			});
		}
		WidgetTestUtil.setTestId(this, "m_managerComposite", this.m_managerComposite);
		this.m_managerComposite.setLayoutData(new GridData());
		((GridData)this.m_managerComposite.getLayoutData()).horizontalSpan = 2;
		((GridData)this.m_managerComposite.getLayoutData()).widthHint = 227;
		if(this.m_managerName != null) {
			this.m_managerComposite.setText(this.m_managerName);
		}

		// アイコンID（ラベル）
		label = new Label(composite, SWT.LEFT);
		label.setText(Messages.getString("icon.id") + " : ");
		label.setLayoutData(new GridData(100, SizeConstant.SIZE_LABEL_HEIGHT));

		// アイコンID（テキスト）
		this.m_txtIconId = new Text(composite, SWT.BORDER | SWT.LEFT);
		WidgetTestUtil.setTestId(this, "m_txtIconId", this.m_txtIconId);
		this.m_txtIconId.setLayoutData(new GridData(220, SizeConstant.SIZE_TEXT_HEIGHT));
		((GridData)this.m_txtIconId.getLayoutData()).horizontalSpan = 2;
		this.m_txtIconId.addVerifyListener(
				new StringVerifyListener(DataRangeConstant.VARCHAR_64));
		this.m_txtIconId.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});
		if (this.m_mode == PropertyDefineConstant.MODE_MODIFY) {
			this.m_txtIconId.setEnabled(false);
		}

		// 説明（ラベル）
		label = new Label(composite, SWT.LEFT);
		label.setText(Messages.getString("description") + " : ");
		label.setLayoutData(new GridData(100, SizeConstant.SIZE_LABEL_HEIGHT));

		// 説明（テキスト）
		this.m_txtDescription = new Text(composite, SWT.BORDER | SWT.LEFT);
		WidgetTestUtil.setTestId(this, "m_txtDescription", this.m_txtDescription);
		this.m_txtDescription.setLayoutData(new GridData(220, SizeConstant.SIZE_TEXT_HEIGHT));
		((GridData)this.m_txtDescription.getLayoutData()).horizontalSpan = 2;
		this.m_txtDescription.addVerifyListener(
				new StringVerifyListener(DataRangeConstant.VARCHAR_64));
		this.m_txtDescription.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		// オーナーロールID（ラベル）
		label = new Label(composite, SWT.LEFT);
		label.setText(Messages.getString("owner.role.id") + " : ");
		label.setLayoutData(new GridData(100, SizeConstant.SIZE_LABEL_HEIGHT));

		// オーナーロールID（Composite）
		if (this.m_mode == PropertyDefineConstant.MODE_ADD) {
			this.m_roleIdListComposite = new RoleIdListComposite(composite,
					SWT.NONE, this.m_managerName, true, Mode.OWNER_ROLE);
		} else {
			this.m_roleIdListComposite = new RoleIdListComposite(composite,
					SWT.NONE, this.m_managerName, false, Mode.OWNER_ROLE);
		}
		WidgetTestUtil.setTestId(this, "m_roleIdListComposite", this.m_roleIdListComposite);
		this.m_roleIdListComposite.setLayoutData(new GridData());
		((GridData)this.m_roleIdListComposite.getLayoutData()).horizontalSpan = 2;
		((GridData)this.m_roleIdListComposite.getLayoutData()).widthHint = 227;

		// ファイルデータ（ラベル）
		label = new Label(composite, SWT.LEFT);
		label.setText(Messages.getString("jobmap.image.file") + " : ");
		label.setLayoutData(new GridData(100, SizeConstant.SIZE_LABEL_HEIGHT));

		// ファイルデータ（テキスト）
		this.m_txtImageFilePath = new Text(composite, SWT.READ_ONLY | SWT.BORDER);
		WidgetTestUtil.setTestId(this, "m_imageFilePathText", this.m_txtImageFilePath);
		this.m_txtImageFilePath.setLayoutData(new GridData(220,
				SizeConstant.SIZE_TEXT_HEIGHT));
		this.m_txtImageFilePath.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		// ファイルデータ（ボタン）
		this.m_btnImageFilePath = new Button(composite, SWT.NONE);
		WidgetTestUtil.setTestId(this, "m_btnImageFilePath", this.m_btnImageFilePath);
		this.m_btnImageFilePath.setText(Messages.getString("refer"));
		this.m_btnImageFilePath.setLayoutData(new GridData(40,
				SizeConstant.SIZE_BUTTON_HEIGHT));
		this.m_btnImageFilePath.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				FileDialog dialog = new FileDialog(m_shell, SWT.OPEN);
				if (m_txtImageFilePath.getText() != null 
						&& !m_txtImageFilePath.getText().equals("")) {
					dialog.setFileName(m_txtImageFilePath.getText().trim());
				}
				dialog.setFilterExtensions(new String[]{"*.gif;*.png;*.jpg;*.bmp", "*.*"});

				String path = dialog.open();
				if(path != null && !"".equals(path)){
					try {
						File file = new File(path);
						m_iconImageFile = file;
						if(file.exists()) {
							if (file.length() > maxFileSize) {
								String[] args = {Long.toString(file.length()), Long.toString(maxFileSize)};
								MessageDialog.openInformation(
										null,
										Messages.getString("message"),
										Messages.getString("message.job.136", args));
							} else {
								m_txtImageFilePath.setText(file.getCanonicalPath());
								// ファイルデータを取得する
								byte[] filedata = JobmapIconImageUtil.getImageFileData(file.getCanonicalPath());
								m_iconImageFiledata = filedata;
								// イメージの反映
								m_lblImage.setImage(JobmapIconImageUtil.getIconImage(filedata));
							}
						}
					} catch (IOException ie) {
						MessageDialog.openInformation(
								null,
								Messages.getString("message"),
								Messages.getString("message.job.137"));
					} catch (Exception ex) {
						MessageDialog.openInformation(
								null,
								Messages.getString("message"),
								Messages.getString("message.job.137") 
								+ HinemosMessage.replace(ex.getMessage()));
					}
				}
			}
		});

		// dummy
		new Label(composite, SWT.NONE);

		// イメージデータ
		this.m_lblImage = new Label(composite, SWT.BORDER);
		this.m_lblImage.setBackground(ColorConstantsWrapper.lightGray());
		this.m_lblImage.setLayoutData(new GridData(
				JobmapIconImageUtil.ICON_WIDTH, 
				JobmapIconImageUtil.ICON_HEIGHT));
		((GridData)this.m_lblImage.getLayoutData()).horizontalSpan = 2;

		// ダイアログを調整
		this.adjustDialog();

		// データ設定
		this.reflectJobMapImage();

		// 必須入力項目を可視化
		this.update();
	}

	/**
	 * ダイアログエリアを調整します。
	 *
	 */
	private void adjustDialog(){
		// サイズを最適化
		// グリッドレイアウトを用いた場合、こうしないと横幅が画面いっぱいになります。
		this.m_shell.pack();
		this.m_shell.setSize(new Point(500, m_shell.getSize().y));

		// 画面中央に配置
		Display display = this.m_shell.getDisplay();
		this.m_shell.setLocation((display.getBounds().width - this.m_shell.getSize().x) / 2,
				(display.getBounds().height - this.m_shell.getSize().y) / 2);
	}

	/**
	 * 更新処理
	 */
	public void update(){

		// 必須項目を明示
		// アイコンID
		if ("".equals(this.m_txtIconId.getText())) {
			this.m_txtIconId.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		} else {
			this.m_txtIconId.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}

		// 説明
		if ("".equals(this.m_txtDescription.getText())) {
			this.m_txtDescription.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		} else {
			this.m_txtDescription.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}

		// イメージファイル
		if ("".equals(this.m_txtImageFilePath.getText())) {
			this.m_txtImageFilePath.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		} else {
			this.m_txtImageFilePath.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
	}

	/**
	 * ダイアログにジョブマップ用アイコンファイル情報を反映します。
	 */
	private void reflectJobMapImage() {
		if (this.m_mode == PropertyDefineConstant.MODE_ADD) {
			return;
		}

		try {
			JobmapImageCacheUtil iconCache = JobmapImageCacheUtil.getInstance();
			this.m_jobmapIconImage  = iconCache.getJobmapIconImageCacheEntry(this.m_managerName, this.m_iconId).getJobmapIconImage();
			this.m_iconImageFiledata = iconCache.getJobmapIconImageCacheEntry(this.m_managerName, this.m_iconId).getFiledata();
		} catch (IconFileNotFound e) {
			MessageDialog.openInformation(
					null,
					Messages.getString("message"),
					Messages.getString("message.job.148", new String[]{this.m_iconId}));
		} catch (InvalidRole e) {
			// アクセス権なしの場合、エラーダイアログを表示する
			MessageDialog.openInformation(
					null,
					Messages.getString("message"),
					Messages.getString("message.accesscontrol.16"));
		} catch (InvalidUserPass e) {
			MessageDialog.openError(
					null,
					Messages.getString("failed"),
					Messages.getString("message.job.140") + " " + HinemosMessage.replace(e.getMessage()));
		} catch (InvalidSetting e) {
			MessageDialog.openError(
					null,
					Messages.getString("failed"),
					Messages.getString("message.job.140") + " " + HinemosMessage.replace(e.getMessage()));
		} catch (Exception e) {
			m_log.warn("action(), " + HinemosMessage.replace(e.getMessage()), e);
			MessageDialog.openError(
					null,
					Messages.getString("failed"),
					Messages.getString("message.hinemos.failure.unexpected") + ", " + HinemosMessage.replace(e.getMessage()));
		}

		// オーナーロールID設定
		if (this.m_jobmapIconImage.getOwnerRoleId() != null) {
			this.m_roleIdListComposite.setText(this.m_jobmapIconImage.getOwnerRoleId());
		}

		// アイコンIDを設定
		if (this.m_jobmapIconImage.getIconId() != null) {
			this.m_txtIconId.setText(this.m_jobmapIconImage.getIconId());
			if(this.m_mode == PropertyDefineConstant.MODE_MODIFY){
				// 変更時、アイコンIDは変更不可とする
				this.m_txtIconId.setEditable(false);
			}
		}

		// 説明を設定
		if (this.m_jobmapIconImage.getDescription() != null) {
			this.m_txtDescription.setText(this.m_jobmapIconImage.getDescription());
		}

		// アイコンイメージファイルパスを設定
		if (m_iconImageFiledata != null) {
			this.m_txtImageFilePath.setText(REGISTERED);
			this.m_lblImage.setImage(JobmapIconImageUtil.getIconImage(m_iconImageFiledata));
		}
	}

	/**
	 * ダイアログの情報からジョブマップ用アイコンファイル情報を作成します。
	 *
	 * @return 入力値の検証結果
	 * 
	 */
	@Override
	protected ValidateResult validate() {
		ValidateResult result = null;

		// オーナーロールID
		if (this.m_roleIdListComposite.getText().length() > 0) {
			this.m_jobmapIconImage.setOwnerRoleId(this.m_roleIdListComposite.getText());
		}

		// 実行契機ID取得
		if (this.m_txtIconId.getText().length() > 0) {
			this.m_jobmapIconImage.setIconId(this.m_txtIconId.getText());
		} else {
			result = new ValidateResult();
			result.setValid(false);
			result.setID(Messages.getString("message.hinemos.1"));
			result.setMessage(Messages.getString("message.job.138"));
			return result;
		}

		// 説明取得
		if (this.m_txtDescription.getText().length() > 0) {
			this.m_jobmapIconImage.setDescription(this.m_txtDescription.getText());
		} else {
			result = new ValidateResult();
			result.setValid(false);
			result.setID(Messages.getString("message.hinemos.1"));
			result.setMessage(Messages.getString("message.job.128"));
			return result;
		}

		// イメージファイル
		if ("".equals(this.m_txtImageFilePath.getText())) {
			return ValidateResult.messageOf(Messages.getString("message.hinemos.1"),
					Messages.getString("message.common.1", new String[] { Messages.getString("jobmap.image.file") }));
		}

		return null;
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
		JobmapImageCacheUtil iconCache = JobmapImageCacheUtil.getInstance();
		try {
			String managerName = this.m_managerComposite.getText();
			//ジョブマップ向けエンドポイント有効チェック
			try {
				JobMapRestClientWrapper wrapper = JobMapRestClientWrapper.getWrapper(managerName);
				wrapper.checkPublish();
			} catch (Exception e) {
				//NGならエラーダイアログを表示して終了する。
				MessageDialog.openInformation(
					null,
					Messages.getString("message"),
					e.getMessage());
				return result;
			}
			if (this.m_mode == PropertyDefineConstant.MODE_MODIFY) {
				iconCache.modifyJobmapIconImage(managerName, this.m_jobmapIconImage, m_iconImageFile, false, this.m_iconImageFiledata);
				Object[] arg = {managerName};
				MessageDialog.openInformation(null, Messages.getString("successful"),
						Messages.getString("message.job.77", arg));
			} else {
				iconCache.modifyJobmapIconImage(managerName, this.m_jobmapIconImage, m_iconImageFile, true, null);
				Object[] arg = {managerName};
				MessageDialog.openInformation(null, Messages.getString("successful"),
						Messages.getString("message.job.79", arg));
			}
			result = true;
		} catch (InvalidRole e) {
			// アクセス権なしの場合、エラーダイアログを表示する
			MessageDialog.openInformation(
					null,
					Messages.getString("message"),
					Messages.getString("message.accesscontrol.16"));
		} catch (IconFileDuplicate e) {
			String[] args = {this.m_jobmapIconImage.getIconId()};
			MessageDialog.openError(
					null,
					Messages.getString("failed"),
					Messages.getString("message.job.139",args) + " " + HinemosMessage.replace(e.getMessage()));
		} catch (InvalidUserPass e) {
			MessageDialog.openError(
					null,
					Messages.getString("failed"),
					Messages.getString("message.job.140") + " " + HinemosMessage.replace(e.getMessage()));
		} catch (InvalidSetting e) {
			MessageDialog.openError(
					null,
					Messages.getString("failed"),
					Messages.getString("message.job.140") + " " + HinemosMessage.replace(e.getMessage()));
		} catch (Exception e) {
			m_log.warn("action(), " + HinemosMessage.replace(e.getMessage()), e);
			MessageDialog.openError(
					null,
					Messages.getString("failed"),
					Messages.getString("message.hinemos.failure.unexpected") + ", " + HinemosMessage.replace(e.getMessage()));
		}
		return result;
	}
}
