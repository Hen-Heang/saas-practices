# OLV Project — Claude Code Development Guide

> 📚 **Reference / study material** — This document was copied from the company project `saas-olv`.
> It teaches the standard patterns used in Korean eGovFramework-based SaaS projects.

## 📚 Related Guides (all study copies)

| File | Contents |
|------|------|
| **[CLAUDE.md](CLAUDE.md)** *(this document)* | Project overview + 7 code patterns + naming rules + forbidden practices |
| **[GUIDE.md](GUIDE.md)** | End-to-end flow of building one screen (smp1~smp10) + request-flow step-by-step |
| [docs/saas-olv-reference/paging-guide.md](docs/saas-olv-reference/paging-guide.md) | AJAX pagination with CommAjax + commPaging |
| [docs/saas-olv-reference/identity-verification-guide.md](docs/saas-olv-reference/identity-verification-guide.md) | OmniOne CX identity verification (CI/DI) |
| [docs/saas-olv-reference/admin-district-combo-guide.md](docs/saas-olv-reference/admin-district-combo-guide.md) | Hierarchical administrative-district combo (Province → City → District → Town) |
| [docs/saas-olv-reference/README.md](docs/saas-olv-reference/README.md) | Index of study materials (this table + learning order) |

---

## 📌 Local Mapping — saas-olv → saas-practices

When the guides reference saas-olv class names, here is the equivalent in **this** project:

| Concept | saas-olv (real project) | saas-practices (this project) |
|---------|-------------------------|-------------------------------|
| Base VO with search/paging/audit | `egovframework.com.cmm.CmmVO` | [`com.henheang.saaspractices.cmm.BaseVO`](src/main/java/com/henheang/saaspractices/cmm/BaseVO.java) |
| Pagination helper | `CmmPaginationInfo` (AJAX-oriented) | [`PaginationVO`](src/main/java/com/henheang/saaspractices/cmm/PaginationVO.java) (server-side rendering) |
| AJAX request wrapper | `CmmInVO<T>` | *(not used yet — saas-practices uses server-side rendering)* |
| AJAX response wrapper | `CmmOutVO` | *(not used yet — saas-practices uses Model attributes)* |
| Working sample domain | `egovframework.com.smp.*` (SmpBoard) | [`com.henheang.saaspractices.ntc.*`](src/main/java/com/henheang/saaspractices/ntc/) (NtcBoard) |
| Sample HTML location | `templates/egovframework/com/smp/SmpBoard*.html` | [`templates/ntc/NtcBoard*.html`](src/main/resources/templates/ntc/) |
| Sample SQL XML | `egovframework/mapper/com/smp/SmpBoard_SQL.xml` | [`mapper/ntc/NtcBoard_SQL.xml`](src/main/resources/mapper/ntc/NtcBoard_SQL.xml) |
| URL pattern | `/smpBoard/list.do` | `/ntcBoard/list.do` |

### Key differences this project uses (compared to saas-olv)

- **Server-side rendering** (returns view + model) instead of AJAX (`CommAjax` + `commPaging`).
  → Easier to learn the pattern first; you can convert to AJAX later (see [paging-guide.md](docs/saas-olv-reference/paging-guide.md)).
- **Single module** instead of multi-module (`olv-core` / `olv-oper` / `olv-pfom` / `olv-api`).
- **No security yet** — no `CmmUserDetailsHelper`, audit IDs are hardcoded as `"practice_user"`.
- **Build**: Gradle (same as saas-olv).
- **DB**: PostgreSQL via MyBatis (same as saas-olv).
- **Templates**: Thymeleaf + Layout Dialect (same as saas-olv).

> ⚠️ When the guides below show `extends CmmVO` — write `extends BaseVO` in this project.
> When they show `CmmPaginationInfo.setupPaging(request)` — use `new PaginationVO(totCnt, pageIndex, recordCountPerPage)` instead.

---

## Project Overview
- **Framework**: Spring Boot 3.3.5 + eGovFramework 5.0.0
- **Java**: 21 (toolchain), **Lombok is NOT used**
- **Build**: Gradle multi-module
- **DB**: PostgreSQL (MyBatis Mapper)
- **Templates**: Thymeleaf + Layout Dialect
- **Auth**: Spring Security 6.x

