package me.zac4j.pkt.passive;

import me.zac4j.pkt.ZBXPacket;


public class ZBXPassiveAgentPacket extends ZBXPacket
{
	public ZBXPassiveAgentPacket () {
		super();
	}

	public ZBXPassiveAgentPacket (byte[] data) {
		super(data);
	}

	public ZBXPassiveAgentPacket (String data) {
		super(data);
	}
}
