import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import com.example.sunyiding.aitryandroidversion.MainActivity
import com.jme3.math.Vector2f


fun makeObstacles(): ArrayList<Obstacle> {
	val list = ArrayList<Obstacle>()
	fun makeObstacle(): Obstacle = Obstacle(Vector2f((Math.random() * 3600).toFloat() - 1200f, (Math.random() * 2400).toFloat() - 800f), Math.random().toFloat() * 100)
	repeat(30) { list.add(makeObstacle()) }
	return list
}

fun explosion(panel: MyPanel) {
	val explosionThread = object : Thread() {
		override fun run() {
			repeat(3) {
				panel.axis.x += 4
				panel.axis.y += 4
				Thread.sleep(20)
			}
			repeat(3) {
				panel.axis.x -= 4
				panel.axis.y -= 4
				Thread.sleep(20)
			}
			repeat(3) {
				panel.axis.x -= 4
				panel.axis.y -= 4
				Thread.sleep(20)
			}
			repeat(3) {
				panel.axis.x += 4
				panel.axis.y += 4
				Thread.sleep(20)
			}
		}
	}
	explosionThread.start()
	
}

class MyPanel(val activity: MainActivity, val list: ArrayList<Obstacle>) : View(activity) {
	var touchWall = 0
	var touchEach = 0
	var bulletHit = 0
	var touchObstacle = 0
	val transports = ArrayList<Transport>()
	var lstUpdate = System.currentTimeMillis()
	val axis = Vector2f(0f, 0f)
	var a = false
	var d = false
	var w = false
	var s = false
	var start = true
	
	override fun onDraw(canvas: Canvas?) {
		if (canvas != null) {
			val time = System.currentTimeMillis()
			val tpf = time - lstUpdate
			
			if (a) axis.addLocal(Vector2f(tpf.toFloat(), 0f))
			if (d) axis.addLocal(Vector2f(-tpf.toFloat(), 0f))
			if (w) axis.addLocal(Vector2f(0f, tpf.toFloat()))
			if (s) axis.addLocal(Vector2f(0f, -tpf.toFloat()))
			
			
			val paint = Paint()
			paint.color = Color.WHITE
			canvas.drawRect(0f, 0f, canvas.width.toFloat(), canvas.height.toFloat(), paint)
			for (i in list) {
				val location = i.location.add(axis)
				paint.color = i.color
				canvas.drawOval((location.x - i.radius), (location.y - i.radius), location.x + i.radius, location.y + i.radius, paint)
			}
			paint.color = Color.GRAY
			canvas.drawRect(-1200 + axis.x - 50, -800 + axis.y - 50, axis.x + 3700 - 1200 - 50, axis.y - 800, paint)
			canvas.drawRect(-1200 + axis.x - 50, 1600 + axis.y, axis.x - 1200 - 50 + 3700, axis.y + 1650, paint)
			canvas.drawRect(-1200 + axis.x - 50, -800 + axis.y - 50, axis.x - 1200, axis.y - 850 + 2500, paint)
			canvas.drawRect(2400 + axis.x, -800 + axis.y - 50, axis.x + 2450, axis.y - 800 - 50 + 2500, paint)
			paint.color = Color.BLACK
			lstUpdate = time
			synchronized(transports) {
				for (i in 0 until transports.size) {
					val transport=transports[i]
					if (start)
						transport.update(tpf)
					transport.draw(canvas, axis)
					if (transport.location.x < -1200 || transport.location.x > 2400 || transport.location.y < -800 || transport.location.y > 1600) {
						transport.dead = true
						if (transport.explode) {
							explosion(this)
							touchWall++
						}
					}
					for (obstacle in list) {
						if (transport.location.distance(obstacle.location) < 2f + obstacle.radius) {
							transport.dead = true
							if (transport.explode) {
								explosion(this)
								touchObstacle++
							}
						}
					}
				}
			}
			synchronized(transports) {
				for (i in 0 until transports.size) {
					if (transports[i].touchable)
						for (j in i + 1 until transports.size) {
							if (transports[j].touchable)
								if (transports[i].location.distanceSquared(transports[j].location) < 50f) {
									transports[i].dead = true
									transports[j].dead = true
									if (transports[i].explode || transports[j].explode) {
										explosion(this)
									}
									if (transports[i].explode && transports[j].explode) {
										touchEach++
									} else if (transports[i].explode || transports[j].explode) {
										bulletHit++
									}
								}
						}
				}
			}
			var i = 0
			while (i < transports.size) {
				if (transports[i].dead) {
					transports.removeAt(i)
				}
				i++
			}
			if (transports[0].states[0] is ArriveState) {
				paint.color = Color.RED
				val v = (transports[0].states[0] as ArriveState).target.add(axis)
				canvas.drawOval(v.x - 2, v.y - 2, v.x + 2, v.y + 2, paint)
			} else {
				if (start) {
					start = false
					repeat(5) { explosion(this) }
				}
			}
			/*g.color= Color.GREEN
			val v2=(transports[1].states[0] as PursuitState).seekTarget.add(axis)
			g.drawOval(v2.x.toInt() - 2, v2.y.toInt() - 2, 4, 4)*/
		}
	}
	
	var lstL = Vector2f(0f, 0f)
	var lstR = Vector2f(0f, 0f)
	var cd = false
	
	enum class Weapon(val value: Int) {
		GUN(0), MISSILE(1), RADIUS(2), SUPER_GUN(3);
		
		fun next(): Weapon = values()[if (ordinal + 1 <= values().size) ordinal + 1 else 0]
		
	}
	
