/*

 Copyright (C) 2006 NTT DATA Corporation

 This program is free software; you can redistribute it and/or
 Modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation, version 2.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 */

package com.clustercontrol.performance.util.code;

import java.io.Serializable;
import java.util.ArrayList;

import com.clustercontrol.performance.monitor.entity.CollectorCategoryMstData;
import com.clustercontrol.performance.monitor.entity.CollectorDeviceInfoData;
import com.clustercontrol.performance.monitor.entity.CollectorItemCodeMstData;

/**
 * 収集項目の親子関係を保持するユーティリティクラス
 * 
 * @version 1.0
 * @since 1.0
 */
public class CollectorItemTreeItem  implements Serializable {
	private static final long serialVersionUID = -8592461412968942952L;

	/** 親 */
	private CollectorItemTreeItem parent = null;

	/** 情報オブジェクト */
	private CollectorItemCodeMstData itemCodeData = null;
	private CollectorCategoryMstData categoryData = null;

	/** 子の格納リスト */
	private ArrayList<CollectorItemTreeItem> children = null;

	/** 収集方法 */
	private String collectMethod;

	/** デバイス情報 */
	private CollectorDeviceInfoData deviceData = null;

	public CollectorItemTreeItem(){
		super();
	}

	/**
	 * コンストラクター
	 * @param parent 親の収集項目要素
	 * @param data 収集項目要素
	 * @param deviceData デバイス情報
	 */
	public CollectorItemTreeItem(
			CollectorItemTreeItem parent,
			CollectorItemCodeMstData itemCodeData,
			CollectorCategoryMstData categoryData,
			String collectMethod,
			CollectorDeviceInfoData deviceData) {
		// 収集方法を設定
		this.collectMethod = collectMethod;

		// デバイス情報を生成
		if(deviceData != null){
			this.deviceData = deviceData;
		} else{
			this.deviceData = new CollectorDeviceInfoData(
					null, //collectorId,
					null, //facilityId,
					"", //deviceNameは空文字とする
					"", //deviceDisplayNameはから文字とする
					-1l, // index,
					null, //deviceId
					null //deviceType
					);
		}

		// 要素が収集項目カテゴリの場合は、自分自身のカテゴリを設定する。
		if (categoryData != null){
			this.setCategoryData(categoryData);
		} else
			// 要素が収集項目コードの場合は、親のカテゴリと同じカテゴリとする。
			if (itemCodeData != null) {
				this.setCategoryData(parent.getCategoryData());
			}

		this.setParent(parent);  // 親を設定
		this.itemCodeData = itemCodeData;  // 収集項目コード情報を設定
		this.categoryData = categoryData;  // 収集項目カテゴリ情報を設定

		if (parent != null) {
			parent.addChildren(this);
		}

		this.children = new ArrayList<CollectorItemTreeItem>();
	}

	/**
	 * parent を取得します。
	 * 
	 * @return parent
	 */
	public CollectorItemTreeItem getParent() {
		return parent;
	}

	/**
	 * 親を設定します。
	 * webサービス(jaxb)のためpublicにしておく
	 * 
	 * @return 親
	 */
	public void setParent(CollectorItemTreeItem parent) {
		// DTOがループすると、webサービスが動作しないので、parentはsetしない。
		// クライアントでsetする。
		// this.parent = parent;
	}

	/**
	 * 子を追加します。
	 * <p>
	 * 
	 * この際、childeの親はこのオブジェクトとして設定されます。
	 * 
	 * @param child
	 *            子
	 */
	private void addChildren(CollectorItemTreeItem child) {
		child.setParent(this);
		children.add(child);
	}

	/**
	 * children を取得します。
	 * 
	 * @return children
	 */
	public ArrayList<CollectorItemTreeItem> getChildren() {
		return children;
	}

	/**
	 * children を設定します。
	 * webサービス(jaxb)のためpublicにしておく。
	 * 
	 * @param children
	 */
	public void setChildren(ArrayList<CollectorItemTreeItem> children) {
		this.children = children;
	}

	/**
	 * 子の要素の数を取得します。
	 * @return 子の要素の数
	 */
	public int size() {
		return children.size();
	}

	public CollectorItemCodeMstData getItemCodeData() {
		return itemCodeData;
	}

	public void setItemCodeData(CollectorItemCodeMstData itemCodeData) {
		this.itemCodeData = itemCodeData;
	}

	public CollectorCategoryMstData getCategoryData() {
		return categoryData;
	}

	public void setCategoryData(CollectorCategoryMstData categoryData) {
		this.categoryData = categoryData;
	}

	/**
	 * デバイス情報を取得します。
	 * @return デバイス情報
	 */
	public CollectorDeviceInfoData getDeviceData(){
		return deviceData;
	}

	/**
	 * デバイス情報を設定します。
	 * webサービス(jaxb)のためsetterを用意しておく
	 */
	public void setDeviceData(CollectorDeviceInfoData deviceData){
		this.deviceData = deviceData;
	}

	/**
	 * 収集方法を取得します。
	 * @return 収集方法
	 */
	public String getCollectMethod() {
		return collectMethod;
	}

	/**
	 * 収集方法を設定します。
	 * webサービス(jaxb)のためsetterを用意しておく。
	 */
	public void setCollectMethod(String collectMethod) {
		this.collectMethod = collectMethod;
	}

	public boolean isCategoryItem(){
		if (categoryData != null){
			return true;
		} else {
			return false;
		}
	}
}