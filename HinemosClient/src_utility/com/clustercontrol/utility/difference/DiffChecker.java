/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.difference;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.dialogs.MessageDialog;

import com.clustercontrol.util.Messages;
import com.clustercontrol.utility.difference.anno.AnnoSubstitute;
import com.clustercontrol.utility.difference.anno.ArrayId;
import com.clustercontrol.utility.difference.anno.Column;
import com.clustercontrol.utility.difference.anno.ColumnOverrides;
import com.clustercontrol.utility.difference.anno.Comparator;
import com.clustercontrol.utility.difference.anno.Comparison;
import com.clustercontrol.utility.difference.anno.Element;
import com.clustercontrol.utility.difference.anno.Ignore;
import com.clustercontrol.utility.difference.anno.Namespace;
import com.clustercontrol.utility.difference.anno.OrderBy;
import com.clustercontrol.utility.difference.anno.Root;
import com.clustercontrol.utility.difference.anno.TranslateOverrides;



/**
 * json にて定義された DTO 検索用目印情報をインポートし、差分検出処理用のデータ構造に展開する。
 * 
 * @version 2.0.0
 * @since 2.0.0
 * 
 *
 */
public class DiffChecker {
	
	protected static Log logger = LogFactory.getLog(DiffChecker.class);
	
	private static class CompIDElementInfo {
		// public String id;
		public Class<?> clazz;
		public Object dto1;
		public Object dto2;
	}
	
	private Object dto1;
	private Object dto2;
	private Class<?> targetClass;

	// 全体 の比較結果。
	private ResultA resultA;

	// 機能毎の比較結果。
	private ResultB currentResultB;

	// 項目毎の比較結果。
	private ResultC currentResultC;

	// 比較結果オブジェクト数
	private long resultCnt = 0L;
	
	// ネームスペースのスタック。
	private Stack<String> stackNameSpace = new Stack<String>();

	// カラム ID のスタック。
	private Stack<Object> stackColumn = new Stack<Object>();

	// 継続フラグ。
	private boolean continueFlg = false;
	
	public DiffChecker(Object dto1, Object dto2, Class<?> targetClass) {
		this(dto1, dto2, targetClass, new ResultA());
	}

	public DiffChecker(Object dto1, Object dto2, Class<?> targetClass, ResultA resultA) {
		this.resultA = resultA;
		this.dto1 = dto1;
		this.dto2 = dto2;
		this.targetClass = targetClass;
	}

	public ResultA getResultA() {
		return resultA;
	}

	/**
	 * 差分を検索する。
	 */
	public boolean check() {
		logger.info("compare: " + targetClass);

		Root root = DiffUtil.getAnnotationByAll(targetClass, Root.class);
		assert root != null;

		// Root 要素なので、機能の比較結果を収める器を作成。
		currentResultB = new ResultB(CSVUtil.getString(root.funcName));

		// 比較する要素を採取する。
		Map<String, DiffChecker.CompIDElementInfo> compElements = collectIdentifiedElement(dto1, dto2, targetClass);
		// 比較処理開始。
		boolean diff = compareIdentifiedElement(compElements);

		resultA.getResultBs().put(currentResultB.getFuncName(), currentResultB);

		// ソート順を記した目印 (OrderBy) を取得。
		final OrderBy orderby = DiffUtil.getAnnotationByAll(targetClass, OrderBy.class);
		if (orderby != null) {
			// ソート用比較クラス作成。
			IndexComparator ic = new IndexComparator(orderby);
			
			// ソート実施。
			for (ResultC resultC: currentResultB.getResultCs()) {
				Collections.sort(resultC.getResultDs(), ic);
			}
		}
		
		return diff;
	}

