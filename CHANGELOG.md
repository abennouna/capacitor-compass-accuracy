# Changelog

All notable changes to this project will be documented in this file.

## [1.0.0] - 2024-01-30

### Added
- Initial release of capacitor-compass-accuracy plugin
- Ported from cordova-plugin-compass-accuracy to Capacitor
- Full Android support with native calibration dialog
- TypeScript definitions for all APIs
- iOS stub implementation (not needed - iOS auto-calibrates compass)
- Web implementation stub (not supported on web)

### Features
- `startMonitoring()` - Monitor compass accuracy with callback
- `stopMonitoring()` - Stop monitoring
- `getCurrentAccuracy()` - Get current accuracy without monitoring
- `simulateAccuracyChange()` - Simulate accuracy changes for testing
- Native Android dialog with visual calibration guide
- Real-time accuracy display in calibration dialog
- Color-coded accuracy levels (green/orange/red/gray)
- Support for HIGH, MEDIUM, LOW accuracy requirements

### Migration from Cordova
This plugin maintains API compatibility with the original Cordova plugin. Key differences:
- Uses Capacitor's plugin architecture
- Uses Promise-based APIs while maintaining callback support for monitoring
- Simplified installation process
- TypeScript type definitions included

### Platform Support
- ✅ Android (full support)
- ⚠️ iOS (not needed - auto-calibrated)
- ⚠️ Web (not supported)
