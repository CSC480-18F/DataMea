
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.CoreMap;

import javax.mail.*;
import javax.mail.internet.MimeMultipart;
import java.io.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.apache.commons.mail.util.MimeMessageParser;
import sun.jvm.hotspot.runtime.VM;

class Email {

    boolean runSentiment;
    double VNEGTHRESH = .8;
    double NEGTHRESH = .7;
    double NEUTHRESH = .5;
    double POSTHRESH = .3;
    ArrayList<String> sentences;
    Message message;
    int[] sentimentScores = new int[5];
    int overallSentiment, sentencesAnalyzed;
    double sentimentPct;
    String content, title, sentimentPctStr;
    Date date;
    Sender sender;
    Flags flags;
    int VNEG = 0;
    int NEG = 1;
    int NEU = 2;
    int POS = 3;
    int VPOS = 4;
    int VMULT = 3;
    File serializedEmail;
    String folder;


    public Email(File f) {
        //to do: recreate emails using this constructor

        recoverEmail(f);


    }

    public void recoverEmail(File f) {
        BufferedReader br;

        try {
            br = new BufferedReader(new FileReader(f));
            this.folder = User.decrypt(br.readLine());
            long unixDate = Long.parseLong(br.readLine());
            this.date = new Date(unixDate);
            this.sender = new Sender(User.decrypt(br.readLine()));
            this.flags = new Flags(br.readLine());

            //add fields to reconstruct sentiment analysis

            br.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.out.println("File not found");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    public Email(Message m, Sender s, Boolean rs) {
        message = m;
        runSentiment = rs;

        try {
            //System.out.println("Content: \n" + m.getContent().toString());
            content = getTextFromMessage(m);
            if (content != null) sentences = getSentences(content);
            title = m.getSubject();
            sender = s;
            date = m.getSentDate();
            flags = m.getFlags();

        } catch (Exception e) {
            e.printStackTrace();
        }

        if (runSentiment) initializeSentiment();
    }

    private void initializeSentiment() {
        int sentenceScore;
        double probability;
        Sentiment sentenceSentiment;

        if (sentences != null) {
            for (String sentence : sentences) {
                if (sentence.endsWith(".") || sentence.endsWith("!") || sentence.endsWith("?")) {
                    //System.out.println("\n" + sentence);
                    sentencesAnalyzed++;
                    sentenceSentiment = analyzeSentiment(sentence);
                    sentenceScore = sentenceSentiment.score;
                    probability = sentenceSentiment.probability;
                    switch (sentenceScore) {
                        case 0:
                            //System.out.println(probability + " chance it is Very Negative");
                            if (probability > VNEGTHRESH) {
                                this.sentimentScores[VNEG]++;
                                //System.out.println("Incrementing Very Negative");
                            } else {
                                this.sentimentScores[NEG]++;
                                //System.out.println("Incrementing Negative");
                            }
                            break;
                        case 1:
                            //System.out.println(probability + " chance it is Negative");
                            if (probability > NEGTHRESH) {
                                this.sentimentScores[NEG]++;
                                //System.out.println("Incrementing Negative");
                            } else {
                                this.sentimentScores[NEU]++;
                                //System.out.println("Incrementing Neutral");
                            }
                            break;
                        case 2:
                            //System.out.println(probability + " chance it is Neutral");
                            if (probability > NEUTHRESH) {
                                this.sentimentScores[NEU]++;
                                //System.out.println("Incrementing Neutral");
                            } else {
                                this.sentimentScores[POS]++;
                                //System.out.println("Incrementing Positive");
                            }
                            break;
                        case 3:
                            //System.out.println(probability + " chance it is Positive");
                            if (probability > POSTHRESH) {
                                this.sentimentScores[POS]++;
                                //System.out.println("Incrementing Positive");
                            } else {
                                this.sentimentScores[VPOS]++;
                                //System.out.println("Incrementing Very Positive");
                            }
                            break;
                        case 4:
                            //System.out.println(probability + " chance it is Very Positive");
                            this.sentimentScores[4]++;
                            //System.out.println("Incrementing Very Positive");
                            break;
                        default:
                            break;

                    }
                }
            }

            overallSentiment = sentimentScores[VPOS] * VMULT + sentimentScores[POS] -
                    sentimentScores[NEG] - sentimentScores[VNEG] * VMULT;

            if (sentencesAnalyzed > 0) sentimentPct = ((double) overallSentiment / sentencesAnalyzed) * 100;
            else sentimentPct = 0;
            DecimalFormat df = new DecimalFormat("0.##");
            sentimentPctStr = df.format(sentimentPct) + "%";
        }
    }








    /*
This function was modified from an existing function by ItsCuties from the site below
http://www.itcuties.com/java/javamail-read-email/
Some additional notes:
-whenever something like javax.mail.internet.MimeMultipart@396f6598 appears as the message content,
it appears to be whenever there is a thread of replies
-anything (from what i've checked) that is html, is a mass email
 */

    private String getTextFromMessage(Message message) throws IOException, MessagingException {
        String result = "";
        System.out.println(message.getContentType());
        if (message.isMimeType("text/plain")) {
            return message.getContent().toString();
        }
        else if(message.isMimeType("multipart/*")){
            Multipart mp = (Multipart) message.getContent();
            if(mp.getBodyPart(1).isMimeType("multipart/*")){
                return getTextFromFirstBodyPart(mp.getBodyPart(0));
            }
            return mp.getBodyPart(1).getContent().toString();
        }
        else {
            System.out.println("First body part is not plain text :(");
        }
        return null;
    }

    private String getTextFromFirstBodyPart(BodyPart bp) throws IOException, MessagingException {

        if (bp.isMimeType("text/plain")) {
            return message.getContent().toString();
        } else if (bp.isMimeType("text/html")) {
            String html = (String) bp.getContent();
            return org.jsoup.Jsoup.parse(html).text();
       } /* else if (bp.isMimeType("multipart/*")) {
            return getTextFromFirstBodyPart(bp.getContent());
        }*/
        else{
            System.out.println("bp[0] type:" + bp.getContentType());
            return null;
        }

    }

    private ArrayList<String> getSentences(String result) {
        result = filter(result);
        //System.out.println("After filter:\n" + result);
        ArrayList<String> sentences = new ArrayList<String>();
        String[] split = result.split("~|\\n");
        for (String s : split) {
            if (s.length() > 0) {
                String trimmed = s.trim();
                char[] c = trimmed.toCharArray();
                if (c.length > 0) {
                    if (c[0] >= 65 && c[0] <= 90) //checking if first letter is uppercase via ascii value
                        sentences.add(trimmed);
                }
            }
        }
        return sentences;
    }


    static Sentiment analyzeSentiment(String message) {

        Properties props = new Properties();
        props.setProperty("annotators", "tokenize, ssplit, parse, sentiment");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

        //System.out.println("Processing annotation");
        Annotation annotation = pipeline.process(message);
        List<CoreMap> sentence = annotation.get(CoreAnnotations.SentencesAnnotation.class);

        int sentimentScore = -1;
        double probability = -1;

        for (CoreMap s : sentence) {
            Tree tree = s.get(SentimentCoreAnnotations.SentimentAnnotatedTree.class);
            sentimentScore = RNNCoreAnnotations.getPredictedClass(new CoreLabel(tree.label()));
            probability = RNNCoreAnnotations.getPredictedClassProb(new CoreLabel(tree.label()));
        }


        Sentiment sentiment = new Sentiment(sentimentScore, probability);

        return sentiment;
    }


    public static String getCurrentTimeStamp() {
        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); //dd/MM/yyyy
        Date now = new Date();
        String strDate = sdfDate.format(now);
        return strDate;
    }

    public static String filter(String text) {

        int ABBR = 14;
        String newText = text;

        String[][] abbreviations = {{"(^|-)(d|D)r\\.", "(^|-)(M|m)r\\.", "(^|-)(M|m)rs\\.", "(^|-)(P|p)rof\\.",
                "^(J|j)an\\.", "^(F|f)eb\\.", "^(M|m)ar\\.", "^(A|a)pr\\.", "^(J|j)un\\.",
                "^(A|a)ug\\.", "^(S|s)ep\\.", "^(O|o)ct\\.", "^(N|n)ov\\.", "^(D|d)ec\\."},
                {"Dr", "Mr", "Mrs", "Professor", "January", "Februrary", "March", "April", "June", "August",
                        "September", "October", "November", "December"}};

        for (int i = 0; i < ABBR; i++) {
            newText = newText.replaceAll(abbreviations[0][i], abbreviations[1][i]);
        }

        String url = "(http|https|ftp|ftps)\\:\\/\\/[a-zA-Z0-9\\-\\.]+\\.[a-zA-Z]{2,3}(\\/\\S*)?";
        newText = newText.replaceAll(url, "");

        String email = "^([a-z0-9_\\.-]+)@(?!domain.com)([\\da-z\\.-]+)\\.([a-z\\.]{2,6})$";
        newText = newText.replaceAll(email, "");

        String punctuation = "[`,~,*,#,^,>,\\-,\\n,\\t]";
        newText = newText.replaceAll(punctuation, "");

        String breakline = "[\\n]";
        newText = newText.replaceAll(breakline, "\n~");

        newText = newText.replaceAll("\\.", ".~");
        newText = newText.replaceAll("\\?", "?~");
        newText = newText.replaceAll("\\!", "!~");
        newText = newText.replaceAll("\\r", " ");

        return newText;
    }


    public String toString() {
        if (this.sentimentPctStr != null)
            return "From: " + this.sender + "\nTitle:" + this.title + "\nDate: " + date + "\nFlags: " + flags.toString()
                    + "\nSentiment: " + this.sentimentPctStr + "\n" + content;
        else
            return "From: " + this.sender + "\nTitle:" + this.title + "\nDate: " + date + "\nFlags: " + flags.toString()
                    + "\n" + content;
    }

    public void display() {
        //System.out.println(this.toString());
    }

}
