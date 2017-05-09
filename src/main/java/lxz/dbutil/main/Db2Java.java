package lxz.dbutil.main;

import lxz.dbutil.util.SqlTypeMap;
import lxz.dbutil.util.Util;
import lxz.dbutil.util.orm.Field;
import lxz.dbutil.util.orm.Table;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.output.FileWriterWithEncoding;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * 读取mysql数据库信息，根据数据表的信息生成mybatis使用的domain、dao、service、mapper等文件。
 * @author lixizhong
 */
public class Db2Java {
	//基础包名
	//private static final String basePackage = "com.jd.jw.store";
	private static final String basePackage = "com.jd.jw.marketing";
	//private static final String basePackage = "com.jd.jw.purchase";

    //数据连接字符串
    //store
    //private static final String url = "jdbc:mysql://192.168.166.17:3306/jw_store?user=root&password=123456&useUnicode=true&characterEncoding=UTF-8&allowMultiQueries=true";
    //purchase
    //private static final String url = "jdbc:mysql://192.168.166.17:3306/jw_purchase?user=root&password=123456&useUnicode=true&characterEncoding=UTF-8&allowMultiQueries=true";
    //marketing
    private static final String url = "jdbc:mysql://192.168.166.17:3306/jw_marketing?user=root&password=123456&useUnicode=true&characterEncoding=UTF-8&allowMultiQueries=true";

    //要生成类文件的表格
    private static final String[] tables =
            new String[]{
                    "jw_jos_user"
            };

    //domain类前缀
    private static final String domainPrefix = "";
    //domain类后缀
    private static final String domainSubfix = "";
    //表前缀，生成相关类时去掉
    private static final String tablePrefix = "";

	//domain类包名
	private static final String domainDirName = "domain";
	//dao包名
	private static final String daoDirName = "dao";
	//service包名
	private static final String serviceDirName = "service";
	//mapper文件包名
	private static final String mapperDirName = "mapper";
	//生成java文件保存的文件夹
	private static final String codeDirName = "code";
	
	//domain类包名及文件夹名
	private static final String domainPackage = basePackage + "." + domainDirName;
	private static String domainDir = codeDirName + File.separatorChar + domainPackage.replace('.', File.separatorChar);

	//mapper包名及文件夹名
	private static final String mapperPackage = basePackage + "." + mapperDirName;
	private static String mapperDir = codeDirName + File.separatorChar + mapperPackage.replace('.', File.separatorChar);

	//dao包名及文件夹名
    private static final String daoPackage = basePackage + "." + daoDirName;
    private static String daoDir = codeDirName + File.separatorChar + daoPackage.replace('.', File.separatorChar);

	//service包名及文件夹名
	private static final String servicePackage = basePackage + "." + serviceDirName;
	private static final String serviceImplPackage = basePackage + "." + serviceDirName + ".impl";
    private static String serviceDir = codeDirName + File.separatorChar + servicePackage.replace('.', File.separatorChar);
    private static String serviceImplDir = serviceDir + File.separatorChar + "impl";

	public static void main(String[] args) throws Exception {
		log("数据库连接字符串：" + url);
		
		Properties props = new Properties();
		
		props.setProperty("remarks", "true");
        props.setProperty("useInformationSchema", "true");
		
        Connection conn = DriverManager.getConnection(url, props);
        
        List<Table> tableList = loadTableList(conn);
        
        initVelocity();
        createDirs();
        
        for (Table table : tableList) {
        	String name = table.getName();
        	
        	if(ArrayUtils.contains(tables, name)){
        		createJavaCode(table);
        	}
		}
        
        log("finished!");
	}
	
	/**
	 * 生成目录文件夹
	 * @throws IOException
	 */
	public static void createDirs() throws IOException{
        log("删除目录: " + codeDirName);
		FileUtils.deleteQuietly(new File(codeDirName));

        log("创建目录: " + domainDir);
		FileUtils.forceMkdir(new File(domainDir));
        log("创建目录: " + daoDir);
		FileUtils.forceMkdir(new File(daoDir));
        log("创建目录: " + mapperDir);
		FileUtils.forceMkdir(new File(mapperDir));
        log("创建目录: " + serviceImplDir);
		FileUtils.forceMkdir(new File(serviceImplDir));
	}
	
	/**
	 * 初始化velocity
	 */
	public static void initVelocity(){
		Properties p = new Properties();
		
		p.put(Velocity.OUTPUT_ENCODING, "utf-8");
		p.put(Velocity.INPUT_ENCODING, "utf-8");
		p.put(Velocity.FILE_RESOURCE_LOADER_PATH, "vm");
		p.put("file.resource.loader.class",
				//"org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
				"lxz.dbutil.util.StructuredGlobbingResourceLoader");
		
		Velocity.init(p);
	}
	
