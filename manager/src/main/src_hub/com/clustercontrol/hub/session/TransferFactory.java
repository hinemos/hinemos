/*

Copyright (C) 2016 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */
package com.clustercontrol.hub.session;

import java.util.List;

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.hub.model.TransferInfo;

/**
 * Transfer を継承した転送用インスタンスを作成するクラス。
 * 後から転送処理のバリエーションをプラグインとして追加できるよう考慮しており、
 * ServiceLoader の仕様に従って適切に追加しておけば、ServiceLoader からロードされる。
 * 
 */
public interface TransferFactory {
	/**
	 * プラグインが設定可能としているパラメータを格納する
	 *
	 */
	public static class Property {
		public final String name;
		public final Object defaultValue;
		public final String description;

		public Property(String name, Object defaultValue, String description) {
			this.name = name;
			this.defaultValue = defaultValue;
			this.description = description;
		}
	}
	
	/**
	 * プラグインが設定可能としているパラメータを取得する
	 * 
	 * @return
	 */
	List<Property> properties();
	
	/**
	 * プラグイン ID を取得する
	 * 
	 * @return
	 */
	String getDestTypeId();
	
	/**
	 * プラグイン名を取得する
	 * 
	 * @return
	 */
	String getName();
	
	/**
	 * プラグインの説明を取得する
	 * 
	 * @return
	 */
	String getDescription();
	
	/**
	 * 転送用インスタンスを作成する
	 * 
	 * @return
	 */
	Transfer createTansfer(TransferInfo tranferInfo, PropertyBinder binder) throws TransferException;
	
	/**
	 * TransferInfo に設定されたパラメータをバリデーションする
	 * 
	 * @return
	 */
	void validate(TransferInfo tranferInfo) throws InvalidSetting;
}