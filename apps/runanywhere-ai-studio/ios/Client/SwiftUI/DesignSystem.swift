// Copyright 2015-present 650 Industries. All rights reserved.
// RunAnywhere AI Studio - Design System

import SwiftUI
import ExpoModulesCore

// MARK: - Color Extension for Hex Support
extension Color {
  init(hex: UInt, alpha: Double = 1.0) {
    self.init(
      .sRGB,
      red: Double((hex >> 16) & 0xff) / 255,
      green: Double((hex >> 8) & 0xff) / 255,
      blue: Double(hex & 0xff) / 255,
      opacity: alpha
    )
  }
}

// MARK: - RunAnywhere Brand Colors
struct RAColors {
  // ====================
  // PRIMARY ACCENT COLORS - RunAnywhere Brand
  // ====================
  static let primaryAccent = Color(hex: 0xFF5500)  // Vibrant orange-red - primary brand color
  static let primaryOrange = Color(hex: 0xFF5500)
  static let primaryBlue = Color(hex: 0x3B82F6)    // Blue-500 - for secondary elements
  static let primaryGreen = Color(hex: 0x10B981)   // Emerald-500 - success green
  static let primaryRed = Color(hex: 0xEF4444)     // Red-500 - error red
  static let primaryYellow = Color(hex: 0xEAB308)  // Yellow-500
  static let primaryPurple = Color(hex: 0x8B5CF6)  // Violet-500 - purple accent

  // ====================
  // TEXT COLORS
  // ====================
  static let textPrimary = Color.primary
  static let textSecondary = Color.secondary
  static let textTertiary = Color(hex: 0x94A3B8)   // Slate-400
  static let textWhite = Color.white

  // ====================
  // BACKGROUND COLORS - Adaptive
  // ====================
  static let backgroundPrimary = Color(.systemBackground)
  static let backgroundSecondary = Color(.secondarySystemBackground)
  static let backgroundTertiary = Color(.tertiarySystemBackground)
  static let backgroundGrouped = Color(.systemGroupedBackground)
  static let backgroundGray5 = Color(.systemGray5)
  static let backgroundGray6 = Color(.systemGray6)
  static let separator = Color(.separator)

  // Dark mode explicit colors - matching RunAnywhere.ai website
  static let backgroundPrimaryDark = Color(hex: 0x0F172A)    // Deep dark blue-gray
  static let backgroundSecondaryDark = Color(hex: 0x1A1F2E)  // Slightly lighter dark surface
  static let backgroundTertiaryDark = Color(hex: 0x252B3A)   // Medium dark surface

  // ====================
  // MESSAGE BUBBLE COLORS
  // ====================
  static let userBubbleGradientStart = primaryAccent
  static let userBubbleGradientEnd = Color(hex: 0xE64500)
  static let messageBubbleUser = primaryAccent
  static let messageBubbleAssistant = backgroundGray5

  // ====================
  // STATUS COLORS
  // ====================
  static let statusGreen = primaryGreen
  static let statusOrange = primaryOrange
  static let statusRed = primaryRed
  static let statusGray = Color(hex: 0x64748B)
  static let statusBlue = primaryBlue

  // ====================
  // SHADOW COLORS
  // ====================
  static let shadowDefault = Color.black.opacity(0.1)
  static let shadowMedium = Color.black.opacity(0.12)
  static let shadowDark = Color.black.opacity(0.3)
}

// MARK: - RunAnywhere Typography
struct RATypography {
  static let largeTitle = Font.largeTitle.weight(.bold)
  static let title = Font.title.weight(.semibold)
  static let title2 = Font.title2.weight(.semibold)
  static let title3 = Font.title3.weight(.semibold)
  static let headline = Font.headline.weight(.semibold)
  static let body = Font.body
  static let callout = Font.callout
  static let subheadline = Font.subheadline
  static let footnote = Font.footnote
  static let caption = Font.caption.weight(.medium)
  static let caption2 = Font.caption2.weight(.medium)

  static func system(_ size: CGFloat, weight: Font.Weight = .regular) -> Font {
    return .system(size: size, weight: weight)
  }
}

// MARK: - RunAnywhere Spacing
struct RASpacing {
  static let xxSmall: CGFloat = 2
  static let xSmall: CGFloat = 4
  static let small: CGFloat = 8
  static let smallMedium: CGFloat = 10
  static let medium: CGFloat = 12
  static let mediumLarge: CGFloat = 14
  static let large: CGFloat = 16
  static let xLarge: CGFloat = 20
  static let xxLarge: CGFloat = 24
  static let xxxLarge: CGFloat = 32
}

