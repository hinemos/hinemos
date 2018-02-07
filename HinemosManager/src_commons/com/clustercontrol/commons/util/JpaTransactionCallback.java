/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.commons.util;

/**
 * JpaTransactionManagerのAPI実行に紐づくcallback API実行クラス
 */
public interface JpaTransactionCallback {


	/**
	 * トランザクション処理フラグ
	 * @return true:トランザクション処理のCallback
	 */
	default boolean isTransaction() {
		return true;
	}

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