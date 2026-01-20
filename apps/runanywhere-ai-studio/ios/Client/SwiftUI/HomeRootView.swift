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
    .environmentObject(ExpoGoNavigation())
    .alert(item: $viewModel.errorToShow) { error in
      Alert(
        title: Text("Error"),
        message: Text(error.message),
        dismissButton: .default(Text("OK"))
      )
    }
  }
}

// MARK: - Navigation Class (Auth removed - stub only)
class ExpoGoNavigation: ObservableObject {
  // RUNANYWHERE: Auth removed - showUserProfile is a no-op
  func showUserProfile() {
    // Authentication has been removed - no account sheet to show
  }
}
