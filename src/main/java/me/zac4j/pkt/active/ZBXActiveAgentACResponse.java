package me.zac4j.pkt.active;

import org.json.JSONArray;
import org.json.JSONObject;

import me.zac4j.exceptions.InvalidPacket;
import me.zac4j.pkt.active.ZBXACEntry;
import me.zac4j.pkt.active.ZBXActiveAgentPacket;


public class ZBXActiveAgentACResponse extends ZBXActiveAgentPacket
{
	protected String response = "success";
	protected JSONArray data = new JSONArray();

	public ZBXActiveAgentACResponse () {
		super();
	}

	public ZBXActiveAgentACResponse (JSONObject json) {
		super(json);
	}

	public ZBXActiveAgentACResponse (String jsonString) throws InvalidPacket {
		super(jsonString);
	}

	public ZBXActiveAgentACResponse (byte[] bytes) throws InvalidPacket {
		super(bytes);
	}

	public String getFResponse() {
		return this.response;
	}

	public void setFResponse(String resp) {
		this.response = resp;
		this.updateJSON();
	}

	public JSONArray getFData() {
		return this.data;
	}

	public void setFData(JSONArray data) {
		this.data = data;
		this.updateJSON();
	}

	public void addACItem(ZBXACEntry item) {
		JSONObject itemJSON;

		itemJSON = new JSONObject();
		itemJSON.put("key", item.getKey());
		itemJSON.put("delay", item.getDelay());
		itemJSON.put("lastlogsize", item.getLastlogsize());
		itemJSON.put("mtime", item.getMtime());

		this.data.put(itemJSON);
		this.updateJSON();
	}

	protected void updateJSON() {
		JSONObject json = new JSONObject();
		json.put("response", this.response);
		json.put("data", this.data);

		super.setJSONData(json);
	}
}
