package me.zac4j;

import java.io.IOException;
import java.net.InetSocketAddress;

import me.zac4j.exceptions.InvalidPacket;
import me.zac4j.pkt.active.ZBXACEntry;
import me.zac4j.pkt.active.ZBXActiveAgentReport;
import me.zac4j.Z4ActiveAgentListener;
import me.zac4j.Z4ActiveAgentReportHandler;


/**
 * An example of Z4ActiveAgentListener's usage
 */


class ReportHandler implements Z4ActiveAgentReportHandler
{
	public void handleReport(ZBXActiveAgentReport pkt, InetSocketAddress src)
		throws InvalidPacket
	{
		// you can simply get the packet body in an org.json.JSONObject
		// object here. For other ways to get attributes, see
		// ZBXActiveAgentReport class for more details.
		System.out.println( pkt.getJSON() );
	}
}


public class ActiveCheckListener
{
	public static void main(String[] args) throws IOException {
		ZBXACEntry[] acItems = new ZBXACEntry[1];
		ZBXACEntry item = new ZBXACEntry();

		/**
		 * lastlogsize and mtime must be given, Zabbix active check
		 * protocol defines this and we must follow.
		 */
		item.setKey("system.cpu.load[all,avg1]");
		item.setDelay(1);
		item.setLastlogsize(0);
		item.setMtime(0);

		acItems[0] = item;

		// port: 20051; backlog: 64
		Z4ActiveAgentListener lsner = new Z4ActiveAgentListener(20051, 64);

		lsner.registerReportHandler(new ReportHandler());
		lsner.setActiveCheckItems(acItems);

		// This will block the thread, let's wait for the print then.
		lsner.run();
	}
}
