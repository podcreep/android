package au.com.codeka.podcreep.model.store

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import au.com.codeka.podcreep.model.Subscription

/**
 * Store is a class we use for local storage of subscriptions, episode details, and all that stuff.
 * This class also manages requesting from the server for updates to this data as required. You
 * should use the {@link LiveData} methods to make sure you're always displaying the most up-to-date
 * data.
 */
class Store() {
  private var subscriptions: StoreData<ArrayList<Subscription>> = StoreData()
  private var subscriptionsLiveData: MutableLiveData<List<Subscription>>

  init {
    subscriptionsLiveData = MutableLiveData()
  }

  /**
   * Gets a list of the subscriptions the user has subscribed to.
   */
  fun subscriptions(): LiveData<List<Subscription>> {
    if (subscriptions.)

    return subscriptionsLiveData
  }
}
