package main.java.echart.collect;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

public class ConnectDesDb {
	public static synchronized Connection getDesConnection()
	  {
	    return _getConnection();
	  }
	  
	  private static Connection _getConnection()
	  {
	    try
	    {
	      String driver = "";
	      String url = "";
	      String username = "";
	      String password = "";
	      Properties p = new Properties();
	      InputStream is = ConnectDesDb.class.getClassLoader().getResourceAsStream("db.properties");
	      p.load(is);
	      driver = p.getProperty("driver", "");
	      url = p.getProperty("des.url", "");
	      username = p.getProperty("des.username", "");
	      password = p.getProperty("des.password", "");
	      Properties pr = new Properties();
	      pr.put("user", username);
	      pr.put("password", password);
	      pr.put("characterEncoding", "UTF-8");
	      pr.put("useUnicode", "TRUE");
	      Class.forName(driver).newInstance();
	      return DriverManager.getConnection(url, pr);
	    }
	    catch (Exception se)
	    {
	      se.printStackTrace();
	    }
	    return null;
	  }
	  
	public static void dbClose(Connection conn,PreparedStatement pstmt,ResultSet rs)
	  {
		 
		try {
			 if(null != pstmt &&!pstmt.isClosed())
				  {
				 	pstmt.close();
				  }
			 if(null != conn &&!conn.isClosed())
			  {
				 conn.close();
			  }
			 if(null != rs &&!rs.isClosed())
			  {
				 rs.close();
			  }
			} catch (SQLException e) {
				e.printStackTrace();
			}
		 
	  }
	  
}
