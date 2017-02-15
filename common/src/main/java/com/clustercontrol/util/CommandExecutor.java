/*

Copyright (C) 2012 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.fault.HinemosUnknown;

/**
 * General Command Executor Class
 */
public class CommandExecutor {

	private static Log log = LogFactory.getLog(CommandExecutor.class);
	
	// コマンド実行時に子プロセスに引き渡す環境変数（key・value）
	private final Map<String, String> envMap = new HashMap<String, String>();

	// thread for command execution
	private final ExecutorService _commandExecutor;

	private final String[] _command;
	private final String _commandLine; //ログ出力用コマンド文字列

	private final Charset _charset;
	public static final Charset _defaultCharset = Charset.forName("UTF-8");

	private final long _timeout;
	private static final long _defaultTimeout = 30000;
	public static final long _disableTimeout = -1;

	private final int _bufferSize;
	public static final int _defaultBufferSize = 8120;

	public static final Object _runtimeExecLock = new Object();

	private Process process = null;

	public CommandExecutor(String[] command) throws HinemosUnknown {
		this(command, _defaultCharset);
	}

	public CommandExecutor(String[] command, Charset charset) throws HinemosUnknown {
		this(command, charset, _defaultTimeout);
	}

	public CommandExecutor(String[] command, long timeout) throws HinemosUnknown {
		this(command, _defaultCharset, timeout);
	}

	public CommandExecutor(String[] command, Charset charset, long timeout) throws HinemosUnknown {
		this(command, charset, timeout, _defaultBufferSize);
	}

	public CommandExecutor(String[] command, Charset charset, long timeout, int bufferSize) throws HinemosUnknown {
		this._command = command;
		this._charset = charset;
		this._timeout = timeout;
		this._bufferSize = bufferSize;

		log.debug("initializing " + this);

		if (_command == null) {
			throw new NullPointerException("command is not defined : " + this);
		}
		
		StringBuilder commandStr = new StringBuilder();
		for (String arg : _command) {
			commandStr.append(' ');
			commandStr.append(arg);
		}
		this._commandLine = commandStr.substring(1); //先頭の空白を取り除いて格納する

		
		if (_charset == null) {
			throw new NullPointerException("charset is not defined : " + this);
		}
		
		_commandExecutor = Executors.newSingleThreadExecutor(
				new ThreadFactory() {
					private volatile int _count = 0;
					@Override
					public Thread newThread(Runnable r) {
						return new Thread(r, "CommandExecutor-" + _count++);
					}
				}
				);
	}
	@Override
	public String toString() {
		return this.getClass().getCanonicalName() + " [command = " + Arrays.toString(_command)
				+ ", charset = " + _charset
				+ ", timeout = " + _timeout
				+ ", bufferSize = " + _bufferSize
				+ "]";
	}

	public Process getProcess() {
		return this.process;
	}
	
	public void addEnvironment(String key, String value) {
		envMap.put(key, value);
	}
	
	public Process execute() throws HinemosUnknown {
		// workaround for JVM(Windows) Bug
		// Runtime#exec is not thread safe on Windows.
	
		try {
			synchronized (_runtimeExecLock) {
				ProcessBuilder pb = new ProcessBuilder(_command);
				// 子プロセスには環境変数"_JAVA_OPTIONS"は渡さない
				pb.environment().remove("_JAVA_OPTIONS");
				for (Map.Entry<String, String> entry : envMap.entrySet()) {
					pb.environment().put(entry.getKey(), entry.getValue());
				}
				process = pb.start();
			}
		} catch (Exception e) {
			log.warn("command executor failure. (command = " + _commandLine + ") " + e.getMessage(), e);
			throw new HinemosUnknown(e.getMessage());
		}
		return process;
	}

