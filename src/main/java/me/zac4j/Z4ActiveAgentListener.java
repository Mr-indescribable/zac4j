package me.zac4j;

import java.util.Iterator;
import java.util.ArrayList;

import java.io.InputStream;
import java.io.IOException;

import java.nio.charset.StandardCharsets;
import java.nio.ByteBuffer;
import java.nio.channels.Selector;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.ClosedChannelException;

import java.net.Socket;
import java.net.ServerSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import org.json.JSONObject;
import org.json.JSONException;

import me.zac4j.exceptions.BrokenPipe;
import me.zac4j.exceptions.NotEnoughData;
import me.zac4j.exceptions.InvalidPacket;
import me.zac4j.pkt.ZBXPacketHeader;
import me.zac4j.pkt.active.ZBXACEntry;
import me.zac4j.pkt.active.ZBXAgentRequests;
import me.zac4j.pkt.active.ZBXActiveAgentPacket;
import me.zac4j.pkt.active.ZBXActiveAgentReport;
import me.zac4j.pkt.active.ZBXActiveAgentACResponse;
import me.zac4j.Z4ActiveAgentReportHandler;


class _SocketAttachment
{
	protected ByteBuffer sendBuf = null;
	protected ByteBuffer hRecvBuf = null;  // buffer for header
	protected ByteBuffer bRecvBuf = null;  // buffer for body
	protected ZBXPacketHeader header = null;

	public _SocketAttachment() {
		this.hRecvBuf = ByteBuffer.allocate( ZBXPacketHeader.ZBX_HEADER_LEN );
	}

	public void clean() {
		this.sendBuf = null;
		this.hRecvBuf = ByteBuffer.allocate( ZBXPacketHeader.ZBX_HEADER_LEN );
		this.bRecvBuf = null;
		this.header = null;
	}

	/**
	 * has data to send
	 */
	public boolean hasD2S() {
		return this.sendBuf != null && this.sendBuf.hasRemaining();
	}

	/**
	 * attach some data which will be sent later
	 */
	public void attachD2S(ByteBuffer buf) {
		this.sendBuf = buf;
		this.sendBuf.position(0);
	}

	public ByteBuffer getD2S() {
		return this.sendBuf;
	}

	public ByteBuffer getHRecvBuf() {
		return this.hRecvBuf;
	}

	public ByteBuffer getBRecvBuf() {
		return this.bRecvBuf;
	}

	public boolean hasHeader() {
		return this.header != null;
	}

	public void setHeader(ZBXPacketHeader header) {
		this.header = header;

		// since the body length is provided by the header
		this.bRecvBuf = ByteBuffer.allocate( header.getDataLen() );
	}

	public ZBXPacketHeader getHeader() {
		return this.header;
	}

	public void nullifyHeader() {
		this.header = null;
	}
}


/**
 * Zabbix 4.x Agent Active Check Listener
 *
 * Notice: SocketTimeoutException is not catched, users should catch it.
 *
 * Usage: see example/ActiveCheckListener.java
 */
public class Z4ActiveAgentListener
{
	protected int POLL_TIMEOUT = 2000;  // ms
	protected int OP_RO = SelectionKey.OP_READ;
	protected int OP_RW = OP_RO | SelectionKey.OP_WRITE;

	protected int backlog;
	protected boolean running = false;
	protected InetSocketAddress sa;
	protected Selector selector;
	protected ServerSocketChannel svrSock;
	protected ZBXActiveAgentACResponse acRespPkt = null;
	protected ArrayList<Z4ActiveAgentReportHandler> reportHandlers;

	public Z4ActiveAgentListener (String addr, int port, int backlog)
			throws IOException
	{
		this.reportHandlers = new ArrayList<Z4ActiveAgentReportHandler>();

		this.sa = new InetSocketAddress(addr, port);
		this.backlog = backlog;

		this.selector = Selector.open();

		this.svrSock = ServerSocketChannel.open();
		this.svrSock.configureBlocking(false);
	}

