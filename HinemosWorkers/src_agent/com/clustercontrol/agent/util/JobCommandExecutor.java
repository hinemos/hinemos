/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.agent.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
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
public class JobCommandExecutor {

	private static Log log = LogFactory.getLog(JobCommandExecutor.class);

	// コマンド実行時に子プロセスに引き渡す環境変数（key・value）
	private final Map<String, String> envMap = new ConcurrentHashMap<>();

	// thread for command execution
	private final ExecutorService _commandExecutor;

	private final String[] _command;
	private final String _commandLine; //ログ出力用コマンド文字列

	private final Charset _charset;
	public static final Charset _defaultCharset = Charset.forName("UTF-8");

	private final long _timeout;
	protected static final long _defaultTimeout = 30000;
	public static final long _disableTimeout = -1;

	private final int _bufferSize;
	public static final int _defaultBufferSize = 8120;

	public static final Object _runtimeExecLock = new Object();

	private Process process = null;

	private boolean _isLimit = true;
	private OutputString outputString;

	public JobCommandExecutor(String[] command) throws HinemosUnknown {
		this(command, _defaultCharset);
	}

	public JobCommandExecutor(String[] command, Charset charset) throws HinemosUnknown {
		this(command, charset, _defaultTimeout);
	}

	public JobCommandExecutor(String[] command, long timeout) throws HinemosUnknown {
		this(command, _defaultCharset, timeout);
	}

	public JobCommandExecutor(String[] command, Charset charset, long timeout) throws HinemosUnknown {
		this(command, charset, timeout, _defaultBufferSize);
	}

	public JobCommandExecutor(String[] command, Charset charset, long timeout, int bufferSize) throws HinemosUnknown {
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

	//標準出力の出力量に制限をもたすかの設定
	public void setLimitOutput(boolean isLimit) {
		this._isLimit = isLimit;
	}

	public void setOutputSetting(OutputString outputSetting) {
		this.outputString = outputSetting;
	}

	//ファイル出力の実行結果
	//when null if do not output
	public OutputString getOutputResult() {
		return outputString;
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
		CommandExecuteTask task = new CommandExecuteTask(process, _charset, _timeout, _bufferSize, _isLimit);
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

		// output string limit flag
		private boolean isLimit = false;

		private final Object _runtimeReadLock = new Object();

		public CommandExecuteTask(Process process, Charset charset, long timeout, int bufferSize, boolean isLimit) {
			this.charset = charset;
			this.timeout = timeout;
			this.bufferSize = bufferSize;
			this.process = process;
			this.isLimit = isLimit;

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

				StreamReader stdoutReader = new StreamReader(process.getInputStream(), "CommandStdoutReader", _bufferSize, outputString.stdout);
				StreamReader stderrReader = new StreamReader(process.getErrorStream(), "CommandStderrReader", _bufferSize, outputString.stderr);

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

			private OutputString.OutputFile outputFile;

			public StreamReader(InputStream is, String threadName, int bufferSize) {
				this(is, threadName, bufferSize, new OutputString.OutputFile());
			}

			public StreamReader(InputStream is, String threadName, int bufferSize, OutputString.OutputFile outputFile) {
				this.is = is;
				this.threadName = threadName;
				this.outputFile = outputFile;

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

				String outputStr;
				List<byte[]> outputList = new ArrayList<byte[]>();
				int total = 0;
				try {
					//書き出し対象のファイルが同じ場合、同時アクセスを防ぐため同期する
					if (outputString.isSameFileAndNotNull()) {
						synchronized (_runtimeReadLock) {
							total = readStream(outputList);
						}
					} else {
						total = readStream(outputList);
					}
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
					int outputSize;
					if (bufferSize < total) {
						outputSize = bufferSize;
					} else {
						outputSize = total;
					}
					byte[] output = new byte[outputSize];
					for (byte[] buf : outputList) {
						int copySize;
						if (buf.length < (output.length - offset)) {
							copySize = buf.length;
						} else {
							copySize = output.length - offset;
						}
						System.arraycopy(buf, 0, output, offset, copySize);
						offset += copySize;
					}

					int outputStrSize;
					if (bufferSize < total) {
						outputStrSize = bufferSize;
					} else {
						outputStrSize = total;
					}
					// 結合したバイト配列を文字列に変換（結合しないと配列をまたがるマルチバイト文字が文字化けする）
					String formedString = new String(output, 0, outputStrSize, charset);
					outputStr = formedString;
				}
				if (total > bufferSize) {
					bufferDiscarded = true;
					log.warn("discarding command's output. (buffer = " + bufferSize + "[byte] < total = " + total + "[byte])");
				}

				return outputStr;
			}

			private int readStream(List<byte[]> outputList) throws IOException {
				int total = 0;
				int size = 0;
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

					// ファイル書き出し
					if (outputFile.isAvailable) {
						if (!isLimit) {
							write(offset, output);
						} else if (total < bufferSize) {
							write(offset, output);
						}
					}

					// 全読み込みサイズを加算
					if (Integer.MAX_VALUE - offset < total) {
						total = Integer.MAX_VALUE;
					} else {
						total += offset;
					}
				}
				log.debug("reached end of stream.");
				return total;
				
			}

			/**
			 * 指定のファイルに結果を書き出します。
			 * @param outputLength 書き出す内容のサイズ長
			 * @param outputLine 書き出す内容(1024(読み取りバッファ長)で分けられたbyte配列)
			 */
			private void write(int outputLength, byte[] outputLine) {
				if (!outputFile.isSuccess) {
					return;
				}

				FileChannel fc = null;
				try {
					try {
						//出力サイズを切り詰める
						ByteBuffer outputBuffer = ByteBuffer.wrap(outputLine, 0, outputLength);
						//出力時のファイルエンコード
						CharBuffer charBuffer = charset.decode(outputBuffer);
						outputBuffer = charset.encode(charBuffer);
						// 書き込み専用のFileChannelオブジェクトオープン
						fc = FileChannel.open(
								Paths.get(outputFile.filename),
								StandardOpenOption.WRITE, StandardOpenOption.APPEND);
						//ファイル出力
						fc.write(outputBuffer);
						outputFile.outputLength += outputLength;
					} finally {
						if (fc != null) {
							try {
								fc.close();
							} catch (IOException e) {
								throw e;
							}
						}
					}
				} catch (Exception e) {
					log.warn("write() : " +outputFile.filename+ e.getMessage());
					outputFile.errorMessage = e.getMessage();
					outputFile.isSuccess = false;
				}
			}
		}
	}
}
