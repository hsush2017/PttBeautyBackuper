package choYM.Util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class HtmlUtil {
	public static Document loadHtml(String url) throws MalformedURLException, IOException {
		// open connection
		HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();

		// set HTTP header
		con.setRequestMethod("GET");
		con.setRequestProperty("User-Agent", "Mozilla/5.0");

		// if response is 200, extract html data, else return null
		int responseCode = con.getResponseCode();
		if (responseCode == 200) {
			StringBuilder strBuilder = new StringBuilder();
			BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"));
			String line = null;
			while ((line = br.readLine()) != null) {
				strBuilder.append(line);
			}
			br.close();

			return Jsoup.parse(strBuilder.toString());
		} else {
			System.out.println("ERROR: " + responseCode);

			return null;
		}
	}
}
