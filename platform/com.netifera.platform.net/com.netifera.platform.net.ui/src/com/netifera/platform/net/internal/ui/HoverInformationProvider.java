package com.netifera.platform.net.internal.ui;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.netifera.platform.net.model.ClientEntity;
import com.netifera.platform.net.model.ClientServiceConnectionEntity;
import com.netifera.platform.net.model.HostEntity;
import com.netifera.platform.net.model.NetblockEntity;
import com.netifera.platform.net.model.NetworkAddressEntity;
import com.netifera.platform.net.model.ServiceEntity;
import com.netifera.platform.net.model.UserEntity;
import com.netifera.platform.net.ui.geoip.IGeoIPService;
import com.netifera.platform.net.ui.geoip.ILocation;
import com.netifera.platform.net.ui.routes.AS;
import com.netifera.platform.net.ui.routes.IIP2ASService;
import com.netifera.platform.ui.api.hover.IHoverInformationProvider;
import com.netifera.platform.util.addresses.INetworkAddress;
import com.netifera.platform.util.addresses.inet.InternetAddress;
import com.netifera.platform.util.addresses.inet.InternetNetblock;

public class HoverInformationProvider implements IHoverInformationProvider {

	private volatile IGeoIPService geoipService;
	private volatile IIP2ASService ip2asService;
	
	protected void setGeoIPService(IGeoIPService geoipService) {
		this.geoipService = geoipService;
	}
	
	protected void unsetGeoIPService(IGeoIPService geoipService) {
		this.geoipService = null;
	}

	protected void setIP2ASService(IIP2ASService ip2asService) {
		this.ip2asService = ip2asService;
	}
	
	protected void unsetIP2ASService(IIP2ASService ip2asService) {
		this.ip2asService = null;
	}

	public String getInformation(Object o) {
		if(o instanceof HostEntity) {
			return getHostInformation((HostEntity)o);
		} else if (o instanceof InternetNetblock) {
			return getNetblockInformation((InternetNetblock)o);
		} else if (o instanceof NetblockEntity) {
			return getNetblockInformation(((NetblockEntity)o).getNetblock());
		} else if (o instanceof ServiceEntity) {
			return getServiceInformation((ServiceEntity)o);
		} else if (o instanceof ClientEntity) {
			return getClientInformation((ClientEntity)o);
		} else if (o instanceof ClientServiceConnectionEntity) {
			return getServiceInformation(((ClientServiceConnectionEntity)o).getService());
		} else if (o instanceof UserEntity) {
			return getUserInformation((UserEntity)o);
		}
		return null;
	}

	private String getHostInformation(HostEntity e) {
		StringBuffer buffer = new StringBuffer();
		if (e.getAddresses().size() >= 1) {
			if (e.getAddresses().size() > 1)
				buffer.append("<p>Addresses: ");
			else
				buffer.append("<p>Address: ");
			Iterator<NetworkAddressEntity> addresses = e.getAddresses().iterator();
			while (addresses.hasNext()) {
				NetworkAddressEntity a = addresses.next();
				buffer.append(escape(a.getAddressString()));
				if (addresses.hasNext())
					buffer.append(", ");
			}
			buffer.append("</p>");
		}
		if (e.getAttribute("os") != null) {
			buffer.append("<p>System: "+escape(e.getAttribute("os")));
			if (e.getAttribute("distribution")!=null)
				buffer.append(" - "+escape(e.getAttribute("distribution")));
			if (e.getAttribute("arch") != null) {
				buffer.append(" ("+escape(e.getAttribute("arch"))+")");
			}
			buffer.append("</p>");
		}
		if (e.getAttribute("cpu") != null) {
			buffer.append("<p>CPU: "+escape(e.getAttribute("cpu")));
			buffer.append("</p>");
		}
		if (e.getAttribute("bogomips") != null) {
			buffer.append("<p>CPU bogomips: "+escape(e.getAttribute("bogomips")));
			buffer.append("</p>");
		}
		if (e.getAttribute("memoryTotal") != null) {
			buffer.append("<p>Memory: "+escape(e.getAttribute("memoryTotal")));
			if (e.getAttribute("memoryFree") != null) {
				buffer.append(" ("+escape(e.getAttribute("memoryFree"))+" free)");
			}
			buffer.append("</p>");
		}
		if (e.getAttribute("workgroup") != null) {
			buffer.append("<p>Workgroup: "+escape(e.getAttribute("workgroup")));
			buffer.append("</p>");
		}
		if (geoipService != null) {
			for (NetworkAddressEntity addressEntity: e.getAddresses()) {
				INetworkAddress address = addressEntity.toNetworkAddress();
				if (address instanceof InternetAddress) {
					ILocation location = geoipService.getLocation((InternetAddress)address);
					if (location != null) {
						buffer.append("<p>Location: ");
						if (location.getCity() != null) {
							buffer.append(escape(location.getCity()+", "+location.getCountry()));
						} else if (location.getCountry() != null) {
							buffer.append(escape(location.getCountry()));
						} else if (location.getContinent() != null) {
							buffer.append(escape(location.getContinent()));
						} else {
							buffer.append(location.getPosition()[0]+" "+location.getPosition()[1]);
						}
						buffer.append("</p>");
						break;
					}
				}
			}
		}
		if (ip2asService != null) {
			Set<String> asSet = new HashSet<String>();
			for (NetworkAddressEntity addressEntity: e.getAddresses()) {
				INetworkAddress address = addressEntity.toNetworkAddress();
				if (address instanceof InternetAddress) {
					AS as = ip2asService.getAS((InternetAddress)address);
					if (as != null)
						asSet.add(as.getDescription());
				}
			}
			if (asSet.size() > 0) {
				buffer.append("<p>AS: ");
				Iterator<String> iterator = asSet.iterator();
				while (iterator.hasNext()) {
					String as = iterator.next();
					buffer.append(escape(as));
					if (iterator.hasNext())
						buffer.append(", ");
				}
				buffer.append("</p>");
			}
		}
		return buffer.toString();
	}

