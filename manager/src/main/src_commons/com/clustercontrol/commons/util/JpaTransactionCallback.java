/*

Copyright (C) 2012 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.commons.util;

/**
 * JpaTransactionManagerのAPI実行に紐づくcallback API実行クラス
 */
public interface JpaTransactionCallback {

	/**
	 * begin()のコール前に呼ばれる処理を実装するインタフェース
	 */
	public void preBegin();

	/**
	 * begin()のコール後に呼ばれる処理を実装するインタフェース<br/>
	 * ただし、begin()で例外が生じた場合、インタフェースはコールされない。<br/>
	 */
	public void postBegin();

	/**
	 * flush()のコール前に呼ばれる処理を実装するインタフェース
	 */
	public void preFlush();

	/**
	 * flush()のコール後に呼ばれる処理を実装するインタフェース<br/>
	 * ただし、flush()で例外が生じた場合、本インタフェースはコールされない。<br/>
	 */
	public void postFlush();

	/**
	 * commit()のコール前に呼ばれる処理を実装するインタフェース
	 */
	public void preCommit();

	/**
	 * commit()のコール後に呼ばれる処理を実装するインタフェース<br/>
	 * ただし、commit()で例外が生じた場合、本インタフェースはコールされない。<br/>
	 */
	public void postCommit();

	/**
	 * rollback()のコール前に呼ばれる処理を実装するインタフェース
	 */
	public void preRollback();

	/**
	 * rollback()のコール後に呼ばれる処理を実装するインタフェース<br/>
	 * ただし、rollback()で例外が生じた場合、本インタフェースはコールされない。<br/>
	 */
	public void postRollback();

	/**
	 * close()のコール前に呼ばれる処理を実装するインタフェース
	 */
	public void preClose();

	/**
	 * close()のコール後に呼ばれる処理を実装するインタフェース<br/>
	 * ただし、close()で例外が生じた場合、本インタフェースはコールされない。<br/>
	 */
	public void postClose();

}