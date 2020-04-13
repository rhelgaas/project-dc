package com.mygdx.game

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.maps.objects.RectangleMapObject
import com.badlogic.gdx.math.Circle
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.sun.org.apache.xpath.internal.operations.Bool
import java.awt.Button
import java.beans.VetoableChangeListener
import javax.swing.plaf.TreeUI
import kotlin.math.E
import kotlin.random.Random

class DivineConfetti : ApplicationAdapter() {
    private var batch: SpriteBatch? = null
    private var img: Texture? = null
    private var core: Core? = null
    private var ilost: KreygGasm? = null
    private var entities = mutableListOf<Entity>()
    private var turrets = mutableListOf<Turret>()
    private var bullets = mutableListOf<Bullet>()
    private var enemies = mutableListOf<Enemy>()
    private var numTurrets = 0
    private var numEnemies = 0
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
    private var level : Int = 0
    private var shape : ShapeRenderer? = null
    private var showMap : Boolean = true
    private var camera : OrthographicCamera? = null
    private var placeTurret = mutableListOf<Rectangle>()
    private var placableTurret : Int = 0
    private var loot : Loot? = null
    private var loots = mutableListOf<Loot>()
    private var lootsC = mutableListOf<Loot>()
    private var chooseScreen = Vector2(100f, 20f)
    private var select : Boolean = true
    private var turret : Turret? = null
    private var availableTurrets = arrayOf<Turret>()

    override fun create() {
        batch = SpriteBatch()
        this.img = Texture("badlogic.jpg")
        this.core = Core(Vector2(Gdx.graphics.width.toFloat()/2 - 200, Gdx.graphics.height.toFloat()/2))
        this.ilost = KreygGasm(Vector2(Gdx.graphics.width.toFloat()/2, Gdx.graphics.height.toFloat()/2))
        this.leadEnemy = Enemy(Vector2(1000f, 1000f))
        this.gameTime = false
        this.camera = OrthographicCamera()
        shape = ShapeRenderer()
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

        //placebla surface for turrets
        this.placeTurret.add(Rectangle(this.core!!.pos!!.x, this.core!!.pos!!.y + 70, 500f, 50f))
        this.placeTurret.add(Rectangle(this.core!!.pos!!.x, this.core!!.pos!!.y - 70, 500f, 50f))

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
        this.enemiesC = enemies.toMutableList()
        this.bulletC = bullets.toMutableList()
        this.lootsC = loots.toMutableList()
        if (gameTime) {
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
            this.mouseInput(Vector2(Gdx.input.x.toFloat(), (Gdx.input.y.toFloat() + (Gdx.input.y.toFloat() - Gdx.graphics.height.toFloat()/2)*-2)))

            if(this.numEnemies <= 0){
                this.gameTime = false
                this.select = true
                turretClear()
            }


        }
        else{
            this.turretAvailable = true
            if(Gdx.input.isButtonPressed(Input.Buttons.LEFT)or Gdx.input.isButtonPressed(Input.Buttons.RIGHT)){
                this.mouseInput(Vector2(Gdx.input.x.toFloat(), Gdx.input.y.toFloat() + (Gdx.input.y.toFloat() - Gdx.graphics.height.toFloat()/2)*-2))
            }
            if(!turretAvailable){
                this.gameTime = true
                this.level += 1
                this.enemySpawn()
            }

            this.batchUpdate()

        }
        if (showMap){
            shape!!.begin(ShapeRenderer.ShapeType.Line)
            shape!!.color = Color.WHITE
            shape!!.rectLine(mapping[0], mapping[1], 5f)
            shape!!.rectLine(mapping[1], mapping[2], 5f)
            shape!!.rectLine(mapping[2], mapping[3], 5f)
            shape!!.rectLine(mapping[3], mapping[4], 5f)
            shape!!.rectLine(mapping[4], mapping[5], 5f)
            for (r in placeTurret){
                shape!!.rect(r.x, r.y, r.width, r.height)
            }
            for (t in turrets){
                shape!!.circle(t.pos!!.x, t.pos!!.y, t.range.toFloat())
            }
            if (turretAvailable){
                shape!!.rect( 20f, Gdx.graphics.height.toFloat() - 20, 10f, 10f)
            }

            shape!!.end()

        }

        if(select){
            this.chooseTurret()
        }

    }
    private fun batchUpdate(){
        batch!!.begin()

        for(entity in entities){
            entity.update()
            batch!!.draw(entity.img, entity.pos!!.x, entity.pos!!.y)

        }
        /*
        if (core!!.isDead()) {
            batch!!.draw(ilost!!.img, ilost!!.pos!!.x, ilost!!.pos!!.y)
        }
        */
        this.batch!!.end()
    }
    override fun dispose() {
        batch!!.dispose()
        this.img!!.dispose()
        shape!!.dispose()
    }

