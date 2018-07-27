import com.example.sunyiding.aitryandroidversion.R

data class Settings(var enemyAim: Float, var enemyNum: Int, var targetNum: Int, var gunNum: Int, var missileNum: Int, var radiusNum: Int, var superGunNum: Int) {
    fun toHashMap(): HashMap<Int, Float> {
        val map = HashMap<Int, Float>()
        map[R.id.aim] = enemyAim
        map[R.id.enemy_num] = enemyNum.toFloat()
        map[R.id.target_num] = targetNum.toFloat()
        map[R.id.gun_num] = gunNum.toFloat()
        map[R.id.missile_num] = missileNum.toFloat()
        map[R.id.radius_num] = radiusNum.toFloat()
        map[R.id.super_gun_num] = superGunNum.toFloat()
        return map
    }
    private constructor():this(0f,0,0,0,0,0,0)
    companion object {
        fun fromHashMap(map: HashMap<Int, Float>):Settings {
            val settings=Settings()
            settings.enemyAim = map[R.id.aim]!!
            settings.enemyNum = map[R.id.enemy_num]!!.toInt()
            settings.targetNum = map[R.id.target_num]!!.toInt()
            settings.gunNum = map[R.id.gun_num]!!.toInt()
            settings.missileNum = map[R.id.missile_num]!!.toInt()
            settings.radiusNum = map[R.id.radius_num]!!.toInt()
            settings.superGunNum = map[R.id.super_gun_num]!!.toInt()
            return settings
        }
    }
}