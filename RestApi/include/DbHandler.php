<?php

/**
 * Helper class to handle database operations.
 * This class contains the CRUD methods for database tables.
 *
 * @author Naga Venkat Kamuju
 */
class DbHandler {

    private $conn;

    function __construct() {
        require_once dirname(__FILE__) . '/DbConnect.php';

        //Open db connection
        $db = new DbConnect();
        $this->conn = $db->connect();
    }


    /* ------------- Users table ------------------ */

    /**
     * Creating new user
     * @param String $name User name
     * @param String $email User login email id
     * @param String $password User login password
     */
    public function createUser($name, $email, $password) {
        
        $response = array();

        // First check if user already existed in db
        if (!$this->isUserExists($email)) {
            
            // Generating password hash
            $password_hash = password_hash($password, PASSWORD_DEFAULT);

            // Generating API key
            $api_key = $this->generateApiKey();

            // insert query
            $stmt = $this->conn->prepare("INSERT INTO users(name, email, password_hash, api_key) values(?, ?, ?, ?)");
            
            $stmt->bind_param("ssss", $name, $email, $password_hash, $api_key);

            $result = $stmt->execute();

            $stmt->close();

            // Check for successful insertion
            if ($result) {
                // User successfully inserted
                return RESULT_CODE_SUCCESS;
            } else {
                // Failed to create user
                return RESULT_CODE_FAILURE;
            }
        } else {
            // User with same email already existed in the db
            return RESULT_CODE_ALREADY_EXISTS;
        }

        return $response;
    }

    /**
     * Checking user login
     * @param String $email User login email id
     * @param String $password User login password
     * @return boolean User login status success/fail
     */
    public function checkLogin($email, $password) {
        
        // fetching user by email
        $stmt = $this->conn->prepare("SELECT password_hash FROM users WHERE email = ?");

        $stmt->bind_param("s", $email);

        $stmt->execute();

        $stmt->bind_result($password_hash);

        $stmt->store_result();

        if ($stmt->num_rows > 0) {

            // Found user with the email, Now verify the password

            $stmt->fetch();

            $stmt->close();

            if(password_verify($password, $password_hash)) {
                
                return TRUE; // User password is correct
            } else {
                
                return FALSE; // user password is incorrect
            }

        } else {
            $stmt->close();

            // user not existed with the email
            return FALSE;
        }
    }

    /**
     * Checking for duplicate user by email address
     * @param String $email email to check in db
     * @return boolean
     */
    private function isUserExists($email) {
        $stmt = $this->conn->prepare("SELECT id from users WHERE email = ?");
        $stmt->bind_param("s", $email);
        $stmt->execute();
        $stmt->store_result();
        $num_rows = $stmt->num_rows;
        $stmt->close();
        return $num_rows > 0;
    }


    /**
     * Fetching user by email
     * @param String $email User email id
     */
    public function getUserByEmail($email) {
        $stmt = $this->conn->prepare("SELECT name, email, api_key FROM users WHERE email = ?");
        $stmt->bind_param("s", $email);
        if ($stmt->execute()) {
            // $user = $stmt->get_result()->fetch_assoc();
            $stmt->bind_result($name, $email, $api_key);
            $stmt->fetch();
            $user = array();
            $user["name"] = $name;
            $user["email"] = $email;
            $user["api_key"] = $api_key;
            $stmt->close();
            return $user;
        } else {
            return NULL;
        }
    }

    /**
     * Fetching user api token
     * @param String $user_id user id primary key in user table
     */
    public function getApiKeyById($user_id) {
        $stmt = $this->conn->prepare("SELECT api_key FROM users WHERE id = ?");
        $stmt->bind_param("i", $user_id);
        if ($stmt->execute()) {
            // $api_key = $stmt->get_result()->fetch_assoc();
            $stmt->bind_result($api_key);
            $stmt->close();
            return $api_key;
        } else {
            return NULL;
        }
    }

    /**
     * Fetching user id by api key
     * @param String $api_key user api key
     */
    public function getUserId($api_key) {
        $stmt = $this->conn->prepare("SELECT id FROM users WHERE api_key = ?");
        $stmt->bind_param("s", $api_key);
        if ($stmt->execute()) {
            $stmt->bind_result($user_id);
            $stmt->fetch();
            // $user_id = $stmt->get_result()->fetch_assoc();
            $stmt->close();
            return $user_id;
        } else {
            return NULL;
        }
    }

