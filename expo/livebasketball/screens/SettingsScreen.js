import React from 'react';
import { ExpoConfigView } from '@expo/samples';
import {
  View,
  List,
  ListItem,
  StyleSheet,
  Platform,
  ScrollView,
  Text,
} from 'react-native'; 

import
{
  SettingsDividerShort,
  SettingsDividerLong,
  SettingsEditText,
  SettingsCategoryHeader,
  SettingsSwitch,
  SettingsPicker
} from 'react-native-settings-components';

import { db } from '../navigation/RootNavigation';

let cellStyle = { 
  flex: 1,
  alignSelf: 'stretch',
}

export default class SettingsScreen extends React.Component {
  static navigationOptions = {
    title: 'Settings',
  };

  state = {
    switchValue: false, 
  }

  componentWillMount() {
    db.ref('settings').on('value', (snapshot) => {
      var settings = snapshot.val();
      this.setState({ 
        switchValue: settings.defaultUser.enableLeaguesByDefault,
      });
    });
  }

  // onValueChange = (switchValue) => {
    // this.setState({ switchValue });
  // }

  onValueChange = (value) => { 
    db.ref('settings/' + 'defaultUser').set({
      enableLeaguesByDefault: value,
    })
  }

  renderRow(setting) {
    console.log(setting);
    return ( 
      <View key={setting.name} style={{ flex: 1, alignSelf: 'stretch'}}>
        <Text> Hi </Text>
        <View style={{ flex: 1, alignSelf: 'stretch' }}>
          <Text>{setting.name}</Text>
        </View>
        <View style={{ flex: 1, alignSelf: 'stretch' }}>
          <Text>{setting.value}</Text>
        </View>
      </View>
    );
  }

  render() {
    let data = [
      {
        name: 'Last Updated',
        value: 'None',
      }
    ];

    let test = (
        <ScrollView style={styles.container} contentContainerStyle={styles.contentContainer}>
          <View style={{ flex: 1, alignItems: 'center', justifyContent: 'center' }}>
            {data.map(datum => this.renderRow(datum))}
          </View>
        </ScrollView> 
    );
    return (
      <View style={styles.container}>
        <ScrollView style={{ flex: 1, backgroundColor: (Platform.OS === 'ios') ? colors.iosSettingsBackground : colors.white }}>

          <SettingsCategoryHeader title={'My Account'} textStyle={(Platform.OS === 'android') ? { color: colors.monza } : null} />

          <SettingsDividerLong android={false} />

          <SettingsSwitch
            title={'Allow Push Notifications'}
            onSaveValue={this.onValueChange}
            value={this.state.switchValue}
            thumbTintColor={(this.state.switchValue) ? colors.switchEnabled : colors.switchDisabled}
            disabled={false}
          />
        </ScrollView> 
      </View>
    );

  }
} 

const colors = {
  iosSettingsBackground: 'rgb(235,235,241)',
  white: '#FFFFFF',
  monza: '#C70039',
  switchEnabled: (Platform.OS === 'android') ? '#C70039' : null,
  switchDisabled: (Platform.OS === 'android') ? '#efeff3' : null,
  switchOnTintColor: (Platform.OS === 'android') ? 'rgba(199, 0, 57, 0.6)' : null,
  blueGem: '#27139A',
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#fff',
  },
  developmentModeText: {
    marginBottom: 20,
    color: 'rgba(0,0,0,0.4)',
    fontSize: 14,
    lineHeight: 19,
    textAlign: 'center',
  },
  contentContainer: {
    paddingTop: 30,
  },
  welcomeContainer: {
    alignItems: 'center',
    marginTop: 10,
    marginBottom: 20,
  },
  welcomeImage: {
    width: 100,
    height: 80,
    resizeMode: 'contain',
    marginTop: 3,
    marginLeft: -10,
  },
  getStartedContainer: {
    paddingTop: 20,
    alignItems: 'center',
    marginHorizontal: 10,
  },
  homeScreenFilename: {
    marginVertical: 7,
  },
  codeHighlightText: {
    color: 'rgba(96,100,109, 0.8)',
  },
  codeHighlightContainer: {
    backgroundColor: 'rgba(0,0,0,0.05)',
    borderRadius: 3,
    paddingHorizontal: 4,
  },
  getStartedText: {
    fontSize: 17,
    color: 'rgba(96,100,109, 1)',
    lineHeight: 24,
    textAlign: 'center',
  },
  tabBarInfoContainer: {
    position: 'absolute',
    bottom: 0,
    left: 0,
    right: 0,
    ...Platform.select({
      ios: {
        shadowColor: 'black',
        shadowOffset: { height: -3 },
        shadowOpacity: 0.1,
        shadowRadius: 3,
      },
      android: {
        elevation: 20,
      },
    }),
    alignItems: 'center',
    backgroundColor: '#fbfbfb',
    paddingVertical: 20,
  },
  tabBarInfoText: {
    fontSize: 17,
    color: 'rgba(96,100,109, 1)',
    textAlign: 'center',
  },
  navigationFilename: {
    marginTop: 5,
  },
  helpContainer: {
    marginTop: 15,
    alignItems: 'center',
  },
  helpLink: {
    paddingVertical: 15,
  },
  helpLinkText: {
    fontSize: 14,
    color: '#2e78b7',
  },
});
