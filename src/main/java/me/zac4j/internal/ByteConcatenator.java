package me.zac4j.internal;


public class ByteConcatenator
{

	/**
	 * concatenate 2 byte arrays
	 *
	 * @param to Byte array to be written
	 * @param from Byte array which provides the data to write
	 * @param offset offset of the "to" parameter
	 */
	public static void concat(byte[] to, byte[] from, int offset) {
		for (int i=0; i<from.length; i++) {
			to[offset + i] = from[i];
		}
	}
}
