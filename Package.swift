// swift-tools-version:5.3
import PackageDescription

// BEGIN KMMBRIDGE VARIABLES BLOCK (do not edit)
let remoteKotlinUrl = "https://<some repo>/artifactory/mvn-mobile-releases//org/dbtools/kmp/kmp-commons/1.0.0-alpha01/kmp-commons-kmmbridge-1.0.0-alpha01.zip"
let remoteKotlinChecksum = "6cbce1c64bf67c6078ce35c89920196914b978e971668ecf38831a54a5537406"
let packageName = "KmpCommons"
// END KMMBRIDGE BLOCK

let package = Package(
    name: packageName,
    platforms: [
        .iOS(.v13)
    ],
    products: [
        .library(
            name: packageName,
            targets: [packageName]
        ),
    ],
    targets: [
        .binaryTarget(
            name: packageName,
            url: remoteKotlinUrl,
            checksum: remoteKotlinChecksum
        )
        ,
    ]
)
