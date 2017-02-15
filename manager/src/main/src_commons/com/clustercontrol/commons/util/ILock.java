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
	
}
