package me.zac4j.pkt.active;

import java.nio.charset.StandardCharsets;

import org.json.JSONObject;
import org.json.JSONException;

import me.zac4j.internal.ByteSpliter;
import me.zac4j.exceptions.InvalidPacket;
import me.zac4j.exceptions.NotEnoughData;
import me.zac4j.pkt.ZBXPacket;
import me.zac4j.pkt.ZBXPacketHeader;


public class ZBXActiveAgentPacket extends ZBXPacket
{
	JSONObject json;  // JSON content in the DATA field

	public ZBXActiveAgentPacket () {
		super();
	}

	public ZBXActiveAgentPacket (JSONObject json) {
		super();
		this.setJSONData(json);
	}

	public ZBXActiveAgentPacket (String jsonString) throws InvalidPacket {
		super();
		this.setJSONStringData(jsonString);
	}

	public ZBXActiveAgentPacket (byte[] data) throws InvalidPacket {
		super();
		this.setByteData(data);
	}

	public void setJSON(JSONObject json) {
		this.json = json;
	}

	public JSONObject getJSON() {
		return this.json;
	}

	public void setJSONData(JSONObject json) {
		this.setJSON(json);
		super.setData( json.toString() );
	}

	public void setJSONStringData(String jsonString) throws InvalidPacket {
		try {
			this.setJSON( new JSONObject(jsonString) );
		} catch (JSONException e) {
			throw new InvalidPacket("invalid JSON");
		}
		super.setData(jsonString);
	}

	public void setByteData(byte[] bytes) throws InvalidPacket {
		String jsonString;

		jsonString = new String(bytes, StandardCharsets.US_ASCII);
		this.setJSONStringData(jsonString);
	}

	public static ZBXActiveAgentPacket fromBytes(byte[] bytes)
			throws InvalidPacket, NotEnoughData
	{
		int headerLen = ZBXPacketHeader.ZBX_HEADER_LEN;
		int dataLen;
		int offset = 0;
		byte[] headerDataBA;
		byte[] bodyBA;
		ZBXActiveAgentPacket pkt = new ZBXActiveAgentPacket();

		if (bytes.length < headerLen) {
			throw new NotEnoughData();
		}

		headerDataBA = ByteSpliter.getFragment(bytes, offset, headerLen);
		pkt.headerFromBytes(headerDataBA);
		offset += headerLen;

		dataLen = pkt.getHeader().getDataLen();
		if (bytes.length < headerLen + dataLen) {
			throw new NotEnoughData();
		}

		bodyBA = ByteSpliter.getFragment(bytes, offset, dataLen);
		pkt.setByteData(bodyBA);

		return pkt;
	}

	/**
	 * Parse packet with a given header
	 *
	 * @param bytes Bytes of the DATA field (without header)
	 * @param header A parsed header
	 */
	public static ZBXActiveAgentPacket
			fromBytes(byte[] bytes, ZBXPacketHeader header)
			throws InvalidPacket, NotEnoughData
	{
		int dataLen = header.getDataLen();
		byte[] bodyBA;
		ZBXActiveAgentPacket pkt = new ZBXActiveAgentPacket();

		if (bytes.length < dataLen) {
			throw new NotEnoughData();
		}

		pkt.setHeader(header);

		bodyBA = ByteSpliter.getFragment(bytes, 0, dataLen);
		pkt.setByteData(bodyBA);

		return pkt;
	}
}
