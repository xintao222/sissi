package com.sissi.protocol.message;

import javax.xml.bind.annotation.XmlRootElement;

import com.sissi.read.Metadata;

/**
 * @author kim 2014年3月3日
 */
@Metadata(uri = Ack.XMLNS, localName = AckReceived.NAME)
@XmlRootElement
public class AckReceived extends Ack {

	public final static String NAME = "received";

	private String id;

	public AckReceived setId(String id) {
		this.id = id;
		return this;
	}

	public boolean id() {
		return this.getId() != null;
	}

	public String getId() {
		return this.id;
	}
}
