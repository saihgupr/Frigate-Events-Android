# Frigate Events Android v1.2 Release Notes

## 🚀 New Features

### Silent Refresh on App Activation
- **Automatic refresh**: App now automatically refreshes events when you return to it from another app
- **Cross-platform support**: Works on both Android TV and mobile devices
- **Invisible updates**: No loading spinner or UI interruption - updates happen silently in the background
- **Smart detection**: Uses dual lifecycle detection (ProcessLifecycle for TV, Activity lifecycle for mobile)

## 🎨 UI Improvements

### Header Consistency
- **Unified styling**: Main page and settings page headers now have identical styling and dimensions
- **Consistent colors**: Both headers use the same white text color and background
- **Fixed width**: Settings header now has the same width as the main page header
- **Clean design**: Removed temporary debugging elements for a polished look

## 🔧 Technical Improvements

### Lifecycle Management
- Added lifecycle dependencies for better app state management
- Implemented dual lifecycle observers for maximum compatibility
- Enhanced logging for debugging and monitoring

### Code Quality
- Improved error handling and logging
- Better separation of concerns in lifecycle management
- Enhanced debugging capabilities

## 📱 Device Compatibility

- **Android TV**: Full support with ProcessLifecycle detection
- **Mobile/Tablet**: Full support with Activity lifecycle detection
- **Cross-platform**: Seamless experience across all Android devices

## 🐛 Bug Fixes

- Fixed header width inconsistency between main page and settings
- Resolved styling differences between pages
- Improved app activation detection reliability

## 📋 Installation

1. Download `FrigateEvents-v1.2.apk`
2. Enable "Install from unknown sources" in Android settings
3. Install the APK file
4. Configure your Frigate server URL in Settings

## 🔄 Upgrade Notes

- This is a direct upgrade from v1.1
- All existing settings and configurations will be preserved
- No data migration required

## 📞 Support

For issues or questions, please check the GitHub repository or create an issue.

---

**Version**: 1.2  
**Build Date**: September 1, 2025  
**Minimum Android Version**: 7.0 (API 24)  
**Target Android Version**: 14 (API 34)