	/**
	 * 指定された ID で識別される要素のペアを比較する。
	 * 
	 * @param compElements
	 */
	private boolean compareIdentifiedElement(Map<String, CompIDElementInfo> compElements) {
		logger.debug("start comparing ids.");

		boolean diff = false;
		for (Map.Entry<String, CompIDElementInfo> entry : compElements.entrySet()) {
			logger.debug("compare: " + entry.getKey());

			ResultC resultC = new ResultC(entry.getKey());
			boolean diffAsElem = false;

			// XML1 側のみに存在する。
			if (entry.getValue().dto1 == null) {
				diff = true;
				diffAsElem = true;
				logger.debug("only dto1");
				resultC.setResultType(ResultC.ResultType.only2);
			// XML2 側のみに存在する。
			} else if (entry.getValue().dto2 == null) {
				diff = true;
				diffAsElem = true;
				logger.debug("only dto2");
				resultC.setResultType(ResultC.ResultType.only1);
			// 両者に存在する。
			} else {
				currentResultC = resultC;
				
				// 比較処理開始。
				PropComparator pc = new PropComparator(entry.getValue().clazz, null, null, null);
				if (pc.compareProperties(entry.getValue().dto1, entry.getValue().dto2)) {
					diff = true;
					diffAsElem = true;
					logger.debug(entry.getKey() + ": " + "diff");
					currentResultC.setResultType(ResultC.ResultType.diff);
				}
				else {
					logger.debug(entry.getKey() + ": " + "equal");
					currentResultC.setResultType(ResultC.ResultType.equal);
				}
			}
			// 差分チェック結果の出力可否を判断
			if (DiffUtil.isAll() || diffAsElem) {
				currentResultB.getResultCs().add(resultC);
			}
		}

		logger.debug("end comparing ids.");
		
		return diff;
	}

	/**
	 * 指定されたオブジェクトを捜索し、同じ ID を持つペアを抜き出す。 
	 * 
	 * @param dto1
	 * @param dto2
	 * @param targetType
	 * @return
	 */
	private Map<String, DiffChecker.CompIDElementInfo> collectIdentifiedElement(
			Object dto1, Object dto2, final Class<?> targetType) {
		logger.debug("start collectting ids.");
		final Map<String, DiffChecker.CompIDElementInfo> compElements = new TreeMap<String, DiffChecker.CompIDElementInfo>(
			new java.util.Comparator<String>() {
				@Override
				public int compare(String o1, String o2) {
					return o1.compareTo(o2);
				}
			});

		// 比較対象の項目が格納されたプロパティを見分けるために、Comparison を検索対象にする。
		List<Class<? extends AnnoSubstitute>> annoList = new ArrayList<Class<? extends AnnoSubstitute>>();
		annoList.add(Comparison.class);

		// XML 1 側の ID で識別可能な要素の探索処理。
		new AnnotationSeeker(dto1, targetType, annoList,
			new AnnotationSeeker.Listener() {
				@Override
				public void found(Object parent, Method method, AnnoSubstitute anno, Class<?> returnType, Object prop) {
					// 該当項目のプライマリーキーを取得する。
					String pk = DiffUtil.getPK(prop, returnType);
					assert pk != null;
					logger.debug("found id1: " + pk);
					assert compElements.get(pk) == null;

					// 検出された要素をプライマリーキーをキーとして、マップに保存。
					DiffChecker.CompIDElementInfo ciei = new CompIDElementInfo();
					// ciei.id = pk;
					ciei.clazz = returnType;
					ciei.dto1 = prop;

					compElements.put(pk, ciei);
				}
			}).walk();

		// XML 2 側の ID で識別可能な要素の探索処理。
		new AnnotationSeeker(dto2, targetType, annoList,
			new AnnotationSeeker.Listener() {
				@Override
				public void found(Object parent, Method method, AnnoSubstitute anno, Class<?> returnType, Object prop) {
					// 該当項目のプライマリーキーを取得する。
					String pk = DiffUtil.getPK(prop, returnType);
					assert pk != null;

					logger.debug("found id2: " + pk);

					// XML 1 側で検出済みのプライマリーキーか確認。 
					DiffChecker.CompIDElementInfo ciei = compElements.get(pk);
					if (ciei == null) {
						// XML 1 側に存在しないプライマリーキーなので、新たにマップへ保存。 
						ciei = new CompIDElementInfo();
						// ciei.id = pk;
						ciei.clazz = returnType;
						ciei.dto2 = prop;
						compElements.put(pk, ciei);
					} else {
						// 比較対象のペアとして保存。
						assert ciei.clazz.isAssignableFrom(prop.getClass());
						ciei.dto2 = prop;
					}
				}
			}).walk();

		logger.debug("end collectting ids.");

		return compElements;
	}

	/**
	 * 指定されたプロパティの比較を行う。
	 * 
	 * 
	 *
	 */
	private class PropComparator {
		private PropertyAccessor pa;
		private ColumnOverrides co;
		private Ignore ig;

