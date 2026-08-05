package com.phytotec.recepcion.data.remote

data class MobileNavItemDto(
    val name: String,
    val icon: String?,
    val path: String?,
    val children: List<MobileNavItemDto> = emptyList(),
)

data class MobileNavigationResponse(
    val items: List<MobileNavItemDto>,
)
