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

import java.net.URL;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;
import org.quizreader.android.database.Definition;
import org.quizreader.android.database.DefinitionDao;
import org.quizreader.android.qzz.QzzFile;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

public class PageReadActivity extends BaseQuizReadActivity {

	public static final int RESULT_END_TITLE = RESULT_FIRST_USER;

	/** Called when the activity is first created. */
	@SuppressLint("SetJavaScriptEnabled")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.page_read);
		WebView webview = (WebView) findViewById(R.id.webView);
		webview.setWebViewClient(new QRWebViewClient());
		webview.setWebChromeClient(new WebChromeClient() {
			public boolean onConsoleMessage(ConsoleMessage cm) {
				Log.d("QuizReader", cm.message() + ", line " + cm.lineNumber() + " " + cm.sourceId());
				return true;
			}
		});
		WebSettings webSettings = webview.getSettings();
		webSettings.setJavaScriptEnabled(true);
		webview.addJavascriptInterface(new QuizReaderInterface(), "qr");
		try {
			QzzFile qzzFile = new QzzFile(title.getFilepath(), this);
			URL htmlURL = qzzFile.getHTML(title.getId(), title.getSection());
			if (htmlURL == null) { // beyond the last section
				setResult(RESULT_END_TITLE);
				finish();
			}
			String externalForm = htmlURL.toExternalForm();
			webview.loadUrl(externalForm + "?paragraph=" + title.getParagraph());
		} catch (Exception ex) {
			webview.loadData(ex.toString(), "text/plain", null);
		}
	}

	public class QuizReaderInterface {
		DefinitionDao defDao = new DefinitionDao(PageReadActivity.this);

		// @JavascriptInterface
		public void showDef(String word) {
			defDao.open();
			List<Definition> definitions = defDao.getDefinitions(title.getId(), word, title.getLanguage());
			defDao.close();
			for (Definition def : definitions) {
				Toast.makeText(PageReadActivity.this, def.getText(), Toast.LENGTH_SHORT).show();
			}
		}

		public int getQuizLevel(String word) {
			return (int) Math.floor(Math.random() * 12);
		}

		public String getEntry(String word) throws JSONException {
			defDao.open();
			List<Definition> definitions = defDao.getDefinitions(title.getId(), word, title.getLanguage());
			defDao.close();
			if (definitions.size() == 0) {
				return null;
			}
			Definition definition = definitions.get(0);
			JSONObject json = new JSONObject();
			json.put("def", definition.getText());
			return json.toString();
		}

		public void finish() {
			PageReadActivity.this.finish();
		}
	}

	private class QRWebViewClient extends WebViewClient {
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			if ("www.quizreader.org".equals(Uri.parse(url).getHost())) {
				return false;
			}
			// do nothing
			return true;
		}
	}
}
