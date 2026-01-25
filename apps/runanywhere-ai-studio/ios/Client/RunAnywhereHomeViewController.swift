// Copyright 2026-present RunAnywhere. All rights reserved.
// Native home screen for RunAnywhere AI Studio

import UIKit

/**
 * RunAnywhereHomeViewController
 *
 * A native home screen for RunAnywhere AI Studio that:
 * 1. Shows RunAnywhere branding and capabilities
 * 2. Allows connecting to Metro bundler for development
 * 3. Bypasses the kernel JS entirely (avoiding polyfill issues)
 */
@objc(RunAnywhereHomeViewController)
public class RunAnywhereHomeViewController: UIViewController {
  
  // MARK: - EXAppViewController compatibility
  
  @objc public var contentView: UIView? {
    return view
  }
  
  @objc public func backgroundControllers() {
    // No-op for native home
  }
  
  // MARK: - Colors (RunAnywhere Brand)
  
  private let brandPrimary = UIColor(red: 0.29, green: 0.56, blue: 0.85, alpha: 1.0)  // Blue
  private let brandAccent = UIColor(red: 0.4, green: 0.8, blue: 0.6, alpha: 1.0)      // Green
  private let backgroundDark = UIColor(red: 0.09, green: 0.106, blue: 0.13, alpha: 1.0)
  private let cardBackground = UIColor(red: 0.12, green: 0.14, blue: 0.18, alpha: 1.0)
  private let textPrimary = UIColor.white
  private let textSecondary = UIColor(white: 0.6, alpha: 1.0)
  private let textMuted = UIColor(white: 0.4, alpha: 1.0)
  
  // MARK: - UI Components
  
  private lazy var scrollView: UIScrollView = {
    let sv = UIScrollView()
    sv.translatesAutoresizingMaskIntoConstraints = false
    sv.showsVerticalScrollIndicator = false
    return sv
  }()
  
  private lazy var contentStack: UIStackView = {
    let stack = UIStackView()
    stack.axis = .vertical
    stack.spacing = 24
    stack.alignment = .fill
    stack.translatesAutoresizingMaskIntoConstraints = false
    return stack
  }()
  
  private lazy var logoLabel: UILabel = {
    let label = UILabel()
    label.text = "RA"
    label.font = UIFont.systemFont(ofSize: 48, weight: .black)
    label.textColor = brandPrimary
    label.textAlignment = .center
    return label
  }()
  
  private lazy var titleLabel: UILabel = {
    let label = UILabel()
    label.text = "RunAnywhere AI Studio"
    label.font = UIFont.systemFont(ofSize: 28, weight: .bold)
    label.textColor = textPrimary
    label.textAlignment = .center
    return label
  }()
  
  private lazy var subtitleLabel: UILabel = {
    let label = UILabel()
    label.text = "On-Device AI Development Environment"
    label.font = UIFont.systemFont(ofSize: 16, weight: .regular)
    label.textColor = textSecondary
    label.textAlignment = .center
    return label
  }()
  
  private lazy var featuresCard: UIView = {
    let card = createCard()
    
    let titleLabel = UILabel()
    titleLabel.text = "Capabilities"
    titleLabel.font = UIFont.systemFont(ofSize: 18, weight: .semibold)
    titleLabel.textColor = textPrimary
    titleLabel.translatesAutoresizingMaskIntoConstraints = false
    
    let featuresStack = UIStackView()
    featuresStack.axis = .vertical
    featuresStack.spacing = 12
    featuresStack.translatesAutoresizingMaskIntoConstraints = false
    
    let features = [
      ("brain", "LLM Inference", "Run large language models locally with llama.cpp"),
      ("cpu", "ONNX Runtime", "Execute ML models with hardware acceleration"),
      ("iphone", "On-Device AI", "No internet required for AI processing"),
      ("bolt.fill", "Fast & Private", "Your data never leaves the device")
    ]
    
    for (icon, title, desc) in features {
      featuresStack.addArrangedSubview(createFeatureRow(icon: icon, title: title, description: desc))
    }
    
    card.addSubview(titleLabel)
    card.addSubview(featuresStack)
    
    NSLayoutConstraint.activate([
      titleLabel.topAnchor.constraint(equalTo: card.topAnchor, constant: 16),
      titleLabel.leadingAnchor.constraint(equalTo: card.leadingAnchor, constant: 16),
      titleLabel.trailingAnchor.constraint(equalTo: card.trailingAnchor, constant: -16),
      
      featuresStack.topAnchor.constraint(equalTo: titleLabel.bottomAnchor, constant: 16),
      featuresStack.leadingAnchor.constraint(equalTo: card.leadingAnchor, constant: 16),
      featuresStack.trailingAnchor.constraint(equalTo: card.trailingAnchor, constant: -16),
      featuresStack.bottomAnchor.constraint(equalTo: card.bottomAnchor, constant: -16)
    ])
    
    return card
  }()
  
