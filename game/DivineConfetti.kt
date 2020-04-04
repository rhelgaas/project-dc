package com.mygdx.game

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Circle
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import java.awt.Button
import javax.swing.plaf.TreeUI
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
    private var numEnemies = 5
    private var menuTTL = 1000
    private var mapping = arrayOf<Vector2>()
    private lateinit var mapSpawn : Vector2
    private var bulletC = mutableListOf<Bullet>()
    private var enemiesC = mutableListOf<Enemy>()
    private var leader = 1000f
    private var bullet : Bullet ? = null
    private var leadEnemy : Enemy? = null
    private var gameTime : Boolean = false
    private var turretAvailable: Boolean = false
    private var level : Int = 1
    private var shape : ShapeRenderer? = null
    override fun create() {
        batch = SpriteBatch()
        this.img = Texture("badlogic.jpg")
        this.core = Core(Vector2(Gdx.graphics.width.toFloat()/2 - 200, Gdx.graphics.height.toFloat()/2))
        this.ilost = KreygGasm(Vector2(Gdx.graphics.width.toFloat()/2, Gdx.graphics.height.toFloat()/2))
        this.leadEnemy = Enemy(Vector2(1000f, 1000f))
        this.gameTime = true
        var shape = ShapeRenderer()
        mapSpawn = Vector2(Gdx.graphics.width.toFloat(), Gdx.graphics.height.toFloat()/2)

        //map, will be moved to configure file
        this.mapping = arrayOf<Vector2>(
                mapSpawn,
                Vector2(mapSpawn.x - 150, mapSpawn.y),
                Vector2(mapSpawn.x - 150,mapSpawn.y + 50),
                Vector2(mapSpawn.x - 400,mapSpawn.y + 50),
                Vector2(mapSpawn.x - 400, mapSpawn.y),
                Vector2(core!!.pos!!.x, core!!.pos!!.y)
        )

        //add to list
        this.entities.add(this.core!!)

        //add turrets
        this.turretSpawn()

        //add enemies
        this.enemySpawn()
    }

    override fun render() {
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        if (gameTime) {
            //update enemy list copy
            this.enemiesC = enemies.toMutableList()
            this.bulletC = bullets.toMutableList()
            //update mouse input
            this.mouseInput()
            //check if turret can fire on target
            this.turretFire()
            //update enemy target
            this.enemyTarget()
            //check if hit on enemies
            this.bulletHit()
            //remove extra bullets
            this.bulletExtra()
            //check if turrets lost target
            this.turretLost()
            //check if core hit
            this.coreHit()
            //update/draw groups
            this.batchUpdate()
        }
        else{
            if(Gdx.input.isButtonPressed(Input.Buttons.LEFT)){
                this.turretAvailable = true
                this.gameTime = true
                this.level += 1
                this.enemySpawn()
            }
            this.batchUpdate()
        }

    }
    private fun batchUpdate(){
        batch!!.begin()
        /*this.shape!!.projectionMatrix(Gdx.graphics.)
        this.shape!!.begin()
        this.shape!!.rectLine(mapping[1], mapping[2], 2f)
        this.shape!!.end()
*/
        for(entity in entities){
            entity.update()
            batch!!.draw(entity.img, entity.pos!!.x, entity.pos!!.y)

        }
        /*
        if (core!!.isDead()) {
            batch!!.draw(ilost!!.img, ilost!!.pos!!.x, ilost!!.pos!!.y)
        }
        */

        if(this.numEnemies <= 0){
            this.gameTime = false
            turretClear()
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

    //spawn enemies
    private fun enemySpawn(){
        var i = 0
        numEnemies = 5 * this.level
        var enemy : Enemy
        while(i < numEnemies){
            enemy = Enemy(Vector2((mapSpawn.x+ 50 *i), mapSpawn.y))
            enemy.target=mapping[enemy.checkpoint]
            enemy.seek(enemy.target)
            this.enemies.add(enemy)
            this.entities.add(enemy)
            i += 1
        }
    }

    private fun enemyTarget(){
        for (e in enemiesC) {
            if (e.circ.contains(e.target)) {
                e.checkpoint += 1
                e.target = mapping[e.checkpoint]
                e.seek(e.target)
                e.hasTarget = true
            }
        }
    }


    private fun turretSpawn(){
        var i = 0
        var turret : Turret
        while(i < numTurrets){
            turret = Turret(Vector2((230 + 50 * i).toFloat(), 200f))
            turrets.add(turret)
            entities.add(turret)
            i += 1
        }

    }

    private fun turretFire(){
        for (t in turrets){
            leader = 10000f
            if (!t.hasTarget && this.numEnemies > 0){
                for(e in this.enemiesC){
                    if (nearestEnemy(t, e, leader))
                        t.target = e
                    leader = t.target.pos!!.dst(e.pos)
                    t.hasTarget = true
                }
            }
            if (t.canFireAt(t.target)&& this.numEnemies > 0){
                bullet = Bullet(t, t.target)
                bullet!!.seek(t.target)
                this.bullets.add(bullet!!)
                this.entities.add(bullet!!)
                t.fireRate = t.cooldown
            }
        }
    }

    private fun turretLost(){
        for(t in this.turrets){
            if(!t.local(t.target)){
                t.hasTarget = false
                t.fireRate = t.cooldown
            }
        }
        turretFire()

    }

    private fun turretClear(){
        for (t in turrets) {
            t.target = this.leadEnemy!!
        }
    }

    private fun coreHit(){
        for (e in this.enemiesC) {
            if (core!!.circ.overlaps(e.circ)){
                core!!.damage(e.power)
                this.enemies.remove(e)
                this.entities.remove(e)
                numEnemies -= 1

            }
        }
    }

    private fun bulletHit() {
        for (b in this.bulletC) {
            for (e in this.enemiesC) {
                if (b.circ.overlaps(e.circ) && this.numEnemies > 0) {
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
    }

    private fun bulletExtra(){

    }

    private fun mouseInput(){
        if(Gdx.input.isButtonPressed(Input.Buttons.LEFT)&&this.turretAvailable) {
            var ypos = Gdx.input.y.toFloat() - Gdx.graphics.height.toFloat()/2

            var turret = Turret(Vector2(Gdx.input.x.toFloat(), Gdx.input.y.toFloat()- ypos *2))
            turrets.add(turret)
            entities.add(turret)
            this.turretAvailable = false
        }
    }

    //when no enemies remains this is called to start next level
    private fun nextLevel(){

    }

}


