/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.jobmanagement.dto;

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.rest.annotation.RestItemName;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertEnum;
import com.clustercontrol.rest.annotation.validation.RestValidateString;
import com.clustercontrol.rest.dto.RequestDto;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype.EndStatusSelectEnum;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype.OperationJobOutputEnum;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype.PriorityRequiredEnum;
import com.clustercontrol.util.MessageConstant;

/* 
 * 本クラスのRestXXアノテーション、correlationCheckを修正する場合は、Infoクラスも同様に修正すること。
 * (ジョブユニットの登録/更新はInfoクラス、ジョブ単位の登録/更新の際はRequestクラスが使用される。)
 * refs #13882
 */
public class JobOutputInfoRequest implements RequestDto {
	/** 標準出力と同じ出力先を使用する */
	@RestItemName(value=MessageConstant.JOB_OUTPUT_FAILURE_SAME_NORMAL)
	private Boolean sameNormalFlg;
	
	/** 出力先 - ディレクトリ */
	@RestValidateString(minLen=1, maxLen=1024)
	private String directory;
	
	/** 出力先 - ファイル名 */
	@RestValidateString(minLen=1, maxLen=1024)
	private String fileName;
	
	/** 追記フラグ */
	private Boolean appendFlg;
	
	/** ファイル出力失敗時の操作を指定 */
	private Boolean failureOperationFlg;
	
	/** ファイル出力失敗時の操作 */
	@RestBeanConvertEnum
	private OperationJobOutputEnum failureOperationType;
	
	/** ファイル出力失敗時 - 終了状態 */
	@RestBeanConvertEnum
	private EndStatusSelectEnum failureOperationEndStatus;
	
	/** ファイル出力失敗時 - 終了値 */
	private Integer failureOperationEndValue;
	
	/** ファイル出力失敗時 - ファイル出力失敗時に通知する */
	private Boolean failureNotifyFlg;

	/** ファイル出力失敗時 - 通知の重要度 */
	@RestBeanConvertEnum
	private PriorityRequiredEnum failureNotifyPriority;

	/** 有効／無効 */
	private Boolean valid;

	public JobOutputInfoRequest() {
	}

	public Boolean getSameNormalFlg() {
		return sameNormalFlg;
	}

	public void setSameNormalFlg(Boolean sameNormalFlg) {
		this.sameNormalFlg = sameNormalFlg;
	}

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

	public Boolean getAppendFlg() {
		return appendFlg;
	}

	public void setAppendFlg(Boolean appendFlg) {
		this.appendFlg = appendFlg;
	}

	public Boolean getFailureOperationFlg() {
		return failureOperationFlg;
	}

	public void setFailureOperationFlg(Boolean failureOperationFlg) {
		this.failureOperationFlg = failureOperationFlg;
	}

	public OperationJobOutputEnum getFailureOperationType() {
		return failureOperationType;
	}

	public void setFailureOperationType(OperationJobOutputEnum failureOperationType) {
		this.failureOperationType = failureOperationType;
	}

	public EndStatusSelectEnum getFailureOperationEndStatus() {
		return failureOperationEndStatus;
	}

	public void setFailureOperationEndStatus(EndStatusSelectEnum failureOperationEndStatus) {
		this.failureOperationEndStatus = failureOperationEndStatus;
	}

	public Integer getFailureOperationEndValue() {
		return failureOperationEndValue;
	}

	public void setFailureOperationEndValue(Integer failureOperationEndValue) {
		this.failureOperationEndValue = failureOperationEndValue;
	}

	public Boolean getFailureNotifyFlg() {
		return failureNotifyFlg;
	}

	public void setFailureNotifyFlg(Boolean failureNotifyFlg) {
		this.failureNotifyFlg = failureNotifyFlg;
	}

	public PriorityRequiredEnum getFailureNotifyPriority() {
		return failureNotifyPriority;
	}

	public void setFailureNotifyPriority(PriorityRequiredEnum failureNotifyPriority) {
		this.failureNotifyPriority = failureNotifyPriority;
	}

	public Boolean getValid() {
		return valid;
	}

	public void setValid(Boolean valid) {
		this.valid = valid;
	}

	@Override
	public void correlationCheck() throws InvalidSetting {
		
	}
	
