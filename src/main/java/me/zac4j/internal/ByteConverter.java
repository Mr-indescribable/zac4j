package me.zac4j.internal;


public class ByteConverter
{
	/**
	 * Integer to Little-Endian
	 */
	public static byte[] int2le(int in) {
		byte[] bts = new byte[4];
		int mask = 0x000000ff;

		for (int i=0; i<4; i++) {
			bts[i] = (byte) ((in & mask) >> (i * 8));
			mask <<= 8;
		}

		return bts;
	}

	/**
	 * Little-Endian to Integer
	 */
	public static int le2int(byte[] ba) {
		byte b;
		int ubyte;
		int num = 0x00000000;

		if (ba.length != 4) {
			throw new IllegalArgumentException("4 bytes only");
		}

		for (int i=0; i<4; i++) {
			b = ba[i];
			ubyte = (0xff & b) << (8 * i);
			num |= ubyte;
		}

		return num;
	}
}
