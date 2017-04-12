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

public class Everyday {
	public static JSONObject getData(JSONObject params, ActionContext context) throws JsonGenerationException, JsonMappingException, IOException, WxErrorException{
		Table table = null;
		Connection conn=null;
		try {
			conn = context.getConnection("question");
			String sql = "  select  DATE_FORMAT(fPhotoTime,'%Y-%m-%d') as createDate from tabquestiona " +
					"where  tabIsDelete = 0 GROUP BY DATE_FORMAT(fPhotoTime,'%Y-%m-%d')    union " +
					" select  DATE_FORMAT(fPhotoTime,'%Y-%m-%d') as createDate   from tabquestionb " +
					"where  tabIsDelete = 0 GROUP BY DATE_FORMAT(fPhotoTime,'%Y-%m-%d')    union " +
					"select  DATE_FORMAT(fPhotoTime,'%Y-%m-%d') as createDate   from tabquestionc " +
					"where  tabIsDelete = 0 GROUP BY DATE_FORMAT(fPhotoTime,'%Y-%m-%d') union " +
					"select  DATE_FORMAT(fPhotoTime,'%Y-%m-%d') as createDate   from tabquestiond " +
					"where  tabIsDelete = 0 GROUP BY DATE_FORMAT(fPhotoTime,'%Y-%m-%d')  union  " +
					"select  DATE_FORMAT(fPhotoTime,'%Y-%m-%d') as createDate   from tabquestione " +
					"where  tabIsDelete = 0 GROUP BY DATE_FORMAT(fPhotoTime,'%Y-%m-%d')    ORDER BY createDate desc  ";
			
			table = DataUtils.queryData(conn, sql, null, null, null, null);
		} catch (SQLException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		} catch (NamingException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		}finally{
			DBConnection.close(conn , null, null);
		}
		return Transform.tableToJson(table);
	}
}
