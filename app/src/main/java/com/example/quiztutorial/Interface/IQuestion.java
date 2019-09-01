package com.example.quiztutorial.Interface;

import com.example.quiztutorial.Model.CurrentQuestion;

public interface IQuestion {
    CurrentQuestion getSelectedAnswer();
    void showCorrectAnswer();
    void disableAnswers();
    void resetQuestion();

}
