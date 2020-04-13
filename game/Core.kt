package com.mygdx.game

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.ParticleEffect
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Circle
import com.badlogic.gdx.math.Intersector
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.BufferUtils.copy
import com.sun.awt.SecurityWarning.setPosition
import com.sun.org.apache.xml.internal.security.encryption.CipherReference
import com.sun.xml.internal.messaging.saaj.util.MimeHeadersUtil.copy

import java.util.*
import javax.xml.soap.Text
import javax.xml.stream.FactoryConfigurationError
import kotlin.math.sqrt


//this.img = Texture("star.png")
//Vector2(Gdx.graphics.width.toFloat()/2, Gdx.graphics.height.toFloat()/2)
open class Entity{
    open var pos : Vector2? = null
    open var img : Texture? = null
    open fun update(){
    }
}

class Core(override var pos: Vector2?): Entity() {
    override var img: Texture? = Texture("star.png")
    var speed: Vector2 = Vector2(0f,0f)


    var circ = Circle()
    var health = 200

    override fun update() {
        circ.set(this.pos, 10f)
        this.pos!!.add(this.speed)
    }

    fun damage(dmg:Int){
        this.health -= dmg
    }

    fun isDead():Boolean{
        return this.health <= 0
    }
}

class KreygGasm(override var pos: Vector2?): Entity() {
    override var img: Texture? = Texture("yikes.png")
}

open class Turret(final override var pos: Vector2?): Entity() {
    override var img: Texture? = Texture("turret.png")
    var speed: Vector2 = Vector2(0f, 0f)
    var health = 100
    lateinit var target: Enemy
    open var ammo : String ="dmgA"


    var range = 150
    var rangeCirc = Circle().set(this.pos, this.range.toFloat())
    open var cooldown = 10
    var fireRate = this.cooldown
    var hasTarget = false

    override fun update() {
        this.fireRate -= 1
    }

    fun local(enemy: Enemy):Boolean{
        return enemy.pos!!.dst(this.pos) < this.range
    }

    fun canFireAt(enemy: Enemy): Boolean{
        return local(enemy) && fireRate <= 0
    }



}

class TurretS(pos: Vector2): Turret(pos) {
    override var img: Texture? = Texture("turretS.png")
    override var ammo = "slowA"
    override var cooldown = 10
}

open class Enemy(override var pos: Vector2?): Entity() {
    override var img: Texture? = Texture("enemy.png")
    open var speed: Vector2 = Vector2(0f, 0f)
    open var health = 100
    lateinit var target : Vector2
    open var maxSpeed : Float = 1.5f


    var hasTarget = false
    var circ = Circle()
    var power = 10
    var checkpoint = 1

    override fun update() {
        this.circ.set(this.pos, 4f)
        this.pos!!.add(this.speed)
    }

    fun isDead():Boolean{
        return this.health <= 0
    }
    fun seek(target: Vector2){
        val des = Vector2(target.x - this.pos!!.x, target.y - this.pos!!.y)
        des.limit(this.maxSpeed)
        this.speed = des
    }
}

class EnemyB(pos: Vector2): Enemy(pos){
    override var img: Texture? = Texture("enemyb.png")
    override var health: Int = 200
    override var maxSpeed: Float = 1f


}

open class Bullet(var turret: Turret, var target: Enemy): Entity() {

    override var pos: Vector2? = copyVec(turret.pos)
    override var img : Texture? = Texture("bullet.png")
    var speed : Vector2  = Vector2(0f, 0f)
    var health = 100
    var maxSpeed: Float = 20f
    var circ = Circle()
    var power = 10
    var ttl = 50

    override fun update(){
        this.circ.set(this.pos, 10f)
        this.ttl -= 1
        this.move()
    }

    fun move() {
        this.speed.limit(this.maxSpeed)
        this.pos!!.add(speed)

    }

    fun seek(target: Enemy){
        val des = Vector2(target.pos!!.x - this.pos!!.x, target.pos!!.y - this.pos!!.y)

        this.speed = this.speed.add(des.scl(this.maxSpeed))
    }

    open fun dmg(target: Enemy){
        target.health -= this.power
    }

    fun isDead():Boolean {
        return this.ttl < 0
    }

}
class BulletS(turret: Turret, target: Enemy):Bullet(turret, target){
    override var img: Texture? = Texture("bullets.png")
    var slow = 0.20
    override fun dmg(target: Enemy){
        target.maxSpeed -= (target.maxSpeed * this.slow).toFloat()
        target.seek(target.target)
    }
}


class Loot(entity: Entity): Entity(){
    override var img: Texture? = Texture("loot.png")
    override var pos: Vector2? = entity.pos
    var circ : Circle = Circle()


    override fun update() {
        this.circ.set(this.pos, 10f)
        super.update()
    }
}


fun copyVec(vec : Vector2?):Vector2{

    return vec!!.cpy()

}
