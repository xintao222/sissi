package com.sissi.pipeline.in.auth.impl;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.sasl.AuthorizeCallback;
import javax.security.sasl.Sasl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sissi.context.JID.JIDBuilder;
import com.sissi.context.JIDContext;
import com.sissi.pipeline.in.auth.AuthCallback;
import com.sissi.pipeline.in.auth.SaslServers;
import com.sissi.protocol.iq.login.Auth;
import com.sissi.protocol.iq.login.Challenge;
import com.sissi.ucenter.AuthAccessor;

/**
 * @author kim 2013年11月25日
 */
public class DigestAuthCallback implements AuthCallback {

	public final static String MECHANISM = "DIGEST-MD5";

	public final static String PROTOCOL = "XMPP";

	public final static String QOP = "auth";

	@SuppressWarnings("serial")
	private final static Map<String, String> PROPS = new TreeMap<String, String>() {
		{
			put(Sasl.QOP, QOP);
		}
	};

	@SuppressWarnings("serial")
	private final Map<Class<? extends Callback>, Handler> handlers = new HashMap<Class<? extends Callback>, Handler>() {
		{
			put(NameCallback.class, new NameCallbackHandler());
			put(PasswordCallback.class, new PasswordCallbackHandler());
			put(AuthorizeCallback.class, new AuthorizeCallbackHandler());
		}
	};

	private final Log log = LogFactory.getLog(this.getClass());

	private final String host;

	private final JIDBuilder jidBuilder;

	private final AuthAccessor authAccessor;

	private final SaslServers saslServers;

	public DigestAuthCallback(String host, JIDBuilder jidBuilder, AuthAccessor authAccessor, SaslServers saslServers) {
		super();
		this.host = host;
		this.jidBuilder = jidBuilder;
		this.authAccessor = authAccessor;
		this.saslServers = saslServers;
	}

	@Override
	public Boolean auth(JIDContext context, Auth auth) {
		try {
			context.write(new Challenge(this.saslServers.set(context, Sasl.createSaslServer(MECHANISM, PROTOCOL, this.host, PROPS, new ServerCallbackHandler(context))).evaluateResponse(new byte[0])));
			return true;
		} catch (Exception e) {
			this.log.fatal(e);
			return false;
		}
	}

	@Override
	public Boolean isSupport(String mechanism) {
		return MECHANISM.equals(mechanism);
	}

	public class ServerCallbackHandler implements CallbackHandler {

		private final JIDContext context;

		public ServerCallbackHandler(JIDContext context) {
			super();
			this.context = context;
		}

		public void handle(final Callback[] callbacks) throws IOException, UnsupportedCallbackException {
			for (Callback callback : callbacks) {
				Handler handler = DigestAuthCallback.this.handlers.get(callback.getClass());
				if (handler != null) {
					handler.handler(this.context, callback);
				}
			}
		}
	}

	private interface Handler {

		public void handler(JIDContext context, Callback callback);
	}

	private class NameCallbackHandler implements Handler {

		@Override
		public void handler(JIDContext context, Callback callback) {
			context.setJid(DigestAuthCallback.this.jidBuilder.build(NameCallback.class.cast(callback).getDefaultName(), null));
		}
	}

	private class PasswordCallbackHandler implements Handler {

		@Override
		public void handler(JIDContext context, Callback callback) {
			String password = DigestAuthCallback.this.authAccessor.access(context.getJid().getUser());
			((PasswordCallback) callback).setPassword(password != null ? password.toCharArray() : new char[0]);
		}
	}

	private class AuthorizeCallbackHandler implements Handler {

		@Override
		public void handler(JIDContext context, Callback callback) {
			AuthorizeCallback authCallback = ((AuthorizeCallback) callback);
			authCallback.setAuthorized(true);
		}
	}
}
