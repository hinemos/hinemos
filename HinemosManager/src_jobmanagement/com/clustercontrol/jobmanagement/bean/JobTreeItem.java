/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.bean;

import java.io.Serializable;
import java.util.ArrayList;

import javax.xml.bind.annotation.XmlType;

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.rest.annotation.validation.RestValidateObject;
import com.clustercontrol.rest.dto.RequestDto;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * ジョブのツリー表示に関する情報を保持するクラス<BR>
 * 
 * @version 2.0.0
 * @since 1.0.0
 */
@XmlType(namespace = "http://jobmanagement.ws.clustercontrol.com")
public class JobTreeItem implements Serializable, RequestDto {
	/** シリアライズ可能クラスに定義するUID */
	@JsonIgnore
	private static final long serialVersionUID = -5749478055659165471L;

	/** ジョブ情報 */
	@RestValidateObject(notNull = true)
	private JobInfo data;

	/** ジョブ詳細 */
	@JsonIgnore
	private JobDetailInfo detail;

	/** 子のジョブツリーアイテムのリスト */
	private ArrayList<JobTreeItem> children = new ArrayList<JobTreeItem>();

	/** 親のジョブツリーアイテム */
	@JsonIgnore
	private JobTreeItem parent;

	/** パスセパレータ */
	@JsonIgnore
	private static final String SEPARATOR = ">";

	public JobTreeItem() {
	}

	/**
	 * コンストラクタ。<BR>
	 * ジョブツリーにジョブ情報を紐付ける。<BR>
	 * 
	 * @param data ジョブ情報
	 * @see com.clustercontrol.jobmanagement.bean.JobInfo
	 */
	public JobTreeItem(JobInfo data) {
		setData(data);
	}

	/**
	 * コンストラクタ。<BR>
	 * ジョブツリーに親ジョブツリーを紐付ける。<BR>
	 * ジョブツリーにジョブ情報を紐付ける。<BR>
	 * 
	 * @param parent 親のジョブツリーアイテム
	 * @param data ジョブ情報
	 * @see com.clustercontrol.jobmanagement.bean.JobInfo
	 */
	public JobTreeItem(JobTreeItem parent, JobInfo data) {

		this.setParent(parent);
		this.setData(data);

		if (parent != null) {
			parent.addChildren(this);
		}

		this.children = new ArrayList<JobTreeItem>();
	}

	/**
	 * 親のジョブツリーアイテムを設定する。<BR>
	 * @param parent 親のジョブツリーアイテム
	 */
	public void setParent(JobTreeItem parent) {
		this.parent = parent;
	}

	/**
	 * 親のジョブツリーアイテムを返す。<BR>
	 * @return 親のジョブツリーアイテム
	 */
	public JobTreeItem getParent() {
		return parent;
	}

	/**
	 * ジョブ情報を設定する。<BR>
	 * @param data ジョブ情報
	 * @see com.clustercontrol.jobmanagement.bean.JobInfo
	 */
	public void setData(JobInfo data) {
		this.data = data;
	}

	/**
	 * ジョブ情報を返す。<BR>
	 * @return ジョブ情報
	 * @see com.clustercontrol.jobmanagement.bean.JobInfo
	 */
	public JobInfo getData() {
		return data;
	}


	/**
	 * ジョブ詳細を設定する。<BR>
	 * @param data ジョブ情報
	 * @see com.clustercontrol.jobmanagement.bean.JobInfo
	 */
	public void setDetail(JobDetailInfo detail) {
		this.detail = detail;
	}

	/**
	 * ジョブ詳細を返す。<BR>
	 * @return ジョブ情報
	 * @see com.clustercontrol.jobmanagement.bean.JobInfo
	 */
	public JobDetailInfo getDetail() {
		return detail;
	}



	/**
	 * 子のジョブツリーアイテムを、子のジョブツリーアイテムのリストに追加する。<BR>
	 * @param child 子のジョブツリーアイテム
	 */
	public void addChildren(JobTreeItem child) {
		children.add(child);
		child.setParent(this);
	}

	/**
	 * 子のジョブツリーアイテムを、子のジョブツリーアイテムのリストから削除する。<BR>
	 * @param child 子のジョブツリーアイテム
	 */
	public void removeChildren(JobTreeItem child) {
		for (int i = 0; i < children.size(); i++) {
			if (child.equals(children.get(i))) {
				children.remove(i);
				break;
			}
		}
	}

	/**
	 * 子のジョブツリーアイテムの数を返す。<BR>
	 * @return 子のジョブツリーアイテムの数
	 */
	public int size() {
		return children.size();
	}

	/**
	 * 子のジョブツリーアイテムを返す。<BR>
	 * @param index 子のジョブツリーアイテムのリストインデックス
	 * @return 子のジョブツリーアイテム
	 */
	public JobTreeItem getChildren(int index) {
		return this.children.get(index);
	}

	public void setChildren(ArrayList<JobTreeItem> children) {
		this.children = children;
	}

	public ArrayList<JobTreeItem> getChildren() {
		return this.children;
	}

	/**
	 * 子のジョブツリーアイテムの配列を返す。<BR>
	 * @return 子のジョブツリーアイテムの配列
	 */
	public JobTreeItem[] getChildrenArray() {
		return children.toArray(new JobTreeItem[children.size()]);
	}

	/**
	 * ジョブツリーアイテムの親子関係を表現するパス文字列を返す。<BR>
	 * <p>
	 * 例）以下のジョブツリーにて、getPath()を呼び出す
	 * <p>
	 * <ul>
	 *  <li>料金システム
	 *  <ul>
	 *   <li>顧客管理
	 *   <ul>
	 *    <li>WEB  <- このインスタンスにてgetPath()を呼び出す
	 *    <li>DB
	 *   </ul>
	 *  </ul>
	 * </ul>
	 * <p>
	 * 結果 ： "料金システム>顧客管理>WEB"。<BR>
	 * 
	 * @return パス文字列
	 */
	public String getPath() {

		// トップ("ジョブ")の場合は、文字を出力しません。
		if (this.getData().getType() == JobConstant.TYPE_COMPOSITE) {
			return "";
		}

		StringBuffer buffer = new StringBuffer();

		buffer.append(this.getData().getName());

		/*
		 * 再起呼び出しすることでもないので。
		 */

		JobTreeItem parent = this.getParent();
		while (parent != null
				&& parent.getData().getType() != JobConstant.TYPE_COMPOSITE) {

			buffer.insert(0, SEPARATOR);
			buffer.insert(0, parent.getData().getName());
			parent = parent.getParent();
		}

		return buffer.toString();
	}

	@Override
	public void correlationCheck() throws InvalidSetting {
		data.correlationCheck();

		if (children != null) {
			for (JobTreeItem child : children) {
				child.correlationCheck();
			}
		}
	}
}