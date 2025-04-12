package com.sphereon.mdoc.example.app.features.sensor

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.juul.krayon.compose.ElementView
import com.sphereon.mdoc.example.app.features.sensor.chart.Sample
import com.sphereon.mdoc.example.app.features.sensor.chart.update
import kotlinx.coroutines.flow.Flow

@Composable
actual fun SensorDisplay(data: Flow<List<Sample>>, modifier: Modifier) {
    ElementView(data, ::update, modifier)
}
