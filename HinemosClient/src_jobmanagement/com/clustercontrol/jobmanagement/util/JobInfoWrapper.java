/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.jobmanagement.util;

import org.openapitools.client.model.JobInfoResponse;

import com.clustercontrol.jobmanagement.util.JobInfoWrapper;

public class JobInfoWrapper extends JobInfoResponse {
	private Boolean propertyFull=false;
	//Utilityで利用している
	private String parentId = null;

	public Boolean getPropertyFull() {
		return propertyFull;
	}

	public void setPropertyFull(Boolean propertyFull) {
		this.propertyFull = propertyFull;
	}

	public String getParentId() {
		return parentId;
	}

	public void setParentId(String parentId) {
		this.parentId = parentId;
	}
	
	@Override 
	public boolean equals( Object target){
		return super.equals(target);
	}
	@Override 
	// 本クラスはJobEditState の editSessionMap の jobunitUpdateTimeMap のkey として利用されている。
	// 上記Mapへの登録後に クラスの内容メンバの値を変更されることがありえるが
	// メンバの内容によってhashCode()が変化してしまうと、Mapのgetが想定通りに動作しないため、
	// 固定化の対応を行っている。
	// （JobInfoResponse はメンバの内容値よって 変化するようになっている）
	//
	// 上記keyのマッチングが内容値でなくインスタンスIDの一致を前提としていることは
	// Mapへの登録と参照が画面遷移を挟んでいることが考慮すると
	// 複数画面間で同じインスタンスを持ち回らないと正常に動作しないため 望ましくないが 
	// 既存実装の変更によるデグレリスクが不明なのでやむえず踏襲する。
	public int hashCode(){
		return System.identityHashCode(this);
	}

}
