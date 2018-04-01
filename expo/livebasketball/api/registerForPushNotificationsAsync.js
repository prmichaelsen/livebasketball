import { Constants, Permissions, Notifications } from 'expo';

// Example server, implemented in Rails: https://git.io/vKHKv
import {
  FIREBASE_DB_URL,
} from 'react-native-dotenv';
const PUSH_ENDPOINT = `${FIREBASE_DB_URL}tokens`

export default (async function registerForPushNotificationsAsync() {
  // Remote notifications do not work in simulators, only on device

  if (!Constants.isDevice) {
    return;
  }

  // Android remote notification permissions are granted during the app
  // install, so this will only ask on iOS
  let { status } = await Permissions.askAsync(Permissions.NOTIFICATIONS);

  // Stop here if the user did not grant permissions
  if (status !== 'granted') {
    return;
  }

  // Get the token that uniquely identifies this device
  let token = await Notifications.getExpoPushTokenAsync();
  let tokenValue = token.substring(token.indexOf('[') + 1, token.indexOf(']'));

  // POST the token to our backend so we can use it to send pushes from there
  return fetch(`${PUSH_ENDPOINT}/${tokenValue}/.json`, {
    method: 'PUT',
    headers: {
      Accept: 'application/json',
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({
      ExponentPushToken: tokenValue,
    }),
  });
});
