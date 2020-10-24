import React from "react";
import ChatBot from "react-simple-chatbot";
import {ThemeProvider} from "styled-components";

function eventHandler(text) {
    var url = "http://127.0.0.1:8080/api/chat/v1/bot?question=" + text
    var request = require("sync-request")
    return JSON.parse(request("GET", url).getBody('utf8')).answer
}

function CustomChatBot(props) {
    const config = {
        width: window.innerWidth / 2 + "px",
        height: window.innerHeight*0.9 + "px",
        floating: false,
        headerTitle: "Открытие-чат",
        botAvatar: "avatar.svg",
        bubbleStyle:{
            borderRadius:10+"px",
        }
        // floatingIcon: "question_icon.svg"
    };

    const theme = {
        background: "white",
        fontFamily: "Arial, Helvetica, sans-serif",
        botDelay: 100,
        headerBgColor: "#0BA2D0",
        headerFontColor: "#fff",
        headerFontSize: "25px",
        botBubbleColor: "#02BAE8",
        botFontColor: "#fff",
        userBubbleColor: "#fff",
        userFontColor: "#4c4c4c",

    };


    const steps = [
        {
            id: "start",
            message: "Здравствуйте! Чем вам помочь?",
            trigger: "user_input"
        },
        {
            id: "user_input",
            user: true,
            trigger: "suggest"
        },
        {
            id: "suggest",
            message: ({previousValue, steps}) => {
                var t = eventHandler(previousValue)
                return t
                // return previousValue;
            },
            trigger: "satisfaction_question"
        },
        {
            id: "satisfaction_question",
            message: "Получилось ли ответить на ваш вопрос?",
            trigger: "satisfaction"
        },
        {
            id: "satisfaction",
            options: [
                {
                    value: true,
                    label: "Да",
                    trigger: "Done"
                },
                {
                    value: false,
                    label: "Нет",
                    trigger: "clarification"
                }
            ],
        },
        {
            id: "clarification",
            message: "Пожалуйста, попробуйте уточнить вопрос",
            trigger: "user_input"
        },
        {
            id: "Done",
            message: "Спасибо за обращение!",
            end: true
        }
    ];
    return (
        <ThemeProvider theme={theme}>
            <ChatBot steps={steps} {...config} />
        </ThemeProvider>
    );
}

export default CustomChatBot;