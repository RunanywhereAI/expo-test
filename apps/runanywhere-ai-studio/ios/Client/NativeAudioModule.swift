// NativeAudioModule.swift
// Native audio recording and playback module for RunAnywhere SDK
// Copyright © 2025 RunAnywhere, Inc. All rights reserved.
//
// NOTE: This module should ideally be part of @runanywhere/core SDK
// but is currently provided by the host app. Future SDK versions
// should bundle this natively.

import Foundation
import AVFoundation
import React

@objc(NativeAudioModule)
class NativeAudioModule: NSObject {
  
  private var audioRecorder: AVAudioRecorder?
  private var audioPlayer: AVAudioPlayer?
  private var recordingURL: URL?
  private var isRecording = false
  private var isPlaying = false
  
  // MARK: - Recording
  
  @objc
  func startRecording(_ resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) {
    DispatchQueue.main.async { [weak self] in
      guard let self = self else {
        reject("ERROR", "Module deallocated", nil)
        return
      }
      
      do {
        // Configure audio session for recording
        let session = AVAudioSession.sharedInstance()
        try session.setCategory(.playAndRecord, mode: .default, options: [.defaultToSpeaker, .allowBluetooth])
        try session.setActive(true)
        
        // Create temp file for recording - use .wav extension for compatibility
        let tempDir = FileManager.default.temporaryDirectory
        let fileName = "recording_\(Int(Date().timeIntervalSince1970)).wav"
        let fileURL = tempDir.appendingPathComponent(fileName)
        self.recordingURL = fileURL
        
        // Recording settings for 16kHz mono PCM (optimal for STT)
        let settings: [String: Any] = [
          AVFormatIDKey: Int(kAudioFormatLinearPCM),
          AVSampleRateKey: 16000.0,
          AVNumberOfChannelsKey: 1,
          AVLinearPCMBitDepthKey: 16,
          AVLinearPCMIsFloatKey: false,
          AVLinearPCMIsBigEndianKey: false,
          AVEncoderAudioQualityKey: AVAudioQuality.high.rawValue
        ]
        
        self.audioRecorder = try AVAudioRecorder(url: fileURL, settings: settings)
        self.audioRecorder?.isMeteringEnabled = true
        self.audioRecorder?.record()
        self.isRecording = true
        
        // SDK expects { path: string } from startRecording
        resolve(["path": fileURL.path])
      } catch {
        reject("RECORDING_ERROR", "Failed to start recording: \(error.localizedDescription)", error)
      }
    }
  }
  
  @objc
  func stopRecording(_ resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) {
    DispatchQueue.main.async { [weak self] in
      guard let self = self else {
        reject("ERROR", "Module deallocated", nil)
        return
      }
      
      guard self.isRecording, let recorder = self.audioRecorder, let url = self.recordingURL else {
        reject("NOT_RECORDING", "No active recording", nil)
        return
      }
      
      recorder.stop()
      self.isRecording = false
      
      // Verify the file exists
      if FileManager.default.fileExists(atPath: url.path) {
        // SDK expects { path: string } from stopRecording
        // The AudioDecoder in the SDK will decode this file
        resolve(["path": url.path])
      } else {
        reject("FILE_ERROR", "Recording file not found", nil)
      }
    }
  }
  
  @objc
  func cancelRecording(_ resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) {
    DispatchQueue.main.async { [weak self] in
      guard let self = self else {
        reject("ERROR", "Module deallocated", nil)
        return
      }
      
      if let recorder = self.audioRecorder {
        recorder.stop()
        self.isRecording = false
      }
      
      if let url = self.recordingURL {
        try? FileManager.default.removeItem(at: url)
      }
      
      resolve(["success": true])
    }
  }
  
  @objc
  func getAudioLevel(_ resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) {
    DispatchQueue.main.async { [weak self] in
      guard let self = self, let recorder = self.audioRecorder, self.isRecording else {
        resolve(["level": 0.0])
        return
      }
      
      recorder.updateMeters()
      let averagePower = recorder.averagePower(forChannel: 0)
      // Convert dB to linear scale (0-1)
      let level = pow(10.0, averagePower / 20.0)
      
      resolve(["level": min(1.0, max(0.0, Double(level)))])
    }
  }
  
  // MARK: - Playback
  
  @objc
  func playAudio(_ uri: String, resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) {
    DispatchQueue.main.async { [weak self] in
      guard let self = self else {
        reject("ERROR", "Module deallocated", nil)
        return
      }
      
      do {
        // Configure audio session for playback
        let session = AVAudioSession.sharedInstance()
        try session.setCategory(.playback, mode: .default)
        try session.setActive(true)
        
        // Handle file:// URLs and plain paths
        let url: URL
        if uri.hasPrefix("file://") {
          url = URL(fileURLWithPath: String(uri.dropFirst(7)))
        } else if uri.hasPrefix("/") {
          url = URL(fileURLWithPath: uri)
        } else if let parsed = URL(string: uri) {
          url = parsed
        } else {
          reject("INVALID_URI", "Invalid audio URI: \(uri)", nil)
          return
        }
        
        self.audioPlayer = try AVAudioPlayer(contentsOf: url)
        self.audioPlayer?.prepareToPlay()
        self.audioPlayer?.play()
        self.isPlaying = true
        
        resolve([
          "success": true,
          "duration": self.audioPlayer?.duration ?? 0
        ])
      } catch {
        reject("PLAYBACK_ERROR", "Failed to play audio: \(error.localizedDescription)", error)
      }
    }
  }
  
  @objc
  func stopPlayback(_ resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) {
    DispatchQueue.main.async { [weak self] in
      self?.audioPlayer?.stop()
      self?.isPlaying = false
      resolve(["success": true])
    }
  }
  
  @objc
  func pausePlayback(_ resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) {
    DispatchQueue.main.async { [weak self] in
      self?.audioPlayer?.pause()
      self?.isPlaying = false
      resolve(["success": true])
    }
  }
  
  @objc
  func resumePlayback(_ resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) {
    DispatchQueue.main.async { [weak self] in
      self?.audioPlayer?.play()
      self?.isPlaying = true
      resolve(["success": true])
    }
  }
  
  @objc
  func getPlaybackStatus(_ resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) {
    DispatchQueue.main.async { [weak self] in
      guard let self = self else {
        resolve(["isPlaying": false, "currentTime": 0, "duration": 0])
        return
      }
      
      resolve([
        "isPlaying": self.audioPlayer?.isPlaying ?? false,
        "currentTime": self.audioPlayer?.currentTime ?? 0,
        "duration": self.audioPlayer?.duration ?? 0
      ])
    }
  }
  
  // MARK: - Module Setup
  
  @objc
  static func requiresMainQueueSetup() -> Bool {
    return true
  }
}
