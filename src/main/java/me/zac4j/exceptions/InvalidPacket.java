package me.zac4j.exceptions;


public class InvalidPacket extends Exception
{
	public InvalidPacket() {
		super();
	}

	public InvalidPacket(String msg) {
		super(msg);
	}
}
