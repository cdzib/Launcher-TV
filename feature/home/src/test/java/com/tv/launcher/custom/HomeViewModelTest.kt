package com.tv.launcher.custom

import com.tv.launcher.custom.core.model.DomainResult
import com.tv.launcher.custom.core.model.apps.*
import com.tv.launcher.custom.core.ui.R
import com.tv.launcher.custom.feature.home.presentation.state.HomeEffect
import com.tv.launcher.custom.feature.home.presentation.state.HomeIntent
import com.tv.launcher.custom.feature.home.presentation.state.HomeUiState
import com.tv.launcher.custom.feature.home.presentation.viewmodel.HomeViewModel
import com.tv.launcher.custom.core.model.theme.*
import com.tv.launcher.custom.core.ui.theme.LayoutKind
import com.tv.launcher.custom.core.ui.theme.toStyle
import androidx.compose.ui.graphics.toArgb
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private class FakeApps(apps: List<AppEntry>) : AppsRepository {
        private val flow = MutableStateFlow(apps)
        override fun observeApps(): Flow<List<AppEntry>> = flow
        override suspend fun launch(packageName: String) = DomainResult.Success(Unit)
    }

    private class FakeTheme(initialAppearance: LauncherAppearance = LauncherAppearance()) : ThemeRepository {
        private val flow = MutableStateFlow(initialAppearance)
        var failNextAppearanceWrite = false
        val savedAppearances = mutableListOf<LauncherAppearance>()
        override fun observeTheme(): Flow<ThemeId> = flow.map { it.themeId }
        override suspend fun setTheme(themeId: ThemeId) { flow.value = flow.value.copy(themeId = themeId) }
        override fun observeAppearance(): Flow<LauncherAppearance> = flow
        override suspend fun setAppearance(appearance: LauncherAppearance) {
            if (failNextAppearanceWrite) {
                failNextAppearanceWrite = false
                throw IllegalStateException("Storage unavailable")
            }
            savedAppearances += appearance
            flow.value = appearance
        }
    }

    @Before fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())
    @After fun tearDown() = Dispatchers.resetMain()

    private fun viewModel(apps: List<AppEntry>, themeRepo: FakeTheme = FakeTheme()): HomeViewModel {
        val appsRepo = FakeApps(apps)
        return HomeViewModel(
            ObserveAppsUseCase(appsRepo), LaunchAppUseCase(appsRepo),
            ObserveAppearanceUseCase(themeRepo), SetAppearanceUseCase(themeRepo)
        )
    }

    @Test
    fun `un fallo al guardar conserva la apariencia y no contamina el siguiente cambio`() {
        val themeRepository = FakeTheme().apply { failNextAppearanceWrite = true }
        val viewModel = viewModel(listOf(AppEntry("a", "A")), themeRepository)
        val effects = mutableListOf<HomeEffect>()
        val effectJob = kotlinx.coroutines.CoroutineScope(Dispatchers.Main).launch { viewModel.effect.collect(effects::add) }

        viewModel.onIntent(HomeIntent.SelectAccent(AccentChoice.GREEN))

        assertEquals(AccentChoice.PROFILE, (viewModel.state.value as HomeUiState.Success).appearance.accent)
        assertEquals(listOf(HomeEffect.ShowMessage(R.string.appearance_save_failed)), effects)

        viewModel.onIntent(HomeIntent.SelectTileShape(TileShapeChoice.SQUARE))

        val savedAppearance = themeRepository.savedAppearances.single()
        assertEquals(AccentChoice.PROFILE, savedAppearance.accent)
        assertEquals(TileShapeChoice.SQUARE, savedAppearance.tileShape)
        effectJob.cancel()
    }

    @Test
    fun `seleccionar acceso guarda su posicion sin perder el tema ni los otros accesos`() {
        val vm = viewModel(listOf(AppEntry("a", "A"), AppEntry("b", "B")))
        vm.onIntent(HomeIntent.SelectTheme(ThemeId.WEBOS))
        vm.onIntent(HomeIntent.SelectShortcut(0, "b"))
        vm.onIntent(HomeIntent.SelectShortcut(4, "a"))
        val appearance = (vm.state.value as HomeUiState.Success).appearance
        assertEquals(mapOf(0 to "b", 4 to "a"), appearance.shortcuts)
        assertEquals(ThemeId.WEBOS, appearance.themeId)
    }

    @Test
    fun `no se guardan accesos fuera de rango ni apps no instaladas`() {
        val vm = viewModel(listOf(AppEntry("a", "A")))
        vm.onIntent(HomeIntent.SelectShortcut(-1, "a"))
        vm.onIntent(HomeIntent.SelectShortcut(5, "a"))
        vm.onIntent(HomeIntent.SelectShortcut(0, "missing"))
        assertTrue((vm.state.value as HomeUiState.Success).appearance.shortcuts.isEmpty())
    }

    @Test
    fun `vaciar un acceso conserva los otros accesos al cambiar tema`() {
        val vm = viewModel(listOf(AppEntry("a", "A")))
        vm.onIntent(HomeIntent.SelectShortcut(0, "a"))
        vm.onIntent(HomeIntent.SelectShortcut(1, "a"))
        vm.onIntent(HomeIntent.SelectShortcut(0, ""))
        vm.onIntent(HomeIntent.SelectTheme(ThemeId.FIRE_TV))
        assertEquals(mapOf(0 to "", 1 to "a"), (vm.state.value as HomeUiState.Success).appearance.shortcuts)
    }

    @Test
    fun `seleccionar fondo personalizado persiste uri sin borrar apariencia`() {
        val vm = viewModel(listOf(AppEntry("a", "A")))
        vm.onIntent(HomeIntent.SelectAccent(AccentChoice.GREEN))
        vm.onIntent(HomeIntent.SelectBackgroundImage("content://wallpaper/1"))
        val appearance = (vm.state.value as HomeUiState.Success).appearance
        assertEquals("content://wallpaper/1", appearance.backgroundImageUri)
        assertEquals(AccentChoice.GREEN, appearance.accent)
    }

    @Test
    fun `color solido borra imagen y permite volver al fondo integrado`() {
        val vm = viewModel(listOf(AppEntry("a", "A")))
        vm.onIntent(HomeIntent.SelectBackgroundImage("content://wallpaper/1"))
        vm.onIntent(HomeIntent.SelectBackgroundColor(0xFF123456))
        assertEquals(0xFF123456, (vm.state.value as HomeUiState.Success).appearance.backgroundColorArgb)
        vm.onIntent(HomeIntent.ClearCustomBackground)
        val appearance = (vm.state.value as HomeUiState.Success).appearance
        assertEquals(null, appearance.backgroundImageUri)
        assertEquals(null, appearance.backgroundColorArgb)
    }

    @Test
    fun `aplicar tema importado conserva los accesos webos existentes`() {
        val vm = viewModel(listOf(AppEntry("app.one", "One")))
        vm.onIntent(HomeIntent.SelectShortcut(0, "app.one"))
        val imported = LauncherAppearance(
            themeId = ThemeId.GOOGLE_TV,
            accentColorArgb = 0xFF123456L,
            backgroundColorArgb = 0xFF654321L
        )

        vm.onIntent(HomeIntent.ApplyImportedTheme(imported))

        val appearance = (vm.state.value as HomeUiState.Success).appearance
        assertEquals(ThemeId.GOOGLE_TV, appearance.themeId)
        assertEquals(0xFF123456L, appearance.accentColorArgb)
        assertEquals(mapOf(0 to "app.one"), appearance.shortcuts)
    }

    @Test
    fun `con apps instaladas el estado es Success`() {
        assertTrue(viewModel(listOf(AppEntry("a", "A"))).state.value is HomeUiState.Success)
    }

    @Test
    fun `sin apps el estado es Empty`() {
        assertTrue(viewModel(emptyList()).state.value is HomeUiState.Empty)
    }

    @Test
    fun `CycleTheme avanza al siguiente tema`() {
        val vm = viewModel(listOf(AppEntry("a", "A")))
        vm.onIntent(HomeIntent.CycleTheme)
        assertEquals(ThemeId.FIRE_TV, vm.state.value.themeId)
    }

    @Test
    fun `cambiar de tema descarta color de fondo solido anterior`() {
        val repository = FakeTheme(LauncherAppearance(backgroundColorArgb = 0xFFFF0000L))
        val vm = viewModel(listOf(AppEntry("a", "A")), repository)

        vm.onIntent(HomeIntent.SelectTheme(ThemeId.ROKU))

        val appearance = (vm.state.value as HomeUiState.Success).appearance
        assertEquals(ThemeId.ROKU, appearance.themeId)
        assertEquals(null, appearance.backgroundColorArgb)
        assertEquals(0xFF35134D.toInt(), appearance.toStyle().palette.background.toArgb())
    }

    @Test
    fun `los perfiles Android TV y Google TV tienen estilos visuales`() {
        assertEquals(LayoutKind.ANDROID_GRID, ThemeId.ANDROID_TV.toStyle().layout)
        assertEquals(LayoutKind.GOOGLE_HOME, ThemeId.GOOGLE_TV.toStyle().layout)
        assertEquals(LayoutKind.APPLE_HOME, ThemeId.APPLE_TV.toStyle().layout)
        assertEquals(LayoutKind.TIZEN_HOME, ThemeId.TIZEN.toStyle().layout)
    }

    @Test
    fun `seleccionar acento actualiza la apariencia`() {
        val vm = viewModel(listOf(AppEntry("a", "A")))
        vm.onIntent(HomeIntent.SelectAccent(AccentChoice.GREEN))
        assertEquals(AccentChoice.GREEN, (vm.state.value as HomeUiState.Success).appearance.accent)
    }

    @Test
    fun `cambios consecutivos conservan los overrides anteriores`() {
        val repository = FakeTheme()
        val vm = viewModel(listOf(AppEntry("a", "A")), repository)
        vm.onIntent(HomeIntent.SelectTheme(ThemeId.GOOGLE_TV))
        vm.onIntent(HomeIntent.SelectAccent(AccentChoice.GREEN))
        val appearance = (vm.state.value as HomeUiState.Success).appearance
        assertEquals(ThemeId.GOOGLE_TV, appearance.themeId)
        assertEquals(AccentChoice.GREEN, appearance.accent)
        assertEquals(2, repository.savedAppearances.size)
        assertEquals(ThemeId.GOOGLE_TV, repository.savedAppearances.first().themeId)
        assertEquals(ThemeId.GOOGLE_TV, repository.savedAppearances.last().themeId)
        assertEquals(AccentChoice.GREEN, repository.savedAppearances.last().accent)
    }

    @Test
    fun `overrides de apariencia se reflejan en el estilo`() {
        val style = LauncherAppearance(
            themeId = ThemeId.GOOGLE_TV,
            accent = AccentChoice.GREEN,
            background = BackgroundChoice.MIDNIGHT,
            tileShape = TileShapeChoice.SQUARE
        ).toStyle()
        assertEquals(0xFF81C784.toInt(), style.palette.accent.toArgb())
        assertEquals(0xFF05070B.toInt(), style.palette.background.toArgb())
        assertEquals(4f, style.tileRadius.value, 0f)
    }
}
