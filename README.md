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

[README(Japanese version)](README.jp.md)  | [Hinemos Portal](http://www.hinemos.info/en/top) | [Latest Packages](https://github.com/hinemos/hinemos/releases/tag/v7.0.0#packages)

## Installation

You can install hinemos with the following commands.

- Manager Installation
  - RHEL 7.x  
```$ rpm -ivh https://github.com/hinemos/hinemos/releases/download/v7.0.0/hinemos-7.0-manager-7.0.0-1.el7.x86_64.rpm```
  - RHEL 8.x  
```$ rpm -ivh https://github.com/hinemos/hinemos/releases/download/v7.0.0/hinemos-7.0-manager-7.0.0-1.el8.x86_64.rpm```


- Web Client Installation
  - RHEL 7.x  
```$ rpm -ivh https://github.com/hinemos/hinemos/releases/download/v7.0.0/hinemos-7.0-web-7.0.0-1.el7.x86_64.rpm```
  - RHEL 8.x  
```$ rpm -ivh https://github.com/hinemos/hinemos/releases/download/v7.0.0/hinemos-7.0-web-7.0.0-1.el8.x86_64.rpm```

See the install document for details.

## ver.7.0 New Features

- Monitor
    - #11348 SDML(Software Defined Monitoring and Logging)＜Preview＞

- Enterprise
    - #11519 RPA Management＜Preview＞

- Utility Tool
    - #13115 Grafana Plugin


## ver.7.0 Improvement

- Client & Agent
    - #11218 Storing filtering conditions
    - #12671 Agent health check
    - #10237 Records of tying login sessions for operation logs with operations 
    - #12483 Improving Windows Hinemos Agent Installer

- Repository
    - #11519 Added cloud and virtualization management items to node information
    - #12342 Added RPA management items to node information
    - #12729 DHCP support feature
    - #12729 Automatic assignment of scopes configured in the Agent setting
    - #11479 Changes in operation made when "auto" is selected as the customized
             command operation mode (repository.cmdb.command.mode) in the Node configuration retrieval setting

- Notification
    - #11692 REST notification
    - #11611 Added ID to notification results
    - #11479 Changes in operation made when "auto" is selected as the command execution mode
             (monitor.event.customcmd.cmdN.mode) in the event custom command setting

- Maintenance
   - #12256 Added "Delete job integration message" to the maintenance types of history deletion
   - #11519 Added "Delete RPA scenario records" to the maintenance types of history deletion
   - #11319 Added a script to output data registered in Hinemos as a file

- Self Check
    - #10151 Sending syslogs of INTERNAL events occurring in Hinemos Agent
    - #7227  Enabled to specify mail title to INTERNAL events occurring in Manager

- Monitor
    - #12427 Improved the specification of the Log Count Monitor feature
    - #12774 Mission Critical feature support for Custom Trap monitoring
    - #11714 Improved the specification of priority change in the string monitor feature
    - #12758 Improved the specification of priority change in the Cloud Service Monitor feature
    - #11519 Added directory to the node property items for the Logfile Monitor feature
    - #11479 Changes in operation made when "auto" is selected as the OS platform definition for
             custom monitor command operation (monitor.custom.command.mode) in the Custom Monitor setting
    - #12149 Added redirect to the HTTP Monitor (Scenario) feature
    - #11524 Added NTLM authentication to the WinRM authentication methods used in the Windows Service Monitor feature

- Job
    - #11318 Pre-generation of job sessions 
    - #12256 Job Link message
    - #12487 FileCheck job
    - #12488 Output stdout as a file
    - #12639 Coping with outlier in return values
    - #12486 Setting multiple wait conditions
    - #12486 Revised judgement logics for wait conditions
    - #12606 Repeated execution
    - #11479 Changes in operation made when "auto" is selected as the startup command operation mode
             (job.command.mode) in the command job setting

- Cloud Management
   - #12342 Cloud Log monitoring
   - #12482 Cloud notification
   - #12395 Mission Critical feature support for cloud resource control job
   - #12130 Made improvement so that management flags for nodes will change according to the operation to computes

- NodeMap
   - #11507 Added facility name to the downloaded node configuration

- Other Improvements
   - #11226 Made improvement so that multiple SMTP servers can be registered for mail delivery
   - #13144 Added OAuth2.0 authentication for mail delivery

See the [release notes](https://github.com/hinemos/hinemos/releases) for details.

## Documentation

English manuals will be available soon.

## License

GNU General Public License (GPL)
