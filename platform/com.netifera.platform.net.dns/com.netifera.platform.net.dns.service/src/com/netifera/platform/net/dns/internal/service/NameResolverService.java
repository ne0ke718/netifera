package com.netifera.platform.net.dns.internal.service;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.StringTokenizer;

import org.jboss.netty.channel.socket.DatagramChannelFactory;
import org.osgi.service.component.ComponentContext;
import org.xbill.DNS.Lookup;

import com.netifera.platform.api.log.ILogManager;
import com.netifera.platform.api.log.ILogger;
import com.netifera.platform.net.dns.service.client.ExtendedResolver;
import com.netifera.platform.net.dns.service.client.SimpleResolver;
import com.netifera.platform.net.dns.service.nameresolver.NameResolver;
import com.netifera.platform.util.addresses.inet.InternetAddress;
import com.netifera.platform.util.addresses.inet.UDPSocketAddress;

public class NameResolverService extends NameResolver {
	
	private DatagramChannelFactory channelFactory;
	private ILogger logger;
	
	protected void setChannelFactory(DatagramChannelFactory channelFactory) {
		if (this.channelFactory == null)
			this.channelFactory = channelFactory;
	}
	
	protected void unsetChannelFactory(DatagramChannelFactory channelFactory) {
		if (this.channelFactory == channelFactory)
			this.channelFactory = null;
	}
	
	protected void setLogManager(ILogManager logManager) {
		logger = logManager.getLogger("Name Resolver");
	}
	
	protected void unsetLogManager(ILogManager logManager) {
		logger = null;
	}
	
	private void addNameServer(String nameServer) throws IOException {
		InternetAddress address = InternetAddress.fromString(nameServer);
//		try {
			SimpleResolver simpleResolver = new SimpleResolver(new UDPSocketAddress(address, 53), channelFactory);
			simpleResolver.setLogger(logger);
			resolver.addResolver(simpleResolver);
			logger.debug("added nameserver " + nameServer);
/*		} catch (SocketException e) { // Network is unreachable
			logger.error("could not add nameserver " + nameServer, e);
		}
*/	}
	
	private boolean activateUnix() {
		boolean activated = false;
		InputStream in = null;
		try {
			try {
				in = new FileInputStream("/peludo/osfs/etc/resolv.conf");
			} catch (IOException e) {
				in = new FileInputStream("/etc/resolv.conf");
			}
			try {
				InputStreamReader isr = new InputStreamReader(in);
				BufferedReader br = new BufferedReader(isr);
				String line;
				while ((line = br.readLine()) != null) {
					if (line.startsWith("nameserver")) {
						StringTokenizer st = new StringTokenizer(line);
						st.nextToken(); /* skip "nameserver" string */
						String nameserver = st.nextToken();
						addNameServer(nameserver);
						logger.info("nameserver "+nameserver);
						activated = true;
					}
				}
			} catch (IOException e) {
				logger.error("I/O error parsing resolv.conf", e);
			} finally {
				try {
					in.close();
				} catch (IOException e) {
					logger.error("I/O error closing resolv.conf", e);
				}
			}
		} catch (FileNotFoundException e) {
			logger.error("File /etc/resolv.conf not found. Cannot initialize name resolver service.");
		}
		return activated;
	}
	
	protected void activate(ComponentContext context) {
		resolver = new ExtendedResolver();
		Lookup.setDefaultResolver(resolver);
		
		//FIXME Unix only
		activateUnix();
		
		if (resolver.getResolvers().length == 0) {
			logger.warning("Could not find any system nameserver, setting a default nameserver");
			try {
				addNameServer("208.67.222.222"); // resolver1.opendns.com
			} catch (IOException e) {
				logger.error("Could not add default nameserver. Cannot initialize name resolver service.");
			} 
		}
		assert resolver.getResolvers().length != 0 : "NameResolverService: no resolvers found. (imminent ArrayIndexOutOfBoundsException in ExtendedResolver)";
	}
	
	protected void deactivate(ComponentContext context) {
		try {
			shutdown();
		} catch (IOException e) {
			logger.error("I/O error shutdowing resolver service", e);
		}
	}
}
