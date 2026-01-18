package host.exp.exponent.experience

import android.app.Application
import com.facebook.react.ReactPackage
import com.facebook.react.defaults.DefaultReactNativeHost
import com.facebook.react.shell.MainReactPackage
import com.margelo.nitro.NitroModulesPackage
import com.margelo.nitro.runanywhere.RunAnywhereCorePackage
import com.margelo.nitro.runanywhere.llama.RunAnywhereLlamaPackage
import com.margelo.nitro.runanywhere.onnx.RunAnywhereONNXPackage
import com.rnfs.RNFSPackage
import com.rnziparchive.RNZipArchivePackage
import host.exp.exponent.audio.NativeAudioPackage
import host.exp.exponent.ExponentManifest
import host.exp.expoview.Exponent
import versioned.host.exp.exponent.ExpoReanimatedPackage
import versioned.host.exp.exponent.ExpoTurboPackage
import versioned.host.exp.exponent.ExponentPackage

interface ExpoNativeHost {
  var devSupportEnabled: Boolean
  var mainModuleName: String?
}

class ExpoGoReactNativeHost(
  application: Application,
  private val instanceManagerBuilderProperties: Exponent.InstanceManagerBuilderProperties
) : DefaultReactNativeHost(application), ExpoNativeHost {
  override var devSupportEnabled = false
  override var mainModuleName: String? = null

  override fun getUseDeveloperSupport(): Boolean {
    return devSupportEnabled
  }

  override fun getJSMainModuleName(): String {
    return mainModuleName ?: super.getJSMainModuleName()
  }

  override fun getJSBundleFile(): String? {
    return instanceManagerBuilderProperties.jsBundlePath
  }

  override val isHermesEnabled = true

  override val isNewArchEnabled = true

  override fun getPackages(): MutableList<ReactPackage> {
    return mutableListOf(
      MainReactPackage(null),
      NitroModulesPackage(),
      // RunAnywhere Native Packages - load native libraries
      RunAnywhereCorePackage(),
      RunAnywhereLlamaPackage(),
      RunAnywhereONNXPackage(),
      // SDK Peer Dependencies - file system and archive support
      RNFSPackage(),
      RNZipArchivePackage(),
      // Native Audio Module for STT recording (WAV format)
      NativeAudioPackage(),
      ExpoReanimatedPackage(),
      ExponentPackage(
        instanceManagerBuilderProperties.experienceProperties,
        instanceManagerBuilderProperties.manifest,
        // DO NOT EDIT THIS COMMENT - used by versioning scripts
        // When distributing change the following two arguments to nulls
        instanceManagerBuilderProperties.expoPackages,
        instanceManagerBuilderProperties.exponentPackageDelegate,
        instanceManagerBuilderProperties.singletonModules
      ),
      ExpoTurboPackage(
        instanceManagerBuilderProperties.experienceProperties,
        instanceManagerBuilderProperties.manifest
      )
    )
  }
}

data class KernelData(
  val initialURL: String? = null,
  val localBundlePath: String? = null
)

class KernelReactNativeHost(
  application: Application,
  private val exponentManifest: ExponentManifest,
  private val data: KernelData?
) : DefaultReactNativeHost(application), ExpoNativeHost {
  override var devSupportEnabled = false
  override var mainModuleName: String? = null

  override fun getUseDeveloperSupport(): Boolean {
    return devSupportEnabled
  }

  override val isHermesEnabled = true

  override val isNewArchEnabled = true

  override fun getJSBundleFile(): String? {
    return data?.localBundlePath
  }

  override fun getJSMainModuleName(): String {
    return if (devSupportEnabled) {
      exponentManifest.getKernelManifestAndAssetRequestHeaders().manifest.getMainModuleName()
    } else {
      mainModuleName ?: super.getJSMainModuleName()
    }
  }

  override fun getPackages(): MutableList<ReactPackage> {
    return mutableListOf(
      MainReactPackage(null),
      NitroModulesPackage(),
      // RunAnywhere Native Packages - load native libraries
      RunAnywhereCorePackage(),
      RunAnywhereLlamaPackage(),
      RunAnywhereONNXPackage(),
      // SDK Peer Dependencies - file system and archive support
      RNFSPackage(),
      RNZipArchivePackage(),
      // Native Audio Module for STT recording (WAV format)
      NativeAudioPackage(),
      ExpoReanimatedPackage(),
      ExponentPackage.kernelExponentPackage(
        application.applicationContext,
        exponentManifest.getKernelManifestAndAssetRequestHeaders().manifest,
        HomeActivity.homeExpoPackages(),
        HomeActivity.Companion,
        data?.initialURL
      ),
      ExpoTurboPackage.kernelExpoTurboPackage(
        exponentManifest.getKernelManifestAndAssetRequestHeaders().manifest,
        data?.initialURL
      )
    )
  }
}
