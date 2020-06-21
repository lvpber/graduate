package graduate.concurrent;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class RaftThreadPool {
	/** jvm����ʹ�õĴ��������� */
	private static int cup = Runtime.getRuntime().availableProcessors();
	
	/** ����̳߳�Ϊ��������Ŀ*2 */
	private static int maxPoolSize = cup * 2;
	
	/** �������д�С */
	private static final int queenSize = 1024;
	
	/** �����̴߳��ʱ�� 1���� */
	private static final long keepTime = 1000 * 60;
	
	/** ���ʱ�䵥λ���� */
	private static TimeUnit keepTimeUnit = TimeUnit.MILLISECONDS;
	
	/** ��ʱִ������ */
	private static ScheduledExecutorService scheduledExecutorService = getScheduled();
	
	/** ����ִ������ */
	private static ThreadPoolExecutor threadPoolExecutor = getThreadPoolExecutor();
	
	private static ThreadPoolExecutor getThreadPoolExecutor() {
		return new RaftThreadPoolExecutor(cup, maxPoolSize, 
				keepTime, keepTimeUnit, 
				new LinkedBlockingDeque(queenSize),
				new RaftNameThreadFactory()
				);
	}
	
	/** ����������ִ������ */
	private static ScheduledExecutorService getScheduled() {
		return new ScheduledThreadPoolExecutor(cup,new RaftNameThreadFactory());
	}
	
	/**  
	 * ����ָ������ִ��ָ������
	 * @param r					Ҫִ�е�����
	 * @param initDelay		��ʼ�ȴ�ʱ��
	 * @param delay			����
	 */
	public static void scheduleAtFixedRate(Runnable r, long initDelay , long delay) {
		scheduledExecutorService.scheduleAtFixedRate(r, initDelay, delay, TimeUnit.MILLISECONDS);
	}
	
	/**
	 * ����ָ������ִ������
	 * @param r			Ҫִ�е�����
	 * @param delay	����
	 */
	public static void scheduleWithFixedDelay(Runnable r , long delay) {
		scheduledExecutorService.scheduleWithFixedDelay(r, 0, delay, TimeUnit.MILLISECONDS);
	}
	
	public static <T> Future<T> submit(Callable r) {
		return threadPoolExecutor.submit(r);
	}
	
	public static void execute(Runnable r) {
		threadPoolExecutor.execute(r);
	}
	
	/**
	 * @param r			r��һ��Runnable���󣬴��ݽ����Ŀ�����һ����ִ�еĶ������(Thread)
	 * 								Ҳ����ֻ��һ�δ��룬���纯��ʽ�ӿڣ�����ǿ�ִ�ж���r�ڴ���run������
	 * 								�Ϳ���ֱ�����У�����Ǻ���ʽ�ӿڣ��������Ĵ���鲻�߱��Լ����е�������
	 * 								��Ҫ�����̳߳ش���
	 * @param sync		true 		�Լ�����
	 * 								false 	�����̳߳�����
	 */
	public static void execute(Runnable r, boolean sync) {
		if(sync) 
		{
			r.run();
		} 
		else 
		{
			threadPoolExecutor.execute(r);
		}
	}
}
