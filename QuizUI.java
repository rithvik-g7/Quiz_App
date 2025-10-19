import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import javax.swing.Timer;

public class QuizUI {
    private JFrame frame;
    private JLabel questionLabel, welcomeLabel, timerLabel;
    private JRadioButton[] optionButtons;
    private ButtonGroup buttonGroup;
    private JButton nextButton;
    private int currentQuestionIndex = 0;
    private int score = 0;
    private int timeLeft = 15;
    private Timer countdownTimer;

    private String username;
    private List<FileManager.QuestionData> questions;

    private static HashMap<String, List<Integer>> userAttempts = new HashMap<>();

    public QuizUI(String username, List<FileManager.QuestionData> questions) {
        this.username = username;
        this.questions = questions;

        userAttempts.putIfAbsent(username, new ArrayList<>());

        initializeUI();
        showQuestion();
    }

    private void initializeUI() {
        frame = new JFrame("🧠 BrainBrew Academy - Quiz App");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(700, 500);
        frame.setLayout(new BorderLayout());

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(52, 152, 219));

        welcomeLabel = new JLabel("Welcome, " + username + "!", SwingConstants.CENTER);
        welcomeLabel.setForeground(Color.WHITE);
        welcomeLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        header.add(welcomeLabel, BorderLayout.NORTH);

        questionLabel = new JLabel("Question will appear here", SwingConstants.CENTER);
        questionLabel.setForeground(Color.WHITE);
        questionLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        header.add(questionLabel, BorderLayout.CENTER);

        timerLabel = new JLabel("Time: 15s", SwingConstants.RIGHT);
        timerLabel.setForeground(Color.WHITE);
        timerLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        header.add(timerLabel, BorderLayout.EAST);

        frame.add(header, BorderLayout.NORTH);

        JPanel optionsPanel = new JPanel(new GridLayout(4, 1, 10, 10));
        optionsPanel.setBackground(Color.WHITE);
        optionsPanel.setBorder(BorderFactory.createEmptyBorder(20, 50, 20, 50));
        optionButtons = new JRadioButton[4];
        buttonGroup = new ButtonGroup();
        for (int i = 0; i < 4; i++) {
            optionButtons[i] = new JRadioButton();
            optionButtons[i].setFont(new Font("SansSerif", Font.PLAIN, 16));
            optionButtons[i].setBackground(Color.WHITE);
            buttonGroup.add(optionButtons[i]);
            optionsPanel.add(optionButtons[i]);
        }
        frame.add(optionsPanel, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel();
        bottomPanel.setBackground(new Color(236, 240, 241));
        nextButton = new JButton("Next");
        nextButton.setFocusPainted(false);
        nextButton.setFont(new Font("SansSerif", Font.BOLD, 16));
        nextButton.setBackground(new Color(46, 204, 113));
        nextButton.setForeground(Color.BLACK);
        nextButton.setPreferredSize(new Dimension(150, 40));
        nextButton.addActionListener(e -> handleNext());

        bottomPanel.add(nextButton);
        frame.add(bottomPanel, BorderLayout.SOUTH);

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private void showQuestion() {
        if (currentQuestionIndex < questions.size()) {
            FileManager.QuestionData q = questions.get(currentQuestionIndex);
            questionLabel.setText("Q" + (currentQuestionIndex + 1) + ": " + q.questionText);

            buttonGroup.clearSelection();
            for (int i = 0; i < 4; i++) {
                optionButtons[i].setText(q.options[i]);
            }

            startTimer();
        } else {
            endQuiz();
        }
    }

    private void startTimer() {
        timeLeft = 15;
        timerLabel.setText("Time: " + timeLeft + "s");

        if (countdownTimer != null && countdownTimer.isRunning()) {
            countdownTimer.stop();
        }

        countdownTimer = new Timer(1000, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                timeLeft--;
                timerLabel.setText("Time: " + timeLeft + "s");

                if (timeLeft <= 0) {
                    countdownTimer.stop();
                    JOptionPane.showMessageDialog(frame, "⏱ Time's up! Moving to next question.");
                    handleNext();
                }
            }
        });
        countdownTimer.start();
    }

    private void handleNext() {
        if (countdownTimer != null && countdownTimer.isRunning()) {
            countdownTimer.stop();
        }

        if (currentQuestionIndex >= questions.size()) return;

        int selectedIndex = -1;
        for (int i = 0; i < optionButtons.length; i++) {
            if (optionButtons[i].isSelected()) {
                selectedIndex = i;
                break;
            }
        }

        String selectedAnswer = (selectedIndex != -1) ? optionButtons[selectedIndex].getText() : "";
        String correctAnswer = questions.get(currentQuestionIndex).correctAnswer;

        if (selectedAnswer.equalsIgnoreCase(correctAnswer)) {
            score++;
        }

        currentQuestionIndex++;
        showQuestion();
    }

    private void endQuiz() {
        if (countdownTimer != null && countdownTimer.isRunning()) {
            countdownTimer.stop();
        }

        userAttempts.get(username).add(score);

        FileManager.saveUserReport(username, userAttempts.get(username), "report.txt");

        String message = "<html><body><h2>🎉 Quiz Completed!</h2>" +
                         "<p><b>User:</b> " + username + "</p>" +
                         "<p><b>Score:</b> " + score + " / " + questions.size() + "</p>" +
                         "<p>Your performance has been recorded.</p>"+
                         "<hr>"+
                         "<p>Do you want to retake the test?</p></body></html>";

        int option = JOptionPane.showConfirmDialog(
                frame, new JLabel(message),
                "Quiz Result", JOptionPane.YES_NO_OPTION,
                JOptionPane.INFORMATION_MESSAGE
        );

        if (option == JOptionPane.YES_OPTION) {
            currentQuestionIndex = 0;
            score = 0;
            showQuestion();
        } else {
            frame.dispose();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            String name = JOptionPane.showInputDialog("Enter your name:");
            if (name == null || name.trim().isEmpty()) {
                JOptionPane.showMessageDialog(null, "Name is required to start the quiz.");
                System.exit(0);
            }

            List<FileManager.QuestionData> questions = FileManager.loadQuestions("questions.txt");
            if (questions.isEmpty()) {
                JOptionPane.showMessageDialog(null, "No questions found in the file.");
                System.exit(0);
            }

            new QuizUI(name.trim(), questions);
        });
    }
}
