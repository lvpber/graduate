package graduate.model.peer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * ���нڵ�ļ��ϣ����Զ�̬��ӽڵ��ɾ���ڵ�
 * ��Ҫʵ��ȥ�أ�Ҫʵ��Serializable�ӿڣ����ڴ���
 * @author 13299
 */
public class PeerSet implements Serializable {
	/** ��¼���л�����Ϣ Socket */
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
	
	/** ��̬�ڲ���ʵ�ֵ���ģʽ�����̰߳�ȫ */
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
