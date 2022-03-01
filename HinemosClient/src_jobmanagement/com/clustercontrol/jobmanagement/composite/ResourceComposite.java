/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.jobmanagement.composite;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.openapitools.client.model.FacilityInfoResponse.FacilityTypeEnum;
import org.openapitools.client.model.JobResourceInfoResponse;
import org.openapitools.client.model.JobResourceInfoResponse.ResourceTypeEnum;
import org.openapitools.client.model.NodeInfoResponseP1;

import com.clustercontrol.ClusterControlPlugin;
import com.clustercontrol.bean.DataRangeConstant;
import com.clustercontrol.bean.RequiredFieldColorConstant;
import com.clustercontrol.bean.SizeConstant;
import com.clustercontrol.composite.action.NumberVerifyListener;
import com.clustercontrol.dialog.ScopeTreeDialog;
import com.clustercontrol.dialog.ValidateResult;
import com.clustercontrol.fault.FacilityNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.RestConnectFailed;
import com.clustercontrol.jobmanagement.bean.ResourceJobActionMessage;
import com.clustercontrol.jobmanagement.bean.ResourceJobConstant;
import com.clustercontrol.jobmanagement.util.JobDialogUtil;
import com.clustercontrol.repository.FacilityPath;
import com.clustercontrol.repository.util.FacilityTreeItemResponse;
import com.clustercontrol.repository.util.RepositoryRestClientWrapper;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.xcloud.bean.CloudConstant;
import com.clustercontrol.xcloud.model.cloud.ICloudScope;
import com.clustercontrol.xcloud.model.cloud.IExtendedProperty;
import com.clustercontrol.xcloud.model.cloud.IInstance;
import com.clustercontrol.xcloud.model.cloud.ILocation;
import com.clustercontrol.xcloud.model.cloud.IStorage;
import com.clustercontrol.xcloud.plugin.job.StorageTreeDialog;

public class ResourceComposite extends Composite {

	/** 対象スコープにファシリティID(String)をセットする際のキー（目印として使用） */
	private static final String KEY_FACILITY = "facility";

	/** 対象スコープで指定したファシリティがスコープかどうか */
	// findbugs対応 staticである必要はないので クラスローカル化
	private boolean isScope = false;
	
	/** 項目名ラベルの長さ */
	private static final int ITEM_LABEL_WIDTH = 140;

	/** 項目名ラベルのインデント長さ */
	private static final int ITEM_LABEL_INDENT = 30;

	/** ロガー */
	private static Log m_log = LogFactory.getLog(ResourceComposite.class);

	/** コンピュートノード選択ラジオボタン */
	private Button m_computeRadio = null;

	/** コンピュートノード - コンピュートノードアクション選択コンボボックス */
	private Combo m_computeActionCombo = null;

	/** コンピュートノード - 制御対象 - スコープ指定選択ラジオボタン */
	private Button m_targetScopeRadio = null;

	/** コンピュートノード - 制御対象 - スコープ指定選択ラジオボタン */
	private Button m_targetComputeRadio = null;

	/** コンピュートノード - 制御対象 - クラウドスコープ選択コンボボックス */
	private Combo m_computeCloudScopeCombo = null;

	/** コンピュートノード - 制御対象 - ロケーション選択コンボボックス */
	private Combo m_computeTargetLocationCombo = null;

	/** コンピュートノード - 制御対象 - スコープテキスト */
	private Text m_computeTargetScopeText = null;

	/** コンピュートノード - 制御対象 - コンピュートIDテキスト */
	private Text m_computeTargetComputeText = null;

	/** コンピュートノード - 制御対象 - スコープ 参照ボタン */
	private Button m_computeTargetScopeButton = null;

	/** コンピュートノード - 状態確認期間 入力用テキスト */
	private Text m_computeComfirmTimeText = null;

	/** コンピュートノード - 状態確認間隔 入力用テキスト */
	private Text m_computeComfirmIntervalText = null;

	/** ストレージ選択 ラジオボタン */
	private Button m_storageRadio = null;

	/** ストレージ - ストレージアクション選択 コンボボックス */
	private Combo m_storageActionCombo = null;

	/** ストレージ - 対象ストレージ テキスト */
	private Text m_storageTargetText = null;

	/** ストレージ - 対象ストレージ 参照ボタン */
	private Button m_storageTargetButton = null;

	/** ストレージ - アタッチ先ノード 選択コンボボックス */
	private Combo m_storageAttachNodeCombo = null;

	/** ストレージ - アタッチ先デバイス 入力用テキスト */
	private Text m_storageAttachDeviceText = null;

	/** 通知 - 通知先スコープ テキスト */
	private Text m_notifyScopeText = null;

	/** 通知 - 通知先スコープ 参照ボタン */
	private Button m_notifyScopeButton = null;

	/** 終了値 - 実行成功 入力用テキスト */
	private Text m_endSuccessText = null;

	/** 終了値 - 実行失敗 入力用テキスト */
	private Text m_endFailureText = null;

	/** リソース制御ジョブ情報 */
	private JobResourceInfoResponse m_resource = null;

	/** マネージャ名 */
	private String m_managerName = null;

	/** オーナーロールID */
	private String m_ownerRoleId = null;

	/** リポジトリRestAPIラッパー */
	private RepositoryRestClientWrapper m_repositoryWrapper = null;

	/** シェル */
	private Shell m_shell = null;

	public ResourceComposite(Composite parent, int style, String managerName) {
		super(parent, style);
		this.m_managerName = managerName;
		this.m_repositoryWrapper = RepositoryRestClientWrapper.getWrapper(managerName);
		initialize();
		m_shell = this.getShell();
	}

	/**
	 * リソース制御ジョブ情報を取得
	 */
	public JobResourceInfoResponse getResourceJobInfo() {
		return m_resource;
	}

	/**
	 * リソース制御ジョブ情報をセット
	 */
	public void setResourceJobInfo(JobResourceInfoResponse resource) {
		this.m_resource = resource;
	}

	/**
	 * オーナーロールIDを取得
	 */
	public String getOwnerRoleId() {
		return m_ownerRoleId;
	}

	/**
	 * オーナーロールIDをセット
	 */
	public void setOwnerRoleId(String ownerRoleI) {
		this.m_ownerRoleId = ownerRoleI;
	}

