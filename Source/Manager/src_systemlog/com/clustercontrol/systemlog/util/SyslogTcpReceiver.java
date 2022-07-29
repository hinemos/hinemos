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
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.channels.InterruptedByTimeoutException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;


/*
 * Tcp 受信用クラス。
 */
public class SyslogTcpReceiver {
	private static Logger logger = Logger.getLogger(SyslogTcpReceiver.class);
	private AsynchronousChannelGroup group;
	// 受信数のカウンタ
	private Counter counter;
	
	// メイン処理用スレッド。
	private Thread mainThread;
	
	// リッスン用
	private String address;
	private int port;
	
	// 読み取り時のブロックのタイムアウト。
	private int readTimeout = 10000;
	
	private boolean isReadTimeout = false;
	
	// コネクション処理用スレッドの上限数
	private int threadNum = 1000;
	
	// 最大同時接続数
	private int maxConnSize;
	
	//bind時のbacklog上限
	private int tcpBacklog;
	
	//TCP KeepAlive
	private boolean tcpKeepAlive;
	
	// 読み取り時の最大サイズ
	private int maxReadSize = -1;
	
	// 読込用の一時バッファーサイズ(8k)
	private static final int READ_BUFFER_SIZE = 8192;
		
	// 読み込んだ情報を処理するハンドラー。
	private ResponseHandler<byte[]> handler;
			
	// 出力用文字セット
	private SyslogPacketHelper parser = new SyslogPacketHelper();
	
	private CountDownLatch sync = new CountDownLatch(1);
	
	private CountDownLatch awaitLatch = new CountDownLatch(1);
	
	//同時接続数制御用	
	private Semaphore cSemaphore;
	
	
	private List<Byte> delimiteres;
	
	public SyslogTcpReceiver(String address, int port, Counter counter, ResponseHandler<byte[]> handler, int maxConnSize, int threadNum, boolean tcpKeepAlive, int tcpBacklog) {
		this.counter = counter;
		this.address = address;
		this.port = port;
		this.handler = handler;
		this.delimiteres = new ArrayList<>();
		this.maxConnSize = maxConnSize;
		this.threadNum = threadNum;
		this.tcpKeepAlive = tcpKeepAlive;
		this.tcpBacklog = tcpBacklog;
		delimiteres.add((byte)10);
	}
	
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
		
		@SuppressWarnings("unused")
		public int getBufferSize() {
			return maxBufferSize == -1 ? Integer.MAX_VALUE: maxBufferSize;
		}
		