## Module Structure
| Module | Role | Port | bootJar |
|--------|------|------|---------|
| olv-core | Common library (VO, utils, config) | - | X (jar only) |
| olv-oper | Admin web (CRUD screens) | 8081 | O |
| olv-pfom | User portal | 8080 | O |
| olv-api  | REST API | - | O |

## File Location Rules

### Java sources (olv-oper example)
```
olv-oper/src/main/java/egovframework/com/{domain}/
├── web/
│   └── {Domain}Controller.java         @Controller
├── service/
│   ├── {Domain}InVO.java               extends CmmVO (input)
│   ├── {Domain}OutVO.java              implements Serializable (output)
│   └── {Domain}Service.java            interface
├── service/impl/
│   └── {Domain}ServiceImpl.java        @Service
└── mapper/
    └── {Domain}Mapper.java             @Mapper (MyBatis)
```

### HTML templates
```
olv-oper/src/main/resources/templates/egovframework/com/{domain}/
├── {Domain}List.html         List
├── {Domain}Regist.html       Register (create)
└── {Domain}Updt.html         Update / detail
```

### SQL Mapper XML
```
olv-oper/src/main/resources/egovframework/mapper/com/{domain}/
└── {Domain}_SQL.xml
```

## Naming Conventions
- URL: `/{camelCaseDomain}/action.do` (e.g. `/smpBoard/list.do`)
- Controller: `{Domain}Controller`
- Service: `{Domain}Service` (interface) / `{Domain}ServiceImpl` (`@Service("{camelCase}Service")`)
- Mapper: `{Domain}Mapper` (`@Mapper`), `namespace` = Mapper FQCN
- InVO: `{Domain}InVO` — extends `CmmVO`, used for input / search / update parameters
- OutVO: `{Domain}OutVO` — `Serializable`, used only for query results
- SQL XML: `{Domain}_SQL.xml`
- DB column `snake_case` → Java field `camelCase`

## Development Order (one screen = 8–10 files)

> Build from the bottom up so you have no compile errors when you reach the layer above.

1. **Check DDL** — understand the table / sequence / primary key
2. **InVO** — `extends CmmVO`, table columns + input-only fields
3. **OutVO** — `implements Serializable`, query-result-only fields
4. **SQL Mapper XML** — `resultMap`, CRUD queries (LIMIT/OFFSET pagination)
5. **Mapper interface** — `@Mapper`, method names must match XML `id`
6. **Service interface** — declare CRUD methods
7. **ServiceImpl** — `@Service`, calls the Mapper
8. **Controller** — `@Controller`, URL mapping, redirect handling
9. **HTML** — List / Regist / Updt (use `layout:decorate`, fragments)

## Code Patterns — write them **exactly** like the `smp` sample

### InVO
```java
package egovframework.com.{domain}.service;

import egovframework.com.cmm.CmmVO;

/**
 * {Domain Korean name} InVO
 * Author :
 * Date   :
 */
public class {Domain}InVO extends CmmVO {

    private static final long serialVersionUID = 1L;

    /* ── Table-specific columns ─────────────────── */

    /** PK description */
    private long pkField;

    /** Field description */
    private String fieldName;

    /* ── Getter / Setter ────────────────────────── */

    public long getPkField() { return pkField; }
    public void setPkField(long pkField) { this.pkField = pkField; }

    public String getFieldName() { return fieldName; }
    public void setFieldName(String fieldName) { this.fieldName = fieldName; }
}
```

### OutVO
```java
package egovframework.com.{domain}.service;

import java.io.Serializable;

/**
 * {Domain Korean name} OutVO
 * Author :
 * Date   :
 */
public class {Domain}OutVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /* ── Table-specific columns ─────────────────── */

    // Query-result fields

    /* ── Audit columns — for display ────────────── */

    private String dataRegId;
    private String dataRegDt;
    private String dataChgId;
    private String dataChgDt;

    /* ── Getter / Setter ────────────────────────── */
    // Write getters/setters manually (Lombok is NOT allowed)
}
```

### Mapper interface
```java
package egovframework.com.{domain}.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import egovframework.com.{domain}.service.{Domain}InVO;
import egovframework.com.{domain}.service.{Domain}OutVO;

@Mapper
public interface {Domain}Mapper {
    List<{Domain}OutVO> selectList({Domain}InVO inVO) throws Exception;
    int selectListTotCnt({Domain}InVO inVO) throws Exception;
    {Domain}OutVO selectDetail({Domain}InVO inVO) throws Exception;
    void insert({Domain}InVO inVO) throws Exception;
    void update({Domain}InVO inVO) throws Exception;
    void delete({Domain}InVO inVO) throws Exception;
}
```

