package me.zac4j.pkt.active;

import org.json.JSONArray;
import org.json.JSONObject;

import me.zac4j.exceptions.InvalidPacket;


public class ZBXActiveAgentReport extends ZBXActiveAgentPacket
{
	public ZBXActiveAgentReport () {
		super();
	}

	public ZBXActiveAgentReport (JSONObject json) {
		super(json);
	}

	public ZBXActiveAgentReport (String jsonString) throws InvalidPacket {
		super(jsonString);
	}

	public ZBXActiveAgentReport (byte[] bytes) throws InvalidPacket {
		super(bytes);
	}

	public String getFRequest() {
		return super.getJSON().getString("request");
	}

	public String getFSession() {
		return super.getJSON().getString("session");
	}

	public JSONArray getFData() {
		return super.getJSON().getJSONArray("data");
	}

	public int getClock() {
		return super.getJSON().getInt("clock");
	}

	public int getNs() {
		return super.getJSON().getInt("ns");
	}
}
