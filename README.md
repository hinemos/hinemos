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

[README(Japanese version)](README.jp.md)  | [Hinemos Portal](http://www.hinemos.info/en/top) | [Latest Packages](https://github.com/hinemos/hinemos/releases/tag/v7.2.0#packages_720)

## Installation

Hinemos can be installed with a single command.

- For installing the Manager
  - RHEL 8.x  
```$ rpm -ivh https://github.com/hinemos/hinemos/releases/download/v7.2.0/hinemos-7.2-manager-7.2.0-1.el8.x86_64.rpm```
  - RHEL 9.x  
```$ rpm -ivh https://github.com/hinemos/hinemos/releases/download/v7.2.0/hinemos-7.2-manager-7.2.0-1.el9.x86_64.rpm```
  - RHEL 10.x  
```$ rpm -ivh https://github.com/hinemos/hinemos/releases/download/v7.2.0/hinemos-7.2-manager-7.2.0-1.el10.x86_64.rpm```


- For installing the Web Client
  - RHEL 8.x  
```$ rpm -ivh https://github.com/hinemos/hinemos/releases/download/v7.2.0/hinemos-7.2-web-7.2.0-1.el8.x86_64.rpm```
  - RHEL 9.x  
```$ rpm -ivh https://github.com/hinemos/hinemos/releases/download/v7.2.0/hinemos-7.2-web-7.2.0-1.el9.x86_64.rpm```
  - RHEL 10.x  
```$ rpm -ivh https://github.com/hinemos/hinemos/releases/download/v7.2.0/hinemos-7.2-web-7.2.0-1.el10.x86_64.rpm```

See the install document for details.

## ver. 7.2 New Features

- Hinemos AI agent
    - #7221478 Hinemos AI agent

- Call Notification
    - #7221306 Call Notification

## ver. 7.2 Improvements

- Maintenance
  - #7221629 Enable retrieval of internal queue count and summary information for custom trap monitoring in Hinemos_manager_summary
  - #7221565 Add filtering functionality for Hinemos properties
  - #7221450 Improve script for collecting environment summary information to output data from cc_collect_data_string table
  - #7213165 Enable configuration changes related to custom trap monitoring reception via maintenance script

- Self Check
  - #7221627 Add internal queue count check for custom trap monitoring to Self Check

- Monitor
  - #7221739 Enable time-based execution for log file monitoring
  - #7221635 Add internal queue count check for custom trap monitoring to JMX monitoring items
  - #7221605 Enable time-based execution for log file monitoring
  - #7221529 Improve monitoring history [Event] view to allow checking node properties from events
  - #7219073 Suppress log reading when both monitoring and collection are disabled in log file monitoring to reduce agent I/O load

- Job
  - #7221715 Increase PostgreSQL shared_buffers memory
  - #7221593 Add Job Unit ID, Job ID, and Job Name to available system job variables and notification replacement strings
  - #7221564 Improve to allow batch deletion of multiple concurrent job execution control queues
  - #7221061 Enable evaluation of job variable conditions after preceding job completion when both are configured with AND condition

- Installer 
  - #7219416 Add systemd support for operations (agent replication script and installation) that were previously limited to SysVinit in Linux agent (Ubuntu)

- Component
  - #7212780 Support specification changes of /usr/ucb/ps command in Solaris 11.4 and later

- MissionCritical
  - #7221052 [HA] Improve behavior so that if encryption keys differ between Cluster Controllers, the standby server shuts down and notification is sent from the master server
  - #7219437 [HA] Prevent Hinemos Manager (JavaVM) health check connectivity checks from using OS proxy settings
  - #7218402 [HA] Enable control of timeout in Hinemos Manager (JavaVM) health check mechanism
  - #7215157 [HA] Allow configurable retry count for checking PostgreSQL synchronization status during standby server startup

- Utility
  - #7221638 [Utility] Add internal queue count check for custom trap monitoring to JMX monitoring items

- Cloud/VM Management
  - #7221717 [AWS][GCP] Provide functionality to delete services registered in cloud service monitoring master
  - #7221461 [Cloud Management] Improve execution time when detecting new log streams under large numbers of monitored log streams in cloud log monitoring
  - #7221358 [AWS] Update RSS URLs for AWS WAF, AWS IoT Device Management, and AWS Resource Groups in AWS Service Health Dashboard
  - #7221319 [AWS] Support "Service impact" delivery cases in cloud service monitoring RSS incidents
  - #7221152 [AWS] Enable connection to AWS endpoints via HTTPS proxy from Hinemos Manager
  - #7221124 [Cloud/VM Management] Enable use of job variables in compute IDs specified in resource control jobs
  - #7220836 [AWS] Support AWS SDK for Java v2
  - #7220330 [AWS] Reduce number of AssumeRole credential retrievals in cloud log monitoring using IAM role authentication
  - #7218587 [Azure] Update Azure REST API version used in Azure integration

- Command Line Tool
  - #7221337 [CLI Tool] Enable deletion of user-added platform master via repository_deleteCollectPlatformMaster.py

- Upgrade Tool
  - #7221781 [Upgrade Tool] Support "Service impact" delivery cases in cloud service monitoring RSS incidents

- Incident Integration Tool
  - #7221559 [Incident Integration Tool] Support OAuth 2.0 authentication and encryption of passwords for ServiceNow integration
  - #7219956 [Incident Integration Tool] Support Python 3.9 or later

- etc
  - #7221643 Improve internal processing during scheduler execution
  - #7219211 Enable control of notifications when scheduled processes are delayed and not executed

For details, refer to [release note](https://github.com/hinemos/hinemos/releases).

## Documentation

English manuals will be available soon.

## License

GNU General Public License (GPL)
