# Pagination Implementation Guide

> 📚 **Reference / study material** — copied from the company project `saas-olv`. Teaches the CommAjax + commPaging pagination pattern.
>
> Samples: `SmpBoardController.java`, `SmpBoardList.html`

---

## 1. Controller

```java
import egovframework.com.cmm.service.CmmInVO;
import egovframework.com.cmm.service.CmmOutVO;
import egovframework.com.cmm.service.CmmPaginationInfo;

/** Show list page */
@RequestMapping("/{urlPrefix}/list.do")
public String selectListView() throws Exception {
    return "egovframework/com/{domain}/{Domain}List";
}

/** List query (AJAX) */
@RequestMapping("/{urlPrefix}/selectList.do")
@ResponseBody
public CmmOutVO selectList(@RequestBody CmmInVO<{Domain}InVO> request) throws Exception {

    CmmPaginationInfo paginationInfo = CmmPaginationInfo.setupPaging(request);
    {Domain}InVO inVO = request.getBody();

    List<{Domain}OutVO> resultList = {camelCase}Service.selectList(inVO);
    int totCnt = {camelCase}Service.selectListTotCnt(inVO);
    paginationInfo.setTotalRecordCount(totCnt);

    return CmmOutVO.of(resultList, paginationInfo);
}
```

- `CmmInVO<T>` — auto-binds the `{ body: InVO, page: {...} }` payload sent by CommAjax.
- `CmmPaginationInfo.setupPaging(request)` — automatically sets `firstIndex` and `recordCountPerPage` on the InVO.
- `CmmOutVO.of(list, paginationInfo)` — produces the `{ body: [...], page: {...} }` response.

---

## 2. SQL Mapper

```sql
SELECT ...
  FROM {table}
 WHERE 1=1
 ORDER BY pk DESC
 LIMIT #{recordCountPerPage} OFFSET #{firstIndex}
```

`recordCountPerPage` and `firstIndex` are populated by `setupPaging()` on the InVO.

---

## 3. HTML

```html
<!-- Search form — give it an id, use button type="button" -->
<form id="searchForm" class="search-bar">
  <select name="searchCondition" class="form-control">
    <option value="1">Title</option>
    <option value="2">Content</option>
  </select>
  <input type="text" name="searchKeyword" class="form-control"/>
  <button type="button" class="btn btn--primary" onclick="fn_search(true)">Search</button>
</form>

<!-- Table — give tbody an id (rendered dynamically by JS) -->
<table class="table table--center">
  <thead>
    <tr><th>No.</th><th>Title</th>...</tr>
  </thead>
  <tbody id="listBody"></tbody>
</table>

<!-- Pagination — empty div, commPaging fills it -->
<div class="section-foot">
  <div class="pagination-wrap"></div>
</div>
```

**Important**
- Do NOT add `th:action` or `method` to `<form>` (no submit).
- Search button MUST be `type="button"` (prevents form submit).
- Leave `<tbody>` empty — the JS callback renders rows.

---

## 4. JavaScript

```javascript
/* Init paging (global) */
var paging = commPaging();
paging.setRowUnit(10);            // default is 100, so always set this
paging.setSearchFn(fn_search);

/* Search */
function fn_search(init) {
    var comAjax = new CommAjax();
    comAjax.setUrl("/{urlPrefix}/selectList.do");
    comAjax.setForm("searchForm");
    comAjax.setCallback(fn_searchCallback);
    comAjax.setPaging(paging);
    comAjax.setPagingInit(init);   // true = page 1, false = current page
    comAjax.ajax();
}

/* Callback — render table */
function fn_searchCallback(data) {
    var list = data.body;
    var html = "";

    if (!list || list.length === 0) {
        html = '<tr><td colspan="5" class="text-center text-muted">No data found.</td></tr>';
    } else {
        for (var i = 0; i < list.length; i++) {
            var item = list[i];
            var rowNum = paging.page.rowcount
                       - ((paging.page.pageindex - 1) * paging.page.rowunit)
                       - i;
            html += '<tr>';
            html += '<td>' + rowNum + '</td>';
            html += '<td><a href="...">' + escapeHtml(item.title) + '</a></td>';
            html += '</tr>';
        }
    }
    $("#listBody").html(html);
}

/* Initial search on page load */
$(function() { fn_search(true); });
```

---

## 5. Checklist

- [ ] Controller: `selectListView()` — returns the view only.
- [ ] Controller: `@RequestBody CmmInVO<InVO>` → `@ResponseBody CmmOutVO`.
- [ ] Controller: `CmmPaginationInfo.setupPaging(request)` + `request.getBody()`.
- [ ] Controller: `return CmmOutVO.of(resultList, paginationInfo)`.
- [ ] SQL: `LIMIT #{recordCountPerPage} OFFSET #{firstIndex}`.
- [ ] HTML: `<form id="searchForm">`, `<button type="button">`.
- [ ] HTML: `<tbody id="listBody">`.
- [ ] HTML: `<div class="pagination-wrap"></div>`.
- [ ] JS: `commPaging()` + `setRowUnit(10)` + `setSearchFn(fn_search)`.
- [ ] JS: `$(function() { fn_search(true); })`.
