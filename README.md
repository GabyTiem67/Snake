# Snake Game

A desktop Snake game built with Java Swing.

This version adds more than the classic arcade loop: multiple game modes, level progression, obstacles, a temporary big-apple bonus, customizable colors, and a local in-memory leaderboard.

## Features

- Classic Snake gameplay with keyboard controls
- `Classic`, `Endless`, and `Challenge` game modes
- Level system with increasing obstacles
- Speed increases as your score grows
- Big apples that appear after collecting enough small apples
- Pause, restart, settings, and exit buttons
- Customizable snake head, snake body, and background colors
- Local leaderboard for top scores during the session
- Particle effects and smoother-looking snake rendering

## Controls

- `Arrow Keys`: move the snake
- `Space`: pause or resume

## Game Modes

- `Classic`: you start with 3 lives; collisions cost a life until game over
- `Endless`: collisions reset the snake, but the game keeps going
- `Challenge`: complete timed objectives to advance through challenge stages

## Current Gameplay Notes

- Window size: `1300 x 750`
- Starting lives in Classic mode: `3`
- Initial level: configurable from `1` to `5`
- Speed increases every `15` points
- Level-ups happen every `20` points, up to level `5`
- A big apple appears after `5` small apples
- Big apples are worth `3` points

## Project Structure

- `SnakeGame.java`: simple entry point
- `GameFrame.java`: main application window
- `GamePanel.java`: game logic, rendering, controls, UI buttons, and settings

## How to Run

Make sure Java is installed, then from this folder run:

```bash
javac SnakeGame.java GameFrame.java GamePanel.java
java SnakeGame
```

You can also run `GameFrame` directly:

```bash
java GameFrame
```

## Tech Stack

- Java
- Swing / AWT

## Notes

- The leaderboard is stored only while the app is running.
- The game opens with an instructions dialog before play starts.
