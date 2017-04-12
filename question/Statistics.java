package question;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import javax.naming.NamingException;

import me.chanjar.weixin.common.exception.WxErrorException;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.justep.baas.action.ActionContext;
import com.justep.baas.data.DataUtils;
import com.justep.baas.data.Table;
import com.justep.baas.data.Transform;

public class Statistics {
	public static JSONObject statistics(JSONObject params, ActionContext context) throws JsonGenerationException, JsonMappingException, IOException, WxErrorException {
		Connection conn = null;
		Table table = null;

		try {
			conn = context.getConnection("question");
			String area = params.getString("area")==null?"":params.getString("area");
			String street = params.getString("street")==null?"":params.getString("street");
			String start = params.getString("startDate");
			String end = params.getString("endDate");
			String cond = "";
			if(start!=null && !"".equals(start) &&  !"undefined".equals(start)&&end!=null && !"".equals(end) &&  !"undefined".equals(end) ){
				cond = " and fCreateDate BETWEEN '"+start+"' and  '"+end+"' ";
			}
			String sql = "select count(*) as area , sum(sumc) as street  from (select  farea ,sum(cnt) as sumc  from (  " +
					"(select fArea ,count(*) as cnt from tabquestiona where tabIsDelete = 0 and  fArea like '%"+area+"%' and fStreet like '%"+street+"%' "+ cond+" group  by fArea  )union(" +
					" select fArea,count(*) as cnt  from tabquestionb where  tabIsDelete = 0 and  fArea like '%"+area+"%' and fStreet like '%"+street+"%' "+ cond+"  group  by fArea  )union(" +
					" select fArea,count(*) as cnt  from tabquestionc where tabIsDelete = 0 and   fArea like '%"+area+"%' and fStreet like '%"+street+"%' "+ cond+"  group  by fArea )union(" +
					" select fArea,count(*) as cnt  from tabquestiond where tabIsDelete = 0 and   fArea like '%"+area+"%' and fStreet like '%"+street+"%' "+ cond+"  group  by fArea  ) union(" +
					" select fArea,count(*) as cnt  from tabquestione where tabIsDelete = 0 and   fArea like '%"+area+"%' and fStreet like '%"+street+"%' "+ cond+"  group  by fArea )" +
					" )a group by farea)aa ";
			System.out.println(sql);
			table = DataUtils.queryData(conn, sql, null, null, null, null);
			JSONObject tableJson = Transform.tableToJson(table);                        
	        JSONArray rows = (JSONArray) tableJson.get("rows");
	        JSONObject rowJson = (JSONObject) rows.get(0);                          
            JSONObject areaJson = (JSONObject) rowJson.get("area");                 
            String farea = areaJson.getString("value");
            JSONObject streetJson = (JSONObject) rowJson.get("street");                 
            String fstreet = streetJson.getString("value");
			JSONObject ret = new JSONObject();
			ret.put("farea", farea);
			ret.put("fstreet", fstreet);
			sql = "select CONCAT(acnt,bcnt,ccnt,dcnt,ecnt) as problem  from ((select  case when count(*)<>0 then '街面秩序;' else '' end  as acnt from tabquestiona  where tabIsDelete = 0 and  fArea like '%"+area+"%' and fStreet like '%"+street+"%' "+ cond+" )a join" +
					"  (select  case when count(*)<>0 then '力量防控;' else '' end  as bcnt from tabquestionb  where tabIsDelete = 0 and  fArea like '%"+area+"%' and fStreet like '%"+street+"%' "+ cond+"  )b join " +
					"(select  case when count(*)<>0 then '安检措施落实;' else '' end  as ccnt from tabquestionc  where tabIsDelete = 0 and  fArea like '%"+area+"%' and fStreet like '%"+street+"%' "+ cond+"  )c join " +
					"(select  case when count(*)<>0 then '刀具管控;' else '' end  as dcnt from tabquestiond  where tabIsDelete = 0 and  fArea like '%"+area+"%' and fStreet like '%"+street+"%' "+ cond+"  )d  join " +
					"(select  case when count(*)<>0 then '重点医院;' else '' end  as ecnt from tabquestione  where tabIsDelete = 0 and  fArea like '%"+area+"%' and fStreet like '%"+street+"%' "+ cond+"  )e " +
					" )";
			System.out.println(sql);
			table = DataUtils.queryData(conn, sql, null, null, null, null);
			JSONObject tableJson2 = Transform.tableToJson(table);                        
	        JSONArray rows2 = (JSONArray) tableJson2.get("rows");
	        JSONObject rowJson2 = (JSONObject) rows2.get(0);                          
            JSONObject areaJson2 = (JSONObject) rowJson2.get("problem");                 
            String problem = areaJson2.getString("value");
			ret.put("problem", problem);
			sql = "select GROUP_CONCAT(farea) as farea ,GROUP_CONCAT(fstreet) as fstreet , GROUP_CONCAT( pro) as cnt from (select farea,fstreet,sum(problem) as pro from ((select farea,fstreet ,sum(LENGTH(fproblem )-LENGTH(REPLACE(fproblem ,' ', ''))+1) as problem  from tabquestiona  where tabIsDelete = 0 and  fArea like '%"+area+"%' and fStreet like '%"+street+"%' "+ cond+"  group by farea,fstreet) union " +
					" (select farea,fstreet ,sum(problem )as pro from (select fArea,fstreet, case when fPoliceDuty like '%2%' then 1 else 0 end + case when fVolunteerDuty like '%2%' then 1 else 0 end  as problem  from tabquestionb  where tabIsDelete = 0 and  fArea like '%"+area+"%' and fStreet like '%"+street+"%' "+ cond+"  )a GROUP BY farea,fstreet   )" +
					" union (select farea,fstreet,sum(problem)as pro from ( select fArea,fstreet, case when  fpag  ='否' then 1 else 0 end + case when  fperson  ='否' then 1 else 0 end + case when  fLiquid ='否' then 1 else 0 end as problem  from tabquestionc  where tabIsDelete = 0 and  fArea like '%"+area+"%' and fStreet like '%"+street+"%' "+ cond+"  ) abc group by farea,fstreet) union" +
					" (select farea,fstreet,sum(problem)as pro from ( select fArea,fstreet, case when  fRealName  ='否' then 1 else 0 end   as problem  from tabquestiond  where tabIsDelete = 0 and  fArea like '%"+area+"%' and fStreet like '%"+street+"%' "+ cond+"  ) abc group by farea,fstreet) union " +
					" (select farea,fstreet ,sum(LENGTH(fproblem )-LENGTH(REPLACE(fproblem ,' ', ''))+1) as problem  from tabquestione  where tabIsDelete = 0 and  fArea like '%"+area+"%' and fStreet like '%"+street+"%' "+ cond+"  group by farea,fstreet)" +
					" ) abc group by farea,fstreet  ORDER BY farea) aaa";
			System.out.println(sql);
			table = DataUtils.queryData(conn, sql, null, null, null, null);
			JSONObject tableJson3 = Transform.tableToJson(table);                        
	        JSONArray rows3 = (JSONArray) tableJson3.get("rows");
	        JSONObject rowJson3 = (JSONObject) rows3.get(0);                          
            JSONObject areaJson33 = (JSONObject) rowJson3.get("fstreet");                 
            String streett = areaJson33.getString("value");
            JSONObject areaJson333 = (JSONObject) rowJson3.get("cnt"); 
            String cnt = areaJson333.getString("value");
            ret.put("streetname", streett);
            ret.put("streetvalue", cnt);
            sql = "select GROUP_CONCAT(farea) as farea ,  GROUP_CONCAT( pro) as cnt  from (select farea,sum(problem) as pro from ((select farea ,sum(LENGTH(fproblem )-LENGTH(REPLACE(fproblem ,' ', ''))+1) as problem  from tabquestiona   where tabIsDelete = 0 and  fArea like '%"+area+"%' and fStreet like '%"+street+"%' "+ cond+" group by farea) union" +
            		" (select farea ,sum(problem )as pro from (select fArea, case when fPoliceDuty like '%2%' then 1 else 0 end + case when fVolunteerDuty like '%2%' then 1 else 0 end  as problem  from tabquestionb  where tabIsDelete = 0 and  fArea like '%"+area+"%' and fStreet like '%"+street+"%' "+ cond+" )a GROUP BY farea   )" +
            		" union (select farea,sum(problem)as pro from ( select fArea, case when  fpag  ='否' then 1 else 0 end + case when  fperson  ='否' then 1 else 0 end + case when  fLiquid ='否' then 1 else 0 end as problem  from tabquestionc  where tabIsDelete = 0 and  fArea like '%"+area+"%' and fStreet like '%"+street+"%' "+ cond+" ) abc group by farea) union" +
            		" (select farea,sum(problem)as pro from ( select fArea, case when  fRealName  ='否' then 1 else 0 end   as problem  from tabquestiond  where tabIsDelete = 0 and  fArea like '%"+area+"%' and fStreet like '%"+street+"%' "+ cond+" ) abc group by farea) union " +
            		" (select farea ,sum(LENGTH(fproblem )-LENGTH(REPLACE(fproblem ,' ', ''))+1) as problem  from tabquestione   where tabIsDelete = 0 and  fArea like '%"+area+"%' and fStreet like '%"+street+"%' "+ cond+" group by farea)" +
            		" ) abc group by farea  ORDER BY farea)aaa";
            System.out.println(sql);
			table = DataUtils.queryData(conn, sql, null, null, null, null);
			JSONObject tableJson4 = Transform.tableToJson(table);                        
	        JSONArray rows4 = (JSONArray) tableJson4.get("rows");
	        JSONObject rowJson4 = (JSONObject) rows4.get(0);                          
            JSONObject areaJson4 = (JSONObject) rowJson4.get("farea");                 
            String areat2 = areaJson4.getString("value");
            JSONObject areaJson44 = (JSONObject) rowJson4.get("cnt");                 
            String cnt2 = areaJson44.getString("value");
            ret.put("areaname", areat2);
            ret.put("areavalue", cnt2);
			return ret;
		} catch (SQLException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		} catch (NamingException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		} finally {
			DBConnection.close(conn, null, null);
		}
		return null;
	}
	
