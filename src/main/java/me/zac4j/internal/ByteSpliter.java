package me.zac4j.internal;


public class ByteSpliter
{
	/**
	 * get a fragment from a byte array
	 *
	 * @param ba data source
	 * @param offset offset of the "ba" parameter
	 * @param len length of the fragment
	 */
	public static byte[] getFragment(byte[] ba, int offset, int len) {
		byte[] res = new byte[len];
		int resOffset = 0;

		for (int i=offset; i<offset+len; i++) {
			res[resOffset] = ba[i];
			resOffset += 1;
		}

		return res;
	}
}
