package sql;

public class SqlDelete extends SqlWhere<SqlDelete> {
	public SqlDelete(String tableName) {
		super(tableName);
	}

    /**
     * 输出sql语句
     */
	public String getSql() {
        if (whereBuilder.length() == 0) {
            throw new IllegalArgumentException("必须指定删除条件");
        }

		StringBuilder builder = new StringBuilder();
		builder.append("delete from ");
		builder.append(tableName);
		builder.append(" ");
		builder.append(whereBuilder.toString());
		
		return builder.toString();
	}
	
	@Override
	public String toString(){
		return getSql();
	}
}
