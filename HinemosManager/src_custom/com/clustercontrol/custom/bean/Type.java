/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.custom.bean;

import javax.xml.bind.annotation.XmlType;
/**
 * 定義する値は取得する値の種別
 * 
 * - NUMBER	数値
 * - STRING　文字列
 *   
 */
@XmlType(namespace = "http://monitor.ws.clustercontrol.com")
public enum Type {
    NUMBER,STRING
}
