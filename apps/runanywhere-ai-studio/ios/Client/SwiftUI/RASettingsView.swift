// Copyright 2015-present 650 Industries. All rights reserved.
// RunAnywhere AI Studio - Settings View

import SwiftUI
import AppTrackingTransparency

struct RASettingsView: View {
  @Binding var selectedTab: HomeTab
  @EnvironmentObject var viewModel: HomeViewModel
  @State private var shouldShowTrackingSection = false
  @State private var isTrackingRequestInFlight = false

  var body: some View {
    NavigationView {
      ScrollView {
        VStack(spacing: RASpacing.xxLarge) {
          // Theme Section
          themeSection

          // Developer Menu Gestures Section
          gesturesSection

          // Tracking Section (if applicable)
          if shouldShowTrackingSection {
            trackingSection
          }

          // App Info Section
          appInfoSection

          // Legal Section
          legalSection
        }
        .padding(RASpacing.large)
      }
      .background(RAColors.backgroundPrimary)
      .navigationTitle("Settings")
      .navigationBarTitleDisplayMode(.inline)
      .task {
        await refreshTrackingStatus()
      }
    }
  }

  // MARK: - Theme Section
  private var themeSection: some View {
    VStack(alignment: .leading, spacing: RASpacing.medium) {
      Text("Theme")
        .font(RATypography.headline)
        .foregroundColor(RAColors.textPrimary)

      VStack(spacing: 0) {
        RAThemeOptionRow(
          icon: "circle.lefthalf.filled.righthalf.striped.horizontal",
          title: "Automatic",
          isSelected: viewModel.selectedTheme == 0,
          action: { viewModel.updateTheme(0) }
        )
        Divider()
        RAThemeOptionRow(
          icon: "sun.max",
          title: "Light",
          isSelected: viewModel.selectedTheme == 1,
          action: { viewModel.updateTheme(1) }
        )
        Divider()
        RAThemeOptionRow(
          icon: "moon",
          title: "Dark",
          isSelected: viewModel.selectedTheme == 2,
          action: { viewModel.updateTheme(2) }
        )
      }
      .background(RAColors.backgroundSecondary)
      .cornerRadius(RABorderRadius.large)

      Text("Automatic uses your device's system appearance setting.")
        .font(RATypography.caption)
        .foregroundColor(RAColors.textSecondary)
    }
  }

  // MARK: - Gestures Section
  private var gesturesSection: some View {
    VStack(alignment: .leading, spacing: RASpacing.medium) {
      Text("Developer Menu Gestures")
        .font(RATypography.headline)
        .foregroundColor(RAColors.textPrimary)

      VStack(spacing: 0) {
        GestureOption(
          imageName: "shake-device",
          title: "Shake device",
          isEnabled: viewModel.shakeToShowDevMenu,
          action: { viewModel.updateShakeGesture(!viewModel.shakeToShowDevMenu) }
        )
        Divider()
        GestureOption(
          imageName: "three-finger-long-press",
          title: "Three-finger long press",
          isEnabled: viewModel.threeFingerLongPressEnabled,
          action: { viewModel.updateThreeFingerGesture(!viewModel.threeFingerLongPressEnabled) }
        )
      }
      .background(RAColors.backgroundSecondary)
      .cornerRadius(RABorderRadius.large)

      Text("Selected gestures toggle the developer menu inside an experience. The menu lets you reload, return home, and access developer tools.")
        .font(RATypography.caption)
        .foregroundColor(RAColors.textSecondary)
    }
  }

  // MARK: - Tracking Section
  private var trackingSection: some View {
    VStack(alignment: .leading, spacing: RASpacing.medium) {
      Text("Tracking")
        .font(RATypography.headline)
        .foregroundColor(RAColors.textPrimary)

      Button(action: requestTrackingPermission) {
        HStack {
          Text("Allow access to app-related data for tracking")
            .font(RATypography.body)
            .foregroundColor(RAColors.textPrimary)
            .multilineTextAlignment(.leading)

          Spacer()

          if isTrackingRequestInFlight {
            ProgressView()
              .tint(RAColors.primaryAccent)
          }
        }
        .padding(RASpacing.large)
        .background(RAColors.backgroundSecondary)
        .cornerRadius(RABorderRadius.large)
      }
      .disabled(isTrackingRequestInFlight)
      .buttonStyle(.plain)

      if let destination = URL(string: "https://runanywhere.dev/privacy") {
        Link("Learn more about what data is collected and why.", destination: destination)
          .font(RATypography.caption)
          .foregroundColor(RAColors.primaryAccent)
      }
    }
  }

