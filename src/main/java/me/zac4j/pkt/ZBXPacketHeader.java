package me.zac4j.pkt;

import me.zac4j.internal.ByteSpliter;
import me.zac4j.internal.ByteConverter;
import me.zac4j.internal.ByteConcatenator;
import me.zac4j.exceptions.NotEnoughData;
import me.zac4j.exceptions.InvalidPacket;


public class ZBXPacketHeader
{
	public static final byte ZBX_FLAGS = 0x01;
	public static final byte[] ZBX_FLAGS_BA = {ZBX_FLAGS};
	public static final byte[] ZBX_PROTO = {'Z', 'B', 'X', 'D'};
	public static final byte[] ZBX_RESERVED = {0x00, 0x00, 0x00, 0x00};
	public static final int ZBX_HEADER_LEN = 4 * 3 + 1;

	protected byte[] dataLenBA = {0x00, 0x00, 0x00, 0x00};
	protected int dataLen = 0;

	public void setDataLen(int len) {
		this.dataLen = len;
		this.dataLenBA = ByteConverter.int2le(len);
	}

	public int getDataLen() {
		return this.dataLen;
	}

	public byte[] getBytes() {
		byte[] bytes = new byte[ZBX_HEADER_LEN];
		int offset = 0;

		ByteConcatenator.concat(bytes, ZBX_PROTO, offset);
		offset += 4;

		ByteConcatenator.concat(bytes, ZBX_FLAGS_BA, offset);
		offset += 1;

		ByteConcatenator.concat(bytes, this.dataLenBA, offset);
		offset += 4;

		ByteConcatenator.concat(bytes, ZBX_RESERVED, offset);

		return bytes;
	}

	public static ZBXPacketHeader fromBytes(byte[] bytes)
			throws InvalidPacket, NotEnoughData
	{
		int offset = 0;
		int dataLen;
		byte[] dataLenBA = new byte[4];
		ZBXPacketHeader header;

		if (bytes.length != ZBX_HEADER_LEN) {
			throw new NotEnoughData();
		}

		for (byte b : ZBX_PROTO) {
			if (bytes[offset] != b) {
				throw new InvalidPacket("Header PROTOCOL field error");
			}

			offset += 1;
		}

		if (bytes[offset] != ZBX_FLAGS) {
			throw new InvalidPacket(
				"Header FLAGS is not 0x01, other values are not supported yet"
			);
		}
		offset += 1;

		dataLenBA = ByteSpliter.getFragment(bytes, offset, 4);
		dataLen = ByteConverter.le2int(dataLenBA);
		offset += 4;

		for (byte b : ZBX_RESERVED) {
			if (bytes[offset] != b) {
				throw new InvalidPacket("Header RESERVED field error");
			}

			offset += 1;
		}

		header = new ZBXPacketHeader();
		header.setDataLen(dataLen);
		return header;
	}
}
