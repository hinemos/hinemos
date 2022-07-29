/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.composite;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.openapitools.client.model.JobQueueResponse;

import com.clustercontrol.dialog.ApiResultDialog;
import com.clustercontrol.jobmanagement.util.JobRestClientWrapper;
import com.clustercontrol.util.WidgetTestUtil;


/**
 * ジョブキュー選択用のドロップダウンリストです。
 *
 * @since 6.2.0
 */
public class JobQueueDropdown extends Composite {

	// キューの名前がこの文字数を超える場合、それ以降の部分は"..."へ置換して表示する
	private static final int MAX_NAME_LENGTH = 32;
	
	// 選択肢の表示フォーマット (1:ID, 2:名前)
	private static final String TEXT_FORMAT = "%2$s(%1$s)";

	private static final Log log = LogFactory.getLog(JobQueueDropdown.class);

	// Key:ID, Value:表示テキスト
	private Map<String, String> dispMap = new ConcurrentHashMap<>(); 

	private Combo combo;
	
	/**
	 * インスタンスを返します。
	 *
	 * @param parent 親のコンポジット
	 * @param style スタイル
	 */
	public JobQueueDropdown(Composite parent, int style) {
		super(parent, style);
		initialize(parent);
	}

	private void initialize(Composite parent) {
		// 変数として利用されるグリッドデータ
		GridData gridData = null;

		GridLayout layout = new GridLayout(1, true);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.numColumns = 10;
		setLayout(layout);

		combo = new Combo(this, SWT.DROP_DOWN | SWT.READ_ONLY);
		WidgetTestUtil.setTestId(this, null, combo);
		gridData = new GridData();
		gridData.horizontalSpan = 5;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		combo.setLayoutData(gridData);
	}

	@Override
	public void update() {
	}

	/**
	 * 指定されたマネージャとロールIDを元に、参照可能なジョブキューのリストを設定します。
	 *
	 * @param managerName マネージャ
	 * @param roleId ロールID
	 */
	public void refreshList(String managerName, String roleId){
		dispMap.clear();
		combo.removeAll();

		// 一覧情報を取得
		List<JobQueueResponse> list = null;
		ApiResultDialog errorDialog = new ApiResultDialog();
		try {
			JobRestClientWrapper wrapper = JobRestClientWrapper.getWrapper(managerName);
			list = wrapper.getJobQueueList(roleId);
		} catch (Throwable t) {
			errorDialog.addFailure(managerName, t, "");
			log.warn("setupList: " + t.getClass().getName() + ", " + t.getMessage());
		}

		// エラーメッセージ表示(エラーがあれば)
		errorDialog.show();

		// コンボボックスの選択肢を設定する
		combo.add("");  // 無選択用の空欄
		if (list != null) {
			for (JobQueueResponse item : list) {
				String id = item.getQueueId();
				String name = item.getName();
				if (name.length() > MAX_NAME_LENGTH) {
					name = name.substring(0, MAX_NAME_LENGTH) + "...";
				}
				
				String disp = String.format(TEXT_FORMAT, id, name);
				dispMap.put(id, disp);
				combo.add(disp);
			}
		}
	}

	@Override
	public void setEnabled(boolean enabled) {
		combo.setEnabled(enabled);
	}

	@Override
	public boolean getEnabled() {
		return combo.getEnabled();
	}

	/**
	 * 選択されているジョブキューのIDを返します。
	 */
	public String getQueueId() {
		String selectedText = combo.getText();
		if (StringUtils.isEmpty(selectedText)) {
			return "";
		}
		for (Map.Entry<String, String> entry : dispMap.entrySet()) {
			if (entry.getValue().equals(selectedText)) {
				return entry.getKey();
			}
		}
		// ここに到達するのは、setQueueIdでリストにないIDが指定された場合のはず。
		// リストにないIDをユーザが選択、あるいは初期表示のまま、ジョブを登録しようとした状況と考えられる。
		// 選択肢から当該IDを除去して空欄にして、選択が無効であることを明確にする。
		combo.remove(selectedText);
		combo.setText("");
		log.warn("getQueueId: Unknown QueueId is specified. queueId=" + selectedText);
		return "";
	}

	/**
	 * 指定されたジョブキューIDが選択された状態にします。
	 * 
	 * @param queueId ジョブキューID。
	 */
	public void setQueueId(String queueId) {
		if (StringUtils.isEmpty(queueId)) {
			return;
		}

		if (!dispMap.keySet().contains(queueId)) {
			// リスト上に存在しないIDが指定された。
			// ジョブ定義から参照されているジョブキューは削除不可＆参照権限削除不可であるため、
			// ここへ到達することは考えにくい。
			// オーナーロールが変化する可能性のあるジョブ定義コピーではジョブキューの入力はクリアされるため、
			// やはりここへは到達しない。
			// よって特別な対応は不要だと考えられるが、万が一到達してしまった場合に
			// ユーザへ与える情報の量を減らさないよう、IDのみ表示する。
			combo.add(queueId);
			combo.setText(queueId);
			log.info("setQueueId: Unknown QueueId is specified. queueId=" + queueId);
			return;
		}

		combo.setText(dispMap.get(queueId));
	}
}
