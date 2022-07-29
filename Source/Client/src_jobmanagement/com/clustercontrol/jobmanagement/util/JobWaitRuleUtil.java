/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.jobmanagement.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.openapitools.client.model.JobNextJobOrderInfoResponse;
import org.openapitools.client.model.JobObjectGroupInfoResponse;
import org.openapitools.client.model.JobObjectInfoResponse;

import com.clustercontrol.dialog.ValidateResult;
import com.clustercontrol.util.Messages;

/**
 * ジョブ待ち条件用オブジェクトについてのユーティリティクラス
 *
 */
public class JobWaitRuleUtil {

	/**
	 * 待ち条件群の重複チェックします
	 *
	 * @param list
	 *            待ち条件群リスト
	 * @return 入力値の検証結果
	 */
	public static ValidateResult validateWaitGroup(List<JobObjectGroupInfoResponse> list) {
		ValidateResult result = null;
		// targetとして処理した待ち条件群を格納する
		Set<JobObjectGroupInfoResponse> decisionSet = new HashSet<JobObjectGroupInfoResponse>();

		// target 比較対象の待ち条件群
		for (JobObjectGroupInfoResponse target : list) {
			boolean notUnique = false;
			int targetSize = target.getJobObjectList().size();

			// 待ち条件群の重複チェック(待ち条件の順不同対応)
			for (JobObjectGroupInfoResponse decisions : decisionSet) {
				// targetと同じ要素数の場合のみ比較
				if (targetSize != decisions.getJobObjectList().size()) {
					continue;
				}

				// 重複の判定に説明とジョブ名は含まないため、コピーを控え全て空欄にする
				// ジョブ名については登録後設定される。
				String[] copyDescList = new String[decisions.getJobObjectList().size()];
				String[] copyJobNameList = new String[decisions.getJobObjectList().size()];

				// 待ち条件群リストの説明とジョブ名のコピーを控える
				for (int i = 0; i < decisions.getJobObjectList().size(); i++) {
					copyDescList[i] = decisions.getJobObjectList().get(i).getDescription();
					copyJobNameList[i] = decisions.getJobObjectList().get(i).getJobName();
					decisions.getJobObjectList().get(i).setDescription(null);
					decisions.getJobObjectList().get(i).setJobName(null);
				}

				int matchCount = 0;
				for (JobObjectInfoResponse wait : target.getJobObjectList()) {
					// 判定対象の説明とジョブ名のコピーを控える
					String cDesc = wait.getDescription();
					String cJobName = wait.getJobName();
					wait.setDescription(null);
					wait.setJobName(null);

					if (decisions.getJobObjectList().contains(wait)) {
						matchCount++;
					}
					wait.setDescription(cDesc);
					wait.setJobName(cJobName);
				}
				// 要素数分一致した場合はユニークではない
				notUnique = targetSize == matchCount;
				// コピーしたものを元に戻す
				for (int i = 0; i < decisions.getJobObjectList().size(); i++) {
					decisions.getJobObjectList().get(i).setDescription(copyDescList[i]);
					decisions.getJobObjectList().get(i).setJobName(copyJobNameList[i]);
				}
				if (notUnique) {
					// 重複した要素があったのでループ終了
					break;
				}
			}
			if (notUnique) {
				result = new ValidateResult();
				result.setValid(false);
				result.setID(Messages.getString("message.hinemos.1"));
				if (targetSize == 1) {
					// 要素数が1の場合は単体なので「待ち条件」表記
					result.setMessage(
							Messages.getString("message.common.16", new String[] { Messages.getString("wait.rule") }));
				} else {
					result.setMessage(Messages.getString("message.common.16",
							new String[] { Messages.getString("job.wait.group") }));
				}
				return result;
			} else {
				decisionSet.add(target);
			}
		}
		return result;
	}

	/**
	 * 待ち条件の重複チェックします
	 *
	 * @param info 
	 * @param infoList
	 * @return 入力値の検証結果
	 */
	public static ValidateResult validateWait(List<JobObjectInfoResponse> infoList) {
		if (infoList.isEmpty()) {
			return null;
		}
		ValidateResult result = null;
		// 重複の判定に説明は含まないため、コピーを控え全て空欄にする
		String[] copyList = new String[infoList.size()];
		for (int i = 0; i < infoList.size(); i++) {
			copyList[i] = infoList.get(i).getDescription();
			infoList.get(i).setDescription(null);
		}
		// targetとして処理した待ち条件群を格納する
		Set<JobObjectInfoResponse> decisionSet = new HashSet<JobObjectInfoResponse>();

		for(JobObjectInfoResponse target : infoList){
			if (decisionSet.contains(target)) {
				result = new ValidateResult();
				result.setValid(false);
				result.setID(Messages.getString("message.hinemos.1"));
				result.setMessage(
						Messages.getString("message.common.16", new String[] { Messages.getString("wait.rule") }));
			} else {
				decisionSet.add(target);
			}
		}

		// コピーしたものを元に戻す
		for (int i = 0; i < infoList.size(); i++) {
			infoList.get(i).setDescription(copyList[i]);
		}
		return result;
	}

