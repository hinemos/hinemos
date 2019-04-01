/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.rap.rwt.SingletonUtil;

import com.clustercontrol.repository.bean.FacilityConstant;
import com.clustercontrol.ws.repository.FacilityInfo;
import com.clustercontrol.ws.repository.FacilityTreeItem;

public class FacilityTreeItemUtil {
	// ログ
	private static Log m_log = LogFactory.getLog( FacilityTreeItemUtil.class );

	/**
	 * Session Singleton
	 */
	private static FacilityTreeItemUtil getInstance(){
		return SingletonUtil.getSessionInstance( FacilityTreeItemUtil.class );
	}
	
	/**
	 * ツリーのディープコピーを生成して返します
	 * 
	 * @return ディープコピー
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static FacilityTreeItem deepCopy(FacilityTreeItem item, FacilityTreeItem parentItem) {
		synchronized(getInstance()) {
		FacilityTreeItem resultItem = new FacilityTreeItem();
			if (item.getData() != null) {
				FacilityInfo resultData = new FacilityInfo();
				if (item.getData().isBuiltInFlg() != null) {
					resultData.setBuiltInFlg(item.getData().isBuiltInFlg() != null ? item.getData().isBuiltInFlg(): Boolean.FALSE);
				}
				if (item.getData().getCreateDatetime() != null) {
					resultData.setCreateDatetime(item.getData().getCreateDatetime());
				}
				resultData.setCreateUserId(item.getData().getCreateUserId());
				resultData.setDescription(item.getData().getDescription());
				if (item.getData().getDisplaySortOrder() != null) {
					resultData.setDisplaySortOrder(item.getData().getDisplaySortOrder());
				}
				resultData.setFacilityId(item.getData().getFacilityId());
				resultData.setFacilityName(item.getData().getFacilityName());
				if (item.getData().getFacilityType() != null) {
					resultData.setFacilityType(item.getData().getFacilityType());
				}
				if (item.getData().getModifyDatetime() != null) {
					resultData.setModifyDatetime(item.getData().getModifyDatetime());
				}
				resultData.setModifyUserId(item.getData().getModifyUserId());
				if (item.getData().isValid() != null) {
					resultData.setValid(item.getData().isValid());
				}
				resultData.setOwnerRoleId(item.getData().getOwnerRoleId());
				if (item.getData().isNotReferFlg() != null) {
					resultData.setNotReferFlg(item.getData().isNotReferFlg());
				}
				resultItem.setData(resultData);
			}
	
			if (item.getChildren() != null) {
				List<FacilityTreeItem> resultChildren = new ArrayList<FacilityTreeItem>();
				for(FacilityTreeItem child : item.getChildren()) {
					if (child != null) {
						FacilityTreeItem resultChild = deepCopy(child, resultItem);
						resultChildren.add(resultChild);
					} else {
						resultChildren.add(null);
					}
				}
				resultItem.getChildren().clear();
				resultItem.getChildren().addAll(resultChildren);
			} else {
				resultItem.getChildren().clear();
			}
			resultItem.setParent(parentItem);
			return resultItem;
		}
	}

	/**
	 * 子ファシリティを追加します。<BR>
	 * <p>
	 * 
	 * この際、childeの親はこのオブジェクトとして設定されます。
	 * 
	 * @param child
	 *            子
	 */
	public static void addChild(FacilityTreeItem parent, FacilityTreeItem child){
		List<FacilityTreeItem> facilityTreeItemList = parent.getChildren();
		facilityTreeItemList.add(child);
		child.setParent(parent);

		return;
	}

	/**
	 * 直下の子ファシリティのデータがあれば消します。<BR>
	 * 1つだけ消します。
	 * 
	 */
	public static boolean removeChild(FacilityTreeItem parent, String facilityId) {
		List<FacilityTreeItem> children = parent.getChildren();
		for (int i = 0; i < children.size(); i++) {
			if (facilityId.equals(children.get(i).getData().getFacilityId())) {
				children.remove(i);
				return true;
			}
		}
		return false;
	}

	/**
	 * 直下の子ファシリティのデータがあれば残します。<BR>
	 * 引数と一致しない物を消します。
	 * 一つ以上消せた場合、trueを返します。
	 * 
	 */
	public static boolean keepChild(FacilityTreeItem parent, String facilityId){

		boolean ret = false;
		if (facilityId == null) {
			return ret;
		}
		List<FacilityTreeItem> children = parent.getChildren();
		for(int i=0 ; i< children.size(); i++){
			if(!facilityId.equals((children.get(i)).getData().getFacilityId())){

				//マッチしない場合にはその要素を消します。
				children.remove(i);

				//さらに消せるかどうかチェックします。
				keepChild(parent, facilityId);

				//１つ以上消せた場合はtrueを返します。
				ret = true;
				break;
			}
		}

		return ret;
	}

	/**
	 * ノードを削除して、スコープだけのツリーにします。
	 */
	public static boolean removeNode(FacilityTreeItem parent) {

		boolean flag = false;

		List<FacilityTreeItem> children = parent.getChildren();
		// for(int i=0 ; i< childrens.size(); i++){
		// removeするとインデックスが変わってしまうため、i--で検索する。
		for(int i=children.size()-1 ; i >= 0; i--){
			if ((children.get(i)).getData().getFacilityType() ==
					FacilityConstant.TYPE_NODE) {
				//マッチした場合にはその要素を消します。
				children.remove(i);
				flag = true;
			} else {
				boolean tmpFlag = removeNode(children.get(i));
				flag = flag || tmpFlag;
			}
		}
		return flag;
	}

	/**
	 * スコープごとのノード表示件数を超えるノード数を削除します。
	 * 
	 * @param parent 対象スコープ
	 */
	public static void removeOverNode(FacilityTreeItem parent) {
		// プレファレンスページよりスコープごとのノード表示数を取得
		int scopeNodecount = 0;
		try {
			scopeNodecount = Integer.parseInt(System.getProperty("scope.node.count", "0"));
		} catch (NumberFormatException e) {
			m_log.info("System environment value \"scope.node.count\" is not correct.");
		}
		List<FacilityTreeItem> children = parent.getChildren();
		// removeするとインデックスが変わってしまうため、i--で検索する。
		for(int i=children.size()-1 ; i >= 0; i--){
			if ((children.get(i)).getData().getFacilityType() == FacilityConstant.TYPE_NODE
					&& scopeNodecount > 0 
					&& scopeNodecount <= i) {
				//マッチした場合にはその要素を消します。
				children.remove(i);
			} else {
				removeOverNode(children.get(i));
			}
		}
	}
}
