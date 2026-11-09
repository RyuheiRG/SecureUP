package com.secureup.app

import com.secureup.app.R
import com.secureup.app.domain.repository.BreachFailureKind
import com.secureup.app.domain.repository.BreachRepository
import com.secureup.app.domain.repository.BreachResult
import com.secureup.app.viewmodel.BreachCheckOutcome
import com.secureup.app.viewmodel.BreachViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BreachViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(dispatcher)

    private val repository = RecordingBreachRepository()
    private val viewModel = BreachViewModel(repository, dispatcher)

    @Test
    fun blankPassword_showsErrorAndDoesNotCallRepository() = runTest(dispatcher) {
        viewModel.onPasswordChanged("   ")
        viewModel.checkPassword()
        advanceUntilIdle()

        assertEquals(R.string.breach_error_password_required, viewModel.uiState.value.errorRes)
        assertEquals(0, repository.calls)
    }

    @Test
    fun foundPassword_exposesOccurrencesAndClearsPassword() = runTest(dispatcher) {
        repository.result = BreachResult.Found(42)
        viewModel.onPasswordChanged("password")
        viewModel.checkPassword()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(BreachCheckOutcome.Found(42), state.outcome)
        assertEquals("", state.password)
        assertFalse(state.isLoading)
        assertNull(state.errorRes)
    }

    @Test
    fun notFound_exposesNotFoundOutcome() = runTest(dispatcher) {
        repository.result = BreachResult.NotFound
        viewModel.onPasswordChanged("password")
        viewModel.checkPassword()
        advanceUntilIdle()

        assertEquals(BreachCheckOutcome.NotFound, viewModel.uiState.value.outcome)
        assertEquals("", viewModel.uiState.value.password)
    }

    @Test
    fun networkFailure_showsNetworkErrorAndClearsPassword() = runTest(dispatcher) {
        repository.result = BreachResult.Failure(BreachFailureKind.NETWORK)
        viewModel.onPasswordChanged("password")
        viewModel.checkPassword()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(R.string.breach_error_network, state.errorRes)
        assertNull(state.outcome)
        assertEquals("", state.password)
    }

    @Test
    fun checkPassword_setsLoadingBeforeRepositoryReturns() = runTest(dispatcher) {
        viewModel.onPasswordChanged("password")
        viewModel.checkPassword()

        assertTrue(viewModel.uiState.value.isLoading)

        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.isLoading)
    }

    private class RecordingBreachRepository : BreachRepository {
        var result: BreachResult = BreachResult.NotFound
        var calls = 0

        override suspend fun checkPassword(password: String): BreachResult {
            calls++
            return result
        }
    }
}
