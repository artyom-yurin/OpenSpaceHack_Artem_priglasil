import React from "react";

function AnswerSuggestion(props) {

    const text = props.text

    return (
        <div class="answerSuggestion">
            {text}
        </div>
    );
}
export default AnswerSuggestion;