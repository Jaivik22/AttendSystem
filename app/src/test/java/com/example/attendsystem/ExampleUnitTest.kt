package com.example.attendsystem

import android.util.Log
import androidx.appcompat.app.AlertDialog
import com.example.attendsystem.Activity.MainActivity
import org.junit.Test

import org.junit.Assert.*
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun `showWelcomeDialog displays correct message`() {
        // Mock dependencies
        val alertDialogBuilder = mock(AlertDialog.Builder::class.java)
        val alertDialog = mock(AlertDialog::class.java)
        val log = mock(Log::class.java)

        `when`(alertDialogBuilder.create()).thenReturn(alertDialog)

        val mainActivity = MainActivity()

        // Call the method
        mainActivity.showWelcomeDialog("John Doe")

        // Verify that the correct message is set
        verify(alertDialogBuilder).setMessage("Welcome John Doe")
    }
}