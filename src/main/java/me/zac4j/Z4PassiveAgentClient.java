package me.zac4j;

import java.io.InputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import java.net.Socket;
import java.net.InetAddress;
import java.net.UnknownHostException;

import me.zac4j.exceptions.BrokenPipe;
import me.zac4j.exceptions.NotEnoughData;
import me.zac4j.exceptions.InvalidPacket;
import me.zac4j.pkt.ZBXPacketHeader;
import me.zac4j.pkt.passive.ZBXPassiveAgentRequest;
import me.zac4j.pkt.passive.ZBXPassiveAgentResponse;


/**
 * Zabbix 4.x Passive Agent Client
 *
 * Usage:
 *     ZBXPassiveAgentResponse resp;
 *     Z4PassiveAgentClient client;
 *     
 *     client = new Z4PassiveAgentClient("127.0.0.1", 10050, 1000);
 *     client.send("system.cpu.load[all,avg1]");
 *     resp = client.recv();
 *     
 *     if ( resp.hasError() ) {
 *         System.out.println( resp.getErrmsg() );
 *     } else {
 *         System.out.println( resp.getContent() );
 *     }
 *
 * Notice: SocketTimeoutException is not catched, users should catch it.
 */
public class Z4PassiveAgentClient
{
	protected Socket sock;
	protected InetAddress addr;
	protected int port;
	protected int timeout;

	/**
	 * Constructor
	 *
	 * @param addr IP Address of the Zabbix Agent
	 * @param port TCP port of the Zabbix Agent
	 * @param timeout socket timeout, in milliseconds
	 */
	public Z4PassiveAgentClient (String addr, int port, int timeout)
			throws UnknownHostException, IOException
	{
		this.addr = InetAddress.getByName(addr);
		this.port = port;
		this.timeout = timeout;

		this.sock = new Socket(this.addr, this.port);
		this.sock.setSoTimeout(timeout);
	}

	public void close() throws IOException {
		this.sock.close();
	}

	/**
	 * send request to the passive zabbix agent
	 *
	 * @param data data field of the ZBXPassiveAgentRequest, A.K.A. item key
	 */
	public void send(String data) throws IOException {
		byte[] pktData;
		ZBXPassiveAgentRequest pkt;

		pkt = new ZBXPassiveAgentRequest(data);
		pktData = pkt.getBytes();
		this.sock.getOutputStream().write(pktData);
	}

	/**
	 * read response from the passive zabbix agent
	 *
	 * @return the data field of the response
	 */
	public ZBXPassiveAgentResponse recv()
			throws InvalidPacket, BrokenPipe
	{
		InputStream istrm;
		byte[] headerBuf;
		byte[] bodyBuf;
		int bodyLen;
		int btsRead;
		int hbufOffset = 0;
		int bbufOffset = 0;
		int headerLen = ZBXPacketHeader.ZBX_HEADER_LEN;
		ZBXPacketHeader header;
		ZBXPassiveAgentResponse pkt;

		try {
			istrm = this.sock.getInputStream();
		} catch (IOException e) {
			throw new BrokenPipe();
		}

		// recv header
		headerBuf = new byte[headerLen];
		do {
			try {
				btsRead = istrm.read(headerBuf, hbufOffset, headerLen);
				hbufOffset += btsRead;
			} catch (IOException e) {
				throw new BrokenPipe();
			}
		} while (hbufOffset < headerLen);

		try {
			header = ZBXPacketHeader.fromBytes(headerBuf);
		} catch (NotEnoughData e) {
			throw new RuntimeException("THIS IS IMPOSSIBLE");
		}

		// recv body
		bodyLen = header.getDataLen();
		bodyBuf = new byte[bodyLen];
		do {
			try {
				btsRead = istrm.read(bodyBuf, bbufOffset, bodyLen);
				bbufOffset += btsRead;
			} catch (IOException e) {
				throw new BrokenPipe();
			}
		} while (bbufOffset < bodyLen);

		try {
			pkt = ZBXPassiveAgentResponse.fromBytes(bodyBuf, header);
		} catch (NotEnoughData e) {
			throw new RuntimeException("THIS IS IMPOSSIBLE");
		}

		return pkt;
	}
}