  private lazy var connectCard: UIView = {
    let card = createCard()
    
    let titleLabel = UILabel()
    titleLabel.text = "Connect to Development Server"
    titleLabel.font = UIFont.systemFont(ofSize: 18, weight: .semibold)
    titleLabel.textColor = textPrimary
    titleLabel.translatesAutoresizingMaskIntoConstraints = false
    
    let descLabel = UILabel()
    descLabel.text = "Enter your Metro bundler URL to load your app"
    descLabel.font = UIFont.systemFont(ofSize: 14)
    descLabel.textColor = textSecondary
    descLabel.numberOfLines = 0
    descLabel.translatesAutoresizingMaskIntoConstraints = false
    
    card.addSubview(titleLabel)
    card.addSubview(descLabel)
    card.addSubview(urlTextField)
    card.addSubview(connectButton)
    
    NSLayoutConstraint.activate([
      titleLabel.topAnchor.constraint(equalTo: card.topAnchor, constant: 16),
      titleLabel.leadingAnchor.constraint(equalTo: card.leadingAnchor, constant: 16),
      titleLabel.trailingAnchor.constraint(equalTo: card.trailingAnchor, constant: -16),
      
      descLabel.topAnchor.constraint(equalTo: titleLabel.bottomAnchor, constant: 8),
      descLabel.leadingAnchor.constraint(equalTo: card.leadingAnchor, constant: 16),
      descLabel.trailingAnchor.constraint(equalTo: card.trailingAnchor, constant: -16),
      
      urlTextField.topAnchor.constraint(equalTo: descLabel.bottomAnchor, constant: 16),
      urlTextField.leadingAnchor.constraint(equalTo: card.leadingAnchor, constant: 16),
      urlTextField.trailingAnchor.constraint(equalTo: card.trailingAnchor, constant: -16),
      urlTextField.heightAnchor.constraint(equalToConstant: 50),
      
      connectButton.topAnchor.constraint(equalTo: urlTextField.bottomAnchor, constant: 16),
      connectButton.leadingAnchor.constraint(equalTo: card.leadingAnchor, constant: 16),
      connectButton.trailingAnchor.constraint(equalTo: card.trailingAnchor, constant: -16),
      connectButton.heightAnchor.constraint(equalToConstant: 50),
      connectButton.bottomAnchor.constraint(equalTo: card.bottomAnchor, constant: -16)
    ])
    
    return card
  }()
  
  private lazy var urlTextField: UITextField = {
    let field = UITextField()
    field.text = "exp://192.168.1.100:8081"
    field.font = UIFont.systemFont(ofSize: 16)
    field.textColor = textPrimary
    field.backgroundColor = backgroundDark
    field.layer.cornerRadius = 10
    field.layer.borderWidth = 1
    field.layer.borderColor = UIColor(white: 0.2, alpha: 1.0).cgColor
    field.leftView = UIView(frame: CGRect(x: 0, y: 0, width: 16, height: 0))
    field.leftViewMode = .always
    field.rightView = UIView(frame: CGRect(x: 0, y: 0, width: 16, height: 0))
    field.rightViewMode = .always
    field.attributedPlaceholder = NSAttributedString(
      string: "exp://192.168.x.x:8081",
      attributes: [.foregroundColor: textMuted]
    )
    field.autocorrectionType = .no
    field.autocapitalizationType = .none
    field.keyboardType = .URL
    field.returnKeyType = .go
    field.translatesAutoresizingMaskIntoConstraints = false
    return field
  }()
  
