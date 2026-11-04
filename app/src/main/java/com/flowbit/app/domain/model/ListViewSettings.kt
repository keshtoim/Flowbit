package com.flowbit.app.domain.model

enum class ListDensity { CARDS, ROWS, MINI }

data class ListViewSettings(
    val density: ListDensity = ListDensity.ROWS,
    val showHistory: Boolean = true,
    val collapseCompleted: Boolean = true,
    val showTagFilter: Boolean = true,
    val bigSummary: Boolean = false,
)
