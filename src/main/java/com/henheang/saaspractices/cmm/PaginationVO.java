package com.henheang.saaspractices.cmm;

/**
 * Computes all values needed to render a pagination bar.
 * <p>
 * Usage:
 *   PaginationVO paging = new PaginationVO(totCnt, pageIndex, recordCountPerPage);
 *   model.addAttribute("paging", paging);
 * <p>
 * In Thymeleaf iterate from paging.firstPage to paging.lastPage (inclusive).
 */
public class PaginationVO {

    private final int currentPage;
    private final int totalCount;
    private final int recordCountPerPage;

    private final int totalPageCount;
    private final int firstPage;           // first page number in current block
    private final int lastPage;            // last page number in current block
    private final boolean hasPrev;         // is there a previous block?
    private final boolean hasNext;         // is there a next block?

    public PaginationVO(int totalCount, int currentPage, int recordCountPerPage) {
        this.totalCount         = totalCount;
        this.currentPage        = currentPage;
        this.recordCountPerPage = recordCountPerPage;

        // total pages (minimum 1)
        this.totalPageCount = Math.max(1, (int) Math.ceil((double) totalCount / recordCountPerPage));

        // which block of 10 pages are we in?
        // number of page links shown per block
        int pageSize = 10;
        int currentBlock = (int) Math.ceil((double) currentPage / pageSize);
        this.firstPage = (currentBlock - 1) * pageSize + 1;
        this.lastPage  = Math.min(firstPage + pageSize - 1, totalPageCount);

        this.hasPrev = firstPage > 1;
        this.hasNext = lastPage  < totalPageCount;
    }

    public int getCurrentPage()       { return currentPage; }
    public int getTotalCount()        { return totalCount; }
    public int getRecordCountPerPage(){ return recordCountPerPage; }
    public int getTotalPageCount()    { return totalPageCount; }
    public int getFirstPage()         { return firstPage; }
    public int getLastPage()          { return lastPage; }
    public boolean isHasPrev()        { return hasPrev; }
    public boolean isHasNext()        { return hasNext; }
    public int getPrevPage()          { return firstPage - 1; }
    public int getNextPage()          { return lastPage  + 1; }
}
