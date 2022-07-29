/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.repository.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import javax.xml.bind.annotation.XmlType;

import com.clustercontrol.repository.model.FacilityInfo;

/**
 * スコープ情報をツリー構造化するためのクラス<BR>
 * 
 * @version 1.0.0
 * @since 1.0.0
 */
@XmlType(namespace = "http://repository.ws.clustercontrol.com")
public class FacilityTreeItem implements Serializable, Cloneable {

	private static final long serialVersionUID = 1663475778653652044L;

	/** 親 */
	private FacilityTreeItem parent = null;

	/** 情報オブジェクト */
	private FacilityInfo data = null;

	/** 子の格納リスト */
	private List<FacilityTreeItem> children = null;

	private HashSet<String> authorizedRoleIdSet = null;

	/**
	 * JAXB(Webservice)のために、引数なしのコンストラクタを用意。
	 */
	public FacilityTreeItem() {}

	/**
	 * 引数の情報を保持したインスタンスを取得します。<BR>
	 * 
	 * @param parent
	 *            親のオブジェクト
	 * @param data
	 *            スコープ情報オブジェクト
	 */
	public FacilityTreeItem(FacilityTreeItem parent, FacilityInfo data) {

		this.setParent(parent);
		this.setData(data);

		if (parent != null) {
			parent.addChildren(this);
		}

		this.children = new ArrayList<FacilityTreeItem>();
	}

	// ----- instance フィールド ----- //

	/**
	 * 親ファシリティ（スコープ）を取得します。<BR>
	 * 
	 * @return 親
	 */
	public FacilityTreeItem getParent() {
		return this.parent;
	}

	/**
	 * 親ファシリティ（スコープ）を設定します。<BR>
	 * <p>
	 * 
	 * インスタンス化の際に親へ関係付けているため、子を削除するメソッドを実装した 後に可視性(スコープ)を拡大して下さい。 <br>
	 * また、新しい親への関係付けも行うように実装して下さい。
	 * 
	 * @return 親ファシリティ
	 */
	public void setParent(FacilityTreeItem parent) {
		// DTOがループすると、webサービスが動作しないので、parentはsetしない。
		// クライアントでsetする。
		// this.parent = parent;
	}

	/**
	 * スコープ情報を取得します。<BR>
	 * 
	 * @return スコープ情報
	 */
	public FacilityInfo getData() {
		return this.data;
	}

	/**
	 * スコープ情報を設定します。<BR>
	 * 
	 * @param data
	 *            スコープ情報
	 */
	public void setData(FacilityInfo data) {
		this.data = data;
	}

	public HashSet<String> getAuthorizedRoleIdSet() {
		return authorizedRoleIdSet;
	}

	public void setAuthorizedRoleIdSet(HashSet<String> authorizedRoleIdSet) {
		this.authorizedRoleIdSet = authorizedRoleIdSet;
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
	public void addChildren(FacilityTreeItem child) {
		child.setParent(this);
		children.add(child);
	}

	/**
	 * 子ファシリティの数を取得します。<BR>
	 * 
	 * @return 子の数
	 */
	public int size() {
		return children.size();
	}

	/**
	 * 全ての子ファシリティを取得します。<BR>
	 * <p>
	 * 
	 * 並び順は、追加された順となっています。
	 * 
	 * @return 全ての子。
	 */
	public FacilityTreeItem[] getChildrenArray() {
		FacilityTreeItem[] result = new FacilityTreeItem[this.size()];
		return children.toArray(result);
	}
	/**
	 * 直下の子ファシリティのデータがあれば消します。<BR>
	 * 1つだけ消します。
	 * 
	 */
	public boolean removeChild(String facilityId){
		for(int i=0 ; i< children.size(); i++){

			if(facilityId.equals((children.get(i)).getData().getFacilityId())){

				//マッチした場合にはその要素を消します。
				children.remove(i);
				return true;
			}
		}

		return false;
	}
	/**
	 * 直下の子ファシリティのデータを取得します。<BR>
	 * 
	 */
	public FacilityTreeItem getChild(String facilityId){


		for(int i=0 ; i< children.size(); i++){

			if(facilityId.equals((children.get(i)).getData().getFacilityId())){

				//マッチした場合にはその要素を返します。
				return children.get(i);
			}
		}

		return null;
	}

	/**
	 * ノードを削除して、スコープだけのツリーにします。
	 */
	public boolean removeNode() {
		boolean flag = false;
		// removeするとインデックスが変わってしまうため、i--で検索する。
		for(int i=children.size()-1 ; i >= 0; i--){
			if ((children.get(i)).getData().getFacilityType() ==
					FacilityConstant.TYPE_NODE) {
				//マッチした場合にはその要素を消します。
				children.remove(i);
				flag = true;
			} else {
				boolean tmpFlag = children.get(i).removeNode();
				flag = flag || tmpFlag;
			}
		}
		return flag;
	}

	/**
	 * 直下の子ファシリティのデータがあれば残します。<BR>
	 * 引数と一致しない物を消します。
	 * 一つ以上消せた場合、trueを返します。
	 * 
	 */
	public boolean keepChild(String facilityId){

		boolean ret = false;
		if (facilityId == null) {
			return ret;
		}
		for(int i=0 ; i< children.size(); i++){
			if(!facilityId.equals((children.get(i)).getData().getFacilityId())){

				//マッチしない場合にはその要素を消します。
				children.remove(i);

				//さらに消せるかどうかチェックします。
				keepChild(facilityId);

				//１つ以上消せた場合はtrueを返します。
				ret = true;
				break;
			}
		}

		return ret;
	}

	// Webサービス(jaxb)のためsetter、getterを用意しておく
	public List<FacilityTreeItem> getChildren() {
		return children;
	}

	@Deprecated
	public void setChildren(List<FacilityTreeItem> children) {
		this.children = children;
	}

	/**
	 * parentはnullとなっているので注意すること。
	 * parentが必要な場合は、completeParentを呼ぶこと。
	 */
	@Override
	public FacilityTreeItem clone(){
		try {
			FacilityTreeItem cloneInfo = (FacilityTreeItem) super.clone();
			cloneInfo.parent = null;
			cloneInfo.data = this.data.clone();
			cloneInfo.children = new ArrayList<FacilityTreeItem>();
			for (FacilityTreeItem item : this.children) {
				cloneInfo.children.add(item.clone());
			}
			return cloneInfo;
		} catch (CloneNotSupportedException e) {
			throw new InternalError(e.toString());
		}
	}

	public static void completeParent(FacilityTreeItem item) {
		if (item.children == null) {
			return;
		}
		for (FacilityTreeItem child : item.children) {
			child.parent = item;
			completeParent(child);
		}
	}

	/**
	 * ファシリティが配下に含まれているかを再帰的に判定します
	 * @param facilityId
	 * @param scopeFlg スコープを判定の対象に含めるかどうか
	 * @param validFlg 有効/無効
	 * @return
	 */
	public boolean isContained(String facilityId, boolean scopeFlg, boolean validFlg) {
		if (facilityId.equals(this.data.getFacilityId())) {
			if (this.data.getFacilityType() == FacilityConstant.TYPE_NODE) {
				return this.data.getValid() == validFlg;
			} else if (scopeFlg) {
				return true;
			}
			return false;
		}
		for (FacilityTreeItem child : this.children) {
			if (child.isContained(facilityId, scopeFlg, validFlg)) {
				return true;
			}
		}
		return false;
	}
}