## ver.7.0 New Features

- Monitor
    - #13586 SDML(Software Defined Monitoring and Logging)

- Enterprise
    - #13585 RPA Management Feature

- Utility Tool
    - #13115 Grafana Plugin

## ver.7.0 Improvement

- Manager, Agent, and Client
    - #15254,#15260,#15187 Communication on Hinemos now using TLS1.2 due to default TLS for JRE changed to 1.3.

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
    - #14716 Improved the content of the notification's monitoring details, application, and scope.

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

See the [release note](https://github.com/hinemos/hinemos/releases/tag/v7.0.1) for details.