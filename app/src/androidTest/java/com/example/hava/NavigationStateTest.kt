package com.example.hava

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import org.junit.Rule
import org.junit.Test

class NavigationStateTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Test fun selectedCitySurvivesActivityRecreation() {
        compose.waitUntil(10_000) { compose.onAllNodesWithText("Adana").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText("Adana").performClick()
        compose.onNodeWithText("← Şehirler").assertIsDisplayed()
        compose.activityRule.scenario.recreate()
        compose.onNodeWithText("← Şehirler").assertIsDisplayed()
        compose.onNodeWithText("Gökyüzüne\nbir bakış.").assertDoesNotExist()
    }
}