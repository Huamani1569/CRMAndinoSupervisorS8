package com.crmandino.supervisor.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.crmandino.supervisor.data.remote.dto.CeldaCalorDto
import com.crmandino.supervisor.data.remote.dto.PuntoVentasDto
import com.crmandino.supervisor.ui.theme.AzulAndino
import com.crmandino.supervisor.ui.theme.DoradoInca
import com.crmandino.supervisor.ui.theme.RojoError
import com.crmandino.supervisor.ui.theme.TextoSecundario
import com.crmandino.supervisor.ui.theme.TurquesaLago
import com.crmandino.supervisor.util.Formato

// Gráficos del CRM analítico dibujados con Canvas (sin librerías externas).

// Ventas reales por mes (barras turquesa) + pronóstico del mes actual (barra dorada) + línea punteada de la meta.
@Composable
fun GraficoVentas(serie: List<PuntoVentasDto>, meta: Double?, modifier: Modifier = Modifier) {
    if (serie.isEmpty()) return
    val maximo = (serie.maxOf { maxOf(it.real ?: 0.0, it.pronostico ?: 0.0) }).coerceAtLeast(meta ?: 0.0).coerceAtLeast(1.0)
    Column(modifier) {
        Canvas(Modifier.fillMaxWidth().height(170.dp)) {
            val n = serie.size
            val ancho = size.width / n
            val barra = ancho * 0.56f
            serie.forEachIndexed { i, p ->
                val x = i * ancho + (ancho - barra) / 2
                val pron = p.pronostico
                if (pron != null) {
                    val h = (pron / maximo).toFloat() * size.height
                    drawRoundRect(DoradoInca.copy(alpha = 0.35f), Offset(x, size.height - h), Size(barra, h), CornerRadius(10f, 10f))
                }
                val h = ((p.real ?: 0.0) / maximo).toFloat() * size.height
                drawRoundRect(if (pron != null) DoradoInca else TurquesaLago, Offset(x, size.height - h), Size(barra, h), CornerRadius(10f, 10f))
            }
            if (meta != null && meta > 0) {
                val y = size.height - (meta / maximo).toFloat() * size.height
                drawLine(RojoError, Offset(0f, y), Offset(size.width, y), strokeWidth = 3f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 10f)))
            }
        }
        Row(Modifier.fillMaxWidth()) {
            serie.forEach {
                Text(nombreMes(it.mes), style = MaterialTheme.typography.labelMedium, color = TextoSecundario,
                    modifier = Modifier.weight(1f), maxLines = 1, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            }
        }
        Row(Modifier.padding(top = 6.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Leyenda(TurquesaLago, "Vendido")
            Leyenda(DoradoInca, "Mes actual + pronóstico")
            if (meta != null) Leyenda(RojoError, "Meta ${Formato.solesCorto(meta)}")
        }
    }
}

private fun nombreMes(iso: String): String {
    val meses = listOf("Ene", "Feb", "Mar", "Abr", "May", "Jun", "Jul", "Ago", "Set", "Oct", "Nov", "Dic")
    val m = iso.takeLast(2).toIntOrNull() ?: return iso
    return meses.getOrElse(m - 1) { iso }
}

@Composable
fun Leyenda(color: Color, texto: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(10.dp).clip(RoundedCornerShape(50)).background(color))
        Spacer(Modifier.width(4.dp))
        Text(texto, style = MaterialTheme.typography.labelMedium, color = TextoSecundario)
    }
}

// Barra de progreso en cápsula (pronóstico vs. meta, probabilidad de cancelación...).
@Composable
fun BarraProgreso(progreso: Float, color: Color, modifier: Modifier = Modifier, alto: androidx.compose.ui.unit.Dp = 10.dp) {
    Box(modifier.fillMaxWidth().height(alto).clip(RoundedCornerShape(50)).background(color.copy(alpha = 0.15f))) {
        Box(Modifier.fillMaxWidth(progreso.coerceIn(0f, 1f)).fillMaxHeight().clip(RoundedCornerShape(50)).background(color))
    }
}

// Barras horizontales con etiqueta y valor (pronóstico por vendedor, margen por segmento...).
@Composable
fun BarrasHorizontales(datos: List<Pair<String, Double>>, color: Color, formato: (Double) -> String, modifier: Modifier = Modifier) {
    val maximo = datos.maxOfOrNull { it.second }?.coerceAtLeast(1.0) ?: 1.0
    Column(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        datos.forEach { (etiqueta, valor) ->
            Column {
                Row {
                    Text(etiqueta, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(formato(valor), style = MaterialTheme.typography.labelMedium, color = AzulAndino)
                }
                BarraProgreso((valor / maximo).toFloat(), color, Modifier.padding(top = 3.dp), 8.dp)
            }
        }
    }
}

// Mapa de calor de solicitudes de servicio por día de la semana y hora (para planificar turnos de agentes).
@Composable
fun MapaCalor(celdas: List<CeldaCalorDto>, modifier: Modifier = Modifier) {
    val horas = 7..21
    val dias = listOf("Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom")
    val maximo = celdas.maxOfOrNull { it.n }?.coerceAtLeast(1) ?: 1
    val valor = celdas.associate { (it.dia to it.hora) to it.n }
    Column(modifier) {
        dias.forEachIndexed { i, d ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(d, style = MaterialTheme.typography.labelMedium, color = TextoSecundario, modifier = Modifier.width(32.dp))
                horas.forEach { h ->
                    val n = valor[(i + 1) to h] ?: 0
                    Box(
                        Modifier.weight(1f).height(16.dp).padding(1.dp).clip(RoundedCornerShape(4.dp))
                            .background(if (n == 0) AzulAndino.copy(alpha = 0.05f) else TurquesaLago.copy(alpha = 0.15f + 0.85f * n / maximo))
                    )
                }
            }
        }
        Row {
            Spacer(Modifier.width(32.dp))
            horas.forEach { h ->
                Text(if (h % 3 == 0) "$h" else "", style = MaterialTheme.typography.labelMedium, color = TextoSecundario, modifier = Modifier.weight(1f), maxLines = 1)
            }
        }
    }
}
