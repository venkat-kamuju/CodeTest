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
require_once '../include/Config.php';
require '.././libs/Slim/Slim.php';

//Register Slim’s built-in autoloader
\Slim\Slim::registerAutoloader();

//Create Slim instance
$app = new \Slim\Slim();

/**
 * User Registration
 * url - /register
 * method - POST
 * params - name, email, password
 */
$app->post('/register', function() use ($app) {

            // check for required params
            verify_required_params(array('name', 'email', 'password'));

            $response = array();

            // reading post params
            $name = $app->request->post('name');
            $email = $app->request->post('email');
            $password = $app->request->post('password');

            // validating email address
            validateEmail($email);

            $db = new DbHandler();
            $result = $db->createUser($name, $email, $password);

            $response[RESULT_CODE_KEY] = $result;
            if ($result == RESULT_CODE_SUCCESS) {
                $response[RESULT_DESCRIPTION_KEY] = "User registered successfully";
            } else if ($result == RESULT_CODE_FAILURE) {
                $response[RESULT_DESCRIPTION_KEY] = "An error occurred while registering user";
            } else if ($result == RESULT_CODE_ALREADY_EXISTS) {
                $response[RESULT_DESCRIPTION_KEY] = "User already exist";
            }

            // echo json response
            echoRespnse(Constants::HTTP_CREATED, $response);
        });

/**
 * User Login
 * url - /login
 * method - POST
 * params - email, password
 */
$app->post('/login', function() use ($app) {

            // check for required params
            verify_required_params(array('email', 'password'));

            // reading post params
            $email = $app->request()->post('email');
            $password = $app->request()->post('password');
            $response = array();

            $db = new DbHandler();
            
            // check for correct email and password
            if ($db->checkLogin($email, $password)) {
            
                // get the user by email
                $user = $db->getUserByEmail($email);

                if ($user != NULL) {
                    $response[RESULT_CODE_KEY] = RESULT_CODE_SUCCESS;
                    $response['name'] = $user['name'];
                    $response['email'] = $user['email'];
                    $response['api_key'] = $user['api_key'];
                } else {
                    // unknown error occurred
                    $response[RESULT_CODE_KEY] = RESULT_CODE_FAILURE;
                    $response[RESULT_DESCRIPTION_KEY] = "An error occurred. Please try again";
                }
            } else {
                // user credentials are wrong
                $response[RESULT_CODE_KEY] = RESULT_CODE_FAILURE;
                $response[RESULT_DESCRIPTION_KEY] = 'Login failed. Incorrect credentials';
            }

            echoRespnse(Constants::HTTP_OK, $response);
        });

/**
 * Insert SKU
 * url - /insert
 * method - POST
 * params from body of HTTP request- sku_name, location_id, meta_info_id.
 * return - JSON array with result code and result description
 */
