import edu.stanford.nlp.io.IOUtils;
import edu.stanford.nlp.io.RuntimeIOException;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.parser.nndep.DependencyParser;
import edu.stanford.nlp.parser.nndep.ParsingSystem;
import edu.stanford.nlp.trees.PennTreebankLanguagePack;
import edu.stanford.nlp.trees.TreebankLanguagePack;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.util.CollectionUtils;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.Timing;
import edu.stanford.nlp.util.logging.Redwood;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.*;

public class CoreNLPDepParser {
    private final TreebankLanguagePack tlp = new PennTreebankLanguagePack();
    private static Redwood.RedwoodChannels log = Redwood.channels(new Object[]{ParsingSystem.class});






    private Set getPunctuationTags() {
        return (this.tlp instanceof PennTreebankLanguagePack ?
                new HashSet(Arrays.asList("''", ",", ".", ":", "``", "-LRB-", "-RRB-")) : CollectionUtils.asSet(this.tlp.punctuationTags()));
    }

    public Map<String, Double> evaluate(List<CoreMap> sentences, List< DependencyTree > goldTrees, List< DependencyTree > trees) {
        HashMap<String, Double> result = new HashMap<>();
        Set punctuationTags = this.getPunctuationTags();
        int nTrees = trees.size();
        int nGSTrees = goldTrees.size();
        int nRemoved = 0;
        if (nTrees != nGSTrees) {
            log.error("Incorrect number of trees.");
            return null;
        } else {
            int correctArcs = 0;
            int correctArcsNoPunc = 0;
            int correctHeads = 0;
            int correctHeadsNoPunc = 0;
            int correctTrees = 0;
            int correctTreesNoPunc = 0;
            int correctRoot = 0;
            int sumArcs = 0;
            int sumArcsNoPunc = 0;
            System.out.println("\nnum trees: " + nTrees + "\n");

            for(int i = 0; i < nTrees; ++i) {
                List<CoreLabel> tokens = (List<CoreLabel>)((CoreMap)sentences.get(i)).get(CoreAnnotations.TokensAnnotation.class);
                if (((DependencyTree)trees.get(i)).n != ((DependencyTree)goldTrees.get(i)).n) {
                    log.error("Tree " + (i + 1) + ": incorrect number of nodes.");
                    --nTrees;
                    --nGSTrees;
                    ++nRemoved;
                }

                else if (!((DependencyTree)trees.get(i)).isTree()) {
                    log.error("Tree " + (i + 1) + ": illegal.");
                    --nTrees;
                    --nGSTrees;
                    ++nRemoved;
                }

                else {
                    int nCorrectHead = 0;
                    int nCorrectHeadNoPunc = 0;
                    int nNoPunc = 0;

                    for (int j = 1; j <= ((DependencyTree) trees.get(i)).n; ++j) {
                        if (((DependencyTree) trees.get(i)).getHead(j) == ((DependencyTree) goldTrees.get(i)).getHead(j)) {
                            ++correctHeads;
                            ++nCorrectHead;
                            if (((DependencyTree) trees.get(i)).getLabel(j).equals(((DependencyTree) goldTrees.get(i)).getLabel(j))) {
                                ++correctArcs;
                            }
                        }

                        ++sumArcs;
                        String tag = (tokens.get(j - 1)).tag();
                        if (!punctuationTags.contains(tag)) {
                            ++sumArcsNoPunc;
                            ++nNoPunc;
                            if (((DependencyTree) trees.get(i)).getHead(j) == ((DependencyTree) goldTrees.get(i)).getHead(j)) {
                                ++correctHeadsNoPunc;
                                ++nCorrectHeadNoPunc;
                                if (((DependencyTree) trees.get(i)).getLabel(j).equals(((DependencyTree) goldTrees.get(i)).getLabel(j))) {
                                    ++correctArcsNoPunc;
                                }
                            }
                        }
                    }

                    if (nCorrectHead == ((DependencyTree) trees.get(i)).n) {
                        ++correctTrees;
                    }

                    if (nCorrectHeadNoPunc == nNoPunc) {
                        ++correctTreesNoPunc;
                    }

                    if (((DependencyTree) trees.get(i)).getRoot() == ((DependencyTree) goldTrees.get(i)).getRoot()) {
                        ++correctRoot;
                    }
                }
            }

            result.put("UAS", (double)correctHeads * 100.0D / (double)sumArcs);
            result.put("UASnoPunc", (double)correctHeadsNoPunc * 100.0D / (double)sumArcsNoPunc);
            result.put("LAS", (double)correctArcs * 100.0D / (double)sumArcs);
            result.put("LASnoPunc", (double)correctArcsNoPunc * 100.0D / (double)sumArcsNoPunc);
            result.put("UEM", (double)correctTrees * 100.0D / (double)nTrees);
            result.put("UEMnoPunc", (double)correctTreesNoPunc * 100.0D / (double)nTrees);
            result.put("ROOT", (double)correctRoot * 100.0D / (double)nTrees);
            System.out.println("Num Trees Removed: " + nRemoved);
            return result;
        }

    }

