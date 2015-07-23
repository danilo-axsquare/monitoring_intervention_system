<?php
	include 'functions.php';

	$json = array("status" => 0, "response" => "");
	$functions = new functions();
	$data_received = json_decode(file_get_contents('php://input'),true);
	if($functions->isPost()) {
		$error = false;
		if(isset($data_received['operation'])) $operation = $data_received['operation']; else $error = true;
		if(!$error) {
			$error = false;
			if(isset($data_received['ip'])) $ip = $data_received['ip']; else $error = true;
			if(!$error){ 
				switch ($operation) {
					case 'getHost':
							$hostInfo = $functions->getHosts($ip);
							echo json_encode($hostInfo);
						break;
					case 'action' :
							if(isset($data_received['command']) and !empty($data_received['command'])) $command = $data_received['command']; else $error = true;
							if(!$error) {
								$responseStatus = $functions->action($ip, $command);
								echo json_encode($responseStatus);
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
