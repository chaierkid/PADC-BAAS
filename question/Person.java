package question;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;

import javax.naming.NamingException;

import me.chanjar.weixin.common.exception.WxErrorException;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;

import question.DBConnection;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.justep.baas.action.ActionContext;
import com.justep.baas.data.DataUtils;
import com.justep.baas.data.Table;
import com.justep.baas.data.Transform;

public class Person {
	public static JSONObject checkPerson(JSONObject params, ActionContext context) throws JsonGenerationException, JsonMappingException, IOException, WxErrorException{
		String userName = params.getString("userName");		
		String password = params.getString("password");	
		Table table = null;
		Connection con=null;
		try {
			con = context.getConnection("question");
			String sql = "select fUserName,fUserType,fPassword from tabuser where fUuserName='"+userName+"' and fPpassword='"+password+"' and tabIsDelete=0 ";
			table = DataUtils.queryData(con, sql, null, null, null, null);
			JSONObject tableJson = Transform.tableToJson(table);                        
	        JSONArray rows = (JSONArray) tableJson.get("rows");
	        JSONObject ret = new JSONObject();
	        System.out.println(sql);
	        System.out.println(rows.size());
	        if (rows.size()<1){
	        	ret.put("loginStatus", "-1"); // 登录失败
	            ret.put("errorTxt", "登录失败,请检查用户名或密码！");    
                //System.out.println("ret==="+ret);
                return ret;
            }else{
            	JSONObject rowJson = (JSONObject) rows.get(0);                          
                JSONObject fUuserNameJson = (JSONObject) rowJson.get("fUuserName");                 
                String refUuserName = fUuserNameJson.getString("value");
                JSONObject sfUserTypeJson = (JSONObject) rowJson.get("fUserType");                 
                String refUserType = sfUserTypeJson.getString("value");
                JSONObject fPasswordJson = (JSONObject) rowJson.get("fPassword");                 
                String refPassword = fPasswordJson.getString("value");
                ret.put("loginStatus", "0"); //登录成功
                ret.put("refUuserName",  refUuserName); 
                ret.put("refUserType",  refUserType); 
                ret.put("refPassword",  refPassword); 
                System.out.println(refUuserName +";;;"+refUserType);
                return ret;
            }       
		} catch (SQLException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		} catch (NamingException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		}finally{
			DBConnection.close(con , null, null);
		}
		return Transform.tableToJson(table);
	}
}
