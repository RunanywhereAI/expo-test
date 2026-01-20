// Copyright 2015-present 650 Industries. All rights reserved.
// RunAnywhere AI Studio - Home View

import SwiftUI

struct RAHomeView: View {
  @EnvironmentObject var viewModel: HomeViewModel
  @StateObject private var reviewManager = UserReviewManager()

  var body: some View {
    VStack(spacing: 0) {
      // Navigation Header with RunAnywhere branding
      RANavigationHeader()

      ScrollView {
        VStack(spacing: RASpacing.xLarge) {
          // Feedback form navigation (hidden link)
          NavigationLink(destination: FeedbackFormView(), isActive: $viewModel.showingFeedbackForm) {
            EmptyView()
          }

          // User Review Section (if applicable)
          if reviewManager.shouldShowReviewSection {
            UserReviewSection(reviewManager: reviewManager) {
              viewModel.showFeedbackForm()
            }
          }

          // Upgrade Warning
          UpgradeWarningView()

          // Development Servers Section - PRIMARY FUNCTIONALITY
          RADevServersSection()

          // Recently Opened Apps
          if !viewModel.recentlyOpenedApps.isEmpty {
            RARecentlyOpenedSection()
          }

          // Projects (if logged in)
          if viewModel.isLoggedIn {
            if viewModel.isLoadingData && viewModel.projects.isEmpty {
              ProjectsLoadingSection()
            } else if !viewModel.projects.isEmpty {
              RAProjectsSection()
            } else if !viewModel.isLoadingData {
              RAEmptyProjectsSection()
            }

            // Snacks (if logged in)
            if viewModel.isLoadingData && viewModel.snacks.isEmpty {
              SnacksLoadingSection()
            } else if !viewModel.snacks.isEmpty {
              RASnacksSection()
            } else if !viewModel.isLoadingData {
              RAEmptySnacksSection()
            }
          }

          // Footer with tips
          footerSection
        }
        .padding()
      }
      .background(RAColors.backgroundPrimary)
      .refreshable {
        await viewModel.refreshData()
      }
    }
    .onAppear {
      viewModel.onViewWillAppear()
      reviewManager.recordHomeAppear()
      reviewManager.updateCounts(apps: viewModel.projects.count, snacks: viewModel.snacks.count)
    }
    .onDisappear {
      viewModel.onViewDidDisappear()
    }
    .onChange(of: viewModel.projects.count) { _ in
      reviewManager.updateCounts(apps: viewModel.projects.count, snacks: viewModel.snacks.count)
    }
    .onChange(of: viewModel.snacks.count) { _ in
      reviewManager.updateCounts(apps: viewModel.projects.count, snacks: viewModel.snacks.count)
    }
  }

  // MARK: - Footer Section
  private var footerSection: some View {
    VStack(spacing: RASpacing.small) {
      Text("Run 'npx expo start' in your project, then enter the URL above")
        .font(RATypography.caption)
        .foregroundColor(RAColors.textTertiary)
        .multilineTextAlignment(.center)

      Link("runanywhere.dev", destination: URL(string: "https://runanywhere.dev")!)
        .font(RATypography.caption)
        .foregroundColor(RAColors.primaryAccent)
    }
    .padding(.top, RASpacing.large)
    .padding(.bottom, RASpacing.xxLarge)
  }
}

// MARK: - RunAnywhere Navigation Header
struct RANavigationHeader: View {
  @EnvironmentObject var viewModel: HomeViewModel
  @EnvironmentObject var navigation: ExpoGoNavigation

  var body: some View {
    HStack {
      // Logo and title
      HStack(spacing: RASpacing.medium) {
        ZStack {
          Circle()
            .fill(RAColors.primaryAccent)
            .frame(width: 36, height: 36)

          Image(systemName: "cpu")
            .font(.system(size: 18, weight: .medium))
            .foregroundColor(.white)
        }

        VStack(alignment: .leading, spacing: 0) {
          Text("RunAnywhere")
            .font(RATypography.headline)
            .foregroundColor(RAColors.textPrimary)
          Text("AI Studio")
            .font(RATypography.caption)
            .foregroundColor(RAColors.primaryAccent)
        }
      }

      Spacer()

      // Account button
      Button(action: { navigation.showUserProfile() }) {
        if viewModel.isLoggedIn, let user = viewModel.user {
          // Create a simple avatar from user profile
          RAUserAvatar(profilePhoto: user.profilePhoto, username: user.username, size: 32)
        } else {
          Image(systemName: "person.circle")
            .font(.system(size: 28))
            .foregroundColor(RAColors.textSecondary)
        }
      }
    }
    .padding(.horizontal, RASpacing.large)
    .padding(.vertical, RASpacing.medium)
    .background(RAColors.backgroundSecondary)
  }
}

// MARK: - Dev Servers Section (Primary Functionality)
struct RADevServersSection: View {
  @EnvironmentObject var viewModel: HomeViewModel
  @State private var manualURL = ""
  @FocusState private var isURLFocused: Bool

