/*
Copyright (C) 2010 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.
 */

package com.clustercontrol.nodemap.bean;

import java.io.Serializable;
import java.util.Properties;

import javax.xml.bind.annotation.XmlType;

/**
 * ファシリティ(ノードとスコープ)
 * @since 1.0.0
 */
@XmlType(namespace = "http://nodemap.ws.clustercontrol.com")
public class FacilityElement implements Serializable {
	private static final long serialVersionUID = -1L;

	private String facilityId;

	private String facilityName;

	private String iconImage;

	private String parentId;

	private Integer x = -1;
	private Integer y = -1;

	private String typeName;

	// 組み込みスコープか否か
	private boolean builtin;

	// 監視の有効/無効フラグ
	private boolean valid;

	// マップへの新規登録ファシリティであることを示すフラグ
	// このフラグがtrueの場合はx, yは初期値（x=-1, y=-1）となる
	private boolean newcomer = true;

	// オーナーロールID
	private String ownerRoleId;

	// toolchip用に利用する属性値
	Properties attributes = new Properties();

	public FacilityElement(){}

	public String getTypeName(){
		return typeName;
	}

	public void setTypeName(String typeName) {
		this.typeName = typeName;
	}

	public String getFacilityId() {
		return facilityId;
	}
	public void setFacilityId(String facilityId) {
		this.facilityId = facilityId;
	}

	public String getFacilityName() {
		return facilityName;
	}
	public void setFacilityName(String facilityName) {
		this.facilityName = facilityName;
	}

	public String getIconImage() {
		return iconImage;
	}

	public void setIconImage(String iconImage) {
		this.iconImage = iconImage;
	}

	public Integer getX() {
		return x;
	}

	public Integer getY() {
		return y;
	}

	// Webサービスのためsetterを用意しておく
	@Deprecated
	public void setX(Integer x) {
		this.x = x;
	}

	@Deprecated
	public void setY(Integer y) {
		this.y = y;
	}

	/**
	 * 表示座標を設定する。
	 * 同時に新規登録フラグはfalseとなる。
	 * @param x x座標
	 * @param y y座標
	 */
	public void setPosition(Integer x, Integer y) {
		this.x = x;
		this.y = y;
		this.newcomer = false;
	}

	public String getAttribute(String key) {
		// キーに対応する属性がない場合は空文字を返す
		return attributes.getProperty(key, "");
	}
	public void setAttributes(String key, Object obj) {
		if(obj != null){
			this.attributes.setProperty(key, obj.toString());
		}
	}

	public String getParentId() {
		return parentId;
	}

	public void setParentId(String parentId) {
		this.parentId = parentId;
	}

	public boolean isBuiltin() {
		return builtin;
	}

	public void setBuiltin(boolean builtin) {
		this.builtin = builtin;
	}

	public String getOwnerRoleId() {
		return ownerRoleId;
	}

	public void setOwnerRoleId(String ownerRoleId) {
		this.ownerRoleId = ownerRoleId;
	}

	/**
	 * 監視の有効/無効フラグを返す
	 * @return 監視の有効/無効
	 */
	public boolean isValid() {
		return valid;
	}

	/**
	 * 監視の有効/無効フラグを設定
	 * @param valid 監視の有効/無効
	 */
	public void setValid(boolean valid) {
		this.valid = valid;
	}

	/**
	 * 新規追加のファシリティであることを示すフラグの値を返す
	 * @return 新規追加の場合はtrue
	 */
	public boolean isNewcomer() {
		return newcomer;
	}

	/**
	 * 新規追加のファシリティであることを示すフラグを設定する
	 * フラグをtrueで設定した場合は、座標情報が初期化（x=-1, y=-1に設定）される
	 * @param newcomer 新規追加の場合はtrue
	 */
	public void setNewcomer(boolean newcomer) {
		if(newcomer == true){
			x = -1;
			y = -1;
		}
		this.newcomer = newcomer;
	}

	// Webサービスのためgetter、setterを用意しておく
	@Deprecated
	public Properties getAttributes() {
		return attributes;
	}

	@Deprecated
	public void setAttributes(Properties attributes) {
		this.attributes = attributes;
	}
}