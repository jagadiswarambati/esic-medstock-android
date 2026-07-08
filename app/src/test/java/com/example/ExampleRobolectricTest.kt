package com.example

import android.content.Context
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.test.core.app.ApplicationProvider
import com.example.ui.screens.MainAppScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.HealthViewModel
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun testAppRender() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val viewModel = HealthViewModel(context as android.app.Application)
    
    composeTestRule.setContent {
      MyApplicationTheme {
        MainAppScreen(viewModel = viewModel)
      }
    }
    
    composeTestRule.waitForIdle()
    assertNotNull(viewModel)
  }
}
