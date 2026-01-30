// swift-tools-version: 5.9
import PackageDescription

let package = Package(
    name: "AbennounaCapacitorCompassAccuracy",
    platforms: [.iOS(.v15)],
    products: [
        .library(
            name: "AbennounaCapacitorCompassAccuracy",
            targets: ["CompassAccuracyPlugin"])
    ],
    dependencies: [
        .package(url: "https://github.com/ionic-team/capacitor-swift-pm.git", from: "8.0.0")
    ],
    targets: [
        .target(
            name: "CompassAccuracyPlugin",
            dependencies: [
                .product(name: "Capacitor", package: "capacitor-swift-pm"),
                .product(name: "Cordova", package: "capacitor-swift-pm")
            ],
            path: "ios/Sources/CompassAccuracyPlugin"),
        .testTarget(
            name: "CompassAccuracyPluginTests",
            dependencies: ["CompassAccuracyPlugin"],
            path: "ios/Tests/CompassAccuracyPluginTests")
    ]
)