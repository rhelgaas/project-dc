package com.mygdx.game

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Circle
import com.badlogic.gdx.math.Intersector
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.BufferUtils.copy
import com.sun.awt.SecurityWarning.setPosition
import com.sun.xml.internal.messaging.saaj.util.MimeHeadersUtil.copy

import java.util.*
import javax.xml.stream.FactoryConfigurationError
import kotlin.math.sqrt


interface Entity  {
    var pos :  Vector2?
    var img : Texture?
    var speed : Vector2?
    var health : Int

    fun update()= this.move()

    fun move(){
        this.pos!!.add(this.speed)
    }

}
//this.img = Texture("star.png")
//Vector2(Gdx.graphics.width.toFloat()/2, Gdx.graphics.height.toFloat()/2)

class Core(override var pos: Vector2?):Entity{
    override var img: Texture? = Texture("star.png")
    override var speed: Vector2? = Vector2(0f,0f)

    var circ = Circle()
    override var health = 100

    override fun update() {
        circ.set(this.pos, 10f)
        super.update()
    }

    fun damage(dmg:Int){
        this.health -= dmg
    }

    fun isDead():Boolean{
        return this.health <= 0
    }
}

class KreygGasm(override var pos: Vector2?):Entity{
    override var img: Texture? = Texture("yikes.png")
    override var health: Int = 0
    override var speed: Vector2? = Vector2(0f, 0f)
}

class Turret(override var pos: Vector2?):Entity{
    override var img: Texture? = Texture("turret.png")
    override var speed: Vector2? = Vector2(0f, 0f)
    override var health = 100
    lateinit var target:  Enemy


    var range = 200
    var cooldown = 10
    var fireRate = this.cooldown

    var hasTarget = false

    override fun update() {
        super.update()
        this.fireRate -= 1
    }

    fun local(enemy: Enemy):Boolean{
        return enemy.pos!!.dst(this.pos) < this.range
    }

    fun canFireAt(enemy: Enemy): Boolean{
        return local(enemy)  && fireRate <= 0
    }



}

class Enemy(override var pos: Vector2?):Entity{
    override var img: Texture? = Texture("enemy.png")
    override var speed: Vector2? = Vector2(-4f, 0f)
    override var health = 100

    var circ = Circle()
    var power = 10

    override fun update() {
        this.circ.set(this.pos, 10f)
        super.update()
    }

    fun damage(dmg: Int){
        this.health -= dmg

    }
    fun isDead():Boolean{
        return this.health <= 0
    }

}

class Bullet(var turret: Turret, var target: Enemy?) :Entity {

    override var pos: Vector2? = copyVec(turret.pos)
    override var img : Texture? = Texture("bullet.png")
    override var speed : Vector2?  = Vector2(0f, 0f)
    override var health = 100
    var maxSpeed = 10f
    var circ = Circle()
    var power = 2

    override fun update(){
        this.circ.set(this.pos, 10f)
        super.update()
    }

    override fun move() {
        this.speed!!.limit(this.maxSpeed)
        this.pos!!.add(speed!!)

    }

    fun seek(target: Enemy){
        var des = Vector2(target.pos!!.x - this.pos!!.x, target.pos!!.y - this.pos!!.y)

        this.speed = this.speed!!.add(des.scl(this.maxSpeed))
    }

}


fun copyVec(vec : Vector2?):Vector2{

    return vec!!.cpy()

}