	/**
	 * コンポジットを配置する
	 */
	private void initialize() {
		Label label = null;
		this.setLayout(JobDialogUtil.getParentLayout());
		// リソース・アクション入力グループ
		Group jobSelectGroup = new Group(this, SWT.NONE);
		jobSelectGroup.setLayout(new GridLayout(3, false));

		// コンピュートノード選択（ラジオボタン）
		m_computeRadio = new Button(jobSelectGroup, SWT.RADIO);
		m_computeRadio.setText(Messages.getString("word.compute"));
		m_computeRadio.setLayoutData(new GridData(200, SizeConstant.SIZE_BUTTON_HEIGHT));
		((GridData) m_computeRadio.getLayoutData()).horizontalSpan = 3;
		m_computeRadio.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Button check = (Button) e.getSource();
				if (check.getSelection()) {
					m_storageRadio.setSelection(false);
				}
				update();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		// リソース・制御対象入力グループ
		Group targetGroup = new Group(jobSelectGroup, SWT.FILL);
		targetGroup.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 3, 1));
		GridLayout layaout = new GridLayout(6, false);
		layaout.verticalSpacing = 6;
		layaout.horizontalSpacing = SWT.FILL;
		targetGroup.setLayout(layaout);
		targetGroup.setText(Messages.getString("resource.job.target.type"));

		// コンピュートノード：制御対象：スコープ指定選択（ラジオボタン）
		m_targetScopeRadio = new Button(targetGroup, SWT.RADIO);
		m_targetScopeRadio.setText(Messages.getString("target.type.scope"));
		m_targetScopeRadio.setLayoutData(new GridData(100, SizeConstant.SIZE_BUTTON_HEIGHT));
		((GridData) m_targetScopeRadio.getLayoutData()).horizontalSpan = 3;
		m_targetScopeRadio.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// findbugs対応 不要メソッド e.getSource()削除
				updateLocationCombo();
				update();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
		// コンピュートノード：制御対象：コンピュートID指定選択（ラジオボタン）
		m_targetComputeRadio = new Button(targetGroup, SWT.RADIO);
		m_targetComputeRadio.setText(Messages.getString("target.type.compute"));
		m_targetComputeRadio.setLayoutData(new GridData(100, SizeConstant.SIZE_BUTTON_HEIGHT));
		((GridData) m_targetComputeRadio.getLayoutData()).horizontalSpan = 3;
		m_targetComputeRadio.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// findbugs対応 不要メソッド e.getSource()削除
				updateLocationCombo();
				update();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		// コンピュートノード：制御対象：スコープ（ラベル）
		label = new Label(targetGroup, SWT.NONE);
		label.setText(Messages.getString("word.scope") + " : ");
		label.setLayoutData(new GridData(ITEM_LABEL_WIDTH, SizeConstant.SIZE_LABEL_HEIGHT));
		((GridData) label.getLayoutData()).horizontalSpan = 2;
		((GridData) label.getLayoutData()).horizontalIndent = ITEM_LABEL_INDENT;

		// コンピュートノード：制御対象：スコープ（テキスト）
		m_computeTargetScopeText = new Text(targetGroup, SWT.BORDER | SWT.READ_ONLY);
		m_computeTargetScopeText.setLayoutData(new GridData(200, SizeConstant.SIZE_TEXT_HEIGHT));
		((GridData) m_computeTargetScopeText.getLayoutData()).horizontalSpan = 2;

		// コンピュートノード：制御対象：スコープ（参照ボタン）
		m_computeTargetScopeButton = new Button(targetGroup, SWT.NONE);
		m_computeTargetScopeButton.setText(Messages.getString("refer"));
		m_computeTargetScopeButton.setLayoutData(new GridData(80, SizeConstant.SIZE_BUTTON_HEIGHT));
		((GridData) m_computeTargetScopeButton.getLayoutData()).horizontalSpan = 2;
		m_computeTargetScopeButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ScopeTreeDialog dialog = new ScopeTreeDialog(m_shell, m_managerName, m_ownerRoleId);
				
				String facilityId = "";
				NodeInfoResponseP1 node = null;
				while(true) {
					if (dialog.open() != IDialogConstants.OK_ID) {
						break;
					}
					FacilityTreeItemResponse selectedCompute = dialog.getSelectItem();

					isScope = (selectedCompute.getData().getFacilityType() == FacilityTypeEnum.SCOPE);
					facilityId = selectedCompute.getData().getFacilityId();
					if (isScope) {
						break;
					}
					try{
						node = getNodeInfo(facilityId);
					} catch (InvalidRole exeption) {
						MessageDialog.openInformation(null, Messages.getString("message"),
								Messages.getString("message.accesscontrol.16"));
						continue;
					} catch (Exception exeption) {
						m_log.warn("getInstance(), " + exeption.getMessage(), exeption);
						MessageDialog.openError(null, Messages.getString("failed"),
								Messages.getString("message.hinemos.failure.unexpected") + ", "
										+ HinemosMessage.replace(exeption.getMessage()));
						continue;
					}
					if ((node.getCloudScope() == null || node.getCloudScope().isEmpty())) {
						MessageDialog.openInformation(null, Messages.getString("message"),
								Messages.getString("message.job.196"));
					} else {
						break;
					}
				}
				if (facilityId == null || facilityId.isEmpty()) {
					return;
				}

				FacilityTreeItemResponse selectedCompute = dialog.getSelectItem();
				FacilityPath path = new FacilityPath(ClusterControlPlugin.getDefault().getSeparator());

				// 対象クラウドスコープ コンボボックスの更新
				updateCloudScopeCombo();
				m_computeTargetScopeText.setText(path.getPath(selectedCompute));
				m_computeTargetScopeText.setData(KEY_FACILITY, facilityId);

				if (!isScope) {
					m_computeCloudScopeCombo.setText(node.getCloudScope());
				}
				updateComputeActionCombo();
				update();
			}
		});

		// コンピュートノード：制御対象：クラウドスコープ（ラベル）
		label = new Label(targetGroup, SWT.NONE);
		label.setText(Messages.getString("cloud.scope") + " : ");
		label.setLayoutData(new GridData(ITEM_LABEL_WIDTH, SizeConstant.SIZE_LABEL_HEIGHT));
		((GridData) label.getLayoutData()).horizontalSpan = 2;
		((GridData) label.getLayoutData()).horizontalIndent = ITEM_LABEL_INDENT;

		// コンピュートノード：制御対象：クラウドスコープ（コンボ）
		m_computeCloudScopeCombo = new Combo(targetGroup, SWT.CENTER | SWT.READ_ONLY);
		m_computeCloudScopeCombo.setLayoutData(new GridData(200, SizeConstant.SIZE_COMBO_HEIGHT));
		((GridData) m_computeCloudScopeCombo.getLayoutData()).horizontalSpan = 4;
		m_computeCloudScopeCombo.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// findbugs対応 不要メソッド e.getSource()削除
				updateLocationCombo();
				updateComputeActionCombo();
				update();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		// コンピュートノード：制御対象：ロケーション（ラベル）
		label = new Label(targetGroup, SWT.NONE);
		label.setText(Messages.getString("word.location") + " : ");
		label.setLayoutData(new GridData(ITEM_LABEL_WIDTH, SizeConstant.SIZE_LABEL_HEIGHT));
		((GridData) label.getLayoutData()).horizontalSpan = 2;
		((GridData) label.getLayoutData()).horizontalIndent = ITEM_LABEL_INDENT;

		// コンピュートノード:制御対象：ロケーション（コンボ）
		m_computeTargetLocationCombo = new Combo(targetGroup, SWT.CENTER | SWT.READ_ONLY);
		m_computeTargetLocationCombo.setLayoutData(new GridData(200, SizeConstant.SIZE_COMBO_HEIGHT));
		((GridData) m_computeTargetLocationCombo.getLayoutData()).horizontalSpan = 4;

		/// コンピュートノード：制御対象：コンピュートID（ラベル）
		label = new Label(targetGroup, SWT.NONE);
		label.setText(Messages.getString("word.compute.id") + " : ");
		label.setLayoutData(new GridData(ITEM_LABEL_WIDTH, SizeConstant.SIZE_LABEL_HEIGHT));
		((GridData) label.getLayoutData()).horizontalSpan = 2;
		((GridData) label.getLayoutData()).horizontalIndent = ITEM_LABEL_INDENT;

		// コンピュートノード：制御対象：コンピュートID（テキスト）
		m_computeTargetComputeText = new Text(targetGroup, SWT.BORDER);
		m_computeTargetComputeText.setLayoutData(new GridData(200, SizeConstant.SIZE_TEXT_HEIGHT));
		((GridData) m_computeTargetComputeText.getLayoutData()).horizontalSpan = 2;
		m_computeTargetComputeText.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				update();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		// アクション（ラベル）
		label = new Label(jobSelectGroup, SWT.NONE);
		label.setText(Messages.getString("action") + " : ");
		label.setLayoutData(new GridData(ITEM_LABEL_WIDTH, SizeConstant.SIZE_LABEL_HEIGHT));
		((GridData) label.getLayoutData()).horizontalIndent = ITEM_LABEL_INDENT;

		// コンピュートノード：アクション選択（コンボ）
		m_computeActionCombo = new Combo(jobSelectGroup, SWT.CENTER | SWT.READ_ONLY);
		m_computeActionCombo.setLayoutData(new GridData(200, SizeConstant.SIZE_COMBO_HEIGHT));
		m_computeActionCombo.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// findbugs対応 不要メソッド e.getSource()削除
				update();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		// カラム数を合わせるための dummy
		new Label(jobSelectGroup, SWT.LEFT);

		// コンピュートノード：状態確認期間（ラベル）
		label = new Label(jobSelectGroup, SWT.NONE);
		label.setText(Messages.getString("status.confirm.time") + " : ");
		label.setLayoutData(new GridData(ITEM_LABEL_WIDTH, SizeConstant.SIZE_LABEL_HEIGHT));
		((GridData) label.getLayoutData()).horizontalIndent = ITEM_LABEL_INDENT;

		// コンピュートノード：状態確認期間（テキスト）
		m_computeComfirmTimeText = new Text(jobSelectGroup, SWT.BORDER);
		m_computeComfirmTimeText.setLayoutData(new GridData(200, SizeConstant.SIZE_TEXT_HEIGHT));
		m_computeComfirmTimeText.addVerifyListener(
				new NumberVerifyListener(0, DataRangeConstant.INTEGER_HIGH));
		m_computeComfirmTimeText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		// 秒（ラベル）
		label = new Label(jobSelectGroup, SWT.NONE);
		label.setText(Messages.getString("second"));
		label.setLayoutData(new GridData(70, SizeConstant.SIZE_LABEL_HEIGHT));

		// コンピュートノード：状態確認間隔（ラベル）
		label = new Label(jobSelectGroup, SWT.NONE);
		label.setText(Messages.getString("status.confirm.interval") + " : ");
		label.setLayoutData(new GridData(ITEM_LABEL_WIDTH, SizeConstant.SIZE_LABEL_HEIGHT));
		((GridData) label.getLayoutData()).horizontalIndent = ITEM_LABEL_INDENT;

		// コンピュートノード：状態確認間隔（テキスト）
		m_computeComfirmIntervalText = new Text(jobSelectGroup, SWT.BORDER);
		m_computeComfirmIntervalText.setLayoutData(new GridData(200, SizeConstant.SIZE_TEXT_HEIGHT));
		m_computeComfirmIntervalText.addVerifyListener(
				new NumberVerifyListener(0, DataRangeConstant.INTEGER_HIGH));
		m_computeComfirmIntervalText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		// 秒（ラベル）
		label = new Label(jobSelectGroup, SWT.NONE);
		label.setText(Messages.getString("second"));
		label.setLayoutData(new GridData(70, SizeConstant.SIZE_LABEL_HEIGHT));

		// ストレージ選択（ラジオボタン）
		m_storageRadio = new Button(jobSelectGroup, SWT.RADIO);
		m_storageRadio.setText(Messages.getString("word.storage"));
		m_storageRadio.setLayoutData(new GridData(200, SizeConstant.SIZE_BUTTON_HEIGHT));
		((GridData) m_storageRadio.getLayoutData()).horizontalSpan = 3;
		((GridData) m_storageRadio.getLayoutData()).verticalIndent = 15;
		m_storageRadio.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Button check = (Button) e.getSource();
				if (check.getSelection()) {
					m_computeRadio.setSelection(false);
				}
				update();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		// AWS、VMが有効でない場合、ストレージは無効とする
		boolean isStorageEnabled = Arrays
				.asList(ClusterControlPlugin.getDefault().getHinemosManager(m_managerName).getCloudPlatforms()).stream()
				.anyMatch(p -> CloudConstant.platform_AWS.equals(p.getId())
						|| CloudConstant.platform_vCenter.equals(p.getId())
						|| CloudConstant.platform_ESXi.equals(p.getId()));
		m_storageRadio.setEnabled(isStorageEnabled);

		// ストレージ：対象ストレージ（ラベル）
		label = new Label(jobSelectGroup, SWT.NONE);
		label.setText(Messages.getString("target.storage") + " : ");
		label.setLayoutData(new GridData(ITEM_LABEL_WIDTH, SizeConstant.SIZE_LABEL_HEIGHT));
		((GridData) label.getLayoutData()).horizontalIndent = ITEM_LABEL_INDENT;

		// ストレージ：対象ストレージ（テキスト）
		m_storageTargetText = new Text(jobSelectGroup, SWT.BORDER | SWT.READ_ONLY);
		m_storageTargetText.setLayoutData(new GridData(200, SizeConstant.SIZE_TEXT_HEIGHT));

		// ストレージ：対象ストレージ（参照ボタン）
		m_storageTargetButton = new Button(jobSelectGroup, SWT.NONE);
		m_storageTargetButton.setText(Messages.getString("refer"));
		m_storageTargetButton.setLayoutData(new GridData(80, SizeConstant.SIZE_BUTTON_HEIGHT));
		m_storageTargetButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				StorageTreeDialog dialog = new StorageTreeDialog(m_shell, m_managerName);
				if (dialog.open() == IDialogConstants.OK_ID) {
					IStorage selectItem = dialog.getSelectItem();
					// 表示用にStorageIDをセット
					m_storageTargetText.setText(createStorageNameForDisplay(selectItem));
					// 内部データとしてStorage情報をセット
					m_storageTargetText.setData(selectItem);
					updateStorageActionCombo(selectItem);
					updateAttachNodeCombo(selectItem);
					update();
				}
			}
		});

		// アクション（ラベル）
		label = new Label(jobSelectGroup, SWT.NONE);
		label.setText(Messages.getString("action") + " : ");
		label.setLayoutData(new GridData(ITEM_LABEL_WIDTH, SizeConstant.SIZE_LABEL_HEIGHT));
		((GridData) label.getLayoutData()).horizontalIndent = ITEM_LABEL_INDENT;

		// ストレージ：アクション選択（コンボ）
		m_storageActionCombo = new Combo(jobSelectGroup, SWT.CENTER | SWT.READ_ONLY);
		m_storageActionCombo.setLayoutData(new GridData(200, SizeConstant.SIZE_COMBO_HEIGHT));
		m_storageActionCombo.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// findbugs対応 不要メソッド e.getSource()削除
				update();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		// カラム数を合わせるための dummy
		new Label(jobSelectGroup, SWT.LEFT);

		// ストレージ：アタッチ先ノード（ラベル）
		label = new Label(jobSelectGroup, SWT.NONE);
		label.setText(Messages.getString("attach.node") + " : ");
		label.setLayoutData(new GridData(ITEM_LABEL_WIDTH, SizeConstant.SIZE_LABEL_HEIGHT));
		((GridData) label.getLayoutData()).horizontalIndent = ITEM_LABEL_INDENT;

		// ストレージ：アタッチ先ノード（コンボ）
		m_storageAttachNodeCombo = new Combo(jobSelectGroup, SWT.CENTER | SWT.READ_ONLY);
		m_storageAttachNodeCombo.setLayoutData(new GridData(200, SizeConstant.SIZE_COMBO_HEIGHT));
		m_storageAttachNodeCombo.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// findbugs対応 不要メソッド e.getSource()削除
				update();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		// カラム数を合わせるための dummy
		new Label(jobSelectGroup, SWT.LEFT);

		// ストレージ：アタッチ先デバイス（ラベル）
		label = new Label(jobSelectGroup, SWT.NONE);
		label.setText(Messages.getString("attach.device") + " : ");
		label.setLayoutData(new GridData(ITEM_LABEL_WIDTH, SizeConstant.SIZE_LABEL_HEIGHT));
		((GridData) label.getLayoutData()).horizontalIndent = ITEM_LABEL_INDENT;

		// ストレージ：アタッチ先デバイス（テキスト）
		m_storageAttachDeviceText = new Text(jobSelectGroup, SWT.BORDER);
		m_storageAttachDeviceText.setLayoutData(new GridData(200, SizeConstant.SIZE_TEXT_HEIGHT));
		m_storageAttachDeviceText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		// カラム数を合わせるための dummy
		new Label(jobSelectGroup, SWT.LEFT);

		// 通知（グループ）
		Group notifyGroup = new Group(this, SWT.NONE);
		notifyGroup.setText(Messages.getString("notify"));
		notifyGroup.setLayout(new GridLayout(3, false));

		// 通知：通知先スコープ（ラベル）
		label = new Label(notifyGroup, SWT.NONE);
		label.setText(Messages.getString("notification.scope") + " : ");
		label.setLayoutData(new GridData(ITEM_LABEL_WIDTH, SizeConstant.SIZE_LABEL_HEIGHT));
		((GridData) label.getLayoutData()).horizontalIndent = ITEM_LABEL_INDENT;

		// 通知：通知先スコープ（テキスト）
		m_notifyScopeText = new Text(notifyGroup, SWT.BORDER | SWT.READ_ONLY);
		m_notifyScopeText.setLayoutData(new GridData(200, SizeConstant.SIZE_TEXT_HEIGHT));

		// 通知：通知先スコープ（参照ボタン）
		m_notifyScopeButton = new Button(notifyGroup, SWT.NONE);
		m_notifyScopeButton.setText(Messages.getString("refer"));
		m_notifyScopeButton.setLayoutData(new GridData(80, SizeConstant.SIZE_BUTTON_HEIGHT));
		m_notifyScopeButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ScopeTreeDialog dialog = new ScopeTreeDialog(m_shell, m_managerName, m_ownerRoleId);
				if (dialog.open() == IDialogConstants.OK_ID) {
					FacilityTreeItemResponse selectItem = dialog.getSelectItem();
					FacilityPath path = new FacilityPath(ClusterControlPlugin.getDefault().getSeparator());
					// 表示用にパス名セット
					m_notifyScopeText.setText(path.getPath(selectItem));
					// 内部データとしてFacilityIDセット
					m_notifyScopeText.setData(selectItem.getData().getFacilityId());
					update();
				}
			}
		});

		// 終了値（グループ）
		Group endGroup = new Group(this, SWT.NONE);
		endGroup.setText(Messages.getString("end.value"));
		endGroup.setLayout(new GridLayout(2, false));

		// 終了値：実行成功（ラベル）
		label = new Label(endGroup, SWT.NONE);
		label.setText(Messages.getString("run.success") + " : ");
		label.setLayoutData(new GridData(ITEM_LABEL_WIDTH, SizeConstant.SIZE_LABEL_HEIGHT));
		((GridData) label.getLayoutData()).horizontalIndent = ITEM_LABEL_INDENT;

		// 終了値：実行成功（テキスト）
		m_endSuccessText = new Text(endGroup, SWT.BORDER);
		m_endSuccessText.setLayoutData(new GridData(200, SizeConstant.SIZE_TEXT_HEIGHT));
		m_endSuccessText.addVerifyListener(
				new NumberVerifyListener(DataRangeConstant.SMALLINT_LOW, DataRangeConstant.SMALLINT_HIGH));
		m_endSuccessText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		// 終了値：実行失敗（ラベル）
		label = new Label(endGroup, SWT.NONE);
		label.setText(Messages.getString("run.error") + " : ");
		label.setLayoutData(new GridData(ITEM_LABEL_WIDTH, SizeConstant.SIZE_LABEL_HEIGHT));
		((GridData) label.getLayoutData()).horizontalIndent = ITEM_LABEL_INDENT;

		// 終了値：実行失敗（テキスト）
		m_endFailureText = new Text(endGroup, SWT.BORDER);
		m_endFailureText.setLayoutData(new GridData(200, SizeConstant.SIZE_TEXT_HEIGHT));
		m_endFailureText.addVerifyListener(
				new NumberVerifyListener(DataRangeConstant.SMALLINT_LOW, DataRangeConstant.SMALLINT_HIGH));
		m_endFailureText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});
	}

	/**
	 * リソース制御ジョブ情報をコンポジットに反映する
	 */
	public void reflectResourceJobInfo() {

		// 初期値セット
		m_computeRadio.setSelection(true);
		m_targetScopeRadio.setSelection(true);
		m_storageRadio.setSelection(false);
		m_computeTargetScopeText.setText("");
		m_computeComfirmTimeText.setText("" + ResourceJobConstant.STATUS_CONFIRM_TIME);
		m_computeComfirmIntervalText.setText("" + ResourceJobConstant.STATUS_CONFIRM_INTERVAL);
		m_storageTargetText.setText("");
		m_storageAttachDeviceText.setText("");
		m_notifyScopeText.setText("");
		m_endSuccessText.setText("" + ResourceJobConstant.SUCCESS_VALUE);
		m_endFailureText.setText("" + ResourceJobConstant.FAILURE_VALUE);
		updateCloudScopeCombo();

		if (m_resource == null) {
			return;
		}

		// 対象リソースの選択反映
		switch (m_resource.getResourceType()) {
		case COMPUTE_COMPUTE_ID:
			m_targetComputeRadio.setSelection(true);
			m_targetScopeRadio.setSelection(false);
			break;
		case COMPUTE_FACILITY_ID:
			break;
		case STORAGE:
			m_computeRadio.setSelection(false);
			m_storageRadio.setSelection(true);
			break;
		}

		if (m_computeRadio.getSelection()) {
			try {
				m_computeCloudScopeCombo.setText(m_resource.getResourceCloudScopeId());
				
				if (m_resource.getResourceType() == ResourceTypeEnum.COMPUTE_COMPUTE_ID) {
					// コンピュート
					m_computeTargetComputeText.setText(m_resource.getResourceTargetId());
					updateLocationCombo();
					m_computeTargetLocationCombo.setText(m_resource.getResourceLocationId());
				} else if (m_resource.getResourceType() == ResourceTypeEnum.COMPUTE_FACILITY_ID) {
					// スコープ ノード
					m_computeTargetScopeText.setText(getFacilityPath(m_resource.getResourceTargetId()));
					m_computeTargetScopeText.setData(KEY_FACILITY, m_resource.getResourceTargetId());
					isScope = isScope(m_resource.getResourceTargetId());
				}

				// コンピュートノードへのアクション
				updateComputeActionCombo();
				m_computeActionCombo.setText(ResourceJobActionMessage.typeEnumToString(m_resource.getResourceAction()));

				// 状態確認期間
				m_computeComfirmTimeText.setText(m_resource.getResourceStatusConfirmTime().toString());

				// 状態確認間隔
				m_computeComfirmIntervalText.setText(m_resource.getResourceStatusConfirmInterval().toString());

			} catch (InvalidRole e) {
				MessageDialog.openInformation(null, Messages.getString("message"),
						Messages.getString("message.accesscontrol.16"));
			} catch (Exception e) {
				m_log.warn("reflectResourceJobInfo() getFacilityPathByTargetScope, " + e.getMessage(), e);
				MessageDialog.openError(null, Messages.getString("failed"),
						Messages.getString("message.hinemos.failure.unexpected") + ", "
								+ HinemosMessage.replace(e.getMessage()));
			}

		} else if (m_storageRadio.getSelection()) {
			// 選択ストレージ情報を取得
			IStorage[] storages = ClusterControlPlugin.getDefault().getHinemosManager(m_managerName).getCloudScopes()
					.getCloudScope(m_resource.getResourceCloudScopeId()).getLocation(m_resource.getResourceLocationId())
					.getComputeResources().getStorages();
			String storageId = m_resource.getResourceTargetId();
			IStorage storageData = Arrays.asList(storages).stream()
					.filter(storage -> storageId.equals(storage.getId())).findFirst().get();

			// 対象ストレージ
			m_storageTargetText.setText(createStorageNameForDisplay(storageData));
			m_storageTargetText.setData(storageData);

			// ストレージへのアクション
			updateStorageActionCombo(storageData);
			m_storageActionCombo.setText(ResourceJobActionMessage.typeEnumToString(m_resource.getResourceAction()));

			if (m_resource.getResourceAction() == JobResourceInfoResponse.ResourceActionEnum.ATTACH) {
				// アタッチ先ノード
				String attachNode = updateAttachNodeCombo(storageData, m_resource.getResourceAttachNode());
				m_storageAttachNodeCombo.setText(attachNode);

				// アタッチ先デバイス
				if (m_resource.getResourceAttachDevice() != null) {
					m_storageAttachDeviceText.setText(m_resource.getResourceAttachDevice());
				}
			}
		}

		try {
			// 通知先スコープ
			m_notifyScopeText.setText(getFacilityPath(m_resource.getResourceNotifyScope()));
			m_notifyScopeText.setData(m_resource.getResourceNotifyScope());
		} catch (InvalidRole e) {
			MessageDialog.openInformation(null, Messages.getString("message"),
					Messages.getString("message.accesscontrol.16"));
		} catch (Exception e) {
			m_log.warn("reflectResourceJobInfo() getFacilityPathByNotifyScope, " + e.getMessage(), e);
			MessageDialog.openError(null, Messages.getString("failed"),
					Messages.getString("message.hinemos.failure.unexpected") + ", "
							+ HinemosMessage.replace(e.getMessage()));
		}

		// 終了値（成功）
		m_endSuccessText.setText(m_resource.getResourceSuccessValue().toString());

		// 終了値（失敗）
		m_endFailureText.setText(m_resource.getResourceFailureValue().toString());
	}

	/**
	 * コンポジットの情報から、リソース制御ジョブ情報を作成・入力値検証を行う
	 * @return 入力値の検証結果 （異常なしの場合は null を返す）
	 */
	public ValidateResult createResourceJobInfo() {
		ValidateResult result = null;
		m_resource = new JobResourceInfoResponse();

		// 対象コンピュート
		if (m_computeRadio.getSelection()) {
			
			if (m_targetComputeRadio.getSelection()) {
				if (m_computeTargetLocationCombo.getSelectionIndex() != -1) {
					m_resource.setResourceLocationId(m_computeTargetLocationCombo.getText());
				} else {
					return createErrorResult(Messages.getString("message.job.194"));
				}
				if (!m_computeTargetComputeText.getText().equals("")) {
					m_resource.setResourceTargetId(m_computeTargetComputeText.getText());
				} else {
					return createErrorResult(Messages.getString("message.job.195"));
				}
				// リソース種別
				m_resource.setResourceType(JobResourceInfoResponse.ResourceTypeEnum.COMPUTE_COMPUTE_ID);
			}

			// 対象スコープ（インスタンス指定か、スコープ指定かで分岐）
			if (m_targetScopeRadio.getSelection()) {
				if (!m_computeTargetScopeText.getText().equals("")) {
					m_resource.setResourceTargetId((String) m_computeTargetScopeText.getData(KEY_FACILITY));
					m_resource.setResourceType(JobResourceInfoResponse.ResourceTypeEnum.COMPUTE_FACILITY_ID);
				} else {
					return createErrorResult(Messages.getString("message.job.176"));
				}
			}

			// コンピュートノードの対象クラウドスコープ
			if (m_computeCloudScopeCombo.getSelectionIndex() != -1) {
				m_resource.setResourceCloudScopeId(m_computeCloudScopeCombo.getText());
			} else {
				return createErrorResult(Messages.getString("message.job.177"));
			}

			// コンピュートノードのアクション
			if (m_computeActionCombo.getSelectionIndex() != -1) {
				m_resource.setResourceAction(ResourceJobActionMessage.typeStringToEnum(m_computeActionCombo.getText()));
			} else {
				return createErrorResult(Messages.getString("message.job.178"));
			}

			// 状態確認期間
			try {
				m_resource.setResourceStatusConfirmTime(Integer.parseInt(m_computeComfirmTimeText.getText()));
			} catch (NumberFormatException e) {
				return createErrorResult(Messages.getString("message.job.179"));
			}

			// 状態確認間隔
			try {
				m_resource.setResourceStatusConfirmInterval(Integer.parseInt(m_computeComfirmIntervalText.getText()));
			} catch (NumberFormatException e) {
				return createErrorResult(Messages.getString("message.job.180"));
			}

		} else {

			// リソース種別
			m_resource.setResourceType(JobResourceInfoResponse.ResourceTypeEnum.STORAGE);

			// ストレージのアクション
			if (ResourceJobActionMessage.typeStringToEnum(m_storageActionCombo.getText()) != null) {
				m_resource.setResourceAction(ResourceJobActionMessage.typeStringToEnum(m_storageActionCombo.getText()));
			} else {
				return createErrorResult(Messages.getString("message.job.178"));
			}

			// 対象ストレージ
			if (!m_storageTargetText.getText().equals("")) {
				IStorage strageData = (IStorage) m_storageTargetText.getData();
				m_resource.setResourceTargetId(strageData.getId());
				m_resource.setResourceCloudScopeId(strageData.getCloudScopeId());
				m_resource.setResourceLocationId(strageData.getLocationId());
			} else {
				return createErrorResult(Messages.getString("message.job.181"));
			}

			// ストレージアクションがアタッチの場合
			if (m_resource.getResourceAction() == JobResourceInfoResponse.ResourceActionEnum.ATTACH) {

				// アタッチ先ノード
				if (m_storageAttachNodeCombo.getSelectionIndex() != -1) {
					IInstance instance = (IInstance) m_storageAttachNodeCombo.getData(m_storageAttachNodeCombo.getText());
					m_resource.setResourceAttachNode(instance.getId());
				} else {
					return createErrorResult(Messages.getString("message.job.182"));
				}

				// アタッチ先デバイス（AWSの場合のみチェック）
				IStorage storage = (IStorage) m_storageTargetText.getData();
				if (storage.getCloudScope().getPlatformId().equals(CloudConstant.platform_AWS)) {
					if (!m_storageAttachDeviceText.getText().equals("")) {
						m_resource.setResourceAttachDevice(m_storageAttachDeviceText.getText());
					} else {
						return createErrorResult(Messages.getString("message.job.183"));
					}
				}
			}
		}

		// 通知先スコープ
		if (!m_notifyScopeText.getText().equals("")) {
			m_resource.setResourceNotifyScopePath(m_notifyScopeText.getText());
			m_resource.setResourceNotifyScope((String) m_notifyScopeText.getData());
		} else {
			return createErrorResult(Messages.getString("message.job.184"));
		}

		// 実行成功値
		try {
			m_resource.setResourceSuccessValue(Integer.parseInt(m_endSuccessText.getText()));
		} catch (NumberFormatException e) {
			return createErrorResult(Messages.getString("message.job.185"));
		}

		// 実行失敗値
		try {
			m_resource.setResourceFailureValue(Integer.parseInt(m_endFailureText.getText()));
		} catch (NumberFormatException e) {
			return createErrorResult(Messages.getString("message.job.186"));
		}
		return result;
	}

	/**
	 * 画面操作時の共通更新処理（必須欄の色変更、有効無効切り替えなど）
	 */
	@Override
	public void update() {
		updateRequiredColor();
		setEnabled(true);
	}

	/**
	 * タブ全体の入力有効・無効を設定する（読み込み専用時はfalse渡しでグレーアウト）
	 */
	@Override
	public void setEnabled(boolean enabled) {
		// コンピュートノード、ストレージ共通
		m_computeRadio.setEnabled(enabled);
		m_storageRadio.setEnabled(enabled);
		m_computeTargetScopeText.setEditable(false);
		m_storageTargetText.setEditable(false);

		// コンピュートノードがジョブ対象の場合
		if (m_computeRadio.getSelection()) {
			m_computeTargetScopeButton.setEnabled(enabled && m_targetScopeRadio.getSelection());
			m_targetScopeRadio.setEnabled(enabled);
			m_targetComputeRadio.setEnabled(enabled);
			if (m_targetScopeRadio.getSelection() && !m_computeTargetScopeText.getText().isEmpty()) {
				//スコープが選択されているか
				m_computeCloudScopeCombo.setEnabled(enabled && isScope);
			} else {
				m_computeCloudScopeCombo.setEnabled(enabled);
			}
			m_computeTargetLocationCombo.setEnabled(enabled && m_targetComputeRadio.getSelection());
			m_computeTargetComputeText.setEditable(enabled && m_targetComputeRadio.getSelection());
			m_computeActionCombo.setEnabled(enabled);
			if (ResourceJobActionMessage.STRING_SNAPSHOT.equals(m_computeActionCombo.getText())) {
				m_computeComfirmTimeText.setEditable(false);
				m_computeComfirmIntervalText.setEditable(false);
			} else {
				m_computeComfirmTimeText.setEditable(enabled);
				m_computeComfirmIntervalText.setEditable(enabled);
			}
			m_storageTargetButton.setEnabled(false);
			m_storageActionCombo.setEnabled(false);
			m_storageAttachNodeCombo.setEnabled(false);
			m_storageAttachDeviceText.setEditable(false);
		}

		// ストレージがジョブ対象の場合
		if (m_storageRadio.getSelection()) {
			m_computeTargetScopeButton.setEnabled(false);
			m_targetScopeRadio.setEnabled(false);
			m_targetComputeRadio.setEnabled(false);
			m_computeCloudScopeCombo.setEnabled(false);
			m_computeTargetLocationCombo.setEnabled(false);
			m_computeTargetComputeText.setEnabled(false);
			m_computeActionCombo.setEnabled(false);
			m_computeComfirmTimeText.setEditable(false);
			m_computeComfirmIntervalText.setEditable(false);
			m_storageTargetButton.setEnabled(enabled);
			m_storageActionCombo.setEnabled(enabled);

			int index = m_storageActionCombo.getSelectionIndex();
			if (index != -1 && ResourceJobActionMessage.STRING_ATTACH.equals(m_storageActionCombo.getItem(index))) {
				m_storageAttachNodeCombo.setEnabled(enabled);

				// アタッチ先デバイスはAWSのみ有効
				IStorage storage = (IStorage) m_storageTargetText.getData();
				if (storage.getCloudScope().getPlatformId().equals(CloudConstant.platform_AWS)) {
					m_storageAttachDeviceText.setEditable(enabled);
				} else {
					m_storageAttachDeviceText.setEditable(false);
				}
			} else {
				m_storageAttachNodeCombo.setEnabled(false);
				m_storageAttachDeviceText.setEditable(false);
			}
		}

		// 通知
		m_notifyScopeText.setEditable(false);
		m_notifyScopeButton.setEnabled(enabled);

		// 終了値
		m_endSuccessText.setEditable(enabled);
		m_endFailureText.setEditable(enabled);
	}

	/**
	 * 入力状態により、必須項目の背景色を変更する（必須項目を明示する）
	 */
	private void updateRequiredColor() {
		// コンピュートノード：制御対象：スコープ
		if (m_computeRadio.getSelection() &&
				m_targetScopeRadio.getSelection() &&
				"".equals(m_computeTargetScopeText.getText())) {
			m_computeTargetScopeText.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		} else {
			m_computeTargetScopeText.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}

		// コンピュートノード：制御対象：クラウドスコープ
		if (m_computeCloudScopeCombo.getEnabled() && m_computeCloudScopeCombo.getSelectionIndex() == -1) {
			m_computeCloudScopeCombo.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		} else {
			m_computeCloudScopeCombo.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}

		// コンピュートノード：制御対象：ロケーション
		if (m_computeTargetLocationCombo.getEnabled() && m_computeTargetLocationCombo.getSelectionIndex() == -1) {
			m_computeTargetLocationCombo.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		} else {
			m_computeTargetLocationCombo.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}

		// コンピュートノード：制御対象：コンピュートID
		if (m_computeRadio.getSelection() &&
				m_targetComputeRadio.getSelection() &&
				"".equals(m_computeTargetComputeText.getText())) {
			m_computeTargetComputeText.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		} else {
			m_computeTargetComputeText.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}

		// コンピュートノード：アクション
		if (m_computeRadio.getSelection() && m_computeActionCombo.getSelectionIndex() == -1) {
			m_computeActionCombo.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		} else {
			m_computeActionCombo.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}

		// コンピュートノード：状態確認期間
		if (m_computeRadio.getSelection() && !ResourceJobActionMessage.STRING_SNAPSHOT.equals(m_computeActionCombo.getText()) &&
				"".equals(m_computeComfirmTimeText.getText())) {
			m_computeComfirmTimeText.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		} else {
			m_computeComfirmTimeText.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}

		// コンピュートノード：状態確認間隔
		if (m_computeRadio.getSelection() && !ResourceJobActionMessage.STRING_SNAPSHOT.equals(m_computeActionCombo.getText()) &&
				"".equals(m_computeComfirmIntervalText.getText())) {
			m_computeComfirmIntervalText.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		} else {
			m_computeComfirmIntervalText.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}

		// ストレージ：対象ストレージ
		if (m_storageRadio.getSelection() && "".equals(m_storageTargetText.getText())) {
			m_storageTargetText.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		} else {
			m_storageTargetText.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}

		// ストレージ：アクション
		if (m_storageRadio.getSelection() && m_storageActionCombo.getSelectionIndex() == -1) {
			m_storageActionCombo.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		} else {
			m_storageActionCombo.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}

		// ストレージ：アタッチ先ノード
		if (m_storageRadio.getSelection()) {
			// ストレージのアタッチが選択されている場合のみ必須判定
			int index = m_storageActionCombo.getSelectionIndex();
			if (index != -1 && m_storageActionCombo.getItem(index).equals(ResourceJobActionMessage.STRING_ATTACH)
					&& m_storageAttachNodeCombo.getSelectionIndex() == -1) {
				m_storageAttachNodeCombo.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
			} else {
				m_storageAttachNodeCombo.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
			}
		}

		// ストレージ：アタッチ先デバイス名
		if (m_storageRadio.getSelection()) {
			// AWSストレージのアタッチが選択されている場合のみ必須判定
			IStorage storage = (IStorage) m_storageTargetText.getData();
			if (storage != null && storage.getCloudScope().getPlatformId().equals(CloudConstant.platform_AWS)) {
				int index = m_storageActionCombo.getSelectionIndex();
				if (index != -1 && m_storageActionCombo.getItem(index).equals(ResourceJobActionMessage.STRING_ATTACH)
						&& "".equals(m_storageAttachDeviceText.getText())) {
					m_storageAttachDeviceText.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
				} else {
					m_storageAttachDeviceText.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
				}
			}
		}

		// 通知先スコープ
		if ("".equals(m_notifyScopeText.getText())) {
			m_notifyScopeText.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		} else {
			m_notifyScopeText.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}

		// 終了値：実行成功
		if ("".equals(m_endSuccessText.getText())) {
			m_endSuccessText.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		} else {
			m_endSuccessText.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}

		// 終了値：実行失敗
		if ("".equals(m_endFailureText.getText())) {
			m_endFailureText.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		} else {
			m_endFailureText.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
	}

	/**
	 * 選択された「クラウドスコープ」によって、ロケーションリスト更新する<BR>
	 */
	private void updateLocationCombo() {
		// 事前チェック
		if (m_computeCloudScopeCombo.getText().equals("")) {
			return;
		}

		m_computeTargetLocationCombo.removeAll();
		String cloudScopeId = m_computeCloudScopeCombo.getText();
		ILocation[] locations = ClusterControlPlugin.getDefault().getHinemosManager(m_managerName).getCloudScopes()
				.getCloudScope(cloudScopeId).getLocations();

		for (ILocation location : locations) {
			m_computeTargetLocationCombo.setData(location.getId(), location.getName() +"("+location.getId()+")");
			m_computeTargetLocationCombo.add(location.getId());
		}

		if (!ClusterControlPlugin.getDefault().getHinemosManager(m_managerName).getCloudScopes()
				.getCloudScope(cloudScopeId).getCloudPlatform().getCloudSpec().getPublicCloud()) {
			m_computeTargetLocationCombo.select(0);
		}
	}

	/**
	 * 選択された「対象スコープ」と「対象クラウドスコープ」によって、アクションリスト更新する<BR>
	 */
	private void updateComputeActionCombo() {

		// 事前チェック
		if (m_computeCloudScopeCombo.getText().equals("")) {
			return;
		}

		// アクションの選択肢をクリア＆ジョブ対象のクラウド情報取得
		m_computeActionCombo.removeAll();
		String cloudScopeId = m_computeCloudScopeCombo.getText();
		String platformId = ClusterControlPlugin.getDefault().getHinemosManager(m_managerName).getCloudScopes()
				.getCloudScope(cloudScopeId).getPlatformId();
		
		boolean suspendEnabled = true;
		boolean snapshotEnabled = true;

		// スコープ指定の場合は、スナップショット不可
		if (isScope) {
			snapshotEnabled = false;
		}

		// 対象クラウドスコープのプラットフォームで実行不可なアクションの無効にする
		if (CloudConstant.platform_AWS.equals(platformId)) {
			suspendEnabled = false;
		} else if (CloudConstant.platform_Azure.equals(platformId)) {
			suspendEnabled = false;
			snapshotEnabled = false;
		} else if (CloudConstant.platform_HyperV.equals(platformId)) {
			snapshotEnabled = false;
		} else if (CloudConstant.platform_ESXi.equals(platformId)) {
			snapshotEnabled = false;
		}

		// プラットフォームに対して可能なアクションのみコンボボックスに表示
		m_computeActionCombo.add(ResourceJobActionMessage.STRING_POWERON);
		m_computeActionCombo.add(ResourceJobActionMessage.STRING_POWEROFF);
		m_computeActionCombo.add(ResourceJobActionMessage.STRING_REBOOT);
		if (suspendEnabled) {
			m_computeActionCombo.add(ResourceJobActionMessage.STRING_SUSPEND);
		}
		if (snapshotEnabled) {
			m_computeActionCombo.add(ResourceJobActionMessage.STRING_SNAPSHOT);
		}
	}

	/**
	 * 選択された「対象ストレージ」のプラットフォームによって、アクションリスト更新する
	 */
	private void updateStorageActionCombo(IStorage selectedStorage) {

		// アクションの選択肢をクリア＆ジョブ対象情報取得
		m_storageActionCombo.removeAll();

		m_storageActionCombo.add(ResourceJobActionMessage.STRING_ATTACH);
		m_storageActionCombo.add(ResourceJobActionMessage.STRING_DETACH);
		m_storageActionCombo.add(ResourceJobActionMessage.STRING_SNAPSHOT);
	}

	/**
	 * 選択されたストレージから、アタッチできるインスタンスをノード選択コンボボックスに追加
	 * @param selectedStorage
	 */
	private void updateAttachNodeCombo(IStorage selectedStorage) {
		updateAttachNodeCombo(selectedStorage, null);
	}

	/**
	 * 選択されたストレージから、アタッチできるインスタンスをノード選択コンボボックスに追加<BR>
	 * @param selectedStorage ストレージ情報
	 * @param attachInstanceId アタッチ先ノードのインスタンスID（※無い場合はnullで良い）
	 * @return 引数attachInstanceIdに対応する表示用テキストを返却
	 */
	private String updateAttachNodeCombo(IStorage selectedStorage, String attachInstanceId) {
		m_storageAttachNodeCombo.removeAll();
		String platformId = selectedStorage.getCloudScope().getPlatformId();
		String returnId = null;

		// プラットフォームがAWSかVMかで異なる（各ClientOptionプロジェクトのAttachStorageDialogクラスと同じ処理）
		if (CloudConstant.platform_AWS.equals(platformId)) {
			String availabilityZone = selectedStorage.getExtendedProperty("aws_availabilityZone");
			for (IInstance instance : selectedStorage.getCloudComputeManager().getInstances()) {
				for (IExtendedProperty prop : instance.getExtendedProperties()) {
					if (!prop.getName().equals("aws_availabilityZone")) {
						continue;
					}
					if (prop.getValue().equals(availabilityZone)) {
						String label = String.format("%s (%s)", instance.getName(), instance.getId());
						m_storageAttachNodeCombo.add(label);
						m_storageAttachNodeCombo.setData(label, instance);
						if (instance.getId().equals(attachInstanceId)) {
							returnId = label;
						}
					}
				}
			}
		} else if (CloudConstant.platform_vCenter.equals(platformId)
				|| CloudConstant.platform_ESXi.equals(platformId)) {
			List<String> hostmounts = selectedStorage.getExtendedPropertyAsList("vmware_host_mount");
			for (IInstance instance : selectedStorage.getCloudComputeManager().getInstances()) {
				String host = instance.getExtendedProperty("vmware_host");
				for (String hostmount : hostmounts) {
					if (host.equals(hostmount)) {
						String label = String.format("%s (%s)", instance.getName(), instance.getFacilityId());
						m_storageAttachNodeCombo.add(label);
						m_storageAttachNodeCombo.setData(label, instance);
						if (instance.getId().equals(attachInstanceId)) {
							returnId = label;
						}
					}
				}
			}
		}
		return returnId;
	}

	/**
	 * 対象クラウドスコープの選択肢を最新に更新する
	 */
	private void updateCloudScopeCombo() {
		m_computeCloudScopeCombo.removeAll();
		ICloudScope[] cloudScopes = ClusterControlPlugin.getDefault().getHinemosManager(m_managerName).getCloudScopes().getCloudScopes();
		for (ICloudScope cloudScope : cloudScopes) {
			m_computeCloudScopeCombo.add(cloudScope.getId());
		}
	}

	/**
	 * 画面表示用のファシリティパス名を取得
	 * @param facilityId
	 * @return
	 * @throws HinemosUnknown 
	 * @throws RestConnectFailed 
	 * @throws InvalidRole 
	 * @throws InvalidUserPass 
	 */
	private String getFacilityPath(String facilityId)
			throws InvalidUserPass, InvalidRole, RestConnectFailed, HinemosUnknown {
		String Path = m_repositoryWrapper.getFacilityPath(facilityId, null).getFacilityPath();
		return HinemosMessage.replace(Path);
	}

	/**
	 * 画面表示用のストレージ名を取得<BR>
	 * ストレージ名がある場合：StorageName(StorageID)<BR>
	 * ストレージ名がない場合：StorageID
	 * @return
	 */
	private String createStorageNameForDisplay(IStorage storage) {
		String displayName;
		if (storage.getName() != null && !storage.getName().equals("")) {
			displayName = storage.getName() + "(" + storage.getId() + ")";
		} else {
			displayName = storage.getId();
		}
		return displayName;
	}

	/**
	 * ノードを取得
	 * @param facilityId
	 * @return NodeInfoResponseP1
	 * @throws HinemosUnknown 
	 * @throws RestConnectFailed 
	 * @throws InvalidRole 
	 * @throws InvalidUserPass 
	 * @throws FacilityNotFound 
	 */
	private NodeInfoResponseP1 getNodeInfo(String facilityId)
			throws FacilityNotFound, InvalidUserPass, InvalidRole, RestConnectFailed, HinemosUnknown {
		NodeInfoResponseP1 nodeInfo = m_repositoryWrapper.getNode(facilityId);
		return nodeInfo;
	}

	/**
	 * ノードかどうか確認
	 * @param facilityId
	 * @return Boolean
	 * @throws HinemosUnknown 
	 * @throws RestConnectFailed 
	 * @throws InvalidRole 
	 * @throws InvalidUserPass 
	 * @throws FacilityNotFound 
	 */
	private Boolean isScope(String facilityId)
			throws FacilityNotFound, InvalidUserPass, InvalidRole, RestConnectFailed, HinemosUnknown {
		return !m_repositoryWrapper.isNode(facilityId).getIsNode();
	}

	/**
	 * 検証結果エラーとなるValidateResultを生成
	 * @param message ValidateResult.setMessage()にセットする文字列
	 * @return
	 */
	private ValidateResult createErrorResult(String message) {
		ValidateResult result = new ValidateResult();
		result.setValid(false);
		result.setID(Messages.getString("message.hinemos.1"));
		result.setMessage(message);
		return result;
	}
}
