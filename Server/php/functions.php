<?php
	include 'daniDB.php';
	/**
	 * Define Thesis Functions
	 *
	 * Define functions for setInfo, addHealthStatus
	 */
	class functions {
		/** define response array */
		private $json;
		/** define DB variable */
		private $DB; 

		/**
		 * Initialize class.
		 *
		 * @return void 
		 */
		function __construct() {
			$this->DB = new daniDB();
			$this->json = array("status" => 0, "response" => "");
		}
		/**
		 * Setta le informazioni di un host.
		 *
		 * @param string $ip IP dell'host.
		 * @param string $hostname Hostname dell'host.
		 * @param string $memory Memoria totale dell'host, epressa in MB.
		 * @param string $OS Sistema operativo installato.
		 * @param string $CPU Specifiche della CPU.
		 * @param string $disk Lista dei dischi disponibili.
		 * @param string $type MAC address of the new device to register.
		 *
		 * @return array Ritorna lo stato delle operazioni eseguite.
		 */
		function setInfoHost($ip, $hostname, $memory, $OS, $CPU, $disk, $type){
			$insert = false;
			$this->DB->query("DELETE FROM Host WHERE ip = '$ip' ");

			$insert = $this->DB->query("INSERT INTO Host (ip,hostname,memory,OS,CPU,disk,type) VALUES ('$ip', '$hostname', '$memory', '$OS', '$CPU', '$disk', '$type')" );
			if(is_int($insert)){
				$this->json['status'] = 200;
				$this->json['response'] = "Informazioni host inserite";
			}else{
				$this->json['status'] = 300;
				$this->json['response'] = "Insert fallita!";
			}
			
			return $this->json;
		}
		
		
		/**
		 * Setta le informazioni di un host.
		 *
		 * @param string $ip IP dell'host.
		 * @param string $memory Memoria utilizzata, epressa in MB.
		 * @param string $CPU Percentuale della CPU utilizzata.
		 * @param string $partitions Percentuale utilizzata di ogni partizione.
		 * @param string $users Utenti attualmente collegati.
		 *
		 * @return array Ritorna lo stato delle operazioni eseguite.
		 */
		function setHealth($ip, $memory, $CPU, $partitions, $users){
			$delete = false;
			$insert = false;
			$half_hour_ago = date("Y-m-d : H:i:s", time()-30*60);
			$delete = $this->DB->query("DELETE FROM Health WHERE ip = '$ip' and date < '$half_hour_ago' ");
			$now = date("Y-m-d : H:i:s", time());
			$insert = $this->DB->query("INSERT INTO Health (ip,memory,CPU,partitions,users,date) VALUES ('$ip', '$memory', '$CPU', '$partitions', '$users', '$now')" );
			if(is_int($insert)){
				$this->json['status'] = 200;
				$this->json['response'] = "Informazioni salute host inserite";
			}else{
				$this->json['status'] = 300;
				$this->json['response'] = "Insert fallita!";
			}
			
			return $this->json;
		}


		/**
		 * Estrae le informazioni di una o più macchine.
		 *
		 * @param string $ip IP della macchina di cui tornare le informazioni(se è "all!" torna le informazioni di tutte le macchine).
		 *
		 * @return array Ritorna le informazioni di una o più macchine.
		 */
		function getHosts($ip){
			$result = array();
			$result['status'] = 200;
			$result['typeHost'] = 'single';
			$result['host'] = array();
			if($ip == "all!") {
				$result['typeHost'] = 'all';
				$result_ip = $this->DB->query("SELECT ip FROM Host");
				if(is_array($result_ip) ) {
					foreach ($result_ip as $val) {
						$resultIntermediate = $this->getSingleHost($val['ip']);
						array_push($result['host'] , $resultIntermediate);
					}
				}else{
					$resultIntermediate = $this->getSingleHost($result_ip);
					array_push($result['host'] , $resultIntermediate);
				}	
			}else{
				$resultIntermediate = $this->getSingleHost($ip);
				if($resultIntermediate != false ) {
					array_push($result['host'] , $resultIntermediate);
				}else{
					$this->json['status'] = 300;
					$this->json['response'] = "Host non monitorato!";
					return $this->json;
				}	
			}
			
			return $result;
		}

		/**
		 * Esegue l'azione richiesta(shutdown o kill session) sulle macchina remota
		 *
		 * @param string $ip IP della macchina su cui eseguire l'azione richiesta
		 * @param string $command Comando da eseguire sulla machhina remota
		 *
		 * @return ritorna un booleano che esprime la riuscita o meno del comando
		 */
		function action($ip,$command) {
			$error = false;
			$return = 1;
			if ($command == 'off') { //spegne la macchina
					exec("ssh -o GSSAPIAuthentication=no root@$ip 'shutdown -h now' ",$output,$return);
			}else { if ($command == 'kill_sessions' ) { //killa tutte le sessione bash
							exec("ssh root@$ip 'killall -9 bash' ",$output,$return);
					} else {
							$error = true;
					}
			}
			if($error == false) {
					if ($return == 0 ) {
							$this->json['status'] = 200;
							$this->json['response'] = 'Action executed';
					} else {
							$this->json['status'] = 301;
							$this->json['response'] = 'Action failed';
					}
			}else {
					$this->json['status'] = 302;
					$this->json['response'] = 'Command not found';
			}
			return $this->json;
                }

		
		/**
		 * Estrae le informazioni di una macchina.
		 *
		 * @param string $ip IP della macchina di cui tornare le informazioni(se è "all!" torna le informazioni di tutte le macchine).
		 *
		 * @return array Ritorna le informazioni di una o più macchine.
		 */
		function getSingleHost($ip) {
			$select = $this->DB->query("SELECT hostname,memory,OS,CPU,disk,type FROM Host WHERE ip = '$ip' ");
			if ($select != false) {
				$result['ip'] = $ip;
				$result['status'] = $this->isReachable($ip);
				$result['information'] = $select;
				$result['health'] = $this->DB->query("SELECT memory,CPU,partitions,users FROM Health WHERE ip = '$ip' and date = (SELECT MAX(date) FROM Health WHERE ip ='$ip' )  ");
			}else{
				return false;
			}	
			return $result;
		}


		/**
		 * Estrae le informazioni di una o più macchine.
		 *
		 * @param string $ip IP della macchina di cui tornare le informazioni(se è "all!" torna le informazioni di tutte le macchine).
		 *
		 * @return array Ritorna le informazioni di una o più macchine.
		 */
		function isReachable($ip) {
		    $pingresult = exec("/bin/ping -c 1 -W 1 $ip", $outcome, $status);
		    if (0 == $status) {
			$status = 1; //raggiungibile
		    } else {
			$status = 0; //non raggiungibile
		    }
		    return $status;
		}

		
		
		
		/**
		 * Check if a request is a POST request.
		 *
		 * @return boolean Return true if the request is a POST request, otherwise false.
		 */
		function isPost() {
			return ($_SERVER['REQUEST_METHOD'] == 'POST') ? true : false;
		}
		/**
		 * Check if a request is a GET request.
		 *
		 * @return boolean Return true if the request is a GET request, otherwise false.
		 */
		function isGet() {
			return ($_SERVER['REQUEST_METHOD'] == 'GET') ? true : false;
		}
		
	}
?>
