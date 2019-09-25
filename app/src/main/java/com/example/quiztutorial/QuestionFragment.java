package com.example.quiztutorial;


import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.Image;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.quiztutorial.Common.Common;
import com.example.quiztutorial.Interface.IQuestion;
import com.example.quiztutorial.Model.CurrentQuestion;
import com.example.quiztutorial.Model.Question;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import static androidx.constraintlayout.widget.Constraints.TAG;


/**
 * A simple {@link Fragment} subclass.
 */
public class QuestionFragment extends Fragment implements IQuestion {

    TextView txt_question_text;
    CheckBox ckbA, ckbB, ckbC, ckbD;
    FrameLayout layout_image;
    ProgressBar progressBar;

    Question question;
    int questionIndex=-1;
    Context context;



    public QuestionFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate layout for this fragment
        View itemView =  inflater.inflate(R.layout.fragment_question, container, false);

        // Get question
        questionIndex =  getArguments().getInt("index", -1);

        if (Common.questionList.size() > 0) {
            question = Common.questionList.get(questionIndex);
        }

        if (question != null ) {

            layout_image =  itemView.findViewById(R.id.layout_image);
            progressBar = itemView.findViewById(R.id.progress_bar);

            if (question.getIsQuestionImage()) {
                ImageView img_question = itemView.findViewById(R.id.img_question);
                Picasso.get().load(question.getQuestionImage()).into(img_question, new Callback() {
                    @Override
                    public void onSuccess() {
                        progressBar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onError(Exception e) {
                        Toast.makeText(getContext(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });
            }

            else  {
                layout_image.setVisibility(View.GONE);
            }
            txt_question_text = itemView.findViewById(R.id.txt_question_text);
            txt_question_text.setText(question.getQuestionText());

            ckbA = itemView.findViewById(R.id.ckbA);
            ckbA.setText(question.getAnswerA());
            ckbA.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    if(b) {
                        Common.selected_values.add(ckbA.getText().toString());
                        Log.d(TAG, "onCheckedChanged: can find A when changed ckbA = "
                        + ckbA.getText().toString());
                    }
                    else {
                        Common.selected_values.remove(ckbA.getText().toString());
                    }

                }
            });


            ckbB =  itemView.findViewById(R.id.ckbB);
            ckbB.setText(question.getAnswerB());
            ckbB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    if(b) {
                        Common.selected_values.add(ckbB.getText().toString());
                        Log.d(TAG, "onCheckedChanged: ckbB answer = "
                                + ckbB.getText().toString());
                    }
                    else {
                        Common.selected_values.remove(ckbB.getText().toString());
                        Log.d(TAG, "onCheckedChanged: can find and remove b when else statement");
                    }

                }
            });

            ckbC = itemView.findViewById(R.id.ckbC);
            ckbC.setText(question.getAnswerC());
            ckbC.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    if(b) {
                        Common.selected_values.add(ckbC.getText().toString());
                        Log.d(TAG, "onCheckedChanged: getting to check box C = " +
                                ckbC.getText().toString());
                    }
                    else {
                        Common.selected_values.remove(ckbC.getText().toString());
                    }

                }
            });

            ckbD = itemView.findViewById(R.id.ckbD);
            ckbD.setText(question.getAnswerD());
            ckbD.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    if(b) {
                        Common.selected_values.add(ckbD.getText().toString());
                        Log.d(TAG, "onCheckedChanged: ckb D found = " +
                                ckbD.getText().toString());
                    }
                    else {
                        Common.selected_values.remove(ckbD.getText().toString());
                    }

                }
            });


        }

        return itemView;
    }

    @Override
    public CurrentQuestion getSelectedAnswer() {

        // Should return the state of the question
        // So right, wrong or unanswered

        CurrentQuestion currentQuestion = new CurrentQuestion(questionIndex, Common.ANSWER_TYPE.NO_ANSWER); // No answer is default
        StringBuilder result = new StringBuilder();
        if (Common.selected_values.size() > 1) {
            // if more than one answer option is ticked
            Object[] arrayAnswer = Common.selected_values.toArray();
            for (int i = 0; i < arrayAnswer.length; i++)
                if (i < arrayAnswer.length - 1) {
                    result.append(new StringBuilder(((String)arrayAnswer[i]).substring(0, 1)).append(","));
                    Log.d(TAG, "getSelectedAnswer Multiple: " + result);
                } else {
                    result.append(new StringBuilder((String)arrayAnswer[i]).substring(0, 1));
                    Log.d(TAG, "getSelectedAnswer: multiple else " + result);
                }
        }

        else if (Common.selected_values.size() == 1) {
            // If only option is picked
                    Object[] arrayAnswer = Common.selected_values.toArray();
                    result.append(((String)arrayAnswer[0]).substring(0,1));
            Log.d(TAG, "getSelectedAnswer: One choice " + result);
        }

        if (question != null ){
            // compare correctAnswer with users answer
            if (!(TextUtils.isEmpty(result))) {
                if(result.toString().equalsIgnoreCase(question.getCorrectAnswer())) {
                    currentQuestion.setType(Common.ANSWER_TYPE.RIGHT_ANSWER);
                    Log.d(TAG, "getSelectedAnswer: user chose the correct answer " +
                            result);
                }
                else {
                    currentQuestion.setType(Common.ANSWER_TYPE.WRONG_ANSWER);
                    Log.d(TAG, "getSelectedAnswer: user chose the wrong answer "
                    + result.toString());
                }

            }
            else {
                currentQuestion.setType(Common.ANSWER_TYPE.NO_ANSWER);
            }
        }

        else {
            Toast.makeText(getContext(), "Can't reach question", Toast.LENGTH_SHORT).show();
            currentQuestion.setType(Common.ANSWER_TYPE.NO_ANSWER);
        }

        Common.selected_values.clear(); // when comparison is finished, clear the selected values
        return currentQuestion;
    }

    @Override
    public void showCorrectAnswer() {

            String[] correctAnswer = question.getCorrectAnswer().split(",");

        for(String answer:correctAnswer) {
            if (answer.equals("A")) {
                ckbA.setTypeface(null, Typeface.BOLD);
                ckbA.setTextColor(Color.GREEN);
                Log.d(TAG, "showCorrectAnswer: answer is A orange");
            } else if (answer.equals("B")) {
                ckbB.setTypeface(null, Typeface.BOLD);
                ckbB.setTextColor(Color.GREEN);
                Log.d(TAG, "showCorrectAnswer: answer is b orange");
            } else if (answer.equals("C")) {
                ckbC.setTypeface(null, Typeface.BOLD);
                ckbC.setTextColor(Color.GREEN);
                Log.d(TAG, "showCorrectAnswer: answer is C orange");
            } else if (answer.equals("D")) {
                ckbD.setTypeface(null, Typeface.BOLD);
                ckbD.setTextColor(Color.GREEN);
                Log.d(TAG, "showCorrectAnswer: answer is D orange");
            }

        }

    }

    @Override
    public void disableAnswers() {
        ckbA.setEnabled(false);
        ckbB.setEnabled(false);
        ckbC.setEnabled(false);
        ckbD.setEnabled(false);
    }

    @Override
    public void resetQuestion() {
        ckbA.setEnabled(true);
        ckbB.setEnabled(true);
        ckbC.setEnabled(true);
        ckbD.setEnabled(true);

        ckbA.setChecked(false);
        ckbB.setChecked(false);
        ckbC.setChecked(false);
        ckbD.setChecked(false);

        ckbA.setTypeface(null, Typeface.NORMAL);
        ckbA.setTextColor(Color.BLACK);
        ckbB.setTypeface(null, Typeface.NORMAL);
        ckbB.setTextColor(Color.BLACK);
        ckbC.setTypeface(null, Typeface.NORMAL);
        ckbC.setTextColor(Color.BLACK);
        ckbD.setTypeface(null, Typeface.NORMAL);
        ckbD.setTextColor(Color.BLACK);

    }
}
