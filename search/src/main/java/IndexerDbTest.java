import java.util.ArrayList;
import java.util.HashMap;
import com.google.gson.Gson;


public class IndexerDbTest {
    public static void main(String[] args) throws Exception {
        IndexerDbAdapter dbAdapter = new IndexerDbAdapter();
        dbAdapter.open();
        dbAdapter.addURL("https://codeforces.com/", "a very long document related to competitive programming");
        dbAdapter.addURL("https://www.geeksforgeeks.org/", "a very long document related to programming in general");
        dbAdapter.addURL("https://www.youtube.com/", null);
        dbAdapter.updateURL("https://www.youtube.com/", null, 0.1, 0.2, 0.3);
        dbAdapter.addLink("https://www.geeksforgeeks.org/", "https://codeforces.com/");
        dbAdapter.addWord("programming", "https://www.geeksforgeeks.org/", 0.4);
        dbAdapter.addWord("programming", "https://codeforces.com/", 0.4);
        dbAdapter.addWord("competitive", "https://codeforces.com/", 0.5);

        dbAdapter.resetPagesRank();
        ArrayList<HashMap<String, String>> arr = dbAdapter.queryWords(new String [] {"programming"}, 10, 1);
        Gson gson = new Gson();
        System.out.println(gson.toJson(arr));
        System.out.println();

        arr = dbAdapter.queryWords(new String [] {"competitive"}, 10, 1);
        System.out.println(arr);
        System.out.println();

        arr = dbAdapter.queryPhrase("competitive programming", 10, 1);
        System.out.println(arr);
        System.out.println();

        System.out.println(dbAdapter.getUnindexedURL());
        dbAdapter.close();
    }
}