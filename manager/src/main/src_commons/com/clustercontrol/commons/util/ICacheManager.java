/*

Copyright (C) 2015 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.commons.util;

import java.io.Serializable;
import java.util.Set;

/**
 * アプリケーション内のキャッシュを管理するkey-valueストアのインタフェース。<br/>
 * HinemosマネージャのJava VM上に保持する全てのアプリケーションキャッシュデータは本インタフェースを通じて管理すること。<br/>
 * （他のオプション製品の正常動作が保証されなくなるため）<br/>
 * ただし、Hinemosマネージャ固有のキャッシュデータ・Hinemosマネージャの動作に応じて可変でないstaticなキャッシュデータは本インタフェースを通じて管理する必要はない。<br/>
 * (設定ファイルなどの値を格納したキャッシュデータなど)
 */
public interface ICacheManager {
	
	/**
	 * key-valueストアから指定されたkeyに対するvalueを返却する。<br/>
	 * なお、返却されたvalueのステートを更新してもkey-valueストア内のvalueに反映されることは保証されない。<br/>
	 * @param key valueを取得するためのkey
	 * @return keyに対応するvalue
	 */
	Serializable get(Serializable key);
	
	/**
	 * key-valueストアに指定されたkey-valueペアを格納する。<br/>
	 * 本インタフェースにて重複するkeyが格納されていた場合、既存のvalueを返却する。<br/>
	 * getで取得したvalueを更新した場合、storeで上書きすることでkey-valueストア上のvalueへの反映が保障される。<br/>
	 * また、obj = nullは格納できないため、キャッシュ未定義化はremoveにて実装する必要がある。<br/>
	 * @param key key-valueペアのkey
	 * @param obj key-valueペアのvalue(上書きされなかった場合はnull)
	 */
	Serializable store(Serializable key, Serializable obj);
	
	/**
	 * key-valueストアから指定されたkeyのkey-valueペアを削除する。<br/>
	 * 該当するkey-valueペアが存在していた場合、そのvalueを返却する。<br/>
	 * @param key 削除するkey
	 * @return 削除されたvalue(key-valueペアが存在しなかった場合はnull)
	 */
	Serializable remove(Serializable key);
	
	/**
	 * 格納されているkeyの一覧を返却する。<br/>
	 * @return
	 */
	<T> Set<T> getKeySet(Class<T> type);
}
