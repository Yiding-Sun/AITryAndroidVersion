import com.jme3.math.Vector2f

class WanderState(transport: Transport,var radius:Float=50f,var distance:Float=150f,var jitter:Float=8f) : State(transport) {
	var wanderTarget=Vector2f(0f,0f)
	var targetWorld=Vector2f(0f,0f)
	override fun update(): Vector2f {
		wanderTarget.addLocal(Vector2f((Math.random().toFloat()*2-1),(Math.random().toFloat()*2-1)).mult(jitter))
		wanderTarget.normalizeLocal()
		wanderTarget.multLocal(radius)
		targetWorld = wanderTarget.add(transport.velocity.normalize().mult(distance)).add(transport.location)
		return SeekState(transport,targetWorld).update()
	}
}