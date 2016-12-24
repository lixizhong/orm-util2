package lxz.dbutil.sql;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author lixizhong
 * 
 * 格式：
 * prebtn 1 ... n1 n2 [n3] n4 n5 ... last nextbtn
 *              |      |      |
 *            pageFrom |    pageTo
 *                currentPage
 */
public class Pagination<T> implements Serializable{
	
	private static final long serialVersionUID = 1L;

	private int currentPage = 1;	//当前页码
	private int totalPage = 0;		//总页数
	private int totalSize = 0;		//总数量
	private int pageSize = 20;		//每页数量
	
//	private Boolean nextPageAvailable = false;		//上一页是否可用
//	private Boolean previousPageAvailable = false;	//下一页是否可用
	
	private int pageSpan = 2;   //页码跨度
	
	private int pageFrom = 1;   //起始页
	private int pageTo = 1;     //结束页
	
	private int fromIndex = 0;	//当前页起始索引
	private int toIndex = 0;      //当前页结束索引
	
	public List<T> list = new ArrayList<T>();
	
	public Pagination(){
		caculatePage();
	}

	/**
	 * 分页类，页码从1开始
	 * @param currentPage
	 * @param pageSize
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
	
	private void caculatePage(){
		if(totalSize <= 0){
			return;
		}
		
		totalPage = new Double(Math.ceil(((double) totalSize) / pageSize)).intValue();

		if(currentPage > totalPage){
			currentPage = totalPage;
		}
		
//		nextPageAvailable = this.currentPage < totalPage;
//		previousPageAvailable = this.currentPage > 1;
		
		if(totalPage <= 10){
			pageFrom  = 1;
			pageTo = totalPage;
		}else{
			if(currentPage - pageSpan < 1){
				pageFrom = 1;
				pageTo = pageFrom + pageSpan * 2;
			}else if(currentPage + pageSpan > totalPage){
				pageTo = totalPage;
				pageFrom = pageTo - pageSpan * 2;
			}else{
				pageFrom = currentPage - pageSpan;
				pageTo = currentPage + pageSpan;
			}
		}
		
		fromIndex = (currentPage - 1) * pageSize;
		toIndex = fromIndex + pageSize - 1;
		if(toIndex > totalSize - 1){
			toIndex = totalSize - 1;
		}
	}

	public void setTotalSize(int totalSize) {
		this.totalSize = totalSize;
		caculatePage();
	}

	public int getCurrentPage() {
		return currentPage;
	}

	public int getTotalPage() {
		return totalPage;
	}

	public int getTotalSize() {
		return totalSize;
	}

	public int getPageSize() {
		return pageSize;
	}

//	public Boolean isNextPageAvailable() {
//		return nextPageAvailable;
//	}
//
//	public Boolean isPreviousPageAvailable() {
//		return previousPageAvailable;
//	}

	public int getFromIndex() {
		return fromIndex;
	}

	public int getToIndex() {
		return toIndex;
	}

	public int getPageSpan() {
		return pageSpan;
	}

	public int getPageFrom() {
		return pageFrom;
	}

	public int getPageTo() {
		return pageTo;
	}
	
	public List<T> getList() {
		return list;
	}

	public void setList(List<T> list) {
		this.list = list;
	}

	public int getListSize() {
		return list.size();
	}
	
	
}
