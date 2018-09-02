var app = angular.module('spaApp', []);

    app.controller('contactsController', function ($scope, $http) {

        /**
         * Get contact list from Server
         */
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


        $scope.selected_contacts = [];

        /**
         * Pick the choosen contacts from HTML form
         */
        $scope.getSelectedContacts = function () {

            $scope.selected_contacts.length = 0;

            if ($scope.contacts.length > 0) {
                for (var i = 0; i < $scope.contacts.length; i++) {
                    if ($scope.contacts[i].Selected) {
                        $scope.selected_contacts.push($scope.contacts[i].Id);
                    }
                }
            }

        }; //getSelectedContacts

        /**
         * Pick the choosen contacts from HTML form
         */
        $scope.postSelectedContacts = function (operation) {

            $scope.getSelectedContacts();

            if ($scope.selected_contacts.length == 0) {
                alert("No contact selected");
            } else {
                var selected_ids = "Selected contacts:";
                for (var i = 0; i < $scope.selected_contacts.length; i++) {
                    selected_ids += "\nId: " + $scope.selected_contacts[i];
                }
                alert(selected_ids);

                if (operation === 'SELECT') {
                    //Post selected contacts to server
                } else if (operation === 'DELETE') {
                    //Post selected contacts to server to delete
                }
            }

        };

        /**
         * Post New Contact info to Server
         */
        $scope.submitAddContactForm = function() {
            var formData = {
                type: $("select[name='contact_type']").val(),
                name: $("input[name='contact_name']").val(),
                title: $("input[name='contact_title']").val(),
                phone: $("input[name='contact_phone']").val(),
                email: $("input[name='contact_email']").val()
            };

            var request = {
                method  : 'POST',
                url     : 'http://example.com/index.php',
                data    : formData,
                headers : { 'Content-Type': 'application/x-www-form-urlencoded' }
            };

            $http(request)
                .success(function(data) {
                    if (data.errors) {
                        //Handle errors
                    } else {
                        //Handle success
                    }
                });
        };

        /**
         * Override button click handler of "Add" button in New Contact dialog to dismiss the dialog
         */
        $(function() {

            $('#add_button').click( function(e) {

                 if (!$scope.add_contact_form.$valid) {
                    return;
                 }

                $('#modalDialog').modal('hide');

            });
        });
});
