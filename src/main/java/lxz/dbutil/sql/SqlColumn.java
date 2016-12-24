package lxz.dbutil.sql;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

/**
 * Sql基础类:包含设置查询或者更新列功能，Created by lixizhong on 2016/12/22.
 */
abstract class SqlColumn<T extends SqlColumn> extends SqlWhere<T>{
    //保存要查询或者更新的列，仅SqlSelect、SqlUpdate使用
    protected List<String> columnList = new LinkedList<String>();

    /**
     * 构造函数，指定表名
     */
    public SqlColumn(String tableName) {
        super(tableName);
    }

    /**
     * 添加需要查询的列
     */
    public T addColumn(String column){
        if(StringUtils.isBlank(column)) {
            throw new IllegalArgumentException("列名不能为空");
        }

        column = column.trim();

        //检查是否重复添加
        if(columnList.contains(column)) {
            throw new IllegalArgumentException(String.format("%s已经存在，不要重复添加", column));
        }

        columnList.add(column);
        return (T) this;
    }

    /**
     * 添加需要查询的列
     */
    public T addColumns(List<String> columnList) {
        for (String c : columnList) {
            addColumn(c);
        }
        return (T) this;
    }

    /**
     * 从字段列表中移除一个要查询的字段
     */
    public T removeColumn(String column) {
        if(StringUtils.isBlank(column)) {
            throw new IllegalArgumentException("列名不能为空");
        }

        column = column.trim();

        ListIterator<String> it = columnList.listIterator();
        while(it.hasNext()) {
            String c = it.next();
            if(c.equals(column)) {
                it.remove();
                break;
            }
        }

        return (T) this;
    }

    /**
     * 清空要查询的字段
     */
    public T clearColumn(){
        columnList.clear();
        return (T) this;
    }
}
