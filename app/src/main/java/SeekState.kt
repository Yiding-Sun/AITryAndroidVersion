import com.jme3.math.Vector2f

class SeekState(transport: Transport,var target:Vector2f):State(transport){
	override fun update(): Vector2f {
		val targetSpeed = target.add(transport.location.negate()).normalize().mult(transport.maxVelocity)
		return targetSpeed.add(transport.velocity.negate()).normalize()
	}
}