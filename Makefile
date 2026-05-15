.PHONY: prepare_release ios_dev ios_dev_sim

VERSION := $(filter-out prepare_release,$(MAKECMDGOALS))

prepare_release:
	@if [ -z "$(VERSION)" ]; then \
		echo "Usage: make prepare_release X.Y.Z"; \
		exit 1; \
	fi
	sed -i '' 's/^VERSION_NAME=.*/VERSION_NAME=$(VERSION)/' gradle.properties
	git commit -S -m 'v$(VERSION)' -- gradle.properties
	git tag -a v$(VERSION) -s -m 'v$(VERSION)'

# Build, install, and launch composeApp on a connected iOS device.
# Auto-detects the first connected device; fails fast before build if none is found.
ios_dev: DEVICE_ID = $(shell xcrun devicectl list devices 2>/dev/null | grep -i connected | grep -oE '[0-9A-F]{8}-[0-9A-F]{16}|[0-9A-F]{8}-[0-9A-F]{4}-[0-9A-F]{4}-[0-9A-F]{4}-[0-9A-F]{12}' | head -n1)
ios_dev:
	@if [ -z "$(DEVICE_ID)" ]; then \
		echo "No connected iOS device found."; \
		echo "  - Plug in the device via USB and tap 'Trust This Computer' if prompted."; \
		echo "  - 'xcrun devicectl list devices' should show State 'connected' (not just 'paired')."; \
		echo "  - First-time signing setup: Xcode > Settings > Accounts must list your Apple ID with the team visible,"; \
		echo "    and iosApp/Configuration/Config.local.xcconfig must set TEAM_ID."; \
		exit 1; \
	fi
	@echo "Targeting device: $(DEVICE_ID)"
	xcodebuild \
		-project iosApp/iosApp.xcodeproj \
		-scheme iosApp \
		-configuration Debug \
		-destination 'generic/platform=iOS' \
		-allowProvisioningUpdates \
		-derivedDataPath iosApp/build \
		build
	@APP="$$(ls -d iosApp/build/Build/Products/Debug-iphoneos/*.app | head -n1)"; \
	BUNDLE_ID="$$(/usr/libexec/PlistBuddy -c 'Print :CFBundleIdentifier' "$$APP/Info.plist")"; \
	xcrun devicectl device install app --device "$(DEVICE_ID)" "$$APP"; \
	xcrun devicectl device process launch --device "$(DEVICE_ID)" "$$BUNDLE_ID"

# Build, install, and launch composeApp on the booted iOS simulator.
# Fails fast before build if no simulator is booted.
ios_dev_sim:
	@if ! xcrun simctl list devices booted 2>/dev/null | grep -q Booted; then \
		echo "No booted simulator found. Boot one in Simulator.app and retry."; \
		exit 1; \
	fi
	xcodebuild \
		-project iosApp/iosApp.xcodeproj \
		-scheme iosApp \
		-configuration Debug \
		-destination 'generic/platform=iOS Simulator' \
		-derivedDataPath iosApp/build \
		build
	@APP="$$(ls -d iosApp/build/Build/Products/Debug-iphonesimulator/*.app | head -n1)"; \
	BUNDLE_ID="$$(/usr/libexec/PlistBuddy -c 'Print :CFBundleIdentifier' "$$APP/Info.plist")"; \
	xcrun simctl install booted "$$APP"; \
	xcrun simctl launch booted "$$BUNDLE_ID"

%:
	@:
