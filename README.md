## Hinemos

<p align="center"> 
  <img alt="download" src="https://img.shields.io/github/downloads/hinemos/hinemos/total.svg"/>
  <img alt="license" src="https://img.shields.io/badge/license-GPL-blue.svg"/> 
  <a href=https://twitter.com/Hinemos_INFO> 
    <img alt="twitter" src="https://img.shields.io/twitter/follow/Hinemos_INFO.svg?style=social&label=Follow&maxAge=2592000"/>
  </a>
</p>

![Hinemos-logo](http://www.hinemos.info/files/images/HinemosLogo.png)

Hinemos is an open source integrated system management software providing both monitoring and job management (workload scheduling) features, to implement system operation automation.

[README(日本語版)](README.jp.md)  | [Hinemos Portal](http://www.hinemos.info/en/top) | [Latest Packages](https://github.com/hinemos/hinemos/releases/tag/v6.2.1#packages)

## Installation

You can install hinemos with the following commands.

- Manager Installation

```$ rpm -ivh https://github.com/hinemos/hinemos/releases/download/v6.2.1/hinemos-6.2-manager-6.2.1-1.el7.x86_64.rpm```

- Web Client Installation

```$ rpm -ivh https://github.com/hinemos/hinemos/releases/download/v6.2.1/hinemos-6.2-web-6.2.1-1.el7.x86_64.rpm```

See the install document for details.

## ver.6.2 New Features

- Manage history and notify changes of the Node Configuration
    - Retrieve information of target's package, program, device etc. regularly and manage its history
    - Detect and notify configuration changes
    - Search through collected data and output data in CSV format (provided with the subscription)

## ver.6.2 Improvement

- Improvement of Agent Update Feature
    - Improvement of the transfer efficiency
    - Able to view more detailed update status

- Concurrent Control Queue
    - Able to control the number of jobs executed across Jobs and JobNets at the same time using Concurrent Control Queue

- Increased the number of items which can be included in an event information
    - Able to add original information to the items included in an event information

- User operation using the event information
    - Operation (command) using event information is defined in advance and can be executed by user judgment

- Improvement of Monitor History[Event] view
    - Able to specify default layout for each Monitor History View

- Improvement of version upgrade tool (provided with the subscription)
    - Able to directly convert definitions of Hinemos ver.4.1 (and later) to Hinemos ver.6.2

See the [release notes](https://github.com/hinemos/hinemos/releases) for details.

## Documentation

English manuals will be available soon.

## License

GNU General Public License (GPL)
