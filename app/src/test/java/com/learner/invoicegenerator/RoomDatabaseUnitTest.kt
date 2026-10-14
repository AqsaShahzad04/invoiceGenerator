package com.learner.invoicegenerator

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.learner.invoicegenerator.data.local.Dao.Clientdao
import com.learner.invoicegenerator.data.local.Dao.InvoiceDao
import com.learner.invoicegenerator.data.local.Dao.ItemDao
import com.learner.invoicegenerator.data.local.Dao.Userdao
import com.learner.invoicegenerator.data.local.Dao.WorkspaceDao
import com.learner.invoicegenerator.data.local.Dao.WorkspaceSettingsDao
import com.learner.invoicegenerator.data.local.InvoiceDatabase
import com.learner.invoicegenerator.data.local.entity.Client
import com.learner.invoicegenerator.data.local.entity.Invoice
import com.learner.invoicegenerator.data.local.entity.Item
import com.learner.invoicegenerator.data.local.entity.User
import com.learner.invoicegenerator.data.local.entity.Workspace
import com.learner.invoicegenerator.data.local.entity.WorkspaceSettings
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.LocalDate

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class RoomDatabaseUnitTest {

    private lateinit var db: InvoiceDatabase
    private lateinit var userDao: Userdao
    private lateinit var clientDao: Clientdao
    private lateinit var itemDao: ItemDao
    private lateinit var workspaceDao: WorkspaceDao
    private lateinit var invoiceDao: InvoiceDao
    private lateinit var settingsDao: WorkspaceSettingsDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, InvoiceDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        userDao = db.userDao()
        clientDao = db.clientDao()
        itemDao = db.itemDao()
        workspaceDao = db.workspaceDao()
        invoiceDao = db.invoiceDao()
        settingsDao = db.workspaceSettingsDao()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun insertUserAndGetByEmail() = runBlocking {
        val user = User(id = 0, name = "John Doe", email = "john@example.com", password = "hashed_password")
        userDao.insertUser(user)

        val retrieved = userDao.getUserByEmail("john@example.com")
        assertNotNull(retrieved)
        assertEquals("John Doe", retrieved?.name)
    }

    @Test
    fun workspaceAndSettings_insertAndRetrieve() = runBlocking {
        val user = User(id = 0, name = "Owner", email = "owner@example.com", password = "pass")
        userDao.insertUser(user)
        val insertedUser = userDao.getUserByEmail("owner@example.com")!!

        val workspace = Workspace(id = 0, name = "Ayesha Enterprise 💼", ownerUserId = insertedUser.id)
        val wsId = workspaceDao.insert(workspace).toInt()

        val settings = WorkspaceSettings(workspaceId = wsId, invoicePrefix = "INV-2026-", taxRate = 18.0)
        settingsDao.insertDefaultSettings(settings)

        val retrievedSettings = settingsDao.getSettingsByWorkspaceId(wsId).first()
        assertNotNull(retrievedSettings)
        assertEquals("INV-2026-", retrievedSettings?.invoicePrefix)
        assertEquals(18.0, retrievedSettings?.taxRate ?: 0.0, 0.001)
    }

    @Test
    fun clientAndItem_insertUpdateDelete() = runBlocking {
        val client = Client(id = 0, businessName = "ACME Corp", contactPerson = "Alice", email = "alice@acme.com", phone = "123", address = "Main St", workspaceId = 1)
        val clientId = clientDao.insertClient(client).toInt()

        val retrievedClient = clientDao.getClientById(clientId, 1)
        assertNotNull(retrievedClient)
        assertEquals("ACME Corp", retrievedClient?.businessName)

        val updatedClient = retrievedClient!!.copy(businessName = "ACME Corp Updated")
        clientDao.updateClient(updatedClient)
        assertEquals("ACME Corp Updated", clientDao.getClientById(clientId, 1)?.businessName)

        val item = Item(id = 0, itemName = "Design Services", barcode = "BAR123", price = 150.0, unit = "hrs", category = "Services", workspaceId = 1)
        val itemId = itemDao.insertItem(item).toInt()

        val retrievedItem = itemDao.getItemById(itemId, 1)
        assertNotNull(retrievedItem)
        assertEquals("BAR123", retrievedItem?.barcode)

        val searchedItem = itemDao.searchItemByBarcode("BAR123", 1)
        assertNotNull(searchedItem)
        assertEquals("Design Services", searchedItem?.itemName)

        clientDao.deleteClient(updatedClient)
        assertNull(clientDao.getClientById(clientId, 1))

        itemDao.deleteItem(retrievedItem!!)
        assertNull(itemDao.getItemById(itemId, 1))
    }

    @Test
    fun invoiceDao_insertAndSearchInvoices() = runBlocking {
        val invoice = Invoice(
            id = 0,
            invoiceNum = "INV-2026-0001",
            workspaceId = 1,
            clientId = 1,
            clientBusinessName = "Client Omega",
            status = "Paid",
            issueDate = LocalDate.now(),
            dueDate = LocalDate.now().plusDays(7),
            paidDate = LocalDate.now(),
            currencyCode = "USD",
            taxPercentage = 10.0,
            discountType = "Percent",
            discountValue = 0.0,
            endNote = "Thank you",
            totalAmount = 500.0,
            templateId = 1
        )
        invoiceDao.insertInvoice(invoice)

        val searched = invoiceDao.searchInvoices(1, "Omega").first()
        assertEquals(1, searched.size)
        assertEquals("INV-2026-0001", searched[0].invoiceNum)

        val byNum = invoiceDao.getInvoiceByInvoiceNum("INV-2026-0001")
        assertNotNull(byNum)
        assertEquals("Paid", byNum?.status)
    }
}
