# Frigate Events

A modern Android app for viewing Frigate security camera events. Works on phones, tablets, and Android TV. Also [available](https://github.com/saihgupr/Frigate-Events) for iOS/macOS.

## Features

- **Event Viewing**: Browse all Frigate events with thumbnails
- **In-Progress Events**: See ongoing recordings with red "In Progress" labels
- **Real-time Updates**: Automatic polling for new events
- **Video Playback**: Watch recorded clips directly in the app
- **TV Optimized**: Full D-pad support for Android TV
- **Dark Theme**: Easy on the eyes for extended viewing

## Screenshots

### 📱 Mobile
<div align="center">
<img src="https://i.imgur.com/WjRZ12u.png" width="30%" alt="Mobile Main Screen" />
<img src="https://i.imgur.com/EPssZwA.png" width="30%" alt="Video Player" />
<img src="https://i.imgur.com/5bfwxYv.png" width="30%" alt="Mobile Settings" />
</div>

### 📺 Android TV
<div align="center">
<img src="https://i.imgur.com/aKwq3zJ.png" width="45%" alt="TV Main Screen" />
<img src="https://i.imgur.com/rkkk4t5.png" width="45%" alt="TV Settings" />
<img src="https://i.imgur.com/FRff6Nv.png" width="45%" alt="In Progress" />
<img src="https://i.imgur.com/Rfoh02g.jpeg" width="45%" alt="Video Player" />
</div>

## Installation

### Quick Install
1. Download APK from [releases](https://github.com/saihgupr/Frigate-Events-Android/releases)
2. Transfer to your Android device
3. Enable "Install from Unknown Sources"
4. Install and launch

### ADB Install
```bash
adb install FrigateEvents-v1.1.apk
adb shell am start -n com.pizzaman.frigateevents/com.pizzaman.frigateevents.MainActivity
```

## Setup

1. Open the app
2. Go to Settings (gear icon)
3. Enter your Frigate server URL (default: `http://192.168.1.168:5000`)
4. Save and enjoy!

## Requirements

- Android 7.0+ (API 24)
- Frigate NVR server running on your network
- Network access to your Frigate server

## Usage

- **Mobile**: Tap and swipe to navigate
- **TV**: Use D-pad on remote control

---

**Built for the Frigate community** - works on phones, tablets, and Android TV!
