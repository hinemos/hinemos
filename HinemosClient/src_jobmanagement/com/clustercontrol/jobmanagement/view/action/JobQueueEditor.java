/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.view.action;

import java.util.Collections;
import java.util.List;

/**
 * ジョブキューの編集(参照:{@link ShowJobQueueAction}、変更:{@link ModifyJobQueueAction}、
 * 削除:{@link DeleteJobQueueAction}、複製:{@link CopyJobQueueAction})を実行するビューが
 * 実装すべきインターフェイスです。<br/>
 * ジョブキューの編集は行わないが、編集されたことを検知したいという場合は、抽象クラス{@link JobQueueEditObserver}の
 * 実装を参考にしてください。
 * <p>
 * 本インターフェスは、編集コマンドから特定ビューへの依存性を排除します。
 */
public interface JobQueueEditor {
	/**
	 * 編集対象とマークされている(通常はビュー上で選択状態にある)ジョブキューを返します。
	 * 編集対象が単一である、参照・変更・複製操作で使用します。
	 * 編集対象が存在しない場合は、emptyを返します。
	 */
	JobQueueEditTarget getJobQueueEditTarget();
	
	/**
	 * 編集対象とマークされている(通常はビュー上で選択状態にある)ジョブキューを返します。
	 * 編集対象が複数である、削除操作で使用します。
	 * 編集対象が存在しない場合は、空のリストを返します。
	 */
	List<JobQueueEditTarget> getJobQueueEditTargets();

	/**
	 * 編集対象とマークされている(通常はビュー上で選択状態にある)ジョブキューの件数を返します。
	 */
	int getSelectedJobQueueCount();

	/**
	 * 編集が行われた後に、コマンドから呼び出されるコールバックです。
	 * 通常は、ビューの表示更新を行い、編集後の状態を画面へ反映させるのに使用します。
	 */
	void onJobQueueEdited();
	
	/**
	 * ジョブキューの編集時に、編集対象に関する情報をやり取りするためのクラスです。
	 */
	public static class JobQueueEditTarget {
		public static JobQueueEditTarget empty = new JobQueueEditTarget();
		
		private String managerName;
		private String queueId;

		private JobQueueEditTarget() {
		}
		
		public JobQueueEditTarget(String managerName, String queueId) {
			this.managerName = managerName;
			this.queueId = queueId;
		}

		public String getManagerName() {
			return managerName;
		}

		public String getQueueId() {
			return queueId;
		}
		
		public boolean isEmpty() {
			return this == empty;
		}
	}

	public static abstract class JobQueueEditObserver implements JobQueueEditor {
		@Override
		public JobQueueEditTarget getJobQueueEditTarget() {
			return JobQueueEditTarget.empty;
		}

		@Override
		public List<JobQueueEditTarget> getJobQueueEditTargets() {
			return Collections.emptyList();
		}

		@Override
		public int getSelectedJobQueueCount() {
			return 0;
		}
	}
}
