# OmniOne CX Identity Verification (OMNIONE) Guide

> 📚 **Reference / study material** — copied from the company project `saas-olv`. Teaches the Korean standard identity-verification (CI/DI) pattern.
>
> Shared Fragment: `templates/fragments/omnione-auth.html`
> Shared JS module: `static/js/com/omnione-auth.js`
> Shared Helper: `OmniOneAuthHelper`

A reusable identity-verification component built on the OmniOne CX standard auth window.
On a screen, you attach the identity-verification button with **one Fragment line + one callback function**.

> **Note on CI/DI**: In Korean identity verification, **CI** (Connecting Information) is a permanent national identifier hash, and **DI** (Duplicated Information) prevents duplicate sign-ups for the same service. Both are highly sensitive PII.

---

## 1. Basic Usage (simplest case)

### 1-1. HTML — one Fragment line + a callback

```html
<form id="joinForm" th:action="@{/member/join.do}" method="post">
    <input type="text"   id="name"    name="name"    readonly />
    <input type="text"   id="birth"   name="birth"   readonly />
    <input type="text"   id="phone"   name="phone"   readonly />
    <input type="hidden" id="certKey" name="certKey" />

    <!-- ★ identity-verification button: a single Fragment line -->
    <div th:replace="~{fragments/omnione-auth :: authButton(onSuccess='fn_onCertOk')}"></div>

    <button type="submit" class="btn btn-primary">Sign up</button>
</form>

<script th:inline="javascript">
/*<![CDATA[*/
function fn_onCertOk(info) {
    // info = { certKey, name, birth, phone }
    document.getElementById('name').value    = info.name;
    document.getElementById('birth').value   = info.birth;
    document.getElementById('phone').value   = info.phone;
    document.getElementById('certKey').value = info.certKey;
}
/*]]>*/
</script>
```

The Fragment automatically handles:

