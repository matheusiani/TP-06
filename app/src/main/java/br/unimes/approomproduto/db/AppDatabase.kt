package br.unimes.approomproduto.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import br.unimes.approomproduto.dao.ProductDao
import br.unimes.approomproduto.model.Product

// A8: version subiu de 1 para 2 porque a entidade Product ganhou a coluna "quantity"
@Database(entities = [Product::class], version = 2)
abstract class AppDatabase : RoomDatabase() {

    abstract fun productDao(): ProductDao

    companion object {
        // Singleton: uma única instância do banco para o app inteiro
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "database-name"
                )
                    .fallbackToDestructiveMigration() // recria o banco se o esquema mudar (apaga os dados)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