		/**
		 * @param targetClass 比較するクラス
		 * @param ig 比較対象外のプロパティ
		 * @param to クラスに指定されている値の変換パターンを上書きする
		 * @param co クラスに指定されているカラム名を上書きする
		 */
		public PropComparator(Class<?> targetClass, Ignore ig, TranslateOverrides to, ColumnOverrides co) {
			this.pa = new PropertyAccessorImpl(targetClass, to);
			this.co = co;
			this.ig = ig;
		}

		/**
		 * 指定されたオブジェクト間の各プロパティを比較する。
		 * 
		 * @param dto1
		 * @param dto2
		 * @return
		 */
		public boolean compareProperties(Object dto1, Object dto2) {
			boolean diff = false;
			try {
				// クラスに設定された名前スペースを取得する。
				Namespace classNs = DiffUtil.getAnnotationByAll(pa.getType(), Namespace.class);
				if (classNs != null) {
					switch (classNs.nameType) {
					case name:
						stackNameSpace.push(classNs.propName);
						break;
					case prop:
						{
							Object name1 = pa.getProperty(dto1, classNs.propName);
							Object name2 = pa.getProperty(dto2, classNs.propName);
							
							if (!name1.equals(name2)) {
								logger.error("Not mach namespaces");
								throw new IllegalStateException("unexpected");
							}
							
							stackNameSpace.push(name1.toString());
						}
					}
				}

				// クラスを探索し、プロパティの比較を行う。
				for (Method method : pa.getType().getMethods()) {
					// プロパティの get 関数に該当するか判定。
					if (DiffUtil.isPropGet(method)) {
						logger.debug("method: " + method.getName());

						// 比較除外リストが設定されいるか判定。
						if (ig != null) {
							boolean isIgnore = false;
							for (String propName: ig.propNames) {
								if (method.getName().substring("get".length()).compareToIgnoreCase(propName) == 0) {
									isIgnore = true;
									break;
								}
							}
							
							if (isIgnore) {
								continue;
							}
						}

						// プロパティに名前空間が設定されているか判定。
						Namespace methodNs = DiffUtil.getAnnotation(method, Namespace.class);
						if (methodNs != null) {
							switch (methodNs.nameType) {
							case name:
								stackNameSpace.push(methodNs.propName);
								break;
							case prop:
								{
									Object name1 = pa.getProperty(dto1, methodNs.propName);
									Object name2 = pa.getProperty(dto2, methodNs.propName);
	
									if (!name1.equals(name2)) {
										logger.error("Not mach namespaces");
										throw new IllegalStateException("unexpected");
									}
	
									stackNameSpace.push(name1.toString());
								}
							}
						}

						// 以降の処理で比較するプロパティを辿れるようにプロパティ名を保存する。
						stackColumn.push(method.getName().substring("get".length()));
						
						// 現在、Comparator は、比較のための十分な情報を実装クラスに提供しないので、使用しない。
						Comparator comparator = DiffUtil.getAnnotation(method, Comparator.class);
						if (comparator != null) {
							Comparator.DiffComparator dc = (Comparator.DiffComparator) comparator.compType.newInstance();
							Object prop1 = DiffUtil.getProperty(dto1, method);
							Object prop2 = DiffUtil.getProperty(dto2, method);
							diff = dc.compare(prop1, prop2, method.getReturnType(), currentResultC) || diff;
						} else {
							// プロパティの比較を行う。
							diff = compareProperty(dto1, dto2, method) || diff;
						}

						stackColumn.pop();

						if (methodNs != null) {
							stackNameSpace.pop();
						}
						
						// 作成された差分情報が10万を超える場合、継続するか確認
						if(!continueFlg && resultCnt > 100000){
							continueFlg = MessageDialog.openConfirm(null,
									Messages.getString("warning"),
									Messages.getString("message.traputil.26"));
							if(!continueFlg){
								logger.warn("message.traputil.27");
								throw new IllegalStateException("cancel");
							}
						}
					}
				}

				if (classNs != null) {
					stackNameSpace.pop();
				}
			} catch (InstantiationException | IllegalAccessException e) {
			} catch (Exception e) {
				throw new IllegalStateException("unexpected", e);
			}

			return diff;
			
		}

