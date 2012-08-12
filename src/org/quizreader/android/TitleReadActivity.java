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

import org.quizreader.android.database.TitleDao;
import org.quizreader.android.qzz.QzzFile;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class TitleReadActivity extends BaseQuizReadActivity {

	private static final int REQUEST_TEACH = 0;
	private static final int REQUEST_QUIZ = 1;
	private static final int REQUEST_READ = 2;

	private TextView bigText;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.title_read);

		TextView titleText = (TextView) findViewById(R.id.titleText);
		titleText.setText(titleText.getText() + title.getName());

		bigText = (TextView) findViewById(R.id.bigText);
		bigText.setText(title.getFilepath());

	}

	public void quizRead(View view) {
		try {
			// if 1st paragraph of a new section then load words for that section
			if (title.getParagraph() == 1) {
				QzzFile qzzFile = new QzzFile(title.getFilepath());
				LoadDefinitionsTask loadDefsTask = new LoadDefinitionsTask(this, title) {
					@Override
					protected void onPostExecute(Integer result) {
						super.onPostExecute(result);
						teachWords();
					}
				};
				loadDefsTask.execute(qzzFile.getDefinitionReader(title.getSection()));
			}
			else {
				teachWords();
			}
		} catch (Exception e) {
			bigText.setText(e.getLocalizedMessage());
		}
	}

	private void teachWords() {
		Intent wordLearnIntent = new Intent(this, PageLearnActivity.class);
		wordLearnIntent.putExtra("titleId", title.getId());
		startActivityForResult(wordLearnIntent, REQUEST_TEACH);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			if (requestCode == REQUEST_TEACH) {
				Intent wordQuizIntent = new Intent(this, PageQuizActivity.class);
				wordQuizIntent.putExtra("titleId", title.getId());
				startActivityForResult(wordQuizIntent, REQUEST_QUIZ);
			}
			else if (requestCode == REQUEST_QUIZ) {
				Intent readIntent = new Intent(this, PageReadActivity.class);
				readIntent.putExtra("titleId", title.getId());
				startActivityForResult(readIntent, REQUEST_READ);
			}
			else if (requestCode == REQUEST_READ) {
				// update paragraph
				updateTitle(title.getSection(), title.getParagraph() + 1);
				// update view
				updateProgressView();
			}
		}
		else if (requestCode == REQUEST_READ && resultCode == RESULT_FIRST_USER) {
			// TODO: what if we hit the last section?
			updateTitle(title.getSection() + 1, 1);
			updateProgressView();
		}
	}

	private void updateProgressView() {
		bigText.setText("next section: " + title.getSection() + ", paragraph: " + title.getParagraph());
	}

	private void updateTitle(int section, int paragraph) {
		title.setSection(section);
		title.setParagraph(paragraph);
		TitleDao titleDao = new TitleDao(this);
		titleDao.open();
		titleDao.saveTitle(title);
		titleDao.close();
	}
}