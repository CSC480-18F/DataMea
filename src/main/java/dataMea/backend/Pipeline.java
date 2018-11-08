package dataMea.backend;

import edu.stanford.nlp.pipeline.StanfordCoreNLP;

import java.util.Properties;

public class Pipeline {

    static StanfordCoreNLP pipeline;

    public static void initPipeline() {
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize, ssplit, parse, sentiment");
        pipeline = new StanfordCoreNLP(props);
    }

    public static StanfordCoreNLP pipeline() {
        return pipeline;
    }


}