  var body: some View {
    VStack(alignment: .leading, spacing: RASpacing.medium) {
      RASectionHeader(title: "ENTER URL OR SCAN QR CODE")

      // Manual URL Entry - PRIMARY ACTION
      VStack(spacing: RASpacing.medium) {
        HStack(spacing: RASpacing.medium) {
          TextField("exp://192.168.x.x:8081", text: $manualURL)
            .textFieldStyle(.plain)
            .font(RATypography.body)
            .padding(RASpacing.medium)
            .background(RAColors.backgroundSecondary)
            .cornerRadius(RABorderRadius.medium)
            .focused($isURLFocused)
            .autocapitalization(.none)
            .disableAutocorrection(true)
            .keyboardType(.URL)
            .onSubmit {
              openURL()
            }

          Button(action: openURL) {
            Image(systemName: "arrow.right.circle.fill")
              .font(.system(size: 32))
              .foregroundColor(manualURL.isEmpty ? RAColors.textTertiary : RAColors.primaryAccent)
          }
          .disabled(manualURL.isEmpty)
        }

        Text("Enter the URL shown when you run 'npx expo start'")
          .font(RATypography.caption)
          .foregroundColor(RAColors.textTertiary)
      }
      .padding(RASpacing.large)
      .background(RAColors.backgroundSecondary)
      .cornerRadius(RABorderRadius.large)

      // Discovered Dev Servers
      if !viewModel.developmentServers.isEmpty {
        VStack(alignment: .leading, spacing: RASpacing.small) {
          Text("Local Development Servers")
            .font(RATypography.caption)
            .foregroundColor(RAColors.textSecondary)
            .textCase(.uppercase)

          ForEach(viewModel.developmentServers, id: \.url) { server in
            RADevServerRow(server: server)
          }
        }
      }
    }
  }

  private func openURL() {
    guard !manualURL.isEmpty else { return }
    isURLFocused = false

    var url = manualURL
    if !url.hasPrefix("exp://") && !url.hasPrefix("http://") && !url.hasPrefix("https://") {
      url = "exp://\(url)"
    }

    viewModel.openApp(url: url)
    manualURL = ""
  }
}

// MARK: - Dev Server Row
struct RADevServerRow: View {
  @EnvironmentObject var viewModel: HomeViewModel
  let server: DevelopmentServer

  var body: some View {
    Button(action: { viewModel.openApp(url: server.url) }) {
      HStack(spacing: RASpacing.medium) {
        ZStack {
          Circle()
            .fill(RAColors.statusGreen.opacity(0.2))
            .frame(width: 8, height: 8)

          Circle()
            .fill(RAColors.statusGreen)
            .frame(width: 4, height: 4)
        }

        VStack(alignment: .leading, spacing: RASpacing.xxSmall) {
          Text(server.description)
            .font(RATypography.body)
            .foregroundColor(RAColors.textPrimary)

          Text(server.url)
            .font(RATypography.caption)
            .foregroundColor(RAColors.textSecondary)
        }

        Spacer()

        Image(systemName: "chevron.right")
          .foregroundColor(RAColors.textTertiary)
      }
      .padding(RASpacing.medium)
      .background(RAColors.backgroundSecondary)
      .cornerRadius(RABorderRadius.medium)
    }
    .buttonStyle(.plain)
  }
}

// MARK: - Recently Opened Section
struct RARecentlyOpenedSection: View {
  @EnvironmentObject var viewModel: HomeViewModel

  var body: some View {
    VStack(alignment: .leading, spacing: RASpacing.medium) {
      HStack {
        RASectionHeader(title: "RECENTLY OPENED")
        Spacer()
        Button("Clear") {
          viewModel.clearRecentlyOpenedApps()
        }
        .font(RATypography.caption)
        .foregroundColor(RAColors.primaryAccent)
      }

      ForEach(viewModel.recentlyOpenedApps.prefix(5), id: \.url) { app in
        RARecentAppRow(app: app)
      }
    }
  }
}

// MARK: - Recent App Row
struct RARecentAppRow: View {
  @EnvironmentObject var viewModel: HomeViewModel
  let app: RecentlyOpenedApp

  var body: some View {
    Button(action: { viewModel.openApp(url: app.url) }) {
      HStack(spacing: RASpacing.medium) {
        Image(systemName: "app")
          .font(.system(size: 24))
          .foregroundColor(RAColors.primaryAccent)
          .frame(width: 40, height: 40)
          .background(RAColors.primaryAccent.opacity(0.1))
          .cornerRadius(RABorderRadius.small)

        VStack(alignment: .leading, spacing: RASpacing.xxSmall) {
          Text(app.name)
            .font(RATypography.body)
            .foregroundColor(RAColors.textPrimary)

          Text(app.url)
            .font(RATypography.caption)
            .foregroundColor(RAColors.textSecondary)
            .lineLimit(1)
        }

        Spacer()

        Image(systemName: "chevron.right")
          .foregroundColor(RAColors.textTertiary)
      }
      .padding(RASpacing.medium)
      .background(RAColors.backgroundSecondary)
      .cornerRadius(RABorderRadius.medium)
    }
    .buttonStyle(.plain)
  }
}

