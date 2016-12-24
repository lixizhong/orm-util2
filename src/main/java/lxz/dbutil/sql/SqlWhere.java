package lxz.dbutil.sql;

import java.util.*;

/**
 * Sql基础类:包含查询条件功能，Created by lixizhong on 2016/12/22.
 */
abstract class SqlWhere<T extends SqlWhere> {
    //表名
    protected String tableName;

    //where子句类型：只能是or或者and
    private String logicType;

    //sql参数Map
    protected Map<String, Object> clauseValueMap = new LinkedHashMap<String, Object>();
    //sql参数Map在mybatis mapper中的命名，必须和声明一致，mybatis会根据此名称调用get方法获取map
    protected String clauseValueMapName = "clauseValueMap";

    //sql类实例在mybatis mapper中的命名，要与dao中保持一致
    //例如: dao中的接口声明如下，注意【@Param("sql")】
    //public Type load(@Param("sql") SqlSelect sql, @Param("typeInstance") Type typeInstance);
    protected String sqlEntityName = "sql";

    //保存where查询条件
    protected StringBuilder whereBuilder = new StringBuilder(256);

    /**
     * 构造函数，指定表名
     */
    public SqlWhere(String tableName) {
        if(StringUtils.isBlank(tableName)) {
            throw new IllegalArgumentException("表名不能为空");
        }
        this.tableName = tableName.trim();
    }

    /**
     * where条件，where()调用必须在and()/or()之前。
     */
    public T where(String whereClause){
        if(StringUtils.isBlank(whereClause)) {
            throw new IllegalArgumentException("条件不能为空");
        }

        whereBuilder.setLength(0);
        whereBuilder.append("where ");
        whereBuilder.append(whereClause.trim());
        return (T) this;
    }

    /**
     * where条件，where()调用必须在and()/or()之前,comparator为in时，value必须为List,Set或者数组
     */
    public T where(String column, String comparator, Object value){
        whereBuilder.setLength(0);
        whereBuilder.append("where ");

        generateCompareExpression(column, comparator, value);

        return (T) this;
    }

    /**
     * and条件，必须在where()调用之后调用
     */
    public T and(String clause){
        if(StringUtils.isBlank(clause)) {
            throw new IllegalArgumentException("条件不能为空");
        }

        if(whereBuilder.length() == 0){
            throw new RuntimeException("调用and之前必须先调用where方法");
        }

        if("or".equalsIgnoreCase(logicType)){
            throw new RuntimeException("同一个sql子句不能混用and和or");
        }

        logicType = "and";
        whereBuilder.append(" and ");

        whereBuilder.append(clause.trim());

        return (T) this;
    }

    /**
     * and条件，必须在where()调用之后调用,comparator为in时，value必须为List,Set或者数组
     */
    public T and(String column, String comparator, Object value){
        if(whereBuilder.length() == 0){
            throw new RuntimeException("调用and之前必须先调用where方法");
        }

        if("or".equalsIgnoreCase(logicType)){
            throw new RuntimeException("同一个sql子句不能混用and和or");
        }

        logicType = "and";
        whereBuilder.append(" and ");

        generateCompareExpression(column, comparator, value);

        return (T) this;
    }

    /**
     * or条件，必须在where()调用之后调用
     */
    public T or(String clause){
        if(StringUtils.isBlank(clause)) {
            throw new IllegalArgumentException("条件不能为空");
        }

        if(whereBuilder.length() == 0){
            throw new RuntimeException("调用or之前必须先调用where方法");
        }

        if("and".equalsIgnoreCase(logicType)){
            throw new RuntimeException("同一个sql子句不能混用and和or");
        }

        logicType = "or";

        whereBuilder.append(" or ");
        whereBuilder.append(clause.trim());

        return (T) this;
    }

    /**
     * or条件，必须在where()调用之后调用,comparator为in时，value必须为List,Set或者数组
     */
    public T or(String column, String comparator, Object value){
        if(whereBuilder.length() == 0){
            throw new RuntimeException("调用or之前必须先调用where方法");
        }

        if("and".equalsIgnoreCase(logicType)){
            throw new RuntimeException("同一个sql子句不能混用and和or");
        }

        logicType = "or";
        whereBuilder.append(" or ");

        generateCompareExpression(column, comparator, value);

        return (T) this;
    }

    /**
     * <pre>
     * 生成sql比较表达式,对于给定的字段和值以及比较字符，生成比较表达式，并将value存入clauseValueMap
     * 例如，给定参数为("id", "=", 1)，则结果如下：
     * 生成的sql表达式: id = #{sql.clauseValueMap.id}
     * clauseValueMap保存：clauseValueMap.put("id", 1)
     * 特别的：对于sql条件为in的情况，value一般为集合或者数组
     * 例如，给定参数为("id", "in", {1,2,3})，结果如下：
     * 生成的sql表达式为：id in (#{sql.clauseValueMap.id_list_0}, #{sql.clauseValueMap.id_list_1}, #{sql.clauseValueMap.id_list_2})
     * clauseValueMap保存：clauseValueMap.put("id_list_0", 1)  .put("id_list_1", 2) .put("id_list_2", 3)
     * </pre>
     */
    private void generateCompareExpression(String column, String comparator, Object value) {
        if(StringUtils.isBlank(column) || StringUtils.isBlank(comparator)) {
            throw new IllegalArgumentException("列名、比较操作符不能为空");
        }

        column = column.trim();
        comparator = comparator.trim().toLowerCase();

        if("in".equalsIgnoreCase(comparator)) {
            if( value != null && ! (value instanceof List) && ! (value instanceof Set) && ! value.getClass().isArray()) {
                throw new IllegalArgumentException("使用in时，value必须为List,Set或者数组");
            }
        }

        int dotIndex = column.indexOf('.');
        //去掉带表名前缀的列名以及反单引号：`table`.`username` => username
        String pureColumn = column.substring(dotIndex == -1 ? 0 : dotIndex + 1).replace("`", "");

        if("in".equalsIgnoreCase(comparator)) {
            //处理value，将value转化为set
            Set<Object> set = new HashSet<Object>();
            if(value instanceof List && ((List) value).size() > 0) {
                set.addAll((List) value);
            }else if(value.getClass().isArray()){
                for (Object o : (Object[]) value) {
                    set.add(o);
                }
            }else if(value instanceof Set){
                set = (Set<Object>) value;
            }else {
                set.add(value);
            }

            //生成sql参数名
            List<String> list = new ArrayList<String>();
            int index = 0;
            for (Object o : set) {
                int i = index++;
                String placeHolder;

                do {
                    placeHolder = String.format("%s_list_%d", pureColumn, i++);
                }while(clauseValueMap.keySet().contains(placeHolder));

                list.add(String.format("#{%s.%s.%s}", sqlEntityName, clauseValueMapName, placeHolder));
                clauseValueMap.put(placeHolder, o);
            }
            whereBuilder.append(String.format("%s in (%s)", column, StringUtils.join(list, ", ")));
        }else{
            int i = 0;
            String placeHolder;

            do {
                placeHolder = String.format("%s_%d", pureColumn, i++);
            }while(clauseValueMap.keySet().contains(placeHolder));

            clauseValueMap.put(placeHolder, value);
            whereBuilder.append(String.format("%s %s #{%s.%s.%s}", column, comparator, sqlEntityName, clauseValueMapName, placeHolder));
        }
    }

    public Map<String, Object> getClauseValueMap() {
        return clauseValueMap;
    }
}