    private fun nearestEnemy(turret: Turret, enemy: Enemy, leader: Float): Boolean{
        return turret.pos!!.dst(enemy.pos) < leader
    }



    //spawn enemies
    private fun enemySpawn(){
        var i = 0
        numEnemies = 2 * this.level
        var enemy : Enemy
        while(i < numEnemies){
            if(i.rem(5)== 0) {
                enemy = EnemyB(Vector2((mapSpawn.x + 50 * i), mapSpawn.y))

            }
            else{
                enemy = Enemy(Vector2((mapSpawn.x + 50 * i), mapSpawn.y))
            }
            enemy.target = mapping[enemy.checkpoint]
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
        while(i < numTurrets){
            this.turret = Turret(Vector2((230 + 50 * i).toFloat(), 200f))
            turrets.add(this.turret!!)
            entities.add(this.turret!!)
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

                if(t.ammo == "dmgA") {
                    this.bullet = Bullet(t, t.target)
                }
                if(t.ammo == "slowA") {
                    this.bullet = BulletS(t, t.target)
                }

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
                    b.dmg(e)
                    bullets.remove(b)
                    entities.remove(b)
                    if (e.isDead()) {
                        val rnd = roll()
                        println(rnd)
                        if(rnd == 9) {
                            this.loot = Loot(e)
                            loots.add(this.loot!!)
                            entities.add(this.loot!!)
                        }
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

    private fun mouseInput(clickPos: Vector2){

        if(Gdx.input.isButtonPressed(Input.Buttons.LEFT)&&this.turretAvailable && placeable(clickPos)) {
            val turret = Turret(clickPos)
            turrets.add(turret)
            entities.add(turret)
            this.turretAvailable = false
        }
        if(Gdx.input.isButtonPressed(Input.Buttons.RIGHT)&&this.turretAvailable && placeable(clickPos)){
            val turret = TurretS(clickPos)
            turrets.add(turret)
            entities.add(turret)
            this.turretAvailable = false
        }

        for (l in lootsC) {
            if((Gdx.input.isButtonPressed(Input.Buttons.LEFT)&&l.circ.contains(clickPos))) {
                println(1)
                turretAvailable = true
                loots.remove(l)
                entities.remove(l)
            }
        }

    }

    private fun placeable(pos:Vector2):Boolean{
        return (placeTurret[0].contains(pos)||placeTurret[1].contains(pos))
    }

    //when no enemies remains this is called to start next level
    private fun nextLevel(){

    }

    private fun roll(): Int {
        return (0..10).random()
    }

    private fun chooseTurret(){
        var rnd: Int
        var i = 0
        if(this.select) {
            while (i < 3) {
                println("hello")
                rnd = roll()
                when (rnd) {
                    0,1,2,3,4 -> this.turret = Turret(chooseScreen)
                    5,6,7,8,9,10 -> this.turret = TurretS(chooseScreen)
                }
                entities.add(this.turret!!)
                this.chooseScreen.x += 50 *i
                i += 1
            }
        }
        this.select = false
    }

}


