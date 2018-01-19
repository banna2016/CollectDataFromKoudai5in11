package main.java.echart.collect;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;




public class App {
	
	 static String maxIssueId = null;//最大开奖期号
	  static String lineCount = null;
	  static String descNumberTbName = null;
	  static String lotteryNum = "";//开奖号码
	  static String maxMissIssueId = null;
	  static String descMissTbName = null;
	  
	 private static List<IPAddress> iplist = new ArrayList<IPAddress>();//ip代理池list
	
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
		initIPList();//初始化代理池
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
		
//		timer.schedule(new TimerTask() {
//			@Override
//			public void run() {
//				try
//				{
//					initIPList();//初始化代理池
//				}
//				catch(Exception e)
//				{
//					e.printStackTrace();
//				}
//				
//			}
//		}, new Date(), 20000L);// 每隔10s输出
	}

	/**
	 * 初始化代理池
	 */
	public static void initIPList()
	{
		try {
			Document doc = Jsoup.connect(
			        "http://www.xicidaili.com/nt/")//普通代理：http://www.xicidaili.com/nt/ 国内http代理：http://www.xicidaili.com/wt/
			         .maxBodySize(0)  
			         .userAgent("Mozilla")
			         .timeout(20000) 
			        .get();
			Elements trs = doc.getElementById("ip_list").select("tr");
			for (int i=1,len=trs.size();i<len;i++) //游标从1开始，因为第一行是页面的标题行
			{
				IPAddress ip = new IPAddress();
				Elements tds = trs.get(i).select("td");
				if(null!=tds)
				{
					//获取ip
					if(null!=tds.get(1))
					{
						ip.setIp(tds.get(1).html());
					}
					//获取端口
					if(null!=tds.get(2)&&!"".equals(tds.get(2).html()))
					{
						ip.setPort(Integer.parseInt(tds.get(2).html()));
					}
					
					//获取速度(用来筛选ip)
					if(null!=tds.get(6))
					{
						Elements time = tds.get(6).select(".bar");
						String timeStr = time.get(0).attr("title");
						ip.setSpeed(timeStr.substring(0, timeStr.length()-1));
					}
					
					//获取连接时间(用来筛选ip)
					if(null!=tds.get(7))
					{
						Elements time = tds.get(7).select(".bar");
						String timeStr = time.get(0).attr("title");
						ip.setTime(Double.parseDouble(timeStr.substring(0, timeStr.length()-1)));
					}
					
					iplist.add(ip);
				}
				else
				{
					continue;
				}
			}
			        
		} catch (IOException e) {
			e.printStackTrace();
		}
		//移除时间长的数据列表
		Filter(iplist);
		//校验当前抓取的ip代理池是否可用
//		IPIsable(iplist);
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
		String name = "HUBSYY";//HUBSYY:湖北 ；AHSYY：安徽
		SrcFiveDataBean srcFiveDataBean = parseDocumentFromUrl(name);
		if(null!=srcFiveDataBean&&!maxIssueId.equals(srcFiveDataBean.getIssueId()))
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
		String json= App.getHttpResponseByIplist(url, 0);
		SrcFiveDataBean  srcDataBean= null;
		if(json.length()>0)
		{
		    JSONObject jsonformat = JSONObject.fromObject(json);
	        JSONArray data = jsonformat.getJSONArray("data");
	        JSONObject object = data.getJSONObject(0);
	        String lotteryNumber = object.getString("lottery_result");
	        String[] numArr = lotteryNumber.split(",");
			srcDataBean= new SrcFiveDataBean();
			srcDataBean.setIssueId(object.getString("lottery_no"));
			srcDataBean.setNo1(Integer.parseInt(numArr[0]));
			srcDataBean.setNo2(Integer.parseInt(numArr[1]));
			srcDataBean.setNo3(Integer.parseInt(numArr[2]));
			srcDataBean.setNo4(Integer.parseInt(numArr[3]));
			srcDataBean.setNo5(Integer.parseInt(numArr[4]));
		}
       
		
		return srcDataBean;
	}
	
	public static String getHttpResponseByIplist(String allConfigUrl,int i) 
	{
		String result = null;
        if(iplist.size()>0)
        {
        	IPAddress ip = iplist.get(i);
        	i++;
        	Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(ip.getIp(), ip.getPort()));
        	result = getHttpResponse(allConfigUrl, proxy);
        	if(result.length()==0)
        	{
        		if(i==iplist.size()-1)
        		{
        			initIPList();
        			getHttpResponseByIplist(allConfigUrl,0);
        		}
        		else
        		{
        			getHttpResponseByIplist(allConfigUrl,i);
        		}
        	}
        }
        
        return result;
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
	public static String getHttpResponse(String allConfigUrl,Proxy proxy) {
        BufferedReader in = null;
        StringBuffer result = new StringBuffer();
       
        	try {
        		URI uri = new URI(allConfigUrl);
                URL url = uri.toURL();
                URLConnection connection = url.openConnection(proxy);
                connection.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;" +
                        "q=0.9,image/webp,*/*;q=0.8");
                connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                connection.setRequestProperty("Charset", "utf-8");
                connection.setRequestProperty("Accept-Language", "zh-CN,zh;q=0.8");
                connection.setRequestProperty("User-Agent","Mozilla/4.0 (compatible; MSIE 7.0; NT 5.1; GTB5; .NET CLR 2.0.50727; CIBA)");
                connection.connect();
                 
                //读取URL响应
                in = new BufferedReader(new InputStreamReader(
                        connection.getInputStream()));
                String line;
                while ((line = in.readLine()) != null) {
                    result.append(line);
                }
   		 } 
   		 catch (IOException e) 
   		 {
   	         e.printStackTrace();
   	     }
   		 catch (URISyntaxException e) {
   			e.printStackTrace();
   		}
   		 finally 
   		 {
   	            try 
   	            {
   	                if (in != null) 
   	                {
   	                    in.close();
   	                }
   	            } 
   	            catch (Exception e2) {
   	                e2.printStackTrace();
   	            }
   	      }
              
		
        return result.toString();
         
    }
	
	//校验有效且速度快的地址
	public static void Filter(List<IPAddress> list) {
        List<IPAddress> newlist = new ArrayList<>();
        
        Collections.sort(list, new Comparator<IPAddress>() {
            @Override
            public int compare(IPAddress o1, IPAddress o2) {
                return o1.getSpeed().compareTo(o2.getSpeed());
            }
        });

        //只返回容器中前100的对象
        for(int i = 0; i < list.size(); i++) {
            if(i < 10) {
                newlist.add(list.get(i));
            }else {
                break;
            }
        }
        iplist.clear();
        iplist=newlist;
    }
	
	public static List<IPAddress> IPIsable(List<IPAddress> ipMessages) {
        Proxy proxy = null;
        Document doc = null;
        for(int i = 0; i < ipMessages.size(); i++) 
        {
            try 
            {
            	 System.getProperties().setProperty("http.proxyHost", ipMessages.get(i).getIp());
                 System.getProperties().setProperty("http.proxyPort", ipMessages.get(i).getPort()+"");
//            	 proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(ipMessages.get(i).getIp(), ipMessages.get(i).getPort()));
            	 Connection con = Jsoup.connect("https://www.baidu.com/");
                 con.header("User-Agent", 
                   "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:45.0) Gecko/20100101 Firefox/45.0");
                 con.timeout(10000);
                 doc = con.get();
            } 
            catch (IOException e) 
            {
            	e.printStackTrace();
                ipMessages.remove(ipMessages.get(i));
                i--;
            }
            catch (Exception e) 
            {
            	e.printStackTrace();
                ipMessages.remove(ipMessages.get(i));
                i--;
            }
        }


        return ipMessages;
    }
}
