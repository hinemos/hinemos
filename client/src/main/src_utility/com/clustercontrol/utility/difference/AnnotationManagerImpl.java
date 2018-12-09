/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.difference;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.utility.difference.anno.AnnoSubstitute;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

/**
 * DTO に関連づいた目印をロード、管理するクラス。
 * 
 * @version 2.0.0
 * @since 2.0.0
 * 
 *
 */
public class AnnotationManagerImpl implements AnnotationManager {
	private static Log logger = LogFactory.getLog(AnnotationManagerImpl.class);

	/**
	 * DTO の各クラスに付加された目印を保持する。
	 */
	private Map<Class<?>, AnnoSubstitute[]> classDescs = new HashMap<Class<?>, AnnoSubstitute[]>();

	/**
	 * DTO の各プロパティに付加された目印を保持する。
	 */
	private Map<Method, AnnoSubstitute[]> getMethodsDescs = new HashMap<Method, AnnoSubstitute[]>();

	/**
	 * 既に読み込みを試みたクラスのセット。
	 */
	private Set<Class<?>> excludeClasses = new HashSet<Class<?>>();

	@Override
	public AnnoSubstitute[] getClassAnno(Class<?> clazz) {
		AnnoSubstitute[] anno = classDescs.get(clazz);
		if (anno == null) {
			addAnnotation(clazz);
			
			anno = classDescs.get(clazz);
		}
		
		return anno;
	}

	@Override
	public AnnoSubstitute[] getPropAnno(Method method) {
		AnnoSubstitute[] anno = getMethodsDescs.get(method);
		if (anno == null) {
			addAnnotation(method.getDeclaringClass());
			
			anno = getMethodsDescs.get(method);
		}
		
		return anno;
	}
	
	private void addAnnotation(Class<?> clazz) {
		// 指定されたクラスで既に目印の読み込みを行ったか判定。
		if (excludeClasses.contains(clazz)) {
			return;
		}

		//　読み込みを試みるので、実施済みリストに指定したクラスを追加。
		excludeClasses.add(clazz);

		// アノテーションで指定されている目印を読み込む。
		if (!addDiffAnnotation(clazz)) {
			// アノテーションで指定されていないので、json ファイルから目印を読み込む。
			loadAnnotation(clazz);
		}
	}
	
	/**
	 * 指定されたクラスに関連する目印を読み込む。
	 * 
	 * @param clazz
	 */
	private void loadAnnotation(Class<?> clazz) {
		// Jackson を利用し、json から、目印の設定を読み込む。
		ClassDescription cd = loadClassDescription(clazz);
		if (cd == null) {
			return;
		}

		// 読み込んだ設定を、クラスとプロパティに関連付ける。
		classDescs.put(clazz, cd.annotations);

		// プロパティの目印を取得し、関連づけを行う。
		List<PropDescription> tempPDs = new ArrayList<PropDescription>(Arrays.asList(cd.props));
		
		int superCnt = 0;//SuperClassのMethodも調べる必要がある。１階層上まで。
		while (superCnt <2 ){
			for (Method method : clazz.getDeclaredMethods()) {
				if (tempPDs.size() == 0) {
					break;
				}
	
				Iterator<PropDescription> pdIter = tempPDs.iterator();
				while (pdIter.hasNext()) {
					PropDescription pd = pdIter.next();
	
					// プロパティの getter 関数を取得。
					String getMethodName = "get" + pd.propName;
					// とりあえず大文字、小文字を無視してプロパティのget 関数を検索。
					if (method.getName().compareToIgnoreCase(getMethodName) == 0 && DiffUtil.isPropGet(method)) {
						getMethodsDescs.put(method, pd.annotations);
						pdIter.remove();
						superCnt =2;
						break;
					}
				}
			}
			clazz = clazz.getSuperclass();
			superCnt++;
		}

		// 該当するプロパティが存在しないので、エラーを出力。
		if (tempPDs.size() > 0) {
			StringBuilder sb = new StringBuilder();
			sb.append("Not Exsist:");
			for (PropDescription pd : tempPDs) {
				sb.append(" ");
				sb.append(pd.propName);
			}
			throw new NoSuchMethodError(sb.toString());
		}
	}
	
