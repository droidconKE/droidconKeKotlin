//
//  ContentView.swift
//  droidconKe
//
//  Created by Evans Chepsiror on 11/06/2025.
//

import SwiftUI
import shared

struct ContentView: View {
    

    var body: some View {
        ComposeView()
                .ignoresSafeArea(.keyboard) // Compose has own keyboard handler
    }
}

struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {

        MainViewControllerKt.mainViewController()

    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {
    }
}

#Preview {
    ContentView()
}
