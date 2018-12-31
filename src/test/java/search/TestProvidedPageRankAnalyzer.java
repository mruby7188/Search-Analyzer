package search;

import datastructures.concrete.ChainedHashSet;
import datastructures.concrete.dictionaries.ChainedHashDictionary;
import datastructures.concrete.DoubleLinkedList;
import datastructures.interfaces.IList;
import datastructures.interfaces.ISet;
import misc.BaseTest;
import org.junit.Test;
import search.analyzers.PageRankAnalyzer;
import search.analyzers.TfIdfAnalyzer;
import search.misc.Bridge;
import search.misc.exceptions.DataExtractionException;
import search.models.Webpage;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/*
 * On gitlab, while testing, this file will be overwritten by our tests.
 * That is, any changes you make in this file will be lost when running tests on gitlab.
 *
 * So, do not write your Page Rank Analyzer tests in this file.
 * Create a separate test file TestPageRankAnalyzer.java to write your tests.
 */

public class TestProvidedPageRankAnalyzer extends BaseTest {
    // We say two floating point numbers are equal if they're within
    // this delta apart from each other.
    public static final double DELTA = 0.00001;
    private ISet<Webpage> gutenberg;
    private ISet<Webpage> wiki;
    private ISet<Webpage> wikiWS;
    private PageRankAnalyzer gutenbergAnalyzer;
    private PageRankAnalyzer wikiAnalyzer;
    private PageRankAnalyzer wikiWSAnalyzer;

    private Webpage buildPage(URI currentPage, URI[] linksTo) {
        IList<URI> links = new DoubleLinkedList<>();
        for (URI uri : linksTo) {
            links.add(uri);
        }
        return new Webpage(
                currentPage,
                links,
                new DoubleLinkedList<>(),
                "title",
                "blurb");
    }

    @Test(timeout=SECOND)
    public void testSpecExample1() {
        URI pageAUri = URI.create("http://example.com/page-a.html");
        URI pageBUri = URI.create("http://example.com/page-b.html");
        URI pageCUri = URI.create("http://example.com/page-c.html");
        URI pageDUri = URI.create("http://example.com/page-d.html");

        ISet<Webpage> pages = new ChainedHashSet<>();
        pages.add(this.buildPage(pageAUri, new URI[] {pageBUri, pageCUri, pageDUri}));
        pages.add(this.buildPage(pageBUri, new URI[] {pageAUri}));
        pages.add(this.buildPage(pageCUri, new URI[] {pageAUri}));
        pages.add(this.buildPage(pageDUri, new URI[] {pageAUri}));

        PageRankAnalyzer analyzer = new PageRankAnalyzer(pages, 0.85, 0.00001, 100);

        assertEquals(0.47973, analyzer.computePageRank(pageAUri), DELTA);
        assertEquals(0.17342, analyzer.computePageRank(pageBUri), DELTA);
        assertEquals(0.17342, analyzer.computePageRank(pageCUri), DELTA);
        assertEquals(0.17342, analyzer.computePageRank(pageDUri), DELTA);
    }

    @Test(timeout=SECOND)
    public void testSpecExample2() {
        URI pageAUri = URI.create("http://example.com/page-a.html");
        URI pageBUri = URI.create("http://example.com/page-b.html");
        URI pageCUri = URI.create("http://example.com/page-c.html");

        ISet<Webpage> pages = new ChainedHashSet<>();
        pages.add(this.buildPage(pageAUri, new URI[] {pageBUri}));
        pages.add(this.buildPage(pageBUri, new URI[] {pageCUri}));
        pages.add(this.buildPage(pageCUri, new URI[] {pageAUri}));

        PageRankAnalyzer analyzer = new PageRankAnalyzer(pages, 0.85, 0.00001, 100);

        assertEquals(0.33333, analyzer.computePageRank(pageAUri), DELTA);
        assertEquals(0.33333, analyzer.computePageRank(pageBUri), DELTA);
        assertEquals(0.33333, analyzer.computePageRank(pageCUri), DELTA);
    }

