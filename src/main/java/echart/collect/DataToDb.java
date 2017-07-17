package main.java.echart.collect;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class DataToDb 
{
	//获取遗漏表中最大期号
	 public String findMaxIssueIdFromDesMissTable()
	  {
	    Connection srcConn = ConnectDesDb.getDesConnection();
	    String issueId = null;
	    PreparedStatement pstmt = null;
	    String sql = "SELECT max(issue_number) FROM "+App.descMissTbName+"";
	    try
	    {
	      pstmt = (PreparedStatement)srcConn.prepareStatement(sql);
	      ResultSet rs = pstmt.executeQuery();
	      while (rs.next()) {
	        issueId = rs.getString(1);
	      }
	      if ((rs != null) && (!rs.isClosed())) {
	        rs.close();
	      }
	    }
	    catch (SQLException e)
	    {
	      e.printStackTrace();
	    }
	    return issueId;
	  }
	 
	 //插入遗漏数据
	 public void insertMissData(SrcFiveDataBean srcDataBean)
	 {
		 Connection conn = ConnectDesDb.getDesConnection();
		 try {
			if (!haveMissDataInIssueId(srcDataBean.getIssueId(), conn))
			  {
			    batchUpdateMiss(srcDataBean, conn);
			  }
		} catch (SQLException e) {
			e.printStackTrace();
		}
	 }
	 //根据期号获取开奖数据
	 public SrcFiveDataBean getRecordByIssueId(String issueId)
	  {
	    Connection srcConn = ConnectDesDb.getDesConnection();
	    SrcFiveDataBean srcDataBean = new SrcFiveDataBean();
	    PreparedStatement pstmt = null;
	    String sql = "SELECT issue_number,no1,no2,no3,no4,no5  FROM "+App.descNumberTbName+" WHERE issue_number = '" + issueId + "'";
	    try
	    {
	      pstmt = (PreparedStatement)srcConn.prepareStatement(sql);
	      ResultSet rs = pstmt.executeQuery();
	      while (rs.next())
	      {
	        srcDataBean.setIssueId(rs.getString(1));
	        srcDataBean.setNo1(rs.getInt(2));
	        srcDataBean.setNo2(rs.getInt(3));
	        srcDataBean.setNo3(rs.getInt(4));
	        srcDataBean.setNo4(rs.getInt(5));
	        srcDataBean.setNo5(rs.getInt(6));
	      }
	      if ((rs != null) && (!rs.isClosed())) {
	        rs.close();
	      }
	    }
	    catch (SQLException e)
	    {
	     e.printStackTrace();
	    }
	    return srcDataBean;
	  }
	  
	 //获取数据表中的最大期号
	  public String findMaxIssueIdFromDescDb()
	  {
		  String issueNumber = "";
		    String sql = null;
		    sql = "SELECT ISSUE_NUMBER FROM "+App.descNumberTbName+" ORDER BY ISSUE_NUMBER DESC LIMIT 1";
		    Connection conn = ConnectDesDb.getDesConnection();
		    PreparedStatement pstmt = null;
		    ResultSet rs = null;
		    try
		    {
		      pstmt = (PreparedStatement)conn.prepareStatement(sql);
		      rs = pstmt.executeQuery();
		      while (rs.next())
		      {
		    	  
		        if (rs.isFirst()) {
		        	issueNumber = rs.getString(1);
		        }
		      }
		    }
		    catch (SQLException e)
		    {
		      e.printStackTrace();
		    }
		    finally {
		    	ConnectDesDb.dbClose(conn, pstmt, rs);
		    }
		    return issueNumber;
	  }
	  
	 /* public boolean judgeIssueNumber(String issueNumber)
	  {
	    Pattern pattern = Pattern.compile("[0-9]*");
	    Matcher isNum = pattern.matcher(issueNumber);
	    if (!isNum.matches()) {
	      return false;
	    }
	    return true;
	  }*/
	  
	  //处理基础数据表数据
	  public  SrcFiveDataBean caluExtentInfo(SrcFiveDataBean srcDataBean)
	  {
		  	int oneInt = srcDataBean.getNo1();
		    int twoInt = srcDataBean.getNo2();
		    int threeInt = srcDataBean.getNo3();
		    int fourInt = srcDataBean.getNo4();
		    int fiveInt = srcDataBean.getNo5();
		    int threeSpan = 0;
		    int threeSum = 0;
		    int oddNumber = 0;
		    int bigCount = 0;
		    threeSum = oneInt + twoInt + threeInt;
		    int fiveSum = oneInt + twoInt + threeInt + fourInt + fiveInt;
		    int[] three = { oneInt, twoInt, threeInt };
		    int[] five = { oneInt, twoInt, threeInt, fourInt, fiveInt };
		    for (int i = 0; i < five.length; i++)
		    {
		      if (five[i] % 2 != 0) {
		        oddNumber++;
		      }
		      if (five[i] > 6) {
		        bigCount++;
		      }
		    }
		    Arrays.sort(three);
		    threeSpan = three[2] - three[0];
		    Arrays.sort(five);
		    int fiveSpan = five[4] - five[0];
		    srcDataBean.setOddNum(oddNumber);
		    srcDataBean.setBigCount(bigCount);
		    srcDataBean.setThreeSpan(threeSpan);
		    srcDataBean.setFiveSpan(fiveSpan);
		    srcDataBean.setThreeSum(threeSum);
		    srcDataBean.setFiveSum(fiveSum);
		    srcDataBean.setBiggestNum(five[4]);
		    srcDataBean.setBiggerNum(five[3]);
		    srcDataBean.setMiddleNum(five[2]);
		    srcDataBean.setSmallerNum(five[1]);
		    srcDataBean.setSmallestNum(five[0]);
		    StringBuffer noArr = new StringBuffer();
		    for (int num : five) 
		    {
		    	noArr.append(CollectUtil.translate(num));
			}
		    srcDataBean.setNoArr(noArr.toString());
		    return srcDataBean;
	  }
	  
	  //插入基础数据
	  public  void insertBaseData(SrcFiveDataBean srcDataBean)
	  {
		  try 
		  {
			  Connection con = ConnectDesDb.getDesConnection();
			  if(!haveDataInIssueId(srcDataBean.getIssueId(),con))
			  {
				  srcDataBean = caluExtentInfo(srcDataBean);//计算基础值
				  insertData(srcDataBean, con);
			  }
		  } 
		  catch (SQLException e) 
	  		{
			  e.printStackTrace();
	  		}
	  }
	 
	  //插入基础数据到表中
	  public  void insertData(SrcFiveDataBean srcDataBean,Connection conn)
	    throws SQLException
	  {
		  String sql = "insert into " + App.descNumberTbName + ""
	    		+ "(ISSUE_NUMBER,NO1,NO2,NO3,NO4,NO5,THREE_SUM,THREE_SPAN,"
	    		+ "FIVE_SPAN,FIVE_SUM,BIG_COUNT,ODD_COUNT,ORIGIN,CREATE_TIME,SMALLER_NUM"
	    		+ ",SMALLEST_NUM,MIDDLE_NUM,BIGGER_NUM,BIGGEST_NUM,NOARR) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		    PreparedStatement pstmt = null;
		    try
		    {
		      pstmt = (PreparedStatement)conn.prepareStatement(sql);
		      pstmt.setString(1, srcDataBean.getIssueId());
		      pstmt.setInt(2, srcDataBean.getNo1());
		      pstmt.setInt(3, srcDataBean.getNo2());
		      pstmt.setInt(4, srcDataBean.getNo3());
		      pstmt.setInt(5, srcDataBean.getNo4());
		      pstmt.setInt(6, srcDataBean.getNo5());
		      pstmt.setInt(7, srcDataBean.getThreeSum());
		      pstmt.setInt(8, srcDataBean.getThreeSpan());
		      pstmt.setInt(9, srcDataBean.getFiveSpan());
		      pstmt.setInt(10, srcDataBean.getFiveSum());
		      pstmt.setInt(11, srcDataBean.getBigCount());
		      pstmt.setInt(12, srcDataBean.getOddNum());
		      pstmt.setInt(13, 1);
		      pstmt.setTimestamp(14, new Timestamp(new Date().getTime()));
		      pstmt.setInt(15, srcDataBean.getSmallerNum());
		      pstmt.setInt(16, srcDataBean.getSmallestNum());
		      pstmt.setInt(17, srcDataBean.getMiddleNum());
		      pstmt.setInt(18, srcDataBean.getBiggerNum());
		      pstmt.setInt(19, srcDataBean.getBiggestNum());
		      pstmt.setString(20, srcDataBean.getNoArr());
		     
		      pstmt.executeUpdate();
		    }
		    catch (SQLException e)
		    {
		      e.printStackTrace();
		    }
		    finally
		    {
		      ConnectDesDb.dbClose(conn, pstmt, null);
		    }
	    }
	  
	  //判断当前期号数据是否已经存在于基础数据表
	  private boolean haveDataInIssueId(String issueId, Connection conn)
	    throws SQLException
	  {
	    boolean flag = false;
	    int count = 0;
	    String sql = "SELECT COUNT(*) FROM " + App.descNumberTbName + " WHERE ISSUE_NUMBER = '" + issueId + "'";
	    PreparedStatement pstmt = null;
	    ResultSet rs = null;
	    try
	    {
	      pstmt = (PreparedStatement)conn.prepareStatement(sql);
	      rs = pstmt.executeQuery();
	      while (rs.next()) {
	        count = rs.getInt(1);
	      }
	      if (count > 0) {
	        flag = true;
	      }
	    }
	    catch (SQLException e)
	    {
	    	System.out.println("haveDataInIssueId" + e.getCause());
	    }
	    finally
	    {
	      if ((rs != null) && (!rs.isClosed())) {
	        rs.close();
	      }
	      if ((pstmt != null) && (!pstmt.isClosed())) {
	        pstmt.close();
	      }
	    }
	    return flag;
	  }
	  
	  //判断当前期号遗漏数据是否已经存在于数据表
	  private boolean haveMissDataInIssueId(String issueId, Connection conn)
	    throws SQLException
	  {
	    boolean flag = false;
	    int count = 0;
	    String sql = "SELECT COUNT(*) FROM "+App.descMissTbName+" WHERE ISSUE_NUMBER = '" + issueId + "'";
	    PreparedStatement pstmt = null;
	    ResultSet rs = null;
	    try
	    {
	      pstmt = (PreparedStatement)conn.prepareStatement(sql);
	      rs = pstmt.executeQuery();
	      while (rs.next()) {
	        count = rs.getInt(1);
	      }
	      if (count > 0) {
	        flag = true;
	      }
	    }
	    catch (SQLException e)
	    {
	    	System.out.println("" + e.getCause());
	    }
	    finally
	    {
	      if ((rs != null) && (!rs.isClosed())) {
	        rs.close();
	      }
	      if ((pstmt != null) && (!pstmt.isClosed())) {
	        pstmt.close();
	      }
	    }
	    return flag;
	  }
	  
	  //批量更新遗漏数据
	  private void batchUpdateMiss(SrcFiveDataBean srcDataBean, Connection conn)
	    throws SQLException
	  {
		    PreparedStatement stmt = null;
		    try
		    {
		      DatabaseMetaData dbmd = conn.getMetaData();
		      boolean a = dbmd.supportsBatchUpdates();
		      if (a)
		      {
		        boolean booleanautoCommit = conn.getAutoCommit();
		        
		        conn.setAutoCommit(false);
		        stmt = (PreparedStatement)conn.prepareStatement("");
		        
		        //先将所有的遗漏值都加1，然后再下面的代码中判断不同组合的当前遗漏值是否要清0
		        stmt.addBatch("UPDATE "+App.descMissTbName+" SET ISSUE_NUMBER = " + srcDataBean.getIssueId() + "," + 
		          "CURRENT_MISS = CURRENT_MISS+1,OPTIONAL_COMPOUND = OPTIONAL_COMPOUND+1," + 
		          "TWOCODE_COMPOUND=TWOCODE_COMPOUND+1,THREECODE_COMPOUND=THREECODE_COMPOUND+1;");
		        
		        List<String> sqlList = null;
		        sqlList = AnalysisMissUtil.updateGroupMiss(srcDataBean, 2);
		        sqlList.addAll(AnalysisMissUtil.updateGroupMiss(srcDataBean, 3));
		        sqlList.addAll(AnalysisMissUtil.updateGroupMiss(srcDataBean, 4));
		        sqlList.addAll(AnalysisMissUtil.updateGroupMiss(srcDataBean, 5));
		        for (String sql : sqlList) {
		          stmt.addBatch(sql);
		        }
		        stmt.addBatch(AnalysisMissUtil.updateGreatFiveGroupMiss(srcDataBean, 6));
		        stmt.addBatch(AnalysisMissUtil.updateGreatFiveGroupMiss(srcDataBean, 7));
		        stmt.addBatch(AnalysisMissUtil.updateGreatFiveGroupMiss(srcDataBean, 8));
		        String[] temp = AnalysisMissUtil.updateBeforeRen2GroupMiss(srcDataBean);
		        stmt.addBatch(temp[0]);
		        stmt.addBatch(temp[1]);
		        temp = AnalysisMissUtil.updateBeforeRen3GroupMiss(srcDataBean);
		        stmt.addBatch(temp[0]);
		        stmt.addBatch(temp[1]);
		        stmt.addBatch(AnalysisMissUtil.updateDirectBeforeRen2GroupMiss(srcDataBean));
		        stmt.addBatch(AnalysisMissUtil.updateDirectBeforeRen3GroupMiss(srcDataBean));
		        
		        stmt.addBatch("UPDATE "+App.descMissTbName+" SET MAX_MISS = CURRENT_MISS WHERE CURRENT_MISS > MAX_MISS AND CURRENT_MISS <> 0;");
		        stmt.addBatch("UPDATE "+App.descMissTbName+" SET TWOCODE_COMPOUND_MAXMISS = TWOCODE_COMPOUND WHERE TWOCODE_COMPOUND > TWOCODE_COMPOUND_MAXMISS AND TWOCODE_COMPOUND <> 0;");
		        stmt.addBatch("UPDATE "+App.descMissTbName+" SET THREECODE_COMPOUND_MAXMISS = THREECODE_COMPOUND WHERE THREECODE_COMPOUND > THREECODE_COMPOUND_MAXMISS AND THREECODE_COMPOUND <> 0;");
		        stmt.addBatch("UPDATE "+App.descMissTbName+" SET OPTIONAL_COMPOUND_MAXMISS = OPTIONAL_COMPOUND WHERE OPTIONAL_COMPOUND > OPTIONAL_COMPOUND_MAXMISS AND OPTIONAL_COMPOUND <> 0;");
		        stmt.executeBatch();
		        
		        conn.commit();
		        conn.setAutoCommit(booleanautoCommit);
		      }
		    }
		    catch (SQLException sqlEx)
		    {
		    }
		    finally
		    {
		      ConnectDesDb.dbClose(conn, stmt, null);
		    }
		  }
}
