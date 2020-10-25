import React, {Component} from "react";
import ChatBot from "react-simple-chatbot";
import {ThemeProvider} from "styled-components";
import API from "../../api";
import AnswerComponent from "../AnswerComponent";

export function requestAnswer(text, chatId) {
    try {
        const url = API() + "/bot?question=" + text
        const request = require("sync-request") // sorry for not using axios
        console.log("token:" + chatId)
        const data = request("GET", url, {
            headers: {
                'Authorization': chatId
            }
        }).getBody('utf8')

        console.log("data:" + data)
        return JSON.parse(data).answer;
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

export class CustomChatBot extends Component {
    constructor(props) {
        super(props);

        this.config = {
            width: window.innerWidth / 2 + "px",
            height: window.innerHeight * 0.9 + "px",
            floating: false,
            headerTitle: "Открытие-чат",
            botAvatar: "avatar.svg",
            bubbleStyle: {
                borderRadius: 10 + "px",
            }
            // floatingIcon: "question_icon.svg"
        };

        this.theme = {
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

        this.steps = [
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
                component: <AnswerComponent chatId={this.props.chatId}/>,
                asMessage: true,
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
                        trigger: "success"
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
                message: "Пожалуйста, попробуйте уточнить вопрос, избегая лишних слов",
                trigger: "user_input"
            },
            {
                id: "success",
                message: "Спасибо за обращение!",
                trigger: "anythingElse"
            },
            {
                id: "fail",
                message: "Похоже ответа на ваш вопрос у нас нет! Попробуйте задать другой вопрос",
                trigger: "anythingElse"
            },
            {
                id: "anythingElse",
                message: "Чем вам еще помочь?",
                trigger: "user_input"
            },
            {
                id: "NetworkError",
                message: "К сожалению, отсутствует подключение к базе знаний. Попробуйте позже",
                end: true
            },
            {
                id: "UnexpectedError",
                message: "К сожалению, возникла непредвиденная ошибка. Попробуйте позже",
                end: true
            }
        ];
    }

    render() {
        return (
            <ThemeProvider theme={this.theme}>
                <ChatBot steps={this.steps} {...this.config} />
            </ThemeProvider>
        );
    }
}