// Copyright 2015-present 650 Industries. All rights reserved.
// RunAnywhere AI Studio - Main Navigation

import SwiftUI
import UIKit

enum HomeTab: Hashable {
  case home
  case diagnostics
  case settings
}

// MARK: - Main Root View
public struct HomeRootView: View {
  @ObservedObject var viewModel: HomeViewModel
  @State private var showingUserProfile = false
  @State private var selectedTab: HomeTab = .home

  init(viewModel: HomeViewModel) {
    self.viewModel = viewModel

    // Customize tab bar appearance with RunAnywhere colors
    let appearance = UITabBarAppearance()
    appearance.configureWithOpaqueBackground()
    UITabBar.appearance().standardAppearance = appearance
    UITabBar.appearance().scrollEdgeAppearance = appearance
    UITabBar.appearance().tintColor = UIColor(Color(hex: 0xFF5500))
  }

  public var body: some View {
    TabView(selection: $selectedTab) {
      NavigationView {
        RAHomeView()
      }
      .tabItem {
        Image(systemName: "house.fill")
        Text("Home")
      }
      .tag(HomeTab.home)

      NavigationView {
        RADiagnosticsTabView()
      }
      .tabItem {
        Image(systemName: "stethoscope")
        Text("Diagnostics")
      }
      .tag(HomeTab.diagnostics)

      RASettingsView(selectedTab: $selectedTab)
        .tabItem {
          Image(systemName: "gearshape.fill")
          Text("Settings")
        }
        .tag(HomeTab.settings)
    }
    .accentColor(RAColors.primaryAccent)
    .environmentObject(viewModel)
    .environmentObject(ExpoGoNavigation(showingUserProfile: $showingUserProfile))
    .sheet(isPresented: $showingUserProfile) {
      AccountSheet()
        .environmentObject(viewModel)
    }
    .alert(item: $viewModel.errorToShow) { error in
      Alert(
        title: Text("Error"),
        message: Text(error.message),
        dismissButton: .default(Text("OK"))
      )
    }
  }
}

// MARK: - Legacy Navigation Class
class ExpoGoNavigation: ObservableObject {
  @Binding var showingUserProfile: Bool

  init(showingUserProfile: Binding<Bool>) {
    self._showingUserProfile = showingUserProfile
  }

  func showUserProfile() {
    showingUserProfile = true
  }
}
