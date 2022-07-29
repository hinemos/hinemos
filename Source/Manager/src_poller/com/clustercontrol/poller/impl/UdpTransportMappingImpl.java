/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.poller.impl;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.PortUnreachableException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.snmp4j.SNMP4JSettings;
import org.snmp4j.TransportStateReference;
import org.snmp4j.security.SecurityLevel;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.transport.UdpTransportMapping;
import org.snmp4j.util.WorkerTask;

/**
 * DefaultUdpTransportMappingを一部修正したクラス。
 *
 */
public class UdpTransportMappingImpl extends UdpTransportMapping {

	private final static Log logger = LogFactory
			.getLog(UdpTransportMappingImpl.class);

	protected DatagramSocket socket = null;
	protected WorkerTask listener;
	protected ListenThread listenerThread;
	private int socketTimeout = 0;

	private int receiveBufferSize = 0; // not set by default
	
	private static int threadSerial = 0;

	/**
	 * Creates a UDP transport with an arbitrary local port on all local
	 * interfaces.
	 *
	 * @throws IOException
	 *             if socket binding fails.
	 */
	public UdpTransportMappingImpl() throws IOException {
		super(new UdpAddress("0.0.0.0/0"));
		socket = new DatagramSocket(udpAddress.getPort());
	}

	public void sendMessage(UdpAddress targetAddress, byte[] message,
			TransportStateReference tmStateReference)
			throws java.io.IOException {
		InetSocketAddress targetSocketAddress = new InetSocketAddress(
				targetAddress.getInetAddress(), targetAddress.getPort());
		if (logger.isDebugEnabled()) {
			logger.debug("Sending message to " + targetAddress
					+ " with length " + message.length + ": "
					+ new OctetString(message).toHexString());
		}
		DatagramSocket s = ensureSocket();
		s.send(new DatagramPacket(message, message.length, targetSocketAddress));
	}

	/**
	 * Closes the socket and stops the listener thread.
	 *
	 * @throws IOException
	 */
	public void close() throws IOException {
		boolean interrupted = false;
		WorkerTask l = listener;
		if (l != null) {
			l.terminate();
			l.interrupt();
			if (socketTimeout > 0) {
				try {
					l.join();
				} catch (InterruptedException ex) {
					interrupted = true;
					logger.warn(ex);
				}
			}
			listener = null;
		}
		DatagramSocket closingSocket = socket;
		if ((closingSocket != null) && (!closingSocket.isClosed())) {
			closingSocket.close();
		}
		socket = null;
		if (interrupted) {
			Thread.currentThread().interrupt();
		}
	}

	/**
	 * Starts the listener thread that accepts incoming messages. The thread is
	 * started in daemon mode and thus it will not block application terminated.
	 * Nevertheless, the {@link #close()} method should be called to stop the
	 * listen thread gracefully and free associated ressources.
	 *
	 * @throws IOException
	 */
	public synchronized void listen() throws IOException {
		if (listener != null) {
			throw new SocketException("Port already listening");
		}
		ensureSocket();
		listenerThread = new ListenThread();
		listener = SNMP4JSettings.getThreadFactory().createWorkerThread(
				"DefaultUDPTransportMapping_" + getAddress() + "-" + threadSerial, listenerThread,
				true);
		if (threadSerial == Integer.MAX_VALUE) {
			threadSerial = 0;
		} else {
			threadSerial ++;
		}
		listener.run();
	}

	private synchronized DatagramSocket ensureSocket() throws SocketException {
		DatagramSocket s = socket;
		if (s == null) {
			s = new DatagramSocket(udpAddress.getPort());
			s.setSoTimeout(socketTimeout);
			this.socket = s;
		}
		return s;
	}


	/**
	 * Returns the priority of the internal listen thread.
	 * 
	 * @return a value between {@link Thread#MIN_PRIORITY} and
	 *         {@link Thread#MAX_PRIORITY}.
	 * @since 1.2.2
	 */
	public int getPriority() {
		WorkerTask lt = listener;
		if (lt instanceof Thread) {
			return ((Thread) lt).getPriority();
		} else {
			return Thread.NORM_PRIORITY;
		}
	}

	/**
	 * Sets the name of the listen thread for this UDP transport mapping. This
	 * method has no effect, if called before {@link #listen()} has been called
	 * for this transport mapping.
	 *
	 * @param name
	 *            the new thread name.
	 * @since 1.6
	 */
	public void setThreadName(String name) {
		WorkerTask lt = listener;
		if (lt instanceof Thread) {
			((Thread) lt).setName(name);
		}
	}

