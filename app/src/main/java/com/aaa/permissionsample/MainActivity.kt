package com.aaa.permissionsample

import android.Manifest
import android.app.Dialog
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.DialogFragment

const val PREFKEY_FIRST_LAUNCH = "PREFKEY_FIRST_LAUNCH"

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
        val checkOK = checkPermissions()
        if(checkOK) {
            /* ★★★ここに権限チェックOK後の処理 */
            Toast.makeText(this, "権限OK", Toast.LENGTH_SHORT).show()
        }
    }

    /* 権限(s)チェック */
    private fun checkPermissions(): Boolean {
        /* 複数の権限チェック */
        val permissions = arrayOf(Manifest.permission.POST_NOTIFICATIONS, /*複数時はここに並べる*/)
        /* 未許可権限を取得 */
        val deniedPermissions: MutableList<String> = ArrayList()
        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED)
                deniedPermissions.add(permission)
        }

        /* 未許可権限チェック */
        if(deniedPermissions.isEmpty())
            return true  /* 権限付与済 何もする必要ない */

        val fstLaunch = sharedPref.getBoolean(PREFKEY_FIRST_LAUNCH, true)
        if(fstLaunch) {
            /* 未許可権限があれば許可要求 */
            permissionLauncher.launch(deniedPermissions.toTypedArray())
            sharedPref.edit(commit=true) { putBoolean(PREFKEY_FIRST_LAUNCH, false)}
        }
        else {
            val rationaleNeeded = deniedPermissions.any { permission ->
                ActivityCompat.shouldShowRequestPermissionRationale(this, permission)
            }

            if(rationaleNeeded) {
                /* 以前に拒否った */
                permissionLauncher.launch(deniedPermissions.toTypedArray())
            }
            else {
                /* 以前に"非表示にした"ならアラートダイアログ → Shutdown */
                PermissionDialogFragment.show(this, R.string.wording_permission_2times)
            }
        }
        return false
    }

    private val permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        isGranted: Map<String, Boolean> ->
            /* 権限チェック */
            if (isGranted.all({it.value == true})) {
                /* ★★★ここに権限チェックOK後の処理 */
                Toast.makeText(this, "権限OK", Toast.LENGTH_SHORT).show()
                return@registerForActivityResult    /* 全て許可済 問題なし */
            }
            else {
                /* ひとつでも権限不足ありならアラートダイアログ→Shutdown */
                PermissionDialogFragment.show(this, R.string.wording_permission)
            }
    }
}

class PermissionDialogFragment(@StringRes private val redid: Int) : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val activity = requireActivity()
        return AlertDialog.Builder(activity)
            .setTitle(activity.getString(R.string.req_permission))
            .setMessage(activity.getString(redid, activity.getString(R.string.app_name)))
            .setPositiveButton("OK") { _, _ ->
                activity.finish()
            }
            .create()
    }

    companion object {
        fun show(activity: AppCompatActivity, @StringRes redid: Int) {
            val fragment = PermissionDialogFragment(redid)
            fragment.show(activity.supportFragmentManager, "PermissionDialog")
        }
    }
}
