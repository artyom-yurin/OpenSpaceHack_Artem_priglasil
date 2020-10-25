import React, {Component, useState} from "react";
import './App.css';
import CustomChatBot from "./components/chatbot/CustomChatBot";
import API from "./api";

class App extends Component {
    constructor(props) {
        super(props);
        const [chatId, setChatId] = useState(0);

    }

    requestChatId() {
        try {
            const url = API + "/authorize"
            const request = require("sync-request"); // sorry for not using axios
            const token = JSON.parse(request("GET", url).getBody('utf8')).token
            this.setState({chatId: token});
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
