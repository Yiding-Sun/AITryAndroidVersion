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
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.support.constraint.ConstraintLayout
import android.view.MotionEvent
import android.view.Window
import android.view.WindowManager
import android.widget.LinearLayout

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
	var lstColor = Color.BLACK
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
		selected = newList[0]
		lstColor = selected.color
		thread = object : Thread() {
			override fun run() {
				super.run()
				try {
					while (!isInterrupted) {
						selected.color = lstColor
						//synchronized(panel.transports) {
							newList = ArrayList(panel.transports.filter { it.color == Color.GREEN })
						//}
						selected = newList.minBy { it.location.distanceSquared(this@MainActivity.state.target) }!!
						lstColor = selected.color
						selected.color = Color.RED
						panel.invalidate()
						Thread.sleep(20)
					}
				}catch (e:InterruptedException){
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
