import com.jme3.math.Vector2f

class EvadeState(transport: Transport, var target: Transport) : State(transport) {
	var evadeTarget = Vector2f(0f, 0f)
	override fun update(): Vector2f {
		if(target.location.distanceSquared(transport.location)>90000f)
			return Vector2f(0f,0f)
		val state = PursuitState(transport, target)
		val answer = state.update().negate()
		evadeTarget = state.seekTarget
		return answer
	}
}