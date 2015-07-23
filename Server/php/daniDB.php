<?php
	/**
	 * Define Brice DB
	 *
	 * Define functions for handle DB
	 */
	class daniDB {
		/** Define DB link */
		protected $link; 
		/** Define DB server */
		private $server;
		/** Define DB user */
		private $username;
		/** Define DB password */
		private $password;
		/** Define DB name */
		private $db;
		/** Define connection status */
		private $connectionStatus;
		
		/**
		 * Intialize class.
		 *
		 * @param boolean $local boolean value to indicate if the connection to DB is local or not .
		 *
		 * @return void 
		 */
		public function __construct($local = true) {
			if($local) {
				$this->server = "127.0.0.1";
				$this->username = "root";
				$this->password = "root";
				$this->db = "thesis";
			}
			else {
				$this->server = "127.0.0.1";
				$this->username = "adminKK5XMvb";
				$this->password = "tMIPKC9GhsP-";
				$this->db = "thesis";
			}
			$this->connect();
		}
		
		/**
		 * Extabilish db connection
		 *
		 * @return void 
		 */
		private function connect() {
			$this->link = mysql_connect($this->server, $this->username, $this->password);
			mysql_select_db($this->db, $this->link);
			$this->connectionStatus = mysql_error();
		}
		
		/**
		 * Return the current DB status
		 *
		 * @return string Return the current status.
		 */
		public function __toString()  {
			return $this->connectionStatus;
		}
	
		/**
		 * Perform DB query
		 *
		 * @param string $stmt string value containing the query.
		 * @param string $debug boolean value to perform debug.
		 *
		 * @return array|boolean Return query result or a boolean value.
		 */	
		public function query($stmt, $debug = false) {
			$ind = strpos($stmt, " ");
			$queryType = substr($stmt, 0, $ind);
			$exec = mysql_query($stmt);
			$stmt = addslashes($stmt);
			if($debug AND !$exec) {echo "<br />ERRORE: Query: $stmt ->".mysql_error();}
			if(!$exec AND !$debug) {
				$error = mysql_error();
				$errorAppo = addslashes($error);
				$stmtAppo = addslashes($stmt);
				$alreadyExist = mysql_query("SELECT * FROM query_error WHERE query = '$stmtAppo' AND error = '$errorAppo'");
				if(mysql_num_rows($alreadyExist) == 0) {
					mysql_query("INSERT INTO query_error (query, error, moment) VALUES('$stmtAppo', '$errorAppo', NOW())");
				}
				return false;
			}else {
				$queryType = strtoupper($queryType);
				switch($queryType) {
					case "INSERT":
						return mysql_insert_id();
					case "UPDATE":
					case "DELETE":
					case "ALTER":
						return ($exec) ? true : false;
					case "SELECT":
						$return = array();
						while($row = mysql_fetch_assoc($exec)){
							$return[] = $row;
						}
						if(count($return) == 1 and count($return[0]) == 1) return array_shift($return[0]);
						elseif(count($return) == 1) return array_shift($return);
						elseif(count($return) == 0) return false;
						else return $return;
				}
			}
			
		}

		/**
		 * Close DB connection
		 */		
		public function close() {
			mysql_close($this->link);
		}
	}
?>