	public static JSONObject statisticsReport(JSONObject params, ActionContext context) throws JsonGenerationException, JsonMappingException, IOException, WxErrorException {
		Connection conn = null;
		Table table = null;

		try {
			conn = context.getConnection("question");
			String area = "";
			String street = "";
			String cond = "";
			String sql = "select count(*) as area , sum(sumc) as street  from (select  farea ,sum(cnt) as sumc  from (  " +
					"(select fArea ,count(*) as cnt from tabquestiona where tabIsDelete = 0 and  fArea like '%"+area+"%' and fStreet like '%"+street+"%' "+ cond+" group  by fArea  )union(" +
					" select fArea,count(*) as cnt  from tabquestionb where  tabIsDelete = 0 and  fArea like '%"+area+"%' and fStreet like '%"+street+"%' "+ cond+"  group  by fArea  )union(" +
					" select fArea,count(*) as cnt  from tabquestionc where tabIsDelete = 0 and   fArea like '%"+area+"%' and fStreet like '%"+street+"%' "+ cond+"  group  by fArea )union(" +
					" select fArea,count(*) as cnt  from tabquestiond where tabIsDelete = 0 and   fArea like '%"+area+"%' and fStreet like '%"+street+"%' "+ cond+"  group  by fArea  ) union(" +
					" select fArea,count(*) as cnt  from tabquestione where tabIsDelete = 0 and   fArea like '%"+area+"%' and fStreet like '%"+street+"%' "+ cond+"  group  by fArea )" +
					" )a group by farea)aa ";
			System.out.println(sql);
			String first ="截至目前，我公司暗访人员共对{0}个区的{1}个点位进行了暗访，共发现{2}个区存在{3}个问题，" +
					"其中{4}<br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;根据统计，发现问题最多的是{5}<br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;综上，建议相关地区对以上存在的问题提高重视程度、认真加以解决，切实弥补加强在{6}等方面存在的漏洞和薄弱环节，共同维护全市安全稳定工作大局。";
			table = DataUtils.queryData(conn, sql, null, null, null, null);
			JSONObject tableJson = Transform.tableToJson(table);                        
	        JSONArray rows = (JSONArray) tableJson.get("rows");
	        JSONObject rowJson = (JSONObject) rows.get(0);                          
            JSONObject areaJson = (JSONObject) rowJson.get("area");                 
            String farea = areaJson.getString("value");
            JSONObject streetJson = (JSONObject) rowJson.get("street");                 
            String fstreet = streetJson.getString("value");
            first = first.replace("{0}", farea).replace("{1}", fstreet);
            sql = " select count(farea) as area ,sum(problem) as pro from (select count(farea) as farea ,sum(problem) as problem  from ((select farea ,sum(LENGTH(fproblem )-LENGTH(REPLACE(fproblem ,' ', ''))+1) as problem  from tabquestiona   where tabIsDelete = 0 and  fArea like '%"+area+"%' and fStreet like '%"+street+"%' "+ cond+" group by farea) union" +
            		" (select farea ,sum(problem )as pro from (select fArea, case when fPoliceDuty like '%2%' then 1 else 0 end + case when fVolunteerDuty like '%2%' then 1 else 0 end  as problem  from tabquestionb  where tabIsDelete = 0 and  fArea like '%"+area+"%' and fStreet like '%"+street+"%' "+ cond+" )a GROUP BY farea   )" +
            		" union (select farea,sum(problem)as pro from ( select fArea, case when  fpag  ='否' then 1 else 0 end + case when  fperson  ='否' then 1 else 0 end + case when  fLiquid ='否' then 1 else 0 end as problem  from tabquestionc  where tabIsDelete = 0 and  fArea like '%"+area+"%' and fStreet like '%"+street+"%' "+ cond+" ) abc group by farea) union" +
            		" (select farea,sum(problem)as pro from ( select fArea, case when  fRealName  ='否' then 1 else 0 end   as problem  from tabquestiond  where tabIsDelete = 0 and  fArea like '%"+area+"%' and fStreet like '%"+street+"%' "+ cond+" ) abc group by farea) union " +
            		" (select farea ,sum(LENGTH(fproblem )-LENGTH(REPLACE(fproblem ,' ', ''))+1) as problem  from tabquestione   where tabIsDelete = 0 and  fArea like '%"+area+"%' and fStreet like '%"+street+"%' "+ cond+" group by farea)" +
            		" ) abc group by farea  )aa ";
            System.out.println(sql);
            table = DataUtils.queryData(conn, sql, null, null, null, null);
			JSONObject tableJson2 = Transform.tableToJson(table);                        
	        JSONArray rows2 = (JSONArray) tableJson2.get("rows");
	        JSONObject rowJson2 = (JSONObject) rows2.get(0);                          
            JSONObject areaJson2 = (JSONObject) rowJson2.get("area");                 
            String farea2 = areaJson2.getString("value");
            JSONObject streetJson2 = (JSONObject) rowJson2.get("pro");                 
            String fstreet2 = streetJson2.getString("value");
            first = first.replace("{2}", farea2).replace("{3}", fstreet2);
            sql ="select * from ((select  '街面秩序' as type ,sum(LENGTH(fproblem )-LENGTH(REPLACE(fproblem ,' ', ''))+1) as problem  from tabquestiona )union" +
            		" (select '力量防控' as type , sum( case when fPoliceDuty like '%2%' then 1 else 0 end + case when fVolunteerDuty like '%2%' then 1 else 0 end)  as problem  from tabquestionb  ) union " +
            		"(select '安检措施落实' as type ,sum(case when  fpag  ='否' then 1 else 0 end + case when  fperson  ='否' then 1 else 0 end + case when  fLiquid ='否' then 1 else 0 end) as problem  from tabquestionc ) UNION " +
            		"(select '刀具管控' as type ,sum(case when  fRealName  ='否' then 1 else 0 end)   as problem  from tabquestiond  ) union " +
            		"(select '重点医院' as type , sum(LENGTH(fproblem )-LENGTH(REPLACE(fproblem ,' ', ''))+1) as problem  from tabquestione  ))aa order by problem desc";
    		System.out.println(sql);
    		table = DataUtils.queryData(conn, sql, null, null, null, null);
    		JSONObject tableJson3 = Transform.tableToJson(table);                        
	        JSONArray rows3 = (JSONArray) tableJson3.get("rows");
	        String  types = "";
	        for(int i=0 ;i<rows3.size();i++){
	        	JSONObject rowJson3 = (JSONObject) rows3.get(i);
	        	JSONObject areaJson3 = (JSONObject) rowJson3.get("type");                 
	            String farea3 = areaJson3.getString("value");
	            JSONObject streetJson3 = (JSONObject) rowJson3.get("problem");                 
	            String fstreet3 = streetJson3.getString("value");
	            types += farea3+fstreet3+"个问题，";
	        }
	        String important = "";
	        for(int i =0;i<2;i++){
	        	JSONObject rowJsonaa = (JSONObject) rows3.get(i);
	            JSONObject areaJsonaa = (JSONObject) rowJsonaa.get("type");                 
	            String fareaaa = areaJsonaa.getString("value");
	            JSONObject streetJsonaa = (JSONObject) rowJsonaa.get("problem");                 
	            String fstreetaa = streetJsonaa.getString("value");
	            important += fareaaa+"、";
	        }
            first = first.replace("{4}", types.substring(0,types.length()-1)+"。");
            sql = "select farea,sum(problem) as pro from ((select farea ,sum(LENGTH(fproblem )-LENGTH(REPLACE(fproblem ,' ', ''))+1) as problem  from tabquestiona   where tabIsDelete = 0 and  fArea like '%"+area+"%' and fStreet like '%"+street+"%' "+ cond+" group by farea) union" +
            		" (select farea ,sum(problem )as pro from (select fArea, case when fPoliceDuty like '%2%' then 1 else 0 end + case when fVolunteerDuty like '%2%' then 1 else 0 end  as problem  from tabquestionb  where tabIsDelete = 0 and  fArea like '%"+area+"%' and fStreet like '%"+street+"%' "+ cond+" )a GROUP BY farea   )" +
            		" union (select farea,sum(problem)as pro from ( select fArea, case when  fpag  ='否' then 1 else 0 end + case when  fperson  ='否' then 1 else 0 end + case when  fLiquid ='否' then 1 else 0 end as problem  from tabquestionc  where tabIsDelete = 0 and  fArea like '%"+area+"%' and fStreet like '%"+street+"%' "+ cond+" ) abc group by farea) union" +
            		" (select farea,sum(problem)as pro from ( select fArea, case when  fRealName  ='否' then 1 else 0 end   as problem  from tabquestiond  where tabIsDelete = 0 and  fArea like '%"+area+"%' and fStreet like '%"+street+"%' "+ cond+" ) abc group by farea) union " +
            		" (select farea ,sum(LENGTH(fproblem )-LENGTH(REPLACE(fproblem ,' ', ''))+1) as problem  from tabquestione   where tabIsDelete = 0 and  fArea like '%"+area+"%' and fStreet like '%"+street+"%' "+ cond+" group by farea)" +
            		" ) abc group by farea  ORDER BY pro desc  ";
            System.out.println(sql);
			table = DataUtils.queryData(conn, sql, null, null, null, null);
			JSONObject tableJson4 = Transform.tableToJson(table);                        
	        JSONArray rows4 = (JSONArray) tableJson4.get("rows");
	        types = "";
	        for(int i =0;i<2;i++){
	        	JSONObject rowJson4 = (JSONObject) rows4.get(i);
	            JSONObject areaJson4 = (JSONObject) rowJson4.get("farea");                 
	            String farea4 = areaJson4.getString("value");
	            JSONObject areaJson44 = (JSONObject) rowJson4.get("pro");                 
	            String fstreet4 = areaJson44.getString("value");
	            types += farea4+fstreet4+"个问题，";
	        }
	        first = first.replace("{5}", types.substring(0,types.length()-1)+"。");
	        first = first.replace("{6}", important.substring(0,important.length()-1));
            JSONObject ret = new JSONObject();
			ret.put("first", first.replaceAll("null", "0"));
			return ret;
		} catch (SQLException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		} catch (NamingException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		} finally {
			DBConnection.close(conn, null, null);
		}
		return null;
	}
	
	
	public static JSONObject getCount(JSONObject params, ActionContext context) throws JsonGenerationException, JsonMappingException, IOException, WxErrorException {
		Connection conn = null;
		Table table = null;
        JSONObject ret = new JSONObject();

		try {
			conn = context.getConnection("question");
			String filter = params.getString("filter")==null?"":params.getString("filter");
			String tableName = params.getString("tableName")==null?"":params.getString("tableName");
			if(filter!=null && !"".equals(filter)){
				filter = " ("+filter+")";
			}
			String sql = " select count(*) as cnt from "+tableName +"  "+tableName +" where "+filter ;
			System.out.println(sql);
			table = DataUtils.queryData(conn, sql, null, null, null, null);
			JSONObject tableJson = Transform.tableToJson(table);                        
	        JSONArray rows = (JSONArray) tableJson.get("rows");
	        JSONObject rowJson = (JSONObject) rows.get(0);                          
            JSONObject areaJson = (JSONObject) rowJson.get("cnt");                 
            String cnt = areaJson.getString("value");
            
			ret.put("totalCount", cnt);
			return ret;
		} catch (SQLException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		} catch (NamingException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		} finally {
			DBConnection.close(conn, null, null);
		}
		return null;
	}
}