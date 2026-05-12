# Administrative District Combo Box (ADMDST) Guide

> 📚 **Reference / study material** — copied from the company project `saas-olv`. Teaches the Korean hierarchical administrative-district drop-down component.
>
> Shared Fragment: `templates/fragments/admdst-combo.html`
> Shared JS module: `static/js/com/admdst-combo.js`
> Shared Helper: `AdmdstHelper`

A hierarchical drop-down component for Korean administrative regions: **Province (시도) → City (시군구) → District (군구) → Town/Village (읍면동)**.

> **Korean admin-region terminology** (kept in original Korean in code and DOM ids):
> - **시도 (Sido)** — top-level province/metropolitan city (e.g. 경기도 Gyeonggi-do)
> - **시군구 (Sgg)** — city / county (e.g. 수원시 Suwon-si)
> - **군구 (Gu)** — district within a large city (e.g. 팔달구 Paldal-gu)
> - **읍면동 (Emd)** — town / township / neighborhood (e.g. 행궁동 Haenggung-dong)
> - **세종 (Sejong)** — Sejong Special Self-Governing City, a 2-depth exception

---

## 1. Basic Usage (1 combo on a page)

### 1-1. Controller

`AdmdstHelper.setModel(model)` — that's all.

```java
@Autowired
private AdmdstHelper admdstHelper;

@RequestMapping("/xxx/list.do")
public String listView(ModelMap model) throws Exception {
    admdstHelper.setModel(model);                        // default (no preset)
    // admdstHelper.setModel(model, "4113559000");       // preset by admdst_cd + lock upper levels
    // admdstHelper.setModelByMngCd(model, "MNG001");    // preset by admdst_mng_cd + lock upper levels
    return "egovframework/com/xxx/xxxList";
}
```

- `setModel(model)` → injects `admdstList` and `presetAdmdstCd=""` into the model.
- `setModel(model, presetCd)` → sets the value at the given `admdst_cd` and **locks** (disables) all select levels at and above that depth.
- `setModelByMngCd(model, admdstMngCd)` → finds the matching `admdst_mng_cd` in the full list, then applies the preset just like `setModel(model, presetCd)`.

### 1-2. HTML

Drop one Fragment line inside a `form-container`.

```html
<div class="row">
    <div class="col-12">
        <label class="form-label"><span>Region</span></label>
        <div class="form-container">
            <div th:replace="~{fragments/admdst-combo :: admdstCombo}"></div>
        </div>
    </div>
</div>
```

The Fragment automatically:

1. Renders 4 `<select class="form-select select2">` (Sido / Sgg / Gu / Emd) + 3 hidden inputs (`admdstCd`/`trimAdmdstCd`/`admdstMngCd`).
2. Injects server data (`admdstList`, `presetAdmdstCd`) into JS globals.
3. Loads `admdst-combo.js` (idempotent guard inside — safe to use the Fragment many times).
4. On `DOMContentLoaded`, calls `ADMDST.init()` and applies `presetAdmdstCd`.

### 1-3. Reading values (page-js)

```javascript
var cd      = ADMDST.get_admdst_cd();       // final selected code (10 digits)
var trimCd  = ADMDST.get_trim_admdst_cd();  // truncated code by depth (for LIKE search)
var mngCd   = ADMDST.get_admdst_mng_cd();   // admin-region management code (admdst_mng_cd)
var depth   = ADMDST.get_selected_depth();  // 0~4
var info    = ADMDST.get_selected_info();   // full state object
```

| Method | Returns |
|--------|---------|
| `get_admdst_cd()` | The 10-digit `admdstCd` of the deepest selected level |
| `get_trim_admdst_cd()` | Depth-truncated code — **for DB LIKE searches**<br>Sido (2) / Sgg (4 or 5) / Gu (5) / Emd (10) |
| `get_admdst_mng_cd()` | `admdstMngCd` of the selected row (unique). `''` when nothing selected |
| `get_selected_depth()` | `0` (none) / `1` (Sido) / `2` (Sgg) / `3` (Gu) / `4` (Emd) |
| `get_selected_info()` | `{ admdstCd, trimAdmdstCd, admdstMngCd, depth, ctpvCd, sggCd, guCd, emdCd, admdstNm }` |

---

## 2. Preset (initial value)

### 2-1. Server-side (specified in Controller)

When you need to **prefill + lock the upper levels** (e.g. by logged-in user region, edit page existing value):

