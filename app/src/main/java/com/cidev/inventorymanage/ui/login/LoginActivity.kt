package com.cidev.inventorymanage.ui.login

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.cidev.inventorymanage.data.AuthRepository
import com.cidev.inventorymanage.databinding.ActivityLoginBinding
import com.cidev.inventorymanage.network.SoapClient
import com.cidev.inventorymanage.ui.menu.MainMenuActivity
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val authRepository = AuthRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // TODO: read the real WMS IP from a settings screen instead of
        // hardcoding — for now this matches ServiceIP.txt (192.168.0.22).
        SoapClient.configureServer("192.168.0.22")

        binding.btnLogin.setOnClickListener { attemptLogin() }
    }

    private fun attemptLogin() {
        val username = binding.edtUsername.text?.toString()?.trim().orEmpty()
        val password = binding.edtPassword.text?.toString().orEmpty()

        if (username.isEmpty() || password.isEmpty()) {
            showError("נא למלא שם משתמש וסיסמה")
            return
        }

        setLoading(true)
        // NOTE: must be a STABLE id across app launches, since the server
        // binds each login to one device (see docs/API_MAP.md).
        val deviceId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)

        lifecycleScope.launch {
            val result = authRepository.login(username, password, deviceId)
            setLoading(false)

            result.onSuccess { user ->
                if (user.isValid) {
                    MainMenuActivity.currentUser = user
                    startActivity(Intent(this@LoginActivity, MainMenuActivity::class.java))
                    finish()
                } else {
                    showError(user.responseMessage.ifBlank { "פרטי התחברות שגויים" })
                }
            }.onFailure { e ->
                showError("שגיאת תקשורת עם השרת: ${e.message}")
            }
        }
    }

    private fun setLoading(loading: Boolean) {
        binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        binding.btnLogin.isEnabled = !loading
    }

    private fun showError(msg: String) {
        binding.txtError.text = msg
        binding.txtError.visibility = View.VISIBLE
    }
}
