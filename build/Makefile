VERSION:=11.0.8_10

.PHONY:build
build:
	docker build -t scmmanager/java-build:${VERSION} .

.PHONY:publish
publish: build
	docker push scmmanager/java-build:${VERSION}