```java
// Pass the full 10-digit admin-region code
admdstHelper.setModel(model, "4113559000"); // Gyeonggi-do > Suwon-si > Paldal-gu > Haenggung-dong
```

Behavior:
- Each select up to the matching depth is auto-selected.
- All upper-depth selects are **disabled** (the user cannot change them).
- Lower depths remain enabled.

| Preset depth | Locked | User can change |
|-----|-----|-----|
| 1 (Sido) | Sido | Sgg / Gu / Emd |
| 2 (Sgg) | Sido, Sgg | Gu / Emd |
| 3 (Gu) | Sido, Sgg, Gu | Emd |
| 4 (Emd) | All | Nothing |

### 2-2. Client-side runtime

When you need to set/change values dynamically from page JS, **always call inside `ADMDST.onReady()`** (so init is guaranteed to be done).

```javascript
$(document).ready(function() {

    ADMDST.onReady(function() {

        // (a) Plain set — user can still change it freely
        ADMDST.set_admdst_cd('4113559000');

        // (b) Set + lock upper levels (same effect as Controller's presetAdmdstCd)
        ADMDST.set_admdst_cd('4113500000', { lock: true });

        // (c) Unlock only (keeps the values)
        ADMDST.unlock();

        // (d) Full reset (clears values and locks)
        ADMDST.clear();
    });

});
```

Why `onReady()`?
- The Fragment calls `ADMDST.init()` on `DOMContentLoaded`.
- Page JS's `$(document).ready()` may run before that.
- `onReady` defers execution until init is complete — timing-safe.

---

## 3. Multiple Combos on the Same Page

The Fragment takes a **prefix parameter**, so you can place multiple sets on the same page by repeating the Fragment call — no manual HTML required.

### 3-1. HTML — just pass a prefix

```html
<!-- Origin -->
<div class="form-container">
    <div th:replace="~{fragments/admdst-combo :: admdstCombo('from')}"></div>
</div>

<!-- Destination -->
<div class="form-container">
    <div th:replace="~{fragments/admdst-combo :: admdstCombo('to')}"></div>
</div>
```

Auto-generated DOM ids / hidden-input names (isolated per prefix):

| Prefix | Select ids | Hidden id / name |
|--------|------------|------------------|
| (none, default) | `sido`, `sgg`, `gu`, `emd` | `admdstCd`, `trimAdmdstCd`, `admdstMngCd` |
| `from` | `fromSido`, `fromSgg`, `fromGu`, `fromEmd` | `fromAdmdstCd`, `fromTrimAdmdstCd`, `fromAdmdstMngCd` |
| `to`   | `toSido`,   `toSgg`,   `toGu`,   `toEmd`   | `toAdmdstCd`,   `toTrimAdmdstCd`,   `toAdmdstMngCd`   |

> Without a prefix, ids are used as-is. `<div th:replace="... :: admdstCombo">` is the default single-combo case.

### 3-2. JS — instances auto-exposed

The Fragment creates an instance on `DOMContentLoaded` and exposes it as `window.ADMDST_{prefix}`.

```javascript
$(document).ready(function() {

    // No onReady for additional instances; use setTimeout 0 or wait for DOMContentLoaded
    setTimeout(function() {

        // Set initial value (per instance)
        window.ADMDST_from.set_admdst_cd('1156000000', { lock: true });

        // Read values
        var fromCd = window.ADMDST_from.get_admdst_cd();
        var toCd   = window.ADMDST_to.get_admdst_cd();

        // Save to a variable if you want IDE autocomplete for `onComplete`-style callbacks
        var comboTo = window.ADMDST_to;
        comboTo.set_admdst_cd('4111000000');

    }, 0);
});
```

### 3-3. Default instance (no prefix) vs additional instances

| Type | How to invoke | API | Auto-applies preset |
|------|---------------|-----|---------------------|
| Default (no prefix) | `admdstCombo` | `ADMDST.xxx()` static proxy + `ADMDST.onReady(...)` | ✓ Auto from Controller's `presetAdmdstCd` |
| Additional (with prefix) | `admdstCombo('from')` etc. | `window.ADMDST_from.xxx()` | ✗ Call `set_admdst_cd` from page JS |

> You can mix 1 default + N additional instances. E.g. search section uses `admdstCombo` (default), registration form uses `admdstCombo('reg')`.

### 3-4. Preset for additional instances (Controller side)

The Helper only auto-applies preset to the **default instance**.
Pass additional instances' initial values as separate model attributes and apply them in page JS:

