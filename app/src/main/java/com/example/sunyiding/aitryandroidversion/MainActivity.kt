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
import Settings
import android.annotation.SuppressLint
import android.graphics.Canvas
import android.graphics.Paint
import android.os.Handler
import android.widget.Button
import android.widget.Spinner

class MainActivity : AppCompatActivity() {
	
	private val list = makeObstacles()
	private val settings: Settings = Settings(1 / 9f, 10, 100, 500, 100, 2000, 200)
	private lateinit var panel: MyPanel
	lateinit var transport: Transport
	lateinit var state: ArriveState
	lateinit var newList: ArrayList<Transport>
	lateinit var selected: Transport
	private lateinit var thread: Thread
	private lateinit var battleStart: Thread
	private lateinit var print: Thread
	var selectedAverageVelocity = Vector2f(0f, 0f)
	var lstColor = Color.BLACK
	var shown = false
	var size = 0
	private var handler = Handler(Handler.Callback {
		if (it.what == 0) {
			panel = MyPanel(this, list)
			MyPanel.Weapon.GUN.bulletLeft = settings.gunNum
			MyPanel.Weapon.MISSILE.bulletLeft = settings.missileNum
			MyPanel.Weapon.RADIUS.bulletLeft = settings.radiusNum
			MyPanel.Weapon.SUPER_GUN.bulletLeft = settings.superGunNum
			transport = Transport(1f, Vector2f(600f, 400f), maxAcceleration = 250f, maxVelocity = 200f, color = Color.BLACK, explode = false)
			state = ArriveState(transport, Vector2f(600f, 400f), SpeedLevel.MIDDLE)
			newList = ArrayList()
			val layout = findViewById<LinearLayout>(R.id.main)
			println("layout.width = ${layout.width}")
			println("layout.height = ${layout.height}")
			layout.addView(panel)
			transport.states.add(state)
			transport.states.add(ObstacleAvoidState(transport, list))
			panel.transports.add(transport)
			repeat(settings.targetNum) {
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
			repeat(settings.enemyNum) {
				val new = object : Transport(1f, Vector2f(3600f * Math.random().toFloat() - 1200f, 2400f * Math.random().toFloat() - 800f), maxAcceleration = 100f, maxVelocity = 100f, color = Color.BLUE) {
					override fun draw(canvas: Canvas, axis: Vector2f) {
						super.draw(canvas, axis)
						val paint = Paint()
						paint.color = color
						val x = heading.normalize()
						val y = Vector2f(-x.y, x.x)
						val p1 = location.add(axis)
						val p2 = x.negate().add(y).mult(size).add(location).add(axis)
						val p3 = x.negate().add(y.negate()).mult(size).add(location).add(axis)
						drawLine(p1, p2, canvas, paint)
						drawLine(p1, p3, canvas, paint)
					}
				}
				new.states.add(SeparationState(new, pursuitList))
				new.states.add(ObstacleAvoidState(new, list))
				new.states.add(PursuitState(new, transport))
				val thread = Thread(Runnable {
					while (!new.dead) {
						Thread.sleep((Math.random() * 2000 + 333).toLong())
						if (new.location.distanceSquared(transport.location) < 100000) {
							val enemyLocation = transport.location
							val bulletVelocity = 500f
							val toEnemy = enemyLocation.add(new.location.negate())
							val angle = toEnemy.angleBetween(transport.velocity)
							//make some error
							val shootAngle = Math.asin(Math.sin(angle.toDouble()) * transport.velocity.length() / bulletVelocity) + Math.random() * Math.PI * settings.enemyAim
							val target = toEnemy.normalize().mult(bulletVelocity)
							target.rotateAroundOrigin(shootAngle.toFloat(), false)
							val bullet = GunBullet(this)
							bullet.velocity = target
							bullet.color = Color.BLUE
							bullet.size = 4f
							bullet.location = Vector2f(new.location)
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
			selected = pursuitList[0]
			lstColor = selected.color
			thread = object : Thread() {
				override fun run() {
					super.run()
					try {
						while (!isInterrupted) {
							selected.color = lstColor
							synchronized(pursuitList) {
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
							size = newList.size
							if (newList.isEmpty()) {
								if (!shown) {
									runOnUiThread {
										AlertDialog.Builder(this@MainActivity, R.style.MyAlertDialogStyle).setTitle("Game Finished").setMessage("""
You've killed all the enemy planes
${panel.touchWall} of them hit the wall
${panel.touchObstacle} of them hit the wall
${panel.touchEach} of them hit each other and die
You only hit ${panel.bulletHit} of them
""").setPositiveButton(R.string.exit) { _, _ -> System.exit(0) }.create().show()
									}
									shown = true
								}
							} else {
								val lstSelected = selected
								selected = newList.minBy { it.location.distanceSquared(this@MainActivity.state.target) }!!
								if (selected === lstSelected) {
									selectedAverageVelocity.addLocal(selected.velocity.mult(2f)).multLocal(0.33f)
								} else {
									selectedAverageVelocity = selected.velocity
								}
								lstColor = selected.color
								selected.color = Color.RED
								panel.invalidate()
								Thread.sleep(15)
							}
						}
					} catch (e: InterruptedException) {
						return
					}
				}
			}
			thread.start()
			battleStart = object : Thread() {
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
			print = object : Thread() {
				override fun run() {
					try {
						while (!isInterrupted) {
							Thread.sleep(5000)
							synchronized(System.out) {
								println("panel = $panel")
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
		false
	})
	
	/*override fun onDestroy() {
		super.onDestroy()
		thread.interrupt()
		battleStart.interrupt()
		print.interrupt()
	}*/
	
	@SuppressLint("InflateParams")
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)
		val dialogView = layoutInflater.inflate(R.layout.dialog_layout, null)
		val dialog = AlertDialog.Builder(this).setTitle("Settings").setView(dialogView).create()
		dialog.show()
		dialogView.findViewById<Button>(R.id.finished).setOnClickListener {
			settings.enemyAim = when (dialogView.findViewById<Spinner>(R.id.aim).selectedItemPosition) {
				0 -> 1 / 6f
				1 -> 1 / 9f
				2 -> 0f
				else -> 0f
			}
			settings.enemyNum = when (dialogView.findViewById<Spinner>(R.id.enemy_num).selectedItemPosition) {
				0 -> 10
				1 -> 5
				2 -> 3
				3 -> 0
				else -> 0
			}
			settings.targetNum = when (dialogView.findViewById<Spinner>(R.id.target_num).selectedItemPosition) {
				0 -> 100
				1 -> 70
				2 -> 50
				else -> 0
			}
			settings.gunNum = when (dialogView.findViewById<Spinner>(R.id.gun_num).selectedItemPosition) {
				0 -> 300
				1 -> 200
				2 -> 100
				else -> 0
			}
			settings.missileNum = when (dialogView.findViewById<Spinner>(R.id.missile_num).selectedItemPosition) {
				0 -> 100
				1 -> 50
				2 -> 30
				else -> 0
			}
			settings.radiusNum = when (dialogView.findViewById<Spinner>(R.id.radius_num).selectedItemPosition) {
				0 -> 700
				1 -> 500
				2 -> 300
				else -> 0
			}
			settings.superGunNum = when (dialogView.findViewById<Spinner>(R.id.super_gun_num).selectedItemPosition) {
				0 -> 100
				1 -> 50
				2 -> 30
				else -> 0
			}
			handler.sendEmptyMessage(0)
			dialog.dismiss()
		}
	}
	
	
	override fun onResume() {
		if (requestedOrientation != ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
			requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
		}
		super.onResume()
	}
}
