# monitoring_intervention_system
Monitoring and intervention system

##Synopsis
This project contains the code to implement a monitoring system of servers of a corporate intranet. Using an Android application you can view the data collected and perform actions on the monitored hosts.

##Motivation
I realized this project at the end of my university career. In addition, this project was carried out as there are no monitoring systems used specifically for smartphones. But most importantly, no monitoring system allows you to perform actions on the monitored hosts.

##Project structure
The project can be split in 3 entity:
* Hosts
* Server
* Smartphone/application

#Host:
In this project, the **hosts** are computers to be monitored. To retrieve informations are used bash scripts that are run periodically. The scripts were tested on the following operating systems: CentOS and RedHat 4/5/6.
The bash scripts collect two types of information/data: <br>
1. **Description host specs**, this informations describes the monitored host, these data rarely changes:
  * RAM memory in MB (e.g. 2048)
  * IP address
  * Operating system (e.g. CentOS 6)
  * Hostname (e. g. webserver.danilo.com)
  * CPU type (e. g. Intel Xeon )
  * Disk information (e. g. xvda: 16.1GB)
  * Machine type (e. g. Virtual)<br>
The name of the script that retrieve this information is : getInfo.sh<br>
2. **Health information**, this data describes the state of health of a host , these data frequently changes:
  * RAM used (e. g. 54% )
  * CPU used (e. g. 80% )
  * Use partitions and mount point (e. g. 54% / - 13% /boot )
  * Number of users connected (e. g. 3)<br>
The name of the script that retrieve this information is : getHealthStatus.sh
To run this script periodically uses the `cron` daemon. To schedule the execution modify the crontab file by running the command `crontab -e`.
Add the following cronjob (customize the path of the script):
```
*/5 * * * * /home/danilo/scripts/getHealthStatus.sh >/dev/null 2>&1
0 * * * * /home/danilo/scripts/getInfo.sh >/dev/null 2>&1
```
These scripts incorporate the information retrieved in the body of an HTTP POST using the JSON format. The recipient of the POST request is the server, you can customize the server IP editing scripts bash. <br>
Example of JSON send from `getInfo.sh` :
```
{
  "operation":"Information",
  "data":{
   "IP": "192.168.0.120'",
   "Hostname":"xen10.danilo.com",
   "Memoria":"1024MB",
   "OS": "Red Hat Enterprise Linux 6" ,
   "CPU" : "Pentium(R) Dual-Core E5300",
   "Tipo macchina" : "virtuale",
   "Dischi": "xvda: 16.1GB"
  }
}
```
Example of JSON send from `getHealthStatus.sh` :
```
{
  "operation":"HealthStatus",
  "data":{
   "IP": "192.168.0.175",
   "Memoria": "50%" ,
   "CPU" : "0.954%",
   "Partizioni" : "54% /- 13% /boot",
   "Utenti": "1"
  }
}
```
#Server

The server is the main point of the structure because it performs the task of storing the information of the host and responds to HTTP requests made from Android application. it is necessary that the server resides in the same intranet of the monitored hosts. It also needs to be accessible from the internet in order to receive the requests of the mobile application. I used Apache like web server and MySQL like database to store information. In the *server* you can find the following PHP files:
  * *internal.php*: this file is the endpoint of the HTTP request made by hosts.
  * *external.php*: this file is the endpoint of the HTTP request made by Android application.
  * *daniDB.php*: It contain the functions to interact with database.
  * *functions.php*: this file contain the functions used by internal.php and external.php.
The database folder contain a dump of the database used in this project. <br>
This database contain only 3 tables:
* Host: Store the hosts information.
* Health: Store the hosts health status.
* query_error: Help in troubleshooting with SQL query.
