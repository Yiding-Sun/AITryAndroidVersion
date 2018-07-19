import com.jme3.math.Vector2f

enum class SpeedLevel(val value: Float){FAST(0.3f),MIDDLE(0.6f),SLOW(0.7f)}
class ArriveState(transport: Transport,var target: Vector2f,val speedLevel: SpeedLevel) : State(transport) {
	override fun update(): Vector2f {
		val toTarget=target.add(transport.location.negate())
		val distant=toTarget.length()
		return if (distant > 2) {
			var speed=distant/speedLevel.value
			speed = Math.min(transport.maxVelocity, speed)
			val desiredVelocity = toTarget.normalize().mult(speed)
			desiredVelocity.add(transport.velocity.negate()).normalize()
		}else{
			transport.velocity.negate().mult(0.0005f)
		}
	}
}