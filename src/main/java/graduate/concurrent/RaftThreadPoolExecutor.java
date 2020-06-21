package graduate.concurrent;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class RaftThreadPoolExecutor extends ThreadPoolExecutor {

	/**
	 * @param corePoolSize			�����߳�����
	 * @param maximumPoolSize	�̳߳�����߳���
	 * @param keepAliveTime		���߳���Ŀ���������߳���Ŀ�󣬶�������߳������ʱ��
	 * @param unit							ʱ��ĵ�λ
	 * @param workQueue				��������
	 * @param raftNameThreadFactory		�̹߳������ڴ����߳�
	 */
	public RaftThreadPoolExecutor(int corePoolSize, int maximumPoolSize, 
			long keepAliveTime, TimeUnit unit,
			BlockingQueue<Runnable> workQueue,
			RaftNameThreadFactory raftNameThreadFactory
			) {
		super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue,raftNameThreadFactory);
		// TODO Auto-generated constructor stub
	}
	
	private static final ThreadLocal<Long> COST_TIME_WATCH = ThreadLocal.withInitial(System::currentTimeMillis);
	
	@Override
	protected void beforeExecute(Thread t,Runnable r) {
		COST_TIME_WATCH.get();
	}
	
	@Override
	protected void afterExecute(Runnable r,Throwable t) {
		COST_TIME_WATCH.remove();
	}
	
	
}
