# Another Hinemos Fork

Forked from [here](https://github.com/hinemos/hinemos).

Current target version: **6.1**

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
