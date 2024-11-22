package com.podcreep.mobile.ui.library

import com.podcreep.mobile.R
import kotlinx.serialization.Serializable

// TODO(dean): label be a resource string
@Serializable
open class NavItem(val label: String, val iconResId: Int, val isTopLevel: Boolean) {
  @Serializable
  class NewReleases : NavItem("New Releases", R.drawable.ic_browsetree_new_episode, true)

  @Serializable
  class InProgress : NavItem("In Progress", R.drawable.ic_browsetree_inprogress, true)

  @Serializable
  class Podcasts : NavItem("Podcasts", R.drawable.ic_browsetree_subscriptions, true)

  @Serializable
  class EpisodeDetails(val podcastID: Long, val episodeID: Long)
    : NavItem("Episode", R.drawable.ic_podcast, false)
}

val topLevelNavItems = listOf(
  NavItem.NewReleases(), NavItem.InProgress(), NavItem.Podcasts()
)
