package com.crmandino.supervisor.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.crmandino.supervisor.model.EtapaVenta
import com.crmandino.supervisor.ui.theme.EtapaCalificado
import com.crmandino.supervisor.ui.theme.EtapaContactado
import com.crmandino.supervisor.ui.theme.EtapaCotizado
import com.crmandino.supervisor.ui.theme.EtapaGanado
import com.crmandino.supervisor.ui.theme.EtapaNuevo
import com.crmandino.supervisor.ui.theme.EtapaPerdido

// Color asociado a cada etapa del embudo.
fun EtapaVenta.color(): Color = when (this) {
    EtapaVenta.NUEVO -> EtapaNuevo
    EtapaVenta.CONTACTADO -> EtapaContactado
    EtapaVenta.CALIFICADO -> EtapaCalificado
    EtapaVenta.COTIZADO -> EtapaCotizado
    EtapaVenta.GANADO -> EtapaGanado
    EtapaVenta.PERDIDO -> EtapaPerdido
}

// Color a partir del código guardado en Room ("NUEVO", "GANADO", ...).
fun colorEtapa(etapa: String): Color = EtapaVenta.desdeTexto(etapa).color()

// Etiqueta de etapa en cápsula de vidrio: texto + color (no depende solo del color).
@Composable
fun EtapaChip(etapa: String, modifier: Modifier = Modifier) {
    val e = EtapaVenta.desdeTexto(etapa)
    Capsula(e.etiqueta, e.color(), modifier)
}