```java
// Controller
admdstHelper.setModel(model, "4113559000");          // default-instance preset (auto)
model.addAttribute("fromInitCd", "1156000000");      // separate keys for additional instances
model.addAttribute("toInitCd",   "2611010100");
```

```html
<!-- page JS -->
<script th:inline="javascript">
    /*<![CDATA[*/
    var FROM_INIT_CD = /*[[${fromInitCd}]]*/ '';
    var TO_INIT_CD   = /*[[${toInitCd}]]*/   '';
    document.addEventListener('DOMContentLoaded', function() {
        if (FROM_INIT_CD) window.ADMDST_from.set_admdst_cd(FROM_INIT_CD, { lock: true });
        if (TO_INIT_CD)   window.ADMDST_to  .set_admdst_cd(TO_INIT_CD,   { lock: true });
    });
    /*]]>*/
</script>
```

---

## 4. `ADMDST.create()` — Advanced / Direct API

The Fragment-parameter approach is the standard. When you need to skip the Fragment and build HTML/JS fully manually (e.g. dynamically inside a modal), call `ADMDST.create()` directly.

```javascript
var combo = ADMDST.create({
    // required
    data           : SERVER_ADMDST_LIST,

    // DOM id mapping (defaults = plain ids without prefix)
    sidoId         : 'mySido',
    sggId          : 'mySgg',
    guId           : 'myGu',
    guAreaId       : 'myGuArea',          // wrapper to toggle when hiding Gu (optional)
    emdId          : 'myEmd',
    hiddenCdId     : 'myAdmdstCd',
    hiddenTrimId   : 'myTrimAdmdstCd',

    // initial value
    presetAdmdstCd : '',                  // select + lock upper levels (empty = no selection)

    // events
    onComplete     : function(info) {}    // fires when Emd is finally selected
});

// Return value = instance API object
combo.set_admdst_cd('4113559000');
```

> The Fragment parameter (`admdstCombo('my')`) calls all of this for you.
> Use direct `create()` only when you must.

---

## 5. Values Sent on Form Submit

The Fragment automatically includes these hidden inputs (per prefix):

| Prefix | Hidden name | Value |
|--------|-------------|-------|
| (none) | `admdstCd`, `trimAdmdstCd`, `admdstMngCd` | final code / truncated code / management code |
| `from` | `fromAdmdstCd`, `fromTrimAdmdstCd`, `fromAdmdstMngCd` | same |
| `to`   | `toAdmdstCd`,   `toTrimAdmdstCd`,   `toAdmdstMngCd`   | same |

Make your server-side InVO field names match the hidden names. The default case binds directly to `admdstCd`, `trimAdmdstCd`.

SQL Mapper examples:

```xml
<!-- Exact match -->
WHERE admdst_cd = #{admdstCd}

<!-- All sub-regions under the selection (LIKE search) -->
WHERE admdst_cd LIKE #{trimAdmdstCd} || '%'
```

`trimAdmdstCd` length by depth:

| Selected depth | Example | Truncated code |
|----------------|---------|----------------|
| Sido (1) | Gyeonggi-do | `41` |
| Sgg (2) — has Gu | Suwon-si | `4111` |
| Sgg (2) — no Gu | Gwangmyeong-si | `41070` |
| Gu (3) | Paldal-gu | `41113` |
| Emd (4) | Haenggung-dong | `4111358500` (full 10 digits) |

---

## 6. Gu (district) Auto-Hide Rule

**By default all 4 depths are visible.** Only when Sgg is explicitly selected does the component check whether Gu data exists, and hide it accordingly.

| State | Sido | Sgg | Gu | Emd |
|-------|------|-----|----|----|
| Initial load | ● | ● | **●** | ● |
| Only Sido selected | ● | ● | **●** | ● |
| Sgg selected (has Gu, e.g. Suwon-si) | ● | ● | ● | ● |
| Sgg selected (no Gu, e.g. Gwangmyeong-si) | ● | ● | **hidden** | ● (filled directly) |
| Sgg deselected (empty option) | ● | ● | **●** | ● |

- If the preset value is at depth 3 (Gu) or deeper, Gu is auto-shown and the value is set.
- When Sgg has no Gu, `get_admdst_cd()` returns the Sgg-level code; selecting Emd returns the Emd-level code.

---

## 7. Sejong Special Case (skips to 2-depth)

Sejong is technically 3-depth in the source data (Sido + Sgg + Emd), but its Sgg level has only 1 row — meaningless to show to users. So the UI shows only **Sido + Emd** (2 combos), and the Sgg code is auto-filled internally.

