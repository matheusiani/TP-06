package br.unimes.approomproduto.view

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import br.unimes.approomproduto.dao.ProductDao
import br.unimes.approomproduto.databinding.ActivityMainBinding
import br.unimes.approomproduto.db.AppDatabase
import br.unimes.approomproduto.model.Product
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var productDao: ProductDao
    private lateinit var adapter: ProductAdapter

    private val moeda = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR"))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializa o ViewBinding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Banco de dados via Singleton (uma única instância para o app todo)
        productDao = AppDatabase.getDatabase(this).productDao()

        // A3: RecyclerView para mostrar a lista na tela
        adapter = ProductAdapter { product -> confirmarExclusao(product) }
        binding.recyclerProdutos.layoutManager = LinearLayoutManager(this)
        binding.recyclerProdutos.adapter = adapter

        binding.btnInserirProduto.setOnClickListener { inserirProduto() }
        binding.btnListarProdutos.setOnClickListener { listarProdutos() }
        binding.btnBuscar.setOnClickListener { buscarPorNome() }
        binding.btnExcluirTodos.setOnClickListener { confirmarExcluirTodos() }
        binding.btnAtualizar.setOnClickListener { atualizarPreco() }
    }

    // ---------- Inserir (A1, A2, A8) ----------
    private fun inserirProduto() {
        val name = binding.editProductName.text.toString().trim()
        val price = binding.editProductPrice.text.toString().replace(',', '.').toDoubleOrNull()
        val quantityText = binding.editProductQuantity.text.toString().trim()
        val description = binding.editProductDescription.text.toString().trim().ifEmpty { null }

        if (name.isEmpty()) {
            binding.editProductName.error = "Informe o nome do produto"
            binding.editProductName.requestFocus()
            return
        }

        // A2: preço vazio, zero ou negativo não é aceito
        if (price == null || price <= 0.0) {
            binding.editProductPrice.error = "Informe um preço válido"
            binding.editProductPrice.requestFocus()
            return
        }

        // A8: quantidade (vazia = 0; negativa não é aceita)
        val quantity = if (quantityText.isEmpty()) 0 else quantityText.toIntOrNull() ?: -1
        if (quantity < 0) {
            binding.editProductQuantity.error = "Informe uma quantidade válida"
            binding.editProductQuantity.requestFocus()
            return
        }

        val product = Product(name = name, price = price, description = description, quantity = quantity)

        // Insere o produto de forma assíncrona (corrotina)
        lifecycleScope.launch {
            productDao.insertAll(product)
            Log.i("TAG", "Produto inserido: $name - R$$price (qtd: $quantity)")
            Toast.makeText(this@MainActivity, "Produto inserido!", Toast.LENGTH_SHORT).show()
            limparCampos()
            listarProdutos()
        }
    }

    // A1: limpa os campos e devolve o cursor ao campo Nome
    private fun limparCampos() {
        binding.editProductName.text.clear()
        binding.editProductPrice.text.clear()
        binding.editProductQuantity.text.clear()
        binding.editProductDescription.text.clear()
        binding.editProductName.requestFocus()
    }

    // ---------- Listar (A3, A4) ----------
    private fun listarProdutos() {
        lifecycleScope.launch {
            val products = productDao.getAll()
            if (products.isNotEmpty()) {
                products.forEach {
                    Log.i("TAG", "Produto = ${it.name}, Preço = R$${it.price}, Qtd = ${it.quantity}, Descrição = ${it.description}")
                }
            } else {
                Log.i("TAG", "Nenhum produto encontrado.")
            }
            mostrarProdutos(products, "Todos os produtos")
        }
    }

    // A3 + A4: mostra a lista na tela e o resumo do estoque
    private fun mostrarProdutos(products: List<Product>, titulo: String) {
        adapter.submitList(products)

        if (products.isEmpty()) {
            binding.txtResumo.text = "$titulo: nenhum produto encontrado."
            return
        }

        val quantidadeProdutos = products.size
        val somaPrecos = products.sumOf { it.price }
        val valorEstoque = products.sumOf { it.price * it.quantity }

        binding.txtResumo.text = buildString {
            appendLine(titulo)
            appendLine("Produtos cadastrados: $quantidadeProdutos")
            appendLine("Soma dos preços: ${moeda.format(somaPrecos)}")
            append("Valor total em estoque (preço × qtd): ${moeda.format(valorEstoque)}")
        }
    }

    // ---------- Buscar (A5) ----------
    private fun buscarPorNome() {
        val texto = binding.editBusca.text.toString().trim()
        if (texto.isEmpty()) {
            listarProdutos()
            return
        }
        lifecycleScope.launch {
            val encontrados = productDao.buscarPorNome("%$texto%")
            Log.i("TAG", "Busca por \"$texto\": ${encontrados.size} resultado(s)")
            mostrarProdutos(encontrados, "Resultado da busca por \"$texto\"")
        }
    }

    // ---------- Excluir (A6) ----------
    private fun confirmarExcluirTodos() {
        AlertDialog.Builder(this)
            .setTitle("Excluir todos")
            .setMessage("Deseja apagar TODOS os produtos cadastrados?")
            .setPositiveButton("Excluir") { _, _ ->
                lifecycleScope.launch {
                    productDao.deleteAll()
                    Log.i("TAG", "Todos os produtos foram excluídos.")
                    Toast.makeText(this@MainActivity, "Produtos excluídos!", Toast.LENGTH_SHORT).show()
                    listarProdutos()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    // A6 (desafio extra): exclui um produto específico com @Delete (toque longo no item)
    private fun confirmarExclusao(product: Product) {
        AlertDialog.Builder(this)
            .setTitle("Excluir produto")
            .setMessage("Excluir \"${product.name}\" (id ${product.id})?")
            .setPositiveButton("Excluir") { _, _ ->
                lifecycleScope.launch {
                    productDao.delete(product)
                    Log.i("TAG", "Produto excluído: id ${product.id} - ${product.name}")
                    Toast.makeText(this@MainActivity, "Produto excluído!", Toast.LENGTH_SHORT).show()
                    listarProdutos()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    // ---------- Atualizar (A7) ----------
    private fun atualizarPreco() {
        val id = binding.editUpdateId.text.toString().trim().toIntOrNull()
        val novoPreco = binding.editUpdatePrice.text.toString().replace(',', '.').toDoubleOrNull()

        if (id == null) {
            binding.editUpdateId.error = "Informe o ID"
            return
        }
        if (novoPreco == null || novoPreco <= 0.0) {
            binding.editUpdatePrice.error = "Informe um preço válido"
            return
        }

        lifecycleScope.launch {
            val produto = productDao.getById(id)
            if (produto == null) {
                binding.editUpdateId.error = "Produto não encontrado"
                return@launch
            }
            productDao.update(produto.copy(price = novoPreco))
            Log.i("TAG", "Preço atualizado: id $id - ${produto.name}: R$${produto.price} -> R$$novoPreco")
            Toast.makeText(this@MainActivity, "Preço atualizado!", Toast.LENGTH_SHORT).show()
            binding.editUpdateId.text.clear()
            binding.editUpdatePrice.text.clear()
            listarProdutos()
        }
    }
}
