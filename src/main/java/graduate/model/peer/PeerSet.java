package graduate.model.peer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 所有节点的集合，可以动态添加节点和删除节点
 * 需要实现去重，要实现Serializable接口，用于传输
 * @author 13299
 */
public class PeerSet implements Serializable {
	/** 记录所有伙伴的消息 Socket */
	private List<Peer> list = new ArrayList<>();
	
	/** Leader  */
	private volatile Peer leader;
	
	/** self */
	private volatile Peer self;
	
	public Peer getLeader() {
		return leader;
	}

	public void setLeader(Peer leader) {
		this.leader = leader;
	}

	public Peer getSelf() {
		return self;
	}

	public void setSelf(Peer self) {
		this.self = self;
	}
	
	private PeerSet() {
		
	}
	
	/** 静态内部类实现单例模式，多线程安全 */
	public static class SingletonHolder{
		private static PeerSet instance = new PeerSet();
	}
	
	public static PeerSet getInstance() {
		return SingletonHolder.instance;
	}

	public List<Peer> getPeers(){
		return list;
	}
	
	public List<Peer> getPeersWithoutSelf(){
		List<Peer> listPeers = new ArrayList<Peer>(list);
		listPeers.remove(self);
		return listPeers;
	}
	
	@Override
    public String toString() 
    {
        return "PeerSet{" +
            "list=" + list +
            ", leader=" + leader +
            ", self=" + self +
            '}';
    }
	
	public void addPeer(Peer peer) {
		list.add(peer);
	}
	
	public void removePeer(Peer peer) {
		list.remove(peer);
	}
	
	
}
