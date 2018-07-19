import com.jme3.math.Vector2f

class FleeState(transport: Transport, var target: Vector2f,panicDistant:Float=200f) : State(transport) {
	val panicDistantSq=panicDistant*panicDistant
	override fun update(): Vector2f {
		if(transport.location.distanceSquared(target)>panicDistantSq) return Vector2f(0f, 0f)
		val targetSpeed = target.add(transport.location.negate()).normalize().mult(transport.maxVelocity)
		return targetSpeed.add(transport.velocity.negate()).negate().normalize().mult(transport.maxAcceleration)
	}
}