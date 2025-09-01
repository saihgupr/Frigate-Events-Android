# 📱 TVReader Android - Installation Guide

## 🚀 Quick Start

### **Option 1: Direct APK Installation (Easiest)**

1. **Download the APK**
   - Download `TVReader-v1.0.apk` from the release

2. **Enable Unknown Sources**
   - Go to **Settings** → **Security** → **Unknown Sources**
   - Enable "Install from Unknown Sources"
   - **Note**: On newer Android versions, you may need to enable this per-app

3. **Install the App**
   - Open the downloaded APK file
   - Tap **Install** and follow the prompts
   - Wait for installation to complete

4. **Launch TVReader**
   - Find TVReader in your app drawer
   - Tap to open and start reading!

### **Option 2: ADB Installation (Advanced)**

1. **Connect your device via USB**
2. **Enable USB Debugging** in Developer Options
3. **Run the command:**
   ```bash
   adb install TVReader-v1.0.apk
   ```

## 📺 **Android TV Installation**

### **Method 1: File Manager Installation**

1. **Transfer APK to TV**
   - Use USB drive, network transfer, or cloud storage
   - Place APK in an accessible location (Downloads folder recommended)
   
2. **Install via File Manager**
   - Open File Manager on your Android TV
   - Navigate to the APK location
   - Select the APK file and choose "Install"
   - Follow the installation prompts

### **Method 2: ADB over Network**

1. **Enable ADB on your TV**
   - Go to **Settings** → **Developer Options**
   - Enable "USB Debugging" and "Network Debugging"

2. **Find your TV's IP address**
   - Go to **Settings** → **Network** → **Wi-Fi** → **Advanced**
   - Note the IP address

3. **Connect and install**
   ```bash
   adb connect YOUR_TV_IP:5555
   adb install TVReader-v1.0.apk
   ```

### **Method 3: Sideload via USB**

1. **Transfer APK to USB drive**
2. **Insert USB drive into Android TV**
3. **Use File Manager to install from USB**

## 📱 **Mobile Device Installation**

### **Android Phones & Tablets**

1. **Download APK** to your device
2. **Enable Unknown Sources** in Security settings
3. **Open APK file** and install
4. **Launch from app drawer**

### **Chrome OS / Chromebooks**

1. **Enable Developer Mode** (if needed)
2. **Download APK** to Downloads folder
3. **Install via Android subsystem**
4. **Launch from app launcher**

## ⚠️ **Important Notes**

### **Security Considerations**
- **Trusted Source**: Only install APKs from trusted sources
- **Permissions**: Grant necessary permissions when prompted
- **Updates**: Manual updates required for this version

### **System Requirements**
- **Android Version**: 7.0 (API 24) or higher
- **Storage**: At least 50MB free space
- **Memory**: 2GB RAM recommended
- **Network**: Internet connection for web imports

## 🆘 **Troubleshooting**

### **Common Installation Issues**

#### **"Installation Blocked" Error**
- **Solution**: Check "Unknown Sources" setting
- **Alternative**: Use ADB installation method

#### **"App Not Compatible" Error**
- **Solution**: Verify Android version compatibility
- **Check**: Minimum Android 7.0 required

#### **"Storage Full" Error**
- **Solution**: Free up device storage
- **Check**: Need at least 50MB free space

#### **"Parse Error" or "Corrupted APK"**
- **Solution**: Re-download the APK file
- **Check**: Verify file integrity and size

### **Performance Issues**

#### **App Runs Slowly**
- **Solution**: Close other background apps
- **Check**: Ensure sufficient RAM available

#### **Books Take Long to Load**
- **Solution**: Check EPUB file size and complexity
- **Tip**: Large books may take longer initially

#### **Import Failures**
- **Solution**: Check network connection
- **Verify**: File permissions and storage access

## 🔧 **Post-Installation Setup**

### **First Launch Configuration**

1. **Grant Permissions**
   - Storage access for EPUB files
   - Network access for web imports
   - Camera access (if scanning QR codes)

2. **Import Your First Book**
   - Use the import button
   - Choose from local storage or network
   - Select an EPUB file to begin

3. **Customize Settings**
   - Adjust text size and font
   - Choose reading theme
   - Set reading preferences

### **Recommended Settings**

- **Text Size**: Medium to Large for TV viewing
- **Theme**: Dark theme for low-light reading
- **Auto-save**: Enable automatic progress saving
- **Network**: Enable for web imports

## 📞 **Support**

### **Getting Help**

1. **Check Documentation**: Review release notes and README
2. **GitHub Issues**: Report bugs and request features
3. **Community**: Join discussions in the project repository

### **Useful Commands**

```bash
# Check if app is installed
adb shell pm list packages | grep tvreader

# Launch app
adb shell am start -n com.pizzaman.tvreader/.MainActivity

# Clear app data (if needed)
adb shell pm clear com.pizzaman.tvreader

# Uninstall app
adb uninstall com.pizzaman.tvreader
```

---

**Version**: 1.0  
**Last Updated**: August 30, 2025  
**Package**: com.pizzaman.tvreader  
**Size**: ~16.8 MB
