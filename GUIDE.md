# Screen Development Sample Guide

> 📚 **Reference / study material** — copied from the company project `saas-olv`, kept here in the practice project for learning the Korean eGovFramework-based SaaS standard patterns.
>
> This package shows **"how to build one screen end-to-end, from start to finish."**
> In real work, you copy this package and just rename the domain.

---

## 0. Local Setup (Eclipse IDE)

This project is a **Gradle multi-module project** (`olv-root`, `olv-core`, `olv-pfom`, `olv-oper`, `olv-api`) built on **Java 21**, **Spring Boot 3.3.5**, and **eGovFramework 5.0.0**.
If you import it the usual way (`Existing Projects into Workspace`) the libraries will not be recognized correctly, so **you MUST import it as a Gradle project following the steps below.**

### Prerequisites
1. **Install JDK 21** on your local PC.
2. **IDE version** — for proper Java 21 support, use **Eclipse 2023-12 or later**, or the **latest STS4 (Spring Tools 4)**.
3. **Eclipse settings (Window → Preferences)**
   - `Java > Installed JREs`: confirm that JDK 21 is added and checked.
   - `Java > Compiler`: confirm **Compiler compliance level = `21`**.

### Import Procedure
1. Eclipse menu → **File → Import...**
2. Choose **Gradle → Existing Gradle Project** → `Next`
3. **Project root directory** → click `Browse...` and select the `olv-root` top-level folder (e.g. `d:\olv\workspace-egov\olv-root`) → `Next`
4. On the **Import Options** screen:
   - **Gradle distribution**: check `Override workspace settings`, then set **Specific Gradle version** to the version that matches the project (mismatched versions cause import failures).
   - **Advanced Options**: verify `Java home` points to your local JDK 21.
5. When the project model finishes loading and all 4 sub-modules show up correctly under the root, click **`Finish`**.

### Troubleshooting
> If after import you see "package/class not found" or `Unsupported class file major version 65`:

1. Go to **Window → Preferences → Gradle**.
2. Set **Java home** to the correct JDK 21 path → `Apply and Close`.
3. Right-click `olv-root` → **Gradle → Refresh Gradle Project** to re-download dependencies.

---

## 1. File Build Order & List

| Step | File | Location | Description |
|:----:|------|----------|-------------|
| smp1 | `SmpBoardInVO.java` | `service/` | Input-parameter VO (extends `CmmVO`) |
| smp2 | `SmpBoardOutVO.java` | `service/` | Query-result VO (extends `CmmVO`) |
| smp3 | `SmpBoardList.html` | `templates/.../smp/` | List page |
| smp4 | `SmpBoardRegist.html` | `templates/.../smp/` | Register page |
| smp5 | `SmpBoardUpdt.html` | `templates/.../smp/` | Update / detail page |
| smp6 | `SmpBoardController.java` | `web/` | Receives HTTP → calls Service → returns View |
| smp7 | `SmpBoardService.java` | `service/` | Business-logic contract (interface) |
| smp8 | `SmpBoardServiceImpl.java` | `service/impl/` | Actual implementation |
| smp9 | `SmpBoardMapper.java` | `mapper/` | MyBatis Mapper interface (`@Mapper`) |
| smp10 | `SmpBoard_SQL.xml` | `mapper/.../smp/` | Actual SQL definitions |

---

## 2. Request-Flow Walkthrough — "List Query" Example

When a user requests `/smpBoard/list.do` in the browser, here is the step-by-step path the data travels until it appears on screen.

---

### STEP 1. Browser → Controller `[smp3 → smp6]`

**[smp3]** In `SmpBoardList.html`, the user clicks the search button:

```html
<form th:action="@{/smpBoard/list.do}" method="get">
  <select name="searchCondition">...</select>
  <input name="searchKeyword" .../>
  <button type="submit">Search</button>
</form>
```

Browser sends the request:
```
GET /smpBoard/list.do?searchCondition=1&searchKeyword=title
```

**[smp6]** The matching `SmpBoardController` method is invoked:

