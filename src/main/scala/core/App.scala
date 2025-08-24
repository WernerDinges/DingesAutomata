package dinges.automata
package core

import scalafx.animation.AnimationTimer
import scalafx.application.JFXApp3
import scalafx.event.EventHandler
import scalafx.geometry.Insets
import scalafx.scene.Scene
import scalafx.scene.canvas.Canvas
import scalafx.scene.control.{Button, Label, Slider, ToggleButton}
import scalafx.scene.input.{MouseButton, MouseEvent}
import scalafx.scene.layout.{BorderPane, HBox}
import scalafx.scene.paint.Color

object App extends JFXApp3 {
  override def start(): Unit = {
    val (w, h) = (256, 128)
    val cellSize = 4
    val ca = ArrayCA(w, h)
    ca.randomize(0.12)

    val canvas = Canvas(w * cellSize, h * cellSize)
    canvas.pickOnBounds = true
    canvas.focusTraversable = true
    val ctx = canvas.graphicsContext2D

    /** Renders the current state of the cellular automaton to the canvas. */
    def render(): Unit = {
      // Clear the canvas
      ctx.fill = Color.Black
      ctx.fillRect(0, 0, canvas.width(), canvas.height())

      var y = 0
      while(y < h) {

        var x = 0
        while(x < w) {

          val d3 = ca.get(x, y)
          if(d3 != 0) {
            // Map the D3 state (1-5) to a color for visualization
            ctx.fill = d3 match {
              case 1 => Color(.25, 0.0, 0.0, 1.0)
              case 2 => Color(0.5, 0.0, 0.0, 1.0)
              case 3 => Color(0.0, 0.0, 0.5, 1.0)
              case 4 => Color(.25, 0.0, 0.5, 1.0)
              case 5 => Color(0.5, 0.0, 0.5, 1.0)
            }
            ctx.fillRect(x * cellSize, y * cellSize, cellSize, cellSize)
          }

          x += 1
        }

        y += 1
      }
    }

    // Place a cell or evolve it by click
    canvas.onMouseClicked = me => {
      val x = (me.getX / cellSize).toInt
      val y = (me.getY / cellSize).toInt
      ca.set(x, y, ca.get(x, y) + 1)
      render()
    }

    // UI Controls
    val stepBtn = Button("Step")
    val clearBtn = Button("Clear")
    val randomBtn = Button("Random")
    val playPause = ToggleButton("Play")
    val speed = Slider(min = 1, max = 60, value = 10)
    val speedLabel = Label("10")

    // The animation loop that drives the simulation when "Play" is toggled on.
    // It uses AnimationTimer to synchronize with the JavaFX rendering thread.
    var last = System.nanoTime()
    val timer = AnimationTimer(t => {
      val now = System.nanoTime()
      // Run updates at `speed.value` fps
      val fps = speed.value.value
      val nanosPerFrame = (1e9 / math.max(1.0, fps)).toLong
      if (now - last >= nanosPerFrame) {
        ca.step()
        render()
        last = now
      }
    })
    render()

    // Event handlers for the control buttons.
    stepBtn.onAction = _ => {
      if (!playPause.selected.value) {
        ca.step(); render()
      }
    }
    playPause.selected.onChange { (_, _, playing) =>
      if (playing) {
        playPause.text = "Pause"
        last = System.nanoTime()
        timer.start()
      } else {
        playPause.text = "Play"
        timer.stop()
      }
    }
    clearBtn.onAction = _ => { ca.clear(); render() }
    randomBtn.onAction = _ => { ca.randomize(0.12); render() }
    speed.value.onChange { (_, _, nv) => speedLabel.text = nv.toString }

    val controls = new HBox(8, playPause, stepBtn, clearBtn, randomBtn, Label("Speed:"), speed, speedLabel)
    controls.padding = Insets(8)

    val root = new BorderPane {
      center = canvas
      bottom = controls
    }

    // --- Stage ---
    stage = new JFXApp3.PrimaryStage {
      title = "Dinges' Automata"
      scene = Scene(parent = root, width = canvas.width(), height = canvas.height() + 60)
    }
  }
}