package Engine;

import com.detectlanguage.Result;
import com.detectlanguage.errors.APIError;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.CoreMap;

import com.detectlanguage.DetectLanguage;

import javax.mail.*;
import java.io.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

class Email {

    final static String API_KEY = "4f4d63ac606a0ee5e0064aa296ce88b4";
    private double VNEGTHRESH;
    private double NEGTHRESH;
    private double NEUTHRESH;
    private double POSTHRESH;
    private ArrayList<String> sentences, languages;
    private Message message;
    Sentiment sentenceSentiment;
    private int[] sentimentScores;
    private int sentencesAnalyzed;
    private double sentimentPct;
    private String content, title, sentimentPctStr;
    private Date date;
    private Sender sender;
    private Flags flags;
    private int VNEG;
    private int NEG;
    private int NEU;
    private int POS;
    private int VPOS;
    private int VMULT;
    private int MAXLEN;
    private String folder;
    File serializedEmail;

    public Email(File f) {
        //to do: recreate emails using this constructor

        recoverEmail(f);


        VNEGTHRESH = .7;
        NEGTHRESH = .65;
        NEUTHRESH = .5;
        POSTHRESH = .3;
        VNEG = 0;
        NEG = 1;
        NEU = 2;
        POS = 3;
        VPOS = 4;
        VMULT = 3;
        MAXLEN = 300;
        sentimentScores = new int[5];
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


    public Email(Message m, Sender s, Boolean rs) throws APIError {

        VNEGTHRESH = .7;
        NEGTHRESH = .65;
        NEUTHRESH = .5;
        POSTHRESH = .3;
        VNEG = 0;
        NEG = 1;
        NEU = 2;
        POS = 3;
        VPOS = 4;
        VMULT = 3;
        message = m;
        sentimentScores = new int[5];
        sentencesAnalyzed = 0;
        MAXLEN = 300;
        boolean runSentiment = rs;

        try {
            //System.out.println("Content: \n" + m.getContent().toString());

            title = m.getSubject();
            sender = s;
            date = m.getSentDate();
            flags = m.getFlags();

        } catch (Exception e) {
            e.printStackTrace();
        }

        if (runSentiment) {
            try {
                content = getTextFromMessage(m);
            } catch (IOException | MessagingException e) {
                e.printStackTrace();
            }

            if (content != null) {
                sentences = getSentences(content);
                languages = getLanguages(sentences);
            }

            //System.out.println(sentences.toString());
            initializeSentiment();
        }
    }

    private void initializeSentiment() {
        int sentenceScore;
        double probability;
        Sentiment sentenceSentiment;
        if (sentences != null && languages.size() == 1 && languages.get(0).equals("en")) {
            for (String sentence : sentences) {
                if (sentence.length() < MAXLEN) {
                    //System.out.println(sentence);
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

            int overallSentiment = sentimentScores[VPOS] * VMULT + sentimentScores[POS] -
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

        try {
            if (message.isMimeType("text/plain")) {
                return message.getContent().toString();
            } else if (message.isMimeType("text/html")) {
                String html = (String) message.getContent();
                return org.jsoup.Jsoup.parse(html).text();
            } else if (message.isMimeType("multipart/*")) {
                Multipart mp = (Multipart) message.getContent();
                if (mp.getBodyPart(1).isMimeType("multipart/*")) {
                    return getTextFromBodyPart(mp.getBodyPart(0));
                }
                return mp.getBodyPart(1).getContent().toString();
            } else {
                throw new Exception("Unknown content type found while extracting message from multipart");
            }
        }
        catch(Exception e){
            System.out.println(e.getMessage());
        }
        return null;

    }

    private String getTextFromBodyPart(BodyPart bp) throws IOException, MessagingException {

        try {
            if (bp.isMimeType("text/plain")) {
                return message.getContent().toString();
            } else if (bp.isMimeType("text/html")) {
                String html = (String) bp.getContent();
                return org.jsoup.Jsoup.parse(html).text();
            } else {
                throw new Exception("Unknown content type found while extracting message from multipart");
            }
        }
        catch(Exception e){
            System.out.println(e.getMessage());
        }
        return null;
    }

    private ArrayList<String> getSentences(String result) {
        result = filter(result);
        //System.out.println("\nresults: " + result);
        ArrayList<String> sentences = new ArrayList<String>();
        String[] split = result.split("~|\\n");
        for (String s : split) {
            if (s.length() > 0) {
                String trimmed = s.trim();
                char[] c = trimmed.toCharArray();
                if (c.length > 0) {
                    if (c[0] >= 65 && c[0] <= 90 &&
                            (trimmed.endsWith(".") || trimmed.endsWith("!") || trimmed.endsWith("?")))
                        sentences.add(trimmed);
                }
            }
        }
        //System.out.println("\nsentences: " + sentences);
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
                "^(A|a)ug\\.", "^(S|s)ep\\.", "^(O|o)ct\\.", "^(N|n)ov\\.", "^(D|d)ec\\.", "p&nbsp"},
                {"Dr", "Mr", "Mrs", "Professor", "January", "Februrary", "March", "April", "June", "August",
                        "September", "October", "November", "December", " "}};

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

    private ArrayList<String> getLanguages(ArrayList<String> sentences) throws APIError {

        DetectLanguage.apiKey = API_KEY;

        ArrayList<String> langs = new ArrayList<>();

        for(String s : sentences) {

            List<Result> results = DetectLanguage.detect(s);
            Result cur;

            for (Result result : results) {
                cur = result;
                if (cur.isReliable)
                    langs.add(cur.language);
            }
        }

        return langs;
    }

    public ArrayList<String> getSentences() {
        return sentences;
    }

    public int[] getSentimentScores() {
        return sentimentScores;
    }

    public int getSentencesAnalyzed() {
        return sentencesAnalyzed;
    }

    public double getSentimentPct() {
        return sentimentPct;
    }

    public String getContent() {
        return content;
    }

    public String getTitle() {
        return title;
    }

    public String getSentimentPctStr() {
        return sentimentPctStr;
    }

    public Date getDate() {
        return date;
    }

    public Sender getSender() {
        return sender;
    }

    public Flags getFlags() {
        return flags;
    }

    public String getFolder() {
        return folder;
    }

    public String toString() {
        if (this.sentimentPctStr != null)
            return "From: " + this.sender + "\nTitle:" + this.title + "\nDate: " + date + "\nFlags: " + flags.toString()
                    + "\nSentiment: " + this.sentimentPctStr + "\n" + content;
        else
            return "From: " + this.sender + "\nTitle:" + this.title + "\nDate: " + date + "\nFlags: " + flags.toString()
                    + "\n" + content;
    }
}

