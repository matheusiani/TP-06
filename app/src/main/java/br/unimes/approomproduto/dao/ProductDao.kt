package br.unimes.approomproduto.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import br.unimes.approomproduto.model.Product

@Dao
interface ProductDao {

    // suspend: o Room não permite consultas na main thread
    @Query("SELECT * FROM product")
    suspend fun getAll(): List<Product>

    @Insert
    suspend fun insertAll(vararg products: Product)

    // A5: busca por nome (o chamador passa "%texto%")
    @Query("SELECT * FROM product WHERE name LIKE :nome")
    suspend fun buscarPorNome(nome: String): List<Product>

    // A6: excluir todos
    @Query("DELETE FROM product")
    suspend fun deleteAll()

    // A6 (desafio extra): excluir um produto específico (o Room usa o id/chave primária)
    @Delete
    suspend fun delete(product: Product)

    // A7: buscar um produto pelo id e atualizar
    @Query("SELECT * FROM product WHERE id = :id")
    suspend fun getById(id: Int): Product?

    @Update
    suspend fun update(product: Product)
}
