package search.analyzers;

import datastructures.interfaces.IDictionary;
import datastructures.interfaces.IList;
import datastructures.interfaces.ISet;
import search.models.Webpage;
import datastructures.concrete.dictionaries.ChainedHashDictionary;
import datastructures.concrete.KVPair;
import datastructures.concrete.ChainedHashSet;
import java.net.URI;

/**
 * This class is responsible for computing how "relevant" any given document is
 * to a given search query.
 *
 * See the spec for more details.
 */
public class TfIdfAnalyzer {
    // This field must contain the IDF score for every single word in all
    // the documents.
    private IDictionary<String, Double> idfScores;
    private int docSize;
    private IDictionary<URI, Double> docVectorNorms;

    // This field must contain the TF-IDF vector for each webpage you were given
    // in the constructor.
    //
    // We will use each webpage's page URI as a unique key.
    private IDictionary<URI, IDictionary<String, Double>> documentTfIdfVectors;

    // Feel free to add extra fields and helper methods.

    public TfIdfAnalyzer(ISet<Webpage> webpages) {
        // Implementation note: We have commented these method calls out so your
        // search engine doesn't immediately crash when you try running it for the
        // first time.
        //
        // You should uncomment these lines when you're ready to begin working
        // on this class.
        this.docSize = webpages.size();
        this.idfScores = this.computeIdfScores(webpages);
        this.documentTfIdfVectors = this.computeAllDocumentTfIdfVectors(webpages);
    }

    // Note: this method, strictly speaking, doesn't need to exist. However,
    // we've included it so we can add some unit tests to help verify that your
    // constructor correctly initializes your fields.
    public IDictionary<URI, IDictionary<String, Double>> getDocumentTfIdfVectors() {
        return this.documentTfIdfVectors;
    }

    // Note: these private methods are suggestions or hints on how to structure your
    // code. However, since they're private, you're not obligated to implement exactly
    // these methods: feel free to change or modify these methods however you want. The
    // important thing is that your 'computeRelevance' method ultimately returns the
    // correct answer in an efficient manner.

    /**
     * Return a dictionary mapping every single unique word found
     * in every single document to their IDF score.
     */
    private IDictionary<String, Double> computeIdfScores(ISet<Webpage> pages) {
        this.documentTfIdfVectors = new ChainedHashDictionary<URI, IDictionary<String, Double>>();
        IDictionary<String, Double> tfScore = new ChainedHashDictionary<String, Double>();
        IDictionary<String, Double> idfScore = new ChainedHashDictionary<String, Double>();
        for (Webpage page : pages) {
            tfScore = computeTfScores(page.getWords());
            for (KVPair<String, Double> word : tfScore) {
                if (idfScore.containsKey(word.getKey())) {
                    idfScore.put(word.getKey(), idfScore.get(word.getKey()) + 1.0);
                } else {
                    idfScore.put(word.getKey(), 1.0);
                }
            }
            this.documentTfIdfVectors.put(page.getUri(), tfScore);
        }
        return idfScore;
    }

    /**
     * Returns a dictionary mapping every unique word found in the given list
     * to their term frequency (TF) score.
     *
     * The input list represents the words contained within a single document.
     */
    private IDictionary<String, Double> computeTfScores(IList<String> words) {
        IDictionary<String, Double> tfScore = new ChainedHashDictionary<String, Double>();
        for (String word : words) {
            if (tfScore.containsKey(word.toLowerCase())) {
                tfScore.put(word.toLowerCase(), tfScore.get(word.toLowerCase()) + 1.0);
            } else {
                tfScore.put(word.toLowerCase(), 1.0);
            }
        }
        return tfScore;
    }

    /**
     * See spec for more details on what this method should do.
     */
    private IDictionary<URI, IDictionary<String, Double>> computeAllDocumentTfIdfVectors(ISet<Webpage> pages) {
        // Hint: this method should use the idfScores field and
        // call the computeTfScores(...) method.
        IDictionary<URI, IDictionary<String, Double>> vectors = new ChainedHashDictionary<URI, 
                                                                          IDictionary<String, Double>>();
        this.docVectorNorms = new ChainedHashDictionary<URI, Double>();
        for (Webpage page : pages) {
            IDictionary<String, Double> scores = new ChainedHashDictionary<String, Double>();
            for (String word : page.getWords()) {
                
               // a * ln(b) == ln(b ^ a)
               scores.put(word.toLowerCase(), Math.log(Math.pow(docSize / idfScores.get(word.toLowerCase()), 
                         documentTfIdfVectors.get(page.getUri()).get(word.toLowerCase()) / page.getWords().size())));
            }
            vectors.put(page.getUri(), scores);
            this.docVectorNorms.put(page.getUri(), norm(scores));
        }
        return vectors;
    }

    /**
     * Returns the cosine similarity between the TF-IDF vector for the given query and the
     * URI's document.
     *
     * Precondition: the given uri must have been one of the uris within the list of
     *               webpages given to the constructor.
     */
    public Double computeRelevance(IList<String> query, URI pageUri) {
        // Note: The pseudocode we gave you is not very efficient. When implementing,
        // this method, you should:
        //
        // 1. Figure out what information can be precomputed in your constructor.
        //    Add a third field containing that information.
        //
        // 2. See if you can combine or merge one or more loops.
        IDictionary<String, Double> docVector = documentTfIdfVectors.get(pageUri);
        IDictionary<String, Double> queryVector = getQueryVector(query);
        ISet<String> q = new ChainedHashSet<String>();
        double num = 0.0;
        for (String word : query) {
            q.add(word);
        }
        double docScore;
        double qScore;
        for (String word : q) {
            docScore = 0.0;
            if (docVector.containsKey(word)) {
                docScore = docVector.get(word);
                qScore = queryVector.get(word);
                num += docScore * qScore;
            }
        }
        double denom = this.docVectorNorms.get(pageUri) * norm(queryVector);
        if (denom == 0) {
            return 0.0;
        }
        return (num / denom);
    }
    
    private Double norm(IDictionary<String, Double> vector) {
        double norm = 0.0;
        for (KVPair<String, Double> pair : vector) {
            norm += Math.pow(pair.getValue(), 2);
        }
        return Math.sqrt(norm);
    }
    
    private IDictionary<String, Double> getQueryVector(IList<String> query) {
        IDictionary<String, Double> result = new ChainedHashDictionary<String, Double>();
        IDictionary<String, Double> tfScore = computeTfScores(query);
        for (String word : query) {
            if (idfScores.containsKey(word) && tfScore.containsKey(word)) {
                result.put(word, Math.pow(Math.log(docSize / idfScores.get(word)), tfScore.get(word) / query.size()));
            } else {
                result.put(word, 0.0);
            }
        }
        return result;
    }
}
