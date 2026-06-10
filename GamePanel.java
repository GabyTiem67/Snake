import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;

public class GamePanel extends JPanel implements ActionListener {
    static final int SCREEN_WIDTH = 1300;
    static final int SCREEN_HEIGHT = 750;
    static final int UNIT_SIZE = 50;
    static final int GAME_UNITS = (SCREEN_WIDTH * SCREEN_HEIGHT) / (UNIT_SIZE * UNIT_SIZE);
    static final int INITIAL_DELAY = 200;
    static final int DELAY_DECREMENT = 20;
    static final int MINIMUM_DELAY = 80;
    static final int POINTS_PER_SPEED_INCREASE = 15;
    static final int POINTS_PER_LEVEL = 20;
    static final int MAX_LEVEL = 5;
    static final int INITIAL_LIVES = 3;
    private int currentDelay = INITIAL_DELAY;
    private int lastSpeedIncreaseScore = 0;
    private final int[] x = new int[GAME_UNITS];
    private final int[] y = new int[GAME_UNITS];
    private final double[] renderX = new double[GAME_UNITS];
    private final double[] renderY = new double[GAME_UNITS];
    private int bodyParts = 6;
    private int applesEaten;
    private int smallApplesCount; // For spawning big apples
    private int totalSmallApplesEaten; // For tracking challenge progress
    private int highScore = 0;
    private int lives = INITIAL_LIVES;
    private int appleX;
    private int appleY;
    private boolean bigAppleActive = false;
    private int bigAppleX;
    private int bigAppleY;
    private long bigAppleStartTime;
    private static final int BIG_APPLE_DURATION = 5000;
    private static final int BIG_APPLE_SIZE = UNIT_SIZE * 2;
    private static final int SMALL_APPLES_FOR_BIG = 5;
    private char direction = 'R';
    private boolean running = false;
    private boolean paused = false;
    private Timer timer;
    private final Random random = new Random();
    private JButton restartButton;
    private JButton leaderboardButton;
    private JButton pauseButton;
    private JButton settingsButton;
    private JButton exitButton;
    private ArrayList<ScoreEntry> leaderboard = new ArrayList<>();
    private Color snakeHeadColor = Color.green;
    private Color snakeBodyColor = new Color(45, 180, 0);
    private Color backgroundColor = Color.black;
    private int currentLevel = 1;
    private int startingLevel = 1;
    private ArrayList<Rectangle> obstacles = new ArrayList<>();
    private enum GameMode { CLASSIC, ENDLESS, CHALLENGE }
    private GameMode gameMode = GameMode.CLASSIC;
    private int challengeIndex = 0;
    private long challengeStartTime;
    private boolean challengeActive = false;
    private String[] challenges = {
        "Collect 10 small apples in 30 seconds",
        "Survive for 30 seconds without hitting obstacles",
        "Eat 2 big apples in 20 seconds"
    };
    private int[] challengeGoals = {10, 30, 2};
    private int[] challengeTimeLimits = {30, 30, 20};
    private int challengeProgress = 0;
    private static final int ANIMATION_STEPS = 5;
    private int animationStep = 0;
    private double stepSize = (double) UNIT_SIZE / ANIMATION_STEPS;
    private ArrayList<Particle> particles = new ArrayList<>();

    private static class ScoreEntry {
        String name;
        int score;

        ScoreEntry(String name, int score) {
            this.name = name;
            this.score = score;
        }
    }

    private static class Particle {
        double x, y;
        double dx, dy;
        double alpha;
        Color color;

        Particle(double x, double y, double dx, double dy, Color color) {
            this.x = x;
            this.y = y;
            this.dx = dx;
            this.dy = dy;
            this.alpha = 1.0;
            this.color = color;
        }

        void update() {
            x += dx;
            y += dy;
            alpha -= 0.05;
        }

