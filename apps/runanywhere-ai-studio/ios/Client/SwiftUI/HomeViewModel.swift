// Copyright 2015-present 650 Industries. All rights reserved.
// RUNANYWHERE: Authentication has been removed - this app uses anonymous mode only

import Foundation
import Combine

@MainActor
class HomeViewModel: ObservableObject {
  let dataService: DataService
  let serverService: DevelopmentServerService
  let settingsManager: SettingsManager

  @Published var recentlyOpenedApps: [RecentlyOpenedApp] = []

  @Published var showingFeedbackForm = false
  @Published var errorToShow: ErrorInfo?
  @Published var isNetworkAvailable = true

  @Published var developmentServers: [DevelopmentServer] = []
  @Published var projects: [ExpoProject] = []
  @Published var snacks: [Snack] = []
  @Published var isLoadingData = false
  @Published var dataError: APIError?

  private var cancellables = Set<AnyCancellable>()
  private let persistenceManager = PersistenceManager.shared

  // RUNANYWHERE: Auth removed - these are stubs for compatibility
  var selectedAccount: Account? { nil }
  var isLoggedIn: Bool { false }
  var isAuthenticated: Bool { false }
  var userName: String? { nil }
  var user: UserActor? { nil }

  var shakeToShowDevMenu: Bool { settingsManager.shakeToShowDevMenu }
  var threeFingerLongPressEnabled: Bool { settingsManager.threeFingerLongPressEnabled }
  var selectedTheme: Int { settingsManager.selectedTheme }
  var buildInfo: [String: Any] { settingsManager.buildInfo }

  convenience init() {
    self.init(
      dataService: DataService(),
      serverService: DevelopmentServerService(),
      settingsManager: SettingsManager()
    )
  }

  init(
    dataService: DataService,
    serverService: DevelopmentServerService,
    settingsManager: SettingsManager
  ) {
    self.dataService = dataService
    self.serverService = serverService
    self.settingsManager = settingsManager

    loadRecentlyOpenedApps()
    setupSubscriptions()
    connectViewModelToBridge()
  }

  func onViewWillAppear() {
    serverService.startDiscovery()

    Task {
      await refreshData()
    }
  }

  func onViewDidDisappear() {
    dataService.stopPolling()
    serverService.stopDiscovery()
  }

  // RUNANYWHERE: Auth removed - these are no-ops
  func signIn() async {
    // Authentication has been removed
  }

  func signUp() async {
    // Authentication has been removed
  }

  func signOut() {
    // Authentication has been removed
  }

  func selectAccount(accountId: String) {
    // Authentication has been removed
  }

  func refreshData() async {
    serverService.discoverDevelopmentServers()
    serverService.refreshRemoteSessions()
  }

  func addToRecentlyOpened(url: String, name: String, iconUrl: String? = nil) {
    let normalizedUrl = normalizeUrl(url)

    if let existingIndex = recentlyOpenedApps.firstIndex(where: {
      normalizeUrl($0.url) == normalizedUrl
    }) {
      let existingApp = recentlyOpenedApps[existingIndex]

      if existingApp.name == name && iconUrl != nil && existingApp.iconUrl == nil {
        var updatedApp = existingApp
        updatedApp.iconUrl = iconUrl
        recentlyOpenedApps[existingIndex] = updatedApp
        persistenceManager.saveRecentlyOpened(recentlyOpenedApps)
        return
      }

      recentlyOpenedApps.remove(at: existingIndex)
    }

    let newApp = RecentlyOpenedApp(
      name: name,
      url: url,
      timestamp: Date(),
      isEasUpdate: false,
      iconUrl: iconUrl
    )

    recentlyOpenedApps.insert(newApp, at: 0)

    if recentlyOpenedApps.count > 10 {
      recentlyOpenedApps = Array(recentlyOpenedApps.prefix(10))
    }

    persistenceManager.saveRecentlyOpened(recentlyOpenedApps)
  }

  func clearRecentlyOpenedApps() {
    recentlyOpenedApps = []
    persistenceManager.saveRecentlyOpened([])
  }

  private func loadRecentlyOpenedApps() {
    recentlyOpenedApps = persistenceManager.loadRecentlyOpened()
      .sorted(by: { $0.timestamp > $1.timestamp })
  }

  func openApp(url: String) {
    openAppViaBridge(url: url)
  }

  func updateShakeGesture(_ enabled: Bool) {
    settingsManager.updateShakeGesture(enabled)
  }

  func updateThreeFingerGesture(_ enabled: Bool) {
    settingsManager.updateThreeFingerGesture(enabled)
  }

  func updateTheme(_ themeIndex: Int) {
    settingsManager.updateTheme(themeIndex)
  }

  func showFeedbackForm() {
    showingFeedbackForm = true
  }

  func showError(_ message: String, apiError: APIError? = nil) {
    errorToShow = ErrorInfo(message: message, apiError: apiError)
  }

  private func setupSubscriptions() {
    // RUNANYWHERE: Auth subscriptions removed

    dataService.$projects
      .sink { [weak self] in self?.projects = $0 }
      .store(in: &cancellables)

    dataService.$snacks
      .sink { [weak self] in self?.snacks = $0 }
      .store(in: &cancellables)

    dataService.$isLoadingData
      .sink { [weak self] in self?.isLoadingData = $0 }
      .store(in: &cancellables)

    dataService.$dataError
      .sink { [weak self] in self?.dataError = $0 }
      .store(in: &cancellables)

    serverService.$developmentServers
      .sink { [weak self] in self?.developmentServers = $0 }
      .store(in: &cancellables)

    settingsManager.objectWillChange
      .sink { [weak self] _ in
        self?.objectWillChange.send()
      }
      .store(in: &cancellables)
  }
}

struct ErrorInfo: Identifiable {
  let id = UUID()
  let message: String
  let apiError: APIError?

  init(message: String, apiError: APIError? = nil) {
    self.message = message
    self.apiError = apiError
  }
}

struct RecentlyOpenedApp: Identifiable, Codable {
  var id = UUID()
  let name: String
  let url: String
  let timestamp: Date
  let isEasUpdate: Bool
  var iconUrl: String?
}

struct DevelopmentServer: Identifiable {
  var id: String { url }
  let url: String
  let description: String
  let source: String
  let isRunning: Bool
  var iconUrl: String?
}

struct ExpoProject: Identifiable, Codable {
  let id: String
  let name: String
  let fullName: String
  let description: String?
  let latestUpdateUrl: String?
  let firstTwoBranches: [Branch]
}

enum ExpoGoError: Error {
  case invalidURL
  case noSessionSecret
  case notImplemented(String)
  case missingURLScheme
}
