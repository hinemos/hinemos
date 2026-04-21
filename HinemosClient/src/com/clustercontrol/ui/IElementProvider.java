/*
 * Copyright (c) 2026 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.ui;

import java.util.Collection;

/*
 * 管理する全要素を返す
 */
public interface IElementProvider {
	Collection<?> getAllElements();
}
