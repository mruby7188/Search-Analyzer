package search.analyzers;

import java.net.URI;

import datastructures.interfaces.IDictionary;
import datastructures.interfaces.IList;
import datastructures.interfaces.ISet;
import search.models.Webpage;
import datastructures.concrete.DoubleLinkedList;

public class EnhancedQueryAnalyzer extends TfIdfAnalyzer {
    
    public EnhancedQueryAnalyzer(ISet<Webpage> webpages) {
        super(webpages);
    }
    
    private IList<String> exactMatchSearch(IList<String> query) {
        String out = "";
        IList<String> exactSearch = new DoubleLinkedList<String>();
        for (String word : query) {
            word.substring(1);
            if (word.charAt(1) == '"' && word.charAt(2) != '"') {
                out = word.substring(1);
            }
            if (word.charAt(word.length()) != '"') {
                while (word.lastIndexOf('"') == -1) {
                    out += word;
                }
                out += word.substring(0, word.lastIndexOf('"'));
            } else {
                exactSearch.add(word);
            }
            exactSearch.add(out);
        }
        return exactSearch;  
    }
    
    public Double computeRelevance(IList<String> query, URI pageUri) {
        return super.computeRelevance(exactMatchSearch(query), pageUri);
    }
}
