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

import org.quizreader.android.qzz.QzzFile;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class TitleReadActivity extends BaseQuizReadActivity {

	private TextView bigText;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.title_read);

		TextView titleText = (TextView) findViewById(R.id.titleText);
		titleText.setText(titleText.getText() + title.getName());

		bigText = (TextView) findViewById(R.id.bigText);
		updateTitleView();
	}

	public void quizRead(View view) {
		try {
			// if before 1st paragraph of a new section then load words for that section
			if (title.getParagraph() == 0) {
				final QzzFile qzzFile = new QzzFile(title.getFilepath(), this);
				new LoadDefinitionsTask(this, title) {
					@Override
					protected void afterLoad(SQLiteDatabase db) { // within same transaction
						title.setParagraph(1); // ready to quizread first paragraph
						titleDao.open(db);
						titleDao.updateTitle(title);
						titleDao.close();
					};

					@Override
					protected void onPostExecute(Integer result) {
						super.onPostExecute(result);
						readTitle();
					}
				}.execute(qzzFile.getDefinitionReader(title.getSection()));
			}
			else {
				readTitle();
			}
		} catch (Exception e) {
			bigText.setText(e.getLocalizedMessage());
		}
	}

	private void readTitle() {
		Intent readIntent = new Intent(this, PageReadActivity.class);
		readIntent.putExtra("titleId", title.getId());
		startActivityForResult(readIntent, 0);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		// grab the title again
		fetchTitle();
		if (resultCode == RESULT_OK) {
			updateTitleView();
			backupProgress(); // too often?
		}
		else if (resultCode == PageReadActivity.RESULT_END_TITLE) { //
			// TODO: what if we hit the end of the last section?
			updateTitleView();
		}
	}

	private void updateTitleView() {
		int chapter = title.getSection() + 1; // sections are zero-based
		bigText.setText("chapter: " + chapter + ", paragraph: " + title.getParagraph());
	}

	private void backupProgress() {
		BackupWordsTask task = new BackupWordsTask(this) {
			@Override
			protected void onPostExecute(Integer result) {
				super.onPostExecute(result);
				// readTitle();
			}
		};
		task.execute((Void) null);
	}
}