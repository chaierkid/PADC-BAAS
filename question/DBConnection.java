package question;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

public class DBConnection {
	public static Connection getConnection() {
		Connection conn = null;
		try {
			Context ct = new InitialContext();
			DataSource ds = (DataSource) ct.lookup("java:comp/env/question");
			conn = ds.getConnection();
		} catch (NamingException e) {
			throw new RuntimeException("数据源没有找到！");
		} catch (SQLException e){
			throw new RuntimeException("获取数据库连接对象失败！");
		}
		
		return conn;
	}
	
	public static void close(Connection conn, Statement st, ResultSet rs) {
				if(rs != null){					
					try {
						rs.close();
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
			if(st != null){				
				try {
					st.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
	}
	public static void close(Statement st) {
		if(st != null){				
			try {
				st.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
}
