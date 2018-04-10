import React from 'react';
import {
  ScrollView,
  StyleSheet,
  FlatList,
  View,
  Text,
  TouchableHighlight,
} from 'react-native';
import Modal from 'react-native-modal';
import { db } from '../navigation/RootNavigation';

export default class LogsScreen extends React.Component {
  static navigationOptions = {
    title: 'Logs',
  };

  state = {
    games: [],
    gamesData: {},
    selectedGameId: null,
    showGameModal: false,
  }; 

  componentWillMount(){
    db.ref('games').on('value', (snapshot) => {
      var games = snapshot.val();
      this.setState({ 
        // we'll map data that meets conditions one and two only
        games: games ? Object.keys(games).filter(id => {
          var game = games[id];
          // first we add their uuid to the object for later use
          game.uuid = id;
          var isMatch = (game.conditionOneMet || game.conditionTwoMet);
          if(!isMatch)
            return false; 
          var homeScore = game.homeScores.reduce((total, num) => total + num);
          var awayScore = game.awayScores.reduce((total, num) => total + num);
          var isHomeWin = homeScore > awayScore;
          var didHomeHaveLead = game.homeScores[0] > game.awayScores[0]; 
          game.winningTeam = isHomeWin ? game.homeTeam : game.awayTeam;
          game.didLeadingTeamWin = (isHomeWin && didHomeHaveLead) || (!isHomeWin && !didHomeHaveLead);
          game.homeScore = homeScore;
          game.awayScore = awayScore;
          game.isHomeWin = isHomeWin;
          return true;
        }).map(id => games[id]) : [],
        gamesData: games,
      });
    });
  } 

  renderRow = ({item}) => { 
    console.log('title', item.title);
    console.log('value', item.value);
    return (
      <View
        style={{
          flex: 1,
          paddingVertical: 2,
          flexDirection: 'row',
        }}>
        <Text
          style={{
            flex: 1,
            fontWeight: 'bold',
          }}
        >
          {item.title}
        </Text>
        <View
          style={{
            justifyContent: 'flex-end',
          }}
        >
          {item.render ? item.render(item) : <Text>{item.value}</Text>}
        </View>
      </View>
    );
  } 

  render() {
    let game = null;
    if(this.state.gamesData && this.state.selectedGameId) {
      game = this.state.gamesData[this.state.selectedGameId];
    }
    const toRender = [
      { title: 'League', key: 'leagueId' },
      { title: 'Winning Team', key: 'winningTeam' },
      { title: 'Home Team', key: 'homeTeam' },
      { title: 'Away Team', key: 'awayTeam' },
      { title: 'Home Team Scores', key: 'homeScores', render: g => 
        (
          <FlatList 
            style={{ flexDirection: 'row' }} 
            keyExtractor={(item, i) => `homeScore-${i}`}
            data={g.homeScores.map(score => String(score))}
            renderItem={({ item }) => (
              <Text style={{ flex: 1, paddingLeft: 10 }}>
                {item}
              </Text>
            )}
          />
        )
      },
      { title: 'Away Team Scores', key: 'awayScores', render: g => 
        (
          <FlatList 
            style={{ flexDirection: 'row' }}
            keyExtractor={(item, i) => `awayScore-${i}`}
            data={g.awayScores.map(score => String(score))}
            renderItem={({ item }) => (
              <Text style={{ flex: 1, paddingLeft: 10 }}>
                {item}
              </Text>
            )}
          />
        ) 
      },
    ];
    return (
      <View style={styles.container}>
        <Modal
          isVisible={this.state.showGameModal}
        >
          <View style={styles.modalContent}>
            <ScrollView style={{ flexGrow: 1}}>
              <View style={{ flex: 1 }}>
                <FlatList
                  data={game ? toRender.map(item => ({ ...game, ...item, value: game[item.key] })) : []}
                  renderItem={this.renderRow}
                  keyExtractor={(item) => item.key}
                />
              </View>
            </ScrollView>
            <TouchableHighlight
              onPress={() => this.setState({ showGameModal: false })}
            >
              <View style={styles.button}>
                <Text>Ok</Text>
              </View>
            </TouchableHighlight>
          </View>
        </Modal>
        <ScrollView style={styles.container} contentContainerStyle={styles.contentContainer}> 
            <View>
              <FlatList
                data={this.state.games}
                renderItem={({ item }) => {
                  return (
                    <TouchableHighlight
                      onPress={() => {
                        this.setState({
                          showGameModal: true,
                          selectedGameId: item.uuid,
                        })
                      }}
                    >
                      <View
                        style={{
                          paddingHorizontal: 25,
                          paddingVertical: 10,
                          flexDirection: 'row',
                        }}>
                        <Text 
                          style={{
                            fontSize: 18,
                            flex: 1,
                          }}
                        >
                          {item.winningTeam}
                        </Text>
                        <Text 
                          style={{
                            fontSize: 18,
                            justifyContent: 'flex-end',
                          }}
                        >
                          {item.didLeadingTeamWin ? 'Win' : 'Loss'}
                        </Text>
                      </View>
                    </TouchableHighlight>
                  );
                }}

                keyExtractor={(item) => item.uuid}
              />
            </View>
        </ScrollView> 
      </View>
    );
  }
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    paddingTop: 15,
    backgroundColor: '#fff',
  },
  button: {
    // backgroundColor: "lightblue",
    padding: 12,
    margin: 16,
    justifyContent: "center",
    alignItems: "center",
    borderRadius: 4,
    // borderColor: "rgba(0, 0, 0, 0.1)"
  },
  modalContent: {
    backgroundColor: "white",
    padding: 22,
    // justifyContent: "center",
    // alignItems: "center",
    borderRadius: 4,
    borderColor: "rgba(0, 0, 0, 0.1)",
  },
  bottomModal: {
    justifyContent: "flex-end",
    margin: 0
  }
});

