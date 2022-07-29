/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.settings.ui.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


/**
 * Hinemos機能ツリー表示に関する情報を保持するクラス<BR>
 * 
 * @version 6.1.0
 * @since 1.2.0
 */
public class FuncTreeItem implements Serializable {
	/** シリアライズ可能クラスに定義するUID */
	private static final long serialVersionUID = -5749478055659165471L;

	/** ジョブ情報 */
	private FuncInfo m_data;

	/** 子のジョブツリーアイテムのリスト */
    private List<FuncTreeItem> m_children = new ArrayList<FuncTreeItem>();

    /** 親のジョブツリーアイテム */
    private FuncTreeItem m_parent;
    
    public FuncTreeItem() {
    }

    /**
     * 親のジョブツリーアイテムを設定する。<BR>
     * @param parent 親のジョブツリーアイテム
     */
    public void setParent(FuncTreeItem parent) {
        this.m_parent = parent;
    }

    /**
     * 親のジョブツリーアイテムを返す。<BR>
     * @return 親のジョブツリーアイテム
     */
    public FuncTreeItem getParent() {
        return m_parent;
    }

    /**
     * ジョブ情報を設定する。<BR>
     * @param data ジョブ情報
     * @see com.clustercontrol.jobmanagement.bean.FuncInfo
     */
    public void setData(FuncInfo data) {
        this.m_data = data;
    }

    /**
     * ジョブ情報を返す。<BR>
     * @return ジョブ情報
     * @see com.clustercontrol.jobmanagement.bean.FuncInfo
     */
    public FuncInfo getData() {
        return m_data;
    }

    /**
     * 子のジョブツリーアイテムを、子のジョブツリーアイテムのリストに追加する。<BR>
     * @param child 子のジョブツリーアイテム
     */
    public void addChildren(FuncTreeItem child) {
        m_children.add(child);
        child.setParent(this);
    }

    /**
     * 子のジョブツリーアイテムの数を返す。<BR>
     * @return 子のジョブツリーアイテムの数
     */
    public int size() {
        return m_children.size();
    }

    /**
     * 子のジョブツリーアイテムの配列を返す。<BR>
     * @return 子のジョブツリーアイテムの配列
     */
    public FuncTreeItem[] getChildren() {
        return m_children.toArray(new FuncTreeItem[m_children.size()]);
    }


}