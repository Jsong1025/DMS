package server;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import net.NetService;
import net.Request;
import net.Response;

import vo.LogRec;

/**
 * 服务器端类
 * 
 * @author Jsong
 *
 */
public class DMSServer {

	/* 端口号 */
	private int serverPort;

	/* 保存记录对文件 */
	private File serverLogRecFile;

	/* 队列大小 */
	private int queueSize;

	/* 保存间歇 */
	private int saveIterval;

	private ServerSocket serverSocket;

	/* 日志文件保存任务 */
	private Timer recSaveTimer;

	/* 保存记录对的队列 */
	private BlockingDeque<LogRec> logRecsQueue;

	public DMSServer() {
		serverPort = 8000;
		serverLogRecFile = new File("server_logRec");
		queueSize = 10000;
		saveIterval = 5000;
	}
	
	/**
	 * 主逻辑
	 */
	public void action() {
		try {
			serverSocket = new ServerSocket(serverPort);
			logRecsQueue = new LinkedBlockingDeque<LogRec>(queueSize);
			
			// 定时向文件中写入队列中的数据
			recSaveTimer = new Timer();
			recSaveTimer.schedule(new SaveProcess(), 0, saveIterval);
			
			// 循环接收客户端发过来的请求数据
			while (true) {
				System.out.println("等待接收客户端访问...");
				Socket socket = serverSocket.accept();
				System.out.println("接收到客户端的访问。。。");
				new Thread(new ReceiveProcess(socket)).start();
			}
			
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException();
		}
	}
	
	public static void main(String[] args) {
		DMSServer server = new DMSServer();
		server.action();
	}

	/**
	 * 接收线程类
	 * 
	 * @author Jsong
	 *
	 */
	class ReceiveProcess implements Runnable {

		Socket socket;

		public ReceiveProcess() {}
		public ReceiveProcess(Socket socket) {
			System.out.println("启动一个客户端线程服务。");
			this.socket = socket;
		}

		@Override
		public void run() {
			try {
				// 从客户端获取请求数据
				NetService net = new NetService();
				Request request = net.receiveRequest(socket);
				List<LogRec> list = request.getData();

				// 将List集合中的数据逐条插入logRecQueue队列中
				Response response;
				try {
					for (LogRec logRec : list) {
						boolean added;
						do {
							added = logRecsQueue.offer(logRec,1,TimeUnit.SECONDS);
						} while (!added);
						System.out.println("向队列插入"+logRec);
					}
					response = new Response(System.currentTimeMillis(), Response.OK);
					System.out.println("全部插入成功！");
				} catch (InterruptedException e) {
					e.printStackTrace();
					response = new Response(System.currentTimeMillis(), Response.ERROR);
				}
				
				// 发送响应，并关闭Socket
				net.sendResponse(socket, response);
				socket.close();

			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	/**
	 * 保存线程类
	 * 
	 * @author Jsong
	 *
	 */
	class SaveProcess extends TimerTask {

		@Override
		public void run() {
			try {
				
				// serverLogRecFile文件不存在，创建文件
//				if (!serverLogRecFile.exists()) {
//					serverLogRecFile.createNewFile();
//				}

				PrintWriter writer = new PrintWriter(serverLogRecFile);

				// 将logRecsQueue队列中的对象依次写入serverLogRecFile文件中
				while (!logRecsQueue.isEmpty()) {
					LogRec logRec = logRecsQueue.poll();
					if (logRec == null) {
						break;
					} else {
						writer.print("123");
						writer.println(logRec);
						System.out.println("写入文件"+logRec);
					}
				}

				writer.close();

			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}
}
