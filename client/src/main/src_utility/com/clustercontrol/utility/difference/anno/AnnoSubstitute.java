/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.difference.anno;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * 目印の基底クラス。
 * Jackson を経由し、Json ファイルから、デシリアライズされる。
 * デシリアライズ 時に派生先の型を Jackson に教えるため、JsonSubTypes によって派生先の情報を追加する。
 * 目印を追加した場合は、JsonSubTypes にて新たに追加する必要がある。
 * 
 * @version 2.0.0
 * @since 2.0.0
 * 
 *
 */
@JsonTypeInfo(use=JsonTypeInfo.Id.NAME, include=JsonTypeInfo.As.PROPERTY, property="type")
@JsonSubTypes({
    @JsonSubTypes.Type(value=Root.class, name="Root"),
    @JsonSubTypes.Type(value=PrimaryKey.class, name="PrimaryKey"),
    @JsonSubTypes.Type(value=Element.class, name="Element"),
    @JsonSubTypes.Type(value=Column.class, name="Column"),
    @JsonSubTypes.Type(value=Comparison.class, name="Comparison"),
    @JsonSubTypes.Type(value=Ignore.class, name="Ignore"),
    @JsonSubTypes.Type(value=Comparator.class, name="Comparator"),
    @JsonSubTypes.Type(value=Translate.class, name="Translate"),
    @JsonSubTypes.Type(value=TranslateOverrides.class, name="TranslateOverrides"),
    @JsonSubTypes.Type(value=ColumnOverrides.class, name="ColumnOverrides"),
    @JsonSubTypes.Type(value=ArrayId.class, name="Array"),
    @JsonSubTypes.Type(value=OrderBy.class, name="OrderBy"),
    @JsonSubTypes.Type(value=Namespace.class, name="Namespace")
}) 
public class AnnoSubstitute {
}
