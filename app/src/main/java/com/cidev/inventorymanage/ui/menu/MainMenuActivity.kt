package com.cidev.inventorymanage.ui.menu

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cidev.inventorymanage.data.model.User
import com.cidev.inventorymanage.databinding.ActivityMainMenuBinding
import com.cidev.inventorymanage.databinding.ItemMenuBinding
import com.cidev.inventorymanage.ui.search.ProductSearchActivity

class MainMenuActivity : AppCompatActivity() {

    companion object {
        var currentUser: User? = null
    }

    private lateinit var binding: ActivityMainMenuBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val user = currentUser
        binding.txtWelcome.text = "שלום ${user?.loginName.orEmpty()} — מחסן: ${user?.defaultWarehouseCode.orEmpty()}"

        binding.recyclerMenu.layoutManager = LinearLayoutManager(this)
        binding.recyclerMenu.adapter = MenuAdapter(buildMenuItems(user))
    }

    private data class MenuEntry(val label: String, val onClick: () -> Unit)

    private fun buildMenuItems(user: User?): List<MenuEntry> {
        if (user == null) return emptyList()
        val items = mutableListOf<MenuEntry>()

        items += MenuEntry("חיפוש מוצר") {
            startActivity(Intent(this, ProductSearchActivity::class.java))
        }

        if (user.isOrderActive) items += placeholder("הזמנות")
        if (user.isTransactionActive) items += placeholder("תנועות מלאי")
        if (user.isInventoryCountActive) items += placeholder("ספירת מלאי")
        if (user.isInventoryCycleCountActive) items += placeholder("ספירה מחזורית")
        if (user.isInsertIntoStockActive) items += placeholder("כניסה למלאי")
        if (user.isOrdersCollectActive) items += placeholder("ליקוט ואריזה (Pick & Pack)")
        if (user.isDeliveryNoteDraftConfirmActive) items += placeholder("אישור טיוטת תעודת משלוח")
        if (user.isProductsManageActive) items += placeholder("ניהול מוצרים")
        if (user.isReturnProductsToSupplyerActive) items += placeholder("החזרה לספק")
        if (user.isReturnProductsFromClientActive) items += placeholder("החזרה מלקוח")
        if (user.isProductionOrderActive) items += placeholder("פקודות ייצור")
        if (user.isInvGeneralEntryActive) items += placeholder("כניסה כללית למלאי")
        if (user.isInvGeneralExitActive) items += placeholder("יציאה כללית ממלאי")

        return items
    }

    private fun placeholder(label: String) = MenuEntry(label) {
        // TODO: replace with the real screen — see docs/API_MAP.md.
    }

    private inner class MenuAdapter(private val items: List<MenuEntry>) :
        RecyclerView.Adapter<MenuAdapter.VH>() {

        inner class VH(val b: ItemMenuBinding) : RecyclerView.ViewHolder(b.root)

        override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): VH {
            val b = ItemMenuBinding.inflate(layoutInflater, parent, false)
            return VH(b)
        }

        override fun onBindViewHolder(holder: VH, position: Int) {
            val entry = items[position]
            holder.b.txtMenuLabel.text = entry.label
            holder.b.root.setOnClickListener { entry.onClick() }
        }

        override fun getItemCount() = items.size
    }
}