        void draw(Graphics g) {
            if (alpha <= 0) return;
            Graphics2D g2d = (Graphics2D) g;
            g2d.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), (int) (alpha * 255)));
            g2d.fillOval((int) x, (int) y, 5, 5);
        }
    }

    public GamePanel() {
        this.setPreferredSize(new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT));
        this.setBackground(backgroundColor);
        this.setFocusable(true);
        this.addKeyListener(new MyKeyAdapter());
        initButtons();
        showInstructions();
        startGame();
    }

    private void initButtons() {
        restartButton = new JButton("Restart");
        restartButton.setFont(new Font("Ink Free", Font.BOLD, 30));
        restartButton.setForeground(Color.red);
        restartButton.setBackground(backgroundColor);
        restartButton.setFocusPainted(false);
        restartButton.setVisible(false);
        restartButton.addActionListener(e -> restartGame());
        this.setLayout(null);
        restartButton.setBounds(SCREEN_WIDTH/2 - 100, SCREEN_HEIGHT/2 + 50, 200, 50);
        this.add(restartButton);

        leaderboardButton = new JButton("Leaderboard");
        leaderboardButton.setFont(new Font("Ink Free", Font.BOLD, 30));
        leaderboardButton.setForeground(Color.red);
        leaderboardButton.setBackground(backgroundColor);
        leaderboardButton.setFocusPainted(false);
        leaderboardButton.setVisible(false);
        leaderboardButton.addActionListener(e -> showLeaderboard());
        leaderboardButton.setBounds(SCREEN_WIDTH/2 - 100, SCREEN_HEIGHT/2 + 120, 200, 50);
        this.add(leaderboardButton);

        pauseButton = new JButton("Pause");
        pauseButton.setFont(new Font("Ink Free", Font.BOLD, 20));
        pauseButton.setForeground(Color.red);
        pauseButton.setBackground(backgroundColor);
        pauseButton.setFocusPainted(false);
        pauseButton.addActionListener(e -> togglePause());
        pauseButton.setBounds(SCREEN_WIDTH - 120, 10, 100, 40);
        this.add(pauseButton);

        settingsButton = new JButton("Settings");
        settingsButton.setFont(new Font("Ink Free", Font.BOLD, 20));
        settingsButton.setForeground(Color.red);
        settingsButton.setBackground(backgroundColor);
        settingsButton.setFocusPainted(false);
        settingsButton.addActionListener(e -> openSettings());
        settingsButton.setBounds(SCREEN_WIDTH - 240, 10, 100, 40);
        this.add(settingsButton);

        exitButton = new JButton("Exit");
        exitButton.setFont(new Font("Ink Free", Font.BOLD, 20));
        exitButton.setForeground(Color.red);
        exitButton.setBackground(backgroundColor);
        exitButton.setFocusPainted(false);
        exitButton.addActionListener(e -> System.exit(0));
        exitButton.setBounds(SCREEN_WIDTH - 360, 10, 100, 40);
        this.add(exitButton);
    }

    private void showInstructions() {
        JOptionPane.showMessageDialog(this, 
            "Welcome to Snake!\n\n" +
            "Instructions:\n" +
            "- Use arrow keys to move the snake.\n" +
            "- Eat small apples (1 point each) to grow.\n" +
            "- After 5 small apples, a big apple (3 points) appears for 3 seconds.\n" +
            "- Snake speed increases every 15 points!\n" +
            "- Advance to the next level every 35 points (up to Level 5)!\n" +
            "- After Level 5, continue playing until game over (Classic Mode).\n" +
            "- Classic Mode: 3 lives; lose a life on collision, game over when all lives are lost.\n" +
            "- Endless Mode: No game over; reset on collision and keep scoring.\n" +
            "- Challenge Mode: Complete objectives to progress (e.g., collect apples, survive time).\n" +
            "- Game pauses on level-up; press SPACEBAR or click Resume to continue.\n" +
            "- Watch out for obstacles that increase with each level!\n" +
            "- Press SPACEBAR to pause/unpause the game.\n" +
            "- Click the Settings button to change colors, starting level, and game mode.\n" +
            "- Avoid hitting the walls, obstacles, or yourself (except in Endless Mode)!\n\n" +
            "Click OK to start playing!",
            "Snake Game Instructions", 
            JOptionPane.INFORMATION_MESSAGE);
        this.requestFocusInWindow();
    }

    private void openSettings() {
        boolean wasPaused = paused;
        if (running && !paused) {
            togglePause();
        }

        JDialog settingsDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Settings", true);
        settingsDialog.setLayout(new GridLayout(6, 1, 10, 10));
        settingsDialog.setSize(300, 300);
        settingsDialog.setLocationRelativeTo(this);

        JButton headColorButton = new JButton("Change Snake Head Color");
        headColorButton.addActionListener(e -> {
            Color newColor = JColorChooser.showDialog(settingsDialog, "Choose Snake Head Color", snakeHeadColor);
            if (newColor != null) {
                snakeHeadColor = newColor;
                repaint();
            }
        });

        JButton bodyColorButton = new JButton("Change Snake Body Color");
        bodyColorButton.addActionListener(e -> {
            Color newColor = JColorChooser.showDialog(settingsDialog, "Choose Snake Body Color", snakeBodyColor);
            if (newColor != null) {
                snakeBodyColor = newColor;
                repaint();
            }
        });

        JButton bgColorButton = new JButton("Change Background Color");
        bgColorButton.addActionListener(e -> {
            Color newColor = JColorChooser.showDialog(settingsDialog, "Choose Background Color", backgroundColor);
            if (newColor != null) {
                backgroundColor = newColor;
                this.setBackground(backgroundColor);
                restartButton.setBackground(backgroundColor);
                leaderboardButton.setBackground(backgroundColor);
                pauseButton.setBackground(backgroundColor);
                settingsButton.setBackground(backgroundColor);
                exitButton.setBackground(backgroundColor);
                repaint();
            }
        });

        JButton levelButton = new JButton("Choose Starting Level");
        levelButton.addActionListener(e -> {
            String[] levels = {"1", "2", "3", "4", "5"};
            String selectedLevel = (String) JOptionPane.showInputDialog(
                settingsDialog,
                "Select starting level (1-5):",
                "Choose Starting Level",
                JOptionPane.PLAIN_MESSAGE,
                null,
                levels,
                String.valueOf(startingLevel)
            );
            if (selectedLevel != null) {
                startingLevel = Integer.parseInt(selectedLevel);
                bodyParts = 6;
                applesEaten = 0;
                lives = INITIAL_LIVES;
                direction = 'R';
                setSafeStartingPosition();
                currentLevel = startingLevel;
                obstacles.clear();
                generateObstacles(currentLevel);
                currentDelay = Math.max(MINIMUM_DELAY, INITIAL_DELAY - (currentLevel - 1) * DELAY_DECREMENT);
                newApple();
                smallApplesCount = 0;
                totalSmallApplesEaten = 0;
                bigAppleActive = false;
                lastSpeedIncreaseScore = 0;
                challengeIndex = 0;
                challengeActive = false;
                challengeProgress = 0;
                if (timer != null) {
                    timer.stop();
                }
                timer = new Timer(currentDelay / ANIMATION_STEPS, this);
                repaint();
            }
        });

        JButton modeButton = new JButton("Choose Game Mode");
        modeButton.addActionListener(e -> {
            String[] modes = {"Classic", "Endless", "Challenge"};
            String selectedMode = (String) JOptionPane.showInputDialog(
                settingsDialog,
                "Select game mode:",
                "Choose Game Mode",
                JOptionPane.PLAIN_MESSAGE,
                null,
                modes,
                gameMode.toString()
            );
            if (selectedMode != null) {
                gameMode = GameMode.valueOf(selectedMode.toUpperCase());
                bodyParts = 6;
                applesEaten = 0;
                lives = INITIAL_LIVES;
                direction = 'R';
                setSafeStartingPosition();
                currentLevel = startingLevel;
                obstacles.clear();
                generateObstacles(currentLevel);
                currentDelay = Math.max(MINIMUM_DELAY, INITIAL_DELAY - (currentLevel - 1) * DELAY_DECREMENT);
                newApple();
                smallApplesCount = 0;
                totalSmallApplesEaten = 0;
                bigAppleActive = false;
                lastSpeedIncreaseScore = 0;
                challengeIndex = 0;
                challengeActive = false;
                challengeProgress = 0;
                if (timer != null) {
                    timer.stop();
                }
                timer = new Timer(currentDelay / ANIMATION_STEPS, this);
                repaint();
            }
        });

        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> settingsDialog.dispose());

        settingsDialog.add(headColorButton);
        settingsDialog.add(bodyColorButton);
        settingsDialog.add(bgColorButton);
        settingsDialog.add(levelButton);
        settingsDialog.add(modeButton);
        settingsDialog.add(closeButton);
        settingsDialog.setVisible(true);

        this.requestFocusInWindow();

        if (running && !wasPaused) {
            togglePause();
        }
    }

    private void setSafeStartingPosition() {
        int startX = SCREEN_WIDTH / 2 - (bodyParts * UNIT_SIZE) / 2;
        int startY = SCREEN_HEIGHT / 2;
        startX = (startX / UNIT_SIZE) * UNIT_SIZE;
        startY = (startY / UNIT_SIZE) * UNIT_SIZE;
        while (isOnObstacle(startX, startY)) {
            startX += UNIT_SIZE;
            if (startX >= SCREEN_WIDTH - bodyParts * UNIT_SIZE) {
                startX = UNIT_SIZE;
                startY += UNIT_SIZE;
            }
            if (startY >= SCREEN_HEIGHT) {
                startY = UNIT_SIZE;
            }
        }
        for (int i = 0; i < bodyParts; i++) {
            x[i] = startX + (bodyParts - i - 1) * UNIT_SIZE;
            y[i] = startY;
            renderX[i] = x[i];
            renderY[i] = y[i];
        }
    }

    public void startGame() {
        setSafeStartingPosition();
        currentLevel = startingLevel;
        obstacles.clear();
        generateObstacles(currentLevel);
        currentDelay = Math.max(MINIMUM_DELAY, INITIAL_DELAY - (currentLevel - 1) * DELAY_DECREMENT);
        newApple();
        running = true;
        paused = false;
        smallApplesCount = 0;
        totalSmallApplesEaten = 0;
        bigAppleActive = false;
        applesEaten = 0;
        lives = INITIAL_LIVES;
        lastSpeedIncreaseScore = 0;
        challengeIndex = 0;
        challengeActive = false;
        challengeProgress = 0;
        animationStep = 0;
        particles.clear();
        if (timer != null) {
            timer.stop();
        }
        timer = new Timer(currentDelay / ANIMATION_STEPS, this);
        timer.start();
        restartButton.setVisible(false);
        leaderboardButton.setVisible(false);
        pauseButton.setText("Pause");
        this.requestFocusInWindow();
    }

    private void restartGame() {
        bodyParts = 6;
        applesEaten = 0;
        lives = INITIAL_LIVES;
        direction = 'R';
        startGame();
    }

    private void togglePause() {
        if (running) {
            paused = !paused;
            if (paused) {
                timer.stop();
                pauseButton.setText("Resume");
            } else {
                timer.start();
                pauseButton.setText("Pause");
            }
            repaint();
            this.requestFocusInWindow();
        }
    }

    private void updateSpeed() {
        int scoreThreshold = (applesEaten / POINTS_PER_SPEED_INCREASE) * POINTS_PER_SPEED_INCREASE;
        if (applesEaten >= lastSpeedIncreaseScore + POINTS_PER_SPEED_INCREASE) {
            lastSpeedIncreaseScore = scoreThreshold;
            currentDelay = Math.max(MINIMUM_DELAY, currentDelay - DELAY_DECREMENT);
            timer.stop();
            timer = new Timer(currentDelay / ANIMATION_STEPS, this);
            timer.start();
        }
    }

    private void generateObstacles(int level) {
        obstacles.clear();
        if (level >= 2) {
            obstacles.add(new Rectangle(0, 0, UNIT_SIZE * 2, UNIT_SIZE * 2));
            obstacles.add(new Rectangle(SCREEN_WIDTH - UNIT_SIZE * 2, SCREEN_HEIGHT - UNIT_SIZE * 2, UNIT_SIZE * 2, UNIT_SIZE * 2));
        }
        if (level >= 3) {
            int centerX = SCREEN_WIDTH / 2 - UNIT_SIZE;
            int centerY = SCREEN_HEIGHT / 2 - UNIT_SIZE;
            obstacles.add(new Rectangle(centerX, centerY, UNIT_SIZE * 2, UNIT_SIZE * 2));
        }
        if (level >= 4) {
            obstacles.add(new Rectangle(UNIT_SIZE * 5, UNIT_SIZE * 5, UNIT_SIZE, UNIT_SIZE));
            obstacles.add(new Rectangle(UNIT_SIZE * 10, UNIT_SIZE * 10, UNIT_SIZE, UNIT_SIZE));
            obstacles.add(new Rectangle(UNIT_SIZE * 15, UNIT_SIZE * 15, UNIT_SIZE, UNIT_SIZE));
        }
        if (level >= 5) {
            int borderThickness = UNIT_SIZE;
            obstacles.add(new Rectangle(0, 0, SCREEN_WIDTH, borderThickness));
            obstacles.add(new Rectangle(0, SCREEN_HEIGHT - borderThickness, SCREEN_WIDTH, borderThickness));
            obstacles.add(new Rectangle(0, 0, borderThickness, SCREEN_HEIGHT));
            obstacles.add(new Rectangle(SCREEN_WIDTH - borderThickness, 0, borderThickness, SCREEN_HEIGHT));
        }
    }

    private void advanceToNextLevel() {
        if (currentLevel < MAX_LEVEL) {
            currentLevel++;
            generateObstacles(currentLevel);
            currentDelay = Math.max(MINIMUM_DELAY, INITIAL_DELAY - (currentLevel - 1) * DELAY_DECREMENT);
            timer.stop();
            timer = new Timer(currentDelay / ANIMATION_STEPS, this);
            paused = true;
            pauseButton.setText("Resume");
            JOptionPane.showMessageDialog(this, "Level Up! Now on Level " + currentLevel + "\nPress SPACEBAR or click Resume to continue.");
            repaint();
            this.requestFocusInWindow();
        }
    }

    private void startChallenge() {
        if (challengeIndex >= challenges.length) {
            running = false;
            timer.stop();
            JOptionPane.showMessageDialog(this, "Congratulations! You completed all challenges!\nFinal Score: " + applesEaten);
            promptForName();
            restartButton.setVisible(true);
            leaderboardButton.setVisible(true);
            return;
        }
        challengeActive = true;
        challengeStartTime = System.currentTimeMillis();
        challengeProgress = 0;
        totalSmallApplesEaten = 0; // Reset for the new challenge
        smallApplesCount = 0;
        JOptionPane.showMessageDialog(this, "Challenge " + (challengeIndex + 1) + ": " + challenges[challengeIndex]);
        this.requestFocusInWindow();
    }

    private void updateChallenge() {
        if (!challengeActive) return;
        long elapsedTime = (System.currentTimeMillis() - challengeStartTime) / 1000;
        int timeLimit = challengeTimeLimits[challengeIndex];
        if (elapsedTime >= timeLimit) {
            running = false;
            timer.stop();
            JOptionPane.showMessageDialog(this, "Challenge Failed! Time's up.");
            promptForName();
            restartButton.setVisible(true);
            leaderboardButton.setVisible(true);
            return;
        }

        boolean challengeCompleted = false;
        switch (challengeIndex) {
            case 0: // Collect 10 small apples
                challengeProgress = totalSmallApplesEaten;
                if (challengeProgress >= challengeGoals[0]) {
                    challengeCompleted = true;
                }
                break;
            case 1: // Survive for 30 seconds
                challengeProgress = (int) elapsedTime;
                if (challengeProgress >= challengeGoals[1]) {
                    challengeCompleted = true;
                }
                break;
            case 2: // Eat 2 big apples
                if (challengeProgress >= challengeGoals[2]) {
                    challengeCompleted = true;
                }
                break;
        }

        if (challengeCompleted) {
            challengeIndex++;
            challengeActive = false;
            JOptionPane.showMessageDialog(this, "Challenge Completed!");
            startChallenge();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    private void draw(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (running) {
            g2d.setColor(Color.gray);
            for (Rectangle obstacle : obstacles) {
                g2d.fillRect(obstacle.x, obstacle.y, obstacle.width, obstacle.height);
            }

            if (!bigAppleActive) {
                g2d.setColor(Color.red);
                g2d.fillOval(appleX, appleY, UNIT_SIZE, UNIT_SIZE);
            }
            if (bigAppleActive) {
                g2d.setColor(Color.orange);
                g2d.fillOval(bigAppleX, bigAppleY, BIG_APPLE_SIZE, BIG_APPLE_SIZE);
            }

            // Draw snake with enhanced graphics
            for (int i = 0; i < bodyParts; i++) {
                int posX = (int) renderX[i];
                int posY = (int) renderY[i];

                // Gradient paint for head and body
                Color startColor = (i == 0) ? snakeHeadColor : snakeBodyColor;
                Color endColor = startColor.darker();
                GradientPaint gradient = new GradientPaint(posX, posY, startColor, posX + UNIT_SIZE, posY + UNIT_SIZE, endColor);
                g2d.setPaint(gradient);

                // Draw rounded rectangles
                int arcSize = UNIT_SIZE / 2; // For rounded corners
                g2d.fillRoundRect(posX, posY, UNIT_SIZE, UNIT_SIZE, arcSize, arcSize);

                // Draw body pattern (scales)
                if (i != 0) { // Skip head
                    g2d.setColor(endColor.brighter());
                    g2d.setStroke(new BasicStroke(2));
                    // Draw a diagonal line to simulate a scale
                    g2d.drawLine(posX + 5, posY + UNIT_SIZE - 5, posX + UNIT_SIZE - 5, posY + 5);
                }

                // Draw eyes on the head
                if (i == 0) {
                    g2d.setColor(Color.white);
                    int eyeSize = UNIT_SIZE / 4;
                    int pupilSize = eyeSize / 2;
                    int offset = UNIT_SIZE / 4;

                    // Adjust eye positions based on direction
                    int leftEyeX = posX, leftEyeY = posY, rightEyeX = posX, rightEyeY = posY;
                    int leftPupilX = posX, leftPupilY = posY, rightPupilX = posX, rightPupilY = posY;

                    switch (direction) {
                        case 'R':
                            leftEyeX = posX + UNIT_SIZE - offset - eyeSize;
                            leftEyeY = posY + offset;
                            rightEyeX = posX + UNIT_SIZE - offset - eyeSize;
                            rightEyeY = posY + UNIT_SIZE - offset - eyeSize;
                            leftPupilX = leftEyeX + eyeSize - pupilSize;
                            leftPupilY = leftEyeY + pupilSize / 2;
                            rightPupilX = rightEyeX + eyeSize - pupilSize;
                            rightPupilY = rightEyeY + pupilSize / 2;
                            break;
                        case 'L':
                            leftEyeX = posX + offset;
                            leftEyeY = posY + offset;
                            rightEyeX = posX + offset;
                            rightEyeY = posY + UNIT_SIZE - offset - eyeSize;
                            leftPupilX = leftEyeX;
                            leftPupilY = leftEyeY + pupilSize / 2;
                            rightPupilX = rightEyeX;
                            rightPupilY = rightEyeY + pupilSize / 2;
                            break;
                        case 'U':
                            leftEyeX = posX + offset;
                            leftEyeY = posY + offset;
                            rightEyeX = posX + UNIT_SIZE - offset - eyeSize;
                            rightEyeY = posY + offset;
                            leftPupilX = leftEyeX + pupilSize / 2;
                            leftPupilY = leftEyeY;
                            rightPupilX = rightEyeX + pupilSize / 2;
                            rightPupilY = rightEyeY;
                            break;
                        case 'D':
                            leftEyeX = posX + offset;
                            leftEyeY = posY + UNIT_SIZE - offset - eyeSize;
                            rightEyeX = posX + UNIT_SIZE - offset - eyeSize;
                            rightEyeY = posY + UNIT_SIZE - offset - eyeSize;
                            leftPupilX = leftEyeX + pupilSize / 2;
                            leftPupilY = leftEyeY + eyeSize - pupilSize;
                            rightPupilX = rightEyeX + pupilSize / 2;
                            rightPupilY = rightEyeY + eyeSize - pupilSize;
                            break;
                    }

                    // Draw eyes
                    g2d.fillOval(leftEyeX, leftEyeY, eyeSize, eyeSize);
                    g2d.fillOval(rightEyeX, rightEyeY, eyeSize, eyeSize);

                    // Draw pupils
                    g2d.setColor(Color.black);
                    g2d.fillOval(leftPupilX, leftPupilY, pupilSize, pupilSize);
                    g2d.fillOval(rightPupilX, rightPupilY, pupilSize, pupilSize);
                }
            }

            for (Particle particle : particles) {
                particle.draw(g2d);
            }

            g2d.setColor(Color.red);
            g2d.setFont(new Font("Ink Free", Font.BOLD, 40));
            FontMetrics metrics = getFontMetrics(g2d.getFont());
            String scoreText = "Score: " + applesEaten + "  High Score: " + highScore;
            g2d.drawString(scoreText, (SCREEN_WIDTH - metrics.stringWidth(scoreText)) / 2, g2d.getFont().getSize());

            g2d.setColor(Color.white);
            g2d.setFont(new Font("Ink Free", Font.BOLD, 30));
            g2d.drawString("Level: " + currentLevel, 10, 30);
            g2d.drawString("Lives: " + lives, 10, 60);
            g2d.drawString("Mode: " + gameMode, 10, 90);

            if (gameMode == GameMode.CHALLENGE && challengeActive) {
                long elapsedTime = (System.currentTimeMillis() - challengeStartTime) / 1000;
                int timeLeft = challengeTimeLimits[challengeIndex] - (int) elapsedTime;
                g2d.drawString("Challenge: " + challenges[challengeIndex], 10, 120);
                g2d.drawString("Progress: " + challengeProgress + "/" + challengeGoals[challengeIndex], 10, 150);
                g2d.drawString("Time Left: " + timeLeft + "s", 10, 180);
            }

            if (paused) {
                g2d.setColor(Color.white);
                g2d.setFont(new Font("Ink Free", Font.BOLD, 75));
                metrics = getFontMetrics(g2d.getFont());
                g2d.drawString("Paused", (SCREEN_WIDTH - metrics.stringWidth("Paused")) / 2, SCREEN_HEIGHT / 2);
            }
        } else {
            gameOver(g2d);
        }
    }

    private void newApple() {
        boolean validPosition;
        do {
            validPosition = true;
            appleX = random.nextInt(SCREEN_WIDTH / UNIT_SIZE) * UNIT_SIZE;
            appleY = random.nextInt(SCREEN_HEIGHT / UNIT_SIZE) * UNIT_SIZE;
            Rectangle appleRect = new Rectangle(appleX, appleY, UNIT_SIZE, UNIT_SIZE);
            for (Rectangle obstacle : obstacles) {
                if (obstacle.intersects(appleRect)) {
                    validPosition = false;
                    break;
                }
            }
        } while (!validPosition);
    }

    private void newBigApple() {
        boolean validPosition;
        do {
            validPosition = true;
            bigAppleX = random.nextInt((SCREEN_WIDTH - BIG_APPLE_SIZE) / UNIT_SIZE) * UNIT_SIZE;
            bigAppleY = random.nextInt((SCREEN_HEIGHT - BIG_APPLE_SIZE) / UNIT_SIZE) * UNIT_SIZE;
            Rectangle bigAppleRect = new Rectangle(bigAppleX, bigAppleY, BIG_APPLE_SIZE, BIG_APPLE_SIZE);
            for (Rectangle obstacle : obstacles) {
                if (obstacle.intersects(bigAppleRect)) {
                    validPosition = false;
                    break;
                }
            }
        } while (!validPosition);
        bigAppleActive = true;
        bigAppleStartTime = System.currentTimeMillis();
    }

    private boolean isOnObstacle(int x, int y) {
        for (Rectangle obstacle : obstacles) {
            if (obstacle.contains(x, y)) {
                return true;
            }
        }
        return false;
    }

    private void move() {
        animationStep++;
        if (animationStep >= ANIMATION_STEPS) {
            for (int i = bodyParts - 1; i > 0; i--) {
                x[i] = x[i - 1];
                y[i] = y[i - 1];
            }
            switch (direction) {
                case 'U': y[0] -= UNIT_SIZE; break;
                case 'D': y[0] += UNIT_SIZE; break;
                case 'L': x[0] -= UNIT_SIZE; break;
                case 'R': x[0] += UNIT_SIZE; break;
            }
            animationStep = 0;
        }

        for (int i = 0; i < bodyParts; i++) {
            double targetX = x[i];
            double targetY = y[i];
            double currentX = renderX[i];
            double currentY = renderY[i];
            renderX[i] = currentX + (targetX - currentX) * ((double) animationStep / ANIMATION_STEPS);
            renderY[i] = currentY + (targetY - currentY) * ((double) animationStep / ANIMATION_STEPS);
        }
    }

    private void spawnParticles(int appleX, int appleY, Color color) {
        for (int i = 0; i < 10; i++) {
            double angle = random.nextDouble() * 2 * Math.PI;
            double speed = random.nextDouble() * 2 + 1;
            double dx = Math.cos(angle) * speed;
            double dy = Math.sin(angle) * speed;
            particles.add(new Particle(appleX + UNIT_SIZE / 2, appleY + UNIT_SIZE / 2, dx, dy, color));
        }
    }

    private void updateParticles() {
        for (int i = particles.size() - 1; i >= 0; i--) {
            Particle particle = particles.get(i);
            particle.update();
            if (particle.alpha <= 0) {
                particles.remove(i);
            }
        }
    }

    private void checkApple() {
        if (!bigAppleActive && x[0] == appleX && y[0] == appleY) {
            int tailX = x[bodyParts - 1];
            int tailY = y[bodyParts - 1];
            bodyParts++;
            applesEaten++;
            smallApplesCount++;
            totalSmallApplesEaten++; // Increment total small apples for challenge
            highScore = Math.max(highScore, applesEaten);
            x[bodyParts - 1] = tailX;
            y[bodyParts - 1] = tailY;
            renderX[bodyParts - 1] = tailX;
            renderY[bodyParts - 1] = tailY;
            spawnParticles(appleX, appleY, Color.red);
            newApple();
            if (smallApplesCount >= SMALL_APPLES_FOR_BIG) {
                newBigApple();
                smallApplesCount = 0; // Reset only the counter for big apple spawning
            }
            updateSpeed();
            if (gameMode == GameMode.CHALLENGE) {
                updateChallenge();
            }
        }
        if (bigAppleActive) {
            if (System.currentTimeMillis() - bigAppleStartTime >= BIG_APPLE_DURATION) {
                bigAppleActive = false;
                return;
            }
            Rectangle snakeHead = new Rectangle(x[0], y[0], UNIT_SIZE, UNIT_SIZE);
            Rectangle bigApple = new Rectangle(bigAppleX, bigAppleY, BIG_APPLE_SIZE, BIG_APPLE_SIZE);
            if (snakeHead.intersects(bigApple)) {
                int tailX = x[bodyParts - 1];
                int tailY = y[bodyParts - 1];
                bodyParts++;
                applesEaten += 3;
                highScore = Math.max(highScore, applesEaten);
                x[bodyParts - 1] = tailX;
                y[bodyParts - 1] = tailY;
                renderX[bodyParts - 1] = tailX;
                renderY[bodyParts - 1] = tailY;
                spawnParticles(bigAppleX + BIG_APPLE_SIZE / 2, bigAppleY + BIG_APPLE_SIZE / 2, Color.orange);
                bigAppleActive = false;
                updateSpeed();
                if (gameMode == GameMode.CHALLENGE && challengeIndex == 2) {
                    challengeProgress++;
                    updateChallenge();
                }
            }
        }
    }

    private void checkCollisions() {
        boolean collision = false;

        if (x[0] < 0 || x[0] >= SCREEN_WIDTH || y[0] < 0 || y[0] >= SCREEN_HEIGHT) {
            collision = true;
        }

        for (int i = bodyParts - 1; i > 0; i--) {
            if (x[0] == x[i] && y[0] == y[i]) {
                collision = true;
                break;
            }
        }

        for (Rectangle obstacle : obstacles) {
            if (obstacle.contains(x[0], y[0])) {
                collision = true;
                break;
            }
        }

        if (collision) {
            if (gameMode == GameMode.CLASSIC) {
                lives--;
                if (lives <= 0) {
                    running = false;
                    timer.stop();
                    promptForName();
                    restartButton.setVisible(true);
                    leaderboardButton.setVisible(true);
                } else {
                    setSafeStartingPosition();
                    direction = 'R';
                    bodyParts = 6;
                    newApple();
                    repaint();
                }
            } else if (gameMode == GameMode.ENDLESS) {
                setSafeStartingPosition();
                direction = 'R';
                bodyParts = 6;
                newApple();
                repaint();
            } else if (gameMode == GameMode.CHALLENGE) {
                if (challengeIndex == 1) {
                    running = false;
                    timer.stop();
                    JOptionPane.showMessageDialog(this, "Challenge Failed! You hit an obstacle.");
                    promptForName();
                    restartButton.setVisible(true);
                    leaderboardButton.setVisible(true);
                } else {
                    setSafeStartingPosition();
                    direction = 'R';
                    bodyParts = 6;
                    newApple();
                    repaint();
                }
            }
        }
    }

    private void promptForName() {
        String name = JOptionPane.showInputDialog(this, "Game Over! Enter your name:", "Score: " + applesEaten, JOptionPane.PLAIN_MESSAGE);
        if (name != null && !name.trim().isEmpty()) {
            leaderboard.add(new ScoreEntry(name.trim(), applesEaten));
            Collections.sort(leaderboard, Comparator.comparingInt((ScoreEntry e) -> e.score).reversed());
            if (leaderboard.size() > 10) {
                leaderboard.subList(10, leaderboard.size()).clear();
            }
        }
        this.requestFocusInWindow();
    }

    private void showLeaderboard() {
        StringBuilder sb = new StringBuilder("Leaderboard:\n\n");
        if (leaderboard.isEmpty()) {
            sb.append("No scores yet!");
        } else {
            for (int i = 0; i < leaderboard.size(); i++) {
                ScoreEntry entry = leaderboard.get(i);
                sb.append(String.format("%d. %s - %d\n", i + 1, entry.name, entry.score));
            }
        }
        JOptionPane.showMessageDialog(this, sb.toString(), "Leaderboard", JOptionPane.INFORMATION_MESSAGE);
        this.requestFocusInWindow();
    }

    private void gameOver(Graphics g) {
        g.setColor(Color.red);
        g.setFont(new Font("Ink Free", Font.BOLD, 40));
        FontMetrics metrics = getFontMetrics(g.getFont());
        String scoreText = "Score: " + applesEaten + "  High Score: " + highScore;
        g.drawString(scoreText, (SCREEN_WIDTH - metrics.stringWidth(scoreText)) / 2, g.getFont().getSize());
        g.setColor(Color.red);
        g.setFont(new Font("Ink Free", Font.BOLD, 75));
        metrics = getFontMetrics(g.getFont());
        g.drawString("Game Over", (SCREEN_WIDTH - metrics.stringWidth("Game Over")) / 2, SCREEN_HEIGHT / 2 - 50);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (running && !paused) {
            move();
            if (animationStep == 0) {
                checkApple();
                checkCollisions();
                int nextLevelThreshold = currentLevel * POINTS_PER_LEVEL;
                if (applesEaten >= nextLevelThreshold) {
                    advanceToNextLevel();
                }
                if (gameMode == GameMode.CHALLENGE && !challengeActive) {
                    startChallenge();
                }
                if (gameMode == GameMode.CHALLENGE) {
                    updateChallenge();
                }
            }
            updateParticles();
        }
        repaint();
    }

    private class MyKeyAdapter extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            switch (e.getKeyCode()) {
                case KeyEvent.VK_LEFT:
                    if (direction != 'R') {
                        direction = 'L';
                    }
                    break;
                case KeyEvent.VK_RIGHT:
                    if (direction != 'L') {
                        direction = 'R';
                    }
                    break;
                case KeyEvent.VK_UP:
                    if (direction != 'D') {
                        direction = 'U';
                    }
                    break;
                case KeyEvent.VK_DOWN:
                    if (direction != 'U') {
                        direction = 'D';
                    }
                    break;
                case KeyEvent.VK_SPACE:
                    togglePause();
                    break;
            }
        }
    }
}