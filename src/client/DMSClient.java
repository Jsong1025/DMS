package client;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;

import util.Util;
import vo.LogData;

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

			byte[] log = new byte[LogData.LOG_LENGTH];
			for (int i = 0; i < batch; i++) {
				fis.read(log);
				LogData logData = parseLog(log);
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

	public static void main(String[] args) {
		DMSClient client = new DMSClient();
		client.readNextLog();
		client.parseLog();
	}
}