	public CommandResult getResult() {
		CommandExecuteTask task = new CommandExecuteTask(process, _charset, _timeout, _bufferSize);
		Future<CommandResult> future = _commandExecutor.submit(task);
		log.debug("executing command. (command = " + _commandLine + ", timeout = " + _timeout + " [msec])");

		// receive result
		CommandResult ret = null;
		try {
			if (_timeout == _disableTimeout) {
				ret = future.get();
			} else {
				ret = future.get(_timeout, TimeUnit.MILLISECONDS);
			}

			log.debug("exit code : " + (ret != null ? ret.exitCode : null));
			log.debug("stdout : " + (ret != null ? ret.stdout : null));
			log.debug("stderr : " + (ret != null ? ret.stderr : null));
			log.debug("buffer discarded : " + (ret != null ? ret.bufferDiscarded : null));
		} catch (TimeoutException e) {
			log.info("command execution failure. (command = " + _commandLine + ") " + e.getMessage());
		} catch (Exception e) {
			log.warn("command execution failure. (command = " + _commandLine + ") " + e.getMessage(), e);
		} finally {
			// release thread pool
			log.debug("releasing command threads.");
			_commandExecutor.shutdownNow();
		}

		return ret;
	}

	/**
	 * Command Result Class
	 */
	public static class CommandResult {
		public final Integer exitCode;			// null when command timeout
		public final String stdout;				// null when command timeout
		public final String stderr;				// null when command timeout
		public final boolean bufferDiscarded;	// true when buffer is discarded

		public CommandResult(int exitCode, String stdout, String stderr, boolean bufferDiscarded) {
			this.exitCode = exitCode;
			this.stdout = stdout;
			this.stderr = stderr;
			this.bufferDiscarded = bufferDiscarded;
		}
	}

	/**
	 * Command Execute Task
	 */
	public class CommandExecuteTask implements Callable<CommandResult> {

		// command timeout (receive timeout)
		private final long timeout;

		// maximun size of received string
		public final int bufferSize;

		// buffer discarded or not
		public boolean bufferDiscarded = false;

		// charset of receive string
		public final Charset charset;

		public final Process process;

		// thread for receive stdout and stderr
		private final ExecutorService _receiverService;

		public CommandExecuteTask(Process process, Charset charset, long timeout, int bufferSize) {
			this.charset = charset;
			this.timeout = timeout;
			this.bufferSize = bufferSize;
			this.process = process;

			log.debug("initializing " + this);

			_receiverService = Executors.newFixedThreadPool(
					2,
					new ThreadFactory() {
						private volatile int _count = 0;
						@Override
						public Thread newThread(Runnable r) {
							return new Thread(r, "CommendExecutor-" + _count++);
						}
					}
					);
		}

		@Override
		public String toString() {
			return this.getClass().getCanonicalName() + " [command = " + Arrays.toString(_command)
					+ ", charset = " + charset
					+ ", timeout = " + timeout
					+ ", bufferSize = " + bufferSize
					+ "]";
		}

		/**
		 * execute command
		 */
		@Override
		public CommandResult call() {
			Process process = this.process;

			Future<String> stdoutTask = null;
			Future<String> stderrTask = null;

			Integer exitCode = null;
			String stdout = null;
			String stderr = null;

			try {
				log.debug("starting child process : " + this);

				StreamReader stdoutReader = new StreamReader(process.getInputStream(), "CommandStdoutReader", _bufferSize);
				StreamReader stderrReader = new StreamReader(process.getErrorStream(), "CommandStderrReader", _bufferSize);
				stdoutTask = _receiverService.submit(stdoutReader);
				stderrTask = _receiverService.submit(stderrReader);

				log.debug("waiting child process : " + _commandLine);
				exitCode = process.waitFor();
				log.debug("child process exited : " + _commandLine);

				if (timeout == _disableTimeout) {
					stdout = stdoutTask.get();
					stderr = stderrTask.get();
				} else {
					stdout = stdoutTask.get(timeout, TimeUnit.MILLISECONDS);
					stderr = stderrTask.get(timeout, TimeUnit.MILLISECONDS);
				}
			} catch (InterruptedException e) {
				log.info("command executor failure. (command = " + _commandLine + ") "+ e.getMessage());
				stdout = "";
				stderr = "Internal Error : " + e.getMessage();
				exitCode = -1;
			} catch (Exception e) {
				log.warn("command executor failure. (command = " + _commandLine + ") " + e.getMessage(), e);
				stdout = "";
				stderr = "Internal Error : " + e.getMessage();
				exitCode = -1;
			} finally {
				log.debug("canceling stdout and stderr reader.");
				if(stdoutTask != null){
					stdoutTask.cancel(true);
				}
				if(stderrTask != null){
					stderrTask.cancel(true);
				}
				if (process != null && process.getOutputStream() != null) {
					try {
						process.getOutputStream().close();
					} catch (IOException e) {
					}
				}
				if (process != null) {
					log.debug("destroying child process.");
					process.destroy();
				}
				// release thread pool
				log.debug("releasing receiver threads.");
				_receiverService.shutdownNow();
			}

			return new CommandResult(exitCode, stdout, stderr, bufferDiscarded);
		}

