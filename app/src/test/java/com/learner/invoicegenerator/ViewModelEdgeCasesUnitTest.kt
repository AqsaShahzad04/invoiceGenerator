package com.learner.invoicegenerator

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.learner.invoicegenerator.data.local.SessionManager
import com.learner.invoicegenerator.data.local.entity.Client
import com.learner.invoicegenerator.data.local.entity.Invoice
import com.learner.invoicegenerator.data.local.entity.InvoiceItemLine
import com.learner.invoicegenerator.data.local.entity.Item
import com.learner.invoicegenerator.data.local.entity.User
import com.learner.invoicegenerator.data.local.entity.Workspace
import com.learner.invoicegenerator.data.repository.ClientRepository
import com.learner.invoicegenerator.data.repository.InvoiceRepository
import com.learner.invoicegenerator.data.repository.ItemRepository
import com.learner.invoicegenerator.data.repository.UserRepository
import com.learner.invoicegenerator.data.repository.WorkspaceRepository
import com.learner.invoicegenerator.data.repository.WorkspaceSettingsRepository
import com.learner.invoicegenerator.ui.auth.ViewModel.InvoiceViewModel
import com.learner.invoicegenerator.ui.auth.ViewModel.ItemState
import com.learner.invoicegenerator.ui.auth.ViewModel.ItemViewModel
import com.learner.invoicegenerator.ui.auth.ViewModel.LoginState
import com.learner.invoicegenerator.ui.auth.ViewModel.LoginViewModel
import com.learner.invoicegenerator.ui.auth.ViewModel.SignUPViewModel
import com.learner.invoicegenerator.ui.auth.ViewModel.SignUpState
import com.learner.invoicegenerator.ui.auth.ViewModel.WorkspaceState
import com.learner.invoicegenerator.ui.auth.ViewModel.WorkspaceViewModel
import com.learner.invoicegenerator.ui.clients.viewmodel.ClientState
import com.learner.invoicegenerator.ui.clients.viewmodel.ClientViewModel
import com.learner.invoicegenerator.utils.PasswordHasher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class ViewModelEdgeCasesUnitTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun loginViewModel_validCredentials_returnsSuccess() {
        runTest {
            val userRepository = mock(UserRepository::class.java)
            val hashedPass = PasswordHasher.hash("password123")
            val user = User(id = 1, name = "Test User", email = "test@example.com", password = hashedPass)
            `when`(userRepository.getUserByEmail("test@example.com")).thenReturn(user)

            val viewModel = LoginViewModel(userRepository, testDispatcher)
            viewModel.loginUser("test@example.com", "password123")
            testDispatcher.scheduler.advanceUntilIdle()

            assertTrue(viewModel.loginState.value is LoginState.Success)
            assertEquals(user, (viewModel.loginState.value as LoginState.Success).user)
        }
    }

    @Test
    fun loginViewModel_invalidPassword_returnsError() {
        runTest {
            val userRepository = mock(UserRepository::class.java)
            val hashedPass = PasswordHasher.hash("password123")
            val user = User(id = 1, name = "Test User", email = "test@example.com", password = hashedPass)
            `when`(userRepository.getUserByEmail("test@example.com")).thenReturn(user)

            val viewModel = LoginViewModel(userRepository, testDispatcher)
            viewModel.loginUser("test@example.com", "wrongpassword")
            testDispatcher.scheduler.advanceUntilIdle()

            assertTrue(viewModel.loginState.value is LoginState.Error)
        }
    }

    @Test
    fun signUpViewModel_existingEmail_returnsError() {
        runTest {
            val userRepository = mock(UserRepository::class.java)
            val existingUser = User(id = 1, name = "Existing", email = "existing@example.com", password = "pass")
            `when`(userRepository.getUserByEmail("existing@example.com")).thenReturn(existingUser)

            val viewModel = SignUPViewModel(userRepository, testDispatcher)
            viewModel.signUp(User(name = "New", email = "existing@example.com", password = "pass"))
            testDispatcher.scheduler.advanceUntilIdle()

            assertTrue(viewModel.signUpState.value is SignUpState.Error)
            assertEquals("An account with this email already exists", (viewModel.signUpState.value as SignUpState.Error).message)
        }
    }

    @Test
    fun clientViewModel_addClient_veryLongNameAndEmojis() {
        runTest {
            val clientRepository = mock(ClientRepository::class.java)
            val sessionManager = mock(SessionManager::class.java)
            `when`(sessionManager.activeWorkspaceId).thenReturn(MutableStateFlow(1))

            val client = Client(
                businessName = "🚀 Very Long Enterprise Business Name With Emojis 💼 & Special Chars @#$!",
                contactPerson = "John Doe",
                email = "john@enterprise.com",
                phone = "+1234567890",
                address = "123 Business Way",
                workspaceId = 1
            )
            `when`(clientRepository.insertClient(client, 1)).thenReturn(101L)

            val viewModel = ClientViewModel(clientRepository, sessionManager)
            viewModel.addClient(client, 1)
            testDispatcher.scheduler.advanceUntilIdle()

            assertTrue(viewModel.addClientState.value is ClientState.Success)
        }
    }

    @Test
    fun itemViewModel_addItem_zeroPrice() {
        runTest {
            val itemRepository = mock(ItemRepository::class.java)
            val sessionManager = mock(SessionManager::class.java)
            `when`(sessionManager.activeWorkspaceId).thenReturn(MutableStateFlow(1))

            val item = Item(
                itemName = "Free Sample Item",
                barcode = "12345678",
                price = 0.0,
                unit = "piece",
                category = "Sample",
                workspaceId = 1
            )
            `when`(itemRepository.insertItem(item, 1)).thenReturn(201L)

            val viewModel = ItemViewModel(itemRepository, sessionManager)
            viewModel.addItems(item, 1)
            testDispatcher.scheduler.advanceUntilIdle()

            assertTrue(viewModel.itemState.value is ItemState.Success)
            assertEquals(201, viewModel.lastAddedItemId.value)
        }
    }

    @Test
    fun workspaceViewModel_addWorkspace_returnsSuccess() {
        runTest {
            val workspaceRepository = mock(WorkspaceRepository::class.java)
            val settingsRepository = mock(WorkspaceSettingsRepository::class.java)
            val sessionManager = mock(SessionManager::class.java)
            `when`(sessionManager.activeWorkspaceId).thenReturn(MutableStateFlow(1))

            val workspace = Workspace(name = "Ayesha Enterprise 🏢", ownerUserId = 1)

            `when`(workspaceRepository.insertWorkspace(workspace)).thenReturn(5L)

            val viewModel = WorkspaceViewModel(workspaceRepository, sessionManager, settingsRepository)
            viewModel.addWorkspace(workspace)
            testDispatcher.scheduler.advanceUntilIdle()

            assertTrue(viewModel.workspaceState.value is WorkspaceState.Success)
            assertEquals(5, (viewModel.workspaceState.value as WorkspaceState.Success).id)
        }
    }

    @Test
    fun invoiceViewModel_addAndModifySelectedItems() {
        runTest {
            val invoiceRepository = mock(InvoiceRepository::class.java)
            val sessionManager = mock(SessionManager::class.java)
            `when`(sessionManager.activeWorkspaceId).thenReturn(MutableStateFlow(1))

            val viewModel = InvoiceViewModel(invoiceRepository, sessionManager)

            val lineItem = InvoiceItemLine(id = 0, invoiceId = 0, itemId = 10, itemName = "Consulting", unitPrice = 100.0, itemQuantity = 1.0, itemUnit = "hrs")

            viewModel.addToSelectedItems(lineItem)
            assertEquals(1, viewModel.selectedItems.value.size)

            viewModel.incrementQuantity(10)
            assertEquals(2.0, viewModel.selectedItems.value[0].itemQuantity, 0.001)

            viewModel.decrementQuantity(10)
            assertEquals(1.0, viewModel.selectedItems.value[0].itemQuantity, 0.001)

            viewModel.removeFromSelectedItems(10)
            assertTrue(viewModel.selectedItems.value.isEmpty())
        }
    }

    @Test
    fun invoiceViewModel_draftStateManagement() {
        runTest {
            val invoiceRepository = mock(InvoiceRepository::class.java)
            val sessionManager = mock(SessionManager::class.java)
            `when`(sessionManager.activeWorkspaceId).thenReturn(MutableStateFlow(1))

            val viewModel = InvoiceViewModel(invoiceRepository, sessionManager)

            val draft = Invoice(
                invoiceNum = "INV-2026-0001",
                workspaceId = 1,
                clientId = 10,
                clientBusinessName = "Test Client",
                status = "Pending",
                issueDate = LocalDate.now(),
                dueDate = LocalDate.now().plusDays(14),
                currencyCode = "USD",
                taxPercentage = 10.0,
                discountType = "Percent",
                discountValue = 5.0,
                endNote = "Thanks",
                totalAmount = 95.0,
                templateId = 2
            )

            viewModel.updateInvoiceDraft(draft)
            assertEquals("INV-2026-0001", viewModel.invoiceDraft.value?.invoiceNum)
            assertEquals(2, viewModel.selectedTemplate.value)

            viewModel.setSelectedTemplate(3)
            assertEquals(3, viewModel.selectedTemplate.value)
            assertEquals(3, viewModel.invoiceDraft.value?.templateId)

            viewModel.resetInvoiceDraft()
            assertEquals(null, viewModel.invoiceDraft.value)
            assertEquals(1, viewModel.selectedTemplate.value)
        }
    }
}
