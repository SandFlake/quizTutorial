package com.example.quiztutorial;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.example.quiztutorial.Adapter.AnswerSheetAdapter;
import com.example.quiztutorial.Adapter.AnswerSheetHelperAdapter;
import com.example.quiztutorial.Adapter.QuestionFragmentAdapter;
import com.example.quiztutorial.Common.Common;
import com.example.quiztutorial.Common.SpaceDecoration;
import com.example.quiztutorial.DBHelper.DBHelper;
import com.example.quiztutorial.Model.CurrentQuestion;
import com.example.quiztutorial.Model.Question;
import com.github.javiersantos.materialstyleddialogs.MaterialStyledDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import android.os.CountDownTimer;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.GravityCompat;
import androidx.appcompat.app.ActionBarDrawerToggle;

import android.view.MenuItem;

import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.gson.Gson;

import androidx.drawerlayout.widget.DrawerLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import android.view.Menu;
import android.widget.Button;
import android.widget.TextView;

import java.util.concurrent.TimeUnit;

public class QuestionActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final int CODE_GET_RESULT = 9999;
    int time_play = Common.TOTAL_TIME;
    boolean isAnswerModeView = false;
    boolean isDoItAgainMode = false;

    TextView txt_right_answer, txt_timer, txt_wrong_answer;

    //just added answer_sheet_helper --- this is where to deal with problem video 4 minute 36.07
    RecyclerView answer_sheet_view, answer_sheet_helper;
    AnswerSheetAdapter answerSheetAdapter;
    AnswerSheetHelperAdapter answerSheetHelperAdapter;

    ViewPager viewPager;
    TabLayout tabLayout;
    DrawerLayout drawer;


    @Override
    protected void onDestroy() {
        if (Common.countDownTimer != null) {
            Common.countDownTimer.cancel();
        }
        super.onDestroy();
    }

    BroadcastReceiver goToQuestionNum = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().toString().equals(Common.KEY_GO_TO_QUESTION)) {
                int question = intent.getIntExtra(Common.KEY_GO_TO_QUESTION, -1);
                if (question != -1)
                    viewPager.setCurrentItem(question); //go to question
                drawer.closeDrawer(Gravity.LEFT);
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(Common.selectedCategory.getName());
        setSupportActionBar(toolbar);

        isDoItAgainMode = false;
        drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View hView = navigationView.getHeaderView(0);
        answer_sheet_helper = hView.findViewById(R.id.answer_sheet);
        answer_sheet_helper.setHasFixedSize(true);
        answer_sheet_helper.setLayoutManager(new GridLayoutManager(this, 3));
        answer_sheet_helper.addItemDecoration(new SpaceDecoration(2));
        answerSheetHelperAdapter = new AnswerSheetHelperAdapter(this, Common.answerSheetList);
        answer_sheet_helper.setAdapter(answerSheetHelperAdapter);

        //nav_header_question.xml is the file where this button is
        Button btn_done = hView.findViewById(R.id.btn_done);
        btn_done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isAnswerModeView) {

                    new MaterialStyledDialog.Builder(QuestionActivity.this)
                            .setTitle("Finished?")
                            .setIcon(R.drawable.ic_sentiment_very_satisfied_pink_24dp)
                            .setDescription("Sure you want to finish?")
                            .setNegativeText("NO!")
                            .onNegative(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    dialog.dismiss();
                                }
                            })
                            .setPositiveText("Fo sho")
                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    dialog.dismiss();
                                    finishGame();
                                    drawer.closeDrawer(Gravity.LEFT);
                                }
                            }).show();
                } else
                    finishGame();
            }
        });


        Common.fragmentList.clear();
        Common.questionList.clear();
        Common.right_answer_count = 0;
        Common.wrong_answer_count = 0;

        //Take questions from DB
        takeQuestion();
    }

    private void setUpQuestion() {


        if (Common.questionList.size() > 0) {

            // in git hub almost same quiz
             //  txt_right_answer = (TextView)findViewById(R.id.txt_right_answer);
            txt_right_answer = (TextView) findViewById(R.id.txt_question_right);
            txt_timer = (TextView) findViewById(R.id.txt_timer);

            txt_timer.setVisibility(View.VISIBLE);
            txt_right_answer.setVisibility(View.VISIBLE);

            txt_right_answer.setText(new StringBuilder(String.format("%d/%d", Common.right_answer_count, Common.questionList.size())));
            countTimer();


            // View
            answer_sheet_view = (RecyclerView) findViewById(R.id.grid_answer);
            answer_sheet_view.setHasFixedSize(true);

            if (Common.questionList.size() > 5)
                answer_sheet_view.setLayoutManager(new GridLayoutManager(this, Common.questionList.size() / 2));

            answerSheetAdapter = new AnswerSheetAdapter(this, Common.answerSheetList);
            answer_sheet_view.setAdapter(answerSheetAdapter);


            viewPager = (ViewPager) findViewById(R.id.viewpager);
            tabLayout = (TabLayout) findViewById(R.id.sliding_tabs);

            genFragmentList();

            QuestionFragmentAdapter questionFragmentAdapter = new QuestionFragmentAdapter(getSupportFragmentManager(),
                    this,
                    Common.fragmentList);

            viewPager.setAdapter(questionFragmentAdapter);
            viewPager.setOffscreenPageLimit(Common.questionList.size());
            tabLayout.setupWithViewPager(viewPager);

            //Event
            viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

                int SCROLLING_RIGHT = 0;
                int SCROLLING_LEFT = 1;
                int SCROLLING_UNDETERMINED = 2;

                int currentScrollDirection = 2;

                private void setScrollingDirection(float positionOffset) {
                    if ((1 - positionOffset) >= 0.5) {
                        this.currentScrollDirection = SCROLLING_RIGHT;
                    } else if ((1 - positionOffset) <= 0.5) {
                        this.currentScrollDirection = SCROLLING_LEFT;
                    }
                }

                private boolean isScrollDirectionUndetermined() {
                    return currentScrollDirection == SCROLLING_UNDETERMINED;
                }

                private boolean isScrollingRight() {
                    return currentScrollDirection == SCROLLING_RIGHT;
                }

                private boolean isScrollingLeft() {
                    return currentScrollDirection == SCROLLING_LEFT;
                }

                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                    if (isScrollDirectionUndetermined()) {
                        setScrollingDirection(positionOffset);
                    }
                }

                @Override
                public void onPageSelected(int position) {
                    QuestionFragment questionFragment;
                    int page = 0;

                    if (position > 0) {
                        if (isScrollingRight()) {
                            questionFragment = Common.fragmentList.get(position - 1);
                            page = position - 1;
                        } else if (isScrollingLeft()) {
                            questionFragment = Common.fragmentList.get(position + 1);
                            page = position + 1;
                        } else {
                            questionFragment = Common.fragmentList.get(page);
                        }
                    } else {
                        questionFragment = Common.fragmentList.get(0);
                        page = 0;
                    }

                    CurrentQuestion question_state = questionFragment.getSelectedAnswer();
                  //  if (!isAnswerModeView) {
                        Common.answerSheetList.set(page, question_state); // Sets question answer for answer sheet
                        answerSheetAdapter.notifyDataSetChanged(); // Changes colour in answer sheet
                        answerSheetHelperAdapter.notifyDataSetChanged();

                        countCorrectAnswers();
                  //  }

                   /* if(isAnswerModeView){
                        questionFragment.showCorrectAnswer();
                        questionFragment.disableAnswers();
                    }

                    if(isDoItAgainMode){
                        questionFragment.resetQuestion();
                    }*/

                    txt_right_answer.setText(new StringBuilder(String.format("%d", Common.right_answer_count))
                            .append("/")
                            .append(String.format("%d", Common.questionList.size())).toString());

                    txt_wrong_answer.setText(String.valueOf(Common.wrong_answer_count));

                    if (question_state.getType() == Common.ANSWER_TYPE.NO_ANSWER) {
                        questionFragment.showCorrectAnswer();
                        questionFragment.disableAnswers();
                    }

                }

                @Override
                public void onPageScrollStateChanged(int state) {
                    if (state == ViewPager.SCROLL_STATE_IDLE) {
                        this.currentScrollDirection = SCROLLING_UNDETERMINED;
                    }

                }
            });

        }

    }

    private void finishGame() {
        int i = viewPager.getCurrentItem(); //Could be i or position, remember you do backwards from him
        QuestionFragment questionFragment = Common.fragmentList.get(i);
        CurrentQuestion question_state = questionFragment.getSelectedAnswer();

       if(!isAnswerModeView) {
            Common.answerSheetList.set(i, question_state);
            answerSheetAdapter.notifyDataSetChanged(); // changes colour in answer sheet
            answerSheetHelperAdapter.notifyDataSetChanged();
            Common.countDownTimer.cancel();

            countCorrectAnswers();

            txt_right_answer.setText(new StringBuilder(String.format("%d", Common.right_answer_count))
                    .append("/")
                    .append(String.format("%d", Common.questionList.size())).toString());

            txt_wrong_answer.setText(String.valueOf(Common.wrong_answer_count));
       }

        if(question_state!=null) {

            if (question_state.getType() == Common.ANSWER_TYPE.NO_ANSWER) {
                questionFragment.showCorrectAnswer();
                questionFragment.disableAnswers();
            }
        }

        //navigate to ResultActivity

        Intent intent = new Intent(QuestionActivity.this, ResultActivity.class);
        Common.timer = Common.TOTAL_TIME - time_play;
        Common.no_answer_count = Common.questionList.size() - (Common.wrong_answer_count + Common.right_answer_count);
        Common.data_question = new StringBuilder(new Gson().toJson(Common.answerSheetList));

        startActivityForResult(intent, CODE_GET_RESULT);

    }

    private void countCorrectAnswers() {
        // Reset the variable
        Common.right_answer_count = Common.wrong_answer_count = 0;

        for (CurrentQuestion item : Common.answerSheetList) {
           // if (item!=null) {
                if (item.getType() == Common.ANSWER_TYPE.RIGHT_ANSWER) {
                    Common.right_answer_count++;
                } else if (item.getType() == Common.ANSWER_TYPE.WRONG_ANSWER) {
                    Common.wrong_answer_count++;
                }
            }
        }
   // }

    private void genFragmentList() {
        for (int i = 0; i < Common.questionList.size(); i++) {
            Bundle bundle = new Bundle();
            bundle.putInt("index", i);
            QuestionFragment fragment = new QuestionFragment();
            fragment.setArguments(bundle);

            Common.fragmentList.add(fragment);
        }
    }

    private void countTimer() {
        if (Common.countDownTimer == null) {
            Common.countDownTimer = new CountDownTimer(Common.TOTAL_TIME, 1000) {
                @Override
                public void onTick(long l) {
                    txt_timer.setText(String.format("%02d:%02d",
                            TimeUnit.MILLISECONDS.toMinutes(l),
                            TimeUnit.MILLISECONDS.toSeconds(l) -
                                    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(l))
                    ));

                    time_play -= 1000;
                }

                @Override
                public void onFinish() {
                    finishGame();

                }
            }.start();
        } else {
            Common.countDownTimer.cancel();
            Common.countDownTimer = new CountDownTimer(Common.TOTAL_TIME, 1000) {
                @Override
                public void onTick(long l) {
                    txt_timer.setText(String.format("%02d:%02d",
                            TimeUnit.MILLISECONDS.toMinutes(l),
                            TimeUnit.MILLISECONDS.toSeconds(l) -
                                    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(l))
                    ));

                    time_play -= 1000;
                }

                @Override
                public void onFinish() {
                    finishGame();

                }
            }.start();

        }
    }

    private void takeQuestion() {
        //just added from git tutorial
        Common.questionList.clear();
        Common.selected_values.clear();

        Common.questionList = DBHelper.getInstance(this).getQuestionByCategory(Common.selectedCategory.getId());
        if (Common.questionList.size() == 0) {
            new MaterialStyledDialog.Builder(this)
                    .setTitle("Whoops")
                    .setIcon(R.drawable.ic_sentiment_very_dissatisfied_black_24dp)
                    .setDescription("No existing questions in " + Common.selectedCategory.getName())
                    .setPositiveText("Okay")
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            dialog.dismiss();
                            finish();
                        }
                    }).show();
        } else {

            if (Common.answerSheetList.size() > 0)
                Common.answerSheetList.clear();
            // Generate answer sheet item from question
            // 30 questions = 30 answers
            // 1 question = 1 answer sheet item

            for (int i = 0; i < Common.questionList.size(); i++) {
                Common.answerSheetList.add(new CurrentQuestion(i, Common.ANSWER_TYPE.NO_ANSWER));
            }
        } setUpQuestion();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }


    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem item = menu.findItem(R.id.menu_wrong_answer);
        ConstraintLayout constraintLayout = (ConstraintLayout) item.getActionView();
        txt_wrong_answer = (TextView) constraintLayout.findViewById(R.id.txt_wrong_answer);
        txt_wrong_answer.setText(String.valueOf(Common.wrong_answer_count));

        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.question, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_finish_game) {
            if (!isAnswerModeView) {
                new MaterialStyledDialog.Builder(this)
                        .setTitle("Finished?")
                        .setIcon(R.drawable.ic_sentiment_very_satisfied_pink_24dp)
                        .setDescription("Sure you want to finish?")
                        .setNegativeText("NO!")
                        .onNegative(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                dialog.dismiss();
                            }
                        })
                        .setPositiveText("Fo sho")
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                dialog.dismiss();
                                finishGame();
                            }
                        }).show();
            } else
                finishGame();
             return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            // Handle the camera action
            // in the video this is called nav_camera... hmmm
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_tools) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CODE_GET_RESULT) {
            if (resultCode == Activity.RESULT_OK) {
                String action = data.getStringExtra("action");
                if (action == null || TextUtils.isEmpty(action)) {
                    int questionNum = data.getIntExtra(Common.KEY_BACK_FROM_RESULT, -1);
                    viewPager.setCurrentItem(questionNum);

                    isAnswerModeView = true;
                    Common.countDownTimer.cancel();

                    int position = viewPager.getCurrentItem();
                    QuestionFragment questionFragment = Common.fragmentList.get(position);
                    CurrentQuestion question_state = questionFragment.getSelectedAnswer();


                    if(question_state!=null)
                    {
                        if (question_state.getType() == Common.ANSWER_TYPE.NO_ANSWER || question_state.getType() == Common.ANSWER_TYPE.WRONG_ANSWER)
                        {
                            questionFragment.showCorrectAnswer();
                            questionFragment.disableAnswers();
                        }
                    }
                    txt_wrong_answer.setVisibility(View.GONE);
                    txt_right_answer.setVisibility(View.GONE);
                    txt_timer.setVisibility(View.GONE);
                } else {
                    if (action.equals("viewquizanswer")) {
                        viewPager.setCurrentItem(0);

                        isAnswerModeView = true;
                        Common.countDownTimer.cancel();

                        txt_wrong_answer.setVisibility(View.GONE);
                        txt_right_answer.setVisibility(View.GONE);
                        txt_timer.setVisibility(View.GONE);

                        for (int i = 0; i < Common.fragmentList.size(); i++) {
                            Common.fragmentList.get(i).showCorrectAnswer();
                            //this is where we have a null pointer issue. Video 4 41.02
                            Common.fragmentList.get(i).disableAnswers();
                        }
                    } else if (action.equals("doitagain")) {
                        viewPager.setCurrentItem(0);
                        isDoItAgainMode = true;

                        isAnswerModeView = false;
                        Common.right_answer_count = 0;
                        Common.wrong_answer_count = 0;
                        Common.selected_values.clear();
                        countTimer();

                        txt_wrong_answer.setVisibility(View.VISIBLE);
                        txt_right_answer.setVisibility(View.VISIBLE);
                        txt_timer.setVisibility(View.VISIBLE);

                        for (CurrentQuestion item : Common.answerSheetList)
                            item.setType(Common.ANSWER_TYPE.NO_ANSWER); //reset all the questions
                        answerSheetAdapter.notifyDataSetChanged();
                        answerSheetHelperAdapter.notifyDataSetChanged();

                        for (int i = 0; i < Common.fragmentList.size(); i++)
                            Common.fragmentList.get(i).resetQuestion();
                    }

                }
            }
        }
    }
}
