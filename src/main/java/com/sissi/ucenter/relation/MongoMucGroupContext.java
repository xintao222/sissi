package com.sissi.ucenter.relation;

import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBObject;
import com.sissi.config.MongoConfig;
import com.sissi.config.impl.MongoProxyConfig;
import com.sissi.context.JID;
import com.sissi.context.JIDBuilder;
import com.sissi.protocol.muc.ItemAffiliation;
import com.sissi.protocol.muc.ItemRole;
import com.sissi.ucenter.MucGroupConfig;
import com.sissi.ucenter.MucGroupContext;
import com.sissi.ucenter.RelationContext;
import com.sissi.ucenter.RelationMuc;

/**
 * @author kim 2014年2月20日
 */
public class MongoMucGroupContext implements MucGroupContext {

	private final String fieldConfigs = "configs";

	private final String fieldMapping = "mapping";

	private final DBObject filter = BasicDBObjectBuilder.start().add(this.fieldConfigs, 1).add(MongoProxyConfig.FIELD_CREATOR, 1).get();

	private final MongoConfig config;

	private final JIDBuilder jidBuilder;

	private final RelationContext relationContext;

	public MongoMucGroupContext(MongoConfig config, JIDBuilder jidBuilder, RelationContext relationContext) {
		super();
		this.config = config;
		this.jidBuilder = jidBuilder;
		this.relationContext = relationContext;
	}

	@Override
	public MucGroupConfig find(JID group) {
		DBObject db = this.config.collection().findOne(BasicDBObjectBuilder.start(MongoProxyConfig.FIELD_JID, group.asStringWithBare()).get(), this.filter);
		return new MongoMucGroupConfig(group, db != null ? DBObject.class.cast(db.get(this.fieldConfigs)) : null, this.config.asString(db, MongoProxyConfig.FIELD_CREATOR));
	}

	private class MongoMucGroupConfig implements MucGroupConfig {

		private final JID group;

		private final DBObject configs;

		private final String creator;

		private final Integer[] mapping;

		public MongoMucGroupConfig(JID group, DBObject configs, String creator) {
			super();
			this.group = group;
			this.configs = configs;
			this.creator = creator;
			this.mapping = MongoMucGroupContext.this.config.asInts(this.configs, MongoMucGroupContext.this.fieldMapping);
		}

		@Override
		public boolean allowed(String key, Object value) {
			switch (key) {
			case MucGroupConfig.HIDDEN:
				if (value == null) {
					return false;
				}
				JID jid = JID.class.cast(value);
				RelationMuc muc = RelationMuc.class.cast(MongoMucGroupContext.this.relationContext.ourRelation(jid, this.group));
				return MongoMucGroupContext.this.config.asBoolean(this.configs, MucGroupConfig.HIDDEN) && !jid.asStringWithBare().equals(this.creator) && !ItemRole.MODERATOR.equals(ItemRole.NONE.equals(muc.getRole()) ? this.mapping(muc.getAffiliaion()) : muc.getRole());
			}
			return false;
		}

		@Override
		public String mapping(String affiliation) {
			int ordinal = ItemAffiliation.parse(affiliation).ordinal();
			return this.mapping != null && (this.mapping.length - 1) > ordinal ? ItemRole.toString(this.mapping[ordinal]) : ItemRole.NONE.toString();
		}
	}
}