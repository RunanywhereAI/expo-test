// Copyright 2015-present 650 Industries. All rights reserved.
// RunAnywhere AI Studio - Diagnostics Tab View
// Essential for testing app permissions (Audio, Location, Geofencing)

import SwiftUI

struct RADiagnosticsTabView: View {
  @EnvironmentObject var viewModel: HomeViewModel

  var body: some View {
    ScrollView {
      VStack(spacing: RASpacing.large) {
        // Header explanation
        VStack(alignment: .leading, spacing: RASpacing.small) {
          Text("Permission Diagnostics")
            .font(RATypography.title3)
            .foregroundColor(RAColors.textPrimary)

          Text("Test and verify permission behaviors for your apps. These diagnostics help you understand how iOS handles audio, location, and geofencing in different scenarios.")
            .font(RATypography.subheadline)
            .foregroundColor(RAColors.textSecondary)
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(RASpacing.large)
        .background(RAColors.backgroundSecondary)
        .cornerRadius(RABorderRadius.large)

        // Audio Diagnostics
        NavigationLink(destination: AudioDiagnosticsView()) {
          RADiagnosticCard(
            icon: "speaker.wave.2",
            iconColor: RAColors.primaryAccent,
            title: "Audio",
            description: "Test audio playback in foreground and background. Verify silent mode behavior and audio session interactions with other apps."
          )
        }
        .buttonStyle(.plain)

        // Location Diagnostics
        NavigationLink(destination: LocationDiagnosticsView()) {
          RADiagnosticCard(
            icon: "location",
            iconColor: RAColors.primaryBlue,
            title: "Background Location",
            description: "Test location tracking when your app is foregrounded, backgrounded, or closed. Verify location permissions and accuracy settings. Location data stays on your device."
          )
        }
        .buttonStyle(.plain)

        // Geofencing Diagnostics
        NavigationLink(destination: GeofencingDiagnosticsView()) {
          RADiagnosticCard(
            icon: "mappin.circle",
            iconColor: RAColors.primaryGreen,
            title: "Geofencing",
            description: "Test region monitoring by defining geographic areas with latitude, longitude, and radius. Verify enter/exit triggers and background notification delivery."
          )
        }
        .buttonStyle(.plain)

        // Privacy note
        VStack(spacing: RASpacing.small) {
          Image(systemName: "lock.shield")
            .font(.system(size: 24))
            .foregroundColor(RAColors.textTertiary)

          Text("All diagnostic data stays on your device")
            .font(RATypography.caption)
            .foregroundColor(RAColors.textTertiary)
        }
        .padding(.top, RASpacing.large)
      }
      .padding(RASpacing.large)
    }
    .background(RAColors.backgroundPrimary)
    .navigationTitle("Diagnostics")
    .navigationBarTitleDisplayMode(.inline)
  }
}

// MARK: - Diagnostic Card
struct RADiagnosticCard: View {
  let icon: String
  let iconColor: Color
  let title: String
  let description: String

  var body: some View {
    HStack(alignment: .top, spacing: RASpacing.medium) {
      ZStack {
        Circle()
          .fill(iconColor.opacity(0.15))
          .frame(width: 44, height: 44)

        Image(systemName: icon)
          .font(.system(size: 20))
          .foregroundColor(iconColor)
      }

      VStack(alignment: .leading, spacing: RASpacing.small) {
        HStack {
          Text(title)
            .font(RATypography.headline)
            .foregroundColor(RAColors.textPrimary)

          Spacer()

          Image(systemName: "chevron.right")
            .font(.caption)
            .foregroundColor(RAColors.textTertiary)
        }

        Text(description)
          .font(RATypography.subheadline)
          .foregroundColor(RAColors.textSecondary)
          .multilineTextAlignment(.leading)
      }
    }
    .padding(RASpacing.large)
    .frame(maxWidth: .infinity, alignment: .leading)
    .background(RAColors.backgroundSecondary)
    .cornerRadius(RABorderRadius.large)
  }
}

#Preview {
  NavigationView {
    RADiagnosticsTabView()
      .environmentObject(HomeViewModel())
  }
}
