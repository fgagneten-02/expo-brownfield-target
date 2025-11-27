package ${{packageId}}

import android.content.Context
import android.os.Bundle
import android.widget.FrameLayout
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.facebook.react.ReactDelegate
import com.facebook.react.ReactInstanceManager
import com.facebook.react.ReactRootView

// Sealed class allows built-in components AND custom components
sealed class RootComponent(open val key: String) {

    // Default included component (same as before)
    data object Main : RootComponent("main")

    // Allows app integrators to define their own components:
    data class Custom(override val key: String) : RootComponent(key)

    companion object {
        // Optional helper if you want enum-like lookup
        fun fromKey(key: String): RootComponent = when (key) {
            Main.key -> Main
            else -> Custom(key)
        }
    }
}

object ReactNativeViewFactory {
    fun createFrameLayout(
        context: Context,
        activity: FragmentActivity,
        rootComponent: RootComponent,
        launchOptions: Bundle? = null,
    ): FrameLayout {
        val reactHost = ReactNativeHostManager.shared.getReactHost()
        if (BuildConfig.IS_NEW_ARCHITECTURE_ENABLED) {
            val reactDelegate = ReactDelegate(activity, reactHost!!, rootComponent.key, launchOptions)

            activity.lifecycle.addObserver(object : DefaultLifecycleObserver {
                override fun onResume(owner: LifecycleOwner) {
                    reactDelegate.onHostResume()
                }

                override fun onPause(owner: LifecycleOwner) {
                    reactDelegate.onHostPause()
                }

                override fun onDestroy(owner: LifecycleOwner) {
                    reactDelegate.onHostDestroy()
                    owner.lifecycle.removeObserver(this) // Cleanup to avoid leaks
                }
            })

            reactDelegate.loadApp()
            return reactDelegate.reactRootView!!
        }

        val instanceManager: ReactInstanceManager? = ReactNativeHostManager.shared.getReactNativeHost()?.reactInstanceManager
        val reactView = ReactRootView(context)
        reactView.startReactApplication(
            instanceManager,
            rootComponent.key,
            launchOptions,
        )

        return reactView
    }
}
