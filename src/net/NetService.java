package net;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import vo.LogRec;

/**
 * 请求响应封装类
 * 
 * @author Jsong
 *
 */
public class NetService {

	/**
	 * 发送请求
	 * 
	 * @param socket	要发送到的Socket对象
	 * @param request	要发送的Request对象
	 * @throws IOException
	 */
	public void sendRequest(Socket socket,Request request) throws IOException {
		OutputStream out = socket.getOutputStream();
		PrintWriter writer = new PrintWriter(out);
		List<LogRec> logRecs = request.getData();

		//写入头部
		writer.println(request.getTime());
		writer.println(request.getIp());
		writer.println(logRecs.size());

		//写入数据
		for (LogRec logRec : logRecs) {
			writer.println(logRec);
		}
		writer.flush();

		writer.close();
		out.close();
	}

	/**
	 * 接收请求
	 * 
	 * @param socket	请求所在Socket的对象
	 * @return			Request对象
	 * @throws IOException
	 */
	public Request receiveRequest(Socket socket) throws IOException {
		InputStream in = socket.getInputStream();
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));

		long time = Long.parseLong(reader.readLine());
		String ip = reader.readLine();
		int size = Integer.parseInt(reader.readLine());

		List<LogRec> list = new ArrayList<LogRec>();
		for (int i = 0; i < size; i++) {
			list.add(new LogRec(reader.readLine()));
		}
		
		reader.close();
		in.close();

		Request request = new Request(time, ip, list);
		return request;
	}

	/**
	 * 发送响应
	 * 
	 * @param socket		要发送的Socket对象
	 * @param response		要发送的Response对象
	 * @throws IOException
	 */
	public void sendResponse(Socket socket,Response response) throws IOException {
		OutputStream out = socket.getOutputStream();
		PrintWriter writer = new PrintWriter(out);

		writer.println(response.getTime());
		writer.println(response.getSate());
		writer.close();
		
		writer.close();
		out.close();
	}

	/**
	 * 接收响应
	 * 
	 * @param socket	响应所在的Socket对象
	 * @return			Response对象
	 * @throws IOException
	 */
	public Response receiveResponse(Socket socket) throws IOException {
		InputStream in = socket.getInputStream();
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));

		long time = Long.parseLong(reader.readLine());
		int sate = Integer.parseInt(reader.readLine());
		
		reader.close();
		in.close();

		Response response = new Response(time, sate);
		return response;
	}
}
