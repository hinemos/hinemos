/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.monitor.factory;

import java.util.Iterator;
import java.util.List;

import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.monitor.bean.StatusExpirationConstant;
import com.clustercontrol.notify.monitor.model.StatusInfoEntity;
import com.clustercontrol.notify.monitor.util.QueryUtil;
import com.clustercontrol.util.HinemosTime;
import com.clustercontrol.util.MessageConstant;

/**
 * ステータス情報を管理するクラス<BR>
 *
 * @version 3.0.0
 * @since 2.0.0
 */
public class ManageStatus {

	/**
	 * 存続期間を経過したステータス情報を削除 または 更新します。
	 * <p>
	 * <ol>
	 *  <li>存続期間を経過したステータス情報を取得します。
	 *  <li>取得したステータス情報の存続期間経過後の処理の制御フラグを確認します。
	 *  <li>制御フラグ（{@link com.clustercontrol.bean.StatusExpirationConstant}）が削除の場合は、ステータス情報を削除します。<BR>
	 *      重要度の場合は、ステータス情報を更新します。更新する項目は、下記の通りです。
	 *      <ul>
	 *       <li>重要度
	 *       <li>メッセージID（空白）
	 *       <li>メッセージ
	 *       <li>最終変更日時
	 *       <li>存続期間経過後の処理の制御フラグ（期限切れ）
	 *      </ul>
	 * </ol>
	 * 
	 * @see com.clustercontrol.bean.StatusExpirationConstant
	 */
	public void execute(){
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();

			// 有効期限切れのステータス情報一覧を取得
			Long now = HinemosTime.currentTimeMillis();
			List<StatusInfoEntity> ct = QueryUtil.getStatusInfoByExpirationStatus(now);

			// 有効期限切れステータスの情報を更新する
			Iterator<StatusInfoEntity> itr = ct.iterator();
			StatusInfoEntity status = null;
			while(itr.hasNext())
			{
				status = itr.next();
				if(status.getExpirationFlg() != null){
					int flg = status.getExpirationFlg().intValue();

					// 削除
					if(StatusExpirationConstant.TYPE_DELETE == flg){
						em.remove(status);
					}
					// 更新されていない旨のメッセージに置換える
					else if(StatusExpirationConstant.TYPE_CRITICAL == flg ||
							StatusExpirationConstant.TYPE_WARNING == flg ||
							StatusExpirationConstant.TYPE_INFO == flg ||
							StatusExpirationConstant.TYPE_UNKNOWN == flg){
						// 重要度の設定
						status.setPriority(flg);
						// メッセージに更新されていない旨のメッセージを設定
						status.setMessage(MessageConstant.MONITOR_STATUS_NO_UPDATE.getMessage());
						// 有効期限切れ制御フラグに有効期限切れを設定
						status.setExpirationFlg(StatusExpirationConstant.TYPE_EXPIRATION);
						// 更新日時を設定
						status.setOutputDate(now);
					}
				}
			}
		}
	}
}