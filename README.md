## Hinemos

<p align="center">
	<img alt="download" src="https://img.shields.io/github/downloads/hinemos/hinemos/total.svg"/>
	<img alt="license" src="https://img.shields.io/badge/license-GPL-blue.svg"/>
	<a href=https://twitter.com/Hinemos_INFO>
		<img alt="twitter" src="https://img.shields.io/twitter/follow/Hinemos_INFO.svg?style=social&label=Follow&maxAge=2592000"/>
	</a>
</p>

![Hinemos-logo](http://www.hinemos.info/files/images/HinemosLogo.png)

Hinemos is an integrated operations management software that maximizes the cost advantages of open source software to achieve "operational automation" such as "monitoring" and "jobs" for increasingly complex large-scale IT systems.

[README(Japanese version)](README.jp.md)  | [Hinemos Portal](http://www.hinemos.info/en/top) | [Latest Packages](https://github.com/hinemos/hinemos/releases/tag/v7.1.1#packages_711)

## Installation

Hinemos can be installed with a single command.

- For installing the Manager
  - RHEL 7.x  
```$ rpm -ivh https://github.com/hinemos/hinemos/releases/download/v7.1.1/hinemos-7.1-manager-7.1.1-1.el7.x86_64.rpm```
  - RHEL 8.x  
```$ rpm -ivh https://github.com/hinemos/hinemos/releases/download/v7.1.1/hinemos-7.1-manager-7.1.1-1.el8.x86_64.rpm```
  - RHEL 9.x  
```$ rpm -ivh https://github.com/hinemos/hinemos/releases/download/v7.1.1/hinemos-7.1-manager-7.1.1-1.el9.x86_64.rpm```


- For installing the Web Client
  - RHEL 7.x  
```$ rpm -ivh https://github.com/hinemos/hinemos/releases/download/v7.1.1/hinemos-7.1-web-7.1.1-1.el7.x86_64.rpm```
  - RHEL 8.x  
```$ rpm -ivh https://github.com/hinemos/hinemos/releases/download/v7.1.1/hinemos-7.1-web-7.1.1-1.el8.x86_64.rpm```
  - RHEL 9.x  
```$ rpm -ivh https://github.com/hinemos/hinemos/releases/download/v7.1.1/hinemos-7.1-web-7.1.1-1.el9.x86_64.rpm```

For details, refer to [Hinemos ver. 7.1 Basic Features Manual](https://github.com/hinemos/hinemos/releases/download/v7.1.1/en_Base_Linux_7.1_rev1.pdf).

## ver. 7.1 New Features

- Hinemos Migration Assistant (Migration support tool from other products)
    - #19379 Hinemos Migration Assistant

## ver. 7.1 Improvements

- General
    - #17640 TLS1.3 support
    - #19151 Oracle Linux 9 support
    - #19155 Amazon Linux 2023 support

- Mission-critical Feature
    - #17619 FIP and second split-brain prevention mechanism support on OCI
    - #19530 Second split-brain prevention mechanism support on Google Cloud

- Cloud Management Feature (Scheduled to be released in July 2024)
    - #18239 Cloud Management Feature Google Cloud support
    - #18238 Cloud Management Feature OCI support

For details, refer to [release note](https://github.com/hinemos/hinemos/releases).

## Documentation

English manual will be available soon.

## License

GNU General Public License (GPL)
