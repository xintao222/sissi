package com.sissi.protocol.iq.disco.feature;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import com.sissi.protocol.iq.disco.Feature;

/**
 * @author kim 2013年12月5日
 */
@XmlRootElement
public class Blocking implements Feature {

	public final static Blocking FEATURE = new Blocking();

	public final static String NAME = "feature";

	private final static String VAR = "urn:xmpp:blocking";

	private Blocking() {

	}

	@Override
	@XmlAttribute
	public String getVar() {
		return VAR;
	}
}