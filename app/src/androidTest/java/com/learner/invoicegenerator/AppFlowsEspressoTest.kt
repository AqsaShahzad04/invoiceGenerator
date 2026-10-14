package com.learner.invoicegenerator

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.learner.invoicegenerator.data.local.SessionManager
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppFlowsEspressoTest {

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val sessionManager = SessionManager.getInstance(context)
        sessionManager.setOnboardingCompleted()
        sessionManager.createLoginSession(1)
        sessionManager.setActiveWorkspace(1)
    }

    @Test
    fun testNavigationAndBottomBarTabs() {
        ActivityScenario.launch(MainActivity::class.java).use {
            // Verify Home tab is selected
            onView(withId(R.id.navHome)).check(matches(isDisplayed()))

            // Click Invoices tab
            onView(withId(R.id.navInvoices)).perform(click())

            // Click Clients tab
            onView(withId(R.id.navClients)).perform(click())

            // Click Settings tab
            onView(withId(R.id.navSettings)).perform(click())
        }
    }

    @Test
    fun testCreateClientFlow() {
        ActivityScenario.launch(MainActivity::class.java).use {
            // Navigate to Clients tab
            onView(withId(R.id.navClients)).perform(click())

            // Click Add Client
            onView(withId(R.id.addClientbtn)).perform(click())

            // Enter Client Details
            onView(withId(R.id.BusinessName)).perform(typeText("Espresso Client Ltd"), closeSoftKeyboard())
            onView(withId(R.id.name)).perform(typeText("John Espresso"), closeSoftKeyboard())
            onView(withId(R.id.email)).perform(typeText("john@espresso.com"), closeSoftKeyboard())

            // Save Client
            onView(withId(R.id.addClientbtn)).perform(click())
        }
    }

    @Test
    fun testCreateItemFlow() {
        ActivityScenario.launch(MainActivity::class.java).use {
            // Navigate to Settings then Items
            onView(withId(R.id.navSettings)).perform(click())

            // Navigate to Home -> Items
            onView(withId(R.id.navHome)).perform(click())
            onView(withId(R.id.cardCatalogue)).perform(click())

            // Click Add Item
            onView(withId(R.id.addClientbtn)).perform(click())

            // Enter Item Details
            onView(withId(R.id.itemNameInput)).perform(typeText("Espresso Service Item"), closeSoftKeyboard())
            onView(withId(R.id.priceInput)).perform(typeText("150"), closeSoftKeyboard())

            // Click Create Item
            onView(withId(R.id.createItemBtn)).perform(click())
        }
    }
}
