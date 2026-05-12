# saas-olv Reference Materials (Study Use)

> 📚 A collection of development guides copied from the Korean company SaaS project (`saas-olv`).
> Kept here in `saas-practices` for studying the eGovFramework / MyBatis / Thymeleaf patterns.
>
> 👉 For how the saas-olv class names map to **this** project, see [`../../CLAUDE.md`](../../CLAUDE.md) → "Local Mapping" section.

## Original Project Tech Stack

| Area | Detail |
|------|--------|
| Framework | Spring Boot 3.3.5 + eGovFramework 5.0.0 |
| Java | 21 (**Lombok is NOT used**) |
| Build | Gradle multi-module |
| DB | PostgreSQL + MyBatis (Mapper interface + XML) |
| Templates | Thymeleaf + Layout Dialect |
| Auth | Spring Security 6.x + Identity Verification (OmniOne CX) |

## Module Structure (`saas-olv`)

| Module | Role | Port |
|--------|------|------|
| `olv-core` | Common library (VO, utils, config) | - |
| `olv-oper` | Admin web (CRUD screens) | 8081 |
| `olv-pfom` | User portal | 8080 |
| `olv-api`  | REST API | - |

## Guide Files

| File | Contents | Priority |
|------|----------|----------|
| **[`../../CLAUDE.md`](../../CLAUDE.md)** | Project overview + 7 code patterns (InVO/OutVO/Mapper/Service/Controller/SQL XML) + naming + forbidden practices | ⭐⭐⭐ Must read |
| **[`../../GUIDE.md`](../../GUIDE.md)** | End-to-end flow of building one screen (smp1~smp10) + step-by-step request flow + new-screen checklist | ⭐⭐⭐ Must read |
| [paging-guide.md](paging-guide.md) | CommAjax + commPaging AJAX pagination pattern | ⭐⭐ |
| [identity-verification-guide.md](identity-verification-guide.md) | OmniOne CX identity verification (CI/DI) Fragment usage | ⭐ |
| [admin-district-combo-guide.md](admin-district-combo-guide.md) | Province → City → District → Town hierarchical combo box | ⭐ |

## Recommended Learning Order

1. **Read `CLAUDE.md` end-to-end** — get the project structure, naming rules, and forbidden practices.
2. **Read `GUIDE.md` end-to-end** — understand the full flow of building one screen (smp1~smp10).
3. **Hand-write the 7 code patterns from `CLAUDE.md`** (InVO / OutVO / Mapper / Service / ServiceImpl / Controller / SQL XML).
4. **Paging guide** — the foundation of every list screen; must master.
5. **Identity-verification / admin-district combo** — when needed (Korean-SaaS-specific features).

## Core Rules to Memorize

| Rule | Reason |
|------|--------|
| Do NOT use Lombok → write getters/setters by hand | Company standard |
| Do NOT use `${}` in SQL → only `#{}` | Prevents SQL injection |
| Do NOT use `th:utext` in HTML → only `th:text` | Prevents XSS |
| Do NOT use DAO pattern → use `@Mapper` interface | MyBatis standard |
| Do NOT hardcode URLs → `th:href="@{/...}"` | Auto-prepends contextPath |
| Static resources MUST use `@{/static/...}` prefix | Matches Nginx routing |
| Do NOT modify `olv-core` (only use it) | Wide impact |
| Do NOT add new dependencies (confirm first) | Security / licensing review |

## CRUD 9-Step Recipe (Memorize)

One screen = 8–10 files. Build **bottom-up** so there are no compile errors:

1. Check DDL (table / sequence / PK)
2. **InVO** (`extends CmmVO`)
3. **OutVO** (`implements Serializable`)
4. **SQL Mapper XML** (resultMap + CRUD queries, `LIMIT/OFFSET` pagination)
5. **Mapper interface** (`@Mapper`)
6. **Service interface**
7. **ServiceImpl** (`@Service("camelCaseService")`)
8. **Controller** (`@Controller`, URL `/{domain}/action.do`)
9. **HTML** (List / Regist / Updt)

## Sample Code Location (in the original project)

> When in doubt, look at and copy this package:
> `olv-oper/src/main/java/egovframework/com/smp/` — 7 `SmpBoard*` files
> + `olv-oper/src/main/resources/templates/egovframework/com/smp/` — 3 HTMLs
> + `olv-oper/src/main/resources/egovframework/mapper/com/smp/SmpBoard_SQL.xml`