$app->post('/insert_sku', 'authenticate', 
    function() use ($app) {

        // check for required params
        verify_required_params(array('sku_name', 'location_id', 'meta_info_id'));

        $sku_name = $app->request()->post('sku_name');
        $location_id = $app->request()->post('location_id');
        $meta_info_id = $app->request()->post('meta_info_id');

        //Prepare output to return to the caller
        $response = array();

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
$app->get('/get_sku', 'authenticate', 
    function () use ($app)  {

        //Prepare a result array
        $response = array();

        //Read arguments from HTTP request
        $location_id = $app->request()->params('location_id');
        $dept_id = $app->request()->params('dept_id');
        $category_id = $app->request()->params('category_id');
        $sub_category_id = $app->request()->params('sub_category_id');

        //Create instance of database helper
        $db = new DbHandler();

        //Fetch SKU from database
        $result = $db->getSKU($location_id, $dept_id, $category_id, $sub_category_id);

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
$app->put('/update_sku/:sku_id', 'authenticate', 
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
$app->delete('/delete_sku/:sku_id', 'authenticate', 
    function($sku_id) use($app) {

        //Check for valid input
        if (!isset($sku_id) || strlen(trim($sku_id)) <= 0) {
            echoErrorRespnse();
        }

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
 * Fetch all locations.
 * method GET
 * url /get_locations
 * return - JSON array with locations
 */
$app->get('/get_locations',
    function () use ($app)  {

        //Prepare a result array
        $response = array();

        //Create instance of database helper
        $db = new DbHandler();

        //Fetch SKU from database
        $result = $db->getLocations();

        if (!is_null($result)) {

            $response[RESULT_CODE_KEY] = RESULT_CODE_SUCCESS;

            $response["location"] = array();

            // looping through result and preparing sku array
            while ($location = $result->fetch_assoc()) {
                $location_item = array();
                $location_item["location_id"] = $location["location_id"];
                $location_item["location_name"] = $location["location_name"];
                array_push($response["location"], $location_item);
            }

        } else {

            $response[RESULT_CODE_KEY] = RESULT_CODE_FAILURE;

        }

        echoRespnse(Constants::HTTP_OK, $response);
    }
);


/**
 * Fetch metadatalike department, category and sub-category.
 * method GET
 * url /get_metadata
 * return - JSON array with meta data
 */
$app->get('/get_metadata',
    function () use ($app)  {

        //Prepare a result array
        $response = array();

        //Create instance of database helper
        $db = new DbHandler();

        //Fetch SKU from database
        $result = $db->getMetadata();

        if (!is_null($result)) {

            $response[RESULT_CODE_KEY] = RESULT_CODE_SUCCESS;

            $response["metadata"] = array();

            // looping through result and preparing sku array
            while ($metadata = $result->fetch_assoc()) {
                $metadata_item = array();
                $metadata_item["dept_id"] = $metadata["dept_id"];
                $metadata_item["dept_name"] = $metadata["dept_name"];
                $metadata_item["category_id"] = $metadata["category_id"];
                $metadata_item["category_name"] = $metadata["category_name"];
                $metadata_item["sub_category_id"] = $metadata["sub_category_id"];
                $metadata_item["sub_category_name"] = $metadata["sub_category_name"];
                array_push($response["metadata"], $metadata_item);
            }

        } else {

            $response[RESULT_CODE_KEY] = RESULT_CODE_FAILURE;

        }

        echoRespnse(Constants::HTTP_OK, $response);
    }
);


/**
 * Authenticate every API request to process
 * Checking if the request has valid api key in the 'Authorization' header
 */
function authenticate(\Slim\Route $route) {

    // Getting request headers
    $headers = apache_request_headers();

    $response = array();

    $app = \Slim\Slim::getInstance();

    // Verifying Authorization Header
    if (isset($headers['Authorization'])) {

        $db = new DbHandler();

        // get the api key
        $api_key = $headers['Authorization'];

        // validating api key
        if (!$db->isValidApiKey($api_key)) {

            // api key is not present in users table
            $response[RESULT_CODE_KEY] = RESULT_CODE_FAILURE;
            $response[RESULT_DESCRIPTION_KEY] = 'Access Denied. Invalid Api key';
            echoRespnse(401, $response);
            $app->stop();

        } else {
            //global $user_id;
            //$user_id = $db->getUserId($api_key);
        }

    } else {
        // api key is missing in header
        $response[RESULT_CODE_KEY] = RESULT_CODE_FAILURE;
        $response[RESULT_DESCRIPTION_KEY] = 'Api key is misssing';
        echoRespnse(400, $response);
        $app->stop();
    }
}


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
    if ($_SERVER['REQUEST_METHOD'] == 'PUT') {
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
        $response = array();
        $app = \Slim\Slim::getInstance();
        $response[RESULT_CODE_KEY] = RESULT_CODE_FAILURE;
        $response[RESULT_DESCRIPTION_KEY] = 'Required field(s) ' . substr($error_fields, 0, -2) . ' is missing or empty';

        // echo error json and stop the app
        echoRespnse(400, $response);
        $app->stop();
    }
}

/**
 * Validating email address
 */
function validateEmail($email) {

    $app = \Slim\Slim::getInstance();

    if (!filter_var($email, FILTER_VALIDATE_EMAIL)) {
        $response[RESULT_CODE_KEY] = RESULT_CODE_FAILURE;
        $response[RESULT_DESCRIPTION_KEY] = 'Email address is not valid';
        $response["email"] = $email;
        echoRespnse(400, $response);
        $app->stop();
    }
}


// Run application
$app->run();

?>
