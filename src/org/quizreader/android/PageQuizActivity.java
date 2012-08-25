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

import android.graphics.Color;
import android.os.Bundle;
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

		quizWordDao = new QuizWordDao(this);
		quizWordDao.open();
		quizWords = quizWordDao.getQuizWords(title.getId(), title.getSection(), title.getParagraph());
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
			int randIndex = random.nextInt(token.length() - 1);
			String like = token.charAt(0) + "%" + token.charAt(randIndex) + "%";
			quizWordsLike = quizWordDao.getQuizWordsLike(title.getId(), testWord.getId(), like);
		}
		int randomIndex = random.nextInt(quizWordsLike.size());
		return quizWordsLike.get(randomIndex);
	}

	private void fillButton(QuizWord quizWord, int i) {
		StringBuffer buff = new StringBuffer();
		for (Definition def : quizWord.getDefinitions()) {
			buff.append(def.getText() + "; ");
		}
		String defString = buff.toString().substring(0, buff.length() - 2);
		radioButtons[i].setText(defString);
		radioButtons[i].setBackgroundColor(Color.BLACK);
		radioButtons[i].setEnabled(true);
	}

	private int nextEmptyIndex() {
		int i = 0;
		while (radioButtons[i].isEnabled()) {
			i++;
		}
		return i;
	}

	public void answer(View view) {
		// paint correct answer green
		RadioButton correctButton = (RadioButton) findViewById(correctAnswerId);
		correctButton.setBackgroundColor(Color.GREEN);
		correctButton.getBackground().setAlpha(90);
		int selectedId = radioGroup.getCheckedRadioButtonId();
		if (selectedId == correctAnswerId) {
			quizWords.remove(testWord);
			progressBar.incrementProgressBy(1);
		}
		else {
			RadioButton selectedButton = (RadioButton) findViewById(selectedId);
			selectedButton.setBackgroundColor(Color.RED);
			selectedButton.getBackground().setAlpha(90);
		}
		for (RadioButton radioButton : radioButtons) {
			radioButton.setEnabled(false);
		}
		okButton.setEnabled(true);
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
