package sql;

import java.util.Iterator;
import java.util.List;

/**
 * Created by lixizhong on 2016/12/24.
 */
class StringUtils {
    /**
     * 判断字符串是否为空
     */
    public static boolean isBlank(String str) {
        return str == null || str.trim().length() == 0;
    }

    /**
     * 判断是否有空字符串
     */
    public static boolean isAnyBlank(String... strs) {
        for (String s : strs) {
            if(isBlank(s))  return true;
        }
        return false;
    }

    /**
     * 判断字符串数组中是否包含给定字符串，允许null
     */
    public static boolean isAny(String str, String[] strs) {
        for (String s : strs) {
            if(s == str ||
                    (s != null && s.equals(str)) ||
                    (str != null && s.equals(s)) ) {
                return true;
            }
        }
        return false;
    }

    /**
     * 使用给定字符串连接字符串列表
     */
    public static String join(List<String> strList, String separator) {
        if (strList == null || strList.size() == 0) {
            return null;
        }

        Iterator<String> iterator = strList.iterator();

        String first = iterator.next();
        if ( ! iterator.hasNext()) {
            return first;
        }

        StringBuilder buf = new StringBuilder(256);
        if (first != null && first.length() > 0) {
            buf.append(first);
        }

        while (iterator.hasNext()) {
            String str = iterator.next();
            if (str != null && str.length() > 0) {
                if (separator != null && buf.length() > 0) {
                    buf.append(separator);
                }

                buf.append(str);
            }
        }
        return buf.toString();
    }
}
