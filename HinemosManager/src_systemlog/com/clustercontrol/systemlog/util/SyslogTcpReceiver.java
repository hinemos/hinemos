/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.systemlog.util;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;

import org.apache.log4j.Logger;

/*
 * Tcp 受信用クラス。
 */
public class SyslogTcpReceiver {
	private static Logger logger = Logger.getLogger(SyslogTcpReceiver.class);

	// 受信数のカウンタ
	private Counter counter;
	
	// メイン処理用スレッド。
	private Thread mainThread;
	
	// リッスン用
	private String address;
	private int port;
	
	// 読み取り時のブロックのタイムアウト。
	private int readTimeout = 10000;
	
	// アクセプトのタイムアウト。
	private static int ACCEPT_TIMEOUT = 1000;

	// 読み取り時の最大サイズ
	private int maxReadSize = -1;
	
	// 読込用の一時バッファーサイズ(8k)
	private static final int READ_BUFFER_SIZE = 8192;
	
	// 受信した結果を読み込んで、さらにハンドラーに渡す処理用のスレッドプール。
	private ExecutorService worker;
	
	// 読み込んだ情報を処理するハンドラー。
	private ResponseHandler<byte[]> handler;
	
	// 読み込んだ情報を処理するハンドラー。
	private ResponseHandler<Exception> exceptionHandler;
	
	// ループの継続判定用。
	private boolean loop = true;
	
	// 出力用文字セット
	private SyslogPacketHelper parser = new SyslogPacketHelper();
	
	private CountDownLatch sync = new CountDownLatch(1);
	
	// ループの継続判定用。
	private List<Byte> delimiteres;
	
	/*
	 * ソケットアクセプトのメイン処理。
	 */
	private class AcceptTask implements Runnable {
		private ServerSocket server;
		
		public AcceptTask(ServerSocket server) {
			this.server = server;
		}
		
		@Override
		public void run() {
			sync.countDown();
			
			logger.info(String.format("listening() : start listening syslog packet using tcp. address=%s, port=%d", address, port));
			try (ServerSocket closable = server) {
				while (loop) {
					Socket client;
					try {
						client = server.accept();
						if (logger.isTraceEnabled()) {
							logger.trace(String.format("listen() : accepted. %s", client.toString()));
						}
						
						// 読み取り中のハングをタイムアウトさせる。
						client.setSoTimeout(readTimeout);
					} catch(SocketTimeoutException e) {
						// 受信したカウントを出力
						if (logger.isDebugEnabled()) {
							if ((System.currentTimeMillis() / 1000) % 30 == 0) {
								logger.debug(counter.toString());
							}
						}
						continue;
					} catch(IOException e) {
						logger.warn("Faile to accept. " + SyslogTcpReceiver.this.toString(), e);
						try {
							Thread.sleep(1000);
						} catch(InterruptedException e1) {
						}
						continue;
					}
					
					Runnable reciever = createReadingTask(client, handler);
					try {
						worker.execute(reciever);
					} catch(RejectedExecutionException e) {
						logger.warn("accept() : rejected from worker-thread-pool. " + e.getMessage());
					}
				}
			} catch(RuntimeException | IOException e) {
				logger.warn("Faile to start SyslogTcpReceiver. " + SyslogTcpReceiver.this.toString(), e);
			}
			logger.info(String.format("listen() : stopped. %s", SyslogTcpReceiver.this.toString()));
		}
	};
	
	/*
	 * 送信バッファーの管理クラス。
	 */
	private static class Buffer {
		private List<ByteBuffer> buffers = new ArrayList<>();
		private final int maxBufferSize;
		private int remain;
		private boolean overflow;
		
		/*
		 * maxReadSize を 0 以下にすると、無制限。
		 */
		public Buffer(int maxBufferSize) {
			this.maxBufferSize = maxBufferSize;
			if (maxBufferSize <= 0) {
				this.remain = Integer.MAX_VALUE;
			} else {
				this.remain = maxBufferSize;
			}
		}
		
		public Buffer put(ByteBuffer buffer) {
			if (maxBufferSize <= 0) {
				buffers.add(buffer);
			} else {
				if (remain > 0) {
					if (remain < buffer.position()) {
						ByteBuffer b = ByteBuffer.allocate(remain);
						overflow = remain < buffer.position();
						b.put(buffer.array(), 0, Math.min(remain, buffer.position()));
						buffers.add(b);
						remain = 0;
					} else {
						buffers.add(buffer);
						remain -= buffer.position();
					}
				}
			}
			return this;
		}
		
		public Buffer put(byte[] src, int offset, int length) {
			if (maxBufferSize <= 0) {
				ByteBuffer b = ByteBuffer.allocate(length);
				b.put(src, offset, length);
				buffers.add(b);
			} else {
				if (remain > 0) {
					if (remain < length) {
						ByteBuffer b = ByteBuffer.allocate(remain);
						overflow = remain < length;
						b.put(src, offset, Math.min(remain, length));
						buffers.add(b);
						remain = 0;
					} else {
						ByteBuffer b = ByteBuffer.allocate(length);
						b.put(src, offset, length);
						buffers.add(b);
						remain -= length;
					}
				}
			}
			return this;
		}
		
