<?php
	include 'functions.php';

	$json = array("status" => 0, "response" => "");
	$functions = new functions();
	$data_received = json_decode(file_get_contents('php://input'),true);
	var_dump($data_received);
	if($functions->isPost()) {
		$error = false;
		$data = array();
		if(isset($data_received['operation'])) $operation = $data_received['operation']; else $error = true;
		if(!$error) {
			$error = false;
			if(isset($data_received['data'])) $data = $data_received['data']; else $error = true;
			if(!$error){ 
				switch ($operation) {
					case 'Information':
							if(isset($data['IP']) and !empty($data['IP'])) $ip = $data['IP']; else $error = true;
							if(isset($data['Hostname']) and !empty($data['Hostname'])) $hostname = $data['Hostname']; else $error = true;
							if(isset($data['Memoria']) and !empty($data['Memoria'])) $memory = $data['Memoria']; else $error = true;
							if(isset($data['OS']) and !empty($data['OS'])) $OS = $data['OS']; else $error = true;
							if(isset($data['CPU']) and !empty($data['CPU'])) $CPU = $data['CPU']; else $error = true;
							if(isset($data['Disco']) and !empty($data['Disco'])) $disk = $data['Disco']; else $error = true;
							if(isset($data['Tipo macchina']) and !empty($data['Tipo macchina'])) $type = $data['Tipo macchina']; else $error = true;

							if(!$error) {
								$infoHost = $functions->setInfoHost($ip, $hostname, $memory, $OS, $CPU, $disk, $type);
								echo json_encode($infoHost);
							}
						break;
					case 'HealthStatus':
							if(isset($data['IP']) and !empty($data['IP'])) $ip = $data['IP']; else $error = true;
							if(isset($data['Memoria']) and !empty($data['Memoria'])) $memory = $data['Memoria']; else $error = true;
							if(isset($data['CPU']) and !empty($data['CPU'])) $CPU = $data['CPU']; else $error = true;
							if(isset($data['Partizioni']) and !empty($data['Partizioni'])) $partitions = $data['Partizioni']; else $error = true;
							if(isset($data['Utenti']) and ($data['Utenti'] != "") and  ((int)$data['Utenti'] >= 0)) $users = $data['Utenti']; else $error = true;

							if(!$error) {
								$healthStatus = $functions->setHealth($ip, $memory, $CPU, $partitions, $users);
								echo json_encode($healthStatus);
							}
						break;
					default:
							$json['status'] = 301;
							$json['response'] = 'Invalid operation!';
							echo json_encode($json);
						break;
				}
				if ($error) {
					$json['status'] = 301;
					$json['response'] = 'Invalid data!';
					echo json_encode($json);
				}
			}else{
				$json['status'] = 301;
				$json['response'] = 'Data not set';
				echo json_encode($json);
			}
		}
		else {
			$json['status'] = 301;
			$json['response'] = 'Operation not set!';
			echo json_encode($json);
		}

	}
	else {
		$json['status'] = 301;
		$json['response'] = "Invalid request!";
		echo json_encode($json);
	}

?>
