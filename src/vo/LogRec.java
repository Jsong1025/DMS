package vo;

/**
 * @author Jsong
 * 
 * 	登陆登出记录对实体类
 * 		此类的一个对象，对应于一对匹配成功的登陆登出记录。
 * 		其中包含登陆信息，登出信息，服务器IP
 */
public class LogRec {
	
	private LogData login;	//登陆
	private LogData logout;	//登出
	private String serHost; //服务器IP
	
	public LogRec() {
	}
	
	public LogRec(LogData login, LogData logout, String serHost) {
		if (login.getType() != LogData.USER_LOGIN) {
			throw new RuntimeException("登陆状态不匹配");
		}
		if (logout.getType() != LogData.USER_LOGOUT) {
			throw new RuntimeException("登出状态不匹配");
		}
		if (!login.getUser().equals(logout.getUser())) {
			throw new RuntimeException("登陆登出用户名不一致");
		}
		if (login.getPid() != logout.getPid()) {
			throw new RuntimeException("登陆登出进程ID不一致");
		}
		
		this.login = login;
		this.logout = logout;
		this.serHost = serHost;
	}
	
	// 带拆分的构造方法
	public LogRec(String log){
		String data[] = log.split("\\|");
		login = new LogData(data[0]);
		logout = new LogData(data[1]);
		serHost = data[2];
	}

	public LogData getLogin() {
		return login;
	}

	public LogData getLogout() {
		return logout;
	}

	public String getSerHost() {
		return serHost;
	}

	public void setLogin(LogData login) {
		this.login = login;
	}

	public void setLogout(LogData logout) {
		this.logout = logout;
	}

	public void setSerHost(String serHost) {
		this.serHost = serHost;
	}
	
	
	@Override
	public String toString() {
		return new String(login.toString()+"|"+logout.toString()+"|"+serHost);
	}
	
}
