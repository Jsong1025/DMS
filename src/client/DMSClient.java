package client;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import net.NetService;
import net.Request;
import net.Response;

import util.Util;
import vo.LogData;
import vo.LogRec;

/**
 * 客户端类
 * 
 *		用于从日志文件中读取、发送数据
 * 
 * @author Jsong
 *
 */
public class DMSClient {

	/* 要读取的日志文件 */
	private File logFile;

	/* 临时文件，保存每一次读取出来的batch条数据  */
	private File tempLogFile;

	/* 临时文件，存放解析成文本内容的日志文件  */
	private File txtLogFile;

	/* 用于存放上一次读取后的游标位置，方便下一次读取  */
	private File lastPositionFile;

	/* 存放未匹配成功的Login日志  */
	private File loginLogFile;

	/* 存放已经匹配成功的日志记录对  */
	private File logRecFile;

	/* 保存当前操作序列号  */
	private File stepIndexFile;

	/* 每次要读取的最大记录数  */
	private int batch;

	/* 当前采集端所在机器IP */
	private String serverHost;

	/* DMS服务器IP */
	private String DMSServerHost;

	/* DMS服务器端口号 */
	private int DMSServerPost;

	private Thread processThread;

	public DMSClient() {
		this.batch = 10;
		this.logFile = new File("wtmpx");
		this.tempLogFile = new File("temp");
		this.txtLogFile = new File("log");
		this.lastPositionFile = new File("last_position");
		this.loginLogFile = new File("login");
		this.logRecFile = new File("logRec");
		this.stepIndexFile = new File("step");
		this.DMSServerHost = "127.0.0.1";
		this.DMSServerPost = 8000;
		this.serverHost = "127.0.0.1";
	}

	/**
	 * 将字节数组解析为LogData对象
	 * 
	 * @param logs
	 * @return
	 */
	public LogData parseLog(byte[] logs){
		String user = Util.toString(logs, LogData.USER_OFFSET, LogData.USER_LENGTH);
		int pid = Util.toInt(logs, LogData.PID_OFFSET);
		short type = Util.toShort(logs, LogData.TYPE_OFFSET);
		int time = Util.toInt(logs, LogData.TIME_OFFSET);
		String host = Util.toString(logs, LogData.HOST_OFFSET, LogData.HOST_LENGTH);
		return new LogData(user, pid, type, time, host);
	}