		public static Buffer build(int maxReadSize) {
			return new Buffer(maxReadSize);
		}
	}
	
	
	/*
	 * ソケットアクセプトのメイン処理。
	 */
	private class AcceptTask implements Runnable {
		private AsynchronousServerSocketChannel server;
		private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d HH:mm:ss", Locale.ENGLISH);
		
		public AcceptTask(AsynchronousServerSocketChannel server) {
			this.server = server;
			this.dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
		}
		@Override
		public void run() {
			sync.countDown();
			
			logger.info(String.format("listening() : start listening syslog packet using tcp. address=%s, port=%d", address, port));
			try (AsynchronousServerSocketChannel closable = server) {
				try{
					//acceptを開始
					server.accept(null, new CompletionHandler<AsynchronousSocketChannel, Void>(){
						@Override
						public void completed(AsynchronousSocketChannel socket, Void attachement){
							//serverが閉じられるまで、acceptを続ける
							if(server.isOpen()){
								server.accept(null,this);
							}
							//keepaliveは有効か？
							if (tcpKeepAlive){
								try {
									socket.setOption(StandardSocketOptions.SO_KEEPALIVE, true);
									if (logger.isDebugEnabled()){
										logger.debug("run(): KEEPALIVE has set for socket :"+socket.getRemoteAddress());
									}
								} catch (IOException e) {
									logger.warn("run(): Failed to set KeepAlive option");
								}
							}
							//最大同時接続数チェック
							if (cSemaphore == null || cSemaphore.tryAcquire()) {
								readMessages(socket);
							} else {
								try {
									// 最大数に達していた場合は、即座にクローズ
									logger.warn(String.format(
											"run(): Too many TCP connection. Maximum Connection: %d Drop connection: %s",
											maxConnSize, socket.getRemoteAddress()));
									socket.close();
								} catch (IOException e) {
									logger.warn(e.getMessage(), e);
								}
							}
							
						}
						@Override
						public void failed(Throwable exc, Void attachment) {
							logger.warn("run(): failed to accept TCP connection."+exc.getMessage());
						}
					});
				}catch(Exception e){
					//基本的にここに来ることはないはず
					logger.warn("run(): failed to start server socket.",e);
				}
				//Non-Blockingなため、このスレッドが終了すると、acceptも全部終了してしまうのでstop()が呼ばれるまで待機
				try {
					awaitLatch.await();
					//ラッチが下りたらAsynchronousServerSocketChannelの全スレッドを停止
					group.shutdownNow();
				} catch (IOException | InterruptedException e) {
					logger.warn(e.getMessage(),e);
				}
			} catch (Exception e2) {
				logger.warn("Failed to start SyslogTcpReceiver. " + SyslogTcpReceiver.this.toString(), e2);
			}
			logger.info(String.format("listen() : stopped. %s", SyslogTcpReceiver.this.toString()));
		}
	}
	
	//ソケットアクセプト後の処理
	private void readMessages(AsynchronousSocketChannel socket){
		ByteBuffer readBuffer = ByteBuffer.allocate(READ_BUFFER_SIZE);
		Buffer sendBuffer = Buffer.build(maxReadSize);
		
		//ソケットからの読み取り時の処理を定義するCompletionHandler
		CompletionHandler<Integer, ByteBuffer> readAction = new CompletionHandler<Integer, ByteBuffer>(){
			//切り捨て判定用
			boolean dropping = false;
			
			@Override
			public void completed(Integer ret, ByteBuffer attachment) {
				List<byte[]> messages = new ArrayList<>();
				//socket管理フラグ
				boolean shouldCloseSocket = true;
				try{
					if (ret < 0){
						// コネクションが閉じたので受信した内容を送信。
						if (logger.isTraceEnabled()) {
							logger.trace(String.format("CH.completed() : complete reading. %s", socket.toString()));
						}
						
						if (!dropping) {
							byte[] message = sendBuffer.array();
							if (message.length > 0) {
								messages.add(message);
								
								//送信処理
								sendMessages(socket, messages);
								// コネクションがクローズしたので、とりあえず成功とする。
								counter.incrementSuccess();
							}
						}
						readBuffer.clear();
					}else if (ret > 0){
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
											logger.warn("CH.completed() : [A]exceeded send buffer. "+convertMessageForLogging(socket, message));
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
									logger.warn("CH.completed() : [B]exceeded send buffer. "+convertMessageForLogging(socket, message));
									messages.add(message);
									counter.incrementWarning();
									
									sendBuffer.reset();
									
									// 切捨て中に移行
									dropping = true;
								}
							}
						}
						//送信処理
						sendMessages(socket, messages);
						readBuffer.clear();
						//コネクションが閉じていない場合は、ソケットからの読み込み処理を続行
						shouldCloseSocket = false;
						readSocket(readBuffer, this, socket);
					}
				}catch(Exception e){
					//通常は起こりえないExceptionが発生した場合、ここに来る
					//ここにたどり着いた場合はソケットをクローズ
					logger.error("CH.completed(): Unknown error occured.", e);
					shouldCloseSocket = true;
				}finally{
					//ソケットをクローズすべきか判断
					if(shouldCloseSocket){
						closeSocket(socket);
					}else{
						if(logger.isTraceEnabled()){
							logger.trace("CH.completed(): continue reading socket");
						}
					}
				}
			}
			@Override
			public void failed(Throwable exc, ByteBuffer attachment) {
				try {
					//readtimeoutの場合
					if (exc instanceof InterruptedByTimeoutException){
						logger.debug("CH.failed(): ReadTimeout has occured. Close connection form IP: "+socket.getRemoteAddress());
					}else{
						//それ以外の場合は何らかのエラーのため失敗扱い
						//keepaliveにより接続が無効と判断された場合も、ここに来る
						logger.warn("CH.failed(): Reading has failed. Close connection from IP: "+socket.getRemoteAddress(),exc);
						counter.incrementFailed();
					}
					closeSocket(socket);
				} catch (IOException e) {
					logger.debug("CH.failed():closing socket failed. Maybe already closed",e);
				}
				
			}
			
		};
		
		//ソケットからの読み込み
		readSocket(readBuffer, readAction, socket);
	}
	
	//リードタイムアウトの有無を判定し、ソケットから読み込みを開始
	private void readSocket(ByteBuffer readBuffer, CompletionHandler<Integer, ByteBuffer> readAction, AsynchronousSocketChannel socket){
		if(isReadTimeout){
			socket.read(readBuffer,readTimeout,TimeUnit.MILLISECONDS,readBuffer,readAction);
		}else{
			socket.read(readBuffer,readBuffer,readAction);
		}
	}
	//ソケットをクローズして、セマフォをリリース
	private void closeSocket(AsynchronousSocketChannel socket){
		try {
			//コネクションのクローズ処理
			if(logger.isDebugEnabled()){
				try {
					logger.debug("closeSocket(): Connection closed for IP: "+socket.getRemoteAddress());
				} catch (IOException e) {
					logger.debug("closeSocket(): Can't get IP address", e);
				}
			}
			socket.close();
		} catch (Exception e) {
			logger.warn("closeSocket(): closing socket failed.", e);
		}finally{
			//同時接続数制限を行っているときのみ、セマフォをリリース
			if(cSemaphore != null){
				cSemaphore.release();
			}else{
				if (logger.isTraceEnabled()){
					logger.trace("closeSocket(): Since maxConnSize is set to infinity, do not use Semaphore");
				}
			}
		}
	}
	
	//sendBufferのオーバーフロー発生時のログメッセージを生成
	private String convertMessageForLogging(AsynchronousSocketChannel socket, byte[] message) {
		try {
			String[] result = parser.splitSyslogMessage(message);
			if (result != null && result.length == 2) {
				return String.format("socket=%s, header=%s, message=%s", socket, result[0], result[1]);
			} else {
				return String.format("socket=%s, string=%s, array=%s", socket,
						new String(message, parser.getHdrCharset()), Arrays.toString(message));
			}
		} catch (Exception e) {
			logger.warn("convertMessageForLogging: Error, " + e.getMessage(), e);
			return String.format("socket=%s, message=%s", socket, Arrays.toString(message));
		}
	}
	
	// 受信したsyslogの送信処理
	private void sendMessages(AsynchronousSocketChannel socket, List<byte[]> messages) {
		try{
			InetSocketAddress sendrAddr = (InetSocketAddress) socket.getRemoteAddress();
			final String sendrIp = sendrAddr.getAddress().toString();
			for (byte[] message: messages) {
				if (logger.isTraceEnabled()) {
					if (parser.containsHeader(message)) {
						logger.trace("CH.completed() : " + convertMessageForLogging(socket, message));
					} else {
						logger.trace(String.format("CH.completed() : Not found a header. %s, message=%s",
								socket.getRemoteAddress(), Arrays.toString(message)));
					}
				}
				//受信したsyslogをSystemLogMonitor#acceptに受け渡し
				handler.accept(message,sendrIp);
			}
		}catch(IOException e){
			//ここに来るのはsocketからIPアドレスを取得できなかった場合
			//この後のsyslogの処理でsender IPは必須になるので、取得できなかった場合は失敗扱い
			logger.warn("sendMessages(): Reading has failed. Failed to get sender IP addr",e);
			
			counter.incrementFailed();
			closeSocket(socket);
		}
	}
	
	public void start() throws IOException {
		if (mainThread != null) {
			logger.warn("start() : already started. " + this.toString());
		}
		//AsynchronousServerSocketChannelの初期化
		//使用するスレッド数を固定するため、AsynchronousChannelGroupを作成
		//スレッド数上限については、SyslogUdpReceiverのworkerと同様
		this.group = AsynchronousChannelGroup.withFixedThreadPool(threadNum, new ThreadFactory() {
					private AtomicInteger index = new AtomicInteger();

					@Override
					public Thread newThread(Runnable r) {
						return new Thread(r, String.format("Thread-SyslogTcpReader-%d", index.incrementAndGet()));
					}
				});
		AsynchronousServerSocketChannel server = AsynchronousServerSocketChannel.open(group);
		//キープアライブが有効かログに出力
		logger.info("start(): TCP KeepAlive is " + tcpKeepAlive);
		server.setOption(StandardSocketOptions.SO_REUSEADDR, true);
		//最大同時接続数を指定の上bindを開始
		server.bind(new InetSocketAddress(address, port), tcpBacklog);
		logger.info("start(): Server backlog size is " + tcpBacklog);
		logger.info("start(): Server Maximum connection size is " + maxConnSize);
		//maxConnSizeが無制限の場合はセマフォを使用しない
		if (maxConnSize < 1) {
			cSemaphore = null;
		} else {
			cSemaphore = new Semaphore(maxConnSize);
		}
		mainThread = new Thread(new AcceptTask(server), "Thread-SyslogTcpReceiver");
		mainThread.start();
		
		try {
			sync.await();
		} catch (InterruptedException e) {
		}

	}
	
	public void stop() {
		//AcceptTask終了用。ラッチが下げられたらAsynchronousServerSocketChannelの各処理が終了。
		awaitLatch.countDown();
		logger.info(String.format("stop() : stopping. %s", this.toString()));
	}
	
	public void join(long wait) throws InterruptedException {
		mainThread.join(wait);
	}
	
	public static SyslogTcpReceiver build(String address, int port, Counter counter, ResponseHandler<byte[]> handler, int maxConnSize, int threadNum, boolean tcpKeepAlive, int tcpBacklog) {
		return new SyslogTcpReceiver(address, port, counter, handler, maxConnSize, threadNum, tcpKeepAlive, tcpBacklog);
	}
	
	public int getReadTimeout() {
		return readTimeout;
	}
	public void setReadTimeout(int readTimeout) {
		this.readTimeout = readTimeout;
	}
	
	public boolean isReadTimeout(){
		return isReadTimeout;
	}
	
	public void enableReadTimeout(boolean enable){
		if(enable){
			isReadTimeout = true;
		}else{
			isReadTimeout = false;
		}
	}
	
	public int getMaxReadSize() {
		return maxReadSize;
	}
	public void setMaxReadSize(int maxReadSize) {
		this.maxReadSize = maxReadSize;
	}
	
	
	public void setSyslogPacketParser(SyslogPacketHelper parser) {
		this.parser = parser;
	}
	
	@Override
	public String toString() {
		return "SyslogTcpReceiver [address=" + address + ", port=" + port +"]";
	}

	public void setDelimiteres(List<Byte> delimiteres) {
		this.delimiteres = delimiteres;
	}
}