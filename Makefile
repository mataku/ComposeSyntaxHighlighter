.PHONY: prepare_release

VERSION := $(filter-out prepare_release,$(MAKECMDGOALS))

prepare_release:
	@if [ -z "$(VERSION)" ]; then \
		echo "Usage: make prepare_release X.Y.Z"; \
		exit 1; \
	fi
	sed -i '' 's/^VERSION_NAME=.*/VERSION_NAME=$(VERSION)/' gradle.properties
	git commit -S -m 'v$(VERSION)' -- gradle.properties
	git tag -a v$(VERSION) -s -m 'v$(VERSION)'

%:
	@:
