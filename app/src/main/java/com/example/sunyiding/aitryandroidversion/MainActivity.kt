package com.example.sunyiding.aitryandroidversion

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import makeObstacles
import MyPanel
import Transport
import com.jme3.math.Vector2f
import ArriveState
import android.graphics.Color
import ObstacleAvoidState
import SeparationState
import AlignmentState
import CohesionState
import EvadeState
import WanderState
import PursuitState
import android.content.pm.ActivityInfo
import android.support.v7.app.AlertDialog
import android.widget.LinearLayout
import GunBullet

class MainActivity : AppCompatActivity() {
	
	val list = makeObstacles()
	lateinit var panel: MyPanel
	lateinit var transport: Transport
	lateinit var state: ArriveState
	lateinit var newList: ArrayList<Transport>
	lateinit var selected: Transport
	lateinit var thread: Thread
	lateinit var battleStart: Thread
	lateinit var print: Thread
	var selectedAverageVelocity=Vector2f(0f,0f)
	var lstColor = Color.BLACK
	var shown = false
	override fun onDestroy() {
		super.onDestroy()
		thread.interrupt()
		battleStart.interrupt()
		print.interrupt()
	}
	
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)
		panel = MyPanel(this, list)
		transport = Transport(1f, Vector2f(200f, 150f), maxAcceleration = 200f, maxVelocity = 150f, color = Color.BLACK)
		state = ArriveState(transport, Vector2f(0f, 0f), SpeedLevel.MIDDLE)
		newList = ArrayList<Transport>()
		val layout = findViewById<LinearLayout>(R.id.main)
		println("layout.width = ${layout.width}")
		println("layout.height = ${layout.height}")
		layout.addView(panel)
		transport.states.add(state)
		transport.states.add(ObstacleAvoidState(transport, list))
		panel.transports.add(transport)
		repeat(70) {
			val new = Transport(1f, Vector2f(3600f * Math.random().toFloat() - 1200f, 2400f * Math.random().toFloat() - 800f), maxAcceleration = 200f, maxVelocity = 150f, color = Color.GREEN)
			val newState = SeparationState(new, newList)
			new.states.add(newState)
			new.states.add(AlignmentState(new, newList))
			new.states.add(CohesionState(new, newList))
			new.states.add(EvadeState(new, transport))
			new.states.add(ObstacleAvoidState(new, list))
			new.states.add(WanderState(new))
			panel.transports.add(new)
			newList.add(new)
		}
		val pursuitList = ArrayList(newList)
		pursuitList.add(transport)
		repeat(3){
			val new = Transport(1f, Vector2f(3600f *Math.random().toFloat()-1200f,2400f*Math.random().toFloat()-800f),maxAcceleration = 100f,maxVelocity = 100f,color = Color.BLUE)
			new.states.add(SeparationState(new,pursuitList))
			new.states.add(ObstacleAvoidState(new, list))
			new.states.add(PursuitState(new, transport))
			val thread=Thread(Runnable {
				while(!new.dead){
					Thread.sleep((Math.random()*2000+333).toLong())
					if(new.location.distanceSquared(transport.location)<1000000){
						val enemyLocation = transport.location
						val bulletVelocity = 500f
						val toEnemy=enemyLocation.add(new.location.negate())
						val angle=toEnemy.angleBetween(transport.velocity)
						val shootAngle=Math.asin(Math.sin(angle.toDouble())*transport.velocity.length()/bulletVelocity)
						val target=toEnemy.normalize().mult(bulletVelocity)
						target.rotateAroundOrigin(shootAngle.toFloat(),false)
						val bullet= GunBullet(this)
						bullet.velocity=target
						bullet.color=Color.BLUE
						bullet.size=4f
						bullet.location=Vector2f(new.location)
						panel.transports.add(bullet)
						val thread = object : Thread() {
							override fun run() {
								Thread.sleep(100)
								bullet.touchable = true
								Thread.sleep(4750)
								bullet.dead = true
							}
						}
						thread.start()
					}
				}
			})
			thread.start()
			panel.transports.add(new)
			pursuitList.add(new)
		}
		selected = newList[0]
		lstColor = selected.color
		thread = object : Thread() {
			override fun run() {
				super.run()
				try {
					while (!isInterrupted) {
						selected.color = lstColor
						synchronized(panel.transports) {
							newList = ArrayList()
							var i = 0
							while (i < panel.transports.size) {
								synchronized(panel.transports[i]) {
									if (panel.transports[i].explode) {
										newList.add(panel.transports[i])
									}
								}
								i++
							}
						}
						if (newList.isEmpty()) {
							if (!shown) {
								runOnUiThread {
									AlertDialog.Builder(this@MainActivity, R.style.MyAlertDialogStyle).setTitle("Game Finished").setMessage("""
You've killed all the enemy planes
${panel.touchWall} of them hit the wall
${panel.touchObstacle} of them hit the wall
${panel.touchEach} of them hit each other and die
You only hit ${panel.bulletHit} of them
""").setPositiveButton(R.string.exit) { p0, p1 -> System.exit(0) }.create().show()
								}
								shown = true
							}
						} else {
							val lstSelected=selected
							selected = newList.minBy { it.location.distanceSquared(this@MainActivity.state.target) }!!
							if(selected===lstSelected){
								selectedAverageVelocity.addLocal(selected.velocity.mult(2f)).multLocal(0.33f)
							}else{
								selectedAverageVelocity=selected.velocity
							}
							lstColor = selected.color
							selected.color = Color.RED
							panel.invalidate()
							Thread.sleep(20)
						}
					}
				} catch (e: InterruptedException) {
					return
				}
			}
		}
		thread.start()
		battleStart =
				object : Thread() {
					override fun run() {
						try {
							Thread.sleep(5000)
							synchronized(panel.transports) {
								for (i in panel.transports) {
									i.touchable = true
								}
							}
						} catch (e: InterruptedException) {
							return
						}
					}
				}
		battleStart.start()
		print =
				object : Thread() {
					override fun run() {
						try {
							while (!isInterrupted) {
								Thread.sleep(5000)
								synchronized(System.out) {
									println("panel = ${panel}")
									println("touchWall = ${panel.touchWall}")
									println("touchEach = ${panel.touchEach}")
									println("touchObstacle = ${panel.touchObstacle}")
									println("bulletHit = ${panel.bulletHit}")
									println("----------------------------------")
								}
							}
						} catch (e: InterruptedException) {
							return
						}
					}
				}
		print.start()
		
	}
	
	var mouseLocation = Vector2f(0f, 0f)
	
	override fun onResume() {
		if (requestedOrientation != ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
			requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
		}
		super.onResume()
	}
}
