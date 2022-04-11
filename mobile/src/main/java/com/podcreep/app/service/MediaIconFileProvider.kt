package com.podcreep.app.service

import android.net.Uri
import android.os.ParcelFileDescriptor
import androidx.core.content.FileProvider
import com.podcreep.util.L

class MediaIconFileProvider : FileProvider() {
  companion object {
    private val L = L("MediaIconFileProvider")
  }

  // TODO: anything to do here?
  override fun openFile(uri: Uri, mode: String): ParcelFileDescriptor? {
    return super.openFile(uri, mode)
  }
}
