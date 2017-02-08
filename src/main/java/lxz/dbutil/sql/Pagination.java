package lxz.dbutil.sql;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author lixizhong
 * <pre>
 * 页码格式：
 * [prebtn] 1 ... n1 n2 [n3] n4 n5 ... last [nextbtn]
 *                 |      |      |
 *              pageFrom  |    pageTo
 *                   currentPage
 * </pre>
 */
public class Pagination<T> implements Serializable{
	private static final long serialVersionUID = 1L;

	private int currentPage = 1;	    //当前页码
    private int pageSize = 20;		//每页数量
	private int totalSize = 0;		//记录总数量
    private int pageSpan = 2;         //页码跨度

    //以下属性需要计算，在调用getter方法时需要判断initFlag是否为true，否则需要调用caculatePage方法进行计算
    private int totalPage = 0;	//总页数
	private int pageFrom = 1;   //起始页
	private int pageTo = 1;     //结束页

    private boolean prevPageAvailable = false;	    //上一页是否可用
    private boolean nextPageAvailable = false;		//下一页是否可用

	private int fromIndex = 0;	//当前页起始记录索引
	private int toIndex = 0;      //当前页结束记录索引

    private int listSize = 0;     //当前页列表长度

    private boolean initFlag = false;   //是否已经调用caculatePage方法完成计算
	
	public List<T> list = new ArrayList<T>();
	
	public Pagination() {}

    /**
     * 分页类
     * @param currentPage 当前页，从1开始
     * @param pageSize 每页数量
     */
    public Pagination(int currentPage, int pageSize){
        if (pageSize <= 0) {
            throw new IllegalArgumentException("pageSize必须大于0");
        }
        if (currentPage <= 0) {
            throw new IllegalArgumentException("页码从1开始");
        }

        this.pageSize = pageSize;
        this.currentPage = currentPage;
    }

	/**
	 * 分页类
	 * @param currentPage 当前页，从1开始
	 * @param pageSize 每页数量
     * @param totalSize 总记录数
	 */
	public Pagination(int currentPage, int pageSize, int totalSize) {
		if (pageSize <= 0) {
			throw new IllegalArgumentException("pageSize必须大于0");
		}
		if (currentPage <= 0) {
			throw new IllegalArgumentException("页码从1开始");
		}
		
		if (totalSize < 0) {
			throw new IllegalArgumentException("总数目必须大于或者等于0");
		}
		
		this.pageSize = pageSize;
		this.totalSize = totalSize;
		this.currentPage = currentPage;
		
		caculatePage();
	}

    public Pagination(int currentPage, int pageSize, int totalSize, int pageSpan) {
        if (pageSize <= 0) {
            throw new IllegalArgumentException("pageSize必须大于0");
        }
        if (currentPage <= 0) {
            throw new IllegalArgumentException("页码从1开始");
        }

        if (totalSize < 0) {
            throw new IllegalArgumentException("总数目必须大于或者等于0");
        }

        if (pageSpan < 1) {
            throw new IllegalArgumentException("pageSpan不能小于1");
        }

        this.pageSize = pageSize;
        this.totalSize = totalSize;
        this.currentPage = currentPage;
        this.pageSpan = pageSpan;

        caculatePage();
    }

    /**
     * 计算分页
     */
	private void caculatePage(){
        initFlag = true;

		if(totalSize <= 0){
			return;
		}

		//计算总页数
		totalPage = new Double(Math.ceil(((double) totalSize) / pageSize)).intValue();

		if(currentPage > totalPage){
			currentPage = totalPage;
		}

		//判断上一页、下一页是否可用
		nextPageAvailable = this.currentPage < totalPage;
		prevPageAvailable = this.currentPage > 1;

        if(totalPage < pageSpan * 2 + 5) {
            pageFrom = 1;
            pageTo = totalPage;
        }else{
            pageFrom = currentPage - pageSpan;
            pageTo = currentPage + pageSpan;

            if(pageFrom <= 3) {
                pageFrom = 1;
            }else if(totalPage - pageTo <= 2) {
                pageTo = totalPage;
            }
        }

		fromIndex = (currentPage - 1) * pageSize;
		toIndex = fromIndex + pageSize - 1;
		if(toIndex > totalSize - 1){
			toIndex = totalSize - 1;
		}
	}

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        if (currentPage <= 0) {
            throw new IllegalArgumentException("页码从1开始");
        }
        this.currentPage = currentPage;
        initFlag = false;
    }

    public int getTotalPage() {
        if( ! initFlag) caculatePage();
        return totalPage;
    }

    public void setTotalPage(int totalPage) {
        this.totalPage = totalPage;
        initFlag = false;
    }

    public int getTotalSize() {
        if( ! initFlag) caculatePage();
        return totalSize;
    }

    public void setTotalSize(int totalSize) {
        if (totalSize < 0) {
            throw new IllegalArgumentException("总数目必须大于或者等于0");
        }
        this.totalSize = totalSize;
        initFlag = false;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        if (pageSize <= 0) {
            throw new IllegalArgumentException("pageSize必须大于0");
        }
        this.pageSize = pageSize;
        initFlag = false;
    }

    public boolean isNextPageAvailable() {
        if( ! initFlag) caculatePage();
        return nextPageAvailable;
    }

    public void setNextPageAvailable(boolean nextPageAvailable) {
        this.nextPageAvailable = nextPageAvailable;
        initFlag = false;
    }

    public boolean isPrevPageAvailable() {
        if( ! initFlag) caculatePage();
        return prevPageAvailable;
    }

    public void setPrevPageAvailable(boolean prevPageAvailable) {
        this.prevPageAvailable = prevPageAvailable;
        initFlag = false;
    }

    public int getPageSpan() {
        return pageSpan;
    }

    public void setPageSpan(int pageSpan) {
        if (pageSpan < 1) {
            throw new IllegalArgumentException("pageSpan不能小于1");
        }
        this.pageSpan = pageSpan;
        initFlag = false;
    }

    public int getPageFrom() {
        if( ! initFlag) caculatePage();
        return pageFrom;
    }

    public void setPageFrom(int pageFrom) {
        this.pageFrom = pageFrom;
        initFlag = false;
    }

    public int getPageTo() {
        if( ! initFlag) caculatePage();
        return pageTo;
    }

    public void setPageTo(int pageTo) {
        this.pageTo = pageTo;
        initFlag = false;
    }

    public int getFromIndex() {
        if( ! initFlag) caculatePage();
        return fromIndex;
    }

    public void setFromIndex(int fromIndex) {
        this.fromIndex = fromIndex;
        initFlag = false;
    }

    public int getToIndex() {
        if( ! initFlag) caculatePage();
        return toIndex;
    }

    public void setToIndex(int toIndex) {
        this.toIndex = toIndex;
        initFlag = false;
    }

    public int getListSize() {
        return listSize;
    }

    public void setListSize(int listSize) {}

    public List<T> getList() {
        return list;
    }

    public void setList(List<T> list) {
        this.list = list;
        if(list != null) listSize = list.size();
        initFlag = false;
    }
}
