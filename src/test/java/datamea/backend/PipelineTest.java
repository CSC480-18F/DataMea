package datamea.backend;

import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import junit.framework.TestCase;

import java.util.Properties;

public class PipelineTest extends TestCase {
    StanfordCoreNLP pipeline;

    //Done
    public void testInitPipeline() {
        Properties props =  new Properties();
        props.setProperty("annotators","tokenize, ssplit, parse, sentiment");
        pipeline = new StanfordCoreNLP(props);
    }
    //needs to be fixed
    public void testPipeline() {
        assertEquals(pipeline,pipeline);
    }
}