    @Test(timeout=SECOND)
    public void testSpecExample3() {
        URI pageAUri = URI.create("http://example.com/page-a.html");
        URI pageBUri = URI.create("http://example.com/page-b.html");
        URI pageCUri = URI.create("http://example.com/page-c.html");
        URI pageDUri = URI.create("http://example.com/page-d.html");
        URI pageEUri = URI.create("http://example.com/page-e.html");

        ISet<Webpage> pages = new ChainedHashSet<>();
        pages.add(this.buildPage(pageAUri, new URI[] {pageBUri, pageDUri}));
        pages.add(this.buildPage(pageBUri, new URI[] {pageCUri, pageDUri}));
        pages.add(this.buildPage(pageCUri, new URI[] {}));
        pages.add(this.buildPage(pageDUri, new URI[] {pageAUri}));
        pages.add(this.buildPage(pageEUri, new URI[] {pageDUri}));


        long start = System.currentTimeMillis();
        PageRankAnalyzer analyzer = new PageRankAnalyzer(pages, 0.85, 0.00001, 100);
        System.out.println(System.currentTimeMillis() - start + " mls");
        
        assertEquals(0.31706, analyzer.computePageRank(pageAUri), DELTA);
        assertEquals(0.18719, analyzer.computePageRank(pageBUri), DELTA);
        assertEquals(0.13199, analyzer.computePageRank(pageCUri), DELTA);
        assertEquals(0.31132, analyzer.computePageRank(pageDUri), DELTA);
        assertEquals(0.05244, analyzer.computePageRank(pageEUri), DELTA);
    }
    
    
    @Test(timeout= 10 * SECOND)
    public void testGutenbergTime1() {
        this.gutenberg = new ChainedHashSet<>();
        try {
       
            System.out.println("Gutenberg Analyzer Built in: ");
           
            long start = System.currentTimeMillis();
              
            this.gutenberg = Files.walk(Paths.get("data", "gutenberg"))
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".htm") || path.toString().endsWith(".html"))
                    .map(Path::toUri)
                    .map(Webpage::load)
                    .collect(Bridge.toISet());
              
            System.out.println(System.currentTimeMillis() - start + " mls");
              
        } catch (IOException ex) {
            throw new DataExtractionException("Could not find given root folder", ex);
        }
        
        System.out.print("Gutenberg Analyzer Constructed in: ");
          
        long start = System.currentTimeMillis();
          
        this.gutenbergAnalyzer = new PageRankAnalyzer(gutenberg, 0.85, 0.00001, 100);
          
        System.out.println(System.currentTimeMillis() - start + " mls");

        ChainedHashDictionary<URI, Double> pageRanks = new ChainedHashDictionary<URI, Double>();
          
        System.out.print("Gutenberg Page Ranks Computed in: ");
          
        long start2 = System.currentTimeMillis();
          
        for (Webpage page : gutenberg) {
              
            pageRanks.put(page.getUri(), this.gutenbergAnalyzer.computePageRank(page.getUri()));
        }
          
        System.out.println(System.currentTimeMillis() - start2 + " mls");
        
    }
    
    @Test(timeout= 600 * SECOND)
    public void testWikiTime1() {
        this.wiki = new ChainedHashSet<>();
        try {
            
            System.out.print("Wikipedia Analyzer Built in: ");
            
            long start = System.currentTimeMillis();
            
            this.wiki = Files.walk(Paths.get("data", "wikipedia"))
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".htm") || path.toString().endsWith(".html"))
                    .map(Path::toUri)
                    .map(Webpage::load)
                    .collect(Bridge.toISet());
          
            System.out.println(System.currentTimeMillis() - start + " mls");
        } catch (IOException ex) {
            throw new DataExtractionException("Could not find given root folder", ex);
        }
           
        System.out.print("Wikipedia Analyzer Constructed in: ");
            
        long start = System.currentTimeMillis();
        this.wikiAnalyzer = new PageRankAnalyzer(wiki, 0.85, 0.00001, 100);
            
        System.out.println(System.currentTimeMillis() - start + " mls");
    
        System.out.print("Wikipedia Page Ranks Computed in: ");
        ChainedHashDictionary<URI, Double> pageRanks = new ChainedHashDictionary<URI, Double>();
          
        long start2 = System.currentTimeMillis();
          
        for (Webpage page : wiki) {
            pageRanks.put(page.getUri(), wikiAnalyzer.computePageRank(page.getUri()));
        }
          
          
        System.out.println(System.currentTimeMillis() - start2 + " mls");
        
    }
    
    @Test(timeout= 600 * SECOND)
    public void testWikiWSTime1() {
        this.wikiWS = new ChainedHashSet<>();
        try {
          
            System.out.print("Wikipedia-with-Spam Analyzer Constructed in: ");
          
            long start = System.currentTimeMillis();
          
            this.wikiWS = Files.walk(Paths.get("data", "wikipedia-with-spam"))
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".htm") || path.toString().endsWith(".html"))
                    .map(Path::toUri)
                    .map(Webpage::load)
                    .collect(Bridge.toISet());          
          
            System.out.println(System.currentTimeMillis() - start + " mls");
          
        } catch (IOException ex) {
            throw new DataExtractionException("Could not find given root folder", ex);
        } 
            
        System.out.print("Wikipedia-with-Spam Analyzer Constructed in: ");
           
        long start = System.currentTimeMillis();
        this.wikiWSAnalyzer = new PageRankAnalyzer(wikiWS, 0.85, 0.00001, 100);
                
        System.out.println(System.currentTimeMillis() - start + " mls");
    
        System.out.print("Wikipedia-With-Spam Page Ranks Computed in: ");
        ChainedHashDictionary<URI, Double> pageRanks = new ChainedHashDictionary<URI, Double>();
          
        long start2 = System.currentTimeMillis();
          
        for (Webpage page : wikiWS) {
            pageRanks.put(page.getUri(), wikiWSAnalyzer.computePageRank(page.getUri()));
        }
            
        System.out.println(System.currentTimeMillis() - start2 + " mls");
        
    }
}