### Service interface
```java
package egovframework.com.{domain}.service;

import java.util.List;

/**
 * {Domain Korean name} service interface
 * Author :
 * Date   :
 */
public interface {Domain}Service {

    /** List query */
    List<{Domain}OutVO> selectList({Domain}InVO inVO) throws Exception;

    /** Total count for the list */
    int selectListTotCnt({Domain}InVO inVO) throws Exception;

    /** Detail query */
    {Domain}OutVO selectDetail({Domain}InVO inVO) throws Exception;

    /** Insert */
    void insert({Domain}InVO inVO) throws Exception;

    /** Update */
    void update({Domain}InVO inVO) throws Exception;

    /** Delete */
    void delete({Domain}InVO inVO) throws Exception;
}
```

### ServiceImpl
```java
package egovframework.com.{domain}.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import egovframework.com.{domain}.mapper.{Domain}Mapper;
import egovframework.com.{domain}.service.{Domain}InVO;
import egovframework.com.{domain}.service.{Domain}OutVO;
import egovframework.com.{domain}.service.{Domain}Service;

/**
 * {Domain Korean name} service implementation
 * Author :
 * Date   :
 */
@Service("{camelCase}Service")
public class {Domain}ServiceImpl implements {Domain}Service {

    @Autowired
    private {Domain}Mapper {camelCase}Mapper;

    @Override
    public List<{Domain}OutVO> selectList({Domain}InVO inVO) throws Exception {
        return {camelCase}Mapper.selectList(inVO);
    }

    @Override
    public int selectListTotCnt({Domain}InVO inVO) throws Exception {
        return {camelCase}Mapper.selectListTotCnt(inVO);
    }

    @Override
    public {Domain}OutVO selectDetail({Domain}InVO inVO) throws Exception {
        return {camelCase}Mapper.selectDetail(inVO);
    }

    @Override
    public void insert({Domain}InVO inVO) throws Exception {
        {camelCase}Mapper.insert(inVO);
    }

    @Override
    public void update({Domain}InVO inVO) throws Exception {
        {camelCase}Mapper.update(inVO);
    }

    @Override
    public void delete({Domain}InVO inVO) throws Exception {
        {camelCase}Mapper.delete(inVO);
    }
}
```

