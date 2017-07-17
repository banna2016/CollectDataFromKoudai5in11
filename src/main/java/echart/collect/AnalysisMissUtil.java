package main.java.echart.collect;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AnalysisMissUtil
{
  public static List<String> updateGroupMiss(SrcFiveDataBean SrcFiveDataBean, int n)
  {
    List<String> sqlList = new ArrayList<String>();
    int[] noArr = getIntArr(SrcFiveDataBean);
    String inStr = getGroupByNumber(noArr, n);
    sqlList.add("UPDATE "+App.descMissTbName+" SET CURRENT_MISS = 0 WHERE TYPE = " + n + "  AND GROUP_NUMBER IN (" + 
      inStr + ")");
    if (n == 3)
    {
      List<String> likeGroupList = getLikeGroupByNumber(noArr, n);
      for (String likeGroup : likeGroupList) {
        sqlList.add("UPDATE "+App.descMissTbName+" SET OPTIONAL_COMPOUND = 0 WHERE TYPE = 4 AND GROUP_NUMBER LIKE (" + likeGroup + ")");
      }
    }
    else if (n == 4)
    {
      List<String> likeGroupList = getLikeGroupByNumber(noArr, n);
      for (String likeGroup : likeGroupList) {
        sqlList.add("UPDATE "+App.descMissTbName+" SET OPTIONAL_COMPOUND = 0 WHERE TYPE IN (5,6) AND GROUP_NUMBER LIKE (" + likeGroup + ")");
      }
    }
    return sqlList;
  }
  
  public static String updateGreatFiveGroupMiss(SrcFiveDataBean SrcFiveDataBean, int n)
  {
    String rtnSql = null;
    int[] noArr = getIntArr(SrcFiveDataBean);
    rtnSql = "UPDATE "+App.descMissTbName+" SET CURRENT_MISS = 0 WHERE TYPE = " + n + "  AND GROUP_NUMBER LIKE  '%" + 
      translate(noArr[0]) + "%" + translate(noArr[1]) + "%" + translate(noArr[2]) + "%" + 
      translate(noArr[3]) + "%" + translate(noArr[4]) + "%'";
    return rtnSql;
  }
  
  public static String[] updateBeforeRen2GroupMiss(SrcFiveDataBean SrcFiveDataBean)
  {
    int[] noArr = { SrcFiveDataBean.getNo1(), SrcFiveDataBean.getNo2() };
    Arrays.sort(noArr);
    String[] rtnSql = new String[2];
    rtnSql[0] = ("UPDATE "+App.descMissTbName+" SET CURRENT_MISS = 0 WHERE TYPE = 9  AND GROUP_NUMBER = '" + translate(noArr[0]) + translate(noArr[1]) + "'");
    rtnSql[1] = ("UPDATE "+App.descMissTbName+" SET TWOCODE_COMPOUND = 0 WHERE TYPE IN (3,4,5,6,7)  AND GROUP_NUMBER LIKE '%" + translate(noArr[0]) + "%" + translate(noArr[1]) + "%'");
    return rtnSql;
  }
  
  public static String[] updateBeforeRen3GroupMiss(SrcFiveDataBean SrcFiveDataBean)
  {
    String[] rtnSql = new String[2];
    int[] noArr = getThreeIntArr(SrcFiveDataBean);
    rtnSql[0] = 
      ("UPDATE "+App.descMissTbName+" SET CURRENT_MISS = 0 WHERE TYPE = 10  AND GROUP_NUMBER = '" + translate(noArr[0]) + translate(noArr[1]) + translate(noArr[2]) + "'");
    rtnSql[1] = ("UPDATE "+App.descMissTbName+" SET THREECODE_COMPOUND = 0 WHERE TYPE IN (4,5,6,7,8)  AND GROUP_NUMBER LIKE '%" + translate(noArr[0]) + "%" + translate(noArr[1]) + "%" + translate(noArr[2]) + "%'");
    return rtnSql;
  }
  
  public static String updateDirectBeforeRen2GroupMiss(SrcFiveDataBean SrcFiveDataBean)
  {
    String rtnSql = null;
    rtnSql = "UPDATE "+App.descMissTbName+" SET CURRENT_MISS = 0 WHERE TYPE = 11  AND GROUP_NUMBER = '" + 
      translate(SrcFiveDataBean.getNo1()) + translate(SrcFiveDataBean.getNo2()) + "'";
    return rtnSql;
  }
  
  public static String updateDirectBeforeRen3GroupMiss(SrcFiveDataBean SrcFiveDataBean)
  {
    String rtnSql = null;
    rtnSql = "UPDATE "+App.descMissTbName+" SET CURRENT_MISS = 0 WHERE TYPE = 12  AND GROUP_NUMBER = '" + 
      translate(SrcFiveDataBean.getNo1()) + translate(SrcFiveDataBean.getNo2()) + translate(SrcFiveDataBean.getNo3()) + 
      "'";
    return rtnSql;
  }
  
  public static int[] getIntArr(SrcFiveDataBean SrcFiveDataBean)
  {
    int[] noArr = { SrcFiveDataBean.getNo1(), SrcFiveDataBean.getNo2(), SrcFiveDataBean.getNo3(), SrcFiveDataBean.getNo4(), 
      SrcFiveDataBean.getNo5() };
    Arrays.sort(noArr);
    return noArr;
  }
  
  public static int[] getThreeIntArr(SrcFiveDataBean SrcFiveDataBean)
  {
    int[] noArr = { SrcFiveDataBean.getNo1(), SrcFiveDataBean.getNo2(), SrcFiveDataBean.getNo3() };
    Arrays.sort(noArr);
    return noArr;
  }
  
  public static String ArrToStr(int[] arr)
  {
    StringBuffer rtnStr = new StringBuffer();
    if ((arr != null) && (arr.length > 0))
    {
      for (int i = 0; i < arr.length; i++) {
        rtnStr.append(arr[i]);
      }
      return rtnStr.toString();
    }
    return null;
  }
  
  public static String getGroupByNumber(int[] arr, int n)
  {
    StringBuffer rtnStr = new StringBuffer();
    if (arr.length > 0) {
      for (int i = 0; i < arr.length; i++) {
        for (int j = i + 1; j < arr.length; j++) {
          if (n == 2)
          {
            if (j != 1) {
              rtnStr.append(",");
            }
            rtnStr.append("'" + translate(arr[i]) + translate(arr[j]) + "'");
          }
          else
          {
            for (int z = j + 1; z < arr.length; z++) {
              if (n == 3)
              {
                if (z != 2) {
                  rtnStr.append(",");
                }
                rtnStr.append("'" + translate(arr[i]) + translate(arr[j]) + translate(arr[z]) + "'");
              }
              else
              {
                for (int o = z + 1; o < arr.length; o++) {
                  if (n == 4)
                  {
                    if (o != 3) {
                      rtnStr.append(",");
                    }
                    rtnStr.append("'" + translate(arr[i]) + translate(arr[j]) + translate(arr[z]) + 
                      translate(arr[o]) + "'");
                  }
                  else
                  {
                    for (int p = o + 1; p < arr.length; p++) {
                      if (n == 5)
                      {
                        if (p != 4) {
                          rtnStr.append(",");
                        }
                        rtnStr.append(
                          "'" + translate(arr[i]) + translate(arr[j]) + translate(arr[z]) + 
                          translate(arr[o]) + translate(arr[p]) + "'");
                      }
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
    return rtnStr.toString();
  }
  
  public static String getDirectGroupByNumber(int[] arr, int n)
  {
    StringBuffer rtnStr = new StringBuffer();
    rtnStr.append("'" + translate(arr[0]) + "'").append("'" + translate(arr[1]) + "'");
    if (n == 3) {
      rtnStr.append(translate(arr[2]));
    }
    return rtnStr.toString();
  }
  
  private static String translate(int temp)
  {
    String rtn = null;
    if (temp < 10) {
      rtn = temp+"";
    } else if (temp == 10) {
      rtn = "A";
    } else if (temp == 11) {
      rtn = "J";
    } else if (temp == 12) {
      rtn = "Q";
    }
    return rtn;
  }
  
  public static List<String> getLikeGroupByNumber(int[] arr, int n)
  {
    List<String> rtnList = new ArrayList<String>();
    if (arr.length > 0) {
      for (int i = 0; i < arr.length; i++) {
        for (int j = i + 1; j < arr.length; j++) {
          if (n == 2) {
            rtnList.add("'%" + translate(arr[i]) + "%" + translate(arr[j]) + "%'");
          } else {
            for (int z = j + 1; z < arr.length; z++) {
              if (n == 3) {
                rtnList.add("'%" + translate(arr[i]) + "%" + translate(arr[j]) + "%" + translate(arr[z]) + "%'");
              } else {
                for (int o = z + 1; o < arr.length; o++) {
                  rtnList.add("'%" + translate(arr[i]) + "%" + translate(arr[j]) + "%" + translate(arr[z]) + "%" + translate(arr[o]) + "%'");
                }
              }
            }
          }
        }
      }
    }
    return rtnList;
  }
}