  // MARK: - App Info Section
  private var appInfoSection: some View {
    VStack(alignment: .leading, spacing: RASpacing.medium) {
      Text("App Info")
        .font(RATypography.headline)
        .foregroundColor(RAColors.textPrimary)

      VStack(spacing: 0) {
        AppInfoRow(label: "Client Version", value: Bundle.main.infoDictionary?["CFBundleShortVersionString"] as? String ?? "1.0.0")
        Divider()
        AppInfoRow(label: "Supported SDK", value: getSupportedSDKVersion())
        Divider()
        AppInfoRow(label: "Powered by", value: "RunAnywhere + Expo")
      }
      .background(RAColors.backgroundSecondary)
      .cornerRadius(RABorderRadius.large)

      Button(action: copyBuildInfoToClipboard) {
        HStack {
          Image(systemName: "doc.on.doc")
          Text("Copy Build Info")
        }
        .font(RATypography.body)
        .foregroundColor(RAColors.primaryAccent)
        .frame(maxWidth: .infinity)
        .padding(RASpacing.large)
        .background(RAColors.backgroundSecondary)
        .cornerRadius(RABorderRadius.large)
      }
    }
  }

  // MARK: - Legal Section
  private var legalSection: some View {
    VStack(alignment: .leading, spacing: RASpacing.medium) {
      Text("Legal")
        .font(RATypography.headline)
        .foregroundColor(RAColors.textPrimary)

      VStack(spacing: 0) {
        if let privacyURL = URL(string: "https://runanywhere.dev/privacy") {
          Link(destination: privacyURL) {
            HStack {
              Image(systemName: "hand.raised")
                .foregroundColor(RAColors.primaryAccent)
              Text("Privacy Policy")
                .foregroundColor(RAColors.textPrimary)
              Spacer()
              Image(systemName: "arrow.up.right")
                .foregroundColor(RAColors.textTertiary)
            }
            .padding(RASpacing.large)
          }
        }

        Divider()

        if let termsURL = URL(string: "https://runanywhere.dev/terms") {
          Link(destination: termsURL) {
            HStack {
              Image(systemName: "doc.text")
                .foregroundColor(RAColors.primaryAccent)
              Text("Terms of Service")
                .foregroundColor(RAColors.textPrimary)
              Spacer()
              Image(systemName: "arrow.up.right")
                .foregroundColor(RAColors.textTertiary)
            }
            .padding(RASpacing.large)
          }
        }

        Divider()

        if let websiteURL = URL(string: "https://runanywhere.dev") {
          Link(destination: websiteURL) {
            HStack {
              Image(systemName: "globe")
                .foregroundColor(RAColors.primaryAccent)
              Text("Visit Website")
                .foregroundColor(RAColors.textPrimary)
              Spacer()
              Image(systemName: "arrow.up.right")
                .foregroundColor(RAColors.textTertiary)
            }
            .padding(RASpacing.large)
          }
        }
      }
      .background(RAColors.backgroundSecondary)
      .cornerRadius(RABorderRadius.large)
    }
  }

  // MARK: - Helper Functions
  private func copyBuildInfoToClipboard() {
    let buildInfo = """
    RunAnywhere AI Studio
    Version: \(Bundle.main.infoDictionary?["CFBundleShortVersionString"] as? String ?? "Unknown")
    SDK: \(getSupportedSDKVersion())
    """
    UIPasteboard.general.string = buildInfo

    let generator = UINotificationFeedbackGenerator()
    generator.notificationOccurred(.success)
  }

  private func refreshTrackingStatus() async {
    let status = ATTrackingManager.trackingAuthorizationStatus
    await MainActor.run {
      shouldShowTrackingSection = (status == .notDetermined)
    }
  }

  private func requestTrackingPermission() {
    guard !isTrackingRequestInFlight else { return }
    isTrackingRequestInFlight = true

    ATTrackingManager.requestTrackingAuthorization { status in
      DispatchQueue.main.async {
        isTrackingRequestInFlight = false
        shouldShowTrackingSection = (status == .notDetermined)
      }
    }
  }

}

// MARK: - Theme Option Row
struct RAThemeOptionRow: View {
  let icon: String
  let title: String
  let isSelected: Bool
  let action: () -> Void

  var body: some View {
    Button(action: action) {
      HStack {
        Image(systemName: icon)
          .font(.system(size: 20))
          .foregroundColor(RAColors.primaryAccent)
          .frame(width: 32)

        Text(title)
          .font(RATypography.body)
          .foregroundColor(RAColors.textPrimary)

        Spacer()

        if isSelected {
          Image(systemName: "checkmark")
            .foregroundColor(RAColors.primaryAccent)
        }
      }
      .padding(RASpacing.large)
    }
    .buttonStyle(.plain)
  }
}

#Preview {
  RASettingsView(selectedTab: .constant(.settings))
    .environmentObject(HomeViewModel())
}
