/**
 * Sample React Native App
 * https://github.com/facebook/react-native
 * @flow
 */

import React, { Component } from 'react';
import {
  AppRegistry,
  StyleSheet,
  Text,
  View,
  TouchableHighlight
} from 'react-native';

import Alert from 'rnkit-alert-view';

class Button extends Component {
  render() {
    return (
      <TouchableHighlight
        onPress={() => this.props.onPress()}
        style={[styles.button, this.props.style]}
      >
        <Text style={styles.buttonText}>{this.props.text}</Text>
      </TouchableHighlight>
    )
  }
}

export default class Example extends Component {

  constructor (props) {
    super(props)
    this.state = {

    }
  }

  showAlert() {
    Alert.alert(
      'Alert Title',
      'alertMessage',
      [
        {text: 'Cancel', onPress: () => console.log('Cancel Pressed!'), style: 'cancel',},
        {text: 'Ok', onPress: () => console.log('OK Pressed!')},
        {text: 'Destructive', onPress: () => console.log('Destructive Pressed!'), style: 'destructive',},
        {text: 'Destructive123', onPress: () => console.log('Destructive Pressed!'), style: 'destructive',},
      ],
    );
  }

  showPrompt() {
    Alert.alert(
      'Alert Title',
      'alertMessage',
      [
        {text: 'Ok1', onPress: (text) => console.log('Ok Pressed!' + text)},
        {text: 'Ok2', onPress: (text) => console.log('Ok Pressed!' + text), style: 'cancel'},
        {text: 'Ok3', onPress: (text) => console.log('Ok Pressed!' + text)},
      ],
      'plain-text',
      '',
      2
    );
  }

  render() {
    return (
      <View style={styles.container}>

        <Button onPress={() => {this.showAlert()}} text={'Show AlertView'}/>
        <Button onPress={() => {this.showPrompt()}} text={'Show PromptView'}/>

        <Text style={styles.welcome}>
          Welcome to React Native!
        </Text>
        <Text style={styles.instructions}>
          To get started, edit index.android.js
        </Text>
        <Text style={styles.instructions}>
          Double tap R on your keyboard to reload,{'\n'}
          Shake or press menu button for dev menu
        </Text>
      </View>
    );
  }
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: '#F5FCFF',
  },
  welcome: {
    fontSize: 20,
    textAlign: 'center',
    margin: 10,
  },
  instructions: {
    textAlign: 'center',
    color: '#333333',
    marginBottom: 5,
  },
  button: {
    backgroundColor: 'rgb(255, 102, 1)',
    height: 40,
    padding: 10,
    margin: 20
  },
  buttonText: {
    color: 'white',
    fontSize: 14
  }
});