// MARK: - Border Radius
struct RABorderRadius {
  static let small: CGFloat = 4
  static let medium: CGFloat = 8
  static let large: CGFloat = 12
  static let xLarge: CGFloat = 16
  static let full: CGFloat = 9999
}

// MARK: - Legacy Expo Compatibility (for existing code)
extension Color {
  static let expoBlue = RAColors.primaryAccent  // Changed to RunAnywhere orange
  static let expoSecondaryText = RAColors.textSecondary
  static let expoSystemBackground = RAColors.backgroundPrimary
  static let expoSecondarySystemBackground = RAColors.backgroundSecondary
  static let expoSecondarySystemGroupedBackground = Color(uiColor: .secondarySystemGroupedBackground)
  static let expoSystemGray4 = Color(uiColor: .systemGray4)
  static let expoSystemGray5 = RAColors.backgroundGray5
  static let expoSystemGray6 = RAColors.backgroundGray6

  // RunAnywhere specific
  static let runAnywhereAccent = RAColors.primaryAccent
  static let runAnywhereOrange = RAColors.primaryOrange
}

extension Font {
  static func expoCaption(_ size: CGFloat = 12) -> Font {
    return .system(size: size, weight: .medium, design: .default)
  }

  static func raCaption(_ size: CGFloat = 12) -> Font {
    return .system(size: size, weight: .medium, design: .default)
  }
}

// MARK: - Legacy Border Radius Compatibility
struct BorderRadius {
  static let small: CGFloat = RABorderRadius.small
  static let medium: CGFloat = RABorderRadius.medium
  static let large: CGFloat = RABorderRadius.large
}

// MARK: - View Modifiers
struct RASectionHeaderStyle: ViewModifier {
  func body(content: Content) -> some View {
    content
      .font(RATypography.caption)
      .foregroundColor(RAColors.textSecondary)
      .textCase(.uppercase)
  }
}

struct ExpoSectionHeaderStyle: ViewModifier {
  func body(content: Content) -> some View {
    content
      .font(.expoCaption())
      .foregroundColor(.expoSecondaryText)
      .textCase(.uppercase)
  }
}

extension View {
  func expoSectionHeader() -> some View {
    self.modifier(ExpoSectionHeaderStyle())
  }

  func raSectionHeader() -> some View {
    self.modifier(RASectionHeaderStyle())
  }
}

// MARK: - Button Styles
struct RAPrimaryButtonStyle: ButtonStyle {
  func makeBody(configuration: Configuration) -> some View {
    configuration.label
      .padding(.horizontal, RASpacing.large)
      .padding(.vertical, RASpacing.medium)
      .background(RAColors.primaryAccent)
      .foregroundColor(.white)
      .cornerRadius(RABorderRadius.medium)
      .scaleEffect(configuration.isPressed ? 0.95 : 1.0)
      .animation(.easeInOut(duration: 0.1), value: configuration.isPressed)
  }
}

struct RASecondaryButtonStyle: ButtonStyle {
  func makeBody(configuration: Configuration) -> some View {
    configuration.label
      .padding(.horizontal, RASpacing.large)
      .padding(.vertical, RASpacing.medium)
      .background(RAColors.backgroundSecondary)
      .foregroundColor(RAColors.primaryAccent)
      .cornerRadius(RABorderRadius.medium)
      .overlay(
        RoundedRectangle(cornerRadius: RABorderRadius.medium)
          .stroke(RAColors.primaryAccent.opacity(0.3), lineWidth: 1)
      )
      .scaleEffect(configuration.isPressed ? 0.95 : 1.0)
      .animation(.easeInOut(duration: 0.1), value: configuration.isPressed)
  }
}

extension ButtonStyle where Self == RAPrimaryButtonStyle {
  static var raPrimary: RAPrimaryButtonStyle { RAPrimaryButtonStyle() }
}

extension ButtonStyle where Self == RASecondaryButtonStyle {
  static var raSecondary: RASecondaryButtonStyle { RASecondaryButtonStyle() }
}

// MARK: - Card Style
struct RACardStyle: ViewModifier {
  func body(content: Content) -> some View {
    content
      .padding(RASpacing.large)
      .background(RAColors.backgroundSecondary)
      .cornerRadius(RABorderRadius.large)
      .shadow(color: RAColors.shadowDefault, radius: 4, x: 0, y: 2)
  }
}

extension View {
  func raCard() -> some View {
    self.modifier(RACardStyle())
  }
}
