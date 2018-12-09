/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.settings;

import java.io.ByteArrayOutputStream;
import java.io.CharArrayWriter;
import java.io.PrintStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.WriterAppender;

import com.clustercontrol.util.Messages;
import com.clustercontrol.utility.util.Config;


/**
 * コマンドに与えられた引数を解析し、適切なアクションを実行するヘルパークラス。<br>
 * 
 * Action クラス名
 * オペレーション種別
 * 接続先
 * ユーザー名
 * パスワード
 * "HTTP.CONNECT.TIMEOUT"
 * "HTTP.REQUEST.TIMEOUT"
 * XML ファイル
 * 
 * @version 6.0.0
 * @since 2.0.0
 * 
 * 
 */
public class WSActionLauncher {
	private static Logger logger = Logger.getLogger(WSActionLauncher.class);
	
	private String m_stdout = "";
	private String m_errout = "";
	
	/**
	 * 処理実行時のイベントのリスナー。<br>
	 */
	private interface EventListener {
		void onInvalidArgs(String args[]);
		void onNotFounfClass(String className);
		void onNotFoundMethod(String className, Operation commandType);
		void onComplete(String className, Operation commandType);
		void onError(String className, Operation commandType);
	}
	
	/**
	 * 処理実行時のイベントに合わせたメッセージを出力するクラス。<br>
	 */
	private static class ErrorMessageProvider implements EventListener {
//		Log logger;
		private Logger logger;
		
		private ErrorMessageProvider(Logger logger) {
			this.logger = logger;
		}

		@Override
		public void onInvalidArgs(String args[]) {
			logger.error("Usage : ClassName OperationType Url AccountName Password ConnectionTimeout RequestTimeout [XML...]");
		}

		@Override
		public void onNotFounfClass(String className) {
			logger.error("Don't find " + className);
		}

		@Override
		public void onNotFoundMethod(String className, Operation operarion) {
			logger.error("Don't find " + operarion.name() + " method in " + className);
		}

		@Override
		public void onComplete(String className, Operation commandType) {
			switch (commandType) {
			case Clear:
				logger.info(className + " : " + Messages.getString("SettingTools.ClearCompleted"));
				break;
			case Import:
				logger.info(className + " : " + Messages.getString("SettingTools.ImportCompleted"));
				break;
			case Export:
				logger.info(className + " : " + Messages.getString("SettingTools.ExportCompleted"));
				break;
			case Diff:
				logger.info(className + " : " + Messages.getString("SettingTools.DiffCompleted"));
				break;
			default:
				logger.info(className + " : " + "Complete process. Dont't find an appropriate message");
				break;
			}
		}
		
		@Override
		public void onError(String className, Operation commandType) {
			logger.error(className + " : " + Messages.getString("SettingTools.EndWithErrorCode"));
		}
	}
	
	private enum Operation {
		Import("import", ImportMethod.class),
		Export("export", ExportMethod.class),
		Clear("clear", ClearMethod.class),
		Diff("diff", DiffMethod.class),
		Invalid("invalid", null);
		
		private Operation(String commandName, Class<?> annoClass) {this.commandName = commandName; this.annoClass = annoClass;}
		public final String commandName;
		public final Class<?> annoClass;
	};

	private String args[];
	private EventListener ev;

	/**
	 * コンストラクター <br>
	 * 
	 * @param コマンドに渡された引数
	 */
	public WSActionLauncher(String[] args)
	{
		this.args = args;
		this.ev = new ErrorMessageProvider(logger);
	}

	private static Operation checkCommandType(String commandName) {
		for (Operation operation: Operation.values()) {
			if (operation.commandName.equalsIgnoreCase(commandName)) {
				return operation;
			}
		}
		return Operation.Invalid;
	}