	/**
	 * 指定したクラスに対応する json ファイルを取得し、目印情報を読み込む。
	 * 
	 * @param clazz
	 * @return
	 */
	private ClassDescription loadClassDescription(Class<?> clazz) {
		InputStream in = null;
		
		try {
			// 目印情報が格納されているパスを作成。
			StringBuilder sb = new StringBuilder();
			// 格納先のルートフォルダ名
			sb.append("desc");
			
			Package p = clazz.getPackage();
			if (p != null) {
				sb.append("/");
				
				// パッケージ名の "." を "_" に変換し、フォルダ名を作成する。
				String pp = p.getName().replace('.', '_');
				sb.append(pp);
			}

			sb.append("/");
			sb.append(clazz.getSimpleName());
			sb.append(".json");

			String path = sb.toString();
			
			logger.debug("read: " + path);

			in = DiffUtil.class.getResourceAsStream(path);
			if (in == null) {
				logger.debug("Don't exist.");
				return null;
			}
			else {
				logger.debug("complete.");
			}
			
			// Jackson で、json 形式のファイルを読み込み。
			ObjectMapper om = new ObjectMapper();
			ObjectReader or = om.readerFor(ClassDescription.class);
			ClassDescription cd = or.readValue(new InputStreamReader(in, "UTF-8"));
			
			return cd;
		}
		catch (Exception e) {
			throw new IllegalStateException(e);
		}
		finally {
			if (in != null) {
				try {
					in.close();
				}
				catch (Exception e) {
				}
			}
		}
	}
	
	@Override
	public void addClassAnnos(Class<?> clazz, AnnoSubstitute[] annos) {
		classDescs.put(clazz, annos);
	}

	@Override
	public void addPropAnnos(Method method, AnnoSubstitute[] annos) {
		getMethodsDescs.put(method, annos);
	}
	
	/**
	 * 指定したクラスにアノテーションを介して目印が設定されていないか確認。
	 * 
	 * @param clazz
	 * @return
	 */
	private boolean addDiffAnnotation(Class<?> clazz) {
		// 目印情報を保持するアノテーションを取得。
		DiffAnnotation da = clazz.getAnnotation(DiffAnnotation.class);
		if (da == null) {
			return false;
		}
		
		ObjectMapper om = new ObjectMapper();
		ObjectReader or = om.readerFor(AnnoSubstitute.class);

		// クラスに関連づいている目印を読み込む。
		List<AnnoSubstitute> casl = new ArrayList<AnnoSubstitute>();
		for (String def: da.value()) {
			try {
				// アノテーションが保持する目印情報を、Jackson を経由して、Java のクラスへ変換する。
				AnnoSubstitute as = or.readValue(def);
				casl.add(as);
			}
			catch (Exception e) {
				throw new IllegalStateException("unexpected", e);
			}
		}
		
		if (casl.size() <= 0) {
			return false;
		}

		// プロパティの get 関数に関連づいている目印を読み込む。
		Map<Method, List<AnnoSubstitute>> masm = new HashMap<Method, List<AnnoSubstitute>>();
		for (Method m: clazz.getDeclaredMethods()) {
			DiffAnnotation mda = m.getAnnotation(DiffAnnotation.class);
			if (mda == null) {
				continue;
			}
			
			List<AnnoSubstitute> masl = new ArrayList<AnnoSubstitute>();
			masm.put(m, masl);

			for (String def: mda.value()) {
				try {
					AnnoSubstitute as = or.readValue(def);
					masl.add(as);
				}
				catch (Exception e) {
					throw new IllegalStateException("unexpected", e);
				}
			}
		}
		
		// クラスに目印情報を関連付ける。
		this.addClassAnnos(clazz, casl.toArray(new AnnoSubstitute[0]));

		// プロパティに目印情報を関連付ける。
		for (Map.Entry<Method, List<AnnoSubstitute>> entry: masm.entrySet()) {
			this.addPropAnnos(entry.getKey(), entry.getValue().toArray(new AnnoSubstitute[0]));
		}

		return true;
	}
}