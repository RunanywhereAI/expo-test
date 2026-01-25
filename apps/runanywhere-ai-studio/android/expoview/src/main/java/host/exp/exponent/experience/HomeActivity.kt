// Copyright 2015-present 650 Industries. All rights reserved.
// RUNANYWHERE: Modified to use native UI instead of kernel JS
package host.exp.exponent.experience

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.WindowCompat
import androidx.core.view.setPadding
import com.facebook.react.soloader.OpenSourceMergedSoMapping
import com.facebook.soloader.SoLoader
import expo.modules.application.ApplicationModule
import expo.modules.asset.AssetModule
import expo.modules.blur.BlurModule
import expo.modules.camera.CameraViewModule
import expo.modules.clipboard.ClipboardModule
import expo.modules.constants.ConstantsModule
import expo.modules.constants.ConstantsPackage
import expo.modules.core.interfaces.Package
import expo.modules.device.DeviceModule
import expo.modules.easclient.EASClientModule
import expo.modules.filesystem.FileSystemModule
import expo.modules.filesystem.legacy.FileSystemLegacyModule
import expo.modules.filesystem.legacy.FileSystemPackage
import expo.modules.font.FontLoaderModule
import expo.modules.font.FontUtilsModule
import expo.modules.haptics.HapticsModule
import expo.modules.keepawake.KeepAwakeModule
import expo.modules.keepawake.KeepAwakePackage
import expo.modules.kotlin.ModulesProvider
import expo.modules.kotlin.modules.Module
import expo.modules.lineargradient.LinearGradientModule
import expo.modules.notifications.NotificationsPackage
import expo.modules.storereview.StoreReviewModule
import expo.modules.taskManager.TaskManagerPackage
import expo.modules.trackingtransparency.TrackingTransparencyModule
import expo.modules.webbrowser.WebBrowserModule
import host.exp.exponent.di.NativeModuleDepsProvider
import host.exp.exponent.experience.splashscreen.legacy.SplashScreenModule
import host.exp.exponent.experience.splashscreen.legacy.SplashScreenPackage
import host.exp.exponent.kernel.Kernel
import host.exp.exponent.kernel.KernelConstants
import host.exp.exponent.utils.ExperienceRTLManager
import host.exp.exponent.utils.currentDeviceIsAPhone
import javax.inject.Inject

/**
 * RUNANYWHERE: Native HomeActivity for RunAnywhere AI Studio
 * 
 * A native home screen for RunAnywhere AI Studio that:
 * 1. Shows RunAnywhere branding and capabilities
 * 2. Allows connecting to Metro bundler for development
 * 3. Bypasses the kernel JS entirely (avoiding polyfill issues)
 */
open class HomeActivity : AppCompatActivity() {
  @Inject
  protected lateinit var kernel: Kernel

  private lateinit var urlInput: EditText
  
  // Brand Colors (matching iOS)
  private val brandPrimary = Color.parseColor("#4A8FD9")      // Blue
  private val brandAccent = Color.parseColor("#66CC99")       // Green
  private val backgroundDark = Color.parseColor("#171B21")
  private val cardBackground = Color.parseColor("#1F242E")
  private val textPrimary = Color.WHITE
  private val textSecondary = Color.parseColor("#999999")
  private val textMuted = Color.parseColor("#666666")
  private val inputBackground = Color.parseColor("#171B21")
  private val inputBorder = Color.parseColor("#333333")
  
  // Default Metro URL
  private val defaultMetroUrl = "exp://192.168.1.100:8081"

  override fun onCreate(savedInstanceState: Bundle?) {
    NativeModuleDepsProvider.instance.inject(HomeActivity::class.java, this)
    
    enableEdgeToEdge()

    if (currentDeviceIsAPhone(this)) {
      @SuppressLint("SourceLockedOrientationActivity")
      requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }

    super.onCreate(savedInstanceState)

    SoLoader.init(this, OpenSourceMergedSoMapping)
    ExperienceRTLManager.setRTLPreferences(this, allowRTL = false, forceRTL = false)

    setupNativeUI()
    
    WindowCompat.getInsetsController(window, window.decorView).apply {
      isAppearanceLightStatusBars = false
    }
    window.statusBarColor = backgroundDark
    window.navigationBarColor = backgroundDark
  }

  private fun setupNativeUI() {
    val scrollView = ScrollView(this).apply {
      setBackgroundColor(backgroundDark)
      isFillViewport = true
    }

    val mainLayout = LinearLayout(this).apply {
      orientation = LinearLayout.VERTICAL
      gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
      setPadding(dpToPx(20), dpToPx(48), dpToPx(20), dpToPx(32))
    }

    // Header Section
    mainLayout.addView(createHeaderSection())
    
    // Capabilities Card
    mainLayout.addView(createCapabilitiesCard())
    
    // Connect Card
    mainLayout.addView(createConnectCard())
    
    // Instructions
    mainLayout.addView(createInstructions())
    
    // Spacer
    val spacer = LinearLayout(this).apply {
      layoutParams = LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.MATCH_PARENT,
        0,
        1f
      )
    }
    mainLayout.addView(spacer)
    
