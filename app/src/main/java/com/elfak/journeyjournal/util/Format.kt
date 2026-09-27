package com.elfak.journeyjournal.util

import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val dateTimeFormat = SimpleDateFormat("dd.MM.yyyy. HH:mm", Locale.getDefault())
private val dateFormat = SimpleDateFormat("dd.MM.yyyy.", Locale.getDefault())

fun Timestamp?.formatDateTime(): String =
    this?.toDate()?.let { dateTimeFormat.format(it) } ?: "-"

fun Long?.formatDate(): String =
    this?.let { dateFormat.format(Date(it)) } ?: "-"

fun Double.formatRating(): String = String.format(Locale.US, "%.1f", this)
