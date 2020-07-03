package graduate.logmodule.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import graduate.logmodule.ILogModule;
import graduate.model.logmodulemodel.Command;
import graduate.model.logmodulemodel.LogEntry;
import graduate.node.impl.NodeImpl;
import graduate.util.StoreUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogModuleImpl implements ILogModule
{
	/** 日志生成 */
	private static final Logger log = LoggerFactory.getLogger(LogModuleImpl.class);

//	/** 日志序列化路径 */
//	private final String LOG_MODULE_FILE_PATH = "/home/lvpb/software/graduate/logfile.txt";
	
//	/** 如果没有就加载 */
//	private List<LogEntry> logEntries = new ArrayList<LogEntry>();
	
	/** 对象与json互转工具 */
	private final Gson gson = new GsonBuilder().create();

	/** 可重入锁 */
	ReentrantLock reentrantLock = new ReentrantLock();

	/** 现在用redis 实现lomodule 和 statemachine，然后需要一个ip地址做key的唯一鉴别，所以引进这个node */
	private final String HOST_ADDR;

	// redis的两个key前缀
	// Host.log.logEntry.index
	private final String logEntryPrefix;
	// Host.log.lastIndex
	private final String logLastIndexPrefix;

	public LogModuleImpl(String hostAddr)
	{

		this.HOST_ADDR = hostAddr;
		logEntryPrefix = HOST_ADDR + ".log.logEntry.index";
		logLastIndexPrefix = HOST_ADDR + ".log.lastIndex";

//		this.node = null;
//		this.logEntryPrefix = "127.0.0.1:8000.log.logEntry.index";
//		this.logLastIndexPrefix = "127.0.0.1:8000.log.lastIndex";
	}

	/**
	 * 为啥写操作要加锁？
	 * @param logEntry
	 */
	@Override
	public void write(LogEntry logEntry){
		// 用于判断是否获取锁
		boolean success = false;
		try {
			reentrantLock.tryLock(3000,TimeUnit.MILLISECONDS);
			logEntry.setIndex(getLastIndex() + 1);
			String logEntryJson = gson.toJson(logEntry);
			StoreUtil.write(logEntryPrefix,""+logEntry.getIndex(),logEntryJson);	// 写进redis
			success = true;
			log.info("LogModuleImpl -> write() : redis success , logEntry info : [{}]" , logEntry );
		} catch (InterruptedException e) {
			log.info("LogModuleImpl -> write() : try lock faile");
			e.printStackTrace();
		} finally {
			if(success)
			{
				updateLastIndex(logEntry.getIndex());
			}
			if(reentrantLock.isHeldByCurrentThread())
			{
				reentrantLock.unlock();
			}
		}
	}

	/**
	 * 删除从startIndex到最后的所有日志
	 * @param startIndex
	 */
	@Override
	public void removeOnStartIndex(Long startIndex) {
		boolean success = false;
		int count = 0;
		long result;
		try {
			reentrantLock.tryLock(3000,TimeUnit.MILLISECONDS);
			for(long i=startIndex;i<=getLastIndex();i++)
			{
				result = StoreUtil.delete(logEntryPrefix,""+i);
				count+=result;
			}
			success = true;
			log.info("LogModuleImpl -> removeOnStartIndex() : remove from startIndex success, count = {}," +
					"startIndex = {},lastIndex = {}",count,startIndex,getLastIndex());
		} catch (InterruptedException e) {
			log.info("LogModuleImpl -> removeOnStartIndex() : try lock faile");
		} finally {
			if(success)
			{
				updateLastIndex(getLastIndex() - count);
			}
			if(reentrantLock.isHeldByCurrentThread())
			{
				reentrantLock.unlock();
			}
		}
	}

	@Override
	public LogEntry read(Long index) {
		if(index == null)
			return null;

		String logEntryJson = StoreUtil.read(logEntryPrefix,""+index);
		if(logEntryJson == null)
			return null;

		return gson.fromJson(logEntryJson,LogEntry.class);
	}

	/**
	 * 从redis中获取最后一项日志的内容
	 * @return
	 */
	@Override
	public LogEntry getLast() {
		Long index = getLastIndex();
		if(index == null)
			return null;

		String logEntryJson = StoreUtil.read(logEntryPrefix,""+index);
		if(logEntryJson == null)
			return null;

		return gson.fromJson(logEntryJson,LogEntry.class);
	}

	/**
	 *	从redis中获取当前主机的最后一条日志的索引值
	 */
	@Override
	public Long getLastIndex() {
		String lastIndexStr = StoreUtil.read(logLastIndexPrefix,"");
		if(lastIndexStr == null)
			return 0L;
		return Long.parseLong(lastIndexStr);
	}

	/**
	 * on lock
	 * 更新lastIndex
	 * @param lastIndex
	 */
	private void updateLastIndex(Long lastIndex)
	{
		if(lastIndex == null)
		{
			return;
		}

		StoreUtil.write(logLastIndexPrefix,"",lastIndex+"");
	}




	public static void main1(String[] args)
	{
		LogModuleImpl logModule = new LogModuleImpl("localhost:8000");

//		LogEntry logEntry1 = LogEntry.newBuilder()
//				.term(1)
//				.index(1L)
//				.command(
//						Command.newBuilder()
//								.key("111")
//								.value("111")
//								.build()
//				)
//				.build();
//
//		logModule.write(logEntry1);

		System.out.println(logModule.getLast() + "   " + logModule.getLastIndex());

	}








//	// 私有构造函数
//	private LogModuleImpl()
//	{
//		// TODO Auto-generated constructor stub
//		// loadLogEntryFromFile();
//	}
//
//	// 日志工厂，私有静态类实现lazy mode
//	private static class LogModuleFactory
//	{
//		private static LogModuleImpl logModuleImpl = new LogModuleImpl();
//	}
//
//	// 获取单例
//	public static LogModuleImpl getLogModuleInstance()
//	{
//		return LogModuleFactory.logModuleImpl;
//	}


	/** 先用redis 实现一下，后续再用文件系统实现，先把算法实现，在修改 */
//	@Override
//	public void write(LogEntry logEntry)
//	{
//		/** 判断是否加锁成功 */
//		boolean success = false;
//		try
//		{
//			/** 尝试获得写锁 */
//			reentrantLock.tryLock(3000, TimeUnit.MILLISECONDS);
//			logEntry.setIndex(new Long(logEntries.size()));
//			/** 先写进文件 append方式 */
//			File file = new File(LOG_MODULE_FILE_PATH);
//			RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");
//			long point = randomAccessFile.length();
//			randomAccessFile.seek(point);
//			String logEntryJson = gson.toJson(logEntry);
//			randomAccessFile.write((logEntryJson + "\r\n").getBytes()) ;
//			randomAccessFile.close();
//			/** 修改内存内容 */
//			logEntries.add(logEntry);
//			success = true;
//			log.info("LogModule.write() -> write file success , logEntry info : [{}]",logEntry);
//		}
//		catch (FileNotFoundException fileNotFoundException)
//		{
//			// TODO: handle exception
//			log.info("LogModule.write() -> 写文件失败 ： 文件没有找到");
////			System.out.println("文件未找到");
//		}
//		catch (IOException ioException) {
//			// TODO: handle exception
//			log.info("LogModule.write() -> 写文件失败 : 写文件发生错误");
////			System.out.println("写文件错误");
//		} catch (InterruptedException e) {
//			log.info("LogModule.write() -> 获取锁失败");
//		}
//		finally {
//			if(success)
//			{
//
//			}
//		}
//	}
//
//	@Override
//	public void removeOnStartIndex(Long startIndex)
//	{
//		// TODO Auto-generated method stub
//		RandomAccessFile randomAccessFile = null;
//		try
//		{
//			/** 先写进文件 append方式 */
//			File file = new File(LOG_MODULE_FILE_PATH);
//			randomAccessFile = new RandomAccessFile(file, "rw");
//			int i = 0;
//			String line;
//			while( i!=startIndex && null != (line = randomAccessFile.readLine()) )
//			{
////				System.out.println(line);
//				i++;
//			}
//			if(i != startIndex)	//要删除的行数大于当前最大行数
//			{
//				System.out.println("当前索引不够最大索引值");
//				return;
//			}
//			long startPos = randomAccessFile.getFilePointer();	//删除的开始位置
//			randomAccessFile.setLength(startPos);
//
//			/** 修改logEntries */
//			int index = startIndex.intValue();
//			int count = logEntries.size() - index;
//			for(i=0;i<count;i++)
//				logEntries.remove(index);
//
//		}
//		catch (Exception e)
//		{
//			// TODO: handle exception
//			e.printStackTrace();
//		}
//		finally
//		{
//			try
//			{
//				if(randomAccessFile != null)
//				randomAccessFile.close();
//			}
//			catch (IOException e)
//			{
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
//	}
//
//	@Override
//	public LogEntry read(Long index)
//	{
//		// TODO Auto-generated method stub
//		if(logEntries == null)
//		{
//			loadLogEntryFromFile();
//		}
//		if(logEntries.size() <= index)
//			return null;
//		return logEntries.get(index.intValue());
//	}
//
//	@Override
//	public LogEntry getLast()
//	{
//		// TODO Auto-generated method stub
//		if(logEntries == null)
//		{
//			loadLogEntryFromFile();
//		}
//		if(logEntries.size() == 0)
//			return null;
//		return logEntries.get(logEntries.size()-1);
//	}
//
//	@Override
//	public Long getLastIndex()
//	{
//		// TODO Auto-generated method stub
//		if(logEntries == null)
//		{
//			loadLogEntryFromFile();
//		}
//		return new Long(logEntries.size() - 1);
//	}
//
//	private void loadLogEntryFromFile()
//	{
//		// load log from file(LOG_MODULE_FILE_PATH)
//		try
//		{
//			File file = new File(LOG_MODULE_FILE_PATH);
//			if(!file.exists())
//			{
//				file.createNewFile();
//			}
//
//			/** 只读打开，快一点 */
//			RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r");
//			this.logEntries = new ArrayList<LogEntry>();
//			String logLine,line;
//			while( null != (line = randomAccessFile.readLine()) )
//			{
//				logLine = new String(line.getBytes("ISO-8859-1"),"utf-8");
//				LogEntry logEntry = gson.fromJson(logLine, LogEntry.class);
//				logEntries.add(logEntry);
//			}
//		}
//		catch(FileNotFoundException fileNotFoundException)
//		{
//			System.out.println("日志文件未找到");
//			this.logEntries = new ArrayList<LogEntry>();
//		}
//		catch (IOException ioException)
//		{
//			// TODO: handle exception
//			System.out.println("日志文件读取发生错误");
//			ioException.printStackTrace();
//		}
//	}
//
//	public void show()
//	{
//		System.out.println("logentries的个数 " +logEntries.size() );
//		for(int i=0;i<logEntries.size();i++)
//		{
//			System.out.println(i + " " + gson.toJson(logEntries.get(i)));
//		}
//	}
}
