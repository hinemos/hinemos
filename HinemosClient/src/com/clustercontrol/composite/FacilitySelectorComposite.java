/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.composite;

import java.util.Optional;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.openapitools.client.model.FacilityInfoResponse;

import com.clustercontrol.ClusterControlPlugin;
import com.clustercontrol.dialog.ScopeTreeDialog;
import com.clustercontrol.repository.FacilityPath;
import com.clustercontrol.repository.util.FacilityTreeItemResponse;
import com.clustercontrol.repository.util.ScopePropertyUtil;
import com.clustercontrol.util.FacilityTreeCache;
import com.clustercontrol.util.FacilityTreeItemUtil;
import com.clustercontrol.util.Messages;

/**
 * {@link ScopeTreeDialog} を利用してHinemosリポジトリから特定ファシリティを選択することのできるコンポジットです。
 */
public class FacilitySelectorComposite extends Composite {
	private static final Log m_log = LogFactory.getLog(FacilitySelectorComposite.class);

	private FacilityTreeItemResponse selectedItem;
	private String selectedManager;

	private Text txtName;
	private Button btnRefer;
	private Button btnClear;

	/**
	 * 特定マネージャから選択する場合はそのマネージャ名、nullなら全マネージャ。
	 * {@link #ownerRoleIdOrNull}が!nullの場合は、こちらも!nullでなければならない。
	 * nullのときの動作詳細については{@link FacilityTreeComposite#update()}を参照。
	 */
	private String managerNameOrNull;

	/**
	 * 特定オーナーロールで選択範囲を制限する場合はそのID、nullならユーザが所属する全ロール。
	 * nullのときの動作詳細については{@link FacilityTreeComposite#update()}を参照。
	 */
	private String ownerRoleIdOrNull;

	private boolean scopeOnly;
	private boolean includesUnregistered;

