import java.io.*;
import java.util.*;

public class FileManager {

    public static class QuestionData {
        public String questionText;
        public String[] options;
        public String correctAnswer;

        public QuestionData(String questionText, String[] options, String correctAnswer) {
            this.questionText = questionText;
            this.options = options;
            this.correctAnswer = correctAnswer;
        }
    }

    public static List<QuestionData> loadQuestions(String filePath) {
        List<QuestionData> questions = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length != 3) continue;
                String[] opts = parts[1].split(",");
                questions.add(new QuestionData(parts[0], opts, parts[2]));
            }
        } catch (IOException e) {
            System.out.println("Error reading questions: " + e.getMessage());
        }
        return questions;
    }

    public static void saveUserReport(String username, List<Integer> scores, String reportPath) {
        try (FileWriter fw = new FileWriter(reportPath, true);
             BufferedWriter bw = new BufferedWriter(fw)) {

            int best = scores.stream().max(Integer::compareTo).orElse(0);
            bw.write("Username: " + username + "\n");
            bw.write("Attempts: " + scores.size() + "\n");
            bw.write("Scores: " + scores.toString().replace("[", "").replace("]", "") + "\n");
            bw.write("Best: " + best + "\n");
            bw.write("-----------------------------\n");
        } catch (IOException e) {
            System.out.println("Error writing report: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        List<FileManager.QuestionData> data = FileManager.loadQuestions("questions.txt");
        for (FileManager.QuestionData q : data) {
            System.out.println(q.questionText);
            System.out.println(Arrays.toString(q.options));
            System.out.println("Answer: " + q.correctAnswer);
        }

        List<Integer> testScores = Arrays.asList(3, 2, 4);
        FileManager.saveUserReport("sanjay", testScores, "report.txt");
    }
}
