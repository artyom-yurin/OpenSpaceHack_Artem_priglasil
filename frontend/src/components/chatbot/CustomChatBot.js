import React, {Component} from "react";
import ChatBot from "react-simple-chatbot";
import {ThemeProvider} from "styled-components";

function requestAnswer(text) {
    try {
        const url = "http://127.0.0.1:8080/api/chat/v1/bot?question=" + text
        const request = require("sync-request") // sorry for not using axios
        return JSON.parse(request("GET", url).getBody('utf8')).answer
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

class CustomChatBot extends Component {
    constructor(props) {
        super(props);



        this.config = {
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
                component: <StepsDescription/>,
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
                id: "anythingElse",
                message: "Чем вам еще помочь?",
                trigger: "user_input"
            },
            {
                id: "error",
                message: "{previousValue}",
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

class StepsDescription extends Component {
    constructor(props) {
        super(props);

        this.requestResult = requestAnswer(this.props.steps.user_input.value);
        if (this.requestResult === "NetworkError") {
            this.props.triggerNextStep({
                value: "К сожалению, отсутствует подключение к базе знаний. Попробуйте позже",
                trigger: "error"
            })
            this.answer = ""
        } else if (this.requestResult === "UnexpectedError") {
            this.props.triggerNextStep({
                value: "К сожалению, возникла непредвиденная ошибка. Попробуйте позже",
                trigger: "error"
            })
            this.answer = ""
        } else {
            // this.requestResult = "1. На вкладке «Мои продукты» в разделе «Кредиты» выберите нужный ипотечный кредит и нажмите «Досрочное погашение»\n" +
            //     "2. На вкладке «Частичное» введите сумму и нажмите «Создать заявку».\n" +
            //     "3. Введите SMS-код для подтверждения\n" +
            //     "4. Заявка создана. Не забудьте пополнить счет погашения кредита на необходимую сумму до даты списания ежемесячного платежа"

            // this.requestResult = this.props.steps.user_input.value;

            this.answer = this.requestResult.split("\n");
            // this.proposedQuestion = this.answer.shift();
            this.answerSteps = this.answer;
        }
    }

    render() {
        if(this.answer==="") return "Кажется что-то пошло не так...";
        return (
            <div style={{width: '100%'}}>
                {/*<h3>{this.proposedQuestion}</h3>*/}
                {this.answerSteps.map((item) => (
                    <>
                        {item}<br/>
                    </>
                ))}
            </div>
        );
    };
}

export default CustomChatBot;