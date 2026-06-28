package il.ronmad.speedruntimer

import android.content.Intent
import android.net.Uri
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment

class SplitIconPickerHelper(
    fragment: Fragment,
    private val onResult: (Uri?) -> Unit
) {
    private val launcher: ActivityResultLauncher<String> =
        fragment.registerForActivityResult(
            ActivityResultContracts.GetContent()
        ) { uri ->
            if (uri != null) {
                try {
                    fragment.requireContext().contentResolver.takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                } catch (e: SecurityException) { }
            }
            onResult(uri)
        }

    fun launch() = launcher.launch("image/*")

    fun clearIcon(split: Split, context: android.content.Context) {
        split.iconUri?.let { uriStr ->
            try {
                context.contentResolver.releasePersistableUriPermission(
                    Uri.parse(uriStr),
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (e: SecurityException) { }
        }
        split.iconUri = null
    }
}