		public byte[] array() {
			int sum = 0;
			for (ByteBuffer buff: buffers) {
				buff.flip();
				sum += buff.limit();
			}
			
			ByteBuffer joined = ByteBuffer.allocate(sum);
			for (ByteBuffer buff: buffers) {
				joined.put(buff);
			}
			
			return joined.array();
		}
		
		public void reset() {
			if (maxBufferSize <= 0) {
				this.remain = Integer.MAX_VALUE;
			} else {
				this.remain = maxBufferSize;
			}
			buffers.clear();
			overflow = false;
		}
		
		@SuppressWarnings("unused")
		public int remain() {
			return remain;
		}
		
		public boolean isOverflow() {
			return overflow;
		}
		
		public int getBufferSize() {
			return maxBufferSize == -1 ? Integer.MAX_VALUE: maxBufferSize;
		}
		
		public static Buffer build(int maxReadSize) {
			return new Buffer(maxReadSize);
		}
	}
	
	/*
	 * ワーカー上で実行する読込処理。
	 */
	private class ReadingTask implements Runnable {
		private Socket client;
		private ResponseHandler<byte[]> handler;
		
		// シスログヘッダー用データフォーマット。
		private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d HH:mm:ss", Locale.ENGLISH);
		
		public ReadingTask(Socket client, ResponseHandler<byte[]> handler) {
			this.client = client;
			this.handler = handler;
			this.dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
		}
		
		@Override
		public void run() {
			try {
				// ByteBuffer が便利なので、channel に変換。
				ReadableByteChannel in = Channels.newChannel(client.getInputStream());
				
				boolean reading = true;
				boolean dropping = false;
				
				// ソケットから読み込むためのバッファー。
				ByteBuffer readBuffer = ByteBuffer.allocate(READ_BUFFER_SIZE);
				
				// 送信バッファー。
				Buffer sendBuffer = Buffer.build(maxReadSize);
				
				while (reading) {
					List<byte[]> messages = new ArrayList<>();
					try {
						readBuffer.clear();
						int len = in.read(readBuffer);
						
						if (len < 0) {
							// コネクションが閉じたので受信した内容を送信。
							if (logger.isTraceEnabled()) {
								logger.trace(String.format("read() : complete reading. %s", client.toString()));
							}
							
							if (!dropping) {
								byte[] message = sendBuffer.array();
								if (message.length > 0) {
									messages.add(message);
									
									// コネクションがクローズしたので、とりあえず成功とする。
									counter.incrementSuccess();
								}
							}
							
							reading = false;
						} else if (len > 0) {
							int offset = 0;
							
							// 読み込んだ内容に対してシークを実行。
							for (int i = 0; i < readBuffer.position(); ++i) {
								byte b = readBuffer.array()[i];
								for (byte d: delimiteres) {
									// 区切り文字に該当するか？
									if (b == d) {
										// 現在読み込んでいるメッセージは切捨て中か？
										if (!dropping) {
											byte[] message = sendBuffer.put(readBuffer.array(), offset, i - offset).array();
											if (sendBuffer.isOverflow()) {
												try {
													String[] result = parser.splitSyslogMessage(message);
													if (result != null && result.length == 2) {
														logger.warn(String.format("read() : exceeded send buffer. %s, header=%s, message=%s", client, result[0], result[1]));
													} else {
														logger.warn(String.format("read() : exceeded send buffer. %s, string=%s, array=%s", client, new String(message, parser.getHdrCharset()), Arrays.toString(message)));
													}
												} catch(IOException e) {
													logger.warn(e.getMessage(), e);
												}
												messages.add(message);
												counter.incrementWarning();
											} else {
												if (message.length > 0) {
													messages.add(message);
													counter.incrementSuccess();
												}
											}
											sendBuffer.reset();
										} else {
											// 切捨て中に区切り文字が見つかったので、以降は送信対象とする。
											dropping = false;
										}
										// 区切り文字分をスキップ
										offset = ++i;
										break;
									}
								}
							}
							
							// 切捨て中か？
							if (!dropping) {
								// 読込バッファーに残っている分を、送信バッファーへ追加。
								if (readBuffer.position() - offset > 0) {
									sendBuffer.put(readBuffer.array(), offset, readBuffer.position() - offset);
									
									if (sendBuffer.isOverflow()) {
										// メッセージの受信許容量を超えたので、一旦送信。次の区切りに達するまで、以降の読み込みはドロップ。
										byte[] message = sendBuffer.array();
										try {
											String[] result = parser.splitSyslogMessage(message);
											if (result != null && result.length == 2) {
												logger.warn(String.format("read() : exceeded send buffer. %s, header=%s, message=%s", client, result[0], result[1]));
											} else {
												logger.warn(String.format("read() : exceeded send buffer. %s, string=%s, array=%s", client, new String(message, parser.getHdrCharset()), Arrays.toString(message)));
											}
										} catch(IOException e) {
											logger.warn(e.getMessage(), e);
										}
										messages.add(message);
										counter.incrementWarning();
										
										sendBuffer.reset();
										
										// 切捨て中に移行
										dropping = true;
									}
								}
							}
						} //else if (len == 0) {
							// 読込サイズが 0 だが、読込継続。
//						}
					} catch(SocketTimeoutException e) {
						if (exceptionHandler != null) {
							exceptionHandler.accept(e,client.getInetAddress().toString());
						}
						// 読み込み時にタイムアウト発生。
						logger.debug(e.getMessage() + " : " + client.toString());
						
						// これまでに読み込んだ内容を送信。
						byte[] message = sendBuffer.put(readBuffer).array();
						if (message.length > 0) {
							messages.add(message);
							// タイムアウトも一応成功。
							counter.incrementSuccess();
						}
						
						reading = false;
					}
					
					// 送信処理
					final String sendrIp = client.getInetAddress().toString();
					for (byte[] message: messages) {
						if (parser.containsHeader(message)) {
							if (logger.isTraceEnabled()) {
								try {
									String[] result = parser.splitSyslogMessage(message);
									if (result != null) {
										logger.trace(String.format("read() : %s, header=%s, message=%s", client.toString(), result[0], result[1]));
									} else {
										logger.warn(String.format("read() : %s, string=%s, array=%s", client, new String(message, parser.getHdrCharset()), Arrays.toString(message)));
									}
								} catch(IOException e) {
									logger.warn(e.getMessage(), e);
								}
							}
						} else {
							if (logger.isTraceEnabled()) {
								logger.trace(String.format("read() : Not found a header. %s, message=%s", client.getInetAddress().getHostAddress(), Arrays.toString(message)));
							}
						}
						
						handler.accept(message,sendrIp);
					}
				}
			} catch(RuntimeException | IOException e) {
				if (exceptionHandler != null) {
					exceptionHandler.accept(e,client.getInetAddress().toString());
				}
				counter.incrementFailed();
				logger.warn(e.getMessage() + " : " + client.toString(), e);
			} finally {
				try {
					client.close();
				} catch (Exception e) {
					logger.warn(e.getMessage(), e);
				}
			}
		}
		
