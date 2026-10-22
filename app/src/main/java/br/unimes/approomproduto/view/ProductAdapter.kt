package br.unimes.approomproduto.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import br.unimes.approomproduto.databinding.ItemProductBinding
import br.unimes.approomproduto.model.Product
import java.text.NumberFormat
import java.util.Locale

// A3 (desafio extra): adapter do RecyclerView que exibe os produtos na tela
class ProductAdapter(
    private val onItemLongClick: (Product) -> Unit
) : ListAdapter<Product, ProductAdapter.ProductViewHolder>(DIFF) {

    private val moeda = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR"))

    inner class ProductViewHolder(val binding: ItemProductBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding = ItemProductBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ProductViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val p = getItem(position)
        with(holder.binding) {
            txtNome.text = "#${p.id} • ${p.name}"
            txtPreco.text = moeda.format(p.price)
            txtDetalhes.text = "Qtd: ${p.quantity} • ${p.description ?: "Sem descrição"}"
            root.setOnLongClickListener {
                onItemLongClick(p)
                true
            }
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<Product>() {
            override fun areItemsTheSame(oldItem: Product, newItem: Product) = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: Product, newItem: Product) = oldItem == newItem
        }
    }
}
