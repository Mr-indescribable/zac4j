package me.zac4j.pkt.passive;

import java.nio.charset.StandardCharsets;

import me.zac4j.internal.ByteSpliter;
import me.zac4j.exceptions.InvalidPacket;
import me.zac4j.exceptions.NotEnoughData;
import me.zac4j.pkt.ZBXPacketHeader;
import me.zac4j.pkt.passive.ZBXPassiveAgentPacket;


public class ZBXPassiveAgentResponse extends ZBXPassiveAgentPacket
{
	protected boolean _hasError = false;
	protected String errmsg;
	protected String content;

	public ZBXPassiveAgentResponse () {
		super();
	}

	public ZBXPassiveAgentResponse (byte[] data) {
		super(data);
	}

	public ZBXPassiveAgentResponse (String data) {
		super(data);
	}

	public boolean hasError() {
		return this._hasError;
	}

	public void setErrmsg(String errmsg) {
		this._hasError = true;
		this.errmsg = errmsg;
	}

	public String getErrmsg() {
		return this.errmsg;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getContent() {
		return this.content;
	}

	/**
	 * Parse a packet
	 */
	public static ZBXPassiveAgentResponse fromBytes(byte[] bytes)
			throws InvalidPacket, NotEnoughData
	{
		int headerLen = ZBXPacketHeader.ZBX_HEADER_LEN;
		int dataLen;
		int offset = 0;
		byte[] headerDataBA;
		byte[] bodyBA;
		_PARBody body;
		ZBXPassiveAgentResponse pkt = new ZBXPassiveAgentResponse();

		if (bytes.length < headerLen) {
			throw new NotEnoughData();
		}

		headerDataBA = ByteSpliter.getFragment(bytes, offset, headerLen);
		pkt.headerFromBytes(headerDataBA);
		offset += headerLen;

		dataLen = pkt.getHeader().getDataLen();
		if (bytes.length < headerLen + dataLen) {
			throw new NotEnoughData();
		}

		bodyBA = ByteSpliter.getFragment(bytes, offset, dataLen);
		body = _PARBody.fromBytes(bodyBA);

		pkt.setContent( body.getContent() );

		if ( body.hasError() ){
			pkt.setErrmsg( body.getErrmsg() );
		}

		return pkt;
	}

	/**
	 * Parse packet with a given header
	 *
	 * @param bytes Bytes of the DATA field (without header)
	 * @param header A parsed header
	 */
	public static ZBXPassiveAgentResponse
			fromBytes(byte[] bytes, ZBXPacketHeader header)
			throws InvalidPacket, NotEnoughData
	{
		int dataLen = header.getDataLen();
		byte[] bodyBA;
		_PARBody body;
		ZBXPassiveAgentResponse pkt = new ZBXPassiveAgentResponse();

		if (bytes.length < dataLen) {
			throw new NotEnoughData();
		}

		pkt.setHeader(header);

		bodyBA = ByteSpliter.getFragment(bytes, 0, dataLen);
		body = _PARBody.fromBytes(bodyBA);

		pkt.setContent( body.getContent() );

		if ( body.hasError() ){
			pkt.setErrmsg( body.getErrmsg() );
		}

		return pkt;
	}
}


class _PARBody
{
	protected boolean _hasError = false;
	protected String errmsg;
	protected String content;

	public boolean hasError() {
		return this._hasError;
	}

	public void setErrmsg(String errmsg) {
		this.errmsg = errmsg;
		this._hasError = true;
	}

	public String getErrmsg() {
		return this.errmsg;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getContent() {
		return this.content;
	}

	public static _PARBody fromBytes(byte[] bytes) {
		String content = null;
		String errmsg = null;
		byte[] contentBA;
		byte[] errmsgBA;
		int cbaOffset = 0;
		int ebaOffset = 0;
		int dataLen = bytes.length;
		boolean goterr = false;
		_PARBody obj = new _PARBody();

		contentBA = new byte[dataLen];
		errmsgBA = new byte[dataLen];

		for (byte b : bytes) {
			if (b == 0) {
				goterr = true;
				continue;
			}

			if (goterr) {
				errmsgBA[ebaOffset] = b;
				ebaOffset += 1;
			} else {
				contentBA[cbaOffset] = b;
				cbaOffset += 1;
			}
		}

		content = new String(contentBA, StandardCharsets.US_ASCII);
		obj.setContent(content);

		if (goterr) {
			errmsg = new String(errmsgBA, StandardCharsets.US_ASCII);
			obj.setErrmsg(errmsg);
		}

		return obj;
	}
}
