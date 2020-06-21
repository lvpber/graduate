package graduate.model.node;

import java.util.List;

/** addr = IpAddr:port */
public class NodeConfig 
{
	public int selfPort;
	
	public List<String> peerAddrs;

	public int getSelfPort() {
		return selfPort;
	}

	public void setSelfPort(int selfPort) {
		this.selfPort = selfPort;
	}

	public List<String> getPeerAddrs() {
		return peerAddrs;
	}

	public void setPeerAddrs(List<String> peerAddrs) {
		this.peerAddrs = peerAddrs;
	}
	
	
}
