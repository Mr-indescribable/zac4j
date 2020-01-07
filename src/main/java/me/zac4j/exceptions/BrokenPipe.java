package me.zac4j.exceptions;


public class BrokenPipe extends Exception
{
	public BrokenPipe() {
		super();
	}

	public BrokenPipe(String msg) {
		super(msg);
	}
}
