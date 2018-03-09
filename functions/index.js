const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp(functions.config().firebase);

//finds new value in array 
Array.prototype.diff = function (a) {
    return this.filter(function (i) {
        return a.indexOf(i) === -1;
    });
};

exports.sendNotification = functions.firestore.document('/users/{userId}').onWrite(event => {
  	var flag = 1;
	var tkn = "tmp";
  
    var newData = event.data.data();

    var oldData = event.data.previous.data();

    //checks for new followings
  	var newDAL = 0;
  	var oldDAL = 0;
    try {	//try getting following list
      	var newDataArray = Object.keys(newData.following).map(function(key) {
        	return String(key);
    		})
        newDAL = newDataArray.length;
    }
  	catch (e) {
      	console.log("No following");
      	flag = 0; //dont check followers if theres no list
    }

  	try { //try getting previous following list
    	var oldDataArray = Object.keys(oldData.following).map(function(key) {
        	return String(key);
    		})
        oldDAL = oldDataArray.length;
    }
  	catch (e) { //if one didnt exist, set to 0
      	console.log("no followers previously, set to 0");
      	oldDAL = 0;
    }

    if ((newDAL > oldDAL) && flag) { //there is a new follower
        if (oldDAL == 0) {
          var result = newDataArray[0]; //get the only follower ID
        }
      	else {
          var result = newDataArray.diff(oldDataArray); //get that new followers ID
        }
        console.log("Person to recieve it: " + result);

        const senderId = event.params.userId; //get the sender ID
        console.log("senderID: ", senderId); 
        var senderName = newData.username; //get senders username
        console.log("username is: ", senderName);

        //query db to get receivers token
        var db = admin.firestore();
        const ref = db.doc('users/' + result);
      	ref.get().then(doc => {
      	if (doc.exists) {
          var fData = doc.data();
          tkn = fData.token;
          console.log("Token : ", tkn);
          
          const payload = { //set up new follower payload
          data: {
          	title: "You have a new follower!",
            message: senderName + " followed you",
            type: "follow",
            person: senderId,
          	}
      	  };
          
          //send the follower message
          return admin.messaging().sendToDevice(fData.token, payload)
                .then(function(response) {
                    console.log("Successfully sent message:", response);
                  })
                  .catch(function(error) {
                    console.log("Error sending message:", error);
                    return error;
                  });
          
          
        }
        }).catch(error => {
            console.log("Error getting document:", error)
        })
      
      	
    }
  
  	flag = 1;
  	//check for new bookmarks
  	var newBAL = 0;
  	var oldBAL = 0;
  	try { //try to get new list of bookmarks
      	var newBookArray = Object.keys(newData.bookmarkedRecipes).map(function(key) {
        	return String(key);
    		})
        newBAL = newBookArray.length;
        }
  	catch (e) {
      	console.log("No bookmarks");
      	flag = 0; //dont check bookmarks
    }

    try {
      	var oldBookArray = Object.keys(oldData.bookmarkedRecipes).map(function(key) {
        	return String(key);
    		})
        oldBAL = oldBookArray.length;
        }
  	catch (e) {
      	console.log("No prevous bookmarks");
      	oldBAL = 0; //set to 0 
    }
    
    if ((newBAL > oldBAL) && flag) { //there is a new bookmark
        if (oldBAL == 0) {
          var recipe = newBookArray[0]; //get the only bookmark ID
        }
      	else {
          var recipe = newBookArray.diff(oldBookArray); //get new bookmarks recipe ID
        }
        console.log("Recipe ID: " + recipe);

        var bsenderName = newData.username; //get senders username
        console.log("Person who bookmarked is: ", bsenderName);

        //query db to get recipe owners id and recipe name
      	var rOwner = "tmp";
      	var rName = "tmp";
        var bdb = admin.firestore();
        const bref = bdb.doc('recipes/' + recipe);
      	bref.get().then(bdoc => {
      	if (bdoc.exists) {
            var bData = bdoc.data();
            rOwner = bData.userId;
            rName = bData.title;
            console.log("Recipe owner ID is: ", rOwner);
            console.log("Recipe name is: ", rName);  

            const rref = bdb.doc('users/' + rOwner);
            rref.get().then(rdoc => {
            if (rdoc.exists) {
            var rData = rdoc.data();
            var rtkn = rData.token;
            console.log("Token : ", rtkn);
            
            const payloadB = { //set up new bookmark payload
            data: {
                title: "Somone bookmarked your recipe!",
                message: bsenderName + " bookmarked your recipe '" + rName + "'",
                type: "book",
                }
            };
            
            //send the bookmark message
            return admin.messaging().sendToDevice(rData.token, payloadB)
                    .then(function(response) {
                        console.log("Successfully sent message:", response);
                    })
                    .catch(function(error) {
                        console.log("Error sending message:", error);
                        return error;
                    });
            
            
            }
            }).catch(error => {
                console.log("Error getting document:", error)
            })


        }
        }).catch(error => {
            console.log("Error getting recipe owner ID:", error)
        })
      	
    }

  	/*const payload = {
        data: {
            title: "You have a new follower!",
            message: senderName + " followed you",
        }
    };*/
   
    return 0;

    
})