	/**
	 * アクションを実行する。<BR>
	 */
	public int action() throws ConvertorException  {
		logger.debug("enter ActionLauncher.action method");

		// コマンドに指定された実行種別を検索。
		Operation commandType = checkCommandType(args[1]);
		if (commandType == Operation.Invalid) {
			ev.onInvalidArgs(args);
			throw new ConvertorException(SettingConstants.ERROR_INVALID_ARGS);
		}

		String files[] = null;
		
		switch (commandType) {
		case Diff:
			{
				// 最低限必要な数が指定されているか判定
				if (args.length < 2) {
					ev.onInvalidArgs(args);
					throw new ConvertorException(SettingConstants.ERROR_INVALID_ARGS);
				}

				// 引数のファイル名を抽出。
				files = new String[args.length - 2];
				for (int i = 2; i < args.length; ++i) {
					files[i - 2] = args[i];
				}
			}
			break;
		default:
			{
				// 最低限必要な数が指定されているか判定
				if (args.length < 6) {
					ev.onInvalidArgs(args);
					throw new ConvertorException(SettingConstants.ERROR_INVALID_ARGS);
				}
				
				// 引数のファイル名を抽出。
				files = new String[args.length - 6];
				for (int i = 6; i < args.length; ++i) {
					files[i - 6] = args[i];
				}

				// 環境情報を追加。
				Config.putConfig("Login.URL", args[2]);
				Config.putConfig("Login.USER", args[3]);
				Config.putConfig("HTTP.CONNECT.TIMEOUT", args[4]);
				Config.putConfig("HTTP.REQUEST.TIMEOUT", args[5]);
			}
			break;
		}
		
		// アクションクラスの Class を取得。
		Class<?> actionClass = null;
		try {
			actionClass = Class.forName(args[0]);
		}
		catch (Exception e) {
			ev.onNotFounfClass(args[0]);
			throw new ConvertorException(e, SettingConstants.ERROR_INVALID_ARGS);
		}catch(Throwable t){
			ev.onNotFounfClass(args[0]);
			throw new ConvertorException(null, SettingConstants.ERROR_INVALID_ARGS);
		}

		// アクションクラス作成
		Object sda = null;
		try {
			sda = actionClass.newInstance();
		}
		catch (Exception e) {
			ev.onInvalidArgs(args);
			throw new ConvertorException(e, SettingConstants.ERROR_INVALID_ARGS);
		}

		// アクション クラスのメソッドを呼び出す。
		int retValue = SettingConstants.ERROR_INPROCESS;
		boolean isCalled = false;
		END_LOOP:
		for (Class<?> clazz = sda.getClass(); clazz != Object.class; clazz = clazz.getSuperclass()) {
			Method[] methods = clazz.getMethods();
			for (Method method : methods) {
				// メソッドのアノテーションを検索して、指定された実行種別に該当するメソッドを検索。
				for (Annotation annotation : method.getAnnotations()) {
					if (annotation.annotationType().equals(commandType.annoClass)) {
						// メソッドのフォーマットをチェック。
						assert checkMethodFormat(method) : "illegal method format : int methodName(String parame1, String param2, .....)";
						// コマンドに指定されたオプションの数とメソッドの引数の個数が一致しているか判定。
						if (files.length != method.getParameterTypes().length) {
							ev.onInvalidArgs(args);
							throw new ConvertorException(SettingConstants.ERROR_INVALID_ARGS);
						}
						Object[] params = new String[method.getParameterTypes().length];
						for (int i = 0; i < method.getParameterTypes().length; ++i) {
							params[i] = files[i];
						}
						// リフレクション経由で呼び出し。
						Logger logger = null;
						CharArrayWriter byteWriter = new CharArrayWriter(8192);
						WriterAppender appender = new WriterAppender(new PatternLayout("%d %-5p [%t] [%c] %m%n"), byteWriter);
						PrintStream oldError = System.err;
						String out = "";
						String error = "";
						try {
							Method getLoggerMethod = actionClass.getMethod("getLogger", new Class<?>[0]);
							logger = (Logger)getLoggerMethod.invoke(sda, new Object[0]);
							
							appender.setEncoding("UTF-8");
							logger.addAppender(appender);

							ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
							System.setErr(new PrintStream(byteStream));
							
							Object returnValue = method.invoke(sda, params);
							retValue = (Integer)returnValue;
							isCalled = true;
							
							out = byteWriter.toString();
							error = byteStream.toString("UTF-8");
						}
						catch (Exception e) {
							throw new ConvertorException(e, SettingConstants.ERROR_INVALID_ARGS);
						}
						finally {
							if (out != null) {
								m_stdout = out;
							}
							
							if (error != null) {
								m_errout = error;
							}

							if (logger != null) {
								logger.removeAppender(appender);
							}
							
							System.setErr(oldError);
						}
	
						break END_LOOP;
					}
				}
			}
		}
		
		// 該当するメソッドを検索し、呼び出したか判定。
		if (!isCalled) {
			ev.onNotFoundMethod(args[0], commandType);
			throw new ConvertorException(SettingConstants.ERROR_INVALID_ARGS);
		}
		
		// 処理の終了
		if ((retValue >= SettingConstants.SUCCESS) && (retValue <= SettingConstants.SUCCESS_MAX) ) {
			ev.onComplete(args[0], commandType);
		} else {
			ev.onError(args[0], commandType);
		}
		
		logger.debug("leave ActionLauncher.action method");

		return retValue;
	}
	
	/**
	 * アクションメソッドのシグニチャーをチェックする。 <br>
	 * 
	 * @param アクションメソッドの定義。
	 * @return 適合している場合、true。
	 */
	private static boolean checkMethodFormat(Method method) {
		for (Class<?> paramType: method.getParameterTypes()) {
			if (!paramType.equals(String.class)) {
				return false;
			}
		}

		Class<?> returnType = method.getReturnType();
		if (!(returnType != null && returnType.equals(int.class))) {
			return false;
		}
		
		return true;
	}
	
	public String getStdOut() {
		return m_stdout;
	}

	public String getErrOut() {
		return m_errout;
	}
}