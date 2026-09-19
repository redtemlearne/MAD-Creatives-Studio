package com.example.model

import java.util.UUID

data class Project(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val lastEditedFormatted: String = "Just now",
    val durationPlaceholder: String = "0:00"
)
