package Engine;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.CoreMap;

import javax.mail.*;
import javax.mail.internet.ContentType;
import javax.mail.internet.MimeMultipart;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;

class Email {

    double VNEGTHRESH = .8;
    double NEGTHRESH = .7;
    double NEUTHRESH = .5;
    double POSTHRESH = .3;
    boolean runSentiment = false;
    boolean extracted = false;
    ArrayList<Email> emailsExtracted;
    ArrayList<String> sentences;
    int[] sentimentScores = new int[5];
    double sentimentPct = 0.0;
    String sentimentPctStr;
    int overallSentiment, sentencesAnalyzed;
    String content, title;
    MimeMultipart mp;
    BodyPart bp;
    Message message;
    Date date;
    Sender sender;
    Flags flags;
    int VNEG = 0;
    int NEG = 1;
    int NEU = 2;
    int POS = 3;
    int VPOS = 4;
    int VMULT = 3;

    //TODO figure out how to extract title, date
    public Email(MimeMultipart m, Sender s, Flags f){
        emailsExtracted = new ArrayList<>();
        extracted = true;
        mp = m;
        sender = s;
        flags = f;
        sentences = getSentences(content);
    }

    public Email(BodyPart b, Sender s, String t, Date d, Flags f){
        emailsExtracted = new ArrayList<>();
        extracted = true;
        bp = b;
        title = t;
        date = d;
        sender = s;
        flags = f;
        sentences = getSentences(content);
    }

    public Email(String c, Sender s, String t, Date d, Flags f){
        emailsExtracted = new ArrayList<>();
        extracted = true;
        content = c;
        sender = s;
        title = t;
        date = d;
        flags = f;
        sentences = getSentences(content);
    }



    public Email(Message m, Sender s, Boolean rs) {
        emailsExtracted = new ArrayList<>();
        message = m;
        runSentiment = rs;

        try {
            //System.out.println("Content: \n" + m.getContent().toString());
            content = getTextFromMessage(m);
            if(content != null) sentences = getSentences(content);
            title = m.getSubject();
            sender = s;
            date = m.getSentDate();
            flags = m.getFlags();

        } catch (Exception e) {
            e.printStackTrace();
        }
        initializeSentiment();
    }

    private void initializeSentiment() {
        int sentenceScore;
        double probability;
        Sentiment sentenceSentiment;

        if (runSentiment) {

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
            }

            overallSentiment = sentimentScores[VPOS] * VMULT + sentimentScores[POS] -
                    sentimentScores[NEG] - sentimentScores[VNEG] * VMULT;

            if(sentencesAnalyzed > 0) sentimentPct = ((double)overallSentiment / sentencesAnalyzed) * 100;
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
        if (message.isMimeType("text/plain")) {
            return message.getContent().toString();
        }
        return null;
    }



/*    private String getTextFromMimeMultipart(
            MimeMultipart mimeMultipart) throws IOException, MessagingException {

        int count = mimeMultipart.getCount();
        if (count == 0)
            throw new MessagingException("Multipart with no body parts not supported.");
        boolean multipartAlt = new ContentType(mimeMultipart.getContentType()).match("multipart/alternative");
        if (multipartAlt)
            // alternatives appear in an order of increasing
            // faithfulness to the original content. Customize as req'd.
            return getTextFromBodyPart(mimeMultipart.getBodyPart(count - 1));
        String result = "";
        for (int i = 0; i < count; i++) {
            BodyPart bodyPart = mimeMultipart.getBodyPart(i);
            String bodyText = getTextFromBodyPart(bodyPart);
            if(bodyText != "") emailsExtracted.add(new Email(bodyText, sender, title, date, flags));
            result += getTextFromBodyPart(bodyPart);
        }
        return result;
    }

    private String getTextFromBodyPart(
            BodyPart bodyPart) throws IOException, MessagingException {

        String result = "";
        if (bodyPart.isMimeType("text/plain")) {
            result = (String) bodyPart.getContent();
        } else if (bodyPart.isMimeType("text/html")) {
            String html = (String) bodyPart.getContent();
            result = org.jsoup.Jsoup.parse(html).text();
        } else if (bodyPart.getContent() instanceof MimeMultipart){
            result = getTextFromMimeMultipart((MimeMultipart)bodyPart.getContent());
        }
        return result;
    }*/

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
        if(this.sentimentPctStr != null)
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