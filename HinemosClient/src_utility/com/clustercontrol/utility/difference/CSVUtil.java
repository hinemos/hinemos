/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.difference;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 差分結果を CSV 形式にて出力するクラス。
 * 
 * @version 2.0.0
 * @since 2.0.0
 * 
 *
 */
public class CSVUtil {
	protected static Log logger = LogFactory.getLog(CSVUtil.class);
	/**
	 * 差分結果を CSV 形式にて出力するクラスのインターフェース。
	 * 
	 */
	public interface CSVSerializer {
		/**
		 * 機能毎の差分結果を CSV 形式でストリームに吐き出す。
		 * 
		 * @param os
		 * @param resultB
		 */
		void write(OutputStream os, ResultB resultB);
	}
	
	/**
	 * CSVSerializer を取得する。
	 * 
	 * @return
	 */
	public static CSVSerializer createCSVSerializer() {
		return new CSVSerializerImpl();
	}
	
	/**
	 * 
	 * 
	 * 
	 *
	 */
	private static class CSVSerializerImpl implements CSVSerializer {
		private static final char CSV_SEPARATOR = ',';

		@Override
		public void write(OutputStream os, ResultB resultB) {
			BufferedWriter bw = null;
			try {
				bw = new BufferedWriter(new OutputStreamWriter(os, "SHIFT_JIS"));
				
				for (ResultC resultC: resultB.getResultCs()) {
					StringBuffer oneLine = new StringBuffer();
					oneLine.append(resultB.getFuncName());
					oneLine.append(CSV_SEPARATOR);
					oneLine.append(resultC.getId());
					oneLine.append(CSV_SEPARATOR);

					switch(resultC.getResultType()) {
					case equal:
						if (resultC.getResultDs().size() > 0) {
							for (ResultD resultD: resultC.getResultDs()) {
								StringBuffer oneLine2 = new StringBuffer();
								oneLine2.append(oneLine);
								oneLine2.append(getString(resultD.getResultType() == ResultD.ResultType.diff ? "diff" : "equal"));
								oneLine2.append(CSV_SEPARATOR);
								oneLine2.append(createDiffString(resultD));

								bw.write(oneLine2.toString());
								bw.newLine();
							}
						}
						else {
							oneLine.append(Messages.getString("equal"));
							bw.write(oneLine.toString());
							bw.newLine();
						}
						break;
					case diff:
						{
							for (ResultD resultD: resultC.getResultDs()) {
								StringBuffer oneLine2 = new StringBuffer();
								oneLine2.append(oneLine);
								oneLine2.append(getString(resultD.getResultType() == ResultD.ResultType.diff ? "diff" : "equal"));
								oneLine2.append(CSV_SEPARATOR);
								oneLine2.append(createDiffString(resultD));

								bw.write(oneLine2.toString());
								bw.newLine();
							}
						}
						break;
					case only1:
						oneLine.append(getString("only1"));
						bw.write(oneLine.toString());
						bw.newLine();
						break;
					case only2:
						oneLine.append(getString("only2"));
						bw.write(oneLine.toString());
						bw.newLine();
						break;
					}
				}

				bw.flush();
			}
			catch (UnsupportedEncodingException e) {}
			catch (FileNotFoundException e){}
			catch (IOException e){}
			finally {
				if (bw != null) {
					try {
						bw.close();
					}
					catch (IOException e) {}
				}
			}
		}

		// 差分出力内容に含まれるカンマと改行(CRLF)をエスケープする
		private static String escapeDiffString(String diffStr){
			return diffStr.replaceAll(",", " ").replaceAll("\n", " ").replaceAll("\r", " ");
		}
		
		private static String createDiffString(ResultD resultD) {
			StringBuffer oneLine = new StringBuffer();
			
			{
				// カラムの ID をデバッグ情報として出力。
				StringBuilder sb = new StringBuilder();
				for (int i = 0; i < resultD.getColumnId().length; ++i) {
					sb.append(resultD.getColumnId()[i]);
					if ((i + 1) != resultD.getColumnId().length) {
						sb.append('.');
					}
				}
				logger.debug(sb.toString());
			}
			
			// 名前空間を追加。
			for (int i = 0; i < resultD.getNameSpaces().length; ++i) {
				oneLine.append(getString(resultD.getNameSpaces()[i]));
				if ((i + 1) != resultD.getNameSpaces().length) {
					oneLine.append(':');
				}
			}
			
			// 属性名を追加。
			if (resultD.getPropName() != null) {
				if (resultD.getNameSpaces().length > 0) {
					oneLine.append(':');
				}
				
				oneLine.append(getString(resultD.getPropName()));
			}

			oneLine.append(CSV_SEPARATOR);

			// 差分結果を追加。
			switch (resultD.getValueType()) {
			case simple:
				{
					for (int i = 0; i < resultD.getValue1().length;) {
						oneLine.append(escapeDiffString(resultD.getValue1()[i].toString()));
						break;
					}
					oneLine.append(CSV_SEPARATOR);
					for (int i = 0; i < resultD.getValue2().length;) {
						oneLine.append(escapeDiffString(resultD.getValue2()[i].toString()));
						break;
					}
				}
				break;
			case array:
				{
					oneLine.append('[');
					for (int i = 0; i < resultD.getValue1().length; ++i) {
						if (i != 0) {
							oneLine.append(':');
						}
						oneLine.append(escapeDiffString(resultD.getValue1()[i].toString()));
					}
					oneLine.append(']');
					oneLine.append(CSV_SEPARATOR);
					oneLine.append('[');
					for (int i = 0; i < resultD.getValue2().length; ++i) {
						if (i != 0) {
							oneLine.append(':');
						}
						
						oneLine.append(escapeDiffString(resultD.getValue2()[i].toString()));
					}
					oneLine.append(']');
				}
				break;
			}

			return oneLine.toString();
		}
	}

	/**
	 * 外部化された文字列を取得する。
	 * 
	 * @param name
	 * @return
	 */
	public static String getString(String name) {
		String message = null;
		try {
			message = Messages.getString(name);
		}
		catch (Throwable e) {
			
		}
		
		return message != null ? message: name;
	}
}
