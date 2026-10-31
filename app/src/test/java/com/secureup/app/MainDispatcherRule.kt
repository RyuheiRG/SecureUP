package com.secureup.app

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/**
 * Reemplaza `Dispatchers.Main` (que no existe en JVM) por un dispatcher de prueba.
 *
 * Con `StandardTestDispatcher` las corrutinas quedan encoladas y solo avanzan
 * cuando la prueba llama a `advanceUntilIdle()`. Eso permite comprobar el estado
 * intermedio (`isLoading = true`) y luego el final, sin esperas reales.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    val testDispatcher: TestDispatcher = StandardTestDispatcher(),
) : TestWatcher() {

    override fun starting(description: Description) {
        Dispatchers.setMain(testDispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}
