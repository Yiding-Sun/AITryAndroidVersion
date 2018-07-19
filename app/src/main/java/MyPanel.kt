import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.MotionEvent
import android.view.View
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
			for (transport in transports) {
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
								} else {
									bulletHit++
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
	override fun onTouchEvent(event: MotionEvent?): Boolean {
		when (event!!.action) {
			MotionEvent.ACTION_DOWN -> {
				if (event.x > width / 2)
					lstR = Vector2f(event.x, event.y).add(axis.negate())
				else
					lstL = Vector2f(event.x, event.y).add(axis.negate())
				return true
			}
			MotionEvent.ACTION_MOVE -> {
				if (event.x > width / 2) {
					for (i in 0 until event.historySize) {
						val new = Vector2f(event.getHistoricalX(i), event.getHistoricalY(i)).add(axis.negate())
						val move = lstR.add(new.negate())
						axis.addLocal(move.negate())
						lstR = new
					}
					return true
				} else {
					for (i in 0 until event.historySize) {
						val new = Vector2f(event.getHistoricalX(i), event.getHistoricalY(i)).add(axis.negate())
						val move = lstL.add(new.negate())
						activity.state.target.addLocal(move.negate())
						lstL = new
					}
					return true
				}
			}
		}
		return true
	}
	
}