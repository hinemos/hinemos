/*

Copyright (C) 2006 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.accesscontrol.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import javax.xml.bind.annotation.XmlType;

import com.clustercontrol.accesscontrol.model.RoleInfo;
import com.clustercontrol.accesscontrol.model.UserInfo;

/**
 * ロールのツリー表示に関する情報を保持するクラス<BR>
 * 
 * @version 2.0.0
 * @since 1.0.0
 */
@XmlType(namespace = "http://accesscontrol.ws.clustercontrol.com")
public class RoleTreeItem implements Serializable {
	/** シリアライズ可能クラスに定義するUID */
	private static final long serialVersionUID = -5749478055659165471L;

	/** ノード情報（UserInfo、RoleInfoが入る） */
	private Object m_data;

	/** ノード種別 */
	private int m_type;

	/** 子のロールツリーアイテムのリスト */
	private ArrayList<RoleTreeItem> m_children = new ArrayList<RoleTreeItem>();

	/** 親のアイテム */
	private RoleTreeItem m_parent;

	/** パスセパレータ */
	private static final String SEPARATOR = ">";

	/**
	 * コンストラクタ。<BR>
	 * 
	 */
	public RoleTreeItem() {
	}
	/**
	 * コンストラクタ。<BR>
	 * ロールツリーにロール情報を紐付ける。<BR>
	 * 
	 * @param parent 親のツリーアイテム
	 * @param data ロール情報
	 * @see com.clustercontrol.accesscontrol.bean.RoleInfo
	 */
	public RoleTreeItem(RoleTreeItem parent, RoleInfo roleInfo) {
		this.setData(roleInfo);
		this.setParent(parent);

		if (parent != null) {
			parent.addChildren(this);
		}

		this.m_children = new ArrayList<RoleTreeItem>();
	}

	/**
	 * コンストラクタ。<BR>
	 * ロールツリーにユーザ情報を紐付ける。<BR>
	 * 
	 * @param parent 親のツリーアイテム
	 * @param data ユーザ情報
	 * @see com.clustercontrol.accesscontrol.bean.UserInfo
	 */
	public RoleTreeItem(RoleTreeItem parent, UserInfo userInfo) {
		this.setData(userInfo);
		this.setParent(parent);

		if (parent != null) {
			parent.addChildren(this);
		}
	}

	public Object getData() {
		return m_data;
	}

	public void setData(Object data) {
		this.m_data = data;
	}

	public int getType() {
		return m_type;
	}

	public void setType(int type) {
		this.m_type = type;
	}

	/**
	 * 親のツリーアイテムを設定する。<BR>
	 * @param parent 親のツリーアイテム
	 */
	public void setParent(RoleTreeItem parent) {
		this.m_parent = parent;
	}

	/**
	 * 親のツリーアイテムを返す。<BR>
	 * @return 親のツリーアイテム
	 */
	public RoleTreeItem getParent() {
		return m_parent;
	}

	/**
	 * 子のツリーアイテムを、子のツリーアイテムのリストに追加する。<BR>
	 * @param child 子のツリーアイテム
	 */
	public void addChildren(RoleTreeItem child) {
		m_children.add(child);
		child.setParent(this);

		if (child.getData() instanceof UserInfo) {
			Collections.sort(m_children, new Comparator<RoleTreeItem>() {
				@Override
				public int compare(RoleTreeItem o1, RoleTreeItem o2) {
					return ((UserInfo)o1.getData()).getUserId().compareTo(((UserInfo)o2.getData()).getUserId());
				}
			});
		}
	}

	/**
	 * 子のツリーアイテムを、子のツリーアイテムのリストから削除する。<BR>
	 * @param child 子のツリーアイテム
	 */
	public void removeChildren(RoleTreeItem child) {
		for (int i = 0; i < m_children.size(); i++) {
			if (child.equals(m_children.get(i))) {
				m_children.remove(i);
				break;
			}
		}
	}

	/**
	 * 子のツリーアイテムの数を返す。<BR>
	 * @return 子のジョブツリーアイテムの数
	 */
	public int size() {
		return m_children.size();
	}

	/**
	 * 子のツリーアイテムを返す。<BR>
	 * @param index 子のツリーアイテムのリストインデックス
	 * @return 子のツリーアイテム
	 */
	public RoleTreeItem getChildren(int index) {
		return m_children.get(index);
	}

	public void setChildren(ArrayList<RoleTreeItem> children) {
		m_children = children;
	}

	public ArrayList<RoleTreeItem> getChildren() {
		return m_children;
	}

	/**
	 * 子のツリーアイテムの配列を返す。<BR>
	 * @return 子のツリーアイテムの配列
	 */
	public RoleTreeItem[] getChildrenArray() {
		return m_children.toArray(new RoleTreeItem[m_children.size()]);
	}

	/**
	 * ジョブツリーアイテムの親子関係を表現するパス文字列を返す。<BR>
	 * <p>
	 * 例）以下のツリーにて、getPath()を呼び出す
	 * <p>
	 * <ul>
	 *  <li>ロール１
	 *  <ul>
	 *   <li>ユーザ１<- このインスタンスにてgetPath()を呼び出す
	 *  </ul>
	 * </ul>
	 * <p>
	 * 結果 ： "ロール１>ユーザ１"。<BR>
	 * 
	 * @return パス文字列
	 */
	public String getPath() {

		// ルートの場合は、文字を出力しません。
		if (this.m_data instanceof RoleInfo
				&& ((RoleInfo)this.m_data).getRoleId().equals(RoleSettingTreeConstant.ROOT_ID)) {
			return "";
		}

		StringBuffer buffer = new StringBuffer();

		if (this.m_data instanceof RoleInfo) {
			buffer.append(((RoleInfo)this.m_data).getRoleName());
		} else if  (this.m_data instanceof UserInfo) {
			buffer.append(((UserInfo)this.m_data).getUserName());
		}

		if (this.m_data instanceof UserInfo) {
			RoleTreeItem parent = this.getParent();
			buffer.insert(0, SEPARATOR);
			buffer.insert(0, ((RoleInfo)parent.getData()).getRoleName());
		}

		return buffer.toString();
	}
}