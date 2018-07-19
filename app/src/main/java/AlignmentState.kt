import com.jme3.math.Vector2f

class AlignmentState(transport: Transport, val list: ArrayList<Transport>) : State(transport) {
	override fun update(): Vector2f {
		val averageHeading=Vector2f(0f,0f)
		val neighbours=list.filter { it.location.distanceSquared(transport.location)<40000f && it !=transport }
		neighbours.forEach {
			averageHeading.addLocal(it.velocity.normalize())
		}
		return if(neighbours.isNotEmpty())
			averageHeading.mult(1f/neighbours.size).add(transport.velocity.normalize().negate())
		else
			Vector2f(0f,0f)
	}
}
