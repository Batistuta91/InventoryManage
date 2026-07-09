# InventoryManage → Android — Master Map

Extracted from `InventoryManage.exe` (.NET Compact Framework, decompiled at
the metadata level — class/method/field names only, not full method bodies).
Working checklist for the port. Update ✅/⬜ as modules land.

## Server

- Legacy client reads the WMS server IP from `ServiceIP.txt` (`192.168.0.22`).
- Communication is **SOAP 1.1**, `.NET`-style envelope, namespace
  `http://tempuri.org/` (default namespace — no custom attribute found).
- **Verified:** IIS virtual directory is named `InventoryManage` — confirmed
  from the admin site's page source (`WebResource.axd` loads from
  `/InventoryManage/`). The exact `.asmx` filename (`Service.asmx`) is
  still an assumption, not yet confirmed by browsing to it directly.
- **Verified:** device binding is real. The admin site (`UsersHandle.aspx`)
  has a grid mapping each login (m1, m2, y1, y2...) to a device GUID, with
  a "ResetDevices" admin action to unbind a device. GUID format matches
  `AmLaunchLog.txt`. Android's `deviceID` must be stable across launches.
- **Not yet verified:** exact SOAP parameter names/shapes for any of the
  64 methods below — no live SOAP call has been made in this project yet.
  `SoapClient.kt` currently guesses parameter names from the matching
  field names in the decompiled data classes.
- Bonus finding: there's a separate browser-based admin/reports site at
  `/InventoryManage/*.aspx` (stock locations, inventory count reports,
  a "distribution" module that redirects to a *different* server at
  `192.168.0.32`). Not needed for the handheld port, but confirms
  `192.168.0.22` is a general Amodat WMS application server, not just a
  bare SOAP endpoint.
- **Build note:** the "official" ksoap2-android Maven coordinates
  (`com.google.code.ksoap2-android:ksoap2-android:3.6.4`) don't resolve
  from Maven Central — they only ever lived on an old Sonatype repo. The
  project uses a JitPack-mirrored fork instead (`com.github.kekru:ksoap2-android`).

## SOAP methods (64 total, sync versions — Begin/End async variants omitted)

| # | Method | Android status |
|---|--------|-----------------|
| 1 | CheckUserLogin | ⬜ |
| 2 | CheckUserLogin2 | ✅ wired (`AuthRepository`) — not live-tested |
| 3 | SearchAccount | ⬜ |
| 4 | SearchProduct | ✅ wired (`ProductRepository`) — not live-tested |
| 5 | SearchSupplyer | ⬜ |
| 6 | IsBatchSerialExist | ⬜ |
| 7 | GetBatchExpirationDate | ⬜ |
| 8 | IsBatchSerialExistOnLocation | ⬜ |
| 9 | GetLocationsProductsBatchNums | ⬜ |
| 10 | SearchProductQtyFromBarcode | ⬜ |
| 11 | SaveDocumentOrder | ⬜ |
| 12 | DocOrderCalcTotals | ⬜ |
| 13 | GetPickItemsByPackages | ⬜ |
| 14 | SavePickItemsByPackages | ⬜ |
| 15 | SaveDocumentInvEntry | ⬜ |
| 16 | GetOpenPurchaseOrders | ⬜ |
| 17 | GetPurchaseOrderProducts | ⬜ |
| 18 | RefreshDocumentInvEntry | ⬜ |
| 19 | GetInvEntryProductsBatchNums | ⬜ |
| 20 | SaveDocumentInvCount | ⬜ |
| 21 | GetCycleList | ⬜ |
| 22 | GetCycleCountProducts | ⬜ |
| 23 | SaveDocumentInvCycleCount | ⬜ |
| 24 | CheckTransactionProductQuantity | ⬜ |
| 25 | SaveDocTransaction | ⬜ |
| 26 | CheckTransactionItemCode | ⬜ |
| 27 | GetStorageList | ⬜ |
| 28 | GetPrinters | ⬜ |
| 29 | GetPriceLists | ⬜ |
| 30 | GetTransactionDrafts | ⬜ |
| 31 | GetTransactionDraftProducts | ⬜ |
| 32 | GetProductsByLocation | ⬜ |
| 33 | SaveDocumentPickPack | ⬜ |
| 34 | GetOrdersToFulfill | ⬜ |
| 35 | GetSalesOrderProducts | ⬜ |
| 36 | ClearBeingHandled | ⬜ |
| 37 | RefreshDocumentPickPack | ⬜ |
| 38 | GetPackRemarksList | ⬜ |
| 39 | GetDeliveryNotesDraft | ⬜ |
| 40 | GetDeliveryNotesDraftProducts | ⬜ |
| 41 | SaveDocumentDeliveryNote | ⬜ |
| 42 | GetOptionalLocationsForProduct | ⬜ |
| 43 | PrintPickItemStickers | ⬜ |
| 44 | PrintInvEntryItemStickers | ⬜ |
| 45 | GetProductLocations | ⬜ |
| 46 | SendError | ⬜ |
| 47 | GetProductsList | ⬜ |
| 48 | GetProductStorages | ⬜ |
| 49 | GetDeliveryPurchases | ⬜ |
| 50 | GetDeliveryPurchaseProducts | ⬜ |
| 51 | SaveDocumentReturnPurchase | ⬜ |
| 52 | GetClientsDeliveries | ⬜ |
| 53 | GetDeliveryNotesProducts | ⬜ |
| 54 | SaveDocumentReturnFromClient | ⬜ |
| 55 | SaveDocumentInvGeneralExit | ⬜ |
| 56 | SaveDocumentInvGeneralEntry | ⬜ |
| 57 | GetProductionOrdersList | ⬜ |
| 58 | GetProductionOrderProducts | ⬜ |
| 59 | SaveProductionOrder | ⬜ |
| 60 | GetTempDocDataList | ⬜ |
| 61 | GetTempDocInvEntry | ⬜ |
| 62 | GetTempDocPickPack | ⬜ |
| 63 | TempDocDelete | ⬜ |
| 64 | GetPackagingContents | ⬜ |