	/**
	 * 生成java文件
	 * @param table
	 * @throws Exception
	 */
	public static void createJavaCode(Table table) throws Exception{

        log("创建表["+table.getName()+"]的文件：");

		VelocityContext context = new VelocityContext();  
		
		context.put("domainPackage", domainPackage);
		context.put("mapperPackage", mapperPackage);
		context.put("daoPackage", daoPackage);
		context.put("servicePackage", servicePackage);
		context.put("serviceImplPackage", serviceImplPackage);

		context.put("table", table);
		
		String domain = domainPrefix + Util.pascalName(StringUtils.removeStart(table.getName(), tablePrefix)) + domainSubfix;
		String domainEntity = StringUtils.lowerCase(StringUtils.substring(domain, 0, 1)) + StringUtils.substring(domain, 1);
		
		context.put("domain", domain);
		context.put("domainEntity", domainEntity);
		context.put("dao", domain + "Dao");
		context.put("daoEntity", domainEntity + "Dao");
		context.put("resultMap", domain + "ResultMap");
		context.put("service", domain + "Service");
		context.put("serviceImpl", domain + "ServiceImpl");
		context.put("serviceEntity", domainEntity + "Service");
        context.put("now", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date()));

		context.put("util", Util.class);
		context.put("strUtil", StringUtils.class);

        log("\t生成domain");
		createCode(table, context, "vm/Domain.vm", domainDir + File.separatorChar + domain + ".java");
        log("\t生成domain, 完成");
        log("\t生成dao");
		createCode(table, context, "vm/Dao.vm", daoDir + File.separatorChar + domain + "Dao.java");
        log("\t生成dao, 完成");
        log("\t生成mapper");
		createCode(table, context, "vm/Mapper.vm.xml", mapperDir + File.separatorChar + domain + "Mapper.xml");
        log("\t生成mapper, 完成");
        log("\t生成service");
		createCode(table, context, "vm/Service.vm", serviceDir + File.separatorChar + domain + "Service.java");
        log("\t生成service, 完成");
        log("\t生成service实现类");
		createCode(table, context, "vm/ServiceImpl.vm", serviceImplDir + File.separatorChar + domain + "ServiceImpl.java");
        log("\t生成service实现类, 完成");
        log("表["+table.getName()+"]的文件完成\r\n");
	}
	
	/**
	 * 根据给定的模板生成代码文件
	 * @param table
	 * @param context
	 * @param tpl
	 * @param path
	 * @throws IOException
	 */
	private static void createCode(Table table, VelocityContext context, String tpl, String path) throws IOException {
		Template template = Velocity.getTemplate(tpl); 
		  
		StringWriter sw = new StringWriter();  
		template.merge( context, sw );
		
		Writer writer = new FileWriterWithEncoding(new File(path), "utf-8");
		
		writer.write(sw.toString());
		
		writer.flush();
		writer.close();
	}
		
	/**
	 * 从数据库加载数据表信息
	 * @param conn
	 * @return
	 * @throws SQLException
	 */
	public static List<Table> loadTableList(Connection conn) throws SQLException {
		
		DatabaseMetaData dbMeta = conn.getMetaData();
        
		log("数据库：" + dbMeta.getDatabaseProductName() + " " + dbMeta.getDatabaseProductVersion());//获取数据库产品名称
		log("驱动版本：" + dbMeta.getDriverVersion());//获取驱动版本
		
		log("=================================================================================================================================");
		
		//参数列表 1:类别名称,2: 模式名称的模式,3:表名称模式,4:要包括的表类型所组成的列表
		ResultSet rs = dbMeta.getTables(null, null, null, new String[]{"TABLE"});
		
		List<Table> tableList = new ArrayList<Table>();
		
		while(rs.next()){
			String tableName = rs.getString("TABLE_NAME");
			String tableComment = rs.getString("REMARKS");
			
			Table table = new Table(tableName, tableComment);
			tableList.add(table);
		}
		
		for (Table table : tableList) {
			
			ResultSet colums = dbMeta.getColumns(null, null, table.getName(), null);
			ResultSet pKeys = dbMeta.getPrimaryKeys(null, null, table.getName());
			
			List<Field> fieldList = new ArrayList<Field>();
			
			while(colums.next()){
				String columnName = colums.getString("COLUMN_NAME");
				String typeName = colums.getString("TYPE_NAME");
				String comment = colums.getString("REMARKS");
				String defaultValue = colums.getString("COLUMN_DEF");
				int dataType = colums.getInt("DATA_TYPE");
				boolean generated = "YES".equals(colums.getString("IS_GENERATEDCOLUMN"));
				boolean autoIncrement = "YES".equals(colums.getString("IS_AUTOINCREMENT"));
				
				@SuppressWarnings("rawtypes")
				Class clazz = SqlTypeMap.toClass(dataType, typeName);
				
				Field field = new Field(columnName, clazz, comment, defaultValue, 
						false, generated, autoIncrement);
				fieldList.add(field);
			}
			
			while(pKeys.next()){
				String columnName = pKeys.getString("COLUMN_NAME");
				
				for (Field field : fieldList) {
					if(field.getName().equals(columnName)){
						field.setPrimaryKey(true);
						break;
					}
				}
			}
			
			table.setFieldList(fieldList);
		}
		
		return tableList;
	}
	
	private static void log(String string){
		System.out.println(string);
	}
	
}

