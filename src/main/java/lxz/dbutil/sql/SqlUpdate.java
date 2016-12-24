package lxz.dbutil.sql;


public class SqlUpdate extends SqlColumn<SqlUpdate> {
    /**
     * 构造函数，指定表名
     */
	public SqlUpdate(String tableName) {
        super(tableName);
	}

	public SqlUpdate addColumn(String column, Object value) {
        int dotIndex = column.indexOf('.');
        //去掉带表名前缀的列名以及反单引号：`table`.`username` => username
        String pureColumn = column.substring(dotIndex == -1 ? 0 : dotIndex + 1).replace("`", "");

        int i = 0;
        String placeHolder;

        do {
            placeHolder = String.format("%s_%d", pureColumn, i++);
        }while(clauseValueMap.keySet().contains(placeHolder));

        clauseValueMap.put(placeHolder, value);
        columnList.add(String.format("%s = #{%s.%s.%s}", column, sqlEntityName, clauseValueMapName, placeHolder));

        return this;
    }

    /**
     * 输出sql语句
     */
	public String getSql() {
		if(columnList.size() == 0){
			throw new IllegalArgumentException("必须指定要更新的列");
		}

        if (whereBuilder.length() == 0) {
            throw new IllegalArgumentException("必须指定更新条件");
        }
		
		StringBuilder builder = new StringBuilder();
		builder.append("update ");
		builder.append(tableName);
		builder.append(" set ");
		builder.append(StringUtils.join(columnList, ", "));
		builder.append(" ");
		builder.append(whereBuilder.toString());
		
		return builder.toString();
	}
	
	@Override
	public String toString(){
		return getSql();
	}
}