// MARK: - Projects Section
struct RAProjectsSection: View {
  @EnvironmentObject var viewModel: HomeViewModel

  var body: some View {
    VStack(alignment: .leading, spacing: RASpacing.medium) {
      RASectionHeader(title: "PROJECTS")

      ForEach(viewModel.projects) { project in
        NavigationLink(destination: ProjectDetailsView(projectId: project.id, initialProject: project)) {
          ProjectRow(project: project) {
            // Navigation handled by NavigationLink
          }
        }
        .buttonStyle(.plain)
      }
    }
  }
}

// MARK: - Empty Projects Section
struct RAEmptyProjectsSection: View {
  var body: some View {
    VStack(alignment: .leading, spacing: RASpacing.medium) {
      RASectionHeader(title: "PROJECTS")

      VStack(spacing: RASpacing.medium) {
        Image(systemName: "folder")
          .font(.system(size: 32))
          .foregroundColor(RAColors.textTertiary)

        Text("No projects yet")
          .font(RATypography.body)
          .foregroundColor(RAColors.textPrimary)

        Text("Create your first project on expo.dev")
          .font(RATypography.caption)
          .foregroundColor(RAColors.textSecondary)
      }
      .frame(maxWidth: .infinity)
      .padding(RASpacing.xxLarge)
      .background(RAColors.backgroundSecondary)
      .cornerRadius(RABorderRadius.large)
    }
  }
}

// MARK: - Snacks Section
struct RASnacksSection: View {
  @EnvironmentObject var viewModel: HomeViewModel

  var body: some View {
    VStack(alignment: .leading, spacing: RASpacing.medium) {
      RASectionHeader(title: "SNACKS")

      ForEach(viewModel.snacks.prefix(5)) { snack in
        SnackRowWithAction(snack: snack)
      }

      if viewModel.snacks.count > 5 {
        NavigationLink(destination: SnacksListView(accountName: viewModel.selectedAccount?.name ?? "")) {
          Text("See all snacks (\(viewModel.snacks.count))")
            .font(RATypography.body)
            .foregroundColor(RAColors.primaryAccent)
            .frame(maxWidth: .infinity)
            .padding(RASpacing.medium)
            .background(RAColors.backgroundSecondary)
            .cornerRadius(RABorderRadius.medium)
        }
      }
    }
  }
}

// MARK: - Empty Snacks Section
struct RAEmptySnacksSection: View {
  var body: some View {
    VStack(alignment: .leading, spacing: RASpacing.medium) {
      RASectionHeader(title: "SNACKS")

      VStack(spacing: RASpacing.medium) {
        Image(systemName: "play.rectangle")
          .font(.system(size: 32))
          .foregroundColor(RAColors.textTertiary)

        Text("No snacks yet")
          .font(RATypography.body)
          .foregroundColor(RAColors.textPrimary)

        Text("Try Snack to experiment with Expo")
          .font(RATypography.caption)
          .foregroundColor(RAColors.textSecondary)
      }
      .frame(maxWidth: .infinity)
      .padding(RASpacing.xxLarge)
      .background(RAColors.backgroundSecondary)
      .cornerRadius(RABorderRadius.large)
    }
  }
}

// MARK: - Section Header
struct RASectionHeader: View {
  let title: String

  var body: some View {
    Text(title)
      .font(RATypography.caption)
      .fontWeight(.semibold)
      .foregroundColor(RAColors.textSecondary)
      .textCase(.uppercase)
  }
}

// MARK: - User Avatar (Simple version without Account dependency)
struct RAUserAvatar: View {
  let profilePhoto: String?
  let username: String
  let size: CGFloat

  var body: some View {
    if let photo = profilePhoto, !photo.isEmpty, let url = URL(string: photo) {
      AsyncImage(url: url) { phase in
        switch phase {
        case .success(let image):
          image
            .resizable()
            .scaledToFill()
        case .failure, .empty:
          placeholderView
        @unknown default:
          placeholderView
        }
      }
      .frame(width: size, height: size)
      .clipShape(Circle())
    } else {
      placeholderView
    }
  }

  private var placeholderView: some View {
    let firstLetter = String(username.prefix(1).uppercased())
    let color = getAvatarColor(for: firstLetter)

    return Circle()
      .fill(color.background)
      .frame(width: size, height: size)
      .overlay(
        Text(firstLetter)
          .font(.system(size: size * 0.44, weight: .medium))
          .foregroundColor(color.foreground)
      )
  }
}

#Preview {
  RAHomeView()
    .environmentObject(HomeViewModel())
    .environmentObject(ExpoGoNavigation())
}
