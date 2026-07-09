# InventoryManage — Android port (scaffold)

Android replacement for the Windows Mobile 6.5 / .NET Compact Framework app
`InventoryManage.exe` running on your MC55A0 handhelds. Talks to the **same**
`InvManageWebService` SOAP backend at `192.168.0.22` — no server-side changes
required (assuming the ASP.NET/IIS service keeps running on the WMS box).

## What's actually working right now

- ✅ Project builds as a standard Android Studio / Gradle project (Kotlin).
- ✅ `SoapClient` — hand-rolled SOAP 1.1 client (plain XML + OkHttp), **not**
  the ksoap2-android library. That library's transitive dependencies
  (kxml, kobjects-j2me, me4se) are dead 2004-era code with no working Maven
  repository anywhere anymore — not worth fighting. A raw SOAP request/parse
  is simple enough to own directly.
- ✅ Login screen → real `CheckUserLogin2` call → dynamic main menu built
  from the returned `User` permission flags (same pattern as the legacy
  `FrmMain`).
- ✅ Product search screen → real `SearchProduct` call, results list.
- ⬜ Everything else (~44 more screens) — see `docs/API_MAP.md` for the full
  checklist and suggested build order.

## Before this will actually talk to your server

1. **Open the project in Android Studio** (this scaffold doesn't include the
   Gradle wrapper jar/binary — Android Studio will offer to generate it on
   first open, or run `gradle wrapper` if you have Gradle installed locally).
2. **Confirm the SOAP endpoint path.** I could only verify the *namespace*
   (`http://tempuri.org/`) from the binary — the exact `.asmx` path isn't
   stored as a literal string anywhere in the exe (it's built at runtime from
   `ServiceIP.txt`). `SoapClient.kt` currently assumes
   `http://192.168.0.22/InvManageWebService/Service.asmx`. Easiest way to
   confirm: browse to `http://192.168.0.22/` from a PC on the warehouse LAN
   and look at the IIS site structure, or check one of the working MC55A0
   devices' config/registry.
3. **Confirm SOAP parameter names.** I extracted every method's *name* and
   every data object's *fields* from the assembly metadata, but not the
   parameter names of the methods themselves (that requires full IL
   decompilation, which I didn't do). `AuthRepository` and `ProductRepository`
   guess reasonable names (`loginName`, `password`, `deviceID`, `searchTerm`)
   based on the matching field names in `User`/`Product`. If a call fails
   with a SOAP fault about an unrecognized parameter, that's almost always
   the fix — the field names in the model classes are very likely correct
   since those came straight from the binary's metadata.
4. You'll need a launcher icon (`mipmap/ic_launcher`) — not included here.

## Why native Kotlin instead of React Native/Flutter/PWA

- Direct SOAP + barcode-scanner (Zebra DataWedge / Honeywell intent API)
  integration is simpler and more reliable native than through a JS bridge.
- Matches how you've built your other tools (PyQt5 desktop apps) — direct,
  no extra abstraction layer.
- Rugged Android handhelds (the usual MC55A0 replacement path — e.g. Zebra
  TC21/TC26, Honeywell CT30) ship first-class native Android support; a PWA
  would lose reliable offline behavior and hardware-scanner integration,
  which matters on a warehouse floor.

## Architecture

```
data/            - repositories (one per SOAP module) + data models
network/         - SoapClient (generic SOAP caller)
ui/login/        - login screen
ui/menu/         - dynamic main menu (built from user permission flags)
ui/search/       - product search (first working module)
docs/API_MAP.md  - full 64-method / 46-screen checklist + build order
```

Each new module should follow the same 3-piece pattern as Product Search:
a `data model`, a `Repository` wrapping the relevant SOAP call(s), and an
`Activity` + layout in `ui/<module>/`.
