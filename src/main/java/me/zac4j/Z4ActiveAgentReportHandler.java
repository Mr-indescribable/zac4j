package me.zac4j;

import java.net.InetSocketAddress;

import me.zac4j.exceptions.InvalidPacket;
import me.zac4j.pkt.active.ZBXActiveAgentReport;


public interface Z4ActiveAgentReportHandler
{
	/**
	 * Handle a report packet from an agent
	 *
	 * An InvalidPacket exception may be thrown if the data in packet body is
	 * incorrect, in that case the report processing will be terminated
	 * instantly and the TCP connection will be closed as well.
	 */
	public void handleReport(
		ZBXActiveAgentReport pkt,
		InetSocketAddress src
	) throws InvalidPacket;
}
