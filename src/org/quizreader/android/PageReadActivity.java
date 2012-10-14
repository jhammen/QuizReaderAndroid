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

import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;

public class PageReadActivity extends BaseQuizReadActivity {

	public static final int RESULT_END_SECTION = RESULT_FIRST_USER;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.page_read);
		WebView webview = (WebView) findViewById(R.id.webView);
		try {
			QzzFile qzzFile = new QzzFile(title.getFilepath());
			String html = qzzFile.getHtml(title.getSection(), title.getParagraph());
			if (html.length() == 0) { // end of the file
				setResult(RESULT_END_SECTION);
				finish();
			}
			webview.loadDataWithBaseURL(null, html, "text/html", "utf-8", null);
		} catch (Exception ex) {
			webview.loadData(ex.getLocalizedMessage(), "text/plain", null);
		}
	}

	public void kontinue(View view) {
		// show next word
		setResult(RESULT_OK);
		finish();
	}
}
