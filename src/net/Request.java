package net;

import java.util.List;

import vo.LogRec;

/**
 * 客户端提交日志封装类
 * 
 * @author Jsong
 *
 */
public class Request {
	
	private long time;			//时间
	private int ip;				//IP
	private List<LogRec> data;	//数据
	
	public Request() {}
	
	public Request(long time, int ip, List<LogRec> data) {
		this.time = time;
		this.ip = ip;
		this.data = data;
	}

	public long getTime() {
		return time;
	}

	public int getIp() {
		return ip;
	}

	public List<LogRec> getData() {
		return data;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public void setIp(int ip) {
		this.ip = ip;
	}

	public void setData(List<LogRec> data) {
		this.data = data;
	}

	public String toString() {
		return "Request [time=" + time + ", ip=" + ip + ", data=" + data + "]";
	}

}