    public double testCoNLL(String testFile, String gsFile) {
        log.info(new Object[]{"Test File: " + testFile});
        Timing timer = new Timing();
        LoadConll testLC = new LoadConll(testFile, false, false);
        LoadConll gsLC = new LoadConll(gsFile, false, false);
        List<CoreMap> testSents = testLC.getSents();
        List<DependencyTree> testTrees = testLC.getTrees();
        List<CoreMap> gsSents = gsLC.getSents();
        List<DependencyTree> gsTrees = gsLC.getTrees();

        int numTestWords = 0;
        int numTestSentences = 0;
        int numGSWords = 0;
        int numGSSentences = 0;
        Iterator<CoreMap> var9 = testSents.iterator();
        Iterator<CoreMap> var10 = gsSents.iterator();

        while(var9.hasNext()) {
            CoreMap testSent = var9.next();
            ++numTestSentences;
            List<CoreLabel> tokens = (List<CoreLabel>)testSent.get(CoreAnnotations.TokensAnnotation.class);
            Iterator<CoreLabel> var12 = tokens.iterator();

            while(var12.hasNext()) {
                CoreLabel token = var12.next();
                String word = token.word();
                ++numTestWords;
            }
        }

        while(var10.hasNext()) {
            CoreMap gsSent = var10.next();
            ++numGSSentences;
            List<CoreLabel> tokens = (List<CoreLabel>)gsSent.get(CoreAnnotations.TokensAnnotation.class);
            Iterator<CoreLabel> var12 = tokens.iterator();

            while(var12.hasNext()) {
                CoreLabel token = var12.next();
                String word = token.word();
                ++numGSWords;
            }
        }


        System.err.printf("# Test Words: %d \n", numTestWords);
        System.err.printf("# Test Sents: %d \n", numTestSentences);
        System.err.printf("# GS Words: %d \n", numGSWords);
        System.err.printf("# GS Sents: %d \n", numGSSentences);


        Map<String, Double> result = evaluate(gsSents, gsTrees, testTrees);
        double uas = false ? (Double)result.get("UASnoPunc") : (Double)result.get("UAS");
        double las = false ? (Double)result.get("LASnoPunc") : (Double)result.get("LAS");
        System.err.printf("UAS = %.4f%n", uas);
        System.err.printf("LAS = %.4f%n", las);
        long millis = timer.stop();

        return las;
    }

    public static void main(String[] args) {
       System.out.println("\nCoreNLP with UD English\n");
       DependencyParser parser = DependencyParser.loadFromModelFile(DependencyParser.DEFAULT_MODEL);
       String sentences_path = "../UD_English.conll";
       parser.testCoNLL(sentences_path, "corenlp_UD.conll");
       
       // My repurposed script
       CoreNLPDepParser coreNLP = new CoreNLPDepParser();
       coreNLP.testCoNLL("../mcparseface_propbank.conll", "../propbank.conll");
       }
}

