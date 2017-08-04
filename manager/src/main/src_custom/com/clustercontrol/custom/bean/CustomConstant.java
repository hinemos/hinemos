/*

Copyright (C) 2011 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.custom.bean;

/**
 * コマンド監視の定数クラス<BR />
 * 
 * @since 4.0
 */
public final class CustomConstant {

	/**
	 *  コマンドの実行種別(INDIVIDUAL : スコープ内の全ノード, SELECTED : 選択した特定のノード)
	 */
	public static enum CommandExecType { INDIVIDUAL, SELECTED };

	// 上記に対応するDBカラム値
	public static final int _execIndividual = 1;
	public static final int _execSelected = 2;

}