| State | Sido | Sgg | Gu | Emd |
|-------|------|-----|----|----|
| Initial load | ● | ● | ● | ● |
| Sejong selected | ● | **hidden** | **hidden** | ● (Sejong Emds auto-filled) |
| Sejong → Emd selected | ● | hidden | hidden | ● |
| Sejong deselected | ● | ● | ● | ● |

### Detection (`_isFlatSido`)
- `_sggMap[ctpvCd].length === 1` (Sgg has only 1 row, like Sejong) → auto-select that single Sgg.
- `_sggMap[ctpvCd].length === 0` AND `dongOrdNo=4` data exists (theoretical case).

### Return-value behavior
| User selection | `get_admdst_cd()` | `get_trim_admdst_cd()` |
|----------------|-------------------|------------------------|
| Sejong Sido only | Sejong Sgg (2-depth) admdstCd, e.g. `"3611000000"` | `"36110"` (5 digits) |
| Sejong + Emd | Emd admdstCd, e.g. `"3611010500"` | `"3611010500"` (10 digits) |

> **Key point**: Even when only Sido is selected, `_selected.sgg` is internally filled, so `LIKE trimAdmdstCd || '%'` SQL searches cover all of Sejong correctly.

### Preset
- Preset works with any of: Sejong Sido code (`"3600000000"`), Sgg code (`"3611000000"`), or Emd code (`"3611010500"`).
- With `lock: true`, the Sido combo is locked; if you preset all the way to Emd, the Emd combo is also locked.

---

## 8. Troubleshooting

| Symptom | Cause / Fix |
|---------|-------------|
| Sgg dropdown stays empty after Sido selection | select2 didn't pick up the option change. The module auto-handles `destroy → re-init`. If it recurs, manually call `$('#sgg').select2('destroy').select2({...})` |
| `change` event on Sido not caught in page JS | select2 uses `$el.trigger('change')` → bind with **jQuery `$('#sido').on('change', fn)`** (plain `addEventListener` won't catch it) |
| `set_admdst_cd()` does nothing | Called before `ADMDST.init()` → run inside **`ADMDST.onReady(function() { ... })`** (default instance only; additional instances need `setTimeout(..., 0)` or `DOMContentLoaded`) |
| Values get mixed between multiple combos | Stop using static `ADMDST.xxx()`; use the per-prefix `window.ADMDST_from.xxx()` / `window.ADMDST_to.xxx()` |
| `admdstList` empty when Fragment renders | Controller forgot to call `admdstHelper.setModel(model)` |
| `window.ADMDST_from` is `undefined` | Prefix typo in the Fragment call, or accessed before `DOMContentLoaded` |
| Same prefix Fragment used twice in the same form | DOM id collision — second instance overwrites the first. **Prefix must be unique per page** |

---

## 9. Reference Files

### olv-oper

| Path | Role |
|------|------|
| `olv-oper/src/main/java/egovframework/com/cmm/web/AdmdstHelper.java` | Shared Helper used by Controllers (`setModel`, `setModelByMngCd`) |
| `olv-oper/src/main/java/egovframework/com/cmm/service/AdmdstService.java` | Admin-region query service |
| `olv-oper/src/main/java/egovframework/com/cmm/service/AdmdstOutVO.java` | Admin-region OutVO (includes `admdstMngCd`) |
| `olv-oper/src/main/java/egovframework/com/cmm/mapper/AdmdstMapper.java` | Admin-region Mapper interface |
| `olv-oper/src/main/resources/egovframework/mapper/com/cmm/admdst/Admdst_SQL.xml` | Admin-region SQL (includes `admdst_mng_cd`) |
| `olv-oper/src/main/resources/templates/fragments/admdst-combo.html` | Shared Thymeleaf Fragment |
| `olv-oper/src/main/resources/static/js/com/admdst-combo.js` | Shared JS module (ADMDST, including `get_admdst_mng_cd`) |

### olv-pfom

| Path | Role |
|------|------|
| `olv-pfom/src/main/java/egovframework/com/cmm/admdst/web/AdmdstHelper.java` | same |
| `olv-pfom/src/main/java/egovframework/com/cmm/admdst/service/AdmdstOutVO.java` | same |
| `olv-pfom/src/main/resources/egovframework/mapper/com/cmm/admdst/Admdst_SQL.xml` | same |
| `olv-pfom/src/main/resources/templates/fragments/admdst-combo.html` | same |
| `olv-pfom/src/main/resources/static/js/admdst-combo.js` | same |
