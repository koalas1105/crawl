package crawl;
import java.io.IOException;
import java.net.URL;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * 下载一整个页面（包括html、css、img、js）
 */
public class DownloadOnePage {
	public static void main(String[] args) throws IOException, ClassNotFoundException {
		String url = "http://www.gov.cn/test/2008-07/01/content_1032304.htm";
		WrappedResource wrappedResource = new WrappedResource();
		wrappedResource.setName(DigestUtils.md5Hex(url));
		
		System.out.println(String.format("Fetching %s...", url));
		Document doc = Jsoup.parse(new URL(url), 30*1000);
		Elements media = doc.select("[src]");
		Elements imports = doc.select("link[href]");
		Elements links = doc.select("a[href]");

		Elements http_equivs = doc.select("meta[http-equiv]");
		for (Element http_equiv : http_equivs) {
			http_equiv.attr("content", "text/html; charset=gbk");
		}

		for (Element src : media) {
        	String href = src.attr("abs:src");
    		src.attr("src", DigestUtils.md5Hex(href));
    		saveContent(wrappedResource, href);
		}

		for (Element link : imports) {
			String href = link.attr("abs:href");
			link.attr("href", DigestUtils.md5Hex(href));
			saveContent(wrappedResource, href);
		}
		
		for (Element link : links) {
			link.attr("href", "#");
		}
		wrappedResource.put(wrappedResource.getName(), doc.toString().getBytes());
		wrappedResource.save("./WebContent");
		wrappedResource = WrappedResource.restore("./WebContent/" + WrappedResource.getFileName(DigestUtils.md5Hex(url)));
		System.out.println(wrappedResource.name);
	}

	private static void saveContent(WrappedResource wrappedResource, String href) throws HttpException,
			IOException {
		System.out.println("Fetching href:[" + href + "]");
		HttpClient client = new HttpClient();
		GetMethod method = new GetMethod(href);
		client.executeMethod(method);
		if (method.getStatusCode() != 200) {
			return;
		}
		byte[] data = method.getResponseBody();
		method.releaseConnection();
		wrappedResource.put(DigestUtils.md5Hex(href), data);
	}
}
