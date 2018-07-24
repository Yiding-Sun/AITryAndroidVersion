import com.jme3.math.Vector2f

operator fun Vector2f.plus(v: Vector2f): Vector2f = add(v)
operator fun Vector2f.plusAssign(v: Vector2f) {
	addLocal(v)
}

operator fun Vector2f.minus(v: Vector2f): Vector2f = add(v.negate())
operator fun Vector2f.minusAssign(v: Vector2f) {
	addLocal(v.negate())
}

operator fun Vector2f.unaryMinus(): Vector2f = negate()
operator fun Vector2f.times(f: Float): Vector2f = mult(f)
operator fun Vector2f.timesAssign(f: Float) {
	multLocal(f)
}

operator fun Vector2f.times(v: Vector2f): Float = dot(v)
operator fun Vector2f.compareTo(v: Vector2f) = length().compareTo(v.length())
fun Vector2f.vertical(): Vector2f = Vector2f(y, -x)
fun Vector2f.toMax(max: Float): Vector2f {
	val vector2f = Vector2f(this)
	return if (vector2f.length() > max) {
		vector2f.normalizeLocal().multLocal(max)
	} else {
		vector2f
	}
}

fun Vector2f.toMaxLocal(max: Float): Vector2f {
	return if (length() > max) {
		normalizeLocal().multLocal(max)
	} else this
}
