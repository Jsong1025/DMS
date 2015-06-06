package vo;

/**
 * @author Jsong
 * 登陆登出数据实体类
 * 		该类的一个对象对应着记录中的一条登陆或登出数据
 */
public class LogData {
	
	private String user;	//OS用户名
	private int pid;		//进程ID
	private short type;		//登陆类型
	private int time;		//登陆或登出时间
	private String host;	//终端IP

	/* 日志文件记录长度 */
	public static final int LOG_LENGTH = 372;
	
	/* 业务账号，用户属性偏移量、长度 */
	public static final int USER_OFFSET = 0;
	public static final int USER_LENGTH = 32;
	
	/* 进程ID pid的偏移量 */
	public static final int PID_OFFSET = 68;
	
	/* 登陆类型type的偏移量 */
	public static final int TYPE_OFFSET = 72;
	
	/* 登陆登出时间time的偏移量 */
	public static final int TIME_OFFSET = 80;
	
	/* 终端IP host的偏移量，长度 */
	public static final int HOST_OFFSET = 114;
	public static final int HOST_LENGTH = 258;
	
	/*登陆登出状态*/
	public static final int USER_LOGIN = 7;
	public static final int USER_LOGOUT = 8;

	public LogData() {
	}

	public LogData(String user, int pid, short type, int time, String host) {
		this.user = user;
		this.pid = pid;
		this.type = type;
		this.time = time;
		this.host = host;
	}
	
	// 带拆分的构造方法
	public LogData(String log){
		String data[] = log.split(",");
		this.user = data[0];
		this.pid = Integer.parseInt(data[1]);
		this.type = Short.parseShort(data[2]);
		this.time = Integer.parseInt(data[3]);
		this.host = data[4];
	}

	public String getUser() {
		return user;
	}

	public int getPid() {
		return pid;
	}

	public short getType() {
		return type;
	}

	public int getTime() {
		return time;
	}

	public String getHost() {
		return host;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public void setPid(int pid) {
		this.pid = pid;
	}

	public void setType(short type) {
		this.type = type;
	}

	public void setTime(int time) {
		this.time = time;
	}

	public void setHost(String host) {
		this.host = host;
	}
	
	@Override
	public String toString() {
		return new String(user+","+pid+","+type+","+time+","+host);
	}
	
}


