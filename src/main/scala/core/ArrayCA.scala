package dinges.automata
package core

/**
 * A cellular automaton that uses a 2D array and double-buffering for simulation.
 * The core update rule is inspired by the D3 (dihedral) group, a non-commutative group of order 6.
 * The states of the cells, from 0 to 5, correspond to the six elements of the D3 group:
 * States 0, 1, 2 represent rotations (R0, R120, R240).
 * States 3, 4, 5 represent reflections (F1, F2, F3).
 */
final class ArrayCA(val width: Int, val height: Int) {
  private val size = width * height
  private val a = Array.ofDim[Byte](size)
  private val b = Array.ofDim[Byte](size)
  // Pointers to the current read and write buffers
  private var read = a
  private var write = b

  /**
   * Calculates the wrapped index for a given (x, y) coordinate,
   * implementing a toroidal (donut-shaped) grid.
   */
  @inline private def idxWrapped(x: Int, y: Int): Int =
    // Correctly wrap negative and out-of-bounds coordinates
    val xx = if (x < 0) x + width else if (x >= width) x - width else x
    val yy = if (y < 0) y + height else if (y >= height) y - height else y
    // 2D coords -> 1D index
    yy * width + xx

  /** Fills the entire grid with the state 0 (the identity element of the D3 group). */
  def clear(): Unit = java.util.Arrays.fill(read, 0.toByte)

  /** Set a cell to a value in 0..5 (will be normalized mod 6) */
  def set(x: Int, y: Int, v: Int): Unit = read(idxWrapped(x, y)) = ((v % 6 + 6) % 6).toByte

  /** Get the state of a cell from the current read buffer. */
  def get(x: Int, y: Int): Int = read(idxWrapped(x, y)).toInt

  /**
   * * Randomly initializes the grid. Each cell has a probability `p` of
   * being set to a random state from 1 to 5, otherwise it's set to 0.
   */
  def randomize(p: Double = 0.2, seed: Long = System.nanoTime()): Unit =
    val rnd = new scala.util.Random(seed)
    var i = 0
    while (i < size) {
      read(i) = (if (rnd.nextDouble() < p) rnd.nextInt(6) else 0).toByte
      i += 1
    }

  /** Defines the 8 neighbors in a Moore neighborhood (excluding the center). */
  private val neigh = Array(
    (-1, -1), (0, -1), (1, -1),
    (-1,  0),          (1,  0),
    (-1,  1), (0,  1), (1,  1)
  )

  /**
   * Performs the D3 group operation on two states. This is the core
   * of the group-theoretic update rule.
   * The D3 group has 6 elements: 3 rotations (R0, R120, R240) and 3 reflections (F1, F2, F3).
   * Here, we map them to integers:
   * Rotations: R0=0, R120=1, R240=2
   * Reflections: F1=3, F2=4, F3=5
   * The operation is a combination of rotation and reflection rules:
   * (R_a) * (R_b) = R_(a+b)
   * (R_a) * (F_b) = F_(a-b)
   * (F_a) * (R_b) = F_(a+b)
   * (F_a) * (F_b) = R_(a-b)
   * The `d3Op` function implements this logic:
   * - `aK` and `bK` are the rotation parts (0, 1, or 2).
   * - `aFlip` and `bFlip` are booleans indicating if the state is a reflection.
   * - `signedB` handles the rotation part, applying `(-1)^aFlip * bK`.
   * - `newFlip` handles the reflection part, using `aFlip XOR bFlip`.
   */
  @inline private def d3Op(aVal: Int, bVal: Int): Int =
    // Extract rotation and flip components from aVal
    val aK = aVal % 3
    val aFlip = aVal >= 3
    // Extract rotation and flip components from bVal
    val bK = bVal % 3
    val bFlip = bVal >= 3
    // Apply the rotation rule: aK + bK if aVal is a rotation, aK - bK if aVal is a reflection
    val signedB = if (aFlip) -bK else bK
    val newK = ((aK + signedB) % 3 + 3) % 3 // Normalizes result to 0, 1, or 2
    // Apply the reflection rule: new state is a reflection if one, but not both, of the
    // original states was a reflection (e.g., a flip XOR b flip).
    val newFlip = aFlip ^ bFlip
    // Combine the new rotation and flip to get the new state value
    if (newFlip) newK + 3 else newK

  /**
   * The main simulation step.
   * It applies the cellular automata rules to update the entire grid based on its neighbors.
   * This method uses double-buffering to update cells in the 'write' buffer while reading
   * from the 'read' buffer, then swaps them.
   */
  def step(): Unit = {
    var y = 0
    while (y < height) {

      val yRow = y * width

      var x = 0
      while (x < width) {

        val i = yRow + x
        // Use the D3 group operation to fold the neighbor states.
        // `acc` will hold the cumulative D3 operation result.
        var acc = 0
        var aliveCount = 0

        var k = 0
        while (k < neigh.length) {
          val dx = neigh(k)._1
          val dy = neigh(k)._2
          val j = idxWrapped(x + dx, y + dy)
          val nv = read(j).toInt
          if (nv != 0) aliveCount += 1
          acc = d3Op(acc, nv)
          k += 1
        }

        val cur = read(i).toInt
        val isAlive = cur != 0

        // Apply Conway's GoL-like rules
        val next =
          if (isAlive && aliveCount >= 2 && aliveCount <= 4) {
            // Survival rule: If a cell is alive and has 2-4 alive neighbors, it survives.
            // Its new state is determined by its current state combined with the folded neighbor states.
            d3Op(cur, acc)
          } else if (!isAlive && aliveCount == 3) {
            // Birth rule: If a cell is dead and has exactly 3 alive neighbors, it becomes alive.
            // The new cell is born with state 1 (R120).
            1
          } else 0

        write(i) = next.toByte

        x += 1
      }

      y += 1
    }

    // Swap buffers
    val tmp = read
    read = write
    write = tmp
  }
}