import com.jme3.math.Vector2f

open class State (val transport: Transport){
	open fun update(): Vector2f = Vector2f(0f, 0f)
	
}