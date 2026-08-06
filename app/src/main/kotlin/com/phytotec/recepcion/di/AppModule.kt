package com.phytotec.recepcion.di

import android.content.Context
import androidx.room.Room
import com.phytotec.recepcion.data.local.AppDatabase
import com.phytotec.recepcion.data.local.dao.AsignacionDao
import com.phytotec.recepcion.data.local.dao.ConfirmacionDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "recepcion.db")
            // No hay datos reales en producción todavía con el esquema
            // viejo (app sin publicar) — un cambio de esquema simplemente
            // recrea la base en vez de exigir una migración escrita a mano.
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideConfirmacionDao(db: AppDatabase): ConfirmacionDao = db.confirmacionDao()

    @Provides
    fun provideAsignacionDao(db: AppDatabase): AsignacionDao = db.asignacionDao()
}
