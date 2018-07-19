import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.view.View
import com.jme3.math.Vector2f
/*
fun main(args: Array<String>) {
	val frame = JFrame("TRY")
	
	panel.addMouseListener(object : MouseAdapter() {
		override fun mouseClicked(e: MouseEvent?) {
		}
		
	})
	panel.addMouseMotionListener(object : MouseMotionAdapter() {
		override fun mouseMoved(e: MouseEvent?) {
			mouseLocation = Vector2f(e!!.x.toFloat(), e.y.toFloat())
		}
	})
	frame.addKeyListener(object : KeyAdapter() {
		var cd = false
		override fun keyPressed(e: KeyEvent?) {
			when (e!!.keyCode) {
				KeyEvent.VK_A -> panel.a = true
				KeyEvent.VK_D -> panel.d = true
				KeyEvent.VK_W -> panel.w = true
				KeyEvent.VK_S -> panel.s = true
				KeyEvent.VK_SPACE -> {
					if (!cd) {
						val bullet = Transport(1f, Vector2f(transport.location), maxVelocity = 200f, maxAcceleration = 300f, color = Color.BLUE, size = 3f, explode = false)
						bullet.touchable = false
						bullet.velocity = Vector2f(transport.velocity)
						bullet.states.add(ObstacleAvoidState(bullet, list))
						bullet.states.add(PursuitState(bullet, selected))
						bullet.states.add(object : State(bullet) {
							override fun update(): Vector2f {
								bullet.maxAcceleration = max(bullet.maxAcceleration - 2f, 20f)
								return Vector2f(0f, 0f)
							}
						})
						selected.states.add(EvadeState(selected, bullet))
						panel.transports.add(bullet)
						val thread = object : Thread() {
							override fun run() {
								cd = true
								Thread.sleep(500)
								bullet.touchable = true
								Thread.sleep(500)
								cd = false
								Thread.sleep(9000)
								bullet.dead = true
							}
						}
						thread.start()
					}
				}
				KeyEvent.VK_Z -> {
					if (!cd) {
						val bullet = object : Transport(1f, Vector2f(transport.location), maxVelocity = 10000f, maxAcceleration = 0f, color = Color.ORANGE, size = 2f, explode = false) {
							override fun draw(g: Graphics, axis: Vector2f) {
								val p1 = location.add(velocity.normalize().mult(size)).add(axis)
								val p2 = location.add(velocity.normalize().mult(size).negate()).add(axis)
								drawLine(p1, p2, g)
							}
						}
						bullet.velocity = transport.velocity.add(transport.velocity.normalize().mult(400f))
						panel.transports.add(bullet)
						val thread = object : Thread() {
							override fun run() {
								cd = true
								Thread.sleep(250)
								bullet.touchable = true
								cd = false
								Thread.sleep(4750)
								bullet.dead = true
							}
						}
						thread.start()
					}
				}
			}
		}
		
		override fun keyReleased(e: KeyEvent?) {
			when (e!!.keyCode) {
				KeyEvent.VK_A -> panel.a = false
				KeyEvent.VK_D -> panel.d = false
				KeyEvent.VK_W -> panel.w = false
				KeyEvent.VK_S -> panel.s = false
			}
		}
	})
	frame.contentPane.add(panel, BorderLayout.CENTER)
	frame.isVisible = true
	frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
	frame.setSize(1200, 800)
}
*/

