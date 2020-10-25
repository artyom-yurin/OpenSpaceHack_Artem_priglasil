import React from "react";
import './App.css';
import CustomChatBot from "./components/chatbot/CustomChatBot";
import AnswerSuggestion from "./components/AnswerSuggestion";

function App() {
    return (
        <div className="App">

            <CustomChatBot/>
            <div className="answerSuggestions">
            </div>
        </div>
    );
}

export default App;
