package com.aaa.permissionsample

import android.Manifest
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

const val PREFKEY_DENIED_COUNT = "PREFKEY_DENIED_COUNT"

class MainActivity : AppCompatActivity() {
    private lateinit var sharedPref: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        sharedPref = getPreferences(Context.MODE_PRIVATE)

        /* 権限(s)チェック */
        checkPermissions()
    }

    /* 権限(s)チェック */
    private fun checkPermissions() {
        /* 複数の権限チェック */
        val permissions = arrayOf(Manifest.permission.POST_NOTIFICATIONS, /*複数時はここに並べる*/)
        /* 未許可権限を取得 */
        val denidedPermissions: MutableList<String> = ArrayList()
        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED)
                denidedPermissions.add(permission)
        }

        /* 未許可権限チェック */
        if(denidedPermissions.isEmpty())
            return
        else {
            val cnt = sharedPref.getInt(PREFKEY_DENIED_COUNT, 0)
            if(cnt < 2)
                /* 未許可権限があれば許可要求 */
                permissionLauncher.launch(denidedPermissions.toTypedArray())
            else {
                /* 2連続で権限不許可ならアラートダイアログ→Shutdown */
                val alertDialog = AlertDialog.Builder(this)
                    .setTitle(getString(R.string.req_permission))
                    .setMessage(R.string.wording_permission)
                    .setPositiveButton("OK") {dialog, which -> dialog.dismiss(); finish()}
                alertDialog.show()
            }
        }
    }

    private val permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        isGranted: Map<String, Boolean> ->
            /* 権限チェック */
            if (isGranted.all({it.value == true}))
                return@registerForActivityResult    /* 全て許可済 問題なし */
            else {
                /* ひとつでも権限不足ありならアラートダイアログ→Shutdown */
                val alertDialog = AlertDialog.Builder(this)
                    .setTitle(getString(R.string.req_permission))
                    .setMessage(R.string.req_permission)
                    .setPositiveButton("OK") {dialog, which -> dialog.dismiss(); finish()}
                alertDialog.show()
            }
    }
}