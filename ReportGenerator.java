import java.io.*;
import java.util.*;

public class ReportGenerator {

    static class UserStats {
        String username;
        List<Integer> scores = new ArrayList<>();

        public UserStats(String username) {
            this.username = username;
        }

        public int getBest() {
            return scores.stream().mapToInt(i -> i).max().orElse(0);
        }

        public int getTotalAttempts() {
            return scores.size();
        }

        public double getAverage() {
            return scores.stream().mapToInt(i -> i).average().orElse(0);
        }

        public int getTotalScore() {
            return scores.stream().mapToInt(i -> i).sum();
        }
    }

    public static void main(String[] args) {
        File reportFile = new File("report.txt");
        File summaryFile = new File("summary_report.txt");

        Map<String, UserStats> userMap = new HashMap<>();
        int totalAttempts = 0;
        int totalScore = 0;
        int totalQuestions = 3;

        try (BufferedReader br = new BufferedReader(new FileReader(reportFile))) {
            String line;
            String username = null;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("Username:")) {
                    username = line.split(":", 2)[1].trim();
                    userMap.putIfAbsent(username, new UserStats(username));
                } else if (line.startsWith("Scores:") && username != null) {
                    String[] parts = line.split(":", 2)[1].trim().split(",");
                    for (String part : parts) {
                        int score = Integer.parseInt(part.trim());
                        userMap.get(username).scores.add(score);
                        totalScore += score;
                        totalAttempts++;
                    }
                } else if (line.startsWith("Best:")) {
                    // optional, ignored here
                } else if (line.startsWith("Generated At:")) {
                    totalQuestions = Math.max(totalQuestions, 3); 
                }
            }

            if (totalAttempts == 0) {
                System.out.println("No attempts found.");
                return;
            }

            
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(summaryFile))) {
                bw.write("===== QUIZ SUMMARY REPORT =====\n");
                bw.write("Total Users: " + userMap.size() + "\n");
                bw.write("Total Attempts: " + totalAttempts + "\n");
                bw.write("Average Score: " + String.format("%.2f", (double) totalScore / totalAttempts*totalQuestions) + "\n");

                UserStats topUser = null, lowUser = null;
                double maxAvg = -1, minAvg = 101;

                for (UserStats user : userMap.values()) {
                    double avg = user.getAverage();
                    if (avg > maxAvg) {
                        maxAvg = avg;
                        topUser = user;
                    }
                    if (avg < minAvg) {
                        minAvg = avg;
                        lowUser = user;
                    }
                }

                bw.write("Best Performer: " + (topUser != null ? topUser.username : "N/A") + " (Avg: " + String.format("%.2f", maxAvg) + ")\n");
                bw.write("Least Performer: " + (lowUser != null ? lowUser.username : "N/A") + " (Avg: " + String.format("%.2f", minAvg) + ")\n");

                double accuracy = (double) totalScore / (totalAttempts * totalQuestions) * 100;
                bw.write("Overall Accuracy: " + String.format("%.2f", accuracy) + "%\n\n");

                bw.write("===== INDIVIDUAL REPORTS =====\n");
                for (UserStats user : userMap.values()) {
                    bw.write("User: " + user.username + "\n");
                    bw.write("  Attempts: " + user.getTotalAttempts() + "\n");
                    bw.write("  Scores: " + user.scores + "\n");
                    bw.write("  Best Score: " + user.getBest() + "\n");
                    bw.write("  Average: " + String.format("%.2f", user.getAverage()) + "\n");
                    bw.write("---------------------------------\n");
                }
                System.out.println("✅ Summary report generated in 'summary_report.txt'");
            }

        } catch (IOException e) {
            System.out.println("⚠️ Error reading report: " + e.getMessage());
        }
    }
}