	/**
	 * 判断日志文件LogFile是否更新，已经更新返回true，没有返回false
	 * 
	 * @return
	 */
	public boolean isUpdate(){
		RandomAccessFile raf = null;
		try {
			raf = new RandomAccessFile(logFile, "r");
			int position = 0;
			if (lastPositionFile.exists()) {
				position = Util.readInt(lastPositionFile);
			}
			raf.seek(position);
			if (raf.read() == -1) {
				return false;
			} else {
				return true;
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			throw new RuntimeException("日志文件不存在");
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (raf != null) {
				try {
					raf.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return false;
	}

	/**
	 * 从日志文件LogFile中读取制定数目batch条记录，存放在临时文件tempLogFile中
	 * 
	 * @return
	 */
	public boolean readNextLog(){

		// 如果tempLogFile文件已经存在，则返回true
		if (this.tempLogFile.exists()) {
			return true;
		}

		// 如果文件没有更新，返回false
		if (!isUpdate()) {
			return false;
		}

		int position = 0;
		if (this.lastPositionFile.exists()) {
			position = Util.readInt(lastPositionFile);
		}
		RandomAccessFile raf = null;
		FileOutputStream fos = null;

		try {
			raf = new RandomAccessFile(logFile, "r");
			fos = new FileOutputStream(tempLogFile);
			raf.seek(position);

			// 循环读取每一条数据，并写入临时文件
			byte b = 0;
			int i;
			for (i = 0; i < batch; i++) {
				for (int j = 0; j < LogData.LOG_LENGTH; j++) {
					if (b == -1) {
						break;
					} else {
						b = (byte)raf.read();
						fos.write(b);
					}
				}
			}

			// 将当前光标位置存入lastPositionFile文件
			position += i;
			Util.saveInt(lastPositionFile, position);

			return true;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			throw new RuntimeException("日志文件不存在");
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (raf != null) {
					raf.close();
				}
				if (fos != null) {
					fos.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return false;		
	}

	/**
	 * 将临时文件tempLogFile文件中的每一条二进制文件解析为LogData对象，
	 * 		并逐一写入txtLogFile文件中
	 * 
	 * @return
	 */
	public boolean parseLog(){
		// txtLogFile文件已经存在，返回true
		if (txtLogFile.exists()) {
			return true;
		}

		FileInputStream fis = null;
		PrintWriter pw = null;
		try {
			fis = new FileInputStream(tempLogFile);
			pw = new PrintWriter(txtLogFile);

			// 从temp文件中逐一读取数据，转换为LogData对象
			byte[] log = new byte[LogData.LOG_LENGTH];
			for (int i = 0; i < batch; i++) {
				fis.read(log);
				LogData logData = parseLog(log);
				//调用LogData中的toString()方法，写入log文件中
				pw.println(logData);
				pw.flush();
			}

			return true;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			throw new RuntimeException("temp临时文件不存在");
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (fis != null) {
					fis.close();
				}
				if (pw != null) {
					pw.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	/**
	 * 从文件中读取每一条登陆信息，存入LogData的List对象中
	 * 
	 * @param file
	 * @return
	 */
	public List<LogData> loadLogDatas(File file){
		List<LogData> list = new ArrayList<LogData>();

		InputStream in = null;
		InputStreamReader isr = null;
		BufferedReader reader = null;
		try {
			in = new FileInputStream(file);
			isr = new InputStreamReader(in);
			reader = new BufferedReader(isr);

			String log;
			while ((log = reader.readLine()) != null) {
				list.add(new LogData(log));
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (in != null) {
					in.close();
				}
				if (isr != null) {
					isr.close();
				}
				if (reader != null) {
					reader.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return list;
	}

	/**
	 * 从文件中读取每一条登陆信息对，存入LogRec的List对象中
	 * 
	 * @param file
	 * @return
	 */
	public List<LogRec> loadLogRecs(File file){
		List<LogRec> list = new ArrayList<LogRec>();

		InputStream in = null;
		InputStreamReader isr = null;
		BufferedReader reader = null;
		try {
			in = new FileInputStream(file);
			isr = new InputStreamReader(in);
			reader = new BufferedReader(isr);

			String log;
			while ((log = reader.readLine()) != null) {
				list.add(new LogRec(log));
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (in != null) {
					in.close();
				}
				if (isr != null) {
					isr.close();
				}
				if (reader != null) {
					reader.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return list;
	}

	/**
	 * 匹配两个LogData对象是否是同一个账号的登陆登出
	 * 
	 * @param login		登入LogData对象
	 * @param logout	登出LogData对象
	 * @return			登陆登出对LogRec对象 / null
	 */
	public LogRec match(LogData login,LogData logout){
		if (login.getType() != LogData.USER_LOGIN) {
			return null;
		}
		if (logout.getType() != LogData.USER_LOGOUT) {
			return null;
		}
		if (login.getUser().equals(logout.getUser()) &&
				login.getHost().equals(logout.getHost()) &&
				login.getPid() == logout.getPid() ){
			return new LogRec(login,logout,this.serverHost);
		}else {
			return null;
		}
	}
	
	/**
	 * 用一个登入LogData对象匹配LogData对象的List集合
	 * 
	 * @param login
	 * @param list
	 * @return	LogRec对象/null
	 */
	public LogRec matchLogouts(LogData login,List<LogData> list){
		for (LogData logData : list) {
			LogRec logRec = match(login, logData);
			if (logRec != null) {
				return logRec;
			}
		}
		return null;
	}

	/**
	 * 读取未匹配的日志文件（LoginLogFile），和本次解析的日志文件（txtLogFile）
	 * 		并进行匹配，并将匹配成对的日志保存在logRecFile文件中。
	 * 		如果还存在未匹配成功的记录，要将之保存在LoginLogFile中
	 * 
	 * @return
	 */
	public boolean matchLog() {
		if (!txtLogFile.exists()) {
			throw new RuntimeException("文件不存在"+txtLogFile);
		}
		if (logRecFile.exists()) {
			return true;
		}
		
		List<LogData> list = loadLogDatas(txtLogFile);
		List<LogData> loginList = new ArrayList<LogData>();
		List<LogRec> matched = new ArrayList<LogRec>();
		
		if (loginLogFile.exists()) {
			list.addAll(loadLogDatas(loginLogFile));
			loginLogFile.delete();
		}
		
		for (LogData logData : list) {
			if (logData.getType() == LogData.USER_LOGIN) {
				LogRec logRec = matchLogouts(logData, list);
				if (logRec == null) {
					loginList.add(logData);
				} else {
					matched.add(logRec);
				}
			}
		}
		
		Util.saveList(loginLogFile, loginList);
		Util.saveList(logRecFile, matched);
		txtLogFile.delete();
		return true;
	}
	
	/**
	 * 将LogRecFile中的内容发送到服务器端
	 * 
	 * @return	true表示发送成功，删除LogRecFile
	 */
	public boolean sendLogs(){
		NetService net = new NetService();
		try {
			// 创建Socket对象，并把LogRecFile中的内容封装到Request对象中
			List<LogRec> data = loadLogRecs(logRecFile);
			Socket socket = new Socket(DMSServerHost,DMSServerPost);
			Request request = new Request(System.currentTimeMillis(), serverHost, data);
			
			//发送并接收响应
			System.out.println("发送数据"+request);
			net.sendRequest(socket, request);
			Response response = net.receiveResponse(socket);
			socket.close();
			
			if (response.getSate() == Response.OK) {
				logRecFile.delete();
				System.out.println("发送成功！");
				return true;
			} else {
				System.out.println("发送失败！");
				return false;
			}
		} catch (UnknownHostException e) {
			System.out.println("发送失败！"+e.getMessage());
			return false;
		} catch (IOException e) {
			System.out.println("发送失败！"+e.getMessage());
			return false;
		}
	}
	
	public static void main(String[] args) {
		DMSClient client = new DMSClient();
		client.readNextLog();
		client.parseLog();
		client.matchLog();
		client.sendLogs();
	}
}
