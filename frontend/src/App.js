import React, {Component} from "react";
import './App.css';
import {CustomChatBot} from "./components/chatbot/CustomChatBot";
import API from "./api";

class App extends Component {
    constructor(props) {
        super(props);
        this.state = {
            chatId: 1337
        }
        this.requestChatId()
    }

    requestChatId() {
        try {
            const url = API() + "/authorize"
            const request = require("sync-request"); // sorry for not using axios
            const data = request("GET", url).getBody('utf8')
            console.log("data:" + data)
            const token = JSON.parse(data).token
            console.log("token:" + token)
            this.state.chatId = token
        } catch (exception) {
            if (exception.name === 'NetworkError') {
                console.log(
                    "NetworkError occurred.\n" +
                    "Exception message: \n" +
                    exception.message + "\n" +
                    "Exception stacktrace: \n" +
                    exception.stack + "\n"
                );
                return "NetworkError"
            } else {
                console.log(
                    exception.name + " occurred.\n" +
                    "Exception message: \n" +
                    exception.message + "\n" +
                    "Exception stacktrace: \n" +
                    exception.stack + "\n"
                );
                return "UnexpectedError"
            }
        }
    }

    render() {
        return (
            <div className="App">
                <CustomChatBot chatId={this.state.chatId}/>
            </div>
        );
    }
}

export default App;
