package com.orilore.ui;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class DBTool {
	/*
	 * 实现单词首字母大写功能
	 */
	public static String toUpper(String name){
		if(name!=null){
			char c = name.toUpperCase().charAt(0);
			String n = c+name.substring(1);
			return n;
		}else{
			return "";
		}
	}
	
	/*
	 * 根据JDBC数据类型ID获取数据类型名称
	 */
	public static String getTypeName(int t){
		String type = "";
		switch(t){
		case 1: type = "String"; break;
		case 4: type = "Integer"; break;
		case 7: type = "Float"; break;
		case 8: type = "Double"; break;
		case 91: type = "String"; break;
		case 93: type = "String"; break;
		case 12: type = "String"; break;
		case -1: type = "String"; break;
		default: type = "Object"; break;
		}
		return type;
	}
	
	/*
	 * 生成MyBatis Configuration.xml 配置文件
	*/
	public static String createConfiguration(String dir,Map<String,String> conn,Object[] tables,String epackage){
		File file = new File(dir+"\\Configuration.xml");
		if(file.exists()){
			return "Configuration.xml 已存在,未做修改！\n";
		}
		String message = "";
		StringBuffer buffer = new StringBuffer();
		buffer.append("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>");
		buffer.append("\n<!DOCTYPE configuration PUBLIC \"-//mybatis.org//DTD Config 3.0//EN\" \"http://mybatis.org/dtd/mybatis-3-config.dtd\">");
		buffer.append("\n<configuration>");
		buffer.append("\n\t<typeAliases>");
		for(Object obj : tables){
			buffer.append("\n\t\t<typeAlias alias=\""+toUpper(obj.toString())+"\" type=\""+epackage+"."+toUpper(obj.toString())+"\"/>"); 
		}
		buffer.append("\n\t</typeAliases>");
		buffer.append("\n\t<environments default=\"development\">");
		buffer.append("\n\t\t<environment id=\"development\">");
		buffer.append("\n\t\t<transactionManager type=\"JDBC\"/>");
		buffer.append("\n\t\t<dataSource type=\"POOLED\">");
		buffer.append("\n\t\t\t<property name=\"driver\" value=\""+conn.get("driver")+"\"/>");
		buffer.append("\n\t\t\t<property name=\"url\" value=\""+conn.get("url")+"\" />");
		buffer.append("\n\t\t\t<property name=\"username\" value=\""+conn.get("uid")+"\"/>");
		buffer.append("\n\t\t\t<property name=\"password\" value=\""+conn.get("pwd")+"\"/>");
		buffer.append("\n\t\t</dataSource>");
		buffer.append("\n\t\t</environment>");
		buffer.append("\n\t</environments>");
		buffer.append("\n\t<mappers>");
		for(Object obj : tables){
			buffer.append("\n\t\t<mapper resource=\""+epackage.replace(".","/")+"/"+toUpper(obj.toString())+".xml\"/>"); 
		}
		buffer.append("\n\t</mappers>");
		buffer.append("\n</configuration>");
		try {			
			File parent = file.getParentFile(); 
			if(parent!=null && !parent.exists()){ 
				parent.mkdirs(); 
			} 
			file.createNewFile(); 
			FileWriter fos = new FileWriter(file);
			BufferedWriter bf = new BufferedWriter(fos);
			bf.write(buffer.toString());
			bf.flush();
			fos.close();
			bf.close();
			message = "Configuration.xml 配置文件已生成！\n";
		} catch (Exception e) {
			message = e.getMessage();
		}
		return message;
	}
	
	/*
	 * 生成MyBatis Mapper映射文件
	 */
	public static String createMapper(String dir,Connection conn,Object[] tables,String epackage,String pb,String pc){
		String message = "";
		try{
			for(Object o : tables){
				StringBuffer buffer = new StringBuffer();
				buffer.append("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>");
				buffer.append("\n<!DOCTYPE mapper PUBLIC \"-//mybatis.org//DTD Mapper 3.0//EN\""); 
				buffer.append("\n\"http://mybatis.org/dtd/mybatis-3-mapper.dtd\">");
				buffer.append("\n<mapper namespace=\""+pc+"."+toUpper(o.toString())+"Mapper"+"\">");
				buffer.append("\n\t<resultMap type=\""+toUpper(o.toString())+"\" id=\""+o.toString()+"ResultMap\">");
				buffer.append("\n\t\t<id column=\"id\" property=\"id\"/>");
				Statement ps = conn.createStatement();
				String sql = "select * from "+o.toString()+" where 1>2";
				ResultSet rs = ps.executeQuery(sql);
				ResultSetMetaData rsmd = rs.getMetaData();
				int n = rsmd.getColumnCount();
				String inserta = "insert into "+o.toString()+"(";
				String updatea = "update "+o.toString()+" set ";
				String updateb = "";
				String insertb = "";
				String insertc = "";
				for(int i=1;i<=n;i++){
					if(rsmd.getColumnName(i).equals("id")){
						continue;
					}
					buffer.append("\n\t\t<result column=\""+rsmd.getColumnName(i)+"\" property=\""+rsmd.getColumnName(i)+"\"/>");
					insertb+=rsmd.getColumnName(i)+",";
					insertc+="#{"+rsmd.getColumnName(i)+"},";
					updateb+=rsmd.getColumnName(i)+"=#{"+rsmd.getColumnName(i)+"},";
				}
				String updatec = updatea+updateb.substring(0,updateb.length()-1)+" where id=#{id}";
				insertb = insertb.substring(0,insertb.length()-1)+") values(";
				insertc = insertc.substring(0,insertc.length()-1)+")";
				rs.close();
				ps.close();
				
				buffer.append("\n\t</resultMap>");
				
				buffer.append("\n\t<select id=\"selectOne\" parameterType=\"int\" resultType=\""+toUpper(o.toString())+"\">");
				buffer.append("\n\t\tselect * from "+o.toString()+" where id=#{id}");
				buffer.append("\n\t</select>");
				
				buffer.append("\n\t<select id=\"select\" resultMap=\""+o.toString()+"ResultMap\">");
				buffer.append("\n\t\tselect * from "+o.toString());
				buffer.append("\n\t</select>");
				
				buffer.append("\n\t<insert id=\"insert\" parameterType=\""+toUpper(o.toString())+"\">"); 
		        buffer.append("\n\t\t"+inserta+insertb+insertc);
		        buffer.append("\n\t</insert>");
		        buffer.append("\n\t<delete id=\"delete\" parameterType=\"int\">");
		        buffer.append("\n\t\tdelete from "+o.toString()+" where id=#{id}");
		        buffer.append("\n\t</delete>");
		        buffer.append("\n\t<update id=\"update\" parameterType=\""+toUpper(o.toString())+"\">");
		    	buffer.append("\n\t\t"+updatec);
		    	buffer.append("\n\t</update>");
				buffer.append("\n</mapper>");
				
				String path = dir+"\\"+pb.replace(".","\\");
				File file = new File(path+"\\"+toUpper(o.toString())+".xml");			
				File parent = file.getParentFile(); 
				if(parent!=null && !parent.exists()){ 
					parent.mkdirs(); 
				} 
				file.createNewFile(); 
				FileWriter fos = new FileWriter(file);
				BufferedWriter bf = new BufferedWriter(fos);
				bf.write(buffer.toString());
				bf.flush();
				fos.close();
				bf.close();
				message += "Mapper文件"+toUpper(o.toString())+".xml已生成！\n";
			}
		} catch (Exception e) {
			message = e.getMessage();
		}
		return message;
	}
	
	/*
	 * 生成MyBatis Mapper接口
	 */
	public static String createInterface(String dir,Object[] tables,String epackage,String ipackage){
		String message = "";
		try{
			
			for(Object o : tables){
				StringBuffer buffer = new StringBuffer();
				buffer.append("package "+ipackage+";");
				buffer.append("\nimport java.util.List;");
				buffer.append("\nimport "+epackage+".*;");
				buffer.append("\npublic interface "+toUpper(o.toString())+"Mapper{");
				buffer.append("\n\tpublic void insert("+toUpper(o.toString())+" bean);");
				buffer.append("\n\tpublic "+toUpper(o.toString())+" selectOne(int id);");
				buffer.append("\n\tpublic void delete(int id);");
				buffer.append("\n\tpublic List<"+toUpper(o.toString())+"> select();");
				buffer.append("\n\tpublic void update("+toUpper(o.toString())+" bean);");
				buffer.append("\n}");
				
				String path = dir+"\\"+ipackage.replace(".","\\");
				File file = new File(path+"\\"+toUpper(o.toString())+"Mapper.java");			
				File parent = file.getParentFile(); 
				if(parent!=null && !parent.exists()){ 
					parent.mkdirs(); 
				} 
				file.createNewFile(); 
				FileWriter fos = new FileWriter(file);
				BufferedWriter bf = new BufferedWriter(fos);
				bf.write(buffer.toString());
				bf.flush();
				fos.close();
				bf.close();
				message += "Mapper 接口"+toUpper(o.toString())+"Mapper.java已生成！\n";
			}
		} catch (Exception e) {
			message = e.getMessage();
		}
		return message;
	}
	
	/*
	 * 自动生成基于MyBatis的业务接口及实现类
	 */
	public static String createMyBatisBizs(String dir,Object[] tables,String packageName,String epname,String dpname){
		String message = "";
		for(Object table : tables){
			String tn = (String)table;
			StringBuffer buffer = new StringBuffer();
			buffer.append("package "+packageName+";\n");	
			buffer.append("import "+epname+".*;\n");
			buffer.append("import java.util.List;\n");
			
			buffer.append("public interface I"+toUpper(tn)+"Biz{\n");
			buffer.append("\tpublic boolean add"+toUpper(tn)+"("+toUpper(tn)+" "+tn+");\n");
			buffer.append("\tpublic boolean remove"+toUpper(tn)+"(int id);\n");
			buffer.append("\tpublic boolean modify"+toUpper(tn)+"("+toUpper(tn)+" "+tn+");\n");
			buffer.append("\tpublic "+toUpper(tn)+" get"+toUpper(tn)+"(int id);\n");
			buffer.append("\tpublic List<"+toUpper(tn)+"> find"+toUpper(tn)+"();\n");
			buffer.append("}");
			
			StringBuffer buffer2 = new StringBuffer();
			buffer2.append("package "+packageName+";\n");	
			buffer2.append("import "+epname+".*;\n");
			buffer2.append("import "+dpname+".*;\n");
			buffer2.append("import java.util.*;\n");
			buffer2.append("import org.apache.ibatis.session.SqlSession;\n");
			buffer2.append("public class "+toUpper(tn)+"Biz implements I"+toUpper(tn)+"Biz{\n");
			buffer2.append("\tprivate SqlSession session = null;\n");
			buffer2.append("\t@Override\n");
			buffer2.append("\tpublic boolean add"+toUpper(tn)+"("+toUpper(tn)+" bean) {\n");
			buffer2.append("\t\ttry{\n");
			buffer2.append("\t\t\t"+toUpper(tn)+"Mapper mapper = session.getMapper("+toUpper(tn)+"Mapper.class);\n");
			buffer2.append("\t\t\tmapper.insert(bean);\n");
			buffer2.append("\t\t\tsession.commit();\n");
			buffer2.append("\t\t\treturn true;\n");
			buffer2.append("\t\t}catch(Exception ex){\n");
			buffer2.append("\t\t\tex.printStackTrace();\n");
			buffer2.append("\t\t\treturn false;\n");
			buffer2.append("\t\t}finally{\n");
			buffer2.append("\t\t\tsession.close();\n");
			buffer2.append("\t\t}\n");
			buffer2.append("\t}\n");
			
			
			buffer2.append("\t@Override\n");
			buffer2.append("\tpublic boolean remove"+toUpper(tn)+"(int id) {\n");
			buffer2.append("\t\ttry{\n");
			buffer2.append("\t\t\t"+toUpper(tn)+"Mapper mapper = session.getMapper("+toUpper(tn)+"Mapper.class);\n");
			buffer2.append("\t\t\tmapper.delete(id);\n");
			buffer2.append("\t\t\tsession.commit();\n");
			buffer2.append("\t\t\treturn true;\n");
			buffer2.append("\t\t}catch(Exception ex){\n");
			buffer2.append("\t\t\tex.printStackTrace();\n");
			buffer2.append("\t\t\treturn false;\n");
			buffer2.append("\t\t}finally{\n");
			buffer2.append("\t\t\tsession.close();\n");
			buffer2.append("\t\t}\n");
			buffer2.append("\t}\n");
			
			buffer2.append("\t@Override\n");
			buffer2.append("\tpublic boolean modify"+toUpper(tn)+"("+toUpper(tn)+" bean) {\n");
			buffer2.append("\t\ttry{\n");
			buffer2.append("\t\t\t"+toUpper(tn)+"Mapper mapper = session.getMapper("+toUpper(tn)+"Mapper.class);\n");
			buffer2.append("\t\t\tmapper.update(bean);\n");
			buffer2.append("\t\t\tsession.commit();\n");
			buffer2.append("\t\t\treturn true;\n");
			buffer2.append("\t\t}catch(Exception ex){\n");
			buffer2.append("\t\t\tex.printStackTrace();\n");
			buffer2.append("\t\t\treturn false;\n");
			buffer2.append("\t\t}finally{\n");
			buffer2.append("\t\t\tsession.close();\n");
			buffer2.append("\t\t}\n");
			buffer2.append("\t}\n");
			
			buffer2.append("\t@Override\n");
			buffer2.append("\tpublic "+toUpper(tn)+" get"+toUpper(tn)+"(int id) {\n");
			buffer2.append("\t\t"+toUpper(tn)+" bean = null;\n");
			buffer2.append("\t\ttry{\n");
			buffer2.append("\t\t\t"+toUpper(tn)+"Mapper mapper = session.getMapper("+toUpper(tn)+"Mapper.class);\n");
			buffer2.append("\t\t\tbean = mapper.selectOne(id);\n");
			buffer2.append("\t\t}catch(Exception ex){\n");
			buffer2.append("\t\t\tex.printStackTrace();\n");
			buffer2.append("\t\t}finally{\n");
			buffer2.append("\t\t\tsession.close();\n");
			buffer2.append("\t\t}\n");
			buffer2.append("\t\treturn bean;\n");
			buffer2.append("\t}\n");
			
			buffer2.append("\t@Override\n");
			buffer2.append("\tpublic List<"+toUpper(tn)+"> find"+toUpper(tn)+"() {\n");
			buffer2.append("\t\tList<"+toUpper(tn)+"> beans = null;\n");
			buffer2.append("\t\ttry{\n");
			buffer2.append("\t\t\t"+toUpper(tn)+"Mapper mapper = session.getMapper("+toUpper(tn)+"Mapper.class);\n");
			buffer2.append("\t\t\tbeans = mapper.select();\n");
			buffer2.append("\t\t}catch(Exception ex){\n");
			buffer2.append("\t\t\tex.printStackTrace();\n");
			buffer2.append("\t\t}finally{\n");
			buffer2.append("\t\t\tsession.close();\n");
			buffer2.append("\t\t}\n");
			buffer2.append("\t\treturn beans;\n");
			buffer2.append("\t}\n");
			
			buffer2.append("}\n\r");
			
			try {
				String path = dir+"\\"+packageName.replace(".","\\");
				File file = new File(path+"\\I"+toUpper(tn)+"Biz.java");
				File parent = file.getParentFile(); 
				if(parent!=null && !parent.exists()){ 
					parent.mkdirs(); 
				} 
				file.createNewFile(); 
				FileWriter fos = new FileWriter(file);
				BufferedWriter bf = new BufferedWriter(fos);
				bf.write(buffer.toString());
				bf.flush();
				fos.close();
				bf.close();
				message+="BIZ接口I"+toUpper(tn)+"Biz.java 创建成功！\n";
			} catch (Exception e) {
				message = e.getMessage();
			}
			
			try {
				String path = dir+"\\"+packageName.replace(".","\\");
				File file = new File(path+"\\"+toUpper(tn)+"Biz.java");
				File parent = file.getParentFile(); 
				if(parent!=null && !parent.exists()){ 
					parent.mkdirs(); 
				} 
				file.createNewFile(); 
				FileWriter fos = new FileWriter(file);
				BufferedWriter bf = new BufferedWriter(fos);
				bf.write(buffer2.toString());
				bf.flush();
				fos.close();
				bf.close();
				message+=toUpper(tn)+"Biz.java 创建成功！\n";
			} catch (Exception e) {
				message = e.getMessage();
			}
			
		}
		return message;
	}	
	
	/*
	 * 生成实体类
	 */
	public static String createEntitys(String dir,Connection conn,Object[] tables,String packageName){
		String message = "";
		for(Object table : tables){
			String tn = (String)table;
			String sql = "select * from "+tn;
			StringBuffer buffer = new StringBuffer();
			buffer.append("package "+packageName+";\n");
			buffer.append("public class "+toUpper(tn)+"{\n");
			try {
				Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery(sql);
				ResultSetMetaData rsmd = rs.getMetaData();
				int columns = rsmd.getColumnCount();
				for(int i=1;i<=columns;i++){
					String cname = rsmd.getColumnName(i);
					int t = rsmd.getColumnType(i);	
					String ctype = getTypeName(t);
					buffer.append("\tprivate "+ctype+" "+cname+";\n");
					buffer.append("\tpublic void set"+toUpper(cname)+"("+ctype+" "+cname+"){\n");
					buffer.append("\t\tthis."+cname+"="+cname+";\n");
					buffer.append("\t}\n\r");
				
					buffer.append("\tpublic "+ctype+" get"+toUpper(cname)+"(){\n");
					buffer.append("\t\treturn this."+cname+";\n");
					buffer.append("\t}\n");
				}
			} catch (Exception e) {
				message = e.getMessage();
			}
			buffer.append("}");
			try {
				String path = dir+"\\"+packageName.replace(".","\\");
				File file = new File(path+"\\"+toUpper(tn)+".java");
				
				File parent = file.getParentFile(); 
				if(parent!=null && !parent.exists()){ 
					parent.mkdirs(); 
				} 
				file.createNewFile(); 
				FileWriter fos = new FileWriter(file);
				BufferedWriter bf = new BufferedWriter(fos);
				bf.write(buffer.toString());
				bf.flush();
				fos.close();
				bf.close();
				message+="实体类"+toUpper(tn)+".java 创建成功！\n";
				
			} catch (Exception e) {
				message = e.getMessage();
			}
		}
		return message;
	}
	
	
	/*
	 * 生成数据库配置文件
	 */
	public static String createDBConfig(String path,String driver,String url,String uid,String pwd){
		String message = "";
		StringBuffer buffer = new StringBuffer();
		buffer.append("driver="+driver+"\n");
		buffer.append("url="+url+"\n");
		buffer.append("username="+uid+"\n");
		buffer.append("password="+pwd+"\n");
		try {
			File file = new File(path+"\\Config.properties");
			File parent = file.getParentFile(); 
			if(parent!=null && !parent.exists()){ 
				parent.mkdirs(); 
			} 
			file.createNewFile(); 
			FileWriter fos = new FileWriter(file);
			BufferedWriter bf = new BufferedWriter(fos);
			bf.write(buffer.toString());
			bf.flush();
			fos.close();
			bf.close();
			message+="数据库连接配置文件 Config.properties 创建成功！\n";		
		} catch (Exception e) {
			message = e.getMessage();
		}
		return message;
	}
	
	/*
	 * 生成DBUtil类
	 */
	public static String createDBUtil(String dir,Connection conn,String packageName,String driver,String uid,String pwd){
		String message = "";
		StringBuffer buffer = new StringBuffer();
		buffer.append("package "+packageName+";\n");
		buffer.append("import java.sql.*;\n");
		buffer.append("import java.util.*;\n");
		buffer.append("import java.io.*;\n");
		buffer.append("public class DBUtil{\n");
		buffer.append("\tpublic Connection getConnection(){\n");
		buffer.append("\t\ttry{\n");
		DatabaseMetaData md;
		String url = "";
		try {
			md = conn.getMetaData();
			url = md.getURL();
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		buffer.append("\t\t\tInputStream is = this.getClass().getResourceAsStream(\"Config.properties\");\n");
		buffer.append("\t\t\tProperties prop = new Properties();\n");
		buffer.append("\t\t\tprop.load(is);\n");
		buffer.append("\t\t\tClass.forName(prop.getProperty(\"driver\"));\n");
		buffer.append("\t\t\tConnection conn = DriverManager.getConnection(prop.getProperty(\"url\"),prop.getProperty(\"username\"),prop.getProperty(\"password\"));\n");
		buffer.append("\t\t\tis.close();\n");
		buffer.append("\t\t\treturn conn;\n");
		buffer.append("\t\t}catch(Exception ex){\n");
		buffer.append("\t\t\tex.printStackTrace();\n");
		buffer.append("\t\t}\n");
		buffer.append("\t\treturn null;\n");
		buffer.append("\t}\n");
		buffer.append("}");
		try {
			String path = dir+"\\"+packageName.replace(".","\\");
			createDBConfig(path, driver, url, uid, pwd);
			File file = new File(path+"\\DBUtil.java");
			File parent = file.getParentFile(); 
			if(parent!=null && !parent.exists()){ 
				parent.mkdirs(); 
			} 
			file.createNewFile(); 
			FileWriter fos = new FileWriter(file);
			BufferedWriter bf = new BufferedWriter(fos);
			bf.write(buffer.toString());
			bf.flush();
			fos.close();
			bf.close();
			message+="数据库连接工具类 DBUtil.java 创建成功！\n";		
		} catch (Exception e) {
			message = e.getMessage();
		}
		return message;
	}
	
	/*
	 * 生成DAO
	 */
	public static String createDaos(String dir,Connection conn,Object[] tables,String packageName,String epname){
		String message = "";
		for(Object table : tables){
			String tn = (String)table;
			String isql="insert into "+tn+"(";
			String usql="update "+tn+" set ";
			Map<String,String> cnames = new LinkedHashMap<String,String>(); 
			try{
				String sql = "select * from "+tn;
				Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery(sql);
				ResultSetMetaData rsmd = rs.getMetaData();
				int columns = rsmd.getColumnCount();
				String cs="";
				String cs2="(";
				String us="";
				for(int j=1;j<=columns;j++){
					if(rsmd.getColumnName(j).equalsIgnoreCase("id")){
						continue;
					}
					String cn = rsmd.getColumnName(j);
					String ct = getTypeName(rsmd.getColumnType(j));
					cnames.put(cn, ct);
					cs+=cn+",";
					cs2+="?,";
					us+=cn+"=?,";
				}
				cs = cs.substring(0, cs.length()-1);
				cs2 = cs2.substring(0, cs2.length()-1)+")";
				us = us.substring(0, us.length()-1);
				isql+=cs+") values"+cs2;
				usql+=us+" where id=?";
			}catch(Exception ex){
				message = ex.getMessage();
			}
			StringBuffer buffer = new StringBuffer();
			buffer.append("package "+packageName+";\n");	
			buffer.append("import "+epname+"."+toUpper(tn)+";\n");
			buffer.append("import java.util.*;\n");
			buffer.append("import java.sql.*;\n");
			buffer.append("public interface I"+toUpper(tn)+"DAO{\n");
			buffer.append("\tpublic boolean insert("+toUpper(tn)+" "+tn+",Connection conn) throws Exception;\n");
			buffer.append("\tpublic boolean delete(int id,Connection conn) throws Exception;\n");
			buffer.append("\tpublic boolean update("+toUpper(tn)+" "+tn+",Connection conn) throws Exception;\n");
			buffer.append("\tpublic "+toUpper(tn)+" selectOne(int id,Connection conn) throws Exception;\n");
			buffer.append("\tpublic List<"+toUpper(tn)+"> select(Connection conn) throws Exception;\n");
			buffer.append("\tpublic void close() throws Exception;\n");
			buffer.append("}");
			
			StringBuffer buffer2 = new StringBuffer();
			buffer2.append("package "+packageName+";\n");	
			buffer2.append("import "+epname+"."+toUpper(tn)+";\n");
			buffer2.append("import java.sql.*;\n");
			buffer2.append("import java.util.*;\n");
			
			buffer2.append("public class "+toUpper(tn)+"DAO implements I"+toUpper(tn)+"DAO{\n");
			buffer2.append("\tprivate PreparedStatement pstmt;\n");
			buffer2.append("\tprivate ResultSet rs;\n");
			
			buffer2.append("\tpublic boolean insert("+toUpper(tn)+" "+tn+",Connection conn) throws Exception{\n");
			buffer2.append("\t\tString sql=\""+isql+"\";\n");
			buffer2.append("\t\tpstmt = conn.prepareStatement(sql);\n");
			
			int p = 1;
			Set<String> keys = cnames.keySet();
			Iterator<String> it = keys.iterator();
			while(it.hasNext()){
				String k = it.next();
				String v = cnames.get(k);
				if(v.equals("Integer")) v = "Int";
				buffer2.append("\t\tpstmt.set"+v+"("+p+","+tn+".get"+toUpper(k)+"());\r");
				p++;
			}
			buffer2.append("\t\tif(pstmt.executeUpdate()>0){\n");
			buffer2.append("\t\t\tthis.close();\n");
			buffer2.append("\t\t\treturn true;\n");
			buffer2.append("\t\t}else{\n");
			buffer2.append("\t\t\tthis.close();\n");
			buffer2.append("\t\t\treturn false;\n");	
			buffer2.append("\t\t}\n");	
			buffer2.append("\t}\n");
			
			
			buffer2.append("\tpublic boolean delete(int id,Connection conn) throws Exception{\n");
			buffer2.append("\t\tString sql = \"delete from "+tn+" where id=?\";\n");
			buffer2.append("\t\tpstmt = conn.prepareStatement(sql);\n");
			buffer2.append("\t\tpstmt.setInt(1,id);\r\n");
			buffer2.append("\t\tif(pstmt.executeUpdate()>0){\n");
			buffer2.append("\t\t\tthis.close();\n");
			buffer2.append("\t\t\treturn true;\n");
			buffer2.append("\t\t}else{\n");
			buffer2.append("\t\t\tthis.close();\n");
			buffer2.append("\t\t\treturn false;\n");
			buffer2.append("\t\t}\n");
			buffer2.append("\t}\n");
			
			buffer2.append("\tpublic boolean update("+toUpper(tn)+" "+tn+",Connection conn) throws Exception{;\n");
			buffer2.append("\t\tString sql=\""+usql+"\";\n");
			buffer2.append("\t\tpstmt = conn.prepareStatement(sql);\n");
			p=1;
			Iterator<String> it2 = keys.iterator();
			while(it2.hasNext()){
				String k = it2.next();
				String v = cnames.get(k);
				if(v.equals("Integer")) v = "Int";
				buffer2.append("\t\tpstmt.set"+v+"("+p+","+tn+".get"+toUpper(k)+"());\r");
				p++;
			}
			buffer2.append("\t\tpstmt.setInt("+p+","+tn+".getId());\r");
			buffer2.append("\t\tif(pstmt.executeUpdate()>0){\n");
			buffer2.append("\t\t\tthis.close();\n");
			buffer2.append("\t\t\treturn true;\n");
			buffer2.append("\t\t}else{\n\r");
			buffer2.append("\t\t\tthis.close();\n");	
			buffer2.append("\t\t\treturn false;\n");	
			buffer2.append("\t\t}\n");	
			buffer2.append("\t}\n");
			
			buffer2.append("\tpublic "+toUpper(tn)+" selectOne(int id,Connection conn) throws Exception{\n");
			buffer2.append("\t\tString sql = \"select * from "+tn+" where id=?\";\n");
			buffer2.append("\t\tpstmt = conn.prepareStatement(sql);\n");
			buffer2.append("\t\tpstmt.setInt(1,id);\r");
			buffer2.append("\t\trs = pstmt.executeQuery();\n");
			buffer2.append("\t\t"+toUpper(tn)+" "+tn+" = new "+toUpper(tn)+"();\n");
			buffer2.append("\t\tif(rs.next()){\n");
			Iterator<String> it3 = keys.iterator();
			while(it3.hasNext()){
				String k = it3.next();
				String v = cnames.get(k);
				if(v.equals("Integer")) v = "Int";
				buffer2.append("\t\t\t"+tn+".set"+toUpper(k)+"(rs.get"+v+"("+"\""+k+"\"));\n");
			}
			buffer2.append("\t\t\t"+tn+".setId(rs.getInt(\"id\"));\n");
			buffer2.append("\t\t}\n");
			buffer2.append("\t\tthis.close();\n");
			buffer2.append("\t\treturn "+tn+";\n");	
			buffer2.append("\t}\n");
			
			buffer2.append("\tpublic List<"+toUpper(tn)+"> select(Connection conn) throws Exception{\n");
			buffer2.append("\t\tString sql = \"select * from "+tn+"\";\n");
			buffer2.append("\t\tpstmt = conn.prepareStatement(sql);\n");
			buffer2.append("\t\trs=pstmt.executeQuery();\n");
			buffer2.append("\t\tList<"+toUpper(tn)+"> "+tn+"s = new ArrayList<"+toUpper(tn)+">();\n");
			buffer2.append("\t\twhile(rs.next()){\n");
			buffer2.append("\t\t\t"+toUpper(tn)+" "+tn+" = new "+toUpper(tn)+"();\n");	
			Iterator<String> it4 = keys.iterator();
			while(it4.hasNext()){
				String k = it4.next();
				String v = cnames.get(k);
				if(v.equals("Integer")) v = "Int";
				buffer2.append("\t\t\t"+tn+".set"+toUpper(k)+"(rs.get"+v+"("+"\""+k+"\"));\n");
			}
			buffer2.append("\t\t\t"+tn+".setId(rs.getInt(\"id\"));\n");
			buffer2.append("\t\t\t"+tn+"s.add("+tn+");\n");
			buffer2.append("\t\t}\n");
			buffer2.append("\t\tthis.close();\n");
			buffer2.append("\t\treturn "+tn+"s;\n");	
			buffer2.append("\t}\n");
			
			buffer2.append("\tpublic void close() throws Exception{\n");
			buffer2.append("\t\tif(rs!=null) rs.close();\n");
			buffer2.append("\t\tif(pstmt!=null) pstmt.close();\n");
			buffer2.append("\t}\n");
			
			buffer2.append("}");
			
			try {
				String path = dir+"\\"+packageName.replace(".","\\");
				File file = new File(path+"\\I"+toUpper(tn)+"DAO.java");
				File parent = file.getParentFile(); 
				if(parent!=null && !parent.exists()){ 
					parent.mkdirs(); 
				} 
				file.createNewFile(); 
				FileWriter fos = new FileWriter(file);
				BufferedWriter bf = new BufferedWriter(fos);
				bf.write(buffer.toString());
				bf.flush();
				fos.close();
				bf.close();
				message+="I"+toUpper(tn)+"DAO.java 创建成功！\n";
			} catch (Exception e) {
				message = e.getMessage();
			}
			
			try {
				String path = dir+"\\"+packageName.replace(".","\\");
				File file = new File(path+"\\"+toUpper(tn)+"DAO.java");
				File parent = file.getParentFile(); 
				if(parent!=null && !parent.exists()){ 
					parent.mkdirs(); 
				} 
				file.createNewFile(); 
				FileWriter fos = new FileWriter(file);
				BufferedWriter bf = new BufferedWriter(fos);
				bf.write(buffer2.toString());
				bf.flush();
				fos.close();
				bf.close();
				message+=toUpper(tn)+"DAO.java 创建成功！\n";
			} catch (Exception e) {
				message=e.getMessage();
			}
			
		}
		return message;
	}
	/*
	 * 自动生成业务接口及实现类
	 */
	public static String createBizs(String dir,Connection conn,Object[] tables,String packageName,String epname,String dpname){
		String message = "";
		for(Object table : tables){
			String tn = (String)table;
			Map<String,String> cnames = new HashMap<String,String>(); 
			try{
				String sql = "select * from "+tn;
				Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery(sql);
				ResultSetMetaData rsmd = rs.getMetaData();
				int columns = rsmd.getColumnCount();
				for(int j=1;j<=columns;j++){
					String cn = rsmd.getColumnName(j);
					String ct = getTypeName(rsmd.getColumnType(j));
					cnames.put(ct, cn);
				}
			}catch(Exception ex){
				message = ex.getMessage();
			}
			StringBuffer buffer = new StringBuffer();
			buffer.append("package "+packageName+";\n");	
			buffer.append("import "+dpname+".*;\n");
			buffer.append("import java.util.*;\n");
			
			buffer.append("public interface I"+toUpper(tn)+"Biz{\n");
			buffer.append("\tpublic boolean add"+toUpper(tn)+"("+toUpper(tn)+" "+tn+");\n");
			buffer.append("\tpublic boolean remove"+toUpper(tn)+"(int id);\n");
			buffer.append("\tpublic boolean modify"+toUpper(tn)+"("+toUpper(tn)+" "+tn+");\n");
			buffer.append("\tpublic "+toUpper(tn)+" get"+toUpper(tn)+"(int id);\n");
			buffer.append("\tpublic List<"+toUpper(tn)+"> find"+toUpper(tn)+"();\n");
			buffer.append("\tpublic void close();\n");
			buffer.append("}");
			
			StringBuffer buffer2 = new StringBuffer();
			buffer2.append("package "+packageName+";\n");	
			buffer2.append("import "+epname+".*;\n");
			buffer2.append("import "+dpname+".*;\n");
			buffer2.append("import java.util.*;\n");
			buffer2.append("import java.sql.*;\n");
			buffer2.append("public class "+toUpper(tn)+"Biz implements I"+toUpper(tn)+"Biz{\n");
			buffer2.append("\tprivate I"+toUpper(tn)+"DAO dao = new "+toUpper(tn)+"DAO();\n");
			buffer2.append("\tprivate DBUtil db = new DBUtil();\n");
			buffer2.append("\tprivate Connection conn = null;\n");
			buffer2.append("\tpublic boolean add"+toUpper(tn)+"("+toUpper(tn)+" "+tn+"){\n");
			buffer2.append("\t\ttry{\n");
			buffer2.append("\t\t\tconn = db.getConnection();\n");
			buffer2.append("\t\t\tif(dao.insert("+tn+",conn)){\n");
			buffer2.append("\t\t\t\treturn true;\n");
			buffer2.append("\t\t\t}else{\n");
			buffer2.append("\t\t\t\treturn false;\n");
			buffer2.append("\t\t\t}\n");
			buffer2.append("\t\t}catch(Exception ex){\n");
			buffer2.append("\t\t\tSystem.out.println(ex.getMessage());\n");
			buffer2.append("\t\t\treturn false;\n");
			buffer2.append("\t\t}finally{\n");
			buffer2.append("\t\t\tthis.close();\n");	
			buffer2.append("\t\t}\n");
			buffer2.append("\t}\n");
			
			
			buffer2.append("\tpublic boolean remove"+toUpper(tn)+"(int id){\n");
			buffer2.append("\t\ttry{\n");
			buffer2.append("\t\t\tconn = db.getConnection();\n");
			buffer2.append("\t\t\tif(dao.delete(id,conn)){\n");
			buffer2.append("\t\t\t\treturn true;\n");
			buffer2.append("\t\t\t}else{\n");
			buffer2.append("\t\t\t\treturn false;\n");
			buffer2.append("\t\t\t}\n");
			buffer2.append("\t\t}catch(Exception ex){\n");
			buffer2.append("\t\t\tSystem.out.println(ex.getMessage());\n");
			buffer2.append("\t\t\treturn false;\n");
			buffer2.append("\t\t}finally{\n");
			buffer2.append("\t\t\tthis.close();\n");	
			buffer2.append("\t\t}\n");
			buffer2.append("\t}\n");
			
			buffer2.append("\tpublic boolean modify"+toUpper(tn)+"("+toUpper(tn)+" "+tn+") {;\n");
			buffer2.append("\t\ttry{\n");
			buffer2.append("\t\t\tconn = db.getConnection();\n");
			buffer2.append("\t\t\tif(dao.update("+tn+",conn)){\n");
			buffer2.append("\t\t\t\treturn true;\n");
			buffer2.append("\t\t\t}else{\n");
			buffer2.append("\t\t\t\treturn false;\n");
			buffer2.append("\t\t\t}\n");
			buffer2.append("\t\t}catch(Exception ex){\n");
			buffer2.append("\t\t\tSystem.out.println(ex.getMessage());\n");
			buffer2.append("\t\t\treturn false;\n");
			buffer2.append("\t\t}finally{\n");
			buffer2.append("\t\t\tthis.close();\n");	
			buffer2.append("\t\t}\n");
			buffer2.append("\t}\n");
			
			buffer2.append("\tpublic "+toUpper(tn)+" get"+toUpper(tn)+"(int id){\n");
			buffer2.append("\t\ttry{\n");
			buffer2.append("\t\t\tconn = db.getConnection();\n");
			buffer2.append("\t\t\treturn dao.selectOne(id,conn);\n");
			buffer2.append("\t\t}catch(Exception ex){\n");
			buffer2.append("\t\t\tSystem.out.println(ex.getMessage());\n");
			buffer2.append("\t\t\t\treturn null;\n");
			buffer2.append("\t\t}finally{\n");
			buffer2.append("\t\t\tthis.close();\n");	
			buffer2.append("\t\t}\n");
			buffer2.append("\t}\n");
			
			buffer2.append("\tpublic List<"+toUpper(tn)+"> find"+toUpper(tn)+"(){\n");
			buffer2.append("\t\ttry{\n");
			buffer2.append("\t\t\tconn = db.getConnection();\n");
			buffer2.append("\t\t\treturn dao.select(conn);\n");
			buffer2.append("\t\t}catch(Exception ex){\n");
			buffer2.append("\t\t\tSystem.out.println(ex.getMessage());\n");
			buffer2.append("\t\t\t\treturn null;\n");
			buffer2.append("\t\t}finally{\n");
			buffer2.append("\t\t\tthis.close();\n");	
			buffer2.append("\t\t}\n");
			buffer2.append("\t}\n");
			
			buffer2.append("\tpublic void close(){\n");
			buffer2.append("\t\ttry{\n");
			buffer2.append("\t\t\tif(conn!=null && !conn.isClosed()) conn.close();\n");
			buffer2.append("\t\t}catch(Exception ex){\n");
			buffer2.append("\t\t\tSystem.out.println(ex.getMessage());\n");
			buffer2.append("\t\t}\n");
			buffer2.append("\t}\n");
			
			buffer2.append("}\n");
			
			try {
				String path = dir+"\\"+packageName.replace(".","\\");
				File file = new File(path+"\\I"+toUpper(tn)+"Biz.java");
				File parent = file.getParentFile(); 
				if(parent!=null && !parent.exists()){ 
					parent.mkdirs(); 
				} 
				file.createNewFile(); 
				FileWriter fos = new FileWriter(file);
				BufferedWriter bf = new BufferedWriter(fos);
				bf.write(buffer.toString());
				bf.flush();
				fos.close();
				bf.close();
				message+="BIZ接口I"+toUpper(tn)+"Biz.java 创建成功！\n";
			} catch (Exception e) {
				message = e.getMessage();
			}
			
			try {
				String path = dir+"\\"+packageName.replace(".","\\");
				File file = new File(path+"\\"+toUpper(tn)+"Biz.java");
				File parent = file.getParentFile(); 
				if(parent!=null && !parent.exists()){ 
					parent.mkdirs(); 
				} 
				file.createNewFile(); 
				FileWriter fos = new FileWriter(file);
				BufferedWriter bf = new BufferedWriter(fos);
				bf.write(buffer2.toString());
				bf.flush();
				fos.close();
				bf.close();
				message+=toUpper(tn)+"Biz.java 创建成功！\n";
			} catch (Exception e) {
				message = e.getMessage();
			}
			
		}
		return message;
	}	
}
