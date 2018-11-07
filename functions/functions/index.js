'use strict'
const functions = require('firebase-functions');
const admin = require('firebase-admin');

admin.initializeApp(functions.config().firebase);

exports.sendNotification =  functions.database.ref('/notifications/{uid}/{notification_id}').onWrite((data, context) => {

  const uid = context.params.uid;
  const notification_id = context.params.notification_id;

  console.log('Notification to: ', uid);

  const getDeviceToken = admin.database().ref(`/Users/${uid}/device_token`).once('value');
  return getDeviceToken.then(result => {

    const deviceToken = result.val();

    const payload = {
      notification: {
        title: "Friend request",
        body: "You have a friend request",
        icon: "default"
      }
    }

    return admin.messaging().sendToDevice( deviceToken, payload).then(response => {

      console.log("this is a Notification");
      return 0;

    });

  });

});