		/**
		 * STDOUT and STDERR Reader Class<br/>
		 */
		public class StreamReader implements Callable<String> {

			// stream of stdout or stderr
			private final InputStream is;

			private final String threadName;

			private int tmpBufSize = 1024;
			
			public StreamReader(InputStream is, String threadName, int bufferSize) {
				this.is = is;
				this.threadName = threadName;

				log.debug("initializing " + this);
			}

			@Override
			public String toString() {
				return this.getClass().getCanonicalName() + " [threadName = " + threadName
						+ ", stream = " + is
						+ "]";
			}

			@Override
			public String call() {
				Thread.currentThread().setName(threadName);

				ArrayList<byte[]> outputList = new ArrayList<byte[]>();
				String outputStr = null;

				int size = 0;
				int total = 0;

				try {
					// read stream
					// until EOF (because some programs will be effected when stream is closed on running state)
					while (size != -1) {
						// receive buffer from stream
						byte[] output = new byte[tmpBufSize];
						int offset = 0;
						
						//tmpBufSizeバイトの配列につめていく
						while ((size = is.read(output, offset, tmpBufSize - offset)) != -1) {
							log.debug("coping to output as stout/stderr. (offset = " + offset + ", size = "  + size + "[byte])");
							
							// offset position
							offset += size;
							
							if (offset >= tmpBufSize) {
								// バイト列にすべてつめたらループを抜ける
								break;
							}

						}

						if (total < bufferSize) {
							// 最大読み込みバイト数に達していなかったらバッファリストに追加する
							// 最大読み込みバイト数に達している場合、バッファリストには追加しないが、コマンドからは継続して出力内容を受け取る必要があるため処理は中断しない
							outputList.add(output); 
						}
					
						// 全読み込みサイズを加算
						total += offset; 
					}
					log.debug("reached end of stream.");
				} catch (IOException e) {
					log.warn("reading stream failure... " + e.getMessage(), e);
				} catch (Exception e) {
					log.warn("unexpected failure... " + e.getMessage(), e);
				} finally {
					if (is != null) {
						try {
							log.debug("closing a stream.");
							is.close();
						} catch (IOException e) {
							log.warn("closing stream failure... " + e.getMessage(), e);
						}
					}
				}

				// byte to string
				if (total == 0) {
					outputStr = "";
				} else {
					// tmpBufferSizeごとの配列のリストをひとつのバイト配列に結合する
					int offset = 0;
					byte[] output = new byte[(bufferSize < total ? bufferSize
							: total)];
					for (byte[] buf : outputList) {
						int copySize = (buf.length < (output.length - offset) ? buf.length : output.length - offset);
						System.arraycopy(buf, 0, output, offset, copySize);
						offset += copySize;
					}

					// 結合したバイト配列を文字列に変換（結合しないと配列をまたがるマルチバイト文字が文字化けする）
					outputStr = new String(output, 0,
							(bufferSize < total ? bufferSize : total), charset);
				}
				if (total > bufferSize) {
					bufferDiscarded = true;
					log.warn("discarding command's output. (buffer = " + bufferSize + "[byte] < total = " + total + "[byte])");
				}

				return outputStr;
			}
		}
	}
}
