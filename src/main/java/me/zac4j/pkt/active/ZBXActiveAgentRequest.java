package me.zac4j.pkt.active;

import org.json.JSONObject;

import me.zac4j.exceptions.InvalidPacket;
import me.zac4j.pkt.active.ZBXActiveAgentPacket;


public class ZBXActiveAgentRequest extends ZBXActiveAgentPacket
{
	public ZBXActiveAgentRequest () {
		super();
	}

	public ZBXActiveAgentRequest (JSONObject json) {
		super(json);
	}

	public ZBXActiveAgentRequest (String jsonString) throws InvalidPacket {
		super(jsonString);
	}

	public ZBXActiveAgentRequest (byte[] bytes) throws InvalidPacket {
		super(bytes);
	}

	public String getFRequest() {
		return super.getJSON().getString("request");
	}

	public String getFHost() {
		return super.getJSON().getString("host");
	}
}
