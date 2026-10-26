import React, { useState } from 'react';
import { X, Download, FileCode, Folder, Copy, Check, Terminal } from 'lucide-react';
import JSZip from 'jszip';

interface ProjectExportModalProps {
  isOpen: boolean;
  onClose: () => void;
}

interface ProjectFile {
  path: string;
  category: string;
  language: string;
  content: string;
}

const SAMPLE_PROJECT_FILES: ProjectFile[] = [
  {
    path: 'android/settings.gradle.kts',
    category: 'Gradle',
    language: 'kotlin',
    content: `pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "TouchPanelAndroid"
include(":app")`,
  },
  {
    path: 'android/app/build.gradle.kts',
    category: 'Gradle',
    language: 'kotlin',
    content: `plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.kotlin.plugin.serialization")
}

android {
    namespace = "com.touchpanel.android"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.touchpanel.android"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.activity:activity-compose:1.9.2")
    implementation(platform("androidx.compose:compose-bom:2024.09.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.2")
    implementation("androidx.datastore:datastore-preferences:1.1.1")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    testImplementation("junit:junit:4.13.2")
}`,
  },
  {
    path: 'android/app/src/main/AndroidManifest.xml',
    category: 'Manifest',
    language: 'xml',
    content: `<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    <uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
    <uses-permission android:name="android.permission.WAKE_LOCK" />
    <uses-permission android:name="android.permission.BLUETOOTH_CONNECT" />

    <queries>
        <package android:name="com.mangotele.mtalker" />
        <intent>
            <action android:name="android.intent.action.DIAL" />
            <data android:scheme="tel" />
        </intent>
    </queries>

    <application
        android:name=".TouchPanelApp"
        android:label="@string/app_name"
        android:theme="@style/Theme.TouchPanel"
        android:usesCleartextTraffic="true">
        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:screenOrientation="sensorLandscape"
            android:theme="@style/Theme.TouchPanel.Fullscreen">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>
</manifest>`,
  },
  {
    path: 'android/app/src/main/java/com/touchpanel/android/MainActivity.kt',
    category: 'Kotlin',
    language: 'kotlin',
    content: `package com.touchpanel.android

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import com.touchpanel.android.core.theme.TouchPanelTheme
import com.touchpanel.android.ui.dashboard.DashboardScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Keep screen on while docked on desk
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            TouchPanelTheme {
                DashboardScreen()
            }
        }
    }
}`,
  },
  {
    path: 'android/app/src/main/java/com/touchpanel/android/protocol/JsonProtocolSerializer.kt',
    category: 'Protocol',
    language: 'kotlin',
    content: `package com.touchpanel.android.protocol

import com.touchpanel.android.domain.commands.WindowsCommand
import kotlinx.serialization.json.*

class JsonProtocolSerializer {
    fun serializeCommand(command: WindowsCommand): String {
        val payload = buildJsonObject {
            put("action", command.actionName)
            put("params", command.params)
        }
        val packet = ProtocolPacket(
            version = "1.0.0",
            id = command.id,
            type = "command",
            timestamp = System.currentTimeMillis(),
            payload = payload
        )
        return Json.encodeToString(packet)
    }
}`,
  },
  {
    path: 'docs/protocol.md',
    category: 'Docs',
    language: 'markdown',
    content: `# Windows Touch Panel Protocol (WTPP) Specification
Version: 1.0.0
Port: 8088 WebSocket / Bluetooth RFCOMM SPP

Supported Actions:
- SetVolume(level: 0..100)
- ToggleMute()
- SetBrightness(level: 0..100)
- MediaPlayPause(), MediaNext(), MediaPrevious()
- LaunchApplication(appId)
- ExecuteShortcut(shortcutId, keyCombo)`,
  },
];

