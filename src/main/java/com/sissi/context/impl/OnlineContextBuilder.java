package com.sissi.context.impl;

import java.net.SocketAddress;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import com.sissi.context.JID;
import com.sissi.context.JIDContext;
import com.sissi.context.JIDContextBuilder;
import com.sissi.context.JIDContextParam;
import com.sissi.context.Status;
import com.sissi.context.StatusBuilder;
import com.sissi.pipeline.Output;
import com.sissi.protocol.Element;
import com.sissi.server.ServerTls;

/**
 * @author kim 2013-11-19
 */
public class OnlineContextBuilder implements JIDContextBuilder {

	public final static String KEY_TLS = "TLS";

	public final static String KEY_OUTPUT = "OUTPUT";

	public final static String KEY_ADDRESS = "ADDRESS";

	private final AtomicLong indexes = new AtomicLong();

	private final StatusBuilder statusBuilder;

	private final String lang;

	private final String domain;

	public OnlineContextBuilder(String lang, String domain, StatusBuilder statusBuilder) {
		super();
		this.statusBuilder = statusBuilder;
		this.lang = lang;
		this.domain = domain;
	}

	@Override
	public JIDContext build(JID jid, JIDContextParam param) {
		UserContext context = new UserContext(param);
		context.status = this.statusBuilder.build(context);
		return context;
	}

	private class UserContext implements JIDContext {

		private final AtomicBoolean isBinding = new AtomicBoolean();

		private final AtomicBoolean isAuth = new AtomicBoolean();

		private final AtomicBoolean isPrepareClose = new AtomicBoolean();

		private final Long index;

		private final Output output;

		private final ServerTls serverTLS;

		private final SocketAddress address;

		private JID jid = OfflineJID.OFFLINE;

		private Integer priority;

		private Status status;

		private String domain;

		private String lang;

		public UserContext(JIDContextParam param) {
			super();
			this.priority = 0;
			this.output = param.find(KEY_OUTPUT, Output.class);
			this.address = param.find(KEY_ADDRESS, SocketAddress.class);
			this.serverTLS = param.find(KEY_TLS, ServerTls.class);
			this.index = OnlineContextBuilder.this.indexes.incrementAndGet();
		}

		public Long getIndex() {
			return this.index;
		}

		public Boolean isBinding() {
			return this.isBinding.get();
		}

		public JIDContext setBinding(Boolean isBinding) {
			this.isBinding.set(isBinding);
			return this;
		}

		@Override
		public Boolean isAuth() {
			return this.isAuth.get();
		}

		@Override
		public UserContext setAuth(Boolean canAccess) {
			this.isAuth.set(canAccess);
			return this;
		}

		public UserContext setJid(JID jid) {
			this.jid = jid;
			return this;
		}

		public JID getJid() {
			return this.jid;
		}

		@Override
		public JIDContext write(Element node) {
			if (!this.isPrepareClose.get()) {
				this.output.output(this, node);
			}
			return this;
		}

		@Override
		public Boolean startTls() {
			return this.serverTLS.startTls(this.getDomain());
		}

		public Boolean isTls() {
			return this.serverTLS.isTls(this.getDomain());
		}

		@Override
		public Status getStatus() {
			return this.status;
		}

		@Override
		public JIDContext setPriority(Integer priority) {
			this.priority = priority;
			return this;
		}

		@Override
		public Integer getPriority() {
			return this.priority;
		}

		@Override
		public SocketAddress getAddress() {
			return this.address;
		}

		public JIDContext setLang(String lang) {
			this.lang = lang;
			return this;
		}

		public JIDContext setDomain(String domain) {
			this.domain = domain;
			return this;
		}

		public String getLang() {
			return this.lang != null ? this.lang : OnlineContextBuilder.this.lang;
		}

		public String getDomain() {
			return this.domain != null ? this.domain : OnlineContextBuilder.this.domain;
		}

		public JIDContext reset() {
			this.isBinding.set(false);
			this.isAuth.set(false);
			this.jid = OfflineJID.OFFLINE;
			this.lang = null;
			this.priority = 0;
			return this;
		}

		@Override
		public Boolean close() {
			if (!this.isPrepareClose.get()) {
				this.closePrepare();
				this.output.close();
			}
			return true;
		}

		public Boolean closePrepare() {
			this.isPrepareClose.set(true);
			this.status.clear();
			this.status = null;
			return true;
		}
	}
}