	private String getNetblockInformation(InternetNetblock netblock) {
		StringBuffer buffer = new StringBuffer();
		if (netblock.getCIDR() >= 16) { //TODO is this too big? might not be accurate
			if (geoipService != null) {
				ILocation location = geoipService.getLocation(netblock);
				if (location != null) {
					buffer.append("<p>Location: ");
					if (location.getCity() != null) {
						buffer.append(escape(location.getCity()+", "+location.getCountry()));
					} else if (location.getCountry() != null) {
						buffer.append(escape(location.getCountry()));
					} else if (location.getContinent() != null) {
						buffer.append(escape(location.getContinent()));
					} else {
						buffer.append(location.getPosition()[0]+" "+location.getPosition()[1]);
					}
					buffer.append("</p>");
				}
			}
			if (ip2asService != null) {
				AS as = ip2asService.getAS(netblock);
				if (as != null) {
					buffer.append("<p>AS: ");
					buffer.append(escape(as.getDescription()));
					buffer.append("</p>");
				}
			}
		}
		return buffer.toString();
	}

	private String getServiceInformation(ServiceEntity e) {
		if (e.getBanner() == null)
			return null;
		if ("HTTP".equals(e.getServiceType()))
			return getHTTPBannerInformation(e.getBanner());
		return "<p>"+escape(truncate(e.getBanner()))+"</p>";
	}

	private String getClientInformation(ClientEntity e) {
		if (e.getBanner() == null)
			return null;
		if (e.getServiceType().equals("HTTP"))
			return getHTTPBannerInformation(e.getBanner());
		return "<p>"+escape(truncate(e.getBanner()))+"</p>";
	}

	private String getHTTPBannerInformation(String data) {
		data = "<p>"+escape(data)+"</p>";
		data = data.replaceAll("<p>Content[^<]+</p>", "");
		data = data.replaceAll("<p>Connection[^<]+</p>", "");
		return data.replaceAll("<p>([^:<]+:)([^<]+)</p>", "<p><span color=\"blue\">$1</span>$2</p>");
	}

	private String truncate(String data) {
		if (data.length() > 400) {
			data = data.substring(0, 400)+"...";
		}
		return data;
	}

	private String escape(String data) {
		data = data.replaceAll("&", "&amp;");
		data = data.replaceAll("<", "&lt;");
		data = data.replaceAll(">", "&gt;");
		data = data.trim().replaceAll("[\\r\\n]+", "</p><p>");
		return data.replaceAll("[^\\p{Print}\\p{Blank}]", "."); // non-printable chars
	}

	private final static int PORTS_TRUNCATE_LENGTH = 50;

	private String getPortString(String ports) {
		if(ports.length() < PORTS_TRUNCATE_LENGTH) return ports;
		int idx = ports.lastIndexOf(',', PORTS_TRUNCATE_LENGTH);
		if(idx == -1) return ports;
		return ports.substring(0,idx) + "...";
		
	}

/*
	private String getPortSetInformation(PortSetEntity portset) {
		final List<String> lines = breakIntoLines(portset.getPorts());

		final StringBuilder result = new StringBuilder();
		result.append("<P>Listening " + portset.getProtocol().toUpperCase(Locale.ENGLISH) + " Ports:</P>");
		for(String line: lines) {
			result.append("<P>");
			result.append(line.replaceAll(",", ", "));
			result.append("</P>\n");
		}
		return result.toString();
	}
*/
	private final static int PORTS_FORMATTED_LINE_LENGTH = 40;

	private List<String> breakIntoLines(String ports) {
		String remaining = ports;
		List<String> lines = new ArrayList<String>();
		while(remaining.length() > PORTS_FORMATTED_LINE_LENGTH) {
			int idx = remaining.lastIndexOf(',', PORTS_FORMATTED_LINE_LENGTH);
			if(idx == -1) {
				// Should not happen
				lines.add(remaining);
				return lines;
			}
			lines.add(remaining.substring(0, idx + 1));
			remaining = remaining.substring(idx + 1);
		}
		if(remaining.length() > 0) 
			lines.add(remaining);
		return lines;
	}
	
	private String getUserInformation(UserEntity e) {
		StringBuffer buffer = new StringBuffer();
		if (e.isLocked()) {
			buffer.append("<p>Locked</p>");
		}
		if (e.isPriviledged()) {
			buffer.append("<p>Priviledged User</p>");
		}
		if (e.getPassword() != null) {
			buffer.append("<p>Password: ");
			buffer.append(escape(e.getPassword().length() > 0 ? e.getPassword() : "<no password>"));
			buffer.append("</p>");
		}
		for (String hashType: e.getHashTypes()) {
			buffer.append("<p>Hash ");
			buffer.append(escape(hashType));
			buffer.append(": ");
			buffer.append(escape(e.getHash(hashType)));
			buffer.append("</p>");
		}
		if (e.getHome() != null) {
			buffer.append("<p>Home: ");
			buffer.append(escape(e.getHome()));
			buffer.append("</p>");
		}
		return buffer.toString();
	}
}
