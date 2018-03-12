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
    try {   //try getting following list
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
        console.log("No followers previously, set to 0");
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
        console.log("No previous bookmarks");
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
                title: "Someone bookmarked your recipe!",
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

exports.sendNotifRev = functions.firestore.document('/reviews/{reviewId}').onWrite(event => {
    console.log("Review Added");
    var reviewData = event.data.data();
    var tkn = "tmp";

    var reviewer = reviewData.author; //gets reviewers ID
    
    var recipeId = reviewData.recipeID; //gets recipes ID

    console.log("The reviewer is ", reviewer);
    console.log("The recipeId is ", recipeId);

    var db = admin.firestore();
    const ref = db.doc('users/' + reviewer); //gets the reviewers name
    ref.get().then(doc => {
        if (doc.exists) {
            var data = doc.data();
            var reviewerName = data.username;
            console.log("Reviewer is: ", reviewerName);

            const rref = db.doc('recipes/' + recipeId); //gets the recipe
            rref.get().then(rdoc => {
                if (rdoc.exists) {
                var rdata = rdoc.data();
                var recipeName = rdata.title; //gets recipe name
                console.log("Recipe name is: ", recipeName);
                var recipeOwnerID = rdata.userId; //gets recipe owner ID
                console.log("The id of the owner is: ", recipeOwnerID);

                const uref = db.doc('users/' + recipeOwnerID); //gets the recipe owner data
                uref.get().then(udoc => {
                    if (udoc.exists) {
                    var udata = udoc.data();
                    var recipeOwnertoken = udata.token;
                    console.log("Token is: ", recipeOwnertoken);
                    
                    const payload = { //set up new review payload
                        data: {
                            title: "There is a new review of your recipe!",
                            message: reviewerName + " reviewed your recipe '" + recipeName + "'",
                            type: "rev",
                            recipe: recipeId,
                            }
                        };

                //send the review message
                    return admin.messaging().sendToDevice(udata.token, payload)
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
                    console.log("Error getting document:", error)
                })

        }
        }).catch(error => {
            console.log("Error getting document:", error)
        })

    return 0;
})

/*const payload = { //set up new follower payload
          data: {
            title: "There is a new review of your recipe!",
            message: " reviewer your recipe '",
            type: "book",
            recipe: recipeId,
            }
          };*/

//send the follower message
          /*return admin.messaging().sendToDevice(data.token, payload)
                .then(function(response) {
                    console.log("Successfully sent message:", response);
                  })
                  .catch(function(error) {
                    console.log("Error sending message:", error);
                    return error;
                  });
          
          */

