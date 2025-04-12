package com.sphereon.mdoc.example.app.features.sensor

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.sphereon.mdoc.example.app.features.sensor.chart.Sample
import kotlinx.coroutines.flow.Flow

@Composable
expect fun SensorDisplay(data: Flow<List<Sample>>, modifier: Modifier = Modifier)