	public Z4ActiveAgentListener (int port, int backlog) throws IOException {
		this("0.0.0.0", port, backlog);
	}

	/**
	 * You may register multiple handlers, these handlers will be put
	 * into an array and will be called one by one when a report is received.
	 */
	public void registerReportHandler(Z4ActiveAgentReportHandler handler) {
		this.reportHandlers.add(handler);
	}

	/**
	 * Put some active check items into the listener, these
	 * items will be automatically given to zabbix agents.
	 *
	 * This method must be called before run().
	 */
	public void setActiveCheckItems(ZBXACEntry[] items) {
		this.acRespPkt = new ZBXActiveAgentACResponse();

		for (ZBXACEntry item : items) {
			this.acRespPkt.addACItem(item);
		}
	}

	public void setRunning(boolean running) {
		this.running = running;
	}

	public boolean getRunning() {
		return this.running;
	}

	public void run() throws IOException {
		if (this.acRespPkt == null) {
			throw new NullPointerException(
				"Active check items is not configured, " +
				"call setActiveCheckItems() first"
			);
		}

		this.svrSock.bind(this.sa, this.backlog);

		try {
			this.svrSock.register(this.selector, SelectionKey.OP_ACCEPT);
		} catch (ClosedChannelException e) {
			throw new RuntimeException("THIS IS IMPOSSIBLE");
		}

		this.running = true;
		while (this.running) {
			this.poll();
		}

		this.onExit();
	}

	public void stop() {
		this.setRunning(false);
	}

	protected void onExit() throws IOException {
		this.svrSock.close();
	}

	protected void poll() throws IOException {
		this.selector.select(POLL_TIMEOUT);
		Iterator iter = this.selector.selectedKeys().iterator();

		while ( iter.hasNext() ) {
			SelectionKey key = (SelectionKey) iter.next();
			iter.remove();

			if ( key.isAcceptable() ) {
				this.onAccept(key);
			} else if ( key.isReadable() ) {
				ensureAttachment(key);
				this.onRead(key);
			} else if ( key.isWritable() ) {
				ensureAttachment(key);
				this.onWrite(key);
			}

		}
	}

	private void ensureAttachment(SelectionKey key) {
		if (key.attachment() == null) {
			key.attach( new _SocketAttachment() );
		}
	}

	protected void onAccept(SelectionKey key) {
		SocketChannel sock;
		ServerSocketChannel svrSock;

		svrSock = (ServerSocketChannel) key.channel();

		try {
			sock = svrSock.accept();
			sock.configureBlocking(false);
		} catch (IOException e) {
			return;
		}

		try {
			sock.register(this.selector, OP_RW);
		} catch (ClosedChannelException e) {
			throw new RuntimeException("THIS IS IMPOSSIBLE");
		}
	}

	protected void onClose(SelectionKey key) {
		SocketChannel sock = (SocketChannel) key.channel();
		key.cancel();

		try {
			sock.close();
		} catch (IOException e) {
			// nothing to do
		}
	}

	protected void onRead(SelectionKey key) {
		_SocketAttachment att;

		att = (_SocketAttachment) key.attachment();

		try {
			if ( !att.hasHeader() ) {
				this.readHeader(key);
			} else {
				this.readBody(key);
			}
		} catch (InvalidPacket e) {
			this.onClose(key);
		} catch (BrokenPipe e) {
			this.onClose(key);
		}
	}

	private void readHeader(SelectionKey key)
			throws InvalidPacket, BrokenPipe
	{
		int btsRead;
		byte[] data;
		ByteBuffer buf;
		SocketChannel sock;
		_SocketAttachment att;
		ZBXPacketHeader header;

		att = (_SocketAttachment) key.attachment();
		buf = att.getHRecvBuf();

		sock = (SocketChannel) key.channel();

		try {
			btsRead = sock.read(buf);
		} catch (IOException e) {
			throw new BrokenPipe();
		}

		if (btsRead < 0) {
			this.onClose(key);
			return;
		}

		if (buf.remaining() == 0) {
			data = buf.array();

			try {
				header = ZBXPacketHeader.fromBytes(data);
			} catch (NotEnoughData e) {
				throw new RuntimeException("THIS IS IMPOSSIBLE");
			}

			att.setHeader(header);
		}
	}

