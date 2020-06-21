package graduate.logmodule.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import graduate.logmodule.ILogModule;
import graduate.model.logmodulemodel.LogEntry;

public class LogModuleImpl implements ILogModule
{
	/** 日志序列化路径 */
	private final String LOG_MODULE_FILE_PATH = "/home/lvpb/software/graduate/logfile.txt"; 
	
	/** 如果没有就加载 */
	private List<LogEntry> logEntries = new ArrayList<LogEntry>();
	
	/** 对象与json互转工具 */
	private final Gson gson = new GsonBuilder().create();
	
	private LogModuleImpl()
	{
		// TODO Auto-generated constructor stub
		loadLogEntryFromFile();
	}
	
	private static class LogModuleFactory
	{
		private static LogModuleImpl logModuleImpl = new LogModuleImpl();
	}
	
	public static LogModuleImpl getLogModuleInstance()
	{
		return LogModuleFactory.logModuleImpl;
	}
	
	
	@Override
	public void write(LogEntry logEntry)
	{
		// TODO Auto-generated method stub
		
		String logEntryJson = gson.toJson(logEntry);
		try
		{
			logEntry.setIndex(new Long(logEntries.size()));
			/** 先写进文件 append方式 */
			File file = new File(LOG_MODULE_FILE_PATH);
			RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");
			long point = randomAccessFile.length();
			randomAccessFile.seek(point);
			randomAccessFile.write((logEntryJson + "\r\n").getBytes()) ;
			randomAccessFile.close();
			/** 修改内存内容 */
			logEntries.add(logEntry);
		} 
		catch (FileNotFoundException fileNotFoundException)
		{
			// TODO: handle exception
			System.out.println("文件未找到");
		}
		catch (IOException ioException) {
			// TODO: handle exception
			System.out.println("写文件错误");
		}
	}

	@Override
	public void removeOnStartIndex(Long startIndex)
	{
		// TODO Auto-generated method stub
		RandomAccessFile randomAccessFile = null;
		try
		{
			/** 先写进文件 append方式 */
			File file = new File(LOG_MODULE_FILE_PATH);
			randomAccessFile = new RandomAccessFile(file, "rw");
			int i = 0;
			String line;
			while( i!=startIndex && null != (line = randomAccessFile.readLine()) )
			{
//				System.out.println(line);
				i++;
			}
			if(i != startIndex)	//要删除的行数大于当前最大行数
			{
				System.out.println("当前索引不够最大索引值");
				return;
			}
			long startPos = randomAccessFile.getFilePointer();	//删除的开始位置
			randomAccessFile.setLength(startPos);
			
			/** 修改logEntries */
			int index = startIndex.intValue();
			int count = logEntries.size() - index;
			for(i=0;i<count;i++)
				logEntries.remove(index);
			
		} 
		catch (Exception e)
		{
			// TODO: handle exception
			e.printStackTrace();
		}
		finally 
		{
			try
			{
				if(randomAccessFile != null)
				randomAccessFile.close();
			} 
			catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Override
	public LogEntry read(Long index)
	{
		// TODO Auto-generated method stub
		if(logEntries == null)
		{
			loadLogEntryFromFile();
		}
		if(logEntries.size() <= index)
			return null;
		return logEntries.get(index.intValue());
	}

	@Override
	public LogEntry getLast()
	{
		// TODO Auto-generated method stub
		if(logEntries == null)
		{
			loadLogEntryFromFile();
		}
		if(logEntries.size() == 0)
			return null;
		return logEntries.get(logEntries.size()-1);
	}

	@Override
	public Long getLastIndex()
	{
		// TODO Auto-generated method stub
		if(logEntries == null)
		{
			loadLogEntryFromFile();
		}
		return new Long(logEntries.size() - 1);
	}

	private void loadLogEntryFromFile()
	{
		// load log from file(LOG_MODULE_FILE_PATH)
		try
		{
			File file = new File(LOG_MODULE_FILE_PATH);
			if(!file.exists())
			{
				file.createNewFile();
			}
			
			/** 只读打开，快一点 */
			RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r");
			this.logEntries = new ArrayList<LogEntry>();
			String logLine,line;
			while( null != (line = randomAccessFile.readLine()) )
			{
				logLine = new String(line.getBytes("ISO-8859-1"),"utf-8");
				LogEntry logEntry = gson.fromJson(logLine, LogEntry.class);
				logEntries.add(logEntry);
			}
		}
		catch(FileNotFoundException fileNotFoundException)
		{
			System.out.println("日志文件未找到");
			this.logEntries = new ArrayList<LogEntry>();
		}
		catch (IOException ioException) 
		{
			// TODO: handle exception
			System.out.println("日志文件读取发生错误");
			ioException.printStackTrace();
		}
	}
	
	public void show()
	{
		System.out.println("logentries的个数 " +logEntries.size() );
		for(int i=0;i<logEntries.size();i++)
		{
			System.out.println(i + " " + gson.toJson(logEntries.get(i)));
		}
	}
}
