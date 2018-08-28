var app = angular.module('spaApp', []);
    app.controller('contactsController', function ($scope, $http) {

        $http.get('http://rest-service.com/greeting')
            .success(function(response) {
                //Handle successful response
                })
            .error(function(response) {
                //Handle failures
            });

        //Adding Dummy records
        $scope.contacts =[
            {Id:"1", Type:"Official", Name:"Ann Brown", Title:"CEO", Phone:"(512) 456-5555", Email:"bann@gmail.com", Selected: false},
            {Id:"2", Type:"Official", Name:"Mary Smith", Title:"CFO", Phone:"(402) 325-2112", Email:"msmith@yahoo.com", Selected: false },
            {Id:"3", Type:"Official", Name:"John Doe", Title:"CTO", Phone:"(405) 135-5656", Email:"jdoe@hotmail.com", Selected: false },
            {Id:"4", Type:"Personal", Name:"Antony Russo", Title:"Engineer", Phone:"(405) 105-6756", Email:"arusso@gmail.com", Selected: false }];

        //Return selected contacts
        $scope.getSelectedContacts = function (msg) {
            
            var selected = false;
            if ($scope.contacts.length > 0) {
                $scope.selected_contacts = msg;
                for (var i = 0; i < $scope.contacts.length; i++) {
                    if ($scope.contacts[i].Selected) {
                        var id = $scope.contacts[i].Id;
                        var name = $scope.contacts[i].Name;
                        $scope.selected_contacts += "\nContact Id: " + id + ", Name: " + name;
                        selected = true;
                    }
                }
            } 
            
            if (!selected) {
                $scope.selected_contacts = "No Contact Selected";
            }

            alert($scope.selected_contacts);
        }
        
        $scope.ph_numbr = /^\+?\d{10}$/;
        
        $scope.eml_add = /^[^\s@]+@[^\s@]+\.[^\s@]{2,}$/;

});