## Screens (46 legacy Forms → Android modules)

**Auth / shell**
- FrmMain → ✅ `MainMenuActivity` (dynamic menu scaffold)

**Product / search**
- FrmSearch, FrmSearchResults, FrmProductsList, FrmProductDetails,
  FrmSearchPackage → ✅ `ProductSearchActivity` (search only, no detail view)

**Pick & Pack** (`isOrdersCollectActive`)
- FrmPickAndPack, FrmCreatePickAndPack, FrmPickAndPackConfirm,
  FrmPickAndPackConfirmExtraInfo, FrmPickAndPackOrdersToFulfill,
  FrmPickAndPackOrdersToConfirm, FrmPickAndPackLineComments,
  FrmPickItemsByPackages, FrmCheckPackagingContents,
  FrmCheckPackagingContentsResults → ⬜ not started

**Inventory count / cycle count**
- FrmInvCount, FrmCreateCycleCount, FrmInvCycleCount → ⬜ not started

**Inventory entry/exit**
- FrmInvEntry, FrmInvGeneralEntry, FrmInvGeneralExit,
  FrmHandleProductBatchSerialNum, FrmHandleProductLocation → ⬜ not started

**Delivery notes / purchases**
- FrmClientDeliveryNotes, FrmOpenDeliveryPurchases, FrmInvOpenOrders,
  FrmSearchOpenOrders → ⬜ not started

**Returns**
- FrmReturnFromClient, FrmReturnFromClientSearch, FrmPurchaseReturn,
  FrmPurchaseReturnSearch → ⬜ not started

**Transactions / drafts**
- FrmTransaction, FrmCreateTransaction, FrmTransactionDrafts,
  FrmListSavedDocument → ⬜ not started

**Production orders**
- FrmProductionOrder, FrmProductionOrdersList, FrmProductionOrderPgDetails,
  FrmProductionOrderPgProducts → ⬜ not started

**Orders**
- FrmOrder, FrmOrderSummery → ⬜ not started

**Misc / infra**
- FrmPrinters, FrmDocBaseRemark, FrmDocBaseRemarkExtra, FrmUpgrade → ⬜ not started

## Recommended build order

1. Confirm the real `.asmx` path and test `CheckUserLogin2` against the
   live server from this scaffold.
2. Product search + product detail.
3. Pick & Pack.
4. Inventory count / cycle count.
5. Everything else, roughly by frequency of daily use.