		/**
		 * 指定されたオブジェクトからプロパティを取得して比較を行う。
		 * 
		 * @param dto1
		 * @param dto2
		 * @param method
		 * @return
		 */
		public boolean compareProperty(Object dto1, Object dto2, Method method) {
			boolean diff = false;

			// Column が付加されている比較対象のプロパティか判定。
			Column column = null;
			if (co != null) {
				String propName = method.getName().substring("get".length());
				for (ColumnOverrides.Value v: co.values) {
					if (propName.compareToIgnoreCase(v.p) == 0) {
						column = v.c;
						break;
					}
				}
			}
			
			if (column == null) {
				column = DiffUtil.getAnnotation(method, Column.class);
			}
			
			boolean diffAsColumn = false;
			if (column != null) {
				logger.debug("column name: " + column.columnName);
			}

			// プロパティが配列か判定。
			ArrayId a = DiffUtil.getAnnotation(method, ArrayId.class);
			// 配列である場合
			if (a != null) {
				if (method.getReturnType().isArray()) {
					// 配列の比較には、配列の要素の型に Element が付加されている必要がある。
					if (DiffUtil.getAnnotationByAll(method.getReturnType().getComponentType(), Element.class) != null) {
						// 配列のプロパティを取得。
						Object[] array1 = (Object[]) DiffUtil.getProperty(dto1, method);
						Object[] array2 = (Object[]) DiffUtil.getProperty(dto2, method);

						// 配列の各要素を比較用に分類する。
						ArrayClassifier ac = null;
						boolean diffAsArray = false;
						if (array1 == null || array2 == null) {
							diffAsArray = array1 != array2;
						} else {
							ac = new ArrayClassifier(array1, array2, a, new PropertyAccessorImpl(method.getReturnType().getComponentType(), DiffUtil.getAnnotation(method, TranslateOverrides.class)));
							diffAsArray = ac.isEqual();
						}

						// 配列の差分チェック結果の出力可否を判断。
						if ((DiffUtil.isAll() && diffAsArray) || (column != null && (diffAsArray || DiffUtil.isAll()))) {
							ResultD resultD = new ResultD();
						
							resultD.setColumnId(stackColumn.toArray(new String[0]));
							resultD.setValueType(ResultD.ValueType.array);
							
							if (column == null) {
								resultD.setPropName(method.getDeclaringClass().getSimpleName() + '.' + method.getName());
								resultD.setResultType(ResultD.ResultType.warning);
							}
							else {
								resultD.setNameSpaces(stackNameSpace.toArray(new String[0]));
								resultD.setPropName(column.columnName);
								resultD.setResultType(diffAsArray ? ResultD.ResultType.diff: ResultD.ResultType.equal);
							}
							
							List<String> temp1 = new ArrayList<String>();
							for (ArrayClassifier.ArrayItem ai: ac.getList1()) {
								temp1.add(ai.id.toString());
							}
							List<String> temp2 = new ArrayList<String>();
							for (ArrayClassifier.ArrayItem ai: ac.getList2()) {
								temp2.add(ai.id.toString());
							}
							
							resultD.setValue1(temp1.toArray(new String[0]));
							resultD.setValue2(temp2.toArray(new String[0]));
							
							currentResultC.getResultDs().add(resultD);
							resultCnt++;
						}
						
						// 比較対象としてマークされていない場合は、配列の差分結果は反映しない。
						if (column != null) {
							diff = diffAsColumn = diffAsArray;
						}
						
						// 個々の配列要素の比較を行うか判断。
						if (ac != null && !a.terminate) {
							// 個々の項目に対して、差分チェック。
							for (ArrayClassifier.BothItem aitem : ac.getBoths()) {
								stackNameSpace.push(aitem.id.toString());
								stackColumn.push(aitem.id.toString());
								PropComparator pc = new PropComparator(method.getReturnType().getComponentType(), DiffUtil.getAnnotation(method, Ignore.class), DiffUtil.getAnnotation(method, TranslateOverrides.class), DiffUtil.getAnnotation(method, ColumnOverrides.class));
								diff = pc.compareProperties(aitem.item1, aitem.item2) || diff;
								stackColumn.pop();
								stackNameSpace.pop();
							}
						}
					} else {
						logger.debug("Component is not Element: " + method.getName() +" Retrun component tyep="+ method.getReturnType().getComponentType().getName());
						throw new IllegalStateException("unexpected : Component is not Element");
					}
				} else {
					logger.debug("Not array" + method.getName());
					throw new IllegalStateException("unexpected : Not array");
				}
			}
			// 要素の配列でない場合
			else {
				// プロパティの型に Element が付加されているか判断。
				// 付加されている場合は、取得できたプロパティ値が保有する各プロパティをさらに比較する。
				Element e = DiffUtil.getAnnotationByAll(method.getReturnType(), Element.class);

				// このプロパティに関して、比較をスルーしてよいか確認。
				if (column != null || e != null) {
					PropValue prop1 = pa.getProperty(dto1, method);
					PropValue prop2 = pa.getProperty(dto2, method);

					boolean diffAsProp = false;
					// xmlタグの差異の有無用チェックプロパティ
					boolean notDiffTags = false;
					// 対象となるプロパティはxmlに存在していたか確認
					boolean isProp1 = DiffUtil.getHasProp(dto1, method);
					boolean isProp2 = DiffUtil.getHasProp(dto2, method);

					// 両方あり、両方なしの場合は差分はない
					if ((isProp1 && isProp2) || (!isProp1 && !isProp2)) {
						notDiffTags = true;
					} else {
						diffAsColumn = true;
						diff = true;
					}
					// xmlタグに差異があるなら以降の比較処理はしない。
					if (notDiffTags) {
						if (prop1 == null || prop2 == null) {
							if (column != null) {
								if (prop1 == null && (prop2 != null && prop2.toString() == "")) {
									diff = diffAsColumn = false;
								}
								else if (prop2 == null && (prop1 != null && prop1.toString() == "")) {
									diff = diffAsColumn = false;
								}
								else {
									diff = diffAsColumn = prop1 != prop2;
								}
							}
							else {
								if (prop1 == null && (prop2 != null && prop2.toString().equals(""))) {
									diffAsProp = false;
								}
								else if (prop2 == null && (prop1 != null && prop1.toString().equals(""))) {
									diffAsProp = false;
								}
								else {
									diffAsProp = prop1 != prop2;
								}
							}
						} else {
							// カラムが付加されているプロパティなので、比較を実行。
							if (column != null) {
								diff = diffAsColumn = !prop1.equals(prop2);
							}
							else {
								// 取得したプロパティのプロパティをさらに比較。
								PropComparator pc = new PropComparator(method.getReturnType(), DiffUtil.getAnnotation(method, Ignore.class), DiffUtil.getAnnotation(method, TranslateOverrides.class), DiffUtil.getAnnotation(method, ColumnOverrides.class));
								diff = pc.compareProperties(prop1.getRealValue(), prop2.getRealValue());
							}
						}
					}

					// 配列の差分チェック結果の出力可否を判断。
					if ((DiffUtil.isAll() && diffAsProp) || (column != null && DiffUtil.isAll()) || diffAsColumn) {
						ResultD resultD = new ResultD();

						resultD.setColumnId(stackColumn.toArray(new String[0]));
						resultD.setValueType(ResultD.ValueType.simple);

						if (column == null) {
							resultD.setPropName(method.getDeclaringClass().getSimpleName() + '.' + method.getName());
							resultD.setResultType(ResultD.ResultType.warning);
						}
						else {
							resultD.setNameSpaces(stackNameSpace.toArray(new String[0]));
							resultD.setPropName(column.columnName);
							resultD.setResultType(diffAsColumn ? ResultD.ResultType.diff : ResultD.ResultType.equal);
						}
						
						// エラーメッセージについて、タグが存在していたかどうかで出力内容を変える
						if (isProp1) {
							resultD.setValue1(new String[]{prop1 == null ? "": prop1.toString()});
						} else {
							resultD.setValue1(new String[]{""});
						}
						if (isProp2) {
							resultD.setValue2(new String[]{prop2 == null ? "": prop2.toString()});
						} else {
							resultD.setValue2(new String[]{""});
						}

						currentResultC.getResultDs().add(resultD);
						resultCnt++;
					}
				}
			}

			logger.debug(method.getName() + ": " + (diffAsColumn ? "diff": "equal"));

			return diff;
		}
	}
}