```java
@RequestMapping("/smpBoard/list.do")
public String selectList(
    @ModelAttribute("searchVO") SmpBoardInVO inVO,  // ← params auto-bound
    ModelMap model) throws Exception {
```

> **What is `@ModelAttribute`?**
> It auto-maps HTTP-request parameters (`name=value`) to setters on the VO.
> `name="searchKeyword"` → calls `inVO.setSearchKeyword("title")` automatically.

---

### STEP 2. Controller → Service interface `[smp6 → smp7]`

The Controller calls the **Service interface** type — it never sees the implementation → **loose coupling**.

```java
// Inside Controller
@Autowired
private SmpBoardService smpBoardService;   // ← interface type!

// Call
List<SmpBoardOutVO> resultList = smpBoardService.selectList(inVO);
```

**[smp7]** `SmpBoardService.java` (interface):

```java
public interface SmpBoardService {
    List<SmpBoardOutVO> selectList(SmpBoardInVO inVO) throws Exception;
    int selectListTotCnt(SmpBoardInVO inVO) throws Exception;
    SmpBoardOutVO selectDetail(SmpBoardInVO inVO) throws Exception;
    void insert(SmpBoardInVO inVO) throws Exception;
    void update(SmpBoardInVO inVO) throws Exception;
    void delete(SmpBoardInVO inVO) throws Exception;
}
```

> **Why have an interface?**
> - Controller only needs to know **"what can be done" (the contract)**.
> - **"How it's done" (the implementation)** is `ServiceImpl`'s job.
> - You can swap implementations without touching Controller code.
> - Spring wires the implementation in at `@Autowired` time.

---

### STEP 3. Service interface → ServiceImpl `[smp7 → smp8]`

Spring **automatically routes** the interface call to the implementation.
(Developers never call `new` themselves!)

#### Wiring principle

```
@Service("smpBoardService")              ← ① bean registered under this name
public class SmpBoardServiceImpl
    implements SmpBoardService           ← ② declares which interface it implements

    ↓ At server start the Spring container automatically:

Injects a SmpBoardServiceImpl instance into the Controller's
@Autowired SmpBoardService smpBoardService  field
```

**[smp8]** `SmpBoardServiceImpl.selectList()` runs:

```java
@Service("smpBoardService")
public class SmpBoardServiceImpl implements SmpBoardService {

    @Autowired
    private SmpBoardMapper smpBoardMapper;    // ← Mapper interface injected

    @Override
    public List<SmpBoardOutVO> selectList(
            SmpBoardInVO inVO) throws Exception {

        // ★ Put any business logic here:
        //   permission checks, data shaping, combining multiple mappers,
        //   transaction handling, etc.

        return smpBoardMapper.selectList(inVO);    // → calls Mapper
    }
}
```

---

### STEP 4. ServiceImpl → Mapper interface `[smp8 → smp9]`

`ServiceImpl` calls the **Mapper interface**.
A Mapper interface is just an interface annotated with `@Mapper`;
**MyBatis auto-generates the implementation** — you never write one yourself.

**[smp9]** `SmpBoardMapper.java`:

```java
@Mapper
public interface SmpBoardMapper {
    List<SmpBoardOutVO> selectList(SmpBoardInVO inVO) throws Exception;
    int selectListTotCnt(SmpBoardInVO inVO) throws Exception;
    SmpBoardOutVO selectDetail(SmpBoardInVO inVO) throws Exception;
    void insert(SmpBoardInVO inVO) throws Exception;
    void update(SmpBoardInVO inVO) throws Exception;
    void delete(SmpBoardInVO inVO) throws Exception;
}
```

> **Benefits of the Mapper-interface style:**
> - Just put `@Mapper` and MyBatis **auto-generates the implementation**.
> - The SQL-XML `namespace` must match the Mapper's **FQCN** (fully qualified class name).
> - Method names auto-match the XML `id`s.
> - No need to call `SqlSession` manually like in the old DAO style.

---

### STEP 5. Mapper interface → SQL Mapper XML `[smp9 → smp10]`