	/**
	 * Returns the name of the listen thread.
	 * 
	 * @return the thread name if in listening mode, otherwise <code>null</code>
	 *         .
	 * @since 1.6
	 */
	public String getThreadName() {
		WorkerTask lt = listener;
		if (lt instanceof Thread) {
			return ((Thread) lt).getName();
		} else {
			return null;
		}
	}

	public void setMaxInboundMessageSize(int maxInboundMessageSize) {
		this.maxInboundMessageSize = maxInboundMessageSize;
	}

	/**
	 * Returns the socket timeout. 0 returns implies that the option is disabled
	 * (i.e., timeout of infinity).
	 * 
	 * @return the socket timeout setting.
	 */
	public int getSocketTimeout() {
		return socketTimeout;
	}

	/**
	 * Gets the requested receive buffer size for the underlying UDP socket.
	 * This size might not reflect the actual size of the receive buffer, which
	 * is implementation specific.
	 * 
	 * @return <=0 if the default buffer size of the OS is used, or a value >0
	 *         if the user specified a buffer size.
	 */
	public int getReceiveBufferSize() {
		return receiveBufferSize;
	}

	/**
	 * Sets the receive buffer size, which should be > the maximum inbound
	 * message size. This method has to be called before {@link #listen()} to be
	 * effective.
	 * 
	 * @param receiveBufferSize
	 *            an integer value >0 and > {@link #getMaxInboundMessageSize()}.
	 */
	public void setReceiveBufferSize(int receiveBufferSize) {
		if (receiveBufferSize <= 0) {
			throw new IllegalArgumentException(
					"Receive buffer size must be > 0");
		}
		this.receiveBufferSize = receiveBufferSize;
	}

	/**
	 * Sets the socket timeout in milliseconds.
	 * 
	 * @param socketTimeout
	 *            the socket timeout for incoming messages in milliseconds. A
	 *            timeout of zero is interpreted as an infinite timeout.
	 */
	public void setSocketTimeout(int socketTimeout) {
		this.socketTimeout = socketTimeout;
		if (socket != null) {
			try {
				socket.setSoTimeout(socketTimeout);
			} catch (SocketException ex) {
				throw new RuntimeException(ex);
			}
		}
	}

	public boolean isListening() {
		return (listener != null);
	}

	@Override
	public UdpAddress getListenAddress() {
		UdpAddress actualListenAddress = null;
		DatagramSocket socketCopy = socket;
		if (socketCopy != null) {
			actualListenAddress = new UdpAddress(socketCopy.getInetAddress(),
					socketCopy.getLocalPort());
		}
		return actualListenAddress;
	}

	/**
	 * If receiving new datagrams fails with a {@link SocketException}, this
	 * method is called to renew the socket - if possible.
	 * 
	 * @param socketException
	 *            the exception that occurred.
	 * @param failedSocket
	 *            the socket that caused the exception. By default, he socket
	 *            will be closed in order to be able to reopen it.
	 *            Implementations may also try to reuse the socket, in
	 *            dependence of the <code>socketException</code>.
	 * @return the new socket or <code>null</code> if the listen thread should
	 *         be terminated with the provided exception.
	 * @throws SocketException
	 *             a new socket exception if the socket could not be renewed.
	 * @since 2.2.2
	 */
	protected DatagramSocket renewSocketAfterException(
			SocketException socketException, DatagramSocket failedSocket)
			throws SocketException {
		if ((failedSocket != null) && (!failedSocket.isClosed())) {
			failedSocket.close();
		}
		DatagramSocket s = new DatagramSocket(udpAddress.getPort(),
				udpAddress.getInetAddress());
		s.setSoTimeout(socketTimeout);
		return s;
	}

	class ListenThread implements WorkerTask {

		private byte[] buf;
		private volatile boolean stop = false;

		public ListenThread() throws SocketException {
			buf = new byte[getMaxInboundMessageSize()];
		}


		private void receive (DatagramSocket socketCopy, DatagramPacket packet) throws IOException {
			socketCopy.receive(packet);
		}
		
