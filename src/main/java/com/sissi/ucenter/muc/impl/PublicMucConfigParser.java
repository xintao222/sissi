package com.sissi.ucenter.muc.impl;

import com.sissi.config.MongoConfig;
import com.sissi.protocol.muc.OwnerConfig;
import com.sissi.ucenter.field.Field;
import com.sissi.ucenter.muc.MucConfigParser;

/**
 * @author kim 2014年4月3日
 */
public class PublicMucConfigParser implements MucConfigParser {

	@Override
	public Object parse(Field<?> field) {
		return Boolean.valueOf(field.getValue().toString());
	}

	@Override
	public String field() {
		return MongoConfig.FIELD_PUBLIC;
	}

	@Override
	public String support() {
		return OwnerConfig.ROOMCONFIG_PUBLIC.toString();
	}
}
