package com.mygdx.game

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Circle
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import kotlin.math.E

class DivineConfetti : ApplicationAdapter() {
    private var batch: SpriteBatch? = null
    private var img: Texture? = null
    private var core: Core? = null
    private var ilost: KreygGasm? = null
    private var entities = mutableListOf<Entity>()
    private var turrets = mutableListOf<Turret>()
    private var bullets = mutableListOf<Bullet>()
    private var enemies = mutableListOf<Enemy>()
    private var numTurrets = 2
    private var numEnemies = 10
    private var menuTTL = 1000

    override fun create() {
        batch = SpriteBatch()
        this.img = Texture("badlogic.jpg")
        this.core = Core(Vector2(Gdx.graphics.width.toFloat()/2 - 200, Gdx.graphics.height.toFloat()/2))
        this.ilost = KreygGasm(Vector2(Gdx.graphics.width.toFloat()/2, Gdx.graphics.height.toFloat()/2))


        //add to list
        this.entities.add(this.core!!)
        //add turrets
        var i = 0
        var turret : Turret
        while(i < numTurrets){
            turret = Turret(Vector2((230 + 50 * i).toFloat(), 200f))
            turrets.add(turret)
            entities.add(turret)
            i += 1
        }

        //add enemies
        i = 0
        var enemy : Enemy
        while(i < numEnemies){
            enemy = Enemy(Vector2((500 + 50 *i).toFloat(), Gdx.graphics.height.toFloat()/2))
            this.enemies.add(enemy)
            this.entities.add(enemy)
            i += 1
        }
    }

    override fun render() {
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        var bullet : Bullet
        var leader = 100000f
        val enemiesC = enemies.toMutableList()
        //val turretsC = turrets.toMutableList()

        for (t in turrets){
            if (!t.hasTarget && numEnemies != 0){
                for(e in enemiesC){
                    if (nearestEnemy(t, e, leader))
                        t.target = e
                        leader = t.target.pos!!.dst(e.pos)
                        t.hasTarget = true
                }
            }
            if (t.canFireAt(t.target)){
                bullet = Bullet(t, t.target)
                bullet.seek(t.target)
                this.bullets.add(bullet)
                this.entities.add(bullet)
                t.fireRate = t.cooldown
            }
        }

        //check if turret can fire on target


        //check if hit on enemies
        val bulletC = bullets.toMutableList()
        for (b in bulletC){
            for (e in enemiesC) {
                if (b.circ.overlaps(e.circ)) {
                    e.damage(b.power)
                    bullets.remove(b)
                    entities.remove(b)
                    if (e.isDead()) {
                        e.pos = Vector2(1000f, 1000f)
                        e.update()
                        entities.remove(e)
                        enemies.remove(e)
                        this.numEnemies -= 1
                        for (t in turrets) {
                            t.hasTarget = false
                        }
                    }
                }
            }
        }

        //check if turrets lost target
        for(t in turrets){
            if(!t.local(t.target)){
                t.hasTarget = false
                t.fireRate = t.cooldown
            }
        }

        //check if core hit

        for (e in enemiesC) {
            if (core!!.circ.overlaps(e.circ)){
                core!!.damage(e.power)
                enemies.remove(e)
                entities.remove(e)

            }
        }
        batch!!.begin()


        for(entity in entities){
            entity.update()
            batch!!.draw(entity.img, entity.pos!!.x, entity.pos!!.y)

        }

        if (core!!.isDead()) {
            batch!!.draw(ilost!!.img, ilost!!.pos!!.x, ilost!!.pos!!.y)
            Gdx.app.exit()
        }

        this.batch!!.end()
    }

    override fun dispose() {
        batch!!.dispose()
        this.img!!.dispose()
    }

    private fun nearestEnemy(turret: Turret, enemy: Enemy, leader: Float): Boolean{
        return turret.pos!!.dst(enemy.pos) < leader
    }

}

