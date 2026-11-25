# JavaFX 2D Apple Dash Platformer

A 2D arcade platformer game developed in Java using JavaFX. The player navigates a multi-tiered level to collect animated apples, evade dynamically spawning spike hazards, and use a score-powered ability to clear obstacles.

## Features

- **Custom Game Loop & Physics:** Runs on JavaFX `AnimationTimer` with gravity acceleration, grounded jump detection, and bounding-box collision checks.
- **Sprite-Sheet Animation Engine:** Abstract `GameObject` base class handles frame-by-frame viewport slicing (`Rectangle2D`) for multi-frame character (`11` frames) and collectible (`17` frames) animations.
- **Dynamic Spawning & Risk-Reward Mechanics:** Spawns new apples in mid-air and spikes on platforms every 3 seconds; collecting every two apples increases the score and removes a random spike.
- **Special Ability (Spike Destroyer):** Allows spending `5` score points to destroy the nearest spike obstacle on the map.
- **Scrolling Camera & Level Loader:** Tracks player movement smoothly across a `960x480` world through a `480x480` viewport and parses entity layouts from `level.txt`.

## Controls

| Key | Action |
| :--- | :--- |
| `A` / `Left Arrow` | Move Left |
| `D` / `Right Arrow` | Move Right |
| `W` / `Up Arrow` | Jump |
| `Space` | Destroy nearest spike (costs `5` score points) |

## Project Structure

```text
apple-dash-platformer/
├── assets/
│   ├── Apple(16x18x17).png
│   ├── Background(960x480x1).png
│   ├── Block(20x20x1).png
│   ├── Player(22x26x11).png
│   ├── Spike(14x7x1).png
│   └── Terrain(48x48x1).png
├── src/
│   ├── GameEngine.java
│   ├── GameObject.java
│   ├── Player.java
│   ├── Apple.java
│   ├── Spike.java
│   ├── Terrain.java
│   └── Block.java
├── level.txt
├── .gitignore
└── README.md
```

## Prerequisites

- Java Development Kit (JDK) 8 or higher
- JavaFX SDK (configured via `--module-path` if using JDK 11+)

## Compilation & Running

1. Compile all source files (update the JavaFX SDK path for JDK 11+):
   ```bash
   javac --module-path /path/to/javafx-sdk/lib --add-modules javafx.controls -d out src/*.java
   ```

2. Run the game from the project root directory (so `assets/` and `level.txt` resolve properly):
   ```bash
   java --module-path /path/to/javafx-sdk/lib --add-modules javafx.controls -cp out GameEngine
   ```