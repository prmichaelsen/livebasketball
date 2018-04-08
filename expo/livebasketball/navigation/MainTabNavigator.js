import React from 'react';
import { Platform } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { TabNavigator, TabBarBottom } from 'react-navigation';

import Colors from '../constants/Colors';

import LeaguesScreen from '../screens/LeaguesScreen';
import LogsScreen from '../screens/LogsScreen';
import SettingsScreen from '../screens/SettingsScreen';

export default TabNavigator(
  {
    Leagues: {
      screen: LeaguesScreen,
    },
    Logs: {
      screen: LogsScreen,
    },
    Settings: {
      screen: SettingsScreen,
    },
  },
  {
    navigationOptions: ({ navigation }) => ({
      tabBarIcon: ({ focused }) => {
        const { routeName } = navigation.state;
        let iconName;
        switch (routeName) {
          case 'Leagues':
            iconName =
              Platform.OS === 'ios'
                ? `ios-basketball${focused ? '' : '-outline'}`
                : 'md-basketball';
            break;
          case 'Logs':
            iconName = Platform.OS === 'ios' ? `ios-list-box${focused ? '' : '-outline'}` : 'md-list-box';
            break;
          case 'Settings':
            iconName =
              Platform.OS === 'ios' ? `ios-settings${focused ? '' : '-outline'}` : 'md-options';
        }
        return (
          <Ionicons
            name={iconName}
            size={28}
            style={{ marginBottom: -3, width: 25 }}
            color={focused ? Colors.tabIconSelected : Colors.tabIconDefault}
          />
        );
      },
    }),
    tabBarComponent: TabBarBottom,
    tabBarPosition: 'bottom',
    animationEnabled: false,
    swipeEnabled: false,
  }
);
