import Foundation
import Capacitor

/**
 * Please read the Capacitor iOS Plugin Development Guide
 * here: https://capacitorjs.com/docs/plugins/ios
 */
@objc(CompassAccuracyPlugin)
public class CompassAccuracyPlugin: CAPPlugin {
    
    @objc func startMonitoring(_ call: CAPPluginCall) {
        call.reject("Compass accuracy monitoring is not needed on iOS. The device compass is automatically calibrated by the OS using the motion coprocessor.")
    }
    
    @objc func stopMonitoring(_ call: CAPPluginCall) {
        call.reject("Compass accuracy monitoring is not available on iOS")
    }
    
    @objc func getCurrentAccuracy(_ call: CAPPluginCall) {
        call.reject("Compass accuracy is not available on iOS")
    }
    
    @objc func simulateAccuracyChange(_ call: CAPPluginCall) {
        call.reject("Simulating accuracy changes is not available on iOS")
    }
}
