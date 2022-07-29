/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rest.annotation.beanconverter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * DTOクラスに対して宣言し、UnitテストによるDTO定義検証に使用します。
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })

public @interface RestBeanConvertAssertion {
	/**
	 * コンバート元のクラス。<br/>
	 * {@link #from}か{@link #to()}のいずれか一方を指定してください。
	 * デフォルトは{@code Object.class}であり、無指定を意味します。
	 */
	Class<?> from() default Object.class;

	/**
	 * コンバート先のクラス。<br/>
	 * {@link #from}か{@link #to()}のいずれか一方を指定してください。
	 * デフォルトは{@code Object.class}であり、無指定を意味します。
	 */
	Class<?> to() default Object.class;

	/**
	 * この名前のフィードは比較検証から除外します。<br/>
	 * 自動変換を使用せずに手動で設定するフィールドがある場合などに使用します。
	 */
	String[] exclude() default {};

	/**
	 * コンバート元/先クラスとのフィールド一致をチェックしない場合は false にします。
	 * デフォルトは true (チェックする)です。
	 */
	boolean checksOpposite() default true;

	/**
	 * フィールド名と setter/getter 名の一致をチェックしない場合は false にします。
	 * デフォルトは true (チェックする)です。
	 */
	boolean checksAccessors() default true;
}
