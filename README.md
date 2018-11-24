# Another Hinemos Fork

Forked from [here](https://github.com/hinemos/hinemos).

Current target version: **6.1**
[![Build Status](https://travis-ci.org/pango853/hinemos.svg?branch=6.1g)](https://travis-ci.org/pango853/hinemos)
[![Maintainability](https://api.codeclimate.com/v1/badges/56927da1c9becc9ae9fe/maintainability)](https://codeclimate.com/github/pango853/hinemos/maintainability)
[![Sonarcloud Status](https://sonarcloud.io/api/project_badges/measure?project=pango853_hinemos&metric=alert_status)](https://sonarcloud.io/dashboard?id=pango853_hinemos)
[![Technical debt](https://sonarcloud.io/api/project_badges/measure??key=pango853_hinemos&metric=sqale_index)](https://sonarcloud.io/component_measures?id=pango853_hinemos&metric=sqale_index)
[![Code smells](https://sonarcloud.io/api/project_badges/measure??key=pango853_hinemos&metric=code_smells)](https://sonarcloud.io/component_measures?id=pango853_hinemos&metric=code_smells)
[![Bugs](https://sonarcloud.io/api/project_badges/measure??key=pango853_hinemos&metric=bugs)](https://sonarcloud.io/component_measures?id=pango853_hinemos&metric=bugs)
[![Coverage](https://sonarcloud.io/api/project_badges/measure??key=pango853_hinemos&metric=coverage)](https://sonarcloud.io/component_measures?id=pango853_hinemos&metric=coverage)


# HOWTO

## Build
```
> SET JDK7_HOME=C:\Program Files\Java\jdk1.7.0_80

> gradle :common:build
> gradle :common:deploy

> gradle :manager:build
> gradle :manager:deployManager
> gradle :manager:deployRHEL
> gradle :manager:deployWS

> gradle :agent:build
> gradle :agent:deploy
> gradle :agent:deployManagerCli
```

# TODO

- [ ] Measure coverage
- [ ] SonarCloud check for manager and agent modules
