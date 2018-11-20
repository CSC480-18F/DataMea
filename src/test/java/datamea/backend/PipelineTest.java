package datamea.backend;

import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import junit.framework.TestCase;

import java.util.Properties;

public class PipelineTest extends TestCase {
    StanfordCoreNLP pipeline;

    public void testInitPipeline() {
        Properties props =  new Properties();
        props.setProperty("annotators","tokenize, ssplit, parse, setiment");
        pipeline = new StanfordCoreNLP(props);
    }

    public void testPipeline() {
        assertEquals(pipeline,pipeline);
    }
}