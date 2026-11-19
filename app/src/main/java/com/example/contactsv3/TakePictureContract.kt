package com.example.contactsv3

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.result.contract.ActivityResultContract

/**
 * ActivityResultContracts.TakePicture n'ajoute pas le droit d'ecriture sur l'Uri
 * fourni a l'application camera. Ce contrat ajoute le flag necessaire.
 */
class TakePictureContract : ActivityResultContract<Uri, Boolean>() {

    override fun createIntent(context: Context, input: Uri): Intent =
        Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            .putExtra(MediaStore.EXTRA_OUTPUT, input)
            .addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)

    override fun parseResult(resultCode: Int, intent: Intent?): Boolean =
        resultCode == Activity.RESULT_OK
}
