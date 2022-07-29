/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.bean;

import com.clustercontrol.xcloud.validation.annotation.ElementId;
import com.clustercontrol.xcloud.validation.annotation.Size;

/**
 * ストレージ作成情報を保持するクラス。 
 *
 */
public class AddStorageRequest extends Request {
	public AddStorageRequest() {
		super();
	}
	
	private String storageName;
	private Integer size;
	
	/**
	 * ストレージ名を取得します。
	 * 
	 * @return ストレージ名。
	 */
	@ElementId("XCLOUD_CORE_STORAGE_NAME")
	@Size(max = 128)
	public String getStorageName() {
		return storageName;
	}
	/**
	 * ストレージ名を指定します。
	 * 作成時には128 字以内の文字列を指定する必要があります。リストア時には、null を指定した場合、バックアップ時の値が使用されます。
	 * 
	 * @param storageName　ストレージ名。
	 */
	public void setStorageName(String storageName) {
		this.storageName = storageName;
	}
	
	/**
	 * ストレージサイズ（ＧＢ）を取得します。
	 * 
	 * @return ストレージサイズ（ＧＢ）。
	 */
	public Integer getSize() {
		return size;
	}
	/**
	 * ストレージサイズ（ＧＢ）を指定します。
	 * 
	 * @param size ストレージサイズ（ＧＢ）。
	 */
	public void setSize(Integer size) {
		this.size = size;
	}

}