	/**
	 * 待ち条件リストから選択したタイプの重複があるかチェックします
	 * 
	 * @param infoList
	 * @param type
	 * @return
	 */
	public static boolean typeUniqueCheck(List<JobObjectInfoResponse> infoList, JobObjectInfoResponse.TypeEnum type) {
		int count = 0;
		for (JobObjectInfoResponse i : infoList) {
			if (i.getType() == type) {
				count++;
			}
		}
		if (count > 1) {
			return false;
		}
		return true;
	}

	/**
	 * ジョブの待ち条件等の変更に従い、ジョブの後続ジョブ実行設定を更新する。
	 * 
	 * @param treeItem 更新対象のジョブツリー
	 * @param nextJobOrderList ジョブの後続ジョブ実行設定リスト、nullの場合はtreeItemの値を直接変更する
	 */
	public static void updateNextJobOrderInfo(JobTreeItemWrapper treeItem, List<JobNextJobOrderInfoResponse> nextJobOrderList) {

		JobInfoWrapper jobInfo = treeItem.getData();

		boolean infoUpdateFlag = false;
		int oldSize = 0;

		if (nextJobOrderList == null) {
			infoUpdateFlag = true;
			nextJobOrderList = jobInfo.getWaitRule().getExclusiveBranchNextJobOrderList();
			oldSize = nextJobOrderList.size();
		}
		//このジョブを待ち条件としているジョブIDリスト
		List<String> nextJobIdList = new ArrayList<>();
		//待ち条件が削除されたジョブを除外するために使用
		List<String> notNextJobIdList = new ArrayList<>();
		JobTreeItemWrapper parent= treeItem.getParent();
		if (parent != null) {
			//同一階層のジョブリスト
			List<JobTreeItemWrapper> siblingJobList = parent.getChildren();
			List<String> siblingJobIdList = siblingJobList.stream().map(tmpTreeItem -> tmpTreeItem.getData().getId()).collect(Collectors.toList());
			//新規に作成された後続ジョブを表示するために使用する
			for (JobTreeItemWrapper sibling : siblingJobList) {
				if (sibling == treeItem) {
					continue;
				}
				//Full Jobで情報が取得されていないジョブはスキップ
				JobInfoWrapper siblingJobInfo = sibling.getData();
				if (siblingJobInfo.getWaitRule() == null) {
					continue;
				}
				List<JobObjectGroupInfoResponse> siblingWaitJobObjectGroupInfoList = siblingJobInfo.getWaitRule().getObjectGroup();
				boolean isExists = false;
				if (siblingWaitJobObjectGroupInfoList != null) {
					for (JobObjectGroupInfoResponse objectGroup : siblingWaitJobObjectGroupInfoList) {
						if (objectGroup.getJobObjectList() == null) {
							continue;
						}
						for (JobObjectInfoResponse objectInfo : objectGroup.getJobObjectList()) {
							//同じ階層のジョブの中でこのジョブを待ち条件としているもの
							if ((objectInfo.getType() == JobObjectInfoResponse.TypeEnum.JOB_END_STATUS ||
									objectInfo.getType() == JobObjectInfoResponse.TypeEnum.JOB_END_VALUE ||
									objectInfo.getType() == JobObjectInfoResponse.TypeEnum.JOB_RETURN_VALUE) &&
									objectInfo.getJobId().equals(treeItem.getData().getId())) {
								nextJobIdList.add(sibling.getData().getId());
								isExists = true;
								break;
							} 
						}
						if (isExists) {
							break;
						}
					}
				}
				//このジョブを待ち条件としていないジョブ
				if (!nextJobIdList.contains(sibling.getData().getId())) {
					notNextJobIdList.add(sibling.getData().getId());
				}
			}
			//同一階層ジョブにないものは優先度設定から除く(削除されたジョブ)
			nextJobOrderList.removeIf(nextJobOrder -> !siblingJobIdList.contains(nextJobOrder.getNextJobId()));
			//後続ジョブでないジョブは優先度設定から除く（待ち条件が削除された場合など）
			nextJobOrderList.removeIf(nextJobOrder -> notNextJobIdList.contains(nextJobOrder.getNextJobId()));
			if (infoUpdateFlag && nextJobOrderList.size() != oldSize) {
				jobInfo.setWaitRuleChanged(true);
			}
		}

		//新規に待ち条件に追加されたジョブも反映する
		//既に優先度設定がある後続ジョブIDリスト
		List<String> nextJobOrderJobIdList = nextJobOrderList.stream()
											.map(nextJobOrder -> nextJobOrder.getNextJobId()).collect(Collectors.toList());
		for (String nextJobId: nextJobIdList) {
			//後続ジョブに優先度設定がなければ下位の優先度として追加する
			if (!nextJobOrderJobIdList.contains(nextJobId)) {
				JobNextJobOrderInfoResponse nextJobOrder = new JobNextJobOrderInfoResponse();
				nextJobOrder.setJobunitId(treeItem.getData().getJobunitId());
				nextJobOrder.setJobId(treeItem.getData().getId());
				nextJobOrder.setNextJobId(nextJobId);
				nextJobOrderList.add(nextJobOrder);
				if (infoUpdateFlag) {
					jobInfo.setWaitRuleChanged(true);
				}
			}
		}

		if (infoUpdateFlag && jobInfo.getWaitRule().getExclusiveBranch()
				&& nextJobOrderList.size() == 0) {
			jobInfo.getWaitRule().setExclusiveBranch(false);
			jobInfo.setWaitRuleChanged(true);
		}
	}
}
