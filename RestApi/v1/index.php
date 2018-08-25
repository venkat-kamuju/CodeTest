<?php

/**
 * Rest API for CRUD operations on SKU.
 * This API uses Slim framework, Slim is a dispatcher that receives an HTTP request,
 * invokes an appropriate callback routine, and returns an HTTP response.
 *
 * @author Naga Venkat Kamuju
 */

//Include dependency classes
require_once '../include/DbHandler.php';
require_once '../include/Constants.php';
require '.././libs/Slim/Slim.php';

//Register Slim’s built-in autoloader
\Slim\Slim::registerAutoloader();

//Create Slim instance
$app = new \Slim\Slim();

/**
 * Insert SKU
 * url - /insert
 * method - POST
 * params from body of HTTP request- sku_name, location_id, meta_info_id.
 * return - JSON array with result code and result description
 */
$app->post('/insert_sku',
    function() use ($app) {

        //Prepare output to return to the caller
        $response = array();

        //Read input params from HTTP request
        $data = json_decode($app->request->getBody());
        $sku_name = $data->sku_name;
        $location_id = $data->location_id;
        $meta_info_id = $data->meta_info_id;

        //Create instance of Database helper
        $db = new DbHandler();

        //Insert SKU into database table.
        $result = $db->insertSKU($sku_name, $location_id, $meta_info_id);

        //Update output array with results
        $response[RESULT_CODE_KEY] = $result;
        if ($result == RESULT_CODE_SUCCESS) {
            $response[RESULT_DESCRIPTION_KEY] = "SKU successfully added";
        } else if ($result == RESULT_CODE_FAILURE) {
            $response[RESULT_DESCRIPTION_KEY] = "An error occurred while adding SKU";
        } else if ($result == RESULT_CODE_SKU_ALREADY_EXISTS) {
            $response[RESULT_DESCRIPTION_KEY] = "This sku already existed";
        }

        //Echo json response
        echoRespnse(Constants::HTTP_CREATED, $response);

    });


/**
 * Fetch SKU for the provided location, department, category and sub-category.
 * If no input is provided, all SKUs are returned.
 * method GET
 * url /get_sku
 * params from url- location, department, category and sub-category.
 * return - JSON array with result code and result description
 */
$app->get('/get_sku',
    function () use ($app)  {

        //Prepare a result array
        $response = array();

        //Read arguments from HTTP request
        $location = $app->request()->params('location');
        $dept = $app->request()->params('dept');
        $category = $app->request()->params('category');
        $sub_category = $app->request()->params('sub_category');

        //Create instance of database helper
        $db = new DbHandler();

        //Fetch SKU from database
        $result = $db->getSKU($location, $dept, $category, $sub_category);

        if (!is_null($result)) {

            $response[RESULT_CODE_KEY] = RESULT_CODE_SUCCESS;

            $response["sku"] = array();

            // looping through result and preparing sku array
            while ($sku = $result->fetch_assoc()) {
                $sku_item = array();
                $sku_item["sku_id"] = $sku["sku_id"];
                $sku_item["sku_name"] = $sku["sku_name"];
                $sku_item["location_id"] = $sku["location_id"];
                $sku_item["location_name"] = $sku["location_name"];
                $sku_item["dept_id"] = $sku["dept_id"];
                $sku_item["dept_name"] = $sku["dept_name"];
                $sku_item["category_id"] = $sku["category_id"];
                $sku_item["category_name"] = $sku["category_name"];
                $sku_item["sub_category_id"] = $sku["sub_category_id"];
                $sku_item["sub_category_name"] = $sku["sub_category_name"];
                array_push($response["sku"], $sku_item);
            }

        } else {

            $response[RESULT_CODE_KEY] = RESULT_CODE_FAILURE;

        }

        echoRespnse(Constants::HTTP_OK, $response);
    }
);

/**
 * Updating existing SKU
 * method PUT
 * params task, status
 * url - /update/:sku_id
 */
$app->put('/update_sku/:sku_id',
    function($sku_id) use($app) {

            //check for required params
            //verify_required_params(array('sku_name', 'location_id', 'meta_info_id'));

            //Read argument from HTTP request
            $sku_name = $app->request()->params('sku_name');
            $location_id = $app->request()->params('location_id');
            $meta_info_id = $app->request()->params('meta_info_id');

            $db = new DbHandler();
            $response = array();

            // updating task
            $result = $db->updateSKU($sku_id, $sku_name, $location_id, $meta_info_id);
            if ($result) {
                $response[RESULT_CODE_KEY] = RESULT_CODE_SUCCESS;
                $response[RESULT_DESCRIPTION_KEY] = "SKU updated successfully";
            } else {
                $response[RESULT_CODE_KEY] = RESULT_CODE_FAILURE;
                $response[RESULT_DESCRIPTION_KEY] = "SKU failed to update. Please try again!";
            }

            echoRespnse(Constants::HTTP_OK, $response);

        });

/**
 * Delete a SKU.
 * method DELETE
 * url /delete
 */
$app->delete('/delete_sku/:sku_id',
    function($sku_id) use($app) {

        $db = new DbHandler();

        $response = array();

        $result = $db->deleteSKU($sku_id);

        if ($result) {
            $response[RESULT_CODE_KEY] = RESULT_CODE_SUCCESS;
            $response[RESULT_DESCRIPTION_KEY] = "SKU deleted succesfully";
        } else {
            $response[RESULT_CODE_KEY] = RESULT_CODE_FAILURE;
            $response[RESULT_DESCRIPTION_KEY] = "SKU failed to delete. Please try again!";
        }

        echoRespnse(Constants::HTTP_OK, $response);
    });

/**
 * Echoing json response to client
 * @param String $status_code Http response code
 * @param Int $response Json response
 */
function echoRespnse($status_code, $response) {

    $app = \Slim\Slim::getInstance();

    // Http response code
    $app->status($status_code);

    // setting response content type to json
    $app->contentType('application/json');

    echo json_encode($response);
}

/**
 * Echoing json response to client
 * @param String $status_code Http response code
 * @param Int $response Json response
 */
function echoErrorRespnse() {

    $response = array();

    $app = \Slim\Slim::getInstance();

    $response[RESULT_CODE_KEY] = RESULT_CODE_FAILURE;
    $response[RESULT_DESCRIPTION_KEY] = 'Invalid input';

    echoRespnse(Constants::HTTP_BAD_REQUEST, $response);

    $app->stop();
}

/**
 * Verifying required params posted or not
 */
function verify_required_params($required_fields) {
    $error = false;
    $error_fields = "";
    $request_params = array();
    $request_params = $_REQUEST;

    // Handling PUT request params
    if ($_SERVER['REQUEST_METHOD'] == 'PUT' || $_SERVER['REQUEST_METHOD'] == 'POST') {
        $app = \Slim\Slim::getInstance();
        parse_str($app->request()->getBody(), $request_params);
    }

    foreach ($required_fields as $field) {
        if (!isset($request_params[$field]) || strlen(trim($request_params[$field])) <= 0) {
            $error = true;
            $error_fields .= $field . ', ';
        }
    }

    if ($error) {
        // Required field(s) are missing or empty
        // echo error json and stop the app
        $response = array();
        $app = \Slim\Slim::getInstance();
        $response["error"] = true;
        $response["message"] = 'Required field(s) ' . substr($error_fields, 0, -2) . ' is missing or empty';
        echoRespnse(400, $response);
        $app->stop();
    }
}

// Run application
$app->run();

?>