  private lazy var connectButton: UIButton = {
    let button = UIButton(type: .system)
    button.setTitle("Connect", for: .normal)
    button.titleLabel?.font = UIFont.systemFont(ofSize: 17, weight: .semibold)
    button.setTitleColor(.white, for: .normal)
    button.backgroundColor = brandPrimary
    button.layer.cornerRadius = 10
    button.translatesAutoresizingMaskIntoConstraints = false
    return button
  }()
  
  private lazy var instructionsLabel: UILabel = {
    let label = UILabel()
    label.text = "Start your development server with: npx expo start"
    label.font = UIFont.systemFont(ofSize: 13)
    label.textColor = textMuted
    label.textAlignment = .center
    label.numberOfLines = 0
    return label
  }()
  
  private lazy var versionLabel: UILabel = {
    let label = UILabel()
    let version = Bundle.main.infoDictionary?["CFBundleShortVersionString"] as? String ?? "1.0"
    let build = Bundle.main.infoDictionary?["CFBundleVersion"] as? String ?? "1"
    label.text = "RunAnywhere AI Studio v\(version) (\(build))"
    label.font = UIFont.systemFont(ofSize: 12)
    label.textColor = textMuted
    label.textAlignment = .center
    return label
  }()
  
  // MARK: - Lifecycle
  
  public override func viewDidLoad() {
    super.viewDidLoad()
    setupUI()
    setupActions()
    
    // Enable shake gesture detection
    becomeFirstResponder()
  }
  
  public override var canBecomeFirstResponder: Bool {
    return true
  }
  
  public override func motionEnded(_ motion: UIEvent.EventSubtype, with event: UIEvent?) {
    if motion == .motionShake {
      // Open dev menu on shake
      EXDevMenuManager.sharedInstance().toggle()
    }
  }
  
  public override var preferredStatusBarStyle: UIStatusBarStyle {
    return .lightContent
  }
  
  // MARK: - UI Setup
  
  private func setupUI() {
    view.backgroundColor = backgroundDark
    
    view.addSubview(scrollView)
    scrollView.addSubview(contentStack)
    
    // Header
    let headerStack = UIStackView(arrangedSubviews: [logoLabel, titleLabel, subtitleLabel])
    headerStack.axis = .vertical
    headerStack.spacing = 8
    headerStack.alignment = .center
    
    contentStack.addArrangedSubview(headerStack)
    contentStack.addArrangedSubview(featuresCard)
    contentStack.addArrangedSubview(connectCard)
    contentStack.addArrangedSubview(instructionsLabel)
    
    // Spacer
    let spacer = UIView()
    spacer.setContentHuggingPriority(.defaultLow, for: .vertical)
    contentStack.addArrangedSubview(spacer)
    
    contentStack.addArrangedSubview(versionLabel)
    
    NSLayoutConstraint.activate([
      scrollView.topAnchor.constraint(equalTo: view.safeAreaLayoutGuide.topAnchor),
      scrollView.leadingAnchor.constraint(equalTo: view.leadingAnchor),
      scrollView.trailingAnchor.constraint(equalTo: view.trailingAnchor),
      scrollView.bottomAnchor.constraint(equalTo: view.bottomAnchor),
      
      contentStack.topAnchor.constraint(equalTo: scrollView.topAnchor, constant: 32),
      contentStack.leadingAnchor.constraint(equalTo: scrollView.leadingAnchor, constant: 20),
      contentStack.trailingAnchor.constraint(equalTo: scrollView.trailingAnchor, constant: -20),
      contentStack.bottomAnchor.constraint(equalTo: scrollView.bottomAnchor, constant: -32),
      contentStack.widthAnchor.constraint(equalTo: scrollView.widthAnchor, constant: -40)
    ])
  }
  