### Controller
```java
package egovframework.com.{domain}.web;

import java.util.List;
import java.util.Map;

import egovframework.com.cmm.service.CmmInVO;
import egovframework.com.cmm.service.CmmOutVO;
import egovframework.com.cmm.service.CmmPaginationInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import egovframework.com.cmm.CmmMessageSource;
import egovframework.com.cmm.LoginVO;
import egovframework.com.cmm.service.CmmFileService;
import egovframework.com.cmm.service.CmmFileStorageService;
import egovframework.com.cmm.service.FileVO;
import egovframework.com.cmm.util.CmmFileUtil;
import egovframework.com.cmm.util.CmmStringUtil;
import egovframework.com.cmm.util.CmmUserDetailsHelper;
import egovframework.com.cmm.exception.CmmBizException;
import egovframework.com.cmm.exception.CmmException;
import egovframework.com.{domain}.service.{Domain}InVO;
import egovframework.com.{domain}.service.{Domain}OutVO;
import egovframework.com.{domain}.service.{Domain}Service;
import jakarta.validation.Valid;

/**
 * {Domain Korean name} controller
 * Author :
 * Date   :
 */
@Controller
public class {Domain}Controller {

    @Autowired
    private {Domain}Service {camelCase}Service;

    @Autowired
    private CmmMessageSource cmmMessageSource;

    @Autowired
    private CmmFileService cmmFileService;

    @Autowired
    private CmmFileStorageService cmmFileStorageService;

    /** Show list page */
    @RequestMapping("/{urlPrefix}/list.do")
    public String selectListView() throws Exception {
        return "egovframework/com/{domain}/{Domain}List";
    }

    /** List query (AJAX) — works with CommAjax + commPaging */
    @RequestMapping("/{urlPrefix}/selectList.do")
    @ResponseBody
    public CmmOutVO selectList(@RequestBody CmmInVO<{Domain}InVO> request) throws Exception {

        // (a) Setup pagination + extract search-condition InVO
        CmmPaginationInfo paginationInfo = CmmPaginationInfo.setupPaging(request);
        {Domain}InVO inVO = request.getBody();

        // (b) Query list
        List<{Domain}OutVO> resultList = {camelCase}Service.selectList(inVO);

        // (c) Total count → finalize pagination → return
        int totCnt = {camelCase}Service.selectListTotCnt(inVO);
        paginationInfo.setTotalRecordCount(totCnt);

        return CmmOutVO.of(resultList, paginationInfo);
    }

    /** Detail */
    @RequestMapping("/{urlPrefix}/detail.do")
    public String selectDetail(
            @RequestParam("{pkParam}") long {pkField},
            @ModelAttribute("searchVO") {Domain}InVO searchInVO,
            ModelMap model) throws Exception {

        {Domain}InVO inVO = new {Domain}InVO();
        inVO.set{PkField}({pkField});
        {Domain}OutVO outVO = {camelCase}Service.selectDetail(inVO);

        if (outVO == null) {
            throw new CmmBizException("M0001", "Requested data not found.");
        }

        model.addAttribute("{camelCase}VO", outVO);

        // Attachment list
        if (outVO.getFileNo() != null && !outVO.getFileNo().isEmpty()) {
            FileVO fileVO = new FileVO();
            fileVO.setFileNo(outVO.getFileNo());
            List<FileVO> result = cmmFileService.selectFileList(fileVO);
            model.addAttribute("fileList", result);
        }

        return "egovframework/com/{domain}/{Domain}Updt";
    }

    /** Show register page */
    @RequestMapping("/{urlPrefix}/insertView.do")
    public String insertView(
            @ModelAttribute("{camelCase}VO") {Domain}InVO inVO) throws Exception {
        return "egovframework/com/{domain}/{Domain}Regist";
    }

    /** Insert */
    @RequestMapping("/{urlPrefix}/insert.do")
    public String insert(
            final MultipartHttpServletRequest multiRequest,
            @Valid @ModelAttribute("{camelCase}VO") {Domain}InVO inVO,
            BindingResult bindingResult,
            ModelMap model) throws Exception {

        if (bindingResult.hasErrors()) {
            return "egovframework/com/{domain}/{Domain}Regist";
        }

        LoginVO user = (LoginVO) CmmUserDetailsHelper.getAuthenticatedUser();
        inVO.setDataRegId(user == null ? "" : CmmStringUtil.isNullToString(user.getId()));

        final Map<String, MultipartFile> files = multiRequest.getFileMap();
        if (!files.isEmpty()) {
            try {
                // Upload path: /{sys}/{domain}/{PREFIX_}yyyyMMddhhmmssSSS{sn}
                //  - {sys} comes from CmmProfile.getSys() (JVM -Dsys: loc/dev/prd)
                //  - the 5th parameter (directory) is the business sub-directory
                List<FileVO> result = CmmFileUtil.uploadMultipartFiles(
                        files, "{PREFIX}_", 0, "", "{domain}", cmmFileStorageService);
                String fileNo = cmmFileService.insertFiles(result);
                inVO.setFileNo(fileNo);
            } catch (Exception e) {
                throw new CmmException("E0001", "System error while uploading attachments.", e);
            }
        }

        {camelCase}Service.insert(inVO);
        return "redirect:/{urlPrefix}/list.do";
    }

    /** Update */
    @RequestMapping("/{urlPrefix}/update.do")
    public String update(
            final MultipartHttpServletRequest multiRequest,
            @Valid @ModelAttribute("{camelCase}VO") {Domain}InVO inVO,
            BindingResult bindingResult,
            ModelMap model) throws Exception {

        if (bindingResult.hasErrors()) {
            return "egovframework/com/{domain}/{Domain}Updt";
        }

        LoginVO user = (LoginVO) CmmUserDetailsHelper.getAuthenticatedUser();
        inVO.setDataChgId(user == null ? "" : CmmStringUtil.isNullToString(user.getId()));

        String fileNo = inVO.getFileNo();
        final Map<String, MultipartFile> files = multiRequest.getFileMap();
        if (!files.isEmpty()) {
            if ("".equals(fileNo) || fileNo == null) {
                List<FileVO> result = CmmFileUtil.uploadMultipartFiles(
                        files, "{PREFIX}_", 0, "", "{domain}", cmmFileStorageService);
                fileNo = cmmFileService.insertFiles(result);
                inVO.setFileNo(fileNo);
            } else {
                FileVO fvo = new FileVO();
                fvo.setFileNo(fileNo);
                int cnt = cmmFileService.getMaxFileSN(fvo);
                List<FileVO> result = CmmFileUtil.uploadMultipartFiles(
                        files, "{PREFIX}_", cnt, fileNo, "{domain}", cmmFileStorageService);
                cmmFileService.insertFiles(result);
            }
        }

        {camelCase}Service.update(inVO);
        return "redirect:/{urlPrefix}/list.do";
    }

    /** Delete */
    @RequestMapping("/{urlPrefix}/delete.do")
    public String delete(
            @ModelAttribute("{camelCase}VO") {Domain}InVO inVO) throws Exception {
        {camelCase}Service.delete(inVO);
        return "redirect:/{urlPrefix}/list.do";
    }
}
```

