import com.jme3.math.Vector2f

class CohesionState(transport: Transport, val list: ArrayList<Transport>) : State(transport) {
	override fun update(): Vector2f {
		val neighbours = list.filter { it.location.distanceSquared(transport.location) < 40000f && it != transport }
		val center=Vector2f(0f,0f)
		neighbours.forEach {
			center.addLocal(it.location)
		}
		return if(neighbours.isNotEmpty())
			SeekState(transport,center.mult(1f/neighbours.size)).update()
		else
			Vector2f(0f,0f)
	}
}