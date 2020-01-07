package me.zac4j.pkt.active;


public class ZBXACEntry
{
	private String key;
	private int delay;
	private int lastlogsize;
	private int mtime;

	public String getKey() {
		return this.key;
	}

	public int getDelay() {
		return this.delay;
	}

	public int getLastlogsize() {
		return this.lastlogsize;
	}

	public int getMtime() {
		return this.mtime;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public void setDelay(int delay) {
		this.delay = delay;
	}

	public void setLastlogsize(int lastlogsize) {
		this.lastlogsize = lastlogsize;
	}

	public void setMtime(int mtime) {
		this.mtime = mtime;
	}

}
