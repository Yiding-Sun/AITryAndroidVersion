import com.jme3.math.Vector2f

class SeparationState(transport: Transport, val list: ArrayList<Transport>) : State(transport) {
	override fun update(): Vector2f {
		val neighbours=list.filter { it.location.distanceSquared(transport.location)<40000f && it !=transport }
		val force = Vector2f(0f, 0f)
		neighbours.forEach {
			val toTarget=transport.location.add(it.location.negate())
			force.addLocal(toTarget.normalize().mult(1/toTarget.length()))
		}
		return force.mult(14f)
	}
}