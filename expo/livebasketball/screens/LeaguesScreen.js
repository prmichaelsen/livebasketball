import React from 'react';
import {
  Image,
  Platform,
  ScrollView,
  StyleSheet,
  Text,
  TouchableOpacity,
  View,
  FlatList,
  Button,
} from 'react-native';
import { WebBrowser } from 'expo';
import { MonoText } from '../components/StyledText';

import { db } from '../navigation/RootNavigation';

export default class LeaguesScreen extends React.Component {
  static navigationOptions = {
    title: 'Leagues',
  };

  componentWillMount() {
    db.ref('leagues').on('value', (snapshot) => {
      var leagues = snapshot.val();
      this.setState({ 
        leagues: leagues ? Object.keys(leagues).map(id => { leagues[id].uuid = id; return leagues[id] }) : [],
        leaguesData: leagues,
      });
    });
  }

  state = {
    leagues: [],
    leaguesData: {},
  };

  onAddLeague = (uuid) => { 
    db.ref('leagues/' + uuid).set({
      ...(this.state.leaguesData[uuid]),
      enabled: true,
    })
  }

  onRemoveLeague = (uuid) => {
    db.ref('leagues/' + uuid).set({
      ...(this.state.leaguesData[uuid]),
      enabled: false,
    }) 
  }

  render() {
    return (
      <View style={styles.container}>
      <Text> 
        Hi
      </Text>
        <ScrollView style={styles.container} contentContainerStyle={styles.contentContainer}>
          {/** 
          <View style={styles.welcomeContainer}>
            <Image
              source={
                __DEV__
                  ? require('../assets/images/robot-dev.png')
                  : require('../assets/images/robot-prod.png')
              }
              style={styles.welcomeImage}
            />
          </View>
         */} 

          <View>
            <FlatList
              data={this.state.leagues}
              renderItem={({ item }) => {
                return ( 
                  <View style={{ 
                    flexDirection: 'row',
                    paddingBottom: 20,
                  }}> 
                    {item.enabled ? 
                      <Button
                        onPress={() => this.onRemoveLeague(item.uuid)} 
                        style={{color:'red'}}
                        title="-"
                      >
                      </Button>
                      :
                      <Button
                        onPress={() => this.onAddLeague(item.uuid)}
                        title="+"
                      />
                    }
                    <Text style={{...styles.item, paddingTop: 10}}>{item.country}{item.name}</Text>
                  </View>
                )
            }}
              keyExtractor={(item) => item.uuid} 
            />

            {/*
            {this._maybeRenderDevelopmentModeWarning()}

            <Text style={styles.getStartedText}>Get started by opening</Text>

            <View style={[styles.codeHighlightContainer, styles.homeScreenFilename]}>
              <MonoText style={styles.codeHighlightText}>screens/HomeScreen.js</MonoText>
            </View>

            <Text style={styles.getStartedText}>
              Change this text and your app will automatically reload.
            </Text>
            */}
          </View>

          {/*}
          <View style={styles.helpContainer}>
            <TouchableOpacity onPress={this._handleHelpPress} style={styles.helpLink}>
              <Text style={styles.helpLinkText}>Help, it didn’t automatically reload!</Text>
            </TouchableOpacity>
          </View>
          */}
        </ScrollView>

        {/*
        <View style={styles.tabBarInfoContainer}>
          <Text style={styles.tabBarInfoText}>This is a tab bar. You can edit it in:</Text>

          <View style={[styles.codeHighlightContainer, styles.navigationFilename]}>
            <MonoText style={styles.codeHighlightText}>navigation/MainTabNavigator.js</MonoText>
          </View>
        </View>
          */}
      </View>
    );
  }

  _maybeRenderDevelopmentModeWarning() {
    if (__DEV__) {
      const learnMoreButton = (
        <Text onPress={this._handleLearnMorePress} style={styles.helpLinkText}>
          Learn more
        </Text>
      );

      return (
        <Text style={styles.developmentModeText}>
          Development mode is enabled, your app will be slower but you can use useful development
          tools. {learnMoreButton}
        </Text>
      );
    } else {
      return (
        <Text style={styles.developmentModeText}>
          You are not in development mode, your app will run at full speed.
        </Text>
      );
    }
  }

  _handleLearnMorePress = () => {
    WebBrowser.openBrowserAsync('https://docs.expo.io/versions/latest/guides/development-mode');
  };

  _handleHelpPress = () => {
    WebBrowser.openBrowserAsync(
      'https://docs.expo.io/versions/latest/guides/up-and-running.html#can-t-see-your-changes'
    );
  };
}

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
