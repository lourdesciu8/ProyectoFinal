package com.example.navegacion.ui.utils

import com.github.mikephil.charting.formatter.ValueFormatter

class DecimalValueFormatter : ValueFormatter() {
    override fun getFormattedValue(value: Float): String {
        return String.format("%.1f", value)
    }
}