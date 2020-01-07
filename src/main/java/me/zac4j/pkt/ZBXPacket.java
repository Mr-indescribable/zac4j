package me.zac4j.pkt;

import java.nio.charset.StandardCharsets;

import me.zac4j.internal.ByteConcatenator;
import me.zac4j.exceptions.InvalidPacket;
import me.zac4j.exceptions.NotEnoughData;
import me.zac4j.pkt.ZBXPacketHeader;


public class ZBXPacket
{
	protected byte[] data;
	protected ZBXPacketHeader header;

	public ZBXPacket () {
		this.header = new ZBXPacketHeader();
	}

	public ZBXPacket (byte[] data) {
		this();
		this.setData(data);
	}

	public ZBXPacket (String data) {
		this();
		this.setData(data);
	}

	public void setHeader(ZBXPacketHeader header) {
		this.header = header;
	}

	public ZBXPacketHeader getHeader() {
		return this.header;
	}

	public void headerFromBytes(byte[] bytes)
			throws InvalidPacket, NotEnoughData
	{
		this.header = ZBXPacketHeader.fromBytes(bytes);
	}

	public void setData(byte[] data) {
		this.header.setDataLen(data.length);
		this.data = data;
	}

	public void setData(String data) {
		byte[] dataBA;

		dataBA = data.getBytes(StandardCharsets.US_ASCII);
		this.setData(dataBA);
	}

	public byte[] getData() {
		return this.data;
	}

	public byte[] getBytes() {
		byte[] bytes;
		byte[] headerBA;
		int headerLen = ZBXPacketHeader.ZBX_HEADER_LEN;
		
		bytes = new byte[headerLen + this.data.length];
		
		headerBA = this.header.getBytes();
		ByteConcatenator.concat(bytes, headerBA, 0);
		ByteConcatenator.concat(bytes, this.data, headerLen);

		return bytes;
	}
}
