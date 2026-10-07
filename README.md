# CRM Andino Supervisor 📊 — Evaluación Parcial Semana 8
App del **supervisor comercial** del caso CRM Andino Perú (Jean Piero Huamani Rodriguez).
Kotlin · Jetpack Compose · **MVVM** · **Room** · **Retrofit** · **Repository** · **WorkManager (SyncWorker)** · EncryptedSharedPreferences.

Consulta el embudo (valor por etapa, dibujado con **Canvas**) y las tareas pendientes por vendedor desde Room; **edita, reasigna y elimina** leads con o sin conexión. Los cambios se guardan en una cola local y se sincronizan con la API REST (`CRMAndinoAPI`), sin duplicados y con detección de conflictos.

- Guía de sustentación (RF, tablas, pruebas): [`SUSTENTACION.md`](SUSTENTACION.md)
- Contexto técnico: [`CONTEXTO.md`](CONTEXTO.md)

## Ejecutar
1. Encender la API (`CRMAndinoAPI` → `npm start`).
2. Android Studio → **File → Open** → carpeta `CRMAndinoSupervisor` → Gradle Sync → ▶ Run en el **emulador**.
3. Ingresar con `supervisor@crmandino.pe` / `super123` (servidor por defecto `http://10.0.2.2:3000/`, editable en el Login).
4. APK: **Build → Generate App Bundles or APKs → Generate APKs**.
