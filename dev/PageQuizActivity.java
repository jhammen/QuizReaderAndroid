/*
 This file is part of QuizReader.

 QuizReader is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 QuizReader is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with QuizReader.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.quizreader.android;

import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.quizreader.android.database.Definition;
import org.quizreader.android.database.QuizWord;
import org.quizreader.android.database.QuizWordDao;
import org.quizreader.android.database.Word;
import org.quizreader.android.database.WordDao;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

public class PageQuizActivity extends BaseQuizReadActivity {

	private TextView wordText;
	private RadioGroup radioGroup;
	private RadioButton[] radioButtons;
	private Button okButton;
	private ProgressBar progressBar;

	private WordDao wordDao;
	private QuizWordDao quizWordDao;
	private List<QuizWord> quizWords;
	private Random random;
	private QuizWord testWord;
	private int correctAnswerId;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.page_quiz);
		wordText = (TextView) findViewById(R.id.wordText);
		radioGroup = (RadioGroup) findViewById(R.id.radioGroup);
		okButton = (Button) findViewById(R.id.answerButton);
		progressBar = (ProgressBar) findViewById(R.id.progressBar);
		radioButtons = new RadioButton[3];
		radioButtons[0] = (RadioButton) findViewById(R.id.radioButton1);
		radioButtons[1] = (RadioButton) findViewById(R.id.radioButton2);
		radioButtons[2] = (RadioButton) findViewById(R.id.radioButton3);
		for (RadioButton radioButton : radioButtons) {
			radioButton.setEnabled(false);
		}

		wordDao = new WordDao(this);
		quizWordDao = new QuizWordDao(this);
		quizWordDao.open();
		quizWords = quizWordDao.getNewQuizWords(title.getId(), title.getSection(), title.getParagraph());
		// do we have enough?
		int minWords = 4;
		if (quizWords.size() < minWords) {
			int count = minWords - quizWords.size();
			List<QuizWord> moreQuizWords = quizWordDao.getMoreQuizWords(title.getId(), title.getSection(), title.getParagraph(), count);
			quizWords.addAll(moreQuizWords);
		}
		quizWordDao.close();
		if (quizWords.size() == 0) { // no words means nothing to do here
			setResult(RESULT_OK);
			finish();
		}
		else {
			progressBar.setMax(quizWords.size());
			random = new Random();
			showNextQuiz();
		}
	}

	private void showNextQuiz() {
		radioGroup.clearCheck();
		okButton.setEnabled(false);

		int index = random.nextInt(quizWords.size());
		testWord = quizWords.get(index);
		wordText.setText(testWord.getWord().getToken());

		int correctIndex = random.nextInt(3);
		correctAnswerId = radioButtons[correctIndex].getId();
		fillButton(testWord, correctIndex);

		quizWordDao.open();
		QuizWord unrelatedWord = quizWordDao.getRandomQuizWord(title.getId(), testWord.getId());
		QuizWord similarWord = findSimilarWord();
		quizWordDao.close();

		int order = random.nextInt(1);
		fillButton(order == 0 ? similarWord : unrelatedWord, nextEmptyIndex());
		fillButton(order == 0 ? unrelatedWord : similarWord, nextEmptyIndex());
	}

	private QuizWord findSimilarWord() {
		List<QuizWord> quizWordsLike = Collections.emptyList();
		int attempts = 0;
		while (quizWordsLike.size() == 0) {
			if (attempts++ > 10) {
				return quizWordDao.getRandomQuizWord(title.getId(), testWord.getId());
			}
			String token = testWord.getWord().getToken();
			int randIndex = token.length() > 1 ? random.nextInt(token.length() - 1) : 0;
			String like = token.charAt(0) + "%" + token.charAt(randIndex) + "%";
			quizWordsLike = quizWordDao.getQuizWordsLike(title.getId(), testWord.getId(), like);
		}
		int randomIndex = random.nextInt(quizWordsLike.size());
		return quizWordsLike.get(randomIndex);
	}

	private void fillButton(QuizWord quizWord, int index) {
		StringBuffer buff = new StringBuffer();
		List<Definition> defs = quizWord.getDefinitions();
		for (int i = 0; i < defs.size(); i++) {
			buff.append(defs.get(i).getText());
			if (i + 1 != defs.size()) {
				buff.append("; ");
			}
		}
		radioButtons[index].setText(buff.toString());
		radioButtons[index].setBackgroundColor(Color.BLACK);
		radioButtons[index].setEnabled(true);
	}

	private int nextEmptyIndex() {
		int i = 0;
		while (radioButtons[i].isEnabled()) {
			i++;
		}
		return i;
	}

	public void answer(View view) {

		// disable all radio buttons
		for (RadioButton radioButton : radioButtons) {
			radioButton.setEnabled(false);
		}

		// paint correct answer green
		RadioButton correctButton = (RadioButton) findViewById(correctAnswerId);
		correctButton.setBackgroundColor(Color.GREEN);
		correctButton.getBackground().setAlpha(90);
		int selectedId = radioGroup.getCheckedRadioButtonId();

		// correct answer was given
		if (selectedId == correctAnswerId) {
			// increment quiz level and remove from the current list
			Word word = testWord.getWord();
			word.setQuizLevel(word.getQuizLevel() + 1);
			wordDao.open();
			wordDao.update(word);
			wordDao.close();
			quizWords.remove(testWord);
			progressBar.incrementProgressBy(1);
			new Handler().postDelayed(new Runnable() {
				public void run() {
					okButton.performClick();
				}
			}, 1000);
		}
		else {
			RadioButton selectedButton = (RadioButton) findViewById(selectedId);
			selectedButton.setBackgroundColor(Color.RED);
			selectedButton.getBackground().setAlpha(90);
			// enable button for next
			okButton.setEnabled(true);
		}
	}

	public void kontinue(View view) {
		if (quizWords.size() > 0) {
			showNextQuiz();
		}
		else {
			setResult(RESULT_OK);
			finish();
		}
	}

}
