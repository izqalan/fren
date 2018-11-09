'use strict'

const functions = require('firebase-functions');
const admin = require('firebase-admin');

admin.initializeApp(functions.config().firebase);

exports.sendNotification =  functions.database.ref('/notifications/{uid}/{notification_id}').onWrite((data, context) => {

  const uid = context.params.uid;
  const notification_id = context.params.notification_id;

  console.log('Notification to: ', uid);

  const fromUser = admin.database().ref(`/notifications/${uid}/${notification_id}`).once('value');
  return fromUser.then(fromUserResult =>{
    const from_user_id = fromUserResult.val().from;
    console.log("notification from: ", from_user_id);

    const userQuery = admin.database().ref(`/Users/${from_user_id}/name`).once('value');
    return userQuery.then(userResult =>{
      const username = userResult.val();

      const getDeviceToken = admin.database().ref(`/Users/${uid}/device_token`).once('value');
      return getDeviceToken.then(result => {
        const deviceToken = result.val();

        const payload = {
          notification: {
            title: "Friend request",
            body: `${username} wants to be your friend.`,
            click_action: "com.izqalan.messenger_TARGET_NOTIFICATION"
          },
          data:{
            from_user_id: from_user_id
          }
        }

        return admin.messaging().sendToDevice( deviceToken, payload).then(response => {
          console.log("Notification sent");
          return 0;
        });

    });

    });

  });


});
