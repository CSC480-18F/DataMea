
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
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import org.apache.commons.mail.util.MimeMessageParser;

class Email {

    double VNEGTHRESH = .8;
    double NEGTHRESH = .7;
    double NEUTHRESH = .5;
    double POSTHRESH = .3;
    ArrayList<String> sentences;
    Message message;
    int[] sentimentScores = new int[5];
    int overallSentiment;
    String content, title;
    Date date;
    Sender sender;
    Flags flags;
    int VNEG = 0;
    int NEG = 1;
    int NEU = 2;
    int POS = 3;
    int VPOS = 4;
    int VMULT = 3;

    public Email(Message m, Sender s, Boolean runSentiment) {

        message = m;

        try {
            sentences = getSentences(m);
            title = m.getSubject();
            sender = new Sender(s.toString());
            date = m.getSentDate();
            flags = m.getFlags();

        } catch (MessagingException e) {
            e.printStackTrace();
        }

        int sentenceScore;
        double probability;
        Sentiment sentenceSentiment;

        if (runSentiment) {

            if (sentences != null) {
                for (String sentence : sentences) {
                    if (sentence.endsWith(".") || sentence.endsWith("!") || sentence.endsWith("?")) {
                        System.out.println("\n" + sentence);
                        sentenceSentiment = analyzeSentiment(sentence);
                        sentenceScore = sentenceSentiment.score;
                        probability = sentenceSentiment.probability;
                        switch (sentenceScore) {
                            case 0:
                                System.out.println(probability + " chance it is Very Negative");
                                if (probability > VNEGTHRESH) {
                                    this.sentimentScores[VNEG]++;
                                    System.out.println("Incrementing Very Negative");
                                } else {
                                    this.sentimentScores[NEG]++;
                                    System.out.println("Incrementing Negative");
                                }
                                break;
                            case 1:
                                System.out.println(probability + " chance it is Negative");
                                if (probability > NEGTHRESH) {
                                    this.sentimentScores[NEG]++;
                                    System.out.println("Incrementing Negative");
                                } else {
                                    this.sentimentScores[NEU]++;
                                    System.out.println("Incrementing Neutral");
                                }
                                break;
                            case 2:
                                System.out.println(probability + " chance it is Neutral");
                                if (probability > NEUTHRESH) {
                                    this.sentimentScores[NEU]++;
                                    System.out.println("Incrementing Neutral");
                                } else {
                                    this.sentimentScores[POS]++;
                                    System.out.println("Incrementing Positive");
                                }
                                break;
                            case 3:
                                System.out.println(probability + " chance it is Positive");
                                if (probability > POSTHRESH) {
                                    this.sentimentScores[POS]++;
                                    System.out.println("Incrementing Positive");
                                } else {
                                    this.sentimentScores[VPOS]++;
                                    System.out.println("Incrementing Very Positive");
                                }
                                break;
                            case 4:
                                System.out.println(probability + " chance it is Very Positive");
                                this.sentimentScores[4]++;
                                System.out.println("Incrementing Very Positive");
                                break;
                            default:
                                break;
                        }
                    }
                }
            }

            overallSentiment = sentimentScores[VPOS] * VMULT + sentimentScores[POS] -
                    sentimentScores[NEG] - sentimentScores[VNEG] * VMULT;
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


    private ArrayList<String> getSentences(Message message)  {
        //System.out.println("Getting text from message");

        try {
            String result = "";
            if (message.isMimeType("text/plain")) {
                //System.out.println("Message is plain text");
                result = message.getContent().toString();
                return _getSentences(result);
            } else if (message.isMimeType("multipart/*")) {
                //System.out.println("Message is multipart");
                MimeMultipart mimeMultipart = (MimeMultipart) message.getContent();
                return getTextFromMimeMultipart(mimeMultipart, false);
            }

        } catch (MessagingException e) {
            System.out.println("Messagin Exception");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("IO Exception");
            e.printStackTrace();
        }
        return null;
    }

    private ArrayList<String> _getSentences(String result) {
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


    public ArrayList<String> getTextFromMimeMultipart(
            MimeMultipart mimeMultipart, Boolean isRecursing)  {
        String title = "";
        try {
            String result = "";
            int count = mimeMultipart.getCount();
            for (int i = 0; i < count; i++) {
                //System.out.println("Body Part: " + (i + 1));
                BodyPart bodyPart = mimeMultipart.getBodyPart(i);

                title = mimeMultipart.getParent().getDescription();
                //System.out.println("bodyPart " + i + "\n" + bodyPart.getContent());
                if (bodyPart.isMimeType("text/plain")) {
                    //System.out.println("Body part is plain text");
                    result = result + "\n" + bodyPart.getContent();
                    return _getSentences(result);
                } else if (bodyPart.isMimeType("text/html")) {
                    //System.out.println("Body part is HTML");
                    String html = (String) bodyPart.getContent();
                    result = result + "\n" + org.jsoup.Jsoup.parse(html).text();
                    return _getSentences(result);
                } else if (bodyPart.getContent() instanceof MimeMultipart) {
                    //System.out.println("Body part is another MimeMultipart object");
                    result = result + getTextFromMimeMultipart((MimeMultipart) bodyPart.getContent(), true).get(0);
                }


            }
            if (isRecursing) {
                ArrayList<String> temp = new ArrayList<String>();
                temp.add(result);
                return temp;
            } else return _getSentences(result);
        }catch (Exception e) {
            System.out.println("This function here throws an error");
            System.out.println(title);
            e.printStackTrace();
            return null;
        }
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
        return "From: " + this.sender + "\nTitle:" + this.title + "\nDate: " + date + "\nFlags: " + flags.toString()
                + "\n" + content;
    }


}