		@Override
		public String toString() {
			return "ReadingTask [client=" + client + "]";
		}
	}
	
	private ReadingTask createReadingTask(Socket client, ResponseHandler<byte[]> handler) {
		return new ReadingTask(client, handler);
	}
	
	public SyslogTcpReceiver(String address, int port, Counter counter, ResponseHandler<byte[]> handler, ExecutorService worker) {
		this.counter = counter;
		this.address = address;
		this.port = port;
		this.handler = handler;
		this.worker = worker;
		this.delimiteres = new ArrayList<>();
		delimiteres.add((byte)10);
	}
	
	public void start() throws IOException {
		if (mainThread != null) {
			logger.warn("start() : already started. " + this.toString());
		}
		
		ServerSocket server = new ServerSocket();
		
		// 定期的にタイムアウトさせて、処理の終了確認をさせる。
		server.setSoTimeout(ACCEPT_TIMEOUT);
			
		server.bind(new InetSocketAddress(address, port));
		
		mainThread = new Thread(new AcceptTask(server), "Thread-SyslogTcpReceiver");
		mainThread.start();
		
		try {
			sync.await();
		} catch (InterruptedException e) {
		}
	}
	
	public void stop() {
		loop = false;
		logger.info(String.format("stop() : stopping. %s", this.toString()));
	}
	
	public void join(long wait) throws InterruptedException {
		mainThread.join(wait);
	}
	
	public static SyslogTcpReceiver build(String address, int port, Counter counter, ResponseHandler<byte[]> handler, ExecutorService worker) {
		return new SyslogTcpReceiver(address, port, counter, handler, worker);
	}
	
	public int getReadTimeout() {
		return readTimeout;
	}
	public void setReadTimeout(int readTimeout) {
		this.readTimeout = readTimeout;
	}
	
	public int getMaxReadSize() {
		return maxReadSize;
	}
	public void setMaxReadSize(int maxReadSize) {
		this.maxReadSize = maxReadSize;
	}
	
	public void setExceptionHandler(ResponseHandler<Exception> exceptionHandler) {
		this.exceptionHandler = exceptionHandler;
	}
	
	public void setSyslogPacketParser(SyslogPacketHelper parser) {
		this.parser = parser;
	}
	
	@Override
	public String toString() {
		return "SyslogTcpReceiver [address=" + address + ", port=" + port + ", loop=" + loop + "]";
	}

	public void setDelimiteres(List<Byte> delimiteres) {
		this.delimiteres = delimiteres;
	}
}