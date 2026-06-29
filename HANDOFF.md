# Vuelvo Comercios — Handoff de desarrollo

> Última actualización: 2026-06-29. Documento para retomar la sesión sin re-derivar contexto.
> App Android (Jetpack Compose) que implementa el diseño **Vuelvo Comercios** (lado comercios/merchant).

---

## 0. Actualización 2026-06-29 — UUID de instalación + registro de suscripción en Firestore

- **UUID por instalación**: `data/DeviceIdStore.kt` (SharedPreferences `vuelvo_device`, clave
  `device_uuid`). Se crea en el primer arranque vía `MainActivity.onCreate` (`getOrCreate()`, que
  además sirve de fallback: genera uno si no existe) y se reutiliza. Se inyecta a `BizApp` y se
  escribe en el tag como **nuevo parámetro `uuid`** del deeplink — `TagConfig.kt` `deeplinkUrl`
  pasó de *property* a **función** `deeplinkUrl(deviceUuid: String)`.
- **Registro en Firestore al suscribirse**: `data/SubscriptionRepository.kt` (clase dedicada, **no
  singleton**; se construye con `Firebase.firestore` en `MainActivity` y se inyecta a `BizApp`).
  Cuando la suscripción pasa a activa (`LaunchedEffect(subscribed)` en `BizApp`) hace un upsert en
  la colección `subscriptions`, doc id = `uuid`, con campos `uuid` y `subscriptionEndDate`
  (Firestore `Timestamp`). La fecha de fin se deriva del plan (`SubscriptionPlan.endDateFromNow()`,
  now + `durationMonths`), ya que Play Billing no expone el expiry en cliente.
- Dependencia `firebase-firestore` activada en `app/build.gradle.kts`. `await` resuelto con
  `suspendCancellableCoroutine` (sin añadir `kotlinx-coroutines-play-services`).

---

## 0 (prev). Actualización 2026-06-24 — Logo, fondo y color de tarjeta (handoff-4)

La pantalla de configuración del tag NFC (`ConfigScreen`) gana **tres opciones** del handoff
`Vuelvo Comercios.html` (jsx `vuelvo-biz-config.jsx`):

1. **Foto / logo del comercio** → `TagForm.logo`.
2. **Imagen de fondo de la tarjeta** → `TagForm.cover`.
3. **Color de la tarjeta** → `TagForm.color` (paleta compartida `CardColors`).

Detalles de implementación:
- Las imágenes se eligen con el **Photo Picker** del sistema (`ActivityResultContracts.PickVisualMedia`,
  sin permisos en runtime) y se guardan **ya como string Base64** (url-safe, sin padding) en el form,
  no como `Uri`. El codificador/decodificador es `ui/biz/TagImageCodec.kt`: reescala (logo 96 px,
  fondo 220 px), respeta EXIF, comprime a JPEG y codifica. Una sola fuente de verdad para preview,
  overlay y tag.
- **Contrato del deeplink NFC** (`TagConfig.kt` `deeplinkUrl`) — ahora:
  `vuelvo://stamp?id&name&cat&sym&color&tile&ink&max&reward&logo&cover`
  - `color` = id de la paleta (`cafe|amber|rose|coral|mint|sky|violet|ink`) → **token cross-platform**;
    iOS debe compartir la misma tabla `CARD_COLORS` y resolver por id. `tile`/`ink` son el hex RRGGBB
    resuelto (fallback para clientes que no conozcan el id). Antes `tile`/`ink` salían del *tipo*; ahora
    del **color elegido**.
  - `logo` / `cover` = imágenes como **cadena Base64**, añadidas al final y solo si existen.
- ⚠️ **Capacidad NFC**: logo + fondo en Base64 son varios KB; **no caben** en tags pequeños
  (NTAG213 ≈137 B, NTAG215 ≈492 B). Para incluir ambas imágenes hace falta NTAG216 (≈868 B) o, en
  producción, alojar las imágenes y escribir solo URLs. `NfcTagWriter` ya falla con gracia
  ("El tag no tiene capacidad suficiente"). Los perfiles de compresión están en `TagImageCodec`
  (`LOGO` / `COVER`) por si se quieren afinar.

---

## 1. Qué es esto

`vuelvo_commerce` (paquete `com.delta.vuelvo_commerce`, minSdk 26, compileSdk 36) implementa la
app **Vuelvo Comercios**: una app de fidelización por sellos NFC para comercios.

