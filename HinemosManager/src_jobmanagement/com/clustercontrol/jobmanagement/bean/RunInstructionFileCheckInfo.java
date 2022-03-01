/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.jobmanagement.bean;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlType;

/**
 * ファイルチェックジョブの実行指示情報を保持するクラス
 */
@XmlType(namespace = "http://jobmanagement.ws.clustercontrol.com")
public class RunInstructionFileCheckInfo implements Serializable {
	private static final long serialVersionUID = 8792918234365101474L;

	/** ディレクトリ */
	private String directory;
	/** ファイル名 */
	private String fileName;

	/** チェック種別 - 作成 */
	private Boolean createValidFlg = false;
	/** ジョブ開始前に作成されたファイルも対象とする */
	private Boolean createBeforeJobStartFlg = false;
	/** チェック種別 - 削除 */
	private Boolean deleteValidFlg = false;
	/** チェック種別 - 変更 */
	private Boolean modifyValidFlg = false;
	/** 変更判定（タイムスタンプ変更/ファイルサイズ変更） */
	private Integer modifyType = 0;
	/** ファイルの使用中は判定しないか */
	private Boolean notJudgeFileInUseFlg = false;

	/** 条件を満たした場合の終了値 */
	private Integer successEndValue = 0;

	public String getDirectory() {
		return directory;
	}

	public void setDirectory(String directory) {
		this.directory = directory;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public Boolean getCreateValidFlg() {
		return createValidFlg;
	}

	public void setCreateValidFlg(Boolean createValidFlg) {
		this.createValidFlg = createValidFlg;
	}

	public Boolean getCreateBeforeJobStartFlg() {
		return createBeforeJobStartFlg;
	}

	public void setCreateBeforeJobStartFlg(Boolean createBeforeJobStartFlg) {
		this.createBeforeJobStartFlg = createBeforeJobStartFlg;
	}

	public Boolean getDeleteValidFlg() {
		return deleteValidFlg;
	}

	public void setDeleteValidFlg(Boolean deleteValidFlg) {
		this.deleteValidFlg = deleteValidFlg;
	}

	public Boolean getModifyValidFlg() {
		return modifyValidFlg;
	}

	public void setModifyValidFlg(Boolean modifyValidFlg) {
		this.modifyValidFlg = modifyValidFlg;
	}

	public Integer getModifyType() {
		return modifyType;
	}

	public void setModifyType(Integer modifyType) {
		this.modifyType = modifyType;
	}

	public Boolean getNotJudgeFileInUseFlg() {
		return notJudgeFileInUseFlg;
	}

	public void setNotJudgeFileInUseFlg(Boolean notJudgeFileInUseFlg) {
		this.notJudgeFileInUseFlg = notJudgeFileInUseFlg;
	}

	public Integer getSuccessEndValue() {
		return successEndValue;
	}

	public void setSuccessEndValue(Integer successEndValue) {
		this.successEndValue = successEndValue;
	}


	// --- auto generated from eclipse.
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((createBeforeJobStartFlg == null) ? 0 : createBeforeJobStartFlg.hashCode());
		result = prime * result + ((createValidFlg == null) ? 0 : createValidFlg.hashCode());
		result = prime * result + ((deleteValidFlg == null) ? 0 : deleteValidFlg.hashCode());
		result = prime * result + ((directory == null) ? 0 : directory.hashCode());
		result = prime * result + ((fileName == null) ? 0 : fileName.hashCode());
		result = prime * result + ((modifyType == null) ? 0 : modifyType.hashCode());
		result = prime * result + ((modifyValidFlg == null) ? 0 : modifyValidFlg.hashCode());
		result = prime * result + ((notJudgeFileInUseFlg == null) ? 0 : notJudgeFileInUseFlg.hashCode());
		result = prime * result + ((successEndValue == null) ? 0 : successEndValue.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RunInstructionFileCheckInfo other = (RunInstructionFileCheckInfo) obj;
		if (createBeforeJobStartFlg == null) {
			if (other.createBeforeJobStartFlg != null)
				return false;
		} else if (!createBeforeJobStartFlg.equals(other.createBeforeJobStartFlg))
			return false;
		if (createValidFlg == null) {
			if (other.createValidFlg != null)
				return false;
		} else if (!createValidFlg.equals(other.createValidFlg))
			return false;
		if (deleteValidFlg == null) {
			if (other.deleteValidFlg != null)
				return false;
		} else if (!deleteValidFlg.equals(other.deleteValidFlg))
			return false;
		if (directory == null) {
			if (other.directory != null)
				return false;
		} else if (!directory.equals(other.directory))
			return false;
		if (fileName == null) {
			if (other.fileName != null)
				return false;
		} else if (!fileName.equals(other.fileName))
			return false;
		if (modifyType == null) {
			if (other.modifyType != null)
				return false;
		} else if (!modifyType.equals(other.modifyType))
			return false;
		if (modifyValidFlg == null) {
			if (other.modifyValidFlg != null)
				return false;
		} else if (!modifyValidFlg.equals(other.modifyValidFlg))
			return false;
		if (notJudgeFileInUseFlg == null) {
			if (other.notJudgeFileInUseFlg != null)
				return false;
		} else if (!notJudgeFileInUseFlg.equals(other.notJudgeFileInUseFlg))
			return false;
		if (successEndValue == null) {
			if (other.successEndValue != null)
				return false;
		} else if (!successEndValue.equals(other.successEndValue))
			return false;
		return true;
	}
}