    /**
     * Validating user api key
     * If the api key is there in db, it is a valid key
     * @param String $api_key user api key
     * @return boolean
     */
    public function isValidApiKey($api_key) {
        $stmt = $this->conn->prepare("SELECT id from users WHERE api_key = ?");
        $stmt->bind_param("s", $api_key);
        $stmt->execute();
        $stmt->store_result();
        $num_rows = $stmt->num_rows;
        $stmt->close();
        return $num_rows > 0;
    }

    /**
     * Generating random Unique MD5 String for user Api key
     */
    private function generateApiKey() {
        return md5(uniqid(rand(), true));
    }

    /* ------------- SKU table ------------------ */

    /**
     * Insert new sku
     * @param String $sku_name SKU description
     * @param int $location_id Location id
     * @param int $meta_info_id Meta info id
     * @return result code
     */
    public function insertSKU($sku_name, $location_id, $meta_info_id) {

        // Check if sku already existed in db
        if ($this->is_sku_exists($sku_name)) {
             return RESULT_CODE_ALREADY_EXISTS;
         }

        if (!$this->conn) {
            return RESULT_CODE_FAILURE;
        }

        //SQL statement to insert record
        $sql_query = "INSERT INTO sku (sku_name, location_id, meta_info_id) values(?, ?, ?)";

        //Prepare SQL statement
        $stmt = $this->conn->prepare($sql_query);
        if ( false === $stmt ) {
            return RESULT_CODE_FAILURE;
        }

        //Bind arugments to SQL Query
        $stmt->bind_param("sii", $sku_name, $location_id, $meta_info_id);

        //Execute SQL Query
        $result = $stmt->execute();

        $stmt->close();

        // Check for successful insertion
        if ($result) {
            return RESULT_CODE_SUCCESS;
        } else {
            return RESULT_CODE_FAILURE;
        }

    }

    /**
     * Fetch SKU for the supplied arguments
     * @param String $location Location name
     * @param String $dept Department name
     * @param String $category Item category name
     * @param String $sub_category Item sub-category name
     * @return list of sku if available, null otherwise
     */
    public function getSKU($location_id, $dept_id, $category_id, $sub_category_id) {

        //Default SQL query to fetch all sku
        $sql_query = "SELECT sku.sku_id, sku.sku_name,
            locations.location_id, locations.location_name,
            departments.dept_id, departments.dept_name,
            categories.category_id, categories.category_name,
            sub_categories.sub_category_id, sub_categories.sub_category_name
            FROM sku, locations, sub_categories, categories, departments
            WHERE sku.location_id = locations.location_id
            AND sku.meta_info_id = sub_categories.sub_category_id
            AND sub_categories.category_id = categories.category_id
            AND categories.dept_id = departments.dept_id ";

        //Append additional clauses to sql query if specific records needs to be fetched.
        $i = 0;
        $bindParams = array();
        if (!is_null($location_id)) {
            $sql_query .= " AND locations.location_id = ?";
            $bindParams[$i] = $location_id;
            $i++;
        }

        if (!is_null($dept_id)) {
            $sql_query .= " AND departments.dept_id  = ?";
            $bindParams[$i] = $dept_id;
            $i++;
        }

        if (!is_null($category_id)) {
            $sql_query .= " AND categories.category_id = ?";
            $bindParams[$i] = $category_id;
            $i++;
        }

        if (!is_null($sub_category_id)) {
            $sql_query .= " AND sub_categories.sub_category_id = ?";
            $bindParams[$i] = $sub_category_id;
        }

        $sql_query .= " ORDER BY sku.sku_id";

        if (!$this->conn) {
            return null;
        }

        //Prepare SQL statement
        $stmt = $this->conn->prepare($sql_query);
        if ( false === $stmt ) {
            return null;
        }

        //Bind arugments to SQL Query
        if (count($bindParams) == 4)
            $stmt->bind_param("iiii", $bindParams[0], $bindParams[1], $bindParams[2], $bindParams[3]);
        else if (count($bindParams) == 3)
            $stmt->bind_param("iii", $bindParams[0], $bindParams[1], $bindParams[2]);
        else if (count($bindParams) == 2)
            $stmt->bind_param("ii", $bindParams[0], $bindParams[1]);
        else if (count($bindParams) == 1)
            $stmt->bind_param("i", $bindParams[0]);        

        //Execute SQL Query
        $result = $stmt->execute();

        //Save result set
        $sku_array = $stmt->get_result();

        $stmt->close();

        //Return results
        return $sku_array;
    }


