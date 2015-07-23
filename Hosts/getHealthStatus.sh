#!/bin/bash
# @Author:Danilo Raspa
# @Date: 09/01/2015
#
pathToServer="http://192.168.0.222/internal.php"
#Questo script raccoglie alcune informazioni sullo stato di salute della macchina e li invia al server tramite POST 
#IPaddress della macchina
ip=$(/sbin/ip addr |grep 'state UP' -A2 | tail -n1 | awk '{print $2}' | cut -f1 -d'/' | tr ' ' '_'|head -n1)
#Memoria utilizzata
mfree=$(cat /proc/meminfo | grep MemFree | cut -d ':' -f 2 | cut -d'k' -f 1 | tr -d ' ')
mtot=$(cat /proc/meminfo | grep MemTotal | cut -d ':' -f 2 | cut -d'k' -f 1 | tr -d ' ')
let memUsed=($mtot-$mfree)*100/$mtot
#Cpu utilizzata
cpuUsed=$(grep 'cpu ' /proc/stat | awk '{usage=($2+$4)*100/($2+$4+$5)} END {print usage "%"}')
#Partizione utilizzata
partitionStatus=$(df -Th | grep -v "Use" |grep -v '192.168'|grep -v nfs| grep % | sed 's/  */ /g' | cut -d ' ' -f 6,7 |tr '\n' '-' | tr ' ' '_')
#Numero di utenti connessi
utentiConnessi=$(ps aux | grep bash | grep -v 'grep\|daemon\|getHealthStatus.sh' |grep root|grep "\-bash" | wc -l| tr ' ' '_')

curl -i \
-H "Accept: application/json" \
-H "Content-Type:application/json" \
-X POST --data '{"operation":"HealthStatus", "data":{ "IP": "'$ip'" , "Memoria":"'$memUsed'%" , "CPU":"'$cpuUsed'" , "Partizioni": "'$partitionStatus'" , "Utenti" : "'$utentiConnessi'"} }' $pathToServer
