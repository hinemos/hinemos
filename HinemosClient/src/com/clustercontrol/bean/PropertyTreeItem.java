/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * プロパティシート用ツリーアイテムクラス<BR>
 * 
 * @version 1.0.0
 * @since 1.0.0
 */
public class PropertyTreeItem implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1796344516594486143L;

	/** 親プロパティ */
	private PropertyTreeItem m_Parent;

	/** 子プロパティ */
	private List<PropertyTreeItem> m_Children = new ArrayList<PropertyTreeItem>();

	/** プロパティID */
	protected String m_ID;

	/** プロパティ項目名 */
	protected String m_Name;

	/**
	 * @return children を戻します。
	 * @since 1.0.0
	 */
	public Object[] getChildren() {
		return m_Children.toArray();
	}

	/**
	 * 
	 * 
	 * @return children
	 * @since 1.0.0
	 */
	public void removeChildren() {
		for (int i = 0; i < m_Children.size(); i++) {
			((Property) m_Children.get(i)).setParent(null);
		}
		m_Children.clear();
	}

	/**
	 * 
	 * @param child
	 * @since 1.0.0
	 */
	public void removeChildren(PropertyTreeItem child) {
		m_Children.remove(child);
	}

	/**
	 * 
	 * @param children
	 *            children を設定します。<BR>
	 * @since 1.0.0
	 */
	public void addChildren(PropertyTreeItem child) {
		m_Children.add(child);
		child.setParent(this);
	}

	/**
	 * 複数の子を追加します。
	 */
	public void addChildren(Iterable<? extends PropertyTreeItem> children) {
		for (PropertyTreeItem child : children) {
			addChildren(child);
		}
	}

	/**
	 * @param children
	 *            children を設定します。<BR>
	 * @param index
	 *            追加位置
	 * @since 1.0.0
	 */
	public void addChildren(PropertyTreeItem child, int index) {
		m_Children.add(index, child);
		child.setParent(this);
	}

	/**
	 * iD を取得します。<BR>
	 * @return iD
	 * @since 1.0.0
	 */
	public String getID() {
		return m_ID;
	}

	/**
	 * IDを設定します。<BR>
	 * @param id
	 *            iD を設定。
	 * @since 1.0.0
	 */
	public void setID(String id) {
		m_ID = id;
	}

	/**
	 * nameを取得します。<BR>
	 * @return name
	 * @since 1.0.0
	 */
	public String getName() {
		return m_Name;
	}

	/**
	 * nameを設定します。<BR>
	 * @param name
	 * 
	 * @since 1.0.0
	 */
	public void setName(String name) {
		m_Name = name;
	}

	/**
	 * parentを取得します。<BR>
	 * @return parent
	 * @since 1.0.0
	 */
	public PropertyTreeItem getParent() {
		return m_Parent;
	}

	/**
	 * parentを設定します。<BR>
	 * @param parent
	 * 
	 * @since 1.0.0
	 */
	public void setParent(PropertyTreeItem parent) {
		m_Parent = parent;
	}

	/**
	 * 子プロパティ数を取得します。<BR>
	 * @param
	 * @since 1.0.0
	 */
	public int size() {
		return m_Children.size();
	}
}
