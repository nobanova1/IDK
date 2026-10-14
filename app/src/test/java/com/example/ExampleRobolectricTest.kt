package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.os.model.OsAppId
import com.example.os.state.OsViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("NexusOS", appName)
    }

    @Test
    fun `test OS view model window opening and file system`() {
        val vm = OsViewModel()
        val root = vm.findNode("/home/user/Documents")
        assertNotNull(root)
        assertTrue(root!!.isDirectory)

        // Test creating file
        val created = vm.createFile("/home/user/Documents", "test_doc.txt", "Hello NexusOS")
        assertTrue(created)
        val file = vm.findNode("/home/user/Documents/test_doc.txt")
        assertNotNull(file)
        assertEquals("Hello NexusOS", file!!.content)

        // Test opening app window
        vm.openApp(OsAppId.TERMINAL)
        assertEquals(1, vm.uiState.value.windows.size)
        assertEquals(OsAppId.TERMINAL, vm.uiState.value.windows[0].appId)
    }
}