	/* 
	 * 関連性チェック
	 * 
	 * @param isStdError 標準エラー向けである
	 * */
	public void correlationCheck(boolean isStdError) throws InvalidSetting {
		String[] mesParam = new String[1];
		if (!isStdError) {
			mesParam[0]= MessageConstant.JOB_OUTPUT_STDOUT_TO_FILE.getMessage();
		} else {
			mesParam[0]= MessageConstant.JOB_OUTPUT_STDERR_TO_FILE.getMessage();
		}
				;
		// [「出力」フラグは必須入力
		if (this.valid == null) {
			throw new InvalidSetting(MessageConstant.MESSAGE_JOB_OUTPUT_PLZ_SET_VALID_FLG.getMessage(mesParam));
		}
		// [「出力」が有効] なら 関連チェックを行なう
		if (this.valid == true) {
			// 標準エラー向けなら「標準出力先と同じにする」が設定されていること
			if ( isStdError && this.sameNormalFlg == null) {
				throw new InvalidSetting(MessageConstant.MESSAGE_JOB_OUTPUT_PLZ_SET_SAME_NORMAL.getMessage());
			}
			// 「標準出力先と同じにする」が有効でないなら、出力先[ディレクトリ,ファイル,追記する]が設定されていること。
			if (sameNormalFlg == null || sameNormalFlg == false) {
				if (this.directory == null) {
					if (!isStdError) {
						throw new InvalidSetting(MessageConstant.MESSAGE_JOB_OUTPUT_PLZ_SET_DIR_FOR_OUT.getMessage());
					} else {
						throw new InvalidSetting(MessageConstant.MESSAGE_JOB_OUTPUT_PLZ_SET_DIR_FOR_ERR.getMessage());
					}
				}
				if (this.fileName == null) {
					if (!isStdError) {
						throw new InvalidSetting(MessageConstant.MESSAGE_JOB_OUTPUT_PLZ_SET_FILE_FOR_OUT.getMessage());
					} else {
						throw new InvalidSetting(MessageConstant.MESSAGE_JOB_OUTPUT_PLZ_SET_FILE_FOR_ERR.getMessage());
					}
				}
				if (this.appendFlg == null) {
					if (!isStdError) {
						throw new InvalidSetting(MessageConstant.MESSAGE_JOB_OUTPUT_PLZ_SET_ADD_FLG_FOR_OUT.getMessage());
					} else {
						throw new InvalidSetting(MessageConstant.MESSAGE_JOB_OUTPUT_PLZ_SET_ADD_FLG_FOR_ERR.getMessage());
					}
				}
			}
			// 出力失敗時の操作[操作する]が設定されていること。
			if (this.failureOperationFlg == null) {
				throw new InvalidSetting(MessageConstant.MESSAGE_JOB_OUTPUT_PLZ_SET_OPERATION_FLG.getMessage(mesParam));
			}
			// 出力失敗時の操作[操作する]が有効なら関連チェックを行う
			if (failureOperationFlg == true) {
				//出力失敗時の操作[操作]は必須入力
				if (this.failureOperationType == null) {
					throw new InvalidSetting(MessageConstant.MESSAGE_JOB_OUTPUT_PLZ_SET_OPERATION_TYPE.getMessage(mesParam));
				}
				//出力失敗時の操作[操作]が状態指定(強制含む)なら関連チェックを行う
				if (this.failureOperationType == OperationJobOutputEnum.STOP_SET_END_VALUE
						|| this.failureOperationType == OperationJobOutputEnum.STOP_SET_END_VALUE_FORCE) {
					//出力失敗時の操作[状態,終了値]は必須入力
					if (this.failureOperationEndStatus == null) {
						throw new InvalidSetting(MessageConstant.MESSAGE_JOB_OUTPUT_PLZ_SET_OPERATION_STATUS.getMessage(mesParam));
					}
					if (this.failureOperationEndValue == null) {
						throw new InvalidSetting(MessageConstant.MESSAGE_JOB_OUTPUT_PLZ_SET_OPERATION_VALUE.getMessage(mesParam));
					}
				}
				
			}
			// 出力失敗時の通知[通知する]が設定されていること。
			if (this.failureNotifyFlg == null) {
				throw new InvalidSetting(MessageConstant.MESSAGE_JOB_OUTPUT_PLZ_SET_NITIFY_FLG.getMessage(mesParam));
			}
			// 出力失敗時の通知[通知する]が有効なら関連チェックを行う
			if (failureNotifyFlg == true) {
				//出力失敗時の通知[重要度]は必須入力
				if (this.failureNotifyPriority == null) {
					throw new InvalidSetting(MessageConstant.MESSAGE_JOB_OUTPUT_PLZ_SET_NITIFY_PRIORITY.getMessage(mesParam));
				}
			}
		} 
		//設定不要（必須入力とならなかった）となった一部項目（画面ではnull入力不可項目）について、画面入力と内容をそろえるために画面デフォルト値で自動補完を行う
		if (this.appendFlg == null) {
			//出力先[追記する]はnullなら"しない"で補完
			this.appendFlg = false;
		}
		if (isStdError && sameNormalFlg == null) {
			//標準エラーの場合、出力先[標準出力先と同じにする]はnullなら"しない"で補完
			this.sameNormalFlg = false;
		}
		if (this.failureOperationFlg == null) {
			// 出力失敗時の操作[操作する]はnullなら "しない"で補完
			this.failureOperationFlg = false;
		}
		if (this.failureOperationType == null) {
			// 出力失敗時の操作[操作]はnullなら "停止[中断]"で補完
			this.failureOperationType = OperationJobOutputEnum.STOP_SUSPEND;
		}
		if (this.failureOperationEndStatus == null) {
			// 出力失敗時の操作[終了状態]はnullなら "異常"で補完
			this.failureOperationEndStatus = EndStatusSelectEnum.ABNORMAL;
		}
		if (this.failureOperationEndValue == null) {
			// 出力失敗時の操作[終了値]はnullなら -1で補完
			this.failureOperationEndValue = -1;
		}
		if (this.failureNotifyFlg == null) {
			// 出力失敗時の通知[通知する]はnullなら "しない"で補完
			failureNotifyFlg = false;
		}
		if (this.failureNotifyPriority == null) {
			// 出力失敗時の通知[重要度]はnullなら "危険"で補完
			this.failureNotifyPriority = PriorityRequiredEnum.CRITICAL;
		}
	}
}
