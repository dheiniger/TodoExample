This is a new [**React Native**](https://reactnative.dev) project, bootstrapped using [`@react-native-community/cli`](https://github.com/react-native-community/cli).

Built with [Clojurescript](https://clojurescript.org/) and [Krell](https://github.com/vouch-opensource/krell)

# From the Krell documentation 

## Install dependencies
```
clj -M -m cljs.main --install-deps
```

> 

## Start the REPL
### Android Studio (Intellij/Cursive)
Set up a configuration for a local repl with clojure.main.  Use these parameters
```
-m krell.main -co build.edn -c -r
```

## Run the app
After setting up an emulator, or real device, run this command
```
npx react-native run-android
```
You may need to right click on your deps.edn file to set it as a clojure project before this will work.

### Status
This app is incomplete and only meant as a reference.  It demonstrates a minimal Clojurescript setup with some basic functionality including using a 3rd party library and setting up local storage.