1. Renders one button (`<button type="button">Identity Verification</button>`).
2. Loads `omnione-auth.js` (idempotent guard inside, so it's safe to use the Fragment many times).
3. On `DOMContentLoaded`, calls `OMNIONE.bind(buttonId, { onSuccess, onFail })`.
4. Registers a `postMessage` listener (to receive messages from `result.html` to the parent window).

### 1-2. Subsequent Controller — retrieve CI/DI via certKey

In the Controller that receives the form submit after verification, retrieve CI/DI with `OmniOneAuthHelper.consume()`.

```java
@Autowired
private OmniOneAuthHelper omniOneAuthHelper;

@PostMapping("/member/join.do")
public String join(@RequestParam("certKey") String certKey,
                   @ModelAttribute("memberVO") MemberInVO vo,
                   HttpSession session) throws Exception {

    OmniOneIdentityResult cert = omniOneAuthHelper.consume(session, certKey);
    if (cert == null) {
        throw new CmmBizException("M0001", "Identity verification expired. Please try again.");
    }

    vo.setName(cert.getName());
    vo.setBirth(cert.getBirth());
    vo.setPhone(cert.getPhone());
    vo.setCi(cert.getCi());
    vo.setDi(cert.getDi());
    memberService.insert(vo);

    return "redirect:/member/joinComplete.do";
}
```

> `consume()` is **single-use** — calling it twice with the same `certKey` returns `null` on the second call (prevents reuse).

---

## 2. Fragment Parameters

```html
<div th:replace="~{fragments/omnione-auth :: authButton(
    onSuccess='fn_onCertOk',
    onFail='fn_onCertFail',
    label='Verify Identity',
    cssClass='btn btn-secondary'
)}"></div>
```

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `onSuccess` | String (JS function name) | — (required) | Global function name to call on success |
| `onFail` | String (JS function name) | `null` | Global function for failure/cancel. Default `alert` if omitted |
| `label` | String | `'Identity Verification'` | Button text |
| `cssClass` | String | `'btn btn-primary'` | Button CSS class |

---

## 3. Callback Parameters (`info` object)

`info` passed to the `onSuccess` callback:

| Field | Type | Description |
|-------|------|-------------|
| `certKey` | String | UUID. Pass to `consume(session, certKey)` later to retrieve CI/DI |
| `name` | String | Full name |
| `birth` | String | Date of birth (YYYYMMDD) |
| `phone` | String | Mobile phone number |

> CI/DI are NEVER sent to the client. Always retrieve them on the server via `consume()`.

`err` passed to the `onFail` callback:

| Field | Type | Description |
|-------|------|-------------|
| `message` | String | Failure reason |

---

## 4. `OmniOneIdentityResult` — return value of `consume()`

| Method | Description |
|--------|-------------|
| `getName()` | Full name |
| `getBirth()` | Date of birth (YYYYMMDD) |
| `getPhone()` | Mobile phone |
| `getCi()` | Connecting Information |
| `getCi2()` | Connecting Information 2 |
| `getDi()` | Duplicated Information |
| `getCiUpdCnt()` | CI change count |
| `getTxid()` | Transaction ID |
| `getCxid()` | Authentication ID |
| `getGender()` | Gender (`M`/`F`) |
| `getNationality()` | Nationality (`L` = local / `F` = foreign) |
| `getProvider()` | Auth provider (telecom carrier, etc.) |
| `getResultCode()` | Result code |
| `getClientMessage()` | Result message |

---

## 5. OMNIONE JS API (direct call)

Use these when you don't want to use the Fragment / button and want to open the auth window directly.

```javascript
// Open the auth popup directly without a button (e.g. force verification under some condition)
OMNIONE.open({
    onSuccess: function(info) {
        document.getElementById('certKey').value = info.certKey;
        document.getElementById('name').value    = info.name;
    },
    onFail: function(err) {
        alert('Identity verification failed: ' + err.message);
    }
});
```

| Method | Description |
|--------|-------------|
| `OMNIONE.bind(buttonId, { onSuccess, onFail })` | Bind the popup-click to a button with the given id |
| `OMNIONE.open({ onSuccess, onFail })` | Open the auth popup directly without a button |

> You cannot open two auth windows at the same time. If one is already in progress, `OMNIONE.open()` is ignored and a console warning is logged.

---

## 6. Runtime Flow

```
[Button click]
  ▼
OMNIONE.open()                           ← omnione-auth.js
  ▼
GET /auth/omnione/cert-info  (Ajax)      ← server prepares session (authToken/certInfo)
  · Response: { ok: true }
  ▼
window.open('/auth/omnione/popup')       ← 412×730 popup (PC)
  ▼
popup.html — calls OACX.LOAD_MODULE()
  · User completes verification (OmniOne standard auth window)
  ▼
Callback(res) — receives res.token, res.txId
  · Creates a form in the parent document → POST /auth/omnione/result
  · Popup closes
  ▼
POST /auth/omnione/result  (handled in parent window)
  · Decrypts resCert using authToken
  · Stores OmniOneIdentityResult in session[OMNIONE_RESULT_{certKey}]
  · Passes only certKey + name/birth/phone to the model
  ▼
result.html renders
  · window.opener.postMessage({ type:'OMNIONE_AUTH_SUCCESS', certKey, name, birth, phone })
  · window.close()
  ▼
Parent window message listener (auto-registered by omnione-auth.js)
  · Calls onSuccess(info)
  ▼
onSuccess callback → fills hidden form fields → user submits
  ▼
Subsequent Controller
  · OmniOneAuthHelper.consume(session, certKey) → retrieves CI/DI (single-use)
  · Save to DB → redirect
```

### Mobile behavior

On mobile, verification happens **in the same window** (no popup).
`result.html` fires `window.dispatchEvent(new CustomEvent('omnione:auth:success', {...}))`,
and `omnione-auth.js`'s listener picks it up and calls `onSuccess(info)` in the same way.

---

## 7. application.yml Settings

Edit `olv-pfom/src/main/resources/application.yml` for your environment:

```yaml
omnione:
  cp-code: "your-service-code"          # CP code from OmniOne
  site-key: "your-site-key"              # Site key from OmniOne
  server-url: "https://ida.omnione.net:7443"          # dev: :7443 / prod: :443 or no port
  callback-url: "https://your-domain/lunch/auth/omnione/result"  # production callback URL
  ux-base-url: "https://ida.omnione.net/sign/esign"   # OmniOne UX base URL

globals:
  security:
    public-urls:
      - /auth/omnione/cert-info
      - /auth/omnione/popup
      - /auth/omnione/result
```

> **For local dev**, remove the `/lunch` prefix from `callback-url`:
> `http://localhost:8080/auth/omnione/result`

> The 3 paths under `globals.security.public-urls` are only needed if **identity verification is used while not logged in**.
> If verification only runs after login, you can comment that block out.

---

## 8. Dependencies (after Nexus upload)

After uploading the 2 external JARs to Nexus, add to `build.gradle`:

```groovy
implementation 'com.raonwhitehat:cx-secucert-java:3.0.1'
implementation 'org.bouncycastle:bcprov-jdk15to18:1.80'
```

> Before the Nexus upload, `import com.raonwhitehat.cert.cx.*` showing as unresolved is expected.
> Any other compile errors must be fixed first.

---

## 9. Security Notes

| Item | Detail |
|------|--------|
| Do NOT expose CI/DI | Never put `OmniOneIdentityResult` directly into a view model. Use the `certKey` → `consume()` pattern only |
| No wildcard session keys | Do NOT register `/auth/omnione/**` in `globals.security.public-urls` — list the 3 paths individually |
| postMessage origin check | `omnione-auth.js` ignores messages where `e.origin !== window.location.origin` |
| No PII logging | Never `log.debug` name, birth, phone, CI, etc. Only `provider` / `txid` are allowed |
| Prevent certKey reuse | `consume()` is single-use; the second call returns `null` |

---

## 10. Troubleshooting

| Symptom | Cause / Fix |
|---------|-------------|
| Button click does nothing | `omnione-auth.js` failed to load or `OMNIONE.bind()` ran at the wrong time. Check the browser console for `[OMNIONE] bind failed` |
| `cert-info` Ajax returns 401/403 | `/auth/omnione/cert-info` not registered in `globals.security.public-urls`. Check `application.yml` |
| Popup is blocked | Disable popup blocker. `OMNIONE.open()` calls `_fail({ message: 'Popup was blocked...' })`, so handle it in `onFail` |
| `onSuccess` callback never fires | The `postMessage` origin from `result.html` and the parent `location.origin` don't match. For local dev they must both be `http://localhost:8080` |
| `consume()` returns `null` | Session expired, or `consume()` was already called once (single-use). Keep `certKey` as a hidden form field and call `consume()` immediately after submit |
| `OmniOne reqCert failed` exception | `cp-code` / `site-key` wrong, or OmniOne server connection failed. Check `server-url` and firewall |
| Mobile callback never fires | No `window.opener` on mobile — uses `CustomEvent('omnione:auth:success')` instead of `postMessage`. Verify the listener in `omnione-auth.js` is registered |
| Local environment uses production `callback-url` | Set `callback-url` to `http://localhost:8080/auth/omnione/result` in `application.yml` |

---

## 11. Reference Files

| Path | Role |
|------|------|
| `olv-pfom/src/main/java/egovframework/com/cmm/auth/omnione/config/OmniOneProperties.java` | Binds `omnione.*` from `application.yml` |
| `olv-pfom/src/main/java/egovframework/com/cmm/auth/omnione/service/OmniOneAuthService.java` | Service interface |
| `olv-pfom/src/main/java/egovframework/com/cmm/auth/omnione/service/OmniOneCertSession.java` | DTO for the `reqCert` result |
| `olv-pfom/src/main/java/egovframework/com/cmm/auth/omnione/service/OmniOneIdentityResult.java` | Decrypted DTO (includes CI/DI) |
| `olv-pfom/src/main/java/egovframework/com/cmm/auth/omnione/service/impl/OmniOneAuthServiceImpl.java` | SDK calls + result API handler |
| `olv-pfom/src/main/java/egovframework/com/cmm/auth/omnione/web/OmniOneAuthController.java` | `cert-info` / `popup` / `result` endpoints |
| `olv-pfom/src/main/java/egovframework/com/cmm/auth/omnione/web/OmniOneAuthHelper.java` | Used by subsequent Controllers to retrieve CI/DI |
| `olv-pfom/src/main/resources/templates/fragments/omnione-auth.html` | Shared Thymeleaf Fragment (one-line button) |
| `olv-pfom/src/main/resources/templates/auth/omnione/popup.html` | Loader for the standard auth window (inside the popup) |
| `olv-pfom/src/main/resources/templates/auth/omnione/result.html` | Receives the auth result, sends `postMessage` back |
| `olv-pfom/src/main/resources/static/js/com/omnione-auth.js` | Shared JS module (OMNIONE) |
| `olv-pfom/src/main/resources/application.yml` | `omnione.*` and `globals.security.public-urls` settings |
