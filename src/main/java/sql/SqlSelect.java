package sql;

import java.util.LinkedList;
import java.util.List;

import static sql.StringUtils.isAny;


public class SqlSelect extends SqlColumn<SqlSelect> {
    /**
     * Join类：处理表关联
     */
    private static class Join {
        public String table;            //关联的表
        public String joinType;         //关联类型
        public String joinCondition;   //关联条件
        //支持的关联类型
        public static final String[] joinTypes = new String[]{"left", "right", "inner", "full"};

        public Join(String table, String joinType, String joinCondition) {
            if(StringUtils.isAnyBlank(table, joinType, joinCondition)) {
                throw new IllegalArgumentException("参数不能为空");
            }

            joinType = joinType.toLowerCase().replace("outer", "").trim();

            if( ! StringUtils.isAny(joinType, joinTypes) ) {
                throw new IllegalArgumentException(String.format("不支持的关联类型: %s", joinType));
            }

            this.table = table.trim();
            this.joinType = joinType;
            this.joinCondition = joinCondition.trim();
        }
    }

    //保存关联的表
    private List<Join> joinList = new LinkedList<Join>();

    //排序类型
    private static final String[] orderTypes = new String[]{"desc", "asc"};

    //group子句
    private StringBuilder groupBuilder = new StringBuilder(64);

    //order子句
    private StringBuilder orderBuilder = new StringBuilder(64);

    //limit子句
    private String limit = "";

    /**
     * 构造方法：指定表名
     */
    public SqlSelect(String tableName){
        super(tableName);
    }

    /**
     * 查询数量，调用此方法时，使用addColumn(s)添加的列会被清空，查询列会变成count(*)
     */
    public SqlSelect count() {
        columnList.clear();
        columnList.add("count(*)");
        return this;
    }

    /**
     * 关联查询
     * @param joinTable 关联的表
     * @param joinType 关联类型: left right inner等等，不要包含join关键字
     * @param joinCondition 关联条件: A.id = B.id，不要包含on关键字
     * @return
     */
    public SqlSelect join(String joinTable, String joinType, String joinCondition) {
        joinList.add(new Join(joinTable, joinType, joinCondition));
        return this;
    }

    /**
     * 排序条件
     * @param column 要排序的列名
     * @param orderType 排序类型，只能为asc或者desc
     * @return
     */
    public SqlSelect orderBy(String column, String orderType){
        if(StringUtils.isBlank(column)) {
            throw new IllegalArgumentException("列名不能为空");
        }

        column = column.trim();

        if( ! StringUtils.isBlank(orderType) && ! isAny(orderType, orderTypes)){
            throw new IllegalArgumentException(String.format("不支持的排序类型: %s", orderType));
        }

        if(orderBuilder.length() > 0){
            orderBuilder.append(", ");
        }

        orderBuilder.append(column);
        if( ! StringUtils.isBlank(orderType)) {
            orderBuilder.append(" ");
            orderBuilder.append(orderType);
        }

        return this;
    }

    /**
     * group条件
     * @param column group的列
     * @return
     */
    public SqlSelect groupBy(String column){
        if(StringUtils.isBlank(column)) {
            throw new IllegalArgumentException("列名不能为空");
        }

        column = column.trim();

        if(groupBuilder.length() > 0){
            groupBuilder.append(", ");
        }

        groupBuilder.append(column);
        return this;
    }

    /**
     * limit from, count
     */
    public SqlSelect limit(int from, int count){
        if(from < 0 || count < 0) {
            throw new IllegalArgumentException("from或者count不能为负数");
        }

        limit = String.format("limit %d, %d", from, count);
        return this;
    }

    /**
     * 等效于limit 0, count
     */
    public SqlSelect limit(int count){
        if(count < 0) {
            throw new IllegalArgumentException("count不能为负数");
        }
        limit = String.format("limit 0, %d", count);
        return this;
    }

    public SqlSelect paginate(@SuppressWarnings("rawtypes") Pagination page){
        limit(page.getFromIndex(), page.getPageSize());
        return this;
    }

    /**
     * 输出sql语句
     */
    public String getSql(){
        if(columnList.size() == 0){
            throw new IllegalArgumentException("必须指定要查询的字段");
        }

        StringBuilder builder = new StringBuilder();

        builder.append("select ");
        builder.append(StringUtils.join(columnList, ", "));
        builder.append(" from ");
        builder.append(tableName);
        builder.append(" ");

        if(joinList.size() > 0) {
            for (Join join : joinList) {
                builder.append(String.format("%s join %s on %s ", join.joinType, join.table, join.joinCondition));
            }
        }

        if(whereBuilder.length() > 0){
            builder.append(whereBuilder);
            builder.append(" ");
        }

        if(groupBuilder.length() > 0){
            builder.append("group by ");
            builder.append(groupBuilder);
            builder.append(" ");
        }

        if(orderBuilder.length() > 0){
            builder.append("order by ");
            builder.append(orderBuilder);
            builder.append(" ");
        }

        builder.append(limit);

        return builder.toString();
    }

    @Override
    public String toString(){
        return getSql();
    }
}