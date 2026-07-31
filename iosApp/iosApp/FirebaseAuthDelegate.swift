import Foundation
import FirebaseCore
import FirebaseAuth
import GoogleSignIn
import ComposeApp

/// Swift implementation of IosAuthDelegate that handles Firebase Auth + Google Sign-In.
/// This class is registered in IosAuthBridge at app startup so Kotlin can call it.
class FirebaseAuthDelegate: IosAuthDelegate {

    func signInWithGoogle(completion: @escaping (String?, String?, String?, String?, String?, String?) -> Void) {
        guard let clientID = FirebaseApp.app()?.options.clientID else {
            completion(nil, nil, nil, nil, nil, "Firebase client ID not found")
            return
        }

        let config = GIDConfiguration(clientID: clientID)
        GIDSignIn.sharedInstance.configuration = config

        guard let windowScene = UIApplication.shared.connectedScenes.first as? UIWindowScene,
              let rootViewController = windowScene.windows.first?.rootViewController else {
            completion(nil, nil, nil, nil, nil, "No root view controller found")
            return
        }

        GIDSignIn.sharedInstance.signIn(withPresenting: rootViewController) { result, error in
            if let error = error {
                completion(nil, nil, nil, nil, nil, error.localizedDescription)
                return
            }

            guard let user = result?.user,
                  let idToken = user.idToken?.tokenString else {
                completion(nil, nil, nil, nil, nil, "Google Sign-In failed: missing token")
                return
            }

            let credential = GoogleAuthProvider.credential(
                withIDToken: idToken,
                accessToken: user.accessToken.tokenString
            )

            Auth.auth().signIn(with: credential) { authResult, error in
                if let error = error {
                    completion(nil, nil, nil, nil, nil, "Firebase auth failed: \(error.localizedDescription)")
                    return
                }

                guard let firebaseUser = authResult?.user else {
                    completion(nil, nil, nil, nil, nil, "Firebase auth returned no user")
                    return
                }

                firebaseUser.getIDToken { firebaseToken, error in
                    if let error = error {
                        completion(nil, nil, nil, nil, nil, "Failed to get Firebase token: \(error.localizedDescription)")
                        return
                    }

                    completion(
                        firebaseToken,
                        firebaseUser.uid,
                        firebaseUser.email,
                        firebaseUser.displayName,
                        firebaseUser.photoURL?.absoluteString,
                        nil
                    )
                }
            }
        }
    }

    func getFirebaseIdToken(completion: @escaping (String?) -> Void) {
        guard let currentUser = Auth.auth().currentUser else {
            completion(nil)
            return
        }

        currentUser.getIDToken { token, error in
            completion(token)
        }
    }

    func signOut() {
        try? Auth.auth().signOut()
        GIDSignIn.sharedInstance.signOut()
    }
}
