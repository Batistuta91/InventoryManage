# InventoryManage → Android — Master Map

Extracted from `InventoryManage.exe` (.NET Compact Framework, decompiled at the
metadata level — class/method/field names only, not full method bodies).
This is the working checklist for the port. Update the ✅/⬜ as modules land.

## Server

- Legacy client reads the WMS server IP from `ServiceIP.txt` (`192.168.0.22`).
- Communication is **SOAP 1.1**, `.NET`-style envelope, namespace
  `http://tempuri.org/` (default namespace — no custom `[WebService(Namespace=...)]`
  attribute found).
- **CONFIRMED (2026-07).** Browsing directly to
  `http://192.168.0.22/InventoryManage/Service.asmx` from a phone on the
  warehouse Wi-Fi showed the auto-generated ASMX service description page,
  listing all 64 methods below — exact match with what was extracted from
  the decompiled exe. `SoapClient.kt` now uses this path by default.
- Bonus finding: there's a whole separate browser-based admin/reports site
  at `/InventoryManage/*.aspx` (stock locations, inventory count reports,
  packaging/distribution reports, a "distribution" module that redirects to
  a *different* server at `192.168.0.32`). Not needed for the handheld port,
  but useful context — confirms `192.168.0.22` is a general Amodat WMS
  application server, not just a bare SOAP endpoint.

## SOAP methods (64 total, sync versions — Begin/End async variants omitted)

| # | Method | Android status |
|---|--------|-----------------|
| 1 | CheckUserLogin | ⬜ |
| 2 | CheckUserLogin2 | ✅ wired (`AuthRepository`) |
| 3 | SearchAccount | ⬜ |
| 4 | SearchProduct | ✅ wired (`ProductRepository`) |
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

Grouped by warehouse workflow (matches the `User.IsXxxActive` flags that
drive the dynamic main menu):

**Auth / shell**
- FrmMain → ✅ `MainMenuActivity` (dynamic menu scaffold)

**Product / search (foundation — used by almost every other module)**
- FrmSearch, FrmSearchResults, FrmProductsList, FrmProductDetails,
  FrmSearchPackage → ✅ `ProductSearchActivity` (search only, no detail view yet)

**Pick & Pack** (`isOrdersCollectActive`)
- FrmPickAndPack, FrmCreatePickAndPack, FrmPickAndPackConfirm,
  FrmPickAndPackConfirmExtraInfo, FrmPickAndPackOrdersToFulfill,
  FrmPickAndPackOrdersToConfirm, FrmPickAndPackLineComments,
  FrmPickItemsByPackages, FrmCheckPackagingContents,
  FrmCheckPackagingContentsResults → ⬜ not started

**Inventory count / cycle count** (`isInventoryCountActive` / `isInventoryCycleCountActive`)
- FrmInvCount, FrmCreateCycleCount, FrmInvCycleCount → ⬜ not started

**Inventory entry/exit** (`isInvGeneralEntryActive` / `isInvGeneralExitActive`)
- FrmInvEntry, FrmInvGeneralEntry, FrmInvGeneralExit,
  FrmHandleProductBatchSerialNum, FrmHandleProductLocation → ⬜ not started

**Delivery notes / purchases** (`isDeliveryNoteDraftConfirmActive`)
- FrmClientDeliveryNotes, FrmOpenDeliveryPurchases, FrmInvOpenOrders,
  FrmSearchOpenOrders → ⬜ not started

**Returns** (`isReturnProductsToSupplyerActive` / `isReturnProductsFromClientActive`)
- FrmReturnFromClient, FrmReturnFromClientSearch, FrmPurchaseReturn,
  FrmPurchaseReturnSearch → ⬜ not started

**Transactions / drafts**
- FrmTransaction, FrmCreateTransaction, FrmTransactionDrafts,
  FrmListSavedDocument → ⬜ not started

**Production orders** (`isProductionOrderActive`)
- FrmProductionOrder, FrmProductionOrdersList, FrmProductionOrderPgDetails,
  FrmProductionOrderPgProducts → ⬜ not started

**Orders**
- FrmOrder, FrmOrderSummery → ⬜ not started

**Misc / infra**
- FrmPrinters, FrmDocBaseRemark, FrmDocBaseRemarkExtra, FrmUpgrade → ⬜ not started

## Recommended build order

1. Confirm the real `.asmx` path and test `CheckUserLogin2` against the live
   server from this scaffold.
2. Product search + product detail (foundation for everything else).
3. Pick & Pack — this is almost certainly the highest-volume daily workflow.
4. Inventory count / cycle count.
5. Everything else, roughly in order of how often it's used day-to-day.