  private func setupActions() {
    connectButton.addTarget(self, action: #selector(connectTapped), for: .touchUpInside)
    urlTextField.delegate = self
    
    let tapGesture = UITapGestureRecognizer(target: self, action: #selector(dismissKeyboard))
    tapGesture.cancelsTouchesInView = false
    view.addGestureRecognizer(tapGesture)
  }
  
  // MARK: - Helper Methods
  
  private func createCard() -> UIView {
    let card = UIView()
    card.backgroundColor = cardBackground
    card.layer.cornerRadius = 16
    card.translatesAutoresizingMaskIntoConstraints = false
    return card
  }
  
  private func createFeatureRow(icon: String, title: String, description: String) -> UIView {
    let container = UIView()
    
    let iconView = UIImageView()
    if let systemImage = UIImage(systemName: icon) {
      iconView.image = systemImage
    } else {
      iconView.image = UIImage(systemName: "star.fill")
    }
    iconView.tintColor = brandAccent
    iconView.contentMode = .scaleAspectFit
    iconView.translatesAutoresizingMaskIntoConstraints = false
    
    let titleLabel = UILabel()
    titleLabel.text = title
    titleLabel.font = UIFont.systemFont(ofSize: 15, weight: .semibold)
    titleLabel.textColor = textPrimary
    titleLabel.translatesAutoresizingMaskIntoConstraints = false
    
    let descLabel = UILabel()
    descLabel.text = description
    descLabel.font = UIFont.systemFont(ofSize: 13)
    descLabel.textColor = textSecondary
    descLabel.numberOfLines = 0
    descLabel.translatesAutoresizingMaskIntoConstraints = false
    
    container.addSubview(iconView)
    container.addSubview(titleLabel)
    container.addSubview(descLabel)
    
    NSLayoutConstraint.activate([
      iconView.leadingAnchor.constraint(equalTo: container.leadingAnchor),
      iconView.topAnchor.constraint(equalTo: container.topAnchor, constant: 2),
      iconView.widthAnchor.constraint(equalToConstant: 24),
      iconView.heightAnchor.constraint(equalToConstant: 24),
      
      titleLabel.leadingAnchor.constraint(equalTo: iconView.trailingAnchor, constant: 12),
      titleLabel.topAnchor.constraint(equalTo: container.topAnchor),
      titleLabel.trailingAnchor.constraint(equalTo: container.trailingAnchor),
      
      descLabel.leadingAnchor.constraint(equalTo: titleLabel.leadingAnchor),
      descLabel.topAnchor.constraint(equalTo: titleLabel.bottomAnchor, constant: 2),
      descLabel.trailingAnchor.constraint(equalTo: container.trailingAnchor),
      descLabel.bottomAnchor.constraint(equalTo: container.bottomAnchor)
    ])
    
    return container
  }
  
  // MARK: - Actions
  
  @objc private func connectTapped() {
    openExperience()
  }
  
  @objc private func dismissKeyboard() {
    view.endEditing(true)
  }
  
  private func openExperience() {
    guard let urlString = urlTextField.text?.trimmingCharacters(in: .whitespacesAndNewlines),
          !urlString.isEmpty else {
      showAlert(title: "Missing URL", message: "Please enter a Metro bundler URL")
      return
    }
    
    guard let url = URL(string: urlString) else {
      showAlert(title: "Invalid URL", message: "Please enter a valid URL")
      return
    }
    
    EXKernel.sharedInstance().createNewApp(with: url, initialProps: nil)
  }
  
  private func showAlert(title: String, message: String) {
    let alert = UIAlertController(title: title, message: message, preferredStyle: .alert)
    alert.addAction(UIAlertAction(title: "OK", style: .default))
    present(alert, animated: true)
  }
}

// MARK: - UITextFieldDelegate

extension RunAnywhereHomeViewController: UITextFieldDelegate {
  public func textFieldShouldReturn(_ textField: UITextField) -> Bool {
    textField.resignFirstResponder()
    openExperience()
    return true
  }
}