MyBatis looks up the query whose **namespace + id matches** and runs it.

**[smp10]** `SmpBoard_SQL.xml`:

```xml
<mapper namespace="egovframework.com.smp.mapper.SmpBoardMapper">  <!-- Mapper FQCN -->

  <select id="selectList"                  <!-- matches Mapper method name -->
    parameterType="egovframework.com.smp.service.SmpBoardInVO"
    resultMap="smpBoard">

    SELECT board_sn, board_title, use_yn, ...
      FROM co_smp_board_m
     WHERE 1=1
    <if test="searchKeyword != null and searchKeyword != ''">
      <if test="searchCondition == '1'">
        AND board_title LIKE '%' || #{searchKeyword} || '%'
      </if>
    </if>
     ORDER BY board_sn DESC
     LIMIT #{recordCountPerPage} OFFSET #{firstIndex}

  </select>
</mapper>
```

> **How the mapping works:**
> - `namespace="egovframework.com.smp.mapper.SmpBoardMapper"` → matches the Mapper interface FQCN.
> - `id="selectList"` → matches the Mapper method `selectList()`.
> - `#{fieldName}` → calls the **getter** on the parameter VO (InVO):
>   - `#{searchKeyword}` → `inVO.getSearchKeyword()`
>   - `#{firstIndex}` → `inVO.getFirstIndex()`
> - `resultMap` → converts DB columns (snake_case) to OutVO fields (camelCase):
>   - `board_title` → `boardTitle`
>   - `data_reg_dt` → `dataRegDt`

---

### STEP 6. DB → Returns (reverse path) `[smp10 → smp8 → smp6]`

The DB result travels back up **in reverse**:

```
DB result (ResultSet)
    │
    ▼  resultMap converts columns → OutVO fields
[smp10]  List<SmpBoardOutVO>  (MyBatis builds this automatically)
    │
    ▼  Mapper interface returns it via MyBatis
[smp9]   (MyBatis-generated impl returns the list)
    │
    ▼  ServiceImpl returns it as-is (or transforms here if needed)
[smp8]   return smpBoardMapper.selectList(inVO);
    │
    ▼  Controller puts it on the Model
[smp6]   model.addAttribute("resultList", resultList);
         model.addAttribute("paginationInfo", paginationInfo);
         return "egovframework/com/smp/SmpBoardList";
                 └─ Thymeleaf renders this HTML path
```

---

### STEP 7. Controller → HTML rendering `[smp6 → smp3]`

The string the Controller returns → **Thymeleaf template path**.
Data put on the Model → accessed in HTML via `${variableName}`.

**[smp3]** Results rendered in `SmpBoardList.html`:

```html
<tr th:each="item, stat : ${resultList}">
  <td th:text="${...rowNumberCalc...}">1</td>
  <td>
    <a th:href="@{/smpBoard/detail.do(boardSn=${item.boardSn})}"
       th:text="${item.boardTitle}">Title</a>
  </td>
  <td th:text="${item.dataRegDt}">2026-01-01</td>
</tr>
```

→ The completed HTML is shown in the browser.

---

## 3. Full Flow at a Glance

```
Browser        Controller       Service(I/F)     ServiceImpl       Mapper(I/F)      SQL XML          DB
  │              │                 │                │               │                │              │
  │  GET request │                 │                │               │                │              │
  │─────────────→│                 │                │               │                │              │
  │              │  selectList()   │                │               │                │              │
  │              │────────────────→│                │               │                │              │
  │              │                 │  (Spring      │               │                │              │
  │              │                 │   wires impl) │               │                │              │
  │              │                 │───────────────→│               │                │              │
  │              │                 │                │ selectList()  │                │              │
  │              │                 │                │──────────────→│                │              │
  │              │                 │                │               │  selectList    │              │
  │              │                 │                │               │───────────────→│              │
  │              │                 │                │               │                │  SELECT ...  │
  │              │                 │                │               │                │─────────────→│
  │              │                 │                │               │                │              │
  │              │                 │                │               │                │  ResultSet   │
  │              │                 │                │               │   List<VO>     │←─────────────│
  │              │                 │                │  List<VO>     │←───────────────│              │
  │              │                 │   List<VO>     │←──────────────│                │              │
  │              │   List<VO>      │←───────────────│               │                │              │
  │              │←────────────────│                │               │                │              │
  │   HTML       │                 │                │               │                │              │
  │←─────────────│                 │                │               │                │              │
```