		public void run() {
			DatagramSocket socketCopy = socket;
			if (socketCopy != null) {
				try {
					socketCopy.setSoTimeout(getSocketTimeout());
					if (receiveBufferSize > 0) {
						socketCopy.setReceiveBufferSize(Math.max(
								receiveBufferSize, maxInboundMessageSize));
					}
					if (logger.isDebugEnabled()) {
						logger.debug("UDP receive buffer size for socket "
								+ getAddress() + " is set to: "
								+ socketCopy.getReceiveBufferSize());
					}
				} catch (SocketException ex) {
					logger.warn("run() : SocketException1 " + ex.getMessage() + ", " + udpAddress);
					setSocketTimeout(0);
				}
			}
			while (!stop) {
				DatagramPacket packet = new DatagramPacket(buf, buf.length,
						udpAddress.getInetAddress(), udpAddress.getPort());
				try {
					socketCopy = socket;
					try {
						if (socketCopy == null) {
							stop = true;
							continue;
						}
						receive(socketCopy, packet);
					} catch (InterruptedIOException iiox) {
						if (iiox.bytesTransferred <= 0) {
							continue;
						}
					}
					if (logger.isDebugEnabled()) {
						logger.debug("Received message from "
								+ packet.getAddress()
								+ "/"
								+ packet.getPort()
								+ " with length "
								+ packet.getLength()
								+ ": "
								+ new OctetString(packet.getData(), 0, packet
										.getLength()).toHexString());
					}
					ByteBuffer bis;
					// If messages are processed asynchronously (i.e.
					// multi-threaded)
					// then we have to copy the buffer's content here!
					if (isAsyncMsgProcessingSupported()) {
						byte[] bytes = new byte[packet.getLength()];
						System.arraycopy(packet.getData(), 0, bytes, 0,
								bytes.length);
						bis = ByteBuffer.wrap(bytes);
					} else {
						bis = ByteBuffer.wrap(packet.getData());
					}
					TransportStateReference stateReference = new TransportStateReference(
							UdpTransportMappingImpl.this, udpAddress, null,
							SecurityLevel.undefined, SecurityLevel.undefined,
							false, socketCopy);
					fireProcessMessage(new UdpAddress(packet.getAddress(),
							packet.getPort()), bis, stateReference);
				} catch (SocketTimeoutException stex) {
					// ignore
					logger.trace("SocketTimeoutException " + stex.getMessage());
				} catch (PortUnreachableException purex) {
					synchronized (UdpTransportMappingImpl.this) {
						listener = null;
					}
					logger.warn("run() : PortUnreachableException " + purex.getMessage() + ", " + udpAddress);
					if (logger.isDebugEnabled()) {
						purex.printStackTrace();
					}
					if (SNMP4JSettings.isForwardRuntimeExceptions()) {
						throw new RuntimeException(purex);
					}
					break;
				} catch (SocketException soex) {
					logger.debug("SocketException " + soex.getMessage());
					if (!stop) {
						logger.warn("Socket for transport mapping "
								+ toString() + ", message=" + soex.getMessage());
					}
					if (SNMP4JSettings.isForwardRuntimeExceptions()) {
						stop = true;
						throw new RuntimeException(soex);
					} else {
						try {
							DatagramSocket newSocket = renewSocketAfterException(
									soex, socketCopy);
							if (newSocket == null) {
								throw soex;
							}
							socket = newSocket;
						} catch (SocketException e) {
							stop = true;
							socket = null;
							logger.warn(
									"Socket renewal for transport mapping "
											+ toString() + " failed with: "
											+ e.getMessage(), e);

						}
					}
				} catch (IOException iox) {
					logger.warn("run() : IOException2 : " + iox.getMessage() + ", " + udpAddress);
					if (logger.isDebugEnabled()) {
						iox.printStackTrace();
					}
					if (SNMP4JSettings.isForwardRuntimeExceptions()) {
						throw new RuntimeException(iox);
					}
				}
			}
			synchronized (UdpTransportMappingImpl.this) {
				listener = null;
				stop = true;
				DatagramSocket closingSocket = socket;
				if ((closingSocket != null) && (!closingSocket.isClosed())) {
					closingSocket.close();
				}
				socket = null;
			}
			if (logger.isDebugEnabled()) {
				logger.debug("Worker task stopped:" + getClass().getName());
			}
		}

		public void close() {
			stop = true;
		}

		public void terminate() {
			close();
			if (logger.isDebugEnabled()) {
				logger.debug("Terminated worker task: " + getClass().getName());
			}
		}

		public void join() throws InterruptedException {
			if (logger.isDebugEnabled()) {
				logger.debug("Joining worker task: " + getClass().getName());
			}
		}

		public void interrupt() {
			if (logger.isDebugEnabled()) {
				logger.debug("Interrupting worker task: "
						+ getClass().getName());
			}
			close();
		}
	}
}
