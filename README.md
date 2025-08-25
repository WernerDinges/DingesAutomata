# Dinges' Automata
An attempt to create a cellular automata inspired by group theory. It turned out very similar to Conwey's GoL but is pretty fun.

It's highly dynamic and is grobally chaotic yet locally interestingly structured.
<img width="738" height="434" alt="Screenshot 2025-08-25 013753" src="https://github.com/user-attachments/assets/fbc6d3b4-ae42-4997-bb6f-43e90de23893" />
<img width="1272" height="634" alt="Screenshot 2025-08-25 012318" src="https://github.com/user-attachments/assets/ae4e827c-2447-4700-97e5-3802f06bb02e" />
<img width="1270" height="633" alt="Screenshot 2025-08-25 012338" src="https://github.com/user-attachments/assets/677dddd2-bf52-4571-8939-b47774fb6927" />
*Red stands for rotations while blue indicated flipping*

## Installation
Download the source code and build the app via `sbt run`.

## Explaination
The evolution rules are based on the $D_{3}$ (dihedral) group. Instead of simple on/off states, this CA's cells have states from 0 to 5.
These states correspond to the six elements of the $D_{3}$ group, which describes the symmetries of an equilateral triangle:
- States 0, 1, 2: Rotations of 0°, 120°, and 240°;
- States 3, 4, 5: Reflections about the triangle's axes.

The core of the simulation is the `d3Op` function, which performs a group operation on two cell states.
This operation is non-commutative, meaning the order matters.

1. **Grid & Neighbors:** The simulation runs on a toroidal grid (a seamless donut shape) where the edges wrap around. Each cell considers its 8 neighbors (a Moore neighborhood);
2. **Folding Neighbors:** For each cell, we "fold" the states of its neighbors together using the `d3Op` function. The result is a single group element that represents the collective influence of the neighborhood;
3. **The Rules:** The simulation combines a Game of Life-like logic with the group operation
   * *Survival:* An "alive" cell (state != 0) with 2-4 alive neighbors survives. Its new state is its current state combined with the folded neighbor state
   * *Birth:* A "dead" cell (state 0) with exactly 3 alive neighbors comes to life, taking on the state 1 (a 120° rotation)
   * *Death:* All other cells either die or stay dead
4. **Double-Buffering:** The simulation uses two arrays to prevent race conditions. It reads from one array to calculate the next state, and writes the results to the second array. After all calculations are done, the two arrays are swapped. This ensures that every cell in a step is updated based on the previous state of the entire grid.

## Controls
* **Play/Pause:** Starts and stops the simulation
* **Step:** Advances the simulation by one frame
* **Clear:** Kill all the cells mercilessly
* **Random:** Initializes the grid with a new random pattern
* **Speed:** Adjusts the frames per second
* **Click:** Clicking on a cell will cycle its state, allowing you to create your own patterns
