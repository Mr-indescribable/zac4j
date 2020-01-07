package me.zac4j.pkt.passive;

import me.zac4j.pkt.passive.ZBXPassiveAgentPacket;


public class ZBXPassiveAgentRequest extends ZBXPassiveAgentPacket
{
	public ZBXPassiveAgentRequest () {
		super();
	}

	public ZBXPassiveAgentRequest (byte[] data) {
		super(data);
	}

	public ZBXPassiveAgentRequest (String data) {
		super(data);
	}
}
