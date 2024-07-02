//
//  AppDelegate.swift
//  iosApp
//
//  Created by Arkadii Ivanov on 11/05/2023.
//  Copyright © 2023 orgName. All rights reserved.
//

import SwiftUI
import ConfettiKit

class AppDelegate : NSObject, UIApplicationDelegate {
    
    let root: AppComponent
    
    override init() {
        KoiniOSKt.doInitKoinIOS()

        root = DefaultAppComponent(
            componentContext: DefaultComponentContext(lifecycle: ApplicationLifecycle()),
            onSignOut: {},
            onSignIn: {},
            isMultiPane: UIDevice.current.userInterfaceIdiom != UIUserInterfaceIdiom.phone,
            initialConferenceId: nil
        )
    }
}