---

## 4. Dependency Injection (DI) — What Spring Wires for You

| Declared at (annotation) | Injected at (`@Autowired`) |
|--------------------------|----------------------------|
| `@Service("smpBoardService")` <br> SmpBoardServiceImpl | Controller's `SmpBoardService` field <br> (declared as interface type) |
| `@Mapper` <br> SmpBoardMapper | ServiceImpl's `SmpBoardMapper` field <br> (MyBatis generates impl, registers as bean) |
| `@Controller` <br> SmpBoardController | Spring MVC registers URL mappings automatically |

> **Developers never use the `new` keyword!**
> Spring's container creates and wires all beans at server startup.

---

## 5. File Location Rules

### Java sources

```
olv-oper/src/main/java/egovframework/com/{domain}/
├── web/
│   └── {Domain}Controller.java       @Controller
├── mapper/
│   └── {Domain}Mapper.java           @Mapper (interface)
├── service/
│   ├── {Domain}InVO.java             extends CmmVO (input)
│   ├── {Domain}OutVO.java            extends CmmVO (query result)
│   └── {Domain}Service.java          interface
└── service/impl/
    └── {Domain}ServiceImpl.java      @Service
```

### HTML templates

```
olv-oper/src/main/resources/templates/egovframework/com/{domain}/
├── {Domain}List.html         List
├── {Domain}Regist.html       Register
└── {Domain}Updt.html         Update / detail
```

### SQL Mapper

```
olv-oper/src/main/resources/egovframework/mapper/com/{domain}/
└── {Domain}_SQL.xml
    namespace = Mapper interface FQCN
    (e.g. egovframework.com.smp.mapper.SmpBoardMapper)
```

---

## 6. New-Screen Checklist

> Follow this order so each layer can be imported by the layer above without compile errors.

- [ ] **1. Check DDL** — table, sequence, PK structure
- [ ] **2. Write InVO** — extends `CmmVO`, holds input parameters (search conditions / form values) → see `[smp1]`
- [ ] **3. Write OutVO** — extends `CmmVO`, holds query results → see `[smp2]`
- [ ] **4. Write SQL Mapper XML** — namespace (Mapper FQCN), resultMap, CRUD queries → see `[smp10]`
- [ ] **5. Write Mapper interface** — `@Mapper`, declare CRUD methods → see `[smp9]`
- [ ] **6. Write Service interface** — declare CRUD methods → see `[smp7]`
- [ ] **7. Write ServiceImpl** — `@Service`, call Mapper → see `[smp8]`
- [ ] **8. Write Controller** — `@Controller`, URL mappings, call Service → see `[smp6]`
- [ ] **9. Write HTML pages** — List / Register / Update → see `[smp3~5]`
- [ ] **10. Compile & boot-test the server**

---

## 7. Sample Table DDL (reference)

```sql
CREATE SEQUENCE seq_co_smp_board_m START WITH 1 INCREMENT BY 1;

CREATE TABLE co_smp_board_m (
    board_sn       BIGINT         NOT NULL DEFAULT nextval('seq_co_smp_board_m'),
    board_title    VARCHAR(200)   NOT NULL,
    board_cn       TEXT,
    use_yn         CHAR(1)        DEFAULT 'Y',
    data_reg_id    VARCHAR(20),
    data_reg_dt    TIMESTAMP      DEFAULT NOW(),
    data_chg_id    VARCHAR(20),
    data_chg_dt    TIMESTAMP,
    CONSTRAINT pk_co_smp_board_m PRIMARY KEY (board_sn)
);
```
