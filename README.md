# capacitor-compass-accuracy

A Capacitor plugin for Android to monitor the accuracy of the device compass and if needed, request the user to calibrate it via a native dialog.

This is a port of the [cordova-plugin-compass-accuracy](https://github.com/dpa99c/cordova-plugin-compass-accuracy) plugin to Capacitor.

## Platform Support

- ✅ **Android**: Full support with native calibration dialog
- ⚠️ **iOS**: Not needed - iOS automatically calibrates the compass using the motion coprocessor since iPhone 5S and iOS 13
- ⚠️ **Web**: Not supported - compass accuracy monitoring is not available on web

## Installation

```bash
npm install capacitor-compass-accuracy
npx cap sync
```

## API

### Interfaces

#### `AccuracyLevel`

Enum indicating the required or current accuracy of the device compass:

- `HIGH` - High accuracy (less than 5 degrees of error)
- `MEDIUM` - Medium accuracy (less than 10 degrees of error)
- `LOW` - Low accuracy (less than 15 degrees of error)
- `UNRELIABLE` - Unreliable accuracy (more than 15 degrees of error)
- `UNKNOWN` - Unknown accuracy value

#### `ResultType`

Enum indicating the type of result being returned:

- `STARTED` - Monitor has been started and the current accuracy is being returned
- `ACCURACY_CHANGED` - Accuracy has changed and the new accuracy is being returned

#### `AccuracyChangeResult`

```typescript
interface AccuracyChangeResult {
  type: ResultType;
  currentAccuracy: AccuracyLevel;
  requiredAccuracy: AccuracyLevel;
  previousAccuracy?: AccuracyLevel;
}
```

### Methods

#### `startMonitoring(options, callback)`

Starts monitoring the accuracy of the device compass for the required accuracy level.

- If the initial or subsequent accuracy is less than the required accuracy, the plugin will display a native dialog requesting calibration
- The dialog will be displayed once per app session
- The callback will be invoked when monitoring starts and whenever accuracy changes

```typescript
import { CompassAccuracy, AccuracyLevel } from 'capacitor-compass-accuracy';

const callbackId = await CompassAccuracy.startMonitoring(
  { requiredAccuracy: AccuracyLevel.HIGH },
  (result, error) => {
    if (error) {
      console.error('Error:', error);
      return;
    }
    
    if (result) {
      console.log('Type:', result.type);
      console.log('Current Accuracy:', result.currentAccuracy);
      console.log('Required Accuracy:', result.requiredAccuracy);
    }
  }
);
```

**Parameters:**
- `options`: `StartMonitoringOptions` - Configuration options
  - `requiredAccuracy`: `AccuracyLevel` (optional) - Required accuracy level (defaults to HIGH)
- `callback`: `(result, error) => void` - Callback function that receives accuracy updates

**Returns:** `Promise<string>` - Callback ID for the monitoring session

#### `stopMonitoring()`

Stops monitoring the accuracy of the device compass.

```typescript
await CompassAccuracy.stopMonitoring();
```

**Returns:** `Promise<void>`

#### `getCurrentAccuracy()`

Gets the current accuracy of the device compass.

```typescript
const result = await CompassAccuracy.getCurrentAccuracy();
console.log('Current Accuracy:', result.currentAccuracy);
```

**Returns:** `Promise<{ currentAccuracy: AccuracyLevel }>`

#### `simulateAccuracyChange(options)`

Simulates a change in compass accuracy. This method is intended for testing only.

```typescript
await CompassAccuracy.simulateAccuracyChange({ 
  accuracy: AccuracyLevel.LOW 
});
```

**Parameters:**
- `options`: `{ accuracy: AccuracyLevel }` - The simulated accuracy level

**Returns:** `Promise<void>`

## Usage Example

```typescript
import { CompassAccuracy, AccuracyLevel, ResultType } from 'capacitor-compass-accuracy';

// Start monitoring with high accuracy requirement
const startMonitoring = async () => {
  try {
    const callbackId = await CompassAccuracy.startMonitoring(
      { requiredAccuracy: AccuracyLevel.HIGH },
      (result, error) => {
        if (error) {
          console.error('Monitoring error:', error);
          return;
        }

        if (result) {
          const action = result.type === ResultType.STARTED ? 'started as' : 'changed to';
          console.log(`Compass accuracy ${action}: ${result.currentAccuracy}`);
          console.log(`Required accuracy: ${result.requiredAccuracy}`);
          
          // The native dialog will be shown automatically if accuracy is insufficient
        }
      }
    );
    
    console.log('Monitoring started with callback ID:', callbackId);
  } catch (error) {
    console.error('Failed to start monitoring:', error);
  }
};

// Stop monitoring
const stopMonitoring = async () => {
  try {
    await CompassAccuracy.stopMonitoring();
    console.log('Monitoring stopped');
  } catch (error) {
    console.error('Failed to stop monitoring:', error);
  }
};

// Get current accuracy without starting monitoring
const checkAccuracy = async () => {
  try {
    const result = await CompassAccuracy.getCurrentAccuracy();
    console.log('Current compass accuracy:', result.currentAccuracy);
  } catch (error) {
    console.error('Failed to get accuracy:', error);
  }
};
```

## Calibration Dialog

When the compass accuracy falls below the required level, the plugin displays a native Android dialog:

- The dialog shows a visual guide for calibrating the compass (figure-of-eight motion)
- The dialog displays the current accuracy level with color coding (green/orange/red/gray)
- The dialog is modal and can only be dismissed by tapping the "DONE" button
- The dialog is shown only once per app session

## How It Works

The plugin monitors the Android magnetometer sensor's accuracy events. When the accuracy falls below the required level:

1. A native calibration dialog is displayed (once per session)
2. The user is instructed to move their phone in a figure-of-eight motion
3. The accuracy status is displayed in real-time as the user calibrates
4. Once accuracy improves above the required level, the dialog is automatically dismissed

## Differences from Cordova Plugin

This Capacitor plugin maintains API compatibility with the original Cordova plugin, with these differences:

- Uses Capacitor's plugin architecture instead of Cordova's
- Written in TypeScript for better type safety
- Uses modern Promise-based APIs while maintaining callback support for monitoring
- Simplified installation process (no plugin.xml)

## License

MIT License

Copyright (c) 2023 Dave Alden (Working Edge Ltd.)

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
