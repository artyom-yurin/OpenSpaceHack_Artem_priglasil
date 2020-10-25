import React, {Component} from 'react'
import {requestAnswer} from "./chatbot/CustomChatBot";

class AnswerComponent extends Component {
    constructor(props) {
        super(props);
        this.state = {
            isError: false,
            isAnswer: true,
            isUnknown: false
        }

        this.requestResult = requestAnswer(this.props.steps.user_input.value, this.props.chatId);
        if (this.requestResult === "NetworkError") {
            this.props.triggerNextStep({
                value: "К сожалению, отсутствует подключение к базе знаний. Попробуйте позже",
                trigger: "NetworkError"
            })
            this.state.isError = true;
        } else if (this.requestResult === "UnexpectedError") {
            this.props.triggerNextStep({
                value: "К сожалению, возникла непредвиденная ошибка. Попробуйте позже",
                trigger: "UnexpectedError"
            })
            this.state.isError = true;
        } else {
            // this.requestResult = "1. На вкладке «Мои продукты» в разделе «Кредиты» выберите нужный ипотечный кредит и нажмите «Досрочное погашение»\n" +
            //     "2. На вкладке «Частичное» введите сумму и нажмите «Создать заявку».\n" +
            //     "3. Введите SMS-код для подтверждения\n" +
            //     "4. Заявка создана. Не забудьте пополнить счет погашения кредита на необходимую сумму до даты списания ежемесячного платежа"

            // this.requestResult = this.props.steps.user_input.value;

            if (this.requestResult==="unknown") {
                this.props.triggerNextStep({
                    value: "Похоже ответа на ваш вопрос у нас нет! Попробуйте задать другой вопрос",
                    trigger: "fail"
                })
                this.state.isUnknown = true;
            }

            const regex = /Уточняющий вопрос: ([A-Яа-я ?]+): {([A-Яа-я ]+)}(, {([A-Яа-я ]+)})*/
            if (regex.test(this.requestResult)) {
                this.state.isAnswer = false;
            }

            this.answer = this.requestResult.split("\n");
            // this.proposedQuestion = this.answer.shift();
            this.answerSteps = this.answer;
        }
    }

    render() {
        if (this.state.isError) return "Кажется что-то пошло не так...";
        if (this.state.isUnknown) return;
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

export default AnswerComponent;