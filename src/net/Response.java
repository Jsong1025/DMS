package net;

/**
 * 服务器返回信息封装类
 * 
 * @author Jsong
 *
 */
public class Response {
	
	public static final int OK = 200; 

	public static final int ERROR = 500;
	
	private long time;
	
	private int sate;
	
	public Response() {}

	public Response(long time, int sate) {
		this.time = time;
		this.sate = sate;
	}

	public long getTime() {
		return time;
	}

	public int getSate() {
		return sate;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public void setSate(int sate) {
		this.sate = sate;
	}

	public String toString() {
		return "Response [time=" + time + ", sate=" + sate + "]";
	}
	
}
