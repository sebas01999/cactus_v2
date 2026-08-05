package com.phytotec.recepcion.data.repository

import com.phytotec.recepcion.data.remote.ApiService
import com.phytotec.recepcion.data.remote.MobileNavItemDto
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MobileNavigationRepository @Inject constructor(
    private val apiService: ApiService,
) {
    suspend fun loadNavigation(): List<MobileNavItemDto> = apiService.getNavigation().items
}