	public FacilitySelectorComposite(Composite parent) {
		super(parent, SWT.NONE);
		selectedItem = null;
		selectedManager = null;

		GridLayout layout = new GridLayout(3, false);
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		setLayout(layout);

		// ファシリティ名表示テキスト
		txtName = new Text(this, SWT.BORDER | SWT.LEFT | SWT.READ_ONLY);
		txtName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		// 参照ボタン
		btnRefer = new Button(this, SWT.NONE);
		btnRefer.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
		btnRefer.setText("  " + Messages.getString("refer") + "  ");

		btnRefer.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// スコープツリーダイアログを開く
				Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
				ScopeTreeDialog dialog = new ScopeTreeDialog(shell, managerNameOrNull, ownerRoleIdOrNull, scopeOnly, includesUnregistered);
				if (dialog.open() == IDialogConstants.OK_ID) {
					// 選択結果を反映する
					FacilityTreeItemResponse item = dialog.getSelectItem();
					if (item != null) {
						// ツリーを遡ってマネージャノードを取得
						FacilityTreeItemResponse manager = ScopePropertyUtil.getManager(item);
						if (manager != null) {
							// 取得できればそれを使う
							select(item, manager.getData().getFacilityId());
						} else {
							// 選択に使用したファシリティツリーがマネージャノードを含んでいない場合は、
							// 特定のマネージャ配下のツリーであると考えられる。
							// この場合は、フィールドのマネージャ名がその特定マネージャである。
							if (managerNameOrNull != null) {
								select(item, managerNameOrNull);
							} else { // ありえないはずだが念のため
								m_log.warn("widgetSelected: Manager not found. facilityId=" + item.getData().getFacilityId());
								unselect();
							}
						}
					} else { // null になることはないはずだが念のため
						m_log.warn("widgetSelected: item is null.");
						unselect();
					}
				}
			}
		});

		// クリアボタン
		btnClear = new Button(this, SWT.NONE);
		btnClear.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
		btnClear.setText(Messages.getString("string.select.clear"));

		btnClear.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				unselect();
			}
		});

		// 初期状態は選択不可
		disable();
	}

	/**
	 * ファシリティを選択できる状態にします。<br/>
	 * すでに選択中であった場合は、選択を解除します。<br/>
	 * ログインユーザの接続している全マネージャの所属ロールから参照可能な範囲で選択します。
	 * 
	 * @param scopeOnly スコープのみ選択するなら true、ノードも含むなら false。
	 * @param includesUnregistered 未登録ノードを参照範囲に含むなら true、含まないなら false。
	 */
	public void enable(boolean scopeOnly, boolean includesUnregistered) {
		enable(null, null, scopeOnly, includesUnregistered);
	}

	/**
	 * ファシリティを選択できる状態にします。<br/>
	 * すでに選択中であった場合は、選択を解除します。<br/>
	 * ログインユーザの所属ロールから参照可能な範囲で選択します。
	 * 
	 * @param managerName ファシリティツリーの参照範囲を限定するマネージャ名。
	 * @param scopeOnly スコープのみ選択するなら true、ノードも含むなら false。
	 * @param includesUnregistered 未登録ノードを参照範囲に含むなら true、含まないなら false。
	 */
	public void enable(String managerName, boolean scopeOnly, boolean includesUnregistered) {
		enable(managerName, null, scopeOnly, includesUnregistered);
	}

	/**
	 * ファシリティを選択できる状態にします。
	 * すでに選択中であった場合は、選択を解除します。
	 * 
	 * @param managerName ファシリティツリーの参照範囲を限定するマネージャ名。
	 * @param ownerRoleId ファシリティツリーの参照範囲を限定するオーナーロールID。
	 * @param scopeOnly スコープのみ選択するなら true、ノードも含むなら false。
	 * @param includesUnregistered 未登録ノードを参照範囲に含むなら true、含まないなら false。
	 */
	public void enable(String managerName, String ownerRoleId, boolean scopeOnly, boolean includesUnregistered) {
		unselect();

		this.managerNameOrNull = managerName;
		this.ownerRoleIdOrNull = ownerRoleId;
		this.scopeOnly = scopeOnly;
		this.includesUnregistered = includesUnregistered;

		btnRefer.setEnabled(true);
		btnClear.setEnabled(true);
	}

	/**
	 * ファシリティを選択できない状態にします。
	 */
	public void disable() {
		btnRefer.setEnabled(false);
		btnClear.setEnabled(false);
	}

	/**
	 * 何も選択されていない場合に表示するメッセージを設定します。
	 */
	public void setHintMessage(String hintMessage) {
		txtName.setMessage(hintMessage);
	}

	/**
	 * 指定されたファシリティを選択した状態にします。
	 * 該当するファシリティが存在しない場合は何も選択されていない状態にします。
	 */
	public void select(String managerName, String facilityId) {
		// マネージャのスコープツリーを走査して、ファシリティIDが一致するものを探す
		FacilityTreeItemResponse found = FacilityTreeItemUtil.visitTreeItems(
				FacilityTreeCache.getTreeItem(managerName),
				item -> {
					String fid = item.getData().getFacilityId();
					if (fid != null && fid.equals(facilityId)) {
						return item;
					}
					return null;
				});
		if (found == null) {
			unselect();  // 見つからなかったら選択なし状態
		} else {
			select(found, managerName);
		}
	}

	/**
	 * 指定されたファシリティを選択した状態にします。
	 */
	private void select(FacilityTreeItemResponse item, String managerName) {
		selectedItem = item;
		selectedManager = managerName;

		// 選択されたのがノードならファシリティ名を、スコープならパスをテキストへ表示
		FacilityInfoResponse info = selectedItem.getData();
		if (info.getFacilityType() == FacilityInfoResponse.FacilityTypeEnum.NODE) {
			txtName.setText(info.getFacilityName());
		} else {
			FacilityPath path = new FacilityPath(ClusterControlPlugin.getDefault().getSeparator());
			txtName.setText(path.getPath(selectedItem));
		}
	}

	/**
	 * 何も選択されていない状態にします。
	 */
	public void unselect() {
		selectedItem = null;
		selectedManager = null;
		txtName.setText("");
	}

	/**
	 * 選択中のファシリティを返します。
	 */
	public Optional<FacilityTreeItemResponse> getSelectedItem() {
		return Optional.ofNullable(selectedItem);
	}

	/**
	 * 選択中のファシリティが所属するマネージャの名前を返します。
	 */
	public Optional<String> getSelecedManager() {
		return Optional.ofNullable(selectedManager);
	}

}
