/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.commons.util;

public interface ILock {
	
	/**
	 * 読み込みロックを取得する。他スレッドによる読み込みロックの取得は可能だが、書き込みロックの取得は全ての読み込みロックが解放されるまで待機される。<br/>
	 */
	void readLock();
	
	/**
	 * 読み込みロックを解放する。ロックが取得されていないスレッドの場合、エラーとなる。<br/>
	 */
	void readUnlock();
	
	/**
	 * 書き込みロックを取得する。他スレッドによる読み込みロックおよび書き込みロックの取得は本ロックが解放されるまで待機される。<br/>
	 * なお、読み込みロックのブロック内で書き込みロックを取得すること（昇格）は許容されない。（無限待ちあるいはエラーとなる）<br/>
	 */
	void writeLock();
	
	/**
	 * 書き込みロックを開放する。ロックが取得されていないスレッドの場合、エラーとなる。<br/>
	 */
	void writeUnlock();
	
	/**
	 * 呼び出し時に別のスレッドにより保持されていない場合のみに、書き込みロックを取得する<br/>
	 */
	boolean tryWriteLock();
	
	/**
	 * 呼び出し時に別のスレッドにより保持されていない場合のみに、読み込みロックを取得する<br/>
	 */
	boolean tryReadLock();
	
}
