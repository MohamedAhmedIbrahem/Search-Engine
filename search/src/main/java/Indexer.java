import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Indexer {
    private Connection connection;
    private Document doc;
    private ArrayList<ArrayList<String>> listOfWords;
    private int totalNumberOfWords;

    public static void main(String[] args) throws InterruptedException {
        IndexerDbAdapter adapter = new IndexerDbAdapter();
        adapter.open();

        int cnt = 0;
        String url;

        while ((url = adapter.getUnindexedURL()) != null) {
            try {
                Indexer indexer = new Indexer(url);
    
                long startTime = System.currentTimeMillis();
                Integer total_words = indexer.getTotalNumberOfWords();
                System.out.println(String.format("Indexing page: %s\nfound %d words",url, total_words));
                HashMap<String, Double> wordScore = Ranker.CalculateWordScore(indexer.getListOfWords(), total_words);
                adapter.updateURL(url, indexer.getContent(), indexer.getTitle(),
                        Ranker.CalculateDateScore(indexer.getLastModified()),
                        Ranker.CalculateGeographicLocationScore(url));
                adapter.addWords(wordScore, url);
                adapter.addImages(url, indexer.getImages().toArray(new String[0]));
                long endTime = System.currentTimeMillis();
                System.out.println(String.format("Indexed %d page(s) in %d ms", ++cnt, endTime-startTime));
                
            } catch (Exception e) {
                e.printStackTrace();
            }
            adapter.setIndexedURL(url, true);
        }
        adapter.removeDuplicateImages();
        adapter.removeDuplicateWords();
        System.out.println("Finished Indexing all links");
        adapter.close();
    }

    Indexer(String url) throws IOException {
        connection = Jsoup.connect(url);
        doc = connection.get();
        HTMLParser();
    }

    public String getContent() {
        return doc.body().text().toLowerCase();
    }

    public String getTitle() {
        return doc.title();
    }

    public ArrayList<ArrayList<String>> getListOfWords() {
        return listOfWords;
    }

    public int getTotalNumberOfWords() {
        return totalNumberOfWords;
    }

    /**
     * This function return Array list of Array list of words for each tag after
     * apply pre processing on them.
     **/
    private void HTMLParser() {

        listOfWords = new ArrayList<ArrayList<String>>();

        totalNumberOfWords = 0;
        String[] tags = { "h1", "h2", "h3", "h4", "h5", "h6", "p", "span", "li", "tr", "a"};
        String title = doc.title();
        ArrayList<String> words = WordsExtractionProcess.SplitStrings(title);
        totalNumberOfWords += words.size();
        listOfWords.add(WordsExtractionProcess.ApplyingStemming(
            WordsExtractionProcess.RemovingStoppingWords(words)));

        for (String tag : tags) {
            String elem = doc.body().getElementsByTag(tag).text();
            words = WordsExtractionProcess.SplitStrings(elem);
            totalNumberOfWords += words.size();
            listOfWords.add(WordsExtractionProcess.ApplyingStemming(
                WordsExtractionProcess.RemovingStoppingWords(words)));
        }

    }

    /**
     * This function get last modified date stored by server
     **/
    public String getLastModified() {
        try {
            String date = connection.execute().header("Last-Modified");
            ArrayList<String> dateArrayList = new ArrayList<>();
            if (date == null)
                return "1 jan 1990";
            else {
                dateArrayList = WordsExtractionProcess.SplitStrings(date);
                return dateArrayList.get(1) +" "+ dateArrayList.get(2) + " " + dateArrayList.get(3);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "1 jan 1990";
    }
    public ArrayList<String> getImages(){
        ArrayList<String> imgURLs=new ArrayList<String>();
        Elements imgs= (Elements) doc.body().getElementsByTag("img");
        for (Element element : imgs) {
            String url = element.attr("abs:src");
            imgURLs.add(url);
        }
        return imgURLs;
    }
}