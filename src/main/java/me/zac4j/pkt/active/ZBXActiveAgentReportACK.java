package me.zac4j.pkt.active;

import org.json.JSONObject;

import me.zac4j.exceptions.InvalidPacket;
import me.zac4j.pkt.active.ZBXActiveAgentPacket;


public class ZBXActiveAgentReportACK extends ZBXActiveAgentPacket
{
	protected String response = "success";
	protected int processed = 0;
	protected int failed = 0;
	protected int total = 0;
	protected float secondsSpent = 0.0f;

	public ZBXActiveAgentReportACK () {
		super();
	}

	public ZBXActiveAgentReportACK (JSONObject json) {
		super(json);
	}

	public ZBXActiveAgentReportACK (String jsonString) throws InvalidPacket {
		super(jsonString);
	}

	public ZBXActiveAgentReportACK (byte[] bytes) throws InvalidPacket {
		super(bytes);
	}

	public String getFResponse() {
		return this.response;
	}

	public void setFResponse(String resp) {
		this.response = resp;
		this.updateJSON();
	}

	public void setInfoProcessed(int num) {
		this.processed = num;
	}

	public void setInfoFailed(int num) {
		this.failed = num;
	}

	public void setInfoTotal(int num) {
		this.total = num;
	}

	public void setInfoSecondsSpent(float sec) {
		this.secondsSpent = sec;
	}

	public int getInfoProcessed() {
		return this.processed;
	}

	public int getInfoFailed() {
		return this.failed;
	}

	public int setInfoTotal() {
		return this.total;
	}

	public float setInfoSecondsSpent() {
		return this.secondsSpent;
	}

	public String getInfoString() {
		return String.format(
			"processed: %d; failed: %d; total: %d; seconds spent: %f",
			this.processed, this.failed, this.total, this.secondsSpent
		);
	}

	public void updateJSON() {
		JSONObject json = new JSONObject();

		json.put("response", this.response);
		json.put("info", this.getInfoString());

		super.setJSONData(json);
	}
}
