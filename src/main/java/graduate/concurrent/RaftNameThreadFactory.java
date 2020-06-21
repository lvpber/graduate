package graduate.concurrent;

import java.util.concurrent.ThreadFactory;

public class RaftNameThreadFactory implements ThreadFactory{

	@Override
	public Thread newThread(Runnable r) {
		// TODO Auto-generated method stub
		Thread thread = new RaftThread("Raft Thread", r);
		thread.setDaemon(true);			//Ϊɶ���ó��ػ��߳�
		
		return thread;
	}
	
}
