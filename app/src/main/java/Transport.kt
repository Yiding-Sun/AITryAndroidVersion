import android.graphics.Canvas
import com.jme3.math.Vector2f
import android.graphics.Color
import android.graphics.Paint
import java.lang.Math.cos
import java.lang.Math.sqrt

open class Transport(
		private val mass: Float,
		var location: Vector2f,
		var maxVelocity: Float = 50f,
		var maxAcceleration: Float = 20f,
		var color: Int = Color.BLACK,
		var size: Float = 5f,
		var explode: Boolean = true
) {
	var velocity = Vector2f(0.00001f, 0f)
	private var acceleration = Vector2f(0f, 0f)
	var heading = Vector2f(1f, 0f)
	val states = ArrayList<State>()
	var dead = false
	var touchable = false
	fun update(tpf: Long) {
		val firstLevelForce = Vector2f(0f, 0f)
		val secondLevelForce = Vector2f(0f, 0f)
		for (state in states) {
			val simpleForce = state.update()
			when (state) {
				is AlignmentState -> secondLevelForce.addLocal(simpleForce)
				is ArriveState -> secondLevelForce.addLocal(simpleForce.mult(150f))
				is CohesionState -> secondLevelForce.addLocal(simpleForce.mult(100f))
				is EvadeState -> secondLevelForce.addLocal(simpleForce.mult(250f))
				is FleeState -> secondLevelForce.addLocal(simpleForce.mult(150f))
				is ObstacleAvoidState -> firstLevelForce.addLocal(simpleForce.mult(100f))
				is PursuitState -> secondLevelForce.addLocal(simpleForce.mult(150f))
				is SeekState -> secondLevelForce.addLocal(simpleForce.mult(150f))
				is SeparationState -> firstLevelForce.addLocal(simpleForce.mult(250f))
				is WanderState -> secondLevelForce.addLocal(simpleForce.mult(50f))
			}
		}
		val force= if (firstLevelForce.length() > maxAcceleration) {
			firstLevelForce.normalize().mult(maxAcceleration)
		} else {
			val l = firstLevelForce.length()
			val a = maxAcceleration
			val cos = cos(firstLevelForce.angleBetween(secondLevelForce).toDouble())
			val x = (2 * l * cos + sqrt(4 * l * l * cos * cos - 4 * l * l + 4 * a * a)) / 2
			firstLevelForce.add(secondLevelForce.normalize().mult(x.toFloat()))
		}
		acceleration.addLocal(force.mult(1 / mass))
		acceleration.toMaxLocal(maxAcceleration)
		acceleration.multLocal(tpf / 1000f)
		velocity.addLocal(acceleration.multLocal(1 / mass))
		velocity.toMaxLocal(maxVelocity)
		if (velocity.lengthSquared() > 5f) {
			heading = velocity.normalize()
		}
		location.addLocal(velocity.mult((tpf / 1000f)))
	}
	
	open fun draw(canvas: Canvas, axis: Vector2f) {
		val paint = Paint()
		paint.color = color
		val x = heading.normalize()
		val y = Vector2f(-x.y, x.x)
		val p1 = x.mult(size * 1.5f).add(location).add(axis)
		val p2 = x.negate().add(y).mult(size).add(location).add(axis)
		val p3 = x.negate().add(y.negate()).mult(size).add(location).add(axis)
		drawLine(p1, p2, canvas, paint)
		drawLine(p1, p3, canvas, paint)
		drawLine(p2, p3, canvas, paint)
	}
	
	fun drawLine(p1: Vector2f, p2: Vector2f, canvas: Canvas, paint: Paint) {
		canvas.drawLine(p1.x, p1.y, p2.x, p2.y, paint)
	}
}