### SQL Mapper XML
```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
        "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="egovframework.com.{domain}.mapper.{Domain}Mapper">

    <resultMap id="{camelCase}" type="egovframework.com.{domain}.service.{Domain}OutVO">
        <!-- DB snake_case → Java camelCase mapping -->
    </resultMap>

    <select id="selectList"
            parameterType="egovframework.com.{domain}.service.{Domain}InVO"
            resultMap="{camelCase}">
        SELECT ...
          FROM {tableName}
         WHERE 1=1
        <if test="searchKeyword != null and searchKeyword != ''">
            <if test="searchCondition == '1'">
               AND ... LIKE '%' || #{searchKeyword} || '%'
            </if>
        </if>
         ORDER BY ... DESC
         LIMIT #{recordCountPerPage} OFFSET #{firstIndex}
    </select>

    <select id="selectListTotCnt"
            parameterType="egovframework.com.{domain}.service.{Domain}InVO"
            resultType="int">
        SELECT COUNT(*)
          FROM {tableName}
         WHERE 1=1
        <!-- same WHERE conditions as selectList -->
    </select>

    <select id="selectDetail" resultMap="{camelCase}">
        SELECT ...
          FROM {tableName}
         WHERE {pk_column} = #{pkField}
    </select>

    <insert id="insert"
            parameterType="egovframework.com.{domain}.service.{Domain}InVO">
        INSERT INTO {tableName} (
            {pk_column}, ...
        ) VALUES (
            nextval('{sequenceName}'), ...
        )
    </insert>

    <update id="update"
            parameterType="egovframework.com.{domain}.service.{Domain}InVO">
        UPDATE {tableName}
           SET ...,
               data_chg_id = #{dataChgId},
               data_chg_dt = NOW()
         WHERE {pk_column} = #{pkField}
    </update>

    <delete id="delete"
            parameterType="egovframework.com.{domain}.service.{Domain}InVO">
        DELETE FROM {tableName}
         WHERE {pk_column} = #{pkField}
    </delete>
</mapper>
```

### HTML patterns
- **List.html**: `layout:decorate="~{layout/default}"`, AJAX pagination
  - Search form: `<form id="searchForm">` (no `th:action`, no `method`)
  - Search button: `<button type="button" onclick="fn_search(true)">` (NOT submit)
  - Table body: `<tbody id="listBody">` — rendered dynamically by JS
  - Pagination: `<div class="pagination-wrap"></div>` — filled by commPaging
  - JS: `commPaging()` + `CommAjax` combo (see `SmpBoardList.html`, `docs/페이징가이드.md`)
- **Regist.html**: `th:object="${{camelCase}VO}"`, `th:field="*{fieldName}"`, `enctype="multipart/form-data"`
- **Updt.html**: PK as hidden, delete form separate (`id="deleteForm"`), submit after `confirm()`
- For detail-page file list, see `SmpBoardUpdt.html` in the `smp` sample

## Common Classes (`olv-core` — **DO NOT modify**, only use)