Fuente del diseño: bundle de **Claude Design** (claude.ai/design) en HTML/CSS/JS:
`C:\Users\newge\Desktop\vuelvo\Vuelvo-commerce\vuelvo\`
- `README.md` — instrucciones del handoff (diseño primario = `project/VuelvoComercios.html`).
- `project/*.jsx` — prototipos React (los leímos enteros y replicamos en Compose).
- `project/icon-comercios-B.svg` — origen del icono de la app (variante oscura).

> Ojo: el bundle también trae una **app de consumidor** (`Vuelvo.html` + `vuelvo-app/cards/scan/rewards.jsx`).
> NO está implementada — solo se construyó el lado Comercios.

---

## 2. Estado actual — HECHO ✅

- Tema y tokens de diseño (morado `#9B5CFF` / `#7B3CE6`, fondos, ink, etc.).
- Set de iconos line recreado desde los mismos paths SVG → `ImageVector` (`addPathNodes`).
- Logo de cabecera como vector drawable (`res/drawable/ic_vuelvo_logo.xml`).
- Componente de tarjeta de sellos (punch-card grid) con degradado radial.
- 3 pantallas: **ConfigScreen** (nuevo tag NFC), **PaywallScreen** (planes / suscripción activa),
  **WriteOverlay** (animación de escritura NFC: anillos → spinner → check).
- Shell **BizApp**: barra de tabs glassy (Escribir tag / Planes·Suscripción), toast, estado.
- **Icono de la app** desde `icon-comercios-B.svg` → adaptive icon (background + foreground + monochrome).
- `assembleDebug` compila correctamente. Build 100% offline (sin Coil/red).

---

## 3. Pendiente / próximos pasos 🔜

| Prioridad | Tarea | Notas |
|-----------|-------|-------|
| - | Verificar visualmente en emulador | Aún no se ha lanzado; usar `/run` o `gradlew installDebug`. |
| Media | Bundlear **Plus Jakarta Sans** | Ahora usa `FontFamily.Default`. Swap en `ui/theme/Type.kt` (`VuFont`). |
| Baja | Escalar foreground del icono | La marca queda algo pequeña dentro de la safe zone (fiel al SVG). |
| Opcional | App **consumidor** (Vuelvo.html) | scan/cards/rewards — no empezada. |
| Futuro | NFC real | El WriteOverlay es solo animación; no escribe tags de verdad. |
| Futuro | Persistencia de datos | El estado es in-memory (`remember`), se pierde al cerrar. |

---

## 4. Mapa de archivos

Todo bajo `app/src/main/java/com/delta/vuelvo_commerce/`:

```
MainActivity.kt                 # enableEdgeToEdge + Surface(VuBg) + BizApp()
ui/
  theme/
    Color.kt                    # tokens Vu* (accent, ink, bg, line, stamp...)
    Type.kt                     # Typography + VuFont (system sans, swap aquí)
    Theme.kt                    # Vuelvo_commerceTheme — light-only, sin dynamic color
  VuelvoIcons.kt                # iconos line (SVG path -> ImageVector vía addPathNodes)
  components/
    Stamps.kt                   # rejilla de sellos (stampCols + Stamp)
  biz/
    BizData.kt                  # BizType, BizPlan, BizPerks, CardColor/CardColors, TagForm + helpers byId
    TagImageCodec.kt            # logo/fondo: Uri -> Base64 string (reescala+JPEG) y decode para previews
    BizWidgets.kt               # BizHeader, FieldLabel
    ConfigScreen.kt             # form tag NFC + TypeGrid/Stepper/LivePreview + GradientButton/InkButton/AccentGradient
    PaywallScreen.kt            # PaywallOffer + ActiveSubscription + PlanRow
    WriteOverlay.kt             # bottom-sheet animación NFC
    BizApp.kt                   # shell: tabs, toast, estado (subscribed/activePlan/form/writing)
```

Recursos:
```
res/drawable/ic_vuelvo_logo.xml          # logo cabecera (V reciclaje, gradiente claro)
res/drawable/ic_launcher_background.xml  # tile oscuro del icono app
res/drawable/ic_launcher_foreground.xml  # V morada + arcos (icono app)
res/drawable/ic_launcher_monochrome.xml  # silueta themed-icon
res/mipmap-anydpi/ic_launcher*.xml       # adaptive icon (apunta a los 3 drawables)
res/values/strings.xml                   # app_name = "Vuelvo Comercios"
```

---

## 5. Decisiones clave / convenciones

- **Iconos**: se reconstruyen con los mismos `d` del SVG vía `addPathNodes(...)` →
  silueta idéntica. `circle`/`rect` convertidos a paths con arcos `a`. Tinte vía `Icon(tint=...)`.
- **Colores derivados**: `VuAccentSoft` = accent @12%, `VuAccentLine` = accent @28% (igual que el CSS).
- **Sin dark mode ni dynamic color**: la marca es light-only fija.
- **Edge-to-edge**: las pantallas llevan su propio `padding(top = 58.dp)` en el header
  (equiv. al `paddingTop: 58` del prototipo); la tab bar suma `navigationBars` inset.
- **El harness iOS (`ios-frame.jsx`, `tweaks-panel.jsx`) NO se replica** — es el marco de
  preview del prototipo, no parte de la app.

---

## 6. Cómo construir / ejecutar

```powershell
# Compilar solo Kotlin (rápido, para iterar)
.\gradlew.bat :app:compileDebugKotlin --console=plain

# APK debug completo (valida también recursos/iconos)
.\gradlew.bat :app:assembleDebug --console=plain

# Instalar en emulador/dispositivo
.\gradlew.bat :app:installDebug
```
Entorno: Windows + PowerShell, Java 23, AGP 8.13.2, Kotlin 2.0.21, Compose BOM 2024.09.00.
No es repo git.

---

## 7. Gotchas encontrados (para no repetir)

- `calculateBottomPadding()` es **miembro** de `PaddingValues`, no un import de
  `androidx.compose.foundation.layout` → no lo importes, llámalo directo.
- `Brush.radialGradient(...)` para los sellos: usar la sobrecarga con color stops
  sin `center`/`radius` absolutos (se centra solo al tamaño del círculo).
- Adaptive icon: la máscara recorta ~17% por lado; el contenido del foreground debe
  quedar dentro de la safe zone central.
