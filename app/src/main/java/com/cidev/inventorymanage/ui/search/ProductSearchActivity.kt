package com.cidev.inventorymanage.ui.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cidev.inventorymanage.data.ProductRepository
import com.cidev.inventorymanage.data.model.Product
import com.cidev.inventorymanage.databinding.ActivityProductSearchBinding
import com.cidev.inventorymanage.databinding.ItemProductBinding
import com.cidev.inventorymanage.ui.menu.MainMenuActivity
import kotlinx.coroutines.launch

class ProductSearchActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProductSearchBinding
    private val repository = ProductRepository()
    private val adapter = ProductAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductSearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.recyclerResults.layoutManager = LinearLayoutManager(this)
        binding.recyclerResults.adapter = adapter

        binding.edtBarcode.setOnEditorActionListener { _, _, _ ->
            search(binding.edtBarcode.text?.toString().orEmpty())
            true
        }
    }

    private fun search(term: String) {
        if (term.isBlank()) return
        val sessionId = MainMenuActivity.currentUser?.sessionID.orEmpty()

        lifecycleScope.launch {
            val result = repository.searchProduct(sessionId, term)
            result.onSuccess { adapter.submit(it) }
            result.onFailure {
                Toast.makeText(this@ProductSearchActivity, "שגיאה: ${it.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private class ProductAdapter : RecyclerView.Adapter<ProductAdapter.VH>() {
        private var items: List<Product> = emptyList()

        fun submit(newItems: List<Product>) {
            items = newItems
            notifyDataSetChanged()
        }

        inner class VH(val b: ItemProductBinding) : RecyclerView.ViewHolder(b.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val b = ItemProductBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return VH(b)
        }

        override fun onBindViewHolder(holder: VH, position: Int) {
            val p = items[position]
            holder.b.txtDescription.text = p.prodDescription.ifBlank { p.prodID }
            holder.b.txtDetails.text = "ברקוד: ${p.prodBarcode} | קטלוגי: ${p.mfrCatalogNum} | מלאי: ${p.prodStorageQuantity} | מיקום: ${p.defaultLocation}"
        }

        override fun getItemCount() = items.size
    }
}