| Class | Purpose |
|-------|---------|
| `CmmVO` | Search (`searchCondition`/`searchKeyword`), pagination (`pageIndex`/`pageUnit`), audit columns |
| `CmmInVO<T>` | CommAjax request VO (auto-binds `{ body: InVO, page: {...} }`) |
| `CmmOutVO` | CommAjax response VO (`CmmOutVO.of(list, paginationInfo)`) |
| `CmmPaginationInfo` | Pagination calculator. `setupPaging(request)` for AJAX, `of(CmmVO)` for sync, `toPageMap()` to convert for commPaging response |
| `LoginVO` | Authenticated user info (`id`, `name`, `userSe`, etc.) |
| `FileVO` | File attachment (`fileNo`, `sn`, `atchFileActlNm`, etc.) |
| `CmmMessageSource` | Message catalog (`getMessage("success.common.select")`) |
| `CmmUserDetailsHelper` | Get logged-in user (`getAuthenticatedUser()`) |
| `CmmStringUtil` | String utilities (`isEmpty`, `nvl`, `escapeHtml`, `isNullToString`, …) |
| `CmmFileUtil` | File upload/validation. `uploadMultipartFiles(..., String directory, ...)` saves to `/{sys}/{directory}/{prefix+ts+sn}` (sys = `CmmProfile.getSys()`) |
| `CmmFileService` | File CRUD service |
| `CmmFileStorageService` | File storage service (S3 / local) |
| `CmmProfile` | Environment (`sys`: `loc`/`dev`/`prd`, from JVM `-Dsys`). Auto-applied to upload-path prefix |
| `CmmBizException` | Business exception (M-code, HTTP 400) |
| `CmmException` | System exception (E-code, HTTP 500) |

## Exception Handling Rules
- Business error: `throw new CmmBizException("M0001", "message");`
- System error: `throw new CmmException("E0001", "message", cause);`
- `CmmExceptionHandler` handles them globally (auto-detects JSON vs HTML)

## Forbidden Practices
- **Get user confirmation before modifying `olv-core`**.
- **No Lombok** — write getters/setters manually.
- **No DAO pattern** → use `@Mapper` interfaces.
- **No `${}` in SQL** → use `#{}` parameter binding only (prevents SQL injection).
- **No `th:utext` in HTML** → use `th:text` (prevents XSS).
- **Adding new dependencies requires explicit user confirmation**.
- **No hardcoded root-absolute URLs** (`href="/..."`, `src="/..."`, `action="/..."`) in templates → always use `th:href="@{/...}"` / `th:src="@{/...}"` / `th:action="@{/...}"`. Tomcat auto-prepends the WAR-name-based contextPath (`/lunch`, `/adlunch`, `/apilunch`).
- **Static resources (`css/js/img/fonts/ibsheet8/favicon`) MUST use the `/static/` prefix**: `@{/static/css/...}`, `@{/static/js/...}`, `@{/static/img/...}`. Spring Boot's `spring.mvc.static-path-pattern: /static/**` is aligned with the production Nginx locations (`/lunch/static/...`, `/adlunch/static/...`). Nginx serves files directly from `/app/web/{prd-olv-pfom|dev-olv-pfom|prd-olv-oper|dev-olv-oper}/{lunch|adlunch}/static/...` (per-environment StatefulSet directory, injected via `PFOM_DIR`/`OPER_DIR` build-args in `Dockerfile-web`), falling back to the WAS if a file is missing. Omitting `/static` (e.g. `@{/css/...}`) breaks the Nginx location and overloads the WAS.
- **No root-absolute `url(/...)` inside CSS** → use paths relative to the CSS file (e.g. `url(../img/...)`).
- **Do NOT use `#request`, `#session`, `#servletContext`, `#response` in Thymeleaf templates** — these were **removed in Thymeleaf 3.1+** (Spring Boot 3.x) and will fail template parsing. If you need contextPath, use `@{/}` (resolves to `/` locally, `/lunch/` in deployed envs).
- **In external `.js` files**, build URLs as `_ctx + "/api/..."`. `_ctx` is read from `<meta name="_ctx" th:content="@{/}"/>` in the layout, and `common.js` strips the trailing `/` so `_ctx = ""` or `"/lunch"`. Inside Thymeleaf inline-script blocks you can also use `[[@{/api/...}]]`.

## Sample Code Reference
For real working examples, see the `SmpBoard*` files in:
`olv-oper/src/main/java/egovframework/com/smp/`
