package main.java.echart.collect;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;




public class App {
	
	 static String maxIssueId = null;//最大开奖期号
	  static String lineCount = null;
	  static String descNumberTbName = null;
	  static String lotteryNum = "";//开奖号码
	  static String maxMissIssueId = null;
	  static String descMissTbName = null;
	
	private static void initParam()
	  {
	    Properties p = new Properties();
	    InputStream is = App.class.getClassLoader().getResourceAsStream("db.properties");
	    try
	    {
	      p.load(is);
	    }
	    catch (IOException e)
	    {
	      e.printStackTrace();
	    }
	    lineCount = p.getProperty("lineCount", "81");
	    descNumberTbName = p.getProperty("descNumberTbName");
	    descMissTbName = p.getProperty("descMissTbName");
	  }
	/*
	 * 执行方法入口
	 */
	public static void main(String[] args) {
		initParam();
		Timer timer = new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				try
				{
					collectData();
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
				
			}
		}, new Date(), 10000L);// 每隔10s输出
	}

	
	/**
	 * 获取口袋彩票数据
	* @Title: parseDocumentFromUrl 
	* @Description: TODO(这里用一句话描述这个方法的作用) 
	* @param @param name    设定文件 
	* @author banna
	* @date 2017年7月17日 上午11:54:15 
	* @return void    返回类型 
	* @throws
	 */
	public static void collectData()
	{
		DataToDb dataToDb = new DataToDb();
		if(null == maxIssueId &&!"".equals(maxIssueId))
		{
			maxIssueId = dataToDb.findMaxIssueIdFromDescDb();
		}
		String name = "AHSYY";//HUBSYY:湖北 ；AHSYY：安徽
		SrcFiveDataBean srcFiveDataBean = parseDocumentFromUrl(name);
		if(!maxIssueId.equals(srcFiveDataBean.getIssueId()))
		{
			maxIssueId = srcFiveDataBean.getIssueId();
			dataToDb.insertBaseData(srcFiveDataBean);
		}
		
		//遗漏统计
		maxMissIssueId = dataToDb.findMaxIssueIdFromDesMissTable();
		SrcFiveDataBean srcData = dataToDb.getRecordByIssueId(CollectUtil.getNextIssueNumber(maxMissIssueId));//获取当前遗漏期号的下一期期号
		if(null != srcData &&null != srcData.getIssueId())
		{
			maxMissIssueId = srcData.getIssueId();
			dataToDb.insertMissData(srcData);
		}
		
    }

	/**
	 * 解析接口中数据
	* @Title: parseDocumentFromUrl 
	* @Description: TODO(这里用一句话描述这个方法的作用) 
	* @param @return    设定文件 
	* @author banna
	* @date 2017年7月17日 下午12:36:50 
	* @return SrcFiveDataBean    返回类型 
	* @throws
	 */
	public static SrcFiveDataBean parseDocumentFromUrl(String name)
	{
		String url = "http://apic.itou.com/api/schedule/chart?platform="
				+ "koudai_wx&token=itouapi_2c0a5b7483f72b7b6d760a6fffeddfa3748363ef&page=1&pagesize=1&play_type=FP3"
				+ "&lottery_type="+name;
		String json= App.getHttpResponse(url);
        JSONObject jsonformat = JSONObject.fromObject(json);
        JSONArray data = jsonformat.getJSONArray("data");
        JSONObject object = data.getJSONObject(0);
        String lotteryNumber = object.getString("lottery_result");
        String[] numArr = lotteryNumber.split(",");
		SrcFiveDataBean  srcDataBean= new SrcFiveDataBean();
		srcDataBean.setIssueId(object.getString("lottery_no"));
		srcDataBean.setNo1(Integer.parseInt(numArr[0]));
		srcDataBean.setNo2(Integer.parseInt(numArr[1]));
		srcDataBean.setNo3(Integer.parseInt(numArr[2]));
		srcDataBean.setNo4(Integer.parseInt(numArr[3]));
		srcDataBean.setNo5(Integer.parseInt(numArr[4]));
		
		return srcDataBean;
	}
	
	/**
	 * 获取接口中数据
	* @Title: getHttpResponse 
	* @Description: TODO(这里用一句话描述这个方法的作用) 
	* @param @param allConfigUrl
	* @param @return    设定文件 
	* @author banna
	* @date 2017年7月17日 下午12:37:10 
	* @return String    返回类型 
	* @throws
	 */
	public static String getHttpResponse(String allConfigUrl) {
        BufferedReader in = null;
        StringBuffer result = null;
        try {
             
            URI uri = new URI(allConfigUrl);
            URL url = uri.toURL();
            URLConnection connection = url.openConnection();
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setRequestProperty("Charset", "utf-8");
         
            connection.connect();
             
            result = new StringBuffer();
            //读取URL响应
            in = new BufferedReader(new InputStreamReader(
                    connection.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                result.append(line);
            }
             
            return result.toString();
             
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
         
        return null;
         
    }
	
	
}
