import com.jme3.math.Vector2f

class PursuitState(transport: Transport, var target: Transport) : State(transport) {
	var seekTarget=Vector2f(0f,0f)
	override fun update(): Vector2f {
		val lookAheadTime = target.location.distance(transport.location) / (transport.maxVelocity + target.maxVelocity)
		seekTarget=target.velocity.mult(lookAheadTime).add(target.location)
		return SeekState(transport,seekTarget).update()
	}
}