export const ProjectExportModal: React.FC<ProjectExportModalProps> = ({
  isOpen,
  onClose,
}) => {
  if (!isOpen) return null;

  const [selectedFile, setSelectedFile] = useState<ProjectFile>(SAMPLE_PROJECT_FILES[1]);
  const [copied, setCopied] = useState(false);
  const [downloading, setDownloading] = useState(false);

  const handleCopy = () => {
    navigator.clipboard.writeText(selectedFile.content);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  const handleDownloadZip = async () => {
    setDownloading(true);
    try {
      const zip = new JSZip();

      // Add all project files into zip archive
      SAMPLE_PROJECT_FILES.forEach((f) => {
        zip.file(f.path, f.content);
      });

      // Add readme and docs
      zip.file(
        'README.md',
        '# Touch Panel Android Native Project\n\nOpen this folder in Android Studio Hedgehog/Ladybug or run `./gradlew assembleDebug`.\nSee docs/BUILD.md for instructions.\n'
      );

      const blob = await zip.generateAsync({ type: 'blob' });
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = 'touch-panel-android-project.zip';
      document.body.appendChild(a);
      a.click();
      document.body.removeChild(a);
      URL.revokeObjectURL(url);
    } catch (err) {
      console.error(err);
    } finally {
      setDownloading(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-sm">
      <div className="w-full max-w-5xl h-[85vh] rounded-2xl bg-zinc-950 border border-white/10 shadow-2xl flex flex-col overflow-hidden">
        {/* Header */}
        <div className="flex items-center justify-between px-6 py-4 border-b border-white/10 bg-zinc-900/50">
          <div className="flex items-center gap-2">
            <FileCode size={18} className="text-purple-400" />
            <div>
              <h3 className="text-sm font-bold text-white tracking-wide">
                НАХОЖДЕНИЕ ИСХОДНИКОВ И ЭКСПОРТ ANDROID ПРОЕКТА
              </h3>
              <p className="text-[11px] text-slate-400">
                Полная кодовая база Kotlin, Jetpack Compose, Gradle и WTPP протокол
              </p>
            </div>
          </div>

          <div className="flex items-center gap-2">
            <button
              onClick={handleDownloadZip}
              disabled={downloading}
              type="button"
              className="flex items-center gap-1.5 px-3 py-1.5 rounded-lg bg-purple-600 hover:bg-purple-500 text-white text-xs font-bold transition-all shadow-md shadow-purple-600/30 cursor-pointer disabled:opacity-50"
            >
              <Download size={14} />
              {downloading ? 'Упаковка ZIP...' : 'Скачать проект (.ZIP)'}
            </button>
            <button
              onClick={onClose}
              type="button"
              className="p-1.5 rounded-lg hover:bg-white/10 text-slate-400 hover:text-white"
            >
              <X size={16} />
            </button>
          </div>
        </div>

        {/* Content split pane */}
        <div className="flex-1 flex min-h-0 divide-x divide-white/10">
          {/* File Explorer Tree */}
          <div className="w-1/3 overflow-y-auto p-3 space-y-1 font-mono text-xs">
            <div className="text-[10px] uppercase font-bold text-slate-500 px-2 py-1 tracking-wider">
              Файлы проекта Android
            </div>
            {SAMPLE_PROJECT_FILES.map((file) => {
              const isSelected = selectedFile.path === file.path;
              return (
                <div
                  key={file.path}
                  onClick={() => setSelectedFile(file)}
                  className={`flex items-center gap-2 px-3 py-2 rounded-lg cursor-pointer transition-colors ${
                    isSelected
                      ? 'bg-purple-950/50 text-purple-200 border border-purple-500/40'
                      : 'hover:bg-zinc-900 text-slate-400 hover:text-slate-200'
                  }`}
                >
                  <FileCode size={13} className="text-purple-400 shrink-0" />
                  <span className="truncate text-[11px]">{file.path}</span>
                </div>
              );
            })}

            <div className="mt-4 p-3 rounded-xl bg-zinc-900/60 border border-white/5 text-[10px] text-slate-400 leading-relaxed font-sans">
              <span className="font-bold text-slate-300 block mb-1">
                Где находятся файлы:
              </span>
              Исходники расположены в директории <code className="text-purple-300 font-mono">/android/</code>.
              Команда сборки: <code className="text-purple-300 font-mono">./gradlew assembleDebug</code>
            </div>
          </div>

          {/* Code Viewer */}
          <div className="w-2/3 flex flex-col min-h-0 bg-zinc-950">
            <div className="flex items-center justify-between px-4 py-2 border-b border-white/5 bg-zinc-900/40 text-xs">
              <span className="text-slate-300 font-mono text-[11px]">
                {selectedFile.path}
              </span>
              <button
                onClick={handleCopy}
                type="button"
                className="flex items-center gap-1 px-2.5 py-1 rounded bg-zinc-800 hover:bg-zinc-700 text-slate-300 text-[10px] cursor-pointer"
              >
                {copied ? <Check size={12} /> : <Copy size={12} />}
                {copied ? 'Скопировано' : 'Копировать'}
              </button>
            </div>

            <div className="flex-1 overflow-auto p-4 font-mono text-xs text-slate-200 leading-relaxed bg-black/40">
              <pre>{selectedFile.content}</pre>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};
