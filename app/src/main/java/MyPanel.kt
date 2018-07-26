import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Build
import android.support.annotation.RequiresApi
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import com.example.sunyiding.aitryandroidversion.MainActivity
import com.example.sunyiding.aitryandroidversion.showDialog
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

data class Death(val location: Vector2f, val reason: String, val time: Long)
class MyPanel(val activity: MainActivity, val list: ArrayList<Obstacle>) : View(activity) {
	var touchWall = 0
	var touchEach = 0
	var bulletHit = 0
	var touchObstacle = 0
	val transports = ArrayList<Transport>()
	val deaths = ArrayList<Death>()
	var lstUpdate = System.currentTimeMillis()
	val axis = Vector2f(0f, 0f)
	var start = true
	fun outOfSight(transport: Transport): Boolean {
		val screenLocation = transport.location.add(axis)
		return screenLocation.x < 0 || screenLocation.x > width || screenLocation.y < 0 || screenLocation.y > height
	}
	
	@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
	override fun onDraw(canvas: Canvas?) {
		if (canvas != null) {
			val time = System.currentTimeMillis()
			val tpf = time - lstUpdate
			
			//Repaint
			val paint = Paint()
			paint.color = Color.WHITE
			canvas.drawRect(0f, 0f, canvas.width.toFloat(), canvas.height.toFloat(), paint)
			//Draw obstacles
			for (i in list) {
				val location = i.location.add(axis)
				paint.color = i.color
				canvas.drawOval((location.x - i.radius), (location.y - i.radius), location.x + i.radius, location.y + i.radius, paint)
			}
			//Draw walls
			paint.color = Color.GRAY
			canvas.drawRect(-1200 + axis.x - 50, -800 + axis.y - 50, axis.x + 3700 - 1200 - 50, axis.y - 800, paint)
			canvas.drawRect(-1200 + axis.x - 50, 1600 + axis.y, axis.x - 1200 - 50 + 3700, axis.y + 1650, paint)
			canvas.drawRect(-1200 + axis.x - 50, -800 + axis.y - 50, axis.x - 1200, axis.y - 850 + 2500, paint)
			canvas.drawRect(2400 + axis.x, -800 + axis.y - 50, axis.x + 2450, axis.y - 800 - 50 + 2500, paint)
			paint.color = Color.BLACK
			lstUpdate = time
			//Draw and update transports
			synchronized(transports) {
				for (i in 0 until transports.size) {
					val transport = transports[i]
					if (start)
						transport.update(tpf)
					transport.draw(canvas, axis)
					if (transport.location.x < -1200 || transport.location.x > 2400 || transport.location.y < -800 || transport.location.y > 1600) {
						transport.dead = true
						if (transport.explode) {
							explosion(this)
							deaths.add(Death(transport.location, "撞墙", System.currentTimeMillis() + 2000))
							touchWall++
						}
					}
					for (obstacle in list) {
						if (transport.location.distance(obstacle.location) < 2f + obstacle.radius) {
							transport.dead = true
							if (transport.explode) {
								explosion(this)
								deaths.add(Death(transport.location, "撞球", System.currentTimeMillis() + 2000))
								touchObstacle++
							}
						}
					}
				}
			}
			//Check touching
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
										deaths.add(Death(transports[i].location, "互撞", System.currentTimeMillis() + 2000))
										touchEach++
									} else if (transports[i].explode || transports[j].explode) {
										deaths.add(Death(transports[i].location, "射中", System.currentTimeMillis() + 2000))
										bulletHit++
									}
								}
						}
				}
			}
			//Remove Death
			var i = 0
			while (i < transports.size) {
				if (transports[i].dead) {
					transports.removeAt(i)
				}
				i++
			}
			//Draw target
			if (transports[0].states[0] is ArriveState) {
				paint.color = Color.RED
				val v = (transports[0].states[0] as ArriveState).target.add(axis)
				canvas.drawOval(v.x - 3, v.y - 3, v.x + 3, v.y + 3, paint)
			} else {
				if (start) {
					start = false
					repeat(5) { explosion(this) }
					showDialog(this,activity)
				}
			}
			//Draw death tips
			paint.color = Color.CYAN
			paint.textSize = 20f
			synchronized(deaths) {
				var i = 0
				while (i < deaths.size) {
					canvas.drawText(deaths[i].reason, deaths[i].location.x + axis.x - 20f, deaths[i].location.y + axis.y - 10f, paint)
					if (deaths[i].time < System.currentTimeMillis()) {
						deaths.removeAt(i)
					}
					i++
				}
			}
			//Draw tips
			paint.color = Color.BLACK
			paint.textSize = 50f
			canvas.drawText("Weapon : $weapon", 0f, 50f, paint)
			canvas.drawText("Bullet : ${weapon.bulletLeft}", 0f, 100f, paint)
			canvas.drawText("Left   : ${activity.size}", 0f, 150f, paint)
			/*g.color= Color.GREEN
			val v2=(transports[1].states[0] as PursuitState).seekTarget.add(axis)
			g.drawOval(v2.x.toInt() - 2, v2.y.toInt() - 2, 4, 4)*/
			showNear(canvas)
		}
	}
	
	var lstL = Vector2f(0f, 0f)
	var lstR = Vector2f(0f, 0f)
	var cd = false
	
	enum class Weapon(val value: Int) {
		GUN(0), MISSILE(1), RADIUS(2), SUPER_GUN(3);
		
		var bulletLeft = 10
		fun next(): Weapon = values()[if (ordinal + 2 <= values().size) ordinal + 1 else 0]
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
				if (weapon.bulletLeft > 0) {
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
							bullet.velocity = activity.transport.velocity.add(activity.transport.velocity.normalize().mult(1500f))
							transports.add(bullet)
							val thread = object : Thread() {
								override fun run() {
									cd = true
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
							val toEnemy = enemyLocation.add(activity.transport.location.negate())
							val angle = toEnemy.angleBetween(activity.selectedAverageVelocity)
							val shootAngle = Math.asin(Math.sin(angle.toDouble()) * activity.selectedAverageVelocity.length() / bulletVelocity)
							val target = toEnemy.normalize().mult(bulletVelocity)
							target.rotateAroundOrigin(shootAngle.toFloat(), false)
							val bullet = GunBullet(activity)
							bullet.velocity = target
							bullet.color = Color.BLUE
							bullet.size = 4f
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
					weapon.bulletLeft--
					if (weapon.bulletLeft == 0) {
						Toast.makeText(activity, "$weapon is out of bullet", Toast.LENGTH_SHORT).show()
					}
				}
			}
		}
		return true
	}
	
	@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
	/*private fun showNear(canvas: Canvas) {
		synchronized(transports) {
			val outside = transports.filter {
				val local = it.location.add(axis)
				local.x < 0 || local.x > width || local.y < 0 || local.y > height
			}
			if (outside.size == transports.size)
				return
			val upSide = ArrayList<Pair<Transport, Float>>()
			val downSide = ArrayList<Pair<Transport, Float>>()
			val leftSide = ArrayList<Pair<Transport, Float>>()
			val rightSide = ArrayList<Pair<Transport, Float>>()
			outside.forEach {
				val local = it.location.add(axis)
				val toLeft = 0 - local.x
				val toRight = local.x - width
				val toUp=0-local.y
				val toDown = local.y - height
				val array= arrayOf(toLeft,toRight,toUp,toDown)
				val min=array.filter { it>0 }.min()
				when (min) {
					toLeft -> leftSide.add(Pair(it, min))
					toRight -> rightSide.add(Pair(it, min))
					toUp -> upSide.add(Pair(it,min))
					toDown -> downSide.add(Pair(it,min))
				}
			}
			if (transports.size == upSide.size + downSide.size + leftSide.size + rightSide.size)
				return
			val lowUp = upSide.minBy { it.second }
			val lowDown = downSide.minBy { it.second }
			val lowLeft = leftSide.minBy { it.second }
			val lowRight = rightSide.minBy { it.second }
			val low = arrayOf(lowUp, lowDown, lowLeft, lowRight).minBy { it?.second ?: 10000000f }
			val relativeLocation = low!!.first.location + axis + Vector2f(width / 2f, height / 2f)
			val target = when (low) {
				lowUp -> {
					val angle = relativeLocation.angleBetween(Vector2f(0f, 1f)).toDouble()
					Vector2f(width / 2 + (height / 2) * Math.tan(angle).toFloat(), 0f)
				}
				lowDown -> {
					val angle = relativeLocation.angleBetween(Vector2f(0f, -1f)).toDouble()
					Vector2f(width / 2 + (height / 2) * Math.tan(angle).toFloat(), height.toFloat())
				}
				lowLeft -> {
					val angle = relativeLocation.angleBetween(Vector2f(-1f, 0f)).toDouble()
					Vector2f(0f, height / 2 + (width / 2) * Math.tan(angle).toFloat())
				}
				lowRight -> {
					val angle = relativeLocation.angleBetween((Vector2f(1f, 0f))).toDouble()
					Vector2f(width.toFloat(), height / 2 + (width / 2) * Math.tan(angle).toFloat())
				}
				else -> Vector2f(0f, 0f)
			}
			val paint = Paint()
			paint.color = Color.RED
			canvas.drawOval(target.x - 20f, target.y - 20f, target.x + 20f, target.y + 20f, paint)
		}
	}*/
	private fun showNear(canvas: Canvas) {
		val location = activity.selected.location+axis
		if (!(location.x < 0 || location.x > width || location.y < 0 || location.y > height)) {
			return
		}
		val direction = (activity.selected.location - activity.state.target).normalize()
		val p1 = activity.state.target + axis + direction * 20f
		val p2 = p1 + direction *(activity.selected.location.distance(activity.state.target)/20)
		val paint = Paint()
		paint.color = Color.BLUE
		canvas.drawLine(p1.x, p1.y, p2.x, p2.y, paint)
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