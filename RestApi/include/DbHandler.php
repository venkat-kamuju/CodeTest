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
             return RESULT_CODE_SKU_ALREADY_EXISTS;
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
    public function getSKU($location, $dept, $category, $sub_category) {

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
            AND categories.dept_id = departments.dept_id 
            ORDER BY sku.sku_id";

        //Append additional clauses to sql query if specific records needs to be fetched.
        if (!is_null($location)) {
            $sql_query .= " AND locations.location_name LIKE ?";
        }

        if (!is_null($dept)) {
            $sql_query .= " AND departments.dept_name  LIKE ?";
        }

        if (!is_null($category)) {
            $sql_query .= " AND categories.category_name LIKE ?";
        }

        if (!is_null($sub_category)) {
            $sql_query .= " AND sub_categories.sub_category_name LIKE ?";
        }

        if (!$this->conn) {
            return null;
        }

        //Prepare SQL statement
        $stmt = $this->conn->prepare($sql_query);
        if ( false === $stmt ) {
            return null;
        }

        //Bind arugments to SQL Query
        if (!is_null($sub_category))
            $stmt->bind_param("ssss", $location, $dept, $category, $sub_category);
        elseif (!is_null($category))
            $stmt->bind_param("sss", $location, $dept, $category);
        elseif (!is_null($dept))
            $stmt->bind_param("ss", $location, $dept);
        elseif (!is_null($location))
            $stmt->bind_param("s", $location);

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
