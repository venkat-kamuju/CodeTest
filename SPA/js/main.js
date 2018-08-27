angular.module('spaApp', [])
.controller('ContactsController', function($scope, $http) {
    $http.get('http://rest-service.com/greeting')
        .success(function(response) {
            //Handle successful response
            })
        .error(function(response) {
            //Handle failures
        });

        //Adding Dummy records
        $scope.ContactDetails =[
            {Id:"1", Type:"Official", Name:"Ann Brown", Title:"CEO", Phone:"(512) 456-5555", Email:"bann@gmail.com" },
            {Id:"2", Type:"Official", Name:"Mary Smith", Title:"CFO", Phone:"(402) 325-2112", Email:"msmith@yahoo.com" },
            {Id:"3", Type:"Official", Name:"John Doe", Title:"CTO", Phone:"(405) 135-5656", Email:"jdoe@hotmail.com" },
            {Id:"4", Type:"Personal", Name:"Antony Russo", Title:"Engineer", Phone:"(405) 105-6756", Email:"arusso@gmail.com" }];

});