	private void readBody(SelectionKey key)
			throws InvalidPacket, BrokenPipe
	{
		int btsRead;
		byte[] data;
		ByteBuffer buf;
		SocketChannel sock;
		_SocketAttachment att;
		ZBXPacketHeader header;
		ZBXActiveAgentPacket pkt;

		att = (_SocketAttachment) key.attachment();
		buf = att.getBRecvBuf();

		sock = (SocketChannel) key.channel();

		try {
			btsRead = sock.read(buf);
		} catch (IOException e) {
			throw new BrokenPipe();
		}

		if (btsRead < 0) {
			this.onClose(key);
			return;
		}

		if (buf.remaining() == 0) {
			data = buf.array();
			header = att.getHeader();

			try {
				pkt = ZBXActiveAgentPacket.fromBytes(data, header);
			} catch (NotEnoughData e) {
				e.printStackTrace();
				throw new RuntimeException("THIS IS IMPOSSIBLE");
			}

			att.clean();
			this.handlePacket(key, pkt);
		}
	}

	/**
	 * handle incoming packet
	 */
	protected void handlePacket(SelectionKey key, ZBXActiveAgentPacket pkt)
			throws InvalidPacket, BrokenPipe
	{
		_SocketAttachment att;
		JSONObject body;
		String request;

		att = (_SocketAttachment) key.attachment();
		body = pkt.getJSON();

		try {
			request = (String) body.get("request");

			if ( request.equals(ZBXAgentRequests.AGENT_GET_AC_ITEMS) ) {
				this.appendACItems(key);
				return;
			} else if ( request.equals(ZBXAgentRequests.AGENT_REPORT) ) {
				this.handleAgentReport(
					key,
					new ZBXActiveAgentReport( pkt.getJSON() )
				);
			} else {
				throw new InvalidPacket("unrecognized request: " + request);
			}
		} catch (JSONException e) {
			throw new InvalidPacket("invalid packet body");
		}
	}

	protected void handleAgentReport(
		SelectionKey key,
		ZBXActiveAgentReport pkt
	) throws BrokenPipe {
		SocketChannel sock;
		InetSocketAddress src;

		sock = (SocketChannel) key.channel();

		try {
			src = (InetSocketAddress) sock.getRemoteAddress();
		} catch (IOException e) {
			throw new BrokenPipe();
		}

		for (Z4ActiveAgentReportHandler handler : this.reportHandlers) {
			try {
				handler.handleReport(pkt, src);
			} catch (InvalidPacket e) {
				this.onClose(key);
				return;
			}
		}
	}

	protected void onWrite(SelectionKey key) {
		SocketChannel sock;
		_SocketAttachment att;

		att = (_SocketAttachment) key.attachment();

		if ( !att.hasD2S() ) {
			this.onNoDataToSend(key);
			return;
		}

		sock = (SocketChannel) key.channel();

		try {
			sock.write( att.getD2S() );
		} catch (IOException e) {
			this.onClose(key);
			return;
		}

		if ( !att.hasD2S() ) {
			this.onNoDataToSend(key);
			return;
		}
	}

	protected void onNoDataToSend(SelectionKey key) {
		key.interestOps(OP_RO);
	}

	/**
	 * Attach data onto the SelectionKey, the data will be sent
	 * through the TCP socket later.
	 */
	protected void appendData(SelectionKey key, ByteBuffer data) {
		_SocketAttachment att;

		att = (_SocketAttachment) key.attachment();
		att.attachD2S(data);
		key.interestOps(OP_RW);
	}

	protected void appendACItems(SelectionKey key) {
		byte[] data;
		ByteBuffer bf;

		data = this.acRespPkt.getBytes();
		bf = ByteBuffer.allocate(data.length);
		bf.put(data);

		this.appendData(key, bf);
	}
}
