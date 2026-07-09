# InventoryManage — Android port (scaffold)

Android replacement for the Windows Mobile 6.5 / .NET Compact Framework app
`InventoryManage.exe` running on your MC55A0 handhelds. Talks to the **same**
`InvManageWebService` SOAP backend at `192.168.0.22` — no server-side changes
required (assuming the ASP.NET/IIS service keeps running on the WMS box).

## What's actually working right now

- ✅ Project builds as a standard Android Studio / Gradle project (Kotlin).
- ✅ `SoapClient` — generic SOAP 1.1 caller using ksoap2-android (via a
  JitPack-mirrored fork — see the dependency comment in `app/build.gradle.kts`
  for why).
- ✅ Login screen → `CheckUserLogin2` call → dynamic main menu built from
  the returned `User` permission flags (same pattern as the legacy `FrmMain`).
- ✅ Product search screen → `SearchProduct` call, results list.
- ⬜ Everything else (~44 more screens) — see `docs/API_MAP.md`.
- ⚠️ **No SOAP call has actually succeeded against the live server yet.**
  Parameter names/shapes are first-guesses from the decompiled binary's
  field names, not confirmed behavior.

## Before this will actually talk to your server

1. Open the project in Android Studio (or run `gradle wrapper` yourself —
   this scaffold doesn't ship a wrapper jar).
2. Confirm the SOAP endpoint path. Verified so far: the IIS virtual
   directory is `InventoryManage`. Not verified: the exact `.asmx`
   filename. Browse to `http://192.168.0.22/InventoryManage/` from the
   warehouse LAN to confirm.
3. Confirm SOAP parameter names by actually running a login attempt and
   reading the SOAP fault (if any) — see `docs/API_MAP.md` for what's
   verified vs. assumed.
4. You'll need a launcher icon (`mipmap/ic_launcher`) — not included here.

## Why native Kotlin instead of React Native/Flutter/PWA

- Direct SOAP + barcode-scanner (Zebra DataWedge / Honeywell intent API)
  integration is simpler and more reliable native than through a JS bridge.
- Matches how you've built your other tools — direct, no extra abstraction
  layer.
- Rugged Android handhelds ship first-class native Android support; a PWA
  would lose reliable offline behavior and hardware-scanner integration.

## Architecture

```
data/            - repositories (one per SOAP module) + data models
network/         - SoapClient (generic SOAP caller)
ui/login/        - login screen
ui/menu/         - dynamic main menu (built from user permission flags)
ui/search/       - product search (first working module)
docs/API_MAP.md  - full 64-method / 46-screen checklist + build order
```

Each new module should follow the same pattern as Product Search: a data
model, a Repository wrapping the relevant SOAP call(s), and an Activity +
layout in `ui/<module>/`.