	var weapon = Weapon.GUN
	override fun onTouchEvent(event: MotionEvent?): Boolean {
		when (event!!.actionMasked) {
			MotionEvent.ACTION_DOWN -> {
				for (j in 0 until event.pointerCount) {
					when {
						event.getX(j) > width / 2 -> lstR = Vector2f(event.getX(j), event.getY(j)).add(axis.negate())
						event.getX(j) <= width / 2 -> lstL = Vector2f(event.getX(j), event.getY(j)).add(axis.negate())
					}
				}
			}
			MotionEvent.ACTION_POINTER_DOWN -> {
				for (j in 0 until event.pointerCount) {
					when {
						event.getX(j) > width / 2 -> lstR = Vector2f(event.getX(j), event.getY(j)).add(axis.negate())
						event.getX(j) <= width / 2 -> lstL = Vector2f(event.getX(j), event.getY(j)).add(axis.negate())
					}
				}
				if (event.pointerCount == 3) {
					weapon = weapon.next()
					Toast.makeText(activity, "Weapon changed, now:$weapon", Toast.LENGTH_SHORT).show()
				}
			}
			MotionEvent.ACTION_MOVE -> {
				if (event.pointerCount <= 2)
					for (j in 0 until event.pointerCount) {
						if (event.getX(j) > width / 2) {
							for (i in 0 until event.historySize) {
								val new = Vector2f(event.getHistoricalX(j, i), event.getHistoricalY(j, i)).add(axis.negate())
								val move = lstR.add(new.negate())
								axis.addLocal(move.negate())
								lstR = new
							}
						} else {
							for (i in 0 until event.historySize) {
								val new = Vector2f(event.getHistoricalX(j, i), event.getHistoricalY(j, i)).add(axis.negate())
								val move = lstL.add(new.negate())
								activity.state.target.addLocal(move.negate())
								lstL = new
							}
						}
					}
			}
		}
		if (event.pointerCount == 2) {
			if (!cd) {
				when (weapon) {
					Weapon.GUN -> {
						val bullet = GunBullet(activity)
						bullet.velocity = activity.transport.velocity.add(activity.transport.velocity.normalize().mult(400f))
						transports.add(bullet)
						val thread = object : Thread() {
							override fun run() {
								cd = true
								Thread.sleep(250)
								bullet.touchable = true
								cd = false
								Thread.sleep(4750)
								bullet.dead = true
							}
						}
						thread.start()
					}
					
					Weapon.MISSILE -> {
						val bullet = Transport(1f, Vector2f(activity.transport.location), maxVelocity = 200f, maxAcceleration = 300f, color = Color.BLUE, size = 3f, explode = false)
						bullet.touchable = false
						bullet.velocity = Vector2f(activity.transport.velocity)
						bullet.states.add(ObstacleAvoidState(bullet, list))
						bullet.states.add(PursuitState(bullet, activity.selected))
						bullet.states.add(object : State(bullet) {
							override fun update(): Vector2f {
								bullet.maxAcceleration = Math.max(bullet.maxAcceleration - 2f, 20f)
								return Vector2f(0f, 0f)
							}
						})
						activity.selected.states.add(EvadeState(activity.selected, bullet))
						transports.add(bullet)
						val thread = object : Thread() {
							override fun run() {
								cd = true
								Thread.sleep(500)
								bullet.touchable = true
								Thread.sleep(500)
								cd = false
								Thread.sleep(9000)
								bullet.dead = true
							}
						}
						thread.start()
					}
					Weapon.RADIUS -> {
						val bullet = GunBullet(activity)
						bullet.velocity = activity.transport.velocity.add(activity.transport.velocity.normalize().mult(1000f))
						transports.add(bullet)
						val thread = object : Thread() {
							override fun run() {
								cd = true
								Thread.sleep(10)
								bullet.touchable = true
								Thread.sleep(30)
								cd = false
								Thread.sleep(210)
								bullet.dead = true
							}
						}
						thread.start()
					}
					Weapon.SUPER_GUN -> {
						val enemyLocation = activity.selected.location
						val bulletVelocity = 500f
						val toEnemy=enemyLocation.add(activity.transport.location.negate())
						val angle=toEnemy.angleBetween(activity.selectedAverageVelocity)
						val shootAngle=Math.asin(Math.sin(angle.toDouble())*activity.selectedAverageVelocity.length()/bulletVelocity)
						val target=toEnemy.normalize().mult(bulletVelocity)
						target.rotateAroundOrigin(shootAngle.toFloat(),false)
						val bullet=GunBullet(activity)
						bullet.velocity=target
						bullet.color=Color.BLUE
						bullet.size=4f
						transports.add(bullet)
						val thread = object : Thread() {
							override fun run() {
								cd = true
								Thread.sleep(250)
								bullet.touchable = true
								Thread.sleep(113)
								cd = false
								Thread.sleep(4750)
								bullet.dead = true
							}
						}
						thread.start()
					}
				}
				
			}
		}
		return true
	}
}

class GunBullet(activity: MainActivity) : Transport(1f, Vector2f(activity.transport.location), maxVelocity = 10000f, maxAcceleration = 0f, color = Color.BLACK, size = 2f, explode = false) {
	override fun draw(canvas: Canvas, axis: Vector2f) {
		val p1 = location.add(velocity.normalize().mult(size)).add(axis)
		val p2 = location.add(velocity.normalize().mult(size).negate()).add(axis)
		val paint = Paint()
		paint.color = color
		drawLine(p1, p2, canvas, paint)
	}
}