import edu.stanford.nlp.io.IOUtils;
import edu.stanford.nlp.io.RuntimeIOException;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.util.CoreMap;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class LoadConll {

    public String inFile;
    public List<CoreMap> sents;
    public List<DependencyTree> trees;


    public String getInFile() {
        return inFile;
    }

    public List<CoreMap> getSents() {
        return sents;
    }

    public List<DependencyTree> getTrees() {
        return trees;
    }

    public LoadConll(String inFile, boolean unlabeled, boolean cPOS) {
        this.inFile = inFile;
        this.sents = new ArrayList<>();
        this.trees = new ArrayList<>();
        loadConllFile(unlabeled, cPOS);
    }

    public void loadConllFile(boolean unlabeled, boolean cPOS) {
        CoreLabelTokenFactory tf = new CoreLabelTokenFactory(false);
        BufferedReader reader = null;

        try {
            reader = IOUtils.readerFromString(this.inFile);
            List<CoreLabel> sentenceTokens = new ArrayList<>();
            DependencyTree tree = new DependencyTree();
            Iterator<String> var9 = IOUtils.getLineIterable(reader, false).iterator();

            while(var9.hasNext()) {
                String line = var9.next();
                String[] splits = line.split("\t");
                if (splits.length < 10) {
                    if (sentenceTokens.size() > 0) {
                        this.trees.add(tree);
                        CoreMap sentence = new CoreLabel();
                        sentence.set(CoreAnnotations.TokensAnnotation.class, sentenceTokens);
                        this.sents.add(sentence);
                        tree = new DependencyTree();
                        sentenceTokens = new ArrayList<>();
                    }
                } else {
                    String word = splits[1];
                    String pos = cPOS ? splits[3] : splits[4];
                    String depType = splits[7];
                    boolean var15 = true;

                    int head;
                    try {
                        head = Integer.parseInt(splits[6]);
                    } catch (NumberFormatException var21) {
                        continue;
                    }

                    CoreLabel token = tf.makeToken(word, 0, 0);
                    token.setTag(pos);
                    token.set(CoreAnnotations.CoNLLDepParentIndexAnnotation.class, head);
                    token.set(CoreAnnotations.CoNLLDepTypeAnnotation.class, depType);
                    sentenceTokens.add(token);
                    if (!unlabeled) {
                        tree.add(head, depType);
                    } else {
                        tree.add(head, "-UNKNOWN-");
                    }
                }
            }
        } catch (IOException var22) {
            throw new RuntimeIOException(var22);
        } finally {
            IOUtils.closeIgnoringExceptions(reader);
        }
    }
}