    // Version
    mainLayout.addView(createVersionLabel())

    scrollView.addView(mainLayout)
    setContentView(scrollView)
  }

  private fun createHeaderSection(): LinearLayout {
    return LinearLayout(this).apply {
      orientation = LinearLayout.VERTICAL
      gravity = Gravity.CENTER
      layoutParams = LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.MATCH_PARENT,
        LinearLayout.LayoutParams.WRAP_CONTENT
      ).apply {
        bottomMargin = dpToPx(24)
      }

      // Logo "RA"
      addView(TextView(this@HomeActivity).apply {
        text = "RA"
        textSize = 48f
        setTextColor(brandPrimary)
        setTypeface(null, Typeface.BOLD)
        gravity = Gravity.CENTER
        setPadding(0, 0, 0, dpToPx(8))
      })

      // Title
      addView(TextView(this@HomeActivity).apply {
        text = "RunAnywhere AI Studio"
        textSize = 26f
        setTextColor(textPrimary)
        setTypeface(null, Typeface.BOLD)
        gravity = Gravity.CENTER
        setPadding(0, 0, 0, dpToPx(8))
      })

      // Subtitle
      addView(TextView(this@HomeActivity).apply {
        text = "On-Device AI Development Environment"
        textSize = 15f
        setTextColor(textSecondary)
        gravity = Gravity.CENTER
      })
    }
  }

  private fun createCapabilitiesCard(): CardView {
    val card = CardView(this).apply {
      radius = dpToPx(16).toFloat()
      cardElevation = 0f
      setCardBackgroundColor(cardBackground)
      layoutParams = LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.MATCH_PARENT,
        LinearLayout.LayoutParams.WRAP_CONTENT
      ).apply {
        bottomMargin = dpToPx(16)
      }
    }

    val cardContent = LinearLayout(this).apply {
      orientation = LinearLayout.VERTICAL
      setPadding(dpToPx(16))
    }

    // Card Title
    cardContent.addView(TextView(this).apply {
      text = "Capabilities"
      textSize = 18f
      setTextColor(textPrimary)
      setTypeface(null, Typeface.BOLD)
      setPadding(0, 0, 0, dpToPx(16))
    })

    // Features
    val features = listOf(
      Triple("🧠", "LLM Inference", "Run large language models locally with llama.cpp"),
      Triple("⚡", "ONNX Runtime", "Execute ML models with hardware acceleration"),
      Triple("📱", "On-Device AI", "No internet required for AI processing"),
      Triple("🔒", "Fast & Private", "Your data never leaves the device")
    )

    features.forEach { (emoji, title, description) ->
      cardContent.addView(createFeatureRow(emoji, title, description))
    }

    card.addView(cardContent)
    return card
  }

  private fun createFeatureRow(emoji: String, title: String, description: String): LinearLayout {
    return LinearLayout(this).apply {
      orientation = LinearLayout.HORIZONTAL
      gravity = Gravity.TOP
      layoutParams = LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.MATCH_PARENT,
        LinearLayout.LayoutParams.WRAP_CONTENT
      ).apply {
        bottomMargin = dpToPx(12)
      }

      // Emoji Icon
      addView(TextView(this@HomeActivity).apply {
        text = emoji
        textSize = 20f
        layoutParams = LinearLayout.LayoutParams(
          dpToPx(32),
          LinearLayout.LayoutParams.WRAP_CONTENT
        )
      })

      // Text Container
      addView(LinearLayout(this@HomeActivity).apply {
        orientation = LinearLayout.VERTICAL
        layoutParams = LinearLayout.LayoutParams(
          0,
          LinearLayout.LayoutParams.WRAP_CONTENT,
          1f
        )

        // Title
        addView(TextView(this@HomeActivity).apply {
          text = title
          textSize = 15f
          setTextColor(textPrimary)
          setTypeface(null, Typeface.BOLD)
        })

        // Description
        addView(TextView(this@HomeActivity).apply {
          text = description
          textSize = 13f
          setTextColor(textSecondary)
        })
      })
    }
  }

  private fun createConnectCard(): CardView {
    val card = CardView(this).apply {
      radius = dpToPx(16).toFloat()
      cardElevation = 0f
      setCardBackgroundColor(cardBackground)
      layoutParams = LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.MATCH_PARENT,
        LinearLayout.LayoutParams.WRAP_CONTENT
      ).apply {
        bottomMargin = dpToPx(16)
      }
    }

    val cardContent = LinearLayout(this).apply {
      orientation = LinearLayout.VERTICAL
      setPadding(dpToPx(16))
    }

    // Card Title
    cardContent.addView(TextView(this).apply {
      text = "Connect to Development Server"
      textSize = 18f
      setTextColor(textPrimary)
      setTypeface(null, Typeface.BOLD)
      setPadding(0, 0, 0, dpToPx(8))
    })

    // Description
    cardContent.addView(TextView(this).apply {
      text = "Enter your Metro bundler URL to load your app"
      textSize = 14f
      setTextColor(textSecondary)
      setPadding(0, 0, 0, dpToPx(16))
    })

    // URL Input
    val inputBackground = GradientDrawable().apply {
      setColor(this@HomeActivity.inputBackground)
      cornerRadius = dpToPx(10).toFloat()
      setStroke(dpToPx(1), inputBorder)
    }
    
    urlInput = EditText(this).apply {
      setText(defaultMetroUrl)
      textSize = 16f
      setTextColor(textPrimary)
      setHintTextColor(textMuted)
      hint = "exp://192.168.x.x:8081"
      background = inputBackground
      setPadding(dpToPx(16), dpToPx(14), dpToPx(16), dpToPx(14))
      inputType = InputType.TYPE_TEXT_VARIATION_URI
      imeOptions = EditorInfo.IME_ACTION_GO
      setOnEditorActionListener { _, actionId, _ ->
        if (actionId == EditorInfo.IME_ACTION_GO) {
          openExperience()
          true
        } else false
      }
      layoutParams = LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.MATCH_PARENT,
        LinearLayout.LayoutParams.WRAP_CONTENT
      ).apply {
        bottomMargin = dpToPx(16)
      }
    }
    cardContent.addView(urlInput)

    // Connect Button
    val buttonBackground = GradientDrawable().apply {
      setColor(brandPrimary)
      cornerRadius = dpToPx(10).toFloat()
    }
    
    cardContent.addView(TextView(this).apply {
      text = "Connect"
      textSize = 17f
      setTextColor(Color.WHITE)
      setTypeface(null, Typeface.BOLD)
      gravity = Gravity.CENTER
      background = buttonBackground
      setPadding(dpToPx(16), dpToPx(14), dpToPx(16), dpToPx(14))
      isClickable = true
      isFocusable = true
      setOnClickListener { openExperience() }
      layoutParams = LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.MATCH_PARENT,
        LinearLayout.LayoutParams.WRAP_CONTENT
      )
    })

    card.addView(cardContent)
    return card
  }

  private fun createInstructions(): TextView {
    return TextView(this).apply {
      text = "Start your development server with: npx expo start"
      textSize = 13f
      setTextColor(textMuted)
      gravity = Gravity.CENTER
      layoutParams = LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.MATCH_PARENT,
        LinearLayout.LayoutParams.WRAP_CONTENT
      ).apply {
        bottomMargin = dpToPx(24)
      }
    }
  }

  private fun createVersionLabel(): TextView {
    val versionName = try {
      packageManager.getPackageInfo(packageName, 0).versionName ?: "1.0"
    } catch (e: PackageManager.NameNotFoundException) {
      "1.0"
    }
    val versionCode = try {
      packageManager.getPackageInfo(packageName, 0).longVersionCode.toString()
    } catch (e: PackageManager.NameNotFoundException) {
      "1"
    }
    
    return TextView(this).apply {
      text = "RunAnywhere AI Studio v$versionName ($versionCode)"
      textSize = 12f
      setTextColor(textMuted)
      gravity = Gravity.CENTER
      layoutParams = LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.MATCH_PARENT,
        LinearLayout.LayoutParams.WRAP_CONTENT
      )
    }
  }

  private fun openExperience() {
    val url = urlInput.text.toString().trim()
    if (url.isEmpty()) {
      Toast.makeText(this, "Please enter a Metro URL", Toast.LENGTH_SHORT).show()
      return
    }

    try {
      kernel.openExperience(
        KernelConstants.ExperienceOptions(url, url, null)
      )
    } catch (e: Exception) {
      Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
    }
  }

  private fun dpToPx(dp: Int): Int {
    return (dp * resources.displayMetrics.density).toInt()
  }

  override fun onResume() {
    super.onResume()
    SoLoader.init(this, OpenSourceMergedSoMapping)
  }

  override fun onConfigurationChanged(newConfig: Configuration) {
    super.onConfigurationChanged(newConfig)
    enableEdgeToEdge()
  }

  companion object : ModulesProvider {
    fun homeExpoPackages(): List<Package> {
      return listOf(
        ConstantsPackage(),
        FileSystemPackage(),
        KeepAwakePackage(),
        NotificationsPackage(),
        TaskManagerPackage(),
        SplashScreenPackage()
      )
    }

    override fun getModulesList(): List<Class<out Module>> {
      return listOf(
        AssetModule::class.java,
        BlurModule::class.java,
        CameraViewModule::class.java,
        ClipboardModule::class.java,
        ConstantsModule::class.java,
        DeviceModule::class.java,
        EASClientModule::class.java,
        FileSystemModule::class.java,
        FileSystemLegacyModule::class.java,
        FontLoaderModule::class.java,
        FontUtilsModule::class.java,
        HapticsModule::class.java,
        KeepAwakeModule::class.java,
        LinearGradientModule::class.java,
        SplashScreenModule::class.java,
        TrackingTransparencyModule::class.java,
        StoreReviewModule::class.java,
        WebBrowserModule::class.java,
        ApplicationModule::class.java
      )
    }
  }
}
