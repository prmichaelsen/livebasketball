import React from 'react';
import { ScrollView, StyleSheet } from 'react-native';
import { Table, Row, Rows } from 'react-native-table-component';
import { db } from '../navigation/RootNavigation';

export default class LogsScreen extends React.Component {
  static navigationOptions = {
    title: 'Logs',
  };

  state = {
    games: [],
    gamesData: {},
    tableData: [['', '', '', '']],
  }; 

  componentWillMount(){
    db.ref('games').on('value', (snapshot) => {
      var games = snapshot.val();
      this.setState({ 
        games: games ? Object.keys(games).map(id => { games[id].uuid = id; return games[id] }) : [],
        gamesData: games,
        tableData: games ? Object.keys(games).map( id => {
          var game = games[id];
          var data = 
          [
            game.homeTeam, 
            game.awayTeam,
            (game.conditionOneMet || game.conditionTwoMet) ? 'Yes' : 'No',
            ((game.roundStatus === "4th Quarter" || game.roundStatus === "Overtime") && parseInt(game.time) >= 12) ? 
              (game.awayScores.reduce((total, num) => total + num) > game.homeScores.reduce((total, num) => total + num)) ? 'Away Win' : 'Home Win'
              :
              'In Progress',
          ]
          return data;
        }) : [['', '', '', '']],
      });
    });
  }

  render() {
    return (
      <ScrollView style={styles.container}>
        <Table>
          <Row
            data={[
              'Home',
              'Away',
              'Match',
              'Outcome',
            ]}
          />
          <Rows
            data={
              this.state.tableData
            }
          />
          </Table>
      </ScrollView>
    );
  }
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    paddingTop: 15,
    backgroundColor: '#fff',
  },
});