    /**
     * Update sku
     * @param int       $sku_id         Id of the SKU
     * @param String    $sku_name       Name of the SKU
     * @param int       $location_id    Location Id of the SKU
     * @param int       $meta_info_id   Meta info Id
     * @return          true if sku update, false otherwise
     */
    public function updateSKU($sku_id, $sku_name, $location_id, $meta_info_id) {

        if (!$this->conn) {
            return false;
        }

        //Sql statement to update sku
        $sql_query = "UPDATE sku SET sku_name = ?, location_id = ?, meta_info_id = ? WHERE sku_id = ?";

        //Prepare SQl statement
        $stmt = $this->conn->prepare($sql_query);
        if ( false === $stmt ) {
            return false;
        }

        //Bind parameters to SQL statement
        $stmt->bind_param("siii", $sku_name, $location_id, $meta_info_id, $sku_id);

        //Execute SQL statement
        $stmt->execute();

        //Fetch no.of rows updated
        $num_affected_rows = $stmt->affected_rows;

        //Close
        $stmt->close();

        //Return true if updated
        return $num_affected_rows > 0;
    }

    /**
     * Delete an SKU
     * @param String $sku_id id of the sku to delete
     * @return true if SKU deleted, false otherwise
     */
    public function deleteSKU($sku_id) {

        if (!$this->conn) {
            return false;
        }

        //Prepare SQl statement
        $stmt = $this->conn->prepare("DELETE FROM sku WHERE sku_id = ?");
        if ( false === $stmt ) {
            return false;
        }

        //Bind parameters to SQL statement
        $stmt->bind_param("i", $sku_id);

        //Execute SQL statement
        $stmt->execute();

        //Fetch no.of rows deleted
        $num_affected_rows = $stmt->affected_rows;

        $stmt->close();

        //Return true if deleted
        return $num_affected_rows > 0;
    }

    /**
     * Checking for duplicate sku
     * @param String $sku_name SKU description
     * @return boolean True if SKU found with supplied name
     */
    private function is_sku_exists($sku_name) {

        if (!$this->conn) {
            return false;
        }

        //Prepare SQl statement
        $stmt = $this->conn->prepare("SELECT sku_id from sku WHERE sku_name = ?");
        if ( false === $stmt ) {
            return false;
        }

        //Bind parameters to SQL statement
        $stmt->bind_param("s", $sku_name);

        //Execute SQL statement
        $stmt->execute();

        //Transfers result set from the last query
        $stmt->store_result();

        //Find out no.of records fetched
        $num_rows = $stmt->num_rows;

        $stmt->close();

        //Return true if record exist.
        return $num_rows > 0;
    }


    /**
     * Fetching Location List
     * @param String $location Location name
     * @param String $dept Department name
     * @param String $category Item category name
     * @param String $sub_category Item sub-category name
     * @return list of sku if available, null otherwise
     */
    public function getLocations() {

        //Default SQL query to fetch all locations
        $sql_query = "SELECT location_id, location_name FROM locations";

        if (!$this->conn) {
            return null;
        }

        //Prepare SQL statement
        $stmt = $this->conn->prepare($sql_query);
        if ( false === $stmt ) {
            return null;
        }

        //Execute SQL Query
        $result = $stmt->execute();

        //Save result set
        $location_array = $stmt->get_result();

        $stmt->close();

        //Return results
        return $location_array;
    }

    /**
     * Fetch meta data
     * @return list of meta data
     */
    public function getMetadata() {

        //Default SQL query to fetch all sku
        $sql_query = "SELECT departments.dept_id, departments.dept_name,
            categories.category_id, categories.category_name,
            sub_categories.sub_category_id, sub_categories.sub_category_name
            FROM sub_categories, categories, departments
            WHERE sub_categories.category_id = categories.category_id
            AND categories.dept_id = departments.dept_id 
            ORDER BY departments.dept_id, categories.category_id, sub_categories.sub_category_id";

        if (!$this->conn) {
            return null;
        }

        //Prepare SQL statement
        $stmt = $this->conn->prepare($sql_query);
        if ( false === $stmt ) {
            return null;
        }

        //Execute SQL Query
        $result = $stmt->execute();

        //Save result set
        $metadata_array = $stmt->get_result();

        $stmt->close();

        //Return results
        return $metadata_array;
    }

}
