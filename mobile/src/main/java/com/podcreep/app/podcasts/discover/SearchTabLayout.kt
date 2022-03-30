package com.podcreep.app.podcasts.discover

import android.content.Context
import android.widget.LinearLayout
import androidx.annotation.Keep
import com.podcreep.concurrency.TaskRunner

class SearchTabLayout @Keep constructor(
    context: Context,
    taskRunner: TaskRunner,
    callbacks: DiscoverLayout.Callbacks)
  : LinearLayout(context) {
}
